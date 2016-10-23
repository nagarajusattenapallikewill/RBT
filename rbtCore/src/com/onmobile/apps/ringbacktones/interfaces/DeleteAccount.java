package com.onmobile.apps.ringbacktones.interfaces;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.subscriptions.RBTTataGSMImpl;

/**
 * Servlet implementation class for Servlet: BookMark
 *
 */
public class DeleteAccount extends javax.servlet.http.HttpServlet implements javax.servlet.Servlet, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(DeleteAccount.class);
	static final long serialVersionUID = 1L;

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public DeleteAccount()
	{
		super();

		Tools.init("Interfaces", true);
	}   	

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		Map<String, String[]> paramMap = request.getParameterMap();
		Map<String, String[]> dummyMap = new HashMap<String,String[]>();
		
		Set<String> keys=paramMap.keySet();
		Iterator<String> itr=keys.iterator();
		while (itr.hasNext()){
			
			String key=itr.next().toString();
			if (key!=null){
				dummyMap.put(key,(String[])paramMap.get(key));
			}
			
		}
		
		String responseStr = Utility.getErrorXML();
		logger.info("entered");
		
		try
		{
			String[] param_request={"3"};
			if (dummyMap!=null)
				dummyMap.put("request",param_request);
			responseStr = RBTTataGSMImpl.getInstance().processRequest(dummyMap);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: responseText: "+ responseStr);
		response.getWriter().write(responseStr);
	}  	

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}   	  	    
}