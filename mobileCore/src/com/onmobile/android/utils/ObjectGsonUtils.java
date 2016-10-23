package com.onmobile.android.utils;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.onmobile.android.configuration.PropertyConfigurator;

/**
 * @author khalid.imam
 *
 */
public class ObjectGsonUtils {
	
	private static String dateFormat;
//	private static ObjectGsonUtils objectGsonUtils = null;
	static {
		dateFormat = PropertyConfigurator.getDateFormat();
	}
	
	static Logger logger = Logger.getLogger(ObjectGsonUtils.class);
	 int flag=0;
	public static String objectToGson(Object o){
		String s1;
		if(o==null)
			return null;
		Gson gson = null;
		if(dateFormat != null && dateFormat.length() > 0) {
			gson = new GsonBuilder().setDateFormat(dateFormat).create();
		} else {
			 gson = new Gson();
		}
		s1= gson.toJson(o);
		if (PropertyConfigurator.isEncryptionEnabled()) {
			JsonElement jsonElement = new JsonParser().parse(s1);
			doEncryption(jsonElement);
			s1 = jsonElement.toString();
		}
		logger.info("JSON Object "+o.toString() + " and json response is " + s1);
		return s1;
	}
	//	
	//	public synchronized static ObjectGsonUtils getInstance(){
	//		if(objectGsonUtils==null)
	//		{
	//			objectGsonUtils = new ObjectGsonUtils();
	//		}
	//		return objectGsonUtils;
	//	}
	private static void doEncryption(JsonElement s1) {
		if (s1.isJsonArray()) {
			for (JsonElement element : s1.getAsJsonArray()) {
				doEncryption(element);
			}
		} else if (s1.isJsonObject()) {
			JsonObject obj = s1.getAsJsonObject();
			if (obj.has("subscriberID")) {
				JsonElement s = obj.get("subscriberID");
				String subId = s.getAsString();
				String encSubId =  AESUtils.encrypt(subId, PropertyConfigurator.getResponseSubscriberIdEncryptionKey());
				if (encSubId != null) {
					obj.addProperty("subscriberID", encSubId);
				}
			}
		}
	}
}