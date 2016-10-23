package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jspsmart.upload.File;
import com.jspsmart.upload.Files;
import com.jspsmart.upload.SmartUpload;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RBTDownloadFile extends HttpServlet implements WebServiceConstants
{
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(Offer.class);

	/*
	 * (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request,
	 * HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		response.setContentType("text/xml; charset=utf-8");
		String responseText = Utility.getResponseXML("FAILURE");
		String filePath = null;		
		try
		{
			SmartUpload smartUpload = new SmartUpload();
			smartUpload.initialize(getServletConfig(), request, response);
			smartUpload.setTotalMaxFileSize(20000000);
			smartUpload.upload();
			
			String type = smartUpload.getRequest().getParameter("type").toUpperCase();
			if (type == null) {
				type = request.getParameter("type");
			}
			String fileName = smartUpload.getRequest().getParameter("fileName");
			if (fileName == null) {
				fileName = request.getParameter("fileName");
			}
			filePath = CacheManagerUtil.getParametersCacheManager().getParameterValue("WEBSERVICE", "DOWNLOAD_PATH_" + type, null);
			if(filePath != null) {
				filePath = filePath + java.io.File.separator + fileName;
				Files files = smartUpload.getFiles();
				for(int i=0;i<files.getCount();i++){
					File file = files.getFile(i);
					file.saveAs(filePath); 
					logger.info(filePath+" uploaded sucessfully");
				}
				responseText = Utility.getResponseXML("SUCCESS");
			}
		}
		catch (Exception e)
		{
			logger.error("Error", e);
		}

		logger.info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	/*
	 * (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request,
	 * HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}