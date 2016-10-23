package com.onmobile.apps.ringbacktones.subscriptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ValidateNumberRequest;

public class RBTChannelHelper implements iRBTConstant {
	
	private static Logger logger = Logger.getLogger(RBTChannelHelper.class);
	String m_messagePathDefault = null;
	String m_messagePath = m_messagePathDefault;
	int m_activationPeriodDefault = 0;
	int m_activationPeriod = m_activationPeriodDefault;
	boolean m_allowReactivationDefault = false;
	boolean m_allowReactivation = m_allowReactivationDefault;
	boolean m_delSelectionsDefault = true;
	boolean m_delSelections = m_delSelectionsDefault;
	boolean m_loopingEnabled = false;
	String m_webSenderNoDefault = null;
	String m_webSenderNo = m_webSenderNoDefault;
	ArrayList m_subActiveDeactivatedBy = null;
	String m_webSMSTextDefault = "The password for accessing Web page is %L";
	String m_webSMSText = m_webSMSTextDefault;
	String m_countryPrefix = "91";
	private static RBTChannelHelper m_ChannelHelper = null;
	private static Object m_initMO = new Object();
	private Parameters m_giftCategory = null;


    private Category m_giftCat = null;

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	boolean m_useSubscriptionManager = false;

	public static RBTChannelHelper init() {
		if(m_ChannelHelper == null) {
			synchronized (m_initMO) {
				if(m_ChannelHelper == null) {
					try {
						m_ChannelHelper = new RBTChannelHelper();
					}
					catch (Exception e) {
						logger.error("", e);
						m_ChannelHelper = null;
					}
				}
			}
		}
		return m_ChannelHelper;
	}

	public RBTChannelHelper() throws Exception {
		Tools.init("RBT_WAR", false);

		m_activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);

		m_allowReactivation = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ALLOW_REACTIVATIONS", "FALSE");

		m_messagePath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MESSAGE_PATH", null);

		m_useSubscriptionManager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");

		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON","WEB_WAP_LOOPING_ENABLED");
		if(param!=null && param.getValue() != null)
			m_loopingEnabled = param.getValue().equalsIgnoreCase("true");

		m_delSelections = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DEL_SELECTIONS", "TRUE");

		m_subActiveDeactivatedBy = new ArrayList();

		String deactBy = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SUB_ACTIVE_DEACTIVATED_BY", null);
		if(deactBy == null) {
			m_subActiveDeactivatedBy.add("AUX");
			m_subActiveDeactivatedBy.add("NEFX");
		}
		else {
			StringTokenizer stk = new StringTokenizer(deactBy, ",");
			while (stk.hasMoreTokens()) {
				m_subActiveDeactivatedBy.add(stk.nextToken());
			}
		}

		m_webSenderNo = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEB_SENDER_NO", null);

		m_webSMSText = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEB_SMS_TEXT", null);
		if(m_webSMSText == null) {
			m_webSMSText = m_webSMSTextDefault;
		}

