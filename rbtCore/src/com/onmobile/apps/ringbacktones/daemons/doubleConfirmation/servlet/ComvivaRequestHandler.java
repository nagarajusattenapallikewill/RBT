package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/** 
 * This class is responsible to handle the request from CG.
 * @author manjunatha.c
 *
 */
public class ComvivaRequestHandler extends BasicRequestHandler{
	public  Map<String,String> populateRequestParameters( HttpServletRequest httpRequest, boolean checkCGFlowForBSNL ){
		Map<String,String>requestParamsMap = new HashMap<String,String>();
		String consentStatus = null;
		
		requestParamsMap.put( "MSISDN" , httpRequest.getParameter( "msisdn" ) );
		requestParamsMap.put( "VAS_ID" , httpRequest.getParameter( "vas_id" ) );
		requestParamsMap.put( "TRANSACTION_ID" , httpRequest.getParameter( "trx_id" ) );
		requestParamsMap.put( "CG_ID" , httpRequest.getParameter( "cg_id" ) );
		requestParamsMap.put( "ERROR_CODE" , httpRequest.getParameter( "error_code" ) );
		requestParamsMap.put( "ERROR_DESC" , httpRequest.getParameter( "error_desc" ) );
		
		consentStatus = httpRequest.getParameter( "consnt_status" );
		if( consentStatus.equalsIgnoreCase("1")){
			requestParamsMap.put( "RESULT", "success" );
		}else{
			requestParamsMap.put( "RESULT", "failure" );
		}
		
		requestParamsMap.put( "CONSENT_TIME" , httpRequest.getParameter( "consnt_time" ) );
		requestParamsMap.put( "OPTION_1" , httpRequest.getParameter( "opt1" ) );
		requestParamsMap.put( "OPTION_2" , httpRequest.getParameter( "opt2" ) );
		requestParamsMap.put( "OPTION_3" , httpRequest.getParameter( "opt3" ) );
		
		return requestParamsMap ;
	}
}
