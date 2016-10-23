package com.onmobile.apps.ringbacktones.lucene.solr;

public class SearchClientFactory {

	private static String AIRTEL = "Airtel";
	private static String IDEA = "Idea";
	
	
	public static SearchClient getSearchClient(String operator) {
		
		if (AIRTEL.equalsIgnoreCase(operator)) {
			return AirtelSearchClient.getSearchClient();
		} else {
			return DefaultSearchClient.getSearchClient();
		}		
	}
}
