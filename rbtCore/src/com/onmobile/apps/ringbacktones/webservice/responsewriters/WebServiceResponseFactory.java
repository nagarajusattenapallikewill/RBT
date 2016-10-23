package com.onmobile.apps.ringbacktones.webservice.responsewriters;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class WebServiceResponseFactory implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(WebServiceResponseFactory.class);
	
	private static Map<Class<? extends ResponseWriter>, ResponseWriter> ResponseWritersMap = new HashMap<Class<? extends ResponseWriter>, ResponseWriter>();

	public static ResponseWriter getResponseWriter(Class<? extends ResponseWriter> ResponseWriterClass)
	{
		ResponseWriter responseWriter = ResponseWritersMap.get(ResponseWriterClass);
		if (responseWriter == null)
		{
			try
			{
				synchronized (WebServiceResponseFactory.class)
				{
					responseWriter = ResponseWritersMap.get(ResponseWriterClass);
					if (responseWriter == null)
					{
						responseWriter = ResponseWriterClass.newInstance();
						ResponseWritersMap.put(ResponseWriterClass, responseWriter);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Unable to create instance for " + ResponseWriterClass.getName(), e);
			}
		}
		return responseWriter;
	}
}
