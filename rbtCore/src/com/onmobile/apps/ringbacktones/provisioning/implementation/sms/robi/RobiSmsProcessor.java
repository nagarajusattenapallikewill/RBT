package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.robi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RobiSmsProcessor extends SmsProcessor{

	public RobiSmsProcessor() throws RBTException {
		super();
	}
	
	@Override
	public void getGift(Task task) {
		String songName = null;
		String giftDefClipID = null;
		String artist = null;
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList != null && smsList.size() > 0)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		String language = subscriber.getLanguage();
		if (task.getObject(CAT_OBJ) != null) {
			task.setObject(param_catid, ((Category) task.getObject(CAT_OBJ))
					.getCategoryId()
					+ "");
			songName = ((Category) task.getObject(CAT_OBJ)).getCategoryName();
		} else if (task.getObject(CLIP_OBJ) != null) {
			task.setObject(param_clipid, ((Clip) task.getObject(CLIP_OBJ))
					.getClipId()
					+ "");
			songName = ((Clip) task.getObject(CLIP_OBJ)).getClipName();
			artist = ((Clip) task.getObject(CLIP_OBJ)).getArtist();
		} else if ((giftDefClipID = RBTParametersUtils.getParamAsString(
				"COMMON", "DEFAULT_CLIP_ID_FOR_GIFT", null)) != null) {
			Clip giftClip = rbtCacheManager.getClip(giftDefClipID);
			if (giftClip != null) {
				task.setObject(param_clipid, String.valueOf(giftClip
						.getClipId()));
				songName = giftClip.getClipName();
				artist = giftClip.getArtist();
			} else {
				task.setObject(param_responseSms, getSMSTextForID(task,
						GIFT_CODE_FAILURE, m_giftCodeFailureDefault, language));
				return;
			}
		} else {
			task.setObject(param_responseSms, getSMSTextForID(task,
					GIFT_CODE_FAILURE, m_giftCodeFailureDefault, language));
			return;
		}
		String gifteeID = task.getString(param_callerid);
		String response = null;
		if(gifteeID != null){
		     response = processGift(task);
		}else{
			 response = WebServiceConstants.INVALID_GIFTEE_ID;
		}
		logger.info("SMSProcessor Gift Response === "+response);
		String smsText = null;
		if (response.equalsIgnoreCase(WebServiceConstants.SUCCESS)) {
			String defaultConfTextForGiftSuccess = getSMSTextForID(task,
					GIFT_SUCCESS, m_giftSuccessDefault, language);
			StringBuffer confForGiftSuccess = new StringBuffer(GIFT_SUCCESS);
			confForGiftSuccess.append("_").append(
					subscriber.getStatus().toUpperCase());
			smsText = getSMSTextForID(task,
					confForGiftSuccess.toString(),
					defaultConfTextForGiftSuccess, language);
		} else if (response.equalsIgnoreCase(WebServiceConstants.INVALID)
				|| response.equalsIgnoreCase(WebServiceConstants.INVALID_GIFTEE_ID)) {
			smsText = getSMSTextForID(task, GIFT_GIFTEE_INVALID_FAILURE, m_giftGifteeInvalidFailureDefault, language);
		} else if (response.equalsIgnoreCase(WebServiceConstants.OWN_NUMBER)) {
			smsText = getSMSTextForID(task, GIFT_OWN_NUMBER_FAILURE, m_giftOwnNumberFailure,
					language);
		} else if (response.equalsIgnoreCase(WebServiceConstants.NOT_ALLOWED)) {
			smsText = getSMSTextForID(task, GIFT_NOT_ALLOWED, m_giftNotAllowed, language);
		} else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_EXPIRED)) {
			smsText = getSMSTextForID(task, GIFT_CLIP_EXPIRED, m_giftClipExpired, language);
		}else if (response.equalsIgnoreCase(WebServiceConstants.CLIP_NOT_EXISTS)) {
			smsText = getSMSTextForID(task, GIFT_CLIP_NOT_EXISTS, m_giftClipExpired, language);
		}else if (response.equalsIgnoreCase(WebServiceConstants.EXISTS_IN_GIFTEE_LIBRAY)) {
			smsText = getSMSTextForID(task, GIFT_ALREADY_EXISTS, m_giftAlreadyExistsDefault,
					language);
		} else if (response.equalsIgnoreCase(WebServiceConstants.LIMIT_EXCEEDED)) {
			smsText = getSMSTextForID(task, MAX_TONE_GIFT_LIMIT_EXCEEDED,
					m_maxToneGiftLimitExceeded, language);
		} else {
			smsText = getSMSTextForID(task, HELP_SMS_TEXT, m_helpDefault, language);
		}
		
		HashMap<String, String> hashMap = new HashMap<String, String>();
		hashMap.put("SMS_TEXT", smsText);
		hashMap.put("SONG_NAME", songName == null ? "" : songName);
		hashMap.put("CALLER_ID",
				task.getString(param_callerid) == null ? "" : task
						.getString(param_callerid));
		hashMap.put("ARTIST", artist == null ? "" : artist); 
		task.setObject(param_responseSms, finalizeSmsText(hashMap));

	}
	
	
	@Override
	public void processDownloadsList(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		if (!isUserActive(subscriber.getStatus())) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()));
			return;
		}

		Downloads downloads = getDownloads(task);
		Download sd[] = downloads.getDownloads();

		if (sd == null) {
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOADS_NOT_PRESENT, m_downloadsNoSelDefault, subscriber
							.getLanguage()));
			return;
		}

		String sms = "";

		int songCount = 1;
		for (int i = 0; sd.length > 0 && i < sd.length; i++) {
			Clip clip = getClipById(sd[i].getToneID() + "", subscriber
					.getLanguage());
			if (clip != null
					&& !sd[i].getDownloadStatus().equalsIgnoreCase("x")){
				String artist = clip.getArtist();
				String songName = clip.getClipName();
				String clipPromoId = clip.getClipPromoId(); 
				String chargeClass = sd[i].getChargeClass();
				ChargeClass chargeClassObj = CacheManagerUtil.getChargeClassCacheManager()
						.getChargeClass(chargeClass);
				String amount = chargeClassObj.getAmount();
				Date downloadEndTime = sd[i].getEndTime(); 
                String endTime = new SimpleDateFormat("dd-MM-yy").format(downloadEndTime);
                String currency = getParamAsString(COMMON, "CURRENCY_SYMBOL", "Rs");
                
				StringBuilder dwnDetail = new StringBuilder();
				dwnDetail.append((songCount++) + ". ");
				dwnDetail.append("Code: "+ clipPromoId).append(", ");
				dwnDetail.append("name:"+songName).append(", ");
				dwnDetail.append("singer:"+artist).append(", ");
				dwnDetail.append("Price:"+amount).append(" "+currency).append(", ");
				dwnDetail.append("expiry:"+endTime + " ");
						
				sms += dwnDetail.toString();
			}
		}
		if (sms.length() > 1){
			String smsString = getSMSTextForID(task, DOWNLOADS_LIST_SUCCESS, m_downloadsListSuccessDefault, subscriber
					.getLanguage());
			smsString = smsString.replaceAll("%RES%", sms);
			task.setObject(param_responseSms, smsString);
//			task.setObject(param_responseSms, getSMSTextForID(task, DOWNLOADS_LIST_SUCCESS,
//							m_downloadsListSuccessDefault, subscriber
//									.getLanguage()) + sms);
		}else{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOADS_NOT_PRESENT, m_downloadsNoSelDefault, subscriber
							.getLanguage()));
		}
		
	}

	@Override
	public void processDownloadSetRequest(Task task)
	{
		Subscriber subscriber = getSubscriber(task);
		if (!isUserActive(subscriber.getStatus()))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_INACTIVE_USER, m_downloadSetInactiveUserTextDefault, subscriber
							.getLanguage()));
			return;
		}

		@SuppressWarnings("unchecked")
		ArrayList<String> smsList = (ArrayList<String>) task
				.getObject(param_smsText);
		if (smsList == null || smsList.size() < 1) 
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_INVALID_PROMOID, m_downloadSetInvalidPromoIDTextDefault, subscriber
							.getLanguage()));
			return;
		}

		if (task.getObject(CLIP_OBJ) == null)
			getCategoryAndClipForPromoID(task, smsList.get(0));

		Clip clip = (Clip) task.getObject(CLIP_OBJ);;
		if (clip == null
				|| clip.getClipEndTime().getTime() < System
						.currentTimeMillis())
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_INVALID_PROMOID, m_downloadSetInvalidPromoIDTextDefault, subscriber
							.getLanguage()));
			return;
		}

		SubscriberDownloads[] downloadsList = RBTDBManager.getInstance()
				.getActiveSubscriberDownloads(subscriber.getSubscriberID());
		if (!isDownloadExistsWithWavFile(downloadsList, clip.getClipRbtWavFile()))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_NO_DOWNLOAD, m_downloadSetNoDownloadTextDefault, subscriber
							.getLanguage()));
			return;
		}
		
		int fromTime = 0;
		int toTime = 2359;
		
		SubscriberStatus[] statuses = RBTDBManager.getInstance()
				.getActiveSubscriberStatus(subscriber.getSubscriberID(),
						task.getString(param_callerid), fromTime, toTime);
		logger.debug("subscriberId: " + subscriber.getSubscriberID() + ", selections: " + statuses);
		
		if (statuses != null && statuses.length > 0) {
			for (SubscriberStatus status : statuses) {
				if (status.status() != 1) {
					logger.info("subscriberId: "
							+ subscriber.getSubscriberID()
							+ ", subscriberWavFile: " + status.subscriberFile()
							+ ", refId: " + status.refID()
							+ ". Selection is with selection status: " + status.status() + ". Not removing.");
					continue;
				}
				
				if (status.subscriberFile() != null && status.subscriberFile().equals(clip.getClipRbtWavFile())) {
					logger.debug("subscriberId: "
							+ subscriber.getSubscriberID()
							+ ", subscriberWavFile: " + status.subscriberFile()
							+ ", refId: " + status.refID()
							+ ". Selection already exists. Not removing.");
					continue;
				}
				
				SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
				selectionRequest.setRefID(status.refID());
				RBTClient.getInstance().deleteSubscriberSelection(selectionRequest);
				String response = selectionRequest.getResponse();
				if (response.equalsIgnoreCase(SUCCESS)) {
					logger.info("Deleted selection with subscriberId: " + subscriber.getSubscriberID() + " and refId: " + status.refID());
				} else {
					logger.info("Selection deletion failed. SubscriberId: " + subscriber.getSubscriberID() + " and refId: " + status.refID());
				}
			}
		}
		String response = processSetSelection(task);
		if (response.toUpperCase().indexOf("SUCCESS")!=-1)
		{
			String smsText = getSMSTextForID(task,
					DOWNLOAD_SET_SUCCESS, m_downloadSetSuccessTextDefault, subscriber
						.getLanguage());
			String callerID = task.getString(param_callerid);
			if (callerID != null) {
				smsText = getSMSTextForID(task,
						DOWNLOAD_SET_CALLERID_SUCCESS, m_downloadSetSuccessTextDefault, subscriber
							.getLanguage());
			} else {
				callerID = param(SMS, SMS_TEXT_FOR_ALL, "all");
			}

			smsText = smsText.replace("%SONG_NAME", (clip.getClipName() == null) ? "":clip.getClipName());
			smsText = smsText.replace("%ARTIST", (clip.getArtist() == null) ? "":clip.getArtist());
			smsText = smsText.replace("%CALLER_ID", callerID);
			task.setObject(param_responseSms, smsText);
		}
		else if (response.equals(WebServiceConstants.ALREADY_EXISTS))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_ALREADY_EXISTS, m_downloadSetAlreadyExistsTextDefault, subscriber
							.getLanguage()));
		}
		else
		{
			task.setObject(param_responseSms, getSMSTextForID(task,
					DOWNLOAD_SET_FAILURE, m_downloadSetFailureTextDefault, subscriber
							.getLanguage()));
		}
	}
   	
}
