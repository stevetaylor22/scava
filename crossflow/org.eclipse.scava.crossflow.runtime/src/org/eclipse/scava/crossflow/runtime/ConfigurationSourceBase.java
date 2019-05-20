package org.eclipse.scava.crossflow.runtime;

public abstract class ConfigurationSourceBase extends Task {

	protected Workflow workflow;
	
	@Override
	public Workflow getWorkflow() {
		return workflow;
	}
	
	public void setWorkflow(Workflow w) {
		this.workflow = w;
	}
	
	@Override
	public String getId() {
		return "ConfigurationSource:"+workflow.getName();
	}
	
	public abstract java.io.Serializable createConfiguration();

}
