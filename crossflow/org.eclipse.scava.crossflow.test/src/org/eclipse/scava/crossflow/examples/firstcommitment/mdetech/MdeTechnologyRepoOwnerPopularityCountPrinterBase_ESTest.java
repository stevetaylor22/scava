/*
 * This file was automatically generated by EvoSuite
 * Tue Oct 23 17:49:31 GMT 2018
 */

package org.eclipse.scava.crossflow.examples.firstcommitment.mdetech;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.EclipseResultPublisher;
import org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.ExtensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple;
import org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.MdeTechnologyExample;
import org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.MdeTechnologyRepoOwnerPopularityCountPrinter;
import org.eclipse.scava.crossflow.runtime.Workflow;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class MdeTechnologyRepoOwnerPopularityCountPrinterBase_ESTest extends MdeTechnologyRepoOwnerPopularityCountPrinterBase_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      mdeTechnologyRepoOwnerPopularityCountPrinter0.setEclipseResultPublisher((EclipseResultPublisher) null);
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      Workflow workflow0 = mdeTechnologyRepoOwnerPopularityCountPrinter0.getWorkflow();
      assertNull(workflow0);
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      EclipseResultPublisher eclipseResultPublisher0 = mdeTechnologyRepoOwnerPopularityCountPrinter0.getEclipseResultPublisher();
      assertNull(eclipseResultPublisher0);
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      ExtensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple extensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple0 = new ExtensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple();
      // Undeclared exception!
      try { 
        mdeTechnologyRepoOwnerPopularityCountPrinter0.consumeMdeTechnologyRepoOwnerPopularityCountEntriesActual(extensionKeywordStargazersRemoteRepoUrlLocalRepoPathTuple0);
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.MdeTechnologyRepoOwnerPopularityCountPrinterBase", e);
      }
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      MdeTechnologyExample mdeTechnologyExample0 = new MdeTechnologyExample();
      mdeTechnologyRepoOwnerPopularityCountPrinter0.setWorkflow(mdeTechnologyExample0);
      mdeTechnologyExample0.mdeTechnologyRepoOwnerPopularityCountPrinter = mdeTechnologyRepoOwnerPopularityCountPrinter0;
      Workflow workflow0 = mdeTechnologyExample0.mdeTechnologyRepoOwnerPopularityCountPrinter.getWorkflow();
      assertEquals("MdeTechnologyExample", workflow0.getName());
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      // Undeclared exception!
      try { 
        mdeTechnologyRepoOwnerPopularityCountPrinter0.taskBlocked("MN9ige$+");
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.MdeTechnologyRepoOwnerPopularityCountPrinterBase", e);
      }
  }

  @Test(timeout = 4000)
  public void test6()  throws Throwable  {
      MdeTechnologyRepoOwnerPopularityCountPrinter mdeTechnologyRepoOwnerPopularityCountPrinter0 = new MdeTechnologyRepoOwnerPopularityCountPrinter();
      MdeTechnologyExample mdeTechnologyExample0 = new MdeTechnologyExample();
      mdeTechnologyExample0.mdeTechnologyRepoOwnerPopularityCountPrinter = mdeTechnologyRepoOwnerPopularityCountPrinter0;
      // Undeclared exception!
      try { 
        mdeTechnologyExample0.mdeTechnologyRepoOwnerPopularityCountPrinter.taskUnblocked();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.eclipse.scava.crossflow.examples.firstcommitment.mdetech.MdeTechnologyRepoOwnerPopularityCountPrinterBase", e);
      }
  }
}