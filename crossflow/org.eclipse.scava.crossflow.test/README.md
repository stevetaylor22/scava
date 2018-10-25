
## Crossflow test project

### About

This project contains JUnit-based tests for org.eclipse.scava.crossflow.* projects generated by [EvoSuite](http://www.evosuite.org), i.e. an automatic test generation suite for Java [1]. Although EvoSuite has won the Search-Based Software Testing competition at ICSE'17 [2]. The basic assumption of EvoSuite is that the current behavior of the system under test represents the expected behavior and thus supports during the discovery of defects (or changes in behavior) that may arise in the future. Empirical research on the effectiveness of such approaches is available [3].


### About EvoSuite
To find defects in software, one needs test cases that execute the software systematically, and oracles that assess the correctness of the observed behavior when running these test cases. EvoSuite is a tool that automatically generates test cases with assertions for classes written in Java code. To achieve this, EvoSuite applies a novel hybrid approach that generates and optimizes whole test suites towards satisfying a coverage criterion. For the produced test suites, EvoSuite suggests possible oracles by adding small and effective sets of assertions that concisely summarize the current behavior; these assertions allow the developer to detect deviations from expected behavior, and to capture the current behavior in order to protect against future defects breaking this behaviour.


### How to generate new/updated JUnit tests:

There are several procedures to generate test suites with EvoSuite, one of which is described below.

 1. Create a JAR of the target project with dependencies for which tests ought to be generated by right-clicking on `org.eclipse.scava.crossflow.examples/build-jar-with-dependencies.xml` and selecting `Run As > Ant Build`.
 2. Issue the following command from the same directory as this file, i.e. README.md, to generate a test suite for Java classes prefixed with `org.eclipse.scava.crossflow`:
	
	`$ java -jar ../org.eclipse.scava.crossflow.runtime.dependencies/evosuite-master-1.0.7-SNAPSHOT.jar -target ../org.eclipse.scava.crossflow.examples/org.eclipse.scava.crossflow.examples-with-dependencies.jar -base_dir ../org.eclipse.scava.crossflow.test/ -prefix org.eclipse.scava.crossflow -Dsearch_budget=60`
 3. Move the content in the created directory `evosuite-tests` to the `src` directory.
	
	`$ mv evosuite-tests/* src/`

More details on how to generate tests using the command-line as well as details on different parameter options can be found [here](http://www.evosuite.org/documentation/commandline/). 


***NOTES:***
- Running the generated test suite may require a high amount of heap memory and thus an appropriate memory limitation setup.
- Test coverage of generated tests can be found in `evosuite-report/statistics.csv`. 


***References:***

[1] http://www.evosuite.org/publications/

[2] http://www.evosuite.org/wp-content/papercite-data/pdf/sbst17_competition.pdf

[3] http://www.evosuite.org/wp-content/papercite-data/pdf/emse16_effectiveness.pdf