package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.factory.ResponseFactory;

public class ProfileCategoriesAction extends Action{
	
	public static Logger logger = Logger.getLogger(ProfileCategoriesAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String subId = request.getParameter("subscriberId");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		
		logger.info("Inside ProfileCategoriesAction action. subscriberId: " + subId + ", browsingLanguage: " + browsingLanguage + ", appName: " + appName);
		String profileCategories=ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getProfileCategories(subId, browsingLanguage, appName);
	    request.setAttribute("response", profileCategories);
		logger.info("Json profileCategories Clips :" + profileCategories);
		return mapping.findForward("success");
	}

}
