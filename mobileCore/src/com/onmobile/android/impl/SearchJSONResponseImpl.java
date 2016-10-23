package com.onmobile.android.impl;

import java.util.List;

import com.onmobile.android.beans.CategoryBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.interfaces.SearchResponse;
import com.onmobile.android.managers.SearchManager;
import com.onmobile.android.utils.ObjectGsonUtils;

public class SearchJSONResponseImpl implements SearchResponse {

	private SearchManager searchManager = new SearchManager();
	
	@Override
	public String searchClipsByNameTune(String searchString, String criteria, int pageNo, String language, String subscriberId, int maxResults) {
		List<ExtendedClipBean> extendedSearchResultClips = searchManager.searchClipsByNameTune(searchString, criteria, pageNo, language, subscriberId, maxResults);
		String jsonClips = null;
		jsonClips = ObjectGsonUtils.objectToGson(extendedSearchResultClips);
		return jsonClips;
	}

	@Override
	public String createNameTune(String searchString, String language,
			String subscriberId) {
		String response = null;
		Boolean callGetNameTuneResponse =  PropertyConfigurator.isNameTuneApiRequired() ;
		if(callGetNameTuneResponse){
			response= searchManager.createNameTuneByOnlineAPI(searchString, language, subscriberId);
		} else{
			response=searchManager.createNameTune(searchString, language, subscriberId);
		}
		return ObjectGsonUtils.objectToGson(response);
	}
	
	@Override
	public String searchSuggestions(String searchText) {
		String responseText = searchManager.searchSuggestions(searchText);
		return responseText;
	}

	@Override
	public String searchForArtists(String search, int maxResults) {
		List<String> artistList = searchManager.searchForArtists(search, maxResults);
		return (ObjectGsonUtils.objectToGson(artistList)) ;
	}

	@Override
	public String searchForPlaylists(String search, int maxResults) {
		List<CategoryBean> categoryList = searchManager.searchForPlaylists(search, maxResults);
		return (ObjectGsonUtils.objectToGson(categoryList)) ;
	}
	
	@Override
	public String searchClips(String searchString, String criteria, int pageNo, String subId, int maxResults, String language) {
		List<ExtendedClipBean> extendedSearchResultClips = searchManager.searchClips(searchString, criteria, pageNo, subId, maxResults,language);
		String jsonClips = null;
		jsonClips = ObjectGsonUtils.objectToGson(extendedSearchResultClips);
		return jsonClips;
	}
}
