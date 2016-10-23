/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: thribhuvan.hl $
 * $Id: XMLParser.java,v 1.118 2015/05/04 12:56:44 thribhuvan.hl Exp $
 * $Revision: 1.118 $
 * $Date: 2015/05/04 12:56:44 $
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ApplicationDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.BiDownloadHistory;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Bookmark;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Bookmarks;
import com.onmobile.apps.ringbacktones.webservice.client.beans.BulkTask;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CallDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeSms;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consents;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.FeedStatus;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Gift;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftInbox;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftOutbox;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Group;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.MobileAppRegistration;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewSubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PendingConfirmationsRemainder;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PickOfTheDay;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RBTLoginUser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RecentSelection;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Retailer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSHistory;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSText;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPromo;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TransData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Transaction;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.WCHistory;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 */
public class XMLParser implements WebServiceConstants {
	private static DocumentBuilder documentBuilder = null;

	public static Rbt getRBT(Element rbtElem, Request request) {
		if (rbtElem == null)
			return null;

		Element subscriberElem = (Element) rbtElem.getElementsByTagName(
				SUBSCRIBER).item(0);
		Subscriber subscriber = getSubscriber(subscriberElem);

		Element giftInboxElem = (Element) rbtElem.getElementsByTagName(
				GIFT_INBOX).item(0);
		GiftInbox giftInbox = getGiftInbox(giftInboxElem);

		Element giftOutboxElem = (Element) rbtElem.getElementsByTagName(
				GIFT_OUTBOX).item(0);
		GiftOutbox giftOutbox = getGiftOutbox(giftOutboxElem);

		Element libraryElem = (Element) rbtElem.getElementsByTagName(LIBRARY)
				.item(0);
		Library library = getLibrary(libraryElem, request);

		Element miPlaylistElem = (Element) rbtElem.getElementsByTagName(
				MI_PLAYLIST).item(0);
		Downloads miPlaylist = getDownloads(miPlaylistElem, request);

		Element bookmarksElem = (Element) rbtElem.getElementsByTagName(
				BOOKMARKS).item(0);
		Bookmarks bookmarks = getBookmarks(bookmarksElem, request);

		Element groupDetailsElem = (Element) rbtElem.getElementsByTagName(
				GROUP_DETAILS).item(0);
		GroupDetails groupDetails = getGroupDetails(groupDetailsElem, request);

		Element smsHistoryElem = (Element) rbtElem.getElementsByTagName(
				SMS_HISTORY).item(0);
		SMSHistory[] smsHistory = getSMSHistory(smsHistoryElem);
		// RBT-8199:Need to include all SMS logs in CCC GUI as part of SMS logs
		Element smsHistoryFromUrlElem = (Element) rbtElem.getElementsByTagName(
				SMS_HISTORY_FROM_UMP).item(0);
		SMSHistory[] smsHistoryFromUrl = getSMSHistory(smsHistoryFromUrlElem);

		Element wcHistoryElem = (Element) rbtElem.getElementsByTagName(
				WC_HISTORY).item(0);
		WCHistory[] wcHistory = getWCHistory(wcHistoryElem);

		Element transactionHistoryElem = (Element) rbtElem
				.getElementsByTagName(TRANSACTION_HISTORY).item(0);
		Transaction[] transactions = getTransactionHistory(transactionHistoryElem);

		Element callDetailsElem = (Element) rbtElem.getElementsByTagName(
				CALL_DETAILS).item(0);
		CallDetails callDetails = getCallDetails(callDetailsElem);

		Element subscriberPromoElem = (Element) rbtElem.getElementsByTagName(
				SUBSCRIBER_PROMO).item(0);
		SubscriberPromo subscriberPromo = getSubscriberPromo(subscriberPromoElem);

		Element offersElem = (Element) rbtElem.getElementsByTagName(OFFERS)
				.item(0);
		Offer[] allOffers = getOffers(offersElem);

		Element biDownloadElem = (Element) rbtElem.getElementsByTagName(
				BI_DOWNLOAD_HISTORY_INFO).item(0);
		BiDownloadHistory[] biDownloadHistories = getBIDownloadHistory(biDownloadElem);

		Element subScriberPacksEle = (Element) rbtElem.getElementsByTagName(
				SUBSCRIBERPACKS).item(0);
		SubscriberPack[] subscriberPacks = getSubscriberPacks(subScriberPacksEle);

		Element consentElm = (Element) rbtElem.getElementsByTagName(CONSENT)
				.item(0);
		Consent consentObj = getConsent(consentElm);

		Element consentsElm = (Element) rbtElem.getElementsByTagName(CONSENTS)
				.item(0);
		Consent[] consentObjects = getConsents(consentsElm);
		Consents consents = null;
		if (consentObjects != null) {
			consents = new Consents();
			consents.setConsent(consentObjects);
		}

		Element mobileAppRegistrationElm = (Element) rbtElem
				.getElementsByTagName(REGISTERIDS).item(0);
		MobileAppRegistration mobileAppRegistration = getMobileAppRegistration(mobileAppRegistrationElm);

		if (mobileAppRegistration != null) {
			Element notificationSmsElm = (Element) rbtElem
					.getElementsByTagName(NOTIFICATION_SMS).item(0);
			if (notificationSmsElm != null) {
				Text responseText = (Text) notificationSmsElm.getFirstChild();
				mobileAppRegistration.setSmsText(responseText.getNodeValue());
			}

		}

		Element viralDataElem = (Element) rbtElem.getElementsByTagName(
				VIRAL_DATA).item(0);
		ViralData[] viralData = XMLParser.getViralData(viralDataElem);

		Rbt rbt = new Rbt();
		rbt.setSubscriber(subscriber);
		rbt.setGiftInbox(giftInbox);
		rbt.setGiftOutbox(giftOutbox);
		rbt.setLibrary(library);
		rbt.setmiPlaylist(miPlaylist);
		rbt.setBookmarks(bookmarks);
		rbt.setGroupDetails(groupDetails);
		rbt.setSmsHistory(smsHistory);
		rbt.setTransactions(transactions);
		rbt.setCallDetails(callDetails);
		rbt.setSubscriberPromo(subscriberPromo);
		rbt.setOffers(allOffers);
		rbt.setBiDownloadHistories(biDownloadHistories);
		rbt.setSubscriberPacks(subscriberPacks);
		rbt.setWcHistory(wcHistory);
		rbt.setConsent(consentObj);
		rbt.setMobileAppRegistration(mobileAppRegistration);
		rbt.setSmsHistoryFromUMP(smsHistoryFromUrl);
		rbt.setConsents(consents);
		rbt.setViralData(viralData);

		return rbt;
	}

	public static Subscriber getSubscriber(Element subscriberElem) {
		if (subscriberElem == null)
			return null;

		Subscriber subscriber = new Subscriber();

		HashMap<String, String> attributeMap = getAttributesMap(subscriberElem);


		//operator
		subscriber.setOperatorUserType(attributeMap.get(OPERATOR_USER_TYPE));
		
		
		subscriber.setSubscriberID(attributeMap.get(SUBSCRIBER_ID));
		subscriber.setAccessCount(Integer.parseInt(attributeMap
				.get(ACCESS_COUNT)));
		subscriber.setCanAllow(attributeMap.get(CAN_ALLOW)
				.equalsIgnoreCase(YES));
		subscriber.setCircleID(attributeMap.get(CIRCLE_ID));
		subscriber.setPrepaid(attributeMap.get(IS_PREPAID)
				.equalsIgnoreCase(YES));
		subscriber.setValidPrefix(attributeMap.get(IS_VALID_PREFIX)
				.equalsIgnoreCase(YES));
		subscriber.setLanguage(attributeMap.get(LANGUAGE));
		subscriber.setStatus(attributeMap.get(STATUS));
		subscriber.setUserType(attributeMap.get(USER_TYPE));

		subscriber.setSubscriptionClass(attributeMap.get(SUBSCRIPTION_CLASS));
		subscriber.setCosID(attributeMap.get(COS_ID));
		subscriber.setActivatedBy(attributeMap.get(ACTIVATED_BY));
		subscriber.setActivationInfo(attributeMap.get(ACTIVATION_INFO));
		subscriber.setDeactivatedBy(attributeMap.get(DEACTIVATED_BY));
		subscriber.setLastDeactivationInfo(attributeMap
				.get(LAST_DEACTIVATION_INFO));
		subscriber.setRefID(attributeMap.get(REF_ID));
		subscriber.setPack(attributeMap.get(PACK));
		subscriber.setPca(attributeMap.get(PCA));
		subscriber.setNextSelectionAmount(attributeMap
				.get(NEXT_SELECTION_AMOUNT));
		subscriber.setNextChargeClass(attributeMap.get(NEXT_CHARGE_CLASS));
		subscriber.setChargeDetails(attributeMap.get(CHARGE_DETAILS));
		subscriber.setLastChargeAmount(attributeMap.get(LAST_CHARGE_AMOUNT));
		subscriber.setLastTransactionType(attributeMap.get(LAST_TRANSACTION_TYPE));

		if (attributeMap.containsKey(IS_FREEMIUM_SUBSCRIBER)) {
			subscriber.setFreemiumSubscriber(attributeMap.get(
					IS_FREEMIUM_SUBSCRIBER).equalsIgnoreCase("true"));
		}

		if (attributeMap.containsKey(IS_FREEMIUM)) {
			subscriber.setFreemium(attributeMap.get(IS_FREEMIUM)
					.equalsIgnoreCase("true"));
		}

		if (attributeMap.containsKey(NUM_OF_FREE_SONGS_LEFT)) {
			subscriber.setNumOfFreeSongsLeft(Integer.parseInt(attributeMap
					.get(NUM_OF_FREE_SONGS_LEFT)));
		}

        if(attributeMap.containsKey(NUM_MAX_SELECTIONS)){
        	 subscriber.setNumMaxSelections(attributeMap.get(NUM_MAX_SELECTIONS)!=null ? Integer.parseInt(attributeMap.get(NUM_MAX_SELECTIONS)): 0 );
        }
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		try {
			if (attributeMap.containsKey(START_DATE))
				subscriber.setStartDate(dateFormat.parse(attributeMap
						.get(START_DATE)));
			if (attributeMap.containsKey(END_DATE))
				subscriber.setEndDate(dateFormat.parse(attributeMap
						.get(END_DATE)));
			if (attributeMap.containsKey(NEXT_CHARGING_DATE))
				subscriber.setNextChargingDate(dateFormat.parse(attributeMap
						.get(NEXT_CHARGING_DATE)));
			if (attributeMap.containsKey(LAST_DEACTIVATION_DATE))
				subscriber.setLastDeactivationDate(dateFormat
						.parse(attributeMap.get(LAST_DEACTIVATION_DATE)));
			if (attributeMap.containsKey(ACTIVATION_DATE))
				subscriber.setActivationDate(dateFormat.parse(attributeMap
						.get(ACTIVATION_DATE)));
			if (attributeMap.containsKey(NEXT_BILLING_DATE))
				subscriber.setNextBillingDate(dateFormat.parse(attributeMap
						.get(NEXT_BILLING_DATE)));
		} catch (ParseException e) {

		}
		if (attributeMap.containsKey(IS_NEWS_LETTER_ON))
			subscriber.setNewsLetterOn(attributeMap.get(IS_NEWS_LETTER_ON)
					.equalsIgnoreCase(YES));
		if (attributeMap.containsKey(IS_UDS_ON))
			subscriber.setUdsOn(attributeMap.get(IS_UDS_ON).equalsIgnoreCase(
					YES));
		if (attributeMap.containsKey(DAYS_AFTER_DEACTIVATION))
			subscriber.setDaysAfterDeactivation(Integer.parseInt(attributeMap
					.get(DAYS_AFTER_DEACTIVATION)));
		if (attributeMap.containsKey(TOTAL_DOWNLOADS))
			subscriber.setTotalDownloads(Integer.parseInt(attributeMap
					.get(TOTAL_DOWNLOADS)));
		if (attributeMap.containsKey(iRBTConstant.param_isSubConsentInserted)) {
			subscriber.setSubConsentInserted(true);
		}
		if (attributeMap.containsKey(iRBTConstant.param_protocolNo)) {
			subscriber.setProtocolNo(attributeMap.get(param_protocolNo));
		}
		if (attributeMap.containsKey(iRBTConstant.param_protocolStaticText)) {
			subscriber.setProtocolStaticText(attributeMap
					.get(param_protocolStaticText));
		}
		subscriber.setOperatorUserInfo(attributeMap.get(OPERATOR_USER_INFO));
		subscriber.setSubscriptionState(attributeMap.get(SUBSCRIPTION_STATE));
		subscriber.setOperatorName(attributeMap.get(XML_OPERATOR_NAME));

		if (attributeMap.containsKey(USER_INFO)) {
			Document document = getDocumentFromString(attributeMap
					.get(USER_INFO));
			if (document != null) {
				HashMap<String, String> userInfoMap = getAttributesMap(document
						.getDocumentElement());
				subscriber.setUserInfoMap(userInfoMap);
			}
		}

		subscriber.setSubscriberType(attributeMap.get(SUBSCRIBER_TYPE));

		return subscriber;
	}

