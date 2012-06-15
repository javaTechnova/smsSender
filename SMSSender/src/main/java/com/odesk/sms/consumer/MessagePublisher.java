package com.odesk.sms.consumer;

import com.odesk.sms.Message;

public interface MessagePublisher {

	public abstract void publishMessage(Message message);

}