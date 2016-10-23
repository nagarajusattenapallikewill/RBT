package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.beans.ClipInfoActionBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.impl.ContentJSONResponseImpl;
import com.onmobile.android.interfaces.ContentResponse;

public class ClipAction  extends Action{


	public static Logger logger = Logger.getLogger(ClipAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		ContentResponse c1=new ContentJSONResponseImpl();
		String catId = request.getParameter("parentId");
		int catIdInt = Integer.parseInt(catId);
		String offset=request.getParameter("offset");
		int offsetInt=Integer.parseInt(offset);
		String BIParam = request.getParameter("BI");
		boolean BIIndc =  false;
		if(BIParam != null && BIParam.equalsIgnoreCase("true")) {
			BIIndc = true;
		}   
		String subId = request.getParameter("subscriberId");
		String devicetype = request.getParameter("devicetype");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		String maxResultsString = request.getParameter("maxResults");
		String circleId = request.getParameter("circleId");
		String isUserLanguage =  request.getParameter("isUserLanguageSelected");
		boolean isUserLanguageSelected = false ;
		if(isUserLanguage != null && isUserLanguage.equalsIgnoreCase("true")) {
			isUserLanguageSelected = true;
		}
		String userStatus = request.getParameter("isSubscribed");
		boolean isSubscribed = false ;
		if(userStatus != null && userStatus.equalsIgnoreCase("true")) { 
			isSubscribed = true;
		}
		String totalSize=request.getParameter("totalSize");
		String sessionID=request.getParameter("sessionID");
		String languagePassed =  PropertyConfigurator.getMappedLanguage(browsingLanguage);
		String circleIdPassed = PropertyConfigurator.getMappedCircleId(circleId);
		int maxResults = -1;
		int totalSizeInt= -1;
		if(totalSize != null && !totalSize.isEmpty()){
			totalSizeInt = Integer.parseInt(totalSize);
		}
		if (maxResultsString != null) {
			try {
				maxResults = Integer.parseInt(maxResultsString);
			} catch (NumberFormatException e) {
				logger.info("subscriberId: " + subId + ". NumberFormatException caught. maxResultsString: " + maxResultsString);
				maxResults = -1;
			}
		}
		logger.info("ClipAction.execute. parentId: " + catId + ", offset: "
				+ offset + ", BI: " + BIParam + ", subscriberId: " + subId
				+ ", devicetype: " + devicetype + ", browsingLanguage: "
				+ browsingLanguage + ", appName: " + appName + ", maxResults: "
				+ maxResults + ", circleId: " + circleId
				+ ", isUserLanguageSelected: " + isUserLanguageSelected
				+ ", isSubscribed: " + isSubscribed + ", totalSizeInt: "
				+ totalSizeInt + ", sessionID: " + sessionID)  ;
		ClipInfoActionBean clipInfoActionBean = new ClipInfoActionBean(
				catIdInt, offsetInt, BIIndc, subId, devicetype,
				languagePassed, appName, maxResults, circleIdPassed,
				isUserLanguageSelected, isSubscribed, totalSizeInt,sessionID);
		
		String s2 = c1.getClips(clipInfoActionBean) ;
		request.setAttribute("response", s2);
		logger.info("ClipAction.execute response: " + s2);
		return mapping.findForward("success"); 
	}


}