package com.odesk.sms.consumer;

import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.odesk.sms.Message;
/**
 * Simulate a publisher, sleep for a random period 
 * and update message domain object
 * @author jayant
 *
 */
@Component("mockPublisher")
public class MockPublisher implements MessagePublisher {

	static private final Log log = LogFactory.getLog(MockPublisher.class);
	@Override
	public void publishMessage(Message message) {
		Random r = new Random();
		int sleepInt =0;
		try {
			sleepInt = r.nextInt(3000);
			log.debug("sleeping for "+sleepInt+" ms");
			Thread.sleep(sleepInt);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		message.setStatus("1");
		message.setDeliveryId("Del"+sleepInt);
	}

}
