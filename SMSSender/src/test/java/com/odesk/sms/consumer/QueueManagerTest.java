package com.odesk.sms.consumer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.odesk.sms.SmsLauncher;
import com.odesk.sms.consumer.QueueManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/config/spring.xml")
public class QueueManagerTest {
	
	@Autowired
	 private ApplicationContext appContext;
	
	 @Test
	 public void checkQueueManager() throws InterruptedException{
		 QueueManager m = appContext.getBean("queueManager",QueueManager.class);
		 m.start();
		 Thread.sleep(10000);
		 SmsLauncher.shutdown= true;
	 }

}
