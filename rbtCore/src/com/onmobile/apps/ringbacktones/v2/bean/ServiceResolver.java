package com.onmobile.apps.ringbacktones.v2.bean;

import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.rbt2.service.IGroupMemberService;
import com.onmobile.apps.ringbacktones.rbt2.service.INextChargeClassService;
import com.onmobile.apps.ringbacktones.rbt2.service.ISubscriptionService;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.service.IFileDownloadService;
import com.onmobile.apps.ringbacktones.v2.service.IUDPService;

public class ServiceResolver{
	
	private Map<String, Object> serviceImpl;
	Logger logger = Logger.getLogger(ServiceResolver.class);
	
	public void setServiceImpl(Map<String, Object> serviceImpl) {
		this.serviceImpl = serviceImpl;
	}

	private Object getServiceImpl(String key) throws Exception{
		Object classObj = serviceImpl.get(key);
		  if(classObj == null){
			  throw new Exception();
		  }
		return classObj;
	}

	
	public IUDPService getUDPServiceImpl(String implPath) {
		IUDPService classObj = null;
		try{
		    classObj = (IUDPService) getServiceImpl("udp_"+implPath);
		} catch (Exception e) {
			logger.info("Exception occured while initializing UDP implementation class..." +e,e);
		}
		return classObj;
	}
	

	public IGroupMemberService getGroupMemberServiceImpl(String implPath) {
		IGroupMemberService classObj = null;
		try{
		    classObj = (IGroupMemberService)getServiceImpl("group_member_"+implPath);
		} catch (Exception e) {
			logger.info("Exception occured while initializing group member implementation class..." +e,e);
		}
		return classObj;
	}
	
	public ISubscriptionService getSubscriptionServiceImpl(String implPath) {
		ISubscriptionService subscriptionService = null;
		try {
			subscriptionService = (ISubscriptionService) getServiceImpl(Constants.SUBSCRIPTION_PREFIX+"_"+implPath);
		} catch (Exception e) {
			logger.info("Exception occured while initializing Subscription implementation class..." +e,e);
		}
		return subscriptionService;
	}
		
	
	public CallLogHistory getCallLogHistoryServiceImpl(String implPath){
		CallLogHistory callLogHistory = null;
		try{
			callLogHistory = (CallLogHistory)  getServiceImpl(Constants.CALL_LOG_HISTORY+"_"+implPath);
		}catch (Exception e) {
			logger.info("Exception occured while initializing CallLogHistory implementation class..." +e,e);
		}
		return callLogHistory;
	}
	
	public INextChargeClassService getChargeClassImpl() {
		INextChargeClassService getChargeClass = null;
		try {
			getChargeClass = (INextChargeClassService) getServiceImpl(Constants.CHARGE_CLASS);
		} catch (Exception e) {
			logger.info("Exception occured while initializing Subscription implementation class..." +e,e);
		}
		return getChargeClass;
	}
	
	
	public IFileDownloadService getFileDownloadServiceImpl(String type) {
		IFileDownloadService classObj = null;
		try{
		    classObj = (IFileDownloadService) getServiceImpl("file_download_service_"+type.toLowerCase());
		} catch (Exception e) {
			logger.info("Exception occured while initializing UDP implementation class..." +e,e);
		}
		return classObj;
	}
	
	public IUserDetailsService getUserDetailsServiceImpl(){
		IUserDetailsService classObj = null;
		try{
		    classObj = (IUserDetailsService) getServiceImpl(Constants.USER_DETAILS);
		} catch (Exception e) {
			logger.info("Exception occured while initializing UDP implementation class..." +e,e);
		}
		return classObj;
		
	}
}
