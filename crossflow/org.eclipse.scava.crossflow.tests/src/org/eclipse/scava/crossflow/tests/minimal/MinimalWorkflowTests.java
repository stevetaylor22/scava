package org.eclipse.scava.crossflow.tests.minimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.scava.crossflow.runtime.BuiltinStreamConsumer;
import org.eclipse.scava.crossflow.runtime.DirectoryCache;
import org.eclipse.scava.crossflow.runtime.Mode;
import org.eclipse.scava.crossflow.runtime.utils.Result;
import org.eclipse.scava.crossflow.runtime.utils.StreamMetadata;
import org.eclipse.scava.crossflow.runtime.utils.StreamMetadata.Stream;
import org.eclipse.scava.crossflow.runtime.utils.TaskStatus;
import org.eclipse.scava.crossflow.runtime.utils.TaskStatus.TaskStatuses;
import org.eclipse.scava.crossflow.tests.WorkflowTests;
import org.junit.Test;

public class MinimalWorkflowTests extends WorkflowTests {

	@Test
	public void testCache() throws Exception {

		List<Integer> numbers = Arrays.asList(1, 1);

		MinimalWorkflow workflow = new MinimalWorkflow();
		if (singleBroker)
			workflow.createBroker(false);
		workflow.getMinimalSource().setNumbers(numbers);
		DirectoryCache cache = new DirectoryCache();
		workflow.setCache(cache);
		workflow.run();

		waitFor(workflow);

		assertEquals(2, workflow.getMinimalSink().getNumbers().size());
		assertEquals(2, workflow.getCopierTask().getExecutions());

		workflow = new MinimalWorkflow();
		if (singleBroker)
			workflow.createBroker(false);
		workflow.getMinimalSource().setNumbers(numbers);
		workflow.setCache(new DirectoryCache(cache.getDirectory()));
		workflow.run();

		waitFor(workflow);

		assertEquals(2, workflow.getMinimalSink().getNumbers().size());
		assertEquals(0, workflow.getCopierTask().getExecutions());
	}

	@Test
	public void testResultsTopic() throws Exception {
		testResultsTopicActual(0);
		// ensures result topic is accurate with an initial delay on master
		testResultsTopicActual(4000);
	}

	public void testResultsTopicActual(int initialDelay) throws Exception {
		List<Integer> numbers = Arrays.asList(1, 2);
		MinimalWorkflow workflow = new MinimalWorkflow();
		if (singleBroker)
			workflow.createBroker(false);
		List<Integer> results = new LinkedList<Integer>();
		workflow.getMinimalSource().setNumbers(numbers);
		//
		workflow.getMinimalSource().setSendToResults(true);
		workflow.getResultsTopic().addConsumer(new BuiltinStreamConsumer<Result>() {
			@Override
			public void consume(Result t) {
				results.add((Integer) t.get(0));
			}
		});
		//
		workflow.run(initialDelay);
		//
		waitFor(workflow);
		assertEquals(numbers, results);
	}

	private class TaskStatusBuiltinStreamConsumer implements BuiltinStreamConsumer<TaskStatus> {
		public int outputQueueSize = 0;

		@Override
		public void consume(TaskStatus t) {
			// System.out.println(t);
			if (t.getCaller().equals("CopierTask:MinimalWorkflow") && t.getStatus().equals(TaskStatuses.WAITING))
				outputQueueSize++;
			if (t.getCaller().equals("MinimalSink:MinimalWorkflow") && t.getStatus().equals(TaskStatuses.WAITING))
				outputQueueSize--;
			//
		}
	}

	private abstract class StreamMetadataBuiltinStreamConsumer implements BuiltinStreamConsumer<StreamMetadata> {
		public List<Map.Entry<Long, Long>> failures = new ArrayList<Map.Entry<Long, Long>>();
		public boolean updated = false;
		public long maxQueueSize = 0;
		public int nonZeroMatches = 0;
	};

	@Test
	public void testStreamMetadataTopic() throws Exception {
		testStreamMetadataTopicActual(false);
		// ensures metadata checks are valid with prefetching enabled for queues
		testStreamMetadataTopicActual(true);
	}

