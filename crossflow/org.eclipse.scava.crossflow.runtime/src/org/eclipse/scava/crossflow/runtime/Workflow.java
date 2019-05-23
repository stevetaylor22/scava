package org.eclipse.scava.crossflow.runtime;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.management.*;
import javax.management.remote.*;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.jmx.DestinationViewMBean;
import org.apache.activemq.command.ActiveMQDestination;
import org.eclipse.scava.crossflow.runtime.utils.ControlSignal.ControlSignals;
import org.eclipse.scava.crossflow.runtime.utils.CrossflowLogger.SEVERITY;
import org.eclipse.scava.crossflow.runtime.utils.TaskStatus.TaskStatuses;
import org.eclipse.scava.crossflow.runtime.utils.*;
import com.beust.jcommander.Parameter;

public abstract class Workflow {

	@Parameter(names = { "-name" }, description = "The name of the workflow")
	protected String name;
	protected Cache cache;

	@Parameter(names = { "-master" }, description = "IP of the master")
	protected String master = "localhost";

	@Parameter(names = { "-port" }, description = "Port of the master")
	protected int port = 61616;

	@Parameter(names = { "-stomp" }, description = "Port to use for STOMP based messages")
	protected int stompPort = 61613;

	@Parameter(names = { "-ws" }, description = "Port to use for WS based messages")
	protected int wsPort = 61614;

	protected boolean enableStomp = true;

	protected boolean enableWS = false;

	protected BrokerService brokerService;

	@Parameter(names = { "-activeMqConfig" }, description = "Location of ActiveMQ configuration file")
	protected String activeMqConfig;

	@Parameter(names = { "-instance" }, description = "The instance of the master (to contribute to)")
	protected String instanceId;

	@Parameter(names = {
			"-mode" }, description = "Must be master_bare, master or worker", converter = ModeConverter.class)
	protected Mode mode = Mode.MASTER;

	@Parameter(names = { "-createBroker" }, description = "Whether this workflow creates a broker or not.", arity = 1)
	protected boolean createBroker = true;

	@Parameter(names = {
			"-parallelization" }, description = "The parallelization of the workflow (for non-singleton tasks), defaults to 1")
	protected int parallelization = 1;// Runtime.getRuntime().availableProcessors();

	private List<String> activeJobs = new ArrayList<>();
	protected HashSet<Stream> activeStreams = new HashSet<>();

	protected HashSet<Task> tasks = new HashSet<>();

	@Parameter(names = {
			"-cacheEnabled" }, description = "Whether this workflow caches intermediary results or not.", arity = 1)
	protected boolean cacheEnabled = false;

	@Parameter(names = {
			"-deleteCache" }, description = "Before starting this workflow, delete the contents of the cache by queue name (use empty string to delete entire cache).")
	protected String deleteCache;

	@Parameter(names = {
			"-inputDirectory" }, description = "The input directory of the workflow.", converter = DirectoryConverter.class)
	protected File inputDirectory = new File("").getAbsoluteFile();
	@Parameter(names = {
			"-outputDirectory" }, description = "The output directory of the workflow.", converter = DirectoryConverter.class)
	protected File outputDirectory = new File("").getParentFile();

	protected File runtimeModel = new File("").getParentFile();
	protected File tempDirectory = null;

	protected BuiltinStream<TaskStatus> taskStatusTopic = null;
	protected BuiltinStream<Result> resultsTopic = null;
	protected BuiltinStream<StreamMetadataSnapshot> streamMetadataTopic = null;
	protected BuiltinStream<TaskStatus> taskMetadataTopic = null;
	protected BuiltinStream<ControlSignal> controlTopic = null;
	protected BuiltinStream<java.io.Serializable> configTopic = null;

	protected BuiltinStream<LogMessage> logTopic = null;
	protected CrossflowLogger logger = new CrossflowLogger(this);

	protected BuiltinStream<FailedJob> failedJobsQueue = null;
	protected BuiltinStream<InternalException> internalExceptionsQueue = null;

	protected List<FailedJob> failedJobs = null;

	protected List<InternalException> internalExceptions = null;

