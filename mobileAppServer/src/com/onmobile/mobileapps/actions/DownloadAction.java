package com.onmobile.mobileapps.actions;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.interfaces.SubscriberResponse;
import com.onmobile.android.utils.StringConstants;
import com.onmobile.android.utils.Utility;

public class DownloadAction  extends DispatchAction{

	public static Logger logger = Logger.getLogger(DownloadAction.class);

	public ActionForward addDownload(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String clipId=request.getParameter("clipId");
		String catId=request.getParameter("categoryId");	
		String mode = request.getParameter("mode");
		String isMusicPack = request.getParameter("isMusicPack");
		if (mode == null || mode.length() == 0)
			mode = SubscriberResponse.CHANNEL;

		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).addSubscriberDownload(subId, clipId, catId, mode, isMusicPack);
		request.setAttribute("response", s2);
		return mapping.findForward("success"); 
	}

	public ActionForward getDownloads(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getListDownloads(subId);
		request.setAttribute("response", s2);
		logger.info("rsponse"+s2);
		return mapping.findForward("success"); 
	}

	public ActionForward removeDownload(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String clipId=request.getParameter("clipId");
		String catId=request.getParameter("categoryId");	
		String mode = request.getParameter("mode");
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).removeSubscriberDownload(subId, clipId, catId, mode);
		request.setAttribute("response", s2);
		logger.info("rsponse"+s2);
		return mapping.findForward("success"); 
	}

	public ActionForward getCountOfAvailableMusicPackDownloads(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getCountOfAvailableMusicPackDownloads(subId);
		request.setAttribute("response", s2);
		logger.info("rsponse"+s2);
		return mapping.findForward("success"); 
	}

	public ActionForward getDownloadsWithSelections(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside SelectionAction.getCurrentPlayingSong");
		String subscriberId = request.getParameter("subscriberId");
		logger.info("subscriberId (mandatory): " + subscriberId);
		String responseString = null;
		if (!Utility.isStringValid(subscriberId)) {
			responseString = StringConstants.MANDATORY_PARAM_INVALID;
			logger.error("Mandatory parameter subscriberId missing. Returning " + responseString);
		} else {
			responseString = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getDownloadsWithSelections(subscriberId);
		}
		request.setAttribute("response", responseString);
		logger.info("rsponse"+responseString);
		return mapping.findForward("success"); 
	}
	
	
	public ActionForward deleteDownload(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String clipId = request.getParameter("clipId");
		String caller = request.getParameter("callerId");
		String fromTime = request.getParameter("fromTime");
		String toTime = request.getParameter("toTime");
		String fromTimeMinutes = request.getParameter("fromTimeMinutes");
		String toTimeMinutes = request.getParameter("toTimeMinutes");
		//RBT_AT-103588
		String circleId = request.getParameter("circleID");
		String refId  = request.getParameter("refID");
		String mode = request.getParameter("mode");
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String catId=request.getParameter("categoryID");	

		String s2 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.removeSubscriberDownload(subId, clipId, caller,
						mode, fromTime, toTime,
						 circleId,refId,catId,fromTimeMinutes,toTimeMinutes);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 
	}
}
