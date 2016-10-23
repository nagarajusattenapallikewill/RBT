package com.onmobile.apps.ringbacktones.wrappers;

import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.RBTSocialUpdateCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.RBTSocialUserCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUser;

public class RBTHibernateDBImplementationWrapper {
	static final org.apache.log4j.Logger c_logger = org.apache.log4j.Logger.getLogger(RBTHibernateDBImplementationWrapper.class);
	public  RBTSocialUserCacheManager rbtSocialUserCache=null;
	public  RBTSocialUpdateCacheManager rbtSocialUpdateCache=null;
	
	public static RBTHibernateDBImplementationWrapper getInstance(){
		RBTHibernateDBImplementationWrapper rbtHiberDBImpl=new RBTHibernateDBImplementationWrapper();
		rbtHiberDBImpl.init();
		return rbtHiberDBImpl;
	}
	
	private void init(){
		String method="RBTHibernateDBImplementationWrapper init()";
		try
		{
			rbtSocialUserCache = RBTSocialUserCacheManager.getInstance();
			rbtSocialUpdateCache=RBTSocialUpdateCacheManager.getInstance();
		}
		catch (Exception e)
		{
			c_logger.warn(method+"->"+ "Got exception while initializing subRbtClinet", e);
		}
	}
	public List<RBTSocialUser> getSNGUser(String subId){
		return rbtSocialUserCache.getActiveSNGUser(subId);
	}
	public boolean deactivateSNGUser(String userID,int mode){
		return rbtSocialUserCache.deactivateSNGUser(userID, mode);
	}
	public boolean updateSNGUser(String userID,String subscriberID,String rbtType,int mode){
		RBTSocialUser rbtSocialUser=new RBTSocialUser();
		rbtSocialUser.setMsisdn(subscriberID);
		rbtSocialUser.setRBTType(rbtType);
		rbtSocialUser.setSocialType(mode);
		rbtSocialUser.setUserId(userID);
		return rbtSocialUserCache.updateSNGUser(rbtSocialUser);
	}
	public boolean activateSNGUser(String userID,String subscriberID,String rbtType ,int mode){
		RBTSocialUser rbtSocialUser=new RBTSocialUser();
		rbtSocialUser.setMsisdn(subscriberID);
		rbtSocialUser.setRBTType(rbtType);
		rbtSocialUser.setSocialType(mode);
		rbtSocialUser.setUserId(userID);
		return rbtSocialUserCache.activateSNGUser(rbtSocialUser);
	}
	
	public boolean updateForSubscriptionActivationSuccess(String subId,long eventType,String rbtType){
		RBTSocialUpdate rbtSocialUpdate=new RBTSocialUpdate();
		rbtSocialUpdate.setMsisdn(subId);
		rbtSocialUpdate.setEventType(eventType);
		rbtSocialUpdate.setRBTType(rbtType);
		rbtSocialUpdate.setCatId(-1);
		rbtSocialUpdate.setClipId(-1);
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	
	public boolean updateForSubscriptionDeactivationSuccess(String subId,long eventType,String rbtType){
		RBTSocialUpdate rbtSocialUpdate=new RBTSocialUpdate();
		rbtSocialUpdate.setMsisdn(subId);
		rbtSocialUpdate.setEventType(eventType);
		rbtSocialUpdate.setRBTType(rbtType);
		rbtSocialUpdate.setCatId(-1);
		rbtSocialUpdate.setClipId(-1);
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	
	public boolean updateForSelectionActivationSuccess(String subId,String callerId,long eventType,int clipId,int catId,String rbtType){
		RBTSocialUpdate rbtSocialUpdate=new RBTSocialUpdate();
		rbtSocialUpdate.setMsisdn(subId);
		rbtSocialUpdate.setEventType(eventType);
		rbtSocialUpdate.setRBTType(rbtType);
		rbtSocialUpdate.setCatId(catId);
		rbtSocialUpdate.setClipId(clipId);
		rbtSocialUpdate.setCallerId(callerId);
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	
	public boolean updateForSelectionDeactivationSuccess(String subId,String callerId,long eventType,int clipId,int catId,String rbtType){
		RBTSocialUpdate rbtSocialUpdate=new RBTSocialUpdate();
		rbtSocialUpdate.setMsisdn(subId);
		rbtSocialUpdate.setEventType(eventType);
		rbtSocialUpdate.setRBTType(rbtType);
		rbtSocialUpdate.setCatId(catId);
		rbtSocialUpdate.setClipId(clipId);
		rbtSocialUpdate.setCallerId(callerId);
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	
	public boolean updateForDownloadActivationSuccess(String subId,long eventType,int clipId,int catId,String rbtType){
		RBTSocialUpdate rbtSocialUpdate=new RBTSocialUpdate();
		rbtSocialUpdate.setMsisdn(subId);
		rbtSocialUpdate.setEventType(eventType);
		rbtSocialUpdate.setRBTType(rbtType);
		rbtSocialUpdate.setCatId(catId);
		rbtSocialUpdate.setClipId(clipId);
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	
	public boolean updateForDownloadDeactivationSuccess(String subId,long eventType,int clipId,int catId,String rbtType){
		RBTSocialUpdate rbtSocialUpdate=new RBTSocialUpdate();
		rbtSocialUpdate.setMsisdn(subId);
		rbtSocialUpdate.setEventType(eventType);
		rbtSocialUpdate.setRBTType(rbtType);
		rbtSocialUpdate.setCatId(catId);
		rbtSocialUpdate.setClipId(clipId);
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	public boolean updateRBTSocialUpdateStatusCache(long sequenceId,String msisdn,int status){
		return rbtSocialUpdateCache.changeUpdatestatus(sequenceId, msisdn, status);
	}
	public boolean updateRBTSocailUpdateCache(RBTSocialUpdate rbtSocialUpdate){
		return rbtSocialUpdateCache.publishUpdates(rbtSocialUpdate);
	}
	public String getUpdateQueryString(int status,long eventtype,int fetchSize, long presentSequenceId){
		
		return rbtSocialUpdateCache.getUpdateQueryString( status, eventtype, fetchSize, presentSequenceId);
	}
}
