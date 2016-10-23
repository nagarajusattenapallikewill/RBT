package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao.ContentInterOperatorRequestDao;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.subscriptions.Utility;
import com.onmobile.mnp.MnpService;
import com.onmobile.mnp.MnpServiceFactory;
import com.onmobile.mnp.dataStore.Circle;
import com.onmobile.mnp.model.CustomerCircle;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorUtility
{
	private static Logger logger = Logger.getLogger(ContentInterOperatorUtility.class);
	private static Logger operatorNotSupportedLogger = Logger.getLogger(ContentInterOperatorUtility.class.getName()+".operator_msisdn");
	private static final Logger transactionLog = Logger.getLogger("TransactionLogger");
	
	private static DocumentBuilder documentBuilder = null;
	public static String localDir = null;
	public static Object lock = new Object();

	private static Map<String, String> operatorIdOperatorRBTNameMap = new HashMap<String, String>();
	public static Map<String, String> operatorNameUrlMap = new HashMap<String, String>();
	
	public static HashMap<Integer, Integer> operatorIdsInterchangeMap = new HashMap<Integer, Integer>();
	public static HashMap<Integer, HashSet<String>> operatorIdInterchangeCirclesMap = new HashMap<Integer, HashSet<String>>();
	
	private static Map<String, Integer> operatorMNPNameOperatorIDMap = new HashMap<String, Integer>();
	
	private static Map<String, Map<String, String>> crossOperatorSubClassMap = null;
	private static Map<String, Map<String, String>> crossOperatorChargeClassMap = null;
	
	private static Object object = new Object();
	private static Object object1 = new Object();

	static
	{
		try
		{
			if (documentBuilder == null)
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			initOperatorNameAndUrlMap();
			initializeInterchageOperatorIdCircleMap();
		}
		catch (ParserConfigurationException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param file
	 */
	public static void processXmlFile(File file, String type) 
	{
		try 
		{
			Document document = null;
			synchronized(lock)
			{
				document = documentBuilder.parse(file);
			}

			Element rootElement = (Element) document.getElementsByTagName("MnpCustomerCircleInfo").item(0);
			Element successRecordsElem = (Element) rootElement.getElementsByTagName("SuccessRecords").item(0);
			NodeList successList = successRecordsElem.getElementsByTagName("SuccessRecord");
			for (int i = 0; i < successList.getLength(); i++)
			{
				Element successElem = (Element) successList.item(i);
				String msisdn = successElem.getElementsByTagName("msisdn").item(0).getFirstChild().getNodeValue();
				String operator = successElem.getElementsByTagName("customer").item(0).getFirstChild().getNodeValue();
				String circleId = successElem.getElementsByTagName("circle").item(0).getFirstChild().getNodeValue();
				
				int operatorID = getOperatorIDFromMNPOperatorName(operator);
				operatorID = getInterchangedOperatorId(operatorID, circleId);

				ArrayList<Integer> unidentifiedStatusList = new ArrayList<Integer>();
				unidentifiedStatusList.add(0);
				unidentifiedStatusList.add(1);
				List<ContentInterOperatorRequestBean> pendingContentRequestBeans = ContentInterOperatorRequestDao.listForMsisdnAndInStatus(msisdn, unidentifiedStatusList);
				if (pendingContentRequestBeans != null)
				{
					for (ContentInterOperatorRequestBean requestBean : pendingContentRequestBeans)
					{
						requestBean.setMsisdn(msisdn);
						requestBean.setOperatorID(operatorID);
						if(Utility.isRequestBlockedForModeOperatorCircle(requestBean.getMode(), circleId, operator)){
							requestBean.setStatus(3);
						}else{
						    requestBean.setStatus(2);
						}
						requestBean.setMnpResponseTime(Calendar.getInstance().getTime());
						requestBean.setMnpResponseType(type);
						ContentInterOperatorRequestDao.update(requestBean);
					}
				}
			}

			Element failureRecordsElem = (Element) rootElement.getElementsByTagName("FailureRecords").item(0);
			NodeList failureList = failureRecordsElem.getElementsByTagName("msisdn");
			for (int i = 0; i < failureList.getLength(); i++)
			{
				String msisdn = failureList.item(i).getFirstChild().getNodeValue();

				List<ContentInterOperatorRequestBean> pendingContentRequestBeans = ContentInterOperatorRequestDao.listForMsisdnAndStatus(msisdn, 1);
				if (pendingContentRequestBeans != null)
				{
					for (ContentInterOperatorRequestBean requestBean : pendingContentRequestBeans)
					{
						requestBean.setMsisdn(msisdn);
						requestBean.setStatus(3);
						requestBean.setMnpResponseTime(Calendar.getInstance().getTime());
						requestBean.setMnpResponseType(type);
						ContentInterOperatorRequestDao.update(requestBean);
					}
				}
			}

			String targetPath = file.getAbsolutePath();
			if (targetPath.endsWith(".tmp"))
				targetPath = targetPath.substring(0, targetPath.indexOf(".tmp"));
			
			copyFile(file, new File(targetPath + ".done"));
			file.delete();
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
		catch (SAXException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public static void copyFile(File source, File destination) throws IOException
	{
		FileChannel sourceFileChannel = null;
		FileChannel destinationFileChannel = null;
		try
		{
			sourceFileChannel = (new FileInputStream(source)).getChannel();
			destinationFileChannel = (new FileOutputStream(destination)).getChannel();
			sourceFileChannel.transferTo(0, source.length(), destinationFileChannel);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (sourceFileChannel != null)
					sourceFileChannel.close();
				if (destinationFileChannel != null)
					destinationFileChannel.close();
			}
			catch (IOException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * @param request
	 * @return
	 */
	public static String addContentInterOperatorRequestToDB(HttpServletRequest request)
	{
		String responseText = "ERROR";
		
		String subscriberID = request.getParameter("msisdn");
		String contentID = request.getParameter("contentid");
		String sourceOperator = request.getParameter("contentorigin");
		String mode = request.getParameter("mode");
		String contentCharge = request.getParameter("contentcharge");
		String subCharge = request.getParameter("subcharge");
		String inLoopSelection = request.getParameter("addinloop");
		// operator name will be passed in place of msisdn_operator
		String msisdnOperator = request.getParameter("msisdn_operator");
		// transaction_id will be passed in place of mode_info
		String modeInfo = request.getParameter("mode_info");
		//if operator_contentid(target_contentid) is present then daemon will process directly. It will not do Atlantis check. 
		//dependency is msisdn_operator parameter should be present in the url.
		String msisdnOperatorToneId = request.getParameter("operator_contentid");
		
		 // Affiliate portal changes
		String useUiChargeClass = request.getParameter("USE_UI_CHARGE_CLASS");
		
        String fromTime = request.getParameter("fromTime");
        String fromTimeInMins = request.getParameter("fromTimeMinutes");
        String toTime = request.getParameter("toTime");
        String toTimeInMins = request.getParameter("toTimeMinutes");
        
        String interval = request.getParameter("interval");

        String selStartTime = request.getParameter("selectionStartTime");
        String selEndTime = request.getParameter("selectionEndTime");
        
        String msisdnCircle = request.getParameter("msisdn_circle");
        String callerId = request.getParameter("callerId");
        // Affiliate portal changes ends
        
		try
		{
			if (subscriberID == null || subscriberID.length() == 0 || ((contentID == null || contentID.length() == 0) && (msisdnOperatorToneId == null || msisdnOperatorToneId.length() == 0)))
			{
				// Write a transaction log with the reason parameter missing.
				String jsonString = convertToJson(subscriberID, "PARAMETER_MISSING", modeInfo, mode, contentID, msisdnOperator);
				transactionLog.info(jsonString);
				
				return "PARAMETER_MISSING";
			}

			ContentInterOperatorRequestBean contentBean = new ContentInterOperatorRequestBean();
			contentBean.setMsisdn(subscriberID);
			contentBean.setSourceContentID(contentID);
			contentBean.setSourceContentOperator(sourceOperator);
			contentBean.setMode(mode);
			contentBean.setSubCharge(subCharge);
			contentBean.setContentCharge(contentCharge);
			contentBean.setAddInLoop(inLoopSelection);
			
	        // Affiliate portal changes
			StringBuilder sb = new StringBuilder();
			if(useUiChargeClass != null) {
				sb.append("useUiChargeClass").append("=").append(useUiChargeClass);
			}
			
			if(fromTime != null) {
				appendPipe(sb);
				sb.append("fromTime").append("=").append(fromTime);
			}
			if(fromTimeInMins != null) {
				appendPipe(sb);
				sb.append("fromTimeMinutes").append("=").append(fromTimeInMins);
			}
			if(toTime != null) {
				appendPipe(sb);
				sb.append("toTime").append("=").append(toTime);
			}
			
			if(toTimeInMins != null) {
				appendPipe(sb);
				sb.append("toTimeMinutes").append("=").append(toTimeInMins);
			}
			
			if(null != interval) {
				appendPipe(sb);
				sb.append("interval").append("=").append(interval);
			}
			
			if(null != selStartTime) {
				appendPipe(sb);
				sb.append("selectionStartTime").append("=").append(selStartTime);
			}
			
			if(null != selEndTime) {
				appendPipe(sb);
				sb.append("selectionEndTime").append("=").append(selEndTime);
			}

			if(null != callerId) {
				appendPipe(sb);
				sb.append("callerId").append("=").append(callerId);
				
			}
			
			logger.debug("ContentBean extra info contains: " + sb.toString());
			contentBean.setExtraInfo(sb.toString());
			
	        // Affiliate portal changes ends
			
			if(msisdnOperator!=null && msisdnOperator.length()>0){
		    //For Skipping MNP HIT for those requests which comes with msisdn_operator parameter
				int operatorID = getOperatorIDFromMNPOperatorName(msisdnOperator);
			    if(operatorID == 0){
			    	operatorNotSupportedLogger.info("Request rejected for SubscriberId = "+subscriberID+" MSISDN_OPERATOR = "+msisdnOperator);
			    	
					// Write a transaction log with the reason OPERATOR_NOT_SUPPORTED.
			    	String jsonString = convertToJson(subscriberID, "OPERATOR_NOT_SUPPORTED", modeInfo, mode, contentID, msisdnOperator);
			    	transactionLog.info(jsonString);
			    	
			    	return "OPERATOR_NOT_SUPPORTED";
			    }
//			    Map<String, String> subClassParamsMap = getCrossOperatorSubClassMap().get(msisdnOperator);
//		    	Map<String, String> contentChargeParamsMap = getCrossOperatorChargeClassMap().get(msisdnOperator);
//			    
//			    if (subCharge != null && subClassParamsMap != null) {
//			    	String tempSubCharge = subClassParamsMap.get(subCharge);
//			    	if (tempSubCharge != null) {
//			    		subCharge = tempSubCharge;
//			    	}
//			    	contentBean.setSubCharge(subCharge);					
//			    }
//			    
//			    if (contentCharge != null && contentChargeParamsMap != null) {
//			    	String tempContentCharge = contentChargeParamsMap.get(contentCharge);
//			    	if (tempContentCharge != null) {
//			    		contentCharge = tempContentCharge;
//			    	}
//			    	contentBean.setContentCharge(contentCharge);
//			    }
			    
			    contentBean.setOperatorID(operatorID);
			    contentBean.setStatus(2);
			    if(msisdnOperatorToneId != null) {
			    	contentBean.setStatus(4);
			    	contentBean.setTargetContentID(msisdnOperatorToneId);			    	
			    }
		        contentBean.setMnpRequestTime(Calendar.getInstance().getTime());
			    contentBean.setMnpResponseTime(Calendar.getInstance().getTime());
			    contentBean.setMnpRequestType("DIRECT");
			    contentBean.setMnpResponseType("DIRECT");
			}else{
				contentBean.setStatus(0);
			}

			if (msisdnCircle == null || msisdnOperator == null) {
				MNPContext mnpContext = new MNPContext(subscriberID);
				mnpContext.setMode("ONLINE");
				SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(mnpContext);
				if (subscriberDetail != null) {
					if (msisdnCircle == null) {
						msisdnCircle = subscriberDetail.getMnpCircleName();
					}
					if (msisdnOperator == null) {
						msisdnOperator = subscriberDetail.getMnpOperatorName();
					}
					logger.info("mnp_operator_name = " + msisdnOperator + " mnp_circle_name = "
							+ msisdnCircle);
				}
			}
            boolean isRequestBlocked = Utility.isRequestBlockedForModeOperatorCircle(mode,msisdnCircle,msisdnOperator);
            if(isRequestBlocked){
            	return "BLOCKED_OPERATOR_CIRCLE";
            }

			contentBean.setRequestTime(Calendar.getInstance().getTime());
			contentBean.setRequestType("CONTENT_INTEROPERATORABILITY");
            contentBean.setModeInfo(modeInfo);
			logger.debug("Saving content interoperator details. contentBean: "
					+ contentBean);
			Long sequenceID = ContentInterOperatorRequestDao.save(contentBean);
			if (sequenceID == null) {
				
				// Write a transaction log with the reason TECHNICAL_DIFFICULTIES.
				String jsonString = convertToJson(subscriberID, "TECHNICAL_DIFFICULTIES", modeInfo, mode, contentID, msisdnOperator);
		    	transactionLog.info(jsonString);
		    	
		    	responseText = "FAILURE";
			} else
				responseText = "SUCCESS";
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			
			// Write a transaction log with the reason TECHNICAL_DIFFICULTIES.
			String jsonString = convertToJson(subscriberID, "TECHNICAL_DIFFICULTIES", modeInfo, mode, contentID, msisdnOperator);
	    	transactionLog.info(jsonString);
	    	
			responseText = "TECHNICAL_DIFFICULTIES";
		}

		logger.info("Response while adding viral data : " + responseText);
		return responseText;
	}

	private static void appendPipe(StringBuilder sb) {
		if(sb.length() > 0) {
			sb.append("|");
		}
	}
	
	/**
	 * @param operatorId
	 * @return
	 */
	public static String getRBTOperatorNameFromOperatorID(String operatorId) 
	{
		String operatorRBTName = null;
		if (operatorIdOperatorRBTNameMap.isEmpty())
		{
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("CONTENT_INTER_OPERATORABILITY", "OPERATOR_ID_OPERATOR_RBT_NAME_MAP");
			if (params != null && params.getValue() != null)
			{
				String opNameOpIDMapStr = params.getValue();
				String[] nameIDPairs = opNameOpIDMapStr.split(";");
				for (String eachPair : nameIDPairs)
				{
					String[] str = eachPair.split(",");
					operatorIdOperatorRBTNameMap.put(str[0], str[1]);
				}
				logger.info("operatorIdOperatorRBTNameMap = " + operatorIdOperatorRBTNameMap);
			}
		}

		if (operatorIdOperatorRBTNameMap.containsKey(operatorId))
			operatorRBTName = operatorIdOperatorRBTNameMap.get(operatorId);

		return operatorRBTName;
	}
	
	/**
	 * 
	 */
	private static void initOperatorNameAndUrlMap()
	{
		List<SitePrefix> sitePrefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (sitePrefixes != null && sitePrefixes.size() > 0)
		{
			for(int i = 0; i < sitePrefixes.size(); i++)
				operatorNameUrlMap.put(sitePrefixes.get(i).getSiteName(), sitePrefixes.get(i).getSiteUrl());
		}
		logger.info("operatorNameUrlMap = " + operatorNameUrlMap);
	}

	/**
	 * @return {@link crossOperatorSubClassMap}
	 */
	public static Map<String, Map<String, String>> getCrossOperatorSubClassMap() {
		Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("CONTENT_INTER_OPERATORABILITY", "CROSS_OPERATOR_SUB_CLASS");
		if (crossOperatorSubClassMap == null && params != null) {
			synchronized (object) {				
				if (crossOperatorSubClassMap == null && params != null) {
					crossOperatorSubClassMap = new HashMap<String, Map<String, String>>();
					try {
						if (params != null && params.getValue() != null) {
							logger.debug(params.getValue());
							for (String operatorWiseSplit : params.getValue().split(";")) {
								String operatorParamsSplit[] = operatorWiseSplit.split(":");
								String operator = operatorParamsSplit[0];
								String paramValPairs = operatorParamsSplit[1];
								Map<String, String> subMap = new HashMap<String, String>();
								for (String param : paramValPairs.split("\\|")) {
									String paramSplit[]	= param.split("=");
									String name = paramSplit[0];
									String value = paramSplit[1];
									subMap.put(name, value);
								}
								crossOperatorSubClassMap.put(operator, subMap);
								logger.debug(crossOperatorSubClassMap);
							}
						}
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.error("Invalid parameter - "  + params.getValue() + " " + e.getMessage(), e);
					}
				}
			}
		}
		
		return crossOperatorSubClassMap;
	}

	/**
	 * @return {@link crossOperatorChargeClassMap}
	 */
	public static Map<String, Map<String, String>> getCrossOperatorChargeClassMap() {
		Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("CONTENT_INTER_OPERATORABILITY", "CROSS_OPERATOR_CHARGE_CLASS");
		if (crossOperatorChargeClassMap == null && params != null) {
			synchronized (object1) {
				if (crossOperatorChargeClassMap == null && params != null) {
					crossOperatorChargeClassMap = new HashMap<String, Map<String, String>>();
					try {
						if (params != null && params.getValue() != null) {
							logger.debug(params.getValue());
							for (String operatorWiseSplit : params.getValue().split(";")) {
								String operatorParamsSplit[] = operatorWiseSplit.split(":");
								String operator = operatorParamsSplit[0];
								String paramValPairs = operatorParamsSplit[1];
								Map<String, String> subMap = new HashMap<String, String>();
								for (String param : paramValPairs.split("\\|")) {
									String paramSplit[]	= param.split("=");
									String name = paramSplit[0];
									String value = paramSplit[1];
									subMap.put(name, value);
								}
								crossOperatorChargeClassMap.put(operator, subMap);
								logger.debug(crossOperatorChargeClassMap);
							}
						}
					} catch(ArrayIndexOutOfBoundsException e) {
						logger.error("Invalid parameter - "  + params.getValue() + " " + e.getMessage(), e);
					}
				}
			}
		}
		return crossOperatorChargeClassMap;
	}
	/**
	 * @param operator
	 * @return
	 */
	public static int getOperatorIDFromMNPOperatorName(String operator) 
	{
		int operatorID = 0;
		if (operatorMNPNameOperatorIDMap.isEmpty())
		{
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("CONTENT_INTER_OPERATORABILITY", "OPERATOR_MNP_NAME_OPERATOR_ID_MAP");
			if (params != null && params.getValue() != null)
			{
				String opNameOpIDMapStr = params.getValue();
				String[] nameIDPairs = opNameOpIDMapStr.split(";");
				for (String eachPair : nameIDPairs)
				{
					String[] str = eachPair.split(",");
					operatorMNPNameOperatorIDMap.put(str[0], Integer.parseInt(str[1]));
				}
				logger.info("operatorMNPNameOperatorIDMap = "	+ operatorMNPNameOperatorIDMap);
			}
		}

		if (operatorMNPNameOperatorIDMap.containsKey(operator))
			operatorID = operatorMNPNameOperatorIDMap.get(operator);

		return operatorID;
	}
	
	public static int getInterchangedOperatorId(int initialOperatorId, String circleId)
	{
		if(operatorIdsInterchangeMap.containsKey(initialOperatorId) && operatorIdInterchangeCirclesMap.containsKey(initialOperatorId))
		{
			int finalOperatorId = operatorIdsInterchangeMap.get(initialOperatorId);
			HashSet<String> circlesSet = operatorIdInterchangeCirclesMap.get(initialOperatorId);
			if(circlesSet.contains(circleId)) {
				return finalOperatorId;
			}
		}
		return initialOperatorId;
	}
	
	private static void initializeInterchageOperatorIdCircleMap()
	{
		Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("CONTENT_INTER_OPERATORABILITY", "INTERCHANGE_OPERATOR_ID_MAP");
		if (params == null || params.getValue() == null)
			return;
		
		String opNameOpIDMapStr = params.getValue();
		String[] nameIDPairs = opNameOpIDMapStr.split(";");
		for (String eachPair : nameIDPairs)
		{
			String[] str = eachPair.split(":");
			if(str.length != 3)
				continue;
			operatorIdsInterchangeMap.put(new Integer(str[0]), new Integer(str[1]));
			String[] circles = str[2].split(",");
			HashSet<String> circlesSet = new HashSet<String>();
			for (String string : circles)
				circlesSet.add(string);
			operatorIdInterchangeCirclesMap.put(new Integer(str[0]), circlesSet);
		}
		logger.info("operatorIdsInterchangeMap="+operatorIdsInterchangeMap+", operatorIdInterchangeCirclesMap="+operatorIdInterchangeCirclesMap);
	}
	
	public static String convertToJson(String msisdn,
			String reasonForFailure, String transactionId,
			String affiliateMode, String endToneId, String endOperatorId) {
		JSONObject jsonObject = new JSONObject();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyyMMddhhmmssSSS");
		try {
			String timestamp = sdf.format(new Date());
			
			jsonObject.append("timestamp", timestamp);
			jsonObject.append("msisdn", msisdn);
			jsonObject.append("reasonForFailure", reasonForFailure);
			jsonObject.append("transactionId", transactionId);
			jsonObject.append("affiliateMode", affiliateMode);
			jsonObject.append("endToneId", endToneId);
			jsonObject.append("endOperatorId", endOperatorId);
			
		} catch (JSONException je) {
			logger.error("Failed to construct JSON string. Error message: "
					+ je.getMessage(), je);
		}
		return jsonObject.toString();
	}
}
