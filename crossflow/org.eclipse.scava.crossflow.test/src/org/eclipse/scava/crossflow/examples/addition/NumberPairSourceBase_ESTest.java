/*
 * This file was automatically generated by EvoSuite
 * Tue Oct 23 19:02:56 GMT 2018
 */

package org.eclipse.scava.crossflow.examples.addition;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.evosuite.runtime.EvoAssertions.*;
import org.eclipse.scava.crossflow.examples.addition.AdditionExample;
import org.eclipse.scava.crossflow.examples.addition.Additions;
import org.eclipse.scava.crossflow.examples.addition.EclipseResultPublisher;
import org.eclipse.scava.crossflow.examples.addition.NumberPairSource;
import org.eclipse.scava.crossflow.runtime.Workflow;
import org.evosuite.runtime.EvoRunner;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.runner.RunWith;

@RunWith(EvoRunner.class) @EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = true, useJEE = true) 
public class NumberPairSourceBase_ESTest extends NumberPairSourceBase_ESTest_scaffolding {

  @Test(timeout = 4000)
  public void test0()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      numberPairSource0.produce();
  }

  @Test(timeout = 4000)
  public void test1()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      // Undeclared exception!
      try { 
        numberPairSource0.taskUnblocked();
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.eclipse.scava.crossflow.examples.addition.NumberPairSourceBase", e);
      }
  }

  @Test(timeout = 4000)
  public void test2()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      Workflow workflow0 = numberPairSource0.getWorkflow();
      assertNull(workflow0);
  }

  @Test(timeout = 4000)
  public void test3()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      AdditionExample additionExample0 = new AdditionExample();
      numberPairSource0.setWorkflow(additionExample0);
      assertEquals("AdditionExample", additionExample0.getName());
  }

  @Test(timeout = 4000)
  public void test4()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      Additions additions0 = numberPairSource0.getAdditions();
      assertNull(additions0);
  }

  @Test(timeout = 4000)
  public void test5()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      // Undeclared exception!
      try { 
        numberPairSource0.taskBlocked("hsX.Y-W@X?C G^UP");
        fail("Expecting exception: NullPointerException");
      
      } catch(NullPointerException e) {
         //
         // no message in exception (getMessage() returned null)
         //
         verifyException("org.eclipse.scava.crossflow.examples.addition.NumberPairSourceBase", e);
      }
  }

  @Test(timeout = 4000)
  public void test6()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      numberPairSource0.setEclipseResultPublisher((EclipseResultPublisher) null);
  }

  @Test(timeout = 4000)
  public void test7()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      EclipseResultPublisher eclipseResultPublisher0 = numberPairSource0.getEclipseResultPublisher();
      assertNull(eclipseResultPublisher0);
  }

  @Test(timeout = 4000)
  public void test8()  throws Throwable  {
      NumberPairSource numberPairSource0 = new NumberPairSource();
      numberPairSource0.setAdditions((Additions) null);
  }
}