	// for master to keep track of active and terminated workers
	protected Collection<String> activeWorkerIds = new HashSet<>();
	protected Collection<String> terminatedWorkerIds = new HashSet<>();
	protected Serializer serializer = new Serializer();

	// excluded tasks from workers
	protected Collection<String> tasksToExclude = new LinkedList<>();

	protected Collection<ExecutorService> executorPools = new LinkedList<>();

	public ExecutorService newExecutor() {
		ExecutorService executor = Executors.newFixedThreadPool(parallelization);
		executorPools.add(executor);
		return executor;
	}

	/**
	 * Sets whether tasks are able to obtain more jobs while they are in the middle
	 * of processing one already
	 */
	protected boolean enablePrefetch = false;

	public void setEnableStomp(boolean enable) {
		enableStomp = enable;
	}

	public void setEnableWS(boolean enable) {
		enableWS = enable;
	}

	public void setActiveMqConfig(String activeMqConfig) {
		this.activeMqConfig = activeMqConfig;
	}

	public String getActiveMqConfig() {
		return activeMqConfig;
	}

	public void excludeTasks(Collection<String> tasks) {
		tasksToExclude = tasks;
	}

	public boolean isCreateBroker() {
		return createBroker;
	}

	public void createBroker(boolean createBroker) {
		this.createBroker = createBroker;
	}

	protected Timer terminationTimer;
	protected Timer streamMetadataTimer;
	boolean aboutToTerminate = false;
	protected boolean terminated = false;
	protected boolean terminating = false;
	private boolean terminationEnabled = true;

	/**
	 * used to manually add local workers to master as they may be enabled too
	 * quickly to be registered using the control topic when on the same machine
	 */
	public void addActiveWorkerId(String id) {
		logger.log(SEVERITY.INFO, "Adding worker " + id);
		activeWorkerIds.add(id);
	}

	// terminate workflow on master after this time (ms) regardless of confirmation
	// from workers
	private int terminationTimeout = 10000;
	// time in milliseconds between stream metadata updates
	private int streamMetadataPeriod = 200;

	private int taskChangePeriod = 1000;

	public void setTerminationTimeout(int timeout) {
		terminationTimeout = timeout;
	}

	public int getTerminationTimeout() {
		return terminationTimeout;
	}

	public Workflow() {
		taskStatusTopic = new BuiltinStream<>(this, "TaskStatusPublisher");
		resultsTopic = new BuiltinStream<>(this, "ResultsBroadcaster");
		streamMetadataTopic = new BuiltinStream<>(this, "StreamMetadataBroadcaster");
		taskMetadataTopic = new BuiltinStream<>(this, "TaskMetadataBroadcaster");
		controlTopic = new BuiltinStream<>(this, "ControlTopic");
		configTopic = new BuiltinStream<>(this, "ConfigurationTopic");
		logTopic = new BuiltinStream<>(this, "LogTopic");
		failedJobsQueue = new BuiltinStream<>(this, "FailedJobs", false);
		internalExceptionsQueue = new BuiltinStream<>(this, "InternalExceptions", false);

		instanceId = UUID.randomUUID().toString();
	}

	private HashMap<String, String> displayedTaskStatuses = new HashMap<>();
	private HashMap<String, Long> waitingTaskStatuses = new HashMap<>();
	private HashSet<String> activeTimers = new HashSet<>();
	private Timer taskStatusDelayedUpdateTimer = new Timer();

