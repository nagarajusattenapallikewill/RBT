package com.onmobile.apps.ringbacktones.ussd.common;

import java.util.HashMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.ussd.airtel.AirtelUSSDSelectionBean;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;

public class USSDWebServiceController {
	private static Logger basicLogger = Logger.getLogger(USSDWebServiceController.class);
	RBTClient rbtClient=null;
	public USSDWebServiceController(){
		try {
			rbtClient=RBTClient.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String addProfileSel(String langg,String callerId,String subId,String isPrepaid,String clipId,String profileHours){
		basicLogger.debug(" addProfileSel "+" callerID "+callerId+" subId "+subId+" isPrepaid "+isPrepaid+" clipId "+clipId+" profilehrs "+profileHours+" langg"+langg);
		String method="addProfileSel";
		String response=null;
		SelectionRequest selectionRequest=new SelectionRequest(null);
		selectionRequest.setMode("USSD");
		selectionRequest.setLanguage(langg);
		selectionRequest.setModeInfo("USSD"+":"+null);
		selectionRequest.setCallerID(callerId);
		selectionRequest.setSubscriberID(subId);
		selectionRequest.setStatus(99);
		selectionRequest.setCategoryID("99");
		selectionRequest.setIsPrepaid((isPrepaid.indexOf("true"))!=-1?true:false);
		selectionRequest.setClipID(clipId);
		selectionRequest.setProfileHours(profileHours);


		try {
			rbtClient.addSubscriberSelection(selectionRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response=selectionRequest.getResponse();
		basicLogger.debug(" addProfileSel response "+response);
		return response;

	}
	/*
	 * Is user define shuffle/album is On
	 */
	public boolean isUDAOn(String subscriberId){
		basicLogger.debug(" isUDAOn subID : "+subscriberId);
		Subscriber sub=getSubscriberObject(subscriberId);
		//if(sub!=null&&(sub.getStatus().equalsIgnoreCase("new_user")|| sub.getStatus().equalsIgnoreCase("deactive"))){
		//	return true;
		//}
		if(sub!=null)
			return sub.isUdsOn();
		return false;
	}
	/*
	 * addSelections
	 */
	public String addSelections(AirtelUSSDSelectionBean selBean){
		basicLogger.info(" addSelections selBean : "+selBean);
		SelectionRequest selectionRequest = new SelectionRequest(selBean.getSubscriberId());
		selectionRequest.setCallerID(selBean.getCallerId());
		selectionRequest.setOperatorUserInfo(selBean.getOperatorUserInfo());
		selectionRequest.setCategoryID(selBean.getCatID());
		selectionRequest.setCircleID(selBean.getCircleId());
		selectionRequest.setClipID(selBean.getClipId());
		selectionRequest.setMode("USSD");
		selectionRequest.setModeInfo("USSD"+":"+null);
		selectionRequest.setIsPrepaid(selBean.isPrepaid());
		selectionRequest.setChargeClass(selBean.getChargeClass());
		selectionRequest.setInLoop(selBean.isLoop());
		//selectionRequest.setStatus(selBean.getStatus());
		if(selBean.isUdsOn()){
			HashMap<String, String> userInfoMap =new HashMap<String,String>();
			userInfoMap.put("UDS_OPTIN", "TRUE");
			selectionRequest.setUserInfoMap(userInfoMap);
		}
		rbtClient.addSubscriberSelection(selectionRequest);
		basicLogger.debug(" addSelections response : "+selectionRequest.getResponse());
		return selectionRequest.getResponse();
	}
	//make UDS ON
	public String makeUDSOn(String subscriberID){
		basicLogger.info(" makeUDSOn "+subscriberID);
		UpdateDetailsRequest updateDetailsReq=new UpdateDetailsRequest(subscriberID);
		updateDetailsReq.setIsUdsOn(true);
		rbtClient.setSubscriberDetails(updateDetailsReq);
		return updateDetailsReq.getResponse();

	}
	//deactivate an user
	public String unSubscribeUser(String subID){
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(subID);
		subscriptionRequest.setMode("USSD");
		rbtClient.deactivateSubscriber(subscriptionRequest);
		return subscriptionRequest.getResponse();
	}
	/*
	 * Get copytunes 
	 */
	public CopyData getCopyTunes(String subscriberID,String callerID){
		basicLogger.info(" getCopyTunes subID : fromsubidID "+subscriberID+" : "+callerID);
		CopyRequest copyRequest=new CopyRequest(subscriberID,callerID);
		CopyDetails copyDataObj = rbtClient.getCopyData(copyRequest);
		CopyData[] copydata=null;
		if(copyDataObj!=null){
			copydata=copyDataObj.getCopyData();
		}
		basicLogger.info(" getCopyTunes copyData req resp : "+copyRequest.getResponse());
		if(copyRequest.getResponse()!=null&&copyRequest.getResponse().equalsIgnoreCase("SUCCESS")&&copydata!=null&&copydata.length!=0){
			return copydata[0];
		}
		return null;
	}
	public String makeCopySelection(String subID, String fromSubID, String catID, String clipID, String status, String callerID,String requestMode){
		basicLogger.debug(" makeCopySelection : subID "+subID+" fromSubid :"+fromSubID+" catID : "+catID+" clipid : "+clipID+" calerID : "+callerID+" reqMode "+requestMode);
		int catId=0;
		int clipId=0;
		int stat=1;
		try{
			catId=Integer.parseInt(catID);
			clipId=Integer.parseInt(clipID);
			stat=Integer.parseInt(status);
		}catch(Exception e){
             stat=1;
		}
		CopyRequest copyRequest = new CopyRequest(subID,fromSubID,catId,clipId, stat,callerID);
		copyRequest.setMode(requestMode);
		rbtClient.copy(copyRequest);
		basicLogger.info(" makeCopySelection response "+copyRequest.getResponse());
		return copyRequest.getResponse();

	}
	/*
	 * To upgrade subscription
	 */
	public String upgradeSubscription(String subscriberID,String rentalPack){
		basicLogger.debug(" upgradeSubscription subID : "+subscriberID+" rentalPack : "+rentalPack);
		boolean isPrepaid=false;
		String circleID=null;
		Subscriber sub=getSubscriberObject(subscriberID);
		if(sub!=null){
			isPrepaid=sub.isPrepaid();
			circleID=sub.getCircleID();
		}
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(subscriberID);
		subscriptionRequest.setCircleID(circleID);
		subscriptionRequest.setIsPrepaid(isPrepaid);
		subscriptionRequest.setMode("USSD");
		subscriptionRequest.setModeInfo("USSD"+":"+null);
		subscriptionRequest.setRentalPack(rentalPack);
		rbtClient.activateSubscriber(subscriptionRequest);
		basicLogger.info(" upgradeSubscription response "+subscriptionRequest.getResponse());
		return subscriptionRequest.getResponse();
	}
	//get subscriberObject
	public Subscriber getSubscriberObject(String subscriberId){
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		return subscriber;
	}
}
