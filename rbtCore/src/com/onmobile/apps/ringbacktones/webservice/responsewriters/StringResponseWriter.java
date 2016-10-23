package com.onmobile.apps.ringbacktones.webservice.responsewriters;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 * 
 */
public class StringResponseWriter implements ResponseWriter
{
	private static Logger logger = Logger.getLogger(StringResponseWriter.class);

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.factory.ResponseWriter#
	 * writeResponse
	 * (com.onmobile.apps.ringbacktones.webservice.factory.WebServiceResponse)
	 */
	@Override
	public void writeResponse(WebServiceResponse webServiceResponse,
			HttpServletResponse httpServletResponse)
	{
		httpServletResponse.setContentType(webServiceResponse.getContentType());

		setHeaders(webServiceResponse, httpServletResponse);

		String response = webServiceResponse.getResponse();
		logger.debug("response: " + response);
		try
		{
			httpServletResponse.getWriter().write(response);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	private void setHeaders(WebServiceResponse webServiceResponse,
			HttpServletResponse httpServletResponse)
	{
		Map<String, String> headersMap = webServiceResponse
				.getResponseHeaders();

		Set<Entry<String, String>> entrySet = headersMap.entrySet();
		for (Entry<String, String> entry : entrySet)
		{
			httpServletResponse.addHeader(entry.getKey(), entry.getValue());
		}
	}
}