	public static GiftInbox getGiftInbox(Element giftInboxElem) {
		if (giftInboxElem == null)
			return null;

		GiftInbox giftInbox = new GiftInbox();
		Element contentsElem = (Element) giftInboxElem.getElementsByTagName(
				CONTENTS).item(0);
		giftInbox.setGifts(getGifts(contentsElem));

		return giftInbox;
	}

	public static GiftOutbox getGiftOutbox(Element giftOutboxElem) {
		if (giftOutboxElem == null)
			return null;

		GiftOutbox giftOutbox = new GiftOutbox();
		Element contentsElem = (Element) giftOutboxElem.getElementsByTagName(
				CONTENTS).item(0);
		giftOutbox.setGifts(getGifts(contentsElem));

		return giftOutbox;
	}

	public static Consent getConsent(Element consentElem) {
		if (consentElem == null)
			return null;

		Consent consentObj = new Consent();
		String chargeclass = consentElem.getAttribute("chargeclass");
		String clip_id = consentElem.getAttribute("clip_id");
		String msisdn = consentElem.getAttribute("msisdn");
		String promoId = consentElem.getAttribute("promoId");
		String sub_class = consentElem.getAttribute("sub_class");
		String trans_id = consentElem.getAttribute("trans_id");
		if (trans_id == null || trans_id.trim().length() == 0) {
			trans_id = consentElem.getAttribute("transID");
		}
		String mode = consentElem.getAttribute("mode");
		String catId = consentElem.getAttribute("catId");
		String srvId = consentElem.getAttribute("srvId");
		String srvClass = consentElem.getAttribute("srvClass");

		String price = consentElem.getAttribute("price");
		String validity = consentElem.getAttribute("value");

		String linkedRefId = consentElem.getAttribute(param_linkedRefId);
		String refId = consentElem.getAttribute(param_refID);
		String clipInfo = consentElem.getAttribute(param_clipInfo);

		String callerId = consentElem.getAttribute("caller_id");
		String reqType = consentElem.getAttribute(param_reqType);
		String songname = consentElem.getAttribute(param_songname);
		String requesttimestamp = consentElem.getAttribute("requesttimestamp");
		String selectionType = consentElem.getAttribute("selection_type");
		// Added description to pass the session id.
		String description = consentElem.getAttribute(DESCRIPTION);
		String circleId = consentElem.getAttribute("circleId");
		String sessionId = consentElem.getAttribute("consent_session_id");
		consentObj.setMsisdn(msisdn);
		consentObj.setChargeclass(chargeclass);
		consentObj.setClipId(clip_id);
		consentObj.setPromoId(promoId);
		consentObj.setTransId(trans_id);
		consentObj.setSubClass(sub_class);
		consentObj.setMode(mode);
		consentObj.setCatId(catId);
		consentObj.setPrice(price);
		consentObj.setValidity(validity);
		consentObj.setSrvId(srvId);
		consentObj.setSrvClass(srvClass);
		consentObj.setLinkedRefId(linkedRefId);
		consentObj.setRefId(refId);
		consentObj.setClipInfo(clipInfo);
		consentObj.setCallerId(callerId);
		consentObj.setReqType(reqType);
		consentObj.setSongname(songname);
		consentObj.setRequestTime(requesttimestamp);
		consentObj.setSelType(selectionType);
		consentObj.setDescription(description);
		consentObj.setCircleId(circleId);
		consentObj.setSessionId(sessionId);
		return consentObj;
	}

	public static Consent getConsentAndProcessedObj(Element consentElem) {

		if (consentElem == null)
			return null;

		Consent consentObj = new Consent();

		String subId = consentElem.getAttribute("subId");
		String callerId = consentElem.getAttribute("callerId");
		String categoryId = consentElem.getAttribute("categoryId");
		String mode = consentElem.getAttribute("mode");
		String startTime = consentElem.getAttribute("startTime");
		String endTime = consentElem.getAttribute("endTime");
		String status = consentElem.getAttribute("status");
		String classType = consentElem.getAttribute("classType");
		String cosId = consentElem.getAttribute("cosId");
		String packCosId = consentElem.getAttribute("packCosId");
		String clipId = consentElem.getAttribute("clipId");
		String selInterval = consentElem.getAttribute("selInterval");
		String fromTime = consentElem.getAttribute("fromTime");
		String toTime = consentElem.getAttribute("toTime");
		String selectionInfo = consentElem.getAttribute("selectionInfo");
		String selType = consentElem.getAttribute("selType");
		String inLoop = consentElem.getAttribute("inLoop");
		String purchaseType = consentElem.getAttribute("purchaseType");
		String useUiChargeClass = consentElem.getAttribute("useUiChargeClass");
		String categoryType = consentElem.getAttribute("categoryType");
		String profileHrs = consentElem.getAttribute("profileHrs");
		String prepaidYes = consentElem.getAttribute("prepaidYes");
		String feedType = consentElem.getAttribute("feedType");
		String wavFileName = consentElem.getAttribute("wavFileName");
		String rbtType = consentElem.getAttribute("rbtType");
		String circleId = consentElem.getAttribute("circleId");
		String language = consentElem.getAttribute("language");
		String requestTime = consentElem.getAttribute("requestTime");
		String extraInfo = consentElem.getAttribute("extraInfo");
		String requestType = consentElem.getAttribute("requestType");
		String consentStatus = consentElem.getAttribute("consentStatus");
		String transId = consentElem.getAttribute("transId");

		consentObj.setMsisdn(subId);
		consentObj.setCallerId(callerId);
		consentObj.setCatId(categoryId);
		consentObj.setMode(mode);
		consentObj.setStartTime(startTime);
		consentObj.setEndTime(endTime);
		consentObj.setStatus(status);
		consentObj.setChargeclass(classType);
		consentObj.setCosId(cosId);
		consentObj.setPackCosId(packCosId);
		consentObj.setClipId(clipId);
		consentObj.setSelInterval(selInterval);
		consentObj.setFromTime(fromTime);
		consentObj.setToTime(toTime);
		consentObj.setSelectionInfo(selectionInfo);
		consentObj.setSelType(selType);
		consentObj.setInLoop(inLoop);
		consentObj.setPurchaseType(purchaseType);
		consentObj.setUseUiChargeClass(useUiChargeClass);
		consentObj.setCategoryType(categoryType);
		consentObj.setProfileHrs(profileHrs);
		consentObj.setPrepaidYes(prepaidYes);
		consentObj.setFeedType(feedType);
		consentObj.setWavFileName(wavFileName);
		consentObj.setRbtType(rbtType);
		consentObj.setCircleId(circleId);
		consentObj.setLanguage(language);
		consentObj.setRequestTime(requestTime);
		consentObj.setExtraInfo(extraInfo);
		consentObj.setReqType(requestType);
		consentObj.setConsentStatus(consentStatus);
		consentObj.setTransId(transId);

		return consentObj;

	}

	public static MobileAppRegistration getMobileAppRegistration(
			Element registraionIdsElm) {
		if (registraionIdsElm == null)
			return null;

		MobileAppRegistration mobileAppRegistration = new MobileAppRegistration();
		Map<String, String> registerIdSubscriberIdMap = new HashMap<String, String>();
		NodeList registerIdNodeList = registraionIdsElm
				.getElementsByTagName(REGISTERID);

		for (int i = 0; i < registerIdNodeList.getLength(); i++) {
			Element registerIdElement = (Element) registerIdNodeList.item(i);
			Text responseText = (Text) registerIdElement.getFirstChild();
			registerIdSubscriberIdMap.put(responseText.getNodeValue(),
					registerIdElement.getAttribute(SUBSCRIBER_ID));
		}

		mobileAppRegistration
				.setRegisterIdSubscriberIdMap(registerIdSubscriberIdMap);

		return mobileAppRegistration;
	}

	public static Gift[] getGifts(Element giftContentsElem) {
		List<HashMap<String, String>> contentsList = getContentsList(giftContentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<Gift> list = new ArrayList<Gift>();
		for (HashMap<String, String> map : contentsList) {
			Gift gift = new Gift();

			if (map.containsKey(ID))
				gift.setToneID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(CATEGORY_ID))
				gift.setCategoryID(Integer.parseInt(map.get(CATEGORY_ID)));

			gift.setToneName(map.get(NAME));
			gift.setToneType(map.get(TYPE));
			gift.setSender(map.get(SENDER));
			gift.setReceiver(map.get(RECEIVER));
			gift.setPreviewFile(map.get(PREVIEW_FILE));
			gift.setRbtFile(map.get(RBT_FILE));
			gift.setStatus(map.get(STATUS));

			if (map.containsKey(EXTRA_INFO)) {
				Document document = getDocumentFromString(map.get(EXTRA_INFO));
				if (document != null) {
					HashMap<String, String> extraInfoMap = getAttributesMap(document
							.getDocumentElement());
					gift.setGiftExtraInfoMap(extraInfoMap);
				}
			}
			if (map.containsKey(SELECTED_BY))
				gift.setSelectedBy(map.get(SELECTED_BY));

			try {
				if (map.containsKey(SENT_TIME))
					gift.setSentTime(dateFormat.parse(map.get(SENT_TIME)));
			} catch (ParseException e) {

			}
			try {
				if (map.containsKey(VALIDITY))
					gift.setValidity(dateFormat.parse(map.get(VALIDITY)));
			} catch (ParseException e) {

			}
			list.add(gift);
		}

		return (list.toArray(new Gift[0]));
	}

