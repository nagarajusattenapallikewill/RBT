/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tata;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider;

/**
 * @author vinayasimha.patil
 *
 */
public class TataRBTContentProvider extends BasicRBTContentProvider
{
	/**
	 * @throws ParserConfigurationException
	 */
	public TataRBTContentProvider() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider#getClip(java.lang.String)
	 */
	@Override
	protected Clip getClip(String contentID, WebServiceContext task)
	{
		String browsingLanguage = task.getString(param_browsingLanguage);
		
		if (contentID != null && contentID.toUpperCase().startsWith("WT"))
			return rbtCacheManager.getClipBySMSAlias(contentID.substring(2), browsingLanguage);

		return super.getClip(contentID, task);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider#getClipContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	public Element getClipContentElement(Document document, Category parentCategory, Clip clip, WebServiceContext task)
	{
		Element element = super.getClipContentElement(document, parentCategory, clip, task);

		String amount = "0";
		String validity = "";

		String parentClassType = "DEFAULT";
		if (parentCategory != null)
			parentClassType = parentCategory.getClassType();

		String childClassType = clip.getClassType();

		ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
		if (chargeClass != null)
		{
			amount = chargeClass.getAmount();
			validity = chargeClass.getSelectionPeriod();
		}

		Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);
		Utility.addPropertyElement(document, element, PERIOD, DATA, validity);

		return element;
	}
}
