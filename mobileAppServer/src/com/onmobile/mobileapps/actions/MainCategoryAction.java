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

public class MainCategoryAction  extends DispatchAction{
	
	
	public static Logger logger = Logger.getLogger(MainCategoryAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		String subId = request.getParameter("subscriberId");
		String language = request.getParameter("language");
		language = language != null ? language.trim() : null;
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		logger.info("MainCategoryAction.execute. subscriberId: " + subId + ", language: " + language + ", browsingLanguage: " + browsingLanguage + ", appName: " + appName);
		String resp = ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getMainCategory(subId, language, browsingLanguage, appName);
		request.setAttribute("response", resp);
		logger.info("rsponse"+resp);
		return mapping.findForward("success"); 
		
	}
	
	
}


