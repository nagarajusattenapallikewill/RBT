package com.onmobile.apps.ringbacktones.lucene.solr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.LanguageIndentifier;

/**
 * @author laxmankumar
 *
 */
public abstract class SearchClient extends BaseClient {
	
	private static Logger log = Logger.getLogger(SearchClient.class);
	
	private static String filterCategoryIds = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_FILTER_CAT_IDS);
	private static String filterClipIds = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_FILTER_CLIP_IDS);
	private static List<String> filterCategoryIdsList = new ArrayList<String>();
	private static List<String> filterClipIdsList = new ArrayList<String>();
	private static String categorTypesNotAllowed = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_CAT_TYPES_NOT_ALLOWED);
	private static List<String> categorTypesNotAllowedList = new ArrayList<String>();
	
	static {
		if(filterCategoryIds != null && !filterCategoryIds.trim().equals(""))
			filterCategoryIdsList = Arrays.asList(filterCategoryIds.split(","));
		
		if(filterClipIds != null && !filterClipIds.trim().equals(""))
			filterClipIdsList = Arrays.asList(filterClipIds.split(","));
		
		if(categorTypesNotAllowed != null && !categorTypesNotAllowed.trim().equals("")) {
			categorTypesNotAllowedList = Arrays.asList(categorTypesNotAllowed.split(","));
		}
		
		log.info("Configured cat_types_not_allowed: " + categorTypesNotAllowed
				+ ", converted to categorTypesNotAllowedList: "
				+ categorTypesNotAllowedList);
	}
	
	public ArrayList<LuceneClip> searchClip(HashMap<String, String> paramsMap, int pageNo,
			int maxResults, String language) {
		// get search results
		ArrayList<LuceneClip> luceneClipList = searchClip(paramsMap, language, null);
		// pagenation logic 
		if (luceneClipList.size() > pageNo * maxResults) {
			int next = (pageNo + 1) * maxResults;
			int max = (luceneClipList.size() >= next) ? next : luceneClipList.size();
			luceneClipList = new ArrayList<LuceneClip>(luceneClipList.subList(pageNo * maxResults, max));
		} else {
			luceneClipList = new ArrayList<LuceneClip>();
		}
		return luceneClipList;
	}
			
	public ArrayList<LuceneClip> searchClip(HashMap<String, String> paramsMap, String language, String queryLanguage) {

		ArrayList<LuceneClip> luceneClipList = new ArrayList<LuceneClip>();
		
		if (paramsMap == null || paramsMap.size() == 0) {
			log.error("Input paramters map is null/empty");
			return luceneClipList;
		}
		
		StringBuffer buff = new StringBuffer();
		String clipName = paramsMap.get("song");
		if(clipName != null) {
			for(int i = 0; i < clipName.length(); i++) {
				buff.append(clipName.charAt(i) + " --> " + (int) clipName.charAt(i) + ",");
			}
		}
		
		log.debug("ClipName in Ascii " + buff.toString());
		
		
		log.info("Input keywords: " + paramsMap + " lang:" + language);
//		log.info("Input parameters ( pageNo:" + pageNo + " maxResults:" + maxResults + " lang:" + language);
		if (paramsMap.containsKey(FIELD_PARENT_CAT_ID)) {
			String parentCatId = paramsMap.remove(FIELD_PARENT_CAT_ID);
			// appending + to field name to make it mandatory
			paramsMap.put("+" + FIELD_PARENT_CAT_ID, parentCatId); 
		} else if (paramsMap.containsKey(FIELD_PARENT_CAT_NAME)) {
			String parentCatName = paramsMap.get(FIELD_PARENT_CAT_NAME);
			Category parentCat = cacheManager.getCategoryByName(parentCatName);
			if (parentCat != null) {
				// appending + to field name to make it mandatory
				paramsMap.put("+" + FIELD_PARENT_CAT_ID, String.valueOf(parentCat.getCategoryId())); 
			}
		}
		if (paramsMap.containsKey(FIELD_SUB_CAT_ID)) {
			String parentCatId = paramsMap.remove(FIELD_SUB_CAT_ID);
			// appending + to field name to make it mandatory
			paramsMap.put("+" + FIELD_SUB_CAT_ID, parentCatId); 
		} else if (paramsMap.containsKey(FIELD_SUB_CAT_NAME)) {
			String subCatName = paramsMap.get(FIELD_SUB_CAT_NAME);
			Category subCat = cacheManager.getCategoryByName(subCatName);
			if (subCat != null) {
				// appending + to field name to make it mandatory
				paramsMap.put("+" + FIELD_SUB_CAT_ID, String.valueOf(subCat.getCategoryId())); 
			}
		}
		String strQuery = getQueryString(paramsMap);
		
		/*
		 * Get query language from LanguageCodeMap. 
		 * defaultLanguage as query language, if queryLanguage is null or queryLanguage is not exist in supportedLanguage list
		 */
		queryLanguage = getQueryLanguage(queryLanguage, strQuery);
		
		HashMap<String, String> fqMap = new HashMap<String, String>();
		fqMap.put(FIELD_SEARCH_TYPE, SEARCH_TYPE_CLIP);
		if(!includeExpiredClips) {
			long curtime = System.currentTimeMillis();
			// query to exclude expired clips => dateexpired:[12312412411 TO *]
			fqMap.put(FIELD_DATE_EXPIRED, "[" + curtime + " TO " + Long.MAX_VALUE + "] ");
		}
		
//		if("TRUE".equalsIgnoreCase(isSupportLanguage)) {
//			
//			String tempLanguage = language;
//			if(tempLanguage == null || tempLanguage.equalsIgnoreCase("null") || (supportedLanguages != null && !supportedLanguages.contains(tempLanguage))) {
//				tempLanguage = defaultLanguage;
//			}
//			
//			fqMap.put(FIELD_LANGUAGE, tempLanguage.toUpperCase());
//		}

		//add code to filter corporate song
		//Configuration value should be ALL / NORMAL
		//check new configuration
		//if support filter == ALL don't do 
		//if support filter == NORMAL  fqMap.put("", "NORMAL");
		try
		{
		String clipType = ConfigReader.getInstance().getParameter(CLIP_TYPE);
		log.debug("clipType: " + clipType +"clipType boolean:"+((clipType.toUpperCase()).equals("ALL")));
		if(!((clipType.toUpperCase()).equals("ALL"))) {
			fqMap.put(FILTER_CLIP_TYPE, clipType.toUpperCase());
		}
		
		}catch(Exception ex)
		{
         log.error("ClipType is not configured", ex);
		}
		// search for only clips
		String strFilterQuery = getFilterQueryString(fqMap);

		if(language==null || language.equals("")) {
			language = defaultLanguage;
		}
		
		//TODO: read it from config
		// fields to get from solr 
	    String fields = "clipid,parentcatid,subcatid";
	    
	    log.debug("query: " + strQuery);
	    log.debug("filterQuery: " + strFilterQuery);
	    List<SearchResult> items = null;
		try {
			// getting only first 100 records
			items = executeQuery(strQuery, strFilterQuery, 0, 100, fields, queryLanguage);
		} catch (SolrServerException e) {
			log.error("Error while talking to solr server", e);
			return luceneClipList;
		}
	    
	    if (items != null && items.size() > 0) {
	    	
	    	log.info("Got search results of size " + items.size());
	    	ArrayList<String> localClipCache = new ArrayList<String>();
	    	ArrayList<String> localCategoryCache = new ArrayList<String>();
		
	    	for(SearchResult record : items){
				String clipId = record.getClipId();
				String parentCatId = record.getParentCategoryId();
				String subCatId = record.getSubCategoryId();
				
				//Filtering clipid and category id				
				if(filterClipIdsList.contains(clipId)) {
					continue;
				}
				
				if(filterCategoryIdsList.contains(subCatId)) {
					continue;
				}
				
				// gathering clipIds
				if (!localClipCache.contains(clipId)) {
					localClipCache.add(clipId);
				}
				// gathering categoryIds
				if (parentCatId != null
						&& !parentCatId.equalsIgnoreCase("null")
						&& !localCategoryCache.contains(parentCatId)) {
					localCategoryCache.add(parentCatId);
				}
				if (subCatId != null && !subCatId.equalsIgnoreCase("null")
						&& !localCategoryCache.contains(subCatId)) {
						localCategoryCache.add(subCatId);
				}
			}
			
			// get the clips at once from the memcache
			String[] clipIds = (String[])localClipCache.toArray(new String[0]);
			Clip[] clips = cacheManager.getClips(clipIds, language);
			
			// get the categories at once from the memcache
			String[] categoryIds = (String[])localCategoryCache.toArray(new String[0]);
			Category[] categories = null;
			if (categoryIds != null && categoryIds.length > 0) {
				log.debug("The category length is " + categoryIds.length);
				categories = cacheManager.getCategories(categoryIds, language);
			}
			
			// get local clip map clip-id vs clip object
			Map<String, Clip> localClipMap = getLocalClipMap(clips);
			log.info("ClipId vs clip object map is of size " + localClipMap.size());
			// get local category map category-id vs category object
			Map<String, Category> localCategoryMap = getLocalCategoryMap(categories);
			log.info("CategoryId vs category object map is of size " + localClipMap.size());

			for (SearchResult record : items) {
				Clip clip = localClipMap.get(record.getClipId());
				if (clip != null) {
					String parentCatId  = record.getParentCategoryId();
					String subCatId  = record.getSubCategoryId();
					LuceneClip lucClip = new LuceneClip(clip, 0, 0, null, null);
					if (parentCatId != null && !parentCatId.equalsIgnoreCase("null")
							&& localCategoryMap.containsKey(parentCatId)) {
						String parentCatName =  localCategoryMap.get(parentCatId).getCategoryName();
						lucClip.setParentCategoryId(Long.parseLong(parentCatId));
						lucClip.setParentCategoryName(parentCatName);
					}
					if (subCatId != null && !subCatId.equalsIgnoreCase("null")
							&& localCategoryMap.containsKey(subCatId)) {
						String subCatName = localCategoryMap.get(subCatId).getCategoryName();
						lucClip.setSubCategoryId(Long.parseLong(subCatId));
						lucClip.setSubCategoryName(subCatName);
					}
					// checking for duplicates
					if (!luceneClipList.contains(lucClip)) {
						luceneClipList.add(lucClip);
					}
				}
			}
			// pagenation logic has been moved to 
		} else {
			log.info("Found empty search result set");
		}
	    return luceneClipList;
	}
	
	public ArrayList<LuceneCategory> searchCategory(HashMap<String, String> paramsMap, int pageNo,
			int maxResults, String language) {
		return searchCategory(paramsMap, pageNo, maxResults, language, null);
	}
	
	public ArrayList<LuceneCategory> searchCategory(HashMap<String, String> paramsMap, int pageNo,
			int maxResults, String language, String queryLanguage) {
		// get search results
		ArrayList<LuceneCategory> luceneCatList = searchCategory(paramsMap, language, queryLanguage);
		// pagenation logic
		if (luceneCatList.size() > pageNo * maxResults) {
			int next = (pageNo + 1) * maxResults;
			int max = (luceneCatList.size() >= next) ? next : luceneCatList.size();
			luceneCatList = new ArrayList<LuceneCategory>(luceneCatList.subList(pageNo * maxResults, max));
		} else {
			luceneCatList = new ArrayList<LuceneCategory>();
		}
		return luceneCatList;
	}
	
	public ArrayList<LuceneCategory> searchCategory(HashMap<String, String> paramsMap, String language, String queryLanguage) {

		ArrayList<LuceneCategory> luceneCategoryList = new ArrayList<LuceneCategory>();
		
		if (paramsMap == null || paramsMap.size() == 0) {
			log.error("Input paramters map is null/empty");
			return luceneCategoryList;
		}
		
		// TODO: If only cat promo id is passed get it from memcache
		
		log.info("Input keywords: " + paramsMap);
		String strQuery = getQueryString(paramsMap);
		
		
		/*
		 * Get query language from LanguageCodeMap. 
		 * defaultLanguage as query language, if queryLanguage is null or queryLanguage is not exist in supportedLanguage list
		 */
		queryLanguage = getQueryLanguage(queryLanguage, strQuery);

		HashMap<String, String> fqMap = new HashMap<String, String>();
		fqMap.put(FIELD_SEARCH_TYPE, SEARCH_TYPE_CATEGORY);
		// search on only categories
		
//		if("TRUE".equalsIgnoreCase(isSupportLanguage)) {
//			
//			String tempLanguage = language;
//			if(tempLanguage == null || tempLanguage.equalsIgnoreCase("null") || (supportedLanguages != null && !supportedLanguages.contains(tempLanguage))) {
//				tempLanguage = defaultLanguage;
//			}
//			
//			fqMap.put(FIELD_LANGUAGE, tempLanguage.toUpperCase());
//		}
		
		
		String strFilterQuery = getFilterQueryString(fqMap); 

		if(language==null || language.equals("")) {
			language = defaultLanguage;
		}
		
		//TODO: read it from config
		// fields to get from solr 
	    String fields = "parentcatid,subcatid";
	    
	    log.debug("query: " + strQuery);
	    log.debug("filterQuery: " + strFilterQuery);
	    List<SearchResult> items = null;
		try {
			// getting only first 100 records
			items = executeQuery(strQuery, strFilterQuery, 0, 100, fields, queryLanguage);
		} catch (SolrServerException e) {
			log.error("Error while talking to solr server", e);
			return luceneCategoryList;
		}
	    
	    if (items != null && items.size() > 0) {
	    	
	    	log.info("Got search results of size " + items.size());
	    	
			for (SearchResult record : items) {
	    		String parentCatId = record.getParentCategoryId();
				String subCatId = record.getSubCategoryId();
				
				//Filtering category id
				if(filterCategoryIdsList.contains(subCatId)) {
					continue;
				}
				
				Category subCategory = cacheManager.getCategory(Integer.parseInt(subCatId));
				String categoryType = (null != subCategory) ? String.valueOf(subCategory.getCategoryTpe()) : null;
				boolean contains = categorTypesNotAllowedList.contains(categoryType);
				int size = categorTypesNotAllowedList.size();
				log.info("Category id: " + subCatId + ", category type: "
						+ categoryType + ", contains: " + contains);
				if (subCategory == null || contains) {
					log.debug("Not allowing Category, it is in not allowed list. id: " + subCatId
							+ ", category type: " + categoryType);
					continue;
				} else if ((size == 0) && (subCategory == null || subCategory.getCategoryTpe() == iRBTConstant.SHUFFLE
						|| subCategory.getCategoryTpe() == iRBTConstant.ODA_SHUFFLE || subCategory.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE)) {
					// show only the shuffle categories
					log.debug("Not allowing Category, category type shuffle. cat id: "
							+ subCatId + ", category type: " + categoryType);
					continue;
				}
				

				Category parentCategory = null;
				if (parentCatId != null && !parentCatId.equalsIgnoreCase("null")) {
					parentCategory = cacheManager.getCategory(Integer.parseInt(parentCatId));
				}
				
				LuceneCategory lucCategory = new LuceneCategory();
				lucCategory.setCategoryId(subCategory.getCategoryId());
				lucCategory.setCategoryName(subCategory.getCategoryName());
				lucCategory.setCategoryAskMobileNumber(subCategory.getCategoryAskMobileNumber());
				lucCategory.setCategoryEndTime(subCategory.getCategoryEndTime());
				lucCategory.setClassType(subCategory.getClassType());
				lucCategory.setCategoryGrammar(subCategory.getCategoryGrammar());
				lucCategory.setCategoryGreeting(subCategory.getCategoryGreeting());
				lucCategory.setCategoryNameWavFile(subCategory.getCategoryNameWavFile());
				lucCategory.setCategoryPreviewWavFile(subCategory.getCategoryPreviewWavFile());
				lucCategory.setCategoryPromoId(subCategory.getCategoryPromoId());
				lucCategory.setCategorySmsAlias(subCategory.getCategorySmsAlias());
				lucCategory.setCategoryStartTime(subCategory.getCategoryStartTime());
				lucCategory.setCategoryTpe(subCategory.getCategoryTpe());
				lucCategory.setMmNumber(subCategory.getMmNumber());
				
				if (parentCategory != null) {
					lucCategory.setParentCategoryId(Integer.parseInt(parentCatId));
					lucCategory.setParentCategoryName(parentCategory.getCategoryName());
				}
				
				// check for duplicate entries.
				if(!luceneCategoryList.contains(lucCategory)){
					luceneCategoryList.add(lucCategory);
				}
//				if (luceneCategoryList.size() >= maxResults) {
//					break;
//				}
	    	}
		} else {
			log.info("Found empty result set");
		}
	    return luceneCategoryList; 
	}
	
	public String getQueryString(HashMap<String, String> paramsMap) {
		StringBuilder queryString = new StringBuilder();
		
		Iterator<String> paramsItr = paramsMap.keySet().iterator();
		while (paramsItr.hasNext()) {
			String field = paramsItr.next();
			String value = paramsMap.get(field);
			// skipping invalid search strings
			if (field == null || field.trim().equals("") || value == null || value.trim().equals("")) {
				continue;
			}
			field = field.trim();
			value = value.trim();
			// normalizes the field name
			field = getFieldName(field);
			// for exact match binding in double quotes
			queryString.append(field).append(":").append("\"").append(value).append("\"").append(" ");
			if(isGramsEnabled) {
				String gramsQuery = getGramsQuery(getGramFieldName(field), value);
				// grams field name will be represented as <filed-name>_gr e.g., album => album_gr
				queryString.append(gramsQuery).append(" ");
			}
			if(isPhoneticsEnabled) {
				String phoneticsQuery = getPhoneticsQuery(getPhoneticFieldName(field), value);
				// phonetics field name will be represented as <filed-name>_ph e.g., album => album_ph
				queryString.append(phoneticsQuery).append(" ");
			}
		}
		return queryString.toString().trim();
	}
	
	public String getFilterQueryString(HashMap<String, String> paramsMap) {

		StringBuilder queryString = new StringBuilder();
		String andString = " AND ";
		Iterator<String> paramsItr = paramsMap.keySet().iterator();
		while (paramsItr.hasNext()) {
			String field = paramsItr.next();
			String value = paramsMap.get(field);
			queryString.append(field).append(":").append(value);
			queryString.append(" AND ");
		}
		String filterQuery = queryString.toString();
		filterQuery = filterQuery.substring(0, (filterQuery.length() - andString.length()));
//		return queryString.toString().trim();
		return filterQuery;
	}	
	public List<SearchResult> executeQuery(String strQuery, String strFilterQuery, int pageNo,
			int maxResults, String fields, String language) throws SolrServerException {
		// prepare query
	    SolrQuery solrQuery = new SolrQuery();
	    solrQuery.setQuery(strQuery);
	    solrQuery.setFilterQueries(strFilterQuery);
	    // sort by score
	    solrQuery.setSortField("score", ORDER.desc);
	    solrQuery.setStart(pageNo * maxResults);
	    solrQuery.setRows(maxResults);
	    // get specified fields only from solr service
	    solrQuery.setFields(fields);
	    QueryResponse rsp = serverMap.get(language.toUpperCase()).query( solrQuery, METHOD.POST );
		rsp.getResults();
	    List<SearchResult> items = rsp.getBeans(SearchResult.class);
	    return items;
	}
	
	private Map<String, Category> getLocalCategoryMap(Category categories[]) {
		Map<String, Category> categoryMap = new HashMap<String, Category>();
		if (categories != null && categories.length > 0) {
			for (int i = 0; i < categories.length; i++) {
				if (categories[i] != null) {
					categoryMap.put(categories[i].getCategoryId() + "", categories[i]);
				}
			}
		}
		return categoryMap;
	}
		
	private Map<String, Clip> getLocalClipMap(Clip clips[]) {
		Map<String, Clip> clipMap = new HashMap<String, Clip>();
		if (clips != null && clips.length > 0) {
			for (int i = 0; i < clips.length; i++) {
//				log.debug("Clip: " + clips[i]);
				if (clips[i] != null) {
					clipMap.put(clips[i].getClipId() + "", clips[i]);
				}
			}
		}
		return clipMap;
	}
}
