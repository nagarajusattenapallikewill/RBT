package com.onmobile.apps.ringbacktones.lucene;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.lucene.solr.ConfigReader;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchConstants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

/**
 * Factory class to get an instance of the AbstractLuceneIndexer.
 * This class does not give a singleton instance. 
 * AirtelLuceneIndexer is no more used as an implementation
 * The code has been merged into the DefaultLuceneIndexer.
 * @author manish.shringarpure
 *
 */

public class LuceneIndexerFactory {
	private static Logger log=Logger.getLogger(com.onmobile.apps.ringbacktones.lucene.LuceneIndexerFactory.class);
	private static AbstractLuceneIndexer indexer;
	private static String LUCENE_INDEXING_IMPL=null;
	private static final String OTHERS_INDEXER="AllLuceneIndexer";
	private static final String FULLY_QUALIFIED_CLASS_NAME_OTHERS = "com.onmobile.apps.ringbacktones.lucene.DefaultLuceneIndexer";
	private static final String FULLY_QUALIFIED_CLASS_NAME_OTHERS_SOLR_SUPPORT = "com.onmobile.apps.ringbacktones.lucene.DefaultSolrSupportIndexer";
	private static String USE_GENERIC_MSEARCH_IMPL = "false";

	/**
	 * This is not a singleton instance creator. 
	 * @return AbstractLuceneIndexer
	 */
	public static AbstractLuceneIndexer getInstance(){
		try{
			//Does not give you a singleton object of the luceneIndexer, 
				LUCENE_INDEXING_IMPL=OTHERS_INDEXER;
				Class indexerClass = null;
				if(LUCENE_INDEXING_IMPL.equals(OTHERS_INDEXER)){
					log.info("Instantiating the impl for "+OTHERS_INDEXER);
					try{
						//if from rbt params
						//RBTClient client=RBTClient.getInstance();
						//String solrSupport=(client.getInstance().getParameter(new ApplicationDetailsRequest("CCC","SOLR_SEARCH_SUPPORT",(String)null))).getValue();
						
						//if from solrconfig
						USE_GENERIC_MSEARCH_IMPL = ConfigReader.getInstance().getParameter(SearchConstants.GENERIC_MSEARCH_IMPL);
						if("true".equalsIgnoreCase(USE_GENERIC_MSEARCH_IMPL)){
							indexerClass = Class.forName(FULLY_QUALIFIED_CLASS_NAME_OTHERS_SOLR_SUPPORT);
						}
					}
					catch(Exception e){
						log.error(e.getMessage());
						e.printStackTrace();
					}
					
					if(null == indexerClass){
						indexerClass = Class.forName(FULLY_QUALIFIED_CLASS_NAME_OTHERS);	
					}
					indexer=(AbstractLuceneIndexer)indexerClass.newInstance();
					log.info("Got an instance of "+indexerClass.getName());
				}
		}
		catch(ClassNotFoundException cnfe){
			cnfe.getMessage();
			log.error("ClassNotFoundException while instantiating the AbstractLuceneIndexer");
			cnfe.printStackTrace();
		}
		catch (InstantiationException e){
			e.getMessage();
			log.error("Instantiation Exception while instantiating the AbstractLuceneIndexer");
			e.printStackTrace();
		}
		catch (IllegalAccessException e){
			e.getMessage();
			log.error("IllegalAccessException while instantiating the AbstractLuceneIndexer");
			e.printStackTrace();
		}
		catch(Throwable e){
			e.getMessage();
			log.error("Exception while instantiating the AbstractLuceneIndexer");
			e.printStackTrace();
		}
		return indexer;
	}
	
}
