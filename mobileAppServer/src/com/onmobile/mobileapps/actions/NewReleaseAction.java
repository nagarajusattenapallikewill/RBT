package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.impl.ContentJSONResponseImpl;
import com.onmobile.android.interfaces.ContentResponse;

public class NewReleaseAction  extends Action{


	public static Logger logger = Logger.getLogger(ClipAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside NewReleaseAction.execute");
		ContentResponse c1 = new ContentJSONResponseImpl();
		String resp = null;
		String offset = request.getParameter("offset");
		int offsetInt = Integer.parseInt(offset);

		String allowedCatSupportString = request.getParameter("allowedcatsupport");
		Boolean allowedCatSupport = Boolean.valueOf(allowedCatSupportString);

		String categoryId = PropertyConfigurator.getNewReleaseCategoryId();
		String subscriberId = request.getParameter("subscriberId");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		String maxResultsString = request.getParameter("maxResults");
		int maxResults = -1;
		if (maxResultsString != null) {
			try {
				maxResults = Integer.parseInt(maxResultsString);
			} catch (NumberFormatException e) {
				logger.info("subscriberId: " + subscriberId + ". NumberFormatException caught. maxResultsString: " + maxResultsString);
				maxResults = -1;
			}
		}
		logger.info("offset: " + offset + ", allowedcatsupport: "
				+ allowedCatSupport + ", configured new release categoryId: "
				+ categoryId + ", subscriberId: " + subscriberId
				+ ", browsingLanguage: " + browsingLanguage + ", appName: "
				+ appName + ", maxResults: " + maxResults);
		if (allowedCatSupport && categoryId != null && categoryId.toLowerCase().startsWith("c")) {
			resp = c1.getNewReleaseCategories(subscriberId, offsetInt, browsingLanguage, appName, maxResults);
		} else {
			logger.debug("allowedCatSupport=false or new release categoryId doesn't start with c/C. Hence retrieving clips.");
			resp = c1.getNewReleaseClips(offsetInt, browsingLanguage, appName, maxResults);
		}
		request.setAttribute("response", resp);
		logger.info("response: " + resp);
		return mapping.findForward("success"); 
	}


}
