package com.onmobile.apps.ringbacktones.webservice.actions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.RBTLoginUser;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.SocialRBTEventLogger;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceException;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;

/**
 * @author sridhar.sindiri
 *
 */
public class SetRBTUserPIN extends AbstractUserPINAction
{
	private static Logger logger = Logger.getLogger(SetRBTUserPIN.class);

	private static SimpleDateFormat dateFormatter = null;
	private static SimpleDateFormat logDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;
		RBTLoginUser rbtLoginUser = null;
		try
		{
			validateParameters(webServiceContext);

			String userID = webServiceContext.getString(param_userID);
			String newUserID = webServiceContext.getString(param_newUserID);
			String password = webServiceContext.getString(param_password);
			String subscriberID = webServiceContext.getString(param_subscriberID);
			String type = webServiceContext.getString(param_type);
			boolean encryptPassword = webServiceContext.containsKey(param_encryptPassword) && webServiceContext.getString(param_encryptPassword).equalsIgnoreCase(YES); 
			Date creationTime = new Date();

			HashMap<String, String> userInfo = new HashMap<String, String>();
			Set<String> keySet = webServiceContext.keySet();
			for (String key : keySet)
			{
				if (key.startsWith(param_userInfo))
					userInfo.put(key.substring(key.indexOf('_') + 1), webServiceContext.getString(key));
			}

			boolean result = false;
			rbtLoginUser = RBTDBManager.getInstance().getRBTLoginUser(userID, null, null, type, null, encryptPassword);
			validateUserPIN(webServiceContext, rbtLoginUser);
			if (rbtLoginUser == null)
			{
				userInfo.put("PIN_COUNT", "1");
				result = (RBTDBManager.getInstance().addRBTLoginUser(userID, password, subscriberID, type, userInfo,
						encryptPassword) != null);

				StringBuilder logBuilder = new StringBuilder();
				logBuilder.append(userID).append(",ADD").append(",").append(password).append(",").append(logDateFormat.format(creationTime));
				RBTEventLogger.logEvent(RBTEventLogger.Event.LOGINUSERPIN, logBuilder.toString());
			}
			else
			{
				int pinCount = getTotalPinsCount(rbtLoginUser);
				pinCount = pinCount + 1;
				userInfo.put("PIN_COUNT", String.valueOf(pinCount));

				int allowedPinsCount = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "MAXIMUM_USER_PINS_ALLOWED", 5);
				if (pinCount > allowedPinsCount)
				{
					return getWebServiceResponse(MAX_PINS_LIMIT_REACHED, null);
				}

				result = RBTDBManager.getInstance().updateRBTLoginUser(userID, newUserID, password, subscriberID, type,
						rbtLoginUser.userInfo(), userInfo, encryptPassword, creationTime, null);

				StringBuilder logBuilder = new StringBuilder();
				logBuilder.append(userID).append(",UPDATE").append(",").append(password).append(",").append(logDateFormat.format(creationTime));
				RBTEventLogger.logEvent(RBTEventLogger.Event.LOGINUSERPIN, logBuilder.toString());
			}
			rbtLoginUser = RBTDBManager.getInstance().getRBTLoginUser(userID, null, null, type, null,encryptPassword);

