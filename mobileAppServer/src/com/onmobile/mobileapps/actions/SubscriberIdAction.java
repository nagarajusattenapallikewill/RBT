package com.onmobile.mobileapps.actions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.impl.SubscriberJSONResponseImpl;

public class SubscriberIdAction  extends Action{

	public static Logger logger = Logger.getLogger(SubscriberIdAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		// ActionForward forward = null;
		logger.info("Inside action");
		String uniqueId = request.getParameter("uniqueId");
		String circleId = request.getParameter("circleId");
		String getCircleIdString = request.getParameter("getCircleId");
		boolean getCircleId = Boolean.valueOf(getCircleIdString);
		String userAgent = request.getHeader("user-agent");
		String reqMsisdn = null;
		String registrationsource = null;

		List<String> strHeaderNames = PropertyConfigurator.getMsisdnHeadersName();
		if(strHeaderNames != null && strHeaderNames.size() > 0) {
			for(String headerName : strHeaderNames) {
				String temp = request.getHeader(headerName);
				if(temp != null) {
					reqMsisdn = temp;
					break;
				}
			}
		}
		if (null != reqMsisdn) {
			registrationsource = "GPRS";
		} else {
			registrationsource = "WIFI";
		}

		logger.info("uniqueId: " + uniqueId + ", circleId: " + circleId + ", getCircleId: " + getCircleId
				+ ", registrationsource: " + registrationsource + ", userAgent: " + userAgent);
		String s2 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON,
				SubscriberJSONResponseImpl.CHANNEL).getMSISDN(uniqueId, null,
				circleId, getCircleId, registrationsource, userAgent);
		request.setAttribute("response: ", s2);
		logger.info("response: " + s2);
		return mapping.findForward("success"); 

	}
}