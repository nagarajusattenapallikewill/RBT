package com.onmobile.android.managers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.CategoryBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.configuration.HttpConfigurations;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.CategoryUtils;
import com.onmobile.android.utils.ClipUtils;
import com.onmobile.android.utils.StringConstants;
import com.onmobile.android.utils.Utility;
import com.onmobile.android.utils.http.HttpUtils;
import com.onmobile.apps.ringbacktones.lucene.AbstractLuceneIndexer;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.lucene.LuceneIndexerFactory;
import com.onmobile.apps.ringbacktones.lucene.msearch.SearchResponse;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchConstants;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;

public class SearchManager {

	private static Logger logger = Logger.getLogger(SearchManager.class);
	
	public List<ExtendedClipBean> searchClips(String searchString, String criteria, int pageNo, String subId, int maxResults, String language) {
		List<ExtendedClipBean> searchClipList = new ArrayList<ExtendedClipBean>();
		ArrayList<LuceneClip> luceneClipList = null;
		AbstractLuceneIndexer indexer = LuceneIndexerFactory.getInstance();
		HashMap<String, String> searchMap = new HashMap<String, String>();
		if (Utility.isStringValid(subId)) {
			searchMap.put("SUBSCRIBER_ID", subId);
		}
		if (maxResults == -1 ) {
			maxResults = PropertyConfigurator.getClipRowCount();
		}
		logger.info("criteria: " + criteria + ", searchString: " + searchString
				+ ", maxResults: " + maxResults + ", PageNo: " + pageNo);
		logger.info("Lucene Indexer: "+ indexer);
		if(language == null || (language=language.trim()).equals("")){
			language = StringConstants.defaultLanguage;
		}
		if(Utility.isStringValid(criteria)){
			searchMap.put(criteria.toLowerCase(), searchString);
			luceneClipList = indexer.searchQuery(searchMap, pageNo, maxResults, language,true,false,language);

		}else{
			searchMap.put("album", searchString);
			searchMap.put("song", searchString);
			searchMap.put("artist", searchString);
			luceneClipList = indexer.searchQuery(searchMap, pageNo, maxResults, language, true, true,language);
		}

		if(luceneClipList!=null && luceneClipList.size()!=0){
			logger.info("clipList size :"+ luceneClipList.size());
			String[] clipIds = new String[luceneClipList.size()];
			for(int i=0; i<luceneClipList.size(); i++){
				clipIds[i] = String.valueOf(luceneClipList.get(i).getClipId());
			}
			searchClipList = ClipUtils.getExtendedClipListByClipIds(clipIds, subId, null, null);
		}
		return searchClipList;
	}

	public List<ExtendedClipBean> searchClipsByNameTune(String searchString,
			String criteria, int pageNo, String language, String subscriberId, int maxResults) {
		List<ExtendedClipBean> searchClipList = new ArrayList<ExtendedClipBean>();
		ArrayList<LuceneClip> luceneClipList = null;
		AbstractLuceneIndexer indexer = LuceneIndexerFactory.getInstance();
		HashMap<String, String> searchMap = new HashMap<String, String>();
		if (maxResults == -1 ) {
			maxResults = PropertyConfigurator.getClipRowCount();
		}
		logger.info("criteria: " + criteria + " searchString: " + searchString
				+ " maxResult: " + maxResults
				+ " PageNo: " + pageNo);
		logger.info("Lucene Indexer: " + indexer);
		if (language == null)
			language = StringConstants.defaultLanguage;

		Boolean isSolrNameTuneSearch = PropertyConfigurator.getIsSolrNameTuneSearch();

		if (isSolrNameTuneSearch) {
			searchMap.put(SearchConstants.DIALER_TONE_NAME,searchString);
			luceneClipList = indexer.multiFeildmsearch(searchMap, pageNo, maxResults, language);
		} else {
			if (criteria != null) {
				searchMap.put(criteria, searchString);
				searchMap.put("SUBSCRIBER_ID", subscriberId);
				searchMap.put("searchChannel","MOBILEAPP");
				luceneClipList = indexer.searchByNametune(searchMap, pageNo,
						maxResults,
						language, language);

			} else {
				searchMap.put("album", searchString);
				searchMap.put("song", searchString);
				searchMap.put("artist", searchString);
				searchMap.put("SUBSCRIBER_ID", subscriberId);
				luceneClipList = indexer.searchByNametune(searchMap, pageNo,
						maxResults,
						language, language);
			}

		}
		if (luceneClipList != null && luceneClipList.size() != 0) {
			logger.info("clipList size :" + luceneClipList.size());
			String[] clipIds = new String[luceneClipList.size()];
			for (int i = 0; i < luceneClipList.size(); i++) {
				clipIds[i] = String.valueOf(luceneClipList.get(i).getClipId());
			}
			searchClipList = ClipUtils.getExtendedClipListByClipIds(clipIds, subscriberId, null, null);
		}
		return searchClipList;
	}

