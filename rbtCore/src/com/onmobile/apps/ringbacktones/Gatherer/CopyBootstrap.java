package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RBTHunterConfigurator;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.cid.ProgressiveCIDPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copy.ProgressiveCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.commongateway.CommonGatewayCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.interoperator.InterOperatorCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.intraoperator.IntraOperatorCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.RRBTCopy.RRBTCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.confirmedCopy.ConfirmedCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.directCopy.DirectCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.failedCopy.FailedCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.optinCopy.OptinCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.optoutConfirmCopy.OptoutConfirmCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.optoutCopy.OptoutCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.starCopy.StarCopyPublisher;
import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.unknownType.UnknownTypePublisher;
import com.onmobile.apps.ringbacktones.Gatherer.management.PerformanceMonitorDaemon;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.DebugDaemon;

public class CopyBootstrap extends Thread
{
	private static Logger logger = Logger.getLogger(CopyBootstrap.class);
	
	private static CopyBootstrap copyBootstrap = new CopyBootstrap();
	
	@Override
	public void run()
	{
		try
		{
			Tools.init("CopyBootstrap", true);
			logger.info("Starting Debug Daemon");
			DebugDaemon.startDebugDemon();
			logger.info("Setting up CopyBootstrap");
			copyBootstrap.setUpCopyDaemon();
			logger.info("Starting PerformanceMonitorDaemon");
			PerformanceMonitorDaemon.startPerformanceMonitorDaemon();
			logger.info("Exiting run method");
		}
		catch(IOException e)
		{
			logger.error("", e);
		}
	}
	
	public static void main(String[] args)
    {
        copyBootstrap.start();
    }
	
	public void setUpCopyDaemon()
	{
		initTypeCopy();
		initCopyStar();
		initCopyFailed();
		if(Utility.getParamAsBoolean(Utility.GATHERER, "IS_RRBT_COPY_ON", "FALSE"))
		{
			initRRBTCopy();
		}
		/**
		 *  types : COPY direct, COPY optout, COPYSTAR optin, COPYCONFIRMED, COPYCONFIRM, failed copy from ftp, expired copy COPYCONFPENDING
			circles : local , sites or central, nononmobile or rdc, 
				COPY direct : ALL
				COPY optout : ALL
				COPY optin : ALL
				COPYSTAR : ALL
				COPYCONFIRMED : LOCAL
				COPYCONFPENDING : LOCAL
				FAILED : LOCAL
				EXPIRED : LOCAL
			
			if
				(PROCESS_COPY) COPY :                  if(IS_STAR_OPT_IN_ALLOWED) COPYSTAR :        COPYCONFIRMED          COPYCONFPENDING  
			else
				if(PRESS_STAR_DOUBLE_CONFIRMATION) COPY optout :     COPYCONFIRM      if(IS_STAR_OPT_IN_ALLOWED) COPYSTAR :        COPYCONFIRMED          COPYCONFPENDING  
			else
				if(IS_OPT_IN) COPY optin :   COPYCONFIRMED          COPYCONFPENDING                
					
			if(copyFailed)
				FAILED : LOCAL
			
			param : sites to process
			
			local
			operator_prefix : or sites :
			non-onmobile
			cross-operator
			
				
		
		*/
		
	}

