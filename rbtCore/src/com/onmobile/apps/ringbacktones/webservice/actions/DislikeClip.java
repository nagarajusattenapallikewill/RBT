/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author vinayasimha.patil
 * 
 */
public class DislikeClip extends AbstractClipRatingAction
{
	private static Logger logger = Logger.getLogger(DislikeClip.class);

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#
	 * processAction
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		try
		{
			validateRequest(webServiceContext);

			int clipID = Integer.parseInt(webServiceContext
					.getString(param_clipID));

			ClipRating clipRating = RBTCacheManager.getInstance().dislikeClip(
					clipID);
			if (clipRating != null)
				response = SUCCESS;
			else
				response = FAILED;
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

		return getWebServiceResponse(response);
	}
}
