package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentInterOperatorOperatorIDCallBack extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;
	private static Logger logger = Logger.getLogger(ContentInterOperatorOperatorIDCallBack.class);

	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);
		this.servletConfig = servletConfig;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String responseText = "SUCCESS";
		responseText = uploadFile(servletConfig, request, response);
		logger.info("uploadFile, Response :" + responseText);
		response.getWriter().write(responseText);
	}

	/**
	 * @param servletConfig
	 * @param request
	 * @param response
	 * @return
	 */
	public String uploadFile(ServletConfig servletConfig, HttpServletRequest request, HttpServletResponse response)
	{		
		try
		{
			SmartUpload smartUpload = new SmartUpload();
			smartUpload.initialize(servletConfig, request, response);
			smartUpload.setTotalMaxFileSize(20000000);
			smartUpload.upload();
			com.jspsmart.upload.File file = smartUpload.getFiles().getFile(0);
			if (file != null)
			{
				if (file.getSize() > 0) 
				{
					String fileName = file.getFileName() + ".tmp";
					String localPath = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "MNP_LOCAL_DIRECTORY", null);
					File folderFile = new File(localPath);
					if (!folderFile.exists())
						folderFile.mkdirs();
					File savedFile = new File(localPath, fileName);	
					file.saveAs(savedFile.getAbsolutePath());
					ContentInterOperatorUtility.processXmlFile(savedFile, "HTTP");
				}
			}

			return "SUCCESS";
		}
		catch (SmartUploadException e)
		{
			logger.error(e.getMessage(), e);
			response.setStatus(500);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
			response.setStatus(500);
		}
		catch (ServletException e)
		{
			logger.error(e.getMessage(), e);
			response.setStatus(500);
		}
		return "FAILURE";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
