package com.onmobile.apps.ringbacktones.provisioning.implementation.StartCopy;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants;
import com.onmobile.apps.ringbacktones.activemonitoring.common.AMConstants.Severity;
import com.onmobile.apps.ringbacktones.activemonitoring.core.MonitorData;
import com.onmobile.apps.ringbacktones.activemonitoring.core.UrlResponseSampler;
import com.onmobile.apps.ringbacktones.common.RBTException;

import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
/**
 * A  class which has all the methods to process Copy Request from BTSL..and viceVersa.. 
 * @author bikash.panda
 *
 */
public class StartCopyProcessor extends Processor{

	protected Logger logger = null; 
	protected Logger rdcToCgiTransactionLogger = null;
	StartCopyPreProcessor startCopyPreProcessor=null;
	UrlResponseSampler startCopyUrlRespSampler = null;
	public StartCopyProcessor() throws RBTException {
		super();
		logger = Logger.getLogger(StartCopyProcessor.class);
		rdcToCgiTransactionLogger = Logger.getLogger("RbtRdcToCgiSongSelectionTransLog");
		startCopyPreProcessor=StartCopyPreProcessor.getInstance();
		startCopyUrlRespSampler = UrlResponseSampler.getInstance();
	}

	/**
	 * @param Task
	 * @return 
	 * Process Copy Request from BTSL etc.
	 */
	public void processStartCopyRequest(Task task){
		String method="processStartCopyRequest";
		String response=failureResponse;
		String url=null;
		boolean useTestUrl = false;
		Hashtable siteDetails = null;
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = -1;
		boolean isOnmobile = false;
		String circleID = null;
		logger.debug(method+" Task  : "+task);
		try{

			String smsType= task.getString(param_sms_type);
			String keyPressed= task.getString(param_keyPressed);
			String sourceClipName =task.getString(param_songname);
			String strSubId =task.getString(param_Called);
			String oprFlag =task.getString(param_opr_flag);
			String sel_by =task.getString(param_sel_by); 
			String caller_id = task.getString(param_caller);
			String wav_file = task.getString(param_clip_id);

			//validation of parameters
			if(strSubId == null || strSubId.trim().length() == 0 || strSubId.trim().equalsIgnoreCase("null"))
				strSubId =task.getString(param_subscriber_id);

			if(caller_id == null || caller_id.trim().length() == 0 || caller_id.trim().equalsIgnoreCase("null"))
				caller_id = task.getString(param_caller_id);

			if(wav_file == null || wav_file.trim().length() <= 0 || wav_file.trim().equalsIgnoreCase("null"))
				wav_file = task.getString(param_vcode);

			if(sel_by != null) 
				sel_by = sel_by.trim();

			if(oprFlag != null && !oprFlag.equalsIgnoreCase("null"))
				oprFlag = "1";
			else
				oprFlag = "0";

			if(wav_file != null && wav_file.indexOf(":") != -1)
				wav_file = wav_file.substring(0,wav_file.indexOf(":"));
			if(wav_file != null && wav_file.indexOf(".") != -1)
				wav_file = wav_file.substring(0,wav_file.indexOf("."));
			if(wav_file != null && wav_file.indexOf("rbt_") != -1)
				wav_file = wav_file.substring(4,wav_file.length());
			if(wav_file != null && wav_file.indexOf("_rbt") != -1)
				wav_file = wav_file.substring(0,wav_file.length()-4);
			if (keyPressed == null || keyPressed.trim().length() == 0) {
				task.setObject(param_response, response);
				logger.info("Keypressed not there returning :" + response);
				return;
			}
			logger.debug(method+"  Params : subscriber_id="+strSubId+" caller_id="+caller_id+" wav_file="+wav_file+" sel_by="+sel_by+" oprFlag="+oprFlag+" smstype="+smsType+" Keypressed="+keyPressed);

			//Get site Details
			Hashtable siteDetailsCalled = startCopyPreProcessor.getSiteDetails(strSubId);

			logger.debug(method+" siteDetailsCalled is "+ siteDetailsCalled);

			String  circleIDCalled =  null;
			if (siteDetailsCalled != null && siteDetailsCalled.containsKey(startCopyPreProcessor.CIRCLE_ID))
				circleIDCalled = (String)siteDetailsCalled.get(startCopyPreProcessor.CIRCLE_ID);
			if (circleIDCalled != null && circleIDCalled.equalsIgnoreCase("XOP") && wav_file.toUpperCase().indexOf("MISSING") == -1)
				wav_file =getWavFile(wav_file);
			logger.debug( method+"circleIDCalled  is "+ circleIDCalled +" wavfile "+wav_file);				

			if(startCopyPreProcessor.isCGICopyTestOn && startCopyPreProcessor.cGItestNumbers != null && startCopyPreProcessor.cGItestNumbers.contains(caller_id))
			{
				url = startCopyPreProcessor.cGItestNumUrl;
				useTestUrl = true;
			}
			else
				siteDetails = startCopyPreProcessor.getSiteDetails(caller_id);

			logger.debug( method+" siteDetails  is "+ siteDetails +" useTestUrl "+useTestUrl);


			if(!useTestUrl)
			{
				url = (String) siteDetails.get(startCopyPreProcessor.SITE_URL);
				useProxy = ((Boolean)siteDetails.get(startCopyPreProcessor.USE_PROXY)).booleanValue();
				proxyHost = (String) siteDetails.get(startCopyPreProcessor.PROXY_HOST);
				proxyPort = ((Integer) siteDetails.get(startCopyPreProcessor.PROXY_PORT)).intValue();
				isOnmobile = ((Boolean)siteDetails.get(startCopyPreProcessor.IS_ONMOBILE)).booleanValue();

				if (siteDetails.containsKey(startCopyPreProcessor.CIRCLE_ID))
					circleID = (String)siteDetails.get(startCopyPreProcessor.CIRCLE_ID);
				logger.debug( method+" iSonmobile : "+isOnmobile);

			}

			if(useTestUrl || isOnmobile)
			{
				if (circleID!=null && circleID.equalsIgnoreCase("XOP")){
					Clip clip= getClipByWavFile("rbt_"+wav_file+"_rbt");
					String clipID=null;
					if(clip!=null)
						clipID=clip.getClipId()+"";
					if(clipID==null)
						clipID="MISSING";
					wav_file = clipID+":rbt_"+wav_file+"_rbt";
				}
				else if(wav_file.toUpperCase().indexOf("MISSING") == -1) { 
					wav_file = "rbt_"+wav_file+"_rbt";
					wav_file += ":26:1";
				}
				int index=url.lastIndexOf("/");
				//sending to om
				logger.debug(method+"send om keypressed is "+ keyPressed);
				if(keyPressed!=null){
					logger.debug(method+"get smstype key press is "+ keyPressed);
					smsType=startCopyPreProcessor.getSMSType(keyPressed);
					logger.debug(method+ " smstype is "+ smsType);
				}
				url=url.substring(0,index)+"/rbt_cross_copy.jsp?subscriber_id="+strSubId+"&caller_id="+caller_id+"&clip_id="+wav_file+"&sel_by="+sel_by+"&sms_type="+smsType+"&keypressed="+keyPressed+"&source_op="+startCopyPreProcessor.interOperatorCopySourceOpr;
				logger.info(method+" final url is " + url);
			}
			else if (circleID!=null && circleID.equalsIgnoreCase("LL")){ //added for supporting new landline config
				url=url+"callermsisdn="+caller_id+"&receivermsisdn="+strSubId+"&songvcode="+wav_file+"&oprFlag="+oprFlag;
				logger.info(method+" Landline url to hit is "+ url);
			}
			else{
				//sending to bts
				logger.debug(method+ " sending to  btsl smstype is  "+smsType);
				if(keyPressed == null || keyPressed.trim().length() == 0)
					keyPressed=startCopyPreProcessor.getKeyPressed(smsType);
				logger.debug(method+"get  key press "+ keyPressed);
				if(wav_file.equalsIgnoreCase("MISSING")){
					wav_file=null;
					logger.debug(method+" final bts url wav_file is Missing No hit to go ");
				}
				url=url+"called="+strSubId+"&caller="+caller_id+"&vcode="+wav_file+"&oprFlag="+oprFlag+"&keyPressed="+keyPressed;
				logger.info(method+"final bts url is " + url);
			}
			if(circleID != null && circleID.trim().equalsIgnoreCase("XOP") && sel_by != null && sel_by.indexOf("XCOPY") != -1)
			{
				logger.info("Copy req from msisdn "+caller_id+" in infinite loop. Dropping the request.");
				task.setObject(param_response, response);
				return;
			}	
				
			
			StringBuffer responseString=new StringBuffer();
			Integer statusCode=new Integer("0");
			boolean responseStatus=false;
			if(sourceClipName != null && url != null && url.indexOf("<songname>") != -1)
				url = url.replaceAll("<songname>", sourceClipName);
			startCopyPreProcessor.addToAccounting("START_REQUEST", url, "-", startCopyPreProcessor.copyAccounting);
			if(wav_file != null && wav_file.toLowerCase().indexOf("default") == -1)
				responseStatus = startCopyPreProcessor.callURL(url,responseString,statusCode,useProxy,proxyHost,proxyPort);
			startCopyPreProcessor.addToAccounting("START_RESPONSE", url, responseString.toString(),startCopyPreProcessor.copyAccounting);
			if(responseStatus)
				response = responseString.toString().trim();
			task.setObject(param_response, response);
			
			// raising or clearing alarms
			Severity severity = Severity.CLEAR;
			String message = null;
			if (response != null && (response.indexOf("TimeOutException") != -1 || response.indexOf("FAILURE") != -1))
			{
				message = "Couldn't establish connection to remote url";
				severity = Severity.CRITICAL;
			}
			startCopyUrlRespSampler.recordUrlResponse(new MonitorData(AMConstants.THIRDPARTY, url, severity, message));
						
			logger.debug(method+" Start Copy result for the following url "+ url + " is -> "+response);
		}catch(Exception e){
			logger.info(method+" Exception  : "+e.getMessage());
			task.setObject(param_response, response);
		}
	}
	
	
	/**
	 * @param Task
	 * @return 
	 * Process Copy Request from BTSL etc.
	 */
	public void processRdcToCgiSongSelectionRequest(Task task){
		String method="processSongSelectionRequest";
		String response=failureResponse;
		String url=null;
		boolean useTestUrl = false;
		Hashtable siteDetails = null;
		boolean useProxy = false;
		String proxyHost = null;
		int proxyPort = -1;
		boolean isOnmobile = false;
		String circleID = null;
		logger.debug(method+" Task  : "+task);
		try{
			
			//MSISDN,TONE_ID,CATEGORY_ID,ADD_IN_LOOP,SUB_CLASS,MODE,STATUS
			String strSubId =task.getString("MSISDN");
			String toneId = task.getString("TONE_ID");
			String category_id = task.getString("CATEGORY_ID");
			String addInLoop = task.getString("ADD_IN_LOOP");
			String subClass = task.getString("SUB_CLASS");
			String mode = task.getString("MODE");
			String status = task.getString("STATUS");
            String modeInfo = task.getString("MODE_INFO");
            String useUIChrgClass = task.getString("USE_UI_CHARGE_CLASS");
            String chargeClass = task.getString("CHARGE_CLASS");
			//validation of parameters
			if(strSubId == null || strSubId.trim().length() == 0 || strSubId.trim().equalsIgnoreCase("null"))
				strSubId =task.getString(param_subscriber_id);

			logger.debug(method+"  Params : subscriber_id="+strSubId+" clipId="+toneId+" mode="+mode+" category_id="+category_id+" addInLoop="+addInLoop+" subClass="+subClass+" status="+status);
			

			//Get site Details
			siteDetails = startCopyPreProcessor.getRdcToCgiSelectionSiteDetails(strSubId,mode);
			
			if(siteDetails == null) {
				rdcToCgiTransactionLogger.info("Not a valid subscriber " + strSubId + " because could not find cirle from the subscriber");
				return;
			}

			logger.debug(method+" siteDetails is "+ siteDetails);

			url = (String) siteDetails.get(startCopyPreProcessor.SITE_URL);
			useProxy = ((Boolean)siteDetails.get(startCopyPreProcessor.USE_PROXY)).booleanValue();
			proxyHost = (String) siteDetails.get(startCopyPreProcessor.PROXY_HOST);
			proxyPort = ((Integer) siteDetails.get(startCopyPreProcessor.PROXY_PORT)).intValue();
			isOnmobile = ((Boolean)siteDetails.get(startCopyPreProcessor.IS_ONMOBILE)).booleanValue();

			if (siteDetails.containsKey(startCopyPreProcessor.CIRCLE_ID))
				circleID = (String)siteDetails.get(startCopyPreProcessor.CIRCLE_ID);
			logger.debug( method+" iSonmobile : "+isOnmobile);



			if(isOnmobile)
			{
				url=url + "?MSISDN="+strSubId+"&TONE_ID="+toneId+"&MODE="+mode+"&STATUS="+status+"&CATEGORY_ID="+category_id+"&ADD_IN_LOOP="+addInLoop+"&SUB_CLASS="+subClass+"&MODE_INFO="+modeInfo;
				if (useUIChrgClass != null && useUIChrgClass.equalsIgnoreCase("true")) {
					url = url + "&USE_UI_CHARGE_CLASS=true";
				}
				if(chargeClass!=null){
					url = url + "&CHARGE_CLASS="+chargeClass;
				}
				logger.info(method+" final url is " + url);
			}
			else{
				//sending to bts
				String wavFile = getWavFile(toneId);
				url=url+"called="+strSubId+"&vcode="+wavFile;
				logger.info(method+"final bts url is " + url);
			}

			StringBuffer responseString=new StringBuffer();
			Integer statusCode=new Integer("0");
			boolean responseStatus=false;

			long currentTime = System.currentTimeMillis();
			responseStatus = startCopyPreProcessor.callURL(url,responseString,statusCode,useProxy,proxyHost,proxyPort);
			currentTime = System.currentTimeMillis() - currentTime;
			if(responseStatus)
				response = responseString.toString().trim();
			task.setObject(param_response, response);
			rdcToCgiTransactionLogger.info("Hitted Url: " + task.getString("HittedUtl") + " hitting url: " + url + " Response:" + response + " Time taken: " + currentTime);
						
			logger.debug(method+" Start Copy result for the following url "+ url + " is -> "+response);
		}catch(Exception e){
			logger.info(method+" Exception  : "+e.getMessage());
			task.setObject(param_response, response);
		}
	}


