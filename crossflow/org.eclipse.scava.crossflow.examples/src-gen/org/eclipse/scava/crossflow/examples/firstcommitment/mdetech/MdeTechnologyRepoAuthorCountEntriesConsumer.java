package org.eclipse.scava.crossflow.examples.firstcommitment.mdetech;

public interface MdeTechnologyRepoAuthorCountEntriesConsumer {

	public void consumeMdeTechnologyRepoAuthorCountEntries(ExtensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple extensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple);
	
	/**
	 * wraps consumeMdeTechnologyRepoAuthorCountEntries() to provide task status information
	 */
	public void consumeMdeTechnologyRepoAuthorCountEntriesActual(ExtensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple extensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple);

}