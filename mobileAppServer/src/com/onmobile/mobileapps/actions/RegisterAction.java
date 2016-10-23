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
import com.onmobile.android.interfaces.SubscriberResponse;

public class RegisterAction extends Action{
	
public static Logger logger = Logger.getLogger(RegisterAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		String subscriberId = request.getParameter("subscriberId");
		String password = request.getParameter("password");
		String type = request.getParameter("type"); //register , login, unregister
		//String isResetPasswordString = request.getParameter("isResetPassword"); //Could be used in the future. Commented for now.
		String uid = request.getParameter("uniqueId");
		String appName = request.getParameter("appName");
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
		
		Boolean isResetPassword = true;
		logger.info("subscriberId: " + subscriberId + ", password: " + password
				+ ", type: " + type + ", isResetPassword: " + isResetPassword
				+ ", uid: " + uid + ", appName: " + appName + ", getCircleId: " + getCircleId
				+ ", registrationsource: " + registrationsource + ", userAgent: " + userAgent);
		if (type != null && type.equalsIgnoreCase("login")
				&& (password == null || password.equalsIgnoreCase(""))) {
			logger.info("Password is null . So returning failure");
			request.setAttribute("response", "failure");
			return mapping.findForward("success"); 
		}
		String s2 = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL)
				.subscribeUser(subscriberId, type, password, isResetPassword,
						uid, appName, getCircleId, registrationsource, userAgent);
		request.setAttribute("response", s2);
		logger.info("status"+s2);
		return mapping.findForward("success"); 
	}

}
