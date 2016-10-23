package com.onmobile.apps.ringbacktones.webservice.implementation;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class RBTProtocolGenerator {
	
	private Logger logger = Logger.getLogger(RBTProtocolGenerator.class);
	
	private String protocolNumLength;
	
	private RBTProtocolGenerator(){
		protocolNumLength = RBTParametersUtils.getParamAsString(
				"WEBSERVICE", "PROTOCOL_NUMBER_SEQUENCE_LENGTH", "12");
	}
	
	private static RBTProtocolGenerator _instance;
	
	
	
	public static RBTProtocolGenerator getInstance(){
		if(_instance == null){
			_instance = new RBTProtocolGenerator();
		}
		
		return _instance;
	}
	
	public String generateUniqueProtocolNum(){
		StringBuffer result = new StringBuffer();
		String format = "%0"+protocolNumLength+"d";
		
		int year = Calendar.getInstance().get(Calendar.YEAR);
		result.append(String.valueOf(year));
		
		String protocolNum = Utility.generatePortocolNumber();
		
		protocolNum = String.format(format,Long.parseLong(protocolNum));
		
		result.append(protocolNum);
		
		logger.info("Generated unique protocol number:"+result.toString());
		
		return result.toString();
	}
	
}
