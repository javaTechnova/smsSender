package com.odesk.sms;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/config/spring.xml")
public class MessageListenerTest {

	@Autowired
	 private ApplicationContext appContext;
	
	@Test
	public void testListener(){
		MessageListener l = appContext.getBean(MessageListener.class);
		l.listen();
	}
	
}
