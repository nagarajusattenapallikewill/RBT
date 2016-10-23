package com.onmobile.apps.ringbacktones.lucene.solr;

public class DefaultSearchClient extends SearchClient{

	private static SearchClient instance = null;

	public static SearchClient getSearchClient() {
		if(null == instance) {
			synchronized (SearchClient.class) {
				if(null == instance) {
					instance = new DefaultSearchClient(); 
				}
			}
		}
		return instance;
	}
	
	private DefaultSearchClient(){
		
	}
	
}
