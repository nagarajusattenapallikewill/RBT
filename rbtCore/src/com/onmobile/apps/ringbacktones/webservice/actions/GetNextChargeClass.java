package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * For getting next charge class for the subscriber
 * 
 * @author sridhar.sindiri
 *
 */
public class GetNextChargeClass implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(GetNextChargeClass.class);

	private static final String INVALID_STATE 		= "invalid_state";

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		Map<String, ChargeClass> chargeClassMap = new HashMap<String, ChargeClass>(); 
		try
		{
			validateRequest(webServiceContext);

			String categoryID = webServiceContext.getString(param_categoryID);
			Category category = null;
			if (categoryID != null)
				category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryID));

			if (categoryID != null && category == null)
			{
				throw new WebServiceException("Category Not Exists", 0, CATEGORY_NOT_EXISTS);
			}
			if (category != null && Utility.isShuffleCategory(category.getCategoryTpe())
					&& category.getCategoryEndTime().getTime() < System.currentTimeMillis())
			{
				throw new WebServiceException("Category Expired", 0, CATEGORY_EXPIRED);
			} 
			List<String> catTypeList = Arrays.asList(RBTParametersUtils.getParamAsString("COMMON",
					"CATEGORY_TYPES_FOR_CATEGORY_CHARGE_CLASSES", "").split(","));
			String shuffleChargeClass = webServiceContext.getString(param_shuffleChargeClass);
			logger.info("shuffleChargeClass : " + shuffleChargeClass);
			if((shuffleChargeClass!=null && shuffleChargeClass.equalsIgnoreCase(YES))
					||(category != null && catTypeList.contains(category.getCategoryTpe()+""))){
				ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(category.getClassType());
				logger.info("charge class of shuffle type"  + chargeClass);
				chargeClassMap.put(category.getCategoryId()+"", chargeClass);
				response = SUCCESS;
				return getWebServiceResponse(response, chargeClassMap);
			}

			String clipID = webServiceContext.getString(param_clipID);
			Clip clip = null;
			String[] clipIDs = null;
			if (clipID != null)
			{
				if (clipID.contains(","))
					clipIDs = clipID.split(",");
				else if (!clipID.endsWith(".3gp") && !clipID.endsWith(".wav"))
					clip = RBTCacheManager.getInstance().getClip(clipID);
			}

			Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
			if (clipIDs == null || clipIDs.length <= 1)
			{
				if (clipID != null && clip == null)
				{
					int selType = -1;
					if (webServiceContext.containsKey(param_selectionType)) {
						String strSelType = webServiceContext
								.getString(param_selectionType);
						try {
							selType = Integer.parseInt(strSelType);
						} catch (NumberFormatException ne) {
						}
					}

					if (selType != iRBTConstant.PROFILE_SEL_TYPE
							&& category.getCategoryTpe() != iRBTConstant.RECORD
							&& category.getCategoryTpe() != iRBTConstant.KARAOKE)
					{
						throw new WebServiceException("Clip Not Exists", 0, CLIP_NOT_EXISTS);
					}
					else
					{
						String defClassType = RBTParametersUtils.getParamAsString("COMMON", "DEFAULT_CHARGE_CLASS_FOR_RECORDED_CLIPS", "DEFAULT");
						ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(defClassType);
						chargeClassMap.put(clipID, chargeClass);
						response = SUCCESS;
						return getWebServiceResponse(response, chargeClassMap);
					}
				}
				if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis())
				{
					throw new WebServiceException("Clip Expired", 0, CLIP_EXPIRED);
				}

				/*
				 * Added below code to return getNextChargeClass as FREE for first song through MOBILEAPP
				 */
				HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML((subscriber != null) ? subscriber.extraInfo() : null);
				if (extraInfoMap == null)
					extraInfoMap = new HashMap<String, String>();

				String mode = webServiceContext.getString(param_mode);
				List<String> supportedModesList = Arrays.asList(RBTParametersUtils.getParamAsString("MOBILEAPP", "mobileapp.free.sel.mode", "").split(","));
				if (supportedModesList.contains(mode) && !extraInfoMap.containsKey("MOBILE_APP_FREE")) {
					ChargeClass chargeClass = CacheManagerUtil
							.getChargeClassCacheManager()
							.getChargeClass(
									RBTParametersUtils.getParamAsString(
											"MOBILEAPP",
											"mobileapp.free.chargeclass", "DEFAULT"));
					if (chargeClass != null)
					{
						chargeClassMap.put(clipID, chargeClass);
						response = SUCCESS;
						return getWebServiceResponse(response, chargeClassMap);
					}
				} 

				ChargeClass chargeClass = DataUtils.getNextChargeClassForSubscriber(webServiceContext, subscriber, category, clip);
				if (chargeClass != null)
					chargeClassMap.put(clipID, chargeClass);
			}
			else
			{
				for (String eachClipID : clipIDs)
				{
					webServiceContext.put(param_clipID, eachClipID);
					clip = RBTCacheManager.getInstance().getClip(eachClipID);
					if (clip == null)
					{
						logger.debug("Clip not exists, clipID : " + eachClipID);
						continue;
					}

					if (clip != null && clip.getClipEndTime().getTime() < System.currentTimeMillis())
					{
						logger.debug("Clip Expired, clipID : " + eachClipID);
						continue;
					}

					ChargeClass chargeClass = DataUtils.getNextChargeClassForSubscriber(webServiceContext, subscriber, category, clip);
					if (chargeClass != null)
						chargeClassMap.put(eachClipID, chargeClass);
				}
			}

			response = SUCCESS;
		}
		catch(WebServiceException e)
		{
			response = e.getResponseString();
			logger.debug(e.getMessage());
		}
		catch(Exception e)
		{
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response, chargeClassMap);
	}

	/**
	 * @param webServiceContext
	 * @throws WebServiceException
	 * @throws RBTException
	 */
	private void validateRequest(WebServiceContext webServiceContext) throws WebServiceException, RBTException
	{
		String subscriberID = webServiceContext.getString(param_subscriberID);
		if (subscriberID == null)
			return;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		Subscriber subscriber = DataUtils.getSubscriber(webServiceContext);
		String validResponse = DataUtils.isValidUser(webServiceContext, subscriber);
		if (!validResponse.equals(VALID))
			throw new WebServiceException("Subscriber Invalid", 0, validResponse);

		if (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_ERROR)
				|| rbtDBManager.isSubscriberDeactivationPending(subscriber)))
		{
			throw new WebServiceException("Subscriber in Error State", 0, INVALID_STATE);
		}
	}

	/**
	 * @param response
	 * @param chargeClassesMap
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response, Map<String, ChargeClass> chargeClassesMap)
	{
		if (logger.isDebugEnabled())
			logger.debug("chargeClassesMap : " + chargeClassesMap);

		Document document = buildChargeClassesXML(response, chargeClassesMap);
		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);
		return webServiceResponse;
	}

	/**
	 * @param response
	 * @param chargeClassesMap
	 * @return
	 */
	private Document buildChargeClassesXML(String response, Map<String, ChargeClass> chargeClassesMap)
	{
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();

		if (!response.equals(SUCCESS))
			return document;

		Element chargeClassesElement = document.createElement(CHARGE_CLASSES);
		element.appendChild(chargeClassesElement);

		Element contentsElem = document.createElement(CONTENTS);
		chargeClassesElement.appendChild(contentsElem);

		if (chargeClassesMap != null)
		{
			for (Entry<String, ChargeClass> chargeClassEntry : chargeClassesMap.entrySet())
			{
				Element contentElem = document.createElement(CONTENT);
				contentElem.setAttribute(ID, chargeClassEntry.getValue().getChargeClass());
				contentElem.setAttribute(AMOUNT, chargeClassEntry.getValue().getAmount());
				contentElem.setAttribute(PERIOD, chargeClassEntry.getValue().getSelectionPeriod());
				contentElem.setAttribute(RENEWAL_AMOUNT, chargeClassEntry.getValue().getRenewalAmount());
				contentElem.setAttribute(RENEWAL_PERIOD, chargeClassEntry.getValue().getRenewalPeriod());
				contentElem.setAttribute(SHOW_ON_GUI, (chargeClassEntry.getValue().getShowonGui().equalsIgnoreCase("y") ? YES : NO));

				if (chargeClassEntry.getValue().getOperatorCode1() != null)
					contentElem.setAttribute(OPERATOR_CODE_1, chargeClassEntry.getValue().getOperatorCode1());

				if (chargeClassEntry.getKey() != null && chargeClassEntry.getKey().length() != 0)
					contentElem.setAttribute(CLIP_ID, chargeClassEntry.getKey());

				contentsElem.appendChild(contentElem);
			}
		}

		return document;
	}
}
