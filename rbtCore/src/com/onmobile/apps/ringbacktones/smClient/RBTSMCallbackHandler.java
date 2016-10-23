package com.onmobile.apps.ringbacktones.smClient;

import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.subscriptions.RBTDaemonHelper;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CallbackRequest;
import com.onmobile.prism.client.core.SMComboRequest;
import com.onmobile.prism.client.core.SMException;
import com.onmobile.prism.client.core.SMGiftRequest;
import com.onmobile.prism.client.core.SMPrechargeRequest;
import com.onmobile.prism.client.core.SMRefundRequest;
import com.onmobile.prism.client.core.SMRequest;
import com.onmobile.prism.client.core.SMResponse;
import com.onmobile.prism.client.core.SMUpgradeRequest;
import com.onmobile.prism.client.handler.SMCallbackInterface;
import com.onmobile.prism.client.handler.SMCallbackStatus;
/**
 * 
 * Modified on July30 2010
 * OfferID will be returned by SM incase of UNLIMITED_DOWNLOADS pack upgradation request.
 * Passing OfferId as null in all smProcessSelection APIs. 
 *
 */
public class RBTSMCallbackHandler implements iRBTConstant, SMCallbackInterface
{
	private static Logger logger = Logger.getLogger(RBTSMCallbackHandler.class);
		
//	private static RBTSMCallbackHandler rbtSmCallbackHandler = null;
//	private static Object syncObject = new Object();
	private static RBTDaemonHelper m_daemonHelper = null;
	
    public boolean init()
    {
    	if (m_daemonHelper == null)
    	{
    		return false;
    	}
        return true;
    }
    
    public RBTSMCallbackHandler() throws Exception
    {
    	if(m_daemonHelper == null){
    		m_daemonHelper = RBTDaemonHelper.init();
    	}
    }

    private String getStatus(SMResponse paramSMResponse) {
    	String status = "SUCCESS";
    	if(paramSMResponse.isSuccess()){
    		if(paramSMResponse.getChargingType().equals("GRACE")){
    			status = "GRACE";
    		}
    	}
    	else{
    		status = "FAILURE";
    	}
    	return status;
    }
    
    private SMCallbackStatus getCallbackStatus(String retStatus){
    	boolean isSuccess = false;
    	if(retStatus != null && retStatus.startsWith("SUCCESS")){
    		isSuccess = true;
    	}
    	SMCallbackStatus callBackStatus = new SMCallbackStatus(isSuccess, retStatus);
    	return callBackStatus;
    }
    
    private String directActivationNDeActivation(String action, String classType, String subId, SMResponse paramSMResponse){
    	logger.info("Action = " + action + " classtype : " + classType + " subId : " + subId);
    	Hashtable<String, Object> userInfo = paramSMResponse.getUserInfo();
    	String retStatus = null;    	
		String smsText = "actsmstext_:";
		if(action.equals("DCT")){
			smsText = "dctsmstext_:";
		}
		if(userInfo != null && userInfo.contains("info")){    			
			//DirectActivation
			String info = userInfo.get("info").toString();
			if(info != null && info.toLowerCase().indexOf(smsText) != -1){
				logger.info("info : " + info);
    			HashMap<String, String> requestParams = new HashMap<String, String>();
    			requestParams.put("SUBSCRIBER_ID", subId);
				requestParams.put("info", info);
				requestParams.put("action", action);
				requestParams.put("MODE", paramSMResponse.getMode());
				requestParams.put("SUBSCRIPTION_CLASS", classType);
				requestParams.put("COSID", null);
				requestParams.put("api", "Sms");
				retStatus = AdminFacade.processDirectActivationRequest(requestParams);
				logger.info("status : " + retStatus);
			}
		}
		return retStatus;
    }
    
    private String getClassType(String srvKey){
    	String classType = null;
    	if(srvKey.startsWith("RBT_ACT_")){
    		//activating subscriber
    		classType = srvKey.substring(8);    		
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		//activating the selection
    		//srv key can be RBT_SEL_<charge class> or RBT_SEL_<charge class>_RBT_ACT 
    		if(srvKey.indexOf("_RBT_ACT") != -1)
    			classType = srvKey.substring(8, srvKey.indexOf("_RBT_ACT"));
    		else
    			classType = srvKey.substring(8);
    	}
    	else if(srvKey.startsWith("RBT_PACK_")){
    			classType = srvKey.substring(9);  
        	}
    	return classType;
    }
    
