package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.MDC;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
/**
 * Servlet Filter implementation class CharacterEncodingFilter
 */
public class CharacterEncodingFilter implements Filter, iRBTConstant
{
	private String charset = "UTF-8"; 
	/**
	 * Default constructor. 
	 */
	public CharacterEncodingFilter()
	{

	}

	/**
	 * @see Filter#destroy()
	 */
	public void destroy()
	{

	}

	/**
	 * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		request.setCharacterEncoding(charset);
		response.setCharacterEncoding(charset);
		
		String msisdn = getMsisdnFromRequest(request);
		if(msisdn != null)
			MDC.put(mdc_msisdn, msisdn);
		chain.doFilter(request, response);
		MDC.remove(mdc_msisdn);
	}

	private String getMsisdnFromRequest(ServletRequest request)
	{
		String msisdn = request.getParameter(WebServiceConstants.param_subscriberID);
		if(msisdn == null)
			msisdn = request.getParameter(Constants.CALLER_ID);
		if(msisdn == null)
			msisdn = request.getParameter(Constants.param_MSISDN);
		if(msisdn == null)
			msisdn = request.getParameter(Constants.param_subID);
		if(msisdn == null)
			msisdn = request.getParameter(Constants.param_msisdn);
		if(msisdn == null)
			msisdn = request.getParameter(Constants.param_MSISDN);		
		return msisdn;
	}


	/**
	 * @see Filter#init(FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{
		String charsetParam = filterConfig.getInitParameter("charset");
		if (charsetParam != null)
			charset = charsetParam; 
	}
}
