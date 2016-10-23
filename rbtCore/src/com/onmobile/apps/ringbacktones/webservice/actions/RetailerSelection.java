package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.CategoriesImpl;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author sridhar.sindiri
 *
 */
public class RetailerSelection implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(RetailerSelection.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		WebServiceResponse webServiceResponse = null;
		try
		{
			String response = "-1:-1:-1";
			webServiceResponse = validateParameters(webServiceContext);
			if (webServiceResponse != null)
			{
				return webServiceResponse;
			}

			String transId = webServiceContext.getString(param_TXNID);
			String retailerMsisdn = webServiceContext.getString(param_RET_MSISDN);
			String customerMsisdn = webServiceContext.getString(param_CUS_MSISDN);
			String mode = webServiceContext.getString(param_MODE);

			webServiceContext.put(param_subscriberID, customerMsisdn);
			webServiceContext.put(param_mode, mode);
			SubscriberDetail subDetail = DataUtils.getSubscriberDetail(webServiceContext);
			String circleID = subDetail.getCircleID();
			if (!subDetail.isValidSubscriber())
			{
				response = "135:" + transId + ":-1";
				webServiceResponse = getWebServiceResponse(response);
				return webServiceResponse;
			}

			Subscriber customer = RBTDBManager.getInstance().getSubscriber(customerMsisdn);
			if (RBTDBManager.getInstance().isTotalBlackListSub(customerMsisdn))
			{
				response = "305:" + transId + ":-1";
				webServiceResponse = getWebServiceResponse(response);
				return webServiceResponse;
			}

			if (customer != null && (customer.subYes().equals(iRBTConstant.STATE_ACTIVATED)))
			{
				Clip clip = (Clip) webServiceContext.get(param_clip);
				Category category = (Category) webServiceContext.get(param_category);
				ChargeClass chargeClassObj = DataUtils.getNextChargeClassForSubscriber(webServiceContext, customer, category, clip);
				String chargeClass = chargeClassObj.getChargeClass();
				String price = chargeClassObj.getAmount();

				if (isSameSongSelection(customerMsisdn, clip.getClipRbtWavFile()))
				{
					response = "300:" + transId + ":" + price;
				}
				else
				{
					String smResponse = chargeRetailer(retailerMsisdn, chargeClass, customerMsisdn, transId);
					if (smResponse.equalsIgnoreCase("SUCCESS"))
					{
						response = "500:" + transId + ":" + price;
						String selResponse = addSelection(customer, category, clip, retailerMsisdn, transId, mode, circleID);
						if (!selResponse.equalsIgnoreCase("SELECTION_SUCCESS"))
							response = "133:" + transId + ":" + price;
					}
					else
					{
						response = "133:" + transId + ":" + price;
					}
				}
			}
			else if (customer != null && !customer.subYes().equals(iRBTConstant.STATE_ACTIVATED)
					&& !customer.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			{
				response = "302:" + transId + ":-1";
			}
			else if (customer == null || customer.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			{
				response = "301:" + transId + ":-1";
			}

			logger.info("Response from retailer selection : " + response);
			webServiceResponse = getWebServiceResponse(response);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			webServiceResponse = getWebServiceResponse("133:-1:-1");
		}

		return webServiceResponse;
	}

	/**
	 * @param response
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response)
	{
		WebServiceResponse webServiceResponse = new WebServiceResponse(response);
		ResponseWriter responseWriter = WebServiceResponseFactory.getResponseWriter(StringResponseWriter.class);
		webServiceResponse.setResponseWriter(responseWriter);

		logger.info("Response from RetailerSelection : " + webServiceResponse);
		return webServiceResponse;
	}

	/**
	 * @param webServiceContext
	 * @return
	 */
	private WebServiceResponse validateParameters(WebServiceContext webServiceContext)
	{
		WebServiceResponse webServiceResponse = null;

		String transId = webServiceContext.getString(param_TXNID);
		String transIdResponse = isValidTxnID(transId);
		if (!transIdResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(transIdResponse);
			return webServiceResponse;
		}

		String retailerMsisdn = webServiceContext.getString(param_RET_MSISDN);
		String retMsisdnIdResponse = isValidRetailerMsisdn(retailerMsisdn, transId);
		if (!retMsisdnIdResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(retMsisdnIdResponse);
			return webServiceResponse;
		}

		String customerMsisdn = webServiceContext.getString(param_CUS_MSISDN);
		String cusMsisdnIdResponse = isValidCustomerMsisdn(customerMsisdn, transId);
		if (!cusMsisdnIdResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(cusMsisdnIdResponse);
			return webServiceResponse;
		}

		String mode = webServiceContext.getString(param_MODE);
		String modeResponse = isValidMode(mode, transId);
		if (!modeResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(modeResponse);
			return webServiceResponse;
		}

		String reqTimeStamp = webServiceContext.getString(param_REQ_TS);
		String timeStampResponse = isValidTimeStamp(reqTimeStamp, transId);
		if (!timeStampResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(timeStampResponse);
			return webServiceResponse;
		}

		String txnType = webServiceContext.getString(param_TXN_TYPE);
		String txnTypeResponse = isValidTxnType(txnType, transId);
		if (!txnTypeResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(txnTypeResponse);
			return webServiceResponse;
		}

		String contentID = webServiceContext.getString(param_VAS_CONTENT_CD);
		String contentIDResponse = isValidContentID(webServiceContext, contentID, transId);
		if (!contentIDResponse.equalsIgnoreCase("VALID"))
		{
			webServiceResponse = getWebServiceResponse(contentIDResponse);
			return webServiceResponse;
		}

		return webServiceResponse;
	}

	/**
	 * @param transId
	 * @return
	 */
	private String isValidTxnID(String transId)
	{
		String response = "VALID";
		if (transId == null)
		{
			response = "260:-1:-1";
		}
		else
		{
			long txnID = -1;
			try
			{
				txnID = Long.parseLong(transId);
			}
			catch (NumberFormatException e)
			{
				response = "261:-1:-1";
			}

			if (txnID < -1)
				response = "262:" + txnID + ":-1";
		}

		return response;
	}

	/**
	 * @param retMsisdn
	 * @param transId
	 * @return
	 */
	private String isValidRetailerMsisdn(String retMsisdn, String transId)
	{
		String response = "VALID";
		if (retMsisdn == null)
		{
			response = "131:" + transId + ":-1";
		}
		else if (retMsisdn.length() != 10 && retMsisdn.length() != 12)
		{
			response = "134:" + transId + ":-1";
		}
		else
		{
			try
			{
				Long.parseLong(retMsisdn);
			}
			catch (NumberFormatException e)
			{
				response = "132:" + transId + ":-1";
			}
		}

		return response;
	}

	/**
	 * @param cusMsisdn
	 * @param transId
	 * @return
	 */
	private String isValidCustomerMsisdn(String cusMsisdn, String transId)
	{
		String response = "VALID";
		if (cusMsisdn == null)
		{
			response = "130:" + transId + ":-1";
		}
		else if (cusMsisdn.length() != 10 && cusMsisdn.length() != 12)
		{
			response = "134:" + transId + ":-1";
		}
		else
		{
			try
			{
				Long.parseLong(cusMsisdn);
			}
			catch (NumberFormatException e)
			{
				response = "132:" + transId + ":-1";
			}
		}

		return response;
	}

	/**
	 * @param mode
	 * @param transId
	 * @return
	 */
	private String isValidMode(String mode, String transId)
	{
		String response = "VALID";
		if (mode == null)
		{
			response = "230:" + transId + ":-1";
		}
		else if (mode.length() > 20)
		{
			response = "231:" + transId + ":-1";
		}

		return response;
	}

	/**
	 * @param timeStamp
	 * @param transId
	 * @return
	 */
	private String isValidTimeStamp(String timeStamp, String transId)
	{
		String response = "VALID";
		if (timeStamp != null)
		{
			try
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				sdf.parse(timeStamp);
			}
			catch (ParseException e)
			{
				response = "140:" + transId + ":-1";
			}
		}
		else
		{
			response = "140:" + transId + ":-1";
		}

		return response;
	}

	/**
	 * @param txnType
	 * @param transId
	 * @return
	 */
	private String isValidTxnType(String txnType, String transId)
	{
		String response = "VALID";
		if (txnType == null)
		{
			response = "220:" + transId + ":-1";
		}
		else if (txnType.length() > 20)
		{
			response = "221:" + transId + ":-1";
		}
		else if (!txnType.equalsIgnoreCase("SEL"))
		{
			response = "222:" + transId + ":-1";
		}

		return response;
	}

	/**
	 * @param contentID
	 * @param transId
	 * @return
	 */
	private String isValidContentID(WebServiceContext webServiceContext, String contentID, String transId)
	{
		String response = "VALID";
		if (contentID == null)
		{
			response = "223:" + transId + ":-1";
		}
		else
		{
			Clip clip = RBTCacheManager.getInstance().getClipByPromoId(contentID);
			if (clip == null)
			{
				Category category = RBTCacheManager.getInstance().getCategoryByPromoId(contentID);
				if (category == null || !Utility.isShuffleCategory(category.getCategoryTpe()))
				{
					response = "223:" + transId + ":-1";
				}
				else if (category.getCategoryEndTime().getTime() < System.currentTimeMillis())
				{
					response = "223:" + transId + ":-1";
				}
				else
					webServiceContext.put(param_category, category);
			}
			else if (clip.getClipEndTime().getTime() < System.currentTimeMillis())
			{
				response = "223:" + transId + ":-1";
			}
			else
				webServiceContext.put(param_clip, clip);
		}

		return response;
	}

	/**
	 * @param retailerMsisdn
	 * @param chargeClass
	 * @param cusMsisdn
	 * @param transId
	 * @return
	 */
	private String chargeRetailer(String retailerMsisdn , String chargeClass ,String cusMsisdn, String transId)
	{
		String response = "ERROR";
		try
		{
			String url = RBTParametersUtils.getParamAsString("RETAILER", "CHARGE_REALTIME_URL", null);
			url = url.replaceAll("%msisdn%", retailerMsisdn);
			url = url.replaceAll("%eventkey%", chargeClass);
			url = url.replaceAll("%info%", cusMsisdn);
			url = url.replaceAll("%transid%", transId);

			logger.info("RBT:: SM Realtime charging URL: " + url);
			HttpParameters httpParameters = new HttpParameters(url);
			
			Utility.setSubMgrProxy(httpParameters);
			
			int connectionTimeout = RBTParametersUtils.getParamAsInt("RETAILER", "CONNECTION_TIMEOUT_IN_SECS_FOR_CHARGE_REALTIME_URL", 6000);
			int soTimeout = RBTParametersUtils.getParamAsInt("RETAILER", "SO_TIMEOUT_IN_SECS_FOR_CHARGE_REALTIME_URL", 6000);
			httpParameters.setConnectionTimeout(connectionTimeout);
			httpParameters.setSoTimeout(soTimeout);
			
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);
			String smRresponse = httpResponse.getResponse();
			if (smRresponse.toUpperCase().contains("SUCCESS"))
			{
				response = "SUCCESS";
			}
			else if (smRresponse.toUpperCase().contains("BAL_LOW"))
			{
				response = "LOWBAL";
			}
		}
		catch (HttpException e)
		{
			logger.error(e.getMessage(), e);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}

		return response;
	}

	/**
	 * @param subscriberID
	 * @param clipRbtWavFile
	 * @return
	 */
	private boolean isSameSongSelection(String subscriberID, String clipRbtWavFile)
	{
		SubscriberStatus[] settings = RBTDBManager.getInstance().getAllActiveSubscriberSettings(subscriberID);
		if (settings == null)
			return false;

		for (SubscriberStatus setting : settings)
		{
			if (setting.subscriberFile().equalsIgnoreCase(clipRbtWavFile))
				return true;
		}

		return false;
	}

	/**
	 * @param customer
	 * @param category
	 * @param clip
	 * @param retailerMsisdn
	 * @param transId
	 * @param mode
	 * @param circleID
	 * @return
	 */
	private String addSelection(Subscriber customer, Category category, Clip clip, String retailerMsisdn, String transId, String mode, String circleID)
	{
		if (category == null)
		{
			int categoryID = RBTParametersUtils.getParamAsInt("RETAILER", "DEFAULT_CAT_ID_FOR_SELECTIONS", 3);
			category = RBTCacheManager.getInstance().getCategory(categoryID);
		}
		else
		{
			clip = RBTCacheManager.getInstance().getActiveClipsInCategory(category.getCategoryId())[0];
		}
			
		Categories categoriesObj = CategoriesImpl.getCategory(category);

		HashMap<String, Object> clipMap = new HashMap<String, Object>();
		clipMap.put("CLIP_CLASS", clip.getClassType());
		clipMap.put("CLIP_END", clip.getClipEndTime());
		clipMap.put("CLIP_GRAMMAR", clip.getClipGrammar());
		clipMap.put("CLIP_WAV", clip.getClipRbtWavFile());
		clipMap.put("CLIP_ID", String.valueOf(clip.getClipId()));
		clipMap.put("CLIP_NAME", clip.getClipName());

		Calendar endCal = Calendar.getInstance();
		endCal.set(2037, 0, 1);
		Date endDate = endCal.getTime();
		Date startDate = new Date();

		int status = 1;
		String selectedBy = mode;
		String selectionInfo = transId + "|" + retailerMsisdn;
		boolean isPrepaid = customer.prepaidYes();
		String messagePath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MESSAGE_PATH", null);
		String classType = RBTParametersUtils.getParamAsString("RETAILER", "FREE_CHARGE_CLASS_FOR_SELECTIONS", "FREE");
		boolean useSubManager = true;
		String chargingPackage = null;
		String subYes = customer.subYes();
		boolean incrSelCount = true;
		boolean inLoop = false;

		String response = RBTDBManager.getInstance().addSubscriberSelections(customer.subID(), "ALL", categoriesObj, clipMap, null,
				startDate, endDate, status, selectedBy, selectionInfo, 0, isPrepaid, true, messagePath, 0, 2359, classType,
				useSubManager, true, "VUI", chargingPackage, subYes, null, circleID, incrSelCount, false, null, false, false, inLoop,
				customer.subscriptionClass(), customer, 0, null, null, true, null, true);

		return response;
	}
}
