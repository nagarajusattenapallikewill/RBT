package com.onmobile.apps.ringbacktones.daemons.interoperator.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;

public class InterCopyTransferToRDC extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public InterCopyTransferToRDC()
	{
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		this.servletConfig = servletConfig;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String responseText = null;
		try
		{
			responseText = InterOperatorUtility.addInterOperatorCopyRequestToDB(request);
		}
		catch (Exception e)
		{
			Logger.getLogger(InterCopyTransferToRDC.class).error("RBT:: " + e.getMessage(), e);
		}
		Logger.getLogger(InterCopyTransferToRDC.class).info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
