package com.onmobile.mobileapps.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.interfaces.SubscriberResponse;
import com.onmobile.android.utils.AESUtils;
import com.onmobile.android.utils.StringConstants;

public class SelectionAction extends DispatchAction{


	public static Logger logger = Logger.getLogger(SelectionAction.class);
	private static final Pattern timePattern = Pattern.compile("^([0-1]?[0-9]|2[0-3]):([0-5]?[0-9])$");
	private static final SimpleDateFormat sdfInput = new SimpleDateFormat("dd/MM/yyyy");
	// Do not pass year component so that it repeats for every year
	private static final SimpleDateFormat sdfOutput = new SimpleDateFormat("ddMM");

	private String PARAM_CLIP_ID = "clipId";
	private String PARAM_CATEGORY_ID = "categoryId";
	private String  PARAM_CALLER_ID= "caller";
	private String  PARAM_SUBSCRIBER_ID= "subscriberId";
	private String SET_TIME_OF_THE_DAY = "setTimeOfTheDay";
	private String START_TIME = "starttime";
	private String END_TIME = "endtime";
	/*private String START_DATE = "startdate";
	private String END_DATE = "enddate";*/
	private String SET_FUTURE_DAY_REQUEST = "setDayReq";
	private String FUTURE_DAY = "futureDay";
	private String SET_DAY_OF_WEEK_REQUEST = "setDowReq";
	private String DAY_OF_WEEK = "dayOfWeek";
	private String MUSIC_PACK = "isMusicPack";
	private String PROFILE_PACK = "isProfilePack";
	private String PARAM_MODE = "mode";
	private String PARAM_IN_LOOP = "in_loop";
	private String PARAM_PROFILE_HOURS = "profileHours";
	
	//RBT-14626
	private String PARAM_CALLTYPE= "callType";
	private String PARAM_PAGE_SIZE= "pageSize";
	private String PARAM_OFFSET= "offset";
	private String PARAM_ADD_CATEGORY_INFO= "addCatInfo";
	
	//	private String  = "";


