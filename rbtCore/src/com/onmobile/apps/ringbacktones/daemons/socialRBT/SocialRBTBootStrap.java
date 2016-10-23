package com.onmobile.apps.ringbacktones.daemons.socialRBT;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.RBTHunterConfigurator;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.copy.ProgressiveSocialRBTCopyUpdate;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.download.ProgressiveSocialRBTDownloadUpdate;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.gift.ProgressiveSocialRBTGiftUpdate;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.selection.ProgressiveSocialRBTSelectionUpdate;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.subscriber.ProgressiveSocialRBTSubscriberUpdate;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTCopyUpdatePublisher;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTDownloadUpdatePublisher;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTGiftUpdatePublisher;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTSelectionUpdatePublisher;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTSubscriberUpdatePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.Hunter;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.debugger.DebugDaemon;
import com.onmobile.apps.ringbacktones.srbt.db.BeanFactory;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

public class SocialRBTBootStrap implements iRBTConstant{
	private static final String classname= "SocialRBTBootStrap"; 
	private static final String HunterNameSocialRBT = "SocialRBT";
	private static final String HunterNameSocialRBTCopy = "SocialRBTCopy";
	private static final String HunterNameSocialRBTGift = "SocialRBTGift";
	private static final String HunterNameSocialRBTSubscriber = "SocialRBTSubscriber";
	private static final String HunterNameSocialRBTSelection = "SocialRBTSelection";
	private static final String HunterNameSocialRBTDownload = "SocialRBTDownload";
	private static final String HunterAllTypes = "AllTypes";
	private static HashMap<String, Object> implementationMap = null;
	private static Logger logger = Logger.getLogger(SocialRBTBootStrap.class);

	public static void main(String[] args) {
		startUp();
	}

	public static void startUp() 
	{
		logger.info("Staring up SocialRBTBootstrap");
		try
		{
			BeanFactory.getInstance();
		} 
		catch (ClassNotFoundException e1) 
		{
			logger.error("Failed to load srbt configuration. "
					+ "ClassNotFoundException: " + e1.getMessage(), e1);
		}
		SocialRBTBootStrap socialRBTBootStrap = new SocialRBTBootStrap();
		RbtGenericCacheWrapper genericCacheWrapper = RBTConnector.getInstance().getRbtGenericCache();
		populateImplementationMap(genericCacheWrapper);
		String paramValue = genericCacheWrapper.getParameter(SRBT, "ALL_IN_ONE_UPDATE_MODEL");
		if(paramValue!=null && paramValue.trim().equalsIgnoreCase("true")){
			logger.info("Since ALL_IN_ONE_UPDATE_MODEL is enabled, registering social RBT hunter");
			socialRBTBootStrap.registerSocialRBTHunter();
		}else{
			logger.info("Since ALL_IN_ONE_UPDATE_MODEL is disabled, registering all social RBT hunter");
			socialRBTBootStrap.registerSocialRBTCopyHunter();
			socialRBTBootStrap.registerSocialRBTGiftHunter();
			socialRBTBootStrap.registerSocialRBTSubscriberHunter();
			socialRBTBootStrap.registerSocialRBTSelectionHunter();
			socialRBTBootStrap.registerSocialRBTDownloadHunter();
		}
		try {
			DebugDaemon.startDebugDemon("SRBT","debugDeamon.port");
		} catch (IOException e) {
			Logger.getLogger(SocialRBTBootStrap.class).error("Failed to start Social RBT debug daemon server", e);
		}
		logger.info("Successfully started SocialRBTBootstrap");
	}

	public static HashMap<String, Object> getImplementationMap() {
		return implementationMap;
	}

