package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.jspsmart.upload.SRequest;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;

public class GrbtDataUpload extends HttpServlet
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(GrbtDataUpload.class);
	private static String operatorName = null;
	private static String grbtDataFolderStr = null;
	private static String grbtCopyDataFolderStr = null;
	private static String grbtDownloadsDataFolderStr = null;
	private static String grbtTPDataFolderStr = null;
	
	static
	{
		operatorName = RBTParametersUtils.getParamAsString("GRBT", "OPERATOR_NAME", null);
		operatorName = operatorName.trim();
		grbtDataFolderStr = "/var/RBT_SYSTEM_LOGS/GRBT/";
		grbtCopyDataFolderStr = grbtDataFolderStr + operatorName + "_COPY";
		grbtDownloadsDataFolderStr = grbtDataFolderStr + operatorName + "_DOWNLOADS";
		grbtTPDataFolderStr = grbtDataFolderStr + operatorName + "_TP";
		
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
	{
		logger.info("Entered");
		PrintWriter out = resp.getWriter();
		resp.setContentType("text/html");
    	String responseStr = "FAILURE";
		if (!isMultipartContent(req))
		{
			logger.info("grbt data request is not multipart request, ignoring");
			out.println("FAILURE");
	    	out.flush();
	    	return;
		}
		try
		{
			SmartUpload smartUpload = new SmartUpload();
			smartUpload.initialize(getServletConfig(), req, resp);
			smartUpload.setTotalMaxFileSize(100000000); //100 MB
			smartUpload.upload();
			
			if(smartUpload.getFiles().getCount() != 1)
			{
				logger.info("num of files="+smartUpload.getFiles().getCount() + ", expected count is 1, so ignoring the request");
				out.println("FAILURE");
		    	out.flush();
		    	return;
			}
			
			com.jspsmart.upload.File file = smartUpload.getFiles().getFile(0);
			
			if (file.getSize() <= 0)
			{
				logger.info("invalid filesize : "+file.getSize()+", ignoring the request");
				out.println("FAILURE");
		    	out.flush();
		    	return;
			}
			
			String fieldName = file.getFieldName();
			String folderName = null;
			if(fieldName.indexOf("COPY") != -1)
				folderName = grbtCopyDataFolderStr;
			else if(fieldName.indexOf("DOWNLOADS") != -1)
				folderName = grbtDownloadsDataFolderStr;
			else if(fieldName.indexOf("TP") != -1)
				folderName = grbtTPDataFolderStr;
			else
			{
				logger.info("Unrecognized file name, ignoring file");
				out.println("FAILURE");
		    	out.flush();
		    	return;
			}	
		
			File temp = new File(folderName);
			if(!temp.exists())
				temp.mkdirs();
			File savedFile = new File(folderName, fieldName);
			file.saveAs(savedFile.getAbsolutePath());
			responseStr = "SUCCESS";
		}
		catch (Exception e)
		{
			logger.error("error grbt uploading file", e);
		}
		
		out.println(responseStr);
    	out.flush();
    }
	
	public static boolean isMultipartContent(HttpServletRequest request)
	{
		if (!request.getMethod().toLowerCase().equals("post"))
			return false;

		String contentType = request.getContentType();
		if (contentType == null)
			return false;

		return (contentType.toLowerCase().startsWith("multipart/"));
	}

}
