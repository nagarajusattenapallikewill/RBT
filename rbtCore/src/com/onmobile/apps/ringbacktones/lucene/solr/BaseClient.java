package com.onmobile.apps.ringbacktones.lucene.solr;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;

import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.common.LanguageIndentifier;
import com.tangentum.phonetix.DoubleMetaphone;

/**
 * @author laxmankumar
 * @author senthil.raja
 *
 */
public class BaseClient implements SearchConstants {

	private static Logger log = Logger.getLogger(BaseClient.class);
	
	protected Map<String, CommonsHttpSolrServer> serverMap = null;
	
	protected RBTCacheManager cacheManager = RBTCacheManager.getInstance();

	protected RBTClient rbtClient = RBTClient.getInstance();
	
	boolean isGramsEnabled = false;

	boolean isPhoneticsEnabled = false;

	boolean includeExpiredClips = false;
	
	protected String defaultLanguage = DEFAULT_LANGUAGE_VALUE;

	private DoubleMetaphone m_metaphone = null;
	
	int minGramSize = 3;
	int maxGramSize = 4;
	
	String regex = "";
	
	protected List<String> supportedLanguages = new ArrayList<String>();
	
	protected String isSupportLanguage = null;
	
	protected String useLangaugeIdentifier = null;

	BaseClient() {
		String url = ConfigReader.getInstance().getParameter(PARAM_SERVER_URL);
		if (url == null || url.trim().equals("")) {
			log.error("Mandatory parameter " + PARAM_SERVER_URL + " is missing/invalid");
			return;
		}
		
		isSupportLanguage = ConfigReader.getInstance().getParameter(SUPPORT_LANGUAGE);
		
		if("true".equalsIgnoreCase(isSupportLanguage)) {
			String supportedLangs = RBTContentJarParameters.getInstance().getParameter("supported_languages");
			log.info("SUPPORTED_LANGUAGES are " + supportedLangs);
			if (supportedLangs != null && !"".equals(supportedLangs)) {
				supportedLanguages = Arrays.asList(supportedLangs.split(","));
				log.info("Supported languages length " + supportedLanguages.size());
			}
			else {
				log.info("Supported Languages not present in rbtcontentjar.properties, so creating indexes for Default language");
			}
		}
		
		String tempDefaultLanguage = RBTContentJarParameters.getInstance().getParameter("default_language");
		log.info("DEFAULT LANGUAGE is " + tempDefaultLanguage);
		if (tempDefaultLanguage != null && !"".equals(tempDefaultLanguage)) {
			log.info("got defaultLanguage language from properties");
			defaultLanguage = tempDefaultLanguage;
		}
		else {
			log.info("There is not configuration for default language, default langauge is consider as " + defaultLanguage);
			defaultLanguage = DEFAULT_LANGUAGE_VALUE;
		}
		
		int iSocketTimeOut = 5000;
		String socketTimeOut = ConfigReader.getInstance().getParameter(PARAM_SOCKET_TIMEOUT);
		try {
			iSocketTimeOut = Integer.parseInt(socketTimeOut);
		} catch (NumberFormatException e) {
			// take default
		}
		
		int iConnTimeOut = 5000;
		String connTimeOut = ConfigReader.getInstance().getParameter(PARAM_CONNECTION_TIMEOUT);
		try {
			iConnTimeOut = Integer.parseInt(connTimeOut);
		} catch (NumberFormatException e) {
			// take default
		}
		

		//Configure slor core sever based on language
		try {
			serverMap = new HashMap<String, CommonsHttpSolrServer>();
			
			CommonsHttpSolrServer solrServer = new CommonsHttpSolrServer(url + "/" + defaultLanguage.toUpperCase());
			solrServer.setSoTimeout(iSocketTimeOut);
			solrServer.setConnectionTimeout(iConnTimeOut);
			solrServer.setDefaultMaxConnectionsPerHost(100);
			solrServer.setMaxTotalConnections(100);
			solrServer.setFollowRedirects(false);  // defaults to false
			
			// allowCompression defaults to false.
			// Server side must support gzip or deflate for this to have any effect.
			solrServer.setAllowCompression(true);
			
			// defaults to 0.  > 1 not recommended.
			solrServer.setMaxRetries(1);
			serverMap.put(defaultLanguage.toUpperCase(), solrServer);
			
			for(String supportedLang : supportedLanguages) {
				solrServer = new CommonsHttpSolrServer(url + "/" + supportedLang.toUpperCase());
				solrServer.setSoTimeout(iSocketTimeOut);
				solrServer.setConnectionTimeout(iConnTimeOut);
				solrServer.setDefaultMaxConnectionsPerHost(100);
				solrServer.setMaxTotalConnections(100);
				solrServer.setFollowRedirects(false);  // defaults to false
				// allowCompression defaults to false.
				// Server side must support gzip or deflate for this to have any effect.
				solrServer.setAllowCompression(true);
				// defaults to 0.  > 1 not recommended.
				solrServer.setMaxRetries(1);
				serverMap.put(supportedLang.toUpperCase(), solrServer);
			}
		} catch (MalformedURLException e) {
			log.error("Error while creating solr server object", e);
			return;
		}
		
		isGramsEnabled = Boolean.parseBoolean(ConfigReader.getInstance().getParameter(PARAM_GRAMS));
		isPhoneticsEnabled = Boolean.parseBoolean(ConfigReader.getInstance().getParameter(PARAM_PHONETICS));
		includeExpiredClips = Boolean.parseBoolean(ConfigReader.getInstance().getParameter(PARAM_INCLUDE_EXPIRED));

		if (isPhoneticsEnabled) {
			int phonetic_max_Length = 6;
			try {
				phonetic_max_Length = Integer.parseInt(ConfigReader.getInstance().getParameter(
						PARAM_PHONETIC_MAX_LENGTH).trim());
			} catch (NumberFormatException nfe) {
				log.warn(PARAM_PHONETIC_MAX_LENGTH + " should be number, now it takes default length is 6");
			}
			m_metaphone = new DoubleMetaphone(phonetic_max_Length);
		}
		
		if (isGramsEnabled) {
			try {
				minGramSize = Integer.parseInt(ConfigReader.getInstance().getParameter(PARAM_MIN_GRAM_SIZE));
				maxGramSize = Integer.parseInt(ConfigReader.getInstance().getParameter(PARAM_MAX_GRAM_SIZE));
			} catch (NumberFormatException e) {
				log.warn("Error while parsing minGramSize/maxGramSize values. Using defalut values", e);
				minGramSize = 3;
				maxGramSize = 4;
			}
		}
		
		regex = ConfigReader.getInstance().getParameter(PARAM_REGEX);
		
		useLangaugeIdentifier = ConfigReader.getInstance().getParameter(PARAM_USE_LANGUAGE_IDENTIFIER);
	}

