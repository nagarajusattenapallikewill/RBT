/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.logger.SmsLogger;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.reporting.framework.capture.api.Configuration;

public class RWSmsResponseEncoder extends ResponseEncoder
{
	
	protected RBTClient rbtClient = null;
	private String eventLogPath = null;
	private MOEventLogger logger = null;
	private static Logger appLogger = Logger.getLogger(RWSmsResponseEncoder.class);
	private static final ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>()
	{
        @Override
        protected SimpleDateFormat initialValue()
        {
            return new SimpleDateFormat("yyyyMMddHHmmss");
        }
    };
	public RWSmsResponseEncoder() throws Exception
	{
		eventLogPath = param("SMS","MO_REQUEST_LOG_PATH",null);
		if(eventLogPath != null){
		File file = new File(eventLogPath);
		if(!file.exists())
			file.mkdirs();
		 logger = new MOEventLogger(new Configuration(eventLogPath));
		}
        rbtClient = RBTClient.getInstance();
	}

	public String encode(Task task)
	{
		String smsText = task.getString(param_ocg_charge_id);
		if(smsText == null)
			smsText = task.getString(param_responseSms);
		if(task.containsKey(param_isdirectact) || task.containsKey(param_isdirectdct))
			smsText = task.getString(param_response);
		if(smsText == null)
		{
			Subscriber subscriber = (Subscriber)task.getObject(param_subscriber); 
			String language =  null;
			if(subscriber !=  null && subscriber.getLanguage() != null)
				language = subscriber.getLanguage();
			smsText = getSMSTextForID(TECHNICAL_FAILURE, m_technicalFailuresDefault,language);
		}
		
		String subscriptionClassOperatorNameMap = param("COMMON", "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);

		if(smsText != null && task.containsKey(param_subscriber) && subscriptionClassOperatorNameMap != null){
			Subscriber sub = (Subscriber)task.getObject(param_subscriber);
			String brandName =  Utility.getBrandName(sub.getCircleID());
			smsText = Utility.findNReplaceAll(smsText, "%BRAND_NAME%", brandName);
			smsText = Utility.findNReplaceAll(smsText, "%BRAND_NAME", brandName);
			
			
			String senderNumber = Utility.getSenderNumberbyType("SMS", sub.getCircleID(),"SENDER_NO");
			smsText = Utility.findNReplaceAll(smsText, "%SENDER_NO%", senderNumber);
			smsText = Utility.findNReplaceAll(smsText, "%SENDER_NO", senderNumber);
			smsText = Utility.findNReplaceAll(smsText, "%NO_SENDER", senderNumber);
			
		}
	
		Long startTime = 0l;
		if (task.containsKey(param_startTime))
			startTime = Long.parseLong(task.getString(param_startTime));
		writeTrans(task.getString(param_queryString),smsText,String.valueOf(System.currentTimeMillis()-startTime),task.getString(param_ipAddress));
		if(eventLogPath != null)
		{
			
			try {
				Subscriber sub = (Subscriber)task.getObject(param_subscriber);
				String shortcode = null;
				if(task.getString(param_shortCode) == null)
					shortcode = param("SMS","DEFAULT_SHORTCODE_FOR_MO_LOGS","-");
				String circleType = null;
				if(sub.isValidPrefix())
					circleType = "LOCAL";
				else if(sub.getCircleID() != null)
					circleType = "ONMOBILE";
				else
					circleType = "NON ONMOBILE";
				if(sub != null && logger != null)
					logger.smsUITransaction(new Timestamp(System.currentTimeMillis()), sub.getSubscriberID() , sub.getCircleID() == null ? "NON ONMOBILE" : sub.getCircleID(), task.getString(param_shortCode) == null ? shortcode : task.getString(param_shortCode), task.getString(param_smsSent), task.getTaskAction() , task.getString(param_finalResponse) , circleType);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return smsText;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder#getContentType(java.util.HashMap)
	 */
	@Override
	public String getContentType(HashMap<String, String> requestParams)
	{
		String contentType = "text/plain; charset=utf-8";
		contentType = param("SMS","SMS_TEXT_ENCODING_CONTENT_TYPE",contentType);
		return contentType;
	}
	
	public String getGenericErrorResponse(HashMap<String, String> requestParams)
	{
		return  getSMSTextForID(TECHNICAL_FAILURE, m_technicalFailuresDefault,null);
	}

	public String param(String type, String paramName, String defaultVal) {
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(type, paramName, defaultVal);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return defaultVal;
	}

	public synchronized void writeTrans(String params, String resp, String diff, String ip)
	{
		try
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append(params).append("|").append(resp).append("|").append(diff).append("|").append(ip).append("|");
			stringBuilder.append(formatter.get().format(Calendar.getInstance().getTime()));
			SmsLogger.getLogger().info(stringBuilder.toString());
		}
		catch(Exception e)
		{
			appLogger.error("Exception", e);
		}
	}
	
	public String getSMSTextForID(String SMSID, String defaultText,String language)
	{
	   String smsText=CacheManagerUtil.getSmsTextCacheManager().getSmsText(SMSID, language);
	   if(smsText!=null)
			return smsText;
		else
			return defaultText;
		
	}

}
