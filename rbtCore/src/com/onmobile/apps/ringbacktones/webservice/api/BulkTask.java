package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author vinayasimha.patil
 *
 */
/**
 * Servlet implementation class for Servlet: BulkTask
 *
 */
public class BulkTask extends HttpServlet implements WebServiceConstants
{
	static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(BulkTask.class);

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public BulkTask()
	{
		super();
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		String responseText = null;

		InputStream inputStream = null;

		String bulkTaskFile = null;
		String bulkTaskResultFile = null;

		try
		{
			HashMap<String, String> requestParams = Utility.getRequestParamsMap(getServletConfig(), request, response, api_BulkTask);
			
			Utility.sqlInjectionInRequestParam(requestParams);
			WebServiceContext task = Utility.getTask(requestParams);
			logger.info("RBT:: task: " + task);

			responseText = RBTAdminFacade.getBulkTaskResponse(task);

			bulkTaskFile = task.getString(param_bulkTaskFile);

			if (responseText.equalsIgnoreCase(SUCCESS))
			{
				bulkTaskResultFile = task.getString(param_bulkTaskResultFile);
				File file  = new File(bulkTaskResultFile);

				response.setContentType("application/octet-stream");
				response.setHeader("Content-Disposition","attachment; filename=" + file.getName() + ";");

				inputStream = new FileInputStream(file);
				int readCount;

				PrintWriter printWriter = response.getWriter();
				while ((readCount = inputStream.read()) != -1)
				{
					printWriter.write(readCount);
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			try
			{
				if (inputStream != null)
					inputStream.close();

				if (bulkTaskFile != null)
				{
					File bulkTaskFileObj = new File(bulkTaskFile);
					if (bulkTaskFileObj.exists())
						bulkTaskFileObj.delete();
				}

				if (bulkTaskResultFile != null)
				{
					File bulkTaskResultFileObj = new File(bulkTaskResultFile);
					if (bulkTaskResultFileObj.exists())
						bulkTaskResultFileObj.delete();
				}
			}
			catch (Exception e)
			{
				logger.error("", e);
			}
		}

		logger.info("RBT:: responseText: " + responseText);
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}