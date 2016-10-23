package com.onmobile.apps.ringbacktones.lucene.generic.msearch.searchimpl;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.lucene.generic.msearch.RBTMSearch;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.ResponseHandler;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchParams;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchResponse;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.utility.ConfigReader;


public class RBTMSearchImpl implements RBTMSearch {
	static Logger log = Logger.getLogger(RBTMSearchImpl.class);
	
	private static String solrServerUrl = null;
	/*static {
		solrServerUrl = ConfigReader.getParameter("SolrServerURL", null);
	}*/
	List<RBTMSearchResponse> rbtmSearchResponses = null;

	@Override
	public List<RBTMSearchResponse> searchClip(RBTMSearchParams rbtmSearchParams, String solrUrl) {
		log.info("Search Params :: "+ rbtmSearchParams);
		log.info("Solr URL :: "+ solrUrl);
		rbtmSearchResponses = getMSearchResults(rbtmSearchParams,solrUrl);
		return rbtmSearchResponses;
	}
	@Override
	public List<RBTMSearchResponse> searchClip(RBTMSearchParams rbtmSearchParams) {
		rbtmSearchResponses = getMSearchResults(rbtmSearchParams);

		/*
		 * if (rbtmSearchParams.getWt().equalsIgnoreCase("xml")) { context =
		 * SpringUtility.getApplicationContext(); ResponseHandler
		 * responseHandler = context.getBean( "xmlResponseHandlerImpl",
		 * ResponseHandlerImpl.class);
		 * responseHandler.callURL(rbtmSearchParams); } else if
		 * (rbtmSearchParams.getWt().equalsIgnoreCase("csv")) { context =
		 * SpringUtility.getApplicationContext(); ResponseHandler
		 * responseHandler = context.getBean( "csvResponseHandlerImpl",
		 * CSVResponseHandlerImpl.class);
		 * responseHandler.callURL(rbtmSearchParams); } else if
		 * (rbtmSearchParams.getWt().equalsIgnoreCase("json")) { context =
		 * SpringUtility.getApplicationContext(); ResponseHandler
		 * responseHandler = context.getBean( "jsonResponseHandlerImpl",
		 * JSONResponseHandlerImpl.class);
		 * responseHandler.callURL(rbtmSearchParams); } }
		 */
		return rbtmSearchResponses;
	}

	@Override
	public List<RBTMSearchResponse> searchNameTunes(RBTMSearchParams rbtmSearchParams) {
		rbtmSearchResponses = getMSearchResults(rbtmSearchParams);
		return rbtmSearchResponses;
	}

	@Override
	public List<RBTMSearchResponse> searchCategory(RBTMSearchParams rbtmSearchParams) {
		rbtmSearchResponses = getMSearchResults(rbtmSearchParams);
		return rbtmSearchResponses;
	}

	@Override
	public List<RBTMSearchResponse> SearchCategoryWithAlphabet(RBTMSearchParams rbtmSearchParams) {
		rbtmSearchResponses = getMSearchResults(rbtmSearchParams);
		return rbtmSearchResponses;
	}

	@Override
	public List<RBTMSearchResponse> suggestions(RBTMSearchParams rbtmSearchParams) {
		rbtmSearchResponses = getMSearchResults(rbtmSearchParams);
		return rbtmSearchResponses;
	}
	
	private List<RBTMSearchResponse> getMSearchResults(
			RBTMSearchParams rbtmSearchParams) {
		List<RBTMSearchResponse> rbtmSearchResponses = null;
		if (rbtmSearchParams != null) {
			ResponseHandler responseHandler = new ResponseHandlerImpl();
			rbtmSearchResponses = responseHandler.callURL(rbtmSearchParams,
					solrServerUrl);
		}
		return rbtmSearchResponses;
	}
	
	private List<RBTMSearchResponse> getMSearchResults(RBTMSearchParams rbtmSearchParams, String solrServerUrlWithCore) {
		List<RBTMSearchResponse> rbtmSearchResponses = null;
		if (rbtmSearchParams != null) {
			ResponseHandler responseHandler = new ResponseHandlerImpl();
			rbtmSearchResponses = responseHandler.callURL(rbtmSearchParams,
					solrServerUrlWithCore);
		}
		return rbtmSearchResponses;
	}

}
