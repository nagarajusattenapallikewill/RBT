package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.oi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.sun.org.apache.xpath.internal.FoundIndex;

public class OISmsProcessor extends SmsProcessor {
	private static String[] alphabets = { "a", "b", "c", "d", "e", "f", "g",
		"h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
		"u", "v", "w", "x", "y", "z" };

	public OISmsProcessor() throws RBTException {
		super();
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String processDeactivation(Task task) {
		logger.info("RBT:: processDeactivation : " + task);
		String response = null;

		try {
			return listAndSendActiveDownloads(0, task);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("RBT:: processDeactivation : " + e.getMessage());
		}
		logger.info("RBT:: processDeactivation *** response is : " + response);
		task.setObject(param_response, response);
		return super.processDeactivation(task);
	}

	
	@Override
	public void processActNSel(Task task) {
		
		try {
			Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
			String language = subscriber.getLanguage();
			@SuppressWarnings("unchecked")
			ArrayList<String> smsList = (ArrayList<String>) task
			.getObject(param_smsText);
			if(smsList == null || smsList.size() <= 0){
				logger.debug("in the request smsText is null");
				task.setObject(param_responseSms, getSMSTextForID(task,
						INVALID_USER_REQUEST, m_smsRequestFailureDefault,
						language));
				return;
			}
			String selectionString = smsList.get(0);
			logger.debug("the requested tokens are :" + selectionString);
			
			task.setObject(param_SMSTYPE, "SMS_DCT_MANAGE");
			ViralData[] viralDataArray = getViraldata(task);
			logger.info("Fetched viralDataArray: "+ viralDataArray);
			
			if ((viralDataArray == null || viralDataArray.length <= 0) && selectionString.length() > 1) {
				logger.info("the request session expired as there are no entry in the viral sms table");
				super.processActNSel(task);
				logger.info("Processed task: "+ task);
				return;
			} 
			
			
			logger.info("Fetched viralDataArray: "+ viralDataArray.length);
			if (viralDataArray != null && viralDataArray.length > 0) {
				ViralData viralData = viralDataArray[0];
				HashMap<String, String> extraInfoMap = viralData.getInfoMap();
				logger.info("extraInfo of viralData :" + viralData.toString());
				boolean keyFound = extraInfoMap.containsKey(selectionString);
				if(keyFound && selectionString.equalsIgnoreCase("x")){
					logger.info("the user seleceted to deactivate from the service running deactivate api:");
					super.processDeactivation(task);
				} else if(keyFound && selectionString.equalsIgnoreCase("y")){
					List<String> alphaList = Arrays.asList(alphabets);
					int alphaStartIndex = alphaList.indexOf(extraInfoMap.get(selectionString));
					logger.info("the next alphabet for more is :" +extraInfoMap.get(selectionString) +" and index is " + alphaStartIndex);
					task.setObject(param_EXTRAINFO, viralData.getInfoMap());
					listAndSendActiveDownloads(alphaStartIndex, task);
				} else if(keyFound){
					logger.debug("the user made the the selection and is processed");
					String clipID = extraInfoMap.get(selectionString);
					logger.debug("selection rbtwavfile :" + extraInfoMap.get(selectionString) + "for selection String :" + selectionString);
					Clip selectionClip = rbtCacheManager.getClip(clipID,language);
					logger.info("the selected clip info that is to be selected is :" + selectionClip);
					task.setObject(CLIP_OBJ, selectionClip);
					SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
					selectionRequest.setClipID(String.valueOf(selectionClip.getClipId()));
					selectionRequest.setMode(SMS);
					rbtClient.deleteSubscriberDownload(selectionRequest);
					String response = selectionRequest.getResponse();
					logger.info("response from deleteSubscriberDownload"
							+ response);
					if (response.equalsIgnoreCase("SUCCESS")) {
						removeViraldata(task);
						//RBT-13403- Deactivation song SMS. 
						String name = selectionClip.getClipName(language);
						String artist = selectionClip.getArtist(language);
						HashMap<String, String> hashMap = new HashMap<String, String>();
						hashMap.put(
								"SMS_TEXT",
								getSMSTextForID(task, DEACTIVATE_SONG_SUCCESS,
										m_deactivationSuccessDefault, language));
						hashMap.put("SONG_NAME", name == null ? "" : name);
						hashMap.put("ARTIST", artist == null ? "" : artist);
						logger.info("SONG_NAME: " + name + " ARTIST: " + artist);
						String smsTextMgmSender = finalizeSmsText(hashMap);
						task.setObject(param_responseSms, smsTextMgmSender);
					}
				} else {
					logger.debug("user sent the invalid reuest string:" + selectionString +" for :" + viralData.toString());
					task.setObject(param_responseSms, getSMSTextForID(task,
							INVALID_USER_REQUEST, m_smsRequestFailureDefault,
							language));
					return;
				}
			} else {
				String sms = getSMSTextForID(task, DCT_MANAGE_SESSION_EXPIRED,
						m_smsRequestFailureDefault, language);
				task.setObject(param_responseSms, sms);
			}
		} catch (Exception e) {
			logger.info("RBT:: processActNSel : " + e.getMessage());
		}
		logger.info("Processed task:" + task);
	}
	
	private String listAndSendActiveDownloads(int accessCount, Task task) {
//		TODO handle if the viral table contains the entries.
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String status = subscriber.getStatus();
		String language = subscriber.getLanguage();
		
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriber.getSubscriberID());
		rbtDetailsRequest.setStatus("" + status);
		Downloads downloads = RBTClient.getInstance().getDownloads(rbtDetailsRequest);
		if(downloads == null || downloads.getDownloads() == null || downloads.getNoOfActiveDownloads() == 0){
			return super.processDeactivation(task);
		}
		
		Download[] downloadsArray = downloads.getDownloads();
		Map<String, String> indexMap =  (task.getObject(param_EXTRAINFO) != null ? (Map<String, String>) task.getObject(param_EXTRAINFO) : new HashMap<String, String>());
		String smsHeaderText = getSMSTextForID(task, DCT_MANAGE_MSSG_HEADER, null, subscriber.getLanguage());
		String clipTextFormat = getSMSTextForID(task, DCT_MANAGE_BASE_TEXT, m_dctManageBaseText, subscriber.getLanguage());
		String clipDeactText = getSMSTextForID(task, DCT_MANAGE_DEACT_TEXT, m_dctManageDeactText, subscriber.getLanguage());
		String clipMoreText = getSMSTextForID(task, DCT_MANAGE_MORE_TEXT, m_dctManageMoreText, subscriber.getLanguage());
		int maxClipsAllowedConf = Integer.valueOf(getParamAsString(param_sms, DCT_MANAGE_REQUEST_MAX_CLIPS_IN_LIST, "3"));
		int maxClipsAllowed = maxClipsAllowedConf;
		//JIRA-OI-907
//		int songMaxChar = Integer.valueOf(getParamAsString(param_sms, DCT_MANAGE_SONG_MAX_CHAR_ALLOWED, "15"));
		int songMaxChar = Integer.valueOf(getParamAsString(param_sms, "SONG_NAME_LENGTH", "15"));
	
		int artistMaxChar = Integer.valueOf(getParamAsString(param_sms, DCT_MANAGE_ARTIST_MAX_CHAR_ALLOWED, "10"));
		String alphabet;
		StringBuilder smsBuilder = new StringBuilder();
		int noOfDownloads = downloadsArray.length;
		for (int i = accessCount; i < noOfDownloads && maxClipsAllowed > 0; i++) {
				Download download = downloadsArray[i];
				logger.info("download is "+download);
				String downloadStatus = download.getDownloadStatus();
				if(!downloadStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE))
					continue;
				
				String downloadWavFile = download.getRbtFile();
				if(downloadWavFile == null)
					continue;
				if(downloadWavFile.endsWith(".wav"))
					downloadWavFile = downloadWavFile.substring(0,downloadWavFile.length()-4);
				Clip downloadClip = rbtCacheManager.getClipByRbtWavFileName(downloadWavFile,language);
				if(downloadClip == null)
					continue;
				String clipName = downloadClip.getClipName();
				if(clipName.length() > songMaxChar) clipName = clipName.substring(0, songMaxChar);
				alphabet = alphabets[accessCount++];
				String clipText = clipTextFormat;
				clipText = clipText.replace("%ALPHABET%", alphabet);
				clipText = clipText.replace("%SONG_NAME%", clipName);
				
				String artistName = downloadClip.getArtist() != null? downloadClip.getArtist() : "";
				if(artistName.length() > artistMaxChar) artistName = artistName.substring(0, artistMaxChar);
				clipText = clipText.replace("%ARTIST_NAME%", artistName);
				String promoID = downloadClip.getClipPromoId() != null? downloadClip.getClipPromoId() : "";
				clipText = clipText.replace("%PROMO_ID%", promoID);
				
				smsBuilder.append(clipText);
				indexMap.put(alphabet, String.valueOf(downloadClip.getClipId()));
				maxClipsAllowed--;
			}
		String finalSMSText = "";
		if(smsHeaderText != null) {
			finalSMSText = smsHeaderText;
		}
		logger.info("noOfDownloads: " + noOfDownloads + ", accessCount: "
				+ accessCount + ", maxClipsAllowedConf: " + maxClipsAllowedConf);
		finalSMSText = finalSMSText + smsBuilder.toString();
		
