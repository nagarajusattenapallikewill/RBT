package com.onmobile.apps.ringbacktones.lucene.generic.msearch.searchimpl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.onmobile.apps.ringbacktones.lucene.generic.msearch.ResponseHandler;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchParams;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchResponse;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.utility.SolrFactory;

public class ResponseHandlerImpl implements ResponseHandler {
	static Logger log = Logger.getLogger(ResponseHandlerImpl.class);
	@Override
	public List<RBTMSearchResponse> callURL(RBTMSearchParams rbtmSearchParams,
			String solrServerURL) {
		/*
		 * String filePath = "D://JAXB//response.xml"; ApplicationContext
		 * context = SpringUtility.getApplicationContext(); SpringApp springApp
		 * = (SpringApp) context.getBean("springApp"); XMLProcess xmlProcess =
		 * (XMLProcess) context.getBean("xmlProcess"); RBTMSearchResponse
		 * rbtMSearchResponse = springApp.unMarshall(filePath, xmlProcess);
		 * if(rbtMSearchResponse!=null) System.out.println(rbtMSearchResponse);
		 * else System.out.println("No Response");
		 */
		try {
			HttpSolrServer server = (HttpSolrServer) SolrFactory
					.getSolrServerInstance(solrServerURL);
			ModifiableSolrParams params = new ModifiableSolrParams();
			if (rbtmSearchParams.getFq() != null
					&& !rbtmSearchParams.getFq().trim().isEmpty())
				params.set("fq", rbtmSearchParams.getFq());

			if (rbtmSearchParams.getStart() != null
					&& !rbtmSearchParams.getStart().trim().isEmpty())
				params.set("start", rbtmSearchParams.getStart());

			if (rbtmSearchParams.getRows() != null
					& !rbtmSearchParams.getRows().trim().isEmpty())
				params.set("rows", rbtmSearchParams.getRows());

			if (rbtmSearchParams.getWt() != null
					&& !rbtmSearchParams.getWt().trim().isEmpty())
				params.set("wt", rbtmSearchParams.getWt());

			if (rbtmSearchParams.getFl() != null
					&& !rbtmSearchParams.getFl().trim().isEmpty())
				params.set("fl", rbtmSearchParams.getFl());

			if (rbtmSearchParams.getQ() != null
					&& !rbtmSearchParams.getQ().trim().isEmpty())
				params.set("q", rbtmSearchParams.getQ());
			
			if (rbtmSearchParams.getQt() != null
					&& !rbtmSearchParams.getQt().trim().isEmpty())
				params.set("qt", rbtmSearchParams.getQt());
			
			/*
			 * params.set("language",rbtmSearchParams.g); params.set("type",
			 * "songName"); params.set("msisdn", "");
			 */

			List<RBTMSearchResponse> rbtmSearchResponses = null;
			QueryResponse response = null;
			try {
				log.info("Search Params sent to Solr are:: "+params);
				response = server.query(params);
				rbtmSearchResponses = response.getBeans(RBTMSearchResponse.class);
			} catch (SolrServerException e) {
				log.error("Solr exception while quering ",e);
			}
			return rbtmSearchResponses;
		} catch (Throwable th) {
			log.error("Throwable caught critical error, returning null as response",th);
			return new ArrayList<RBTMSearchResponse>();
		}
	}

}
