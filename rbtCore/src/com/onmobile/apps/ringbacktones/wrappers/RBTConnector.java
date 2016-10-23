package com.onmobile.apps.ringbacktones.wrappers;

import org.apache.log4j.Logger;

public class RBTConnector
{
	private  MemCacheWrapper memCache=null;
	private  SubscriberRbtClientWrapper subscriberRbtclient=null;
	private  RbtGenericCacheWrapper rbtGenericCache = null;
	private  RBTHibernateDBImplementationWrapper rbtHiberDBImpl=null; 
	private  SRBTDaoWrapper srbtDaoWrapper = null; 
//	private static AbstractLuceneIndexer luceneIndexer = null;
	
	
	static Logger logger = Logger.getLogger(RBTConnector.class);
	

	//use this if lucene indexer is needed
	private static void init(){
//		try{
//			luceneIndexer = LuceneIndexerFactory.getInstance();
//		}catch(Exception e) {
//			logger.error("Exception in Initialising LuceneIndexer",e);
//			e.printStackTrace();
//		}
//		
	}
	private RBTConnector()
	{
	
	}
	
	public static  RBTConnector  getInstance()
	{
		RBTConnector connector = new RBTConnector();
//		if(luceneIndexer==null){
//			init();
//		}
		try{
			connector.memCache = MemCacheWrapper.getInstance();
		}catch(Exception e){
			logger.error("Exception in initialising RBTCacheManager",e);
		}
		
		try{
			connector.rbtHiberDBImpl = RBTHibernateDBImplementationWrapper.getInstance();
		}catch(Exception e){
			logger.error("Exception in initialising RBTHibernateDBImplementationWrapper",e);
		}
//		try{
//			connector.srbtDaoWrapper = SRBTDaoWrapper.getInstance();
//		}catch(Exception e){
//			logger.error("Exception in initialising SRBTDaoWrapper",e);
//		}
		
		try{
			connector.rbtGenericCache = RbtGenericCacheWrapper.getInstance();
		}catch(Exception e){
		   logger.error("Exception in initialising rbtParametersCache",e);
		}

		try {
			connector.subscriberRbtclient = SubscriberRbtClientWrapper.getInstance();
			
		} catch (Exception e) {
			logger.error("Exception in RBTClient",e);
		}
		return connector;
	}
	public  MemCacheWrapper getMemCache() {
		return memCache;
	}
	public RbtGenericCacheWrapper getRbtGenericCache() {
		return rbtGenericCache;
	}
	public SubscriberRbtClientWrapper getSubscriberRbtclient() {
		return subscriberRbtclient;
	}
	public RBTHibernateDBImplementationWrapper getRbtHiberDBImpl() {
		return rbtHiberDBImpl;
	}
	public SRBTDaoWrapper getSrbtDaoWrapper() {
		return srbtDaoWrapper;
	}
}
