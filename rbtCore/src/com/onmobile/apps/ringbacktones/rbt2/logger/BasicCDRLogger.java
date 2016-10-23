package com.onmobile.apps.ringbacktones.rbt2.logger;


import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.common.PatternLayoutWithHeader;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class BasicCDRLogger {
	
	private static Logger logger = Logger.getLogger(BasicCDRLogger.class);
	
	private String filePath = null;
	private FileAppender appender = null;
	private String header = null;
	

	private String operatorName = null;
	private String countryName = null;
	private String conversionPattern = null;
	private String loggerName = null;
	private String maxFileSize = null;

	public BasicCDRLogger(){}
		
	public String getMaxFileSize() {
		return maxFileSize;
	}

	public void setMaxFileSize(String maxFileSize) {
		this.maxFileSize = maxFileSize;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}
	
	public String getConversionPattern() {
		return conversionPattern;
	}

	public void setConversionPattern(String conversionPattern) {
		this.conversionPattern = conversionPattern;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getFilePath() {
		return filePath;
	}


	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}


	public FileAppender getAppender() {
		return appender;
	}


	public void setAppender(FileAppender appender) {
		this.appender = appender;
	}


	public String getHeader() {
		return header;
	}


	public void setHeader(String header) {
		this.header = header;
	}
	


	public String getOperatorName() {
		return operatorName;
	}


	public void setOperatorName(String operatorName) {
		this.operatorName = operatorName;
	}


	public FileAppender getLoggerFileAppender() {

		PatternLayoutWithHeader layout = new PatternLayoutWithHeader();
		layout.setHeader(header);
		layout.setConversionPattern(conversionPattern);
		FileAppender fa = appender;
		fa.setLayout(layout);
		fa.setFile(filePath);
		fa.setName("test");
		fa.setAppend(true);
		if(fa instanceof RollingFileAppender){
			fa = (RollingFileAppender)fa;
			((RollingFileAppender) fa).setMaxFileSize(maxFileSize);
		}
		
		fa.activateOptions();
		return fa;
	}
	
	public void writeCDRLog(LoggerDTO loggerDTO) {}
	public LoggerDTO getLoggerDTOForSelACTSuccess(LoggerDTO loggerDTO, SubscriberStatus subscriberStatus){ return null;}
	public LoggerDTO getLoggerDTOForSelACTFailure(LoggerDTO loggerDTO,String subscriberID, SelectionRequest selectionRequest, AssetType asseType){return null;}
	public LoggerDTO getLoggerDTOForDownloadACTSuccess(LoggerDTO loggerDTO,SubscriberDownloads download){return null;}
	public LoggerDTO getLoggerDTOForDownloadACTFaliure(LoggerDTO loggerDTO, SelectionRequest selectionRequest, String type){return null;}
	public LoggerDTO getLoggerDTOForSelectionDCT(LoggerDTO loggerDTO, SubscriberStatus subscriberStatus, String subscriberId) {return null;}
	public LoggerDTO getLoggerDTOForDownloadDCT(LoggerDTO loggerDTO,SubscriberDownloads download, String subscriberId){return null;}
	public LoggerDTO getLoggerDTOForCutRbtSelACTSuccess(LoggerDTO loggerDTO, Setting subscriberStatus){return null;}

	
	
}
