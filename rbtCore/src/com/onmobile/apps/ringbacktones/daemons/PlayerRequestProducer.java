package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.helper.AbstractIntegrationHelper;
import com.onmobile.apps.ringbacktones.rbt2.helper.impl.GriffIntegrationHelperImpl;
import com.onmobile.apps.ringbacktones.rbt2.helper.impl.TPIntegrationHelperImpl;

public class PlayerRequestProducer extends Thread
{
	RequestType requestType;
	int threadCount;
	boolean iContinue = true;
	int pendingRequestCount = 0;
	long zeroPendingRequestLoopCount = 0;
	String circleId;
	static Logger logger = Logger.getLogger(PlayerRequestProducer.class);
	
	ThreadPoolExecutor tpe;
	ArrayList<Future<String>> futureList;
	
	public PlayerRequestProducer(RequestType requestType, int threadCount, String circleId)
	{
		this.requestType = requestType;
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
		ThreadFactory threadFactory = new RequestThreadFactory(requestType);
		tpe = new ThreadPoolExecutor(threadCount, threadCount, 5, TimeUnit.MINUTES, queue, threadFactory);
		this.circleId = circleId;
		setName(requestType+"-Producer"+"-"+circleId);
		logger.info("Created plyerRequestProducer for type="+requestType + ", circleId="+circleId);
	}
	