	private static void populateImplementationMap(RbtGenericCacheWrapper genericCacheWrapper){
		String method = "populateImplementationMap";
		logger.info("Populating implementation map");
		if(implementationMap == null){
			implementationMap = new HashMap<String,Object>();
		}
		populateGiftPublisher(genericCacheWrapper);
		populateCopyPublisher(genericCacheWrapper);
		populateSubPublisher(genericCacheWrapper);
		populateSelPublisher(genericCacheWrapper);
		populateDownloadPublisher(genericCacheWrapper);
		logger.info("Successfully populated all publishers. ");
	}
	private static void populateGiftPublisher(RbtGenericCacheWrapper genericCacheWrapper){
		logger.info("Populating Gift Publisher");
		String method = "populateGiftPublisher";
		String paramValue = null;
		String giftUpdatePublisherClass = "com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations.BasicSRBTGiftUpdatePublisher";
		paramValue = genericCacheWrapper.getParameter(SRBT, "GIFT_PUBLISHER_CLASS");
		if (paramValue != null)
			giftUpdatePublisherClass = paramValue;
		if(implementationMap == null){
			implementationMap = new HashMap<String,Object>();
		}
		try
		{
			Class<?> srbtGiftPublisherClass = Class.forName(giftUpdatePublisherClass);
			SRBTGiftUpdatePublisher srbtGiftUpdatePublisher = (SRBTGiftUpdatePublisher) srbtGiftPublisherClass.newInstance();
			implementationMap.put("GIFT_UPDATE_PUBLISHER_CLASS", srbtGiftUpdatePublisher);
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(method + "-> ClassNotFoundException", e);
		}
		catch (InstantiationException e)
		{
			logger.fatal(method + "-> InstantiationException", e);
		}
		catch (IllegalAccessException e)
		{
			logger.fatal(method + "-> IllegalAccessException", e);
		}
		logger.info("Successfully populated Gift Publisher");
	}
	private static void populateSubPublisher(RbtGenericCacheWrapper genericCacheWrapper){
		logger.info("Populating Sub Publisher");
		String method = "populateSubPublisher";
		String paramValue = null;
		String subscriberUpdatePublisherClass = "com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations.BasicSRBTSubscriberUpdatePublisher";
		paramValue = genericCacheWrapper.getParameter(SRBT, "SUB_PUBLISHER_CLASS");
		if (paramValue != null)
			subscriberUpdatePublisherClass = paramValue;
		if(implementationMap == null){
			implementationMap = new HashMap<String,Object>();
		}
		try
		{	
			Class<?> srbtSubPublisherClass = Class.forName(subscriberUpdatePublisherClass);
			SRBTSubscriberUpdatePublisher srbtSubUpdatePublisher = (SRBTSubscriberUpdatePublisher) srbtSubPublisherClass.newInstance();
			implementationMap.put("SUB_UPDATE_PUBLISHER_CLASS", srbtSubUpdatePublisher);
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(method + "-> ClassNotFoundException", e);
		}
		catch (InstantiationException e)
		{
			logger.fatal(method + "-> InstantiationException", e);
		}
		catch (IllegalAccessException e)
		{
			logger.fatal(method + "-> IllegalAccessException", e);
		}
		logger.info("Successfully populated Sub Publisher");
	}
	private static void populateSelPublisher(RbtGenericCacheWrapper genericCacheWrapper){
		logger.info("Populating Sel Publisher");
		String method = "populateSelPublisher";
		String paramValue = null;
		String selectionUpdatePublisherClass = "com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations.BasicSRBTSelectionUpdatePublisher";
		paramValue = genericCacheWrapper.getParameter(SRBT, "SEL_PUBLISHER_CLASS");
		if (paramValue != null)
			selectionUpdatePublisherClass = paramValue;
		if(implementationMap == null){
			implementationMap = new HashMap<String,Object>();
		}
		try
		{
			Class<?> srbtSelPublisherClass = Class.forName(selectionUpdatePublisherClass);
			SRBTSelectionUpdatePublisher srbtSelUpdatePublisher = (SRBTSelectionUpdatePublisher) srbtSelPublisherClass.newInstance();
			implementationMap.put("SEL_UPDATE_PUBLISHER_CLASS", srbtSelUpdatePublisher);
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(method + "-> ClassNotFoundException", e);
		}
		catch (InstantiationException e)
		{
			logger.fatal(method + "-> InstantiationException", e);
		}
		catch (IllegalAccessException e)
		{
			logger.fatal(method + "-> IllegalAccessException", e);
		}
		logger.info("Successfully populated Sel Publisher");
	}
	private static void populateDownloadPublisher(RbtGenericCacheWrapper genericCacheWrapper){
		logger.info("Populating Download Publisher");
		String method = "populateDownloadPublisher";
		String paramValue = null;
		String downloadUpdatePublisherClass = "com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations.BasicSRBTDownloadUpdatePublisher";
		paramValue = genericCacheWrapper.getParameter(SRBT, "DOWNLOAD_PUBLISHER_CLASS");
		if (paramValue != null)
			downloadUpdatePublisherClass = paramValue;
		if(implementationMap == null){
			implementationMap = new HashMap<String,Object>();
		}
		try
		{
			Class<?> srbtDownloadPublisherClass = Class.forName(downloadUpdatePublisherClass);
			SRBTDownloadUpdatePublisher srbtDownloadUpdatePublisher = (SRBTDownloadUpdatePublisher) srbtDownloadPublisherClass.newInstance();
			implementationMap.put("DOWNLOAD_UPDATE_PUBLISHER_CLASS", srbtDownloadUpdatePublisher);
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(method + "-> ClassNotFoundException", e);
		}
		catch (InstantiationException e)
		{
			logger.fatal(method + "-> InstantiationException", e);
		}
		catch (IllegalAccessException e)
		{
			logger.fatal(method + "-> IllegalAccessException", e);
		}
		logger.info("Successfully populated Download Publisher");
	}
	private static void populateCopyPublisher(RbtGenericCacheWrapper genericCacheWrapper){
		logger.info("Populating Copy Publisher");
		String method = "populateCopyPublisher";
		String paramValue = null;
		String copyUpdatePublisherClass = "com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations.BasicSRBTCopyUpdatePublisher";
		paramValue = genericCacheWrapper.getParameter(SRBT, "COPY_PUBLISHER_CLASS");
		if (paramValue != null)
			copyUpdatePublisherClass = paramValue;
		if(implementationMap == null){
			implementationMap = new HashMap<String,Object>();
		}
		try
		{
			Class<?> srbtCopyPublisherClass = Class.forName(copyUpdatePublisherClass);
			SRBTCopyUpdatePublisher srbtCopyUpdatePublisher = (SRBTCopyUpdatePublisher) srbtCopyPublisherClass.newInstance();
			implementationMap.put("COPY_UPDATE_PUBLISHER_CLASS", srbtCopyUpdatePublisher);
		}
		catch (ClassNotFoundException e)
		{
			logger.fatal(method + "-> ClassNotFoundException", e);
		}
		catch (InstantiationException e)
		{
			logger.fatal(method + "-> InstantiationException", e);
		}
		catch (IllegalAccessException e)
		{
			logger.fatal(method + "-> IllegalAccessException", e);
		}
		logger.info("Successfully populated Copy Publisher");
	}
	