		int totalPages = 0;
		if((noOfDownloads % maxClipsAllowedConf) == 0) {
			totalPages = noOfDownloads / maxClipsAllowedConf;
		} else {
			totalPages = (noOfDownloads / maxClipsAllowedConf) + 1;
		}
		int currentPage = 0;
		if((accessCount % maxClipsAllowedConf) == 0) {
			currentPage = accessCount / maxClipsAllowedConf;
		} else {
			currentPage = (accessCount / maxClipsAllowedConf) + 1;
		}
		
//		noOfDownloads: 1, accessCount: 1, maxClipsAllowedConf: 2
//		noOfDownloads: 2, accessCount: 2, maxClipsAllowedConf: 2
//		noOfDownloads: 3, accessCount: 2, maxClipsAllowedConf: 2
//		noOfDownloads: 4, accessCount: 2, maxClipsAllowedConf: 2
		if(totalPages == currentPage) {

			clipDeactText = clipDeactText.replace("%ALPHABET%", "x");
			finalSMSText = finalSMSText + clipDeactText;
			indexMap.remove("y");
			indexMap.put("x", "x");
			logger.info("Appened x (deactivation) keyword: " + indexMap);
		} else {

			clipMoreText = clipMoreText.replace("%ALPHABET%", "y");
			finalSMSText = finalSMSText + clipMoreText;
			indexMap.put("y", alphabets[accessCount]);
			logger.info("Appened y (more) keyword: " + indexMap);
		}

//		if(noOfDownloads >= accessCount){
//			if(maxClipsAllowedConf > accessCount) {
//				clipMoreText = clipMoreText.replace("%ALPHABET%", "y");
//				finalSMSText = finalSMSText + clipMoreText;
//				indexMap.put("y", alphabets[accessCount]);
//				logger.info("Appened y (more) keyword: " + indexMap);
//			} else {
//				clipDeactText = clipDeactText.replace("%ALPHABET%", "x");
//				finalSMSText = finalSMSText + clipDeactText;
//				indexMap.remove("y");
//				indexMap.put("x", "x");
//				logger.info("Appened x (deactivation) keyword: " + indexMap);
//			}
//		} else {
//			clipDeactText = clipDeactText.replace("%ALPHABET%", "x");
//			finalSMSText = finalSMSText + clipDeactText;
//			indexMap.remove("y");
//			indexMap.put("x", "x");
//			logger.info("Appened x (deactivation) keyword: " + indexMap);
//		}
		
		task.setObject(param_sms_type, "SMS_DCT_MANAGE");
		task.setObject(param_responseSms, finalSMSText);
		task.setObject(param_info, WebServiceConstants.VIRAL_DATA);//TODO it may be needed or not check it
		task.setObject(param_SEARCHCOUNT, String.valueOf(accessCount));
		removeViraldata(subscriber.getSubscriberID(), "SMS_DCT_MANAGE");
		task.setObject(param_EXTRAINFO, DBUtility.getAttributeXMLFromMap(indexMap));
		logger.info("Inserted into viral sms table. ExtraInfoXml: "
				+ DBUtility.getAttributeXMLFromMap(indexMap)
				+ ", finalSMSText: " + finalSMSText + "indexMap size :"
				+ indexMap.size());
		addViraldata(task);
		return "success";
	}
}
