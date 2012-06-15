package com.odesk.sms.producer;

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
public class MessageProducerTest {

	@Autowired
	 private ApplicationContext appContext;
	
	 @Test
	 public void startProducing() throws InterruptedException{
		MessageProducer producer = appContext.getBean("messageProducer", MessageProducer.class);
		producer.start();
		QueueManager m = appContext.getBean("queueManager",QueueManager.class);
		 m.start();
		Thread.currentThread().sleep(15000);
		//SmsLauncher.shutdown= true;
		
	}
}
