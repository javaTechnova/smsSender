package com.odesk.sms.producer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import com.odesk.sms.Message;
import com.odesk.sms.NamedThreadFactory;
import com.odesk.sms.ShutdownNotifier;
import com.odesk.sms.SmsLauncher;
import com.odesk.sms.consumer.QueueManager;

@Component("messageProducer")

public class MessageProducer implements ShutdownNotifier{

	static final Log log = LogFactory.getLog(MessageProducer.class);
	// inject message poller
	private MessageDAO poller;
	
	private ScheduledThreadPoolExecutor service;
	
	private NamedThreadFactory threadFactory;
	
	@Value("${message.poll.interval}")
	private long pollInterval;
	
	
	private QueueManager queueManager;
	
	
	// called by Bean post Processor when spring context initializes
	public void init(){
		// single threaded producer thread 
		service = new ScheduledThreadPoolExecutor(1, 
											threadFactory,
											new ThreadPoolExecutor.CallerRunsPolicy());
		
	}
	public void start(){
			ScheduledFuture<?> result = 
					service.scheduleAtFixedRate(new MessagePollRunnable(), 0, pollInterval, TimeUnit.MILLISECONDS);
			
		}
		
	
	
	@Autowired
	public void setPoller(MessageDAO poller) {
		this.poller = poller;
	}
	
	@Autowired
	@Qualifier("producerThreadFactory")
	public void setThreadFactory(NamedThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}
	
	
	public void setPollInterval(long pollInterval) {
		this.pollInterval = pollInterval;
	}
	
	@Autowired
	public void setQueueManager(QueueManager queueManager) {
		this.queueManager = queueManager;
	}
	
	private class MessagePollRunnable implements Runnable{
			@Override
			public void run() {
					List<Message> messages = poller.fetchMessages();
					for(Message message:messages){
						queueManager.addMessageToQueue(message);
					}
			}
	}

	@Override
	public void shutdown() {
		log.info("Shutdown initiated for Scheduled Message Producer Thread");
		service.shutdown(); // Disable new tasks from being submitted
	   try {
	     // Wait a while for existing tasks to terminate
	     if (!service.awaitTermination(60, TimeUnit.SECONDS)) {
	    	 service.shutdownNow(); // Cancel currently executing tasks
	       // Wait a while for tasks to respond to being cancelled
	       if (!service.awaitTermination(60, TimeUnit.SECONDS))
	           log.error("Consumer Pool did not terminate");
	     }
	   } catch (InterruptedException ie) {
	     // (Re-)Cancel if current thread also interrupted
		   service.shutdownNow();
	     // Preserve interrupt status
	    Thread.currentThread().interrupt();
	   }
	 }
		
}
	

	
	
	

