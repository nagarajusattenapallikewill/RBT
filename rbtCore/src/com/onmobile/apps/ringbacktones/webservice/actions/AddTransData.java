package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author sridhar.sindiri
 *
 */
public class AddTransData implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(AddTransData.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	@Override
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = ERROR;

		File file = null;
		BufferedReader bufferedReader = null;
		try
		{
			String filePath = webServiceContext.getString(param_bulkTaskFile);
			String type = webServiceContext.getString(param_type);
			if (filePath == null || type == null)
			{
				logger.info("Type parameter is missing, so not processing the trans data");
				return getWebServiceResponse(INVALID_PARAMETER, webServiceContext);
			}

			file = new File(filePath);
			bufferedReader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bufferedReader.readLine()) != null)
			{
				line = line.trim();
				if (line.length() == 0)
					continue;

				String transID = line.trim();

				TransData transdata = RBTDBManager.getInstance().addTransData(transID, null, type);
				if (transdata == null)
				{
					response = FAILED;
				}
			}
			response = SUCCESS;
		}
		catch (Exception e)
		{
			logger.error("", e);
			response = ERROR;
		}
		finally
		{
			try
			{
				if (bufferedReader != null)
					bufferedReader.close();
				if (file != null)
					file.delete();
			}
			catch (IOException e)
			{
			}
		}

		logger.info("response: " + response);
		return getWebServiceResponse(response, webServiceContext);
	}

	/**
	 * @param response
	 * @param webServiceContext
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response, WebServiceContext webServiceContext)
	{
		logger.info("Getting xml response for the response : " + response);

		webServiceContext.put(param_response, response);
		Document document = Utility.getResponseDocument(response);
		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}
