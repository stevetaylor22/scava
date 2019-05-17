package org.eclipse.scava.metricprovider.trans.documentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.scava.metricprovider.trans.documentation.model.Documentation;
import org.eclipse.scava.metricprovider.trans.documentation.model.DocumentationEntry;
import org.eclipse.scava.metricprovider.trans.documentation.model.DocumentationTransMetric;
import org.eclipse.scava.nlp.tools.plaintext.PlainTextObject;
import org.eclipse.scava.nlp.tools.plaintext.documentation.PlainTextDocumentationMarkdownBased;
import org.eclipse.scava.nlp.tools.plaintext.documentation.PlainTextDocumentationOthers;
import org.eclipse.scava.nlp.tools.preprocessor.fileparser.FileParser;
import org.eclipse.scava.platform.IMetricProvider;
import org.eclipse.scava.platform.ITransientMetricProvider;
import org.eclipse.scava.platform.MetricProviderContext;
import org.eclipse.scava.platform.delta.ProjectDelta;
import org.eclipse.scava.platform.delta.vcs.PlatformVcsManager;
import org.eclipse.scava.platform.delta.vcs.VcsChangeType;
import org.eclipse.scava.platform.delta.vcs.VcsCommit;
import org.eclipse.scava.platform.delta.vcs.VcsCommitItem;
import org.eclipse.scava.platform.delta.vcs.VcsProjectDelta;
import org.eclipse.scava.platform.delta.vcs.VcsRepositoryDelta;
import org.eclipse.scava.platform.logging.OssmeterLogger;
import org.eclipse.scava.platform.vcs.workingcopy.manager.WorkingCopyCheckoutException;
import org.eclipse.scava.platform.vcs.workingcopy.manager.WorkingCopyFactory;
import org.eclipse.scava.platform.vcs.workingcopy.manager.WorkingCopyManagerUnavailable;
import org.eclipse.scava.repository.model.Project;
import org.eclipse.scava.repository.model.VcsRepository;
import org.eclipse.scava.repository.model.github.GitHubWiki;

import com.mongodb.DB;

public class DocumentationTransMetricProvider implements ITransientMetricProvider<DocumentationTransMetric> {

	protected PlatformVcsManager platformVcsManager;
	
	protected OssmeterLogger logger;
	
	public DocumentationTransMetricProvider() {
		logger = (OssmeterLogger) OssmeterLogger.getLogger("metricprovider.trans.documentation");
	}
	
	@Override
	public String getIdentifier() {
		return DocumentationTransMetricProvider.class.getCanonicalName();
	}

	@Override
	public String getShortIdentifier() {
		return "trans.documentation";
	}

	@Override
	public String getFriendlyName() {
		return "Documentation processing";
	}

	@Override
	public String getSummaryInformation() {
		return "This metric process the files or text returned from the documentation readers and transform raw text into plain text.";
	}

	@Override
	public boolean appliesTo(Project project) {
		for(VcsRepository repository : project.getVcsRepositories())
			if(repository instanceof GitHubWiki) return true;
		return false;
	}

	@Override
	public void setUses(List<IMetricProvider> uses) {
		
		
	}

	@Override
	public List<String> getIdentifiersOfUses() {
		return Collections.emptyList();
	}

	@Override
	public void setMetricProviderContext(MetricProviderContext context) {
		this.platformVcsManager=context.getPlatformVcsManager();
		
	}

	@Override
	public DocumentationTransMetric adapt(DB db) {
		return new DocumentationTransMetric(db);
	}

