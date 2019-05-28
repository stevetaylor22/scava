/*******************************************************************************
 * Copyright (c) 2018 Edge Hill University
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.scava.metricprovider.trans.plaintextprocessing.model;

import com.googlecode.pongo.runtime.*;
import com.mongodb.*;
// protected region custom-imports on begin
// protected region custom-imports end

public class PlainTextProcessingTransMetric extends PongoDB {
	
	public PlainTextProcessingTransMetric() {}
	
	public PlainTextProcessingTransMetric(DB db) {
		setDb(db);
	}
	
	protected BugTrackerCommentPlainTextProcessingCollection bugTrackerComments = null;
	protected NewsgroupArticlePlainTextProcessingCollection newsgroupArticles = null;
	
	// protected region custom-fields-and-methods on begin
	// protected region custom-fields-and-methods end
	
	
	public BugTrackerCommentPlainTextProcessingCollection getBugTrackerComments() {
		return bugTrackerComments;
	}
	
	public NewsgroupArticlePlainTextProcessingCollection getNewsgroupArticles() {
		return newsgroupArticles;
	}
	
	
	@Override
	public void setDb(DB db) {
		super.setDb(db);
		bugTrackerComments = new BugTrackerCommentPlainTextProcessingCollection(db.getCollection("PlainTextProcessingTransMetric.bugTrackerComments"));
		pongoCollections.add(bugTrackerComments);
		newsgroupArticles = new NewsgroupArticlePlainTextProcessingCollection(db.getCollection("PlainTextProcessingTransMetric.newsgroupArticles"));
		pongoCollections.add(newsgroupArticles);
	}
}
