package org.eclipse.crossmeter.repository.model;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import org.eclipse.crossmeter.platform.factoids.*;
import org.eclipse.crossmeter.repository.model.*;
import org.eclipse.crossmeter.repository.model.bts.bugzilla.*;
import org.eclipse.crossmeter.repository.model.cc.forum.*;
import org.eclipse.crossmeter.repository.model.cc.nntp.*;
import org.eclipse.crossmeter.repository.model.cc.wiki.*;
import org.eclipse.crossmeter.repository.model.eclipse.*;
import org.eclipse.crossmeter.repository.model.github.*;
import org.eclipse.crossmeter.repository.model.googlecode.*;
import org.eclipse.crossmeter.repository.model.metrics.*;
import org.eclipse.crossmeter.repository.model.redmine.*;
import org.eclipse.crossmeter.repository.model.sourceforge.*;
import org.eclipse.crossmeter.repository.model.vcs.cvs.*;
import org.eclipse.crossmeter.repository.model.vcs.git.*;
import org.eclipse.crossmeter.repository.model.vcs.svn.*;

@JsonTypeInfo(use=JsonTypeInfo.Id.NAME,
	include=JsonTypeInfo.As.PROPERTY,
	property = "_type")
@JsonSubTypes({
	@Type(value = NamedElement.class, name="org.eclipse.crossmeter.repository.model.NamedElement"), 	@Type(value = org.eclipse.crossmeter.repository.model.eclipse.EclipsePlatform.class, name="org.eclipse.crossmeter.repository.model.eclipse.EclipsePlatform"),
	@Type(value = org.eclipse.crossmeter.repository.model.eclipse.Release.class, name="org.eclipse.crossmeter.repository.model.eclipse.Release"),
	@Type(value = org.eclipse.crossmeter.repository.model.googlecode.GoogleLabel.class, name="org.eclipse.crossmeter.repository.model.googlecode.GoogleLabel"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.OS.class, name="org.eclipse.crossmeter.repository.model.sourceforge.OS"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Topic.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Topic"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.ProgrammingLanguage.class, name="org.eclipse.crossmeter.repository.model.sourceforge.ProgrammingLanguage"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Audience.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Audience"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Environment.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Environment"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Category.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Category"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Tracker.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Tracker"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.BugTS.class, name="org.eclipse.crossmeter.repository.model.sourceforge.BugTS"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Request.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Request"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.FeatureRequest.class, name="org.eclipse.crossmeter.repository.model.sourceforge.FeatureRequest"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.SupportRequest.class, name="org.eclipse.crossmeter.repository.model.sourceforge.SupportRequest"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Patch.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Patch"),
	@Type(value = org.eclipse.crossmeter.repository.model.sourceforge.Bug.class, name="org.eclipse.crossmeter.repository.model.sourceforge.Bug"),
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class NamedElement extends Object {

	protected String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