	private void initCopyConfirm()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameConfirmOptoutCopy);
		Utility.updateCircleIdForType("COPYCONFIRM");
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		OptoutConfirmCopyPublisher progressiveCopyPublisher = new OptoutConfirmCopyPublisher("LOCAL", "COPYCONFIRM");
		QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
		queueContainer.setQueueContainerName("LOCAL");
		nameQueueContainerMap.put("LOCAL", queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Exiting");
	}

	private void initCopyConfirmed()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameConfirmedCopy);
		Utility.updateCircleIdForType("COPYCONFIRMED");
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ConfirmedCopyPublisher progressiveCopyPublisher = new ConfirmedCopyPublisher("LOCAL", "COPYCONFIRMED");
		QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
		queueContainer.setQueueContainerName("LOCAL");
		nameQueueContainerMap.put("LOCAL", queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Exiting");
	}

	private void initCopyFailed()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameFailedCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		FailedCopyPublisher progressiveCopyPublisher = new FailedCopyPublisher();
		QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
		queueContainer.setQueueContainerName("LOCAL");
		nameQueueContainerMap.put("LOCAL", queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Exiting");
	}

	
	private void initCopyStar()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameStarCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		for(String sites : Utility.sitesList)
		{
			ProgressiveCopyPublisher progressiveCopyPublisher = null;
			if(sites.equals("LOCAL"))
				progressiveCopyPublisher = new StarCopyPublisher(sites,"COPYSTAR");
			else if (sites.equals("CROSS"))
				progressiveCopyPublisher = new InterOperatorCopyPublisher(sites,"COPYSTAR");
			else if (sites.equals("CGATE"))
				progressiveCopyPublisher = new CommonGatewayCopyPublisher(sites,"COPYSTAR");
			else if (sites.equals(Utility.JUNK))
				progressiveCopyPublisher = new UnknownTypePublisher(sites,"COPYSTAR");
			else 
				progressiveCopyPublisher = new IntraOperatorCopyPublisher(sites,"COPYSTAR");
			
			progressiveCopyPublisher.setPublisherActive(false);
			QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
			queueContainer.setQueueContainerName(sites);
			nameQueueContainerMap.put(sites, queueContainer);
		}
		ProgressiveCIDPublisher cidPublisher = new ProgressiveCIDPublisher("COPYSTAR", 1);
		QueueContainer cidQueueContainer = new QueueContainer(cidPublisher);
		cidQueueContainer.setQueueContainerName(Utility.Cid);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.setCidQueue(cidQueueContainer);
		hunter.register();
		
		initCopyConfirmed();
		logger.info("Exiting");
	}

	private void initTypeCopy()
	{
		logger.info("Entering");
		Utility.updateCircleIdForType("COPY");
		if(Utility.getParamAsBoolean(Utility.GATHERER, Utility.IS_OPT_IN, "FALSE"))
			initOptinCopy();
		else if(Utility.getParamAsBoolean(Utility.GATHERER, Utility.PRESS_STAR_DOUBLE_CONFIRMATION, "FALSE") || 
				Utility.getParamAsBoolean(Utility.GATHERER, "PRESS_STAR_DOUBLE_CONFIRMATION_INACTIVE_USER", "FALSE"))
			initOptoutCopy();
		else
			initDirectCopy();
		logger.info("Exiting");
	}

	private void initDirectCopy()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameDirectCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		for(String sites : Utility.sitesList)
		{
			ProgressiveCopyPublisher progressiveCopyPublisher = null;
			if(sites.equals("LOCAL"))
				progressiveCopyPublisher = new DirectCopyPublisher(sites,"COPY");
			else if (sites.equals("CROSS"))
				progressiveCopyPublisher = new InterOperatorCopyPublisher(sites,"COPY");
			else if (sites.equals("CGATE"))
				progressiveCopyPublisher = new CommonGatewayCopyPublisher(sites,"COPY");
			else if (sites.equals(Utility.JUNK))
				progressiveCopyPublisher = new UnknownTypePublisher(sites,"COPY");
			else 
				progressiveCopyPublisher = new IntraOperatorCopyPublisher(sites,"COPY");
			
			progressiveCopyPublisher.setPublisherActive(false);
			QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
			queueContainer.setQueueContainerName(sites);
			nameQueueContainerMap.put(sites, queueContainer);
		}
		ProgressiveCIDPublisher cidPublisher = new ProgressiveCIDPublisher("COPY", 1);
		QueueContainer cidQueueContainer = new QueueContainer(cidPublisher);
		cidQueueContainer.setQueueContainerName(Utility.Cid);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.setCidQueue(cidQueueContainer);
		hunter.register();
		logger.info("Exiting");
	}

	private void initOptoutCopy()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameOptoutCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		for(String sites : Utility.sitesList)
		{
			ProgressiveCopyPublisher progressiveCopyPublisher = null;
			if(sites.equals("LOCAL"))
				progressiveCopyPublisher = new OptoutCopyPublisher(sites,"COPY");
			else if (sites.equals("CROSS"))
				progressiveCopyPublisher = new InterOperatorCopyPublisher(sites,"COPY");
			else if (sites.equals("CGATE"))
				progressiveCopyPublisher = new CommonGatewayCopyPublisher(sites,"COPY");
			else if (sites.equals(Utility.JUNK))
				progressiveCopyPublisher = new UnknownTypePublisher(sites,"COPY");
			else 
				progressiveCopyPublisher = new IntraOperatorCopyPublisher(sites,"COPY");
			
			progressiveCopyPublisher.setPublisherActive(false);
			QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
			queueContainer.setQueueContainerName(sites);
			nameQueueContainerMap.put(sites, queueContainer);
		}
		ProgressiveCIDPublisher cidPublisher = new ProgressiveCIDPublisher("COPY", 1);
		QueueContainer cidQueueContainer = new QueueContainer(cidPublisher);
		cidQueueContainer.setQueueContainerName(Utility.Cid);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.setCidQueue(cidQueueContainer);
		hunter.register();
	
		initCopyConfirm();
		logger.info("Exiting");
	}

	private void initOptinCopy()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameOptinCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		for(String sites : Utility.sitesList)
		{
			ProgressiveCopyPublisher progressiveCopyPublisher = null;
			if(sites.equals("LOCAL"))
				progressiveCopyPublisher = new OptinCopyPublisher(sites,"COPY");
			else if (sites.equals("CROSS"))
				progressiveCopyPublisher = new InterOperatorCopyPublisher(sites,"COPY");
			else if (sites.equals("CGATE"))
				progressiveCopyPublisher = new CommonGatewayCopyPublisher(sites,"COPY");
			else if (sites.equals(Utility.JUNK))
				progressiveCopyPublisher = new UnknownTypePublisher(sites,"COPY");
			else 
				progressiveCopyPublisher = new IntraOperatorCopyPublisher(sites,"COPY");
			
			progressiveCopyPublisher.setPublisherActive(false);
			QueueContainer queueContainer = new QueueContainer(progressiveCopyPublisher);
			queueContainer.setQueueContainerName(sites);
			nameQueueContainerMap.put(sites, queueContainer);
		}
		ProgressiveCIDPublisher cidPublisher = new ProgressiveCIDPublisher("COPY", 1);
		QueueContainer cidQueueContainer = new QueueContainer(cidPublisher);
		cidQueueContainer.setQueueContainerName(Utility.Cid);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.setCidQueue(cidQueueContainer);
		hunter.register();
		logger.info("Exiting");
	}
	
	private void initRRBTCopy()
	{
		logger.info("Entering");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(Utility.HunterNameRRBTCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		RRBTCopyPublisher progressiveSqlQueryPublisher = new RRBTCopyPublisher("RRBT_COPY");
		//progressiveCopyPublisher.setPublisherActive(true);
		QueueContainer queueContainer = new QueueContainer(progressiveSqlQueryPublisher);
		queueContainer.setQueueContainerName("RRBT");
		nameQueueContainerMap.put("RRBT", queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Exiting");
	
	}
	public void setupHunterParameters(Hunter hunter)
    {
        String hunterName = hunter.getHunterName();
        
    }
}