	@Override
	public void measure(Project project, ProjectDelta projectDelta, DocumentationTransMetric db) {

		db.getDocumentationEntries().getDbCollection().drop();
		db.sync();
		
		VcsProjectDelta vcsProjectDelta = projectDelta.getVcsDelta();
		
		for(VcsRepositoryDelta vcsDelta : vcsProjectDelta.getRepoDeltas())
		{
			List<VcsCommit> commits = vcsDelta.getCommits();
			
			if(commits.size()==0)
				return;
			
			Set<String> filesToProcess = new HashSet<String>();
			Set<String> filesToDelete = new HashSet<String>();
			VcsRepository repository = vcsDelta.getRepository();
			String lastRevision="";
			
			boolean needToReadAll=false;
			//We have never seen a commit before
			
			Documentation documentation = findDocumentation(db, repository.getUrl());
			
			if(documentation==null)
			{
				documentation = new Documentation();
				documentation.setDocumentationId(repository.getUrl());
				documentation.setLastRevisionAnalyzed("");
				db.getDocumentation().add(documentation);
				db.sync();
			}
			
			
			
			//If the last commit analyzed does not match the commit previous to today, 
			//then we need to read everything as in the meantime there was a change
			if(!documentation.getLastRevisionAnalyzed().equals(commits.get(0).getRevision()))
				needToReadAll=true;
			//If we have more than one commit but we don't have to read everything, then we can process only those for today
			if(commits.size()>0 && needToReadAll==false)
				commits.remove(0);
			
			if(needToReadAll)
			{
				documentation.getEntriesId().clear();
				documentation.getRemovedEntriesId().clear();
				documentation.setLastUpdateDate(projectDelta.getDate().toString());
				
				//As we are reading everything, then we can just read the last revision of today
				VcsCommit commit = commits.get(commits.size()-1);
				
				documentation.setLastRevisionAnalyzed(commit.getRevision());
				
				Map<String, File> workingCopyFolders = new HashMap<String, File>();				
				Map<String, File> scratchFolders = new HashMap<String, File>();
				
				try {
					WorkingCopyFactory.getInstance().checkout(project, commit.getRevision(), workingCopyFolders, scratchFolders);
					
					
					try (Stream<Path> filePaths = Files.walk(workingCopyFolders.get(repository.getUrl()).toPath()))
			        { 
						String root = workingCopyFolders.get(repository.getUrl()).toString();
						
						for(Path file : filePaths.filter(Files::isRegularFile).toArray(Path[]::new))
						{
							
							String fileName=file.getName(file.getNameCount()-1).toString();
					 		String relativePath=file.getParent().toString().substring(root.length());
					 		
					 		if(!relativePath.equals(""))
					 		{
					 			relativePath=relativePath.replaceAll("\\\\", "/");
					 			relativePath+="/";
					 		}
					 		relativePath+=fileName;
					 		if(relativePath.startsWith("/."))
					 			continue;
					 		
					 		System.err.println(relativePath);
					 		processFile(file.toFile(), db, documentation, repository.getUrl(), relativePath, fileName);
						}
						db.sync();
						
			        } catch (IOException e) {
			        	logger.error("Error while reading file from local copy of documentation:", e);
						e.printStackTrace();
					}
				}
				 catch (WorkingCopyManagerUnavailable | WorkingCopyCheckoutException e) {
						logger.error("Error while creating local copy of documentation:", e);
						e.printStackTrace();
				}
			}
			else if(commits.size()>0)
			{
				for(VcsCommit commit : commits)
				{
					lastRevision = commit.getRevision();
					for(VcsCommitItem item : commit.getItems())
					{
						if(item.getChangeType()==VcsChangeType.ADDED || item.getChangeType()==VcsChangeType.UPDATED)
							filesToProcess.add(item.getPath());
						if(item.getChangeType()==VcsChangeType.DELETED)
						{
							filesToProcess.removeIf(file -> file.equals(item.getPath()));
							filesToDelete.add(item.getPath());
						}
							
					}
				}
				
				documentation.setLastRevisionAnalyzed(lastRevision);
				
				if(filesToProcess.size()>0 || filesToDelete.size()>0)
				{
					
					documentation.getRemovedEntriesId().clear();
					for(String relativePath : filesToDelete)
					{
						if(documentation.getEntriesId().contains(relativePath))
						{
							documentation.getEntriesId().remove(relativePath);
							documentation.getRemovedEntriesId().add(relativePath);
						}
					}
					
					documentation.setLastUpdateDate(projectDelta.getDate().toString());
					
					if(filesToProcess.size()>0)
					{	
						Map<String, File> workingCopyFolders = new HashMap<String, File>();				
						Map<String, File> scratchFolders = new HashMap<String, File>();
						
						try {
							WorkingCopyFactory.getInstance().checkout(project, lastRevision, workingCopyFolders, scratchFolders);
							File file;
							for(String relativePath : filesToProcess)
							{
								try
								{
									file = new File(workingCopyFolders.get(repository.getUrl())+"/"+relativePath);
									
									if(!Files.exists(file.toPath()))
										throw new FileNotFoundException("The file "+relativePath+" has not been found");
									
									processFile(file, db, documentation, repository.getUrl(), relativePath, file.getName());
									
								}
								catch(InvalidPathException e)
								{
									logger.error("File: "+relativePath+" couldn't be found in the working copy as it contains an illegal character "
											+ "i.e. \\ / : * ? \" < > |");
								} catch (FileNotFoundException e) {
									logger.error("File: "+relativePath+" couldn't be found in the working copy.", e);
									e.printStackTrace();
								} catch (Exception e) {
									continue;
								}
							}
							db.sync();
							
						} catch (WorkingCopyManagerUnavailable | WorkingCopyCheckoutException e) {
							logger.error("Error while creating local copy of documentation:", e);
							e.printStackTrace();
						}
						
					}
				}
				db.sync();
			}
		}
		
	}
	
