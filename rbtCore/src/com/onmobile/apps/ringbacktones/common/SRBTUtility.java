package com.onmobile.apps.ringbacktones.common;

import java.io.StringWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.net.URLCodec;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTSocialUpdate;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialCopyDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialDownloadsDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialGiftDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSelectionsDAO;
import com.onmobile.apps.ringbacktones.srbt.dao.RbtSocialSubscriberDAO;
import com.onmobile.apps.ringbacktones.srbt.dos.RbtSocialSelections;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.MemCacheWrapper;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;
import com.onmobile.apps.ringbacktones.wrappers.RBTHibernateDBImplementationWrapper;
import com.onmobile.apps.ringbacktones.wrappers.SRBTDaoWrapper;

public class SRBTUtility implements iRBTConstant{
	static Logger logger = Logger.getLogger(SRBTUtility.class);
	private static String classname = "SRBTUtility";
	private static TransformerFactory transformerFactory = null;
	private static RBTConnector rbtConnector=null;
	private static URLCodec m_urlEncoder = new URLCodec();


	public static String getInfoXML(InfoXMLBean infoXMLBean){
		String returnDocStr = null;
		Document document = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			document = documentBuilder.newDocument();
			Element element = document.createElement(SRBT);
			element = getInfoElement(document,infoXMLBean,element);
			document.appendChild(element);
//			element.appendChild(infoElem);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		if(document != null){
			returnDocStr = getStringFromDocument(document);
		}
		return returnDocStr;
	} 
	public static String getCircleId(String subId){
		SubscriberDetail subDet = getSubDetails(subId);
		if(subDet!=null){
			return subDet.getCircleID();
		}
		return null;
	}
	public static SubscriberDetail getSubDetails(String subId){
		MNPContext mnpContext = new MNPContext(subId);
		return RbtServicesMgr.getSubscriberDetail(mnpContext);
	}
	public static Element getInfoElement(Document document, InfoXMLBean infoXMLBean,Element element){
//		Element element = document.createElement(INFO);
		if(document!=null && infoXMLBean!=null){
			element.setAttribute(SRBT_COPY, infoXMLBean.isCopyAllowed() ? YES : NO);
			element.setAttribute(SRBT_GIFT, infoXMLBean.isGiftAllowed() ? YES : NO);
			if(infoXMLBean.getOmMsg()!=null && infoXMLBean.getOmMsg().length()>0){
				element.setAttribute(SRBT_OMMSG, infoXMLBean.getOmMsg());
			}
			if(infoXMLBean.getOprtMsg()!=null && infoXMLBean.getOprtMsg().length()>0){
				element.setAttribute(SRBT_OPRTMSG, infoXMLBean.getOmMsg());
			}
			if(infoXMLBean.getProMsg()!=null && infoXMLBean.getProMsg().length()>0){
				element.setAttribute(SRBT_PROMOMSG, infoXMLBean.getProMsg());
			}
			if(infoXMLBean.getUserMsg()!=null && infoXMLBean.getUserMsg().length()>0){
				element.setAttribute(SRBT_USERMSG, infoXMLBean.getUserMsg());
			}
			if(infoXMLBean.getUserSqnId()!= -1){
				element.setAttribute(SRBT_USER_SQN_ID, ""+infoXMLBean.getUserSqnId());
			}
			element.setAttribute(PUBLISH_CALLER_NAME, infoXMLBean.isPublishCallerNameAllowed() ? YES : NO);
			if(infoXMLBean.getMessageId()!= -1){
				element.setAttribute(MESSAGE_ID, ""+infoXMLBean.getMessageId());
			}
//			if(infoXMLBean.isAllowPublish()){
				element.setAttribute(ALLOW_PUBLISH, infoXMLBean.isAllowPublish() ? YES : NO);
//			}
			if(infoXMLBean.getOldSubscriberId() != null){
				element.setAttribute(OLD_SUBSCRIBER_ID, ""+infoXMLBean.getOldSubscriberId());
			}				
		}
		return element;
	}
	public static InfoXMLBean getInfoXMLBean(String action, String subAction){
		// action possible values are "download","selection","susbcriber","gift","copy"
		// subaction possible values are "act","deact"
		InfoXMLBean infoBean = new InfoXMLBean();
		if(subAction==null){
			subAction = "act";
		}
		if(SRBT_INFO_ATTR_CONFIG!=null && action!=null){
			action = action.trim().toLowerCase();
			subAction= subAction.trim().toLowerCase();
			StringTokenizer st= new StringTokenizer(SRBT_INFO_ATTR_CONFIG,",");
			while(st.hasMoreTokens()){
				String paramName = st.nextToken();
				if(paramName !=null ){
					paramName = paramName.trim().toLowerCase();
					//					sample param = download_act_copy
					String value = RBTConnector.getInstance().getRbtGenericCache().getParameter(SRBT, action+SRBT_PARAM_NAME_SEPERATOR+subAction+SRBT_PARAM_NAME_SEPERATOR+paramName);
					if(value != null && value.length()>0){
						value = value.trim();
						if(paramName.equalsIgnoreCase(SRBT_COPY) && value.equalsIgnoreCase("true")){
							infoBean.setCopyAllowed(true);
						}else if(paramName.equalsIgnoreCase(SRBT_GIFT) && value.equalsIgnoreCase("true")){
							infoBean.setGiftAllowed(true);
						}else if(paramName.equalsIgnoreCase(SRBT_OMMSG)){
							infoBean.setOmMsg(value);
						}else if(paramName.equalsIgnoreCase(SRBT_OPRTMSG)){
							infoBean.setOprtMsg(value);
						}else if(paramName.equalsIgnoreCase(SRBT_PROMOMSG)){
							infoBean.setProMsg(value);
						}
					}
				}
			}
		}
		return infoBean;
	}
	public static String getStringFromDocument(Document document){
		String method = "getStringFromDocument";
		if(document != null){
			try
			{
				DOMSource domSource = new DOMSource(document);
				StringWriter writer = new StringWriter();
				StreamResult result = new StreamResult(writer);
				TransformerFactory transformerFactory = getTransformerFactory();
				Transformer transformer = transformerFactory.newTransformer();
				transformer.transform(domSource, result);
				return writer.toString();
			}
			catch(TransformerException e)
			{
				logger.fatal(classname+"->" +method +"->" +"getStringFromDocument", e);
			}
		}
		return null;
	}
	public static TransformerFactory getTransformerFactory()
	{
		if(transformerFactory == null)
		{
			synchronized(XMLUtils.class)
			{
				if(transformerFactory == null)
					transformerFactory = TransformerFactory.newInstance();
			}
		}

		return transformerFactory;
	}
	public static InfoXMLBean popualteSNGUserInfo(InfoXMLBean infoBean, HashMap<String, String> extraInfoMap){
		if(infoBean != null){
			infoBean.setUserSqnId(getUserSqnId(extraInfoMap));
			infoBean.setUserMsg(getUserMsg(extraInfoMap));
		}
		return infoBean;
	}
	public static long getUserSqnId(HashMap<String, String> extraInfoMap){
		long userSqnId = -1;
		if (extraInfoMap != null && extraInfoMap.containsKey(SRBT_USER_SQN_ID)){
			String userSequenceId = extraInfoMap.get(SRBT_USER_SQN_ID);
			if(userSequenceId!=null && userSequenceId.length()>0){
				userSqnId = (new Long(userSequenceId.trim())).longValue();
			}
		}
		return userSqnId;
	}
	public static String getUserMsg(HashMap<String, String> extraInfoMap){
		String userMsgStr = "";
		if (extraInfoMap != null && extraInfoMap.containsKey(SRBT_USERMSG)){
			String userMsg = extraInfoMap.get(SRBT_USERMSG);
			if(userMsg!=null && userMsg.length()>0){
				userMsgStr = userMsg.trim();
			}
		}
		return userMsgStr;
	}
	public static String getCopySubId(SubscriberStatus subscriberStatus){
		String copySubId = null;
		if(subscriberStatus!=null && subscriberStatus.selectionInfo()!=null){
			String selInfo = subscriberStatus.selectionInfo();
			if(selInfo != null && selInfo.indexOf(SRBT_COPY_START_TOKEN)!=-1 && selInfo.indexOf(SRBT_COPY_END_TOKEN)!=-1){
				String temp = selInfo.substring(selInfo.indexOf(SRBT_COPY_START_TOKEN)+ (selInfo.indexOf(SRBT_COPY_START_TOKEN)+SRBT_COPY_START_TOKEN.length()), selInfo.indexOf(SRBT_COPY_END_TOKEN));
				if(temp!=null && temp.indexOf("-")!=-1){
					copySubId = temp.substring(temp.indexOf("-")+1);
				}
			}
		}
		return copySubId;
	} 
	public static boolean isCopySelection(SubscriberStatus subscriberStatus){
		if(subscriberStatus!=null && subscriberStatus.selectionInfo()!=null){
			String selInfo = subscriberStatus.selectionInfo();
			logger.debug("Subscriber Statis selection info : " + selInfo);
			if(selInfo != null && selInfo.indexOf(SRBT_COPY_START_TOKEN)!=-1){
				return true;
			}
		}
		return false;
	}
	public static String getGifteeId(SubscriberStatus subscriberStatus){
		String gifterId = null;
		if(subscriberStatus!=null && subscriberStatus.selectionInfo()!=null){
			String extraInfo = subscriberStatus.extraInfo();
			if(extraInfo != null){
				HashMap<String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				if(extraInfoMap!=null && extraInfoMap.size()>0 && extraInfoMap.containsKey(SRBT_GIFT_TOKEN)){
					gifterId = extraInfoMap.get(SRBT_GIFT_TOKEN);
				}
			}
		}
		return gifterId;
	}
	public static boolean isGiftSelection(SubscriberStatus subscriberStatus){
		if(subscriberStatus!=null && subscriberStatus.selectionInfo()!=null){
			logger.debug("Subscriber Statis selection info : " + subscriberStatus.selectionInfo());
			String extraInfo = subscriberStatus.extraInfo();
			logger.debug("Subscriber Statis extraInfo : " + extraInfo);
			if(extraInfo != null){
				HashMap<String,String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
				if(extraInfoMap!=null && extraInfoMap.size()>0 && extraInfoMap.containsKey(SRBT_GIFT_TOKEN)){
					return true;
				}
			}
		}
		return false;
	}
	public static String getChargeClassValue(ChargeClass chargeClass){
		if(chargeClass!=null){
			return chargeClass.getChargeClass();
		}
		return null;
	}
	public static String getEncodedUrlString(String param)
	{
		String ret = null;
		try
		{
			ret = m_urlEncoder.encode(param, "UTF-8");
		}
		catch(Throwable t)
		{
			ret = null;
		}
		return ret;
	}
	public static HttpResponseBean makeSocialRBTHttpCall(String baseActPublishingUrl,boolean useProxy,String proxyHost,int proxyPort,boolean toRetry,int timeOut){
		String method="makeSocialRBTHttpCall";
		logger.info(classname+"->"+method+"->entering");
		StringBuffer response=new StringBuffer();
		Integer statusCode=new Integer(-1);
		boolean responseReceived=Tools.callURL(baseActPublishingUrl, statusCode, response, useProxy, proxyHost, proxyPort, toRetry, timeOut);
		logger.info(classname+"->"+method+"->responseReceived=="+responseReceived);
		logger.info(classname+"->"+method+"->response=="+response.toString());
		return new HttpResponseBean(responseReceived,statusCode.intValue(),response.toString());
	}
	public static String getEndDateString(){
		Date currDate=Calendar.getInstance().getTime();
		currDate.setHours(0);
		currDate.setMinutes(0);
		currDate.setMonth(1);
		currDate.setSeconds(0);
		currDate.setYear(2037);
		String endDateStr=currDate.toLocaleString();
		return endDateStr;
	}

	public static  boolean getParamAsBoolean(String param, String defaultVal) {
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, defaultVal).equalsIgnoreCase("TRUE");
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param
					+ " returning defaultVal >" + defaultVal);
			return defaultVal.equalsIgnoreCase("TRUE");
		}
	}
	public static int getParamAsInt(String param,int defaultValue) {
		int returnVal=defaultValue;
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			String parameter= rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param,""+ defaultValue);
			if(parameter!=null){
				try {
					returnVal=Integer.parseInt(parameter);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return returnVal;
		}
		return returnVal;
	}
	public static String getParamAsString(String param,String defaultValue) {
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, defaultValue);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return null;
		}
	}
	public static String getParamAsString(String param) {
		if(rbtConnector==null){
			rbtConnector=RBTConnector.getInstance();
		}
		try {
			return rbtConnector.getRbtGenericCache().getParameter("SRBT",
					param, null);
		} catch (Exception e) {
			logger.info("Unable to get param ->" + param);
			return null;
		}
	}
	public static boolean getSelectionUrlMetaData(String url,RBTSocialUpdate rbtSocialUpdate,StringBuffer strBuff){
		if(url==null){
			return false;
		}
		if(strBuff==null){
			return false;
		}
		strBuff.append(url);
		if(rbtSocialUpdate!=null){
			Clip clip=MemCacheWrapper.getInstance().getClip(rbtSocialUpdate.getClipId());
			if (clip!=null) {
				String clipName = clip.getClipName();
				String artist=clip.getArtist();
				if(clipName!=null){
					strBuff.append("&clipName="+SRBTUtility.getEncodedUrlString(clipName));
				}
				if(artist!=null){
					strBuff.append("&artist="+SRBTUtility.getEncodedUrlString(artist));
				}
			}
			Category cat=MemCacheWrapper.getInstance().getCategory(rbtSocialUpdate.getCatId());
			if(cat!=null){
				String catName=cat.getCategoryName();
				if(catName!=null){
					strBuff.append("&catName="+SRBTUtility.getEncodedUrlString(catName));
				}
			}
			String callerId=rbtSocialUpdate.getCallerId();
			if(callerId!=null){
				if(callerId.indexOf("G")==-1 && callerId.indexOf("g")==-1){
					strBuff.append("&callerId="+callerId);
				}else{
					//populate group Id logic here
				}
			}
			if(clip==null || cat==null){
				return false;
			}
		}
		return true;
	}
	public static boolean getDownlodUrlMetaData(String url,RBTSocialUpdate rbtSocialUpdate,StringBuffer strBuff){
		if(url==null){
			return false;
		}
		if(strBuff==null){
			return false;
		}
		strBuff.append(url);
		if(rbtSocialUpdate!=null){
			Clip clip=MemCacheWrapper.getInstance().getClip(rbtSocialUpdate.getClipId());
			if (clip!=null) {
				String clipName = clip.getClipName();
				String artist=clip.getArtist();
				if(clipName!=null){
					strBuff.append("&clipName="+SRBTUtility.getEncodedUrlString(clipName));
				}
				if(artist!=null){
					strBuff.append("&artist="+SRBTUtility.getEncodedUrlString(artist));
				}
			}
			Category cat=MemCacheWrapper.getInstance().getCategory(rbtSocialUpdate.getCatId());
			if(cat!=null){
				String catName=cat.getCategoryName();
				if(catName!=null){
					strBuff.append("&catName="+SRBTUtility.getEncodedUrlString(catName));
				}
			}
			if(clip==null || cat==null){
				return false;
			}
		}
		return true;
	}
	public static int getSocialRBTMode(String mode){
		int modeVal=1;
		if(mode!=null){
			mode=mode.trim();
			StringTokenizer st=new StringTokenizer(mode,"_");
			int count=0;
			while(st.hasMoreElements()){
				String temp=st.nextToken();
				if(count==1){
					mode=temp.trim();
				}
				count++;
			}
			try {
				modeVal=Integer.parseInt(mode);
			} catch (Exception e) {
				modeVal=1;
			}
		}
		return modeVal;
	}

	private static InfoXMLBean updateInfoXMLBean(InfoXMLBean infoBean, Map<String,String> extraInfoMap){
		String allowPublish = (extraInfoMap != null ? extraInfoMap.get(ALLOW_PUBLISH) : null);
		if (allowPublish == null || "true".equalsIgnoreCase(allowPublish) || "yes".equalsIgnoreCase(allowPublish)) {
            infoBean.setAllowPublish(true);
        }
        String messageId = (extraInfoMap != null ? extraInfoMap.get(MESSAGE_ID) : null);
        if (messageId != null) {
            infoBean.setMessageId(Long.parseLong(messageId));
        }
        return infoBean;
	}
	
	public static void updateSocialSubscriberForSuccess(boolean m_socialRBTAllowed, boolean m_socialRBTAllUpdateInOneModel, HashMap<String, String> extraInfoMap, String strSubID, String rbtSystemType, 
			String classType, String activatedBy,short evtType){
		if(!m_socialRBTAllowed) {
			return;
		}
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		boolean publishUpdate = false;
		if(m_socialRBTAllUpdateInOneModel)
		{
			publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForSubscriptionActivationSuccess(strSubID,eventTypeForSocialUserBaseActivation,rbtSystemType);
		}
		else
		{
			InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_SUBSCRIBER_ACTION, SRBT_ACT_SUB_ACTION);
			SRBTUtility.popualteSNGUserInfo(infoBean,extraInfoMap);
			infoBean = updateInfoXMLBean(infoBean, extraInfoMap);
            String info = SRBTUtility.getInfoXML(infoBean);
            logger.info("Event type for subscriber_id:: "+strSubID+" is "+evtType);
			publishUpdate = SRBTDaoWrapper.getInstance().updateForSubscriptionActivationSuccess(strSubID, currDate, classType, info, activatedBy,evtType);
		}
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(strSubID, "", eventTypeForSocialUserBaseActivation, -1, -1, currDateStr, endDateStr, 1, rbtSystemType);
		}	
	}
	
	public static void updateSocialSubscriberForChangeMsisdnSuccess(boolean m_socialRBTAllowed, boolean m_socialRBTAllUpdateInOneModel, String subscriberId, String newSubscriberId, String activatedBy,short evtType){
		if(!m_socialRBTAllowed) {
			return;
		}		
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_SUBSCRIBER_ACTION, SRBT_ACT_SUB_ACTION);
		infoBean.setOldSubscriberId(subscriberId);
		boolean publishUpdate = false;
		String info = SRBTUtility.getInfoXML(infoBean);
        logger.info("Event type for old subscriber_id:: "+subscriberId+" new subscriber_id:: " + newSubscriberId+" is "+evtType);
        
        long msisdn = Long.parseLong(subscriberId);
        long newMsisdn = Long.parseLong(newSubscriberId);
        
        RbtSocialSubscriberDAO.updateSubscriberId(msisdn, newMsisdn);
        RbtSocialSelectionsDAO.updateSubscriberId(msisdn, newMsisdn);
        RbtSocialDownloadsDAO.updateSubscriberId(msisdn, newMsisdn);
        RbtSocialCopyDAO.updateSubscriberId(msisdn, newMsisdn);
        RbtSocialGiftDAO.updateSubscriberId(msisdn, newMsisdn);
        
        SRBTUtility.updateSocialSiteUser(subscriberId, newSubscriberId);
		publishUpdate = SRBTDaoWrapper.getInstance().updateForSubscriptionActivationSuccess(newSubscriberId, currDate, null, info, activatedBy,evtType);
		
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(subscriberId, "", evtType, -1, -1, currDateStr, endDateStr, 1, null);
		}	
	}
	
	public static void deactivateSocialSiteUser(String msisdn){
		boolean responseFlag = false;
		SRBTDaoWrapper.getInstance();
		responseFlag = SRBTDaoWrapper.deleteSocialSiteUser(msisdn);
		if(responseFlag){
			logger.info("Successfully deactivated, msisdn :: "+msisdn );
		}
	}
	
	public static void updateSocialSiteUser(String msisdn, String newMsisdn){
		boolean responseFlag = false;
		SRBTDaoWrapper.getInstance();
		responseFlag = SRBTDaoWrapper.updateSocialSiteUser(msisdn, newMsisdn);
		if(responseFlag){
			logger.info("Successfully updated, msisdn :: "+msisdn + " to new msisdn " + newMsisdn);
		}
	}
	
	public static void updateSocialGiftForSuccess(boolean m_socialRBTAllowed, String extraInfo, String giftedTo, String gifter, Date giftSentTime, String giftTime, String rbtSystemType, String srvKey, String mode, String toneType, int clipId){
		if(!m_socialRBTAllowed){
			return;
		}
		boolean publishUpdate = false;
		Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		logger.debug("ExtraInfoMap : " + extraInfoMap);
		InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_GIFT_ACTION, SRBT_ACT_SUB_ACTION);    						    						
		infoBean = updateInfoXMLBean(infoBean, extraInfoMap);
		String info = SRBTUtility.getInfoXML(infoBean);
		logger.debug("infoXML : " + info);
		publishUpdate = SRBTDaoWrapper.getInstance().updateForGiftSuccess(giftedTo, gifter,  clipId, -1, giftSentTime, giftSentTime,srvKey, info,false, mode, toneType);
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(giftedTo, gifter , eventTypeForSocialUserGift, clipId,-1, giftTime, giftTime, 1, rbtSystemType);
		}
	}
	
	public static void updateSocialDownloadForSuccess(boolean m_socialRBTAllowed, SubscriberDownloads download, String toneType, boolean m_socialRBTAllUpdateInOneModel, String strSubID, int clipId, String rbtSystemType,
			String callerID, ChargeClass chargeClass, String modeOfActivation, String response){
		if(!m_socialRBTAllowed){
			return;
		}
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(download.extraInfo());
		if(download.categoryID() == 104) {
			toneType = WebServiceConstants.CATEGORY_RECORD;
		}
		updateSocialDownload(m_socialRBTAllUpdateInOneModel, extraInfoMap, strSubID, callerID, clipId, download.categoryID(), rbtSystemType, SRBTUtility.getChargeClassValue(chargeClass), currDate, currDateStr, endDateStr, modeOfActivation, toneType);
//		if(response.indexOf("sel")!=-1)
//		{
//			updateSocialSelection(m_socialRBTAllUpdateInOneModel, strSubID, callerID, extraInfoMap, clipId, download.categoryID(), currDate, currDateStr, endDateStr, rbtSystemType, SRBTUtility.getChargeClassValue(chargeClass), modeOfActivation, toneType);
//		}
	}
	
	private static void updateSocialDownload(boolean m_socialRBTAllUpdateInOneModel, Map<String,String> extraInfoMap, String strSubID, String callerID, int clipId, int categoryID, String rbtSystemType, String chargeClass, Date currDate, String currDateStr, String endDateStr, String modeOfActivation, String toneType) {
		boolean publishUpdate = false;
		if(m_socialRBTAllUpdateInOneModel)
		{
			publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForDownloadActivationSuccess(strSubID,eventTypeForSocialUserDownloadActivation,clipId,categoryID,rbtSystemType);
		}else{
			InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_DOWNLOAD_ACTION, SRBT_ACT_SUB_ACTION);			        								        						
			infoBean = updateInfoXMLBean(infoBean, extraInfoMap);
            String info = SRBTUtility.getInfoXML(infoBean);
			publishUpdate = SRBTDaoWrapper.getInstance().updateForDownloadActivationSuccess(strSubID, callerID, clipId, categoryID, currDate, chargeClass, info, modeOfActivation, toneType);
		}
		if(publishUpdate)
		{
			SDRUtility.writeSocialRBTTrans(strSubID, callerID , eventTypeForSocialUserDownloadActivation, clipId, categoryID, currDateStr, endDateStr, 1, rbtSystemType);
		}
	}
	
	public static void updateSocialDownloadForDeactivation(boolean m_socialRBTAllowed, SubscriberDownloads download, String toneType, String strSubID, boolean m_socialRBTAllUpdateInOneModel, int clipId,
			String rbtSystemType, String callerID, ChargeClass chargeClass, String response) {
		if(!m_socialRBTAllowed){
			return;
		}
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		boolean publishUpdate = false;
		if(download.categoryID() == 104) {
			toneType = WebServiceConstants.CATEGORY_RECORD;
		}
		if(m_socialRBTAllUpdateInOneModel){
			publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForDownloadDeactivationSuccess(strSubID,eventTypeForSocialUserDownloadDeactivation,clipId,download.categoryID(),rbtSystemType);
		}else{
			InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_DOWNLOAD_ACTION, SRBT_DEACT_SUB_ACTION);
			String info = SRBTUtility.getInfoXML(infoBean);
			publishUpdate = SRBTDaoWrapper.getInstance().updateForDownloadDeactivationSuccess(strSubID, callerID, clipId, download.categoryID(), currDate,SRBTUtility.getChargeClassValue(chargeClass), info, toneType);
		}
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(strSubID, callerID , eventTypeForSocialUserDownloadDeactivation, clipId, download.categoryID(), currDateStr, endDateStr, 1, rbtSystemType);
		}
		if(response.indexOf("sel")!=-1){
			publishUpdate = false;
			if(m_socialRBTAllUpdateInOneModel){
				publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForSelectionDeactivationSuccess(strSubID,callerID,eventTypeForSocialUserSelDeactivation,clipId,download.categoryID(),rbtSystemType);
			}else{
				InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_SELECTION_ACTION, SRBT_DEACT_SUB_ACTION);
				String info = SRBTUtility.getInfoXML(infoBean);
				publishUpdate = SRBTDaoWrapper.getInstance().updateForSelectionDeactivationSuccess(strSubID, callerID, clipId, download.categoryID(), currDate,SRBTUtility.getChargeClassValue(chargeClass), info,false, toneType);
			}
			if(publishUpdate){
				SDRUtility.writeSocialRBTTrans(strSubID, callerID , eventTypeForSocialUserSelDeactivation, clipId, download.categoryID(), currDateStr, endDateStr, 1, rbtSystemType);
			}
		}
	}
	
	private static void updateSocialSelection(boolean m_socialRBTAllUpdateInOneModel, String strSubID, String callerID, 
			Map<String,String> extraInfoMap, int clipId, int categoryId, Date currDate, String currDateStr, String endDateStr, 
			String rbtSystemType, String chargeClass, String  modeOfActivation, String toneType) {
		boolean publishUpdate = false;
		if(m_socialRBTAllUpdateInOneModel){
			publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForSelectionActivationSuccess(strSubID,callerID,eventTypeForSocialUserSelActivation,clipId,categoryId,rbtSystemType);
		}else{
			InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_SELECTION_ACTION, SRBT_ACT_SUB_ACTION);
			String publishCallerName = (extraInfoMap != null ? extraInfoMap.get(PUBLISH_CALLER_NAME) : null);
            if ("true".equalsIgnoreCase(publishCallerName) || "yes".equalsIgnoreCase(publishCallerName)) {
                  infoBean.setPublishCallerNameAllowed(true);
            }
            infoBean = updateInfoXMLBean(infoBean, extraInfoMap);
            String info = SRBTUtility.getInfoXML(infoBean);
			publishUpdate = SRBTDaoWrapper.getInstance().updateForSelectionActivationSuccess(strSubID, callerID, clipId, categoryId, currDate, chargeClass, info,true, modeOfActivation, toneType);
		}
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(strSubID, callerID , eventTypeForSocialUserSelActivation, clipId, categoryId, currDateStr, endDateStr, 1, rbtSystemType);
		}
	}
	
	private static void updateSocialCopy(Map<String,String> extraInfoMap, String strSubID, String copySubId, int iClipId, int categoryID, Date currDate, String chargeClass, String modeOfActivation, String toneType, String currDateStr, String endDateStr, String rbtSystemType) {
		InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_COPY_ACTION, SRBT_ACT_SUB_ACTION);		    						
		boolean publishUpdate = false;
		if(copySubId == null)
		{
			copySubId = "";
		}
		infoBean = updateInfoXMLBean(infoBean, extraInfoMap);
        String info = SRBTUtility.getInfoXML(infoBean);
		String srcUserID = null;
		if (extraInfoMap != null && extraInfoMap.containsKey(SRBT_COPY_SOURCE_USER_ID))
			srcUserID = extraInfoMap.get(SRBT_COPY_SOURCE_USER_ID);
		publishUpdate = SRBTDaoWrapper.getInstance().updateForCopySelectionSuccess(strSubID, copySubId,  iClipId, categoryID, currDate, currDate, chargeClass, info,false, modeOfActivation, srcUserID, toneType);
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(strSubID, copySubId, eventTypeForSocialUserCopySelection, iClipId, categoryID, currDateStr, endDateStr, 1, rbtSystemType);
		}
	}
	
	private static void updateSocialGiftSelection(Map<String,String> extraInfoMap, String gifterId, String strSubID, int iClipId, int categoryID, Date currDate, String chargeClass, String modeOfActivation, String toneType, String currDateStr, String endDateStr, String rbtSystemType) {
		boolean publishUpdate = false;
		InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_GIFT_ACTION, SRBT_ACT_SUB_ACTION);
		infoBean = updateInfoXMLBean(infoBean, extraInfoMap);
		String info = SRBTUtility.getInfoXML(infoBean);
		logger.info("Gifter Id " + gifterId);
		if(gifterId == null){
			gifterId = "";
		}
		publishUpdate = SRBTDaoWrapper.getInstance().updateForGiftSelectionSuccess(strSubID, gifterId,  iClipId, categoryID, currDate, currDate, chargeClass, info,false, modeOfActivation, toneType);
		logger.info("Public update :  " + publishUpdate);
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(gifterId, strSubID, eventTypeForSocialUserGiftSelection, iClipId, categoryID, currDateStr, endDateStr, 1, rbtSystemType);
		}

	}

	public static void updateSocialSelectionForDeactivation(boolean m_socialRBTAllowed, boolean m_socialRBTAllUpdateInOneModel, String strSubID, String callerID, int iClipId, int categoryID, String rbtSystemType, ChargeClass chargeClass, String toneType){
		if(!m_socialRBTAllowed){
			return;
		}
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		boolean publishUpdate = false;
		if(m_socialRBTAllUpdateInOneModel){
			publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForSelectionDeactivationSuccess(strSubID,callerID,eventTypeForSocialUserSelDeactivation,iClipId,categoryID,rbtSystemType);
		}else {
			InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_SELECTION_ACTION, SRBT_DEACT_SUB_ACTION);
			String info = SRBTUtility.getInfoXML(infoBean);
			publishUpdate = SRBTDaoWrapper.getInstance().updateForSelectionDeactivationSuccess(strSubID, callerID, iClipId, categoryID, currDate, SRBTUtility.getChargeClassValue(chargeClass), info,false, toneType);
		}
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(strSubID, callerID , eventTypeForSocialUserSelDeactivation, iClipId, categoryID, currDateStr, endDateStr, 1, rbtSystemType);
		}
	}
	
	public static void updateSocialSelectionForSuccess(boolean m_socialRBTAllowed, boolean m_socialRBTAllUpdateInOneModel, String strSubID, String callerID, int iClipId, SubscriberStatus subscriberStatus, String rbtSystemType, 
			String extraInfo, ChargeClass chargeClass, String modeOfActivation, String toneType){
		if(!m_socialRBTAllowed){
			return;
		}
		logger.info("Allowed SRBT");
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		HashMap<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
		if(m_socialRBTAllUpdateInOneModel){
			RBTHibernateDBImplementationWrapper.getInstance().updateForSelectionActivationSuccess(strSubID,callerID,eventTypeForSocialUserSelActivation,iClipId,subscriberStatus.categoryID(),rbtSystemType);
			SDRUtility.writeSocialRBTTrans(strSubID, callerID, eventTypeForSocialUserSelActivation, iClipId, subscriberStatus.categoryID(), currDateStr, endDateStr, 1, rbtSystemType);
		}else {
			if(SRBTUtility.isCopySelection(subscriberStatus))
			{
				logger.info("Copy Selection callback");
				String copySubId = SRBTUtility.getCopySubId(subscriberStatus);
				updateSocialCopy(extraInfoMap, strSubID, copySubId, iClipId, subscriberStatus.categoryID(), currDate, SRBTUtility.getChargeClassValue(chargeClass), modeOfActivation, toneType, currDateStr, endDateStr, rbtSystemType);
			}
			else if(SRBTUtility.isGiftSelection(subscriberStatus))
			{
				logger.info("Gift Selection callback");
				String gifterId = SRBTUtility.getGifteeId(subscriberStatus);
				updateSocialGiftSelection(extraInfoMap, gifterId, strSubID, iClipId, subscriberStatus.categoryID(), currDate, SRBTUtility.getChargeClassValue(chargeClass), modeOfActivation, toneType, currDateStr, endDateStr, rbtSystemType);
			}
			else
			{
				logger.info("Selection callback");
				updateSocialSelection(m_socialRBTAllUpdateInOneModel, strSubID, callerID, extraInfoMap, iClipId, subscriberStatus.categoryID(), currDate, currDateStr, endDateStr, rbtSystemType, SRBTUtility.getChargeClassValue(chargeClass), modeOfActivation, toneType);
			}
		}
	}
	
	public static void updateSocialSubscriberForDeactivation(boolean m_socialRBTAllowed, boolean m_socialRBTAllUpdateInOneModel, String strSubID, String rbtSystemType, String classType) {
		if(!m_socialRBTAllowed){
			return;
		}
		Date currDate=Calendar.getInstance().getTime();
		String currDateStr=currDate.toLocaleString();
		String endDateStr=SRBTUtility.getEndDateString();
		boolean publishUpdate = false;
		if(m_socialRBTAllUpdateInOneModel){
			publishUpdate = true;
			RBTHibernateDBImplementationWrapper.getInstance().updateForSubscriptionDeactivationSuccess(strSubID,eventTypeForSocialUserBaseDeactivation,rbtSystemType);
		}else{
			InfoXMLBean infoBean = SRBTUtility.getInfoXMLBean(SRBT_SUBSCRIBER_ACTION, SRBT_DEACT_SUB_ACTION);
			String info = SRBTUtility.getInfoXML(infoBean);
			publishUpdate = SRBTDaoWrapper.getInstance().updateForSubscriptionDeactivationSuccess(strSubID, currDate, classType, info);
		}
		if(publishUpdate){
			SDRUtility.writeSocialRBTTrans(strSubID, "", eventTypeForSocialUserBaseDeactivation, -1, -1, currDateStr, endDateStr, 1, rbtSystemType);
		}
	}
}
