package com.onmobile.apps.ringbacktones.lucene.generic.msearch.utility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.impl.XMLResponseParser;

public class SolrFactory {

	private static Map<String, SolrServer> urlToServer = new ConcurrentHashMap<String, SolrServer>();

	public static SolrServer getSolrServerInstance(String solrUrl) {
		if (urlToServer.containsKey(solrUrl)) {
			return urlToServer.get(solrUrl);
		}
		HttpSolrServer serverInstance = new HttpSolrServer(solrUrl);
		urlToServer.put(solrUrl, serverInstance);
		configureSolrServer(serverInstance);
		return serverInstance;
	}

	private static void configureSolrServer(HttpSolrServer serverInstance) {
		serverInstance.setMaxRetries(1); // defaults to 0. > 1 not recommended.
		serverInstance.setConnectionTimeout(5000); // 5 seconds to establish TCP
		// The following settings are provided here for completeness.
		// They will not normally be required, and should only be used
		// after consulting javadocs to know whether they are truly required.
		serverInstance.setSoTimeout(1000); // socket read timeout
		serverInstance.setDefaultMaxConnectionsPerHost(100);
		serverInstance.setMaxTotalConnections(100);
		serverInstance.setFollowRedirects(false); // defaults to false
		// allowCompression defaults to false.
		// Server side must support gzip or deflate for this to have any effect.
		serverInstance.setAllowCompression(false);
		serverInstance.setParser(new XMLResponseParser());
	}

}
