/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.XMLParser;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author vinayasimha.patil
 * 
 */
public class GetClipRating implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(GetClipRating.class);

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#
	 * processAction
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		Collection<ClipRating> clipRatings = null;
		try
		{
			validateRequest(webServiceContext);

			String clipIDStr = webServiceContext.getString(param_clipID);
			String[] clipIDs = clipIDStr.split(",");
			List<Integer> clipIDList = new ArrayList<Integer>();
			for (String clipID : clipIDs)
			{
				clipIDList.add(Integer.parseInt(clipID));
			}

			Map<Integer, ClipRating> clipRatingsMap = RBTCacheManager
					.getInstance().getClipsRatings(clipIDList);

			getNoOfDownloadsForClips(webServiceContext, clipRatingsMap);

			if(clipRatingsMap != null) {
				clipRatings = clipRatingsMap.values();
			}
			response = SUCCESS;
		}
		catch (WebServiceException e)
		{
			response = e.getResponseString();
			logger.debug(e.getMessage());
		}
		catch (Exception e)
		{
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response, clipRatings);
	}

	private void validateRequest(WebServiceContext webServiceContext)
			throws WebServiceException
	{
		if (!webServiceContext.containsKey(param_clipID))
		{
			throw new WebServiceException(
					"clipID parameter is missing", 0,
					INVALID_PARAMETER);
		}
	}

	private WebServiceResponse getWebServiceResponse(String response,
			Collection<ClipRating> clipRatings)
	{
		Document document = buildClipRatingsXML(response, clipRatings);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);
		return webServiceResponse;
	}

	private void getNoOfDownloadsForClips(WebServiceContext webServiceContext,
			Map<Integer, ClipRating> clipRatingsMap)
	{
		String clipRatingDetailsURL = RBTParametersUtils.getParamAsString(
				iRBTConstant.BI, "CLIP_RATING_DETAILS_URL", null);
		if (clipRatingDetailsURL == null)
		{
			logger.info("BI - CLIP_RATING_DETAILS_URL parameter is not defined, so not getting the information from BI");
			return;
		}
		
		if(clipRatingsMap == null) {
			logger.info("There is not record in clipRatingsMap. Please check it in DB");
			return;
		}

		try
		{
			String clipIDs = webServiceContext.getString(param_clipID);
			clipRatingDetailsURL = clipRatingDetailsURL.replaceAll("%CLIP_ID%",
					clipIDs);

			HttpParameters httpParameters = new HttpParameters(
					clipRatingDetailsURL);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(
					httpParameters, null);
			if (logger.isInfoEnabled())
				logger.info("httpResponse: " + httpResponse);

			Document document = XMLUtils.getDocumentFromString(httpResponse
					.getResponse().trim());
			Element clipRatingsElem = document.getDocumentElement();

			NodeList clipRatingNodes = clipRatingsElem
					.getElementsByTagName(CLIP_RATING);
			for (int i = 0; i < clipRatingNodes.getLength(); i++)
			{
				Element clipRatingElem = (Element) clipRatingNodes.item(i);
				HashMap<String, String> attributesMap = XMLParser
						.getAttributesMap(clipRatingElem);

				int clipID = Integer.parseInt(attributesMap.get(CLIP_ID));
				ClipRating clipRating = clipRatingsMap.get(clipID);
				if(clipRating != null) {
					clipRatingsMap.get(clipID).setNoOfDownloads(
							Integer.parseInt(attributesMap.get(NO_OF_DOWNLOADS)));
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

	}

	public static Document buildClipRatingsXML(String response,
			Collection<ClipRating> clipRatings)
	{
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();

		if (!response.equals(SUCCESS))
			return document;

		Element clipRatingsElem = document.createElement(CLIP_RATINGS);
		element.appendChild(clipRatingsElem);

		if (clipRatings != null)
		{
			for (ClipRating clipRating : clipRatings)
			{
				Element clipRatingElem = document.createElement(CLIP_RATING);
				clipRatingElem.setAttribute(CLIP_ID,
						String.valueOf(clipRating.getClipId()));
				clipRatingElem.setAttribute(NO_OF_VOTES,
						String.valueOf(clipRating.getNoOfVotes()));
				clipRatingElem.setAttribute(SUM_OF_RATINGS,
						String.valueOf(clipRating.getSumOfRatings()));
				clipRatingElem.setAttribute(LIKE_VOTES,
						String.valueOf(clipRating.getLikeVotes()));
				clipRatingElem.setAttribute(DISLIKE_VOTES,
						String.valueOf(clipRating.getDislikeVotes()));
				clipRatingElem.setAttribute(NO_OF_DOWNLOADS,
						String.valueOf(clipRating.getNoOfDownloads()));

				clipRatingsElem.appendChild(clipRatingElem);
			}
		}

		return document;
	}
}
