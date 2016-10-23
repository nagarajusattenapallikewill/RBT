package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.utils.StringConstants;

public class ActivateAction  extends Action{
	
	
	public static Logger logger = Logger.getLogger(ActivateAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		   logger.info("Inside ActivateAction");
		String subId = request.getParameter("subscriberId");
		String mode = request.getParameter("mode");
		String subscriptionClass = request.getParameter("subscriptionClass");
		String baseOfferId = request.getParameter("baseOfferId");
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		logger.info("ActivateAction. subscriberId: " + subId + ", mode: " + mode + ", subscriptionClass: " + subscriptionClass + ", baseOfferId: " + baseOfferId);
		String s2=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).activate_Subscriber(subId, mode, subscriptionClass, baseOfferId);
	    request.setAttribute("response", s2);
		logger.info("response"+s2);
		return mapping.findForward("success"); 
		
	}


}