	private void registerSocialRBTHunter() {
		logger.info("Registering Social RBT Publisher");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameSocialRBT);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ProgressiveSocialRBTUpdatePublisher updatePublisher = new ProgressiveSocialRBTUpdatePublisher(-1, 1);
		QueueContainer queueContainer = new QueueContainer(updatePublisher);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Registered social RBT hunter.");
	}
	private void registerSocialRBTCopyHunter() {
		logger.info("Registering social RBT Copy hunter");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameSocialRBTCopy);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ProgressiveSocialRBTCopyUpdate copyUpdate = new ProgressiveSocialRBTCopyUpdate(1);
		QueueContainer queueContainer = new QueueContainer(copyUpdate);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Registered social RBT Copy hunter");
	}
	private void registerSocialRBTGiftHunter() {
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameSocialRBTGift);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		// 1 for gifting successful, 2 for gift acceptance
		ProgressiveSocialRBTGiftUpdate giftUpdate = new ProgressiveSocialRBTGiftUpdate("1,2");
		QueueContainer queueContainer = new QueueContainer(giftUpdate);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Registered social RBT Gift hunter.");
	}
	private void registerSocialRBTSubscriberHunter() {
		logger.info("Registering social RBT Subscriber hunter.");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameSocialRBTSubscriber);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ProgressiveSocialRBTSubscriberUpdate subscriberUpdate = new ProgressiveSocialRBTSubscriberUpdate();
		QueueContainer queueContainer = new QueueContainer(subscriberUpdate);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Registered social RBT Subscriber hunter.");
	}
	private void registerSocialRBTSelectionHunter() {
		logger.info("Registering social RBT Selection hunter.");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameSocialRBTSelection);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ProgressiveSocialRBTSelectionUpdate selectionUpdate = new ProgressiveSocialRBTSelectionUpdate();
		QueueContainer queueContainer = new QueueContainer(selectionUpdate);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Registered social RBT Selection hunter.");
	}
	private void registerSocialRBTDownloadHunter() {
		logger.info("Registering social RBT Download hunter.");
		Hunter hunter = new Hunter();
		hunter.setConfigurator(new RBTHunterConfigurator());
		hunter.setHunterName(HunterNameSocialRBTDownload);
		HashMap<String, QueueContainer> nameQueueContainerMap = new HashMap<String, QueueContainer>();
		ProgressiveSocialRBTDownloadUpdate downloadUpdate = new ProgressiveSocialRBTDownloadUpdate();
		QueueContainer queueContainer = new QueueContainer(downloadUpdate);
		queueContainer.setQueueContainerName(HunterAllTypes);
		nameQueueContainerMap.put(HunterAllTypes, queueContainer);
		hunter.setSiteQueContainer(nameQueueContainerMap);
		hunter.register();
		logger.info("Registered social RBT Download hunter.");
	}
}
