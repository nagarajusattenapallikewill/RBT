/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tatagsm;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation;

/**
 * @author vinayasimha.patil
 *
 */
public class TataGSMRBTInformation extends BasicRBTInformation
{
	/**
	 * @throws ParserConfigurationException
	 */
	public TataGSMRBTInformation() throws ParserConfigurationException
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

		Element libraryElem = (Element) document.getElementsByTagName(LIBRARY).item(0);
		if (libraryElem != null)
		{
			Attr nextChargeClassAttr = libraryElem.getAttributeNode(NEXT_SELECTION_AMOUNT);
			if (nextChargeClassAttr == null)
			{
				Element subscriberElem = (Element)document.getElementsByTagName(SUBSCRIBER).item(0);
				String subscriberStatus = subscriberElem.getAttribute(STATUS);

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

				if (nextChargeClass != null && !nextChargeClass.equalsIgnoreCase("DEFAULT"))
				{
					ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
					libraryElem.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
				}
			}

			Attr isAlbumUserAttr = libraryElem.getAttributeNode(IS_ALBUM_USER);
			if (isAlbumUserAttr == null)
				libraryElem.setAttribute(IS_ALBUM_USER, NO);
		}

		return document;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getSubscriberLibraryElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected Element getSubscriberLibraryElement(Document document, WebServiceContext task)
	{
		Element element = super.getSubscriberLibraryElement(document, task);

		String isAlbumUser = NO;
		Element settingsElem = (Element) element.getElementsByTagName(SETTINGS).item(0);
		if (settingsElem != null)
		{
			HashMap<String, Integer> callerMap = new HashMap<String, Integer>();

			NodeList contentNodeList = settingsElem.getChildNodes().item(0).getChildNodes();
			for (int i = 0; i < contentNodeList.getLength(); i++)
			{
				Element contentElem = (Element) contentNodeList.item(i);
				NodeList propertyNodeList = contentElem.getChildNodes();

				String callerID = ((Element)propertyNodeList.item(0)).getAttribute(VALUE);
				int loopIndex = 0; 
				if (callerMap.containsKey(callerID))
					loopIndex = callerMap.get(callerID);
				loopIndex++;
				callerMap.put(callerID, loopIndex);

				Utility.addPropertyElement(document, contentElem, LOOP_INDEX, DATA, String.valueOf(loopIndex));

				if (contentElem.getAttribute(TYPE).equalsIgnoreCase(CATEGORY_SHUFFLE))
					isAlbumUser = YES;
			}
		}
		element.setAttribute(IS_ALBUM_USER, isAlbumUser);

		Subscriber subscriber = null;
		if (task.containsKey(param_subscriber))
			subscriber = (Subscriber) task.get(param_subscriber);
		else
			subscriber = rbtDBManager.getSubscriber(task.getString(param_subscriberID));

		String nextChargeClass = rbtDBManager.getNextChargeClass(subscriber);
		if (nextChargeClass != null && !nextChargeClass.equalsIgnoreCase("DEFAULT"))
		{
			ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(nextChargeClass);
			element.setAttribute(NEXT_SELECTION_AMOUNT, chargeClass.getAmount());
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#getCallDetailsElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.webservice.common.Task, com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber)
	 */
	@Override
	protected Element getCallDetailsElement(Document document, WebServiceContext task,
			WebServiceSubscriber webServiceSubscriber, Subscriber subscriber)
	{
		Element element = TataGSMXMLElementGenerator.generateCallDetailsElement(document, task, webServiceSubscriber, subscriber);
		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTInformation#canBeGifted(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	protected String canBeGifted(String subscriberID, String callerID, String contentID)
	{
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(callerID, "GIFT"));
		if (subscriberDetail != null && subscriberDetail.isValidSubscriber())
			return VALID;

		return INVALID;
	}
}
