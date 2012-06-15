package com.odesk.sms.consumer;

import org.apache.log4j.MDC;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.odesk.sms.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/config/spring.xml")
public class FiveCentsPublisherTest {

	 @Autowired
	 private ApplicationContext appContext;
	
	 @Test
	 public void testPublish() throws InterruptedException{
		 final MessagePublisher pub = appContext.getBean(FiveCentsMessagePublisher.class);
		 for(int i=0;i<3;i++){
			 Thread t = new Thread(new Runnable(){
				@Override
				public void run() {
					Message m = new Message();
					m.setId(123);
					MDC.put("msg_id", m.getId());
					m.setTo("0412345678");
					m.setFrom("0411222333");
					m.setMessage("Test Message 123");
					pub.publishMessage(m);
					
				}	
			 });
			 t.start();
			 t.join();
			 
		 }
	 }
}