    /* (non-Javadoc)
     * @see com.onmobile.prism.client.handler.SMCallbackInterface#activateSubscription(com.onmobile.prism.client.core.SMRequest, com.onmobile.prism.client.core.SMResponse)
     */
    public SMCallbackStatus activateSubscription(SMRequest paramSMRequest, SMResponse paramSMResponse)
      throws SMException{
    	if(paramSMRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = getStatus(paramSMResponse);
    	String retStatus = null;
    	String srvKey = paramSMRequest.getSrvkey();
    	String classType = null;
    	String action = "ACT";
    	if(srvKey.startsWith("RBT_ACT_")){
    		//activating subscriber
    		classType = getClassType(srvKey);
    		retStatus = directActivationNDeActivation(action, classType, paramSMRequest.getMdn(), paramSMResponse);
    		if(retStatus == null){    		
    			retStatus = m_daemonHelper.subscription(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null, null);
    		}
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		//activating the selection
    		//srv key can be RBT_SEL_<charge class> or RBT_SEL_<charge class>_RBT_ACT 
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.smProcessSelection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), 
    				paramSMRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null);
    	}
    	else if(srvKey.startsWith("RBT_PACK_")){// RBT-14301: Uninor MNP changes.
    	retStatus = m_daemonHelper.packSelection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), null,null);
		
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public SMCallbackStatus deactivateSubscription(SMRequest paramSMRequest, SMResponse paramSMResponse)
      throws SMException{
    	if(paramSMRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = getStatus(paramSMResponse);
    	String retStatus = null;
    	String srvKey = paramSMRequest.getSrvkey();
    	String classType = null;
    	String action = "DCT";
    	if(paramSMResponse.getMode().equalsIgnoreCase("SYSTEM")){
    		action = "REN";
    		status = "FAILURE";
    	}
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);
    		retStatus = directActivationNDeActivation(action, classType, paramSMRequest.getMdn(), paramSMResponse);
    		if(retStatus == null){// RBT-14301: Uninor MNP changes.
    			retStatus = m_daemonHelper.subscription(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null,null);
    		}
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		//deactivating the selection
    		//srv key can be RBT_SEL_<charge class> or RBT_SEL_<charge class>_RBT_ACT 
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.smProcessSelection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), 
    				paramSMRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null);
    	}
    	else if(srvKey.startsWith("RBT_PACK_") ){// RBT-14301: Uninor MNP changes.
    		retStatus = m_daemonHelper.packSelection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), null,null);
    		
    		
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public  SMCallbackStatus comboSubscription(SMComboRequest paramSMComboRequest, SMResponse paramSMResponse)
      throws SMException{    	
//    	if(paramSMComboRequest == null || paramSMResponse == null){
//    		throw new SMException("Arguments are passing as null, Please check arguments");
//    	}
//    	String status = getStatus(paramSMResponse);
//    	String retStatus = null;
//    	String srvKey = paramSMComboRequest.getSrvkey();
//    	String subSrvKey = paramSMComboRequest.getLinkedSrvKey();
//    	String classType = null;
//    	String action = "ACT";
//    	if(subSrvKey.startsWith("RBT_ACT_")){
//    		classType = getClassType(subSrvKey);
//    		retStatus = m_daemonHelper.subscription(paramSMComboRequest.getMdn(), action, null, status, paramSMComboRequest.getReqRefId(), paramSMComboRequest.getType(), 
//    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
//    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus());
//    	}
//    	
//    	if(retStatus != null && retStatus.equals("SUCCESS") && (srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_"))){
//    		classType = getClassType(srvKey);
//    		retStatus = m_daemonHelper.smProcessSelection(paramSMComboRequest.getMdn(), action, null, status, paramSMComboRequest.getReqRefId(), 
//    				paramSMComboRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
//    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus());
//    	}
//    	logger.info("Ret Status : " + retStatus);
//    	return getCallbackStatus(retStatus);
    	
    	throw new SMException("RBT does not support this model currently");
    }

    public  SMCallbackStatus eventCharge(SMComboRequest paramSMComboRequest, SMResponse paramSMResponse)
      throws SMException{
    	//RBT does not support this model currently
    	throw new SMException("RBT does not support this model currently"); 
    }

    public  SMCallbackStatus chargeGift(SMGiftRequest paramSMGiftRequest, SMResponse paramSMResponse)
      throws SMException{
    	if(paramSMGiftRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = "SUCCESSS";
    	if(!paramSMResponse.isSuccess()){
    		status = "FAILURE";
    	}
    	String classType = null;
    	String srvKey = paramSMGiftRequest.getSrvkey();
    	classType = getClassType(srvKey);
    	
    	String refId = paramSMGiftRequest.getReqRefId();
    	if(refId.startsWith("RBTGIFT") || refId.startsWith("RBT_GIFT")){
    		if(classType!= null && classType.indexOf("_GIFT") != -1)
				classType = classType.substring(0, classType.indexOf("_GIFT"));
    	}
    	
    	String retStatus = m_daemonHelper.processGift(paramSMGiftRequest.getMdn(), status, paramSMGiftRequest.getReqRefId(), 
    			paramSMResponse.getAmount(), classType, paramSMGiftRequest.getEventKey(), paramSMResponse.getTransId(),null);// RBT-14301: Uninor MNP changes.
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public  SMCallbackStatus upgradeService(SMUpgradeRequest paramSMUpgradeRequest, SMResponse paramSMResponse)
      throws SMException{
    	if(paramSMUpgradeRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = getStatus(paramSMResponse);
    	String retStatus = null;
    	String srvKey = paramSMUpgradeRequest.getSrvkey();
    	String classType = null;
    	String action = "UPG";
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);// RBT-14301: Uninor MNP changes.
    		retStatus = m_daemonHelper.subscription(paramSMUpgradeRequest.getMdn(), action, null, status, paramSMUpgradeRequest.getReqRefId(), paramSMUpgradeRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null, null);// RBT-14301: Uninor MNP changes.
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public  SMCallbackStatus autoUpgradeService(SMUpgradeRequest paramSMUpgradeRequest, SMResponse paramSMResponse)
      throws SMException{ 
    	if(paramSMUpgradeRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = "SUCCESS";
    	String retStatus = null;
    	String srvKey = paramSMUpgradeRequest.getSrvkey();
    	String classType = null;
    	String action = "REN";
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.subscription(paramSMUpgradeRequest.getMdn(), action, null, status, paramSMUpgradeRequest.getReqRefId(), paramSMUpgradeRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null, null);// RBT-14301: Uninor MNP changes.
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.smProcessSelection(paramSMUpgradeRequest.getMdn(), action, null, status, paramSMUpgradeRequest.getReqRefId(), 
    				paramSMUpgradeRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null);
    	}
    	else if(srvKey.startsWith("RBT_PACK_")){
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.packSelection(paramSMUpgradeRequest.getMdn(), action, null, status, paramSMUpgradeRequest.getReqRefId(), paramSMUpgradeRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), null,null);// RBT-14301: Uninor MNP changes.
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }    

    public  SMCallbackStatus confirmCharge(SMRequest paramSMRequest, SMResponse paramSMResponse)
      throws SMException{ 
    	if(paramSMRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String retStatus = m_daemonHelper.processConfirmCharge(paramSMRequest.getMdn(), paramSMRequest.getReqRefId());
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public  SMCallbackStatus preCharge(SMPrechargeRequest paramSMPrechargeRequest, SMResponse paramSMResponse)
      throws SMException{ 
    	if(paramSMPrechargeRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = getStatus(paramSMResponse);
    	String retStatus = null;
    	String srvKey = paramSMPrechargeRequest.getSrvkey();
    	String classType = null;
    	String action = "ACT";
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);
    		retStatus = directActivationNDeActivation(action, classType, paramSMPrechargeRequest.getMdn(), paramSMResponse);
    		if(retStatus == null){
    			retStatus = m_daemonHelper.subscription(paramSMPrechargeRequest.getMdn(), action, null, status, paramSMPrechargeRequest.getReqRefId(), paramSMPrechargeRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null, null);// RBT-14301: Uninor MNP changes.
    		}
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.smProcessSelection(paramSMPrechargeRequest.getMdn(), action, null, status, paramSMPrechargeRequest.getReqRefId(), 
    				paramSMPrechargeRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null);
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public  SMCallbackStatus preChargeCombo(SMPrechargeRequest paramSMPrechargeRequest, SMResponse paramSMResponse)
      throws SMException{ 
//    	if(paramSMPrechargeRequest == null || paramSMResponse == null){
//    		throw new SMException("Arguments are passing as null, Please check arguments");
//    	}
//    	String status = getStatus(paramSMResponse);
//    	String retStatus = null;
//    	String srvKey = paramSMPrechargeRequest.getSrvkey();
//    	String subSrvKey = paramSMPrechargeRequest.getLinkedSrvKey();
//    	String classType = null;
//    	String action = "ACT";
//    	if(subSrvKey.startsWith("RBT_ACT_")){
//    		classType = getClassType(subSrvKey);
//    		retStatus = m_daemonHelper.subscription(paramSMPrechargeRequest.getMdn(), action, null, status, paramSMPrechargeRequest.getReqRefId(), paramSMPrechargeRequest.getType(), 
//    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
//    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus());
//    	}
//    	
//    	if(retStatus != null && retStatus.equals("SUCCESS") && (srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_"))){
//    		classType = getClassType(srvKey);
//    		retStatus = m_daemonHelper.smProcessSelection(paramSMPrechargeRequest.getMdn(), action, null, status, paramSMPrechargeRequest.getReqRefId(), 
//    				paramSMPrechargeRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
//    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus());
//    	}
//    	logger.info("Ret Status : " + retStatus);
//    	return getCallbackStatus(retStatus);
    	
    	throw new SMException("RBT does not support this model currently");
    	
    }

    public  SMCallbackStatus refund(SMRefundRequest paramSMRefundRequest, SMResponse paramSMResponse)
      throws SMException{
    	if(paramSMRefundRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String classType = null;
    	String action = "ACT";
    	String strSrvKey = paramSMRefundRequest.getSrvkey();
    	String status = "SUCCESS";
    	String retStatus = null;
    	if(!paramSMResponse.isSuccess()){
    		status = "FAILURE";
    	}
    	if(strSrvKey.startsWith("RBT_ACT_"))
    	{
    		classType = getClassType(strSrvKey);
    		retStatus = m_daemonHelper.refundSubscription(paramSMRefundRequest.getMdn(), action, null, status, paramSMRefundRequest.getReqRefId(), 
    				paramSMRefundRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(),null);// RBT-14301: Uninor MNP changes.
    	}
    	else if(strSrvKey.startsWith("RBT_SEL_") || strSrvKey.startsWith("RBT_SET_"))
    	{
    		classType = getClassType(strSrvKey);
    		retStatus = m_daemonHelper.refundSelection(paramSMRefundRequest.getMdn(), action, null, status, paramSMRefundRequest.getReqRefId(), paramSMRefundRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(),null);// RBT-14301: Uninor MNP changes.
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    /* (non-Javadoc)
     * @see com.onmobile.prism.client.handler.SMCallbackInterface#refundCombo(com.onmobile.prism.client.core.SMRefundRequest, com.onmobile.prism.client.core.SMResponse)
     */
    public  SMCallbackStatus refundCombo(SMRefundRequest paramSMRefundRequest, SMResponse paramSMResponse)
      throws SMException{ 
//    	if(paramSMRefundRequest == null || paramSMResponse == null){
//    		throw new SMException("Arguments are passing as null, Please check arguments");
//    	}
//    	String classType = null;
//    	String action = "ACT";
//    	String subSrvKey = paramSMRefundRequest.getLinkedSrvKey();
//    	String strSrvKey = paramSMRefundRequest.getSrvkey();  
//    	String status = "SUCCESS";
//    	String retStatus = null;
//    	if(!paramSMResponse.isSuccess()){
//    		status = "FAILURE";
//    	}
//    	if(subSrvKey.startsWith("RBT_ACT_"))
//    	{
//    		classType = getClassType(subSrvKey);
//    		retStatus = m_daemonHelper.refundSubscription(paramSMRefundRequest.getMdn(), action, null, status, paramSMRefundRequest.getReqRefId(), 
//    				paramSMRefundRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage());
//    	}
//    	
//    	if(retStatus != null && retStatus.equals("SUCCESS") && (strSrvKey.startsWith("RBT_SEL_") || strSrvKey.startsWith("RBT_SET_")))
//    	{
//    		classType = getClassType(strSrvKey);
//    	
//    		retStatus = m_daemonHelper.refundSelection(paramSMRefundRequest.getMdn(), action, null, status, paramSMRefundRequest.getReqRefId(), paramSMRefundRequest.getType(), 
//    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage());
//		}
//    	logger.info("Ret Status : " + retStatus);
//    	return getCallbackStatus(retStatus);
    	
    	throw new SMException("RBT does not support this model currently");
    }
    

    public  SMCallbackStatus renewalTrigger(SMRequest paramSMRequest, SMResponse paramSMResponse)
      throws SMException{ 
    	if(paramSMRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = getStatus(paramSMResponse);
    	String retStatus = null;
    	String srvKey = paramSMRequest.getSrvkey();
    	String classType = null;
    	String action = "REN";
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.subscription(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null, null);// RBT-14301: Uninor MNP changes.
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		classType = getClassType(srvKey);    		
    		
    		retStatus = m_daemonHelper.smProcessSelection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), 
    				paramSMRequest.getType(), paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null);
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }

    public  SMCallbackStatus suspend(SMRequest paramSMRequest, SMResponse paramSMResponse)
      throws SMException{ 
    	if(paramSMRequest == null || paramSMResponse == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String status = getStatus(paramSMResponse);
    	String srvKey = paramSMRequest.getSrvkey();
    	String classType = null;
    	String action = null;
    	String retStatus = null;
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);
    		action = "SUS";
    		retStatus = m_daemonHelper.subscription(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), paramSMResponse.getMessage(), 
    				Integer.toString(paramSMResponse.getStatusCode()), paramSMResponse.getCurrentSubStatus(), null, null, null, null,null,null, null);// RBT-14301: Uninor MNP changes.
    	}
    	else if(srvKey.startsWith("RBT_SEL_") || srvKey.startsWith("RBT_SET_")){
    		classType = getClassType(srvKey);
    		action = "SUS";
    		
    		CallbackRequest callbackReqObj=new CallbackRequest();
			callbackReqObj.setStrSubID(paramSMRequest.getMdn());
			callbackReqObj.setAction(action);
			callbackReqObj.setStatus(status);
			callbackReqObj.setRefID(paramSMRequest.getReqRefId());
			callbackReqObj.setType(paramSMRequest.getType());
			callbackReqObj.setAmountCharged(paramSMResponse.getAmount());
			callbackReqObj.setClassType(classType);
			callbackReqObj.setReason(paramSMResponse.getMessage());
			callbackReqObj.setReasonCode(Integer.toString(paramSMResponse.getStatusCode()));
    		/*retStatus = m_daemonHelper.selection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), null, null,null);*/
    		// RBT-14301: Uninor MNP changes.
    		retStatus = m_daemonHelper.selection(callbackReqObj);
    	}
    	else if(srvKey.startsWith("RBT_PACK_")){
    		classType = getClassType(srvKey);
    		action = "SUS";
    		retStatus = m_daemonHelper.packSelection(paramSMRequest.getMdn(), action, null, status, paramSMRequest.getReqRefId(), paramSMRequest.getType(), 
    				paramSMResponse.getAmount(), classType, paramSMResponse.getMessage(), Integer.toString(paramSMResponse.getStatusCode()), null,null);// RBT-14301: Uninor MNP changes.
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    } 
    
    public SMCallbackStatus resume(SMRequest smReq, SMResponse smRes) throws SMException{ 
    	if(smReq == null || smRes == null){
    		throw new SMException("Arguments are passing as null, Please check arguments");
    	}
    	String action = "RES";
    	String status = getStatus(smRes);
    	String classType = null;
    	String retStatus = null;
    	String srvKey = smReq.getSrvkey();
    	if(srvKey.startsWith("RBT_ACT_")){
    		classType = getClassType(srvKey);
    		retStatus = m_daemonHelper.subscription(smReq.getMdn(), action, null, status, smReq.getReqRefId(), smReq.getType(), 
    	    			smRes.getAmount(), classType, smRes.getMessage(), smRes.getMessage(), 
    					Integer.toString(smRes.getStatusCode()), smRes.getCurrentSubStatus(), null, null, null, null,null,null, null);// RBT-14301: Uninor MNP changes.
    	}
    	logger.info("Ret Status : " + retStatus);
    	return getCallbackStatus(retStatus);
    }
    
    public SMCallbackStatus directActivation(SMRequest smReq, SMResponse smRes) throws SMException{
    	try{
    		Hashtable userInfo = smRes.getUserInfo();
    		String info = null;
    		if(userInfo == null){
    			throw new SMException("UserInfo object is null");
    		}
    		if(userInfo.containsKey("info")){
    			info = userInfo.get(info).toString();
    		}
    		else{
    			throw new SMException("UserInfo object doesn't contains info parameter");
    		}
    		
    		String retStatus = m_daemonHelper.dircectAct(smReq.getMdn(), info, smRes.getUserType(),smRes.getMode());
    		return getCallbackStatus(retStatus);
    	}
    	catch(Exception e){
    		logger.error("", e);
    		throw new SMException(e.getMessage());    		
    	}
    }
}