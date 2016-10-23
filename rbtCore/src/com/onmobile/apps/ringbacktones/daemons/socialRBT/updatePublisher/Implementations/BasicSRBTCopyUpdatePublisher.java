package com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpResponseBean;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTCopyUpdatePublisher;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SocialRBTBaseUpdatePublisher;
import com.onmobile.apps.ringbacktones.srbt.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSiteUserDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialCopy;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSiteUser;

public class BasicSRBTCopyUpdatePublisher implements SRBTCopyUpdatePublisher {
	static SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	static Logger logger = Logger.getLogger(SocialRBTBaseUpdatePublisher.class);
	static String className="BasicSRBTCopyUpdatePublisher";
	private static boolean useProxy=false;
	private static String proxyHost=null;
	private static int proxyPort=80;
	private static boolean toRetry=false;
	private static int timeOut=10000;
	private static String basePublishingUrl=null;
	private static boolean callVariablesInitialized=false;
	private static final String baseAction ="activation";
	
	
	
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


	public boolean publishCopyUpdate(RbtSocialCopy rbtSocialCopy) {
		String method = "publishCopyUpdate";
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
		url =url+"action="+baseAction;
		Date dt=rbtSocialCopy.getStartDate();
		String setTime=sdf.format(dt);

		long sngID = -1;
		try
		{
			// Currently assuming only one social site
			List<RbtSocialSiteUser> socialUserList = RbtSocialSiteUserDAO.load(rbtSocialCopy.getMsisdn());
			if (socialUserList != null && socialUserList.size() > 0)
				sngID = socialUserList.get(0).getSngId();
		}
		catch(DataAccessException e)
		{
			logger.error("Exception", e);
		}
				
		String finalUrl=url
		+"&subId="+rbtSocialCopy.getMsisdn()
		+"&setTime="+SRBTUtility.getEncodedUrlString(setTime)
		+"&startTime="+SRBTUtility.getEncodedUrlString(setTime)
		+"&info="+SRBTUtility.getEncodedUrlString(rbtSocialCopy.getInfo())
		+"&copySubId="+rbtSocialCopy.getCopyForMsisdn()
		+"&clipId="+rbtSocialCopy.getClipId()
		+"&catId="+rbtSocialCopy.getCatId()
		+"&status="+rbtSocialCopy.getStatus()
		+"&operatorSqnId="+rbtSocialCopy.getOperatorSqnId()
		+"&circleId="+rbtSocialCopy.getCircleId()
		+"&mode="+rbtSocialCopy.getMode()
		+"&actionApi="+SRBTUtility.getEncodedUrlString("publishCopyAction.action")
		+"&toneType="+rbtSocialCopy.getToneType();
		if(sngID != -1)
		{
			finalUrl += "&sngId="+sngID;
		}
		if (rbtSocialCopy.getSourceUserId() != null)
		{
			finalUrl += "&sourceUserId="+rbtSocialCopy.getSourceUserId();
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
