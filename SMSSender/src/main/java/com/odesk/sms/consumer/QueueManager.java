package com.odesk.sms.consumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.odesk.sms.Message;
import com.odesk.sms.NamedThreadFactory;
import com.odesk.sms.ShutdownNotifier;
import com.odesk.sms.SmsLauncher;

@Component("queueManager")

public class QueueManager implements ShutdownNotifier{

	static private final Log log = LogFactory.getLog(QueueManager.class);
	//internal queue size
	@Value("${message.queue.size}")
	private int queueSize;
	
	@Value("${thread.queue.size}")
	private int threadQueueSize;
	
	//thread pool manager
	private ExecutorService queueManager;
	
	// number of threads in the pool
	@Value("${thread.pool.size}")
	private int threadPoolSize;
	
	// time to keep threads alive in ms
	@Value("${thread.keepAlive.time}")
	private long keepAliveTime;
	
	// time to wait before terminating running threads
	// preventing threads to hang on forever
	private long longRunningThreadInterval;
	
	// thread Factory
	private NamedThreadFactory namedThreadFactory;
	
	
	private ArrayBlockingQueue<Message> messageQueue;
	
	private MessageHandler handler;
	
	

	public void init(){
		messageQueue = new ArrayBlockingQueue<Message>(queueSize); 
		// initializing the Internal Queue Manager
		queueManager = new ThreadPoolExecutor(threadPoolSize,
											  threadPoolSize,
											  keepAliveTime,
											  TimeUnit.MILLISECONDS,
											  new ArrayBlockingQueue<Runnable>(threadQueueSize),
											  namedThreadFactory,
											  new ThreadPoolExecutor.CallerRunsPolicy());
	}
	
	public void start(){
		// run the actual polling as part of a separate thread
		Thread t = new Thread(new Runnable(){
		@Override
		public void run() {
			while(!SmsLauncher.shutdown){
				Message message = null;
				//message = messageQueue.poll();
				try {
					message = messageQueue.take();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(message != null){
					final Message m = message;
					log.debug("Time spent by message Id["+message.getId()+"] in internal queue="+(System.currentTimeMillis()-message.getInTime())+" ms");
					queueManager.execute(new Runnable(){
						@Override
						public void run() {
							MDC.put("msg_id", m.getId());
							// handler does the job of 
							//a)publishing to 5 cents
							//b)updating local database
							handler.handleMessage(m);	
						}
					});
			}
		}}});
		t.start();
	}	
		
	public void setQueueSize(int queueSize) {
		this.queueSize = queueSize;
	}


	public void setThreadPoolSize(int threadPoolSize) {
		this.threadPoolSize = threadPoolSize;
	}


	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}


	@Autowired
	@Qualifier("consumerThreadFactory")
	public void setNamedThreadFactory(NamedThreadFactory namedThreadFactory) {
		this.namedThreadFactory = namedThreadFactory;
	}
	
	
	public void setThreadQueueSize(int threadQueueSize) {
		this.threadQueueSize = threadQueueSize;
	}
	
	@Autowired
	public void setHandler(MessageHandler handler) {
		this.handler = handler;
	}
	/**
	 * Inserts message to queue, will block if queue full
	 * @param message
	 */
	public void addMessageToQueue(Message message){
		try {
			messageQueue.put(message);
		} catch (InterruptedException e) {
			log.error("InterruptedException while waiting to insert messages into queue");
			throw new RuntimeException(e);
		}
	} 
	
	// used for benchmark information
	public int getActiveThreadCount(){
		if(!((ThreadPoolExecutor)(queueManager)).isTerminating())
			return ((ThreadPoolExecutor)(queueManager)).getActiveCount();
		return 0;
	}
	
	// used for benchmark information
	public long getTaskCount(){
		if(!((ThreadPoolExecutor)(queueManager)).isTerminating())
			return ((ThreadPoolExecutor)(queueManager)).getTaskCount();
		return 0;
	}
	
	@Override
	public void shutdown() {
		log.info("Shutdown initiated for Scheduled Message Consumer Thread");
		queueManager.shutdown(); // Disable new tasks from being submitted
	   try {
	     // Wait a while for existing tasks to terminate
	     if (!queueManager.awaitTermination(60, TimeUnit.SECONDS)) {
	    	 queueManager.shutdownNow(); // Cancel currently executing tasks
	       // Wait a while for tasks to respond to being cancelled
	       if (!queueManager.awaitTermination(60, TimeUnit.SECONDS))
	           log.error("Consumer Pool did not terminate");
	     }
	   } catch (InterruptedException ie) {
	     // (Re-)Cancel if current thread also interrupted
		   queueManager.shutdownNow();
	     // Preserve interrupt status
	    Thread.currentThread().interrupt();
	   }
	 }
	 
}
