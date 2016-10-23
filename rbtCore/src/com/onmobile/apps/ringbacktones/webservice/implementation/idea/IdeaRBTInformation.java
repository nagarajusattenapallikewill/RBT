/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.idea;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberBookMark;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 *
 */
public class IdeaRBTInformation extends BasicRBTInformation
{
	private static Logger logger = Logger.getLogger(IdeaRBTInformation.class);
	/**
	 * @throws ParserConfigurationException
	 */
	public IdeaRBTInformation() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getRBTInformationDocument(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	public Document getRBTInformationDocument(WebServiceContext task)
	{
		Document document = super.getRBTInformationDocument(task);

		Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
		boolean canAllow = subscriberElem.getAttribute(CAN_ALLOW).equalsIgnoreCase(YES) ? true : false;
		boolean isValidPrefix = subscriberElem.getAttribute(IS_VALID_PREFIX).equalsIgnoreCase(YES) ? true : false;
		String subscriberStatus = subscriberElem.getAttribute(STATUS);

		boolean addBookMarks = canAllow && isValidPrefix && !(subscriberStatus.equalsIgnoreCase(DEACT_PENDING)
				|| subscriberStatus.equalsIgnoreCase(SUSPENDED));
		if (addBookMarks)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberDownloads[] bookmarks = rbtDBManager.getSubscriberBookMarks(subscriberID);
			WebServiceSubscriberBookMark[] webServiceSubscriberBookMarks = getWebServiceSubscriberBookMarkObjects(task, bookmarks);
			Element bookMarksElement = getSubscriberBookMarksElement(document, task, webServiceSubscriberBookMarks, bookmarks);

			Element subDetailsElem = (Element)document.getElementsByTagName(SUBSCRIBER_DETAILS).item(0);
			subDetailsElem.appendChild(bookMarksElement);
		}

		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr nextChargeClassAttr = libraryElem.getAttributeNode(NEXT_CHARGE_CLASS);
			if (nextChargeClassAttr == null)
			{
				Subscriber subscriber = (Subscriber) task.get(param_subscriber);
				String nextChargeClass = null;
				if (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus.equalsIgnoreCase(DEACTIVE))
				{
					CosDetails cos = DataUtils.getCos(task, subscriber);
					nextChargeClass = rbtDBManager.getChargeClassFromCos(cos, 0);
				}
				else
				{
					nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
				}

				if (nextChargeClass != null)
				{
					ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
					libraryElem.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
					libraryElem.setAttribute(NEXT_CHARGE_CLASS, chargeClass.getChargeClass());
				}
			}
		}

		return document;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	protected Element getSubscriberElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServicesubscriber, Subscriber subscriber)
	{
		Element element = super.getSubscriberElement(document, task, webServicesubscriber, subscriber);

		if (rbtDBManager.isSubscriberActive(subscriber))
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			element.setAttribute(START_DATE, dateFormat.format(webServicesubscriber.getStartDate()));
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryElement(document, task);

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(task.getString(param_subscriberID));

		String nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
		if (nextChargeClass != null)
		{
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
			element.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
			element.setAttribute(NEXT_CHARGE_CLASS, chargeClass.getChargeClass());
		}

		return element;
	}
	
	/**
	 * RBT-14278 - IVR for All Second Consent Failures.
	 * 
	 * 
	 */
	protected Element getConsentPendingRecord(Document document, WebServiceContext task){
		logger.info("Inside IdeaRBTInformation:getConsentPendingRecord()");
		
		String subscriberID = task.getString(param_subscriberID);
		String mode = task.getString(param_mode);
		
		Map<String,String> userDefinedNamesForRequestType = populateRequestType();
		
		List<DoubleConfirmationRequestBean> baseConsentPendingRecordList = null; 

		String baseSrvKey = null;
		String baseTransId = null;
		String chargeClass = null;
		String wavFileName = null;
		Integer clipID = null;
		String songName = null;
		String vcode = null;
		String requestType = null;
		Element element = null;
		
		try {
			
			//Added for TTG-14814
			List<String> modeMappingList = new ArrayList<String>();
			modeMappingList.add(mode);
			mode = Utility
					.listToStringWithQuots(modeMappingList);
			//End of TTG-14814
			
			baseConsentPendingRecordList = rbtDBManager.getConsentRecordListForStatusNMsisdnMode(
					"0",subscriberID, mode,true);
			element = document.createElement("consents");
			if(null != baseConsentPendingRecordList){
				
				for(DoubleConfirmationRequestBean doubleConfirmationRequestBean : baseConsentPendingRecordList){
					
					chargeClass = null;
					baseSrvKey = null;
					wavFileName = null;
					songName = null;
					vcode = null;
					baseTransId = null;
					mode = null;
					requestType = null;
					
					
					mode = doubleConfirmationRequestBean.getMode();
					
					requestType = doubleConfirmationRequestBean.getRequestType();
					
					if(userDefinedNamesForRequestType.containsKey(requestType)){
						requestType = userDefinedNamesForRequestType.get(requestType);
					}
					
					baseSrvKey = doubleConfirmationRequestBean.getSubscriptionClass();
					baseTransId = doubleConfirmationRequestBean.getTransId();
					chargeClass = doubleConfirmationRequestBean.getClassType();
					wavFileName = doubleConfirmationRequestBean.getWavFileName();
					
					clipID = doubleConfirmationRequestBean.getClipID();
					Clip clip =RBTCacheManager.getInstance().getClip(clipID);
					if(clip == null && wavFileName != null){
						clip =RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName);
					}
					if(clip!=null){
						songName = clip.getClipName();
						if(wavFileName.indexOf("rbt_") != -1 && wavFileName.indexOf("_rbt") != -1) {
							vcode = wavFileName.substring("rbt_".length(),wavFileName.indexOf("_rbt"));
						}
					}
					
					Element consentElement = document.createElement("consent");
					if(chargeClass!=null)
						consentElement.setAttribute("srvKey", chargeClass);
					if(baseSrvKey!=null)
					    consentElement.setAttribute("baseSrvKey", baseSrvKey);
					if(wavFileName!=null)
					    consentElement.setAttribute("wavFile", wavFileName);
					if(songName!=null)
					    consentElement.setAttribute("songName", songName);
					if(vcode!=null)
					    consentElement.setAttribute("vcode", vcode);
					if(baseTransId!=null)
					    consentElement.setAttribute("transId", baseTransId);
					if(mode != null)
						consentElement.setAttribute("mode", mode);
					if(requestType != null)
						consentElement.setAttribute("type", requestType);
					
					element.appendChild(consentElement);
				}
				
			}
			
		} catch (Exception ex) {
			logger.info("Exception while retreiving consent pending records!!");
			logger.error(ex);
		}
		
		return element;
	}

	private Map<String, String> populateRequestType() {
		String userDefinedRequestTypeStr = RBTParametersUtils.getParamAsString("COMMON",
				"USER_DEFINED_NAMES_FOR_REQUEST_TYPE", null);
		Map<String,String> userDefinedRequestTypeMap = MapUtils.convertToMap(userDefinedRequestTypeStr, ";", ":", null);
		
		logger.info("User defined Request types : "+userDefinedRequestTypeMap);
		return userDefinedRequestTypeMap;
	}
}
