package com.onmobile.apps.ringbacktones.daemons.interoperator.servlet;

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
import com.onmobile.apps.ringbacktones.daemons.interoperator.threads.InterOperatorCopyThread;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;

public class InterOperatorOperatorIdCallback extends HttpServlet {
private static final long serialVersionUID = 1L;
	
	private ServletConfig servletConfig = null;
	Logger logger=Logger.getLogger(InterOperatorOperatorIdCallback.class);
	
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);
		this.servletConfig = servletConfig;
	}
    
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String responseText = "SUCCESS";
		responseText = uploadFile(servletConfig, request, response);
		logger.info( "uploadloadFile"+ "Response :" + responseText);
		response.getWriter().write(responseText);
	}
	
	public String uploadFile(ServletConfig servletConfig,
			HttpServletRequest request, HttpServletResponse response) {		
		try {
			SmartUpload smartUpload = new SmartUpload();
			smartUpload.initialize(servletConfig, request, response);
			smartUpload.setTotalMaxFileSize(20000000);
			smartUpload.upload();
			for (int i = 0; i < smartUpload.getFiles().getCount();i++)
				{
				   com.jspsmart.upload.File file = smartUpload.getFiles().getFile(i);
				   if(file!=null)
					{	
					   if (file.getSize() > 0) 
						{
						   String fileName = file.getFileName()+".tmp";
							String localPath=CacheManagerUtil.getParametersCacheManager().getParameter("RDC","LOCAL_DIRECTORY").getValue();
							File folderFile = new File(localPath);
							if(!folderFile.exists())
								folderFile.mkdirs();
							File savedFile = new File(localPath, fileName);	
							file.saveAs(savedFile.getAbsolutePath());
							InterOperatorCopyThread thread = new InterOperatorCopyThread(savedFile);
							thread.run();
			
						}
					}
					return "SUCCESS";

				}
			
		}	
		catch (SmartUploadException e) {
			logger.error(e);
			response.setStatus(500);

		}
		catch (IOException e) {
			logger.error(e);
			response.setStatus(500);

		}
		catch (ServletException e) {
			logger.error(e);
			response.setStatus(500);
		}
		return "FAILURE";
	}
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
