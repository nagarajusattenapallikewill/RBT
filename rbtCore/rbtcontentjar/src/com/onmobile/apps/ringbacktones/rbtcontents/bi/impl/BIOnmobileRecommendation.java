package com.onmobile.apps.ringbacktones.rbtcontents.bi.impl;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfoAction;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class BIOnmobileRecommendation implements BIInterface{

	private static Logger logger = Logger.getLogger(BIOnmobileRecommendation.class);
	
	@Override
	public Object[] process(Category category, String subscriberId, String circleId, boolean doReturnActiveClips, String language, String appName,  boolean isFromCategory, ClipInfoAction clipInfoAction){
		String responseString = null;
		String url = RBTContentJarParameters.getInstance().getParameter("BI_URL_" + category.getCategoryTpe());
		String biUrlGenereStr = RBTContentJarParameters.getInstance().getParameter("CONFIG_BI_URL_GENERE");
		String biUrlCategoryStr = RBTContentJarParameters.getInstance().getParameter("CONFIG_BI_URL_CATEGORY");
		String biUrlClipStr = RBTContentJarParameters.getInstance().getParameter("CONFIG_BI_URL_CLIP");
		String clipCatId = String.valueOf(category.getCategoryId());
		try
		{
			if(null != subscriberId){
				url = url.replaceAll("<msisdn>", subscriberId);
				logger.info("RBT:: URL: " + url);
			}
			logger.info("Is flow is from category browsing :" + isFromCategory);
			
			if(isFromCategory) {
					url = url.replace("<contentId>", (biUrlGenereStr!=null? biUrlGenereStr:BI_URL_GENERE));
					url = url.replaceAll("<contentType>", (biUrlCategoryStr!=null? biUrlCategoryStr:BI_URL_CATEGORY));
			} else {
					url = url.replaceAll("<contentId>", (clipCatId!=null ? clipCatId:BI_URL_CLIP_CAT_ID));
					url = url.replaceAll("<contentType>", (biUrlClipStr!=null? biUrlClipStr:BI_URL_CLIP));
				}
			
			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			responseString = httpResponse.getResponse();
		}
		catch (Exception e)
		{
			logger.error("", e);
			responseString = "ERROR";
		}
		if (responseString == null)
		{
			return null;
		}
		String clipIDsOrCatIDsStr = null; 
		Document document = XMLUtils.getDocumentFromString(responseString.trim());
		if (document != null)
		{
			Element contentElem = (Element) document.getElementsByTagName("content").item(0);
			clipIDsOrCatIDsStr = contentElem.getAttribute("id");
		}
		else
		{
			logger.warn("Invalid Xml format, could not get the document from the httpResponse");
		}
		ArrayList<Clip> clipList = new ArrayList<Clip>();
		ArrayList<Category> subCategoryList = new ArrayList<Category>();
		Date now = new Date();
		if (clipIDsOrCatIDsStr != null && clipIDsOrCatIDsStr.trim().length() > 0)
		{
			String[] contentIDs = clipIDsOrCatIDsStr.split(",");
			//Date curDate = new Date();			
			// if category browsing checking flag and returning sub categories of parent catId
			if(isFromCategory) {
				for (String contentID : contentIDs)
				{	
					if(null != contentID && !contentID.isEmpty()) {
						int cetegoryObj = Integer.parseInt(contentID);
						Category subCategory = RBTCacheManager.getInstance().getCategory(cetegoryObj, language);
						logger.info("list of subcategories :" + subCategory);
						if(subCategory == null || ( !subCategory.getCategoryStartTime().before(now) || !subCategory.getCategoryEndTime().after(now) )) {
							continue;
						}
						subCategoryList.add(subCategory);
					}
					return (Category[]) subCategoryList.toArray(new Category[0]);
				}
			}
				// else clips
			else {
				for (String contentID : contentIDs) {
					Clip clip = RBTCacheManager.getInstance().getClip(contentID, language, appName);
					if(clip == null || (doReturnActiveClips && (!clip.getClipStartTime().before(now) || !clip.getClipEndTime().after(now)))) {
						continue;
					}
					clipList.add(clip);
				 }
				return (Clip[]) clipList.toArray(new Clip[0]);
			}
		}
		return null;
	}

	@Override
	public boolean processHitBIForPurchase(String subscriberId, String refId,
			String mode, String toneId) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static void main(String args[]) throws Exception{
		String className = "com.onmobile.apps.ringbacktones.rbtcontents.bi.impl.BIOnmobileRecommendation";
		BIInterface bi = (BIInterface) Class.forName(className).newInstance();
		Clip clips[] = (Clip[])bi.process(null, null, null, false, null, null,false,null);
		for(Clip clip : clips) {
			System.out.println(clip.getClipId() + "<br>");
		}


	}
}