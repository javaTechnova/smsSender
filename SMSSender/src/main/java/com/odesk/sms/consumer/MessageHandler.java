package com.odesk.sms.consumer;

import com.odesk.sms.Message;

/**
 * Top Level interface to handle message, in this case perform the following task
 * i) Invoke 5 cents API to publish message
 * ii) Update database with delivery, status information
 * @author jayant
 *
 */
public interface MessageHandler {

	public Message handleMessage(Message message);

}