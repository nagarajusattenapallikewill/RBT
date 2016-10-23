package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.GCMRegistration;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ObjectGsonUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NotificationDetails;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

public class GetMobileAppNotification implements WebServiceAction, WebServiceConstants{

	private static final Logger logger = Logger.getLogger(GetMobileAppNotification.class);
	private String limitCount = "2000";
	
	
//	private Sender sender = null;
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;
		GCMRegistration[] gcmRegistrations = null;
		String smsText = null;
		limitCount = CacheManagerUtil.getParametersCacheManager().getParameterValue("MOBILE_APP", "GET_REGISTRATION_LIMIT_COUNT", "2000");
		try {

			smsText = webServiceContext.getString(param_smsText);
			String type = webServiceContext.getString(param_type);
			String clipIds = webServiceContext.getString(param_clipID);
			String catId = webServiceContext.getString(param_categoryID);
			String title = webServiceContext.getString(param_title);
			String lang = webServiceContext.getString(param_language);
			String pageNo = webServiceContext.getString(param_pageNo);
			int iPageNo = -1;
			if(pageNo != null) {
				try{
					iPageNo = Integer.parseInt(pageNo);
				}
				catch(NumberFormatException nfe) {}
			}
			
//			List<NotificationDetails> notificationDetailsList = new ArrayList<NotificationDetails>();
//			NotificationDetails notificationDetails = new NotificationDetails();
//			Category categoryObj = null;
//			if(catId != null) {
//			   categoryObj = RBTCacheManager.getInstance().getCategory(Integer.valueOf(catId));
//			}
//			if(type != null && clipIds != null) {
//				if(clipIds.contains(",")) {
//					String[] clipIdsArr = clipIds.split(",");
//			        Clip[] clips = RBTCacheManager.getInstance().getClips(clipIdsArr);
//			        String content = SendMobileAppNotifications.getClipJSONObj(clips, type, title, categoryObj, lang);
//			        notificationDetails.setContent(content);
//			        notificationDetailsList.add(notificationDetails);
//			        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
//				} else {
//					Clip[] clips = new Clip[1];
//					clips[0] = RBTCacheManager.getInstance().getClip(clipIds);
//					String content = SendMobileAppNotifications.getClipJSONObj(clips, type, title, categoryObj, lang);
//					notificationDetails.setContent(content);
//			        notificationDetailsList.add(notificationDetails);
//			        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
//				}
//			} else if(type != null && catId != null) {
//				String content = SendMobileAppNotifications.getCategoryJSONObj(type, title, categoryObj);
//				notificationDetails.setContent(content);
//		        notificationDetailsList.add(notificationDetails);
//		        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
//			}
//			logger.info("notificationClipList:"+ smsText);
//			if (smsText == null) {
//				return getWebServiceResponse(INVALID_PARAMETER);
//			}

//			if (sender == null) {
//				String apiKey = RBTParametersUtils.getParamAsString("COMMON", "MOBILEAPP_API_KEY", null);
//				if (apiKey == null) {
//					logger.info("MOBILEAPP_API_KEY parameter not configured.");
//					return getWebServiceResponse("invalid_api_key");
//				}
//				sender = new Sender(apiKey);
//			}

			if(iPageNo == -1) {
				logger.info("os_Type"+webServiceContext.getString(param_os_Type));
				gcmRegistrations = RBTDBManager.getInstance().getAllGCMRegistrations(webServiceContext.getString(param_os_Type));
			} else {
				int offset = 0;
				int rowCount = Integer.parseInt(limitCount);
				if(iPageNo == 1 || iPageNo == 0) {
					offset = 0;
				}
				else {
					offset = (iPageNo - 1) * rowCount;
//					rowCount = iPageNo * rowCount;
				}
				logger.info("offset"+offset+"rowCount"+rowCount+"os_Type"+webServiceContext.getString(param_os_Type));
				gcmRegistrations = RBTDBManager.getInstance().getAllGCMRegistrations(offset, rowCount, webServiceContext.getString(param_os_Type));
			}
			if (gcmRegistrations == null || gcmRegistrations.length == 0) {
				logger.info("No registrations for sending notifications.");
				return getWebServiceResponse("no_registrations");
			}

			response = SUCCESS;
		} catch (Exception e) {
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response, gcmRegistrations, smsText);

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
	
	/**
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceResponse(String response, GCMRegistration[] gcmRegistrations, String smsText) {
		Document document = Utility.getMobileAppNotificationDocument(response, gcmRegistrations, smsText);		
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}

}
