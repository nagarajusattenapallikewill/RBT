package com.onmobile.apps.ringbacktones.monitor.common;

/**
 * 
 * @author Sreekar
 *
 */
public interface Constants {
	public static final String param_tracetype		= "tracetype";
	public static final String param_msisdn			= "msisdn";
	public static final String param_called			= "called";
	public static final String param_ccode			= "ccode";
	public static final String param_vcode			= "vcode";
	public static final String param_albumcode		= "albumcode";
	public static final String param_keypressed		= "keypressed";
	public static final String param_action			= "action";
	public static final String param_smstext		= "smstext";
	//TRACE TYPES
	public static final String TRACE_TYPE_COPY		= "COPY";
	public static final String TRACE_TYPE_SMS		= "SMS";
	public static final String TRACE_TYPE_IVR		= "IVR";
	public static final String TRACE_TYPE_WEBSERVICE= "WEBSERVICE";
	public static final String TRACE_TYPE_CCC		= "CCC";
	public static final String TRACE_TYPE_ENVIO		= "ENVIO";
	public static final String TRACE_TYPE_USSD		= "USSD";
	public static final String TRACE_TYPE_EC		= "EC";
	public static final String TRACE_TYPE_MOD		= "MOD";
	public static final String TRACE_TYPE_AUTODIAL	= "AUTODIAL";
	public static final String TRACE_TYPE_PROMOTION	= "PROMOTION";
	// ACTIONS
	public static final String ACTION_ACTIVATE		= "activate";
	public static final String ACTION_SELECTION		= "selection";
	public static final String ACTION_DEACTIVATE	= "deactivate";
	
	public static final String RESPONSE_GENERIC_FAILURE = "FAILURE";
	public static final String RESPONSE_TRACE_IN_PROCESS = "OTHER_TRACE_IN_PROCESS";
	public static final String RESPONSE_NOT_TRACE_NUMBER = "NOT_TRACE_NUMBER";
	public static final String RESPONSE_SUCCESS = "SUCCESS";
	public static final String RESPONSE_NO_ACTIVE_MONITOR = "NO_ACTIVE_MONITOR";
	
	//Log Type
	public static final String SQL_TYPE_LOGGER ="SQL_TYPE_LOG";
}