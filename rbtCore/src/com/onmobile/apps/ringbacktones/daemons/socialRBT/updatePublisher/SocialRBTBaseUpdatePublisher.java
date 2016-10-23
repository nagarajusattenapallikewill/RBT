package com.onmobile.apps.ringbacktones.daemons.socialRBT.updatePublisher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpResponseBean;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUser;
import com.onmobile.apps.ringbacktones.wrappers.RBTHibernateDBImplementationWrapper;



public class SocialRBTBaseUpdatePublisher implements iRBTConstant{
	static SimpleDateFormat sdf=new SimpleDateFormat("yyMMddHHmmssZ");
	static Logger logger = Logger.getLogger(SocialRBTBaseUpdatePublisher.class);
	static String className="SocialRBTBaseUpdatePublisher";
	private static boolean useProxy=false;
	private static String proxyHost=null;
	private static int proxyPort=80;
	private static boolean toRetry=false;
	private static int timeOut=10000;
	private static String baseActPublishingUrl=null;
	private static String baseDeactPublishingUrl=null;
	private static String selActPublishingUrl=null;
	private static String selDeactPublishingUrl=null;
	private static String downloadActPublishingUrl=null;
	private static String downloadDeactPublishingUrl=null;
	private static boolean callVariablesInitialized=false;
	private static String baseActAction="activation";
	private static String baseDeactAction="deactation";
	private static String selActAction="selAct";
	private static String selDeactAction="selDeact";
	private static String downloadActAction="downloadAct";
	private static String downloadDectAction="downloadDeact";
	
	public void init(){
		String method="init";
	//	logger.info(className+"->"+method+"->inside init()");
		if(!callVariablesInitialized){
		//	logger.info(className+"->"+method+"->!callVariablesInitialized");
			useProxy=SRBTUtility.getParamAsBoolean("USE_PROXY", "false");
			proxyHost=SRBTUtility.getParamAsString("PROXY_HOST", null);
			proxyPort=SRBTUtility.getParamAsInt("PROXY_PORT", 80);
			toRetry=SRBTUtility.getParamAsBoolean("TO_RETRY", "false");
			timeOut=SRBTUtility.getParamAsInt("TIME_OUT", 10000);
	//		logger.info(className+"->"+method+"->useProxy="+useProxy+";proxyHost="+proxyHost+";proxyPort="+proxyPort+";toRetry="+toRetry+";timeOut="+timeOut);
			callVariablesInitialized=true;
		}
		if(baseActPublishingUrl==null){
			baseActPublishingUrl=SRBTUtility.getParamAsString("BASE_ACTIVATION_PUBLISHING_URL", null);
		}
		if(baseDeactPublishingUrl==null){
			baseDeactPublishingUrl=SRBTUtility.getParamAsString("BASE_DEACTIVATION_PUBLISHING_URL", baseActPublishingUrl);
		}
		if(selActPublishingUrl==null){
			selActPublishingUrl=SRBTUtility.getParamAsString("SEL_ACTIVATION_PUBLISHING_URL", baseActPublishingUrl);
		}
		if(selDeactPublishingUrl==null){
			selDeactPublishingUrl=SRBTUtility.getParamAsString("SEL_DEACTIVATION_PUBLISHING_URL", baseActPublishingUrl);
		}
		if(downloadActPublishingUrl==null){
			downloadActPublishingUrl=SRBTUtility.getParamAsString("DOWNLOAD_ACTIVATION_PUBLISHING_URL", baseActPublishingUrl);
		}
		if(downloadDeactPublishingUrl==null){
			downloadDeactPublishingUrl=SRBTUtility.getParamAsString("DOWNLOAD_DEACTIVATION_PUBLISHING_URL", baseActPublishingUrl);
		}
//		logger.info(className+"->"+method+"->baseActPublishingUrl="+baseActPublishingUrl+";baseDeactPublishingUrl="+baseDeactPublishingUrl
//				+";selActPublishingUrl="+selActPublishingUrl+";selDeactPublishingUrl="+selDeactPublishingUrl+";downloadActPublishingUrl="+downloadActPublishingUrl+";downloadDeactPublishingUrl="+downloadDeactPublishingUrl);
	}
	
