package com.onmobile.apps.ringbacktones.lucene.generic.msearch;

import java.util.List;

import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchParams;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchResponse;

public interface ResponseHandler {
	
	public List<RBTMSearchResponse> callURL(RBTMSearchParams rbtmSearchParams, String solrServerURL);

}
