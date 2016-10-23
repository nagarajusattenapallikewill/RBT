package com.onmobile.apps.ringbacktones.lucene.msearch;

import java.util.List;

import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;

public class SearchResponse {
	String totalResults;
	List<LuceneCategory> results;

	public String getTotalResults() {
		return totalResults;
	}

	public List<LuceneCategory> getResults() {
		return results;
	}

	public SearchResponse(String totalResults, List<LuceneCategory> results) {
		this.results = results;
		this.totalResults = totalResults;
	}

}