	public static Library getLibrary(Element libraryElem, Request request) {
		if (libraryElem == null)
			return null;

		Library library = new Library();

		HashMap<String, String> attributeMap = getAttributesMap(libraryElem);

		if (attributeMap.containsKey(IS_ALBUM_USER))
			library.setAlbumUser(attributeMap.get(IS_ALBUM_USER)
					.equalsIgnoreCase(YES));
		if (attributeMap.containsKey(PROVIDE_DEFAULT_LOOP_OPTION))
			library.setProvideDefaultLoopOption(attributeMap.get(
					PROVIDE_DEFAULT_LOOP_OPTION).equalsIgnoreCase(YES));
		if (attributeMap.containsKey(IS_MUSICBOX_DEFAULT_SETTING_PENDING))
			library.setMusicboxDefaultSettingPending(attributeMap.get(
					IS_MUSICBOX_DEFAULT_SETTING_PENDING).equalsIgnoreCase(YES));
		if (attributeMap.containsKey(IS_CLIP_DEFAULT_SETTING_PENDING))
			library.setClipDefaultSettingPending(attributeMap.get(
					IS_CLIP_DEFAULT_SETTING_PENDING).equalsIgnoreCase(YES));
		if (attributeMap.containsKey(TOTAL_DOWNLOADS))
			library.setTotalDownloads(Integer.parseInt(attributeMap
					.get(TOTAL_DOWNLOADS)));
		if (attributeMap.containsKey(NO_OF_FREE_SONGS_LEFT))
			library.setFreeDownloadsLeft(Integer.parseInt(attributeMap
					.get(NO_OF_FREE_SONGS_LEFT)));
		if (attributeMap.containsKey(IS_RECENT_SEL_CONSENT)) {
			library.setRecentSelConsent(true);
		}
		library.setNextSelectionAmount(attributeMap.get(NEXT_SELECTION_AMOUNT));
		library.setNextChargeClass(attributeMap.get(NEXT_CHARGE_CLASS));

		Element settingsElement = (Element) libraryElem.getElementsByTagName(
				SETTINGS).item(0);
		if (settingsElement != null)
			library.setSettings(getSettings(settingsElement, request));

		Element downloadsElement = (Element) libraryElem.getElementsByTagName(
				DOWNLOADS).item(0);
		if (downloadsElement != null)
			library.setDownloads(getDownloads(downloadsElement, request));

		Element recentSelectionElement = (Element) libraryElem
				.getElementsByTagName(RECENT_SELECTIONS).item(0);
		if (recentSelectionElement != null)
			library.setRecentSelection(getRecentSelection(
					recentSelectionElement, request));
		return library;
	}

	public static Bookmarks getBookmarks(Element bookmarksElem, Request request) {
		if (bookmarksElem == null)
			return null;

		Bookmarks bookmarks = new Bookmarks();

		Element contentsElem = (Element) bookmarksElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");

		List<Bookmark> list = new ArrayList<Bookmark>();
		for (HashMap<String, String> map : contentsList) {
			Bookmark bookmark = new Bookmark();

			if (map.containsKey(ID))
				bookmark.setToneID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(SHUFFLE_ID))
				bookmark.setShuffleID(Integer.parseInt(map.get(SHUFFLE_ID)));
			if (map.containsKey(CATEGORY_ID))
				bookmark.setCategoryID(Integer.parseInt(map.get(CATEGORY_ID)));

			bookmark.setSubscriberID(request.getSubscriberID());
			bookmark.setToneName(map.get(NAME));
			bookmark.setToneType(map.get(TYPE));
			bookmark.setPreviewFile(map.get(PREVIEW_FILE));
			bookmark.setRbtFile(map.get(RBT_FILE));
			bookmark.setAmount(map.get(AMOUNT));

			try {
				if (map.containsKey(END_DATE))
					bookmark.setEndDate(dateFormat.parse(map.get(END_DATE)));
			} catch (ParseException e) {

			}

			list.add(bookmark);
		}

		bookmarks.setBookmarks(list.toArray(new Bookmark[0]));

