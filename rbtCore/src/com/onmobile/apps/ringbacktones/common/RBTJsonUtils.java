package com.onmobile.apps.ringbacktones.common;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * Utility class to convert from json and vice-versa.
 * 
 * @author rajesh.karavadi
 * @Since Aug 20, 2013
 */
public class RBTJsonUtils {

	private static Logger logger = Logger.getLogger(RBTJsonUtils.class);

	private static ObjectMapper mapper = new ObjectMapper();

	/**
	 * Convert given object to Json string
	 * 
	 * @param obj
	 * @return Json String
	 */
	public static String convertToJson(Object obj) {
		String str = null;
		try {
			str = mapper.writeValueAsString(obj);
		} catch (JsonGenerationException jge) {
			logger.error("Failed convert from Object: " + obj
					+ ". JsonGenerationException: " + jge.getMessage(), jge);
		} catch (JsonMappingException jme) {
			logger.error("Failed convert from Object: " + obj
					+ ". JsonMappingException: " + jme.getMessage(), jme);
		} catch (IOException ioe) {
			logger.error("Failed convert from Object: " + obj
					+ ". IOException: " + ioe.getMessage(), ioe);
		}
		logger.debug("Converted Object: " + obj + ", to string: " + str);
		return str;
	}

	/**
	 * Converts given Json string to object of type given class.
	 * 
	 * @param jsonString
	 * @param clazz
	 * @return object
	 */
	public static Object convertFromJson(String jsonString, Class<?> clazz) {
		Object obj = null;
		try {
			if (null != jsonString) {
				obj = mapper.readValue(jsonString, clazz);
			}
		} catch (JsonParseException jpe) {
			logger.error("Failed convert Object from str: " + jsonString
					+ ". JsonParseException: " + jpe.getMessage(), jpe);
		} catch (JsonMappingException jme) {
			logger.error("Failed convert Object from str: " + jsonString
					+ ". JsonMappingException: " + jme.getMessage(), jme);
		} catch (IOException ioe) {
			logger.error("Failed convert Object from str: " + jsonString
					+ ". IOException: " + ioe.getMessage(), ioe);
		}
		logger.debug("Converted str: " + jsonString + ", to object: " + obj);
		return obj;
	}

}

