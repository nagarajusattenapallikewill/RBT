/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.romania;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider;

/**
 * @author vinayasimha.patil
 *
 */
public class RomaniaRBTContentProvider extends BasicRBTContentProvider
{
	/**
	 * @throws ParserConfigurationException
	 */
	public RomaniaRBTContentProvider() throws ParserConfigurationException
	{
		super();
	}

	@Override
	public Element getCategoryContentElement(Document document, Category parentCategory, Category category, WebServiceContext task)
	{
		Element element = document.createElement(CONTENT);

		int categoryType = category.getCategoryTpe();
		String contentType = Utility.getCategoryType(categoryType);

		element.setAttribute(ID, String.valueOf(category.getCategoryId()));
		element.setAttribute(NAME, category.getCategoryName());
		element.setAttribute(TYPE, contentType);

		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		
		Utility.addPropertyElement(document, element, CATEGORY_NAME_FILE, PROMPT, Utility.getPromptName(category.getCategoryNameWavFile(), format));
		Utility.addPropertyElement(document, element, CATEGORY_PREVIEW_FILE, PROMPT, Utility.getPromptName(category.getCategoryPreviewWavFile(), format));
		Utility.addPropertyElement(document, element, CATEGORY_GREETING, PROMPT, Utility.getPromptName(category.getCategoryGreeting(), format));

		if (categoryType == 0)
		{
			String amount = "0";

			String parentClassType = "DEFAULT";
			if (parentCategory != null)
				parentClassType = parentCategory.getClassType();

			String childClassType = category.getClassType();

			ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
			if (chargeClass != null)
				amount = chargeClass.getAmount();

			Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider#getClipContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	public Element getClipContentElement(Document document, Category parentCategory, Clip clip, WebServiceContext task)
	{
		Element element = super.getClipContentElement(document, parentCategory, clip, task);

		SimpleDateFormat clipEndDateFormat = new SimpleDateFormat("ddMMyyyy");

		Date endDate = clip.getClipEndTime();
		Utility.addPropertyElement(document, element, END_DATE, DATA, clipEndDateFormat.format(endDate));

		String amount = "0";

		String parentClassType = "DEFAULT";
		if (parentCategory != null)
			parentClassType = parentCategory.getClassType();

		String childClassType = clip.getClassType();

		ChargeClass chargeClass = DataUtils.getValidChargeClass(parentClassType, childClassType);
		if (chargeClass != null)
			amount = chargeClass.getAmount();

		Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);

		return element;
	}
}
