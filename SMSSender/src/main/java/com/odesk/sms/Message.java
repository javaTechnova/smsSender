package com.odesk.sms;


/**
 * domain object representing an SMS message
 * @author jayant
 *
 */
public class Message {

	private int id;
	private String deliveryId;
	private int userId;
	private String status;
	private String to;
	private String from;
	private int credits;
	private String message;
	private String date;
	private String date2;
	private long inTime;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public int getCredits() {
		return credits;
	}
	public void setCredits(int credits) {
		this.credits = credits;
	}
	public String getDeliveryId() {
		return deliveryId;
	}
	public void setDeliveryId(String deliveryId) {
		this.deliveryId = deliveryId;
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getDate2() {
		return date2;
	}
	public void setDate2(String date2) {
		this.date2 = date2;
	}
	
	public long getInTime() {
		return inTime;
	}
	public void setInTime(long inTime) {
		this.inTime = inTime;
	}
	
	
	@Override
	public int hashCode(){
		return id;
	}
	
	@Override
	public boolean equals(Object o){
		if(o != null && o instanceof Message){
			if(((Message)o).getId() == this.getId()){
				return true;
			}
		}
		return false;
	}
	
}
