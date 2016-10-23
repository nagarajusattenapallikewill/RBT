package com.onmobile.apps.ringbacktones.daemons;

import java.util.concurrent.ThreadFactory;

public class RequestThreadFactory implements ThreadFactory
{
	RequestType requestType;
	int serialNo = 1;
	public RequestThreadFactory(RequestType requestType)
	{
		this.requestType = requestType;
	}

	@Override
	public Thread newThread(Runnable arg0)
	{
		return new Thread(arg0, requestType+"-"+serialNo++);
	}
	
	
}
