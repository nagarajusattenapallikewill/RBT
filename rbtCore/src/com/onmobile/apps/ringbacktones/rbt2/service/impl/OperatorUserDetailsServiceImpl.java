package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.OperatorUserDetailsImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;

public class OperatorUserDetailsServiceImpl implements IUserDetailsService {

	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	private RBTDBManager rbtdbManager = RBTDBManager.getInstance();
	private static final String USER_DETAILS_PREFIX = "operator_user_details_";
	private MemCachedClient mc = null;
	Logger logger = Logger.getLogger(OperatorUserDetailsServiceImpl.class);

	@Override
	public Object getUserDetails(String msisdn) throws UserException {
		OperatorUserDetails operatorUserDetails = null;
		boolean isD2CDeployed = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DTOC_DEPLOYED", "FALSE");
		try {
			Subscriber subscriber = rbtdbManager.getSubscriber(msisdn);
			int freeTrailCosId = RBTParametersUtils.getParamAsInt("COMMON","DTOC_FREE_TRIAL_COS_ID", 0);
			if (isD2CDeployed) {
				if(subscriber == null){
					return operatorUserDetails;
				}
				operatorUserDetails = com.onmobile.apps.ringbacktones.subscriptions.Utility.getOperatorUserDetails(msisdn, subscriber, freeTrailCosId, isD2CDeployed);
				return operatorUserDetails;

			} else {
				if (isCacheAlive()) {
					operatorUserDetails = (OperatorUserDetails) mc.get(USER_DETAILS_PREFIX + msisdn);
					if (operatorUserDetails == null) {
						operatorUserDetails = rbtdbManager.getUserDetails(msisdn);
						mc.set(USER_DETAILS_PREFIX + msisdn, operatorUserDetails);
					}
				} else {
					logger.info("memcache in not up");
					operatorUserDetails = rbtdbManager.getUserDetails(msisdn);
				}
				if(subscriber!=null && operatorUserDetails==null){
					if(!subscriber.subYes().equalsIgnoreCase(iRBTConstant.STATE_DEACTIVATED)){
						String oprUserType = com.onmobile.apps.ringbacktones.subscriptions.Utility.getOperatorUserType(subscriber.subscriptionClass());
						if(oprUserType!=null){
							if(OperatorUserTypes.PAID_APP_USER_LOW_BALANCE.getDefaultValue().equals(oprUserType)){
								if(subscriber.extraInfo()!=null){
									Map<String,String> extraInfoMap=DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
									
									String temp = (extraInfoMap == null? null : extraInfoMap.get(Constants.PAID_UNDER_LOWBAL));
									if(temp == null || !temp.equalsIgnoreCase("TRUE")) {
										oprUserType = OperatorUserTypes.TRADITIONAL.getDefaultValue();
									}

								}else{
									oprUserType=OperatorUserTypes.TRADITIONAL.getDefaultValue();
								}
							}
							operatorUserDetails = new OperatorUserDetailsImpl(msisdn, oprUserType,
									subscriber.subscriptionClass(), ServiceUtil.getOperatorName(subscriber),
									com.onmobile.apps.ringbacktones.subscriptions.Utility.getCirclerName(subscriber));
						}else{
								operatorUserDetails = com.onmobile.apps.ringbacktones.subscriptions.Utility.getOperatorUserDetails(msisdn, subscriber, freeTrailCosId, isD2CDeployed);
						}
					}else{
						operatorUserDetails = new OperatorUserDetailsImpl(msisdn, OperatorUserTypes.NEW_USER.getDefaultValue(),
								subscriber.subscriptionClass(), ServiceUtil.getOperatorName(subscriber),
								com.onmobile.apps.ringbacktones.subscriptions.Utility.getCirclerName(subscriber));
					}
				}
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
		return operatorUserDetails;

	}

	@Override
	public Object putUserDetails(String msisdn, String serviceKey, String status, String operatorName, String circleID) throws UserException {
		OperatorUserDetails operatorUserDetails = null;
		try {
			if (circleID == null || serviceKey == null || status == null || operatorName == null) {
				logger.error("All param are mandatory");
				return null;
			}
			if (isCacheAlive()) {
				operatorUserDetails = (OperatorUserDetails) rbtdbManager.createUserDetails(msisdn, serviceKey, status, operatorName, circleID);
				mc.set(USER_DETAILS_PREFIX + msisdn, operatorUserDetails);
			} else {
				logger.info("memcache in not up");
				operatorUserDetails = (OperatorUserDetails) rbtdbManager.createUserDetails(msisdn, serviceKey, status, operatorName, circleID);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
		return operatorUserDetails;

	}

	@Override
	public Object updateUserDetails(String msisdn, String serviceKey, String status, String operatorName, String circleID) throws UserException {
		OperatorUserDetails operatorUserDetails = null;
		try {
			if (circleID == null && serviceKey == null && status == null && operatorName == null) {
				logger.error("updating failed cause serviceKey and status both cannot be empty");
				return null;
			}
			if (isCacheAlive()) {
				operatorUserDetails = (OperatorUserDetails) rbtdbManager.updateUserDetails(msisdn, serviceKey, status, operatorName, circleID);
				mc.replace(USER_DETAILS_PREFIX + msisdn, operatorUserDetails);
			} else {
				logger.info("memcache in not up");
				operatorUserDetails = (OperatorUserDetails) rbtdbManager.updateUserDetails(msisdn, serviceKey, status, operatorName, circleID);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
		return operatorUserDetails;

	}

	@Override
	public Object removeUserDetails(String msisdn) throws UserException {
		String status = "failure";
		try {
			if (isCacheAlive()) {
				OperatorUserDetails operatorUserDetails = (OperatorUserDetails) mc.get(USER_DETAILS_PREFIX + msisdn);
				if (operatorUserDetails != null) {
					status = (String) rbtdbManager.removeUserDetails(msisdn);
					mc.delete(USER_DETAILS_PREFIX + msisdn);
				}
			} else {
				status = (String) rbtdbManager.removeUserDetails(msisdn);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
			return null;
		}
		return status;
	}

	@PostConstruct
	void initalizeMemCache() {
		logger.info("inside initalizeMemCache");
		rbtdbManager = RBTDBManager.getInstance();
		boolean isCacheAlive = MemcacheClientForCurrentPlayingSong.getInstance().isCacheAlive();
		if (isCacheAlive) {
			logger.info("building mem");
			mc = MemcacheClientForCurrentPlayingSong.getInstance().getMemcache();
			List<OperatorUserDetails> operatorUserDetails = null;
			try {
				boolean end = false;
				int limit = 0;
				while(!end){
				operatorUserDetails = rbtdbManager.getAllUserDetails(limit);
				limit++;
				if (operatorUserDetails == null || operatorUserDetails.isEmpty()) {
					end = true;
					break;
				}
				for (OperatorUserDetails userDetail : operatorUserDetails) {
					mc.set(USER_DETAILS_PREFIX + userDetail.subID(), userDetail);
				}
				}
			} catch (Exception e) {
				logger.info("memcache Exception" + e.getMessage());
			}
		} else {
			logger.info("memcache in not up");
		}

	}

	@PreDestroy
	void destroyMemCache() {

	}
	
	private boolean isCacheAlive(){
		try{
			MemcacheClientForCurrentPlayingSong.getInstance().checkCacheInitialized();
		return MemcacheClientForCurrentPlayingSong.getInstance().isCacheAlive();
		}catch(Exception e){
			return false;
		}
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
}