	public String getFieldName(String fieldName){
		fieldName = fieldName.toLowerCase();
		fieldName = fieldName.replace("_", "");
		return fieldName;
	}
	
	public String getGramFieldName(String fieldName){
		fieldName = fieldName.toLowerCase();
		fieldName = fieldName.replace("_", "");
		return fieldName + "_gr";
	}
	
	public String getPhoneticFieldName(String fieldName){
		fieldName = fieldName.toLowerCase();
		fieldName = fieldName.replace("_", "");
		return fieldName + "_ph";
	}
	
	public String getPhoneticsQuery(String fieldName, String input) {
		String output = getPhonetics(input);
		if (output != null) {
			output = output.replaceAll(" ", " " + fieldName + ":");
			output = fieldName + ":" + output;
			return output;
		} 
		return "";
	}

	public String getPhonetics(String input) {
		String output = "";		
		if (input == null) {
			return null;
		}
		input = input.replaceAll(regex, "");
		try {
			// to check whether the input is a string or a number
			Long.valueOf(input);
//			return input;
			return null;
		} catch (Exception e) {
			//Phonetics not required for clip id, or vcode, or promo code.
		}
		String[] token = input.split(" ");
		int tokenSize = token.length;
		for (int i = 0; i < tokenSize; i++) {
			String str = token[i];
			if ("".equals(str)) {
				continue;
			}
			output = output + m_metaphone.generateKey(str) + " ";
		}
		input = input.replaceAll(" ", "");
		String phonem = m_metaphone.generateKey(input);
		if (phonem != null) {
			phonem=phonem.trim();
		}
		output = phonem +  " " + output.trim();
		return output;
	}

	protected String getGramsQuery(String field, String input) {
		String grams = getGrams(input);
		if (grams != null) {
			String grQuery = field + ":" + grams.replaceAll(" ", " " + field + ":");
			return grQuery;
		} 
		return "";
	}
	
	protected String getGrams(String input) {
		if (input == null) {
			return null;
		}
		input = input.replaceAll(regex, "");
		try {
			// to check whether the input is a string or a number
			Long.valueOf(input);
//			return input;
			return null;
		} catch (Exception e) {
			// grams not required for decimal values
		}
		input = input.replaceAll(" ", "");
		StringBuilder output = new StringBuilder();
		int size = input.length();
		for (int n = minGramSize; n <= maxGramSize; n++) {
			int i = 0;
			while (i < (size - n)){
				output.append(input.substring(i, (i + n)).trim()).append(" ");
				i++;
			}
			output.append(input.substring(i)).append(" ");
		}
		return output.toString().trim();
	}

	protected String getQueryLanguage(String queryLanguage, String strQuery) {
		/*
		 * Get query language from LanguageCodeMap. 
		 * defaultLanguage as query language, if queryLanguage is null or queryLanguage is not exist in supportedLanguage list
		 */
		if(queryLanguage == null) {
			if("YES".equalsIgnoreCase(useLangaugeIdentifier) || "TRUE".equalsIgnoreCase(useLangaugeIdentifier)) {
				queryLanguage = LanguageIndentifier.identifyLanguage(strQuery);
				log.info("Solr query language identfied by language identifier is: " + queryLanguage);
			}
			
			if(queryLanguage == null || !supportedLanguages.contains(queryLanguage)) {
				queryLanguage = defaultLanguage;
			}
		}
		
		log.info("Solr queryLanguage:" + queryLanguage);
		return queryLanguage;
	}
	
//	public static void main(String[] args) {
//		BaseClient bc = new BaseClient();
//		System.out.println(bc.getGrams(" onmobil'e   global ltd  "));
//		System.out.println(bc.getGramsQuery("X", " onm-obile   global ltd  "));
//		System.out.println(bc.getPhonetics("onmobile   g;lobal ltd  "));
//		System.out.println(bc.getPhoneticsQuery("X", " onmobile   glo:bal ltd  "));
//	}
}
