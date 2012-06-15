package com.odesk.sms.consumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.odesk.sms.Message;
import com.odesk.sms.producer.MessageDAO;

@Component("messageHandler")
public class MessageHandlerImpl implements MessageHandler {

	static private final Log log = LogFactory.getLog(MessageHandlerImpl.class);
	private MessagePublisher publisher;
	
	private MessageDAO dao;
	
	/* 
	 * @see com.odesk.sms.consumer.MessageHandler#handleMessage()
	 */
	@Override
	public Message handleMessage(Message message){
		publisher.publishMessage(message);
		dao.updateMessage(message);
		log.info("Processing Completed !!!");
		return message;
	}
	
	@Autowired
	@Qualifier("fiveCentsPublisher")
	public void setPublisher(MessagePublisher publisher) {
		this.publisher = publisher;
	}

	@Autowired
	public void setDao(MessageDAO dao) {
		this.dao = dao;
	}
	
	
}
