package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.impl.ContentJSONResponseImpl;
import com.onmobile.android.interfaces.ContentResponse;

public class HomeCategoryAction  extends Action{
	
	
	public static Logger logger = Logger.getLogger(HomeCategoryAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
	    ContentResponse contentResponse=new ContentJSONResponseImpl();
	    String subId = request.getParameter("subscriberId");
		String language = request.getParameter("language");
		language = language != null ? language.trim() : null;
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		logger.info("HomeCategoryAction.execute. language: subscriberId: "
				+ subId + language + ", browsingLanguage: " + browsingLanguage
				+ ", appName: " + appName);
		String resp = contentResponse.getHomeCategory(subId,language, browsingLanguage, appName);
		request.setAttribute("response", resp);
		logger.info("rsponse"+resp);
		return mapping.findForward("success"); 
		
	}


}


