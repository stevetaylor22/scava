package org.eclipse.scava.crossflow.examples.firstcommitment.mdetech;

import org.eclipse.scava.crossflow.runtime.Workflow;

public abstract class MdeTechnologySourceBase {
	
	protected MdeTechnologyExample workflow;
	
	public void setWorkflow(MdeTechnologyExample workflow) {
		this.workflow = workflow;
	}
	
	public Workflow getWorkflow() {
		return workflow;
	}
	
	protected MdeTechnologies mdeTechnologies;
	
	public void setMdeTechnologies(MdeTechnologies mdeTechnologies) {
		this.mdeTechnologies = mdeTechnologies;
	}
	
	public MdeTechnologies getMdeTechnologies() {
		return mdeTechnologies;
	}
	
	
	
	protected EclipseResultPublisher eclipseResultPublisher;
	
	public void setEclipseResultPublisher(EclipseResultPublisher eclipseResultPublisher) {
		this.eclipseResultPublisher = eclipseResultPublisher;
	}
	
	public EclipseResultPublisher getEclipseResultPublisher() {
		return eclipseResultPublisher;
	}
	
	
	public abstract void produce();
	
}