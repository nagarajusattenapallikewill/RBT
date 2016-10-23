package com.onmobile.apps.ringbacktones.Gatherer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class RBTRTSMSThread extends Thread implements iRBTConstant{
	private static Logger logger = Logger.getLogger(RBTRTSMSThread.class);

	String _class = "RBTRTSMSThread";
	RBTGatherer m_parentThread = null;
	private RBTDBManager rbtDBManager = null;
	private RBTConnector rbtConnector = null;
	
	Integer statusCode = new Integer(-1);
	StringBuffer response = new StringBuffer();
	
	public RBTRTSMSThread(RBTGatherer rbtGatherer) throws Exception
	{
		logger.info("Entering.....");
		m_parentThread = rbtGatherer;
		if(init())
			logger.info("RBT::inited");
		else
			throw new Exception(" In RBTRTSMSThread: Cannot init Parameters");
		
	}
	
	public boolean init(){
		logger.info("Entering");
		rbtConnector = RBTConnector.getInstance();
		rbtDBManager = RBTDBManager.getInstance();
		return true;
	}
	public void run(){
		
		ViralSMSTable vst = null;
		boolean responseStatus = false;
		while(m_parentThread !=null && m_parentThread.isAlive()){
			try
			{
				synchronized(RBTRTSMSSender.m_pendingRTSMS)
				{
					while(RBTRTSMSSender.m_pendingRTSMS.size()==0){
						
						RBTRTSMSSender.m_pendingRTSMS.wait();
					}
					
					vst = (ViralSMSTable)RBTRTSMSSender.m_pendingRTSMS.remove(0);
				}
				String subscriberId = rbtDBManager.subID(vst.subID());
				String clipId = vst.clipID();
				String result = vst.type().substring(8);
				Date sentTime = vst.sentTime();
				String strURL = getParamAsString("GATHERER", "RT_SMS_URL",null);
				strURL += "MSISDN="+subscriberId+"&RBTID="+clipId+"&SELSTATUS="+result;
				
				
				if(vst.count()<3){
						
					Date requestTimeStamp = new Date();
					SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
					String requestTimeString = formatter.format(requestTimeStamp);
						
					responseStatus = Tools.callURL(strURL,statusCode,response,getParamAsBoolean("GATHERER","RT_SMS_USEPROXY","FALSE"),getParamAsString("GATHERER","RT_SMS_PROXYHOST",null),
							getParamAsInt("GATHERER","RT_SMS_PROXYPORT",0),getParamAsInt("GATHERER","RT_SMS_CONNECTIONTIMEOUT",5000),getParamAsInt("GATHERER","RT_SMS_TIMEOUT",5000));
					
					Date responseTimeStamp = new Date();
					long responseTimeInMillis = responseTimeStamp.getTime()-requestTimeStamp.getTime();
						
					synchronized(RBTRTSMSSender.m_pendingRTSMS){
						if(responseStatus){
							removeViralPromotion(vst.subID(),null,sentTime,RT_INIT+":"+result);
						
							WriteSDR.addToAccounting(getParamAsString("GATHERER","RT_SMS_LOGPATH",null),getParamAsInt("GATHERER","RT_SMS_ROTATIONSIZE",1000),"RBT_RT_SMS_SENDER",subscriberId,null,
									"rt sms send","SUCCESS",requestTimeString,""+responseTimeInMillis,null,strURL,
									response.toString());
							
						}else{
							
							setSearchCountCopy(vst.subID(),RT_INIT+":"+result,vst.count()+1,sentTime,null);
							
							WriteSDR.addToAccounting(getParamAsString("GATHERER","RT_SMS_LOGPATH",null),getParamAsInt("GATHERER","RT_SMS_ROTATIONSIZE",1000),"RBT_RT_SMS_SENDER",subscriberId,null,
									"rt sms send","FAILURE",requestTimeString,""+responseTimeInMillis,null,strURL,
									response.toString());
						}
					}
				}else{
						
					removeViralPromotion(vst.subID(),null,sentTime,RT_INIT+":"+result);
				}
				
			}
			catch(InterruptedException ie)
			{
				ie.printStackTrace();
				logger.error("", ie);
				
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.error("", e);
				
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				logger.error("", t);
			}
		}
		
	}
	
	public void setSearchCountCopy(String subscriberId,String type,int count,Date sentTime,String callerId){
		
		rbtDBManager.setSearchCountCopy(subscriberId,type,count,sentTime,callerId);
	}
	
	public boolean removeViralPromotion(String subscriberId,String callerId,Date sentTime,String type){
		return rbtConnector.getSubscriberRbtclient().removeViralData(subscriberId, callerId, type, sentTime);
//		return rbtDBManager.removeViralPromotion(subscriberId,callerId,sentTime,type);
	}
	
	private String getParamAsString(String type, String param, String defaultValue)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultValue);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultValue;
		}
	}
	
	private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defaultVal;
		}
	}
	
	 public boolean getParamAsBoolean(String type, String param, String defaultVal)
	    {
	    	try{
	    		return rbtConnector.getRbtGenericCache().getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defaultVal.equalsIgnoreCase("TRUE");
	    	}
	    }
}