	@Override
	public void run()
	{
		logger.info("Started playerRequestProducer thread");
		while(iContinue)
		{
			getPendingRequests();
			if(pendingRequestCount == 0)
				sleepNow();
			else
				getResults();
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
		long sleepTime = (++zeroPendingRequestLoopCount > 6 ? 6 :  zeroPendingRequestLoopCount)*15 ;
		logger.info("No pending palyerRequests of type "+requestType + ", zeroTaskLoopCount=" + zeroPendingRequestLoopCount + ", sleepTime=" + sleepTime + " sec");
		try{Thread.sleep(sleepTime*1000);}catch (Exception e) {}
	}
	
	private void getPendingRequests()
	{
		logger.info("getting player requests of type "+ requestType);
		switch (this.requestType)
		{
			case PLAYER_BASE_ACT:
				getBaseActRequests();
				break;
			case PLAYER_BASE_DCT:
				getBaseDeactRequests();
				break;
			case PLAYER_BASE_SUS:
				getBaseSusRequests();
				break;
			case PLAYER_SEL_ACT:
				getSelActRequests();
				break;
			case PLAYER_SEL_DCT:
				getSelDeactRequests();
				break;
			case PLAYER_CHANGE_MSISDN:
				getChangeMsisdnRequests();
				break;
			default:
				break;
		}
		logger.info("done getting sm requests of type "+ requestType+ ", pending requests found "+ pendingRequestCount);
	}

	private void getBaseActRequests()
	{
		// RBT-16004 Added for checking rbt 2
		List<Subscriber> baseActList = RBTPlayerUpdateDaemon.dbManager.getSubsToUpdatePlayer(RBTPlayerUpdateDaemon.getParamAsInt("FETCH_SIZE", 5000), circleId , isRBT2());
		pendingRequestCount = baseActList == null ? 0 : baseActList.size();
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		PlayerRequest playerRequest = null;
		for(int i = 0; i < baseActList.size(); i++) 
		{
			playerRequest = new PlayerRequest(requestType);
			playerRequest.setSubscriber(baseActList.get(i));
			futureList.add(tpe.submit(playerRequest));
		}
	}
	
	private void getBaseSusRequests()
	{
		// RBT-16004 Added for checking rbt 2
		List<Subscriber> baseDeactList = RBTPlayerUpdateDaemon.dbManager.smGetSubscriberToDeactivateInPlayer(RBTPlayerUpdateDaemon.getParamAsInt("FETCH_SIZE", 5000), true, circleId , isRBT2());
		pendingRequestCount = baseDeactList == null ? 0 : baseDeactList.size();
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		PlayerRequest playerRequest = null;
		for(int i = 0; i < baseDeactList.size(); i++) 
		{
			playerRequest = new PlayerRequest(requestType);
			playerRequest.setSubscriber(baseDeactList.get(i));
			futureList.add(tpe.submit(playerRequest));
		}
	}
	
	private void getBaseDeactRequests()
	{
		// RBT-16004 Added for checking rbt 2
		List<Subscriber> baseDeactList = RBTPlayerUpdateDaemon.dbManager.smGetSubscriberToDeactivateInPlayer(RBTPlayerUpdateDaemon.getParamAsInt("FETCH_SIZE", 5000), false, circleId , isRBT2());
		pendingRequestCount = baseDeactList == null ? 0 : baseDeactList.size();
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		PlayerRequest playerRequest = null;
		for(int i = 0; i < baseDeactList.size(); i++) 
		{
			playerRequest = new PlayerRequest(requestType);
			playerRequest.setSubscriber(baseDeactList.get(i));
			futureList.add(tpe.submit(playerRequest));
		}
	}

	private void getSelActRequests()
	{
		// RBT-16004 Added for checking rbt 2
		ArrayList<String> selActList = RBTPlayerUpdateDaemon.dbManager.getSelectionsToAddToPlayer(RBTPlayerUpdateDaemon.getParamAsInt("FETCH_SIZE", 5000), circleId , isRBT2());
		pendingRequestCount = selActList == null ? 0 : selActList.size();
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		PlayerRequest playerRequest = null;
		for(int i = 0; i < selActList.size(); i++) 
		{
			playerRequest = new PlayerRequest(requestType);
			playerRequest.setSubscriberId(selActList.get(i));
			futureList.add(tpe.submit(playerRequest));
		}
	}
	
	private void getSelDeactRequests()
	{
		// RBT-16004 Added for checking rbt 2
		ArrayList<String> selDeactList = RBTPlayerUpdateDaemon.dbManager.getSelectionsToRemoveFromPlayer(RBTPlayerUpdateDaemon.getParamAsInt("FETCH_SIZE", 5000), circleId, isRBT2());
		pendingRequestCount = selDeactList == null ? 0 : selDeactList.size();
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		PlayerRequest playerRequest = null;
		for(int i = 0; i < selDeactList.size(); i++) 
		{
			playerRequest = new PlayerRequest(requestType);
			playerRequest.setSubscriberId(selDeactList.get(i));
			futureList.add(tpe.submit(playerRequest));
		}
	}
	
	private void getChangeMsisdnRequests()
	{
		ViralSMSTable[] viralRecord = RBTPlayerUpdateDaemon.dbManager.getViralSMSByType("CHANGEMSISDN");
		pendingRequestCount = viralRecord == null ? 0 : viralRecord.length;
		if(pendingRequestCount == 0)
			return;
		
		futureList = new ArrayList<Future<String>>();
		PlayerRequest playerRequest = null;
		for(int i = 0; i < viralRecord.length; i++) 
		{
			playerRequest = new PlayerRequest(requestType);
			playerRequest.setViralRecord(viralRecord[i]);
			futureList.add(tpe.submit(playerRequest));
		}
	}
	
	// RBT-16004 PlayerDaemon is not picking the subscriber entry with circle_id=operatorname_circlename for tone player update
	private boolean isRBT2()
	{
		boolean isRBT2 = false;
		AbstractIntegrationHelper integrationHelper = null;
		try{
			 integrationHelper = (AbstractIntegrationHelper) ConfigUtil.getBean(BeanConstant.INTEGRATION_HELPER_BEAN);
		}catch(NoSuchBeanDefinitionException e){
			logger.info("Exception occured while initializing bean.");
			if(integrationHelper == null){
				integrationHelper = new TPIntegrationHelperImpl();
			}
		}
		
		 if(integrationHelper.getClass() == GriffIntegrationHelperImpl.class){
			 isRBT2 = true;
		 }
		logger.info(" isRBT2 returning :"+ isRBT2);
		return isRBT2;
	}
}
