/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.esia;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider;

/**
 * @author vinayasimha.patil
 *
 */
public class EsiaRBTContentProvider extends BasicRBTContentProvider
{

	/**
	 * @throws ParserConfigurationException
	 */
	public EsiaRBTContentProvider() throws ParserConfigurationException
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider#getCategoryContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category)
	 */
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
		
		Utility.addPropertyElement(document, element, CATEGORY_NAME_FILE, PROMPT, Utility.getPromptName(category.getCategoryNameWavFile(),format));
		Utility.addPropertyElement(document, element, CATEGORY_PREVIEW_FILE, PROMPT, Utility.getPromptName(category.getCategoryPreviewWavFile(),format));
		Utility.addPropertyElement(document, element, CATEGORY_GREETING, PROMPT, Utility.getPromptName(category.getCategoryGreeting(),format));
		Utility.addPropertyElement(document, element, CATEGORY_GRAMMAR, GRAMMAR, category.getCategoryGrammar());

		return element;
	}

}
