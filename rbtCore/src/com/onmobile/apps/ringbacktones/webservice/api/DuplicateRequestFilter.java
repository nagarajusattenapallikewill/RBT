package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.DuplicateRequestHandler;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class DuplicateRequestFilter implements Filter
{
	private static Logger logger = Logger.getLogger(DuplicateRequestFilter.class);

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy()
	{

	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
	{
		String subscriberID = request.getParameter(WebServiceConstants.param_subscriberID);
		String threadName = Thread.currentThread().getName();
		if (!DuplicateRequestHandler.addPendingRequestToMap(subscriberID, threadName))
		{
			response.setContentType("text/xml; charset=utf-8");
			String responseText = Utility.getRequestPendingXML();

			logger.warn("Request pending for the subscriber : " + subscriberID
					+ ", ThreadName : " + DuplicateRequestHandler.getThreadNameForRequestPendingSubscriber(subscriberID));
			response.getWriter().write(responseText);
		}
		else
		{
			try
			{
				filterChain.doFilter(request, response);
			}
			finally
			{
				logger.debug("Removing the subscriber from the pendingRequestMap : " + subscriberID);
				DuplicateRequestHandler.removePendingRequestFromMap(subscriberID);
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{

	}
}
