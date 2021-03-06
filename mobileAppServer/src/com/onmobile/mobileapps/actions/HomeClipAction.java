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
public class HomeClipAction extends Action {

public static Logger logger = Logger.getLogger(HomeClipAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
	    ContentResponse c1=new ContentJSONResponseImpl();
		String catId = request.getParameter("parentId");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		logger.info("HomeClipAction.execute. parentId: " + catId + ", browsingLanguage: " + browsingLanguage + ", appName: " + appName);
		String s2 = c1.getPromotionalClipsForCategory(catId, browsingLanguage, appName);
		request.setAttribute("response", s2);
		logger.info("status"+s2);
		return mapping.findForward("success"); 
	}
}
