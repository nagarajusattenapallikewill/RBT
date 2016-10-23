package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.onmobile.apps.ringbacktones.content.GCMRegistration;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ObjectGsonUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NotificationBean;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NotificationDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public class SendMobileAppNotifications implements WebServiceAction, WebServiceConstants
{
	private static final Logger logger = Logger.getLogger(SendMobileAppNotifications.class);

	private Sender sender = null;
	private static Executor threadPool = Executors.newFixedThreadPool(5);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;
		try {

			String smsText = webServiceContext.getString(param_smsText);
			String type = webServiceContext.getString(param_type);
			String clipIds = webServiceContext.getString(param_clipIds);
			String catId = webServiceContext.getString(param_catId);
			String title = webServiceContext.getString(param_title);
			String lang = webServiceContext.getString(param_language);
			List<NotificationDetails> notificationDetailsList = new ArrayList<NotificationDetails>();
			NotificationDetails notificationDetails = new NotificationDetails();
			Category categoryObj = null;
			if(catId != null) {
			   categoryObj = RBTCacheManager.getInstance().getCategory(Integer.valueOf(catId));
			}
			if(type != null && clipIds != null) {
				if(clipIds.contains(",")) {
					String[] clipIdsArr = clipIds.split(",");
			        Clip[] clips = RBTCacheManager.getInstance().getClips(clipIdsArr);
			        String content = getClipJSONObj(clips, type, title, categoryObj, lang);
			        notificationDetails.setContent(content);
			        notificationDetailsList.add(notificationDetails);
			        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
				} else {
					Clip[] clips = new Clip[1];
					clips[0] = RBTCacheManager.getInstance().getClip(clipIds);
					String content = getClipJSONObj(clips, type, title, categoryObj, lang);
					notificationDetails.setContent(content);
			        notificationDetailsList.add(notificationDetails);
			        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
				}
			} else if(type != null && catId != null) {
				String content = getCategoryJSONObj(type, title, categoryObj);
				notificationDetails.setContent(content);
		        notificationDetailsList.add(notificationDetails);
		        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
			}
			logger.info("notificationClipList:"+ smsText);
			if (smsText == null) {
				return getWebServiceResponse(INVALID_PARAMETER);
			}

			if (sender == null) {
				Parameter parameter = getParameter("COMMON", "MOBILEAPP_API_KEY");				
				String apiKey = null;

				if(parameter != null) {
					apiKey = parameter.getValue();	
				}
				if (apiKey == null) {
					logger.info("MOBILEAPP_API_KEY parameter not configured.");
					return getWebServiceResponse("invalid_api_key");
				}
				parameter = getParameter("MOBILEAPP", "MOBILEAPP_GCM_PROXY_ENABLED");
				boolean isProxyEnabled = false;
				if (parameter != null) {
					isProxyEnabled = Boolean.valueOf(parameter.getValue());	
				}
				if (isProxyEnabled) {
					logger.info("MOBILEAPP_GCM_PROXY_ENABLED is true");
					String proxyIP = null;
					Integer proxyPort = null;
					parameter = getParameter("MOBILEAPP","MOBILEAPP_GCM_PROXY_IP");
					if (parameter != null) {
						proxyIP = parameter.getValue();
					}
					parameter = getParameter("MOBILEAPP", "MOBILEAPP_GCM_PROXY_PORT");
					if (parameter != null) {
						try {
							proxyPort = Integer.valueOf(parameter.getValue());
						} catch (NumberFormatException e) {
							logger.info(e,e);
						}
					}
					logger.info("proxy IP: " + proxyIP + ", proxy port: " + proxyPort);
					if (proxyIP != null && proxyPort != null) {
						sender = new ProxySender(apiKey, proxyIP, proxyPort);
					} else {
						logger.info("Proxy IP and/or Port not properly configured");	
					}
				} else {
					logger.info("MOBILEAPP_GCM_PROXY_ENABLED is false or not confgured");
				}
				if (sender == null) {
					sender = new Sender(apiKey);
				}
			}

			GCMRegistration[] gcmRegistrations = RBTDBManager.getInstance().getAllGCMRegistrations();
			if (gcmRegistrations == null || gcmRegistrations.length == 0) {
				logger.info("No registrations for sending notifications.");
				return getWebServiceResponse("no_registrations");
			}

			if (gcmRegistrations.length == 1)
			{
				// send a single message using plain post
				String registrationId = gcmRegistrations[0].registrationID();
				Message message = new Message.Builder().addData("message", smsText).build();
				Result result = sender.send(message, registrationId, 5);
				logger.info("Sent message to one device: " + result);
			}
			else
			{
				List<String> partialDevices = new ArrayList<String>();
				int counter = 0;
				int total = gcmRegistrations.length;
				for (GCMRegistration gcmRegistration : gcmRegistrations)
				{
					counter++;
					partialDevices.add(gcmRegistration.registrationID());
					int partialSize = partialDevices.size();
					if (partialSize == 1000 || counter == total) {
						asyncSend(partialDevices, smsText);
						partialDevices.clear();
					}
				}
			}

			response = SUCCESS;
		} catch (Exception e) {
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response);
	}

	public static String getClipJSONObj(Clip[] clips, String type, String title, Category categoryObj, String lang) {
		int len = clips.length;
		int catId = 0;
		String catName = "";
		if(categoryObj != null) {
		  catId = categoryObj.getCategoryId();
		  catName = categoryObj.getCategoryName();
		}
		
		List<NotificationBean> notificationClipList =  new ArrayList<NotificationBean>();
		for(int i=0; i<len; i++) {
			NotificationBean notificationClip = new NotificationBean(type, 
					 title, clips[i].getClipId(), clips[i].getClipName(), clips[i].getAlbum(), clips[i].getArtist(), 
					 clips[i].getClipInfo(Clip.ClipInfoKeys.IMG_URL), clips[i].getContentType(), catId, catName);
			notificationClipList.add(notificationClip);
		}
		logger.info("notificationClipList:"+ notificationClipList);
		return (ObjectGsonUtils.objectToGson(notificationClipList));
	}
	
	public static String getCategoryJSONObj(String type, String title, Category categoryObj) {
		List<NotificationBean> notificationClipList =  new ArrayList<NotificationBean>();
			NotificationBean notificationClip = new NotificationBean(type, 
					 title,  categoryObj.getCategoryId(), categoryObj.getCategoryName());
			notificationClipList.add(notificationClip);
		logger.info("notificationClipList:"+ notificationClipList);
		return (ObjectGsonUtils.objectToGson(notificationClipList));
	}
	private void asyncSend(List<String> partialDevices, final String smsText) {

		try {
			// make a copy
			final List<String> devices = new ArrayList<String>(partialDevices);
			threadPool.execute(new Runnable() {

				public void run() {
					Message message = new Message.Builder().addData("message", smsText).build();
					MulticastResult multicastResult;
					try {
						multicastResult = sender.send(message, devices, 5);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
						return;
					}

					logger.debug("MulticastResult : " + multicastResult);

	/*
	 * Analyze the results
	 */
	//				List<Result> results = multicastResult.getResults();
	//				// analyze the results
	//				for (int i = 0; i < devices.size(); i++) {
	//					String regId = devices.get(i);
	//					Result result = results.get(i);
	//					String messageId = result.getMessageId();
	//					if (messageId != null) {
	//						logger.info("Succesfully sent message to device: "
	//								+ regId + "; messageId = " + messageId);
	//						String canonicalRegId = result
	//								.getCanonicalRegistrationId();
	//						if (canonicalRegId != null) {
	//							// same device has more than on registration id:
	//							// update it
	//							logger.info("canonicalRegId " + canonicalRegId);
	//							Datastore.updateRegistration(regId, canonicalRegId);
	//						}
	//					} else {
	//						String error = result.getErrorCodeName();
	//						if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
	//							// application has been removed from device -
	//							// unregister it
	//							logger.info("Unregistered device: " + regId);
	//							reg(regId);
	//						} else {
	//							logger.info("Error sending message to " + regId
	//									+ ": " + error);
	//						}
	//					}
	//				}
				}
			});
		}
		catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}

	/**
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceResponse(String response) {
		Document document = Utility.getResponseDocument(response);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
	
	private Parameter getParameter(String type, String name) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType(type);
		applicationDetailsRequest.setName(name);
		return RBTClient.getInstance().getParameter(applicationDetailsRequest);
	}
}
