package com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpResponseBean;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTSelectionUpdatePublisher;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SocialRBTBaseUpdatePublisher;
import com.onmobile.apps.ringbacktones.srbt.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSiteUserDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSelections;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSiteUser;

public class BasicSRBTSelectionUpdatePublisher implements SRBTSelectionUpdatePublisher {
	static SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	static Logger logger = Logger.getLogger(BasicSRBTSelectionUpdatePublisher.class);
	static String className="BasicSRBTSelectionUpdatePublisher";
	private static boolean useProxy=false;
	private static String proxyHost=null;
	private static int proxyPort=80;
	private static boolean toRetry=false;
	private static int timeOut=10000;
	private static String basePublishingUrl=null;
	private static boolean callVariablesInitialized=false;
	private static final String baseActAction="activation";
	private static final String baseDeactAction="deactation";
	
	
	public void init(){
		String method="init";
		logger.info(className+"->"+method+"->inside init()");
		if(!callVariablesInitialized){
			logger.info(className+"->"+method+"->!callVariablesInitialized");
			useProxy=SRBTUtility.getParamAsBoolean("USE_PROXY", "false");
			proxyHost=SRBTUtility.getParamAsString("PROXY_HOST", null);
			proxyPort=SRBTUtility.getParamAsInt("PROXY_PORT", 80);
			toRetry=SRBTUtility.getParamAsBoolean("TO_RETRY", "false");
			timeOut=SRBTUtility.getParamAsInt("TIME_OUT", 10000);
			logger.info(className+"->"+method+"->useProxy="+useProxy+";proxyHost="+proxyHost+";proxyPort="+proxyPort+";toRetry="+toRetry+";timeOut="+timeOut);
			callVariablesInitialized=true;
		}
		if(basePublishingUrl==null){
			basePublishingUrl = SRBTUtility.getParamAsString("BASE_PUBLISHING_URL", null);
		}
		logger.info(className+"->"+method+"->baseActPublishingUrl="+basePublishingUrl);
	}
	
	public boolean publishSelectionUpdate(RbtSocialSelections rbtSocialSelections){
		String method = "publishSelectionUpdate";
		boolean returnFlag = false;
		init();
		String url=basePublishingUrl;
		if(url !=null){
			if(url.charAt(url.length()-1)!= '/'){
				url=url+"/publishAction.action?";
			}else{
				url=url+"publishAction.action?";
			}
		}
		
		url =url+"action="+baseActAction;
		Date dt=rbtSocialSelections.getStartDate();
		String setTime=sdf.format(dt);
		
		long sngID = -1;
		try
		{
			// Currently assuming only one social site
			List<RbtSocialSiteUser> socialUserList = RbtSocialSiteUserDAO.load(rbtSocialSelections.getMsisdn());
			if (socialUserList != null && socialUserList.size() > 0)
				sngID = socialUserList.get(0).getSngId();
		}
		catch(DataAccessException e)
		{
			logger.error("Exception", e);
		}
		
		String finalUrl=url
		+"&subId="+rbtSocialSelections.getMsisdn()
		+"&callerId="+rbtSocialSelections.getCallerMsisdn()
		+"&setTime="+SRBTUtility.getEncodedUrlString(setTime)
		+"&info="+SRBTUtility.getEncodedUrlString(rbtSocialSelections.getInfo())
		+"&clipId="+rbtSocialSelections.getClipId()
		+"&catId="+rbtSocialSelections.getCatId()
		+"&chargeClass="+SRBTUtility.getEncodedUrlString(rbtSocialSelections.getChargeClassType())
		+"&circleId="+SRBTUtility.getEncodedUrlString(rbtSocialSelections.getCircleId())
		+"&operatorSqnId="+rbtSocialSelections.getOperatorSqnId()
		+"&mode="+rbtSocialSelections.getMode()
		+"&actionApi="+SRBTUtility.getEncodedUrlString("publishSelectionAction.action")
		+"&toneType="+rbtSocialSelections.getToneType();
		if(sngID != -1)
		{
			finalUrl += "&sngId="+sngID;
		}
		
		logger.info(className+"->"+method+"->finalUrl="+finalUrl);
		HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
		if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
			if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
				returnFlag = true;
			}
			logger.info(className+"->"+method+"->reponseBean.getResponse()="+reponseBean.getResponse());
		}
		logger.info(className+"->"+method+"->returnFlag="+returnFlag);
			
		return returnFlag;
	}

}
