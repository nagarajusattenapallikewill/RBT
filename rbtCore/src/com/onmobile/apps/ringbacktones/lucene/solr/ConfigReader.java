package com.onmobile.apps.ringbacktones.lucene.solr;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

/**
 * @author laxmankumar
 * @author senthil.raja
 *
 */
public class ConfigReader {

	private static Logger log = Logger.getLogger(ConfigReader.class);
	
	private static ConfigReader instance = new ConfigReader();

	private ResourceBundle bundle = null;

	private ConfigReader() {
		bundle = ResourceBundle.getBundle("solrconfig");
	}

	public static ConfigReader getInstance() {
		return instance;
	}

	public String getParameter(String paramName) {
		String value = null;
		try {
			value = bundle.getString(paramName);
		} catch (MissingResourceException mr) {
			log.error("Property missing. propertyName: " + paramName + "in solrconfig.propeties" + "Exception: " +  mr);
		}
		return value;
	}

	public HashMap<String, String> getParameters(){
		HashMap<String,String> paramHashMap = new HashMap<String, String>();
		Enumeration<String> enumeration = bundle.getKeys();
		while(enumeration.hasMoreElements()){
			String key = enumeration.nextElement();
			String value = bundle.getString(key.trim());
			paramHashMap.put(key, value);
		}		
		return paramHashMap;
	}
	
}
