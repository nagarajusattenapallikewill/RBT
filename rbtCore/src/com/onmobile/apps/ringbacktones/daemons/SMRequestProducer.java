package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

public class SMRequestProducer extends Thread
{
	RequestType requestType;
	int threadCount;
	boolean iContinue = true;
	int pendingRequestCount = 0;
	long zeroPendingRequestLoopCount = 0;
	static Logger logger = Logger.getLogger(SMRequestProducer.class);
	
	ThreadPoolExecutor tpe;
	ArrayList<Future<String>> futureList;
	
	public SMRequestProducer(RequestType requestType, int threadCount)
	{
		this.requestType = requestType;
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		ThreadFactory threadFactory = new RequestThreadFactory(requestType);
		tpe = new ThreadPoolExecutor(threadCount, threadCount, 5, TimeUnit.MINUTES, queue, threadFactory);
		setName(requestType+"-Producer");
		logger.info("created smrequestProducer for type "+requestType);
	}
	
	@Override
	public void run()
	{
		logger.info("started smrequestProducer for type "+requestType);
		while(iContinue)
		{
			getPendingRequests();
			if(pendingRequestCount == 0)
				sleepNow();
			else
				getResults();
			
			if(RBTDaemonManager.getParamAsInt("SMDAEMON_SLEEP_INTERVAL_SECONDS", -1) != -1)
				sleepConfiguredTime(RBTDaemonManager.getParamAsInt("SMDAEMON_SLEEP_INTERVAL_SECONDS", 1));
			else if(!RBTDaemonManager.getParamAsBoolean("PROCESS_DOWNLOADS", "FALSE") && requestType.equals(RequestType.SM_SEL_ACT))
			{
				if(RBTDaemonManager.getParamAsInt(RequestType.SM_SEL_ACT.name()+"_PRODUCER_SLEEP_SEC", -1) != -1)
					sleepConfiguredTime(RBTDaemonManager.getParamAsInt(RequestType.SM_SEL_ACT.name()+"_PRODUCER_SLEEP_SEC", -1));
			}
			else if (!RBTDaemonManager.getParamAsBoolean("PROCESS_DOWNLOADS", "FALSE") &&  requestType.equals(RequestType.SM_SEL_ACT_LOW))
			{
				if(RBTDaemonManager.getParamAsInt(RequestType.SM_SEL_ACT_LOW.name()+"_PRODUCER_SLEEP_SEC", -1) != -1)
					sleepConfiguredTime(RBTDaemonManager.getParamAsInt(RequestType.SM_SEL_ACT_LOW.name()+"_PRODUCER_SLEEP_SEC", -1));
			}
			else if (requestType.equals(RequestType.SM_DWN_ACT))
			{
				if(RBTDaemonManager.getParamAsInt(RequestType.SM_DWN_ACT.name()+"_PRODUCER_SLEEP_SEC", -1) != -1)
					sleepConfiguredTime(RBTDaemonManager.getParamAsInt(RequestType.SM_SEL_ACT_LOW.name()+"_PRODUCER_SLEEP_SEC", -1));
			}
		}
	}
	
	
	private void getResults()
	{
		logger.info("cheking futures of type "+requestType);
		zeroPendingRequestLoopCount = 0;
		for(int i = 0; i < futureList.size(); i++)
		{
			try
			{
				futureList.get(i).get();
			}
			catch(Exception e)
			{
				
			}
		}
		logger.info("done cheking futures of type "+requestType);
	}

	private void sleepNow()
	{
		int noOfItration = RBTDaemonManager.getParamAsInt("SMREQUEST_DAEMON_MAX_NO_OF_ITRATION", 6);
		int sleepInterval = RBTDaemonManager.getParamAsInt("SMREQUEST_DAEMON_SLEEP_INTERVAL_SECONDS", 15);
		long sleepTime = (++zeroPendingRequestLoopCount > noOfItration ? noOfItration :  zeroPendingRequestLoopCount )*sleepInterval ;
		logger.info("No pending smrequests of type "+requestType + ", zeroTaskLoopCount=" + zeroPendingRequestLoopCount + ", sleepTime=" + sleepTime + " sec");
		try{Thread.sleep(sleepTime*1000);}catch (Exception e) {}
	}
	
	private void sleepConfiguredTime(long sleepTime)
	{
		logger.info("Going to forced sleep of "+ sleepTime + " sec.");
		try{Thread.sleep(sleepTime*1000);}catch (Exception e) {}
	}
	
	private void getPendingRequests()
	{
		logger.info("getting sm requests of type "+ requestType);
		switch (this.requestType)
		{
			case SM_BASE_ACT:
				getBaseActRequests();
				break;
			case SM_BASE_ACT_LOW:
				getBaseActRequestsLow();
				break;
			case SM_BASE_DCT:
				getBaseDeactRequests();
				break;
			case SM_SEL_ACT:
				getSelActRequests();
				break;
			case SM_SEL_ACT_LOW:
				getSelActRequestsLow();
				break;
			case SM_SEL_DCT:
				getSelDeactRequests();
				break;
			case SM_SEL_ACT_DIR:
				getSelActDirectRequests();
				break;
			case SM_SEL_REN:
				getSelRenRequests();
				break;
			case SM_DWN_ACT:
				getDownloadActRequests();
				break;
			case SM_DWN_DCT:
				getDownloadDeactRequests();
				break;
			case SM_PACK_ACT:
				getPackActRequests();
				break;
			case SM_PACK_DCT:
				getPackDeactRequests();
				break;
			default:
				break;
		}
		logger.info("done getting sm requests of type "+ requestType+ ", pending requests found "+ pendingRequestCount);
	}

	private void getBaseActRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("BaseActivationTps");

