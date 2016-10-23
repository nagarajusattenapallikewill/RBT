package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Servlet implementation class SAT
 */
public class SatPushNotification extends HttpServlet implements
		WebServiceConstants {
	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(SatPushNotification.class);
	private static int statusForConsentPending = 0;
	private static int statusForConboConsentPending = 1;
	private static int statusAfterSuccessfulSATNotification = 5;
	private static int statusAfterSuccessfulSATNotificationforCombo = 6;
	private static String validToken = RBTParametersUtils.getParamAsString(
			iRBTConstant.COMMON,
			"DOUBLE_OPT_IN_SAT_REQUEST_VALID_TOKEN", null);


	
	private enum ResponseCode {
		responseCodeForSuccessfullyRegistration("SVC0027",
				"Request successfully registered."), responseCodeForInvalidAuthenticationInformation(
				"AUT001", "Invalid Authentication Information."), responseCodeForInvalidRequestFormat(
				"SVC005",
				"Invalid Request.The format or values that were sent in the HTTP request are invalid"), responseCodeForUnexpectederror(
				"SVC999", "Unexpected error");

		private String code;
		private String description;

		private ResponseCode(String code, String description) {
			this.code = code;
			this.description = description;

		}

	};
	
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = null;
		Document document = null;
		response.setContentType("text/xml; charset=utf-8");
		String responseText ="ERROR";
		String responseCode = null;
		String description = "";
		RBTDBManager rbtdbManager = RBTDBManager.getInstance();
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;
		InputStream stream = request.getInputStream();
		logger.info("Notification request:" + stream.toString());

		try {
			builder = factory.newDocumentBuilder();
			document = builder.parse(stream);
			String acceptKeyword = null;
			String msisdn = null;
			String retry = null;
			String token = null;
			String channelId = null;
			String message = null;
			String shortcode = null;
			if(document == null){
				
				responseCode = ResponseCode.responseCodeForInvalidRequestFormat.code;
				description =ResponseCode.responseCodeForInvalidRequestFormat.description;
				responseText = getResponseString(responseCode, description);
				response.getWriter().write(responseText);
				return;
			}

			if (document.getElementsByTagName("acceptKeyword").item(0) == null
					|| document.getElementsByTagName("msisdn").item(0) == null
					|| document.getElementsByTagName("channelId").item(0) == null
					|| document.getElementsByTagName("retry").item(0) == null
					|| document.getElementsByTagName("shortcode").item(0) == null
					|| document.getElementsByTagName("token").item(0) == null
					|| document.getElementsByTagName("message").item(0) == null) {
				logger.info("Invalid Request.The format or values that were sent in the HTTP request are invalid:");
				responseCode = ResponseCode.responseCodeForInvalidRequestFormat.code;
				description = ResponseCode.responseCodeForInvalidRequestFormat.description;
				responseText = getResponseString(responseCode, description);
				response.getWriter().write(responseText);
				return;

			}
			if (document.getElementsByTagName("acceptKeyword").item(0)
					.hasChildNodes()) {
				acceptKeyword = document.getElementsByTagName("acceptKeyword")
						.item(0).getFirstChild().getNodeValue();
			}
			if (document.getElementsByTagName("msisdn").item(0).hasChildNodes()) {
				msisdn = document.getElementsByTagName("msisdn").item(0)
						.getFirstChild().getNodeValue();
			}
			if (document.getElementsByTagName("retry").item(0).hasChildNodes()) {
				retry = document.getElementsByTagName("retry").item(0)
						.getFirstChild().getNodeValue();
			}
			if (document.getElementsByTagName("token").item(0).hasChildNodes()) {
				token = document.getElementsByTagName("token").item(0)
						.getFirstChild().getNodeValue();
			}
			if (document.getElementsByTagName("message").item(0).hasChildNodes()) {
				message = document.getElementsByTagName("message").item(0)
						.getFirstChild().getNodeValue();
			}
			if (document.getElementsByTagName("shortcode").item(0).hasChildNodes()) {
				shortcode = document.getElementsByTagName("shortcode").item(0)
						.getFirstChild().getNodeValue();
			}
			if (document.getElementsByTagName("channelId").item(0).hasChildNodes()) {
				channelId = document.getElementsByTagName("channelId").item(0)
						.getFirstChild().getNodeValue();
			}
			if (acceptKeyword == null || acceptKeyword.isEmpty()
					|| message == null || message.isEmpty()
					|| shortcode == null || shortcode.isEmpty()
					|| channelId == null || channelId.isEmpty()
					|| token == null  || retry == null
					|| retry.isEmpty() || msisdn == null || msisdn.isEmpty()) {
				logger.info("Invalid Request.The format or values that were sent in the HTTP request are invalid:");
				responseCode = ResponseCode.responseCodeForInvalidRequestFormat.code;
				description = ResponseCode.responseCodeForInvalidRequestFormat.description;
				responseText = getResponseString(responseCode, description);
				response.getWriter().write(responseText);
				return;

			}
			if( validToken == null ){
				logger.info("DOUBLE_OPT_IN_SAT_REQUEST_VALID_TOKEN is not configured: returning unexpected error");
				responseCode = ResponseCode.responseCodeForUnexpectederror.code;
				description = ResponseCode.responseCodeForUnexpectederror.description;
				responseText = getResponseString(responseCode, description);
				response.getWriter().write(responseText);
				return;
			}
			if (token != null && !token.equals(validToken)) {

				responseCode = ResponseCode.responseCodeForInvalidAuthenticationInformation.code;
				logger.info("Invalid Authentication Information.  Review the credentials with the Account Manager: response code: "
						+ responseCode);
				description = ResponseCode.responseCodeForInvalidAuthenticationInformation.description;
				responseText = getResponseString(responseCode, description);
				response.getWriter().write(responseText);
				return;
			}
			logger.info("Xml values msisdn:"+ msisdn+", acceptKeyword:"+acceptKeyword+", shortCode:"+shortcode+", token:"+token+", channelID:"+channelId
					+", retry:"+retry+", message:"+message);
			if (retry.equals("0")) {
				doubleConfirmationRequestBean = rbtdbManager
						.getLatestDoubleConfirmationRequestBeanForSAT(msisdn);
				if (doubleConfirmationRequestBean != null) {
					String transId = doubleConfirmationRequestBean.getTransId();
					String extraInfo = doubleConfirmationRequestBean
							.getExtraInfo();
					String selTransId = null;
					boolean isComboReq = false;
					if (extraInfo != null) {
						Map<String, String> extraInfoMap = DBUtility
								.getAttributeMapFromXML(extraInfo);
						if (extraInfoMap != null
								&& extraInfoMap.containsKey("TRANS_ID")) {
							selTransId = extraInfoMap.get("TRANS_ID");
							isComboReq = true;
						}
					}
					logger.info("SatPushNotification: Updating consent request to consent ststus:"
							+ statusAfterSuccessfulSATNotification
							+ ", msisdn:" + msisdn);
					rbtdbManager
							.updateConsentStatusOfConsentRecord(
									msisdn,
									transId,
									String.valueOf(statusAfterSuccessfulSATNotification));
					if (isComboReq) {
						logger.info("SatPushNotification: Updating selection of combo request for"
								+ msisdn);
						rbtdbManager
								.updateConsentStatusOfConsentRecord(
										msisdn,
										selTransId,
										String.valueOf(statusAfterSuccessfulSATNotificationforCombo));
					}

					responseCode = ResponseCode.responseCodeForSuccessfullyRegistration.code;
					description =ResponseCode.responseCodeForSuccessfullyRegistration.description;
				} else {
					logger.info("No content found for msisdn in consent table:"+ msisdn);
					responseCode = ResponseCode.responseCodeForUnexpectederror.code;
					description =ResponseCode.responseCodeForUnexpectederror.description;
				}
			} else if (retry.equals("1")) {
				logger.info("retry is 1, Resetting consent request ");
				List<DoubleConfirmationRequestBean> DoubleConfirmationRequestBeans = rbtdbManager
						.getAllDoubleConfirmationRequestBeanForSATUpgrade(msisdn, "3", null);
				if (DoubleConfirmationRequestBeans != null && DoubleConfirmationRequestBeans.size() > 0) {
					for (DoubleConfirmationRequestBean expriedDoubleConfirmationRequestBean : DoubleConfirmationRequestBeans) {
						String extraInfo = expriedDoubleConfirmationRequestBean.getExtraInfo();
						String selTransId = null;
						String reqConsentStatus = String.valueOf(expriedDoubleConfirmationRequestBean.getConsentStatus());
						boolean isComboReq = false;
						Date date = new Date();
						if (extraInfo != null) {
							Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
							if (extraInfoMap != null) {
								if (extraInfoMap.containsKey("TRANS_ID")) {
									selTransId = extraInfoMap.get("TRANS_ID");
									isComboReq = true;
								}
								if (extraInfoMap.containsKey("RETRY_COUNT")) {
									if (reqConsentStatus.equalsIgnoreCase("3")) {
										extraInfoMap.put("RETRY_COUNT", "0");
									}
								}
								if (extraInfoMap.containsKey("RETRY_CBCK_COUNT") && !extraInfoMap.get("RETRY_CBCK_COUNT").isEmpty() ) {
										extraInfoMap.put("RETRY_CBCK_COUNT", String.valueOf(Integer
												.parseInt(extraInfoMap.get("RETRY_CBCK_COUNT")) + 1));
								}else{
									extraInfoMap.put("RETRY_CBCK_COUNT","1");
								}
							}
							
							extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
						}else{
							Map<String, String> extraInfoMap = new HashMap<String, String>();
							extraInfoMap.put("RETRY_CBCK_COUNT","1");
							extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
						}
						if (reqConsentStatus.equalsIgnoreCase("3") || reqConsentStatus.equalsIgnoreCase("1") ) {
							rbtdbManager.updateExtraInfoAndStatusWithReqTime(msisdn,
									expriedDoubleConfirmationRequestBean.getTransId(), extraInfo,
									String.valueOf(statusForConsentPending), date);

							if (isComboReq) {
								rbtdbManager.updateExtraInfoAndStatusWithReqTime(msisdn, selTransId, "null",
										String.valueOf(statusForConboConsentPending), date);
							}
						}
					}
				}
				responseCode = ResponseCode.responseCodeForSuccessfullyRegistration.code;
				description = ResponseCode.responseCodeForSuccessfullyRegistration.description;
			} else {
				logger.info("SAT notification was not sent for msisdn: "
						+ msisdn);
				responseCode = ResponseCode.responseCodeForUnexpectederror.code;
				description = ResponseCode.responseCodeForUnexpectederror.description;
			}
		} catch (Exception e) {
			responseCode = ResponseCode.responseCodeForUnexpectederror.code;
			description =ResponseCode.responseCodeForUnexpectederror.description;
			logger.debug("Exception: Returning error response:"
					+ e.getMessage());
		}
		responseText = getResponseString(responseCode, description);
		logger.info("SatPushNotification:responseText" + responseText);
		response.getWriter().write(responseText);
	}

	private String getResponseString(String responseCode, String description) {
		DocumentBuilder documentBuilder = null;
		StringWriter writer = null;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document document = documentBuilder.newDocument();
			Element element = document.createElement("response_data");
			document.appendChild(element);
			Element codeElement = document.createElement("code");
			Text codeText = document.createTextNode(responseCode);
			codeElement.appendChild(codeText);
			element.appendChild(codeElement);
			Element descriptionElement = document.createElement("description");
			Text descriptionText = document.createTextNode(description);
			descriptionElement.appendChild(descriptionText);
			element.appendChild(descriptionElement);
			DOMSource domSource = new DOMSource(document);
			writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(domSource, result);
		} catch (Exception e) {
			logger.info("Unexpected error");
			return "ERROR";
		}
		return writer.toString();

	}
}
