package com.onmobile.apps.ringbacktones.wrappers;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.api.Selection;
import com.onmobile.apps.ringbacktones.webservice.api.SetSubscriberDetails;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RBTLoginUser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.DownLoadBean;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.GiftProcessorBean;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.GiftSelBean;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SelectionRequestBean;
import com.onmobile.apps.ringbacktones.wrappers.rbtclientbeans.SubscriptionBean;


public class SubscriberRbtClientWrapper {
	private  RBTClient subRbtClient = null;

	private  static String CCC="CCC";
	private static String CRICKET="CRICKET";
	private static String SONG_PACK="SONG_PACK";


	static final org.apache.log4j.Logger c_logger = org.apache.log4j.Logger.getLogger(SubscriberRbtClientWrapper.class);

	public  String getCCC() {
		return CCC;
	}
	public  void setCCC(String ccc) {
		CCC = ccc;
	}
	private SubscriberRbtClientWrapper(){

	}
	private  void init()
	{
		String method="SubscriberWebClientController init()";
		try
		{
			subRbtClient =RBTClient.getInstance();	
		}
		catch (Exception e)
		{
			subRbtClient = null;
			c_logger.warn(method+"->"+ "Got exception while initializing subRbtClinet", e);
		}
	}
	
	public static SubscriberRbtClientWrapper getInstance(){
		SubscriberRbtClientWrapper subWebClientController=new SubscriberRbtClientWrapper();
		subWebClientController.init();
		return (subWebClientController);
	}
	public boolean makeThirdPartyRequest(String subId,String info){
		String method="makeThirdPartyRequest";
		boolean returnFlag=false;
		UtilsRequest utilsRequest = new UtilsRequest(subId);
        utilsRequest.setInfo(info);
        subRbtClient.makeThirdPartyRequest(utilsRequest);
        String response=utilsRequest.getResponse();
        if(response!=null && response.length()>0 && response.equalsIgnoreCase("0")){
        	returnFlag=true;
        }
        c_logger.warn(method+"->response=="+utilsRequest.getResponse());
        
		return returnFlag;
	}
	public HashMap<String, String> getUserInfoFromOperator(String subId,boolean isPrepaid,StringBuffer strBuff){
		RbtDetailsRequest rbtDetRequest=new RbtDetailsRequest(null);
		rbtDetRequest.setSubscriberID(subId);
		Boolean isPrepaidBoolean=Boolean.valueOf(isPrepaid);
		rbtDetRequest.setIsPrepaid(isPrepaidBoolean);
		HashMap<String, String> operatorInfo=subRbtClient.getUserInfoFromOperator(rbtDetRequest);
		if(rbtDetRequest.getResponse()!=null && rbtDetRequest.getResponse().length()>0){
			strBuff.append(rbtDetRequest.getResponse());
		}
		if(operatorInfo!=null && operatorInfo.size()>0){
			return operatorInfo;
		}
		return null;
	}
	public Feed getFeed(String type,String name,String mode){
		ApplicationDetailsRequest appDetRequest=new ApplicationDetailsRequest();
		appDetRequest.setType(type);
		appDetRequest.setName(name);
		appDetRequest.setMode(mode);
		
		Feed feed=subRbtClient.getFeed(appDetRequest);
		return feed;
	}
	public ArrayList<Feed> getFeeds(String type,String name,String mode){
		ApplicationDetailsRequest appDetRequest=new ApplicationDetailsRequest();
		appDetRequest.setType(type);
		appDetRequest.setName(name);
		appDetRequest.setMode(mode);
		
		Feed[] feedTemp=subRbtClient.getFeeds(appDetRequest);
		ArrayList<Feed> feed=null;
		if(feedTemp!=null && feedTemp.length>0){
			for(int index=0;index<feedTemp.length;index++){
				Feed tempFeed=feedTemp[index];
				if(tempFeed!=null){
					if(feed==null){
						feed=new ArrayList<Feed>();
					}
					feed.add(tempFeed);
				}
			}
		}
		return feed;
	}
	public ArrayList<Feed> getFeeds(String type,String mode){
		ApplicationDetailsRequest appDetRequest=new ApplicationDetailsRequest();
		appDetRequest.setType(type);
		appDetRequest.setMode(mode);
		Feed[] feedTemp=subRbtClient.getFeeds(appDetRequest);
		ArrayList<Feed> feed=null;
		if(feedTemp!=null && feedTemp.length>0){
			for(int index=0;index<feedTemp.length;index++){
				Feed tempFeed=feedTemp[index];
				if(tempFeed!=null){
					if(feed==null){
						feed=new ArrayList<Feed>();
					}
					feed.add(tempFeed);
				}
			}
		}
		return feed;
	}
	public Rbt upgradeSongPacks(String subId,boolean isPrepaid,int cosId,String mode,StringBuffer responseString){
		String method="upgradeSongPacks";
		SelectionRequest selectionRequest=new SelectionRequest(subId);
		selectionRequest.setIsPrepaid(isPrepaid);
		selectionRequest.setCosID(cosId);
		selectionRequest.setMode(mode);
		Rbt rbt=subRbtClient.upgradeSelectionPack(selectionRequest);
		c_logger.info(" selectSongPacks : "+method+"->Response : "+selectionRequest.getResponse());
		responseString.append(selectionRequest.getResponse());
		return rbt;
	}
	public String resumeService(String subId,boolean suspend,String type){
		String method="suspendService";
		UtilsRequest utilsRequest =new UtilsRequest(subId,suspend,type);
		subRbtClient.suspension(utilsRequest);
		c_logger.info(" suspendService : "+method+"-> Response : "+utilsRequest.getResponse());	
		return utilsRequest.getResponse();
	}
	public String suspendService(String subId,boolean suspend,String type,String mode){
		String method="suspendService";
		UtilsRequest utilsRequest =new UtilsRequest(subId,suspend,type);
		utilsRequest.setMode(mode);
		subRbtClient.suspension(utilsRequest);
		c_logger.info(" suspendService : "+method+"-> Response : "+utilsRequest.getResponse());	
		return utilsRequest.getResponse();
	}
	public Cos getCos(String circleId,boolean isPrepaid,String subId,String mode){
		String method="getSongPacks";
		ArrayList<Cos> songPackList=null;
		ApplicationDetailsRequest appDetRequest=new ApplicationDetailsRequest();
		appDetRequest.setCircleID(circleId);
		appDetRequest.setIsPrepaid(isPrepaid);
		appDetRequest.setMode(mode);
		appDetRequest.setSubscriberID(subId);
		c_logger.info(method+"->"+ "circleId=="+circleId+",prepaidYes=="+isPrepaid+",mode=="+mode);
		Cos cos=subRbtClient.getCos(appDetRequest);
	
		c_logger.info(method+"->"+ "response=="+appDetRequest.getResponse());
		
		return cos;
	}
	public ArrayList<Cos> getCoses(String circleId,boolean isPrepaid,String type,String mode){
		String method="getSongPacks";
		ArrayList<Cos> songPackList=null;
		ApplicationDetailsRequest appDetRequest=new ApplicationDetailsRequest();
		appDetRequest.setType(type);
		appDetRequest.setCircleID(circleId);
		appDetRequest.setIsPrepaid(isPrepaid);
		appDetRequest.setMode(mode);
		c_logger.info(method+"->"+ "circleId=="+circleId+",prepaidYes=="+isPrepaid+",type=="+type+",mode=="+mode);
		Cos[] coses=subRbtClient.getCoses(appDetRequest);
	
		c_logger.info(method+"->"+ "response=="+appDetRequest.getResponse());
		if(coses!=null && coses.length>0){
			for(int index=0;index<coses.length;index++){
				Cos temoCos=(Cos)coses[index];
				if(temoCos!=null){
					if(songPackList==null){
						songPackList=new ArrayList<Cos>();
					}
					songPackList.add(temoCos);
				}
			}
		}
//		DataRequest dre=new DataRequest(null);
//		dre.
		return songPackList;
	}
	public RBTLoginUser getUserDetails(String username,String type,String mode,boolean encryptionAllowed,StringBuffer responseBuff){
		String method="getUserDetails";
		ApplicationDetailsRequest appDetRequest=new ApplicationDetailsRequest();
		appDetRequest.setUserID(username);
		appDetRequest.setType(type);
		appDetRequest.setMode(mode);
		appDetRequest.setEncryptPassword(encryptionAllowed);
		RBTLoginUser userDetails=subRbtClient.getRBTLoginUser(appDetRequest);
		responseBuff.append(appDetRequest.getResponse());
		if(userDetails!=null){
			return userDetails;
		}
		return null;
	}
	public void giftRBTSelection(GiftSelBean giftBean,String requestMode,StringBuffer responseBuff){
		String method="giftRBTSelection";
		GiftRequest giftRequest=new GiftRequest();
		giftRequest.setGifterID(giftBean.getSubId());
		giftRequest.setGifteeID(giftBean.getGifteeId());
		giftRequest.setMode(requestMode);
		giftRequest.setCategoryID(giftBean.getGiftCatId());
		giftRequest.setToneID(giftBean.getGiftClipId());
		subRbtClient.sendGift(giftRequest);
		responseBuff.append(giftRequest.getResponse());
		c_logger.info(method+"->"+"subId=="+giftBean.getSubId()+",response=="+giftRequest.getResponse());
	}
	public void giftRBTService(String msisdn,String gifteeId,String requestMode,StringBuffer responseBuff){
		String method="giftRBTService";
		GiftRequest giftRequest=new GiftRequest();
		giftRequest.setGifterID(msisdn);
		giftRequest.setGifteeID(gifteeId);
		giftRequest.setMode(requestMode);
		subRbtClient.sendGift(giftRequest);
		responseBuff.append(giftRequest.getResponse());
		c_logger.info(method+"->"+"subId=="+msisdn+",response=="+giftRequest.getResponse());
	}
	public Rbt getSubscriberGiftOutboxNChargingDetails(String msisdn,StringBuffer responseBuff){
		RbtDetailsRequest rbtDetRequest=new RbtDetailsRequest(null);
		rbtDetRequest.setSubscriberID(msisdn);
		rbtDetRequest.setMode(CCC);
		rbtDetRequest.setInfo("gift_outbox,transaction_history");
		Rbt subDet=subRbtClient.getRBTUserInformation(rbtDetRequest);
		responseBuff.append(rbtDetRequest.getResponse());
		if(subDet!=null){
			return subDet;
		}
		DataRequest dre=new DataRequest(null);
//		dre.
		return null;
	}
	public Subscriber getSubscriber(String msisdn,String mode){
		RbtDetailsRequest rbtDetRequest=new RbtDetailsRequest(null);
		rbtDetRequest.setSubscriberID(msisdn);
		rbtDetRequest.setMode(mode);
		Subscriber sub=subRbtClient.getSubscriber(rbtDetRequest);
		if(sub!=null){
			return sub;
		}
		return null;
	}
	public Rbt getSubscriberDetails(String msisdn,String mode,String selInfo,StringBuffer responseBuff){
		RbtDetailsRequest rbtDetRequest=new RbtDetailsRequest(null);
		rbtDetRequest.setSubscriberID(msisdn);
		rbtDetRequest.setMode(mode);
		if(selInfo!=null){
			rbtDetRequest.setInfo(selInfo);
		}
		//		rbtDetRequest.setInfo("gift_outbox,transaction_history");
		Rbt subDet=subRbtClient.getRBTUserInformation(rbtDetRequest);
		responseBuff.append(rbtDetRequest.getResponse());
		if(subDet!=null){
			return subDet;
		}
		return null;
	}
	public Subscriber activateSubscriber(SubscriptionBean subBean,StringBuffer strBuff,String requestMode,String actInfo){
		String method="activateSubscriber";
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(null);
		subscriptionRequest.setSubscriberID(subBean.getSubId());
		subscriptionRequest.setMode(requestMode);
		subscriptionRequest.setModeInfo(actInfo);
		if(subBean.getCosId() != null)
		{
			subscriptionRequest.setCosID(Integer.parseInt(subBean.getCosId()));
			if(RBTDBManager.getInstance().azaanDefaultCosId != null && RBTDBManager.getInstance().azaanDefaultCosId.equals(subBean.getCosId()))
				subscriptionRequest.setSubscriptionClass(subBean.getSubcriptionClass());
		}
		else 
			subscriptionRequest.setSubscriptionClass(subBean.getSubcriptionClass());
		
		if(subBean.getSubOfferId() != null) {
			subscriptionRequest.setOfferID(subBean.getSubOfferId());
		}
		
		boolean allowGetOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ALLOW_GET_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		boolean allowOnlyBaseOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ALLOW_ONLY_BASE_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		
		subscriptionRequest.setCircleID(subBean.getCircleID());
		subscriptionRequest.setIsPrepaid(subBean.getIsPrepaid());
		
		if(allowGetOffer || allowOnlyBaseOffer)
			setBaseOffer(subscriptionRequest);
		if(subBean!=null && subBean.getLanguage()!=null && subBean.getLanguage().length()>0 && !subBean.getLanguage().equalsIgnoreCase("null")){
			subscriptionRequest.setLanguage(subBean.getLanguage());
		}
		if(subBean!=null && subBean.getExtraInfo()!=null){
			subscriptionRequest.setUserInfoMap(subBean.getExtraInfo());
		}
		
		Subscriber sub=null;
		CosDetails cosDetails = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(subBean.getCosId());
		if(cosDetails != null && (iRBTConstant.LIMITED_DOWNLOADS.equalsIgnoreCase(cosDetails.getCosType()) || iRBTConstant.UNLIMITED_DOWNLOADS_OVERWRITE.equalsIgnoreCase(cosDetails.getCosType()) 
				|| iRBTConstant.LIMITED_SONG_PACK_OVERLIMIT.equalsIgnoreCase(cosDetails.getCosType())))
			sub = subRbtClient.upgradeSubscriber(subscriptionRequest);
		else
			sub = subRbtClient.activateSubscriber(subscriptionRequest);
		String response=subscriptionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+subBean.getSubId()+",response=="+response);
		return sub;
	}
	public Subscriber updateSubPack(SubscriptionBean subBean,StringBuffer strBuff,String requestMode,String actInfo){
		String method="updateSubPack";
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(null);
		subscriptionRequest.setSubscriberID(subBean.getSubId());
		subscriptionRequest.setRentalPack(subBean.getSubcriptionClass());
		subscriptionRequest.setMode(requestMode);
		subscriptionRequest.setModeInfo(actInfo);
		if(subBean!=null && subBean.getExtraInfo()!=null){
			subscriptionRequest.setUserInfoMap(subBean.getExtraInfo());
		}
		Subscriber sub=subRbtClient.activateSubscriber(subscriptionRequest);
		String response=subscriptionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+subBean.getSubId()+",response=="+response);
		return sub;
	}
	public Subscriber updateSubcriberInfo(SubscriptionBean subBean,StringBuffer strBuff,String requestMode,String actInfo){
		String method="updateSubPack";
		UpdateDetailsRequest updateSub=new UpdateDetailsRequest(subBean.getSubId());
		if (subBean.getAge()!=-1) {
			updateSub.setAge(subBean.getAge());
		}
		if(subBean.getBlackListed()!=null && subBean.getBlackListed()){
			updateSub.setIsBlacklisted(subBean.getBlackListed());
		}
		if(subBean.getGender()!=null && subBean.getGender().length()>0 && !subBean.getGender().equalsIgnoreCase("null")){
			updateSub.setGender(subBean.getGender());
		}
		if(subBean.getType()!=null && subBean.getType().length()>0 && !subBean.getType().equalsIgnoreCase("null")){
				updateSub.setType(subBean.getType());
		}
		if(subBean.getNewsletterOn()!=null && subBean.getNewsletterOn()){
			updateSub.setIsNewsLetterOn(subBean.getNewsletterOn());
		}
		if(subBean.getOverlayOn()!=null && subBean.getOverlayOn()){
			updateSub.setIsOverlayOn(subBean.getOverlayOn());
		}
		if(subBean.getPollOn()!=null && subBean.getPollOn()){
			updateSub.setIsPollOn(subBean.getPollOn());
		}
		
		if (subBean.getBlackListType()!=null && subBean.getBlackListType().length()>0 && !subBean.getBlackListType().equalsIgnoreCase("null")) {
			updateSub.setBlacklistType(subBean.getBlackListType());
		}
		if (subBean.getLanguage()!=null && subBean.getLanguage().length()>0 && !subBean.getLanguage().equalsIgnoreCase("null")) {
			updateSub.setLanguage(subBean.getLanguage());
		}
		if(subBean.getDisableIntroPrompt()!=null && subBean.getDisableIntroPrompt()){
			updateSub.setIsPressStarIntroEnabled(false);
		}
		if(subBean.getPrepaidToPostpaidFlag()!=null && subBean.getPrepaidToPostpaidFlag()){
			updateSub.setIsPrepaid(!subBean.getIsPrepaid());
		}
		Subscriber subscriber=subRbtClient.setSubscriberDetails(updateSub);
		String response=updateSub.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+subBean.getSubId()+",response=="+response);
		return subscriber;
	}
	public boolean unsubscribe(String msisdn,boolean checkSubscriptionClass,String requestMode,String deActInfo,StringBuffer strBuff){
		String method="unsubscribe";
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(null);
		subscriptionRequest.setMode(requestMode);
		subscriptionRequest.setSubscriberID(msisdn);
		subscriptionRequest.setModeInfo(deActInfo);
		subscriptionRequest.setCheckSubscriptionClass(checkSubscriptionClass);
		Subscriber subscriber=subRbtClient.deactivateSubscriber(subscriptionRequest);
		String response=subscriptionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+msisdn+",response=="+response);
		if(subscriber!=null && subscriber.getStatus()!=null && (subscriber.getStatus().equalsIgnoreCase("deact_pending") || subscriber.getStatus().equalsIgnoreCase("deactive"))){
			return true;
		}
			
		return false;
	}
	public Subscriber unsubscribe(String msisdn,String requestMode,String deActInfo,StringBuffer strBuff){
		String method="unsubscribe";
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(null);
		subscriptionRequest.setMode(requestMode);
		subscriptionRequest.setSubscriberID(msisdn);
		subscriptionRequest.setModeInfo(deActInfo);
		Subscriber subscriber=subRbtClient.deactivateSubscriber(subscriptionRequest);
		String response=subscriptionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+msisdn+",response=="+response);
		return subscriber;
	}
	public ArrayList<CopyData> getCopyDetails(String msisdn,String copyNumber,StringBuffer strBuff){
		ArrayList copyDataList=null;
		CopyRequest copyRequest = new CopyRequest(msisdn, copyNumber);
		copyRequest.setMode(CCC);
		CopyDetails copyDetails = subRbtClient.getCopyData(copyRequest); 
		CopyData[] arrCopyData= copyDetails.getCopyData();
		if(arrCopyData!=null && arrCopyData.length>0){
			copyDataList=new ArrayList<CopyData>();
			for(int index=0;index<arrCopyData.length;index++){
				copyDataList.add(arrCopyData[index]);
			}
		}
		strBuff.append(copyRequest.getResponse());
		return copyDataList;
	}
	public Rbt acceptGift(GiftProcessorBean giftBean,String requestMode,String selInfo,StringBuffer strBuff){
		String method="acceptGift";
		SelectionRequest selectionRequest=new SelectionRequest(null);
		selectionRequest.setCallerID(giftBean.getCallerId());
		selectionRequest.setCategoryID(giftBean.getCatId());
		selectionRequest.setIsPrepaid(giftBean.isPrepaid());
		if(giftBean.getToneId()!=null && !giftBean.getToneId().equalsIgnoreCase("-1")){
		selectionRequest.setClipID(giftBean.getToneId());
		}
		if(giftBean.getGifterId()!=null && !giftBean.getGifterId().equalsIgnoreCase("-1")){
		selectionRequest.setGifterID(giftBean.getGifterId());
		}
		selectionRequest.setMode(requestMode);
		selectionRequest.setSubscriberID(giftBean.getSubId());
		selectionRequest.setModeInfo(selInfo);
		selectionRequest.setGiftSentTime(giftBean.getSentTime());
		Rbt rbt=subRbtClient.acceptGift(selectionRequest);
		String response=selectionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+giftBean.getSubId()+",response=="+response);
		return rbt;
	}
	public void delGift(GiftProcessorBean giftBean,String requestMode,StringBuffer strBuff){
		String method="delGift";
		GiftRequest giftRequest=new GiftRequest();
		giftRequest.setGifterID(giftBean.getGifterId());
		giftRequest.setMode(requestMode);
		giftRequest.setGiftSentTime(giftBean.getSentTime());
		giftRequest.setGifteeID(giftBean.getSubId());
		subRbtClient.rejectGift(giftRequest);
		String response=giftRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+giftBean.getSubId()+",response=="+response);
	}
	public Rbt downloadWithoutSel(DownLoadBean downLoadBean,String requestMode,String selInfo,StringBuffer responseBuff){
		String method="downloadWithoutSel";
		SelectionRequest selectionRequest=new SelectionRequest(null);
		selectionRequest.setSubscriberID(downLoadBean.getSubId());
		selectionRequest.setMode(requestMode);
		selectionRequest.setModeInfo(selInfo);
		selectionRequest.setCategoryID(downLoadBean.getDownloadCatId());
		selectionRequest.setClipID(downLoadBean.getDownloadClipId());
		selectionRequest.setIsPrepaid(downLoadBean.isPrepaid());
		selectionRequest.setChargeClass(downLoadBean.getDownloadChargeClass());
		Rbt rbt=subRbtClient.addSubscriberDownload(selectionRequest);
		responseBuff.append(selectionRequest.getResponse());
		c_logger.info(method+"->"+"subId=="+downLoadBean.getSubId()+",response=="+selectionRequest.getResponse());
		return rbt;
	}
	public Settings getSettings(String subscriberID){
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		Settings settings = subRbtClient.getSettings(rbtDetailsRequest);
		return settings;
	}
	public Rbt makeSelection(SelectionRequestBean selBean,String requestMode,String selInfo,StringBuffer strBuff, boolean isCallerSubscribed, String rbtTypeStr, String cosIdStr, String actBy){
		String method="makeSelection";
		Rbt rbt=null;
		SelectionRequest selectionRequest=new SelectionRequest(null);
		selectionRequest.setSubscriberID(selBean.getSubscriberId());
		selectionRequest.setUseUIChargeClass(selBean.getUseUIChargeClass());
		selectionRequest.setMode(requestMode);
		selectionRequest.setActivationMode(actBy);
		selectionRequest.setModeInfo(selInfo);
		selectionRequest.setCallerID(selBean.getCallerId());
		if(selBean.getToneId()!=null && selBean.getToneId()!="-1"){
			selectionRequest.setClipID(selBean.getToneId());
		}
		if(selBean.getCatId()!=null){
			selectionRequest.setCategoryID(selBean.getCatId());
		}
		if(selBean.getChargeClass()!=null && !selBean.getChargeClass().equalsIgnoreCase("") && !selBean.getChargeClass().equalsIgnoreCase("null")){
			selectionRequest.setChargeClass(selBean.getChargeClass());
		}
		selectionRequest.setSubscriberID(selBean.getSubscriberId());
		selectionRequest.setIsPrepaid(selBean.isPrepaid());
		if(selBean!=null && selBean.getSubscriptionClass()!=null){
			selectionRequest.setSubscriptionClass(selBean.getSubscriptionClass());
		}
		
		if(selBean.getSubOfferId() != null) {
			selectionRequest.setSubscriptionOfferID(selBean.getSubOfferId());			
		}
		
		if(selBean.getSelOfferId() != null) {
			selectionRequest.setOfferID(selBean.getSelOfferId());
		}
		
		boolean allowGetOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ALLOW_GET_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		boolean allowOnlyBaseOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ALLOW_ONLY_BASE_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		if(allowGetOffer || allowOnlyBaseOffer)
			setSelOffer(selectionRequest, isCallerSubscribed, rbtTypeStr, cosIdStr);
		else{
			HashMap<String, String> map = selBean.getSubscriberExtraInfo();
			String freeCopyAvailed = map.get(iRBTConstant.FREE_COPY_AVAILED);
			String freeCopySubClassAndChargeClass = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "FREE_COPY_SUB_CHARGE_CLASS", null);
			if(freeCopySubClassAndChargeClass != null && (freeCopyAvailed == null || freeCopyAvailed.equalsIgnoreCase("FALSE"))) {				
					String[] classes = freeCopySubClassAndChargeClass.split("\\,");  //SUB_CLASS,CHARGE_CLASS
					if(classes.length > 0) {
						selectionRequest.setSubscriptionClass(classes[0]);
					}
					if(classes.length > 1) {
						selectionRequest.setChargeClass(classes[1]);
						selectionRequest.setUseUIChargeClass(true);
					}
			}

			map.put(iRBTConstant.FREE_COPY_AVAILED,"TRUE");
			selBean.setSubscriberExtraInfo(map);
			HashMap<String,String> selectionMap = selBean.getExtraInfo();
			selectionMap.put(iRBTConstant.FREE_COPY_AVAILED, "TRUE");
			selBean.setExtraInfo(selectionMap);				
		}
		
		
		String rbtOption=selBean.getSetRBTOption();
		if(rbtOption!=null && rbtOption.equalsIgnoreCase("TimeOfTheDay")){
			selectionRequest.setStatus(selBean.getTimeOfTheDayStatus());
			if(selBean.getFromTimeOfTheDay()!=0 && selBean.getToTimeOfTheDay()!=23){
				selectionRequest.setFromTime(selBean.getFromTimeOfTheDay());
				selectionRequest.setToTime(selBean.getToTimeOfTheDay());
			}
		}else if(rbtOption!=null && rbtOption.equalsIgnoreCase("FutureDate")){
			selectionRequest.setStatus(selBean.getFutureDateStatus());
			c_logger.info(method+"->getSelInterval=="+selBean.getSelInterval());
			if(selBean.getSelInterval()!=null){
				selectionRequest.setInterval(selBean.getSelInterval());
			}
		}else if(rbtOption!=null && rbtOption.equalsIgnoreCase("DayOfTheWeek")){
			selectionRequest.setStatus(selBean.getDayOfTheWeekStatus());
			if(selBean.getSelInterval()!=null){
				selectionRequest.setInterval(selBean.getSelInterval());
			}
		}else{
			selectionRequest.setStatus(selBean.getStatus());
		}

		if(selBean!=null && selBean.getSetInLoop()!=null && selBean.getSetInLoop().equalsIgnoreCase("true")){
			selectionRequest.setInLoop(true);
		}
		if(selBean!=null && selBean.getProfileHour()!=null){
			selectionRequest.setProfileHours(selBean.getProfileHour());
		}
		if(selBean!=null && selBean.getCricpack()!=null){
			selectionRequest.setCricketPack(selBean.getCricpack());
		}
		if(selBean!=null && selBean.getMmContext()!=null){
			selectionRequest.setMmContext(selBean.getMmContext());
		}
		if(selBean!=null && selBean.getExtraInfo()!=null){
			Map<String, String> selectionInfoMapLocal = selectionRequest.getSelectionInfoMap();
			if(selectionInfoMapLocal == null) {
				selectionRequest.setSelectionInfoMap(selBean.getExtraInfo());
			}
			else {
				selectionRequest.getSelectionInfoMap().putAll(selBean.getExtraInfo());
			}
		}
		if(selBean!=null && selBean.getSubscriberExtraInfo()!=null ){
			selectionRequest.setUserInfoMap(selBean.getSubscriberExtraInfo());
		}
		c_logger.info(method+"->going to call rbt client to make selection");
		rbt=subRbtClient.addSubscriberSelection(selectionRequest);
		String response=selectionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+selBean.getSubscriberId()+",response=="+response);
		return rbt;
	}
	public Rbt makeSelection(SelectionRequestBean selBean,String requestMode,String selInfo){
		String method="makeSelection";
		Rbt rbt=null;
		SelectionRequest selectionRequest=new SelectionRequest(null);
		selectionRequest.setSubscriberID(selBean.getSubscriberId());
		selectionRequest.setUseUIChargeClass(selBean.getUseUIChargeClass());
		selectionRequest.setMode(requestMode);
		selectionRequest.setModeInfo(selInfo);
		selectionRequest.setCallerID(selBean.getCallerId());
		if(selBean.getToneId()!=null && selBean.getToneId()!="-1"){
			selectionRequest.setClipID(selBean.getToneId());
		}
		if(selBean.getCatId()!=null){
			selectionRequest.setCategoryID(selBean.getCatId());
		}
		if(selBean.getChargeClass()!=null && !selBean.getChargeClass().equalsIgnoreCase("") && !selBean.getChargeClass().equalsIgnoreCase("null")){
			selectionRequest.setChargeClass(selBean.getChargeClass());
		}
		selectionRequest.setSubscriberID(selBean.getSubscriberId());
		selectionRequest.setIsPrepaid(selBean.isPrepaid());
		String rbtOption=selBean.getSetRBTOption();
		if(rbtOption!=null && rbtOption.equalsIgnoreCase("TimeOfTheDay")){
			selectionRequest.setStatus(selBean.getTimeOfTheDayStatus());
			if(selBean.getFromTimeOfTheDay()!=0 && selBean.getToTimeOfTheDay()!=23){
				selectionRequest.setFromTime(selBean.getFromTimeOfTheDay());
				selectionRequest.setToTime(selBean.getToTimeOfTheDay());
			}
		}else if(rbtOption!=null && rbtOption.equalsIgnoreCase("FutureDate")){
			selectionRequest.setStatus(selBean.getFutureDateStatus());
			c_logger.info(method+"->getSelInterval=="+selBean.getSelInterval());
			if(selBean.getSelInterval()!=null){
				selectionRequest.setInterval(selBean.getSelInterval());
			}
		}else if(rbtOption!=null && rbtOption.equalsIgnoreCase("DayOfTheWeek")){
			selectionRequest.setStatus(selBean.getDayOfTheWeekStatus());
			if(selBean.getSelInterval()!=null){
				selectionRequest.setInterval(selBean.getSelInterval());
			}
		}else{
			selectionRequest.setStatus(selBean.getStatus());
		}

		if(selBean!=null && selBean.getSetInLoop()!=null && selBean.getSetInLoop().equalsIgnoreCase("true")){
			selectionRequest.setInLoop(true);
		}
		if(selBean!=null && selBean.getProfileHour()!=null){
			selectionRequest.setProfileHours(selBean.getProfileHour());
		}
		if(selBean!=null && selBean.getCricpack()!=null){
			selectionRequest.setCricketPack(selBean.getCricpack());
		}
		if(selBean!=null && selBean.getSubscriptionClass()!=null){
			selectionRequest.setSubscriptionClass(selBean.getSubscriptionClass());
		}
		if(selBean!=null && selBean.getExtraInfo()!=null ){
			Map<String, String> selectionInfoMapLocal = selectionRequest.getSelectionInfoMap();
			if(selectionInfoMapLocal == null) {
				selectionRequest.setSelectionInfoMap(selBean.getExtraInfo());
			}
			else {
				selectionRequest.getSelectionInfoMap().putAll(selBean.getExtraInfo());
			}
		}
		c_logger.info(method+"->going to call rbt client to make selection");
		rbt=subRbtClient.addSubscriberSelection(selectionRequest);
		String response=selectionRequest.getResponse();
		c_logger.info(method+"->"+"subId=="+selBean.getSubscriberId()+",response=="+response);
		return rbt;
	}
	public Library deleteSelections(SelectionRequestBean selBean,String requestMode,String selInfo,StringBuffer strBuff){
		String method="deleteSettings";
		SelectionRequest selectionRequest=new SelectionRequest(null);
		selectionRequest.setMode(requestMode);
		selectionRequest.setCallerID(selBean.getCallerId());
		selectionRequest.setModeInfo(selInfo);
		if(!selBean.getToneId().equalsIgnoreCase("-1")){
			selectionRequest.setClipID(selBean.getToneId());
		}
		selectionRequest.setFromTime(selBean.getFromTimeOfTheDay());
		selectionRequest.setToTime(selBean.getToTimeOfTheDay());
		selectionRequest.setInterval(selBean.getSelInterval());
		selectionRequest.setStatus(selBean.getStatus());
		selectionRequest.setSubscriberID(selBean.getSubscriberId());
		selectionRequest.setIsPrepaid(selBean.isPrepaid());
		Library libr=subRbtClient.deleteSubscriberSelection(selectionRequest);
		String response=selectionRequest.getResponse();
		strBuff.append(response);
		c_logger.info(method+"->"+"subId=="+selBean.getSubscriberId()+",response=="+response);
		return libr;
	}
	public String makeCopySelection(String subID, String fromSubID, int catID, int clipID, int status, String callerID,String requestMode){
		CopyRequest copyRequest = new CopyRequest(subID,fromSubID,catID,clipID, status,callerID);
		copyRequest.setMode(requestMode);
		subRbtClient.copy(copyRequest);
		return copyRequest.getResponse();
	}
