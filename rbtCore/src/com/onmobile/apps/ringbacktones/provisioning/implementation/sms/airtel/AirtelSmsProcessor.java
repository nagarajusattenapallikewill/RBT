package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.airtel;

import java.util.ArrayList;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;

public class AirtelSmsProcessor extends SmsProcessor
{
	public AirtelSmsProcessor() throws RBTException
	{
		
	}
	
	public void preProcess(Task task)
	{
		removeODAKeywordsIfExists(task);
		super.preProcess(task);
		String feature = task.getTaskAction();
		boolean isActRequest = task.getString(IS_ACTIVATION_REQUEST).equalsIgnoreCase("true");
		if(feature.equalsIgnoreCase(ACTIVATE_N_SELECTION) && !isActRequest)
			task.setTaskAction(CATEGORY_SEARCH_KEYWORD);
	}

	/**
	 * Added for Airtel comes with Music opt in feature. If user sends this keyword we will invoke
	 * the SM client's confirm charge API informing that the user wants to continue the service
	 * 
	 * @author Sreekar
	 */
	public void processConfirmCharge(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subID = subscriber.getSubscriberID();
		HashMap<String, String> xtraInfo = subscriber.getUserInfoMap();
		if(xtraInfo!= null && xtraInfo.containsKey(EXTRA_INFO_OFFER_ID)) {
			String subOfferID = xtraInfo.get(EXTRA_INFO_OFFER_ID);
			String offerID = parameterCacheManager.getParameter(COMMON, ACWM_OFFER_ID, "-100").getValue();
			if(offerID.equalsIgnoreCase(subOfferID)){
				//added by mohsin
				task.setObject(param_callerid, null);
				task.setObject(param_subscriberID, subID);
				task.setObject(param_SMSTYPE,"OPTIN");
				ViralData[] viral = getViraldata(task);
				if(!param(SMS, "DIRECT_OPT_IN_ACWM", false) &&(viral == null || viral.length <=0)){
					addViraldata(subID, null, "OPTIN", null, "SMS",0, null);
					task.setObject(param_responseSms, getSMSTextForID(task,CONFIRM_CHARGE_CONFIRM, m_confirmChargeConfirmFailureDefault,subscriber.getLanguage()));
				}else{
					confirmSubscription(task);
					if(task.containsKey(param_responseSms))
						removeViraldata(subID, null, "OPTIN");
				}
			}
		}
		if(!task.containsKey(param_responseSms))
			task.setObject(param_responseSms, getSMSTextForID(task,CONFIRM_CHARGE_FAILURE, m_confirmChargeFailureDefault,subscriber.getLanguage()));
	}
	
	public void processActNSel(Task task)
	{
		super.processActNSel(task);
		if (task.containsKey(param_isSuperHitAlbum) && task.containsKey(param_isPromoIDFailure))
		{
			processListCategories(task);
		}
	}
	
	public void processListCategories(Task task)
	{
		String smsText = "";
		if (task.containsKey(param_isPromoIDFailure))
			smsText = task.getString(param_responseSms);
		
		super.processListCategories(task);
		
		String finalSmsText = task.getString(param_responseSms);
		finalSmsText = smsText +" "+ finalSmsText;
		task.setObject(param_responseSms, finalSmsText);
	}
	
	private void removeODAKeywordsIfExists(Task task)
	{
		ArrayList<String> smsList = (ArrayList<String>)task.getObject(param_smsText);
		ArrayList<String> odaKeyWords = tokenizeArrayList(param(SMS, ODA_KEYWORD, "SUPERHIT,ALBUM"), null);
		logger.info("oda key words >"+odaKeyWords);
		
		if (smsList == null || smsList.size() == 1)
			return;
		if (odaKeyWords == null)
		{
			logger.info("oda key words are null>");
			return;
		}
		
		for (String key : odaKeyWords) 
		{
			if (smsList.contains(key))
			{
				smsList.remove(key);
				task.setObject(param_isSuperHitAlbum, "TRUE");
			}
		}
		logger.info("smslist >"+smsList);
		task.setObject(param_smsText, smsList);
		
	}
}