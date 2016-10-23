package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorability extends HttpServlet implements Constants 
{
	private static final long serialVersionUID = -7671371024971160833L;
	private static Logger logger = Logger.getLogger(ContentInterOperatorability.class);

	private static Logger contentLogger = Logger.getLogger(ContentInterOperatorability.class.getName() + ".log1");
	private static Logger selectionLogger = Logger.getLogger(ContentInterOperatorability.class.getName() + ".log3");
	private static Logger responseLogger = Logger.getLogger("contentInterOpResponseLogger");
	private static final Logger transactionLog = Logger.getLogger("TransactionLogger");

	private static Map<String, Map<String, String>> operatorContentCategoryMap = new HashMap<String, Map<String,String>>(); 
	private static Map<String, Map<String, String>> operatorSubClassChargeMap = new HashMap<String, Map<String,String>>();

	private static Map<String, String> selectionResponsesMap = new HashMap<String, String>();
	private static Map<String, String> allResponsesMap = new HashMap<String, String>();

	/**
	 * 
	 */
	public ContentInterOperatorability() 
	{
		super();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException 
	{
		super.init(config);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		String subscriberID = request.getParameter("msisdn");
		// transaction_id will be passed in place of mode_info
		String modeInfo = request.getParameter("mode_info");
		String mode = request.getParameter("mode");
		String contentID = request.getParameter("contentid");
		// operator name will be passed in place of msisdn_operator
		String msisdnOperator = request.getParameter("msisdn_operator");
		
		boolean isMnpSupported = RBTParametersUtils.getParamAsBoolean("CONTENT_INTER_OPERATORABILITY", "IS_MNP_SUPPORTED", "FALSE");
		if (isMnpSupported)
		{
			String responseText = ERROR;
			long startTime = System.currentTimeMillis();
			try
			{
				
				responseText = ContentInterOperatorUtility.addContentInterOperatorRequestToDB(request);
			}
			catch (Throwable t)
			{
				logger.error(t.getMessage(), t);
				
				// Write a transaction log with the reason TECHNICAL_DIFFICULTIES.
				String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "TECHNICAL_DIFFICULTIES",
						modeInfo, mode, contentID, msisdnOperator);
				transactionLog.info(errorJson);
				
				responseText = "TECHNICAL_DIFFICULTIES";
			}
			finally
			{
				logger.info("RBT:: Response Text : " + responseText);
				responseText = getFinalResponse(responseText);

				responseLogger.info(request.getRequestURL() + "?" + request.getQueryString() + ", " + responseText + ", " + (System.currentTimeMillis() - startTime));
				response.getWriter().write(responseText);
			}
		}
		else
		{
			String responseText = ERROR;
			long startTime = System.currentTimeMillis();
			try
			{
				if (subscriberID == null || subscriberID.length() == 0 || contentID == null || contentID.length() == 0)
				{
					String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "TECHNICAL_DIFFICULTIES",
							modeInfo, mode, contentID, msisdnOperator);
					transactionLog.info(errorJson);

					responseText = "PARAMETER_MISSING";
					return;
				}

				// gets the operator for the msisdn
				String targetOperator = Utility.getSubscriberOperator(subscriberID, null,mode);
				if (targetOperator == null || targetOperator.equalsIgnoreCase("UNKNOWN")
						|| targetOperator.equalsIgnoreCase("BLOCKED_OPERATOR_CIRCLE"))
				{
					String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "INVALID_NUMBER",
							modeInfo, mode, contentID, msisdnOperator);
					transactionLog.info(errorJson);

					responseText = "INVALID_NUMBER";
					return;
				}

				String sourceOperator = request.getParameter("contentorigin");
				if (sourceOperator == null || sourceOperator.length() == 0)
				{
					Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "DEFAULT_CONTENT_PROVIDER", "VODAFONE");
					sourceOperator = params.getValue();
				}

				if (!sourceOperator.equalsIgnoreCase(targetOperator))
				{
					long contentStartTime = System.currentTimeMillis();
					// gets the contentID for the target operator
					String contentUrl = Utility.m_ContentUrl;
					String url = contentUrl + "sourceCLIPID="+ contentID + "&sourceOperator=" + sourceOperator + "&targetOperator=" + targetOperator + "&extraLogInfo=" + subscriberID + ",CENTRAL_WEB";

					StringBuffer xtraInfo = new StringBuffer();
					String targetClipID = Utility.callContentURL(url, xtraInfo);

					if (targetClipID == null || targetClipID.equalsIgnoreCase("RETRY"))
					{
						responseText = "COPY_CONTENT_ERROR";
						contentLogger.info(url + ", " + responseText + ", " + (System.currentTimeMillis() - contentStartTime));
						
						// Write a transaction log with the reason COPY_CONTENT_ERROR.
						String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "COPY_CONTENT_ERROR",
								modeInfo, mode, contentID, msisdnOperator);
						transactionLog.info(errorJson);

						return;
					}
					else if (targetClipID.equalsIgnoreCase("COPYCONTENTMISSING"))
					{
						responseText = "CONTENT_MISSING";
						contentLogger.info(url + ", " + responseText + ", " + (System.currentTimeMillis() - contentStartTime));
						
						// Write a transaction log with the reason CONTENT_MISSING.
						String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "CONTENT_MISSING",
								modeInfo, mode, contentID, msisdnOperator);
						transactionLog.info(errorJson);
						
						return;
					}
					contentID = targetClipID.trim();
					contentLogger.info(url + ", " + contentID + ", " + (System.currentTimeMillis() - contentStartTime));
				}

				if (mode == null || mode.length() == 0)
				{
					Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "DEFAULT_MODE_FOR_" + targetOperator.toUpperCase(), "VP");
					mode = params.getValue();
				}

				String contentCharge = request.getParameter("contentcharge");
				String categoryID = getCategoryIDByContentCharge(contentCharge, targetOperator);

				String subCharge = request.getParameter("subcharge");
				String subscriptionClass = getSubscriptionClassByContentCharge(subCharge, targetOperator);

				String isActivate = request.getParameter("activatesub");
				if (isActivate == null || isActivate.length() == 0)
					isActivate = "TRUE";

				String inLoopSelection = request.getParameter("addinloop");
				if (inLoopSelection == null || inLoopSelection.length() == 0)
					inLoopSelection = "FALSE";

				HashMap<String, String> requestParams = new HashMap<String, String>();
				requestParams.put(param_MSISDN, subscriberID);
				requestParams.put(param_TONE_ID, contentID);
				requestParams.put(param_SELECTED_BY, mode);
				requestParams.put(param_CATEGORY_ID, categoryID);
				requestParams.put(param_SUBSCRIPTION_CLASS, subscriptionClass);
				requestParams.put(param_ISACTIVATE, isActivate);
				requestParams.put(param_IN_LOOP, inLoopSelection);
				requestParams.put(param_REQUEST, "SELECTION");
				requestParams.put(param_REDIRECT_NATIONAL, "TRUE");

				//gets the operator url
				String operatorURL = Utility.getOperatorURL(targetOperator);

				String selectionResponse = makeSelection(requestParams, targetOperator, operatorURL);
				
				if("TECHFAILURE".equals(selectionResponse)) {
					// Write a transaction log with the reason FAILURE.
					String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "TECHFAILURE",
							modeInfo, mode, contentID, msisdnOperator);
					transactionLog.info(errorJson);
				}

				responseText = getSelectionResponse(selectionResponse);
			}
			catch (Exception e)
			{
				logger.error("RBT:: " + e.getMessage(), e);
				
				// Write a transaction log with the reason FAILURE.
				String errorJson = ContentInterOperatorUtility.convertToJson(subscriberID, "FAILURE",
						modeInfo, mode, contentID, msisdnOperator);
				transactionLog.info(errorJson);
				
				responseText = FAILURE;
			}
			finally
			{
				logger.info("RBT:: Response Text : " + responseText);
				responseText = getFinalResponse(responseText);

				responseLogger.info(request.getRequestURL() + "?" + request.getQueryString() + ", " + responseText + ", " + (System.currentTimeMillis() - startTime));
				response.getWriter().write(responseText);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

	/**
	 * @param contentCharge
	 * @param operatorName
	 * @return
	 */
	public static String getCategoryIDByContentCharge(String contentCharge, String operatorName)
	{
		logger.info("Getting categoryID for contentCharge :: " + contentCharge + ", operatorName : " + operatorName);
		String categoryID = null;

		if (!operatorContentCategoryMap.containsKey(operatorName))
		{
			Map<String, String> catChargeMap = new HashMap<String, String>();
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "CAT_CHARGE_MAP_FOR_" + operatorName.toUpperCase());
			if (params != null && params.getValue() != null)
			{
				String[] chargeCategoryIDs = params.getValue().trim().split(";");
				for (String catChargePair : chargeCategoryIDs)
				{
					String[] str = catChargePair.split(",");
					catChargeMap.put(str[0], str[1]);
				}
			}
			operatorContentCategoryMap.put(operatorName, catChargeMap);
		}

		Map<String, String> chargeCategoryMap = operatorContentCategoryMap.get(operatorName);
		categoryID = chargeCategoryMap.get(contentCharge);

		if (categoryID == null)
		{
			categoryID = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "DEFAULT_CATEGORY_ID_FOR_" + operatorName.toUpperCase(), "6").getValue().trim();
		}

		logger.info("categoryID :: " + categoryID);
		return categoryID;
	}

	/**
	 * @param subCharge
	 * @param operatorName
	 * @return
	 */
	public static String getSubscriptionClassByContentCharge(String subCharge, String operatorName)
	{
/*		logger.info("Getting subscriptionClass for subcharge :: " + subCharge + ", operatorName : " + operatorName);
		String subClass = null;

		if (!operatorSubClassChargeMap.containsKey(operatorName))
		{
			Map<String, String> subClassChargeMap = new HashMap<String, String>();
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "SUBCLASS_CHARGE_MAP_FOR_" + operatorName.toUpperCase());
			if (params != null && params.getValue() != null)
			{
				String[] chargeSubclass = params.getValue().trim().split(";");
				for (String subClassChargePair : chargeSubclass)
				{
					String[] str = subClassChargePair.split(",");
					subClassChargeMap.put(str[0], str[1]);
				}
			}
			operatorSubClassChargeMap.put(operatorName, subClassChargeMap);
		}

		Map<String, String> chargeCategoryMap = operatorSubClassChargeMap.get(operatorName);
		subClass = chargeCategoryMap.get(subCharge);
*/
		if (subCharge == null)
			subCharge = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "DEFAULT_SUBCLASS_FOR_" + operatorName.toUpperCase(), "DEFAULT").getValue().trim();

		logger.info("Subscription Class :: " + subCharge);
		return subCharge;
	}

	/**
	 * @param requestParams
	 * @param targetOperator
	 * @param operatorURL
	 * @return
	 */
	private String makeSelection(HashMap<String, String> requestParams, String targetOperator, String operatorURL) 
	{
		long selectionStartTime = System.currentTimeMillis();
		String response = ERROR;
		if (operatorURL != null && operatorURL.indexOf("//") != -1 && operatorURL.indexOf("/", operatorURL.indexOf("//") + 2) != -1)
			operatorURL = operatorURL.substring(0, operatorURL.indexOf("/", operatorURL.indexOf("//") + 2));

		operatorURL = operatorURL + "/rbt/rbt_promotion.jsp?";
		logger.info("RBT:: operator Selection URL: " + operatorURL);
		logger.info("Request params: " + requestParams);
		HttpParameters httpParameters = new HttpParameters(operatorURL);
		try 
		{
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, requestParams);
			logger.info("RBT:: httpResponse: " + httpResponse);
			response = httpResponse.getResponse();
		}
		catch (Exception e) {
			response = TECHFAILURE;
			logger.error("RBT:: " + e.getMessage(), e);
		}
		
		selectionLogger.info(operatorURL + ", " + requestParams + ", " + response + ", " + (System.currentTimeMillis() - selectionStartTime));
		return response;
	}

	/**
	 * @param response
	 * @return
	 */
	private String getSelectionResponse(String response)
	{
		logger.info("Response from promotion jsp : " + response);
		if (selectionResponsesMap.isEmpty())
		{
			String allResponsesStr = null;
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "ALL_SELECTION_RESPONSES");
			if (params != null && params.getValue() != null)
				allResponsesStr = params.getValue();

			if (allResponsesStr != null)
			{
				String[] responses = allResponsesStr.split(",");
				for (String eachResponse : responses)
				{
					String configuredResponse = CacheManagerUtil.getParametersCacheManager().getParameterValue("RDC", "SEL_RESPONSE_" + eachResponse.toUpperCase(), eachResponse);
					selectionResponsesMap.put(eachResponse, configuredResponse);
				}
			}
		}

		if (selectionResponsesMap.containsKey(response))
			response = selectionResponsesMap.get(response);

		logger.info("Configured Selection response : " + response);
		return response;
	}

	private String getFinalResponse(String response)
	{
		if (!allResponsesMap.containsKey(response))
		{
			String configuredResponse = CacheManagerUtil.getParametersCacheManager().getParameterValue("RDC", "XOP_SEL_RESPONSE_" + response.toUpperCase(), response);
			allResponsesMap.put(response, configuredResponse);
		}

		if (allResponsesMap.containsKey(response))
			response = allResponsesMap.get(response);

		return response;
	}
}
