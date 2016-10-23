/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.airtel;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider;

/**
 * @author vinayasimha.patil
 *
 */
public class AirtelRBTContentProvider extends BasicRBTContentProvider
{

	/**
	 * @throws ParserConfigurationException
	 */
	public AirtelRBTContentProvider() throws ParserConfigurationException
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
		Clip clip = super.getClip(contentID, task);

		if (clip == null && contentID != null)
			clip = rbtCacheManager.getClipByRbtWavFileName("rbt_" + contentID + "_rbt", browsingLanguage);

		return clip;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider#getCategoryContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category)
	 */
	@Override
	public Element getCategoryContentElement(Document document, Category parentCategory, Category category, WebServiceContext task)
	{
		Element element = super.getCategoryContentElement(document, parentCategory, category, task);
		String supportedLanguages = RBTContentJarParameters.getInstance().getParameter("supported_languages");
		if (supportedLanguages != null)
		{
			// Again getting category object for getting the information of category in all supported languages
			category = rbtCacheManager.getCategory(category.getCategoryId(), "ALL");

			String[] languages = supportedLanguages.split(",");
			for (String language : languages)
			{
				String grammar = category.getCategoryGrammar(language);
				if (grammar != null)
				{
					Element grammarElem = document.createElement(PROPERTY);
					grammarElem.setAttribute(NAME, CATEGORY_GRAMMAR);
					grammarElem.setAttribute(TYPE, GRAMMAR);
					grammarElem.setAttribute(LANGUAGE, language);
					grammarElem.setAttribute(VALUE, grammar);
					element.appendChild(grammarElem);
				}
			}
		}

		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider#getClipContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	@Override
	public Element getClipContentElement(Document document, Category parentCategory, Clip clip, WebServiceContext task)
	{
		Element element = document.createElement(CONTENT);

		element.setAttribute(ID, String.valueOf(clip.getClipId()));
		element.setAttribute(NAME, clip.getClipName());
		element.setAttribute(TYPE, CLIP);

		Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(clip.getClipPreviewWavFile()));
		Utility.addPropertyElement(document, element, PROMO_CODE, DATA,clip.getClipPromoId() );
		if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
			String wavFile = clip.getClipRbtWavFile();
			String vcode = null;
			if(wavFile!=null){
				vcode = wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
			}
		     Utility.addPropertyElement(document, element, VCODE, DATA, vcode);
		     
		}

		return element;
	}
}
