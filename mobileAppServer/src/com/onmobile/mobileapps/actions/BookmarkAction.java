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

public class BookmarkAction  extends DispatchAction{


	public static Logger logger = Logger.getLogger(BookmarkAction.class);

	public ActionForward addBookmark(ActionMapping mapping, ActionForm form, 
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
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).addSubscriberBookMark(subId, clipId, catId, mode);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 

	}

	public ActionForward getBookmarks(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subId = request.getParameter("subscriberId");
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getListBookmarks(subId);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 

	}

	public ActionForward removeBookmark(ActionMapping mapping, ActionForm form, 
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
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).removeSubscriberBookMark(subId, clipId, catId, mode);
		request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 
	}


}