	public RBTSocialUpdate updatePublisher(RBTSocialUpdate rbtSocialUpdate){
		init();
		RBTSocialUpdate rbtSocialUpdateResponse=null;
		//logger.info("rbtSocialUpdate="+rbtSocialUpdate.toString());
		if(rbtSocialUpdate.getEventType()==iRBTConstant.eventTypeForSocialUserBaseActivation){
			return baseAvtivationUpdatePublisher( rbtSocialUpdate);
		}else if(rbtSocialUpdate.getEventType()==iRBTConstant.eventTypeForSocialUserBaseDeactivation){
			return baseDeavtivationUpdatePublisher( rbtSocialUpdate);
		}else if(rbtSocialUpdate.getEventType()==iRBTConstant.eventTypeForSocialUserDownloadActivation){
			return downloadAvtivationUpdatePublisher( rbtSocialUpdate);
		}else if(rbtSocialUpdate.getEventType()==iRBTConstant.eventTypeForSocialUserDownloadDeactivation){
			return downloadDeavtivationUpdatePublisher( rbtSocialUpdate);
		}else if(rbtSocialUpdate.getEventType()==iRBTConstant.eventTypeForSocialUserSelActivation){
			return selAvtivationUpdatePublisher( rbtSocialUpdate);
		}else if(rbtSocialUpdate.getEventType()==iRBTConstant.eventTypeForSocialUserSelDeactivation){
			return selDeavtivationUpdatePublisher( rbtSocialUpdate);
		}
		//logger.info("rbtSocialUpdateResponse="+rbtSocialUpdateResponse.toString());
		return rbtSocialUpdateResponse;
	}
	