		return bookmarks;
	}

	public static Settings getSettings(Element settingsElem, Request request) {
		if (settingsElem == null)
			return null;

		Settings settings = new Settings();

		HashMap<String, String> attributeMap = getAttributesMap(settingsElem);

		Element contentsElem = (Element) settingsElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<Setting> list = new ArrayList<Setting>();
		for (HashMap<String, String> map : contentsList) {
			Setting setting = new Setting();

			if (map.containsKey(ID))
				setting.setToneID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(SHUFFLE_ID))
				setting.setShuffleID(map.get(SHUFFLE_ID));
			if (map.containsKey(FROM_TIME))
				setting.setFromTime(Integer.parseInt(map.get(FROM_TIME)));
			if (map.containsKey(FROM_TIME_MINUTES))
				setting.setFromTimeMinutes(Integer.parseInt(map
						.get(FROM_TIME_MINUTES)));
			if (map.containsKey(TO_TIME))
				setting.setToTime(Integer.parseInt(map.get(TO_TIME)));
			if (map.containsKey(TO_TIME_MINUTES))
				setting.setToTimeMinutes(Integer.parseInt(map
						.get(TO_TIME_MINUTES)));
			if (map.containsKey(STATUS))
				setting.setStatus(Integer.parseInt(map.get(STATUS)));
			if (map.containsKey(CATEGORY_ID))
				setting.setCategoryID(Integer.parseInt(map.get(CATEGORY_ID)));

			if (map.containsKey(param_udpId))
				setting.setUdpId(Integer.parseInt(map.get(param_udpId)));
			
			// Added for cut rbt dtoc
			if (map.containsKey(CUT_RBT_START_TIME))
				setting.setCutRBTStartTime(map.get(CUT_RBT_START_TIME));
			
			setting.setSubscriberID(request.getSubscriberID());
			setting.setToneName(map.get(NAME));
			setting.setToneType(map.get(TYPE));
			setting.setCallerID(map.get(CALLER_ID));
			setting.setPreviewFile(map.get(PREVIEW_FILE));
			setting.setRbtFile(map.get(RBT_FILE));
			setting.setUgcRbtFile(map.get(UGC_RBT_FILE));
			setting.setChargeClass(map.get(CHARGE_CLASS));
			setting.setSelInterval(map.get(INTERVAL));
			setting.setSelectionStatus(map.get(SELECTION_STATUS));
			setting.setSelectionStatusID(map.get(SELECTION_STATUS_ID));
			setting.setSelectionType(map.get(SELECTION_TYPE));
			setting.setSelectedBy(map.get(SELECTED_BY));

			String selectionInfo = map.get(SELECTION_INFO);
			if (selectionInfo != null) {
				int dctStartIndex = selectionInfo.indexOf("|DCT:");
				if (dctStartIndex != -1) {
					int dctEndIndex = selectionInfo.indexOf("|",
							dctStartIndex + 5);
					String deselectionInfo = selectionInfo.substring(
							dctStartIndex + 5, dctEndIndex);
					setting.setDeselectionInfo(deselectionInfo);

					selectionInfo = selectionInfo.substring(0, dctStartIndex)
							+ selectionInfo.substring(dctEndIndex);
				}
			}

			setting.setSelectionInfo(selectionInfo);
			setting.setDeselectedBy(map.get(DESELECTED_BY));
			setting.setChargingModel(map.get(CHARGING_MODEL));
			setting.setOptInOutModel(map.get(OPTINOUT_MODEL));
			setting.setRefID(map.get(REF_ID));

			try {
				if (map.containsKey(SET_TIME))
					setting.setSetTime(dateFormat.parse(map.get(SET_TIME)));
				if (map.containsKey(START_TIME))
					setting.setStartTime(dateFormat.parse(map.get(START_TIME)));
				if (map.containsKey(END_TIME))
					setting.setEndTime(dateFormat.parse(map.get(END_TIME)));
				if (map.containsKey(NEXT_CHARGING_DATE))
					setting.setNextChargingDate(dateFormat.parse(map
							.get(NEXT_CHARGING_DATE)));
				if (map.containsKey(NEXT_BILLING_DATE))
					setting.setNextBillingDate(dateFormat.parse(map
							.get(NEXT_BILLING_DATE)));
				if (map.containsKey(LAST_CHARGE_AMOUNT))
					setting.setLastChargeAmount(map.get(LAST_CHARGE_AMOUNT));
				if (map.containsKey(LAST_TRANSACTION_TYPE))
					setting.setTransactionStatus(map.get(LAST_TRANSACTION_TYPE));
			} catch (ParseException e) {

			}

			if (map.containsKey(SELECTION_EXTRA_INFO)) {
				Document document = getDocumentFromString(map
						.get(SELECTION_EXTRA_INFO));
				if (document != null) {
					HashMap<String, String> selectionInfoMap = getAttributesMap(document
							.getDocumentElement());
					setting.setSelectionInfoMap(selectionInfoMap);
				}
			}

			if (map.containsKey(LOOP_STATUS)) {
				setting.setLoopStatus(map.get(LOOP_STATUS));
			}

			if (map.containsKey(IS_CURRENT_SETTING)) {
				setting.setIsCurrentSetting(Boolean.parseBoolean(map
						.get(IS_CURRENT_SETTING)));
			}
			list.add(setting);
		}

		if (attributeMap.containsKey(NO_OF_DEFAULT_SETTINGS))
			settings.setNoOfDefaultSettings(Integer.parseInt(attributeMap
					.get(NO_OF_DEFAULT_SETTINGS)));
		if (attributeMap.containsKey(NO_OF_SPECIAL_SETTINGS))
			settings.setNoOfSpecialSettings(Integer.parseInt(attributeMap
					.get(NO_OF_SPECIAL_SETTINGS)));

		settings.setSettings(list.toArray(new Setting[0]));

		return settings;
	}

	public static RecentSelection getRecentSelection(Element recentSelElem,
			Request request) {
		if (recentSelElem == null)
			return null;
		RecentSelection recentSelection = new RecentSelection();
		HashMap<String, String> attributeMap = getAttributesMap(recentSelElem);
		String classType = attributeMap.get(CHARGE_CLASS);
		if (classType != null)
			recentSelection.setClassType(classType);

		return recentSelection;
	}

	public static Downloads getDownloads(Element downloadsElem, Request request) {
		if (downloadsElem == null)
			return null;

		Downloads downloads = new Downloads();

		HashMap<String, String> attributeMap = getAttributesMap(downloadsElem);

		Element contentsElem = (Element) downloadsElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<Download> list = new ArrayList<Download>();
		for (HashMap<String, String> map : contentsList) {
			Download download = new Download();

			if (map.containsKey(ID))
				download.setToneID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(SHUFFLE_ID))
				download.setShuffleID(map.get(SHUFFLE_ID));
			if (map.containsKey(CATEGORY_ID))
				download.setCategoryID(Integer.parseInt(map.get(CATEGORY_ID)));
			if (map.containsKey(IS_SET_FOR_ALL))
				download.setSetForAll(map.get(IS_SET_FOR_ALL).equalsIgnoreCase(
						YES));

			download.setSubscriberID(request.getSubscriberID());
			download.setToneName(map.get(NAME));
			download.setToneType(map.get(TYPE));
			download.setPreviewFile(map.get(PREVIEW_FILE));
			download.setRbtFile(map.get(RBT_FILE));
			download.setUgcRbtFile(map.get(UGC_RBT_FILE));
			download.setDownloadStatus(map.get(DOWNLOAD_STATUS));
			download.setDownloadType(map.get(DOWNLOAD_TYPE));
			download.setChargeClass(map.get(CHARGE_CLASS));
			download.setSelectedBy(map.get(SELECTED_BY));
			download.setDeselectedBy(map.get(DESELECTED_BY));
			download.setRefID(map.get(REF_ID));

			String selectionInfo = map.get(SELECTION_INFO);
			if (selectionInfo != null) {
				int dctStartIndex = selectionInfo.indexOf("|DCT:");
				if (dctStartIndex != -1) {
					int dctEndIndex = selectionInfo.indexOf("|",
							dctStartIndex + 5);
					String deselectionInfo = selectionInfo.substring(
							dctStartIndex + 5, dctEndIndex);
					download.setDeselectionInfo(deselectionInfo);

					selectionInfo = selectionInfo.substring(0, dctStartIndex)
							+ selectionInfo.substring(dctEndIndex);
				}
			}

			download.setSelectionInfo(selectionInfo);

            try {
            	if(map.containsKey(NEXT_BILLING_DATE)){
            		download.setNextBillingDate(dateFormat.parse(map.get(NEXT_BILLING_DATE)));
            	}
                if (map.containsKey(SET_TIME))
                    download.setSetTime(dateFormat.parse(map.get(SET_TIME)));
                if (map.containsKey(END_TIME))
                    download.setEndTime(dateFormat.parse(map.get(END_TIME)));
                if (map.containsKey(NEXT_BILLING_DATE))
                    download.setNextBillingDate(dateFormat.parse(map
                            .get(NEXT_BILLING_DATE)));
                if (map.containsKey(NEXT_CHARGING_DATE))
                    download.setNextChargingDate(dateFormat.parse(map
                            .get(NEXT_CHARGING_DATE)));
                if (map.containsKey(LAST_CHARGED_DATE))
                    download.setLastChargedDate(dateFormat.parse(map
                            .get(LAST_CHARGED_DATE)));
            } catch (ParseException e) {}

			if (map.containsKey(DOWNLOAD_INFO)) {
				Document document = getDocumentFromString(map
						.get(DOWNLOAD_INFO));
				if (document != null) {
					HashMap<String, String> downloadInfoMap = getAttributesMap(document
							.getDocumentElement());
					download.setDownloadInfoMap(downloadInfoMap);
				}
			}

			if (map.containsKey(LAST_CHARGE_AMOUNT))
				download.setLastChargeAmount(map.get(LAST_CHARGE_AMOUNT));
			
			if (map.containsKey(LAST_TRANSACTION_TYPE))
				download.setTransactionStatus(map.get(LAST_TRANSACTION_TYPE));

			list.add(download);
		}

		if (attributeMap.containsKey(NO_OF_ACTIVE_DOWNLOADS))
			downloads.setNoOfActiveDownloads(Integer.parseInt(attributeMap
					.get(NO_OF_ACTIVE_DOWNLOADS)));

		downloads.setDownloads(list.toArray(new Download[0]));

		return downloads;
	}

	public static GroupDetails getGroupDetails(Element groupDetailsElem,
			Request request) {
		if (groupDetailsElem == null)
			return null;

		GroupDetails groupDetails = new GroupDetails();

		HashMap<String, String> attributeMap = getAttributesMap(groupDetailsElem);

		Element groupsElem = (Element) groupDetailsElem.getElementsByTagName(
				GROUPS).item(0);
		Element contentsElem = (Element) groupsElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		Element groupMembersElem = (Element) groupDetailsElem
				.getElementsByTagName(GROUP_MEMBERS).item(0);

		List<Group> list = new ArrayList<Group>();
		for (HashMap<String, String> map : contentsList) {
			Group group = new Group();

			group.setSubscriberID(request.getSubscriberID());
			group.setGroupID(map.get(ID));
			group.setGroupName(map.get(NAME));
			group.setGroupPromoID(map.get(GROUP_PROMO_ID));
			group.setPredefinedGroupID(map.get(PREDEFINED_GROUP_ID));
			group.setGroupStatus(map.get(GROUP_STATUS));
			group.setGroupNamePrompt(map.get(GROUP_NAME_PROMPT));

			GroupMember[] groupMembers = null;
			Element thisGroupMembersElem = (Element) groupMembersElem
					.getElementsByTagName(GROUP + "_" + group.getGroupID())
					.item(0);
			if (thisGroupMembersElem != null) {
				HashMap<String, String> memberAttributeMap = getAttributesMap(thisGroupMembersElem);

				Element membersContentsElem = (Element) thisGroupMembersElem
						.getElementsByTagName(CONTENTS).item(0);
				List<HashMap<String, String>> memberContentsList = getContentsList(membersContentsElem);

				List<GroupMember> memberList = new ArrayList<GroupMember>();
				for (HashMap<String, String> memberMap : memberContentsList) {
					GroupMember groupMember = new GroupMember();

					groupMember.setGroupID(group.getGroupID());
					groupMember.setMemberID(memberMap.get(ID));
					groupMember.setMemberName(memberMap.get(NAME));
					groupMember.setMemberStatus(memberMap.get(MEMBER_STATUS));

					memberList.add(groupMember);
				}

				if (memberAttributeMap.containsKey(NO_OF_ACTIVE_MEMBERS))
					group.setNoOfActiveMembers(Integer
							.parseInt(memberAttributeMap
									.get(NO_OF_ACTIVE_MEMBERS)));

				groupMembers = memberList.toArray(new GroupMember[0]);
			}
			group.setGroupMembers(groupMembers);

			list.add(group);
		}

		if (attributeMap.containsKey(NO_OF_ACTIVE_GROUPS))
			groupDetails.setNoOfActiveGroups(Integer.parseInt(attributeMap
					.get(NO_OF_ACTIVE_GROUPS)));

		groupDetails.setGroups(list.toArray(new Group[0]));

		return groupDetails;
	}

	public static SMSHistory[] getSMSHistory(Element smsHistoryElem) {
		if (smsHistoryElem == null)
			return null;

		Element contentsElem = (Element) smsHistoryElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<SMSHistory> list = new ArrayList<SMSHistory>();
		for (HashMap<String, String> map : contentsList) {
			SMSHistory smsHistory = new SMSHistory();

			smsHistory.setSmsType(map.get(TYPE));
			smsHistory.setSmsText(map.get(SMS_TEXT));
			smsHistory.setSentTime(map.get(SENT_TIME));

			list.add(smsHistory);
		}
		boolean isSmsHistroyDesc = RBTParametersUtils.getParamAsBoolean(
				iRBTConstant.COMMON, "SUPPORT_FOR_DESC_SMS_HISTORY_ENABLED",
				"FALSE");
		if (isSmsHistroyDesc) {
			Collections.reverse(list);
		}
		SMSHistory[] smsHistory = list.toArray(new SMSHistory[0]);
		return smsHistory;
	}

	public static WCHistory[] getWCHistory(Element wcHistoryElem) {
		if (wcHistoryElem == null)
			return null;

		Element contentsElem = (Element) wcHistoryElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<WCHistory> list = new ArrayList<WCHistory>();
		for (HashMap<String, String> map : contentsList) {
			WCHistory wcHistory = new WCHistory();

			wcHistory.setAction(map.get(WC_ACTION));
			wcHistory.setRequestDate(map.get(WC_REQUEST_DATE));
			wcHistory.setMode(map.get(WC_MODE));
			wcHistory.setModeInfo(map.get(WC_MODE_INFO));
			wcHistory.setRetailerID(map.get(WC_RETAILER_ID));
			list.add(wcHistory);
		}

		WCHistory[] wcHistory = list.toArray(new WCHistory[0]);
		return wcHistory;
	}

	public static Transaction[] getTransactionHistory(
			Element transactionHistoryElem) {
		if (transactionHistoryElem == null)
			return null;

		Element contentsElem = (Element) transactionHistoryElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		List<Transaction> list = new ArrayList<Transaction>();
		for (HashMap<String, String> map : contentsList) {
			Transaction transaction = new Transaction();

            transaction.setType(map.get(TYPE));
            transaction.setAmount(map.get(AMOUNT));
            transaction.setMode(map.get(MODE));
            transaction.setSongName(map.get(SONG_NAME));
            String clipId = map.get(CLIP_ID);
            String catId = map.get(CATEGORY_ID);
            if(catId != null){
            	Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(catId));
            	boolean isShuffleCategory = false;
            	if(category != null){
                	isShuffleCategory = Utility.isShuffleCategory(category.getCategoryTpe());
            	}
            	transaction.setCategoryId(catId);
            	transaction.setShuffleCategory(isShuffleCategory);
            }
            transaction.setClipId(clipId);
            try {
                if (map.containsKey(DATE))
                    transaction.setDate(dateFormat.parse(map.get(DATE)));
            } catch (ParseException e) {

			}
			transaction.setRefundAmount(map.get(REF_AMOUNT));
			transaction.setAgentId(map.get(AGENT_ID));
			try {
				if (map.containsKey(NEXT_CHARGING_DATE))
					transaction.setNextChargeDate(dateFormat.parse(map
							.get(NEXT_CHARGING_DATE)));
			} catch (ParseException e) {

			}
			transaction.setService(map.get(SERVICEKEY));
			transaction.setReason(map.get(REASON));
			transaction.setValidity(map.get(VALIDITY));

			if (map.containsKey(PROTOCOL_NO) && map.get(PROTOCOL_NO) != null
					&& !map.get(PROTOCOL_NO).trim().isEmpty()) {
            	transaction.setProtocolNum(Long.parseLong(map.get(PROTOCOL_NO)));
            }
			
			try {
				if (map.containsKey(COS_ID)) {
					transaction.setCosId(Integer.parseInt(map.get(COS_ID)));
				}
			} catch (NumberFormatException e) {
			}
            list.add(transaction);
        }

		Transaction[] transactions = list.toArray(new Transaction[0]);
		return transactions;
	}

	public static CallDetails getCallDetails(Element callDetailsElem) {
		if (callDetailsElem == null)
			return null;

		CallDetails callDetails = new CallDetails();
		return callDetails;
	}

	public static SubscriberPromo getSubscriberPromo(Element subscriberPromoElem) {
		if (subscriberPromoElem == null)
			return null;

		HashMap<String, String> attributeMap = getAttributesMap(subscriberPromoElem);

		SubscriberPromo subscriberPromo = new SubscriberPromo();

		if (attributeMap.containsKey(IS_PREPAID))
			subscriberPromo.setPrepaid(attributeMap.get(IS_PREPAID)
					.equalsIgnoreCase(YES));
		if (attributeMap.containsKey(FREE_DAYS))
			subscriberPromo.setFreeDays(Integer.parseInt(attributeMap
					.get(FREE_DAYS)));

		subscriberPromo.setSubscriberID(attributeMap.get(SUBSCRIBER_ID));
		subscriberPromo.setActivatedBy(attributeMap.get(ACTIVATED_BY));
		subscriberPromo
				.setSubscriptionType(attributeMap.get(SUBSCRIPTION_TYPE));

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		try {
			if (attributeMap.containsKey(START_DATE))
				subscriberPromo.setStartDate(dateFormat.parse(attributeMap
						.get(START_DATE)));
			if (attributeMap.containsKey(END_DATE))
				subscriberPromo.setEndDate(dateFormat.parse(attributeMap
						.get(END_DATE)));
		} catch (ParseException e) {

		}

		return subscriberPromo;
	}

	public static Offer[] getOffers(Element offersElem) {
		if (offersElem == null)
			return null;

		ArrayList<Offer> allOffersList = new ArrayList<Offer>();
		NodeList childOffers = offersElem.getElementsByTagName(OFFER);

		for (int i = 0; i < childOffers.getLength(); i++) {
			Element offerElem = (Element) childOffers.item(i);
			HashMap<String, String> offerDetail = getPropertiesMap(offerElem);

			Offer offer = new Offer();

			if (offerDetail.containsKey(OFFER_AMOUNT))
				offer.setAmount(Double.parseDouble(offerDetail
						.get(OFFER_AMOUNT)));
			if (offerDetail.containsKey(OFFER_TYPE))
				offer.setType(Integer.parseInt(offerDetail.get(OFFER_TYPE)));
			if (offerDetail.containsKey(OFFER_VALID_DAYS))
				offer.setValidityDays(Integer.parseInt(offerDetail
						.get(OFFER_VALID_DAYS)));

			offer.setMetaInfo(offerDetail.get(OFFER_METAINFO));
			offer.setOfferDesc(offerDetail.get(OFFER_DESC));
			offer.setOfferID(offerDetail.get(OFFER_ID));
			offer.setSrvKey(offerDetail.get(OFFER_SRVKEY));
			offer.setSmOfferType(offerDetail.get(OFFER_SM_OFFER_TYPE));
			offer.setSmRate(offerDetail.get(OFFER_SM_RATE));
			offer.setCreditsAvailable(offerDetail.get(OFFER_CREDITS_AVAILABLE));
			if (offerDetail != null
					&& offerDetail.get(OFFER_TYPE_VALUE) != null
					&& offerDetail.get(OFFER_TYPE_VALUE).length() > 0)
				offer.setOfferTypeValue(Integer.valueOf(offerDetail
						.get(OFFER_TYPE_VALUE)));

			offer.setOfferValidity(offerDetail.get(OFFER_VALIDITY));
			offer.setOfferRenewalAmount(offerDetail.get(OFFER_RENEWAL_AMOUNT));
			offer.setOfferRenewalValidity(offerDetail
					.get(OFFER_RENEWAL_VALIDITY));
			allOffersList.add(offer);
		}

		return allOffersList.toArray(new Offer[0]);
	}

	public static Consent[] getConsents(Element consentsElem) {
		if (consentsElem == null)
			return null;
		ArrayList<Consent> allConsentList = new ArrayList<Consent>();
		NodeList childConsents = consentsElem.getElementsByTagName(CONSENT);

		for (int i = 0; i < childConsents.getLength(); i++) {
			Element consentElem = (Element) childConsents.item(i);
			Consent consent = new Consent();
			consent.setMsisdn(consentElem.getAttribute("subId"));
			consent.setCallerId(consentElem.getAttribute("callerId"));
			consent.setCatId(consentElem.getAttribute("categoryId"));
			consent.setMode(consentElem.getAttribute("mode"));
			consent.setStartTime(consentElem.getAttribute("startTime"));
			consent.setEndTime(consentElem.getAttribute("endTime"));
			consent.setStatus(consentElem.getAttribute("status"));
			consent.setChargeclass(consentElem.getAttribute("classType"));
			consent.setCosId(consentElem.getAttribute("cosId"));
			consent.setPackCosId(consentElem.getAttribute("packCosId"));
			consent.setClipId(consentElem.getAttribute("clipId"));
			consent.setSelInterval(consentElem.getAttribute("selInterval"));
			consent.setFromTime(consentElem.getAttribute("fromTime"));
			consent.setToTime(consentElem.getAttribute("fromTime"));
			consent.setSelectionInfo(consentElem.getAttribute("selectionInfo"));
			consent.setSelType(consentElem.getAttribute("selType"));
			consent.setInLoop(consentElem.getAttribute("inLoop"));
			consent.setPurchaseType(consentElem.getAttribute("purchaseType"));
			consent.setUseUiChargeClass(consentElem
					.getAttribute("useUiChargeClass"));
			consent.setCategoryType(consentElem.getAttribute("categoryType"));
			consent.setProfileHrs(consentElem.getAttribute("profileHrs"));
			consent.setProfileHrs(consentElem.getAttribute("prepaidYes"));
			consent.setFeedType(consentElem.getAttribute("feedType"));
			consent.setWavFileName(consentElem.getAttribute("wavFileName"));
			consent.setRbtType(consentElem.getAttribute("rbtType"));
			consent.setCircleId(consentElem.getAttribute("circleId"));
			consent.setLanguage(consentElem.getAttribute("language"));
			consent.setRequestTime(consentElem.getAttribute("requestTime"));
			consent.setExtraInfo(consentElem.getAttribute("extraInfo"));
			consent.setReqType(consentElem.getAttribute("requestType"));
			consent.setConsentStatus(consentElem.getAttribute("consentStatus"));
			consent.setTransId(consentElem.getAttribute("transId"));

			allConsentList.add(consent);
		}

		return allConsentList.toArray(new Consent[0]);

	}

	public static CopyDetails getCopyDetails(Element copyDetailsElem) {
		if (copyDetailsElem == null)
			return null;

		HashMap<String, String> attributeMap = getAttributesMap(copyDetailsElem);

		CopyDetails copyDetails = new CopyDetails();
		copyDetails.setSubscriberID(attributeMap.get(SUBSCRIBER_ID));
		copyDetails.setFromSubscriber(attributeMap.get(FROM_SUBSCRIBER));

		if (attributeMap.containsKey(USER_HAS_MULTIPLE_SELECTIONS))
			copyDetails.setUserHasMultipleSelections(attributeMap.get(
					USER_HAS_MULTIPLE_SELECTIONS).equalsIgnoreCase(YES));

		Element contentsElem = (Element) copyDetailsElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy");

		List<CopyData> list = new ArrayList<CopyData>();
		for (HashMap<String, String> map : contentsList) {
			CopyData copyData = new CopyData();

			if (map.containsKey(ID))
				copyData.setToneID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(CATEGORY_ID))
				copyData.setCategoryID(Integer.parseInt(map.get(CATEGORY_ID)));
			if (map.containsKey(STATUS))
				copyData.setStatus(Integer.parseInt(map.get(STATUS)));
			if (map.containsKey(ISSHUFFLEORLOOP))
				copyData.setShuffleOrLoop(Boolean.parseBoolean(map
						.get(ISSHUFFLEORLOOP)));

			copyData.setToneName(map.get(NAME));
			copyData.setToneType(map.get(TYPE));
			copyData.setPreviewFile(map.get(PREVIEW_FILE));
			copyData.setAmount(map.get(AMOUNT));
			copyData.setPeriod(map.get(PERIOD));

			try {
				if (map.containsKey(END_DATE))
					copyData.setEndDate(dateFormat.parse(map.get(END_DATE)));
			} catch (ParseException e) {

			}

			list.add(copyData);
		}

		CopyData[] copyData = list.toArray(new CopyData[0]);
		copyDetails.setCopyData(copyData);

		return copyDetails;
	}

	public static ApplicationDetails getApplicationDetails(
			Element applicationDetailsElem) {
		if (applicationDetailsElem == null)
			return null;

		ApplicationDetails applicationDetails = new ApplicationDetails();

		Element parametersElem = (Element) applicationDetailsElem
				.getElementsByTagName(PARAMETERS).item(0);
		Parameter[] parameters = getParameters(parametersElem);

		Element subscriptionClassesElem = (Element) applicationDetailsElem
				.getElementsByTagName(SUBSCRIPTION_CLASSES).item(0);
		SubscriptionClass[] subscriptionClasses = getSubscriptionClasses(subscriptionClassesElem);

		Element chargeClassesElem = (Element) applicationDetailsElem
				.getElementsByTagName(CHARGE_CLASSES).item(0);
		ChargeClass[] chargeClasses = getChargeClasses(chargeClassesElem);

		Element smsTextsElem = (Element) applicationDetailsElem
				.getElementsByTagName(SMS_TEXTS).item(0);
		SMSText[] smsTexts = getSMSTexts(smsTextsElem);

		Element pickOfTheDaysElem = (Element) applicationDetailsElem
				.getElementsByTagName(PICK_OF_THE_DAYS).item(0);
		PickOfTheDay[] pickOfTheDays = getPickOfTheDays(pickOfTheDaysElem);

		Element rbtLoginUserElem = (Element) applicationDetailsElem
				.getElementsByTagName(RBT_LOGIN_USERS).item(0);
		RBTLoginUser[] rbtLoginUsers = getRBTLoginUsers(rbtLoginUserElem);

		Element sitesElem = (Element) applicationDetailsElem
				.getElementsByTagName(SITES).item(0);
		Site[] sites = getSites(sitesElem);

		Element chargeSmsesElem = (Element) applicationDetailsElem
				.getElementsByTagName(CHARGE_SMS).item(0);
		ChargeSms[] chargeSmses = getChargeSmses(chargeSmsesElem);

		Element cosDetailsElem = (Element) applicationDetailsElem
				.getElementsByTagName(COS_DETAILS).item(0);
		Cos[] coses = getCoses(cosDetailsElem);

		Element retailerElem = (Element) applicationDetailsElem
				.getElementsByTagName(RETAILER).item(0);
		Retailer[] retailers = getRetailers(retailerElem);

		Element feedStatusesElem = (Element) applicationDetailsElem
				.getElementsByTagName(FEED_STATUS).item(0);
		FeedStatus[] feedStatuses = getFeedStatuses(feedStatusesElem);

		Element feedDetailsElem = (Element) applicationDetailsElem
				.getElementsByTagName(FEED_DETAILS).item(0);
		Feed[] feeds = getFeeds(feedDetailsElem);

		Element predefinedGroupsElem = (Element) applicationDetailsElem
				.getElementsByTagName(PREDEFINED_GROUPS).item(0);
		PredefinedGroup[] predefinedGroups = getPredefinedGroups(predefinedGroupsElem);

		applicationDetails.setParameters(parameters);
		applicationDetails.setSubscriptionClasses(subscriptionClasses);
		applicationDetails.setChargeClasses(chargeClasses);
		applicationDetails.setSmsTexts(smsTexts);
		applicationDetails.setPickOfTheDays(pickOfTheDays);
		applicationDetails.setRbtLoginUsers(rbtLoginUsers);
		applicationDetails.setSites(sites);
		applicationDetails.setChargeSmses(chargeSmses);
		applicationDetails.setCoses(coses);
		applicationDetails.setRetailers(retailers);
		applicationDetails.setFeedStatuses(feedStatuses);
		applicationDetails.setFeeds(feeds);
		applicationDetails.setPredefinedGroups(predefinedGroups);

		return applicationDetails;
	}

	public static Parameter[] getParameters(Element parametersElem) {
		if (parametersElem == null)
			return null;

		Element contentsElem = (Element) parametersElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<Parameter> list = new ArrayList<Parameter>();
		for (HashMap<String, String> map : contentsList) {
			String type = map.get(TYPE);
			map.remove(TYPE);

			Set<String> keySet = map.keySet();
			for (String key : keySet) {
				Parameter parameter = new Parameter();

				parameter.setType(type);
				parameter.setName(key);
				parameter.setValue(map.get(key));

				list.add(parameter);
			}
		}

		Parameter[] parameters = list.toArray(new Parameter[0]);
		return parameters;
	}

	public static SubscriptionClass[] getSubscriptionClasses(
			Element subscriptionClassesElem) {
		if (subscriptionClassesElem == null)
			return null;

		Element contentsElem = (Element) subscriptionClassesElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<SubscriptionClass> list = new ArrayList<SubscriptionClass>();
		for (HashMap<String, String> map : contentsList) {
			SubscriptionClass subscriptionClass = new SubscriptionClass();

			if (map.containsKey(SHOW_ON_GUI))
				subscriptionClass.setShowOnGUI(map.get(SHOW_ON_GUI)
						.equalsIgnoreCase(YES));

			subscriptionClass.setSubscriptionClass(map.get(ID));
			subscriptionClass.setAmount(map.get(AMOUNT));
			subscriptionClass.setPeriod(map.get(PERIOD));
			subscriptionClass.setRenewalAmount(map.get(RENEWAL_AMOUNT));
			subscriptionClass.setRenewalPeriod(map.get(RENEWAL_PERIOD));

			list.add(subscriptionClass);
		}

		SubscriptionClass[] subscriptionClasses = list
				.toArray(new SubscriptionClass[0]);
		return subscriptionClasses;
	}

	public static ChargeClass[] getChargeClasses(Element chargeClassesElem) {
		if (chargeClassesElem == null)
			return null;

		Element contentsElem = (Element) chargeClassesElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<ChargeClass> list = new ArrayList<ChargeClass>();
		for (HashMap<String, String> map : contentsList) {
			ChargeClass chargeClass = new ChargeClass();

			if (map.containsKey(SHOW_ON_GUI))
				chargeClass.setShowOnGUI(map.get(SHOW_ON_GUI).equalsIgnoreCase(
						YES));

			chargeClass.setChargeClass(map.get(ID));
			chargeClass.setAmount(map.get(AMOUNT));
			chargeClass.setPeriod(map.get(PERIOD));
			chargeClass.setRenewalAmount(map.get(RENEWAL_AMOUNT));
			chargeClass.setRenewalPeriod(map.get(RENEWAL_PERIOD));
			chargeClass.setOperatorCode1(map.get(OPERATOR_CODE_1));

			list.add(chargeClass);
		}

		ChargeClass[] chargeClasses = list.toArray(new ChargeClass[0]);
		return chargeClasses;
	}

	public static SMSText[] getSMSTexts(Element smsTextsElem) {
		if (smsTextsElem == null)
			return null;

		Element contentsElem = (Element) smsTextsElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<SMSText> list = new ArrayList<SMSText>();
		for (HashMap<String, String> map : contentsList) {
			SMSText smsText = new SMSText();

			String type = map.get(TYPE);
			map.remove(TYPE);

			smsText.setType(type);
			smsText.setSmsConditionMap(map);

			list.add(smsText);
		}

		SMSText[] smsTexts = list.toArray(new SMSText[0]);
		return smsTexts;
	}

	public static PickOfTheDay[] getPickOfTheDays(Element pickOfTheDaysElem) {
		if (pickOfTheDaysElem == null)
			return null;

		Element contentsElem = (Element) pickOfTheDaysElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<PickOfTheDay> list = new ArrayList<PickOfTheDay>();
		for (HashMap<String, String> map : contentsList) {
			PickOfTheDay pickOfTheDay = new PickOfTheDay();

			if (map.containsKey(ID))
				pickOfTheDay.setClipID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(CATEGORY_ID))
				pickOfTheDay.setCategoryID(Integer.parseInt(map
						.get(CATEGORY_ID)));

			pickOfTheDay.setClipName(map.get(NAME));
			pickOfTheDay.setCircleID(map.get(CIRCLE_ID));
			pickOfTheDay.setUserType(map.get(USER_TYPE));
			pickOfTheDay.setProfile(map.get(PROFILE));
			pickOfTheDay.setPlayDate(map.get(PLAY_DATE));
			pickOfTheDay.setLanguage(map.get(LANGUAGE));

			list.add(pickOfTheDay);
		}

		PickOfTheDay[] pickOfTheDays = list.toArray(new PickOfTheDay[0]);
		return pickOfTheDays;
	}

	public static RBTLoginUser[] getRBTLoginUsers(Element rbtLoginUserElem) {
		if (rbtLoginUserElem == null)
			return null;

		List<RBTLoginUser> list = new ArrayList<RBTLoginUser>();
		NodeList RBTLoginUserNodeList = rbtLoginUserElem
				.getElementsByTagName(RBT_LOGIN_USER);

		for (int i = 0; i < RBTLoginUserNodeList.getLength(); i++) {
			Element loginUserElement = (Element) RBTLoginUserNodeList.item(i);
			HashMap<String, String> attributesMap = getAttributesMap(loginUserElement);

			RBTLoginUser rbtLoginUser = new RBTLoginUser();

			rbtLoginUser.setUserID(attributesMap.get(USER_ID));
			rbtLoginUser.setPassword(attributesMap.get(PASSWORD));
			rbtLoginUser.setSubscriberID(attributesMap.get(SUBSCRIBER_ID));
			rbtLoginUser.setType(attributesMap.get(TYPE));
			if (null != attributesMap.get(NEW_USER)) {
				rbtLoginUser.setNewUser(Boolean.valueOf(attributesMap
						.get(NEW_USER)));
			}

			try {
				if (attributesMap.containsKey(CREATION_TIME)) {
					SimpleDateFormat dateFormat = new SimpleDateFormat(
							"yyyyMMddHHmmss");
					rbtLoginUser.setCreationTime(dateFormat.parse(attributesMap
							.get(CREATION_TIME)));
				}
			} catch (ParseException e) {
			}

			if (attributesMap.containsKey(PASSWORD_DAYS_LEFT)) {
				rbtLoginUser.setPasswordExipryLeftDays(Integer
						.parseInt(attributesMap.get(PASSWORD_DAYS_LEFT)));
			}
			if (attributesMap.containsKey(PASSWORD_EXPIRED)) {
				rbtLoginUser.setPasswordExpired(attributesMap.get(
						PASSWORD_EXPIRED).equalsIgnoreCase("TRUE"));
			}

			attributesMap.remove(USER_ID);
			attributesMap.remove(PASSWORD);
			attributesMap.remove(SUBSCRIBER_ID);
			attributesMap.remove(TYPE);
			attributesMap.remove(CREATION_TIME);
			attributesMap.remove(PASSWORD_DAYS_LEFT);
			attributesMap.remove(PASSWORD_EXPIRED);

			rbtLoginUser.setUserInfo(attributesMap);

			list.add(rbtLoginUser);
		}

		RBTLoginUser[] rbtLoginUsers = list.toArray(new RBTLoginUser[0]);
		return rbtLoginUsers;
	}

	public static Site[] getSites(Element siteElem) {
		if (siteElem == null)
			return null;

		Element contentsElem = (Element) siteElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<Site> list = new ArrayList<Site>();
		for (HashMap<String, String> map : contentsList) {
			Site site = new Site();

			if (map.containsKey(ACCESS_ALLOWED))
				site.setAccessAllowed(map.get(ACCESS_ALLOWED).equalsIgnoreCase(
						YES));

			site.setCircleID(map.get(ID));
			site.setSiteName(map.get(NAME));
			site.setSiteURL(map.get(SITE_URL));
			site.setPlayerURL(map.get(PLAYER_URL));
			site.setPlayUnchargedFor(map.get(PLAY_UNCHARGED_FOR));

			if (map.containsKey(SITE_PREFIX)) {
				String[] sitePrefixes = map.get(SITE_PREFIX).split(",");
				site.setSitePrefixes(sitePrefixes);
			}

			if (map.containsKey(SUPPORTED_LANGUAGES)) {
				String[] supportedLanguages = map.get(SUPPORTED_LANGUAGES)
						.split(",");
				site.setSupportedLanguages(supportedLanguages);
			}

			list.add(site);
		}

		Site[] sites = list.toArray(new Site[0]);
		return sites;
	}

	public static ChargeSms[] getChargeSmses(Element chargeSmsesElem) {
		if (chargeSmsesElem == null)
			return null;

		Element contentsElem = (Element) chargeSmsesElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<ChargeSms> list = new ArrayList<ChargeSms>();
		for (HashMap<String, String> map : contentsList) {
			ChargeSms chargeSms = new ChargeSms();

			chargeSms.setClassName(map.get(ID));
			chargeSms.setClassType(map.get(TYPE));
			chargeSms.setPrepaidSuccessSms(map.get(PREPAID_SUCCESS_SMS));
			chargeSms.setPrepaidFailureSms(map.get(PREPAID_FAILURE_SMS));
			chargeSms.setPostpaidSuccessSms(map.get(POSTPAID_SUCCESS_SMS));
			chargeSms.setPostpaidFailureSms(map.get(POSTPAID_FAILURE_SMS));
			chargeSms.setPrepaidNEFSuccessSms(map.get(PREPAID_NEF_SUCCESS_SMS));
			chargeSms.setPrepaidRenewalSuccessSms(map
					.get(PREPAID_RENEWAL_SUCCESS_SMS));
			chargeSms.setPrepaidRenewalFailureSms(map
					.get(PREPAID_RENEWAL_FAILURE_SMS));
			chargeSms.setPostpaidRenewalSuccessSms(map
					.get(POSTPAID_RENEWAL_SUCCESS_SMS));
			chargeSms.setPostpaidRenewalFailureSms(map
					.get(POSTPAID_RENEWAL_FAILURE_SMS));

			list.add(chargeSms);
		}

		ChargeSms[] chargeSmses = list.toArray(new ChargeSms[0]);
		return chargeSmses;
	}

	public static Cos[] getCoses(Element cosesElem) {
		if (cosesElem == null)
			return null;

		Element contentsElem = (Element) cosesElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<Cos> list = new ArrayList<Cos>();
		for (HashMap<String, String> map : contentsList) {
			Cos cos = new Cos();

			if (map.containsKey(ID))
				cos.setCosID(Integer.parseInt(map.get(ID)));
			if (map.containsKey(VALID_DAYS))
				cos.setValidDays(Integer.parseInt(map.get(VALID_DAYS)));
			if (map.containsKey(FREE_SONGS))
				cos.setNoOfFreeSongs(Integer.parseInt(map.get(FREE_SONGS)));
			if (map.containsKey(FREE_MUSICBOXES))
				cos.setNoOfFreeMusicboxes(Integer.parseInt(map
						.get(FREE_MUSICBOXES)));
			if (map.containsKey(IS_DEFAULT))
				cos.setDefault(map.get(IS_DEFAULT).equalsIgnoreCase(YES));

			cos.setCirlceID(map.get(CIRCLE_ID));
			cos.setUserType(map.get(USER_TYPE));
			cos.setSubscriptionClass(map.get(SUBSCRIPTION_CLASS));
			cos.setChargeClass(map.get(CHARGE_CLASS));
			cos.setPromoClips(map.get(PROMO_CLIPS));
			cos.setAccessMode(map.get(ACCESS_MODE));
			cos.setSmsKeyword(map.get(SMS_KEYWORDS));
			cos.setOperatorCode(map.get(OPERATOR_CODE));

			try {
				if (map.containsKey(START_DATE))
					cos.setStartDate(dateFormat.parse(map.get(START_DATE)));
				if (map.containsKey(END_DATE))
					cos.setEndDate(dateFormat.parse(map.get(END_DATE)));
			} catch (ParseException e) {

			}

			list.add(cos);
		}

		Cos[] coses = list.toArray(new Cos[0]);
		return coses;
	}

	public static Retailer[] getRetailers(Element retailerElem) {
		if (retailerElem == null)
			return null;

		Element contentsElem = (Element) retailerElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<Retailer> list = new ArrayList<Retailer>();
		for (HashMap<String, String> map : contentsList) {
			Retailer retailer = new Retailer();

			retailer.setRetailerID(map.get(ID));
			retailer.setName(map.get(NAME));
			retailer.setType(map.get(TYPE));

			list.add(retailer);
		}

		Retailer[] retailers = list.toArray(new Retailer[0]);
		return retailers;
	}

	public static FeedStatus[] getFeedStatuses(Element feedStatusesElem) {
		if (feedStatusesElem == null)
			return null;

		Element contentsElem = (Element) feedStatusesElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<FeedStatus> list = new ArrayList<FeedStatus>();
		for (HashMap<String, String> map : contentsList) {
			FeedStatus feedStatus = new FeedStatus();

			feedStatus.setType(map.get(TYPE));
			feedStatus.setStatus(map.get(STATUS));
			feedStatus.setFeedFile(map.get(FEED_FILE));
			feedStatus.setSmsKeywords(map.get(SMS_KEYWORDS));
			feedStatus.setSubKeywords(map.get(SUB_KEYWORDS));
			feedStatus.setFeedOnSuccessSms(map.get(FEED_ON_SUCCESS_SMS));
			feedStatus.setFeedOnFailureSms(map.get(FEED_ON_FAILURE_SMS));
			feedStatus.setFeedOffSuccessSms(map.get(FEED_OFF_SUCCESS_SMS));
			feedStatus.setFeedOffFailureSms(map.get(FEED_OFF_FAILURE_SMS));
			feedStatus.setFeedFailureSms(map.get(FEED_FAILURE_SMS));
			feedStatus.setFeedNonActiveUserSms(map
					.get(FEED_NON_ACTIVE_USER_SMS));

			list.add(feedStatus);
		}

		FeedStatus[] feedStatuses = list.toArray(new FeedStatus[0]);
		return feedStatuses;
	}

	public static Feed[] getFeeds(Element feedDetailsElem) {
		if (feedDetailsElem == null)
			return null;

		Element contentsElem = (Element) feedDetailsElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<Feed> list = new ArrayList<Feed>();
		for (HashMap<String, String> map : contentsList) {
			Feed feed = new Feed();

			feed.setFeedKeyword(map.get(ID));
			feed.setFeedName(map.get(NAME));
			feed.setFeedType(map.get(TYPE));
			feed.setChargeClass(map.get(CHARGE_CLASS));
			feed.setFeedOnSuccessSms(map.get(FEED_ON_SUCCESS_SMS));
			feed.setFeedOnFailureSms(map.get(FEED_ON_FAILURE_SMS));

			try {
				if (map.containsKey(START_DATE))
					feed.setStartDate(dateFormat.parse(map.get(START_DATE)));
				if (map.containsKey(END_DATE))
					feed.setEndDate(dateFormat.parse(map.get(END_DATE)));
			} catch (ParseException e) {

			}

			list.add(feed);
		}

		Feed[] feeds = list.toArray(new Feed[0]);
		return feeds;
	}

	public static PredefinedGroup[] getPredefinedGroups(
			Element predefinedGroupsElem) {
		if (predefinedGroupsElem == null)
			return null;

		Element contentsElem = (Element) predefinedGroupsElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<PredefinedGroup> list = new ArrayList<PredefinedGroup>();
		for (HashMap<String, String> map : contentsList) {
			PredefinedGroup predefinedGroup = new PredefinedGroup();

			predefinedGroup.setGroupID(map.get(ID));
			predefinedGroup.setGroupName(map.get(NAME));
			predefinedGroup.setGroupNamePrompt(map.get(GROUP_NAME_PROMPT));

			list.add(predefinedGroup);
		}

		PredefinedGroup[] predefinedGroups = list
				.toArray(new PredefinedGroup[0]);
		return predefinedGroups;
	}

	public static ViralData[] getViralData(Element viralDataElem) {
		if (viralDataElem == null)
			return null;

		Element contentsElem = (Element) viralDataElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<ViralData> list = new ArrayList<ViralData>();
		for (HashMap<String, String> map : contentsList) {
			ViralData viralData = new ViralData();

			if (map.containsKey(COUNT))
				viralData.setCount(Integer.parseInt(map.get(COUNT)));

			viralData.setSmsID(Long.parseLong(map.get(SMS_ID)));
			viralData.setSubscriberID(map.get(SUBSCRIBER_ID));
			viralData.setCallerID(map.get(CALLER_ID));
			viralData.setType(map.get(TYPE));
			viralData.setClipID(map.get(CLIP_ID));
			viralData.setSelectedBy(map.get(SELECTED_BY));

			try {
				if (map.containsKey(SENT_TIME))
					viralData.setSentTime(dateFormat.parse(map.get(SENT_TIME)));
				if (map.containsKey(SET_TIME))
					viralData.setSetTime(dateFormat.parse(map.get(SET_TIME)));
			} catch (ParseException e) {

			}

			if (map.containsKey(INFO)) {
				Document document = getDocumentFromString(map.get(INFO));
				if (document != null) {
					HashMap<String, String> infoMap = getAttributesMap(document
							.getDocumentElement());
					viralData.setInfoMap(infoMap);
				}
			}

			list.add(viralData);
		}

		ViralData[] viralData = list.toArray(new ViralData[0]);
		return viralData;
	}

	public static PendingConfirmationsRemainder[] getPendingConfirmationsRemainderData(
			Element pendingConfirmationsRemainderEle) {
		if (pendingConfirmationsRemainderEle == null)
			return null;

		Element contentsElem = (Element) pendingConfirmationsRemainderEle
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<PendingConfirmationsRemainder> list = new ArrayList<PendingConfirmationsRemainder>();
		for (HashMap<String, String> map : contentsList) {
			PendingConfirmationsRemainder pendingConfirmationsRemainder = new PendingConfirmationsRemainder();

			pendingConfirmationsRemainder.setSubscriberId(map
					.get(SUBSCRIBER_ID));
			pendingConfirmationsRemainder.setRemaindersLeft(Integer
					.parseInt(map.get(REMINDERS_LEFT)));
			pendingConfirmationsRemainder.setRemainderText(map
					.get(REMINDER_TEXT));
			pendingConfirmationsRemainder.setSender(map.get(SENDER));
			pendingConfirmationsRemainder.setSmsId(Long.parseLong(map
					.get(SMS_ID)));

			try {
				if (map.containsKey(LAST_REMINDER_SENT)) {
					pendingConfirmationsRemainder
							.setLastRemainderSent(dateFormat.parse(map
									.get(LAST_REMINDER_SENT)));
				}
				if (map.containsKey(SMS_RECEIVED_TIME)) {
					pendingConfirmationsRemainder.setSmsReceivedTime(dateFormat
							.parse(map.get(SMS_RECEIVED_TIME)));
				}
			} catch (ParseException e) {

			}

			list.add(pendingConfirmationsRemainder);
		}

		PendingConfirmationsRemainder[] remainderSmsDataArr = list
				.toArray(new PendingConfirmationsRemainder[0]);
		return remainderSmsDataArr;
	}

	public static TransData[] getTransData(Element transDataElem) {
		if (transDataElem == null)
			return null;

		Element contentsElem = (Element) transDataElem.getElementsByTagName(
				CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<TransData> list = new ArrayList<TransData>();
		for (HashMap<String, String> map : contentsList) {
			TransData transData = new TransData();

			transData.setTransID(map.get(ID));
			transData.setType(map.get(TYPE));
			transData.setSubscriberID(map.get(SUBSCRIBER_ID));
			transData.setAccessCount(map.get(CALLER_ID));

			try {
				if (map.containsKey(DATE))
					transData.setDate(dateFormat.parse(map.get(DATE)));
			} catch (ParseException e) {

			}

			list.add(transData);
		}

		TransData[] transData = list.toArray(new TransData[0]);
		return transData;
	}

	public static RbtSupport[] getRbtSupportData(Element rbtSupportDataElem) {
		if (rbtSupportDataElem == null)
			return null;

		Element contentsElem = (Element) rbtSupportDataElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<RbtSupport> list = new ArrayList<RbtSupport>();
		for (HashMap<String, String> map : contentsList) {
			RbtSupport rbtSupport = new RbtSupport();

			rbtSupport.setId(Long.parseLong(map.get(ID)));
			rbtSupport.setSubscriberId(Long.parseLong(map.get(SUBSCRIBER_ID)));
			rbtSupport.setCallerId(Long.parseLong(map.get(CALLER_ID)));
			rbtSupport.setClipId(Integer.parseInt(map.get(CLIP_ID)));
			rbtSupport.setExtraInfo(map.get(INFO));
			rbtSupport.setType(Integer.parseInt(map.get(TYPE)));
			try {
				if (map.containsKey(DATE))
					rbtSupport.setRequestDate(dateFormat.parse(map.get(DATE)));
			} catch (ParseException e) {

			}

			list.add(rbtSupport);
		}

		RbtSupport[] rbtSupportData = list.toArray(new RbtSupport[0]);
		return rbtSupportData;
	}

	public static BulkTask[] getBulkTasks(Element bulkTaskElem) {
		if (bulkTaskElem == null)
			return null;

		List<HashMap<String, String>> contentsList = getContentsList(bulkTaskElem);

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		List<BulkTask> bulkList = new ArrayList<BulkTask>();

		for (HashMap<String, String> map : contentsList) {
			BulkTask bulkTask = new BulkTask();

			if (map.containsKey(BULK_TASK_ID))
				bulkTask.setTaskId(Integer.parseInt(map.get(BULK_TASK_ID)));
			if (map.containsKey(SELECTION_TYPE))
				bulkTask.setSelectionType(Integer.parseInt(map
						.get(SELECTION_TYPE)));
			if (map.containsKey(BULK_TASK_STATUS))
				bulkTask.setTaskStatus(Integer.parseInt(map
						.get(BULK_TASK_STATUS)));

			bulkTask.setTaskName(map.get(BULK_TASK_NAME));
			bulkTask.setCircleId(map.get(CIRCLE_ID));
			bulkTask.setActivationClass(map.get(SUBSCRIPTION_CLASS));
			bulkTask.setSelectionClass(map.get(CHARGE_CLASS));
			bulkTask.setTaskType(map.get(BULK_TASK_TYPE));
			bulkTask.setActivatedBy(map.get(ACTIVATED_BY));
			bulkTask.setActInfo(map.get(ACTIVATION_INFO));
			bulkTask.setTaskMode(map.get(BULK_TASK_MODE));

			try {
				if (map.containsKey(BULK_UPLOAD_TIME))
					bulkTask.setUploadTime(dateFormat.parse(map
							.get(BULK_UPLOAD_TIME)));
				if (map.containsKey(BULK_PROCESS_TIME))
					bulkTask.setProcessTime(dateFormat.parse(map
							.get(BULK_PROCESS_TIME)));
				if (map.containsKey(BULK_END_TIME))
					bulkTask.setEndTime(dateFormat.parse(map.get(BULK_END_TIME)));
			} catch (ParseException e) {

			}

			if (map.containsKey(BULK_TASK_INFO)) {
				Document document = getDocumentFromString(map
						.get(BULK_TASK_INFO));
				if (document != null) {
					HashMap<String, String> taskInfoMap = getAttributesMap(document
							.getDocumentElement());
					bulkTask.setTaskInfo(taskInfoMap);
				}
			}

			bulkList.add(bulkTask);
		}

		BulkTask[] bulkTasks = bulkList.toArray(new BulkTask[0]);
		return bulkTasks;
	}

	public static BiDownloadHistory[] getBIDownloadHistory(
			Element biDownloadHistoryElem) {
		if (biDownloadHistoryElem == null)
			return null;

		Element contentsElem = (Element) biDownloadHistoryElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		List<BiDownloadHistory> list = new ArrayList<BiDownloadHistory>();
		for (HashMap<String, String> map : contentsList) {
			BiDownloadHistory biDownloadHistory = new BiDownloadHistory();

			biDownloadHistory.setPromoId(map.get(VALUE));

			list.add(biDownloadHistory);
		}

		BiDownloadHistory[] biDownloadHistories = list
				.toArray(new BiDownloadHistory[0]);
		return biDownloadHistories;
	}

	public static List<String> getBiRespose(Element biResponseElement) {
		List<String> retList = new ArrayList<String>();
		NodeList nodeList = biResponseElement.getChildNodes();
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			Node node = nodeList.item(i);
			String name = node.getNodeName();
			if (!name.startsWith("rc")) {
				continue;
			}
			retList.add(node.getTextContent());
		}
		return retList;
	}

	public static List<String> getBiReRespose(Document document) {
		List<String> retList = new ArrayList<String>();

		Element contentElem = (Element) document
				.getElementsByTagName("content").item(0);
		String clipIDsStr = contentElem.getAttribute("id");
		if (clipIDsStr != null && clipIDsStr.trim().length() > 0) {
			String[] contentIDs = clipIDsStr.split(",");
			for (String contentID : contentIDs) {
				retList.add(contentID);
			}
		}
		return retList;
	}

	public static Map<Integer, ClipRating> getClipRatings(
			Element clipRatingsElem) {
		Map<Integer, ClipRating> clipRatings = new LinkedHashMap<Integer, ClipRating>();

		if (clipRatingsElem == null)
			return clipRatings;

		NodeList clipRatingNodes = clipRatingsElem
				.getElementsByTagName(CLIP_RATING);
		for (int i = 0; i < clipRatingNodes.getLength(); i++) {
			Element clipRatingElem = (Element) clipRatingNodes.item(i);
			HashMap<String, String> attributesMap = getAttributesMap(clipRatingElem);

			ClipRating clipRating = new ClipRating();
			int clipID = Integer.parseInt(attributesMap.get(CLIP_ID));
			clipRating.setClipId(clipID);
			clipRating.setNoOfVotes(Integer.parseInt(attributesMap
					.get(NO_OF_VOTES)));
			clipRating.setSumOfRatings(Integer.parseInt(attributesMap
					.get(SUM_OF_RATINGS)));
			clipRating.setLikeVotes(Integer.parseInt(attributesMap
					.get(LIKE_VOTES)));
			clipRating.setDislikeVotes(Integer.parseInt(attributesMap
					.get(DISLIKE_VOTES)));
			clipRating.setNoOfDownloads(Integer.parseInt(attributesMap
					.get(NO_OF_DOWNLOADS)));

			clipRatings.put(clipID, clipRating);
		}

		return clipRatings;
	}

	public static HashMap<String, String> getAttributesMap(Element element) {
		HashMap<String, String> attributeMap = new HashMap<String, String>();

		if (element != null) {
			NamedNodeMap namedNodeMap = element.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++) {
				Attr attr = (Attr) namedNodeMap.item(i);
				attributeMap.put(attr.getName(), attr.getValue());
			}
		}

		return attributeMap;
	}

	public static List<HashMap<String, String>> getContentsList(Element element) {
		List<HashMap<String, String>> contentsList = new ArrayList<HashMap<String, String>>();

		if (element != null) {
			NodeList contentNodeList = element.getElementsByTagName(CONTENT);

			for (int i = 0; i < contentNodeList.getLength(); i++) {
				Element contentElement = (Element) contentNodeList.item(i);
				HashMap<String, String> propertiesMap = getAttributesMap(contentElement);
				propertiesMap.putAll(getPropertiesMap(contentElement));
				contentsList.add(propertiesMap);
			}
		}

		return contentsList;
	}

	public static HashMap<String, String> getPropertiesMap(Element element) {
		HashMap<String, String> propertiesMap = new HashMap<String, String>();

		if (element != null) {
			NodeList propertyNodeList = element.getElementsByTagName(PROPERTY);
			for (int i = 0; i < propertyNodeList.getLength(); i++) {
				Element propertyElement = (Element) propertyNodeList.item(i);
				String key = propertyElement.getAttribute(NAME);
				String value = propertyElement.getAttribute(VALUE);

				propertiesMap.put(key, value);
			}
		}

		return propertiesMap;
	}

	synchronized private static Document getDocumentFromString(String xml) {
		if (xml == null || xml.equals(""))
			return null;

		Document document = null;
		try {
			if (documentBuilder == null)
				documentBuilder = DocumentBuilderFactory.newInstance()
						.newDocumentBuilder();

			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					xml.trim().getBytes("UTF-8"));
			document = documentBuilder.parse(byteArrayInputStream);
		} catch (ParserConfigurationException e) {
		} catch (SAXException e) {
		} catch (IOException e) {
		}

		return document;
	}

	public static Map<Integer, ChargeClass> getNextChargeClasses(
			Element chargeClassesElem) {
		Map<Integer, ChargeClass> chargeClasses = new LinkedHashMap<Integer, ChargeClass>();

		if (chargeClassesElem == null)
			return chargeClasses;

		Element contentsElem = (Element) chargeClassesElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		for (Map<String, String> map : contentsList) {
			ChargeClass chargeClass = new ChargeClass();
			if (map.containsKey(SHOW_ON_GUI))
				chargeClass.setShowOnGUI(map.get(SHOW_ON_GUI).equalsIgnoreCase(
						YES));

			chargeClass.setChargeClass(map.get(ID));
			chargeClass.setAmount(map.get(AMOUNT));
			chargeClass.setPeriod(map.get(PERIOD));
			chargeClass.setRenewalAmount(map.get(RENEWAL_AMOUNT));
			chargeClass.setRenewalPeriod(map.get(RENEWAL_PERIOD));
			chargeClass.setOperatorCode1(map.get(OPERATOR_CODE_1));
			chargeClass.setSubscriptionAmount(map.get(SUBSCRIPTION_AMOUNT));
			chargeClass.setSubscriptionPeriod(map.get(SUBSCRIPTION_PERIOD));

			if (map.containsKey(CLIP_ID))
				chargeClasses.put(Integer.parseInt(map.get(CLIP_ID)),
						chargeClass);
			else
				chargeClasses.put(null, chargeClass);
		}

		return chargeClasses;
	}

	public static Map<String, ChargeClass> getNextChargeClassesForRMO(
			Element chargeClassesElem) {
		Map<String, ChargeClass> chargeClasses = new LinkedHashMap<String, ChargeClass>();

		if (chargeClassesElem == null)
			return chargeClasses;

		Element contentsElem = (Element) chargeClassesElem
				.getElementsByTagName(CONTENTS).item(0);
		List<HashMap<String, String>> contentsList = getContentsList(contentsElem);

		for (Map<String, String> map : contentsList) {
			ChargeClass chargeClass = new ChargeClass();
			if (map.containsKey(SHOW_ON_GUI))
				chargeClass.setShowOnGUI(map.get(SHOW_ON_GUI).equalsIgnoreCase(
						YES));

			chargeClass.setChargeClass(map.get(ID));
			chargeClass.setAmount(map.get(AMOUNT));
			chargeClass.setPeriod(map.get(PERIOD));
			chargeClass.setRenewalAmount(map.get(RENEWAL_AMOUNT));
			chargeClass.setRenewalPeriod(map.get(RENEWAL_PERIOD));
			chargeClass.setOperatorCode1(map.get(OPERATOR_CODE_1));

			if (map.containsKey(CLIP_ID))
				chargeClasses.put(map.get(CLIP_ID), chargeClass);
			else
				chargeClasses.put(null, chargeClass);
		}

		return chargeClasses;
	}

	public static SubscriberPack[] getSubscriberPacks(Element element) {
		List<HashMap<String, String>> contentsList = new ArrayList<HashMap<String, String>>();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		if (element != null) {
			NodeList contentNodeList = element
					.getElementsByTagName(SUBSCRIBERPACK);

			for (int i = 0; i < contentNodeList.getLength(); i++) {
				Element contentElement = (Element) contentNodeList.item(i);
				HashMap<String, String> propertiesMap = getAttributesMap(contentElement);
				propertiesMap.putAll(getPropertiesMap(contentElement));
				contentsList.add(propertiesMap);
			}
		}

		List<SubscriberPack> list = new ArrayList<SubscriberPack>();
		for (HashMap<String, String> map : contentsList) {
			SubscriberPack subscriberPack = new SubscriberPack();

            subscriberPack.setPackChargeClass(map.get(PACK_CHARGE_CLASS));
            subscriberPack.setCosId(map.get(PACK_COS_ID));
            subscriberPack.setCosType(map.get(PACK_COS_TYPE));
            subscriberPack.setPackMode(map.get(PACK_MODE));
            subscriberPack.setPackModeInfo(map.get(PACK_MODE_INFO));
            subscriberPack.setIntRefId(map.get(PACK_REF_ID));
            subscriberPack.setStatus(map.get(PACK_STATUS));
            subscriberPack.setLastTransactionType(map.get(LAST_TRANSACTION_TYPE));
            subscriberPack.setNumMaxSelections(Integer.parseInt(map
                    .get(PACK_NUM_MAX_SELECTIONS)));
            try {
                subscriberPack.setCreationTime(sdf.parse(map
                        .get(PACK_START_TIME)));
                if (map.containsKey(PACK_DEACTIVATE_TIME))
                    subscriberPack.setDeactivateDate(sdf.parse(map
                            .get(PACK_DEACTIVATE_TIME)));
                if (map.containsKey(PACK_LAST_CHARGING_TIME))
                    subscriberPack.setLastChargingDate(sdf.parse(map
                            .get(PACK_LAST_CHARGING_TIME)));
                if (map.containsKey(NEXT_BILLING_DATE))
                    subscriberPack.setNextChargingDate(sdf.parse(map
                            .get(NEXT_BILLING_DATE)));
            } catch (ParseException e) {}
            subscriberPack.setDeactivateMode(map.get(PACK_DEACTIVATE_MODE));
            subscriberPack.setDeactivateModeInfo(map
                    .get(PACK_DEACTIVATE_MODE_INFO));
            if(map.containsKey(LAST_CHARGE_AMOUNT)){
            	subscriberPack.setAmountCharged(map.get(LAST_CHARGE_AMOUNT));
            }
            list.add(subscriberPack);
        }

		SubscriberPack[] subscriberPacks = list.toArray(new SubscriberPack[0]);
		return subscriberPacks;
	}

	public static List<String> getSearchResponse(Document document) {
		List<String> clipIdList = new ArrayList<String>();

		if (document != null) {
			NodeList contentNodes = document.getElementsByTagName("content");
			if (null != contentNodes) {
				int noOfNodes = contentNodes.getLength();
				Element contentElem;
				String clipId;
				for (int index = 0; index < noOfNodes; index++) {
					contentElem = (Element) contentNodes.item(index);
					// Add clip to list
					clipId = contentElem.getAttribute("id");
					clipIdList.add(clipId);
				}
			}
		}

		return clipIdList;
	}

	public static NextServiceCharge getNextServiceChargeClass(
			Element nextServiceChargeElem) {
		NextServiceCharge nextServiceCharge = new NextServiceCharge();
		NewChargeClass newChargeClass = null ;
		NewSubscriptionClass newSubscriptionClass = null;

		if (nextServiceChargeElem == null)
			return nextServiceCharge;

		Element contentsElem = (Element) nextServiceChargeElem
				.getElementsByTagName(CONTENTS).item(0);
		Element newChargeeClassElem = (Element) contentsElem
				.getElementsByTagName(NEWCHARGE_CLASS).item(0);
		Element newSubClassElem = (Element) nextServiceChargeElem
				.getElementsByTagName(NEWSUBSCRIPTION_CLASS).item(0);

		if (newChargeeClassElem != null) {
			newChargeClass = new NewChargeClass();
			newChargeClass.setAmount(newChargeeClassElem.getAttribute(AMOUNT));
			newChargeClass
					.setIsRenewal(Boolean.parseBoolean(newChargeeClassElem
							.getAttribute(IS_RENEWAL)));
			newChargeClass.setOfferID(Integer.parseInt(newChargeeClassElem
					.getAttribute(OFFER_ID)));
			newChargeClass.setRenewalAmount(newChargeeClassElem
					.getAttribute(RENEWAL_AMOUNT));
			newChargeClass.setRenewalValidity(newChargeeClassElem
					.getAttribute(RENEWAL_VALIDITY));
			newChargeClass.setServiceKey(newChargeeClassElem
					.getAttribute(SERVICE_KEY));
			newChargeClass.setValiditiy(newChargeeClassElem
					.getAttribute(VALIDITY));

		}

		if (newSubClassElem != null) {
			newSubscriptionClass = new NewSubscriptionClass(); 
			newSubscriptionClass
					.setAmount(newSubClassElem.getAttribute(AMOUNT));
			newSubscriptionClass.setIsRenewal(Boolean
					.parseBoolean(newSubClassElem.getAttribute(IS_RENEWAL)));
			newSubscriptionClass.setOfferID(Integer.parseInt(newSubClassElem
					.getAttribute(OFFER_ID)));
			newSubscriptionClass.setRenewalAmount(newSubClassElem
					.getAttribute(RENEWAL_AMOUNT));
			newSubscriptionClass.setRenewalValidity(newSubClassElem
					.getAttribute(RENEWAL_VALIDITY));
			newSubscriptionClass.setServiceKey(newSubClassElem
					.getAttribute(SERVICE_KEY));
			newSubscriptionClass.setValiditiy(newSubClassElem
					.getAttribute(VALIDITY));

		}

		nextServiceCharge.setChargeClass(newChargeClass);
		nextServiceCharge.setSubscriptionClass(newSubscriptionClass);

		return nextServiceCharge;
	}
}
