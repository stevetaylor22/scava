package org.eclipse.scava.crossflow.tests.basecase;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.activemq.broker.BrokerRegistry;
import org.apache.commons.io.FileUtils;
import org.eclipse.scava.crossflow.runtime.Mode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BaseTestCase {
	
	private final static String BROKER_NAME = "localhost";
	private final static long SLEEP_DURATION = 2000;


	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// clean-up file(s) generated by previous test case executions
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
		// tear-down broker
		BrokerRegistry.getInstance().lookup(BROKER_NAME).stop();
	}

	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
	
	@Test
	void testCase() {
		
		// setup test case configuration
		BaseCase master = new BaseCase();
		master.setName("Master-"+UUID.randomUUID().toString());
		BaseCase worker = new BaseCase();
		worker.setName("Worker");
		worker.setMode(Mode.WORKER);
		
		try {
			// run test case configuration
			master.run();
			worker.run();
			
			// sleep / wait before test result assertion
			Thread.sleep(SLEEP_DURATION);
		
			// assert statements		
			assertEquals( Mode.MASTER, master.getMode() );
			assertEquals( Mode.WORKER, worker.getMode() );
			assertNotNull( master.getAdditions() );
			assertNotNull( master.getAdditionResults() );
			
		} catch (Exception e1) {
			fail("Unexpected exception occurred during test case execution.");
			e1.printStackTrace();
		}
		
	}

}
