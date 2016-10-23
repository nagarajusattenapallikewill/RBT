package com.onmobile.apps.ringbacktones.lucene.solr;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

/**
 * @author senthil.raja
 *
 */
public class IndexCreator extends BaseClient{
	
	private static IndexCreator instance = null;
	
	public Logger log = Logger.getLogger(IndexCreator.class);
	
	public static IndexCreator getInstance(){
		if(instance == null){
			instance = new IndexCreator();
		}
		return instance;
		
	}
	
	int counter = 1;
	int size = 0;
	public void createIndex(Set<SolrInputDocument> docSet, String language) throws Exception{
		try{
			size += docSet.size();
			log.info("Document size : " + docSet.size());
			UpdateResponse response = serverMap.get(language.toUpperCase()).add(docSet.iterator());
			log.info("Solr core " + language.toUpperCase() + " Response Elapsed Time " + response.getElapsedTime());
			counter++;
			
		}
		catch(SolrServerException sse){
			log.error(sse.getMessage(),sse);
			throw sse;
		}
		catch(IOException ioe){
			log.error(ioe.getMessage(),ioe);
			throw ioe;
		}
		
	}
	
	public void commit(String language) throws SolrServerException, IOException {
		serverMap.get(language.toUpperCase()).commit();		
	}
	
	public void optimize(String language) throws SolrServerException, IOException {
		long start = System.currentTimeMillis();
		serverMap.get(language.toUpperCase()).optimize();
		log.info("Solr core " + language + " Successfully optimized Time taken : " + (System.currentTimeMillis() - start));
	}

}
