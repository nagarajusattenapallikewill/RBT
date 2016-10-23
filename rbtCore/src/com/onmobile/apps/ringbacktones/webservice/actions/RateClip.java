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
public class RateClip extends AbstractClipRatingAction
{
	private static Logger logger = Logger.getLogger(RateClip.class);

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
			int rating = Integer.parseInt(webServiceContext
					.getString(param_rating));

			ClipRating clipRating = RBTCacheManager.getInstance().rateClip(
					clipID, rating);
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

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.webservice.actions.AbstractClipRatingAction
	 * #validateRequest(com.onmobile.apps.ringbacktones.webservice.common.
	 * WebServiceContext)
	 */
	@Override
	protected void validateRequest(WebServiceContext webServiceContext)
			throws WebServiceException
	{
		super.validateRequest(webServiceContext);

		String rating = webServiceContext.getString(param_rating);
		if (rating == null)
		{
			throw new WebServiceException(
					"rating parameter is missing", 0,
					INVALID_PARAMETER);
		}

		try
		{
			Integer.parseInt(rating);
		}
		catch (NumberFormatException e)
		{
			throw new WebServiceException("rating " + rating
					+ " is not an integer", 0, INVALID_PARAMETER);
		}
	}
}