//	public void addTransData(){
//		DataRequest dataRequest=new DataRequest(null);
//		subRbtClient.addTransData(dataRequest);
//	}
//	public void getTransData(){
//		DataRequest dataRequest=new DataRequest(null);
//		subRbtClient.getTransData(dataRequest);
//	}
//	public void getTransDatas(){
//		DataRequest dataRequest=new DataRequest(null);
//		subRbtClient.getTransDatas(dataRequest);
//	}
//	public void removeTransData(){
//		DataRequest dataRequest=new DataRequest(null);
//		subRbtClient.removeTransData(dataRequest);
//	}
//	public void getViralData(){
//		DataRequest dataRequest=new DataRequest(null);
//		
//		subRbtClient.getViralData(dataRequest);
//	}
//	public void processViralData(){
//		DataRequest dataRequest=new DataRequest(null);
//		subRbtClient.processViralData(dataRequest);
//	}

	public ViralData[] getViralData(String subId,String callerId,String type,String mode, String clipID){
		DataRequest dataRequest=new DataRequest(null);
		boolean responseStatus=false;
		if (subId!=null) {
			dataRequest.setSubscriberID(subId);
		}
		if (callerId!=null) {
			dataRequest.setCallerID(callerId);
		}
		if (type!=null) {
			dataRequest.setType(type);
		}
		if(mode != null)
			dataRequest.setMode(mode);
		if(clipID != null)
			dataRequest.setClipID(clipID);
		
		ViralData[] v = subRbtClient.getViralData(dataRequest);
		String responseTemp=dataRequest.getResponse();
		if(responseTemp!=null ){
			responseStatus=true;
			responseTemp=responseTemp.trim();
			responseTemp=responseTemp.toUpperCase();
			if(responseTemp.indexOf("SUCCESS")!=-1){
				responseStatus=true;
			}
		}
		if(responseStatus == true)
			return v;
		else
			return null;
	}

	public boolean removeViralData(String subId,String callerId,String type,Date sentTime){
		DataRequest dataRequest=new DataRequest(null);
		boolean responseStatus=false;
		if (subId!=null) {
			dataRequest.setSubscriberID(subId);
		}
		if (callerId!=null) {
			dataRequest.setCallerID(callerId);
		}
		if (type!=null) {
			dataRequest.setType(type);
		}
		if(sentTime!=null){
			dataRequest.setSentTime(sentTime);
		}
		subRbtClient.removeViralData(dataRequest);
		String responseTemp=dataRequest.getResponse();
		if(responseTemp!=null ){
			responseStatus=true;
			responseTemp=responseTemp.trim();
			responseTemp=responseTemp.toUpperCase();
			if(responseTemp.indexOf("SUCCESS")!=-1){
				responseStatus=true;
			}
		}
		return responseStatus;
	}
	
	public boolean addViralData(String subscriberID, String callerID, String type,
			String clipID, String mode, HashMap<String, String> infoMap){
		DataRequest dataRequest=new DataRequest(null);
		boolean responseStatus=false;
		if (subscriberID!=null) {
			dataRequest.setSubscriberID(subscriberID);
		}
		if (callerID!=null) {
			dataRequest.setCallerID(callerID);
		}
		if (type!=null) {
			dataRequest.setType(type);
		}
		if(mode!=null){
			dataRequest.setMode(mode);
		}
		if(clipID!=null){
			dataRequest.setClipID(clipID);
		}
		if(infoMap != null)
			dataRequest.setInfoMap(infoMap);
		subRbtClient.addViralData(dataRequest);
		String responseTemp=dataRequest.getResponse();
		if(responseTemp!=null ){
			responseStatus=true;
			responseTemp=responseTemp.trim();
			responseTemp=responseTemp.toUpperCase();
			if(responseTemp.indexOf("SUCCESS")!=-1){
				responseStatus=true;
			}
		}
		return responseStatus;
	}

	public boolean updateViralData(String subscriberID, String callerID, String newCallerID, Date sentTime, String oldType, String newType, String clipId,String selectedBy, String extraInfo){
		DataRequest dataRequest=new DataRequest(null);
		boolean responseStatus=false;
		if (subscriberID!=null) {
			dataRequest.setSubscriberID(subscriberID);
		}
		if (callerID!=null) {
			dataRequest.setCallerID(callerID);
		}
		if (newCallerID!=null) {
			dataRequest.setNewCallerID(newCallerID);
		}
		if (oldType!=null) {
			dataRequest.setType(oldType);
		}
		if (newType!=null) {
			dataRequest.setNewType(newType);
		}
		if(sentTime!=null){
			dataRequest.setSentTime(sentTime);
		}
		if(extraInfo!=null){
			dataRequest.setInfo(extraInfo);
		}
		if(selectedBy!=null){
			dataRequest.setMode(selectedBy);
		}
		if(clipId!=null){
			dataRequest.setClipID(clipId);
		}
		subRbtClient.updateViralData(dataRequest);
		String responseTemp=dataRequest.getResponse();
		if(responseTemp!=null ){
			responseStatus=true;
			responseTemp=responseTemp.trim();
			responseTemp=responseTemp.toUpperCase();
			if(responseTemp.indexOf("SUCCESS")!=-1){
				responseStatus=true;
			}
		}
		return responseStatus;
	}
	
	private void setSelOffer(SelectionRequest selRequest, boolean isCallerSubscribed, String rbtTypeStr, String cosIdStr)
	{
		c_logger.info("inside getSelOffer");
		String offerId = null;
		String subscriberID = selRequest.getSubscriberID();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		String mode = selRequest.getMode();
		boolean isPrepaid = true;
		if(selRequest.getIsPrepaid() != null)
			isPrepaid = selRequest.getIsPrepaid();
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setIsPrepaid(isPrepaid);
		// Parameters to be set in hashmap are CONTENT_TYPE, RBT_TYPE, SUBSCRIPTION_CLASS
		//CONTENT_TYPE, CLIP_CHARGE_CLASS, CATEGORY_CHARGE_CLASS, UI_CHARGE_CLASS, RBT_TYPE, LITE_USER
		
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		String clipId = selRequest.getClipID();
		String contentType = null;
		String clipChargeClass = null;
		if(clipId != null)
		{
			Clip clip = RBTConnector.getInstance().getMemCache().getClip(clipId);
			if(clip != null)
			{
				contentType = clip.getContentType();
				clipChargeClass = clip.getClassType();
				extraInfoMap.put(Offer.CLIP_ID, clipId);
			}
		}
		String rbtType = null;
		if(rbtTypeStr != null && rbtTypeStr.equalsIgnoreCase(WebServiceConstants.AD_RBT))
			rbtType = WebServiceConstants.AD_RBT;
		String subscriptionClass = selRequest.getSubscriptionClass();
		extraInfoMap.put(Offer.SUBSCRIPTION_CLASS, subscriptionClass);
		extraInfoMap.put(Offer.RBT_TYPE, rbtType);
		extraInfoMap.put(Offer.CONTENT_TYPE, contentType);
		extraInfoMap.put(Offer.CLIP_CHARGE_CLASS, clipChargeClass);
		//ExtraInfo for Sel
		String lite_user = "FALSE";
		if(cosIdStr != null)
		{
			List<CosDetails> cosDetailList = CacheManagerUtil.getCosDetailsCacheManager().getCosDetails(cosIdStr);
			if(cosDetailList != null && cosDetailList.size() > 0)
			{
				CosDetails cosDetail = cosDetailList.get(0);
				if(cosDetail != null && cosDetail.getCosType() != null)
				{
					if(cosDetail.getCosType().equalsIgnoreCase(WebServiceConstants.COS_TYPE_LITE));
						lite_user  = "TRUE";
				}	
			}	
		}
		extraInfoMap.put(Offer.LITE_USER, lite_user);
		
		String catId = selRequest.getCategoryID();
		String categoryChargeClass = null;
		int catIdInt = -1;
		if(catId != null)
		{
			try
			{
				catIdInt = Integer.parseInt(catId);
			}
			catch(Exception e)
			{
				catIdInt = -1;
			}
			Category category = RBTConnector.getInstance().getMemCache().getCategory(catIdInt);
			if(category != null && category.getClassType() != null)
				categoryChargeClass = category.getClassType();
		}
		extraInfoMap.put(Offer.CATEGORY_CHARGE_CLASS, categoryChargeClass);
		extraInfoMap.put(Offer.UI_CHARGE_CLASS, selRequest.getChargeClass());		
		
		rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
		Offer[] offers = null;
		
		boolean isSupportPackageOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ENABLE_PACKAGE_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		if(!isCallerSubscribed && selRequest.getSubscriptionOfferID() == null)
		{
			c_logger.info("in getSelOffer with inactive user");
			int offerTypeBase = Offer.OFFER_TYPE_SUBSCRIPTION;
			rbtDetailsRequest.setOfferType(offerTypeBase+"");
			c_logger.info("in getSelOffer with inactive user. rbtDetailsRequest : "+rbtDetailsRequest);
			if(isSupportPackageOffer){
				offers = subRbtClient.getPackageOffer(rbtDetailsRequest);
			}
			else {
				offers = subRbtClient.getOffers(rbtDetailsRequest);
			}
			String baseOfferId = null;
			if(offers != null && offers.length > 0 && offers[0].getOfferID() != null)
				baseOfferId = offers[0].getOfferID();
			c_logger.info("in getSelOffer with inactive user. baseOfferId = "+baseOfferId);
			if(baseOfferId != null)
				selRequest.setSubscriptionOfferID(baseOfferId);
			if(offers != null && offers.length > 0 && offers[0].getSrvKey() != null)
				selRequest.setSubscriptionClass(offers[0].getSrvKey());
		}
		
		if(RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ALLOW_GET_OFFER", "FALSE").equalsIgnoreCase("TRUE") && selRequest.getOfferID() == null)
		{	
			int offerType = Offer.OFFER_TYPE_SELECTION;
			rbtDetailsRequest.setOfferType(offerType+"");
			rbtDetailsRequest.setClipID(clipId);
			c_logger.info("in getSelOffer . for selection rbtDetailsRequest : "+rbtDetailsRequest);
			if(isSupportPackageOffer){
				offers = subRbtClient.getPackageOffer(rbtDetailsRequest);
			}else {
				offers = subRbtClient.getOffers(rbtDetailsRequest);
			}
			if(offers != null && offers.length > 0 && offers[0].getOfferID() != null)
			{
				offerId = offers[0].getOfferID();
				selRequest.setOfferID(offerId);
				if(offers[0].getSrvKey() != null)
					selRequest.setChargeClass(offers[0].getSrvKey());
				//Added by Sreekar for Vf-Spain on 2013-01-26
				if(offers[0].getSmOfferType() != null) {
					HashMap<String, String> userInfoMap = selRequest.getUserInfoMap();
					if(userInfoMap == null)
						userInfoMap = new HashMap<String, String>();
					userInfoMap.put(iRBTConstant.EXTRA_INFO_OFFER_TYPE, offers[0].getSmOfferType());
					selRequest.setSelectionInfoMap(userInfoMap);
				}
			}
		}
		c_logger.info("exiting getSelOffer with offerId = "+offerId);
	}
	
	
	private void setBaseOffer(SubscriptionRequest subRequest)
	{
		c_logger.info("inside getBaseOffer");
		if(subRequest.getOfferID() != null) {
			return;
		}
		String offerId = null;
		String subscriberID = subRequest.getSubscriberID();
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		String mode = subRequest.getMode();
		boolean isPrepaid = subRequest.getIsPrepaid();
		int offerType = Offer.OFFER_TYPE_SUBSCRIPTION;
		rbtDetailsRequest.setMode(mode);
		rbtDetailsRequest.setIsPrepaid(isPrepaid);
		rbtDetailsRequest.setOfferType(offerType+"");
		// Parameters to be set in hashmap are CONTENT_TYPE, RBT_TYPE, SUBSCRIPTION_CLASS
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		
		String subscriptionClass = subRequest.getSubscriptionClass();
		extraInfoMap.put(Offer.SUBSCRIPTION_CLASS, subscriptionClass);
		rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
		c_logger.info(" in getBaseOffer with rbtDetailsRequest = "+rbtDetailsRequest);
		boolean isSupportPackageOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter("COMMON", "ENABLE_PACKAGE_OFFER", "FALSE").equalsIgnoreCase("TRUE");
		Offer[] offers = null;
		if(isSupportPackageOffer) {
			offers = subRbtClient.getPackageOffer(rbtDetailsRequest);
		}
		else {
			offers = subRbtClient.getOffers(rbtDetailsRequest);
		}
		if(offers != null && offers.length > 0 && offers[0].getOfferID() != null)
		{
			offerId = offers[0].getOfferID();
			subRequest.setOfferID(offerId);
			if(offers[0].getSrvKey() != null)
				subRequest.setSubscriptionClass(offers[0].getSrvKey());
		}
		c_logger.info("exiting getBaseOffer with offerId = "+offerId);
	}
	
	public void updateSubscription(SubscriptionBean subBean)
	{
		c_logger.info("Entering updateSubscription with subBean = "+subBean);
		String response  = null;
		SubscriptionRequest subscriptionRequest=new SubscriptionRequest(null);
		subscriptionRequest.setSubscriberID(subBean.getSubId());
		if(subBean!=null && subBean.getExtraInfo()!=null){
			subscriptionRequest.setUserInfoMap(subBean.getExtraInfo());
		}
				
		Subscriber sub=subRbtClient.updateSubscription(subscriptionRequest);
		response=subscriptionRequest.getResponse();
		c_logger.info("exiting updateSubscription with response = "+response);
	}
	public void updateViralData(long smsId, String circleId)
	{
		DataRequest dataRequest=new DataRequest(null);
		dataRequest.setSmsID(smsId);
		dataRequest.setOnlyResponse(true);
		dataRequest.setCircleID(circleId);
		subRbtClient.updateViralData(dataRequest);
	}
	
	public void updateViralSmsType(long smsId, String smsType)
    {
        DataRequest dataRequest=new DataRequest(null);
        dataRequest.setSmsID(smsId);
        dataRequest.setType(smsType);
        dataRequest.setOnlyResponse(true);
        subRbtClient.updateViralData(dataRequest);
    }
}
