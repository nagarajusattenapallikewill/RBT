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
import com.onmobile.android.interfaces.SubscriberResponse;

public class ParamRefreshAction extends Action {

	public static Logger logger = Logger.getLogger(ParamRefreshAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws OMAndroidException {
		logger.info("Inside ParamRefreshAction");
		String value = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL)
				.refreshAllParamsInMemcache();

		request.setAttribute("response", value);
		logger.info("param response : " + value);
		return mapping.findForward("success");
	}
}