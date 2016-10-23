package com.onmobile.apps.ringbacktones.utils;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

/**
 * @author khalid.imam
 *
 */
public class ObjectGsonUtils {

	
	static Logger logger = Logger.getLogger(ObjectGsonUtils.class);
	 int flag=0;
	public static String objectToGson(Object o){
		String s1;
		if(o==null)
			return null;
		Gson gson = new Gson();
		s1= gson.toJson(o);
		logger.info("JSON Object "+o.toString() + " and json response is " + s1);
	    return s1;
	}


}
