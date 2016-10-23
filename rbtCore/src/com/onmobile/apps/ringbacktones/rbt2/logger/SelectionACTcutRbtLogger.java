package com.onmobile.apps.ringbacktones.rbt2.logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.springframework.beans.factory.annotation.Autowired;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.common.PatternLayoutWithHeader;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbt2.builder.impl.UGCAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.impl.ComboCutRbtRequestResolver;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class SelectionACTcutRbtLogger extends BasicCDRLogger {
	
	private static Logger logger = Logger.getLogger(SelectionACTcutRbtLogger.class);
	
	@Override
	public String toString() {
		return "SelectionACTcutRbtLogger [product=" + product + ", version=" + version + ", source=" + source + "]";
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		SelectionACTcutRbtLogger.product = product;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		SelectionACTcutRbtLogger.version = version;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		SelectionACTcutRbtLogger.source = source;
	}

	private static String product ;
	
	private static String version ;

	private static  String source;
	private static  String cutRbtfilepath;
	private String conversionPattern = null;
	private String maxFileSize = null;
	private FileAppender appender = null;

	

	
	public String getConversionPattern() {
		return conversionPattern;
	}

	public void setConversionPattern(String conversionPattern) {
		this.conversionPattern = conversionPattern;
	}

	public String getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public FileAppender getAppender() {
		return appender;
	}

	public void setAppender(FileAppender appender) {
		this.appender = appender;
	}



	public static ParametersCacheManager parameterCacheManager = null;

	static {
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();

	}

	Date date = new Date();
	String  modifiedDate= new SimpleDateFormat("yyyy-MM-dd").format(date);
	String loggerName=product+"_"+version+"_"+modifiedDate;
	
	public SelectionACTcutRbtLogger(){
		
		//String header="";
		//header=header+"#product="+product+System.getProperty("line.separator")+"#version="+version+System.getProperty("line.separator")+"#source="+source+System.getProperty("line.separator")+"##";
		//header=header+"CurrentTimeStamp, msisdn, clipId, clipname, fulltrack wavfile name,start time, selection wavfile name, internalref id, callerId, status";
		//setHeader(header);
		
		setLoggerName(loggerName);

	}
	
	public void writeCDRLog(LoggerDTO loggerDTO) {
		
		FileAppender loggerFileAppender = getLoggerFileAppender();
		Logger.getLogger(loggerName).setLevel(Level.ALL);
		Logger.getLogger(loggerName).addAppender(loggerFileAppender);
		Logger.getLogger(loggerName).info(
				
					loggerDTO.getSubscriberId() +" , " + 
					loggerDTO.getClipid() +" , " +
				    loggerDTO.getTonename() +" , " +
				    loggerDTO.getFulltrackName()+" , " +
				    loggerDTO.getStarttime()+" , " +
				    loggerDTO.getWavfileName() +" , " +
					loggerDTO.getRefId()+" , " +
					loggerDTO.getCallerId()+" , " +
					loggerDTO.getStatus()
					
					);
				  
	}		
	
	
	public LoggerDTO getLoggerDTOForCutRbtSelACTSuccess(LoggerDTO loggerDTO,
			Setting subscriberStatus) {
	
		loggerDTO.setDefaultValues();
		Clip clip = null;
		if (subscriberStatus != null) {
			try{
				String cutwavFileName = subscriberStatus.getRbtFile().replace(".wav", "");
				cutwavFileName = cutwavFileName+"_cut_"+subscriberStatus.getCutRBTStartTime()+".wav";
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(cutwavFileName);
		//	clip = RBTCacheManager.getInstance().getClip(1);
			loggerDTO.setSubscriberId(Long.parseLong(subscriberStatus.getSubscriberID()));
			loggerDTO.setClipid(clip.getClipId());
			loggerDTO.setTonename(clip.getClipName());
		//	String cutwavFileName = subscriberStatus.getRbtFile().replace(".wav", "");
		//	cutwavFileName = cutwavFileName+"_cut_"+subscriberStatus.getCutRBTStartTime()+".wav";
			loggerDTO.setWavfileName(cutwavFileName);
			loggerDTO.setFulltrackName(getfulltrackname(clip.getClipId()));
			if(subscriberStatus.getRbtFile()!=null)
			loggerDTO.setStarttime(subscriberStatus.getSetTime().toString());
			loggerDTO.setRefId(subscriberStatus.getRefID());
			loggerDTO.setCallerId(subscriberStatus.getCallerID());
			
			loggerDTO.setStatus(subscriberStatus.getStatus()+"");
			}catch(Exception e)
			{
				logger.error(e);
				logger.info("exception : "+e,e);
			}
			
		}
			return loggerDTO;
	}
	
	
	private String getfulltrackname(int clipID)  {

		String fulltrackName = null;
		//int clipID = (int) purchaseCombo.getAsset().getId();
		logger.info("\n\t:---> clipID" + clipID);
		String key = param(iRBTConstant.COMMON, "CUTRBT_FULLTRACKWAVNAME_KEY", "fullTrackWavName");
		Clip clip2 = RBTCacheManager.getInstance().getClip(clipID);
		Map<String, String> clipInfoMap = MapUtils.convertIntoMap(clip2.getClipInfo(), "|", "=", null);
		fulltrackName = clipInfoMap.get(key);
		logger.info("\n\t:---> fulltrackName" + fulltrackName);
		if (fulltrackName == null) {
			logger.error("fulltrackName is null s.");
		}
		return fulltrackName;
	}

	

	public static String param(String type, String paramName, String defaultVal) {
		Parameters param = parameterCacheManager.getParameter(type, paramName, defaultVal);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return defaultVal;
	}
/*
	public static int param(String type, String paramName, int defaultVal) {
		Parameters param = parameterCacheManager.getParameter(type, paramName, String.valueOf(defaultVal));
		if (param != null) {
			try {
				String value = param.getValue();
				if (value != null)
					return Integer.parseInt(value.trim());
			} catch (Exception e) {
				return defaultVal;
			}
		}
		return defaultVal;

	}
	*/
	public FileAppender getLoggerFileAppender() {
		String  modifiedDate= new SimpleDateFormat("yyyy-MM-dd").format(date);
		PatternLayoutWithHeader layout = new PatternLayoutWithHeader();
		String header="";
		header=header+"#product="+product+System.getProperty("line.separator")+"#version="+version+System.getProperty("line.separator")+"#source="+source+System.getProperty("line.separator")+"##";
		header=header+"CurrentTimeStamp, msisdn, clipId, clipname, fulltrack wavfile name,start time, selection wavfile name, internalref id, callerId, status";
		layout.setHeader(header);
		layout.setConversionPattern(conversionPattern);
		FileAppender fa = appender;
		fa.setLayout(layout);
		fa.setFile(cutRbtfilepath+product+"_"+version+"_"+modifiedDate+".csv");
		
		fa.setName("test");
		fa.setAppend(true);
		if(fa instanceof RollingFileAppender){
			fa = (RollingFileAppender)fa;
			((RollingFileAppender) fa).setMaxFileSize(maxFileSize);
		}
		
		fa.activateOptions();
		return fa;
	}

	public static String getCutRbtfilepath() {
		return cutRbtfilepath;
	}

	public static void setCutRbtfilepath(String cutRbtfilepath) {
		SelectionACTcutRbtLogger.cutRbtfilepath = cutRbtfilepath;
	}

}
