package com.onmobile.apps.ringbacktones.activemonitoring.common;

public interface AMConstants 
{
	public enum TaskType
	{
		TASKTYPE_SMACTURL,
		TASKTYPE_SMDCTURL,
		TASKTYPE_SMRENURL,
		TASKTYPE_SMUPGURL,
		TASKTYPE_PLAYERURL,
		TASKTYPE_ACT,
		TASKTYPE_DCT,
		TASKTYPE_UPDATE
	}
	
	public enum CounterType
	{
		COUNTER_BASE_ACT,
		COUNTER_BASE_DCT,
		COUNTER_SEL_ACT,
		COUNTER_SEL_DCT,
		COUNTER_DWN_ACT,
		COUNTER_DWN_DCT,
		COUNTER_SEL_REN,
		COUNTER_BASE_REN
	}
	
	public enum Severity
	{
		CRITICAL,
		ERROR,
		WARNING,
		INFO,
		DEBUG,
		CLEAR
	}
	
	
	/** TRAP COMPONENTS **/
	public static String SMDAEMON 		= "SMDAEMON";
	public static String PLAYERDAEMON 	= "PLAYERDAEMON";
	public static String COPYPROCESSOR 	= "COPYPROCESSOR";
	public static String GATHERER		= "GATHERER";
	public static String CALLBACK		= "CALLBACK";
	public static String THIRDPARTY		= "THIRDPARTY";
	public static String CCC			= "CCC";
	public static String SMSUI			= "SMSUI";
	public static String WEBSERVICE		= "WEBSERVICE";
	
	public static String SMDAEMON_ACT_URL = "smdaemon_act_url";
	public static String SMDAEMON_DCT_URL = "smdaemon_dct_url";
	public static String SMDAEMON_REN_URL = "smdaemon_ren_url";
	public static String SMDAEMON_UPG_URL = "smdaemon_upg_url";
	
	public static String PLAYER_URL 	  = "player_url";
	
	public static String STARTCOPY_URL 	  = "startcopy_url";
	public static String WDS_URL 	 	  = "wds_url";
	
	/** GENERIC **/
	public static String CONTENT 		= "content";
	public static String NAME 			= "name";
	public static String VALUE 			= "value";
	public static String PROPERTY 		= "property";
	
	
}

