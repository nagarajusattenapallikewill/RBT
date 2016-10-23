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

public class OfferAction extends Action {

	public static Logger logger = Logger.getLogger(ParameterAction.class);
	private static final String PARAM_MODE = "mode";

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws OMAndroidException {
		logger.info("Inside OfferAction");
		String subscriberId = request.getParameter("subscriberId");
		String clipId = request.getParameter("clipId");
		String offerType = request.getParameter("offerType");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName") ;
		String mode = request.getParameter(PARAM_MODE);
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		logger.info("subscriberId: " + subscriberId + ", clipId: " + clipId + ", offerType: " + offerType + ", mode: " + mode+", browsingLanguage:"+browsingLanguage+", appName:"+appName);
		String value = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).getOffer(subscriberId, clipId, offerType, mode,browsingLanguage ,appName);
		request.setAttribute("response", value);
		logger.info("param response : " + value);
		return mapping.findForward("success");

	}
}