package com.onmobile.apps.ringbacktones.servlets;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.smsgateway.accounting.Accounting;

public class CCCAccountingManager {

	private static Logger logger = Logger.getLogger(CCCAccountingManager.class);
	
	public static Accounting cccGUIAccounting = null;
	public static ConfigForAccounting configurations=null;
	public static String CLASSNAME="CCCAccountingManager";
	public static String presentSDRWorkingDir=null;
	public static void createAccountings(String presentSDRWorkingDir)
	{
		String transactionSDRFormat = "APP_ID USERNAME udid wstid opParam msisdn TIME REQUEST RESPONSE RESPPONSE_STATUS";
		cccGUIAccounting = Accounting.getInstance(presentSDRWorkingDir, configurations.getSdrSize(),
				configurations.getSdrInterval(), configurations.getSdrRotation(), configurations.isSdrBillingOn(), transactionSDRFormat);
		if (cccGUIAccounting == null)
			logger.info("RBT::ONMOBILE Accounting class couldnt not be created");
		else{
			logger.info("RBT::ONMOBILE Accounting class created");

		}
	}
	public static void addToCCCGUIAccounting(UserDetails userDetails,String msisdn,String request, String response,boolean responseStatus){
		 logger.info("inside addToCCCGUIAccounting");
	   try
	   {
		   Calendar cal=Calendar.getInstance();
		   Date currDate=cal.getTime();
		   logger.info("currDate=="+currDate.toString());
		   int currMonth=currDate.getMonth();
		   //
		   int currYear=currDate.getYear();
		   currYear=currYear+1900;
		   int currDay=currDate.getDate();
		   String strCurrDay=null;
		   if(currDay<10){
			   strCurrDay="0"+currDay;
		   }else{
			   strCurrDay=""+currDay;
		   }
//		   logger.info("currYear=="+currYear);
//		   logger.info("currMonth=="+currMonth);
//		   logger.info("currDay=="+currDay);
//		   logger.info("strCurrDay=="+strCurrDay);
//		   logger.info("configurations.getSdrWorkingDir()=="+configurations.getSdrWorkingDir());
		   presentSDRWorkingDir=configurations.getSdrWorkingDir();
		   boolean isNewFolder=false;
		   File file=new File(configurations.getSdrWorkingDir()+File.separator+currYear);
		   logger.info("checking for file=="+file.getAbsoluteFile());
		synchronized (presentSDRWorkingDir) {
			if (!file.exists() || !file.isDirectory()) {
				file.mkdir();
				isNewFolder = true;
				logger.info("file=="
						+ file.getAbsoluteFile()
						+ " is not a dir or a doesnt exist");
			} else {
				logger.info("file=="
						+ file.getAbsoluteFile()
						+ "  does exist and a dir as well ");
			}
			file = new File(configurations.getSdrWorkingDir() + File.separator
					+ currYear + File.separator + getMonth(currMonth));
			logger.info("checking for file==" + file.getAbsoluteFile());
			if (!file.exists() || !file.isDirectory()) {
				file.mkdir();
				isNewFolder = true;
				logger.info("file=="
						+ file.getAbsoluteFile()
						+ " is not a dir or a doesnt exist");
			} else {
				logger.info("file=="
						+ file.getAbsoluteFile()
						+ "  does exist and a dir as well ");
			}
			file = new File(configurations.getSdrWorkingDir() + File.separator
					+ currYear + File.separator + getMonth(currMonth)
					+ File.separator + strCurrDay);
			logger.info("checking for file==" + file.getAbsoluteFile());
			if (!file.exists() || !file.isDirectory()) {
				file.mkdir();
				isNewFolder = true;
				logger.info("file=="
						+ file.getAbsoluteFile()
						+ " is not a dir or a doesnt exist");
			} else {
				logger.info("file=="
						+ file.getAbsoluteFile()
						+ "  does exist and a dir as well ");
			}
			logger.info("file name=="
					+ file.getAbsolutePath());
			if (file.isDirectory()) {
				presentSDRWorkingDir = file.getAbsolutePath();
				if (isNewFolder || cccGUIAccounting == null) {
					logger.info("file name==" + file.getAbsolutePath()
									+ " is a new folder");
					
						createAccountings(presentSDRWorkingDir);
				}
			}
		}		   
		if (cccGUIAccounting != null && file.isDirectory())
	       {
	    	   logger.info("going to write into the file");
	           HashMap acMap = new HashMap();
	           acMap.put("APP_ID", "CCC_GUI");
	           acMap.put("USERNAME", userDetails.userName);
	           acMap.put("udid", userDetails.udid);
	           acMap.put("wstid", userDetails.wstid);
	           acMap.put("opParam", userDetails.opParam);
	           acMap.put("msisdn", msisdn);
	           acMap.put("TIME", (new SimpleDateFormat("yyyyMMddHHmmssms"))
	                   .format((new Date(System.currentTimeMillis()))));
	           acMap.put("REQUEST", request);
	           acMap.put("RESPONSE", response);
	           if(responseStatus){
	        	   acMap.put("RESPPONSE_STATUS", "true");
	           }else{
	        	   acMap.put("RESPPONSE_STATUS", "false");
	           }
	               cccGUIAccounting.generateSDR("sms", acMap);
	               logger.info("RBT::Writing to the accounting file");
	           acMap = null;
	       }
	   }
	   catch (Exception e)
	   {
	       logger.error("", e);
	   }
	}
	public static String getMonth(int month){
		if(month==0){
			return "JAN";
		}
		else if(month==1){
			return "FEB";
		}
		else if(month==2){
			return "MAR";
		}
		else if(month==3){
			return "APR";
		}
		else if(month==4){
			return "MAY";
		}
		else if(month==5){
			return "JUN";
		}
		else if(month==6){
			return "JUL";
		}
		else if(month==7){
			return "AUG";
		}
		else if(month==8){
			return "SEP";
		}
		else if(month==9){
			return "OCT";
		}
		else if(month==10){
			return "NOV";
		}
		else{
			return "DEC";
		}
	}
}
