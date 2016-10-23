package com.onmobile.apps.ringbacktones.rbt2.response;

import org.apache.log4j.Logger;

public class ResponseHandlerFactory {

    private static Logger logger = Logger.getLogger(ResponseHandlerFactory.class);
    private static String responseHandlerClassStr;
	private static String defaultResponseHandler = "com.onmobile.apps.ringbacktones.rbt2.response.StringGriffResponseHandler";
    private static Class<?> responseHandlerClass = null;
    private static IResponseHandler responseHandler;

    public static IResponseHandler getResponseHandler(String responseHandlerName) {
    	
    	  if(responseHandlerName != null)
          responseHandlerClassStr = responseHandlerName;
    	
          if (null == responseHandlerClassStr || responseHandlerClassStr.trim().length() == 0) {
                responseHandlerClassStr = defaultResponseHandler;
          }
          try {
                responseHandlerClass = Class.forName(responseHandlerClassStr);
                responseHandler = (IResponseHandler) responseHandlerClass.newInstance();

          } catch (Exception e) {
                logger.error("Exception while getting Griff response class: ", e);
          }

          return responseHandler;

    }



}
