package com.onmobile.apps.ringbacktones.logger;

import java.io.File;
import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.logger.RbtLogger.ROLLING_FREQUENCY;

public class SMHitLogger
{
	static Logger smHitLogger;
	static Logger smActualUrlLogegr; 
	static Logger logger = Logger.getLogger(CallBackLogTool.class);
	public static final String MSISDN = "MSISDN";
	public static final String CIRCLE_ID = "CIRCLE_ID";
	public static final String TIME_TAKEN = "TIME_TAKEN";
	public static final String ENTITY = "ENTITY";
	public static final String TYPE = "TYPE";
	public static final String MODE = "MODE";
	public static final String SRVKEY = "SRVKEY";
	public static final String SUB_TYPE = "SUB_TYPE";
	public static final String REFID = "REFID";
	public static final String CLIP_ID = "CLIP_ID";
	public static final String CLIP_TYPE = "CLIP_TYPE";
	public static final String CAT_ID = "CAT_ID";
	public static final String CAT_TYPE = "CAT_TYPE";
	public static final String PRECHARGE = "PRECHARGE";
	public static final String INFO = "INFO";
	public static final String OLD_SRVKEY = "OLD_SRKKEY";
	public static final String COS_ID = "COS_ID";
	public static final String COS_TYPE = "COS_TYPE";
	public static final String OFFER_ID = "OFFER_ID";
	public static final String CONTENT_STATUS = "CONTENT_STATUS";
	public static final String SM_RESPONSE_STRING = "SM_RES_STR";
	public static final String SM_RESPONSE_CODE = "SM_RES_CODE";
	public static final String REACT_REFID = "REACT_REFID";
	public static final String BASE_CONSENT_PARAM = "BASE_CONSENT_PARAM";
	public static final String SEL_CONSENT_PARAM = "SEL_CONSENT_PARAM";
	
	public static final String ENTITY_BASE = "BASE";
	public static final String ENTITY_CONTENT = "CONTENT";
	public static final String ENTITY_COMBO_BASE = "COMBO_BASE";
	public static final String ENTITY_COMBO_CONTENT = "COMBO_CONTENT";
	
	
	public static final String TYPE_ACTIVATION = "ACT";
	public static final String TYPE_REACTIVATION = "RCT";
	public static final String TYPE_DEACTIVATION = "DCT";
	public static final String TYPE_UPGRADE = "UP";
	public static final String TYPE_DELAYED_DEACTIVATION = "DELAY_D";
	public static final String TYPE_EVENT = "EVENT";
	public static final String TYPE_RENEWAL = "REN";
	public static final String TYPE_REALTIME = "REAL";
	public static final String TYPE_DIRECT_DEACT = "DIR_D";
	
	public static final String URL = "URL";
	public static final String LINKED_REF_ID = "LINKED_REF_ID";
	static
	{
		try
		{
			String loggerName = "RBT_TO_SM_EXCHANGE";
			smHitLogger = RbtLogger.createRollingFileLogger(RbtLogger.smDaemonTransactionPrefix + loggerName, ROLLING_FREQUENCY.HOURLY);
			String actualUrlLoggerName = "RBT_TO_SM_HTTP_URL";
			smActualUrlLogegr = RbtLogger.createRollingFileLogger(RbtLogger.smDaemonTransactionPrefix + actualUrlLoggerName,ROLLING_FREQUENCY.HOURLY);
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in initializing SM Daemon Transaction Logger", e);
		}
	}
	
	//TIMESTAMP,MSISDN,CIRCLEID,TIMETAKEN,SM_RES_STR,SM_RES_CODE,ENTITY,TYPE,MODE,SRVKEY,OLD_SRKKEY,COSID,COS_TYPE,OFFER_ID,SUB_TYPE,
	//REFID,CLIP_ID,CLIP_TYPE,CAT_ID,CAT_TYPE,CONTENT_STATUS,PRECHGARGED

	public static void writeCallBackTransactionLog(HashMap<String, String> m)
	{
		try
		{
			if(!smHitLogger.isEnabledFor(Level.INFO))
				return;
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append(",");
			strBuilder.append(m.get(MSISDN)).append(",");
			strBuilder.append(m.get(CIRCLE_ID)).append(",");
			strBuilder.append(m.get(TIME_TAKEN)).append(",");
			String response = m.get(SM_RESPONSE_STRING);
			if(response != null)
			{
				response = response.trim();
				if(response.indexOf(",") != -1)
					response = response.replaceAll(",", ";");
			}
			strBuilder.append(response).append(",");
			strBuilder.append(m.get(SM_RESPONSE_CODE)).append(",");
			strBuilder.append(m.get(ENTITY)).append(",");
			strBuilder.append(m.get(TYPE)).append(",");
			strBuilder.append(m.get(MODE)).append(",");
			strBuilder.append(m.get(SRVKEY)).append(",");
			strBuilder.append(m.get(OLD_SRVKEY)).append(",");
			strBuilder.append(m.get(COS_ID)).append(",");
			strBuilder.append(m.get(COS_TYPE)).append(",");
			strBuilder.append(m.get(OFFER_ID)).append(",");
			strBuilder.append(m.get(SUB_TYPE)).append(",");
			strBuilder.append(m.get(REFID)).append(",");
			strBuilder.append(m.get(CLIP_ID)).append(",");
			strBuilder.append(m.get(CLIP_TYPE)).append(",");
			strBuilder.append(m.get(CAT_ID)).append(",");
			strBuilder.append(m.get(CAT_TYPE)).append(",");
			strBuilder.append(m.get(CONTENT_STATUS)).append(",");
			strBuilder.append(m.get(PRECHARGE));
			smHitLogger.info(strBuilder.toString());
			
			StringBuilder urlBuilder = new StringBuilder();
			//TIMESTAMP,ENTITY,TYPE,RESPONSE_STR,RESPONSE_CODE,TIMETAKEN,URL
			urlBuilder.append(",");
			urlBuilder.append(m.get(ENTITY)).append(",");
			urlBuilder.append(m.get(TYPE)).append(",");
			urlBuilder.append(m.get(TIME_TAKEN)).append(",");
			urlBuilder.append(response).append(",");
			urlBuilder.append(m.get(SM_RESPONSE_CODE)).append(",");
			urlBuilder.append(m.get(URL)).append(",");
			
			//Added for TTG-14814
			urlBuilder.append(m.get(REFID)).append(",");
			urlBuilder.append(m.get(LINKED_REF_ID)).append(",");
			urlBuilder.append(m.get(MODE));
			//End of TTG-14814
			
			smActualUrlLogegr.info(urlBuilder.toString());
		}
		catch(Exception e)
		{
			if(logger.isEnabledFor(Level.ERROR))
				logger.error("Issue in writing rbt to sm http hit logs", e);
		}
	}
}