	public ActionForward addSelection(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SelectionAction.addSelection");

		String s1=null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String caller=request.getParameter(PARAM_CALLER_ID);
		if (caller != null)
			caller = caller.replace("+", "");

		String clipId=request.getParameter(PARAM_CLIP_ID);
		String categoryId=request.getParameter(PARAM_CATEGORY_ID);
		String isMusicPack = request.getParameter(MUSIC_PACK);
		String mode = request.getParameter(PARAM_MODE);
		String inLoop =  request.getParameter(PARAM_IN_LOOP);
		String subscriptionClass = request.getParameter("subscriptionClass");
		String baseOfferId = request.getParameter("baseOfferId");
		String chargeClass = request.getParameter("chargeClass");
		String selOfferId = request.getParameter("selOfferId");
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		//RBT-15120 Added for VF Spain
		String smOfferType = request.getParameter("smOfferType");
		String udsOptIn = request.getParameter("UDS_OPTIN");
		boolean isUdsOptIn = false ;
		if(udsOptIn != null && !udsOptIn.isEmpty()){
			isUdsOptIn = Boolean.parseBoolean(udsOptIn);
		}
		
		if (chargeClass == null) {
			logger.debug("About to obtain chargeClass from config.");
			chargeClass = PropertyConfigurator.getDefaultChargeClass("DEFAULT");
			if (chargeClass.equalsIgnoreCase("null")) {
				chargeClass = null;
			}
		}
		logger.info("subId: " + subId + ", caller: " + caller + ", clipId: " + clipId + ", categoryId: " + categoryId + ", isMusicPack: " + isMusicPack
				+ ", mode: " + mode + ", inLoop: " + inLoop + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
				+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", useUIChargeClass: " + useUIChargeClass+", smOfferType: "+smOfferType);
		
		// RBT-6497:-Handset Client- First song download via app for free
		if(null != mode && mode.trim().length()>0) {
			s1 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).setSubscriberSelection(subId, caller, 
					clipId, categoryId, mode, chargeClass, isMusicPack, inLoop, false, subscriptionClass, baseOfferId, selOfferId, useUIChargeClass,smOfferType,null,isUdsOptIn);
		} else {
			s1 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).setSubscriberSelection(subId, caller, 
					clipId, categoryId, ResponseFactory.RBT_CHANNEL, chargeClass, isMusicPack, inLoop, false, subscriptionClass, baseOfferId, selOfferId, useUIChargeClass,smOfferType,null,isUdsOptIn);
		}
		request.setAttribute("response", s1);

		logger.info("response"+s1);
		return mapping.findForward("success"); 
	}
	
	public ActionForward addSelectionForConsent(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SelectionAction.addSelectionForConsent");

		String s1=null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String caller=request.getParameter(PARAM_CALLER_ID);
		if (caller != null)
			caller = caller.replace("+", "");

		String clipId=request.getParameter(PARAM_CLIP_ID);
		String categoryId=request.getParameter(PARAM_CATEGORY_ID);
		String isMusicPack = request.getParameter(MUSIC_PACK);
		String mode = request.getParameter(PARAM_MODE);
		String inLoop =  request.getParameter(PARAM_IN_LOOP);

		String subscriptionClass = request.getParameter("subscriptionClass");
		String baseOfferId = request.getParameter("baseOfferId");
		String chargeClass = request.getParameter("chargeClass");
		String selOfferId = request.getParameter("selOfferId");
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		//RBT-15120 Added for VF Spain
		String smOfferType = request.getParameter("smOfferType");
		String circleID =  request.getParameter("circleID");
		String udsOptIn = request.getParameter("UDS_OPTIN");
		boolean isUdsOptIn = false ;
		if(udsOptIn != null && !udsOptIn.isEmpty()){
			isUdsOptIn = Boolean.parseBoolean(udsOptIn);
		}
		if (chargeClass == null) {
			logger.debug("About to obtain chargeClass from config.");
			chargeClass = PropertyConfigurator.getDefaultChargeClass("DEFAULT");
			if (chargeClass.equalsIgnoreCase("null")) {
				chargeClass = null;
			}
		}
		logger.info("subId: " + subId + ", caller: " + caller + ", clipId: " + clipId + ", categoryId: " + categoryId + ", isMusicPack: " + isMusicPack
				+ ", mode: " + mode + ", inLoop: " + inLoop + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
				+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", useUIChargeClass: " + useUIChargeClass+", smOfferType: "+smOfferType +", circleID: "+circleID 
				+ ", isUdsOptIn: " + isUdsOptIn);
		// RBT-6497:-Handset Client- First song download via app for free
		if(null != mode && mode.trim().length()>0) {
			s1 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).setSubscriberSelection(subId, caller, 
					clipId, categoryId, mode, chargeClass, isMusicPack, inLoop, true, subscriptionClass, baseOfferId, selOfferId, useUIChargeClass,smOfferType,circleID,isUdsOptIn);
		} else {
			s1 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).setSubscriberSelection(subId, caller, 
					clipId, categoryId, ResponseFactory.RBT_CHANNEL, chargeClass, isMusicPack, inLoop, true, subscriptionClass, baseOfferId, selOfferId, useUIChargeClass,smOfferType,circleID,isUdsOptIn);
		}
		request.setAttribute("response", s1);

		logger.info("response"+s1);
		return mapping.findForward("success"); 
	}
	
	public ActionForward getSelectionAmount(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");

		String s1=null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String clipId = request.getParameter(PARAM_CLIP_ID);
		if (clipId != null && PropertyConfigurator.isEncryptionEnabled()) {
			String encryptedSubscriberId = AESUtils.encrypt(subId, PropertyConfigurator.getRequestSubscriberIdEncryptionKey());
			String trimFileName = null;
			if (clipId.indexOf(encryptedSubscriberId) != -1) {
				trimFileName = clipId.substring(encryptedSubscriberId.length());
				logger.debug("trimFileName: " + trimFileName);
				clipId = subId + trimFileName;
			}
		}
		String categoryId = request.getParameter(PARAM_CATEGORY_ID);
		String isMusicPack = request.getParameter(MUSIC_PACK);
		String isProfilePack = request.getParameter(PROFILE_PACK);

		String chargeClass = request.getParameter("chargeClass");
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		
		if (chargeClass == null) {
			logger.debug("About to obtain chargeClass from config.");
			chargeClass = PropertyConfigurator.getDefaultChargeClass("DEFAULT");
			if (chargeClass.equalsIgnoreCase("null")) {
				chargeClass = null;
			}
		}

		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		
		s1 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode).getSelectionAmount(
				subId, clipId, categoryId, mode, chargeClass, isMusicPack,
				isProfilePack, useUIChargeClass);

		request.setAttribute("response", s1);

		logger.info("response"+s1);
		return mapping.findForward("success"); 
	}

	public ActionForward getGiftAmount(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws OMAndroidException {
		
		logger.info("Inside action");

		String s1 = null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String clipId = request.getParameter(PARAM_CLIP_ID);
		String categoryId = request.getParameter(PARAM_CATEGORY_ID);
		// String chargeClass=request.getParameter("class");
		String chargeClass = "DEFAULT";
		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		s1 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.getGiftAmount(subId, clipId, categoryId,
						mode, chargeClass);

		request.setAttribute("response", s1);

		logger.info("response" + s1);
		return mapping.findForward("success");
	}

	public ActionForward addSelectionToMusicPack(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		logger.info("Inside SelectionAction.addSelectionToMusicPack");

		String s1=null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String caller=request.getParameter(PARAM_CALLER_ID);
		if (caller != null)
			caller = caller.replace("+", "");

		String clipId=request.getParameter(PARAM_CLIP_ID);
		String categoryId=request.getParameter(PARAM_CATEGORY_ID);
		String isMusicPack = request.getParameter(MUSIC_PACK);
		String mode = request.getParameter(PARAM_MODE);
		if (mode == null || mode.trim().length() == 0) {
			mode = ResponseFactory.RBT_CHANNEL;
		}
		
		String subscriptionClass = request.getParameter("subscriptionClass");
		String baseOfferId = request.getParameter("baseOfferId");
		String chargeClass = request.getParameter("chargeClass");
		String selOfferId = request.getParameter("selOfferId");
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		//RBT-15120 Added for VF Spain
		String smOfferType = request.getParameter("smOfferType");
		
		if (chargeClass == null) {
			chargeClass = "DEFAULT";
		}
		String udsOptIn = request.getParameter("UDS_OPTIN");
		boolean isUdsOptIn = false ;
		if(udsOptIn != null && !udsOptIn.isEmpty()){
			isUdsOptIn = Boolean.parseBoolean(udsOptIn);
		}
		
		logger.info("subId: " + subId + ", caller: " + caller + ", clipId: " + clipId + ", categoryId: " + categoryId + ", isMusicPack: " + isMusicPack
				+ ", mode: " + mode + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
				+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", useUIChargeClass: " + useUIChargeClass+", smOfferType: "+smOfferType);
		s1=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).setSubscriberSelection(subId, caller, 
				clipId, categoryId, mode, chargeClass, isMusicPack, null, false, subscriptionClass, baseOfferId, selOfferId, useUIChargeClass, smOfferType, null,isUdsOptIn);

		request.setAttribute("response", s1);

		logger.info("response"+s1);
		return mapping.findForward("success"); 
	}

	public ActionForward getSelections(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		boolean isDownloadsMerged = true;
		String paramIsDownloadsMerged = request.getParameter("isDownloadsMerged");
		if (paramIsDownloadsMerged != null && paramIsDownloadsMerged.equalsIgnoreCase("FALSE")) {
			logger.debug("Parameter isDownloadsMerged is false.");
			isDownloadsMerged = false;
		}
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName") ;
		//RBT_AT-103588
		String circleId = request.getParameter("circleID");
		logger.info("subscriberId: " + subId + ", isDownloadsMerged: " + paramIsDownloadsMerged+", browsingLanguage:"+browsingLanguage+", appName:"+appName);
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getListSelections(subId, isDownloadsMerged,browsingLanguage ,appName, circleId);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 
	}
	
	public ActionForward getProfileSelections(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getListProfileSelections(subId);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 
	}

	public ActionForward removeSelection(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String clipId = request.getParameter("clipId");
		String caller = request.getParameter("caller");
		String rbtWavFile = request.getParameter("rbtWavFile");
		String selStartTime = request.getParameter("selectionStartTime");
		String selEndTime = request.getParameter("selectionEndTime");
		//RBT_AT-103588
		String circleId = request.getParameter("circleID");
		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		
		String s2 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.removeSubscriberSelection(subId, clipId, caller,
						mode, selStartTime, selEndTime,
						rbtWavFile, circleId);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 
	}

	public ActionForward setPersonalizedSongSelection(ActionMapping map, ActionForm form, HttpServletRequest request, HttpServletResponse response)
	throws ServletException {
		logger.info("inside method SelectionAction.setPersonalizedSongSelection");
		
		String s1 = null;
		
		//String fwdTo = "set.song.success";

		String subId = request.getParameter("subscriberId");
		String setReqType = request.getParameter("setReqType");
		String setTimeReqType = request.getParameter("setTimeReqType");
		String clipId = request.getParameter("clipId");
		String catId = request.getParameter("categoryId");
		String gifterId = request.getParameter("gifterId");
		String giftSentTime = request.getParameter("giftSentTime");
		String selectionStartTime =  request.getParameter("selectionStartTime");
		String selectionEndTime =  request.getParameter("selectionEndTime");
		String inLoop = request.getParameter(PARAM_IN_LOOP);
		//RBT-15120 Added for VF Spain
		String smOfferType = request.getParameter("smOfferType");
		
		Integer startTimeHrs = 0;
		Integer startTimeMins = 0;
		Integer endTimeHrs = 23;
		Integer endTimeMins = 59;
		//		String errorMsg = "";
		
		String callerId = "ALL";
		String tmpCallerId = request.getParameter("caller");
		if (tmpCallerId != null && !tmpCallerId.trim().equals("")) {
			callerId = tmpCallerId;
			/*if (callerId.startsWith("G")) {
				fwdTo = "set.group.success";
			} else {
				fwdTo = "set.contact.success";	
			}*/
		}


		Integer status = 1;
		String interval = null;

		if (SET_TIME_OF_THE_DAY.equals(setTimeReqType)) {

			String startTimeIn = request.getParameter(START_TIME);
			String endTimeIn = request.getParameter(END_TIME);

			Matcher matcher = timePattern.matcher(startTimeIn);
			if (!matcher.matches()) {
				logger.error("Invalid start time of the day field -> " + startTimeIn);
			}

			matcher = timePattern.matcher(endTimeIn);
			if (!matcher.matches()) {
				logger.error("Invalid end time of the day field -> " + endTimeIn);
			}

			try {
				String[] startTimeTokens = startTimeIn.split(":");
				startTimeHrs = Integer.parseInt(startTimeTokens[0]);
				startTimeMins = Integer.parseInt(startTimeTokens[1]);
				String[] endTimeTokens = endTimeIn.split(":");
				endTimeHrs = Integer.parseInt(endTimeTokens[0]);
				endTimeMins = Integer.parseInt(endTimeTokens[1]);

				if (endTimeMins == 0)
				{
					endTimeMins = 59;
					endTimeHrs = endTimeHrs - 1;
				}
			} catch (NumberFormatException nme) {
				logger.error("Invalid time of the day field -> start:" + startTimeIn + " end:" + endTimeIn);
			}
			status = status > 80 ? status : 80;
		}

		if (SET_FUTURE_DAY_REQUEST.equals(setReqType)) {

			String futureDay = request.getParameter(FUTURE_DAY);
			Date futureDate = null;

			try {
				futureDate = sdfInput.parse(futureDay);
			} catch (Exception e) {
				logger.error("Invalid date input -> " + futureDay);
			}

			if (futureDate.before(new Date())) {
				logger.error("Invalid date input: Past date -> " + futureDay);
			}
			interval = "Y" + sdfOutput.format(futureDate);
			status = status > 95 ? status : 95;

		} else if (SET_DAY_OF_WEEK_REQUEST.equals(setReqType)) {
			String[] dayOfWeek = request.getParameterValues(DAY_OF_WEEK);
			interval = arrayToString(dayOfWeek);
			status = status > 75 ? status : 75;
			logger.info("Interval : " + interval);
			List<String> intervalList = Arrays.asList(interval.split(","));
			List<String> allDaysList = new ArrayList<String>();
			allDaysList.add("W1");
			allDaysList.add("W2");
			allDaysList.add("W3");
			allDaysList.add("W4");
			allDaysList.add("W5");
			allDaysList.add("W6");
			allDaysList.add("W7");

			if (intervalList.containsAll(allDaysList))
			{
				status = 1;
				interval = null;
			}
		}

		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}

		String modeInfo = mode;
		Integer cosId = null;
		String lang = null;
		
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
		String finalDate = null;
	    try {
	    	if(giftSentTime != null)
	    	{
	    		Date newdate = sdf.parse(giftSentTime);
	    		SimpleDateFormat tosdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	    		finalDate = tosdf.format(newdate);
	    	}
	    	String subscriptionClass = request.getParameter("subscriptionClass");
			String baseOfferId = request.getParameter("baseOfferId");
			String chargeClass = request.getParameter("chargeClass");
			String selOfferId = request.getParameter("selOfferId");
			String useUIChargeClassString = request.getParameter("useUIChargeClass");
			boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
			
			logger.info("subId: " + subId + ", caller: " + callerId + ", clipId: " + clipId + ", categoryId: " + catId + ", gifterId: " + gifterId
					+ ", giftSentTime: " + giftSentTime + ", selectionStartTime: " + selectionStartTime + ", selectionEndTime: " + selectionEndTime
				    + ", mode: " + mode + ", inLoop: " + inLoop + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
					+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", useUIChargeClass: " + useUIChargeClass+ ", smOfferType: "+smOfferType);
			
	    	s1=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).setSubscriberSelection(subId, callerId, clipId, catId, status,true, mode, modeInfo, lang,
					startTimeHrs, startTimeMins, endTimeHrs, endTimeMins, interval, cosId, gifterId, finalDate, selectionStartTime, selectionEndTime, inLoop, false,
					subscriptionClass, baseOfferId, chargeClass, selOfferId, useUIChargeClass,smOfferType);
	    	request.setAttribute("response", s1);
			logger.info("response"+s1);	    	
			return map.findForward("success");

	    } catch (ParseException e) {
				logger.error("",e);
		}
		
		return map.findForward("failure");
	}
	
	public ActionForward setPersonalizedSongSelectionConsent(ActionMapping map, ActionForm form, HttpServletRequest request, HttpServletResponse response)
			throws ServletException {
				logger.info("inside method SelectionAction.setPersonalizedSongSelection");
				
				String s1 = null;
				
				//String fwdTo = "set.song.success";

				String subId = request.getParameter("subscriberId");
				String setReqType = request.getParameter("setReqType");
				String setTimeReqType = request.getParameter("setTimeReqType");
				String clipId = request.getParameter("clipId");
				String catId = request.getParameter("categoryId");
				String gifterId = request.getParameter("gifterId");
				String giftSentTime = request.getParameter("giftSentTime");
				String selectionStartTime =  request.getParameter("selectionStartTime");
				String selectionEndTime =  request.getParameter("selectionEndTime");
				String inLoop = request.getParameter(PARAM_IN_LOOP);
				//RBT-15120 Added for VF Spain
				String smOfferType = request.getParameter("smOfferType");

				Integer startTimeHrs = 0;
				Integer startTimeMins = 0;
				Integer endTimeHrs = 23;
				Integer endTimeMins = 59;
				//		String errorMsg = "";
				
				String callerId = "ALL";
				String tmpCallerId = request.getParameter("caller");
				if (tmpCallerId != null && !tmpCallerId.trim().equals("")) {
					callerId = tmpCallerId;
					/*if (callerId.startsWith("G")) {
						fwdTo = "set.group.success";
					} else {
						fwdTo = "set.contact.success";	
					}*/
				}


				Integer status = 1;
				String interval = null;

				if (SET_TIME_OF_THE_DAY.equals(setTimeReqType)) {

					String startTimeIn = request.getParameter(START_TIME);
					String endTimeIn = request.getParameter(END_TIME);

					Matcher matcher = timePattern.matcher(startTimeIn);
					if (!matcher.matches()) {
						logger.error("Invalid start time of the day field -> " + startTimeIn);
					}

					matcher = timePattern.matcher(endTimeIn);
					if (!matcher.matches()) {
						logger.error("Invalid end time of the day field -> " + endTimeIn);
					}

					try {
						String[] startTimeTokens = startTimeIn.split(":");
						startTimeHrs = Integer.parseInt(startTimeTokens[0]);
						startTimeMins = Integer.parseInt(startTimeTokens[1]);
						String[] endTimeTokens = endTimeIn.split(":");
						endTimeHrs = Integer.parseInt(endTimeTokens[0]);
						endTimeMins = Integer.parseInt(endTimeTokens[1]);

						if (endTimeMins == 0)
						{
							endTimeMins = 59;
							endTimeHrs = endTimeHrs - 1;
						}
					} catch (NumberFormatException nme) {
						logger.error("Invalid time of the day field -> start:" + startTimeIn + " end:" + endTimeIn);
					}
					status = status > 80 ? status : 80;
				}

				if (SET_FUTURE_DAY_REQUEST.equals(setReqType)) {

					String futureDay = request.getParameter(FUTURE_DAY);
					Date futureDate = null;

					try {
						futureDate = sdfInput.parse(futureDay);
					} catch (Exception e) {
						logger.error("Invalid date input -> " + futureDay);
					}

					if (futureDate.before(new Date())) {
						logger.error("Invalid date input: Past date -> " + futureDay);
					}
					interval = "Y" + sdfOutput.format(futureDate);
					status = status > 95 ? status : 95;

				} else if (SET_DAY_OF_WEEK_REQUEST.equals(setReqType)) {
					String[] dayOfWeek = request.getParameterValues(DAY_OF_WEEK);
					interval = arrayToString(dayOfWeek);
					status = status > 75 ? status : 75;
					logger.info("Interval : " + interval);
					List<String> intervalList = Arrays.asList(interval.split(","));
					List<String> allDaysList = new ArrayList<String>();
					allDaysList.add("W1");
					allDaysList.add("W2");
					allDaysList.add("W3");
					allDaysList.add("W4");
					allDaysList.add("W5");
					allDaysList.add("W6");
					allDaysList.add("W7");

					if (intervalList.containsAll(allDaysList))
					{
						status = 1;
						interval = null;
					}
				}

				String mode = request.getParameter(PARAM_MODE);
				logger.info("mode: " + mode);
				if (mode == null || mode.trim().isEmpty()) {
					mode = StringConstants.CHANNEL;
					logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
				}

				String modeInfo = mode;
				Integer cosId = null;
				String lang = null;
				
				SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
				String finalDate = null;
			    try {
			    	if(giftSentTime != null)
			    	{
			    		Date newdate = sdf.parse(giftSentTime);
			    		SimpleDateFormat tosdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			    		finalDate = tosdf.format(newdate);
			    	}
			    	
			    	String subscriptionClass = request.getParameter("subscriptionClass");
					String baseOfferId = request.getParameter("baseOfferId");
					String chargeClass = request.getParameter("chargeClass");
					String selOfferId = request.getParameter("selOfferId");
					String useUIChargeClassString = request.getParameter("useUIChargeClass");
					boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
					
					logger.info("subId: " + subId + ", caller: " + callerId + ", clipId: " + clipId + ", categoryId: " + catId + ", gifterId: " + gifterId
							+ ", giftSentTime: " + giftSentTime + ", selectionStartTime: " + selectionStartTime + ", selectionEndTime: " + selectionEndTime
						    + ", mode: " + mode + ", inLoop: " + inLoop + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
							+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", useUIChargeClass: " + useUIChargeClass+", smOfferType: "+smOfferType);
					
			    	s1=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).setSubscriberSelection(subId, callerId, clipId, catId, status,true, mode, modeInfo, lang,
							startTimeHrs, startTimeMins, endTimeHrs, endTimeMins, interval, cosId, gifterId, finalDate, selectionStartTime, selectionEndTime, inLoop, true,
							subscriptionClass, baseOfferId, chargeClass, selOfferId, useUIChargeClass,smOfferType);
			    	request.setAttribute("response", s1);
					logger.info("response"+s1);	    	
					return map.findForward("success");

			    } catch (ParseException e) {
						logger.error("",e);
				}
				
				return map.findForward("failure");
	}

	private static String arrayToString(String[] input) {
		if (input == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (String str : input) {
			sb.append(str);
			sb.append(",");
		}
		String result = sb.toString();
		if (result.endsWith(",")) {
			result = result.substring(0, result.lastIndexOf(","));
		}
		return result;
	}
	
	public ActionForward addProfileSelection(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");

		String s1=null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String caller=request.getParameter(PARAM_CALLER_ID);
		String clipId=request.getParameter(PARAM_CLIP_ID);
		String categoryId=request.getParameter(PARAM_CATEGORY_ID);
		String selectionStartTime =  request.getParameter("selectionStartTime");
		String selectionEndTime =  request.getParameter("selectionEndTime");
		String rbtWavFile =  request.getParameter("rbtWavFile"); 
		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String subscriptionClass = request.getParameter("subscriptionClass");
		String baseOfferId = request.getParameter("baseOfferId");
		String chargeClass = request.getParameter("chargeClass");
		String selOfferId = request.getParameter("selOfferId");
		String profileHours = request.getParameter(PARAM_PROFILE_HOURS);
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		//RBT-15120 Added for VF Spain
		String smOfferType = request.getParameter("smOfferType");
		
		logger.info("subId: " + subId + ", caller: " + caller + ", clipId: " + clipId + ", categoryId: " + categoryId + ", selectionStartTime: " + selectionStartTime
				+ ", selectionEndTime: " + selectionEndTime + ", rbtWavFile: " + rbtWavFile
				+ ", mode: " + mode + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
				+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", profileHours: " + profileHours + ", useUIChargeClass: " + useUIChargeClass+", smOfferType: "+smOfferType);
		
		s1 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.setSubscriberProfileSelection(subId, caller, clipId,
						categoryId, mode,
						selectionStartTime, selectionEndTime, rbtWavFile, false,
						subscriptionClass, baseOfferId, chargeClass, selOfferId, profileHours, useUIChargeClass,smOfferType);

		request.setAttribute("response", s1);

		logger.info("response"+s1);
		return mapping.findForward("success"); 
	}
	
	public ActionForward addProfileSelectionConsent(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");

		String s1=null;

		String subId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String caller=request.getParameter(PARAM_CALLER_ID);
		String clipId=request.getParameter(PARAM_CLIP_ID);
		String categoryId=request.getParameter(PARAM_CATEGORY_ID);
		String selectionStartTime =  request.getParameter("selectionStartTime");
		String selectionEndTime =  request.getParameter("selectionEndTime");
		String rbtWavFile =  request.getParameter("rbtWavFile"); 
		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String subscriptionClass = request.getParameter("subscriptionClass");
		String baseOfferId = request.getParameter("baseOfferId");
		String chargeClass = request.getParameter("chargeClass");
		String selOfferId = request.getParameter("selOfferId");
		String profileHours = request.getParameter(PARAM_PROFILE_HOURS);
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		//RBT-15120 Added for VF Spain
		String smOfferType = request.getParameter("smOfferType");
		
		logger.info("subId: " + subId + ", caller: " + caller + ", clipId: " + clipId + ", categoryId: " + categoryId + ", selectionStartTime: " + selectionStartTime
				+ ", selectionEndTime: " + selectionEndTime + ", rbtWavFile: " + rbtWavFile
				+ ", mode: " + mode + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId
				+ ", chargeClass: " + chargeClass + ", selOfferId: " + selOfferId + ", profileHours: " + profileHours + ", useUIChargeClass: " + useUIChargeClass);
		
		s1 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.setSubscriberProfileSelection(subId, caller, clipId,
						categoryId, mode,
						selectionStartTime, selectionEndTime, rbtWavFile, true,
						subscriptionClass, baseOfferId, chargeClass, selOfferId, profileHours, useUIChargeClass,smOfferType);

		request.setAttribute("response", s1);

		logger.info("response"+s1);
		return mapping.findForward("success"); 
	}

	public ActionForward getSelectionDetails(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SelectionAction.getSelectionDetails");

		String s1=null;

		String subscriberId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String clipId = request.getParameter(PARAM_CLIP_ID);
		if (clipId != null && PropertyConfigurator.isEncryptionEnabled()) {
			String encryptedSubscriberId = AESUtils.encrypt(subscriberId, PropertyConfigurator.getRequestSubscriberIdEncryptionKey());
			String trimFileName = null;
			if (clipId.indexOf(encryptedSubscriberId) != -1) {
				trimFileName = clipId.substring(encryptedSubscriberId.length());
				logger.debug("trimFileName: " + trimFileName);
				clipId = subscriberId + trimFileName;
			}
		}
		String categoryId = request.getParameter(PARAM_CATEGORY_ID);
		String isMusicPack = request.getParameter(MUSIC_PACK);
		String isProfilePack = request.getParameter(PROFILE_PACK);
		String chargeClass = request.getParameter("chargeClass");
		String useUIChargeClassString = request.getParameter("useUIChargeClass");
		boolean useUIChargeClass = Boolean.valueOf(useUIChargeClassString);
		
		if (chargeClass == null) {
			logger.debug("About to obtain chargeClass from config.");
			chargeClass = PropertyConfigurator.getDefaultChargeClass("DEFAULT");
			if (chargeClass.equalsIgnoreCase("null")) {
				chargeClass = null;
			}
		}

		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		
		s1 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode).getSelectionDetails(
				subscriberId, clipId, categoryId, mode, chargeClass,
				isMusicPack, isProfilePack, useUIChargeClass);

		request.setAttribute("response", s1);

		logger.info("getSelectionDetails response: "+s1);
		return mapping.findForward("success"); 
	}
	
	public ActionForward getCurrentPlayingSong(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SelectionAction.getCurrentPlayingSong");
		String subscriberId = request.getParameter("subscriberId");
		String callerId = request.getParameter("callerId");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		String mode = request.getParameter(PARAM_MODE);
		String callType = request.getParameter(PARAM_CALLTYPE);
		String addCatObj = request.getParameter(PARAM_ADD_CATEGORY_INFO);
		boolean addCatObject = Boolean.valueOf(addCatObj);
		logger.info("subscriberId: " + subscriberId + ", callerId: " + callerId
				+ ", browsingLanguage: " + browsingLanguage + ", appName: "
				+ appName + ", mode: " + mode + " , callType: " + callType
				+ " , addCatObj: " + addCatObject);

		StringBuffer responseCode = new StringBuffer();
		String value = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.getCurrentPlayingSong(subscriberId, callerId,
						browsingLanguage, appName, mode, callType,responseCode, addCatObject);
		request.setAttribute("response", value);
		logger.info("responseStatusCode: " + responseCode);
		response.setStatus(Integer.parseInt(responseCode.toString()));
		logger.info("param response : " + value);
		return mapping.findForward("success");
	}
	
	//RBT-14626
	public ActionForward getCallLogHistory(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SelectionAction.getCurrentPlayingSong");
		String subscriberId = request.getParameter(PARAM_SUBSCRIBER_ID);
		String mode = request.getParameter(PARAM_MODE);
		String callType = request.getParameter(PARAM_CALLTYPE);
		String pageSize = request.getParameter(PARAM_PAGE_SIZE);
		String offset = request.getParameter(PARAM_OFFSET);
		logger.info("subscriberId: " + subscriberId + ", mode: " + mode +" , callType: "+callType +" , pageSize: "+pageSize+ " , offset: "+ offset);
		
		String resp =  ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode).getCallLogHistory(subscriberId, mode, callType,pageSize, offset);
		
		request.setAttribute("response", resp);
		logger.info("resp for calllog history is : "+resp);
	
		return mapping.findForward("success");
	}
}