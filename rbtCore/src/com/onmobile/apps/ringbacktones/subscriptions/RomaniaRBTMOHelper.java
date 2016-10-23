package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class RomaniaRBTMOHelper extends RBTMOHelper implements iRBTConstant{
	
	private String _class = "RomaniaRBTMOHelper";
	private String SUBSCRIBER_ID = "SUBSCRIBER_ID";
	private String SUBSCRIBER_OBJ = "SUBSCRIBER_OBJ";
	private String CALLER_ID = "CALLER_ID";
	private String CLIP_OBJECT = "CLIP_OBJECT";
    private String CATEGORY_OBJECT = "CATEGORY_OBJECT";
	
	RomaniaRBTMOHelper() throws Exception{
		
		super();
	}
	
		public void processNewsletter(HashMap z, ArrayList smsList,
			String requirement) {

		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		if (!isSubActive(subscriber, z)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_SUCCESS);
			return;
		}

		Subscriber sub = m_rbtDBManager.getSubscriber(subscriberID);
		HashMap hm = m_rbtDBManager.getExtraInfoMap(sub);
		String result = null;

		if (hm != null)
			result = (String) hm.get(IS_NEWSLETTER_ON);
		if (result == null)
			result = NEWSLETTER_OFF;

		if (requirement.equalsIgnoreCase("OFF")) {

			if (result.equalsIgnoreCase(NEWSLETTER_OFF)) {

				setReturnValues(z, getSMSTextForID(z, "NEWSLETTER_ALREADY_OFF",
						m_newsLetterAlreadyOffDefault), STATUS_SUCCESS);
				return;
			}

			boolean success = RBTDBManager.getInstance()
					.updateExtraInfo(subscriberID.trim(),
							iRBTConstant.IS_NEWSLETTER_ON, NEWSLETTER_OFF);

			if (success)
				setReturnValues(z, getSMSTextForID(z, "NEWSLETTER_OFF_SUCCESS",
						m_newsLetterTurnedOffSuccessDefault), STATUS_SUCCESS);
			else
				setReturnValues(z, getSMSTextForID(z, "NEWSLETTER_ON_FAILURE",
						m_newsLetterRequestFailureDefault),
						STATUS_NOT_AUTHORIZED);
			return;
		}
		if (requirement.equalsIgnoreCase("ON")) {

			if (result.equalsIgnoreCase(NEWSLETTER_ON)) {

				setReturnValues(z, getSMSTextForID(z, "NEWSLETTER_ALREADY_ON",
						m_newsLetterAlreadyOnDefault), STATUS_SUCCESS);
				return;
			}

			boolean success = RBTDBManager.getInstance()
					.updateExtraInfo(subscriberID.trim(),
							iRBTConstant.IS_NEWSLETTER_ON, NEWSLETTER_ON);

			if (success)
				setReturnValues(z, getSMSTextForID(z, "NEWSLETTER_ON_SUCCESS",
						m_newsLetterTurnedOnSuccessDefault), STATUS_SUCCESS);
			else
				setReturnValues(z, getSMSTextForID(z, "NEWSLETTER_ON_FAILURE",
						m_newsLetterRequestFailureDefault),
						STATUS_NOT_AUTHORIZED);
			return;
		}
	}

	public void processCOPY(HashMap z, ArrayList smsList) {

		String _method = "processCOPY";
		String subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		String callerID = (String) getFromZTable(z, CALLER_ID);
		String setForCaller = null;
		if (smsList.size() > 0)
			setForCaller = (String) smsList.get(0);

		if ((callerID == null || (callerID).equalsIgnoreCase(subscriberID))
				|| !isSubscriberActivated(getSubscriber(callerID), z)) {
			if (callerID == null)
				setReturnValues(z, getSMSTextForID(z,
						"TEMPORARY_OVERRIDE_FAILURE",
						m_temporaryOverrideFailureDefault),
						STATUS_TECHNICAL_FAILURE);
			else
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_FAILURE", m_copyFailureSMSDefault), callerID,
						null), STATUS_TECHNICAL_FAILURE);
			return;
		}

		Subscriber subscriber = (Subscriber) getFromZTable(z, SUBSCRIBER_OBJ);

		if (!(isSubActive(subscriber, z) || m_isActOptional || m_isActOptionalCopy)) {
			setReturnValues(z, getSMSTextForID(z, "HELP", m_helpDefault),
					STATUS_TECHNICAL_FAILURE);
			return;
		}

		int rbtType = TYPE_RBT;
		String revRBT = (String) getFromZTable(z, "REV_RBT");
		if (revRBT != null && revRBT.equalsIgnoreCase("TRUE"))
			rbtType = TYPE_RRBT;

		String name = null;
		String status = null;
		SubscriberStatus[] subscriberStatus = null;
		SubscriberStatus[] callerSubscriberStatus = null;
		List<SubscriberStatus> activeSubscriberStatus = new ArrayList<SubscriberStatus>();
		List<SubscriberStatus> callerActiveSubscriberStatus = new ArrayList<SubscriberStatus>();
		SubscriberStatus copySubscriberStatus = null;
		boolean isPrepaid = true;
		if (subscriber != null)
			isPrepaid = subscriber.prepaidYes();
		String playUncharged = "ALL";
		if (localSitePrefix != null
				&& !localSitePrefix.playUncharged(isPrepaid))
			playUncharged = "NONE";

		subscriberStatus = getSubscriberRecords(callerID, rbtType, null);
		callerSubscriberStatus = getSubscriberRecords(callerID, rbtType,
				subscriberID);

		

		if (callerSubscriberStatus!=null){
			for (int i = 0; i < callerSubscriberStatus.length; i++) {
				if (callerSubscriberStatus[i].selStatus().equals(
							STATE_ACTIVATED)) {

						callerActiveSubscriberStatus.add(callerSubscriberStatus[i]);
					}
				}

		}
		
		if (callerActiveSubscriberStatus.size()==0) {
			if (subscriberStatus == null && setForCaller == null) {

				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "COPY", subscriberID, null, 0, "SMS",
						null,null);

				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_SUCCESS", m_copySuccessSMSDefault), callerID,
						m_defaultClip), STATUS_SUCCESS);
				return;
			}

			if (subscriberStatus == null && setForCaller != null) {

				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "COPY", subscriberID,
						"default:-1:-1|" + setForCaller, 0, "SMS", null, null);

				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_SUCCESS", m_copySuccessSMSDefault), callerID,
						m_defaultClip), STATUS_SUCCESS);

				return;
			}

			for (int i = 0; i < subscriberStatus.length; i++) {
				if (subscriberStatus[i].selStatus().equals(STATE_ACTIVATED)) {

					activeSubscriberStatus.add(subscriberStatus[i]);
				}
			}

			if (activeSubscriberStatus.size() == 0 && setForCaller == null) {
				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "COPY", subscriberID, null, 0, "SMS",
						null, null);

				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_SUCCESS", m_copySuccessSMSDefault), callerID,
						m_defaultClip), STATUS_SUCCESS);

				return;
			}

			if (activeSubscriberStatus.size() == 0 && setForCaller != null) {
				RBTDBManager.getInstance().insertViralSMSTableMap(
						callerID, null, "COPY", subscriberID,
						"default:-1:-1|" + setForCaller, 0, "SMS", null, null);

				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_SUCCESS", m_copySuccessSMSDefault), callerID,
						m_defaultClip), STATUS_SUCCESS);

				return;
			}

			if (activeSubscriberStatus.size() == 1) {
				copySubscriberStatus = activeSubscriberStatus.get(0);
			} else {
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_FAILURE", m_copyFailureUGSSMSDefault), callerID,
						null), STATUS_SUCCESS);
				return;
			}
		} else {
			
			if (callerActiveSubscriberStatus.size() == 1) {
				copySubscriberStatus = callerActiveSubscriberStatus.get(0);
			} else if (callerActiveSubscriberStatus.size() > 1) {
				setReturnValues(z, getSubstituedSMS(getSMSTextForID(z,
						"COPY_FAILURE", m_copyFailureUGSSMSDefault), callerID,
						null), STATUS_SUCCESS);
				return;
			}
		}
		int catID = 26;
		String subWavFile = null;

		if (copySubscriberStatus != null)
			catID = copySubscriberStatus.categoryID();
		Categories cat = getCategory(catID, getCircleID(subscriberID));
		String clip;
		String endTime = null;
		if (copySubscriberStatus != null)
			subWavFile = copySubscriberStatus.subscriberFile();
		ClipMinimal clipObject = null;
		if (subWavFile != null)
			clipObject = m_rbtDBManager.getClipRBT(subWavFile);
		if (clipObject != null) {
			name = clipObject.getClipName();

			if (copySubscriberStatus != null)

				if (clipObject.getEndTime() != null)
					endTime = clipObject.getEndTime().toString();
		}
		if (subscriberStatus != null)
			status = "" + copySubscriberStatus.status();
		else
			status = "1";
		if (setForCaller != null)
			status = status + "|" + setForCaller;
		if (cat != null && cat.type() == SHUFFLE)
			clip = subWavFile + ":" + "S" + catID + ":" + status;
		else
			clip = subWavFile + ":" + catID + ":" + status;

		RBTDBManager.getInstance().insertViralSMSTableMap(callerID, null,
				"COPY", subscriberID, clip, 0, "SMS", null,null);
		setReturnValues(z, getSubstituedSMS(getSMSTextForID(z, "COPY_SUCCESS2",
				m_copySuccessSMSDefault2), callerID, name, endTime),
				STATUS_SUCCESS);
		return;

	}

	
	public void processGIFT(HashMap z, ArrayList smsList){
		String method = "processGIFT";
		String callerID = null;
		String subscriberID = null;
		subscriberID = (String) getFromZTable(z, SMS_SUBSCRIBER_ID);
		callerID = (String) getFromZTable(z, CALLER_ID);
		
		for(int i = 0; i < smsList.size(); i++) {
			String listMember = (String)smsList.get(i);
			if(listMember.indexOf(callerID) != -1)
				smsList.remove(i);
		}
		
        if (!isInitializationDone())
        {
            setReturnValues(z, getSMSTextForID(z, "TECHNICAL_FAILURE", m_technicalFailureDefault), STATUS_TECHNICAL_FAILURE);
            return;
        }

        
        Subscriber subscriber = (Subscriber)getFromZTable(z, SUBSCRIBER_OBJ);
        if(subscriber == null || !isSubActive(subscriber, z))
        {
        	setReturnValues(z, getSMSTextForID(z, "GIFT_INACTIVE_GIFTER", m_giftInactiveGifterDefault), STATUS_SUCCESS);
            return;
        }
        
        String clipID = null;
    	String gift = null;
    	
    	
    	if (callerID == null
    			|| callerID.equalsIgnoreCase(subscriberID)
    			|| (!m_giftNational && !(isValidSub(callerID)))
    			|| (m_giftNational && (!m_rbtDBManager
    					.isValidOperatorPrefix(callerID))))
    	{
    		setReturnValues(z, getSMSTextForID(z, "GIFT_CODE_FAILURE", m_giftCodeFailureDefault), STATUS_SUCCESS);
    		return;
    	}
    	
        if(smsList.size()>0){
        	String token = (String) smsList.get(0);
        	getCategoryAndClipForPromoID(token, z, false);
        	ClipMinimal clipMinimal = (ClipMinimal) getFromZTable(z, CLIP_OBJECT);
        	Categories category = (Categories) getFromZTable(z, CATEGORY_OBJECT);
        	if (clipMinimal == null)
        	{
        		setReturnValues(z, getSMSTextForID(z, "GIFT_CODE_FAILURE", m_giftCodeFailureDefault), STATUS_SUCCESS);
        		return;
        	}

        	if (category != null)
        	{
        		gift = category.name();
        		clipID = "C" + category.id();
        	}
        	else if (clipMinimal != null)
        	{
        		gift = clipMinimal.getClipName();
        		clipID = "" + clipMinimal.getClipId();
        	}
        	
        }else{
        	
        	clipID = null;
        	gift = "Subscription";
        }
       
        String canBeGifted = RBTDBManager.getInstance().canBeGifted(subscriberID,callerID,clipID);
        if(canBeGifted.substring(5,6).equals("S")){
        	insertViralSMSTable(subscriberID, callerID, clipID, "GIFT", 0);
        	setReturnValues(z, getSubstituedSMS(getSMSTextForID(z, "GIFT_SUCCESS", m_giftSuccessDefault), gift, callerID),
        				STATUS_SUCCESS);
        }else{
        	if(canBeGifted.equals(GIFT_FAILURE_GIFTER_NOT_ACT))
        		setReturnValues(z,getSMSTextForID(z, "GIFT_INACTIVE_GIFTER", m_giftInactiveGifterDefault),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_ACT_PENDING))
        		setReturnValues(z,getSMSTextForID(z, "GIFT_CALLER_STATUS_PENDING",m_giftCallerStatusPendingDefault),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_DEACT_PENDING))
        		setReturnValues(z,getSMSTextForID(z, "GIFT_CALLER_STATUS_PENDING",m_giftCallerStatusPendingDefault),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_TECHNICAL_DIFFICULTIES))
        		setReturnValues(z,getSMSTextForID(z, "GIFT_TECHNICAL_DIFFICULTIES_FAILURE",m_giftTechnicalDifficultiesFailureDefault),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_ACT_GIFT_PENDING))
        		setReturnValues(z,getSubstituedSMS(getSMSTextForID(z, "GIFT_ALREADY_PENDING",m_giftAlreadyPendingDefault), gift, callerID),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_GIFT_IN_USE))
        		setReturnValues(z,getSubstituedSMS(getSMSTextForID(z, "GIFT_ALREADY_IN_USE",m_giftAlreadyInUseDefault), null, callerID),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_SONG_GIFT_PENDING))
        		setReturnValues(z,getSubstituedSMS(getSMSTextForID(z, "GIFT_ALREADY_PENDING",m_giftAlreadyPendingDefault), gift, callerID),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS))
        		setReturnValues(z,getSubstituedSMS(getSMSTextForID(z, "GIFT_ALREADY_PRESENT",m_giftAlreadyPresentDefault), gift, callerID),STATUS_SUCCESS);
        	else if(canBeGifted.equals(GIFT_FAILURE_GIFTEE_INVALID))
        		setReturnValues(z,getSubstituedSMS(getSMSTextForID(z, "GIFT_FAILURE_GIFTEE_INVALID", m_giftInvalidGifteeDefault), null, callerID),STATUS_SUCCESS);
        }
	}
}