	public String searchSuggestions(String searchText) {
		String url = PropertyConfigurator.getSuggestionSearchUrl();
		if (!Utility.isStringValid(url)) {
			logger.error("Suggestion URL is empty!. searchText: " + searchText + ", Returning null.");
			return null;
		}
		if (url.indexOf("%TEXT%") == -1) {
			logger.error("Suggestion URL does not contain the searchText placeholder, %TEXT%. searchText: " + searchText + ", Returning null.");
			return null;
		}
		url = url.replaceAll("%TEXT%", searchText);
		logger.info("Redirecting to URL: " + url);
		HttpParameters httpParameters = new HttpParameters(HttpConfigurations.isUseProxy(), HttpConfigurations.getProxyHost(),
				HttpConfigurations.getProxyPort(), HttpConfigurations.getHttpConnectionTimeout(), HttpConfigurations.getHttpSoTimeout(),
				HttpConfigurations.getMaxTotalHttpConnections(), HttpConfigurations.getMaxHostHttpConnections());
		httpParameters.setUrl(url);
		String responseText = HttpUtils.makeRequest(null, httpParameters, null,false);
		logger.debug("Search suggestions done for searchText: " + searchText);
		return responseText;
	}

	public String createNameTune(String searchString, 
			String language, String subscriberId) {
		logger.info("Inside createNameTune API");
		String nameTuneTxt = "failed" ;
		String filePath =  PropertyConfigurator.getSharedPath();
		logger.info("filePath :" + filePath);
		logger.info("searchString :" + searchString +"subscriberId :" + subscriberId );
		FileWriter outFile = null;
		PrintWriter out =  null;
		try {
			outFile = new FileWriter(filePath+subscriberId+"_"+System.currentTimeMillis() +".txt");
			out = new PrintWriter(outFile);
			out.write(searchString);
			nameTuneTxt = PropertyConfigurator.getNameTuneText();
			logger.info("nameTuneTxt :" + nameTuneTxt);
		} catch (IOException e) {

		} finally {
			if(out != null) {
				out.close();
			}
		}
		return nameTuneTxt;
	}
	
	public String createNameTuneByOnlineAPI(String searchString, 
			String language, String subscriberId){
		logger.info("Inside createNameTuneByOnlineAPI API");
		String url = PropertyConfigurator.getNameTuneUrl();
		if (!Utility.isStringValid(url)) {
			logger.error("Suggestion URL is empty!. searchText: " + ", Returning null.");
			return null;
		}
		url = url.replace("<query>", searchString);
		url = url.replace("<msisdn>", subscriberId);
		if(language == null || language.isEmpty()){
			language =  PropertyConfigurator.getLanguage();
		}
		url = url.replace("<language>", language);
		logger.info("Redirecting to URL: " + url);
		HttpParameters httpParameters = new HttpParameters(HttpConfigurations.isUseProxy(), HttpConfigurations.getProxyHost(),
				HttpConfigurations.getProxyPort(), HttpConfigurations.getHttpConnectionTimeout(), HttpConfigurations.getHttpSoTimeout(),
				HttpConfigurations.getMaxTotalHttpConnections(), HttpConfigurations.getMaxHostHttpConnections());
		httpParameters.setUrl(url);
		String responseText = HttpUtils.makeRequest(null, httpParameters, null,true);
		if(responseText ==  null){
			responseText = "FAILURE";
		}
		return responseText;
		
		
	}

	public List<String> searchForArtists(String search, int maxResults) {
		logger.debug("search: " + search + ", maxResults: " + maxResults);
		if (maxResults == -1) {
			maxResults = PropertyConfigurator.getArtistRowCount();
		}
		AbstractLuceneIndexer indexer = LuceneIndexerFactory.getInstance();
		ArrayList<String> artistList = indexer.searchForArtists(search, maxResults);
		logger.info("artists: " + artistList);
		return artistList;
	}

	public List<CategoryBean> searchForPlaylists(String search,
			int maxResults) {
		logger.debug("search: " + search + ", maxResults: " + maxResults);
		if (maxResults == -1) {
			maxResults = PropertyConfigurator.getCategoryRowCount();
		}
		AbstractLuceneIndexer indexer = LuceneIndexerFactory.getInstance();
		SearchResponse searchResponse = indexer.searchCategory(search, maxResults);
		List<CategoryBean> categoryList = CategoryUtils.getCategoryBeanListFromSearchResponse(searchResponse);
		logger.info("categoryList.size: " + categoryList.size());
		return categoryList;
	}
}