			if (result) {
				response = SUCCESS;
				String smsText = RBTParametersUtils.getParamAsString(type.toUpperCase(), "REGISTRATION_SMS_TEXT", "");
				if (null != smsText && 0 < smsText.trim().length()) {
					response = sendSMS(webServiceContext, smsText);
				}
			} else {
				response = FAILED;
			}
		}
		catch (WebServiceException we)
		{
			response = we.getResponseString();
			logger.debug(we.getMessage());
		}
		catch (Exception e)
		{
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response, rbtLoginUser);
	}

	/**
	 * @param userID
	 * @param type
	 * @return
	 */
	private int getTotalPinsCount(RBTLoginUser rbtLoginUser)
	{
		Date lastUserCreationTime = rbtLoginUser.creationTime();
		Date currentDate = new Date();

		if (dateFormatter == null)
			dateFormatter = new SimpleDateFormat(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DATE_FORMAT_FOR_USER_PINS", null));

		String presentDate = dateFormatter.format(currentDate);
		String lastCreationDate = dateFormatter.format(lastUserCreationTime);

		int pinCount = 0;
		if (presentDate.equals(lastCreationDate))
		{
			Map<String, String> userInfoMap = rbtLoginUser.userInfo();
			String pinCountStr = userInfoMap.get("PIN_COUNT");
			if (pinCountStr != null)
				pinCount = Integer.parseInt(pinCountStr);
		}

		return pinCount;
	}

	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.AbstractUserPINAction#validateParameters(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	protected void validateParameters(WebServiceContext webServiceContext) throws WebServiceException
	{
		super.validateParameters(webServiceContext);

		String password = webServiceContext.getString(param_password);
		if (password == null)
		{
			throw new WebServiceException("password parameter is missing", 0, INVALID_PARAMETER);
		}
	}

	/**
	 * @param webServiceContext
	 * @param rbtLoginUser
	 * @throws WebServiceException
	 */
	private void validateUserPIN(WebServiceContext webServiceContext, RBTLoginUser rbtLoginUser) throws WebServiceException
	{
		if (rbtLoginUser != null)
		{
			String password = rbtLoginUser.password();
			Date userCreationTime = rbtLoginUser.creationTime();
			int expiryMins = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "PASSWORD_EXPIRY_TIME_IN_MINS", 15);
			if (userCreationTime != null
					&& ((System.currentTimeMillis() - userCreationTime.getTime()) < (expiryMins * 60 * 1000))
					&& password != null)
			{
				throw new WebServiceException("Pin already generated within last configured time", 0, ALREADY_REGISTERED);
			}
		}
	}

	/**
	 * @param response
	 * @param rbtLoginUser
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response, RBTLoginUser rbtLoginUser)
	{
		Document document = Utility.getResponseDocument(response);
		Element element = document.getDocumentElement();
		if ((response.equals(SUCCESS) || response.equals(ALREADY_REGISTERED)))
		{
			Element applicationDetailsElem = document.createElement(APPLICATION_DETAILS);
			element.appendChild(applicationDetailsElem);
			Element rbtLoginUsersElement = document.createElement(RBT_LOGIN_USERS);
			applicationDetailsElem.appendChild(rbtLoginUsersElement);

			Element rbtLoginUserelement = BasicXMLElementGenerator.generateRBTLoginUserElement(document, rbtLoginUser);
			if (rbtLoginUserelement != null)
				rbtLoginUsersElement.appendChild(rbtLoginUserelement);
		}

		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}

	private String sendSMS(WebServiceContext task, String smsText) {
		String response = ERROR;

		try {
			String type = task.getString(param_type);
			String senderID = RBTParametersUtils.getParamAsString(type.toUpperCase(), "SENDER_NO", "");
			String receiverID = task.getString(param_subscriberID);
			
			logger.info("Registration SMS parameters type:" + type
					+ " senderID:" + senderID + " receiverID:" + receiverID
					+ " smsText:" + smsText);

			if (senderID == null
					|| 0 == senderID.trim().length()
					|| smsText == null
					|| 0 == smsText.trim().length()
					|| !com.onmobile.apps.ringbacktones.services.common.Utility
							.isValidNumber(receiverID)) {
				response = INVALID_PARAMETER;
			} else {
				String password = task.getString(param_password);
				smsText = smsText.replaceAll("%P", password);

				int index = Integer.parseInt(RBTParametersUtils
						.getParamAsString(iRBTConstant.SMS, "SMS_TEXT_LENGTH",
								"154"));
				List<String> smsTextList = new ArrayList<String>();
				String brokenSmsText = null;
				while (smsText.length() != 0) {
					// index = 154;
					if (smsText.length() <= index) {
						brokenSmsText = smsText;
						smsText = "";
					} else {
						while (index >= 0 && smsText.charAt(index) != ' ')
							index--;
						brokenSmsText = smsText.substring(0, index);
						smsText = smsText.substring(index + 1);
					}

					smsTextList.add(brokenSmsText);
				}

				Parameters addPageNoAtBeginingParam = CacheManagerUtil.getParametersCacheManager()
						.getParameter(iRBTConstant.COMMON,
								"ADD_PAGE_NO_AT_BEGINING", "FALSE");
				boolean addPageNoAtBegining = addPageNoAtBeginingParam
						.getValue().trim().equalsIgnoreCase("TRUE");

				boolean sendSMSResponse = false;
				for (int i = 0; i < smsTextList.size(); i++) {
					smsText = smsTextList.get(i);

					if (smsTextList.size() > 1) {
						if (addPageNoAtBegining)
							smsText = (i + 1) + "/" + smsTextList.size() + " "
									+ smsText;
						else
							smsText = smsText + " " + (i + 1) + "/"
									+ smsTextList.size();
					}

					sendSMSResponse = Tools.sendSMS(senderID, receiverID,
							smsText, false);
				}

				if (sendSMSResponse)
					response = SUCCESS;
				else
					response = FAILED;
			}

		} catch (Exception e) {
			logger.error("", e);
			response = ERROR;
		}

		logger.info("response: " + response);
		return response;
	}
}
