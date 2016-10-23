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

public class SDPDirectConsent extends HttpServlet implements Constants {

	private static final long serialVersionUID = 1L;

	private ServletConfig servletConfig = null;
	private static Logger logger = Logger.getLogger(SDPDirectConsent.class);
	private static Logger consentRequestlogger = Logger.getLogger(SDPDirectConsent.class
			.getName()
			+ ".CONSENT");

	public SDPDirectConsent() {
		super();
	}

	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);

		this.servletConfig = servletConfig;
	}

	

	/*
	 * Combo
	 * https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
	 * &channelType=<mode>&srvkey=<parent service key>&productId=<clip_ID/promo
	 * code>&productCategoryId=<>&orderTypeId=<C>&transid=<transid> &
	 * timestamp=<YYYYMMDDhhmmss>&info=categoryid:<song
	 * category>|songSrvKey:<childname>&sdpomtxid=<>;
	 * 
	 * Song
	 * https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
	 * &channelType=<mode>&srvkey=<child service key>&productId=<clip_ID/promo
	 * code>&productCategoryId=<>&orderTypeId=<R>&transid=<transid> &
	 * timestamp=<YYYYMMDDhhmmss>&info=categoryid:<song
	 * category>|songSrvKey:<childname>&sdpomtxid=<>;
	 * 
	 * Base
	 * https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
	 * &channelType=<mode>&srvkey=<parent service
	 * key>&productId=<>&productCategoryId=<>&orderTypeId=<R>&transid=<transid>
	 * & timestamp=<YYYYMMDDhhmmss>&info=<>&sdpomtxid=<>;
	 * 
	 * Base upgrade
	 * https://<IP:PORT>/rbt/SdpDirect.do?msisdn=91<msisdn>&consent=<yes/no>
	 * &channelType=<mode>&srvkey=<new parent service
	 * key>&productId=<>&productCategoryId=<>&orderTypeId=<U>&transid=<transid>
	 * & timestamp=<YYYYMMDDhhmmss>&info=<>&sdpomtxid=<>;
	 */ 

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.debug("Recieved SDPDirectConsent request. Requst parameters are: "
				+ request.getQueryString());

		String responseText = "3009|FAILED";
		try {
			HashMap<String, String> requestParams = Utility
			            .getRequestParamsMap(getServletConfig(),request, response, api_Consent);

			String url = request.getRequestURI() + "?" + request.getQueryString();

			String responseStr = AdminFacade.processSDPDirectConsentRequest(requestParams);
            if(responseStr != null){
            	responseText = responseStr;
            }
			consentRequestlogger.info(new Date().toString() + "|" + url + "|" + responseText);
			
			response.getWriter().write(responseText);

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


}
