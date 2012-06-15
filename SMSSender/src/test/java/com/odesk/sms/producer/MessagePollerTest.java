package com.odesk.sms.producer;

import java.util.List;

import org.apache.log4j.MDC;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.odesk.sms.Message;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/config/spring.xml")
public class MessagePollerTest {

	 @Autowired
	 private ApplicationContext appContext;
	
	 @Test
	 public void testPolling(){
		MessageDAO poll = appContext.getBean("messageDao", MessageDAO.class);
		List<Message> messages = poll.fetchMessages();
		assertEquals(2,messages.size());
		assertEquals(4343035, messages.get(0).getId());
	}
	 
	 @Test
	 public void testUpdate(){
		 MessageDAO poll = appContext.getBean("messageDao", MessageDAO.class);
		 Message m = new Message();
			m.setId(4343036);
			MDC.put("msg_id", m.getId());
			m.setTo("0412345678");
			m.setFrom("0411222333");
			m.setDeliveryId("TEst123");
			m.setStatus("2");
			m.setMessage("Test Message 123");
			poll.updateMessage(m);
	 }
	
	
	
}
