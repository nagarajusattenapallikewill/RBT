package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class Consent extends HttpServlet implements Constants {

	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;
	private static Logger logger = Logger.getLogger(Consent.class);
	private static Logger consentRequestlogger = Logger.getLogger(Consent.class
			.getName()
			+ ".CONSENT");

	public Consent() {
		super();
	}

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		this.servletConfig = servletConfig;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */

	/*
	 * Base Activation(OM)
	 * http://ip:port/rbt/consent.do?msisdn=9886679873&transid=046b6c7f-0b8a-43b9-b35d-6489e6daee91
	 * &consent=yes&songid=&srvkey=DEFAULT&timestamp=20130417120101&info=&mode=WAP
	 * 
	 * Song Selection(OM)
	 * http://ip:port/rbt/consent.do?msisdn=9886679873&transid=046b6c7f-0b8a-43b9-b35d-6489e6daee91
	 * &consent=yes&songid=12345678&srvkey=DEFAULT&timestamp=20130417120101&info=CATEGORY_ID:123&mode=WAP
	 * 
	 * Base + Song (OM)
	 * http://ip:port/rbt/consent.do?msisdn=9886679873&transid=046b6c7f-0b8a-43b9-b35d-6489e6daee91
	 * &consent=yes&songid=12345678&srvkey=DEFAULT&timestamp=20130417120101
	 * &info=CATEGORY_ID:123|SONG_SRVKEY:FREE&mode=WAP
	 */

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		logger.debug("Recieved Consent request. Requst parameters are: "
				+ request.getQueryString());

		String responseText = "<rbt><response>error</response></rbt>";
		try {

			HashMap<String, String> requestParams = Utility
					.getRequestParamsMap(getServletConfig(), request, response,
							api_Consent);

			String url = request.getRequestURI() + "?"
					+ request.getQueryString();

			String responseStr = AdminFacade
					.processConsentRequest(requestParams);
			responseStr = getChangedResponse(responseStr);
			
			if (responseStr != null
					&& (responseStr.indexOf("<rbt>") != -1)){
				responseText = responseStr.substring(responseStr.indexOf("<rbt>"));
			}else {
				responseText = "<rbt><response>" + responseStr + "</response></rbt>";
				consentRequestlogger.info(new Date().toString() + "|" + url
						+ "|" + responseText);
				
			}
			Document document = XMLUtils.getDocumentFromString(responseText);
			String responseCode = requestParams.get("RESPONSE_CODE");
			if (responseCode != null) {
				response.setStatus(Integer.valueOf(responseCode));
			}
			response.getWriter()
					.write(XMLUtils.getStringFromDocument(document));

		} catch (Exception ex) {
			logger.error("Exception while processing Consent Request.....",ex);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	private String getChangedResponse(String response) {

		if (response == null) {
			return null;
		} else if (response.indexOf("success") != -1) {
			return "Request will be processed";
		}
		return response;
	}

}