		Subscriber[] activatedSubscribers = RBTDaemonManager.smGetActivatedSubscribers(RBTDaemonManager.m_lowPriorityModes, false);
		pendingRequestCount = activatedSubscribers == null ? 0 : activatedSubscribers.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
		for(int i = 0; i < activatedSubscribers.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSubscriber(activatedSubscribers[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("BaseActivationTps", activatedSubscribers.length);
	}
	
	private void getBaseActRequestsLow()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("BaseLowActivationTps");
		
		Subscriber[] activatedSubscribers = RBTDaemonManager.smGetActivatedSubscribers(RBTDaemonManager.m_lowPriorityModes, true);
		pendingRequestCount = activatedSubscribers == null ? 0 : activatedSubscribers.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
		for(int i = 0; i < activatedSubscribers.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSubscriber(activatedSubscribers[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("BaseLowActivationTps", activatedSubscribers.length);
	}
	
	private void getBaseDeactRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("BaseDeactivationTps");
		
		Subscriber[] deactivatedSubscribers = RBTDaemonManager.smGetDeactivatedSubscribers();
		pendingRequestCount = deactivatedSubscribers == null ? 0 : deactivatedSubscribers.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
		for(int i = 0; i < deactivatedSubscribers.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSubscriber(deactivatedSubscribers[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("BaseDeactivationTps", deactivatedSubscribers.length);
	}

	private void getSelActRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("SelectionActivationTps");
		
		SubscriberStatus[] activatedSelections = RBTDaemonManager.smGetActivatedSelections(RBTDaemonManager.m_lowPriorityModes, false);
		pendingRequestCount = activatedSelections == null ? 0 : activatedSelections.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
		
		for(int i = 0; i < activatedSelections.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSelection(activatedSelections[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("SelectionActivationTps", activatedSelections.length);
	}
	
	private void getSelActRequestsLow()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("SelectionLowActivationTps");

		SubscriberStatus[] activatedSelections = RBTDaemonManager.smGetActivatedSelections(RBTDaemonManager.m_lowPriorityModes, true);
		pendingRequestCount = activatedSelections == null ? 0 : activatedSelections.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
		
		for(int i = 0; i < activatedSelections.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSelection(activatedSelections[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("SelectionLowActivationTps", activatedSelections.length);
	}
	
	private void getSelActDirectRequests()
	{
		SubscriberStatus[] activatedSelections = RBTDaemonManager.smGetDirectActivatedSelections();
		pendingRequestCount = activatedSelections == null ? 0 : activatedSelections.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
		
		for(int i = 0; i < activatedSelections.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSelection(activatedSelections[i]);
			futureList.add(tpe.submit(smRequest));
		}
	}

	private void getSelDeactRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("SelectionDeactivationTps");
		
		SubscriberStatus[] deactivatedSelections = RBTDaemonManager.smGetDeactivatedSelections();
		pendingRequestCount = deactivatedSelections == null ? 0 : deactivatedSelections.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;

		for(int i = 0; i < deactivatedSelections.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSelection(deactivatedSelections[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("SelectionDeactivationTps", deactivatedSelections.length);
	}

	private void getSelRenRequests()
	{
		SubscriberStatus[] renewalSelections = RBTDaemonManager.smGetRenewalSelections();
		pendingRequestCount = renewalSelections == null ? 0 : renewalSelections.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;

		for(int i = 0; i < renewalSelections.length; i++)
		{
			smRequest = new SMRequest(requestType);
			smRequest.setSelection(renewalSelections[i]);
			futureList.add(tpe.submit(smRequest));
		}
	}

	private void getDownloadActRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("DownloadActivationTps");
		
		SubscriberDownloads[] activatedDownloads = RBTDaemonManager.smGetDownloadsToBeActivated();
		pendingRequestCount = activatedDownloads == null ? 0 : activatedDownloads.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;

		for(int i = 0; i < activatedDownloads.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setDownload(activatedDownloads[i]);
			futureList.add(tpe.submit(smRequest));
		}
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("DownloadActivationTps", activatedDownloads.length);
	
	}

	private void getDownloadDeactRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("DownloadDeactivationTps");
		
		SubscriberDownloads[] deactivatedDownloads = RBTDaemonManager.smGetDeactivatedDownloads();
		pendingRequestCount = deactivatedDownloads == null ? 0 : deactivatedDownloads.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;

		for(int i = 0; i < deactivatedDownloads.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setDownload(deactivatedDownloads[i]);
			futureList.add(tpe.submit(smRequest));
		}
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("DownloadDeactivationTps", deactivatedDownloads.length);
	}

	private void getPackActRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("PackActivationTps");
		
		ProvisioningRequests[] activatedPacks = RBTDaemonManager.smGetActivatedPacks();
		pendingRequestCount = activatedPacks == null ? 0 : activatedPacks.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
	
		for(int i = 0; i < activatedPacks.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setProvisioningRequests(activatedPacks[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("PackActivationTps", activatedPacks.length);
	}
	
	private void getPackDeactRequests()
	{
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.endTpsSampling("PackDeactivationTps");
		
		ProvisioningRequests[] deactivatedPacks = RBTDaemonManager.smGetDeactivatedPacks();
		pendingRequestCount = deactivatedPacks == null ? 0 : deactivatedPacks.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		SMRequest smRequest = null;
	
		for(int i = 0; i < deactivatedPacks.length; i++) 
		{
			smRequest = new SMRequest(requestType);
			smRequest.setProvisioningRequests(deactivatedPacks[i]);
			futureList.add(tpe.submit(smRequest));
		}
		
		if (RBTDaemonManager.isFcapsEnabled)
			SMDaemonPerformanceMonitor.startTpsSampling("PackDeactivationTps", deactivatedPacks.length);
	}

	
}
