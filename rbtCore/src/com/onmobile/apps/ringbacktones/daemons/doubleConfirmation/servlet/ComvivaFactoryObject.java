package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.BasicResponseHandler;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class ComvivaFactoryObject {
	
	private static Logger logger = Logger.getLogger(ComvivaFactoryObject.class);
	
	private static String requestHandlerClassStr ;
	private static BasicRequestHandler requestHandler ;
	private static String defaultRequestHandler = "com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.servlet.BasicRequestHandler" ;
	private static Class<?> requestHandlerClass = null ;
	
	static {
		
		Parameters informationParam = CacheManagerUtil
				.getParametersCacheManager().getParameter(
						iRBTConstant.CONSENT, "COMVIVA_REQUEST_HANDLER_CLASS", defaultRequestHandler);
		
		requestHandlerClassStr = informationParam.getValue().trim();
		
		try {
			
			requestHandlerClass = Class.forName( requestHandlerClassStr );
		    requestHandler = ( BasicRequestHandler ) requestHandlerClass.newInstance();

		} catch ( ClassNotFoundException e ) {
			logger.error( "Param COMVIVA_REQUEST_HANDLER_CLASS not configured!!",e );
		} catch (InstantiationException e) {
			logger.error( "Unable to Instantiate COMVIVA_REQUEST_HANDLER_CLASS ",e );
		} catch (IllegalAccessException e) {
			logger.error( "IllegalAccessException ",e );
		}
		
	}
	
	public static BasicRequestHandler getRequestInstance(){
		return requestHandler ;
	}
	
	
	private static String responseHandlerClassStr ;
	private static BasicResponseHandler responseHandler ;
	private static String defaultResponseHandler = "com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads.BasicResponseHandler" ;
	private static Class<?> responseHandlerClass = null ;
	
	static{
		Parameters informationParam = CacheManagerUtil
				.getParametersCacheManager().getParameter(
						iRBTConstant.CONSENT, "COMVIVA_RESPONSE_HANDLER_CLASS", defaultResponseHandler);
		responseHandlerClassStr = informationParam.getValue().trim();
		
		try {
			
			responseHandlerClass = Class.forName( responseHandlerClassStr );
			responseHandler = ( BasicResponseHandler ) responseHandlerClass.newInstance();

		} catch ( ClassNotFoundException e ) {
			logger.error( "Param COMVIV_RESPONSE_HANDLER_CLASS not configured!!",e );
		} catch (InstantiationException e) {
			logger.error( "Unable to Instantiate COMVIVA_RESPONSE_HANDLER_CLASS ",e );
		} catch (IllegalAccessException e) {
			logger.error( "IllegalAccessException ",e );
		}
	}
	
	public static BasicResponseHandler getResponseInstance(){
		return responseHandler;
	}
	
}