	protected RBTSocialUpdate baseAvtivationUpdatePublisher(RBTSocialUpdate rbtSocialUpdate){
		String method="baseAvtivationUpdatePublisher";
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		String url=baseActPublishingUrl;
		url=url+"?action="+baseActAction;
		List<RBTSocialUser> rbtSocialUserList=RBTHibernateDBImplementationWrapper.getInstance().getSNGUser(rbtSocialUpdate.getMsisdn());
		if(rbtSocialUserList!=null && rbtSocialUserList.size()>0){
			for(int count=0;count<rbtSocialUserList.size();count++){
				RBTSocialUser rbtSocialUser=rbtSocialUserList.get(count);
				if(rbtSocialUser!=null){
					String userId=rbtSocialUser.getUserId();
					Date dt=rbtSocialUpdate.getStartTime();
					String setTime=sdf.format(dt);
//					String setTime=rbtSocialUpdate.getStartTime().toLocaleString();
					String finalUrl=url+"&userId="+SRBTUtility.getEncodedUrlString(userId)+"&setTime="+setTime;
					logger.info(className+"->"+method+"->finalUrl="+finalUrl);
					HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
					if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
						if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
							rbtSocialUpdate.setStatus(iRBTConstant.successfullyPublishedStatus);
						}else{
							rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
						}
					}else{
						if(reponseBean==null){
						//	logger.info(className+"->"+method+"->reponseBean==null");
						}
						if(!reponseBean.isResponseReceived()){
						//	logger.info(className+"->"+method+"->!reponseBean.isResponseReceived()");
						}
						if(reponseBean.getResponse()==null){
						//	logger.info(className+"->"+method+"->reponseBean.getResponse()==null");
						}
						rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
					}
				}
			}
		}
		RBTHibernateDBImplementationWrapper.getInstance().updateRBTSocialUpdateStatusCache(rbtSocialUpdate.getId(),rbtSocialUpdate.getMsisdn(),rbtSocialUpdate.getStatus());
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		return rbtSocialUpdate;
	}
	
	protected RBTSocialUpdate baseDeavtivationUpdatePublisher(RBTSocialUpdate rbtSocialUpdate){
		String method="baseDeavtivationUpdatePublisher";
		/**
		String url=baseDeactPublishingUrl;
		url=url+"?action="+baseDeactAction;
		List<RBTSocialUser> rbtSocialUserList=RBTHibernateDBImplementationWrapper.getInstance().getSNGUser(rbtSocialUpdate.getMsisdn());
		if(rbtSocialUserList!=null && rbtSocialUserList.size()>0){
			for(int count=0;count<rbtSocialUserList.size();count++){
				RBTSocialUser rbtSocialUser=rbtSocialUserList.get(count);
				if(rbtSocialUser!=null){
					String userId=rbtSocialUser.getUserId();
					Date dt=rbtSocialUpdate.getStartTime();
					String setTime=sdf.format(dt);
//					String setTime=rbtSocialUpdate.getStartTime().toLocaleString();
					String finalUrl=url+"&userId="+SRBTUtility.getEncodedUrlString(userId)+"&setTime="+setTime;
					logger.info(className+"->"+method+"->finalUrl="+finalUrl);
					HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
					if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
						if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
							rbtSocialUpdate.setStatus(iRBTConstant.successfullyPublishedStatus);
						}else{
							rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
						}
					}else{
						rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
					}
				}
			}
		}
		**/
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		rbtSocialUpdate.setStatus(iRBTConstant.notValidUpdateStatus);
		RBTHibernateDBImplementationWrapper.getInstance().updateRBTSocialUpdateStatusCache(rbtSocialUpdate.getId(),rbtSocialUpdate.getMsisdn(),rbtSocialUpdate.getStatus());
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		return rbtSocialUpdate;
	}
	protected RBTSocialUpdate downloadAvtivationUpdatePublisher(RBTSocialUpdate rbtSocialUpdate){
		String method="downloadAvtivationUpdatePublisher";
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		String url=downloadActPublishingUrl;
		url=url+"?action="+downloadActAction;
		List<RBTSocialUser> rbtSocialUserList=RBTHibernateDBImplementationWrapper.getInstance().getSNGUser(rbtSocialUpdate.getMsisdn());
		if(rbtSocialUserList!=null && rbtSocialUserList.size()>0){
			for(int count=0;count<rbtSocialUserList.size();count++){
				RBTSocialUser rbtSocialUser=rbtSocialUserList.get(count);
				if(rbtSocialUser!=null){
					String userId=rbtSocialUser.getUserId();
					Date dt=rbtSocialUpdate.getStartTime();
					String setTime=sdf.format(dt);
//					String setTime=rbtSocialUpdate.getStartTime().toLocaleString();
					String finalUrl=url+"&userId="+SRBTUtility.getEncodedUrlString(userId)+"&setTime="+setTime;
					StringBuffer strBuff=new StringBuffer();
					boolean isValidMetaData=SRBTUtility.getDownlodUrlMetaData(finalUrl, rbtSocialUpdate,strBuff);
					if(isValidMetaData && strBuff!=null){
					finalUrl=strBuff.toString();
					logger.info(className+"->"+method+"->finalUrl="+finalUrl);
					HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
					if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
						if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
							rbtSocialUpdate.setStatus(iRBTConstant.successfullyPublishedStatus);
						}else{
							rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
						}
					}else{
						rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
					}
					}
				}
			}
		}
		RBTHibernateDBImplementationWrapper.getInstance().updateRBTSocialUpdateStatusCache(rbtSocialUpdate.getId(),rbtSocialUpdate.getMsisdn(),rbtSocialUpdate.getStatus());
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		return rbtSocialUpdate;
	}
	
	protected RBTSocialUpdate downloadDeavtivationUpdatePublisher(RBTSocialUpdate rbtSocialUpdate){
		String method="downloadDeavtivationUpdatePublisher";
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		String url=downloadDeactPublishingUrl;
		/**
		url=url+"?action="+downloadDeactAction;
		List<RBTSocialUser> rbtSocialUserList=RBTHibernateDBImplementationWrapper.getInstance().getSNGUser(rbtSocialUpdate.getMsisdn());
		if(rbtSocialUserList!=null && rbtSocialUserList.size()>0){
			for(int count=0;count<rbtSocialUserList.size();count++){
				RBTSocialUser rbtSocialUser=rbtSocialUserList.get(count);
				if(rbtSocialUser!=null){
					String userId=rbtSocialUser.getUserId();
					Date dt=rbtSocialUpdate.getStartTime();
					String setTime=sdf.format(dt);
//					String setTime=rbtSocialUpdate.getStartTime().toLocaleString();
					String finalUrl=url+"&userId="+SRBTUtility.getEncodedUrlString(userId)+"&setTime="+setTime;
					StringBuffer strBuff=new StringBuffer();
					boolean isValidMetaData=SRBTUtility.getDownlodUrlMetaData(finalUrl, rbtSocialUpdate,strBuff);
					if(isValidMetaData && strBuff!=null){
					finalUrl=strBuff.toString();
					logger.info(className+"->"+method+"->finalUrl="+finalUrl);
					HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
					if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
						if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
							rbtSocialUpdate.setStatus(iRBTConstant.successfullyPublishedStatus);
						}else{
							rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
						}
					}else{
						rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
					}
					}
				}
			}
		}
		**/
		rbtSocialUpdate.setStatus(iRBTConstant.notValidUpdateStatus);
		RBTHibernateDBImplementationWrapper.getInstance().updateRBTSocialUpdateStatusCache(rbtSocialUpdate.getId(),rbtSocialUpdate.getMsisdn(),rbtSocialUpdate.getStatus());
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		return rbtSocialUpdate;
	}
	
	protected RBTSocialUpdate selAvtivationUpdatePublisher(RBTSocialUpdate rbtSocialUpdate){
		String method="selAvtivationUpdatePublisher";
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		String url=selActPublishingUrl;
		url=url+"?action="+selActAction;
		List<RBTSocialUser> rbtSocialUserList=RBTHibernateDBImplementationWrapper.getInstance().getSNGUser(rbtSocialUpdate.getMsisdn());
		if(rbtSocialUserList!=null && rbtSocialUserList.size()>0){
			for(int count=0;count<rbtSocialUserList.size();count++){
				RBTSocialUser rbtSocialUser=rbtSocialUserList.get(count);
				if(rbtSocialUser!=null){
					String userId=rbtSocialUser.getUserId();
					Date dt=rbtSocialUpdate.getStartTime();
					String setTime=sdf.format(dt);
//					String setTime=rbtSocialUpdate.getStartTime().toLocaleString();
					String finalUrl=url+"&userId="+SRBTUtility.getEncodedUrlString(userId)+"&setTime="+setTime;
					StringBuffer strBuff=new StringBuffer();
					boolean isValidMetaData=SRBTUtility.getSelectionUrlMetaData(finalUrl, rbtSocialUpdate,strBuff);
					if(isValidMetaData && strBuff!=null){
					finalUrl=strBuff.toString();
					logger.info(className+"->"+method+"->finalUrl="+finalUrl);
					HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
					if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
						if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
							rbtSocialUpdate.setStatus(iRBTConstant.successfullyPublishedStatus);
						}else{
							rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
						}
					}else{
						rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
					}
					}
				}
			}
		}
		RBTHibernateDBImplementationWrapper.getInstance().updateRBTSocialUpdateStatusCache(rbtSocialUpdate.getId(),rbtSocialUpdate.getMsisdn(),rbtSocialUpdate.getStatus());
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		return rbtSocialUpdate;
	}
	
	protected RBTSocialUpdate selDeavtivationUpdatePublisher(RBTSocialUpdate rbtSocialUpdate){
		String method="selDeavtivationUpdatePublisher";
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		String url=selDeactAction;
		/**
		url=url+"?action="+selDeactAction;
		List<RBTSocialUser> rbtSocialUserList=RBTHibernateDBImplementationWrapper.getInstance().getSNGUser(rbtSocialUpdate.getMsisdn());
		if(rbtSocialUserList!=null && rbtSocialUserList.size()>0){
			for(int count=0;count<rbtSocialUserList.size();count++){
				RBTSocialUser rbtSocialUser=rbtSocialUserList.get(count);
				if(rbtSocialUser!=null){
					String userId=rbtSocialUser.getUserId();
					Date dt=rbtSocialUpdate.getStartTime();
					String setTime=sdf.format(dt);
//					String setTime=rbtSocialUpdate.getStartTime().toLocaleString();
					String finalUrl=url+"&userId="+SRBTUtility.getEncodedUrlString(userId)+"&setTime="+setTime;
					StringBuffer strBuff=new StringBuffer();
					boolean isValidMetaData=SRBTUtility.getSelectionUrlMetaData(finalUrl, rbtSocialUpdate,strBuff);
					if(isValidMetaData && strBuff!=null){
					finalUrl=strBuff.toString();
					logger.info(className+"->"+method+"->finalUrl="+finalUrl);
					HttpResponseBean reponseBean= SRBTUtility.makeSocialRBTHttpCall(finalUrl,useProxy, proxyHost, proxyPort, toRetry, timeOut);
					if(reponseBean!=null && reponseBean.isResponseReceived() && reponseBean.getResponse()!=null){
						if(reponseBean.getResponse()!=null && (reponseBean.getResponse().trim().indexOf("success")!=-1 || reponseBean.getResponse().trim().indexOf("Success")!=-1 || reponseBean.getResponse().trim().indexOf("SUCCESS")!=-1)){
							rbtSocialUpdate.setStatus(iRBTConstant.successfullyPublishedStatus);
						}else{
							rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
						}
					}else{
						rbtSocialUpdate.setStatus(iRBTConstant.publishingErrorStatus);
					}
					}
				}
			}
		}
		**/
		rbtSocialUpdate.setStatus(iRBTConstant.notValidUpdateStatus);
		RBTHibernateDBImplementationWrapper.getInstance().updateRBTSocialUpdateStatusCache(rbtSocialUpdate.getId(),rbtSocialUpdate.getMsisdn(),rbtSocialUpdate.getStatus());
	//	logger.info(className+"->"+method+"->rbtSocialUpdate="+rbtSocialUpdate.toString());
		return rbtSocialUpdate;
	}
}
