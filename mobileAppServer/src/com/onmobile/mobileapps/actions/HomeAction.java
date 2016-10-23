package com.onmobile.mobileapps.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;

public class HomeAction  extends Action{


	public static Logger logger = Logger.getLogger(HomeAction.class);

	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		String resp = null;
		try {
			String subId = request.getParameter("subscriberId");
			String language = request.getParameter("language");
			String browsingLanguage = request.getParameter("browsingLanguage");
			String appName = request.getParameter("appName");
			Date modifiedSince = null;
			String dateFormat = PropertyConfigurator.getDatePattern();
			String modifiedSinceString = request.getHeader("if-modified-since");
			String offset=request.getParameter("offset");
			int offsetInt=0;
			String maxResultsString = request.getParameter("maxResults");
			int maxResults = -1;
			if (maxResultsString != null) {
				try {
					maxResults = Integer.parseInt(maxResultsString);
				} catch (NumberFormatException e) {
					logger.info("subscriberId: " + subId + ". NumberFormatException caught. maxResultsString: " + maxResultsString);
					maxResults = -1;
				}
			}
			if (offset != null) {
				try {
					offsetInt = Integer.parseInt(offset);
				} catch (NumberFormatException e) {
					logger.info("subscriberId: " + subId + ". NumberFormatException caught. maxResultsString: " + maxResultsString);
					offsetInt = 0;
				}
			}
			if (modifiedSinceString != null && dateFormat != null) {
				try {
					modifiedSince = new SimpleDateFormat(dateFormat).parse(modifiedSinceString);
				} catch (ParseException e) {
					logger.error("ERROR OCCURED:: "+e, e);
				}
			}
			logger.info("HomeAction.execute. subscriberId: " + subId
					+ ", language: " + language + ", browsingLanguage: "
					+ browsingLanguage + ", appName: " + appName + "modifiedDate: "
					+ modifiedSince);
			resp = ResponseFactory.getContentResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).getActiveCategories(subId, language, browsingLanguage, appName, modifiedSince,offsetInt,maxResults);
			if (modifiedSince != null && resp == null) {
				resp = "";
				Date currDate = new Date();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
				String lastModifiedDate = simpleDateFormat.format(currDate);
				response.setStatus(304);
				response.setHeader("last-modified",lastModifiedDate);
			}
		} catch (Throwable t) {
			logger.error(t,t);
			resp = "error";
		}
		request.setAttribute("response", resp);
		logger.info("rsponse"+resp);
		return mapping.findForward("success");

	}
}



