package com.onmobile.android.factory;

import org.apache.log4j.Logger;

import com.onmobile.android.impl.ContentJSONResponseImpl;
import com.onmobile.android.impl.SearchJSONResponseImpl;
import com.onmobile.android.impl.SubscriberJSONResponseImpl;
import com.onmobile.android.interfaces.ContentResponse;
import com.onmobile.android.interfaces.SearchResponse;
import com.onmobile.android.interfaces.SubscriberResponse;

public class ResponseFactory {
	private static Logger logger = Logger.getLogger(ResponseFactory.class);
	public final static int RESPONSE_TYPE_XML=1;
	public final static int RESPONSE_TYPE_JSON=2;
	
	public final static String RBT_CHANNEL = "MOBILEAPP" ;
	
	
	public static ContentResponse getContentResponse(int responseType){
		logger.info("inside getContentResponse");
		ContentResponse response = null;
		if(responseType==RESPONSE_TYPE_JSON){
			logger.info("inside JSON");
			response = new ContentJSONResponseImpl();
			logger.info("returning contentJSONResponseImpl");
		}/*else if(responseType==RESPONSE_TYPE_XML){
			logger.info("inside XML");
			response = new ContentXMLResponseImpl();
			logger.info("returning contentXMLResponseImpl");
		}*/
		return response;
	}
	
	public static SubscriberResponse getSubscriberResponse(int responseType, String channel){
		SubscriberResponse response = null;
		if(responseType==RESPONSE_TYPE_JSON){
			response = new SubscriberJSONResponseImpl(channel);
		}/*else if(responseType==RESPONSE_TYPE_XML){
			response = new SubscriberXMLResponseImpl();
		}*/
		return response;
	}
	
	public static SearchResponse getSearchResponse(int responseType){
		logger.info("inside getSearchResponse");
		SearchResponse response = null;
		if(responseType==RESPONSE_TYPE_JSON){
			logger.info("inside JSON");
			response = new SearchJSONResponseImpl();
			logger.info("returning searchJSONResponseImpl");
		}
		return response;
	}

}
