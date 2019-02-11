package org.eclipse.scava.platform.client.api;

import java.net.UnknownHostException;
import java.util.List;

import org.eclipse.scava.platform.Configuration;
import org.eclipse.scava.platform.Platform;
import org.eclipse.scava.platform.analysis.AnalysisTaskService;
import org.eclipse.scava.platform.analysis.data.model.AnalysisTask;
import org.eclipse.scava.repository.model.Project;
import org.eclipse.scava.repository.model.ProjectRepository;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.ServerResource;

import com.mongodb.Mongo;

public class ProjectDeleteResource extends ServerResource {

	@Delete
	public Representation deleteAnalysisTask() {
		Mongo mongo = null;
		Platform platform = null;
		try {
			mongo = Configuration.getInstance().getMongoConnection();
			platform = new Platform(mongo);				
			AnalysisTaskService service = platform.getAnalysisRepositoryManager().getTaskService();
			
			String projectId = (String) getRequest().getAttributes().get("projectid");

			List<AnalysisTask> analysisTasks = service.getAnalysisTasksByProject(projectId);
			for (AnalysisTask analysisTask : analysisTasks) {
				service.deleteAnalysisTask(analysisTask.getAnalysisTaskId());
			}
			
			ProjectRepository projectRepo = platform.getProjectRepositoryManager().getProjectRepository();
			Iterable<Project> projectToDelete = projectRepo.getProjects().findByShortName(projectId);
			for (Project project : projectToDelete) {
				if (project.getShortName().equals(projectId)) {
					platform.getMetricsRepository(project).getDb().dropDatabase();
					projectRepo.getProjects().remove(project);
				}
			}
			projectRepo.sync();

			StringRepresentation rep = new StringRepresentation("");
			rep.setMediaType(MediaType.APPLICATION_JSON);
			getResponse().setStatus(Status.SUCCESS_OK);
			return rep;

		} catch (UnknownHostException e) {
			e.printStackTrace();
			StringRepresentation rep = new StringRepresentation("");
			rep.setMediaType(MediaType.APPLICATION_JSON);
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return rep;
		} finally {
			if (mongo != null) mongo.close();
			platform = null;
		}

	}
}