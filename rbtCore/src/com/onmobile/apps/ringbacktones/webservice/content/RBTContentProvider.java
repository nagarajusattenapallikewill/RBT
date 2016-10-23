/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 *
 */
public interface RBTContentProvider
{
	public String getContentXML(WebServiceContext task);
	public String addContent(WebServiceContext task);

	public Element getCategoryContentElement(Document document, Category parentCategory, Category category);
	public Element getClipContentElement(Document document, Category parentCategory, Clip clip);
	public Element getCategoryContentElement(Document document, Category parentCategory, Category category,WebServiceContext task);
	public Element getClipContentElement(Document document, Category parentCategory, Clip clip,WebServiceContext task);
	public String getRecommendationMusicFromThirdParty(WebServiceContext task);
	public String getCircleTopTen(WebServiceContext task);
	public String getSearchContentXML(WebServiceContext task);
	public String getClipCategoryFromMecache(WebServiceContext task);
}
