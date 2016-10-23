/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.lucene.LuceneIndexerFactory;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.webservice.client.XMLParser;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 *
 */
public class BasicRBTContentProvider implements RBTContentProvider, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(BasicRBTContentProvider.class);

	private DocumentBuilderFactory documentBuilderFactory;
	protected DocumentBuilder documentBuilder;

	protected RBTDBManager rbtDBManager = null;
	protected RBTCacheManager rbtCacheManager = null;
	protected ParametersCacheManager parametersCacheManager = null;
    protected static List<String> configuredModesListForIDInContentName = null;
    
    private static ObjectMapper mapper = new ObjectMapper();
	/**
	 * @throws ParserConfigurationException 
	 * 
	 */
	public BasicRBTContentProvider() throws ParserConfigurationException
	{
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilder = documentBuilderFactory.newDocumentBuilder();

		rbtDBManager = RBTDBManager.getInstance();
		rbtCacheManager = RBTCacheManager.getInstance();
		parametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		if(configuredModesListForIDInContentName == null){
			Parameters parameter= parametersCacheManager.getParameter("COMMON","MODES_FOR_SUPPORTING_CONTENT_ID_IN_CONTENT_NAME", null);
			String modes = (parameter != null) ? parameter.getValue():null;
			configuredModesListForIDInContentName = (modes!=null) ? Arrays.asList(modes.split(",")) : null;
		}
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.RBTContentProvider#getContentXML(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	public final String getContentXML(WebServiceContext task)
	{
		if(configuredModesListForIDInContentName != null) {
			String mode = task.getString(param_mode);
			if(mode == null || mode.trim().length() == 0) {
				task.put(param_mode, "IVR");
			}
		}
		
		Document document = getContentsDocument(task);
		String response = XMLUtils.getStringFromDocument(document);

		return response;
	}

	public final String getSearchContentXML(WebServiceContext task){
		String searchType = task.getString(param_searchType);
		Document document = null;
		if(searchType != null && searchType.equalsIgnoreCase(ALBUM)){
			 document = getCategorySearchDocument(task);
		}else{
		     document = getSearchDocument(task);
		}
		String response = XMLUtils.getStringFromDocument(document);

		return response;
	}
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.RBTContentProvider#addContent(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	public String addContent(WebServiceContext task)
	{
		String response = ERROR;
		try
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

			int clipID = Integer.parseInt(task.getString(param_clipID));
			String name = task.getString(param_name);
			String nameWavFile = task.getString(param_nameWavFile);
			String previewWavFile = task.getString(param_previewWavFile);
			String rbtWavFile = task.getString(param_rbtWavFile);
			String grammar = task.getString(param_grammar);
			String smsAlias = task.getString(param_smsAlias);
			String promoID = task.getString(param_promoID);
			String classType = task.getString(param_classType);
			Date startTime = dateFormat.parse(task.getString(param_startTime));
			Date endTime = dateFormat.parse(task.getString(param_endTime));
			String album = task.getString(param_album);
			String language = task.getString(param_language);
			String demoWavFile = task.getString(param_demoWavFile);
			String artist = task.getString(param_artist);
			String info = task.getString(param_info);
			String contentType = task.getString(param_contentType);

			char addToAccessTable = 'y';

			Clip clip = new Clip();
			clip.setClipId(clipID);
			clip.setClipName(name);
			clip.setClipNameWavFile(nameWavFile);
			clip.setClipPreviewWavFile(previewWavFile);
			clip.setClipRbtWavFile(rbtWavFile);
			clip.setClipGrammar(grammar);
			clip.setClipSmsAlias(smsAlias);
			clip.setAddToAccessTable(addToAccessTable);
			clip.setClipPromoId(promoID);
			clip.setClassType(classType);
			clip.setClipStartTime(startTime);
			clip.setClipEndTime(endTime);
			clip.setSmsStartTime(new Date());
			clip.setAlbum(album);
			clip.setLanguage(language);
			clip.setClipDemoWavFile(demoWavFile);
			clip.setArtist(artist);
			clip.setClipInfo(info);
			clip.setContentType(contentType);
			Clip clipTemp = ClipsDAO.getClip(clipID);
			if(clipTemp!=null){
				response = ALREADY_EXISTS;
			}else{
				clip = ClipsDAO.saveClip(clip);
				if (clip != null)
					response = SUCCESS;
				else
					response = FAILED;
			}
		}
		catch (Exception e)
		{
			response = FAILED;
			logger.error("", e);
		}

		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element responseElem = Utility.getResponseElement(document, response);
		element.appendChild(responseElem);

		return (XMLUtils.getStringFromDocument(document));
	}
   /**
    * @author deepak.kumar
    * @param task for the search through Lucene for different content type as configured in DB in 
    * parameter SEARCH_CONTENT_TYPE_MAPPING
    * @return document
    */
	
	protected Document getSearchDocument(WebServiceContext task){
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);
		
		Element contentElement = document.createElement(CONTENTS);
		element.appendChild(contentElement);
		
		String contentType = task.getString(param_type);
		HashMap<String, String> contentMap = new HashMap<String,String>();
		Parameters parameters = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "SEARCH_CONTENT_TYPE_MAPPING");
		String parameterValue = null;
		if(parameters!=null){
		  	 parameterValue = parameters.getValue();
		}
		if(parameterValue!=null){
			String contentParam[] = parameterValue.split(":");
			if(contentParam!=null){
		  		for(String str : contentParam){
		  		   if(str.contains("=")){
		  				String values[] = str.split("=");
		  				contentMap.put(values[0], values[1]);
					}
				}
			}
		 }
		
		String search_type = contentMap.get(contentType);
		HashMap<String, String> map = new HashMap<String,String>();
		map.put(search_type, task.getString(param_searchText));	
		int pageNo = 0;
		if(task.getString(param_pageNo)!=null){
		     pageNo = Integer.parseInt(task.getString(param_pageNo));
		}
		int maxResults = 100;
		if(task.getString(param_maxResults)!=null){
		    maxResults = Integer.parseInt(task.getString(param_maxResults));
		}
		String language = task.getString(param_language);
		logger.info("Search through Lucene: Map = " + map + "PageNo=" + pageNo + "MaxResults = " + maxResults + "language=" + language);
		ArrayList<LuceneClip> luceneClipList = null;
		try{
			map.put("SUBSCRIBER_ID", task.getString(param_subscriberID));
			//querylanguage and language both will be same.
			luceneClipList = LuceneIndexerFactory.getInstance().searchQuery(map, pageNo, maxResults,language,language);
			if(luceneClipList!=null){
				for(int i=0;i<luceneClipList.size();i++){
					LuceneClip luceneClip = luceneClipList.get(i);
					if(luceneClip!=null){
					   Element clipContentElement = getClipSearchElement(document,luceneClip, task);
					   contentElement.appendChild(clipContentElement);
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return document;
	}
	
	protected Document getCategorySearchDocument(WebServiceContext task){
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);
		
		Element contentsElement = document.createElement(CONTENTS);
		element.appendChild(contentsElement);
		
		String contentType = task.getString(param_type);
		HashMap<String, String> contentMap = new HashMap<String,String>();
		Parameters parameters = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "SEARCH_CONTENT_TYPE_MAPPING");
		String parameterValue = null;
		if(parameters!=null){
		  	 parameterValue = parameters.getValue();
		}
		if(parameterValue!=null){
			String contentParam[] = parameterValue.split(":");
			if(contentParam!=null){
		  		for(String str : contentParam){
		  		   if(str.contains("=")){
		  				String values[] = str.split("=");
		  				contentMap.put(values[0], values[1]);
					}
				}
			}
		 }
		String search_type = contentMap.get(contentType)!=null?contentMap.get(contentType):contentType;
		HashMap<String, String> map = new HashMap<String,String>();
		map.put(search_type, task.getString(param_searchText));	
		int pageNo = 0;
		if(task.getString(param_pageNo)!=null){
		     pageNo = Integer.parseInt(task.getString(param_pageNo));
		}
		int maxResults = 100;
		if(task.getString(param_maxResults)!=null){
		    maxResults = Integer.parseInt(task.getString(param_maxResults));
		}
		String language = task.getString(param_language);
		logger.info("Category Search through Lucene: Map = " + map + " PageNo=" + pageNo + " MaxResults = " + maxResults + " language=" + language
				          + " Album Search");
		map.put(SUBSCRIBER_ID, task.getString(param_subscriberID));
		ArrayList<LuceneCategory> luceneCategoryList = LuceneIndexerFactory.getInstance().searchCategoryQuery(map, pageNo,
				                                                        maxResults, language, true, language);
		if(luceneCategoryList!=null){
			for(int i=0;i<luceneCategoryList.size();i++){
				LuceneCategory luceneCategory = luceneCategoryList.get(i);
				if(luceneCategory!=null){
					   Element categoryContentElement = getCategorySearchElement(document,luceneCategory);
					   contentsElement.appendChild(categoryContentElement);
				}
				
			}
		}
		return document;

	}
	
	public Element getCategorySearchElement(Document document,LuceneCategory luceneCat)
	{
		
		Element contentElem = document.createElement(CONTENT);
		contentElem.setAttribute(ID,String.valueOf(luceneCat.getCategoryId()));
		contentElem.setAttribute(NAME,luceneCat.getCategoryName());
		contentElem.setAttribute(TYPE,Utility.getCategoryType(luceneCat.getCategoryTpe()));
		Utility.addPropertyElement(document, contentElem, CATEGORY_NAME_FILE, PROMPT, luceneCat.getCategoryName());
		Utility.addPropertyElement(document, contentElem, CATEGORY_PREVIEW_FILE, PROMPT, luceneCat.getCategoryPreviewWavFile());
		Utility.addPropertyElement(document, contentElem, CATEGORY_GREETING, PROMPT, luceneCat.getCategoryGreeting());
		Utility.addPropertyElement(document, contentElem, CHARGE_CLASS, DATA, luceneCat.getClassType());
		String catPromoId = luceneCat.getCategoryPromoId();
		if (null != catPromoId) {
			Utility.addPropertyElement(document, contentElem,
					CATEGORY_PROMO_ID, DATA, catPromoId);
		}

		String amount = "0";
		String period = "";
		
		String renewalAmount = "0";
		String renewalPeriond = "";
		
		String classTypeStr = "DEFAULT";

		if (luceneCat.getClassType() != null)
		{
			classTypeStr = luceneCat.getClassType();
			ChargeClass classType = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classTypeStr);
			if (classType != null)
			{
				amount = classType.getAmount();
				period = classType.getSelectionPeriod();
				renewalAmount = classType.getRenewalAmount();
				renewalPeriond = classType.getRenewalPeriod();
			}
		}

		Utility.addPropertyElement(document, contentElem, AMOUNT, DATA, amount);
		Utility.addPropertyElement(document, contentElem, PERIOD, DATA, period);
		Utility.addPropertyElement(document, contentElem, RENEWAL_AMOUNT, DATA, renewalAmount);
		Utility.addPropertyElement(document, contentElem, RENEWAL_PERIOD, DATA, renewalPeriond);
		

		return contentElem;
	}

	
	
	
	protected Document getContentsDocument(WebServiceContext task)
	{
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		String contentType = task.getString(param_contentType);

		if (contentType.equalsIgnoreCase(CLIP))
		{
			Element contentsElem = getClipElement(document, task);
			element.appendChild(contentsElem);
		}
		else if (contentType.equalsIgnoreCase(CATEGORY))
		{
			Element contentsElem = getCategoryElement(document, task);
			element.appendChild(contentsElem);
		}
		else if (contentType.equalsIgnoreCase(EMOTION_UGC_CLIPS))
		{
			Element contentsElem = getEmotionClipsElement(document, task);
			element.appendChild(contentsElem);
		}
		else if (Utility.isCategoryType(contentType))
		{
			Category[] categories = getCategories(task);
			Element contentsElem = getCategoriesElement(document, task, categories);
			element.appendChild(contentsElem);
		}
		else if (contentType.equalsIgnoreCase(SUPER_SONGS))
		{
			Element contentsElem = getSuperSongsElement(document, task);
			element.appendChild(contentsElem);
		}
		else if (contentType.equalsIgnoreCase(RE_SONGS))
		{
			Element contentsElem = getRESongsElement(document, task);
			element.appendChild(contentsElem);
		}
		else
		{
			Clip[] clips = getClips(task);
			Element contentsElem = getClipsElement(document, task, clips);
			element.appendChild(contentsElem);
		}

		return document;
	}
	
	protected Element getClipElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(CONTENTS);

		String contentID = task.getString(param_contentID);
		Clip clip = getClip(contentID, task);
		if (clip != null)
		{   
			if(configuredModesListForIDInContentName!=null){
			  boolean isClipIDInClipNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
			  if(isClipIDInClipNameAllowed){
				clip.setClipName(clip.getClipId()+"");
			  }
			}
			Element contentElem = getClipContentElement(document, null, clip, task);
			element.appendChild(contentElem);
		}

		return element;
	}

	protected Clip getClip(String contentID, WebServiceContext task)
	{
		Clip clip = null;
		String browsingLanguage = task.getString(param_browsingLanguage);

		if (contentID == null)
			return null;

		try
		{
			int clipID = Integer.parseInt(contentID);
			clip = rbtCacheManager.getClip(clipID, browsingLanguage);
		}
		catch (NumberFormatException e)
		{

		}

		if (clip == null)
			clip = rbtCacheManager.getClipByPromoId(contentID, browsingLanguage);
		if (clip == null)
			clip = rbtCacheManager.getClipByRbtWavFileName(contentID, browsingLanguage);

		return clip;
	}

	protected Element getCategoryElement(Document document, WebServiceContext task)
	{
		int contentID = Integer.parseInt(task.getString(param_contentID));
		String browsingLanguage = task.getString(param_browsingLanguage);
		
		Element element = document.createElement(CONTENTS);
		Category category = rbtCacheManager.getCategory(contentID, browsingLanguage);
		if (category != null)
		{   
			if(configuredModesListForIDInContentName!=null){
			  boolean isCategoryIDInCategoryNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
			  if(isCategoryIDInCategoryNameAllowed){
				category.setCategoryName(category.getCategoryId()+"");
			  }
			}

			Element contentElem = getCategoryContentElement(document, null, category, task);
			element.appendChild(contentElem);
		}

		return element;
	}

	protected Element getEmotionClipsElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(CONTENTS);

		String subscriberID = task.getString(param_subscriberID);
		String calledNo = task.getString(param_calledNo);
		String browsingLanguage = task.getString(param_browsingLanguage);
		
		Clip[] clips = rbtCacheManager.getClipsByAlbum(subscriberID, browsingLanguage);

		Date currentDate = new Date();
		int noOfContents = 0;
		if (clips != null)
		{
			for (Clip clip : clips)
			{
				String contentType = clip.getContentType();
				if (contentType != null
						&& contentType.equalsIgnoreCase("EMOTION_UGC")
						&& clip.getClipEndTime().after(currentDate)
						&& clip.getClipRbtWavFile().contains(subscriberID + "_" + calledNo))
				{
					if(configuredModesListForIDInContentName!=null){
					  boolean isClipIDInClipNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
					  if(isClipIDInClipNameAllowed){
						clip.setClipName(clip.getClipId()+"");
					  }
					}

					Element contentElem = getClipContentElement(document, null, clip, task);
					element.appendChild(contentElem);

					noOfContents++;
				}
			}
		}

		element.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));

		return element;
	}

	protected Clip[] getClips(WebServiceContext task)
	{
		Clip[] clips = null;
		Integer totalNoOfContents = 0; 
		
		String subscriberId = task.getString(param_subscriberID);

		String contentType = task.getString(param_contentType);
		String browsingLanguage = task.getString(param_browsingLanguage);

		int startIndex = 0;
		int endIndex = -1;
		if (task.containsKey(param_startIndex) && task.containsKey(param_endIndex))
		{
			startIndex = Integer.parseInt(task.getString(param_startIndex));
			endIndex = Integer.parseInt(task.getString(param_endIndex));
		}
		else if (task.containsKey(param_pageNo))
		{
			Parameters clipContentsPerPageParam = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "CLIP_CONTENTS_PER_PAGE", "50");
			int clipContentsPerPage = Integer.parseInt(clipContentsPerPageParam.getValue().trim());

			int pageNo = Integer.parseInt(task.getString(param_pageNo));
			startIndex = (pageNo - 1) * clipContentsPerPage;
			endIndex = (pageNo * clipContentsPerPage) - 1;
		}
		if (startIndex < 0)
			startIndex = 0;

		if (contentType.equalsIgnoreCase(UGC_CLIPS))
		{
			clips = rbtCacheManager.getClipsByAlbum(task.getString(param_contentID), browsingLanguage);
			ArrayList<Clip> ugcClipsList = new ArrayList<Clip>();
			Date currentDate = new Date();
			if (clips != null)
			{
				for (Clip clip : clips)
				{
					if (clip.getClipEndTime().after(currentDate))
						ugcClipsList.add(clip);
				}
			}

			if (ugcClipsList.size() > 0)
			{
				totalNoOfContents = ugcClipsList.size();
				Clip[] ugcClips = ugcClipsList.toArray(new Clip[0]);

				clips = null;
				if (startIndex < ugcClips.length)
				{
					if (endIndex < 0 || endIndex >= ugcClips.length)
						endIndex = ugcClips.length - 1;

					int noOfContents = endIndex - startIndex + 1;
					clips = new Clip[noOfContents];

					System.arraycopy(ugcClips, startIndex, clips, 0, noOfContents);
				}
			}
		}
		else if (contentType.equalsIgnoreCase(CATEGORY_PROFILE_CLIPS))
		{
			int contentID = Integer.parseInt(task.getString(param_contentID));
			clips = rbtCacheManager.getActiveClipsInCategory(contentID, browsingLanguage, subscriberId, null);

			String language = task.getString(param_language);
			ArrayList<Clip> profileClipsList = new ArrayList<Clip>();
			if (clips != null)
			{
				String languageKey = "_" + language;
				for (Clip clip : clips)
				{
					if (clip.getClipRbtWavFile() != null && clip.getClipRbtWavFile().contains(languageKey))
						profileClipsList.add(clip);
				}
			}
			if (profileClipsList.size() > 0)
			{
				totalNoOfContents = profileClipsList.size();
				Clip[] profileClips = profileClipsList.toArray(new Clip[0]);

				clips = null;
				if (startIndex < profileClips.length)
				{
					if (endIndex < 0 || endIndex >= profileClips.length)
						endIndex = profileClips.length - 1;

					int noOfContents = endIndex - startIndex + 1;
					clips = new Clip[noOfContents];

					System.arraycopy(profileClips, startIndex, clips, 0, noOfContents);
				}
			}
		}
		else
		{
			int contentID = Integer.parseInt(task.getString(param_contentID));

			int noOfContents = -1;
			if (endIndex >= 0)
				noOfContents = endIndex - startIndex + 1;

			StringBuffer totalNoOfContentsBuff = new StringBuffer();
			clips = rbtCacheManager.getActiveClipsInCategory(contentID, startIndex, noOfContents, browsingLanguage, subscriberId, totalNoOfContentsBuff);
//			totalNoOfContents = rbtCacheManager.getActiveClipsCountInCategory(contentID);
			totalNoOfContents = Integer.parseInt(totalNoOfContentsBuff.toString());
		}

		task.put(param_totalNoOfContents, totalNoOfContents);

		return clips;
	}

	protected final Element getClipsElement(Document document, WebServiceContext task, Clip[] clips)
	{
		Element element = document.createElement(CONTENTS);

		int noOfContents = (Integer) task.get(param_totalNoOfContents);
		element.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));

		int startIndex = 0;
		int endIndex = noOfContents - 1;
		if (task.containsKey(param_startIndex) && task.containsKey(param_endIndex))
		{
			startIndex = Integer.parseInt(task.getString(param_startIndex));
			endIndex = Integer.parseInt(task.getString(param_endIndex));
		}
		else if (task.containsKey(param_pageNo))
		{
			Parameters clipContentsPerPageParam = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "CLIP_CONTENTS_PER_PAGE", "50");
			int clipContentsPerPage = Integer.parseInt(clipContentsPerPageParam.getValue().trim());

			int pageNo = Integer.parseInt(task.getString(param_pageNo));
			startIndex = (pageNo - 1) * clipContentsPerPage;
			endIndex = (pageNo * clipContentsPerPage) - 1;
		}
		if (startIndex < 0)
			startIndex = 0;
		if (endIndex < 0 || endIndex >= noOfContents)
			endIndex = noOfContents - 1;

		if (clips != null)
		{
			Category parentCategory = null;
			String browsingLanguage = task.getString(param_browsingLanguage);
			
			try
			{
				int contentID = Integer.parseInt(task.getString(param_contentID));
				parentCategory = rbtCacheManager.getCategory(contentID, browsingLanguage);
			}
			catch (NumberFormatException e)
			{
			}

			for (Clip clip : clips)
			{
				if(configuredModesListForIDInContentName!=null){
				  boolean isClipIDInClipNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
				  if(isClipIDInClipNameAllowed){
					clip.setClipName(clip.getClipId()+"");
				  }
				}

				Element contentElem = getClipContentElement(document, parentCategory, clip, task);
				element.appendChild(contentElem);
			}
		}

		element.setAttribute(START_INDEX, String.valueOf(startIndex));
		element.setAttribute(END_INDEX, String.valueOf(endIndex));

		return element;
	}

	protected final String getMemcacheClipsCategoryJson(Object[] objects)
	{

		StringWriter out = new StringWriter();
		String responseString = "ERROR";
		try {
			JSONArray jsonArray = new JSONArray();
			
			if (objects != null)
			{
				int i = 0;
				for (Object object : objects)
				{
					if(object != null) {
						jsonArray.put(i++, mapper.writeValueAsString(object));
					}
				}
				jsonArray.write(out);
			}
			if(jsonArray.length() > 0) {
				responseString = out.toString();
			}
		}
		catch(Exception e) {
			logger.error("Exception: ", e);
			return "ERROR";
		}
		
		return responseString;
	}
	
	protected Category[] getCategories(WebServiceContext task)
	{
		int contentID = Integer.parseInt(task.getString(param_contentID));
		String circleID = task.getString(param_circleID);
		String isPrepaid = task.getString(param_isPrepaid);
		String language = task.getString(param_language);

		char isPrepaidChar = 'b';
		if (isPrepaid != null)
			isPrepaidChar = isPrepaid.charAt(0);

		if (contentID != 0)
			language = null;

		int startIndex = 0;
		int endIndex = -1;
		if (task.containsKey(param_startIndex) && task.containsKey(param_endIndex))
		{
			startIndex = Integer.parseInt(task.getString(param_startIndex));
			endIndex = Integer.parseInt(task.getString(param_endIndex));
		}
		else if (task.containsKey(param_pageNo))
		{
			Parameters categoryContentsPerPageParam = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "CATEGORY_CONTENTS_PER_PAGE", "100");
			int categoryContentsPerPage = Integer.parseInt(categoryContentsPerPageParam.getValue().trim());

			int pageNo = Integer.parseInt(task.getString(param_pageNo));
			startIndex = (pageNo - 1) * categoryContentsPerPage;
			endIndex = (pageNo * categoryContentsPerPage) - 1;
		}
		if (startIndex < 0)
			startIndex = 0;

		int noOfContents = -1;
		if (endIndex >= 0)
			noOfContents = endIndex - startIndex + 1;
		
		String browsingLanguage = task.getString(param_browsingLanguage);
		Category[] categories = rbtCacheManager.getActiveCategoriesInCircle(circleID, contentID, isPrepaidChar, language, startIndex, noOfContents, browsingLanguage);
		Integer totalNoOfContents = rbtCacheManager.getActiveCategoriesCountInCircle(circleID, contentID, isPrepaidChar, language);

		task.put(param_totalNoOfContents, totalNoOfContents);

		return categories;
	}

	protected final Element getCategoriesElement(Document document, WebServiceContext task, Category[] categories)
	{
		Element element = document.createElement(CONTENTS);

		int noOfContents = (Integer) task.get(param_totalNoOfContents);
		element.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));

		int startIndex = 0;
		int endIndex = noOfContents - 1;
		if (task.containsKey(param_startIndex) && task.containsKey(param_endIndex))
		{
			startIndex = Integer.parseInt(task.getString(param_startIndex));
			endIndex = Integer.parseInt(task.getString(param_endIndex));
		}
		else if (task.containsKey(param_pageNo))
		{
			Parameters categoryContentsPerPageParam = parametersCacheManager.getParameter(iRBTConstant.WEBSERVICE, "CATEGORY_CONTENTS_PER_PAGE", "100");
			int categoryContentsPerPage = Integer.parseInt(categoryContentsPerPageParam.getValue().trim());

			int pageNo = Integer.parseInt(task.getString(param_pageNo));
			startIndex = (pageNo - 1) * categoryContentsPerPage;
			endIndex = (pageNo * categoryContentsPerPage) - 1;
		}
		if (startIndex < 0)
			startIndex = 0;
		if (endIndex < 0 || endIndex >= noOfContents)
			endIndex = noOfContents - 1;

		if (categories != null)
		{
			String browsingLanguage = task.getString(param_browsingLanguage);			
			int contentID = Integer.parseInt(task.getString(param_contentID));
			Category parentCategory = rbtCacheManager.getCategory(contentID, browsingLanguage);

			for (Category category : categories)
			{
				if(configuredModesListForIDInContentName!=null){
				  boolean isCategoryIDInCategoryNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
				  if(isCategoryIDInCategoryNameAllowed){
					category.setCategoryName(category.getCategoryId()+"");
				  }
				}

				Element contentElem = getCategoryContentElement(document, parentCategory, category, task);
				element.appendChild(contentElem);
			}
		}

		element.setAttribute(START_INDEX, String.valueOf(startIndex));
		element.setAttribute(END_INDEX, String.valueOf(endIndex));

		return element;
	}

	protected Element getSuperSongsElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(CONTENTS);

		int noOfContents = 0;
		
		String subscriberId = task.getString(param_subscriberID);

		String langauge = task.getString(param_language);
		Parameters superSongsParam = parametersCacheManager.getParameter(iRBTConstant.SUPERSONGS, langauge, null);
		if (superSongsParam != null)
		{
			int noOfSuperSongs = 0;
			ArrayList<String> superCategoryList = new ArrayList<String>();
			HashMap<String, String[]> superCategorIndexMap = new HashMap<String, String[]>();


			String[] superSongs = superSongsParam.getValue().split(",");
			for (String superSong : superSongs)
			{
				String category = superSong.substring(0, superSong.indexOf(':'));
				String[] indexes = (superSong.substring(superSong.indexOf(':') + 1)).split("-");

				noOfSuperSongs += indexes.length;
				superCategoryList.add(category);
				superCategorIndexMap.put(category, indexes);
			}

			Clip[] superClips = new Clip[noOfSuperSongs];
			ArrayList<Integer> clipIDList = new ArrayList<Integer>();

			for (String category : superCategoryList)
			{
				Clip[] clips = null;
				String browsingLanguage = task.getString(param_browsingLanguage);
				if (category.startsWith("BI"))
					clips = getSuperSongsFromThirdParty(task, category.substring(3));
				else
					clips = rbtCacheManager.getActiveClipsInCategory(Integer.parseInt(category), browsingLanguage, subscriberId, null);

				if (clips != null)
				{
					String[] indexes = superCategorIndexMap.get(category);

					for (int i = 0, indexCount = 0; i < clips.length; i++)
					{
						if (indexCount == indexes.length)
							break;

						if (clipIDList.contains(clips[i].getClipId()))
							continue;

						int clipIndex = Integer.parseInt(indexes[indexCount]) - 1;
						if (clipIndex >= noOfSuperSongs)
							clipIndex = noOfSuperSongs - 1;

						superClips[clipIndex] = clips[i];

						indexCount++;
						clipIDList.add(clips[i].getClipId());
					}
				}
			}

			Parameters maxNoOfSuperSongsParam = parametersCacheManager.getParameter(iRBTConstant.SUPERSONGS, "MAX_NUMBER_OF_SONGS", "20");
			int maxNoOfSuperSongs = Integer.parseInt(maxNoOfSuperSongsParam.getValue());
			noOfContents = 0;
			for (Clip clip : superClips)
			{
				if (clip != null)
				{
					noOfContents++;
                    if(configuredModesListForIDInContentName!=null){
					  boolean isClipIDInClipNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
					  if(isClipIDInClipNameAllowed){
						clip.setClipName(clip.getClipId()+"");
					  }
                    }

					Element contentElem = getClipContentElement(document, null, clip, task);
					element.appendChild(contentElem);

					if (noOfContents == maxNoOfSuperSongs)
						break;
				}
			}
		}

		element.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));

		return element;
	}
	
	/**
	 * @param document
	 * @param task
	 * @return
	 */
	protected Element getRESongsElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(CONTENTS);

		int noOfContents = 0;
		Clip[] clips = getRESongsFromThirdParty(task);
		if (clips != null)
		{
			for (Clip clip : clips)
			{
				if (clip != null)
				{
					noOfContents++;
					if(configuredModesListForIDInContentName!=null){
					  boolean isClipIDInClipNameAllowed = configuredModesListForIDInContentName.contains(task.get(param_mode));
					  if(isClipIDInClipNameAllowed){
						clip.setClipName(clip.getClipId()+"");
					  }
					}
					Element contentElem = getClipContentElement(document, null, clip, task);
					element.appendChild(contentElem);
				}
			}
		}
		element.setAttribute(NO_OF_CONTENTS, String.valueOf(noOfContents));
		return element;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.RBTContentProvider#getCategoryContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category)
	 */
	public Element getCategoryContentElement(Document document, Category parentCategory, Category category)
	{
		return getCategoryContentElement(document, parentCategory, category,null);
	}

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

		String amount = "0";
		String period = "";
		
		String renewalAmount = "0";
		String renewalPeriond = "";
		
		String classTypeStr = "DEFAULT";

		if (category.getClassType() != null)
		{
			classTypeStr = category.getClassType();
			ChargeClass classType = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classTypeStr);
			if (classType != null)
			{
				amount = classType.getAmount();
				period = classType.getSelectionPeriod();
				renewalAmount = classType.getRenewalAmount();
				renewalPeriond = classType.getRenewalPeriod();
			}
		}

		Utility.addPropertyElement(document, element, CHARGE_CLASS, DATA, classTypeStr);
		Utility.addPropertyElement(document, element, AMOUNT, DATA, amount);
		Utility.addPropertyElement(document, element, PERIOD, DATA, period);
		Utility.addPropertyElement(document, element, RENEWAL_AMOUNT, DATA, renewalAmount);
		Utility.addPropertyElement(document, element, RENEWAL_PERIOD, DATA, renewalPeriond);
		Utility.addPropertyElement(document, element, CATEGORY_PROMO_ID, DATA, category.getCategoryPromoId());	
		
		return element;
	}

	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.content.RBTContentProvider#getClipContentElement(org.w3c.dom.Document, com.onmobile.apps.ringbacktones.rbtcontents.beans.Category, com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip)
	 */
	public Element getClipContentElement(Document document, Category parentCategory, Clip clip)
	{
		return getClipContentElement(document, parentCategory, clip,null);
	}

	public Element getClipContentElement(Document document, Category parentCategory, Clip clip, WebServiceContext task)
	{
		Element element = document.createElement(CONTENT);

		element.setAttribute(ID, String.valueOf(clip.getClipId()));
		element.setAttribute(NAME, clip.getClipName());
		element.setAttribute(TYPE, CLIP);
		String format= null;
		if(task != null)
			 format = task.getString(param_format);
		Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(clip.getClipPreviewWavFile(),format));
		Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(clip.getClipRbtWavFile(),format));
		Utility.addPropertyElement(document, element, DEMO_FILE, PROMPT, Utility.getPromptName(clip.getClipDemoWavFile(),format));
		Utility.addPropertyElement(document, element, GRAMMAR, GRAMMAR, clip.getClipGrammar());
		String clipContentType = "NORMAL";
		if (clip.getContentType() != null && clip.getContentType().length() != 0)
			clipContentType = clip.getContentType();
		
		String	promoId  =  clip.getClipPromoId() ;

		Utility.addPropertyElement(document, element, CONTENT_TYPE, DATA, clipContentType);
		
		Utility.addPropertyElement(document, element, PROMO_CODE, DATA, promoId);
		
		Utility.addPropertyElement(document, element, ARTIST, DATA, clip.getArtist());
		
		if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_IMAGE_URL_IN_CONTENT_RESPONSE, false))
			Utility.addPropertyElement(document, element, IMAGE_URL, DATA, clip.getClipInfoMap() != null ? clip.getClipInfoMap().get(ClipInfoKeys.IMG_URL.toString()) : null);
		
		Utility.addPropertyElement(document, element, ALBUM, DATA, clip.getAlbum());
		
		Utility.addPropertyElement(document, element, CLIP_INFO, DATA, clip.getClipInfo());
		
		Utility.addPropertyElement(document, element, CHARGE_CLASS, DATA, clip.getClassType());
		
		if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
			String wavFile = clip.getClipRbtWavFile();
			String vcode = null;
			if(wavFile!=null)
				vcode = wavFile.replaceAll("rbt_", "").replaceAll("_rbt", "");
			Utility.addPropertyElement(document, element, VCODE, DATA, vcode);
		}
		
		

		return element;
	}

	
	public Element getClipSearchElement(Document document,Clip clip)
	{
		return getClipSearchElement(document, clip, null);
	}

	public Element getClipSearchElement(Document document,Clip clip, WebServiceContext task)
	{
		Element element = getClipContentElement(document, null, clip, task);

		Utility.addPropertyElement(document, element, param_alias, DATA, clip.getClipSmsAlias());
		Utility.addPropertyElement(document, element, class_type, DATA, clip.getClassType());
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DISPLAY_PRICE_AND_END_DATE_IN_CLIP_SEARCH_XML", "FALSE")){
			String classTpeStr = clip.getClassType();
			ChargeClass classType = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classTpeStr);
			if(classType!=null){
			   Utility.addPropertyElement(document, element, param_price, DATA, classType.getAmount());
			   Utility.addPropertyElement(document, element, RENEWAL_AMOUNT, DATA, classType.getRenewalAmount());
			   Utility.addPropertyElement(document, element, RENEWAL_PERIOD, DATA, classType.getRenewalPeriod());
			}
			Date clipEndDate = clip.getClipEndTime();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmSS");
			if(clipEndDate!=null){
			   String content_validity_date = sdf.format(clipEndDate);
			   Utility.addPropertyElement(document, element, CONTENT_VALIDITY, DATA, content_validity_date);
			}
	    }
		
		return element;
	}

	@SuppressWarnings("unchecked")
	protected Clip[] getSuperSongsFromThirdParty(WebServiceContext task, String category)
	{
		Clip[] clips = null;
		HashMap<String, String> superSongsMap = null;

		try
		{
			if (task.containsKey(param_superSongsMap))
			{
				superSongsMap = (HashMap<String, String>) task.get(param_superSongsMap);
			}
			else
			{
				Parameters biURLParam = parametersCacheManager.getParameter(iRBTConstant.SUPERSONGS, "BI_URL", null);
				String url = biURLParam.getValue().trim();
				url = url.replaceAll("%SUBSCRIBER_ID%", task.getString(param_subscriberID));
				url = url.replaceAll("%LANGUAGE%", task.getString(param_language));
				url = url.replaceAll("%CIRCLE_ID%", task.getString(param_circleID));
				url = url.replaceAll("%REC_TYPE%", task.getString(param_recommendationType));

				HttpParameters httpParameters = new HttpParameters(url);
				logger.info("RBT:: httpParameters: " + httpParameters);

				HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
				logger.info("RBT:: httpResponse: " + httpResponse);

				if (httpResponse != null && httpResponse.getResponse() != null)
				{
					Document document = XMLUtils.getDocumentFromString(httpResponse.getResponse());
					Element contentsElem = (Element) document.getElementsByTagName(CONTENTS).item(0);
					List<HashMap<String, String>> list = XMLParser.getContentsList(contentsElem);

					superSongsMap = new HashMap<String, String>();
					for (HashMap<String,String> hashMap : list)
					{
						superSongsMap.put(hashMap.get(NAME).toLowerCase(), hashMap.get("PROMPT"));
					}
				}
			}

			String ids = null;
			if (superSongsMap != null)
				ids = superSongsMap.get(category.toLowerCase());
			if (ids != null)
			{
				String[] contentIDs = ids.split(",");
				Date curDate = new Date();

				ArrayList<Clip> clipList = new ArrayList<Clip>();
				for (String contentID : contentIDs)
				{
					Clip clip = getClip(contentID.trim(), task);
					if (clip != null && clip.getClipEndTime().after(curDate))
						clipList.add(clip);
				}

				if (clipList.size() > 0)
					clips = clipList.toArray(new Clip[0]);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			task.put(param_superSongsMap, superSongsMap);
		}

		return clips;
	}

	public String getRecommendationMusicFromThirdParty(WebServiceContext task)
	{
		String responseString = null;
		try
		{
			String action = task.getString(param_action);
			Parameters biURLParam = null;
			
			if(action.equalsIgnoreCase(action_recommendation_music)) {
				biURLParam = parametersCacheManager.getParameter(iRBTConstant.BI, "BI_RECOMMENDATION_MUSIC_URL", null);
			}
			else {
				biURLParam = parametersCacheManager.getParameter(iRBTConstant.BI, "BI_RE_RECOMMENDATION_MUSIC_URL", null);
			}
			if(null == biURLParam){
				logger.warn("BI_RECOMMENDATION_MUSIC_URL parameter is not configured");
				return "ERROR";
			}
			String url = biURLParam.getValue().trim();
			String subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			String mode = task.getString(param_mode);
			if(mode == null) {
				mode = "SRBT";
			}			
			Parameters param = parametersCacheManager.getParameter(iRBTConstant.BI, "INACTIVE_USER_HIT_BI", "FALSE");
			boolean inActiveUserHitBI = param.getValue().equalsIgnoreCase("TRUE");			
			if(!inActiveUserHitBI && subscriber == null){
				return "ERROR";
			}
			String cirtleId = null;
			if(subscriber == null) {
				SubscriberDetail subscriberDetail = DataUtils.getSubscriberDetail(task);
				cirtleId = subscriberDetail.getCircleID();
			}
			else {
				cirtleId = subscriber.circleID();
			}
			url = url.replace("%SUBSCRIBER_ID%", task.getString(param_subscriberID));
			url = url.replace("%SRV_KEY_WORD%", "DEFAULT");
			url = url.replace("%FLAG%", "1");
			url = url.replace("%CIRCLE_ID%", cirtleId);
			logger.info("RBT:: URL: " + url);
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
		return responseString;
	}
	
	public String getClipCategoryFromMecache(WebServiceContext task)
	{
		String responseString = "ERROR";
		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		
		try
		{
			
			String info = task.getString(param_type);
			String method = task.getString(param_info);
			String contentId = task.getString(param_contentId);
			String language = task.getString(param_language);
			String appName = task.getString(param_appName);
			if(CLIP.equalsIgnoreCase(info)) {				
				Clip[] clips = null;
				
				if(BYCLIPID.equalsIgnoreCase(method)) {
					Clip clip = RBTCacheManager.getInstance().getClip(contentId, language, appName);
					clips = new Clip[1];
					clips[0] = clip;
				}
				else if(BYCLIPPROMOID.equalsIgnoreCase(method)) {
					Clip clip = RBTCacheManager.getInstance().getClipByPromoId(contentId, language);
					clips = new Clip[1];
					clips[0] = clip;
				}
				else if(BYCLIPALBUM.equalsIgnoreCase(method)) {
					clips = RBTCacheManager.getInstance().getClipsByAlbum(contentId, language);
				}
				else if(BYCLIPALIAS.equalsIgnoreCase(method)) {
					Clip clip = RBTCacheManager.getInstance().getClipBySMSAlias(contentId, language);
					clips = new Clip[1];
					clips[0] = clip;					
				}
				else if(BYCLIPWAVFILE.equalsIgnoreCase(method)) {
					Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(contentId, language, appName);
					clips = new Clip[1];
					clips[0] = clip;					
				}
				else if(BYCLIPS.equalsIgnoreCase(method)) {
					String[] clipIds = contentId.split(",");
					clips = RBTCacheManager.getInstance().getClips(clipIds, language, appName);
				}
				else if(BYCLIPSINCATEGORY.equalsIgnoreCase(method)) {
					int categoryId = Integer.parseInt(contentId);
					String strOffSet =  task.getString(param_offSet);
					int offset = 0;
					if(strOffSet != null) {
						offset = Integer.parseInt(strOffSet);
					}
					int rowCount = -1;
					String strRowCount = task.getString(param_rowCount);
					if(strRowCount != null) {
						rowCount = Integer.parseInt(strRowCount);
					}
					String subscriberId = task.getString(param_subscriberID); 
					clips = RBTCacheManager.getInstance().getClipsInCategory(categoryId, offset, rowCount, language, subscriberId, null);
				}
				else if(BYACTIVECLIPSINCATEGORY.equalsIgnoreCase(method)) {
					int categoryId = Integer.parseInt(contentId);
					String strOffSet =  task.getString(param_offSet);
					int offset = 0;
					if(strOffSet != null) {
						offset = Integer.parseInt(strOffSet);
					}
					int rowCount = -1;
					String strRowCount = task.getString(param_rowCount);
					if(strRowCount != null) {
						rowCount = Integer.parseInt(strRowCount);
					}
					String subscriberId = task.getString(param_subscriberID); 
					clips = RBTCacheManager.getInstance().getActiveClipsInCategory(categoryId, offset, rowCount, language, subscriberId, null, null, appName);
				}
				responseString = getMemcacheClipsCategoryJson(clips);
			}
			else if(CATEGORY.equalsIgnoreCase(info)) {
				Category[] categories = null;
				if(BYCATID.equalsIgnoreCase(method)) {
					Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(contentId), language, appName);
					categories = new Category[1];
					categories[0] = category;
				}
				else if(BYCATMMNUM.equalsIgnoreCase(method)) {
					Category category = RBTCacheManager.getInstance().getCategoryByMmNumber(contentId, language);
					categories = new Category[1];
					categories[0] = category;
				}
				else if(BYCATNAME.equalsIgnoreCase(method)) {
					Category category = RBTCacheManager.getInstance().getCategoryByName(contentId, language);
					categories = new Category[1];
					categories[0] = category;
				}
				else if(BYCATPROMOID.equalsIgnoreCase(method)) {
					Category category = RBTCacheManager.getInstance().getCategoryByPromoId(contentId, language);
					categories = new Category[1];
					categories[0] = category;
				}
				else if(BYCATALIAS.equalsIgnoreCase(method)) {
					Category category = RBTCacheManager.getInstance().getCategoryBySMSAlias(contentId, language);
					categories = new Category[1];
					categories[0] = category;
				}
				else if(BYCATPROMOIDWITHCIRCLE.equalsIgnoreCase(method)) {
					String circleId = task.getString(param_circleID);
					char prepaidYes = task.getString(param_isPrepaid).charAt(0);
					categories = RBTCacheManager.getInstance().getCategoryByPromoId(circleId, prepaidYes, contentId, language);
				}
				else if(BYCATIDS.equalsIgnoreCase(method)) {
					String[] categoryIds = contentId.split(",");
					categories = RBTCacheManager.getInstance().getCategories(categoryIds, language);
				}
				else if(BYCATINCIRCLE.equalsIgnoreCase(method)) {
					String circleId = task.getString(param_circleID);
					String strOffSet =  task.getString(param_offSet);
					int offset = 0;
					if(strOffSet != null) {
						offset = Integer.parseInt(strOffSet);
					}
					int rowCount = -1;
					String strRowCount = task.getString(param_rowCount);
					if(strRowCount != null) {
						rowCount = Integer.parseInt(strRowCount);
					}
					char prepaidYes = task.getString(param_isPrepaid).charAt(0);
					String browsingLanguage = task.getString(param_browsingLanguage);
					int parentCategoryId = Integer.parseInt(contentId);
					categories = RBTCacheManager.getInstance().getCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, offset, rowCount, browsingLanguage);
				}
				else if(BYCATTYPE.equalsIgnoreCase(method)) {
					categories = RBTCacheManager.getInstance().getCategoryByType(contentId, language);
				}
				else if(BYCATARRPROMOID.equalsIgnoreCase(method)) {
					String circleId = task.getString(param_circleID);
					char prepaidYes = task.getString(param_isPrepaid).charAt(0);
					categories = RBTCacheManager.getInstance().getCategoryByPromoId(circleId, prepaidYes, contentId, language);
				}
				else if(BYACTCATINCIRCLE.equalsIgnoreCase(method)) {
					String circleId = task.getString(param_circleID);
					String strOffSet =  task.getString(param_offSet);
					int offset = 0;
					if(strOffSet != null) {
						offset = Integer.parseInt(strOffSet);
					}
					int rowCount = -1;
					String strRowCount = task.getString(param_rowCount);
					if(strRowCount != null) {
						rowCount = Integer.parseInt(strRowCount);
					}
					char prepaidYes = task.getString(param_isPrepaid).charAt(0);
					String browsingLanguage = task.getString(param_browsingLanguage);
					int parentCategoryId = Integer.parseInt(contentId);
					categories = RBTCacheManager.getInstance().getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language, offset, rowCount, browsingLanguage, appName);
				}
				else if(BYACTCATINCIRCLELANG.equalsIgnoreCase(method)) {
					String circleId = task.getString(param_circleID);
					char prepaidYes = task.getString(param_isPrepaid).charAt(0);
					String browsingLanguage = task.getString(param_browsingLanguage);
					int parentCategoryId = Integer.parseInt(contentId);
					categories = RBTCacheManager.getInstance().getActiveCategoriesInCircleByLanguage(circleId, parentCategoryId, prepaidYes, browsingLanguage);
				}
				responseString = getMemcacheClipsCategoryJson(categories);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			responseString = "ERROR";
		}
		Element responseElem = Utility.getResponseElement(document, responseString);
		element.appendChild(responseElem);
		return XMLUtils.getStringFromDocument(document);
	}
	
	public String getCircleTopTen(WebServiceContext task)
	{
		String responseString = null;
		try
		{
			Parameters biURLParam = parametersCacheManager.getParameter(iRBTConstant.BI, "BI_CIRCLE_TOP_TEN_URL", null);
			String url = biURLParam.getValue().trim();
			String subscriberID = task.getString(param_subscriberID);
			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
			String mode = task.getString(param_mode);
			if(mode == null) {
				mode = "SRBT";
			}
			Parameters param = parametersCacheManager.getParameter(iRBTConstant.BI, "INACTIVE_USER_HIT_BI", "FALSE");
			boolean inActiveUserHitBI = param.getValue().equalsIgnoreCase("TRUE");			
			if(!inActiveUserHitBI && subscriber == null){
				return "ERROR";
			}
			String cirtleId = null;
			if(subscriber == null) {
				SubscriberDetail subscriberDetail = DataUtils.getSubscriberDetail(task);
				cirtleId = subscriberDetail.getCircleID();
			}
			else {
				cirtleId = subscriber.circleID();
			}
			url = url.replace("%SUBSCRIBER_ID%", task.getString(param_subscriberID));
			url = url.replace("%ACTION%", "new");
			url = url.replace("%FLAG%", "1");
			url = url.replace("%CIRCLE_ID%", cirtleId);
			logger.info("RBT:: URL: " + url);
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
		return responseString;
	}

	protected Clip[] getRESongsFromThirdParty(WebServiceContext task)
	{
		Clip[] clips = null;
		try
		{
			Parameters reURLParam = parametersCacheManager.getParameter(iRBTConstant.RESONGS, "RE_URL", null);
			String url = reURLParam.getValue().trim();
			url = url.replaceAll("%SUBSCRIBER_ID%", task.getString(param_subscriberID));
			url = url.replaceAll("%LANGUAGE%", task.getString(param_language));
			url = url.replaceAll("%CIRCLE_ID%", task.getString(param_circleID));
			url = url.replaceAll("%REC_TYPE%", task.getString(param_recommendationType));

			HttpParameters httpParameters = new HttpParameters(url);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			String clipIDsStr = null;
			if (httpResponse != null && httpResponse.getResponse() != null)
			{
				Document document = XMLUtils.getDocumentFromString(httpResponse.getResponse().trim());
				if (document != null)
				{
					Element contentElem = (Element) document.getElementsByTagName(CONTENT).item(0);
					clipIDsStr = contentElem.getAttribute(ID);
				}
				else
				{
					logger.warn("Invalid Xml format, could not get the document from the httpResponse");
				}
			}

			if (clipIDsStr != null && clipIDsStr.trim().length() > 0)
			{
				String[] contentIDs = clipIDsStr.split(",");
				Date curDate = new Date();
				ArrayList<Clip> clipList = new ArrayList<Clip>();
				for (String contentID : contentIDs)
				{
					Clip clip = getClip(contentID.trim(), task);
					if (clip != null && clip.getClipEndTime().after(curDate))
					{
						clipList.add(clip);
					}
					else
					{
						logger.warn("Clip not found or expired for clipID : " + contentID);
					}
				}

				if (clipList.size() > 0)
					clips = clipList.toArray(new Clip[0]);
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return clips;
	}
}
