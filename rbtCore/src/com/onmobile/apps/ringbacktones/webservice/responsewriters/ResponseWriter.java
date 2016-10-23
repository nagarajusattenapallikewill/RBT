package com.onmobile.apps.ringbacktones.webservice.responsewriters;

import javax.servlet.http.HttpServletResponse;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;


/**
 * @author sridhar.sindiri
 *
 */
public interface ResponseWriter 
{
	public void writeResponse(WebServiceResponse webServiceResponse, HttpServletResponse httpServletResponse); 
}
