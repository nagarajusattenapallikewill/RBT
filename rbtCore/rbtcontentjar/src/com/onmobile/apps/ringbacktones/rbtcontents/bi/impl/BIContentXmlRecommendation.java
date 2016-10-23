package com.onmobile.apps.ringbacktones.rbtcontents.bi.impl;

import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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

public class BIContentXmlRecommendation implements BIInterface{

	private static Logger logger = Logger.getLogger(BIContentXmlRecommendation.class);
	
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
		String clipIDsOrCatIDsStr = "";
		Document document = XMLUtils.getDocumentFromString(responseString.trim());
		if (document != null)
		{
			NodeList contentNodes = document.getElementsByTagName("content");
			if(null != contentNodes) {
				int noOfNodes = contentNodes.getLength();
				Element contentElem;
				String clipId, type;
				for(int index=0; index<noOfNodes; index++) {
					contentElem = (Element) contentNodes.item(index);
					type = contentElem.getAttribute("type");
					if(null != type && type.equals("clip")) {
						clipId = contentElem.getAttribute("id");
						//Add clip to list
						if(null != clipId) {
							clipIDsOrCatIDsStr += "," + clipId;
						}
					}
				}
			}
			if(clipIDsOrCatIDsStr.length() > 0) {
				clipIDsOrCatIDsStr = clipIDsOrCatIDsStr.substring(1);
			}
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
				// if category browsing checking flag and returning sub categories of parent catId
				if(isFromCategory) {
					for (String contentID : contentIDs) {
						if(null != contentID && !contentID.isEmpty()) {
							int categoryObj = Integer.parseInt(contentID);
							Category subCategory = RBTCacheManager.getInstance().getCategory(categoryObj, language);
							logger.info("list of subcategory Id is :" + subCategory.getCategoryId());
							if(subCategory == null || ( !subCategory.getCategoryStartTime().before(now) || !subCategory.getCategoryEndTime().after(now) )) {
								continue;
							}
								subCategoryList.add(subCategory);
						}
					}
					return (Category[]) subCategoryList.toArray(new Category[0]);
				}
				// else clips
				else {
					for (String contentID : contentIDs)
						{
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
		return false;
	}
	
	public static void main(String args[]) throws Exception{
		
		String responseString = "<rbt><contents end_index=\"6\" no_of_contents=\"7\" start_index=\"0\"><content id=\"3722092\" name=\"tuna haki ya kusifu\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3722092_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3722092_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"212371200000\" /></content><content id=\"3557647\" name=\"Mtetezi Wangu\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3557647_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3557647_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"315532800000\" /></content><content id=\"3721979\" name=\"Natamani\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3721979_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3721979_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"212371200000\" /></content><content id=\"3550942\" name=\"Mawazo\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3550942_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3550942_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"315532800000\" /></content><content id=\"3557662\" name=\"nina wimbo\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3557662_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3557662_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"315532800000\" /></content><content id=\"3550938\" name=\"Mbagala\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3550938_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3550938_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"315532800000\" /></content><content id=\"3548161\" name=\"Songa Mbele\" type=\"clip\"><property name=\"preview_file\" type=\"prompt\" value=\"rbt_3548161_rbt.wav\" /><property name=\"rbt_file\" type=\"prompt\" value=\"rbt_3548161_rbt.wav\" /><property name=\"demo_file\" type=\"prompt\" value=\"null.wav\" /><property name=\"amount\" type=\"data\" value=\"10\" /><property name=\"classtype\" type=\"data\" value=\"DEFAULT\" /><property name=\"period\" type=\"data\" value=\"315532800000\" /></content></contents></rbt>";
		
		String clipIDsStr = "";
		Document document = XMLUtils.getDocumentFromString(responseString.trim());
		if (document != null)
		{
			NodeList contentNodes = document.getElementsByTagName("content");
			if(null != contentNodes) {
				int noOfNodes = contentNodes.getLength();
				Element contentElem;
				String clipId, type;
				for(int index=0; index<noOfNodes; index++) {
					contentElem = (Element) contentNodes.item(index);
					type = contentElem.getAttribute("type");
					if(null != type && type.equals("clip")) {
						clipId = contentElem.getAttribute("id");
						//Add clip to list
						if(null != clipId) {
							clipIDsStr += "," + clipId;
						}
					}
				}
			}
			if(clipIDsStr.length() > 0) {
				clipIDsStr = clipIDsStr.substring(1);
			}
			System.out.println(clipIDsStr);
		}
		else
		{
			System.out.println("Invalid Xml format, could not get the document from the httpResponse");
		}
	}
}