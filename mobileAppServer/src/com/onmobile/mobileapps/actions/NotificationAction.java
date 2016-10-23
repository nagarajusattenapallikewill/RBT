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


public class NotificationAction extends DispatchAction{

	public static Logger logger = Logger.getLogger(NotificationAction.class);
	
	public ActionForward get(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subscriberId = request.getParameter("subscriberId");	
		String os_type = request.getParameter("os_type");
		String regId = request.getParameter("regId");
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getNotificationStatus(subscriberId, os_type, regId);
		request.setAttribute("response", s2);
		logger.info("Response" + s2);
		return mapping.findForward("success"); 
	}
	
	public ActionForward set(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside action");
		String subscriberId = request.getParameter("subscriberId");
		String enableNotification = request.getParameter("enableNotification");
		String os_type = request.getParameter("os_type");
		String regId = request.getParameter("regId");
		Boolean status = true;
		if (enableNotification != null && enableNotification.equalsIgnoreCase("no")) {
			status = false;
		}
		String s2 = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).setNotificationStatus(subscriberId, status, os_type, regId);
		request.setAttribute("response", s2);
		logger.info("Response" + s2);
		return mapping.findForward("success"); 
	}
}
