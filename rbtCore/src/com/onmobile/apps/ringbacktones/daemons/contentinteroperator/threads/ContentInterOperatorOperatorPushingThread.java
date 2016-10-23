package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao.ContentInterOperatorRequestDao;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;
import com.onmobile.apps.ringbacktones.subscriptions.ContentInterOperatorability;
import com.onmobile.apps.ringbacktones.utils.MapUtils;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorOperatorPushingThread extends Thread
{
	private ContentInterOperatorDBFetcher dbFetcher = null;
	private static Logger logger = Logger.getLogger(ContentInterOperatorDBFetcher.class);
	private static Logger oprPushlogger = Logger.getLogger("oprPushLogger");
	private static final Logger transactionLog = Logger.getLogger("TransactionLogger");

	/**
	 * @param dbFetcher
	 */
	public ContentInterOperatorOperatorPushingThread(ContentInterOperatorDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while(true)
		{
			ContentInterOperatorRequestBean contentRequest = null; 
			synchronized (dbFetcher.contentQueue)
			{
				if(dbFetcher.contentQueue.size() > 0)
				{
					logger.info("Operator push  thread found contentrequest." + dbFetcher.contentQueue.get(0));
					contentRequest = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(contentRequest);
				}
				else
				{
					try
					{
						logger.info("Operator push thread waiting as queue size = " + dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					}
					catch (InterruptedException e)
					{
						logger.info("Mnp thread interrupted. Will check queue now");
					}
					continue;
				}	
			}

			String  processedOperatorResponse = hitOperator(contentRequest);
			contentRequest.setRequestTransferTime(Calendar.getInstance().getTime());
			if (processedOperatorResponse.equals("SUCCESS"))
			{
				contentRequest.setStatus(6);
				ContentInterOperatorRequestDao.delete(contentRequest.getSequenceID());
			}
			else if (processedOperatorResponse.equals("FAILURE"))
			{
				contentRequest.setStatus(7);
				ContentInterOperatorRequestDao.delete(contentRequest.getSequenceID());
			}

			dbFetcher.pendingQueue.remove(contentRequest);
		}
	}

	/**
	 * @param contentRequest
	 * @return
	 */
	private String hitOperator(ContentInterOperatorRequestBean contentRequest)
	{
		String operatorName = ContentInterOperatorUtility.getRBTOperatorNameFromOperatorID(String.valueOf(contentRequest.getOperatorID()));
		String url = getOperatorUrl(operatorName);
		if(url == null)
		{
			logger.info("Operator url not found for sequenceId = " + contentRequest.getSequenceID());
			return "INVALID_OPERATOR_ID";
		}

		if(!operatorName.equals("RELIANCE") && !operatorName.equals("UNINOR_COMVIVA")) {
			if (url != null && url.indexOf("//") != -1 && url.indexOf("/", url.indexOf("//") + 2) != -1)
				url = url.substring(0, url.indexOf("/", url.indexOf("//") + 2));
			
			if(operatorName.equals("AIRTEL")) {
				url = url + "/startcopy/rbt_rdc_to_cgi_song_selection.do?";
			}
			else {
				url = url + "/rbt/rbt_rdc_song_selection.do?";
			}
		
		}

		String contentCharge = contentRequest.getContentCharge();
		String categoryID = ContentInterOperatorability.getCategoryIDByContentCharge(contentCharge, operatorName);

		String subCharge = contentRequest.getSubCharge();
		
		
		Map<String, String> subClassParamsMap = ContentInterOperatorUtility.getCrossOperatorSubClassMap().get(operatorName);
    	Map<String, String> contentChargeParamsMap = ContentInterOperatorUtility.getCrossOperatorChargeClassMap().get(operatorName);
	    
	    if (subCharge != null && subClassParamsMap != null) {
	    	String tempSubCharge = subClassParamsMap.get(subCharge);
	    	if (tempSubCharge != null) {
	    		subCharge = tempSubCharge;
	    	}
//	    	contentBean.setSubCharge(subCharge);					
	    }
	    
	    if (contentCharge != null && contentChargeParamsMap != null) {
	    	String tempContentCharge = contentChargeParamsMap.get(contentCharge);
	    	if (tempContentCharge != null) {
	    		contentCharge = tempContentCharge;
	    	}
//	    	contentBean.setContentCharge(contentCharge);
	    }
		
		String subClass = ContentInterOperatorability.getSubscriptionClassByContentCharge(subCharge, operatorName);

		String addInLoop = null;
		if (contentRequest.getAddInLoop() != null && contentRequest.getAddInLoop().equalsIgnoreCase("true"))
			addInLoop = "yes";

		String mode = contentRequest.getMode();
		if (mode == null || mode.length() == 0)
		{
			mode = RBTParametersUtils.getParamAsString("RDC", "DEFAULT_MODE_FOR_" + operatorName.toUpperCase(), "VP");
		}

		HashMap<String, String> parametersMap = new HashMap<String, String>();
		if(operatorName.equals("RELIANCE"))
		{
			parametersMap.put("MDN", contentRequest.getMsisdn()+"");
			parametersMap.put("tuneCode", contentRequest.getTargetContentID());
			parametersMap.put("BMdn", null);
		}
		else if(operatorName.equals("UNINOR_COMVIVA")) {
			parametersMap.put("interface", "OBD");
			parametersMap.put("msgType", "tprov");
			parametersMap.put("subscriberId", contentRequest.getMsisdn()+"");
			parametersMap.put("vcode", contentRequest.getTargetContentID());
			parametersMap.put("pack", "default");
			parametersMap.put("provisioningInterface", "w");
			String subscriptionPlanId = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "SUPSCRIPTION_PLAN_ID_" + operatorName.toUpperCase(), "VAS0003ALL_40");
			parametersMap.put("subscription_plan_id", subscriptionPlanId);
		}
		else 
		{
			// pass charge class, time based settings
			parametersMap.put("MSISDN", contentRequest.getMsisdn());
			parametersMap.put("TONE_ID", contentRequest.getTargetContentID());
			parametersMap.put("CATEGORY_ID", categoryID);
			parametersMap.put("ADD_IN_LOOP", addInLoop);
			parametersMap.put("SUB_CLASS", subClass);
			parametersMap.put("MODE", mode);
			parametersMap.put("STATUS", String.valueOf(contentRequest.getStatus()));
			parametersMap.put("MODE_INFO", contentRequest.getModeInfo());

			if(contentCharge != null)
				parametersMap.put("CHARGE_CLASS", contentCharge);
			
			// Affiliate portal changes
			String extraInfo = contentRequest.getExtraInfo();
			Map<String, String> map = MapUtils.convertToMap(extraInfo, "|", "=", null);
			logger.debug("Content extra info contains: " + extraInfo);
			
			if (map.containsKey("useUiChargeClass")) {
				parametersMap.put("USE_UI_CHARGE_CLASS", map.get("useUiChargeClass"));
			}
			
			if (map.containsKey("fromTime")) {
				parametersMap.put("FROM_TIME", map.get("fromTime"));
			}
			if (map.containsKey("fromTimeMinutes")) {
				parametersMap.put("FROM_TIME_MINUTES",
						map.get("fromTimeMinutes"));
			}
			if (map.containsKey("toTime")) {
				parametersMap.put("TO_TIME", map.get("toTime"));
			}
			if (map.containsKey("toTimeMinutes")) {
				parametersMap.put("TO_TIME_MINUTES", map.get("toTimeMinutes"));
			}

			if (map.containsKey("interval")) {
				parametersMap.put("INTERVAL", map.get("interval"));
			}

			if (map.containsKey("selectionStartTime")) {
				parametersMap.put("SELECTION_START_TIME",
						map.get("selectionStartTime"));
			}
			if (map.containsKey("selectionEndTime")) {
				parametersMap.put("SELECTION_END_TIME",
						map.get("selectionEndTime"));
			}
			if (map.containsKey("callerId")) {
				parametersMap.put("CALLER_ID",
						map.get("callerId"));
			}

			// Affiliate portal changes ends
		}
		
		ContentInterOperatorHttpResponse ioHttpResponse = ContentInterOperatorHttpUtils.getResponse(url, parametersMap, null);
		String httpResponseString = ioHttpResponse.getHttpResponseString();
		if (httpResponseString != null && httpResponseString.length() > 30)
			httpResponseString = httpResponseString.substring(0,30);

		oprPushlogger.info(ioHttpResponse.getFinalUrl() + "!" + ioHttpResponse.getHttpResponseCode() + "!" + httpResponseString);
		if(ioHttpResponse.getHttpResponseCode() == 200)
		{
			String responseString = ioHttpResponse.getHttpResponseString();
			
			if (null != responseString && !responseString.equalsIgnoreCase("SUCCESS")) {
				// Write a transaction log with the reason response.
				String errorJson = ContentInterOperatorUtility.convertToJson(contentRequest.getMsisdn(), responseString,
						contentRequest.getModeInfo(), contentRequest.getMode(), contentRequest.getSourceContentID(), operatorName);
				transactionLog.info(errorJson);
			}

			if(responseString == null)
				return "FAILURE";

			responseString = responseString.trim();
			if (responseString.equalsIgnoreCase("SUCCESS"))
				return "SUCCESS";
		}
		return "FAILURE";
	}

	/**
	 * @param operatorName
	 * @return
	 */
	private String getOperatorUrl(String  operatorName)
	{
		if(operatorName == null)
			return null;

		String url = ContentInterOperatorUtility.operatorNameUrlMap.get(operatorName);
		return url;
	}

}
