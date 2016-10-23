package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class BasicRequestHandler {
	
	public  Map<String,String> populateRequestParameters( HttpServletRequest httpRequest, boolean checkCGFlowForBSNL ){
		Map<String,String>requestParamsMap = new HashMap<String,String>();
		String msisdn = null;
		
		if ( checkCGFlowForBSNL ) {
			
			requestParamsMap.put( "RESULT", httpRequest.getParameter("status") );
			requestParamsMap.put( "TRANSACTION_ID", httpRequest.getParameter("transactionid") );
			requestParamsMap.put( "CG_ID", httpRequest.getParameter("cgid") );
			requestParamsMap.put( "SONG_ID", httpRequest.getParameter("songid") );
			requestParamsMap.put( "CONSENT_MODE", httpRequest.getParameter("consentmode") );
			msisdn = httpRequest.getParameter("msisdn");
			
			if ( null != msisdn && !msisdn.isEmpty() && msisdn.length() > 10 ) {
				int beginIndex = msisdn.length() - 10;
				msisdn = msisdn.substring( beginIndex, msisdn.length() );
			}
			
			requestParamsMap.put( "MSISDN", msisdn );
			
		} else {
			
			requestParamsMap.put( "RESULT", httpRequest.getParameter("Result") );
			requestParamsMap.put( "MSISDN", httpRequest.getParameter("MSISDN") );
			requestParamsMap.put( "TRANSACTION_ID", httpRequest.getParameter("transID") );
			requestParamsMap.put( "CG_ID", httpRequest.getParameter("TPCGID") );
			requestParamsMap.put( "CIRCLE_ID", httpRequest.getParameter("circleID") );
		
		}
		
		return requestParamsMap;
	}
	
}
