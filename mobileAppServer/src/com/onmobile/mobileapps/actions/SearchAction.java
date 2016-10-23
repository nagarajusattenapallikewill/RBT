package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;

public class SearchAction extends Action {

	public static Logger logger = Logger.getLogger(SearchAction.class);
	int page = 0;

	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
					throws OMAndroidException {
		logger.info("Inside action");
		String search = request.getParameter("search");
		String criteria = request.getParameter("criteria");
		String pageno = request.getParameter("pageno");
		String searchType = request.getParameter("searchtype");
		String language = request.getParameter("language");
		String subscriberId = request.getParameter("subscriberId");
		String maxResultsString = request.getParameter("maxResults");
		int maxResults = -1;
		if (maxResultsString != null) {
			try {
				maxResults = Integer.parseInt(maxResultsString);
			} catch (NumberFormatException e) {
				logger.info("NumberFormatException caught. maxResultsString: " + maxResultsString);
				maxResults = -1;
			}
		}
		int page = 0;
		if(pageno != null) {
			page = Integer.parseInt(pageno);
		}
		logger.info("SearchAction.execute. search: " + search + ", criteria: "
				+ criteria + ", pageno: " + pageno + ", searchtype: "
				+ searchType + ", language: " + language + ", subscriberId: "
				+ subscriberId + ", maxResults: " + maxResults);
		
		String s2 = null;
		if (searchType != null && searchType.equalsIgnoreCase("nametune")) {
			s2 = ResponseFactory.getSearchResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).searchClipsByNameTune(
							search, criteria, page, language, subscriberId, maxResults);
		} else if (searchType != null && searchType.equalsIgnoreCase("createnametune")) {
			s2 = ResponseFactory.getSearchResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).createNameTune(
							search, language, subscriberId); 
		} else if (searchType != null && searchType.equalsIgnoreCase("suggestion")) {
			s2 = ResponseFactory.getSearchResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).searchSuggestions(search);
		} else if (searchType != null && searchType.equalsIgnoreCase("artist")) {
			s2 = ResponseFactory.getSearchResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).searchForArtists(search, maxResults);
		} else if (searchType != null && searchType.equalsIgnoreCase("playlist")) {
			s2 = ResponseFactory.getSearchResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).searchForPlaylists(search, maxResults);
		} else {
			s2 = ResponseFactory.getSearchResponse(
					ResponseFactory.RESPONSE_TYPE_JSON).searchClips(search,
							criteria, page, subscriberId, maxResults,language);
		}

		request.setAttribute("response", s2);
		logger.info("search response" + s2);
		return mapping.findForward("success");

	}
}