	protected void connect() throws Exception {

		if (tempDirectory == null) {
			tempDirectory = Files.createTempDirectory("crossflow").toFile();
		}
		taskStatusTopic.init();
		resultsTopic.init();
		streamMetadataTopic.init();
		taskMetadataTopic.init();
		controlTopic.init();
		configTopic.init();
		logTopic.init();
		failedJobsQueue.init();
		internalExceptionsQueue.init();

		activeStreams.add(taskStatusTopic);
		activeStreams.add(configTopic);
		activeStreams.add(failedJobsQueue);
		activeStreams.add(internalExceptionsQueue);
		// XXX do not add this topic/queue or any other non-essential ones to
		// activestreams as the workflow should be able to terminate regardless of their
		// state
		// activeStreams.add(resultsTopic);
		// activeStreams.add(controlTopic);
		// activeStreams.add(streamMetadataTopic);

		controlTopic.addConsumer(new BuiltinStreamConsumer<ControlSignal>() {

			@Override
			public void consume(ControlSignal signal) {
				// System.err.println("consumeControlTopic on " + getName() + " : " +
				// signal.getSignal() + " : " + signal.getSenderId());
				if (isMaster()) {
					switch (signal.getSignal()) {
					case ACKNOWLEDGEMENT:
						terminatedWorkerIds.add(signal.getSenderId());
						break;
					case WORKER_ADDED:
						activeWorkerIds.add(signal.getSenderId());
						break;
					case WORKER_REMOVED:
						activeWorkerIds.remove(signal.getSenderId());
						break;
					default:
						break;
					}

				}
				else if (signal.getSignal().equals(ControlSignals.TERMINATION)) {
					try {
						terminate();
					} catch (Exception e) {
						unrecoverableException(e);
					}
				}
			}
		});

		// XXX if the worker sends this before the master is listening to this topic
		// this information is lost which affects termination
		if (!isMaster())
			controlTopic.send(new ControlSignal(ControlSignals.WORKER_ADDED, getName()));

		if (isMaster()) {
			taskStatusTopic.addConsumer(new BuiltinStreamConsumer<TaskStatus>() {

				@Override
				public void consume(TaskStatus status) {
					// NumberPairSource:Master : INPROGRESS
					// System.err.println("consumeTaskStatusTopic on " + getName() + " : " +
					// status.getCaller() + " : " + status.getStatus());
					switch (status.getStatus()) {

					case INPROGRESS: {
						activeJobs.add(status.getCaller());
						cancelTermination();
						break;
					}
					case WAITING: {
						activeJobs.remove(status.getCaller());
						break;
					}
					default:
						break;
					}

					// sending to task metadata logic
					try {
						int colonIndex = status.getCaller().indexOf(":");
						String taskName = status.getCaller().substring(0,
								colonIndex > 0 ? colonIndex : status.getCaller().length());

						long time = System.currentTimeMillis();

						// if the task is new (not displayed yet), send its initial status
						if (!displayedTaskStatuses.containsKey(taskName)) {
							taskMetadataTopic.send(status);
							displayedTaskStatuses.put(taskName, status.getStatus() + ":" + time);
							//System.out.println("updating task " + taskName + " from NEW to " + status.getStatus());
							return;
						}

						// if a task was in the waiting list (delayed display change) but has a status
						// not waiting arrive in the meantime, remove it from that list
						if (!status.getStatus().equals(TaskStatuses.WAITING))
							waitingTaskStatuses.remove(taskName);

						String[] displayedSplit = displayedTaskStatuses.get(taskName).split(":");

						// if the task is not currently displayed as inprogress or the new status is not
						// waiting
						if (!displayedSplit[0].equals(TaskStatuses.INPROGRESS.toString())
								|| !status.getStatus().equals(TaskStatuses.WAITING)) {
							// immediate visual update unless status is already finished
							if (!displayedSplit[0].equals(status.getStatus().toString())
									&& !displayedSplit[0].equals(TaskStatuses.FINISHED.toString())) {
								taskMetadataTopic.send(status);
								displayedTaskStatuses.put(taskName, status.getStatus() + ":" + time);
								//System.out.println("updating task " + taskName + " from " + displayedSplit[0] + " to "
										//+ status.getStatus());
							}
							// if the task is displayed as in progress and the new status is waiting --
							// delay the visual update
						} else {
							// add the task to the delayed waiting list if it is not there (keep earliest
							// time it was added to it)
							if (!waitingTaskStatuses.containsKey(taskName))
								waitingTaskStatuses.put(taskName, time);

							// create a delayed trigger (only once per task per cycle) for updating the ui
							// to waiting if upon firing the
							// task has not been updated since

							if (!activeTimers.contains(taskName)) {
								activeTimers.add(taskName);
								taskStatusDelayedUpdateTimer.schedule(new TimerTask() {

									@Override
									public void run() {
										activeTimers.remove(taskName);
										//
										long delayedtime = System.currentTimeMillis();
										//String[] dSplit = displayedTaskStatuses.get(taskName).split(":");
										if (waitingTaskStatuses.containsKey(taskName) && (delayedtime
												- waitingTaskStatuses.get(taskName) > taskChangePeriod)) {
											waitingTaskStatuses.remove(taskName);
											displayedTaskStatuses.put(taskName, status.getStatus() + ":" + delayedtime);
											//
											//System.out.println("updating task " + taskName + " from " + dSplit[0]
													//+ " to " + status.getStatus() + " (DELAYED)");
											try {
												taskMetadataTopic.send(status);
											} catch (Exception e) {
												System.err.println(
														"Error in delayed task status metadata update timer task:");
												e.printStackTrace();
											}
										} else {
											// System.out.println("Delayed update did not update task:" + taskName);
											// System.out.println(waitingTaskStatuses.containsKey(taskName)
											// ? (delayedtime - waitingTaskStatuses.get(taskName))
											// : "n/a");
										}
									}
								}, (long) (taskChangePeriod * 1.1));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}

			});

			failedJobs = new ArrayList<>();
			failedJobsQueue.addConsumer(new BuiltinStreamConsumer<FailedJob>() {

				@Override
				public void consume(FailedJob failedJob) {
					failedJob.getException().printStackTrace();
					failedJobs.add(failedJob);
				}
			});

			logTopic.addConsumer(new BuiltinStreamConsumer<LogMessage>() {

				@Override
				public void consume(LogMessage message) {
					// TODO do we need to persist log?
					System.out.println(message.toString());
					//
				}
			});

			internalExceptions = new ArrayList<>();
			internalExceptionsQueue.addConsumer(new BuiltinStreamConsumer<InternalException>() {

				@Override
				public void consume(InternalException internalException) {
					internalException.getException().printStackTrace();
					internalExceptions.add(internalException);
				}
			});

			if (terminationEnabled) {

				// timer for managing automatic workflow termination
				terminationTimer = new Timer();

				terminationTimer.schedule(new TimerTask() {

					@Override
					public void run() {

						// System.err.println(activeJobs.size() + " " + areStreamsEmpty());

						boolean canTerminate = activeJobs.size() == 0 && areStreamsEmpty();

						if (aboutToTerminate) {
							if (canTerminate) {
								terminate();
							}
						} else {
							aboutToTerminate = canTerminate;
						}
					}
				}, delay, 2000);

			}

			// timer for publishing stream metadata
			streamMetadataTimer = new Timer();

			streamMetadataTimer.schedule(new TimerTask() {

				@Override
				public void run() {
					StreamMetadataSnapshot sm = new StreamMetadataSnapshot();
					//
					for (Stream c : new ArrayList<>(activeStreams)) {

						try {

							for (String destinationName : c.getDestinationNames()) {

								String destinationType = c.isBroadcast() ? "Topic" : "Queue";

								String url = "service:jmx:rmi:///jndi/rmi://" + master + ":1099/jmxrmi";
								JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(url));
								MBeanServerConnection connection = connector.getMBeanServerConnection();

								ObjectName destination = new ObjectName(
										"org.apache.activemq:type=Broker,brokerName=" + master + ",destinationType="
												+ destinationType + ",destinationName=" + destinationName);

								DestinationViewMBean mbView = MBeanServerInvocationHandler.newProxyInstance(connection,
										destination, DestinationViewMBean.class, true);

//									System.err.println(destinationName + ":" 
//											+ destinationType + " " 
//											+ mbView.getQueueSize() + " "
//											+ mbView.getInFlightCount());

								try {

									sm.addStream(destinationName, mbView.getQueueSize(), mbView.getInFlightCount(),
											c.isBroadcast(), mbView.getConsumerCount());

								} catch (Exception ex) {
									// Ignore exception
								}

								connector.close();

							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					//
					try {
						streamMetadataTopic.send(sm);
					} catch (Exception e) {
						// Ignore exception
						// e.printStackTrace();
					}

				}
			}, 1000, streamMetadataPeriod);

		}
	}

	public void cancelTermination() {
		aboutToTerminate = false;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Cache getCache() {
		return cache;
	}

	public void setCache(Cache cache) {
		//
		cacheEnabled = cache != null;
		//
		this.cache = cache;
		//
		if (cache != null)
			cache.setWorkflow(this);
		//
	}

	public boolean isMaster() {
		return mode == Mode.MASTER || mode == Mode.MASTER_BARE;
	}

	public boolean isWorker() {
		return mode == Mode.MASTER || mode == Mode.WORKER;
	}

	public Mode getMode() {
		return mode;
	}

	public void setMaster(String master) {
		this.master = master;
	}

	public String getMaster() {
		return master;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getBroker() {
		// adds a more lenient delay for heavily loaded servers (60 instead of 10 sec)
		return "tcp://" + master + ":" + port + "?wireFormat.maxInactivityDurationInitalDelay=60000";
	}

	// TODO: Fix this to allow dynamic port
	public String getStompBroker() {
		return "stomp://" + master + ":" + stompPort;
	}

	// TODO: Fix this to allow dynamic port
	public String getWSBroker() {
		return "ws://" + master + ":" + wsPort;
	}

	public void stopBroker() throws Exception {
		brokerService.deleteAllMessages();
		// brokerService.stopAllConnectors(new ServiceStopper());
		brokerService.stopGracefully("", "", 1000, 1000);
		System.out.println("terminated broker (" + getName() + ")");
		// logger.log(SEVERITY.INFO, "terminated broker (" + getName() + ")");
	}

	public void run() throws Exception {
		//
		if (cacheEnabled && cache == null) {
			logger.log(SEVERITY.INFO,
					"cacheEnabled==true but no cache was defined, creating a default DirectoryCache in the temp folder of the machine.");
			DirectoryCache c = new DirectoryCache();
			setCache(c);
		}
		if (deleteCache != null)
			cache.clear(deleteCache);
		//
		run(0);
	}

	protected long delay = 0;

	/**
	 * delays the execution of sources for 'delay' milliseconds. Needs to set the
	 * delay field in the superclass.
	 * 
	 * @param delay
	 * @throws Exception
	 */
	public abstract void run(long delay) throws Exception;

	private synchronized boolean areStreamsEmpty() {

		try {
			for (Stream c : new ArrayList<>(activeStreams)) {
				for (String destinationName : c.getDestinationNames()) {

					String destinationType = c.isBroadcast() ? "Topic" : "Queue";

					String url = "service:jmx:rmi:///jndi/rmi://" + master + ":1099/jmxrmi";
					JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(url));
					MBeanServerConnection connection = connector.getMBeanServerConnection();

					ObjectName destination = new ObjectName("org.apache.activemq:type=Broker,brokerName=" + master
							+ ",destinationType=" + destinationType + ",destinationName=" + destinationName);

					DestinationViewMBean mbView = MBeanServerInvocationHandler.newProxyInstance(connection, destination,
							DestinationViewMBean.class, true);

					try {

//						System.err.println(getName() + " : " + getInstanceId());
//						System.err.println(destinationName + ":" + destinationType + " " + mbView.getQueueSize() + " "
//								+ mbView.getInFlightCount());

						if (!c.isBroadcast()) {
							if (mbView.getQueueSize() > 0) {
								return false;
							}
						} else {
							if (mbView.getInFlightCount() > 1) {
								return false;
							}
						}

					} catch (Exception ex) {
						// ex.printStackTrace();
						// Ignore exception
					}

					connector.close();

				}
			}
		} catch (Exception ex) {
			unrecoverableException(ex);
			return true;
		}

		return true;

	}

	public synchronized void terminate() {

		if (terminated)
			return;

		terminating = true;
		
		if (terminationTimer != null)
			terminationTimer.cancel();

		// close all tasks

		for (Task t : tasks)
			t.close();

		// send task termination to metadata
		try {
			for (Task t : tasks)
				taskMetadataTopic.send(new TaskStatus(TaskStatuses.FINISHED, t.getId(), ""));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			// master graceful termination logic
			if (isMaster()) {

				// ask all workers to terminate
				logger.log(SEVERITY.INFO, "Asking all workers to terminate.");
				controlTopic.send(new ControlSignal(ControlSignals.TERMINATION, getName()));

				long startTime = System.currentTimeMillis();
				// wait for workers to terminate or for the termination timeout
				while ((System.currentTimeMillis() - startTime) < terminationTimeout) {
					// System.out.println(terminatedWorkerIds);
					// System.out.println(activeWorkerIds);
					if (terminatedWorkerIds.equals(activeWorkerIds)) {
						logger.log(SEVERITY.INFO, "All workers terminated, terminating master.");
						System.out.println("all workers terminated, terminating master...");
						break;
					}
					Thread.sleep(100);
				}
				System.out.println("terminating master...");
				logger.log(SEVERITY.INFO, "Terminating master...");

			}

			// termination logic
			System.out.println("terminating workflow... (" + getName() + ")");

			if (isMaster()) {
				//
				streamMetadataTimer.cancel();

			}

			try {
				streamMetadataTopic.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}

			if (isMaster()) {

				try {
					controlTopic.stop();
				} catch (Exception e) {
					// Ignore any exception
					e.printStackTrace();
				}
				activeStreams.remove(controlTopic);

			} else {
				controlTopic.send(new ControlSignal(ControlSignals.ACKNOWLEDGEMENT, getName()));

				try {
					controlTopic.stop();
				} catch (Exception e) {
					// Ignore any exception
					e.printStackTrace();
				}
				activeStreams.remove(controlTopic);
			}

			// stop all permanent streams

			try {
				configTopic.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}
			
			try {
				resultsTopic.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}

			try {
				logTopic.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}

			activeStreams.remove(configTopic);
			activeStreams.remove(resultsTopic);
			activeStreams.remove(logTopic);

			//
			// stop all remaining stream connections
			for (Stream stream : activeStreams) {
				try {
					stream.stop();
				} catch (Exception e) {
					// Ignore any exception
					e.printStackTrace();
				}
			}

			try {
				taskStatusTopic.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}

			if (taskStatusDelayedUpdateTimer != null)
				taskStatusDelayedUpdateTimer.cancel();

			try {
				failedJobsQueue.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}
			try {
				internalExceptionsQueue.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}
			try {
				taskMetadataTopic.stop();
			} catch (Exception e) {
				// Ignore any exception
				e.printStackTrace();
			}

			// destroy all thread pools used by tasks
			for (ExecutorService executor : executorPools) {
				List<Runnable> pending = executor.shutdownNow();
				if (pending.size() > 0)
					System.err.println("WARNING: there were pending tasks in the threadpool upon termination!");
			}

			if (isMaster()) {
				//
				System.out.println("createBroker: " + createBroker);
				if (createBroker) {
					stopBroker();
				}
			}

			terminated = true;
			notifyAll();
			System.out.println("workflow " + getName() + " terminated.");
			//
		} catch (Exception ex) {
			// There is nothing to do at this stage -- print error for debugging purposes
			// only
			ex.printStackTrace();
		}
	}

	public boolean isTerminating() {
		return terminating;
	}
	
	public boolean hasTerminated() {
		return terminated;
	}

	public BuiltinStream<TaskStatus> getTaskStatusTopic() {
		return taskStatusTopic;
	}

	public BuiltinStream<Result> getResultsTopic() {
		return resultsTopic;
	}

	public BuiltinStream<StreamMetadataSnapshot> getStreamMetadataTopic() {
		return streamMetadataTopic;
	}

	public BuiltinStream<ControlSignal> getControlTopic() {
		return controlTopic;
	}

	public BuiltinStream<java.io.Serializable> getConfigurationTopic() {
		return configTopic;
	}
	
	public BuiltinStream<FailedJob> getFailedJobsQueue() {
		return failedJobsQueue;
	}

	public BuiltinStream<LogMessage> getLogTopic() {
		return logTopic;
	}

	public List<FailedJob> getFailedJobs() {
		return failedJobs;
	}

	public List<InternalException> getInternalExceptions() {
		return internalExceptions;
	}

	public void reportInternalException(Exception ex) {
		try {
			internalExceptionsQueue.send(new InternalException(ex, this.getName()));
		} catch (Exception e) {
			unrecoverableException(e);
		}
	}

	public void unrecoverableException(Exception ex) {
		ex.printStackTrace();
	}

	public void setTaskInProgess(Task caller) throws Exception {
		taskStatusTopic.send(new TaskStatus(TaskStatuses.INPROGRESS, caller.getId(), ""));
	}

	public void setTaskWaiting(Task caller) throws Exception {
		taskStatusTopic.send(new TaskStatus(TaskStatuses.WAITING, caller.getId(), ""));
	}

	public void setTaskBlocked(Task caller, String reason) throws Exception {
		taskStatusTopic.send(new TaskStatus(TaskStatuses.BLOCKED, caller.getId(), reason));
	}

	public void setTaskUnblocked(Task caller) throws Exception {
		taskStatusTopic.send(new TaskStatus(TaskStatuses.INPROGRESS, caller.getId(), ""));
	}

	public void setTaskFinished(Task caller) throws Exception {
		taskStatusTopic.send(new TaskStatus(TaskStatuses.FINISHED, caller.getId(), ""));
	}

	public File getInputDirectory() {
		return inputDirectory;
	}

	public void setInputDirectory(File inputDirectory) {
		this.inputDirectory = inputDirectory;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	public void setRuntimeModel(File runtimeModel) {
		this.runtimeModel = runtimeModel;
	}

	public File getRuntimeModel() {
		return runtimeModel;
	}

	public File getTempDirectory() {
		return tempDirectory;
	}

	public void setTempDirectory(File tempDirectory) {
		this.tempDirectory = tempDirectory;
	}

	public Serializer getSerializer() {
		return serializer;
	}

	/**
	 * default = 3000
	 * 
	 * @param p
	 */
	public void setStreamMetadataPeriod(int p) {
		streamMetadataPeriod = p;
	}

	public int getStreamMetadataPeriod() {
		return streamMetadataPeriod;
	}

	/**
	 * 
	 * @return A set containing all ActiveMQDestination objects used by all active
	 *         JobStreams
	 */
	public Set<ActiveMQDestination> getAllJobStreamsInternals() {
		Set<ActiveMQDestination> ret = new HashSet<>();
		activeStreams.stream().filter(s -> s instanceof JobStream)
				.forEach(js -> ret.addAll(((JobStream<?>) js).getAllQueues()));
		return ret;
	}

	/**
	 * 
	 * @return Whether queues used by this workflow will keep providing messages to
	 *         consumers before they are finished with their current task (default =
	 *         false)
	 */
	public boolean isEnablePrefetch() {
		return enablePrefetch;
	}

	/**
	 * 
	 * @param enablePrefetch Sets whether queues used by this workflow will keep
	 *                       providing messages to consumers before they are
	 *                       finished with their current task (default = false)
	 */
	public void setEnablePrefetch(boolean enablePrefetch) {
		this.enablePrefetch = enablePrefetch;
	}

	/**
	 * 
	 * @return The maximum number of parallel instances for each non source/sink
	 *         task running in this workflow
	 */
	public int getParallelization() {
		return parallelization;
	}

	/**
	 * 
	 * @param parallelization Sets the maximum number of parallel instances for each
	 *                        non source/sink task running in this workflow (default
	 *                        = 1)
	 */
	public void setParallelization(int parallelization) {
		if (parallelization > 0)
			this.parallelization = parallelization;
	}

	/**
	 * For master workflows this flag determines whether automatic workflow
	 * termination will be triggered when there are no more messages in any queue
	 * and no tasks are in progress
	 * 
	 * @return Whether this workflow will terminate automatically
	 */
	public boolean isTerminationEnabled() {
		return terminationEnabled;
	}

	/**
	 * For master workflows this flag determines whether automatic workflow
	 * termination will be triggered when there are no more messages in any queue
	 * and no tasks are in progress
	 * 
	 * @param enableTermination Sets whether this workflow will automatically
	 *                          terminate (default = true)
	 */
	public void setEnableTermination(boolean enableTermination) {
		terminationEnabled = enableTermination;
	}

	public void log(SEVERITY level, String message) {
		logger.log(level, message);
	}
	
	/**
	 * Waits until {@link #hasTerminated()} return true.
	 */
	public synchronized void awaitTermination() {
		while (!terminated) {
			try {
				wait();
			}
			catch (InterruptedException ie) {
				logger.log(SEVERITY.INFO, ie.getMessage());
			}
		}
	}

}
