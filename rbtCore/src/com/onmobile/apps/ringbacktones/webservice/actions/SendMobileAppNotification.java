package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.google.android.gcm.server.Sender;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.daemons.MobileAppNotificationThread;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.utils.ObjectGsonUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NotificationDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.ResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.StringResponseWriter;
import com.onmobile.apps.ringbacktones.webservice.responsewriters.WebServiceResponseFactory;

public class SendMobileAppNotification  extends HttpServlet implements WebServiceConstants{

	private static final long serialVersionUID = 27020228788327123L;


	private static final Logger logger = Logger.getLogger(SendMobileAppNotification.class);
	
	
	private Sender sender = null;
	private String path = null;
	
	public SendMobileAppNotification()
	{
		super();
		RBTClient client = RBTClient.getInstance();
		ApplicationDetailsRequest request = new ApplicationDetailsRequest();
		request.setType("COMMON");
		request.setName("MOBILEAPP_API_VALID_SUBSCRIBERIDS_ABSOLUTE_PATH_PREFIX");
		Parameter parameter = client.getParameter(request);							//Client request required, as this servlet runs on MobileApp Tomcat.
		if (parameter != null && parameter.getValue() != null) {
			path = parameter.getValue();
		}
		logger.info("MOBILEAPP_API_VALID_SUBSCRIBERIDS_ABSOLUTE_PATH_PREFIX initialized as: " + path);
	}

	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		WebServiceResponse webServiceResponse = null;
//		HashMap<String, String> requestParamsMap = Utility.getRequestParamsMap(getServletConfig(), request, response, api_WebService);
//		WebServiceContext webServiceContext = Utility.getTask(requestParamsMap);
//		logger.info("webServiceContext: " + webServiceContext);
		
		WebServiceContext webServiceContext = new WebServiceContext();
		webServiceContext.put(param_smsText, request.getParameter(param_smsText));
		webServiceContext.put(param_type, request.getParameter(param_type));
		webServiceContext.put(param_clipIds, request.getParameter(param_clipIds));
		webServiceContext.put(param_catId, request.getParameter(param_catId));
		webServiceContext.put(param_title, request.getParameter(param_title));
		webServiceContext.put(param_language, request.getParameter(param_language));
		webServiceContext.put(param_pageNo, request.getParameter(param_pageNo));
		webServiceContext.put(param_os_Type, request.getParameter(param_os_Type));
		webServiceContext.put(param_fileName, request.getParameter(param_fileName));
		
