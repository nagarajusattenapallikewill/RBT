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

public class RegisterIdAction extends DispatchAction {

	private static final Logger logger = Logger
			.getLogger(RegisterIdAction.class);

	public ActionForward addRegisterId(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws OMAndroidException
	{
		logger.info("Inside action");
		String regId = request.getParameter("regId");
		String subId = request.getParameter("subscriberId");
		String os_type = request.getParameter("os_type");

		String value = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).addGCMRegistration(regId, subId, os_type);

		request.setAttribute("response", value);
		logger.info("param response : " + value);
		return mapping.findForward("success");
	}

	public ActionForward removeRegisterId(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws OMAndroidException
	{
		logger.info("Inside action");
		String regId = request.getParameter("regId");
		String subId = request.getParameter("subscriberId");

		String value = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).removeGCMRegistration(regId, subId);

		request.setAttribute("response", value);
		logger.info("param response : " + value);
		return mapping.findForward("success");
	}
}
