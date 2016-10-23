package com.onmobile.apps.ringbacktones.interfaces.sm.callback.core;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.CallbackProcessor;

public class SmCallbackServlet extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		CallbackWorkUnit callbackWorkUnit = new CallbackWorkUnit(request, response);
		CallbackProcessor.processCallback(callbackWorkUnit);
		callbackWorkUnit.log();
		response.getWriter().write(callbackWorkUnit.getResponseString());
		response.getWriter().flush();
		response.getWriter().close();
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException
	{
		doGet(request, response);
	}
}
