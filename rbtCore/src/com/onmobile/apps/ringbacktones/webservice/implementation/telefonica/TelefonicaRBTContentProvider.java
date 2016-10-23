/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.telefonica;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider;

/**
 * @author vinayasimha.patil
 *
 */
public class TelefonicaRBTContentProvider extends BasicRBTContentProvider
{
	/**
	 * @throws ParserConfigurationException
	 */
	public TelefonicaRBTContentProvider() throws ParserConfigurationException
	{
		super();
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
		
		String renewalPeriod = "";
		String renewalAmount = "0";

		String parentClassType = "DEFAULT";
		if (parentCategory != null)
			parentClassType = parentCategory.getClassType();

		String childClassType = clip.getClassType();

		ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
		if (chargeClass != null)
		{
			amount = chargeClass.getAmount();
			validity = chargeClass.getSelectionPeriod();
			
			renewalPeriod = chargeClass.getRenewalPeriod();
			renewalAmount = chargeClass.getRenewalAmount();
		}

		Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);
		Utility.addPropertyElement(document, element, PERIOD, DATA, validity);
		Utility.addPropertyElement(document, element, RENEWAL_PERIOD, DATA, renewalPeriod);
		Utility.addPropertyElement(document, element, RENEWAL_AMOUNT, DATA, renewalAmount);
		
		if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_IMAGE_URL_IN_CONTENT_RESPONSE, false))
			Utility.addPropertyElement(document, element, IMAGE_URL, DATA, clip.getClipInfoMap() != null ? clip.getClipInfoMap().get(ClipInfoKeys.IMG_URL.toString()) : null);

		return element;
	}
}
