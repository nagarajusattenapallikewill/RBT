package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.esia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.smClient.RBTSMClientHandler;
import com.onmobile.apps.ringbacktones.smClient.beans.Offer;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPromo;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


public class EsiaSmsProcessor extends SmsProcessor
{
	String offerId = null;
	ArrayList<String> offerSongsId = new ArrayList<String>();
	RBTDBManager rbtDBManager = null;
	public EsiaSmsProcessor() throws RBTException
	{
		String param = param(SMS,NEW_STARTER_PACK, null);
		if(param != null)
		{
			String[] offerIdAndOfferSongIds = param.split(";");
			offerId = offerIdAndOfferSongIds[0];
			if(offerIdAndOfferSongIds.length > 1) {
				offerSongsId = Processor.tokenizeArrayList(offerIdAndOfferSongIds[1], ",");
			}
		}

		rbtDBManager = RBTDBManager.getInstance();
		
	}

	public void preProcess(Task task)
	{
		ArrayList<String> smsText = (ArrayList<String>)task.getObject(param_smsText);
		if(smsText != null && smsText.size() > 0)
		{
			if(smsText.contains("on"))
			{
				task.setObject(param_optin, "OPTIN");
				smsText.remove("on");
			}
			if(smsText.contains("sub"))
			{
				task.setObject(param_optin, "OPTOUT");
				smsText.remove("sub");
			}

			if(smsText.contains("minggu"))
			{
				task.setObject(param_chargeModel, "WEEKLY");
				smsText.remove("minggu");
			}

			if(smsText.contains("bulan"))
			{
				task.setObject(param_chargeModel, "MONTHLY");
				smsText.remove("bulan");
			}
			
			String dailyChargingKeyword = param(SMS, "DAILY_CHARGE_KEYWORD","daily");
			if(smsText.contains(dailyChargingKeyword))
			{
				task.setObject(param_chargeModel, "DAILY");
				smsText.remove(dailyChargingKeyword);
			}

			if(!task.containsKey(param_optin))
				task.setObject(param_optin, param(SMS,DEFAULT_SUBSCRIPTION_TYPE,"OPTOUT"));
			if(!task.containsKey(param_chargeModel))
				task.setObject(param_chargeModel, param(SMS,DEFAULT_CHARGING_CYCLE,"MONTHLY"));
			if(smsText.contains("set"))
				smsText.remove("set");
		}
		super.preProcess(task);
		if(task.getString(param_USER_INFO) != null)
			task.setObject(param_actby, "SMS:"+task.getString(param_USER_INFO).trim().toUpperCase());
	}
	
	public void processActivationRequest(Task task)
	{
		
	}
	
