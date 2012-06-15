package com.odesk.sms;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

	private String name;
	private NamedThreadFactory() {}
	private int threadCnt = 0;
	
	public NamedThreadFactory(String name) {
		super();
		this.name = name;
	}
	
	public Thread newThread(Runnable run) {
		this.threadCnt++;
		return new Thread(run, this.name + "-" + this.threadCnt);
	}
}
