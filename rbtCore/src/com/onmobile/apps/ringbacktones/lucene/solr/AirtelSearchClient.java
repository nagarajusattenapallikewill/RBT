package com.onmobile.apps.ringbacktones.lucene.solr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;

public class AirtelSearchClient extends SearchClient{

	private static SearchClient instance = null;
	private static Logger log = Logger.getLogger(AirtelSearchClient.class);

	public static SearchClient getSearchClient() {
		if(null == instance) {
			synchronized (SearchClient.class) {
				if(null == instance) {
					instance = new AirtelSearchClient(); 
				}
			}
		}
		return instance;
	}
	
	private AirtelSearchClient(){
		
	}
	
	public ArrayList<LuceneCategory> searchCategory(HashMap<String, String> paramsMap, String language) {
		return searchCategory(paramsMap, language, null);
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

		HashMap<String, String> fqMap = new HashMap<String, String>();
		fqMap.put(FIELD_SEARCH_TYPE, SEARCH_TYPE_CATEGORY);
		// search on only categories
		String strFilterQuery = getFilterQueryString(fqMap); 

		if(language==null || language.equals("")) {
			language = defaultLanguage;
		}
		
		//TODO: read it from config
		// fields to get from solr 
	    String fields = "parentcatid,subcatid";
	    
	    log.info("query: " + strQuery);
	    log.info("filterQuery: " + strFilterQuery);
	    List<SearchResult> items = null;
	    
	    //Get queryLanguage from queryString
	    queryLanguage = getQueryLanguage(queryLanguage, strQuery);
	    
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
				Category subCategory = cacheManager.getCategory(Integer.parseInt(subCatId));
				// show only the ODA shuffle categories
				/*if (subCategory == null || subCategory.getCategoryTpe() != 16) {
					continue;
				}*/
				//RBT-12933 commented and added for checking cat type from configuration BOQUET_CAT_TYPE
				ArrayList boquetCatTypeList =Tools.tokenizeArrayList(RBTParametersUtils.getParamAsString("COMMON", "BOQUET_CAT_TYPE","16"), null) ;
				if(subCategory==null || !boquetCatTypeList.contains(subCategory.getCategoryTpe()+"")){
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

}
