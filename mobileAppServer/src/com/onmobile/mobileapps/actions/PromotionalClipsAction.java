package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.factory.ResponseFactory;

public class PromotionalClipsAction extends Action{
	
	public static Logger logger = Logger.getLogger(PromotionalClipsAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.info("Inside promotionalClip action");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		logger.info("PromotionalClipsAction.execute. browsingLanguage: " +  browsingLanguage + ", appName: " + appName);
		String promotionalClipsStr=ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getPromotionalClips(browsingLanguage, appName);
	    request.setAttribute("response", promotionalClipsStr);
		logger.info("Json promotional Clips :" + promotionalClipsStr);
		return mapping.findForward("success");
	}

}