	/**
	 * @param Task
	 * @return String
	 * Validate Parameters and put proper values
	 */
	public String validateParameters(Task task){
		String response="valid";
		String method="validateParameters";
		return response;
	}
	public String getWavFile(String vcode){
		String wavFile=null;
		//Clip clip=getClipByWavFile("rbt_"+vcode.trim()+"_rbt");
		Clip clip=getClipById(vcode);
		if(clip!=null)
			wavFile=clip.getClipRbtWavFile();
		if(wavFile!=null){
			if(wavFile.endsWith(".wav")) 
				wavFile = wavFile.substring(0,wavFile.length()-4); 
			if(wavFile.startsWith("rbt_")) 
				wavFile = wavFile.substring(4); 
			if(wavFile.endsWith("_rbt")) 
				wavFile = wavFile.substring(0,wavFile.length()-4);
		}
		return wavFile;
	}
	public Task getTask(HashMap<String, String> requestParams) {
		HashMap<String, Object> taskSession = new HashMap<String, Object>();
		taskSession.putAll(requestParams);
		Task task = new Task(null, taskSession);
		return task;
	}
	public boolean isValidPrefix(String subId) {
		return false;
	}
	public void processGiftAckRequest(Task task) {
	}
	public void processSelection(Task task) {
	}
}
