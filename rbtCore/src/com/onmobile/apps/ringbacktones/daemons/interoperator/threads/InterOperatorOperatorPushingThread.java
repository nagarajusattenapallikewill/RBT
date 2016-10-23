package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.subscriptions.Utility;

public class InterOperatorOperatorPushingThread extends Thread
{
	private InterOperatorDBFetcher dbFetcher = null;
	static Logger logger = Logger.getLogger(InterOperatorDBFetcher.class);
	static private SimpleDateFormat m_ttslFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	static Logger oprPushlogger = Logger.getLogger(InterOperatorOperatorPushingThread.class);
	static Logger copyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestDao.class);
	static Logger oldCopyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestBean.class);
	static URLCodec m_urlEncoder = new URLCodec();
	public InterOperatorOperatorPushingThread(InterOperatorDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}
	
	public void run()
	{
		while(true)
		{
			InterOperatorCopyRequestBean copyRequest = null; 
			try
			{
				synchronized (dbFetcher.copyQueue)
				{
					if(dbFetcher.copyQueue.size() > 0)
					{
						logger.info("Operator push  thread found copyrequest."+dbFetcher.copyQueue.get(0));
						copyRequest = dbFetcher.copyQueue.remove(0);
						dbFetcher.pendingQueue.add(copyRequest);
					}
					else
					{
						try {
							logger.info("Operator push thread waiting as queue size="+dbFetcher.copyQueue.size());
							dbFetcher.copyQueue.wait();
						} catch (InterruptedException e) {
							logger.info("Mnp thread interrupted. Will check queue now");
						}
						continue;
					}	
				}
				
				String  processedOperatorResponse = hitOperator(copyRequest);
				copyRequest.setRequestTransferTime(Calendar.getInstance().getTime());
				if(processedOperatorResponse.equals("SUCCESS"))
				{
					copyRequest.setStatus(6);
					copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(copyRequest));
					oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(copyRequest));
					InterOperatorCopyRequestDao.delete(copyRequest.getCopyId());
				}
				else if (processedOperatorResponse.equals("FAILURE"))
				{
					copyRequest.setStatus(7);
					copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(copyRequest));
					oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(copyRequest));
					InterOperatorCopyRequestDao.delete(copyRequest.getCopyId());
				}
				else
				{
					int retryCount = copyRequest.getTransferRetryCount()+1;
					if(retryCount > 2)
					{
						copyRequest.setStatus(7);
						copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(copyRequest));
						oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(copyRequest));
						InterOperatorCopyRequestDao.delete(copyRequest.getCopyId());
					}
					else
					{
						copyRequest.setTransferRetryCount(copyRequest.getTransferRetryCount()+1);
						InterOperatorCopyRequestDao.update(copyRequest);
					}
					
				}
			}
			catch(Exception e)
			{
				logger.error("Exception", e);
			}
			if(copyRequest != null)
				dbFetcher.pendingQueue.remove(copyRequest);
		}
	}
	
	private String hitOperator(InterOperatorCopyRequestBean copyRequest)
	{
		String  returnValue = "SUCCESS";
		String operatorName = InterOperatorUtility.getRBTOperatorNameFromOperatorID(copyRequest.getCopierOperatorId()+"");
		Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "SUCCESS_RESPONSE_SUB_STRING");
		String srtSuccessSubString = "Successfully";
		if(params != null) {
			srtSuccessSubString = params.getValue();
		}
		String url = getOperatorUrl(operatorName);
		if(url == null)
		{
			logger.info("Operator url not found for copyId="+copyRequest.getCopyId());
			return "FAILURE";
		}	
		// Changes done for RL-27488
		String rdcURL = RBTParametersUtils.getParamAsString("RDC",
				"CROSS_OPERATOR_URL_PARAM_MAP", null);
		if (rdcURL != null && !rdcURL.isEmpty())
			rdcURL = getReplacedRdcURL(rdcURL, copyRequest, operatorName);
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		if (rdcURL != null && !rdcURL.isEmpty()) {
			String[] urlParams = rdcURL.split("&");
			if (urlParams != null && urlParams.length > 0) {
				for (String param : urlParams) {
					String[] paramKeyValues = param.split("=");
					if (paramKeyValues != null && paramKeyValues.length > 1) {
						parametersMap.put(paramKeyValues[0], paramKeyValues[1]);
					} else if (paramKeyValues != null
							&& paramKeyValues.length == 1) {
						parametersMap.put(paramKeyValues[0], "");
					}
				}
			}
		}
		// Changes ended for RL-27488
		InterOperatorHttpResponse ioHttpResponse = InterOperatorHttpUtils.getResponse(url, parametersMap, null);
		String httpResponseString = ioHttpResponse.getHttpResponseString();
		if(httpResponseString != null && httpResponseString.length() > 30)
			httpResponseString = httpResponseString.substring(0,30);
		oprPushlogger.info(ioHttpResponse.getFinalUrl()+"!"+ioHttpResponse.getHttpResponseCode()+"!"+httpResponseString);
		if(ioHttpResponse.getHttpResponseCode() == 200)
		{
			String responseString = ioHttpResponse.getHttpResponseString();
			if(responseString == null)
				return "FAILURE";
			responseString = responseString.trim();
			if(operatorName.equals("TTSL") && responseString.equals("99"))
				return "SUCCESS";
			if (responseString.equals("0") || responseString.indexOf("SUCCESS") != -1 || responseString.indexOf("Successfully") != -1 || responseString.toLowerCase().indexOf(srtSuccessSubString.toLowerCase()) != -1)
				return "SUCCESS";
			return "FAILURE";
		}
		
		return "RETRY";
	}

	private String getOperatorUrl(String  operatorName)
	{
		if(operatorName == null)
			return null;
		String url = InterOperatorUtility.operatorNameUrlMap.get(operatorName);
		return url;
	}
	
	private String getEncodedUrlString(String param) {
		String ret = null;
		try {
			ret = m_urlEncoder.encode(param, "UTF-8");
		} catch (Throwable t) {
			ret = null;
		}
		return ret;
	}
	
	// Added for RL-27488
	private String getReplacedRdcURL(String url,
			InterOperatorCopyRequestBean copyReq, String operatorName) {
		logger.info("Inside getReplacedRdcURL:");
		String operatorUrl = null;
		HashMap<String, String> urlMap = new HashMap<String, String>();
		String[] opUrls = url.split(";");
		if (opUrls != null && opUrls.length > 0) {
			for (String opUrl : opUrls) {
				String[] urlParams = opUrl.split(":");
				if (urlParams != null && urlParams.length > 0) {
					urlMap.put(urlParams[0], urlParams[1]);
				}
			}
		}
		if (urlMap != null && !urlMap.isEmpty() && urlMap.size() > 0) {
			if (urlMap.containsKey(operatorName)) {
				operatorUrl = urlMap.get(operatorName);
			} else {
				operatorUrl = urlMap.get("ALL");
			}
		}
		logger.info("operatorName--> " + operatorName);
		logger.info("operatorUrl--> " + operatorUrl);
		logger.info("copyReq--> " + copyReq);
		operatorUrl = operatorUrl.replaceAll("%subscriber_id%",
				copyReq.getCopieeMdn() + "");
		operatorUrl = operatorUrl.replaceAll("%caller_id%",
				copyReq.getCopierMdn() + "");
		operatorUrl = operatorUrl.replaceAll("%sel_by%",
				copyReq.getSourceMode());
		if (operatorName.equals("TTSL") && copyReq.getSourcePromoCode() != null) {
			operatorUrl = operatorUrl.replaceAll("$clip_id$",
					getEncodedUrlString(copyReq.getSourcePromoCode()));
			operatorUrl = operatorUrl.replaceAll("%clip_id%",
					copyReq.getSourcePromoCode());
		} else {
			if (copyReq.getTargetContentId() != null
					&& copyReq.getTargetContentId().contains("MISSING"))
				operatorUrl = operatorUrl.replaceAll(
						"%clip_id%",
						copyReq.getTargetContentId() + ":"
								+ copyReq.getSourceContentDetails());
			else
				operatorUrl = operatorUrl.replaceAll("%clip_id%",
						copyReq.getTargetContentId());
		}
		operatorUrl = operatorUrl.replaceAll("%sms_type%", "COPY");
		operatorUrl = operatorUrl.replaceAll("%opr_flag%", "1");
		operatorUrl = operatorUrl.replaceAll("$songname$",
				getEncodedUrlString(copyReq.getSourceSongName()));
		operatorUrl = operatorUrl.replaceAll("%songname%",
				copyReq.getSourceSongName());
		operatorUrl = operatorUrl.replaceAll("$keypressed$",
				getEncodedUrlString(copyReq.getKeyPressed()));
		operatorUrl = operatorUrl.replaceAll("%keypressed%",
				copyReq.getKeyPressed());
		logger.info("operatorUrl--> " + operatorUrl);
		return operatorUrl;
	}
	// Ended for RL-27488
}

