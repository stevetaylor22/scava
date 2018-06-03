/*******************************************************************************
 * Copyright (C) 2017 University of L'Aquila
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.scava.test.importer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.scava.Application;
import org.eclipse.scava.business.impl.GithubImporter;
import org.eclipse.scava.business.integration.ArtifactRepository;
import org.eclipse.scava.business.model.Artifact;
import org.eclipse.scava.config.SwaggerConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, SwaggerConfig.class })
@WebAppConfiguration
@TestPropertySource(locations="classpath:application.properties")
public class GithubImporterTest {
	@Autowired
	private GithubImporter importer;
	
	@Autowired
	private ArtifactRepository artifactRepository;
	
	private static final Logger logger = LoggerFactory.getLogger(GithubImporterTest.class);

	@Before
	public void init(){
		artifactRepository.deleteAll();
	}
	@After
	public void dispose(){
		artifactRepository.deleteAll();
	}
	@Test
	public void importProjectTest() throws IOException {
		importer.importProject("MDEGroup/MDEProfile");
		assertEquals(artifactRepository.count(), 1);
	}
	@Test
	public void importer() throws IOException, XmlPullParserException{
		Artifact art = importer.importProject("pylerSM/Xinstaller");
		assertNotNull(art);
	}
}
