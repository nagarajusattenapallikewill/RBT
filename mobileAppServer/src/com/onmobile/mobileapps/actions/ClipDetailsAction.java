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

public class ClipDetailsAction extends Action {
	
	public static Logger logger = Logger.getLogger(ClipDetailsAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
		 HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		 ContentResponse c1=new ContentJSONResponseImpl();
		 String clipId = request.getParameter("clipId");
		 String browsingLanguage = request.getParameter("browsingLanguage");
		 String appName = request.getParameter("appName");
		logger.info("ClipDetailsAction.execute. clipId: " + clipId
				+ ", browsingLanguage: " + browsingLanguage
				+ ", appName: " + appName);
		 String resp = c1.getClipInfo(clipId, browsingLanguage, appName);
		 request.setAttribute("response", resp);
		 logger.info("ClipDetailsAction.execute. response: " + resp);
	  return mapping.findForward("success"); 
	}
	
}