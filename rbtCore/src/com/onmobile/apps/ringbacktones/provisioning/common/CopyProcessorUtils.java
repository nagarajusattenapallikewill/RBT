package com.onmobile.apps.ringbacktones.provisioning.common;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class CopyProcessorUtils implements Constants,iRBTConstant{

	private static ParametersCacheManager parameterCacheManager = null;
	

	private static ArrayList<String> normalCopy=null;
	private static ArrayList<String> starCopy=null;
	private static ArrayList<String> rtCopy=null;
	private static ArrayList<String> selfGiftCopy=null;
	private static ArrayList<String> giftCopy=null;
	private static ArrayList<String> rrbtCopy=null;
	private static HashSet<String> virtualNumbers=null;
	private static ArrayList<String> rrbtConfirmActKeys=null;
	private static ArrayList<String> hashDownload=null;
	private static ArrayList<String> crossCopy=null;
	//RBT-14671 - # like
	private static ArrayList<String> toLikeKeys=null;
	//RBT-15472	
	private static ArrayList<String> promoteKeys=null;


	protected static Logger logger = Logger.getLogger(CopyProcessorUtils.class);
	
	
	static{
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();
		String virtualnos=getParameter("RRBT","VIRTUAL_NUMBERS");
		if(virtualnos!=null)
		{
		virtualNumbers=new HashSet<String>();
		String Virtual_no[]=virtualnos.split(",");
		if(Virtual_no!=null && Virtual_no.length>0)
		{
			for(int z=0;z<Virtual_no.length;z++)
			virtualNumbers.add(Virtual_no[z]);
		}
		}
		normalCopy=tokenizeArrayList(getParameter("COMMON","NORMALCOPY_KEY"), ",");
		starCopy=tokenizeArrayList(getParameter("COMMON","STARCOPY_KEY"), ",");
		rtCopy=tokenizeArrayList(getParameter("COMMON","RTCOPY_KEY"), ",");
		selfGiftCopy=tokenizeArrayList(getParameter("COMMON","SELFGIFTCOPY_KEY"), ",");
		giftCopy=tokenizeArrayList(getParameter("COMMON","GIFTCOPY_KEY"), ",");
		rrbtCopy=tokenizeArrayList(getParameter("COMMON","RRBTCOPY_KEY"), ",");
		rrbtConfirmActKeys = tokenizeArrayList(getParameter("COMMON", "RRBT_CONFIRM_ACT_KEY"), ",");
		crossCopy=tokenizeArrayList(getParameter("COMMON",CROSSCOPY_KEY), ",");
		//RBT-14671 - # like
		toLikeKeys = tokenizeArrayList(getParameter("COMMON",TOLIKE_KEY), ",");
		//RBT-15472	
		promoteKeys=tokenizeArrayList(getParameter("COMMON",PROMOTE_KEY), ","); 
		hashDownload = new ArrayList<String>();
		List<String> pressDownloadList = tokenizeArrayList(getParameter("COMMON","PRESS_DOWNLOAD_KEY_URL_MAP"), ";");
		if(pressDownloadList != null) {
			for(String value : pressDownloadList) {
				String[] arr = value.split(",");
				if(arr.length == 2) {
					hashDownload.add(arr[0].toLowerCase());
				}
			}
		}
	}
	
	// RBT-14671 - # like
	public static String findLikeTypeFromDTMF(String keyWord) {
		logger.info("RBT:: In ServiceProcessor processPlayerHelper()");
		String type = null;
		if (toLikeKeys != null) {
			for (String key : toLikeKeys) {
				if (keyWord.indexOf(key.toString().toLowerCase()) != -1) {
					type = LIKE;
					break;
				}
			}
		}
		return type;
	}

	public static String findSMSTypeFromDTMF(String keyWord, String details){
		logger.info("RBT:: In ServiceProcessor processPlayerHelper() Normalcopy:"+normalCopy+" starcopy: "+starCopy+" rtcopy : "+rtCopy+" HashDownload: " + hashDownload +" crossCopy: " + crossCopy + " keypressed : "+keyWord);
		String key=null;
		String type=null;
		String sub=null;
		String param=getParameter("GATHERER","IS_RRBT_COPY_ON");
		Boolean isPromoteCopy = false ;
		if(getParameter("GATHERER",IS_COPY_PROMOTE)!=null){
			isPromoteCopy =  Boolean.parseBoolean(getParameter("GATHERER",IS_COPY_PROMOTE));
		}
		
		if(param!=null&&param.equalsIgnoreCase("TRUE"))
		{
			String subId[]=details.split(":");
			if(subId!=null&&subId.length>0)
				sub=subId[0];
			if(sub!=null&&!sub.equals(""))
				if(rrbtCopy!=null&& (virtualNumbers == null || virtualNumbers.contains(sub) ))
				{
					logger.info("RBT: Reverse Rbt copy");
					for(int i=0;i<rrbtCopy.size();i++){
						key=(String)rrbtCopy.get(i).toString().toLowerCase();
						if(keyWord.indexOf(key)!=-1){
							type=RRBT_COPY;
							break;
						}
					}
				}
		}
		
		String jingleWavFile = getParameter("COMMON", "RRBT_CAMPAIGN_JINGLE");
		if (jingleWavFile != null && rrbtConfirmActKeys != null)
		{
			String[] detailsTokens = details.split(":");
			String subscriberWavFile = detailsTokens[2].trim();
			for (String confirmKey : rrbtConfirmActKeys) 
			{
				if (subscriberWavFile.equalsIgnoreCase(jingleWavFile) && (keyWord.indexOf(confirmKey) != -1))
				{
					type = "CONFIRM_ACT";
					return type;
				}
			}
		}
		
		boolean isStarcopy=false;
//		boolean isCopy=false;
		if(keyWord!=null&&keyWord.length()>=1){
			keyWord=keyWord.toLowerCase();
			
			//press # to download
			if(hashDownload!=null){
				for(int i=0;i<hashDownload.size();i++){
		             key=hashDownload.get(i).toLowerCase();
		             if(keyWord.indexOf(key)!=-1){
		            	 type = HASH_DOWNLOAD;
		            	 isStarcopy=true;
		            	 break;		            	
		             }
				}
			}
			
			if(normalCopy!=null){
				for(int i=0;i<normalCopy.size();i++){
		             key=(String)normalCopy.get(i).toString().toLowerCase();
		             //RBT-10651
		             boolean condition=false;
		             logger.info("Inside normal copy and keyword is :"+keyWord);
		             String keySuffix=getParameter("COMMON","COPY_KEY_SUFFIX");
		             logger.info("keySuffix :"+keySuffix);
		             if(keySuffix!=null && keySuffix.equalsIgnoreCase("true"))
		             {
		            	 condition=keyWord.startsWith(key);
		            	
		             }else
		             {
		            	 condition=keyWord.indexOf(key)!=-1;
		             }
		             logger.info("condition after updating: "+condition);
		             if(condition){
//		            	 type = "COPY";
		            	 type = COPY;
		            	 isStarcopy=true;
//		            	 isCopy=true;
		            	 break;		            	
		             }
				}
			}
		
            if(rtCopy!=null){
            	for(int i=0;i<rtCopy.size();i++){
	   				 key=(String)rtCopy.get(i).toString().toLowerCase();
	   				 if(keyWord.indexOf(key)!=-1){
//	   					 type = "RTCOPY";
	   					type = RTCOPY;
	   	            	 isStarcopy=true;
	   	            	 break;
	   				 } 
	   			}
            }
            
            if(selfGiftCopy!=null){
            	for(int i=0;i<selfGiftCopy.size();i++){
	   				 key=(String)selfGiftCopy.get(i).toString().toLowerCase();
	   				 if(keyWord.indexOf(key)!=-1){
//	   					 type = "SELF_GIFT";
	   					type = SELF_GIFT;
	   	            	 isStarcopy=true;
	   	            	 break;
	   	            	 
	   	             }
	   			}
            }
		
		    if(giftCopy!=null){
		    	
		    	for(int i=0;i<giftCopy.size();i++){
					 key=(String)giftCopy.get(i).toString().toLowerCase();
					 if(keyWord.indexOf(key)!=-1){
//						 type = "GIFTCOPY";
						 type = GIFTCOPY;
		            	 isStarcopy=true;
		            	 break;
		            	 
		             }	 
				}
		    }
		    
			if(isStarcopy==false&&starCopy!=null){
				 logger.info("RBT: copystar");
				for(int i=0;i<starCopy.size();i++){
					 key=(String)starCopy.get(i).toString().toLowerCase();
					 if(keyWord.indexOf(key)!=-1){
//						 type="COPYSTAR";
						 type=COPYSTAR;
						 isStarcopy=true;
						 break;
		            }
				}
			}
			
			 if(isStarcopy == false && crossCopy !=null){
			    	for(int i=0;i<crossCopy.size();i++){
						 key=(String)crossCopy.get(i).toString().toLowerCase();
						 if(keyWord.indexOf(key)!=-1){
							 type = CROSSCOPY;
			            	 
			            	 break;
			            	 
			             }	 
					}
			    }
			// RBT-14671 - # like
			if (isStarcopy == false && toLikeKeys != null && type == null) {
				for (String keyFromConf : toLikeKeys) {
					if (keyWord.indexOf(keyFromConf.toString().toLowerCase()) != -1) {
						type = LIKE;
						break;
					}
				}
			}
			
			if (isPromoteCopy && type == null && promoteKeys != null) {
				for (int i = 0; i < promoteKeys.size(); i++) {
					key = (String) promoteKeys.get(i).toString().toLowerCase();
					if (keyWord.startsWith(key)) {
						type = PROMOTE;
						break;
					}
				}
			}

		}else{
			  logger.info("RBT:: processplayerhelper else part : ");
			if (details!=null&&details.indexOf(":RT")!=(-1)){
//				type="RTCOPY";
				type=RTCOPY;
			}else{
//				type="COPY";
				type=COPY;
			}
				
		}
		
	    logger.info("RBT:: findSMSTypeFromDTMF sms type : "+type);
		return type;
	}

	private static String getParameter(String type,String paramName) {
		Parameters param = parameterCacheManager.getParameter(type, paramName);
		if (param != null){
			String value = param.getValue();
			if (value != null) return value.trim();
		}
		return null;
	}
	
	private static ArrayList<String> tokenizeArrayList(String stringToTokenize, String delimiter)
	{
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize,
				delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());

		return result;
	}
}
