package com.onmobile.apps.ringbacktones.wrappers;

import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTPrimitive;
import com.onmobile.apps.ringbacktones.srbt.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialCopyDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialDownloadsDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialGiftDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSelectionsDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSiteUserDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSubscriberDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialCopy;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialDownloads;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialGift;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSelections;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSiteUser;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSubscriber;

public class SRBTDaoWrapper implements iRBTConstant{
	static final org.apache.log4j.Logger c_logger = org.apache.log4j.Logger.getLogger(SRBTDaoWrapper.class);
	private static Boolean isMySqlDB = null;
	private static long operatorSqnId = -1;
	private static final String classname = "SRBTDaoWrapper";

	public static SRBTDaoWrapper getInstance(){
		SRBTDaoWrapper srbtDaoWrapper=new SRBTDaoWrapper();
		srbtDaoWrapper.init();
		if(isMySqlDB == null){
			String dbType = ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
			if(dbType != null && dbType.equalsIgnoreCase(RBTPrimitive.DB_MYSQL)){
				isMySqlDB = true;
			}else{
				isMySqlDB = false;
			}
		}
		return srbtDaoWrapper;
	}

	private void init(){
		String method="SRBTDaoWrapper init()";
		if(operatorSqnId ==-1){
			String oprSqnId = RBTConnector.getInstance().getRbtGenericCache().getParameter(SRBT, "OPERATOR_SQN_ID");
			if(oprSqnId!=null && oprSqnId.length()>0){
				try {
					operatorSqnId = (new Long(oprSqnId)).longValue();
				} catch (NumberFormatException e1) {
					operatorSqnId = -1;
					e1.printStackTrace();
				}
			}
		}
		c_logger.info(method + "->"+"operatorSqnId ="+operatorSqnId);
	}
	public static boolean activateSNGUser(String userID, String subscriberID, String sngId,String mode){
		
		long sngSqnId = -1;
		try {
			sngSqnId = Long.parseLong(sngId);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		long msisdn = -1;
		try {
			msisdn = Long.parseLong(subscriberID);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return false;
		}
		
		RbtSocialSiteUser socialUser = new RbtSocialSiteUser();
		socialUser.setMsisdn(msisdn);
		socialUser.setSngId(sngSqnId);
		socialUser.setUserId(userID);
		socialUser.setInfo(mode);
		return RbtSocialSiteUserDAO.insert(socialUser);
	}
	public static boolean deactivateSNGUser(String userID, String subscriberID, String sngId,String mode){
		
		
		boolean response =  false;
		try {
			response = RbtSocialSiteUserDAO.deactivateSocialSiteUser(Long.parseLong(sngId), userID, Long.parseLong(subscriberID));
		} catch (NumberFormatException e) {
			response =  false;
			e.printStackTrace();
		} catch (DataAccessException e) {
			response =  false;
			e.printStackTrace();
		}
		return response;
	}
	public static boolean deleteSocialSiteUser(String subscriberID){
		
		
		boolean response =  false;
		try {
			response = RbtSocialSiteUserDAO.deleteSocialSiteUser(Long.parseLong(subscriberID));
		} catch (NumberFormatException e) {
			response =  false;
			e.printStackTrace();
		} catch (DataAccessException e) {
			response =  false;
			e.printStackTrace();
		}
		return response;
	}
	public static boolean updateSocialSiteUser(String subscriberID, String newSubscriberID){
		
		boolean response =  false;
		try {
			List<RbtSocialSiteUser> userList = RbtSocialSiteUserDAO.updateSocilaSiteUser(Long.parseLong(subscriberID), Long.parseLong(newSubscriberID));
			response = userList != null && userList.size() > 0;
		} catch (NumberFormatException e) {
			response =  false;
			e.printStackTrace();
		} catch (DataAccessException e) {
			response =  false;
			e.printStackTrace();
		}
		return response;
	}
	public static boolean deactivateAllSNGUser(String userID, String sngId,String mode){
		
		boolean response =  false;
		try {
			response = RbtSocialSiteUserDAO.deactivateSocialSiteUser(Long.parseLong(sngId), userID);
		} catch (NumberFormatException e) {
			response =  false;
			e.printStackTrace();
		} catch (DataAccessException e) {
			response =  false;
			e.printStackTrace();
		}
		return response;
	}
	public static List<RbtSocialSiteUser> getActiveSNGSiteUser(String subId)
	{
		boolean returnFlag = false;
		String sql = "select * from rbt_social_site_user where msisdn='"+subId+"'" ;
		List<RbtSocialSiteUser> userList = null;
		try{
			userList = RbtSocialSiteUserDAO.load(RbtSocialSiteUser.class, sql);
		}
		catch(Exception e) {
			c_logger.error("Exception:",e);
		}
		return userList;
	}
	public String getCopyUpdateQueryString(int status,int fetchSize, long presentSequenceId){
		return RbtSocialCopyDAO.getUpdateQueryString(""+status, fetchSize, presentSequenceId, isMySqlDB);
	}
	public String getGiftUpdateQueryString(String status,int fetchSize, long presentSequenceId){

		return RbtSocialGiftDAO.getUpdateQueryString(status, fetchSize, presentSequenceId, isMySqlDB);
	}
	public String getSubscriberUpdateQueryString(int fetchSize, long presentSequenceId){

		return RbtSocialSubscriberDAO.getUpdateQueryString(fetchSize, presentSequenceId, isMySqlDB);
	}
	public String getSelectionUpdateQueryString(int fetchSize, long presentSequenceId){

		return RbtSocialSelectionsDAO.getUpdateQueryString(fetchSize, presentSequenceId, isMySqlDB);
	}
	public String getDownloadUpdateQueryString(int fetchSize, long presentSequenceId){
		return RbtSocialDownloadsDAO.getUpdateQueryString(fetchSize, presentSequenceId, isMySqlDB);
	}
	public boolean updateForSubscriptionActivationSuccess(String subId, Date startDate, String subscriptionClass, String info, String mode,short evtType){
		List<RbtSocialSiteUser> userList = getActiveSNGSiteUser(subId);
			if( userList == null || userList.size() == 0){
			return false;
		}
		RbtSocialSubscriber rbtSocialSub = new RbtSocialSubscriber();
		rbtSocialSub.setMsisdn((new Long(subId)).longValue());
		rbtSocialSub.setInfo(info);
		rbtSocialSub.setStartDate(startDate);
		rbtSocialSub.setSubscriptionClassType(subscriptionClass);
		rbtSocialSub.setCircleId(SRBTUtility.getCircleId(subId));
		rbtSocialSub.setOperatorSqnId(operatorSqnId);
		rbtSocialSub.setEvtType(evtType);
		rbtSocialSub.setMode(mode);
		return RbtSocialSubscriberDAO.insert(rbtSocialSub);
	}
	public boolean updateForSubscriptionDeactivationSuccess(String subId, Date startDate, String subscriptionClass, String info){
		/*
		if(!(isActiveSNGSiteUser(subId))){
			return false;
		}
		RbtSocialSubscriber rbtSocialSub = new RbtSocialSubscriber();
		rbtSocialSub.setInfo(info);
		rbtSocialSub.setStartDate(startDate);
		rbtSocialSub.setCircleId(circleId);
		rbtSocialSub.setOperatorSqnId(operatorSqnId);
		return RbtSocialSubscriberDAO.insert(rbtSocialSub);
		*/
		return false;
	}
	public boolean updateForSelectionActivationSuccess(String subId,String callerId,int clipId, int catId, Date startDate, String chargeClass, String info,boolean displayCallerId, String mode, String toneType){
		List<RbtSocialSiteUser> userList = getActiveSNGSiteUser(subId);
		if( userList == null || userList.size() == 0){
		return false;
		}
		c_logger.info("SubId " + subId + " CallerId " + callerId);
//		String circleCode = null;
		RbtSocialSelections rbtSocialSeletcion = new RbtSocialSelections();
		rbtSocialSeletcion.setMsisdn((new Long(subId)).longValue());
		rbtSocialSeletcion.setStartDate(startDate);
		rbtSocialSeletcion.setInfo(info);
		rbtSocialSeletcion.setClipId(clipId);
		rbtSocialSeletcion.setCatId(catId);
		rbtSocialSeletcion.setChargeClassType(chargeClass);
		rbtSocialSeletcion.setCircleId(SRBTUtility.getCircleId(subId));
		rbtSocialSeletcion.setOperatorSqnId(operatorSqnId);
		rbtSocialSeletcion.setMode(mode);
		if(callerId == null || callerId.equalsIgnoreCase("") || callerId.equalsIgnoreCase("null") || callerId.equalsIgnoreCase("ALL")){
			callerId = "0";
		}
		if(!displayCallerId){
			callerId = "-1";
		}
		rbtSocialSeletcion.setCallerMsisdn((new Long(callerId)).longValue());
		rbtSocialSeletcion.setToneType(toneType);
		return RbtSocialSelectionsDAO.insert(rbtSocialSeletcion);
	}
	public boolean updateForSelectionDeactivationSuccess(String subId,String callerId,int clipId, int catId, Date startDate, String chargeClass, String info,boolean displayCallerId, String toneType){
		return false;
	}
	public boolean updateForDownloadActivationSuccess(String subId,String callerId,int clipId, int catId, Date startDate, String chargeClass, String info, String mode, String toneType){
		List<RbtSocialSiteUser> userList = getActiveSNGSiteUser(subId);
		if( userList == null || userList.size() == 0){
		return false;
		}
//		String circleCode = null;
		RbtSocialDownloads rbtSocialDownload = new RbtSocialDownloads();
		rbtSocialDownload.setMsisdn((new Long(subId)).longValue());
		rbtSocialDownload.setStartDate(startDate);
		rbtSocialDownload.setInfo(info);
		rbtSocialDownload.setClipId(clipId);
		rbtSocialDownload.setCatId(catId);
		rbtSocialDownload.setChargeClassType(chargeClass);
		rbtSocialDownload.setCircleId(SRBTUtility.getCircleId(subId));
		rbtSocialDownload.setOperatorSqnId(operatorSqnId);
		rbtSocialDownload.setMode(mode);
		rbtSocialDownload.setToneType(toneType);
		return RbtSocialDownloadsDAO.insert(rbtSocialDownload);
	}
	public boolean updateForDownloadDeactivationSuccess(String subId,String callerId,int clipId, int catId, Date endDate, String chargeClass, String info, String toneType){
		return false;
	}
	public boolean updateForGiftSelectionSuccess(String gifteeId,String gifterId,int clipId, int catId,Date setDate, Date startDate, String chargeClass, String info,boolean displayGifterId, String mode, String toneType){
		List<RbtSocialSiteUser> userList = getActiveSNGSiteUser(gifteeId);
		if( userList == null || userList.size() == 0){
		return false;
		}
		RbtSocialGift rbtSocialGift = new RbtSocialGift();
		rbtSocialGift.setCatId(catId);
		rbtSocialGift.setClipId(clipId);
		if(!(gifteeId == null || gifteeId.equalsIgnoreCase("") || gifteeId.equalsIgnoreCase("null") || gifteeId.equalsIgnoreCase("ALL"))){
			rbtSocialGift.setGifteeMsisdn((new Long(gifteeId)).longValue());
		}
		
		if(!displayGifterId){
			gifterId = "-1";
		}
		rbtSocialGift.setGifterMsisdn((new Long(gifterId)).longValue());
		
		rbtSocialGift.setStartDate(startDate);
		rbtSocialGift.setSetDate(setDate);
		rbtSocialGift.setInfo(info);
		Short status = new Short("2");
		rbtSocialGift.setStatus(status);
		rbtSocialGift.setOperatorSqnId(operatorSqnId);
		rbtSocialGift.setMode(mode);
		rbtSocialGift.setCircleId(SRBTUtility.getCircleId(gifteeId));
		rbtSocialGift.setToneType(toneType);
		return RbtSocialGiftDAO.insert(rbtSocialGift);
	}
	public boolean updateForGiftSuccess(String gifteeId,String gifterId,int clipId, int catId,Date setDate, Date startDate, String chargeClass, String info,boolean displayGifteeId, String mode, String toneType){
		List<RbtSocialSiteUser> userList = getActiveSNGSiteUser(gifterId);
		if( userList == null || userList.size() == 0){
			return false;
		}
		RbtSocialGift rbtSocialGift = new RbtSocialGift();
		rbtSocialGift.setCatId(catId);
		rbtSocialGift.setClipId(clipId);
		if(!(gifteeId == null || gifteeId.equalsIgnoreCase("") || gifteeId.equalsIgnoreCase("null") || gifteeId.equalsIgnoreCase("ALL"))){
			if(!displayGifteeId){
				gifteeId = "-1";
			}
		}
		rbtSocialGift.setGifteeMsisdn((new Long(gifteeId)).longValue());
		rbtSocialGift.setGifterMsisdn((new Long(gifterId)).longValue());
		rbtSocialGift.setStartDate(startDate);
		rbtSocialGift.setSetDate(setDate);
		rbtSocialGift.setInfo(info);
		Short status = new Short("1");
		rbtSocialGift.setStatus(status);
		rbtSocialGift.setOperatorSqnId(operatorSqnId);
		rbtSocialGift.setMode(mode);
		rbtSocialGift.setCircleId(SRBTUtility.getCircleId(gifterId));
		rbtSocialGift.setToneType(toneType);
		return RbtSocialGiftDAO.insert(rbtSocialGift);
	}
	public boolean updateForCopySelectionSuccess(String subId,
			String copySubId, int clipId, int catId, Date setDate,
			Date startDate, String chargeClass, String info,
			boolean displayCopySubId, String mode, String srcUserID, String toneType) 
	{
		List<RbtSocialSiteUser> userList = getActiveSNGSiteUser(subId);
		if( userList == null || userList.size() == 0){
		return false;
		}
		RbtSocialCopy rbtSocialCopy = new RbtSocialCopy();
		rbtSocialCopy.setCatId(catId);
		rbtSocialCopy.setClipId(clipId);
		if(!(copySubId == null || copySubId.equalsIgnoreCase("") || copySubId.equalsIgnoreCase("null") || copySubId.equalsIgnoreCase("ALL"))){
			if(!displayCopySubId){
				copySubId = "-1";
			}
			rbtSocialCopy.setCopyForMsisdn((new Long(copySubId)).longValue());
		}
		rbtSocialCopy.setMsisdn((new Long(subId)).longValue());
		rbtSocialCopy.setStartDate(startDate);
		rbtSocialCopy.setSetDate(setDate);
		rbtSocialCopy.setInfo(info);
		Short status = new Short("1");
		rbtSocialCopy.setStatus(status);
		rbtSocialCopy.setOperatorSqnId(operatorSqnId);
		rbtSocialCopy.setCircleId(SRBTUtility.getCircleId(subId));
		rbtSocialCopy.setMode(mode);
		rbtSocialCopy.setSourceUserId(srcUserID);
		rbtSocialCopy.setToneType(toneType);
		return RbtSocialCopyDAO.insert(rbtSocialCopy);
	}
	
	public boolean removeSRBTSubscriberUpdate(RbtSocialSubscriber rbtSocialSubscriber){
		return RbtSocialSubscriberDAO.delete(rbtSocialSubscriber);
	}
	public boolean updateSRBTSubscriberUpdate(RbtSocialSubscriber rbtSocialSubscriber){
		return RbtSocialSubscriberDAO.update(rbtSocialSubscriber);
	}
	public boolean removeSRBTSelectionsUpdate(RbtSocialSelections rbtSocialSelections){
		return  RbtSocialSelectionsDAO.delete(rbtSocialSelections);
	}
	public boolean updateSRBTSelectionsUpdate(RbtSocialSelections rbtSocialSelections){
		return RbtSocialSelectionsDAO.update(rbtSocialSelections);
	}
	public boolean removeSRBTDownloadsUpdate(RbtSocialDownloads rbtSocialDownloads){
		return RbtSocialDownloadsDAO.delete(rbtSocialDownloads);
	}
	public boolean updateSRBTDownloadsUpdate(RbtSocialDownloads rbtSocialDownloads){
		return RbtSocialDownloadsDAO.update(rbtSocialDownloads);
	}
	public boolean removeSRBTCopyUpdate(RbtSocialCopy rbtSocialCopy){
		return  RbtSocialCopyDAO.delete(rbtSocialCopy);
	}
	public boolean updateSRBTCopyUpdate(RbtSocialCopy rbtSocialCopy){
		 return RbtSocialCopyDAO.update(rbtSocialCopy);
	}
	public boolean removeSRBTGiftUpdate(RbtSocialGift rbtSocialGift){
		return RbtSocialGiftDAO.delete(rbtSocialGift);
	}
	public boolean updateSRBTGiftUpdate(RbtSocialGift rbtSocialGift){
		return RbtSocialGiftDAO.update(rbtSocialGift);
	}









public void moveRecords(long msisdn) {
		
	}





























}

