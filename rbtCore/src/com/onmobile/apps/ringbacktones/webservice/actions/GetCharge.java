/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.actions;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author vinayasimha.patil
 * 
 */
public class GetCharge implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(GetCharge.class);

	private static final String SUCCESS = "SUCCESS";
	private static final String ERROR_TECHNICAL_FAILURE = "ERROR|TECHNICAL_FAILURE";
	private static final String ERROR_INVALID_MSISDN = "ERROR|INVALID_MSISDN";
	private static final String ERROR_BLACKLISTED = "ERROR|BLACKLISTED";
	private static final String ERROR_SUSPENDED = "ERROR|SUSPENDED";
	private static final String ERROR_INVALID_STATE = "ERROR|INVALID_STATE";
	private static final String ERROR_CONTENT_INVALID = "ERROR|CONTENT_INVALID";
	private static final String ERROR_CONTENT_EXPIRED = "ERROR|CONTENT_EXPIRED";
	private static final String ERROR_MISSING_PARAMETERS = "ERROR|MISSING_PARAMETERS";

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#
	 * processAction
	 * (com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR_TECHNICAL_FAILURE;
		try
		{
			validateRequest(webServiceContext);

			String contentID = webServiceContext.getString(param_contentId);
			Category category = null;
			Clip clip = RBTCacheManager.getInstance().getClipByPromoId(
					contentID);
			if (clip == null)
			{
				category = RBTCacheManager.getInstance().getCategoryByPromoId(
						contentID);
			}

			if (clip == null && category == null)
			{
				throw new WebServiceException("Content Not Exists", 0,
						ERROR_CONTENT_INVALID);
			}
			else if (category != null
					&& !Utility.isShuffleCategory(category.getCategoryTpe()))
			{
				throw new WebServiceException(
						"Passed category promoID but its not shuffle", 0,
						ERROR_CONTENT_INVALID);
			}
			else if ((clip != null && clip.getClipEndTime().getTime() < System
					.currentTimeMillis())
					|| (category != null && category.getCategoryEndTime()
							.getTime() < System.currentTimeMillis()))
			{
				throw new WebServiceException("Content Expired", 0,
						ERROR_CONTENT_EXPIRED);
			}

			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);

			ChargeClass chargeClass = DataUtils
					.getNextChargeClassForSubscriber(webServiceContext,
							subscriber, category, clip);

			response = SUCCESS + "|" + chargeClass.getAmount();
		}
		catch (WebServiceException e)
		{
			response = e.getResponseString();
			logger.debug(e.getMessage());
		}
		catch (Exception e)
		{
			response = ERROR_TECHNICAL_FAILURE;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response);
	}

	private void validateRequest(WebServiceContext webServiceContext)
			throws WebServiceException, RBTException
	{
		String subscriberID = webServiceContext.getString(param_msisdn);
		if (subscriberID == null)
		{
			throw new WebServiceException("'msisdn' parameter is missing", 0,
					ERROR_MISSING_PARAMETERS);
		}

		if (!webServiceContext.containsKey(param_contentId))
		{
			throw new WebServiceException("'contentId' parameter is missing",
					0, ERROR_MISSING_PARAMETERS);
		}

		// Setting the subscriberID parameter to value of msisdn parameter, so
		// that same can be used in other webservice APIs.
		webServiceContext.put(param_subscriberID, subscriberID);

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (rbtDBManager.isTotalBlackListSub(subscriberID))
		{
			throw new WebServiceException("Subscriber Blacklisted", 0,
					ERROR_BLACKLISTED);
		}

		Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);

		if (rbtDBManager.isSubscriberDeactivated(subscriber))
		{
			SubscriberDetail subscriberDetail = DataUtils
					.getSubscriberDetail(webServiceContext);
			if (subscriberDetail == null
					|| !subscriberDetail.isValidSubscriber())
			{
				throw new WebServiceException("Subscriber Invalid", 0,
						ERROR_INVALID_MSISDN);
			}
		}
		else if (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_ERROR)
				|| rbtDBManager.isSubscriberDeactivationPending(subscriber))
		{
			throw new WebServiceException("Subscriber in Error State", 0,
					ERROR_INVALID_STATE);
		}
		else if (rbtDBManager.isSubscriberSuspended(subscriber))
		{
			throw new WebServiceException("Subscriber Suspended", 0,
					ERROR_SUSPENDED);
		}
	}

	private WebServiceResponse getWebServiceResponse(String response)
	{
		WebServiceResponse webServiceResponse = new WebServiceResponse(response);
		webServiceResponse.setContentType("text/plain; charset=utf-8");
		ResponseWriter responseWriter = WebServiceResponseFactory
				.getResponseWriter(StringResponseWriter.class);
		webServiceResponse.setResponseWriter(responseWriter);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
