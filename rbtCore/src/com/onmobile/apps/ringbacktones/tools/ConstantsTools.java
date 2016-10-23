package com.onmobile.apps.ringbacktones.tools;

public interface ConstantsTools
{

	//DB Types MYSQL or SAPDB
	public static String DB_SAPDB = "SAPDB";
	public static String DB_MYSQL = "MYSQL";
	
	//Parameter table Types
	public static String COMMON = "COMMON";
	public static String SMS = "SMS";
	public static String DAEMON = "DAEMON";
	public static String GATHERER = "GATHERER";
	
	
	//RBT_TEXT table type,subtpe names
	public static String UNSUBSCRIPTION_MSG = "UNSUBSCRIPTION_MSG";
	public static String UPGRADE_TNB_MSG = "UPGRADE_TNB_MSG";
	
	//Charge Class name
	public static String FREE = "FREE";
	
	// SMS Daemon Reminder Type
	public static String TNB_OPTIN = "TNB_OPTIN";
	public static String TNB_OPTOUT = "TNB_OPTOUT";
	public static String TRIAL = "TRIAL";
	
	//Parameter table parameter names
	public static String REM_PUB_ROTATE_NUM = "REM_PUB_ROTATE_NUM";
	public static String REM_PUB_ROTATE_SLEEP_SEC = "REM_PUB_ROTATE_SLEEP_SEC";
	public static String REM_PUB_SLEEP_SEC = "REM_PUB_SLEEP_SEC";
	public static String UPDATE_TNB_TO_NORMAL_ON_DEFAULT_SELECTION = "UPDATE_TNB_TO_NORMAL_ON_DEFAULT_SELECTION";
	public static String SMS_NO = "SMS_NO";
	public static String SMS_NO_DCT_DOWNLOAD_CONFIRM = "SMS_NO_DCT_DOWNLOAD_CONFIRM";
	public static String ADD_TO_DOWNLOADS = "ADD_TO_DOWNLOADS";
	public static String BULK_PROMOID = "BULK_PROMOID";
	public static String DEACT_MODE_OPT_IN = "DEACT_MODE_OPT_IN";
	public static String TNB_OPTIN_REMINDER_TRANS_DIR = "TNB_OPTIN_REMINDER_TRANS_DIR";
	public static String TNB_OPTOUT_REMINDER_TRANS_DIR = "TNB_OPTOUT_REMINDER_TRANS_DIR";
	public static String TRIAL_REMINDER_TRANS_DIR = "TRIAL_REMINDER_TRANS_DIR";
	public static String START_REMINDER_CLEANUP = "START_REMINDER_CLEANUP";
	public static String SUPPORT_TNB_NEW_FLOW = "SUPPORT_TNB_NEW_FLOW";
}
