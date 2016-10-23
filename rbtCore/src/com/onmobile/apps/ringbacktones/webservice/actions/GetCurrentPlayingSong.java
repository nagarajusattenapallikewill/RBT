package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;
/**
 * RBT-13660.
 * <br>Real time tone playing information to be shown
 * @author rony.gregory
 *
 */
public class GetCurrentPlayingSong implements WebServiceAction, WebServiceConstants{

	private static final Logger logger = Logger.getLogger(GetCurrentPlayingSong.class);
	private static final String NO_RETRY_CODE = "204";
	private static final String RETRY_CODE = "449";	

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		WebServiceResponse response = null;

		String subcriberId = webServiceContext.getString(param_subscriberID);
		String callerId = webServiceContext.getString(param_callerID);
		String caller = null;
		//RBT-14624 Signal app - RBT tone play notification feature
		String calltype = webServiceContext.getString(param_info);
		logger.info("Parameters:- " + param_subscriberID + ": " + subcriberId + ", "+ param_callerID +": " + callerId + ", "+ param_info +": " + calltype);
		
		//RBT-15205 Throwing NullPointerException while hitting getCurrentPlayingSong request without callType
		if((subcriberId == null || subcriberId.trim().equals("")) || 
				(calltype == null || calltype.trim().equals(""))){
			logger.info("Returning due to wrong parameters.. subcriberId: "+subcriberId +" or calltype: "+calltype);
			return getWebServiceRetryResponse(ERROR, NO_RETRY_CODE);
		}
		
		if(calltype.equalsIgnoreCase("incoming")){
			calltype = "calledId";
		}else if(calltype.equalsIgnoreCase("outgoing")){
			calltype = "callerId";
		}
		
		String key = getKeyForCurrentPlayingSong(subcriberId, subcriberId, calltype);
		logger.debug("key: " + key);		

		CurrentPlayingSongBean currentSong = (CurrentPlayingSongBean) MemcacheClientForCurrentPlayingSong
				.getInstance().getMemcache().get(key);
		logger.info("currentSong: " + currentSong);
		if (currentSong != null) {
			if(calltype.equalsIgnoreCase("calledId")){
				caller = currentSong.getCallerId();
			}else if(calltype.equalsIgnoreCase("callerId")){
				caller = currentSong.getCalledId();
			}
			logger.info("caller :: " + caller);
			String wavFileName = currentSong.getWavFileName();
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName);
			List<String> configuredCategory = Arrays.asList((RBTParametersUtils
					.getParamAsString("MOBILEAPP",
							"REALTIME_SONG_NOTIFICATION_CONTENT_TYPES",
							"AZAAN,COPTIC,DUA,CRICKET,FOOTBALL,ADRBT,UGC"))
					.toLowerCase().split(","));
			Pattern	pattern = Pattern.compile("rbt_[0-9]+_[0-9]+_rbt");
			Matcher matcher = pattern.matcher(wavFileName);
			if (clip != null) {
				if (clip.getClipInfo() != null
						&& clip.getClipInfo().indexOf("ADRBT") != -1) {
					response = getWebServiceSuccessResponse("ADRBT",-1, SUCCESS,caller);
				} else {
					response = getWebServiceSuccessResponse(
							String.valueOf(currentSong.getWavFileName()),currentSong.getCategoryId(), SUCCESS,caller);
					logger.info("clipId: " + clip.getClipId());
				}
			} else if (configuredCategory.contains(wavFileName.toLowerCase())) {
				response = getWebServiceSuccessResponse(wavFileName,-1, SUCCESS,caller);
			} else if (matcher.matches()) {
				response = getWebServiceSuccessResponse("UGC",-1, SUCCESS,caller);
			}
		}
		if (response ==  null) {
			logger.info("Clip not found.");
			
			//RBT-14624 Signal app - RBT tone play notification feature
			String msisdn = subcriberId;
			if(calltype.equals("callerId")){
				msisdn = callerId;
			}
			
			RBTLoginUser user = Utility.getRBTLoginUserBasedOnAppName(msisdn, null);
			if (user == null) {
				logger.info("Subscriber not present in rbt_login_user table or appName config is missing. subscriberId: "
						+ subcriberId
						+ ". No retry. Status code: "
						+ NO_RETRY_CODE + "(NO_RETRY_CODE)");
				response = getWebServiceRetryResponse(ERROR, NO_RETRY_CODE);
			} else {
				logger.info("Subscriber is a signal user. msisdn: " + msisdn + ". To be retried. Status code: " + RETRY_CODE + "(RETRY_CODE)");
				response = getWebServiceRetryResponse(ERROR, RETRY_CODE);
			}
		}
		logger.info(param_subscriberID + ": " + subcriberId + ", "+ param_callerID +": " + callerId + ", response: " + response);
		return response;
	}

	/**
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceRetryResponse(String response, String responseCode) {
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();

		Element retryElem = document.createElement("RETRY");
		element.appendChild(retryElem);
		Text text = document.createTextNode(responseCode);
		retryElem.appendChild(text);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}

	/**
	 * @param clipId
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceSuccessResponse(String wavFileName, int catId, String response, String callerId) {
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();

		Element wavFileElem = document.createElement(WAV_FILE);
		element.appendChild(wavFileElem);
		Text text = document.createTextNode(wavFileName);
		wavFileElem.appendChild(text);
		
		Element catIdElem = document.createElement(CATEGORY_ID);
		element.appendChild(catIdElem);
		Text text1 = document.createTextNode(String.valueOf(catId));
		catIdElem.appendChild(text1);
		if(callerId != null){
			Element callerIdElem = document.createElement(CALLER_ID);
			element.appendChild(callerIdElem);
			Text callerIdText = document.createTextNode(String.valueOf(callerId));
			callerIdElem.appendChild(callerIdText);
		}
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}

	//RBT-14624	Signal app - RBT tone play notification feature
	public static String getKeyForCurrentPlayingSong(String calledId, String callerId , String type) {
		if(type.equals("callerId")){
			return "callerId_"+callerId;
		}else{
			return "calledId_"+calledId;
		}
		
	}
}