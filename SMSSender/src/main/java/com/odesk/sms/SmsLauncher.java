package com.odesk.sms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.odesk.sms.consumer.QueueManager;
import com.odesk.sms.producer.MessageProducer;


/**
 * Entry point to configure and launch the SMS Sender app
 * @author jay
 *
 */

public class SmsLauncher {

	static private final Log log = LogFactory.getLog(SmsLauncher.class); 
	 /*This flag will be used by the following to initiate an orderly shutdown
	 a) Producer Thread
	 b) Consumer Thread
	 c) Server Socket
	 This flag will be set once the client sends an 'exit' message to the server
	 */
	public static volatile boolean shutdown = false;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final ClassPathXmlApplicationContext ctx;
		if(args.length ==1 && "prod".equals(args[0])){
			ctx = new ClassPathXmlApplicationContext("/spring-live.xml");
		}else
			ctx = new ClassPathXmlApplicationContext("/spring.xml");
		
		QueueManager queueMgr = ctx.getBean(QueueManager.class);
		queueMgr.start();
		MessageProducer producer = ctx.getBean(MessageProducer.class);
		producer.start();
		MessageListener listener = ctx.getBean(MessageListener.class);
		//register observers
		listener.addObserver(queueMgr);
		listener.addObserver(producer);
		listener.listen();
		// add a shutdown hook to close spring application context
		Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
            		ctx.close();
            		
	        }
		});
		// enable benchmark
		if(System.getProperty("benchmarkEnabled")!= null 
				&& "true".equals(System.getProperty("benchmarkEnabled"))){
			Thread t = new Thread(new Benchmark(queueMgr));
			t.setDaemon(true);
			t.start();
		}
	}
	
	static class Benchmark implements Runnable{

		private QueueManager q;
		Benchmark(QueueManager q){
			this.q = q;
		}
		static private final Log log = LogFactory.getLog(Benchmark.class);
		// default to 10 seconds
		private long sleepInterval = System.getProperty("benchmarklogInterval")!= null?
									Long.valueOf(System.getProperty("benchmarklogInterval")):10000L;
		@Override
		public void run() {
			while(true){
				log.info("********* Benchmark Information ***********");
				log.info("Total Memory = "+Runtime.getRuntime().totalMemory()/(1024*1024)+ "MB");
				log.info("Free Memory = "+Runtime.getRuntime().freeMemory()/(1024*1024)+ "MB" );
				log.info("Max Memory = "+Runtime.getRuntime().maxMemory()/(1024*1024)+ "MB");
				log.info("Approximate number of active threads executing tasks = "+q.getActiveThreadCount());
				//log.info("Approximate number of tasks scheduled for execution = "+q.getTaskCount());
				log.info("********* Benchmark Information ***********");
				try {
					Thread.sleep(sleepInterval);
				} catch (InterruptedException e) {
					log.error("Benchmakr thread interrupted, message ="+e.getMessage());
				}
			}
			
		}
		
	}
}
