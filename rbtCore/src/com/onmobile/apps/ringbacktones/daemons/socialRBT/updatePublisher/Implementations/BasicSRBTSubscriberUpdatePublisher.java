package com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.Implementations;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpResponseBean;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher.SRBTSubscriberUpdatePublisher;
import com.onmobile.apps.ringbacktones.srbt.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSiteUserDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSubscriberDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSiteUser;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSubscriber;

public class BasicSRBTSubscriberUpdatePublisher implements SRBTSubscriberUpdatePublisher {
	static SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
	static Logger logger = Logger.getLogger(BasicSRBTSubscriberUpdatePublisher.class);
	static String className="BasicSRBTSubscriberUpdatePublisher";
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
	
	public boolean publishSubscriberUpdate(RbtSocialSubscriber rbtSocialSubscriber) {
		String method = "publishSubscriberUpdate";
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
		Date dt=rbtSocialSubscriber.getStartDate();
		String setTime=sdf.format(dt);
		
		long sngID = -1;
		try
		{
			// Currently assuming only one social site
			List<RbtSocialSiteUser> socialUserList = RbtSocialSiteUserDAO.load(rbtSocialSubscriber.getMsisdn());
			if (socialUserList != null && socialUserList.size() > 0)
				sngID = socialUserList.get(0).getSngId();
		}
		catch(DataAccessException e)
		{
			logger.error("Exception", e);
		}
		String finalUrl=url
		+"&subId="+rbtSocialSubscriber.getMsisdn()
		+"&setTime="+SRBTUtility.getEncodedUrlString(setTime)
		+"&info="+SRBTUtility.getEncodedUrlString(rbtSocialSubscriber.getInfo())
		+"&subClass="+SRBTUtility.getEncodedUrlString(rbtSocialSubscriber.getSubscriptionClassType())
		+"&circleId="+SRBTUtility.getEncodedUrlString(rbtSocialSubscriber.getCircleId())
		+"&operatorSqnId="+rbtSocialSubscriber.getOperatorSqnId()
		+"&mode="+rbtSocialSubscriber.getMode()
		+"&evtType="+rbtSocialSubscriber.getEvtType()
		+"&actionApi="+SRBTUtility.getEncodedUrlString("publishSubscriberAction.action");
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
	
	public static void main(String args[]) {
		RbtSocialSubscriber socialSub = RbtSocialSubscriberDAO.loadSingle(RbtSocialSubscriber.class, "select * from RBT_SOCIAL_SUBSCRIBER where sequence_id= "+ 10);
		new BasicSRBTSubscriberUpdatePublisher().publishSubscriberUpdate(socialSub);
	}

}
