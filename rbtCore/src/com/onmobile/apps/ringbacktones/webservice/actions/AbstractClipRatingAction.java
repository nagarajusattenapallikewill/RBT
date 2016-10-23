/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author vinayasimha.patil
 *
 */
public abstract class AbstractClipRatingAction implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger
			.getLogger(AbstractClipRatingAction.class);

	protected void validateRequest(WebServiceContext webServiceContext)
			throws WebServiceException
	{
		String clipID = webServiceContext.getString(param_clipID);
		if (clipID == null)
		{
			throw new WebServiceException(
					"clipID parameter is missing", 0,
					INVALID_PARAMETER);
		}

		Clip clip = RBTCacheManager.getInstance().getClip(clipID);
		if (clip == null)
		{
			throw new WebServiceException(
					"clip " + clipID + " not exists", 0, CLIP_NOT_EXISTS);
		}
	}

	protected WebServiceResponse getWebServiceResponse(String response)
	{
		Document document = Utility.getResponseDocument(response);
		WebServiceResponse webServiceResponse = Utility
				.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
