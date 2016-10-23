package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpResponseBean;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.wrappers.MemCacheWrapper;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class SocialRBTUtility {
	private static URLCodec m_urlEncoder = new URLCodec();
	private static RBTConnector rbtConnector=null;
	static Logger logger = Logger.getLogger(SocialRBTUtility.class);
	private static String className="SocialRBTUtility";
	
	public static HttpResponseBean makeSocialRBTHttpCall(String baseActPublishingUrl,boolean useProxy,String proxyHost,int proxyPort,boolean toRetry,int timeOut){
		String method="makeSocialRBTHttpCall";
		logger.info(className+"->"+method+"->entering");
		StringBuffer response=new StringBuffer();
		Integer statusCode=new Integer(-1);
		boolean responseReceived=Tools.callURL(baseActPublishingUrl, statusCode, response, useProxy, proxyHost, proxyPort, toRetry, timeOut);
		logger.info(className+"->"+method+"->responseReceived=="+responseReceived);
		logger.info(className+"->"+method+"->response=="+response.toString());
		return new HttpResponseBean(responseReceived,statusCode.intValue(),response.toString());
	}
	public static String getEncodedUrlString(String param)
	{
		String ret = null;
		try
		{
			ret = m_urlEncoder.encode(param, "UTF-8");
		}
		catch(Throwable t)
		{
			ret = null;
		}
		return ret;
	}

	public static int getSocialRBTMode(String mode){
		int modeVal=1;
		if(mode!=null){
			mode=mode.trim();
			StringTokenizer st=new StringTokenizer(mode,"_");
			int count=0;
			while(st.hasMoreElements()){
				String temp=st.nextToken();
				if(count==1){
					mode=temp.trim();
				}
				count++;
			}
			try {
				modeVal=Integer.parseInt(mode);
			} catch (Exception e) {
				modeVal=1;
			}
		}
		return modeVal;
	}

	public static String getEndDateString(){
		Date currDate=Calendar.getInstance().getTime();
		currDate.setHours(0);
		currDate.setMinutes(0);
		currDate.setMonth(1);
		currDate.setSeconds(0);
		currDate.setYear(2037);
		String endDateStr=currDate.toLocaleString();
		return endDateStr;
	}

	public static  boolean getParamAsBoolean(String param, String defaultVal) {
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, defaultVal).equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info(
					"Unable to get param ->" + param
					+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	public static int getParamAsInt(String param,int defaultValue) {
		int returnVal=defaultValue;
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			String parameter= rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param,""+ defaultValue);
			if(parameter!=null){
				try {
					returnVal=Integer.parseInt(parameter);
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		} catch (Exception e) {
			logger.info(
					"Unable to get param ->" + param);
			return returnVal;
		}
		return returnVal;
	}
	public static String getParamAsString(String param,String defaultValue) {
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, defaultValue);
		} catch (Exception e) {
			logger.info(
					"Unable to get param ->" + param);
			return null;
		}
	}
	public static String getParamAsString(String param) {
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, null);
		} catch (Exception e) {
			logger.info(
					"Unable to get param ->" + param);
			return null;
		}
	}
	public static boolean getSelectionUrlMetaData(String url,RBTSocialUpdate rbtSocialUpdate,StringBuffer strBuff){
		if(url==null){
			return false;
		}
		if(strBuff==null){
			return false;
		}
		strBuff.append(url);
		if(rbtSocialUpdate!=null){
			Clip clip=MemCacheWrapper.getInstance().getClip(rbtSocialUpdate.getClipId());
			if (clip!=null) {
				String clipName = clip.getClipName();
				String artist=clip.getArtist();
				if(clipName!=null){
					strBuff.append("&clipName="+SocialRBTUtility.getEncodedUrlString(clipName));
				}
				if(artist!=null){
					strBuff.append("&artist="+SocialRBTUtility.getEncodedUrlString(artist));
				}
			}
			Category cat=MemCacheWrapper.getInstance().getCategory(rbtSocialUpdate.getCatId());
			if(cat!=null){
				String catName=cat.getCategoryName();
				if(catName!=null){
					strBuff.append("&catName="+SocialRBTUtility.getEncodedUrlString(catName));
				}
			}
			String callerId=rbtSocialUpdate.getCallerId();
			if(callerId!=null){
				if(callerId.indexOf("G")==-1 && callerId.indexOf("g")==-1){
					strBuff.append("&callerId="+callerId);
				}else{
					//populate group Id logic here
				}
			}
			if(clip==null || cat==null){
				return false;
			}
		}
		return true;
	}
	public static boolean getDownlodUrlMetaData(String url,RBTSocialUpdate rbtSocialUpdate,StringBuffer strBuff){
		if(url==null){
			return false;
		}
		if(strBuff==null){
			return false;
		}
		strBuff.append(url);
		if(rbtSocialUpdate!=null){
			Clip clip=MemCacheWrapper.getInstance().getClip(rbtSocialUpdate.getClipId());
			if (clip!=null) {
				String clipName = clip.getClipName();
				String artist=clip.getArtist();
				if(clipName!=null){
					strBuff.append("&clipName="+SocialRBTUtility.getEncodedUrlString(clipName));
				}
				if(artist!=null){
					strBuff.append("&artist="+SocialRBTUtility.getEncodedUrlString(artist));
				}
			}
			Category cat=MemCacheWrapper.getInstance().getCategory(rbtSocialUpdate.getCatId());
			if(cat!=null){
				String catName=cat.getCategoryName();
				if(catName!=null){
					strBuff.append("&catName="+SocialRBTUtility.getEncodedUrlString(catName));
				}
			}
			if(clip==null || cat==null){
				return false;
			}
		}
		return true;
	}
}