		webServiceResponse = processAction(webServiceContext);
		logger.info("webServiceResponse : " + webServiceResponse);
		webServiceResponse.getResponseWriter().writeResponse(webServiceResponse, response);
	}
	
	/* (non-Java-doc)
	 * @see javax.servlet.http.HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
	
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext) {
		String response = ERROR;
		String smsText = null;
		String smsTextForIOS = null;
		try {

			smsText = webServiceContext.getString(param_smsText);
			String type = webServiceContext.getString(param_type);
			String clipIds = webServiceContext.getString(param_clipIds);
			String catId = webServiceContext.getString(param_catId);
			String title = webServiceContext.getString(param_title);
			String lang = webServiceContext.getString(param_language);
			String pageNo = webServiceContext.getString(param_pageNo);
			String fileName = webServiceContext.getString(param_fileName);
			
			int iPageNo = -1;
			if(pageNo != null) {
				try{
					iPageNo = Integer.parseInt(pageNo);
				}
				catch(NumberFormatException nfe) {}
			}
			
			List<NotificationDetails> notificationDetailsList = new ArrayList<NotificationDetails>();
			NotificationDetails notificationDetails = new NotificationDetails();
			Category categoryObj = null;
			if(catId != null) {
			   categoryObj = RBTCacheManager.getInstance().getCategory(Integer.valueOf(catId));
			}
			if(type != null && clipIds != null) {
				if(clipIds.contains(",")) {
					String[] clipIdsArr = clipIds.split(",");
			        Clip[] clips = RBTCacheManager.getInstance().getClips(clipIdsArr);
			        String content = SendMobileAppNotifications.getClipJSONObj(clips, type, title, categoryObj, lang);
			        notificationDetails.setContent(content);
			        notificationDetailsList.add(notificationDetails);
			        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
			        smsTextForIOS = clips[0].getClipName();
				} else {
					Clip[] clips = new Clip[1];
					clips[0] = RBTCacheManager.getInstance().getClip(clipIds);
					String content = SendMobileAppNotifications.getClipJSONObj(clips, type, title, categoryObj, lang);
					notificationDetails.setContent(content);
			        notificationDetailsList.add(notificationDetails);
			        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
			        smsTextForIOS = clips[0].getClipName();
				}
			} else if(type != null && catId != null) {
				String content = SendMobileAppNotifications.getCategoryJSONObj(type, title, categoryObj);
				notificationDetails.setContent(content);
		        notificationDetailsList.add(notificationDetails);
		        smsText = ObjectGsonUtils.objectToGson(notificationDetailsList);
		        smsTextForIOS = categoryObj.getCategoryName();
			}
			logger.info("notificationClipList:"+ smsText);
			if (smsText == null) {
				return getWebServiceResponse(INVALID_PARAMETER);
			}			
			if(webServiceContext.getString(param_os_Type) == null || 
					webServiceContext.getString(param_os_Type).equalsIgnoreCase("android")) {
				if (sender == null) {
					Parameter parameter = getParameter("COMMON", "MOBILEAPP_API_KEY");				
					String apiKey = null;

					if(parameter != null) {
						apiKey = parameter.getValue();	
					}
					if (apiKey == null) {
						logger.info("MOBILEAPP_API_KEY parameter not configured.");
						return getWebServiceResponse("invalid_api_key");
					}
					parameter = getParameter("MOBILEAPP", "MOBILEAPP_GCM_PROXY_ENABLED");
					boolean isProxyEnabled = false;
					if (parameter != null) {
						isProxyEnabled = Boolean.valueOf(parameter.getValue());	
					}
					if (isProxyEnabled) {
						logger.info("MOBILEAPP_GCM_PROXY_ENABLED is true");
						String proxyIP = null;
						Integer proxyPort = null;
						parameter = getParameter("MOBILEAPP","MOBILEAPP_GCM_PROXY_IP");
						if (parameter != null) {
							proxyIP = parameter.getValue();
						}
						parameter = getParameter("MOBILEAPP", "MOBILEAPP_GCM_PROXY_PORT");
						if (parameter != null) {
							try {
								proxyPort = Integer.valueOf(parameter.getValue());
							} catch (NumberFormatException e) {
								logger.info(e,e);
							}
						}
						logger.info("proxy IP: " + proxyIP + ", proxy port: " + proxyPort);
						if (proxyIP != null && proxyPort != null) {
							sender = new ProxySender(apiKey, proxyIP, proxyPort);
						} else {
							logger.info("Proxy IP and/or Port not properly configured");	
						}
					} else {
						logger.info("MOBILEAPP_GCM_PROXY_ENABLED is false or not confgured");
					}
					if (sender == null) {
						sender = new Sender(apiKey);
					}
				}
			}
			MobileAppNotificationThread.setAtomicInteger(1);
			
			List<String> validSubscriberIds = null;
			if (fileName != null) { 
				try {
					validSubscriberIds = fillValidSubscriberIds(validSubscriberIds, fileName);
				} catch (IOException e) {
					response = "FILE NOT FOUND";
					logger.error(e.getMessage(), e);
					return getWebServiceResponse(response);
				}
			} 
			logger.info("fileName: " + fileName + ", validSubscriberIds: " + validSubscriberIds);
			
			for(int i = 0; i < 5; i++) {
				new MobileAppNotificationThread("MobileAppNotificainThread-" + i, smsText, smsTextForIOS, sender, webServiceContext, validSubscriberIds).start();
			}

			response = "Thread sending push notification sms. Please wait till all threads complete";
		} catch (Exception e) {
			response = TECHNICAL_DIFFICULTIES;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response);

	}
	
	/**
	 * @param response
	 * @return
	 */
	protected WebServiceResponse getWebServiceResponse(String response) {
		Document document = getResponseDocument(response);
		WebServiceResponse webServiceResponse = getWebServiceResponseXML(document);

		if (logger.isInfoEnabled())
			logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
	
	
	private Document getResponseDocument(String response) {
		DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();

		Document document = documentBuilder.newDocument();
		Element element = document.createElement(RBT);
		document.appendChild(element);

		Element invalidIPElem = getResponseElement(document, response);
		element.appendChild(invalidIPElem);
		return document;
	}
	
	private Element getResponseElement(Document document, String response)
	{
		Element element = document.createElement(RESPONSE);
		Text text = document.createTextNode(response);
		element.appendChild(text);

		return element;
	}
	
	private WebServiceResponse getWebServiceResponseXML(Document document)
	{
		String response = XMLUtils.getStringFromDocument(document);

		WebServiceResponse webServiceResponse = new WebServiceResponse(response);
		webServiceResponse.setContentType("text/xml; charset=utf-8");
		ResponseWriter responseWriter = WebServiceResponseFactory
				.getResponseWriter(StringResponseWriter.class);
		webServiceResponse.setResponseWriter(responseWriter);
		
		return webServiceResponse;
	}

	private List<String> fillValidSubscriberIds(List<String> validSubscriberIds, String fileName) throws IOException {
		validSubscriberIds = new ArrayList<String>();
		logger.info("Inside fillValidSubscriberIds");
		if (path == null) {
			throw new IOException("File path prefix config missing in DB");
		}
		logger.info("File path: " + path + fileName);
		File file = new File(path + fileName);
		BufferedReader bufferedReader = null;

		bufferedReader = new BufferedReader(new FileReader(file));
		String line = null;
		while ((line = bufferedReader.readLine()) != null) {
			line = line.trim();
			if (line.length() == 0) {
				continue;
			}
			String subscriberId = line.trim();
			validSubscriberIds.add(subscriberId);		
		}

		if (bufferedReader != null) {
			bufferedReader.close();

		}
		return validSubscriberIds;
	}

	private Parameter getParameter(String type, String name) {
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType(type);
		applicationDetailsRequest.setName(name);
		return RBTClient.getInstance().getParameter(applicationDetailsRequest);
	}

}