	public synchronized void testStreamMetadataTopicActual(boolean enablePrefetch) throws Exception {

		MinimalWorkflow workflow = new MinimalWorkflow();
		if (singleBroker)
			workflow.createBroker(false);

		//
		workflow.setInstanceId("testStreamMetadataTopicWorkflow");
		//
		workflow.setEnablePrefetch(enablePrefetch);
		//
		List<Integer> numbers = new LinkedList<Integer>();
		for (int i = 1; i <= 10; i++)
			numbers.add(i);
		//
		workflow.setStreamMetadataPeriod(500);
		workflow.getMinimalSource().setNumbers(numbers);
		workflow.getCopierTask().setDelay(10);
		workflow.getMinimalSink().setDelay(500);

		TaskStatusBuiltinStreamConsumer con = new TaskStatusBuiltinStreamConsumer();
		StreamMetadataBuiltinStreamConsumer con2 = new StreamMetadataBuiltinStreamConsumer() {
			public static final String OUTPUT_STREAM_ID = "OutputPost.MinimalSink.testStreamMetadataTopicWorkflow";

			@Override
			public void consume(StreamMetadata t) {

				// t.pruneNames(OUTPUT_STREAM_ID.length());
				// System.out.println(t);

				long streamSize = -1;
				long statusBasedSize = -1;
				long streamInFlight = -1;
				try {
					//
					try {
						streamSize = t.getStream(OUTPUT_STREAM_ID).getSize();
						streamInFlight = t.getStream(OUTPUT_STREAM_ID).getInFlight();
					} catch (Exception e) {
						System.out.println("stream: " + OUTPUT_STREAM_ID + " not found!");
					}
					statusBasedSize = (long) con.outputQueueSize;

					//
					System.out.println(streamSize + " (" + streamInFlight + ") | " + statusBasedSize);
					if (!updated && streamSize < statusBasedSize) {
						// mbean has not yet updated
						System.out.println("mbean not up to date yet");
						return;
					}
					// mbean has updated
					updated = true;
					assertEquals(streamSize, (long) statusBasedSize);
					if (streamSize > 0)
						nonZeroMatches++;
					maxQueueSize = streamSize > maxQueueSize ? streamSize : maxQueueSize;
				} catch (Throwable e) {
					// e.printStackTrace();
					failures.add(new AbstractMap.SimpleEntry<Long, Long>(streamSize, statusBasedSize));
				}
			}
		};

		workflow.getTaskStatusTopic().addConsumer(con);
		//
		workflow.getStreamMetadataTopic().addConsumer(con2);
		//
		workflow.run();
		//
		waitFor(workflow);

		System.out.println(workflow.getMinimalSink().getNumbers());

		System.out
				.println("need: " + con2.failures.size() + " < " + con2.nonZeroMatches + " maxQ: " + con2.maxQueueSize);

		assertTrue(con2.failures.size() == 0 || con2.failures.size() + 1 < con2.nonZeroMatches);

		assertTrue(con2.updated);

		assertTrue(con2.maxQueueSize > 0);
	}

	@Test
	public void testStreamMetadataTopicMultiConsumer() throws Exception {
		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
		MinimalWorkflow workflow = new MinimalWorkflow();
		if (singleBroker)
			workflow.createBroker(false);
		workflow.setInstanceId("testStreamMetadataTopicWorkflowMC");
		workflow.setName("master");

		workflow.setStreamMetadataPeriod(500);
		workflow.getMinimalSource().setNumbers(numbers);
		workflow.getMinimalSink().setDelay(100);

		StreamMetadataBuiltinStreamConsumer con = new StreamMetadataBuiltinStreamConsumer() {
			public static final String INPUT_STREAM_ID = "InputPost.CopierTask.testStreamMetadataTopicWorkflowMC";

			@Override
			public void consume(StreamMetadata t) {

				// t.pruneNames(INPUT_STREAM_ID.length());
				// System.out.println(t);

				long subs = -1;
				try {
					Stream stream = t.getStream(INPUT_STREAM_ID);
					subs = stream.getNumberOfSubscribers();
					System.out.println(subs + " == " + "2");
					assertEquals(t.getStream(INPUT_STREAM_ID).getNumberOfSubscribers(), 2);
				} catch (Throwable e) {
					if (failures == null)
						failures = new ArrayList<Map.Entry<Long, Long>>();
					failures.add(new AbstractMap.SimpleEntry<Long, Long>(subs, ((long) 2)));
				}
			}
		};
		workflow.getStreamMetadataTopic().addConsumer(con);
		//

		MinimalWorkflow workflow2 = new MinimalWorkflow(Mode.WORKER);
		workflow2.setInstanceId("testStreamMetadataTopicWorkflowMC");
		workflow2.setMaster("localhost");
		workflow2.setName("worker");

		workflow.run();
		workflow2.run();

		//

		waitFor(workflow);

		assertEquals(0, con.failures.size());
	}

}