		m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
	}

	private boolean isSubActive(Subscriber subscriber) {
		return (RBTDBManager.getInstance().isSubActive(subscriber));
	}

	public String getCircleID(String subscriberID) {
		return RBTDBManager.getInstance().getCircleId(subscriberID);
	}

	// this method is no use if only GUI selections are required.
	/*
	 * public StringBuffer getSubscriber(String strSubID) throws Exception {
	 * Subscriber subscriber = RBTDBManager.init(m_dbURL, m_usePool,
	 * m_countryPrefix) .getSubscriber(strSubID); StringBuffer strXML = new
	 * StringBuffer();
	 * 
	 * if (!isSubActive(subscriber)) { strXML.append("<rbt></rbt>"); } else {
	 * SubscriberStatus[] subscriberStatus = RBTDBManager .init(m_dbURL,
	 * m_usePool, m_countryPrefix) .getSubscriberRecords(strSubID, "VUI",
	 * m_useSubscriptionManager); if (subscriberStatus == null) {
	 * strXML.append("<rbt><selections count=\"0\"/></rbt>"); } else {
	 * strXML.append("<rbt><selections count=\"" + subscriberStatus.length +
	 * "\" >");
	 * 
	 * for (int i = 0; i < subscriberStatus.length; i++) { String clipName =
	 * null; String callerID = null;
	 * 
	 * Categories category = (Categories) RBTDBManager .init(m_dbURL, m_usePool,
	 * m_countryPrefix) .getCategory(subscriberStatus[i].categoryID()); if
	 * (category != null) { if (category.type() == SHUFFLE || category.type() ==
	 * RECORD) { clipName = category.name().trim(); } else { Clips clip =
	 * RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix) .getClipRBT(
	 * subscriberStatus[i] .subscriberFile()); if (clip != null) clipName =
	 * clip.name().trim(); }
	 * 
	 * if (subscriberStatus[i].callerID() == null) { callerID = "ALL"; } else {
	 * callerID = subscriberStatus[i].callerID().trim(); }
	 * 
	 * if (clipName != null) { strXML.append("<selection caller=\"" + callerID +
	 * "\" song=\"" + clipName + "\" fromTime=\"" +
	 * subscriberStatus[i].fromTime() + "\" toTime=\"" +
	 * subscriberStatus[i].toTime() + "\" />"); } }
	 *  } strXML.append("</selections></rbt>"); } } return strXML; }
	 */

	public StringBuffer getSubscriber(String strSubID) throws Exception {
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		StringBuffer strXML = new StringBuffer();

		if(!isSubActive(subscriber)) {
			strXML.append("<rbt></rbt>");
		}
		else {
			// change done by manoj.jaiswal
			strXML.append(getSelections(strSubID));
		}
		String ltpPoints = "0";
		if(subscriber != null)
			ltpPoints = RBTDBManager.getInstance().getLTPPoints(
					subscriber.activationInfo());
		strXML = addLTPAttribute(strXML, ltpPoints);
		return strXML;
	}

	/**
	 * @author manoj.jaiswal this method will give selections of a subscriber
	 *         for a GUI application.
	 */
	public StringBuffer getSubscriberSelections(String strSubID) throws Exception {
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		StringBuffer strXML = new StringBuffer();

		if(!isSubActive(subscriber)) {
			strXML.append("<rbt></rbt>");
		}
		else {
			// change done by manoj.jaiswal
			strXML.append(getSelections(strSubID));
		}
		return strXML;
	}

	public StringBuffer getSubscriberSelections(String strSubID,String mode,String source) throws Exception
	{
		StringBuffer strXML = new StringBuffer();
		String blackList="false";
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		Date lastBillDate2=null;
		if (RBTDBManager.getInstance()
				.isSubscriberDeactivated(subscriber)) {
			String strLastBillDate=null;
			if (subscriber!=null) {
				if (RBTDBManager.getInstance()
						.isTotalBlackListSub(subscriber.subID())) {
					blackList = "true";
				} else {
					blackList = "false";
				}
				lastBillDate2=subscriber.nextChargingDate();
				if (lastBillDate2!=null) {
					logger.info(" subscriber table entry is not null");
					
					strLastBillDate = "" + lastBillDate2.getDate() + "-";
				
					strLastBillDate = strLastBillDate
							+ getMonth(lastBillDate2.getMonth()) + "-";
					strLastBillDate = strLastBillDate
							+ (lastBillDate2.getYear() + 1900);
				}else{
					logger.info(" subscriber table entry is  null");
					
					strLastBillDate="NA";
				}			
			}	
			else{
				blackList = "false";
				strLastBillDate="NA";
			}
			strXML.append("sub_active=false;sub_id="+strSubID+"+;last_bill_date="+strLastBillDate+";lang=hindi;sub_type=default;blackList="+blackList+";sub_yes=X");

		} else {
			String strLastBillDate=null;
			lastBillDate2=subscriber.nextChargingDate();
			if (lastBillDate2!=null) {
				logger.info(" subscriber table entry is not null");
				
				strLastBillDate = "" + lastBillDate2.getDate() + "-";
				
				strLastBillDate = strLastBillDate
						+ getMonth(lastBillDate2.getMonth()) + "-";
				strLastBillDate = strLastBillDate
						+ (lastBillDate2.getYear() + 1900);
			}else{
				logger.info(" subscriber table entry is  null");
				
				strLastBillDate="NA";
			}			
			if(RBTDBManager.getInstance()
					.isTotalBlackListSub(subscriber.subID())){
				blackList="true";
			}
			else{
				blackList="false";
			}
			strXML.append("sub_active=true;sub_id="+subscriber.subID()+";last_bill_date="+strLastBillDate+";lang="+subscriber.language()+";sub_type="+subscriber.subscriptionClass()+";blackList="+blackList+";sub_yes="+subscriber.subYes());

			if (source.equalsIgnoreCase("copy")) {
				// change done by manoj.jaiswal
				strXML.append(getSelections(strSubID, mode,"copy").toString());
				strXML.append(getGiftInbox(strSubID,"copy").toString());
			}	
		}
		return strXML;

	}
	public StringBuffer getSubscriberSelections(String strSubID,String mode) throws Exception
	{
		String blackList="false";
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		StringBuffer strXML = new StringBuffer();
		Date lastBillDate2=null;
		if(RBTDBManager.getInstance().isSubscriberDeactivated(subscriber))
		{
			String strLastBillDate=null;
			if (subscriber!=null) {
				if (RBTDBManager.getInstance().isTotalBlackListSub(subscriber.subID())) {
					blackList = "true";
				} else {
					blackList = "false";
				}
				lastBillDate2=subscriber.nextChargingDate();
				if (lastBillDate2!=null) {
					logger.info(" subscriber table entry is not null");

					strLastBillDate = "" + lastBillDate2.getDate() + "-";

					strLastBillDate = strLastBillDate
					+ getMonth(lastBillDate2.getMonth()) + "-";
					strLastBillDate = strLastBillDate
					+ (lastBillDate2.getYear() + 1900);
				}else{
					logger.info(" subscriber table entry is  null");

					strLastBillDate="NA";
				}			
			}	
			else{
				blackList = "false";
				strLastBillDate="NA";
			}




			strXML.append("sub_active=false;sub_id="+strSubID+";last_bill_date="+strLastBillDate+"timepass;lang=hindi;sub_type=default;blackList="+blackList+";sub_yes=X;subType=0");
		}
		else
		{
			String strLastBillDate=null;
			lastBillDate2=subscriber.nextChargingDate();
			if (lastBillDate2!=null) {
				logger.info(" subscriber table entry is not null");

				strLastBillDate = "" + lastBillDate2.getDate() + "-";

				strLastBillDate = strLastBillDate
				+ getMonth(lastBillDate2.getMonth()) + "-";
				strLastBillDate = strLastBillDate
				+ (lastBillDate2.getYear() + 1900);
			}else{
				logger.info(" subscriber table entry is  null");

				strLastBillDate="NA";
			}			
			if(RBTDBManager.getInstance()
					.isTotalBlackListSub(subscriber.subID())){
				blackList="true";
			}
			else{
				blackList="false";
			}
			String lang=subscriber.language();
			if(lang==null || lang.equalsIgnoreCase("null")){
				lang="not selected";
			}
			int rbtType=subscriber.rbtType();
			strXML.append("sub_active=true;sub_id="+subscriber.subID()+";last_bill_date="+strLastBillDate+";lang="+lang+";sub_type="+subscriber.subscriptionClass()+";blackList="+blackList+";sub_yes="+subscriber.subYes()+";rbtType="+rbtType);
		}
		strXML.append(getSelections(strSubID, mode).toString());
		strXML.append(getGiftInbox(strSubID).toString());
		return strXML;
	}
	//	public StringBuffer getSubscriberSelections(String strSubID, String mode) throws Exception {
	//		String blackList = "false";
	//		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
	//		StringBuffer strXML = new StringBuffer();
	//		Date lastBillDate2 = null;
	//		if(RBTDBManager.getInstance().isSubscriberDeactivated(subscriber)) {
	//			String strLastBillDate = null;
	//			if(subscriber != null) {
	//				if(RBTDBManager.getInstance().isTotalBlackListSub(subscriber.subID())) {
	//					blackList = "true";
	//				}
	//				else {
	//					blackList = "false";
	//				}
	//				lastBillDate2 = subscriber.nextChargingDate();
	//				if(lastBillDate2 != null) {
	//					Tools.logDetail("RBTChannelHelper", "getSubscriberSelections",
	//							" subscriber table entry is not null");
	//
	//					strLastBillDate = "" + lastBillDate2.getDate() + "-";
	//					
	//
	//					strLastBillDate = strLastBillDate + getMonth(lastBillDate2.getMonth()) + "-";
	//					
	//					strLastBillDate = strLastBillDate + (lastBillDate2.getYear() + 1900);
	//					
	//				}
	//				else {
	//					Tools.logDetail("RBTChannelHelper", "getSubscriberSelections",
	//							" subscriber table entry is  null");
	//
	//					strLastBillDate = "NA";
	//				}
	//			}
	//			else {
	//				blackList = "false";
	//				strLastBillDate = "NA";
	//			}
	//
	//			strXML.append("sub_active=false;sub_id=" + strSubID + ";last_bill_date="
	//					+ strLastBillDate + "timepass;lang=hindi;sub_type=default;blackList="
	//					+ blackList + ";sub_yes=X");
	//		}
	//		else {
	//			String strLastBillDate = null;
	//			lastBillDate2 = subscriber.nextChargingDate();
	//			if(lastBillDate2 != null) {
	//				Tools.logDetail("RBTChannelHelper", "getSubscriberSelections",
	//						" subscriber table entry is not null");
	//
	//				strLastBillDate = "" + lastBillDate2.getDate() + "-";
	//				
	//
	//				strLastBillDate = strLastBillDate + getMonth(lastBillDate2.getMonth()) + "-";
	//				
	//				strLastBillDate = strLastBillDate + (lastBillDate2.getYear() + 1900);
	//				
	//			}
	//			else {
	//				Tools.logDetail("RBTChannelHelper", "getSubscriberSelections",
	//						" subscriber table entry is  null");
	//
	//				strLastBillDate = "NA";
	//			}
	//			if(RBTDBManager.getInstance().isTotalBlackListSub(subscriber.subID())) {
	//				blackList = "true";
	//			}
	//			else {
	//				blackList = "false";
	//			}
	//			String lang = subscriber.language();
	//			if(lang == null || lang.equalsIgnoreCase("null")) {
	//				lang = "not selected";
	//			}
	//			strXML.append("sub_active=true;sub_id=" + subscriber.subID() + ";last_bill_date="
	//					+ strLastBillDate + ";lang=" + lang + ";sub_type="
	//					+ subscriber.subscriptionClass() + ";blackList=" + blackList + ";sub_yes="
	//					+ subscriber.subYes());
	//
	//		}
	//		strXML.append(getSelections(strSubID, mode).toString());
	//		strXML.append(getGiftInbox(strSubID).toString());
	//		return strXML;
	//	}

	public String getMonth(int month) {
		if(month == 0) {
			return "JAN";
		}
		else if(month == 1) {
			return "FEB";
		}
		else if(month == 2) {
			return "MAR";
		}
		else if(month == 3) {
			return "APR";
		}
		else if(month == 4) {
			return "MAY";
		}
		else if(month == 5) {
			return "JUN";
		}
		else if(month == 6) {
			return "JUL";
		}
		else if(month == 7) {
			return "AUG";
		}
		else if(month == 8) {
			return "SEP";
		}
		else if(month == 9) {
			return "OCT";
		}
		else if(month == 10) {
			return "NOV";
		}
		else {
			return "DEC";
		}
	}

	public StringBuffer getSubscriberSubYes(String strSubID) throws Exception {
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		StringBuffer strXML = new StringBuffer();

		if(subscriber != null && subscriber.subYes() != null) {
			strXML.append(subscriber.subYes());
		}
		return strXML;
	}

	private int getGiftCount(ViralSMSTable[] gifts) {
		int giftCount = 0;
		if(gifts != null) {
			ClipMinimal giftClip = null;
			int giftIndex = 0;
			for(giftIndex = 0; giftIndex < gifts.length; giftIndex++) {
				giftClip = RBTDBManager.getInstance().getClipById(
						new Integer(gifts[giftIndex].clipID()).intValue());
				if(giftClip != null) {
					giftCount++;
				}
			}
		}
		return giftCount;
	}

	public StringBuffer getGiftInbox(String subscriberID) {
		StringBuffer strXML = new StringBuffer();
		int giftIndex = 0;
		ClipMinimal giftClip = null;
		int giftCategoryID = -1;
		ViralSMSTable[] gifts = RBTDBManager.getInstance().getViralSMSesByType(
				subscriberID, "GIFTED");
		if(m_giftCategory == null)
			m_giftCategory = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,
			"GIFT_CATEGORY");
		if(m_giftCat == null)
			m_giftCat = (Category) RBTDBManager.getInstance().getCategory(Integer.parseInt(m_giftCategory.getValue()));
		int giftCount = 0;
		giftCount = getGiftCount(gifts);
		if(gifts != null) {
			strXML.append(";selectionsCount=" + giftCount);
			for(giftIndex = 0; giftIndex < gifts.length; giftIndex++) {
				String gifterID = gifts[giftIndex].subID();
				giftClip = RBTDBManager.getInstance().getClipById(
						new Integer(gifts[giftIndex].clipID()).intValue());
				if(giftClip != null) {
					String clipName = giftClip.getClipName().trim();
					if(clipName == null || clipName.equalsIgnoreCase("null")) {
						clipName = "null";
					}
					else {

						if(clipName.indexOf(",") > 0) {
							StringTokenizer st3 = new StringTokenizer(clipName, ",");
							String temp = null;
							String temp1 = null;
							int count = 0;
							while (st3.hasMoreElements()) {
								if(count == 0) {
									temp = st3.nextToken();
									temp1 = temp;

								}
								else {
									temp = "/" + st3.nextToken();
									temp1 = temp1 + temp;
								}
								count++;
							}
							clipName = temp1;
						}
						if(clipName.indexOf(";") > 0) {
							StringTokenizer st3 = new StringTokenizer(clipName, ";");
							String temp = null;
							String temp1 = null;
							int count = 0;
							while (st3.hasMoreElements()) {
								if(count == 0) {
									temp = st3.nextToken();
									temp1 = temp;
								}
								else {
									temp = "\\" + st3.nextToken();
									temp1 = temp1 + temp;
								}
								count++;
							}
							clipName = temp1;
						}

					}
					String sentTime = gifts[giftIndex].sentTime().toString();
					if(sentTime == null)
						sentTime = "null";
					String artist = giftClip.getArtist();
					if(artist == null || artist.equalsIgnoreCase("null")) {
						artist = "null";
					}
					else {

						if(artist.indexOf(",") > 0) {
							StringTokenizer st3 = new StringTokenizer(artist, ",");
							String temp = null;
							String temp1 = null;
							int count = 0;
							while (st3.hasMoreElements()) {
								if(count == 0) {
									temp = st3.nextToken();
									temp1 = temp;

								}
								else {
									temp = "/" + st3.nextToken();
									temp1 = temp1 + temp;
								}
								count++;
							}
							artist = temp1;
						}
						if(artist.indexOf(";") > 0) {
							StringTokenizer st3 = new StringTokenizer(artist, ";");
							String temp = null;
							String temp1 = null;
							int count = 0;
							while (st3.hasMoreElements()) {
								if(count == 0) {
									temp = st3.nextToken();
									temp1 = temp;
								}
								else {
									temp = "\\" + st3.nextToken();
									temp1 = temp1 + temp;
								}
								count++;
							}
							artist = temp1;
						}

					}

					String wavfile = giftClip.getWavFile();
					if(wavfile == null)
						wavfile = "null";
					strXML
					.append(";Type=gift,gifter=" + gifterID + ",song=" + clipName
							+ ",clipId=" + getEmptyStringIfNegative(giftClip.getClipId())
							+ ",categoryId=" + m_giftCat.getID() + ",artist="
							+ getEmptyStringIfNull(artist) + ",wavfile="
							+ getEmptyStringIfNull(wavfile) + ",senttime=" + sentTime);


				}
			}
		}
		else {

			strXML.append(";giftCount=0");

		}

		return strXML;
	}

	/*
	 * 
	 * added by sandeep
	 * 
	 */
	public StringBuffer getGiftOutbox(String subscriberId){
		String strXML="";
		int giftIndex = 0;
		ClipMinimal giftClip = null;
		String[] smsTypes = {"GIFTED","ACCEPT_ACK","REJECT_ACK"};
		ViralSMSTable[] gifts = RBTDBManager.getInstance()
		.getViralSMSByTypesForSubscriber(subscriberId, smsTypes);
		if(m_giftCategory == null)
			m_giftCategory = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "GIFT_CATEGORY");
		
		if(m_giftCat == null)
			m_giftCat = (Category) RBTDBManager.getInstance().getCategory(Integer.parseInt(m_giftCategory.getValue()));
		if(gifts != null) {
			// strXML.append(";selectionsCount=" + gifts.length);

			for(giftIndex=0;giftIndex<gifts.length;giftIndex++){
				String gifterID = gifts[giftIndex].callerID();
				String status = "";
				if(gifts[giftIndex].type().equalsIgnoreCase("ACCEPT_ACK")){
					status="Gift Accepted";
				}else if(gifts[giftIndex].type().equalsIgnoreCase("GIFTED")){
					status="GIFT PENDING";
				}else if(gifts[giftIndex].type().equalsIgnoreCase("REJECT_ACK")){
					status="GIFT REJECTED";
				}
				giftClip = RBTMOHelper.init().getClip(new Integer(gifts[giftIndex].clipID()).intValue());
				String clipName = giftClip.getClipName().trim();
				if(clipName==null)
					clipName="--";
				SimpleDateFormat sm = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");
				String sentTime =sm.format(gifts[giftIndex].sentTime());
				if(sentTime==null)
					sentTime="--";
				String artist=giftClip.getArtist();
				if(artist==null)
					artist="--";
				String wavfile= giftClip.getWavFile().substring(4, giftClip.getWavFile().length()-4);
				if(wavfile==null)
					wavfile="--";
				String selBy=gifts[giftIndex].selectedBy();
				String chargedAmount="-";
				if(selBy!=null&&selBy.indexOf(":")>0){
					chargedAmount =selBy.split(":")[1];
				}else{
					Map<String, String> giftInfoMap = DBUtility.getAttributeMapFromXML(gifts[giftIndex].extraInfo());
					if(giftInfoMap != null) {
						String amountCharged = giftInfoMap.get("aountCharged");
						if(amountCharged != null) {
							chargedAmount = amountCharged;
						}
					}
				}
				strXML=strXML+gifterID
				+ "::" +clipName
				+ "::" + getEmptyStringIfNegative(giftClip.getClipId())
				+ "::" + m_giftCat.getID()
				+ "::" + getEmptyStringIfNull(wavfile)
				+ "::" + sentTime
				+ "::" + chargedAmount
				+ "::" + status+";";
			}
		}
		else{

			strXML="--";

		}	
		return( new StringBuffer(strXML));
	}
	
	public StringBuffer getGiftInbox(String subscriberID,String source){

		StringBuffer strXML = new StringBuffer();
		strXML.append(";giftCount=0");


		return strXML;
	}

	private int getSelectionCount(SubscriberStatus[] subscriberStatus) {
		int count = 0;
		if(subscriberStatus != null) {

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String circleID = rbtDBManager.getCircleId(subscriberStatus[0].subID());
			char isPrepaid = subscriberStatus[0].prepaidYes() ? 'y' : 'n';
			for(int i = 0; i < subscriberStatus.length; i++) {

				Categories category = rbtDBManager.getCategory(subscriberStatus[i].categoryID(), circleID, isPrepaid);
				int categoryType = category.type();
				int categoryId = category.id();

				if(categoryType == 0 || categoryType == 4) {

					if(categoryType == 0) {

						Clips[] clips = null;
						//clips = RBTDBManager.getInstance().getAllClips(categoryId);
						clips = RBTDBManager.getInstance().getAllClipsCCC(categoryId);
						if(clips != null) {

							count++;

						}
					}
					else {

						count++;

					}

				}
				else {
					System.out.println("**************normal selection ");
					ClipMinimal clip = RBTDBManager.getInstance().getClipRBT(
							subscriberStatus[i].subscriberFile());

					if(clip != null) {

						count++;

					}
				}
			}
		}

		return count;
	}

	private int getShuffleCount(int categoryId) {

		Clips[] clips = null;
		Clips clip = null;
		int count = 0;
		clips = RBTDBManager.getInstance().getAllClipsCCC(categoryId);
		if(clips != null) {

			for(int p = 0; p < clips.length; p++) {

				clip = clips[p];
				if(clip != null) {

					count++;

				}
			}

			return count;
		}

		return count;
	}

	private StringBuffer getSelections(String strSubID, String statusType) throws Exception {
		StringBuffer strXML = new StringBuffer();
		// SubscriberStatus[] subscriberStatus = RBTDBManager.init(m_dbURL,
		// m_nConn)
		// .getSubscriberRecords(strSubID,statusType, m_useSubscriptionManager);

		SubscriberStatus[] subscriberStatus = RBTDBManager.getInstance()
		.getAllActiveSubSelectionRecords(strSubID);
		subscriberStatus = sortSubscriberStatusBySetTime(subscriberStatus);
		HashMap hashMap = new HashMap();
		for(int i = 0; subscriberStatus != null && i < subscriberStatus.length; i++) {
			if(subscriberStatus[i].status() == 1) {
				String callerID = subscriberStatus[i].callerID();
				if(callerID == null)
					callerID = "DEFAULT";
				if(hashMap.containsKey(callerID))
					continue;
				if(subscriberStatus[i].callerID() == null) {
					hashMap.put("DEFAULT", subscriberStatus[i]);
				}
				else {
					hashMap.put(subscriberStatus[i].callerID(), subscriberStatus[i]);
				}
			}
		}
		if(hashMap.size() > 0) {
			Collection collection = hashMap.values();
			subscriberStatus = (SubscriberStatus[])collection.toArray(new SubscriberStatus[0]);

		}
		else {
			subscriberStatus = null;
		}

		if(subscriberStatus == null) {
			strXML.append(";selectionsCount=0");
		}
		else {
			int selCount = subscriberStatus.length;
			int selCountTemp = getSelectionCount(subscriberStatus);
			if(selCountTemp <= selCount) {
				selCount = selCountTemp;
			}
			strXML.append(";selections count=" + selCount);

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String circleID = rbtDBManager.getCircleId(subscriberStatus[0].subID());
			char isPrepaid = subscriberStatus[0].prepaidYes() ? 'y' : 'n';
			for(int i = 0; i < subscriberStatus.length; i++) {
				String artist = null;
				String clipName = null;
				String callerID = null;
				int clipId = -1;
				String albumName = null;
				String wavfile = null;
				String setTime = null;
				Clips[] clips = null;
				int categoryType = -1;
				int categoryId = -1;
				Categories category = rbtDBManager.getCategory(subscriberStatus[i].categoryID(), circleID, isPrepaid);

				categoryId = category.id();
				categoryType = category.type();
				if(categoryType == 0 || categoryType == 4) {
					clipName = category.name().trim();
					if(categoryType == 0) {
						Clips clip = null;
						clips = RBTDBManager.getInstance().getAllClipsCCC(categoryId);
						if(clips != null) {
							if(subscriberStatus[i].callerID() == null) {
								callerID = "DEFAULT";
							}
							else {
								callerID = subscriberStatus[i].callerID().trim();
							}
							int shfCount = clips.length;
							System.out.println("shfCount==" + shfCount);
							int shfCountTemp = getShuffleCount(categoryId);
							System.out.println("shfCountTemp==" + shfCountTemp);
							if(shfCountTemp <= shfCount) {
								shfCount = shfCountTemp;
							}
							System.out.println(";Type=selShuffle,caller=" + callerID
									+ ",shuffleCount=" + shfCount);
							strXML.append(";Type=selShuffle,caller=" + callerID + ",shuffleCount="
									+ shfCount);
							for(int p = 0; p < clips.length; p++) {
								clip = clips[p];
								if(clip != null) {
									clipId = clip.id();

									clipName = clip.name();
									if(clipName == null || clipName.equalsIgnoreCase("null")) {
										clipName = "null";
									}
									else {

										if(clipName.indexOf(",") > 0) {
											StringTokenizer st3 = new StringTokenizer(clipName, ",");
											String temp = null;
											String temp1 = null;
											int count = 0;
											while (st3.hasMoreElements()) {
												if(count == 0) {
													temp = st3.nextToken();
													temp1 = temp;

												}
												else {
													temp = "/" + st3.nextToken();
													temp1 = temp1 + temp;
												}
												count++;
											}
											if(temp1 != null) {
												clipName = temp1;
											}
										}
										if(clipName.indexOf(";") > 0) {
											StringTokenizer st3 = new StringTokenizer(clipName, ";");
											String temp = null;
											String temp1 = null;
											int count = 0;
											while (st3.hasMoreElements()) {
												if(count == 0) {
													temp = st3.nextToken();
													temp1 = temp;
												}
												else {
													temp = "\\" + st3.nextToken();
													temp1 = temp1 + temp;
												}
												count++;
											}
											if(temp1 != null) {
												clipName = temp1;
											}
										}

									}
									albumName = clip.album();
									if(albumName == null || albumName.equalsIgnoreCase("null")) {
										albumName = "null";
									}
									else {

										if(albumName.indexOf(",") > 0) {
											StringTokenizer st3 = new StringTokenizer(albumName,
											",");
											String temp = null;
											String temp1 = null;
											int count = 0;
											while (st3.hasMoreElements()) {
												if(count == 0) {
													temp = st3.nextToken();
													temp1 = temp;

												}
												else {
													temp = "/" + st3.nextToken();
													temp1 = temp1 + temp;
												}
												count++;
											}
											if(temp1 != null) {
												albumName = temp1;
											}
										}
										if(albumName.indexOf(";") > 0) {
											StringTokenizer st3 = new StringTokenizer(albumName,
											";");
											String temp = null;
											String temp1 = null;
											int count = 0;
											while (st3.hasMoreElements()) {
												if(count == 0) {
													temp = st3.nextToken();
													temp1 = temp;
												}
												else {
													temp = "\\" + st3.nextToken();
													temp1 = temp1 + temp;
												}
												count++;
											}
											if(temp1 != null) {
												albumName = temp1;
											}
										}

									}
									artist = clip.artist();
									if(artist == null || artist.equalsIgnoreCase("null")) {
										artist = "null";
									}
									else {
										if(artist.indexOf(",") > 0) {
											StringTokenizer st3 = new StringTokenizer(artist, ",");
											String temp = null;
											String temp1 = null;
											int count = 0;
											while (st3.hasMoreElements()) {
												if(count == 0) {
													temp = st3.nextToken();
													temp1 = temp;

												}
												else {
													temp = "/" + st3.nextToken();
													temp1 = temp1 + temp;
												}
												count++;
											}
											if(temp1 != null) {
												artist = temp1;
											}
										}
										if(artist.indexOf(";") > 0) {
											StringTokenizer st3 = new StringTokenizer(artist, ";");
											String temp = null;
											String temp1 = null;
											int count = 0;
											while (st3.hasMoreElements()) {
												if(count == 0) {
													temp = st3.nextToken();
													temp1 = temp;
												}
												else {
													temp = "\\" + st3.nextToken();
													temp1 = temp1 + temp;
												}
												count++;
											}
											if(temp1 != null) {
												artist = temp1;
											}
										}
									}
									wavfile = clip.previewFile();
									if(wavfile == null)
										wavfile = "null";
									Date setTimeDate = subscriberStatus[i].setTime();
									if(setTimeDate == null) {
										setTime = "null";
									}
									else {
										setTime = setTimeDate.toString();
									}
								}

								if(clipName != null) {
									strXML.append(",song="
											+ clipName.trim()
											+ ",clipId="
											+ getEmptyStringIfNegative(clipId)
											+ ",categoryId="
											+ subscriberStatus[i].categoryID()
											+ ",artist="
											+ getEmptyStringIfNull(artist)
											+ ",wavfile="
											+ getEmptyStringIfNull(wavfile)
											+ ",setTime="
											+ getEmptyStringIfNull(subscriberStatus[i].setTime()
													.toString()));
								}
							}
						}
					}
					else {
						if(subscriberStatus[i].callerID() == null) {
							callerID = "DEFAULT";
						}
						else {
							callerID = subscriberStatus[i].callerID().trim();
						}
						if(clipName != null) {

							strXML.append(";Type=sel,caller=" + callerID + ",song="
									+ clipName.trim() + ",clipId="
									+ getEmptyStringIfNegative(clipId) + ",categoryId="
									+ subscriberStatus[i].categoryID() + ",artist="
									+ getEmptyStringIfNull(artist) + ",wavfile="
									+ getEmptyStringIfNull(wavfile) + ",setTime="
									+ getEmptyStringIfNull(setTime));
						}
					}
				}
				else {
					ClipMinimal clip = RBTDBManager.getInstance().getClipRBT(
							subscriberStatus[i].subscriberFile());
					if(clip != null) {
						clipId = clip.getClipId();

						clipName = clip.getClipName();
						if(clipName == null || clipName.equalsIgnoreCase("null")) {
							clipName = "null";
						}
						else {

							if(clipName.indexOf(",") > 0) {
								StringTokenizer st3 = new StringTokenizer(clipName, ",");
								String temp = null;
								String temp1 = null;
								int count = 0;
								while (st3.hasMoreElements()) {
									if(count == 0) {
										temp = st3.nextToken();
										temp1 = temp;

									}
									else {
										temp = "/" + st3.nextToken();
										temp1 = temp1 + temp;
									}
									count++;
								}
								if(temp1 != null) {
									clipName = temp1;
								}
							}
							if(clipName.indexOf(";") > 0) {
								StringTokenizer st3 = new StringTokenizer(clipName, ";");
								String temp = null;
								String temp1 = null;
								int count = 0;
								while (st3.hasMoreElements()) {
									if(count == 0) {
										temp = st3.nextToken();
										temp1 = temp;
									}
									else {
										temp = "\\" + st3.nextToken();
										temp1 = temp1 + temp;
									}
									count++;
								}
								if(temp1 != null) {
									clipName = temp1;
								}
							}

						}
						albumName = clip.getAlbum();
						if(albumName == null || albumName.equalsIgnoreCase("null")) {
							albumName = "null";
						}
						else {
							if(albumName.indexOf(",") > 0) {
								StringTokenizer st3 = new StringTokenizer(albumName, ",");
								String temp = null;
								String temp1 = null;
								int count = 0;
								while (st3.hasMoreElements()) {
									if(count == 0) {
										temp = st3.nextToken();
										temp1 = temp;

									}
									else {
										temp = "/" + st3.nextToken();
										temp1 = temp1 + temp1;
									}
									count++;
								}
								if(temp1 != null) {
									albumName = temp1;
								}
							}
							if(albumName.indexOf(";") > 0) {
								StringTokenizer st3 = new StringTokenizer(albumName, ";");
								String temp = null;
								String temp1 = null;
								int count = 0;
								while (st3.hasMoreElements()) {
									if(count == 0) {
										temp = st3.nextToken();
										temp1 = temp;
									}
									else {
										temp = "\\" + st3.nextToken();
										temp1 = temp1 + temp;
									}
									count++;
								}
								if(temp1 != null) {
									albumName = temp1;
								}
							}

						}
						artist = clip.getArtist();
						if(artist == null || artist.equalsIgnoreCase("null")) {
							artist = "null";
						}
						else {
							if(artist.indexOf(",") > 0) {
								StringTokenizer st3 = new StringTokenizer(artist, ",");
								String temp = null;
								String temp1 = null;
								int count = 0;
								while (st3.hasMoreElements()) {
									if(count == 0) {
										temp = st3.nextToken();
										temp1 = temp;

									}
									else {
										temp = "/" + st3.nextToken();
										temp1 = temp1 + temp;
									}
									count++;
								}
								if(temp1 != null) {
									artist = temp1;
								}
							}
							if(artist.indexOf(";") > 0) {
								StringTokenizer st3 = new StringTokenizer(artist, ";");
								String temp = null;
								String temp1 = null;
								int count = 0;
								while (st3.hasMoreElements()) {
									if(count == 0) {
										temp = st3.nextToken();
										temp1 = temp;
									}
									else {
										temp = "\\" + st3.nextToken();
										temp1 = temp1 + temp;
									}
									count++;
								}
								if(temp1 != null) {
									artist = temp1;
								}
							}
						}
						wavfile = clip.getWavFile();
						if(wavfile == null)
							wavfile = "null";
						Date setTimeDate = subscriberStatus[i].setTime();
						if(setTimeDate == null) {
							setTime = "null";
						}
						else {
							setTime = setTimeDate.toString();
						}
					}
					if(subscriberStatus[i].callerID() == null) {
						callerID = "DEFAULT";
					}
					else {
						callerID = subscriberStatus[i].callerID().trim();
					}
					if(clipName != null) {

						strXML.append(";Type=sel,caller=" + callerID + ",song=" + clipName.trim()
								+ ",clipId=" + getEmptyStringIfNegative(clipId) + ",categoryId="
								+ subscriberStatus[i].categoryID() + ",artist="
								+ getEmptyStringIfNull(artist) + ",wavfile="
								+ getEmptyStringIfNull(wavfile) + ",setTime="
								+ getEmptyStringIfNull(setTime));
					}
				}

			}
		}
		return strXML;
	}

	public static SubscriberStatus[] sortSubscriberStatusBySetTime(SubscriberStatus[] settings) {
		if(settings == null)
			return null;

		SubscriberStatus subscriberStatus = null;
		for(int i = 0; i < settings.length; i++) {
			for(int j = i; j < settings.length; j++) {
				if(settings[i].setTime().compareTo(settings[j].setTime()) < 0) {
					subscriberStatus = settings[i];
					settings[i] = settings[j];
					settings[j] = subscriberStatus;
				}
			}
		}

		return settings;
	}

	private StringBuffer getSelections(String strSubID, String statusType,String source) throws Exception {
		StringBuffer strXML = new StringBuffer();
//		SubscriberStatus[] subscriberStatus = RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix)
//		.getSubscriberRecords(strSubID,statusType, m_useSubscriptionManager);
//		if (subscriberStatus == null)
//		{
//		strXML.append(";selectionsCount=0");
//		}
//		else
//		{


		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		SubscriberStatus setting = null;
		if(!RBTDBManager.getInstance().isSubscriberDeactivated(subscriber))
		{


			SubscriberStatus[] subscriberStatus1 = RBTDBManager.getInstance().getAllActiveSubSelectionRecords(strSubID);
			subscriberStatus1 = sortSubscriberStatusBySetTime(subscriberStatus1);
			for(int i = 0; subscriberStatus1 != null && i < subscriberStatus1.length; i++) {
				if(subscriberStatus1[i].status() == 1 && subscriberStatus1[i].callerID() == null) {
					if(subscriberStatus1[i].categoryType() != 0)
						setting = subscriberStatus1[i];

					break;
				}
			}



		}
		if(setting!=null){
			strXML.append(";selections count=1");
			String artist=null;
			String clipName = null;
			String callerID = null;
			int clipId = -1;
			String wavFile = null;
			String albumName = null;
			String categoryName = null;
			String wavfile=null;
			String setTime=null;
			int categoryType = -1;
			int categoryId = -1;
			Categories category = (Categories)(RBTDBManager.getInstance().getCategory(setting.categoryID()));

			categoryId = setting.categoryID();

			wavFile = setting.subscriberFile();
			if (categoryType == 0 || categoryType == 4)
			{
				clipName = category.name().trim();
				if(clipName==null || clipName.equalsIgnoreCase("nul")){
					clipName="null";
				}else{
					if(clipName.indexOf(",")>0){
						StringTokenizer st3=new StringTokenizer(artist,",");
						String temp=null;String temp1=null;
						int count=0;
						while(st3.hasMoreElements()){
							if(count==0){
								temp=st3.nextToken();
								temp1=temp;

							}else{
								temp="/"+st3.nextToken();	
								temp1=temp1+temp;
							}
							count++;
						}
						if(temp1!=null){
							clipName=temp1;
						}
					}if(clipName.indexOf(";")>0){
						StringTokenizer st3=new StringTokenizer(artist,";");
						String temp=null;String temp1=null;
						int count=0;
						while(st3.hasMoreElements()){
							if(count==0){
								temp=st3.nextToken();
								temp1=temp;
							}else{
								temp="\\"+st3.nextToken();
								temp1=temp1+temp;
							}
							count++;
						}
						if(temp1!=null){
							clipName=temp1;
						}
					}

				}
			}
			else
			{
				ClipMinimal clip =  RBTDBManager.getInstance().getClipRBT(
								setting.subscriberFile());
				if (clip != null){
					clipId = clip.getClipId();

					clipName = clip.getClipName();
					if((clipName==null) || clipName.equalsIgnoreCase("null")){
						clipName="null";
					}else{


						if(clipName.indexOf(",")>0){
							StringTokenizer st3=new StringTokenizer(artist,",");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;

								}else{
									temp="/"+st3.nextToken();	
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								clipName=temp1;
							}
						}if(clipName.indexOf(";")>0){
							StringTokenizer st3=new StringTokenizer(artist,";");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;
								}else{
									temp="\\"+st3.nextToken();
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								clipName=temp1;
							}
						}

					}
					albumName = clip.getAlbum();
					if((albumName==null)||albumName.equalsIgnoreCase("null")){
						albumName="null";
					}else{
						if(albumName.indexOf(",")>0){
							StringTokenizer st3=new StringTokenizer(albumName,",");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;

								}else{
									temp="/"+st3.nextToken();	
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								albumName=temp1;
							}
						}if(albumName.indexOf(";")>0){
							StringTokenizer st3=new StringTokenizer(albumName,";");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;
								}else{
									temp="\\"+st3.nextToken();
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								albumName=temp1;
							}
						}
					}
					artist=clip.getArtist();
					if((artist==null)|| artist.equalsIgnoreCase("null")){
						artist="null";
					}else{

						if(artist.indexOf(",")>0){
							StringTokenizer st3=new StringTokenizer(artist,",");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;

								}else{
									temp="/"+st3.nextToken();	
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								artist=temp1;
							}
						}if(artist.indexOf(";")>0){
							StringTokenizer st3=new StringTokenizer(artist,";");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;
								}else{
									temp="\\"+st3.nextToken();
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								artist=temp1;
							}
						}
					}
					wavfile=clip.getWavFile();
					if((wavfile==null)|| wavfile.equalsIgnoreCase("null")){
						wavfile="null";
					}else{


						if(wavfile.indexOf(",")>0){
							StringTokenizer st3=new StringTokenizer(wavfile,",");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;

								}else{
									temp="/"+st3.nextToken();	
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								wavfile=temp1;
							}
						}if(wavfile.indexOf(";")>0){
							StringTokenizer st3=new StringTokenizer(wavfile,";");
							String temp=null;String temp1=null;
							int count=0;
							while(st3.hasMoreElements()){
								if(count==0){
									temp=st3.nextToken();
									temp1=temp;
								}else{
									temp="\\"+st3.nextToken();
									temp1=temp1+temp;
								}
								count++;
							}
							if(temp1!=null){
								wavfile=temp1;
							}
						}

					}
					Date setTimeDate=setting.setTime();
					if(setTimeDate==null){
						setTime="null";
					}
					else{
						setTime=setTimeDate.toString();
					}
				}
			}


			callerID = "DEFAULT";

			if (clipName != null)
			{

				strXML.append(";Type=sel,caller=" +callerID
						+ ",song=" + clipName
						+ ",clipId=" + getEmptyStringIfNegative(clipId)
						+ ",categoryId=" + categoryId
						+ ",artist=" + getEmptyStringIfNull(artist)
						+ ",wavfile=" + getEmptyStringIfNull(wavfile)
						+",setTime="+getEmptyStringIfNull(setting.setTime().toString()));

			}

		}
		else{
			strXML.append(";selectionsCount=0");
		}

		return strXML;
	}

	
	private StringBuffer getSelections(String strSubID) throws Exception {
		StringBuffer strXML = new StringBuffer();
		SubscriberStatus[] subscriberStatus = RBTDBManager.getInstance()
		.getSubscriberRecords(strSubID, "GUI", m_useSubscriptionManager);
		if(subscriberStatus == null) {
			strXML.append("<rbt><selections count=\"0\"/></rbt>");
		}
		else {
			strXML.append("<rbt><selections count=\"" + subscriberStatus.length + "\" >");

			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String circleID = rbtDBManager.getCircleId(subscriberStatus[0].subID());
			char isPrepaid = subscriberStatus[0].prepaidYes() ? 'y' : 'n';
			for(int i = 0; i < subscriberStatus.length; i++) {

				String setTime = null;
				String clipName = null;
				String callerID = null;
				int clipId = -1;
				String wavFile = null;
				String albumName = null;
				String categoryName = null;
				int categoryType = -1;
				int categoryId = -1;

				Date setDate = subscriberStatus[i].setTime();

				setTime = simpleDateFormat.format(setDate);
				Categories category = rbtDBManager.getCategory(subscriberStatus[i].categoryID(), circleID, isPrepaid);
				if(category != null) {
					categoryId = category.id();
					categoryType = category.type();
					categoryName = category.name();
					wavFile = subscriberStatus[i].subscriberFile();
					if(categoryType == 0 || categoryType == 4) {
						clipName = category.name().trim();
					}
					else {
						ClipMinimal clip = RBTDBManager.getInstance().getClipRBT(
								subscriberStatus[i].subscriberFile());
						if(clip != null) {
							clipId = clip.getClipId();
							clipName = clip.getClipName();
							albumName = clip.getAlbum();
						}
					}

					if(subscriberStatus[i].callerID() == null) {
						callerID = "ALL";
					}
					else {
						callerID = subscriberStatus[i].callerID().trim();
					}
					if(clipName != null) {
						strXML.append("<selection caller=\"" + callerID + "\" song=\""
								+ clipName.trim() + "\" clipId=\""
								+ getEmptyStringIfNegative(clipId) + "\" wavFile=\""
								+ getEmptyStringIfNull(wavFile) + "\" categoryId=\"" + categoryId
								+ "\" categoryType=\"" + categoryType + "\" categoryName=\""
								+ getEmptyStringIfNull(categoryName) + "\" album=\""
								+ getEmptyStringIfNull(albumName) + "\" fromTime=\""
								+ subscriberStatus[i].fromTime() + "\" toTime=\""
								+ subscriberStatus[i].toTime() + "\" setTime=\""
								+ setTime + "\" status=\""
								+ subscriberStatus[i].status() +  "\" />");
					}
				}
			}
			strXML.append("</selections></rbt>");
		}
		return strXML;
	}

	private String getEmptyStringIfNull(String value) {
		return value == null ? "" : value.trim();
	}

	private String getEmptyStringIfNegative(int value) {
		return value == -1 ? "" : "" + value;
	}

	public StringBuffer getActiveCategories(String circleID, char prepaidYes) throws Exception {
		Categories[] categories = RBTDBManager.getInstance().getActiveCategories(circleID,
				prepaidYes);
		StringBuffer strXML = new StringBuffer();

		if(categories == null) {
			strXML.append("<rbt><categories></categories></rbt>");
		}
		else {
			categories = rearrangeIndexWise(categories);
			strXML.append("<rbt><categories>");
			for(int i = 0; i < categories.length; i++) {
				if(categories[i].type() == RECORD) {
					continue;
				}
				else {
					strXML.append("<category id=\"" + categories[i].id() + "\" name=\""
							+ replaceSpecialCharacters(categories[i].name())
							+ "\" parentCategory=\"" + categories[i].parentID() + "\"   promoID=\""
							+ categories[i].promoID() + "\"  classType=\""
							+ categories[i].classType() + "\" >");
					if(categories[i].type() == PARENT || categories[i].type() == BOUQUET) {
						strXML.append(getSubCategories(categories[i].id(), categories[i].type(),
								categories[i].circleID(), categories[i].prepaidYes()));
					}
					else {
						strXML.append(getClips(categories[i].id()));
					}

					strXML.append("</category>");
				}
			}
			strXML.append("</categories></rbt>");
		}
		return strXML;
	}

	private String getSubCategories(int categoryID, int type, String circleID, char prepaidYes)
	throws Exception {
		String buf = new String();
		Categories[] sub1Categories = null;
		if(type == PARENT) {
			sub1Categories = RBTDBManager.getInstance().getSubCategories(categoryID,
					circleID, prepaidYes);
		}
		else if(type == BOUQUET) {
			sub1Categories = RBTDBManager.getInstance().getActiveBouquet(categoryID,
					circleID, prepaidYes);
		}
		if(sub1Categories == null) {
			return "";
		}
		sub1Categories = rearrangeIndexWise(sub1Categories);
		for(int i = 0; i < sub1Categories.length; i++) {
			buf = buf + "<sub1Category id=\"" + sub1Categories[i].id() + "\" name=\""
			+ replaceSpecialCharacters(sub1Categories[i].name()) + "\" parentCategory=\""
			+ sub1Categories[i].parentID() + "\" promoID=\"" + sub1Categories[i].promoID()
			+ "\" classType=\"" + sub1Categories[i].classType() + "\">";
			if(sub1Categories[i].type() == BOUQUET) {
				buf = buf
				+ getBouquet(sub1Categories[i].id(), sub1Categories[i].type(), circleID,
						prepaidYes);
			}
			else {
				buf = buf + getClips(sub1Categories[i].id());
			}
			buf = buf + "</sub1Category>";
		}
		return buf;
	}

	private String getBouquet(int categoryID, int type, String circleID, char prepaidYes)
	throws Exception {
		String bouquet = new String();
		Categories[] sub2Categories = null;
		sub2Categories = RBTDBManager.getInstance().getActiveBouquet(categoryID, circleID,
				prepaidYes);

		if(sub2Categories == null) {
			return "";
		}

		for(int i = 0; i < sub2Categories.length; i++) {
			bouquet = bouquet + "<sub2Category id=\"" + sub2Categories[i].id() + "\" name=\""
			+ replaceSpecialCharacters(sub2Categories[i].name()) + "\" parentCategory=\""
			+ sub2Categories[i].parentID() + "\" promoID=\"" + sub2Categories[i].promoID()
			+ "\" classType=\"" + sub2Categories[i].classType() + "\" >";
			bouquet = bouquet + getClips(sub2Categories[i].id());
			bouquet = bouquet + "</sub2Category>";
		}
		return bouquet;
	}

	private String getClips(int categoryID) throws Exception {
		String buf = new String();

		Clips[] clips = RBTDBManager.getInstance().getAllClips(categoryID);
		if(clips == null) {
		}
		else {
			ArrayList activeClipsList = new ArrayList();
			ArrayList inactiveClipsList = new ArrayList();
			for(int j = 0; j < clips.length; j++) {
				if(clips[j].clipInList())
					activeClipsList.add(clips[j]);
				else
					inactiveClipsList.add(clips[j]);

			}
			if(activeClipsList != null && activeClipsList.size() > 0 && inactiveClipsList != null
					&& inactiveClipsList.size() > 0) {
				activeClipsList.addAll(activeClipsList.size(), inactiveClipsList);
			}
			if(activeClipsList != null && activeClipsList.size() > 0)
				clips = (Clips[])activeClipsList.toArray(new Clips[0]);
			for(int j = 0; j < clips.length; j++) {
				buf = buf
				+ ("<clip name=\"" + replaceSpecialCharacters(clips[j].name())
						+ "\" wavFile=\"" + replaceSpecialCharacters(clips[j].wavFile())
						+ "\" promoID=\"" + clips[j].promoID() + "\" album=\""
						+ replaceSpecialCharacters(clips[j].album()) + "\" />");
			}
		}
		return buf;
	}

	public StringBuffer activateSubscriber(String strSubID, boolean isPrepaid, String activatedBy,
			String actInfo, String subClass, String circleId) throws Exception {
		StringBuffer strXML = new StringBuffer();

		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);

		if(subscriber != null) {
			if(m_activationPeriod != 0
					&& RBTDBManager.getInstance().isSubDeactive(subscriber))

			{
				Date activationPeriod = subscriber.endDate();
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(activationPeriod);
				long difference = (System.currentTimeMillis() - calendar.getTime().getTime())
				/ (1000 * 60 * 60);
				long timePeriod = (new Integer(m_activationPeriod)).longValue();
				if(difference < timePeriod) {
					strXML.append("<rbt><subscriber activate=\"" + m_activationPeriod
							+ "\" /></rbt>");
					return strXML;
				}
			}

		}
		subscriber = RBTDBManager.getInstance()
		.activateSubscriber(strSubID, activatedBy, null, isPrepaid, m_activationPeriod, 0,
				actInfo, subClass, m_useSubscriptionManager, circleId);

		if(subscriber == null) {
			strXML.append("<rbt><subscriber activate=\"false\"/></rbt>");
		}
		else {
			strXML.append("<rbt><subscriber activate=\"true\"/></rbt>");
		}
		return strXML;
	}
	public StringBuffer getcopy(String strSubID,String udid,String catId,String wavfile,String callerno,String copyno){
		StringBuffer strXML = new StringBuffer();
		String clipID = wavfile +":"+ catId +":1";

		if(callerno != null)
			clipID += "|"+ callerno;
		String channel="CCC";
		if(udid!=null){
			channel=channel+":"+udid;
		}
		RBTDBManager.getInstance().insertViralSMSTableMap(copyno, Calendar.getInstance().getTime(), "COPY", strSubID, clipID, 0,channel, null,null);
		strXML.append("	Yahoo !!! your copy has been successful");
		return strXML;
	}
	//	public StringBuffer getcopy(String strSubID, String mode, String catId, String wavfile,
	//			String callerno, String copyno) {
	//		StringBuffer strXML = new StringBuffer();
	//		String clipID = wavfile + ":" + catId + ":1";
	//
	//		if(callerno != null)
	//			clipID += "|" + callerno;
	//
	//		RBTDBManager.getInstance().insertViralSMSTable(copyno,
	//				Calendar.getInstance().getTime(), "COPY", strSubID, clipID, 0, "CCC", null);
	//		strXML.append("	Yahoo !!! your copy has been successful");
	//		return strXML;
	//	}
	public StringBuffer activateSubscriber(String strSubID, boolean isPrepaid,
			String activatedBy, String actInfo, String subClass,String lang_type, String circleId)
	throws Exception
	{
		StringBuffer strXML = new StringBuffer();

		Subscriber subscriber = RBTDBManager.getInstance()
		.getSubscriber(strSubID);
		//		ViralSMSTable[] gifts = RBTDBManager.getInstance()
		//				.getViralSMSesByType(strSubID, "GIFTED");
		//		if(gifts != null && gifts.length > 0)
		//		{
		//		String subClassType = "GIFT";
		//		String classType = "GIFT";
		//		Parameters  giftChargeParam= RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix) 
		//		.getParameter(COMMON,"GIFT_SUBSCRIPTION_CLASS");
		//		if(giftChargeParam!=null){
		//		subClassType=giftChargeParam.value().trim();
		//		}
		//		subscriber = RBTDBManager.init(m_dbURL, m_usePool,
		//		m_countryPrefix)
		//		.activateSubscriber(strSubID, "GUI", null, isPrepaid, m_activationPeriod, 
		//		0, actInfo, subClassType,m_useSubscriptionManager);
		//		RBTDBManager.init(m_dbURL, m_usePool,
		//		m_countryPrefix)
		//		.setSubscriberLanguage(strSubID,lang_type);
		//		}
		//		else {
		Calendar cal=Calendar.getInstance();
		Date currentDate=cal.getTime();
		subscriber = RBTDBManager.getInstance()
		.activateSubscriber(strSubID, activatedBy, currentDate, isPrepaid,
				m_activationPeriod, 0, actInfo, subClass,
				m_useSubscriptionManager, circleId);
		RBTDBManager.getInstance().setSubscriberLanguage(strSubID,lang_type);
		//		}
		if (subscriber == null)


		{
			strXML.append("error");
		}
		else

		{
			strXML.append("yahoo!!!activation completed");
		}
		return strXML;
	}
	//	public StringBuffer activateSubscriber(String strSubID, boolean isPrepaid, String activatedBy,
	//			String actInfo, String subClass, String lang_type) throws Exception {
	//		StringBuffer strXML = new StringBuffer();
	//
	//		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
	//		/*ViralSMSTable[] gifts = RBTDBManager.getInstance().getViralSMSesByType(strSubID,
	//				"GIFTED");*/
	//		// if(gifts != null && gifts.length > 0)
	//		// {
	//		// String subClassType = "GIFT";
	//		// String classType = "GIFT";
	//		// Parameters giftChargeParam= RBTDBManager.getInstance()
	//		// .getParameter(COMMON,"GIFT_SUBSCRIPTION_CLASS");
	//		// if(giftChargeParam!=null){
	//		// subClassType=giftChargeParam.value().trim();
	//		// }
	//		// subscriber = RBTDBManager.init(m_dbURL, m_usePool,
	//		// m_countryPrefix)
	//		// .activateSubscriber(strSubID, "GUI", null, isPrepaid,
	//		// m_activationPeriod,
	//		// 0, actInfo, subClassType,m_useSubscriptionManager);
	//		// RBTDBManager.init(m_dbURL, m_usePool,
	//		// m_countryPrefix)
	//		// .setSubscriberLanguage(strSubID,lang_type);
	//		// }
	//		// else {
	//		Calendar cal = Calendar.getInstance();
	//		Date currentDate = cal.getTime();
	//
	//		subscriber = RBTDBManager.getInstance().activateSubscriber(strSubID, activatedBy,
	//				currentDate, isPrepaid, m_activationPeriod, 0, actInfo, subClass,
	//				m_useSubscriptionManager);
	//		RBTDBManager.getInstance().setSubscriberLanguage(strSubID, lang_type);
	//		// }
	//		if(subscriber == null)
	//
	//		{
	//			strXML.append("error");
	//		}
	//		else
	//
	//		{
	//			strXML.append("yahoo!!!activation completed");
	//		}
	//		return strXML;
	//	}
	public StringBuffer updateSubscriber(String subId,String finaltype,String inittype,String newlang,String strActBy){
		boolean dct=true;
		if (!(finaltype.equalsIgnoreCase(inittype))&&(!(finaltype.equalsIgnoreCase("DEFAULT")))) {
			// Intentionally sending null for subscriber argument.
			dct = RBTDBManager.getInstance()
			.convertSubscriptionType(subId, inittype, finaltype,strActBy,0,false, null, null);
		}	

		if(newlang!=null)
			RBTDBManager.getInstance().setSubscriberLanguage(subId, newlang);

		StringBuffer strXML = new StringBuffer();


		if (!dct)
		{
			strXML.append("Error");
		}
		else
		{
			strXML.append("Yahoo !!! subscriber Updated");
		}
		return strXML;
	}
	
	public StringBuffer deactivateSubscriber(String strSubID,
			String deactivatedBy,String deActInfo) throws Exception
			{
		Calendar cal=Calendar.getInstance();
		Date currentDate=cal.getTime();

		Subscriber sub=RBTDBManager
		.getInstance()
		.getSubscriber(strSubID);
		String dct= RBTDBManager
		.getInstance()
		.deactivateSubscriber( strSubID,  deactivatedBy,
				currentDate,  m_delSelections,  true,
				m_useSubscriptionManager,  false,  true,sub.rbtType(),  sub,deActInfo, null);

		boolean success = false;
		if(dct != null && dct.equals("SUCCESS"))
			success = true;
		StringBuffer strXML = new StringBuffer();


		if (!success)
		{
			if(deactivatedBy.equalsIgnoreCase("CCC")){
				strXML.append("Error");
			}else{
				strXML.append("<rbt><subscriber deactivate=\"false\"/></rbt>");
			}
		}
		else
		{
			strXML.append("<rbt><subscriber deactivate=\"true\"/></rbt>");
		}
		return strXML;
			}


	public StringBuffer giftSelection(String subscriberID,String callerno,String sentTime,int clipid,String strChannelType,String gifter){
		StringBuffer strXML = new StringBuffer(); 
		boolean success = false; 
		Parameters giftCategory=null;
		Categories category=null;

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
		giftCategory = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "GIFT_CATEGORY");


		String subClassType = "GIFT";
		String classType = "FREE";
		Parameters  giftChargeParam= CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"GIFT_CHARGE_CLASS");
		if(giftChargeParam!=null){
			classType=giftChargeParam.getValue().trim();
		}
		try
		{
			if(!strChannelType.equalsIgnoreCase("GUIDel")){
				strXML = addSelections(subscriberID, callerno, Integer.parseInt(giftCategory.getValue()),clipid, 0, 2359, strChannelType, "GIFT", classType,null,null);
				RBTDBManager.getInstance() 
				.updateViralPromotion(gifter,subscriberID,sdf.parse(sentTime), "GIFTED", "ACCEPT_ACK", new Date(),strChannelType,null);
			}else{
				RBTDBManager.getInstance() 
				.updateViralPromotion(gifter,subscriberID,sdf.parse(sentTime), "GIFTED", "REJECT_ACK", new Date(), strChannelType,null);

			}
		}
		catch(Exception e)
		{
			logger.error("", e);
			strXML = new StringBuffer();
			strXML.append("Error");
		}
		return strXML;
	}
	
	public StringBuffer removeSelection(String strSubID, String strCallerID,
			String strChannel) throws Exception
			{
		/*RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix)
                .deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 23,
                                             m_useSubscriptionManager,
                                             strChannel);*/

		StringBuffer strXML = new StringBuffer(); 
		boolean success = false; 
		if(strCallerID == null){ 
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE")){ 
				if(isCorpSub(strSubID)){ 
					strXML.append("Dude you are a corporateUser!!! Sorry:("); 
					return strXML; 
				} 
				else 
					success = RBTDBManager.getInstance() 
					.deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 2359,
							m_useSubscriptionManager,
							strChannel);
			}
			else 
				success = RBTDBManager.getInstance() 
				.deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 2359, 
						m_useSubscriptionManager, 
						strChannel); 
		} 
		else 
			success = RBTDBManager.getInstance() 
			.deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 2359, 
					m_useSubscriptionManager, 
					strChannel); 
		if(success){ 
			strXML.append(" Yahoo !!! Removed Successfully");
		}
		else{
			strXML.append("error");
		}

		return strXML;
	}
	public StringBuffer removeSelectionForWebWap(String strSubID, String strCallerID, int status,int fromTime,int toTime,String strChannel,String wavFile)
	throws Exception {
		/*
		 * RBTDBManager.getInstance()
		 * .deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 23,
		 * m_useSubscriptionManager, strChannel);
		 */



		StringBuffer strXML = new StringBuffer();
		boolean success = false;
		if(strCallerID == null) {
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE")) {
				if(isCorpSub(strSubID)) {
					strXML.append("<rbt><remove success=\"false\"/></rbt>");
					return strXML;
				}
				else
					success = RBTDBManager.getInstance().deactivateSubscriberRecords(strSubID, strCallerID, status,fromTime, toTime, m_useSubscriptionManager,strChannel, wavFile);
			}
			else
				success = RBTDBManager.getInstance().deactivateSubscriberRecords(strSubID, strCallerID, status,fromTime, toTime, m_useSubscriptionManager,strChannel, wavFile);
		}
		else
			success = RBTDBManager.getInstance().deactivateSubscriberRecords(strSubID, strCallerID, status,fromTime, toTime, m_useSubscriptionManager,strChannel, wavFile);
		if(success) {
			strXML.append("<rbt><remove success=\"true\"/></rbt>");
		}
		else {
			strXML.append("error");
		}

		return strXML;
	}

	public StringBuffer addSelectionsByName(String strSubID, String strCallerID, int categoryID,
			String strRBT, int fromTime, int toTime, String selectedBy, String selectionInfo,
			String chargeClass,String selInterval) throws Exception {
		ClipMinimal clip = RBTDBManager.getInstance().getClipRBT(strRBT);
		return checkCorpUserAndAddSelection(strSubID, strCallerID, categoryID, fromTime, toTime,
				selectedBy, selectionInfo, chargeClass, clip,selInterval);
	}
	public StringBuffer addSelections(String strSubID, String strCallerID, int categoryID,
			String strRBT, int fromTime, int toTime, String selectedBy, String selectionInfo,
			String chargeClass,String selInterval) throws Exception {
		// Calendar endCal = Calendar.getInstance();
		// endCal.set(2037, 0, 1);
		Date endDate = null;// endCal.getTime();

		StringBuffer strXML = new StringBuffer();

		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);

		if(subscriber == null) {
			strXML.append("<rbt><setSelection success=\"false\"/></rbt>");
		}
		else {
			String subYes = null;
			if(subscriber != null)
				subYes = subscriber.subYes();

			SubscriptionClass sub = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(
					subscriber.subscriptionClass());

			if(sub != null && sub.getFreeSelections() > 0
					&& subscriber.maxSelections() < sub.getFreeSelections())
				chargeClass = "FREE";

			boolean OptIn = false;
			if(subscriber.activationInfo() != null
					&& subscriber.activationInfo().indexOf(":optin:") != -1)
				OptIn = true;

			int status = 1;
			if(fromTime != 0 || toTime != 2359)
				status = 80;
			boolean success = true;
			RBTDBManager dbManager = RBTDBManager.getInstance();
			String resp = null;
			if(!dbManager.isSelectionAllowed(subscriber, strCallerID))
				success = false;
			else if(dbManager.moreSelectionsAllowed(strSubID, strCallerID))
				resp = dbManager.addSubscriberSelectionsChannel(strSubID, strCallerID, categoryID,
						strRBT, null, null, endDate, status, selectedBy, selectionInfo, 0,
						subscriber.prepaidYes(), false, m_messagePath, fromTime, toTime,
						chargeClass, m_useSubscriptionManager, true, null, null, subYes, null,
						true, OptIn, dbManager.allowLooping() && m_loopingEnabled, sub
						.getSubscriptionClass(), subscriber,selInterval, null);

			if(success)
				strXML.append("<rbt><setSelection success=\"true\" message=\""+resp+"\"/></rbt>");
			else
				strXML.append("<rbt><setSelection success=\"false\" message=\"SELECTION_NOT_ALLOWED\"/></rbt>");
		}
		return strXML;
	}

	public StringBuffer addSelections(String strSubID, String strCallerID, int categoryID,
			int clipID, int fromTime, int toTime, String selectedBy, String selectionInfo,
			String chargeClass,String selInterval, String circleId) throws Exception {
		StringBuffer strxml = new StringBuffer();
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		if(RBTDBManager.getInstance().isSubscriberDeactivated(subscriber)) {
			strxml = activateSubscriber(strSubID, true, selectedBy, selectionInfo, "DEFAULT", "eng",circleId);
			if(strxml.toString().equalsIgnoreCase("error")) {
				return strxml;
			}
		}
		ClipMinimal clip = RBTDBManager.getInstance().getClipById(clipID);
		return checkCorpUserAndAddSelection(strSubID, strCallerID, categoryID, fromTime, toTime,
				selectedBy, selectionInfo, chargeClass, clip,selInterval);
	}
	private StringBuffer checkCorpUserAndAddSelection(String strSubID, String strCallerID,
			int categoryID, int fromTime, int toTime, String selectedBy, String selectionInfo,
			String chargeClass, ClipMinimal clip,String selInterval) throws Exception {
		StringBuffer strXML = new StringBuffer();
		if(strCallerID == null) {
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE")) {
				if(isCorpSub(strSubID)) {
					strXML
					.append("<rbt><setSelection success=\"false\" reason=\"corpUser\"/></rbt>");
					return strXML;
				}
				else
					return checkWavFileAndAddSelection(strSubID, strCallerID, categoryID, fromTime,
							toTime, selectedBy, selectionInfo, chargeClass, clip,selInterval);
			}
			else {
				return checkWavFileAndAddSelection(strSubID, strCallerID, categoryID, fromTime,
						toTime, selectedBy, selectionInfo, chargeClass, clip,selInterval);
			}
		}
		else {
			return checkWavFileAndAddSelection(strSubID, strCallerID, categoryID, fromTime, toTime,
					selectedBy, selectionInfo, chargeClass, clip,selInterval);
		}
	}
	
	private StringBuffer checkWavFileAndAddSelection(String strSubID, String strCallerID,
			int categoryID, int fromTime, int toTime, String selectedBy, String selectionInfo,
			String chargeClass, ClipMinimal clip,String selInterval) throws Exception {
		StringBuffer strXML = new StringBuffer();
		String wavFile = null;
		if(null != clip)
			wavFile = clip.getWavFile();
		if(wavFile == null) {
			strXML.append("<rbt><setSelection success=\"false\" reason=\"wavFile\"/></rbt>");
			return strXML;
		}
		return addSelections(strSubID, strCallerID, categoryID, wavFile, fromTime, toTime,
				selectedBy, selectionInfo, chargeClass,selInterval);
	}

	public StringBuffer getChargeClass() throws Exception {
		List<ChargeClass> chargeClass = CacheManagerUtil.getChargeClassCacheManager().getAllChargeClass();
		StringBuffer strXML = new StringBuffer();

		if(chargeClass == null) {
			strXML.append("<rbt><chargeClasses></chargeClasses></rbt>");
		}
		else {
			strXML.append("<rbt><chargeClasses>");
			for(int i = 0; i < chargeClass.size(); i++) {
				strXML.append("<chargeClass type=\"" + chargeClass.get(i).getChargeClass() + "\" amount=\""
						+ chargeClass.get(i).getAmount() + "\">");
				strXML.append("</chargeClass>");
			}
			strXML.append("</chargeClasses></rbt>");
		}
		return strXML;
	}

	public StringBuffer searchSong(String strSongType) throws Exception {
		String strSearchType = null;
		String strSong = null;
		strSong = strSongType.substring(0, strSongType.indexOf("_")).trim();
		strSearchType = strSongType.substring(strSongType.lastIndexOf("_") + 1).trim();
		StringBuffer strXML = new StringBuffer();
		if(RBTMOHelper.rbtClipsLucene == null) {
			strXML.append("<rbt><searchSongs></searchSongs></rbt>");
		}
		else {
			String[] songs = RBTMOHelper.rbtClipsLucene.search(strSong, false, strSearchType);
			if(songs == null) {
				strXML.append("<rbt><searchSongs></searchSongs></rbt>");
			}
			else {
				strXML.append("<rbt><searchSongs>");
				for(int i = 0; i < songs.length; i++) {
					int clipID = Integer.parseInt(songs[i].trim());
					ClipMinimal clip = RBTDBManager.getInstance().getClipById(clipID);
					if(clip != null) {
						strXML.append("<searchSong name=\"" + clip.getClipName() + "\" wavFile=\""
								+ clip.getWavFile() + "\" promotionID=\"" + clip.getPromoID()
								+ "\" album=\"" + clip.getAlbum() + "\" lang=\"" + clip.getLanguage()
								+ "\"/>");
					}
				}
				strXML.append("</searchSongs></rbt>");
			}
		}
		return strXML;
	}

	public StringBuffer sendSMS(String strSubID, String strPwd) throws Exception {
		StringBuffer strXML = new StringBuffer();
		if(strSubID != null && m_webSenderNo != null) {

			String smsText = m_webSMSText;
			if(smsText.lastIndexOf("%L") != -1) {
				int index = smsText.lastIndexOf("%L");
				smsText = smsText.substring(0, index) + strPwd + smsText.substring(index + 2);
			}

			boolean success = Tools.sendSMS(m_webSenderNo, strSubID, smsText, false);

			if(!success) {
				strXML.append("<rbt><sms success=\"false\"/></rbt>");
			}
			else {
				strXML.append("<rbt><sms success=\"true\"/></rbt>");
			}
		}
		else {
			strXML.append("<rbt><sms success=\"false\"/></rbt>");
		}
		return strXML;
	}

	private Categories[] rearrangeIndexWise(Categories[] cat) {
		if(cat == null)
			return cat;
		ArrayList nonZeroIndexList = new ArrayList();
		ArrayList zeroIndexList = new ArrayList();
		for(int j = 0; j < cat.length; j++) {
			if(cat[j].index() == 0)
				zeroIndexList.add(cat[j]);
			else
				nonZeroIndexList.add(cat[j]);

		}
		if(nonZeroIndexList != null && nonZeroIndexList.size() > 0 && zeroIndexList != null
				&& zeroIndexList.size() > 0) {
			nonZeroIndexList.addAll(nonZeroIndexList.size(), zeroIndexList);
		}
		if(nonZeroIndexList != null && nonZeroIndexList.size() > 0)
			cat = (Categories[])nonZeroIndexList.toArray(new Categories[0]);
		return cat;
	}

	private String replaceSpecialCharacters(String s) {
		if(s == null)
			return null;
		String sModified = "";
		char c;
		for(int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if(c == '&')
				sModified += "&amp;";
			else if(c < 128)
				sModified += c;
		}
		return sModified;
	}

	public boolean isCorpSub(String strSubID) {
		// String _method = "isCorpSub()";
		// //logger.info("****** parameters are --
		// "+strSubID);
		SubscriberStatus subStatus = RBTDBManager.getInstance().getActiveSubscriberRecord(
				strSubID, null, 0, 0, 2359);

		if(subStatus != null)
			return true;

		return false;
	}

	public StringBuffer getViralSmsDetail(String strCustomer) {
		StringBuffer strBuff = new StringBuffer();
		String nameFile = null;
		String userStatus = null;
		String updateDate = null;
		String[][] strArray = null;
		ViralSMSTable[] viralSmsValue = null;
		String type = "EC";// SmStype
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		// logger.info("the value
		// of rbtDBManager is "+rbtDBManager);
		viralSmsValue = rbtDBManager.getViralSMSesByType(strCustomer, type);
		if(viralSmsValue != null) {
			strArray = new String[viralSmsValue.length][];
			for(int i = 0; i < viralSmsValue.length; i++) {
				strArray[i] = new String[5];
			}
			for(int i = 0; i < viralSmsValue.length; i++) {
				String retailerID = viralSmsValue[i].subID();
				updateDate = viralSmsValue[i].sentTime() + "";
				String clipId = viralSmsValue[i].clipID();
				if(clipId != null && (!clipId.equalsIgnoreCase("null")) && clipId.length() > 0) {
					ClipMinimal clip = null;
					int clipId1 = Integer.parseInt(clipId);
					clip = rbtDBManager.getClipById(clipId1);
					nameFile = clip.getClipName();
					userStatus = "Downloaded";
				}
				else {
					userStatus = "Subscriber";
					int defaultSong = Integer.parseInt(RBTDBManager.getInstance()
							.getDefaultClipFromParametersDB());
					Clips clip = null;
					clip = rbtDBManager.getClip(defaultSong);
					nameFile = clip.name();
				}
				strArray[i][0] = strCustomer;
				strArray[i][1] = nameFile;
				strArray[i][2] = userStatus;
				strArray[i][3] = updateDate;
				strArray[i][4] = retailerID;
				strBuff.append(strArray[i][0] + "," + strArray[i][1] + "," + strArray[i][2] + ","
						+ strArray[i][3] + "," + strArray[i][4] + ";");
			}
			return strBuff;
		}
		else {
			return null;
		}
	}

	/**
	 * @author manoj.jaiswal
	 */
	public StringBuffer getSubscriptionStatus(String strSubID) throws Exception {
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(strSubID);
		StringBuffer strXML = new StringBuffer();
		if(!isSubActive(subscriber)) {
			strXML.append("<rbt><subscriber status=\"inActive\"/></rbt>");
		}
		else {
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE")) {
				if(isCorpSub(strSubID)) {
					strXML.append("<rbt><subscriber status=\"active\" type=\"corpUser\"/></rbt>");
				}
				else
					strXML.append("<rbt><subscriber status=\"active\" type=\"common\"/></rbt>");
			}
			else
				strXML.append("<rbt><subscriber status=\"active\" type=\"common\"/></rbt>");
		}
		return strXML;
	}

	private StringBuffer addLTPAttribute(StringBuffer theXML, String ltpPoints) {
		// String method = "addLTPAttribute";
		// System.out.println("**** Inside addLTPAttribute with xml as "+theXML
		// + " and ltp as "+ltpPoints);
		String theXMLString = theXML.toString();
		if(theXMLString.length() > 5) {
			theXMLString = "<rbt LTP = \"" + ltpPoints + "\">" + theXMLString.substring(5);
		}
		// System.out.println("**** Inside addLTPAttribute returning
		// "+theXMLString);
		return new StringBuffer(theXMLString);
	}

	public StringBuffer redeemLTPPoints(String subID, String ltpPoints) {
		return new StringBuffer(RBTDBManager.getInstance().redeemLTPPoints(subID,
				ltpPoints));
	}
	public StringBuffer gift(String subscriberID,String callerno,String sentTime,int clipid,String strChannelType,String gifter){ 
		StringBuffer strXML = new StringBuffer(); 
		// boolean success = false; 
		Parameters giftCategory=null; 
		// Categories category=null; 
		Date c=null; 

		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS"); 
		giftCategory = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "GIFT_CATEGORY"); 

		//category = RBTDBManager.getInstance() .getCategory(Integer.parseInt(giftCategory.value())); 

		//  String subClassType = "GIFT"; 
		String classType = "FREE"; 
		Parameters  giftChargeParam= CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,"GIFT_CHARGE_CLASS"); 
		if(giftChargeParam!=null){ 
			classType=giftChargeParam.getValue().trim(); 
		} 
		try 
		{ 
			if(!strChannelType.equalsIgnoreCase("GUIDel")){ 
				strXML = addSelections(subscriberID, callerno, Integer.parseInt(giftCategory.getValue()),clipid, 0, 2359, strChannelType, "GIFT", classType,null, null); 
				RBTDBManager.getInstance()
				.updateViralPromotion(gifter,subscriberID,sdf.parse(sentTime), "GIFTED", "ACCEPT_ACK", new Date(),"CCC",null); 
			}else{ 
				RBTDBManager.getInstance()
				.updateViralPromotion(gifter,subscriberID,sdf.parse(sentTime), "GIFTED", "REJECT_ACK", new Date(), "CCC",null); 

			} 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			strXML = new StringBuffer(); 
			strXML.append("Error"); 
		} 
		return strXML; 
	} 
	public StringBuffer addGift(String giftee,String gifter, String mode,String contentId){ 
		StringBuffer strXML = new StringBuffer(); 

		String canBeGifted = null;
		if(giftee == null || giftee.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(gifter == null || gifter.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(mode == null || mode.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		/*if(contentId == null || contentId.trim().equalsIgnoreCase("null")) 
                return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>");*/ 

		try 
		{ 	
			ValidateNumberRequest validateNumberRequest = new ValidateNumberRequest(gifter, giftee, contentId, null);
			RBTClient.getInstance().validateGifteeNumber(validateNumberRequest);
			canBeGifted = validateNumberRequest.getResponse();
			if(!canBeGifted.equalsIgnoreCase("VALID")){
				return strXML.append("<rbt><status type='FAILURE' message='"+canBeGifted+"'/></rbt>");
			}
			RBTDBManager.getInstance().insertViralSMSTableMap(gifter.trim(), Calendar.getInstance().getTime(), "GIFT", giftee.trim(), contentId, 0, mode, null,null); 
			return strXML.append("<rbt><status type='SUCCESS' message='GIFT ADDED'/></rbt>"); 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 

	public StringBuffer updateSubscriberSelection(String subscriberId,String callerId,String subWavFile,String setTime,
			int fromTime,int toTime,String selInterval, String selectedBy){

		StringBuffer strXML = new StringBuffer();
		Date setDate = null;
		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>");
		if(setTime == null) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>");

		try{
			setDate = simpleDateFormat.parse(setTime);
			String success = RBTDBManager.getInstance().updateSubscriberSelection(subscriberId,callerId,subWavFile,setDate,
					fromTime,toTime,selInterval,selectedBy);

			if(success.startsWith("SELECTION_SUCCESS"))
				return strXML.append("<rbt><status type='SUCCESS'/></rbt>");
			else
				return strXML.append("<rbt><status type='FAILURE'/></rbt>");

		}catch(Exception e){

			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>");
		}
	}

	public StringBuffer getDownloads(String subscriberId){ 
		StringBuffer strXML = new StringBuffer(); 

		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>");


		try 
		{ 
			strXML.append("<rbt>"); 
			SubscriberDownloads[] sd = RBTDBManager.getInstance().getActiveSubscriberDownloads(subscriberId); 
			if(sd==null || sd.length == 0) 
				strXML.append("<downloads count='0'>"); 
			else 
			{ 
				strXML.append("<downloads count='"+sd.length+"' >"); 
				for (int i = 0; i < sd.length; i++ ) 
					strXML.append("<download promoId='"+sd[i].promoId()+"' downloadStatus='"+sd[i].downloadStatus()+"' categoryId='"+sd[i].categoryID()+"' categoryType='"+ sd[i].categoryType() +"' />"); 
			} 
			return strXML.append("</downloads></rbt>"); 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 

	public StringBuffer removeDownload(String subscriberId, String wavFile, String deactivationInfo, String catId, String catType){ 
		StringBuffer strXML = new StringBuffer(); 

		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(wavFile == null || wavFile.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(deactivationInfo == null || deactivationInfo.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 

		try 
		{ 
			boolean success = RBTDBManager.getInstance().expireSubscriberDownload(subscriberId.trim(), wavFile.trim(), Integer.parseInt(catId), Integer.parseInt(catType) , deactivationInfo.trim(), null, false); 
			if(success) 
				return strXML.append("<rbt><status type='SUCCESS' message='REMOVED'/></rbt>"); 
			else 
				return strXML.append("<rbt><status type='SUCCESS' message='NOT REMOVED'/></rbt>"); 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 

	private Categories getCategory(SubscriberStatus status)
	{
		if(status == null)
			return null;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String circleID = rbtDBManager.getCircleId(status.subID());
		char isPrepaid = status.prepaidYes() ? 'y' : 'n';
		Categories category = rbtDBManager.getCategory(status.categoryID(), circleID, isPrepaid);
		return category; 
	}

	public StringBuffer setNewsletter(String subscriberId, String strRequestValue){ 
		StringBuffer strXML = new StringBuffer(); 

		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(strRequestValue == null || strRequestValue.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(strRequestValue.equalsIgnoreCase("true"))
			strRequestValue = iRBTConstant.NEWSLETTER_ON;
		else
			strRequestValue = iRBTConstant.NEWSLETTER_OFF;

		try 
		{ 
			boolean success = RBTDBManager.getInstance().updateExtraInfo(subscriberId.trim(), iRBTConstant.IS_NEWSLETTER_ON, strRequestValue); 
			if(success) 
				return strXML.append("<rbt><status type='SUCCESS'/></rbt>"); 
			else 
				return strXML.append("<rbt><status type='SUCCESS' /></rbt>"); 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 

	public StringBuffer getNewsletter(String subscriberId){ 
		StringBuffer strXML = new StringBuffer(); 

		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		try 
		{ 
			Subscriber sub = RBTDBManager.getInstance().getSubscriber(subscriberId);
			HashMap hm = RBTDBManager.getInstance().getExtraInfoMap(sub);
			String result = null;
			if(hm != null)
				result = (String)hm.get(iRBTConstant.IS_NEWSLETTER_ON);
			if(result == null)
				result = iRBTConstant.NEWSLETTER_OFF;
			return strXML.append("<rbt><status type='"+result+"' /></rbt>"); 
		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 

	public StringBuffer addDownload(String subscriberId, String wavFile, String categoryId, String categoryType, String actBy){ 
		StringBuffer strXML = new StringBuffer(); 

		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		if(wavFile == null || wavFile.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>");
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
		if(!isSubActive(subscriber) )
			return strXML.append("<rbt><status type='FAILURE' message='INACTIVE SUBSCRIBER'/></rbt>");

		try 
		{ 
			int catID = new Integer(categoryId).intValue();
			int catType = new Integer(categoryType).intValue();
			boolean isActive = false;
			String subYes = subscriber.subYes();
			if(subYes != null && (subYes.equalsIgnoreCase("B") || subYes.equalsIgnoreCase("O")))
				isActive = true;
			String result = RBTDBManager.getInstance().addDownload( subscriberId,  wavFile,  catID,  catType,  actBy, null, isActive);
			return strXML.append("<rbt><status type='SUCCESS' message='"+result+"'/></rbt>"); 

		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 

	public StringBuffer getSubscriberInfo(String subscriberId){ 
		StringBuffer strXML = new StringBuffer(); 

		if(subscriberId == null || subscriberId.trim().equalsIgnoreCase("null")) 
			return strXML.append("<rbt><status type='FAILURE' message='INVALID PARAMETERS'/></rbt>"); 
		try 
		{ 
			HashMap hm = RBTDBManager.getInstance().getSubscriberInfo( subscriberId);
			String status  = "UNKNOWN";
			String userType = "UNKNOWN";
			if(hm != null)
			{
				status = (String)hm.get("STATUS");
				userType = (String)hm.get("USER_TYPE");
				return strXML.append("<rbt><response type='SUCCESS' status='"+status+"' user_type='"+userType+"'/></rbt>");
			}	
			return strXML.append("<rbt><response type='FAILURE' status='INTERNAL ERROR'/></rbt>"); 

		} 
		catch(Exception e) 
		{ 
			logger.error("", e); 
			return strXML.append("<rbt><status type='FAILURE' message='INTERNAL ERROR'/></rbt>"); 
		} 
	} 





}