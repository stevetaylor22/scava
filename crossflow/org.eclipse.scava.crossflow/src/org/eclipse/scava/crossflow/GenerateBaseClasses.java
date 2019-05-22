/*******************************************************************************
 * Copyright (c) 2008 The University of York.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * Contributors:
 *     Dimitrios Kolovos - initial API and implementation
 *     Konstantinos Barmpis - adaption for CROSSFLOW
 ******************************************************************************/
package org.eclipse.scava.crossflow;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.egl.EglFileGeneratingTemplateFactory;
import org.eclipse.epsilon.egl.EgxModule;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.eol.IEolModule;
import org.eclipse.epsilon.eol.exceptions.EolRuntimeException;
import org.eclipse.epsilon.eol.exceptions.models.EolModelLoadingException;
import org.eclipse.epsilon.eol.execute.context.Variable;
import org.eclipse.epsilon.eol.models.IModel;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;
import org.eclipse.epsilon.flexmi.FlexmiResourceFactory;

public class GenerateBaseClasses {

	//FIXME automate language-specific generator enabling
	//enum Languages {put enums in}
	
	protected IEolModule module;
	protected List<Variable> parameters = new ArrayList<>();

	protected Object result;

	boolean useFileMetamodel;
	String metamodelUri;
	String projectLocation;
	String modelRelativePath;
	String packageName;

	/**
	 * Default constructor - Crossflow metamodel is already in package registry
	 */
	public GenerateBaseClasses() {
		this.useFileMetamodel = false;
		this.metamodelUri = "org.eclipse.scava.crossflow";
	}

	/**
	 * Constructor allowing specification of alternate location of Crossflow
	 * metamodel.
	 * 
	 * @param crossflowMetamodelLocation Location of crossflow metamodel as a URI
	 *                                   path
	 */
	public GenerateBaseClasses(String crossflowMetamodelLocation) {
		this.useFileMetamodel = true;
		this.metamodelUri = crossflowMetamodelLocation;
	}

	public void run(String projectLocation, String modelRelativePath) throws Exception {

		this.projectLocation = projectLocation;
		this.modelRelativePath = modelRelativePath;

		execute();
	}

	public void execute() throws Exception {

		// 
		
		module = createModule();
		module.parse(getFileURI("crossflow.egx"));

		if (module.getParseProblems().size() > 0) {
			System.err.println("Parse errors occured...");
			for (ParseProblem problem : module.getParseProblems()) {
				System.err.println(problem.toString());
			}
			return;
		}

		for (IModel model : getModels()) {
			module.getContext().getModelRepository().addModel(model);
		}

		module.getContext().getFrameStack().put(parameters);

		result = execute(module);

		module.getContext().getModelRepository().dispose();
		
		//TODO is there a better way to add languages other than manually?
		
		module = createModule();
		module.parse(getFileURI("python/crossflow.egx"));

		if (module.getParseProblems().size() > 0) {
			System.err.println("Parse errors occured...");
			for (ParseProblem problem : module.getParseProblems()) {
				System.err.println(problem.toString());
			}
			return;
		}

		for (IModel model : getModels()) {
			module.getContext().getModelRepository().addModel(model);
		}

		for (Variable parameter : parameters) {
			module.getContext().getFrameStack().put(parameter);
		}

		result = execute(module);

		module.getContext().getModelRepository().dispose();
	}

	protected Object execute(IEolModule module) throws EolRuntimeException {
		return module.execute();
	}

	protected URI getFileURI(String fileName) throws URISyntaxException {
		URI binUri = GenerateBaseClasses.class.getResource(fileName).toURI();
		return new URI(binUri.toString().replace("bin", "src"));
	}

	public IEolModule createModule() {
		try {
			EglFileGeneratingTemplateFactory templateFactory = new EglFileGeneratingTemplateFactory();
			templateFactory.setOutputRoot(new File(projectLocation).getAbsolutePath());
			return new EgxModule(templateFactory);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	public List<IModel> getModels() throws Exception {
		List<IModel> models = new ArrayList<IModel>();
		models.add(createAndLoadAnEmfModel(metamodelUri, modelRelativePath, "Model", true, false, false));

		return models;
	}

	private EmfModel createAndLoadAnEmfModel(String metamodelURI, String modelFile, String modelName,
			boolean readOnLoad, boolean storeOnDisposal, boolean isCached) throws EolModelLoadingException {
		final EmfModel theModel = new EmfModel() {
			@Override
			protected ResourceSet createResourceSet() {
				final ResourceSet resourceSet = super.createResourceSet();
				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("flexmi",
						new FlexmiResourceFactory());
				return resourceSet;
			}
		};
		StringProperties properties = new StringProperties();
		properties.put(useFileMetamodel ? EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI : EmfModel.PROPERTY_METAMODEL_URI,
				metamodelURI);
		properties.put(EmfModel.PROPERTY_MODEL_FILE, modelFile);
		properties.put(EmfModel.PROPERTY_NAME, modelName);
		properties.put(EmfModel.PROPERTY_READONLOAD, readOnLoad + "");
		properties.put(EmfModel.PROPERTY_STOREONDISPOSAL, storeOnDisposal + "");
		properties.put(EmfModel.PROPERTY_CACHED, isCached + "");
		theModel.load(properties, (IRelativePathResolver) null);
		return theModel;
	}

}
