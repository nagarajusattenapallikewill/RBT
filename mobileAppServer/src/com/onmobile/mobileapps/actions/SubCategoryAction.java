package com.onmobile.mobileapps.actions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.factory.ResponseFactory;

public class SubCategoryAction extends DispatchAction{

	public static Logger logger = Logger.getLogger(SubCategoryAction.class);


	public ActionForward getSubCategory(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
					throws Exception {
		String subCategoryResponse = null;
		try {
			String parentCategoryId = request.getParameter("parentId");
			String subId = request.getParameter("subscriberId");
			String browsingLanguage = request.getParameter("browsingLanguage");
			String appName = request.getParameter("appName");
			Date modifiedSince = null;
			String dateFormat = PropertyConfigurator.getDatePattern();
			String modifiedSinceString = request.getHeader("if-modified-since");
			if (modifiedSinceString != null && dateFormat != null) {
				try {
					modifiedSince = new SimpleDateFormat(dateFormat).parse(modifiedSinceString);
				} catch (ParseException e) {
					logger.error("ERROR OCCURED:: "+e, e);
				}
			}
			logger.info("SubCategoryAction.execute. parentId:" + parentCategoryId
					+ ", subscriberId: " + subId + ", browsingLanguage: "
					+ browsingLanguage + ", appName: " + appName + "modifiedDate: "
					+ modifiedSince);
			subCategoryResponse = ResponseFactory.getContentResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).getSubCategories(subId,
							parentCategoryId, browsingLanguage, appName, modifiedSince);
			if (modifiedSince != null && subCategoryResponse == null) {
				subCategoryResponse = "";
				Date currDate = new Date();
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
				String lastModifiedDate = simpleDateFormat.format(currDate);
				response.setStatus(304);
				response.setHeader("last-modified", lastModifiedDate);
			}
		} catch (Throwable t) {
			logger.error(t,t);
			subCategoryResponse = "error";
		}
		request.setAttribute("response", subCategoryResponse);
		logger.info("subCategoryResponse " + subCategoryResponse);
		return mapping.findForward("success");
	}
}
