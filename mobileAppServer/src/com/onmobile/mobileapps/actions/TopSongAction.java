package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.factory.ResponseFactory;

public class TopSongAction extends Action{
	
	public static Logger logger = Logger.getLogger(PromotionalClipsAction.class);
	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String subscriberId = request.getParameter("subscriberId");
		String browsingLanguage = request.getParameter("browsingLanguage");
		String appName = request.getParameter("appName");
		logger.info("TopSongAction.execute. subscriberId: " + subscriberId + ", browsingLanguage: " + browsingLanguage + ", appName: " + appName);
		String pickOfTheDayStr=ResponseFactory.getContentResponse(ResponseFactory.RESPONSE_TYPE_JSON).getPickOfTheDay(subscriberId, browsingLanguage, appName);
	    request.setAttribute("response", pickOfTheDayStr);
		logger.info("Json pickOfTheDay Clips :" + pickOfTheDayStr);
		return mapping.findForward("success");
	}

}