	public void processActNSel(Task task)
	{
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
//		boolean isActOptional = true;
//		boolean isActRequest = task.getString(IS_ACTIVATION_REQUEST).equalsIgnoreCase("true");
		
		ArrayList<String> smsList = (ArrayList<String>) task.getObject(param_smsText);
		String response = null;
		if(smsList.size() > 0 )
		{
			getCategoryAndClipForPromoID(task,smsList.get(0));
			Clip clip = (Clip)task.getObject(CLIP_OBJ);
			if(clip == null)
				clip = rbtCacheManager.getClipFromPromoMaster(smsList.get(0));
			if(clip == null || clip.getClipEndTime().getTime() < System.currentTimeMillis())
			{
				if(getProfileClip(task) == null)
				{
						task.setObject(param_responseSms, getSMSTextForID(task,PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber.getLanguage()));
						return;
				}
			}
			else
				task.setObject(param_clipid, clip.getClipId()+"");
			if(clip != null && clip.getClassType() != null && clip.getClassType().startsWith("TRIAL"))
			{
				if(!Utility.isTrialWithActivations(clip.getClassType(), param(COMMON, TRIAL_WITH_ACT,null)))
				{	if(!isUserActive(subscriber.getStatus()))
					{
						task.setObject(param_responseSms, getSMSTextForID(task,ACT_TRIAL_FAILURE, m_actTrialFailureDefault, subscriber.getLanguage()));
						return;
					}
				}
				else
				{
//					SubscriberPromo sp = getSubscriberPromo(task);
//					Setting[] settings = getActiveSubSettings(subscriber.getSubscriberID(), 1);
//					
//					if (sp != null && Utility.isTrialBlackoutPeriod(task, sp.getStartDate(), settings))
//					{
//						task.setObject(param_responseSms, getSMSTextForID(task,TRIAL_REPEAT_FAILURE, m_repeatTrialFailureDefault, subscriber.getLanguage()));
//						return;
//					}
//					else
//					{
//						if(sp != null)
//							deleteSubscriberPromo(task);
//						addSubscriberPromo(task);
//					}
					
					String type = "TRIAL";
					boolean isNspTrial = false;
					boolean isNewStarterpackAllowed = false;
					if(offerSongsId != null && offerSongsId.contains(clip.getClipId()+""))
					{
						logger.info("the request is NSP song request");
						type = "NSP_TRIAL";
						isNspTrial = true;
//						boolean isNewStarterpackAllowed = false;
						if(isUserActive(subscriber.getStatus()))
						{
							task.setObject(param_mode,"CCC");
							subscriber = getSubscriber(task);
							HashMap<String,String> extraInfo = subscriber.getUserInfoMap();
							if(extraInfo != null && extraInfo.containsKey(EXTRA_INFO_OFFER_ID))
							{
								if(extraInfo.get(EXTRA_INFO_OFFER_ID) != null && offerId != null && offerId.equalsIgnoreCase(extraInfo.get(EXTRA_INFO_OFFER_ID)))
									isNewStarterpackAllowed = true;
							}
							else
							{
								isNewStarterpackAllowed = isSubscriberValidForNSP(task, subscriber);
							}
						}
						else
						{
							isNewStarterpackAllowed = isSubscriberValidForNSP(task, subscriber);
//							try
//							{
//								Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriber.getSubscriberID(), task.getString(ACT_BY), Offer.OFFER_TYPE_SUBSCRIPTION , subscriber.getUserType());
//								if(offer != null && offer.length > 0)
//								{
//									if(offer[0].getOfferID() != null && offerId != null && offerId.equalsIgnoreCase(offer[0].getOfferID()))
//									{
//										isNewStarterpackAllowed = true;
//										task.setObject(EXTRA_INFO_OFFER_ID, offerId);
//									}
//								}	
//							}
//							catch(Exception e)
//							{
//								
//							}
						}
//						if(!isNewStarterpackAllowed)
//						{
//							task.setObject(param_responseSms, getSMSTextForID(task,TRIAL_REPEAT_FAILURE, m_repeatTrialFailureDefault, subscriber.getLanguage()));
//							return;
//						}						
					}
					
					SubscriberPromo sp = getSubscriberPromo(task,type);
					Setting[] settings = getActiveSubSettings(subscriber.getSubscriberID(), 1);
					
					if (sp != null && Utility.isTrialBlackoutPeriod(task, sp.getStartDate(), settings))
					{
						task.setObject(param_responseSms, getSMSTextForID(task,TRIAL_REPEAT_FAILURE, m_repeatTrialFailureDefault, subscriber.getLanguage()));
						return;
					}
					else
					{
						if(sp != null)
							deleteSubscriberPromo(task,type);
						addSubscriberPromo(task,type);
					}
					
					if(isNspTrial && !isNewStarterpackAllowed){
						logger.info("new starter pack not allowed");
						task.setObject(param_responseSms, getSMSTextForID(task,TRIAL_REPEAT_FAILURE, m_repeatTrialFailureDefault, subscriber.getLanguage()));
						return;
					}
					
					logger.info("allow new starter pack");
					
				}
			}
			//Disallowing starter pack for ADRBT user
			String catId = (String)task.getObject(param_catid);
			String starterPackCategories = param(SMS,STARTER_PACK_CATEGORIES,null);
			if(checkAdRBTstatus(subscriber) && starterPackCategories!=null && catId !=null){
				StringTokenizer stk = new StringTokenizer(starterPackCategories , ",");
				while(stk.hasMoreTokens()){
					if(((String)stk.nextElement()).equals(catId)){
						logger.info("ESIA starter pack not allowed for adRBT user(subscriberId="+subscriber.getSubscriberID());
						task.setObject(param_responseSms, "ADRBT users not allowed for ESIA starter pack");
						return;
					}
				}
			}
			
			if(param(COMMON,ALLOW_LOOPING,false) && param(COMMON,ADD_SEL_TO_LOOP,false))
				task.setObject(param_inLoop, "YES");
			clip = getProfileClip(task);
			if(clip != null)
			{
				processSetTempOverride(task);
				return;
			}
			HashMap<String,String> hashMap = new HashMap<String,String>();
			hashMap.put("CALLER_ID", task.getString(param_callerid) == null ? param(SMS,SMS_TEXT_FOR_ALL,"all") : task.getString(param_callerid));
			logger.info("RBT:: : processSetSelection api invoking");
			response = processSetSelection(task);
			logger.info("RBT:: : processSetSelection response : " + response);
			Setting[] settingList = null;
			if(response.equals("success"))
			{
				clip = (Clip)task.getObject(CLIP_OBJ);
				if(clip != null)
					hashMap.put("SONG_NAME", clip.getClipName());
				if(param(SMS,GIVE_UGS_SONG_LIST,false))
				{
					Settings settings = getSettings(task);
					logger.info("RBT:: processDeactivation : " + (settings == null ? null : settings.getNoOfDefaultSettings()));
					if(settings != null)
					{
						String songList = "";
						settingList = settings.getSettings();
						if(settingList != null && settingList.length > 0)
						{
							for(int i = 0; i < settingList.length  ; i++)
							{
								logger.info("RBT:: processDeactivation2 : " + task.getString(param_callerid));
								logger.info("RBT:: processDeactivation3 : " +  settingList[i].getCallerID());
								if( (task.getString(param_callerid) == null && settingList[i].getCallerID().equalsIgnoreCase("all")) || (task.getString(param_callerid) != null && task.getString(param_callerid).equalsIgnoreCase(settingList[i].getCallerID())))
								{
									logger.info("RBT:: processDeactivation4 : " + task);
									String wavFile = settingList[i].getRbtFile();
									if(wavFile != null && wavFile.length() > 4)
									{	wavFile = wavFile.substring(0,wavFile.length()-4);
										Clip setClip = getClipByWavFile(wavFile,subscriber.getLanguage());
										if(setClip != null)
										{
											songList = songList + ", "+setClip.getClipName();
										}
									}
								}
								logger.info("RBT:: processDeactivation3 : " + songList);	 
							}
							if(songList.length() > 2)
								songList = songList.substring(2);
							hashMap.put("SONG_LIST",songList);
						}
					}	
					
				}
					
				if(param(SMS,GIVE_UGS_SONG_LIST,false)  && settingList.length > 1)
					hashMap.put("SMS_TEXT", getSMSTextForID(task,PROMO_ID_SUCESS_UGS_LIST, m_promoSuccessWithUGSSongListTextDefault, subscriber.getLanguage()));
				else
					hashMap.put("SMS_TEXT", getSMSTextForID(task,PROMO_ID_SUCCESS, m_promoSuccessTextDefault, subscriber.getLanguage()));
				logger.info("RBT:: hashMap : " + hashMap);
				task.setObject(param_responseSms, finalizeSmsText(hashMap));
				
				catId = ((String)task.getObject(param_catid));
				String subscriberId = task.getString(param_subscriberID);
				if(subscriberId==null) subscriberId = task.getString(param_subID);
				if(starterPackCategories!=null && catId !=null && subscriberId!=null ){
					StringTokenizer stk = new StringTokenizer(starterPackCategories , ",");
					while(stk.hasMoreTokens()){
						if(((String)stk.nextElement()).equals(catId)){
							rbtDBManager.addShufflePromoEntry(catId,subscriberId);
						}
					}
				}else{
					logger.error("type:SMS, param:STARTER_PACK_CATEGORIES not configured in rbt_parameters table");
				}
				
			}
			if(response.equals(WebServiceConstants.SELECTION_SUSPENDED))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,SELECTION_SUSPENDED_TEXT, m_SuspendedSelDefault, subscriber.getLanguage()));
			}
			else if(isActivationFailureResponse(response))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			}
			else if(response.equals(WebServiceConstants.ALREADY_EXISTS))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,SELECTION_ALREADY_EXISTS_TEXT, getSMSTextForID(task,PROMO_ID_SUCCESS, m_promoSuccessTextDefault, subscriber.getLanguage()), subscriber.getLanguage()));
			}
			else if(response.equals(WebServiceConstants.ALREADY_ACTIVE))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,SELECTION_DOWNLOAD_ALREADY_ACTIVE_TEXT, getSMSTextForID(task,PROMO_ID_SUCCESS, m_promoSuccessTextDefault, subscriber.getLanguage()), subscriber.getLanguage()));
			}
			else if(response.equals(WebServiceConstants.NOT_ALLOWED))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,SELECTION_ADRBT_NOTALLOWED_, m_ADRBTSelectionFailureDefault, subscriber.getLanguage()));
			}
			else if(response.equals(WebServiceConstants.SELECTION_OVERLIMIT))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,SELECTION_OVERLIMIT, getSMSTextForID(task,PROMO_ID_FAILURE, m_promoIDFailureDefault, subscriber.getLanguage()), subscriber.getLanguage()));
			}
			else if(response.equals(WebServiceConstants.OFFER_NOT_FOUND))
			{
				task.setObject(param_responseSms, getSMSTextForID(task,OFFER_NOT_FOUND_TEXT, m_OfferAlreadyUsed, subscriber.getLanguage()));
			}
		}
	}
	
	@Override
	public void processUpgradeSelRequest(Task task)
	{
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus()))
		{
			task.setObject(param_responseSms,
					getSMSTextForID(task, USER_NOT_ACTIVE,
							m_userNotActiveDefault, subscriber.getLanguage()));
			return;
		}
		
		String promoID = null;
		List<String> wavFiles = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task.getObject(param_smsText);
		if (smsList == null || smsList.size() == 0)
		{
			SubscriberStatus[] settings = rbtDBManager
			.getAllActiveSubscriberSettings(subscriber.getSubscriberID());
			if(settings == null || settings.length<1)
			{
				task.setObject(param_responseSms,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault, subscriber.getLanguage()));
				return;
			}
			for(int i=0;i<settings.length;i++)
				wavFiles.add(settings[i].subscriberFile());
		}
		else
		{
			promoID = smsList.get(0);
		}

		boolean isSuccess = false;
		task.setObject(param_SUBID, subscriber.getSubscriberID());
		
		if(promoID != null){ 
			Clip clip = rbtCacheManager.getClipByPromoId(promoID, subscriber.getLanguage());
			if (clip == null)
			{
				task.setObject(param_responseSms,
						getSMSTextForID(task, PROMO_ID_FAILURE,
								m_promoIDFailureDefault, subscriber.getLanguage()));
				return;
			}
			task.setObject(param_CLIPID, String.valueOf(clip.getClipId()));

			String response = upgradeSelectionPack(task);
			if(response.equalsIgnoreCase("SUCCESS"))
				isSuccess = true;
		}
		else if(wavFiles !=null && wavFiles.size()>0)
		{
			
			for(int j=0;j<wavFiles.size();j++)
			{	
				Clip clip = rbtCacheManager.getClipByRbtWavFileName(wavFiles.get(j), subscriber.getLanguage());
				if (clip != null)
				{
					task.setObject(param_CLIPID, String.valueOf(clip.getClipId()));

					String response = upgradeSelectionPack(task);
					if(response.equalsIgnoreCase("SUCCESS"))
						isSuccess = true;
				}
			}
		}

		if (isSuccess)
			task.setObject(param_responseSms, getSMSTextForID(task, SEL_UPGRADE_SUCCESS, m_selUpgradeSuccessDefault, subscriber.getLanguage()));
		else
			task.setObject(param_responseSms, getSMSTextForID(task, SEL_UPGRADE_FAILURE, m_selUpgradeFailureDefault, subscriber.getLanguage()));
	}
	
	@Override
	public void processChargingConsentRequest(Task task)
	{
		boolean consent = (task.getTaskAction().equals(CONSENT_YES_KEYWORD));
		String consentString = consent ? "YES" : "NO";

		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (!isUserActive(subscriber.getStatus()))
		{
			logger.info("Inactive user");
			task.setObject(
					param_responseSms,
					getSMSTextForID(task, "CONSENT_" + consentString
							+ "_REQUEST_INACTIVE_USER", "CONSENT_"
							+ consentString + "_REQUEST_INACTIVE_USER",
							subscriber.getLanguage()));
			return;
		}

		String subscriberID = subscriber.getSubscriberID();

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		boolean isSuccess = false ;
		if (smsList == null || smsList.size() == 0)
		{
			Map<String, String> upgradeChargeClassMap = new HashMap<String, String>();
			String chargeClassMapStr = RBTParametersUtils
					.getParamAsString(iRBTConstant.COMMON,
							"UPGRADE_CHARGE_CLASS_MAP", null);
			if (chargeClassMapStr != null) {
				String[] mappings = chargeClassMapStr.split(",");
				for (String eachMapping : mappings) {
					String[] tokens = eachMapping.split(":");
					upgradeChargeClassMap.put(tokens[0].trim(),
							tokens[1].trim());
				}
			}

			SubscriberStatus[] settings = rbtDBManager
			.getAllActiveSubscriberSettings(subscriber.getSubscriberID());
			for(int i=0;i<settings.length;i++)
			{
				String newClassType = upgradeChargeClassMap.get(settings[i].classType());
				if (newClassType != null) {
					//upgradable selection is present
					Category category = null;
					Clip clip = getClipByWavFile(settings[i].subscriberFile(), subscriber.getLanguage());
					if (clip != null)
					{
						String response = getSelectionConsent(clip, category, subscriberID, consent);
						if(response.equalsIgnoreCase("SUCCESS"))
							isSuccess = true;
					}
				}
			}
			
			String smsText = null;
			if(isSuccess){
				logger.info("Subscriber consent success");
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_NO_SELECTION_SUCCESS", "CONSENT_" + consentString
						+ "_REQUEST_NO_SELECTION_SUCCESS", subscriber.getLanguage());
			}
			else
			{
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_SELECTION_FAILURE", "CONSENT_"
						+ consentString + "_REQUEST_SELECTION_FAILURE",
						subscriber.getLanguage());
			}
			
			task.setObject(param_responseSms, smsText);

		}
		else
		{
			Category category = null;
			String promoID = smsList.get(0).trim();
			Clip clip = getClipByPromoId(promoID, subscriber.getLanguage());
			if (clip == null)
			{
				category = getCategoryByPromoId(promoID, subscriber.getLanguage());
			}

			if (clip == null && category == null)
			{
				task.setObject(
						param_responseSms,
						getSMSTextForID(task, "CONSENT_" + consentString
								+ "_REQUEST_INVALID_CONTENT", "CONSENT_"
								+ consentString + "_REQUEST_INVALID_CONTENT",
								subscriber.getLanguage()));
				return;
			}

			String smsText = null;
			String response = getSelectionConsent(clip, category, subscriberID, consent);
			if (response == null)
			{
				logger.info("Subscriber does not have selections");
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_NO_DOWNLOAD", "CONSENT_" + consentString
						+ "_REQUEST_NO_DOWNLOAD", subscriber.getLanguage());
			}
			else
			{
				smsText = getSMSTextForID(task, "CONSENT_" + consentString
						+ "_REQUEST_DOWNLOAD_" + response, "CONSENT_"
						+ consentString + "_REQUEST_DOWNLOAD_" + response,
						subscriber.getLanguage());
			}

			String contentName = "";
			if (clip != null)
				contentName = clip.getClipName();
			else if (category != null)
				contentName = category.getCategoryName();

			HashMap<String, String> hashMap = new HashMap<String, String>();
			hashMap.put("SMS_TEXT", smsText);
			hashMap.put("SONG_NAME", contentName);
			task.setObject(param_responseSms, finalizeSmsText(hashMap));

		}
	}
	
	private String getSelectionConsent(Clip clip, Category category , String subscriberID , boolean consent)
	{
		
			// Selection Consent Request
			Library library = rbtClient.getLibraryHistory(new RbtDetailsRequest(subscriberID));
			Settings settingsObj = null;
			if (library != null)
				settingsObj = library.getSettings();
			Setting[] settings = null;
			if (settingsObj != null)
				settings = settingsObj.getSettings();

			String response = null;
			if (settings != null)
			{
				for (Setting setting : settings)
				{
					String selectionStatus = setting.getSelectionStatus();
					if ((!selectionStatus.equals(WebServiceConstants.DEACT_PENDING)
							&& !selectionStatus.equals(WebServiceConstants.DEACTIVE)) 
							&& ((category != null
							&& com.onmobile.apps.ringbacktones.webservice.common.Utility
									.isShuffleCategory(category.getCategoryTpe())
							&& category.getCategoryId() == setting.getCategoryID())
							|| (clip != null && clip.getClipId() == setting.getToneID())))
					{
						String srvKey = "RBT_SEL_" + setting.getChargeClass();
						String refID = setting.getRefID();

						response = Utility.hitConsentRequestToSM(
								subscriberID, srvKey, refID, consent);
					}
				}
			}
			return response;
	}


	private boolean checkAdRBTstatus(Subscriber subscriber) {
		String activatedBy = subscriber.getActivatedBy();
		boolean isAdRBTSubscriber = false;
		if(activatedBy!=null) isAdRBTSubscriber = activatedBy.equalsIgnoreCase("adrbt");
		return isAdRBTSubscriber;
	}

	private boolean isSubscriberValidForNSP(Task task, Subscriber subscriber){
		try
		{
			Offer[] offer = RBTSMClientHandler.getInstance().getOffer(subscriber.getSubscriberID(), task.getString(ACT_BY), Offer.OFFER_TYPE_SUBSCRIPTION , subscriber.getUserType());
			if(offer != null && offer.length > 0)
			{
				if(offer[0].getOfferID() != null && offerId != null && offerId.equalsIgnoreCase(offer[0].getOfferID()))
				{
					task.setObject(EXTRA_INFO_OFFER_ID, offerId);
					return true;
				}
			}	
		}
		catch(Exception e)
		{
			
		}
		return false;
	}
}
