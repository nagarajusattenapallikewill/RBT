package com.onmobile.apps.ringbacktones.lucene.generic.msearch;

import java.util.List;

import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchParams;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchResponse;


public interface RBTMSearch {

	public List<RBTMSearchResponse> searchClip(RBTMSearchParams rbtmSearchParams);
	
	public List<RBTMSearchResponse> searchClip(RBTMSearchParams rbtmSearchParams, String solrUrl);
	
	public List<RBTMSearchResponse> searchNameTunes(RBTMSearchParams rbtmSearchParams);
	
	public List<RBTMSearchResponse> searchCategory(RBTMSearchParams rbtmSearchParams);
	
	public List<RBTMSearchResponse> SearchCategoryWithAlphabet(RBTMSearchParams rbtmSearchParams);
	
	public List<RBTMSearchResponse> suggestions(RBTMSearchParams rbtmSearchParams);
}