	private void processFile(File file, DocumentationTransMetric db, Documentation documentation, String documentId, String entryId, String fileName)
	{
		try {
			String fileContent = FileParser.extractTextAsString(file);
			
			if(!fileContent.isEmpty())
	 		{
		 		DocumentationEntry documentationEntry = findDocumentationEntry(db, documentId, entryId);
				if(documentationEntry==null)
				{
					documentationEntry = new DocumentationEntry();
					documentationEntry.setDocumentationId(documentId);
					documentationEntry.setEntryId(entryId);
					if(!documentation.getEntriesId().contains(entryId))
						documentation.getEntriesId().add(entryId);
					db.getDocumentationEntries().add(documentationEntry);
				}
				else
					documentationEntry.getPlainText().clear();
				documentationEntry.getPlainText().addAll(getPlainText(fileName, fileContent));
	 		}
			
		} catch (UnsupportedOperationException e) {
			logger.error(e.getMessage()+file.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
 		
	}
	
	private List<String> getPlainText(String fileName, String fileContent)
	{
		PlainTextObject plainText=null;
		if(fileName.endsWith(".md") || fileName.endsWith(".markdown"))
		{
			plainText = PlainTextDocumentationMarkdownBased.process(fileContent);
		}
		else
			plainText = PlainTextDocumentationOthers.process(fileContent);
		if(plainText==null)
			return new ArrayList<String>(0);
		return plainText.getPlainTextAsList();
	}
	
	private DocumentationEntry findDocumentationEntry(DocumentationTransMetric db, String documentationId, String relativePathFile)
	{
		DocumentationEntry documentationEntry = null;
		Iterable<DocumentationEntry> documentationEntryIt = db.getDocumentationEntries().
				find(DocumentationEntry.DOCUMENTATIONID.eq(documentationId),
						DocumentationEntry.ENTRYID.eq(relativePathFile));
		for(DocumentationEntry de : documentationEntryIt)
			documentationEntry=de;
		return documentationEntry;
	}
	
	private Documentation findDocumentation(DocumentationTransMetric db, String documentationId)
	{
		Documentation documentation = null;
		Iterable<Documentation> documentationIt = db.getDocumentation().
				find(Documentation.DOCUMENTATIONID.eq(documentationId));
		for(Documentation d : documentationIt)
			documentation=d;
		return documentation;
	}

}