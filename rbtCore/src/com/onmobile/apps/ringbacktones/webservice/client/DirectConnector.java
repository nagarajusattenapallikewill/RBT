/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction;
import com.onmobile.apps.ringbacktones.webservice.actions.WebServiceActionFactory;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.content.RBTContentProviderFactory;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

/**
 * @author vinayasimha.patil
 * @author abhinav.anand
 */
public class DirectConnector implements Connector
{
	private Configurations configurations = null;

	private DocumentBuilder documentBuilder = null;
	
	/**
	 * @param configurations
	 */
	public DirectConnector(Configurations configurations) throws Exception
	{
		this.configurations = configurations;

		RBTAdminFacade.initialize();

		documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#redirectWebServiceRequest(java.utils.HashMap, java.lang.String)
	 */
	public String redirectWebServiceRequest(HashMap<String,String> requestParams, String api)
	{
		
		String response = null;
		try
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams);
			configurations.getLogger().info(stringBuilder.toString());

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}

			response = callWebServiceAPI(requestParams, fileParams, api);

			if (response != null)
			{
				response = response.trim();
			}
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return response;
	}

	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#makeWebServiceRequest(com.onmobile.apps.ringbacktones.webservice.client.requests.Request, java.lang.String, java.lang.String)
	 */
	public Parser makeWebServiceRequest(ConnectorHandler connectorHandler, Request request, String api,
			String action)
	{
		if (configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: request: " + request);

		Document document = null;
		try
		{
			HashMap<String, String> requestParams = request.getRequestParamsMap();
			if (action != null) requestParams.put(param_action, action);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams);
			configurations.getLogger().info(stringBuilder.toString());

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}

			String response = callWebServiceAPI(requestParams, fileParams, api);

			if (response != null)
			{
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));

				synchronized (documentBuilder)
				{
					document = documentBuilder.parse(byteArrayInputStream);
				}
			}
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}
		Parser parser = new Parser();
		parser.setDocument(document);
		parser.setRequest(request);
		parser.setParser(new RBTParser());
		return parser;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#redirectWebServiceBulkRequest(java.utils.HashMap, java.lang.String)
	 */
	/**
	public File redirectWebServiceBulkRequest(HashMap<String,String> requestParams, String api)
	{
		File file = null;
		try
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams);
			configurations.getLogger().info(stringBuilder.toString());

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}

			String response = callWebServiceAPI(requestParams, fileParams, api);
			configurations.getLogger().info("RBT:: response: " + response);

			if (response != null)
				file = new File(response);
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return file;
	}

	**/
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.Connector#makeWebServiceBulkRequest(com.onmobile.apps.ringbacktones.webservice.client.requests.Request, java.lang.String, java.lang.String)
	 */
	public File makeWebServiceBulkRequest(Request request, String api,
			String action)
	{
		if (configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: request: " + request);

		File file = null;
		try
		{
			HashMap<String, String> requestParams = request.getRequestParamsMap();
			if (action != null) requestParams.put(param_action, action);

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: api: ").append(api).append(System.getProperty("line.separator"));
			stringBuilder.append("RBT:: requestParams: ").append(requestParams);
			configurations.getLogger().info(stringBuilder.toString());

			HashMap<String, File> fileParams = null;
			if (requestParams.containsKey(param_bulkTaskFile))
			{
				String bulkTaskFile = requestParams.get(param_bulkTaskFile);
				File bulkTaskFileObj = new File(bulkTaskFile);

				fileParams = new HashMap<String, File>();
				fileParams.put(param_bulkTaskFile, bulkTaskFileObj);

				requestParams.remove(param_bulkTaskFile);
			}

			String response = callWebServiceAPI(requestParams, fileParams, api);

			if (response != null)
				file = new File(response);
		}
		catch(Exception e)
		{
			configurations.getLogger().error("RBT:: " + e.getMessage(), e);
		}

		return file;
	}

	private String callWebServiceAPI(HashMap<String, String> requestParams, HashMap<String, File> fileParams, String api)
	{
		requestParams.put(param_api, api);

		if (fileParams != null && !api.equalsIgnoreCase(api_RBTDownloadFile))
		{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

			Set<String> keySet = fileParams.keySet();
			for (String key : keySet)
			{
				try
				{
					File source = fileParams.get(key);
					String fileName = "BulkTask-" + dateFormat.format(new Date()) + ".txt";
					String tmpDir = System.getProperty("java.io.tmpdir");
					File destination = new File(tmpDir, fileName);
					Utility.copyFile(source, destination);

					requestParams.put(key, destination.getAbsolutePath());
				}
				catch (IOException e)
				{
					configurations.getLogger().error("RBT:: " + e.getMessage(), e);
				}
			}
		}

		String responseText = Utility.getErrorXML();
		try
		{
			WebServiceContext task = Utility.getTask(requestParams);
			configurations.getLogger().info("RBT:: task: " + task);

			if (api.equalsIgnoreCase(api_Rbt))
				responseText = RBTAdminFacade.getRBTInformationXML(task);
			else if (api.equalsIgnoreCase(api_Subscription)) 
					responseText = RBTAdminFacade.getSubscriptionResponseXML(task);
			else if (api.equalsIgnoreCase(api_Selection))
					responseText = RBTAdminFacade.getSelectionResponseXML(task);
			else if (api.equalsIgnoreCase(api_SubscriptionPreConsent))
				responseText = RBTAdminFacade.getPreConsentSubscriptionResponseXML(task);
			else if (api.equalsIgnoreCase(api_SelectionPreConsent))
				responseText = RBTAdminFacade.getConsentPreSelectionResponseXML(task);
			else if (api.equalsIgnoreCase(api_SelectionConsentIntegration))
				responseText = RBTAdminFacade.getConsentSelIntegrationResponseXML(task);
			else if (api.equalsIgnoreCase(api_BookMark))
				responseText = RBTAdminFacade.getBookMarkResponseXML(task);
			else if (api.equalsIgnoreCase(api_Copy))
				responseText = RBTAdminFacade.getCopyResponseXML(task);
			else if (api.equalsIgnoreCase(api_Gift))
				responseText = RBTAdminFacade.getGiftResponseXML(task);
			else if (api.equalsIgnoreCase(api_ValidateNumber))
				responseText = RBTAdminFacade.getValidateNumberResponseXML(task);
			else if (api.equalsIgnoreCase(api_SetSubscriberDetails))
				responseText = RBTAdminFacade.getSetSubscriberDetailsResponseXML(task);
			else if (api.equalsIgnoreCase(api_Group))
				responseText = RBTAdminFacade.getGroupXML(task);
			else if (api.equalsIgnoreCase(api_ApplicationDetails))
				responseText = RBTAdminFacade.getApplicationDetailsResponseXML(task);
			else if (api.equalsIgnoreCase(api_Sng))
				responseText = RBTAdminFacade.getSngXML(task);
			else if (api.equalsIgnoreCase(api_Search)){
	            task.put(param_action, api_Search);
				responseText = RBTContentProviderFactory.processContentRequest(task);
			}
			else if (api.equalsIgnoreCase(api_BulkTask))
			{
				responseText = RBTAdminFacade.getBulkTaskResponse(task);
				if (responseText.equalsIgnoreCase(SUCCESS))
					responseText = task.getString(param_bulkTaskResultFile);
				else
					responseText = null;
			}
			else if (api.equalsIgnoreCase(api_BulkUploadTask))
				responseText = RBTAdminFacade.getBulkProcessTaskResponse(task);
			else if (api.equalsIgnoreCase(api_Utils))
				responseText = RBTAdminFacade.getUtilsXML(task);
			else if (api.equalsIgnoreCase(api_Data))
				responseText = RBTAdminFacade.getDataResponseXML(task);
			else if (api.equalsIgnoreCase(api_Offer))
				responseText = RBTAdminFacade.getOfferResponseXML(task);
			else if (api.equalsIgnoreCase(api_Content))
				responseText = RBTContentProviderFactory.processContentRequest(task);
			else if (api.equalsIgnoreCase(api_WebService))
			{
				WebServiceResponse webServiceResponse = null;
				String action = task.getString(param_action);
				WebServiceAction webServiceAction = WebServiceActionFactory.getWebServiceActionProcessor(action);
				if (webServiceAction == null)
				{
					configurations.getLogger().error("Action name not registered: " + action);
					String responseString = Utility.getResponseXML(INVALID_ACTION);
					webServiceResponse = new WebServiceResponse(responseString);
					webServiceResponse.setContentType("text/xml; charset=utf-8");
					ResponseWriter responseWriter = WebServiceResponseFactory.getResponseWriter(StringResponseWriter.class);
					webServiceResponse.setResponseWriter(responseWriter);
				}
				else
				{
					WebServiceContext webServiceContext = Utility.getTask(requestParams);
					webServiceResponse = webServiceAction.processAction(webServiceContext);
				}
				responseText = webServiceResponse.getResponse();
			}
			else if (api.equalsIgnoreCase(api_RBTDownloadFile)) {
				responseText = Utility.getResponseXML("Direct Connection Not Supported");
			}
		}
		catch (Exception e)
		{
			configurations.getLogger().error(e.getMessage(), e);
		}

		if(configurations.getLogger().isDebugEnabled())
			configurations.getLogger().debug("RBT:: responseText: " + responseText);

		return responseText;
	}

	@Override
	public String makeRestRequest(Request restrequest, String api,
			String action) throws Exception{
		throw new Exception("Method is not implemented");
	}

	@Override
	public Parser makeWebServiceRequest(Request request, String api,
			String action) {
		ConnectorHandler connectionHanlder = null;
		return makeWebServiceRequest(connectionHanlder, request, api, action);
	}

}
