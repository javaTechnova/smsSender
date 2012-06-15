package com.odesk.sms.consumer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.odesk.sms.Message;

@Component("fiveCentsPublisher")
public class FiveCentsMessagePublisher implements MessagePublisher {

	static private final Log log = LogFactory.getLog(FiveCentsMessagePublisher.class);
	
	private DefaultHttpClient httpClient;
	@Value("${http.connection.timeout}")
	private int connectionTimeout;
	
	@Value("${http.base.url}")
	private String httpURL;
	
	@Value("${fivecents.username}")
	private String userName;
	
	@Value("${fivecents.pwd}")
	private String password;


	@Value("${mock.5cents}")
	// defaults to false
	private boolean isMock = false;
	
	public void init(){
		httpClient = new DefaultHttpClient();
		httpClient.getParams().setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
	}
	
	/* (non-Javadoc)
	 * @see com.odesk.sms.consumer.MessagePublisher#publishMessage(com.odesk.sms.Message)
	 */
	@Override
	public void publishMessage(Message message){
		// simulate mock behavior while testing
		if(isMock){
			publishMockMessage(message);
		}else{
			HttpGet get = null;
			BufferedReader br = null;
			try {
				StringBuilder builder = new StringBuilder(httpURL);
				builder.append("?username=")
					   .append(URLEncoder.encode(userName,"UTF-8"))
					   .append("&password=")
					   .append(URLEncoder.encode(password,"UTF-8"))
					   .append("&to=")
					   .append(message.getTo())
					   .append("&sender=")
					   .append(message.getFrom())
					   .append("&message=")
					   .append(URLEncoder.encode(message.getMessage(),"UTF-8"));
				log.debug("URL for sending request to 5 cents = "+builder.toString());
				
					
					get = new HttpGet(builder.toString());
					HttpResponse response = httpClient.execute(get);
					if (response.getStatusLine().getStatusCode() != 200) {
						throw new RuntimeException("Failed : HTTP error code : "
						   + response.getStatusLine().getStatusCode());
					}
			 
					br = new BufferedReader(
			                         new InputStreamReader((response.getEntity().getContent())));
					String output = br.readLine();
			 		log.info("Output from 5cents Server = "+output);
			 		if(output != null){
			 			try{
			 				String[] arr = output.split("\\|");
			 				if("0".equals(arr[0])){
			 					log.info("Message rejected by 5 cents, rejection reason="+arr[3]);
			 				}
			 				message.setStatus(arr[0]);
			 				message.setDeliveryId(arr[2]);
			 			}catch(Exception e){
			 				//ignoring runtime error so threads dont die, if status is available we should be 
			 				// able to consider this as successful transaction
			 				log.warn("Output from 5 cents did not contain the relavent fields [either status/delivery Id missing");
			 			}
			 		}
			
				} catch (ClientProtocolException e) {
					log.error("ClientProtocolException thrown while trying to publish message to 5 cents, message="+e.getMessage());
					throw new RuntimeException(e);
				}catch (UnsupportedEncodingException e1) {
					throw new RuntimeException(e1);
				}catch (IOException e) {
					log.error("IOException thrown while trying to publish message to 5 cents, message="+e.getMessage());
					throw new RuntimeException(e);
				} finally{
					if(br!= null){
						try {
							br.close();
						} catch (IOException e) {
							log.error("IOException while trying to close IO stream ="+e.getMessage());
							throw new RuntimeException(e);
						}
					}
					get.releaseConnection();
				}
			}	
	}
	
	 void publishMockMessage(Message message) {
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
	
	
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public void setHttpURL(String httpURL) {
		this.httpURL = httpURL;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
	public void setMock(boolean isMock) {
		this.isMock = isMock;
	}


	// called by Spring Context
	public void destroy(){
		log.info("Shutting HttpClient Connection Manager down");
		httpClient.getConnectionManager().shutdown();
	}
}
