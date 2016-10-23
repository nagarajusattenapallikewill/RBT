package com.onmobile.apps.ringbacktones.webservice.features.RN;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NewSubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.sshtools.common.ui.NewWindowAction;

/**
 * 
 * @author rony.gregory
 *
 */
public class RNResponseUtils {
	private static Logger logger = Logger.getLogger(RNResponseUtils.class);
	private static Map<String, String> subscriptionStatusMapping = null;
	static {
		String subscriptionStatusMappingString = RNPropertyUtils.getProperty("subscription_status_mapping");
		if (subscriptionStatusMappingString != null) {
			subscriptionStatusMappingString = subscriptionStatusMappingString.toLowerCase();
			subscriptionStatusMapping = MapUtils.convertIntoMap(subscriptionStatusMappingString, ",", ":", null);
		}
	}

	public static Document getResponseDocument(RNBean rnBean, Document document) {
		logger.info("RNBean: " + rnBean);
		if (rnBean == null) {
			return null;
		}
		String api = rnBean.getApi();
		if (api == null) {
			return null;
		}
		String responseString = rnBean.getResponseString();
		String action = rnBean.getAction();
		String info = rnBean.getInfo();
		String subscriberId = rnBean.getSubscriberId();
		String clipId = rnBean.getClipId();
		String mode = rnBean.getMode();

		if (api.equalsIgnoreCase(WebServiceConstants.api_Selection)) {
			if (action.equalsIgnoreCase(WebServiceConstants.action_deleteSetting)) {
				getResponseForSelectionDeactivation(responseString, document);
			}
		} else if (api.equalsIgnoreCase(WebServiceConstants.api_SelectionPreConsent)) {
			if (action.equalsIgnoreCase(WebServiceConstants.action_set)) {
				getResponseForSelectionSet(responseString, document, subscriberId, clipId, mode);
			}
		} else if (api.equalsIgnoreCase(WebServiceConstants.api_Rbt)) {
			if (info.equalsIgnoreCase(WebServiceConstants.SUBSCRIBER)) {
				getResponseForSubscriber(responseString, document, subscriberId);
			} else if (info.equalsIgnoreCase(WebServiceConstants.LIBRARY) || info.equalsIgnoreCase(WebServiceConstants.SETTINGS)) {
				getResponseForLibrary(responseString, document);
			}
		} else if (api.equalsIgnoreCase(WebServiceConstants.api_WebService)) {
			if (action
					.equalsIgnoreCase(WebServiceConstants.action_getNextChargeClass)) {
				getResponseForGetNextChargeClass(responseString, document);
			} else if (action
					.equalsIgnoreCase(WebServiceConstants.action_getNextServiceCharge)) {
				getResponseForGetNextServiceCharge(responseString, document);
			}

		}
		
		logger.info("Response xml: " + getString(document));
		return document;
	}

	/*Get Next Charge Class - http://172.24.191.21:8085/packinfo.xml?msisdn=91xxxxxxxxx&channel=xxx
	 * <rbt>
	<response>SUCEESS</response>
	<subcriber>
	<activatePrice>30</activatePrice>
	<downloadPrice>15</downloadPrice>
	</subcriber>
	<subscriptionStatus>EMPTY</subscriptionStatus>
	</rbt>*/
	private static void getResponseForGetNextChargeClass(
			String responseString, Document document) {
		Element rbtEle = null; 
		Element chargeClassEle = null;
		String errorResponse = null;
		try {
			Document responseDocument = getDocumentFromString(responseString);
			String errorMsg = getOptionalTagTextFromDocument("errorMsg", responseDocument);
			if (errorMsg != null) {
				errorResponse = RNPropertyUtils.getErrorResponse(errorMsg);
			} else {
				String downloadPrice = getMandatoryTagTextFromDocument("downloadPrice", responseDocument);
				String subscriptionAmount = getOptionalTagTextFromDocument("activatePrice", responseDocument);
				String subscriptionPeriod = getOptionalTagTextFromDocument("subcriptionValidity", responseDocument);
				chargeClassEle = generateChargeClassesElement(document, downloadPrice, subscriptionAmount, subscriptionPeriod);
			}
		} catch (ParserConfigurationException e) {
			logger.error(e,e);
		} catch (SAXException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		} catch (Exception e) {
			logger.error(e,e);
		}
		if (chargeClassEle != null) {
			rbtEle = getRBTSuccessResponse(document);
			rbtEle.appendChild(chargeClassEle);
		} else {
			getRBTFailureResponse(document, errorResponse);
		}
	}

	//Song info API at RN
	private static void getResponseForLibrary(String responseString, Document document) {
		Element rbtEle = null; 
		Element libraryElement = null;
		String errorResponse = null;
		try {
			//responseString = responseString.replaceAll("ExistingSelectionPack", "selectionPack");
			Document responseDocument = getDocumentFromString(responseString);
			String errorMsg = getOptionalTagTextFromDocument("errorMsg", responseDocument);
			if (errorMsg != null) {
				errorResponse = RNPropertyUtils.getErrorResponse(errorMsg);
			} else {
				String circle = getOptionalTagTextFromDocument("circle", responseDocument);
				if (circle == null) {
					circle = getOptionalTagTextFromDocument("circleId", responseDocument);
				}
				Element settingElement = document.createElement(WebServiceConstants.SETTINGS);
				Element downloadElement = document.createElement(WebServiceConstants.DOWNLOADS);
				Element settingContentsElem = document.createElement(WebServiceConstants.CONTENTS);
				Element downloadContentsElem = document.createElement(WebServiceConstants.CONTENTS);
				NodeList songNodes = responseDocument.getElementsByTagName(
						"songDeatil");
				String dateFormat = RNPropertyUtils.getProperty("date_format");
				if (dateFormat == null) {
					dateFormat = "dd MMM yyyy HH:mm:ss z";
				}
				SimpleDateFormat inputSdf = new SimpleDateFormat(dateFormat);
				SimpleDateFormat ouputtSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				int totalCount = 0;
				if (songNodes != null) {
					settingElement.appendChild(settingContentsElem);
					downloadElement.appendChild(downloadContentsElem);	
					for (int i = 0; i < songNodes.getLength(); i++) {
						Element songElement = (Element) songNodes.item(i);
						String songCategory =  getOptionalTagTextFromElement("songCategory", songElement);
						if (songCategory != null && songCategory.equalsIgnoreCase("SpecialCaller")) {
							continue;
						}
						String songId = getOptionalTagTextFromElement("songId", songElement);
						String songName = getOptionalTagTextFromElement("songName", songElement);
						String subscriptionEndDate = getOptionalTagTextFromElement("subscriptionEndDate", songElement);
						String subscriptionStartDate = getOptionalTagTextFromElement("subscriptionStartDate", songElement);
						String selectionPack = getOptionalTagTextFromElement("selectionPack", songElement);
						selectionPack = getModifiedSelectionPack(selectionPack);
						NodeList callerIdNodes = songElement.getElementsByTagName("callerId");
						Element callerIdElem = null;
						String callerId = null;
						if(callerIdNodes.getLength() > 0){
							callerIdElem = (Element) callerIdNodes.item(0);
							if(callerIdElem != null){
								callerId = getOptionalTagTextFromElement("mdn", callerIdElem);
							}
						}
						Element settingContentElem = document.createElement(WebServiceConstants.CONTENT);
						settingContentsElem.appendChild(settingContentElem);
						addPropertiesToSettingContentElement(document, inputSdf,
								ouputtSdf, songId, songName, subscriptionEndDate,
								subscriptionStartDate, settingContentElem, selectionPack, callerId);
						totalCount++;
					}
				}
				settingContentsElem.setAttribute(WebServiceConstants.NO_OF_DEFAULT_SETTINGS, totalCount + "");
				settingContentsElem.setAttribute(WebServiceConstants.NO_OF_SETTINGS, totalCount + "");
				settingContentsElem.setAttribute(WebServiceConstants.NO_OF_SPECIAL_SETTINGS, "0");
				downloadContentsElem.setAttribute(WebServiceConstants.NO_OF_ACTIVE_DOWNLOADS, "0");
				downloadContentsElem.setAttribute(WebServiceConstants.NO_OF_DOWNLOADS, "0");

				libraryElement = document.createElement(WebServiceConstants.LIBRARY);
				libraryElement.appendChild(settingElement);
				libraryElement.appendChild(downloadElement);
			}
		} catch (ParserConfigurationException e) {
			logger.error(e,e);
		} catch (SAXException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		}  catch (Exception e) {
			logger.error(e,e);
		}
		if (libraryElement != null) {
			rbtEle = getRBTSuccessResponse(document);
			rbtEle.appendChild(libraryElement);
		} else {
			getRBTFailureResponse(document, errorResponse);
		}
	}

	private static void addPropertiesToSettingContentElement(Document document,
			SimpleDateFormat inputSdf, SimpleDateFormat ouputSdf,
			String songId, String songName, String subscriptionEndDate,
			String subscriptionStartDate, Element settingContentElem, String selectionPack, String callerId) {
		settingContentElem.setAttribute(WebServiceConstants.ID, songId);
		settingContentElem.setAttribute(WebServiceConstants.NAME, songName);
		if(callerId != null && !callerId.isEmpty()){
			Utility.addPropertyElement(document,
					settingContentElem, WebServiceConstants.CALLER_ID, WebServiceConstants.DATA, callerId);
		} else{
			Utility.addPropertyElement(document,
					settingContentElem, WebServiceConstants.CALLER_ID, WebServiceConstants.DATA, "all");
		}
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.FROM_TIME, WebServiceConstants.DATA, "0");
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.FROM_TIME_MINUTES, WebServiceConstants.DATA, "0");
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.TO_TIME, WebServiceConstants.DATA, "23");
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.TO_TIME_MINUTES, WebServiceConstants.DATA, "59");
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.STATUS, WebServiceConstants.DATA, "1");
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.SELECTION_STATUS, WebServiceConstants.DATA, WebServiceConstants.ACTIVE);
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.SELECTION_TYPE, WebServiceConstants.DATA, WebServiceConstants.NORMAL);
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.CATEGORY_ID, WebServiceConstants.DATA, "3");
		Utility.addPropertyElement(document, 
				settingContentElem, WebServiceConstants.PREVIEW_FILE, WebServiceConstants.PROMPT, "dummy.wav");
		Utility.addPropertyElement(document, 
				settingContentElem, WebServiceConstants.RBT_FILE, WebServiceConstants.PROMPT, "dummy.wav");
		Date startDate = null;
		if (subscriptionStartDate != null) {
			try {
				startDate = inputSdf.parse(subscriptionStartDate);
			} catch (ParseException e) {
				logger.info("Invalid date. subscriptionStartDate: " + subscriptionStartDate);
			}
		}
		if (startDate == null) {
			startDate = new Date();
		}
		Utility.addPropertyElement(document, 
				settingContentElem, WebServiceConstants.SET_TIME, WebServiceConstants.DATA, ouputSdf.format(startDate));
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.START_TIME,  WebServiceConstants.DATA, ouputSdf.format(startDate));
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.CHARGE_CLASS,  WebServiceConstants.DATA, selectionPack);
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.REF_ID,  WebServiceConstants.DATA, UUID.randomUUID().toString());
		Date endDate = null;
		if (subscriptionEndDate != null) {
			try {
				endDate = inputSdf.parse(subscriptionEndDate);
			} catch (ParseException e) {
				logger.info("Invalid date. subscriptionStartDate: " + subscriptionStartDate);
			}
		}
		if (endDate == null) {
			Utility.addPropertyElement(document,
					settingContentElem, WebServiceConstants.END_TIME,  WebServiceConstants.DATA, "20370101133255000");
		} else {		
			Utility.addPropertyElement(document,
					settingContentElem, WebServiceConstants.END_TIME,  WebServiceConstants.DATA, ouputSdf.format(endDate));
		}
		Utility.addPropertyElement(document,
				settingContentElem, WebServiceConstants.LOOP_STATUS,  WebServiceConstants.DATA, "l");
	}

	//Song info API at RN
	private static void getResponseForSubscriber(String responseString, Document document, String subscriberId) {
		Element rbtEle = null; 
		Element subscriberElement = null;
		String errorResponse = null;
		try {
			//responseString = responseString.replaceAll("ExistingSelectionPack", "selectionPack");
			Document responseDocument = getDocumentFromString(responseString);
			String errorMsg = getOptionalTagTextFromDocument("errorMsg", responseDocument);
			if (errorMsg != null) {
				errorResponse = RNPropertyUtils.getErrorResponse(errorMsg);
			} else {
				String circle = getOptionalTagTextFromDocument("circle", responseDocument);
				if (circle == null) {
					circle = getOptionalTagTextFromDocument("circleId", responseDocument);
				}
				String isPrepaid = getOptionalTagTextFromDocument("isPrepaid", responseDocument);
				//String status = getMandatoryTagTextFromDocument("status", responseDocument);
				String subscriptionId = getOptionalTagTextFromDocument("subscriptionId", responseDocument);
				if (subscriptionId != null) {
					subscriptionId = subscriptionId.replaceFirst("RN_ACT_", "");
				}

				subscriberElement = document.createElement(WebServiceConstants.SUBSCRIBER);
				subscriberElement.setAttribute(WebServiceConstants.SUBSCRIBER_ID, subscriberId);
				subscriberElement.setAttribute(WebServiceConstants.IS_VALID_PREFIX, WebServiceConstants.YES);
				subscriberElement.setAttribute(WebServiceConstants.ACCESS_COUNT, "0");
				subscriberElement.setAttribute(WebServiceConstants.CAN_ALLOW, WebServiceConstants.YES);
				subscriberElement.setAttribute(WebServiceConstants.IS_PREPAID, WebServiceConstants.NO);
				if (isPrepaid != null) {
					subscriberElement.setAttribute(WebServiceConstants.IS_PREPAID, isPrepaid.equalsIgnoreCase("Y")?WebServiceConstants.YES:WebServiceConstants.NO);
				}

				String subscriptionStatus = getMandatoryTagTextFromDocument("subscriptionStatus", responseDocument);

				if (subscriptionStatusMapping.containsKey(subscriptionStatus.toLowerCase())) {
					subscriptionStatus = subscriptionStatusMapping.get(subscriptionStatus.toLowerCase());
				}
				subscriberElement.setAttribute(WebServiceConstants.STATUS, subscriptionStatus);	

				if (circle != null) {
					subscriberElement.setAttribute(WebServiceConstants.CIRCLE_ID, circle);				
				}

				subscriberElement.setAttribute(WebServiceConstants.SUBSCRIPTION_CLASS, subscriptionId);
				subscriberElement.setAttribute(WebServiceConstants.COS_ID, "1");
				subscriberElement.setAttribute(WebServiceConstants.REF_ID, UUID.randomUUID().toString());
			}
		} catch (ParserConfigurationException e) {
			logger.error(e,e);
		} catch (SAXException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		} catch (Exception e) {
			logger.error(e,e);
		}
		if (subscriberElement != null) {
			rbtEle = getRBTSuccessResponse(document);
			rbtEle.appendChild(subscriberElement);
		} else {
			getRBTFailureResponse(document, errorResponse);
		}

	}

	//Deactivation API at RN
	private static void getResponseForSelectionDeactivation(
			String responseString, Document document) {
		String response = null;
		String errorResponse = null;
		try {
			Document responseDocument = getDocumentFromString(responseString);
			String errorMsg = getOptionalTagTextFromDocument("errorMsg", responseDocument);
			if (errorMsg != null) {
				errorResponse = RNPropertyUtils.getErrorResponse(errorMsg);
			} else {
				response = getMandatoryTagTextFromDocument("response", responseDocument);
			}
		} catch (ParserConfigurationException e) {
			logger.error(e,e);
		} catch (SAXException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		} catch (Exception e) {
			logger.error(e,e);
		}
		if (response != null && response.equalsIgnoreCase("success")) {
			getRBTSuccessResponse(document);
		} else {
			getRBTFailureResponse(document, errorResponse);
		}
	}

	//User info API at RN
	private static void getResponseForSelectionSet(String responseString, Document document, String subscriberId, String clipId, String mode) {
		Element rbtEle = null; 
		Element selectionConsentEle = null;
		String errorResponse = null;
		try {
			//			responseString = responseString.replaceAll("NewSubscriptionId", "subscriptionId");
			//			responseString = responseString.replaceAll("NewSelectionPack", "selectionPack");
			//			responseString = responseString.replaceAll("ExistingSelectionPack", "selectionPack");
			Document responseDocument = getDocumentFromString(responseString);
			String errorMsg = getOptionalTagTextFromDocument("errorMsg", responseDocument);
			if (errorMsg != null) {
				errorResponse = RNPropertyUtils.getErrorResponse(errorMsg);
			} else {
				String childTrId = getOptionalTagTextFromDocument("childTrId", responseDocument);
				String circle = getOptionalTagTextFromDocument("circle", responseDocument);
				String transactionId = null;
				if (childTrId == null) {
					childTrId = getOptionalTagTextFromDocument("transactionId", responseDocument); 
				} else {
					transactionId = getOptionalTagTextFromDocument("transactionId", responseDocument);
				}
				String subscriptionId = getOptionalTagTextFromDocument(
						"newSubcriptionpack", responseDocument);
				if (subscriptionId != null && subscriptionId.indexOf("RN_ACT_") != -1) {
					subscriptionId = subscriptionId.replaceAll("RN_ACT_", ""); 
				}
				String selectionPack = getOptionalTagTextFromDocument("newSelectionpack", responseDocument);
				selectionPack = getModifiedSelectionPack(selectionPack);	 
				selectionConsentEle = generateSelectionConsentElement(document, childTrId, circle, transactionId, subscriberId, clipId, subscriptionId, selectionPack, mode);
			}
		} catch (ParserConfigurationException e) {
			logger.error(e,e);
		} catch (SAXException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		} catch (Exception e) {
			logger.error(e,e);
		}
		if (selectionConsentEle != null) {
			rbtEle = getRBTSuccessResponse(document);
			rbtEle.appendChild(selectionConsentEle);
		} else {
			getRBTFailureResponse(document, errorResponse);
		}
	}

	private static String getModifiedSelectionPack(String selectionPack) {
		if (selectionPack != null) { 
			if (selectionPack.indexOf("RN_SEL_") != -1) {
				selectionPack = selectionPack.replaceAll("RN_SEL_", "");
			}
			if (selectionPack.indexOf("_RRBT") != -1) {
				selectionPack = selectionPack.replaceAll("_RRBT", "");
			}
		}
		return selectionPack;
	}
	private static Document getDocumentFromString(String responseString)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		InputSource input = new InputSource(new StringReader(responseString));
		Document responseDocument = builder.parse(input);
		return responseDocument;
	}

	public static Element generateChargeClassesElement(Document document, String downloadPrice, String subscriptionAmount, String subscriptionPeriod) {
		Element element = document.createElement(WebServiceConstants.CHARGE_CLASSES);
		Element contentsElem = document.createElement(WebServiceConstants.CONTENTS);
		element.appendChild(contentsElem);
		Element contentElem = document.createElement(WebServiceConstants.CONTENT);
		contentElem.setAttribute(WebServiceConstants.ID, "DUMMY");
		contentElem.setAttribute(WebServiceConstants.AMOUNT, downloadPrice);
		contentElem.setAttribute(WebServiceConstants.RENEWAL_AMOUNT, downloadPrice);
		if (subscriptionAmount != null) {
			contentElem.setAttribute(WebServiceConstants.SUBSCRIPTION_AMOUNT, subscriptionAmount);
		}
		if (subscriptionPeriod != null) {
			contentElem.setAttribute(WebServiceConstants.SUBSCRIPTION_PERIOD, subscriptionPeriod);
		}
		contentElem.setAttribute(WebServiceConstants.SHOW_ON_GUI, "y");
		contentsElem.appendChild(contentElem);
		return element;
	}
	private static Element generateSelectionConsentElement(Document document,
			String childTrId, String circle, String transactionId, String subscriberId, String clipId, String subscriptionId, String selectionPack, String mode) {
		Element consentElem = document.createElement(WebServiceConstants.param_consent);
		consentElem.setAttribute(WebServiceConstants.param_msisdn, subscriberId);
		consentElem.setAttribute(WebServiceConstants.MODE, mode);
		consentElem.setAttribute("sub_class", subscriptionId);
		consentElem.setAttribute("trans_id", UUID.randomUUID().toString());
		consentElem.setAttribute("clip_id", clipId);
		//consentElem.setAttribute("promoId", promo_id);
		consentElem.setAttribute("chargeclass", selectionPack);
		consentElem.setAttribute("circleId", circle);
		consentElem.setAttribute("catId", "7");
		if (transactionId != null) {
			consentElem.setAttribute(WebServiceConstants.param_linkedRefId, transactionId);
		}
		if (childTrId != null) {
			consentElem.setAttribute(WebServiceConstants.param_refID, childTrId);
		}
		return consentElem;
	}

	private static String getMandatoryTagTextFromDocument(String tagName, Document responseDocument) {
		Element element = (Element) responseDocument
				.getElementsByTagName(tagName).item(0);
		String value = element.getTextContent();
		return value;
	}

	private static String getOptionalTagTextFromDocument(String tagName, Document responseDocument) {
		String value = null;
		try {
			Element element = (Element) responseDocument
					.getElementsByTagName(tagName).item(0);
			value = element.getTextContent();
		} catch (Exception e) {

		}
		return value;
	}

	private static String getOptionalTagTextFromElement(String tagName, Element element) {
		String value = null;
		try {
			Element tagElement = (Element) element
					.getElementsByTagName(tagName).item(0);
			value = tagElement.getTextContent();
		} catch (Exception e) {

		}
		return value;
	}
	private static Element getRBTSuccessResponse(Document document) {
		Element rbtEle = document.createElement(WebServiceConstants.RBT);
		Element rbtResponseEle = document.createElement(WebServiceConstants.RESPONSE);
		rbtResponseEle.setTextContent(WebServiceConstants.SUCCESS);
		rbtEle.appendChild(rbtResponseEle);
		document.appendChild(rbtEle);
		return rbtEle;
	}

	private static Element getRBTFailureResponse(Document document, String errorResponse) {
		Element rbtEle = document.createElement(WebServiceConstants.RBT);
		Element rbtResponseEle = document.createElement(WebServiceConstants.RESPONSE);
		if (errorResponse != null) {
			rbtResponseEle.setTextContent(errorResponse);	
		} else {
			rbtResponseEle.setTextContent(WebServiceConstants.FAILURE);	
		}
		rbtEle.appendChild(rbtResponseEle);
		document.appendChild(rbtEle);
		return rbtEle;
	}

	public static String getString(Document doc) {
		try {
			DOMSource domSource = new DOMSource(doc);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			writer.flush();
			return writer.toString();
		} catch (TransformerException ex) {
			logger.error(ex, ex);
			return null;
		}
	}
	
	public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
		transformer.setOutputProperty(OutputKeys.METHOD, "xml");
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

		transformer.transform(new DOMSource(doc), 
				new StreamResult(new OutputStreamWriter(out, "UTF-8")));
	}

	public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException {
		DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document document = documentBuilder.newDocument();
		//next charge class response
		//<rbt><response>SUCEESS</response><subcriber><activatePrice>30</activatePrice><downloadPrice>15</downloadPrice></subcriber><subscriptionStatus>EMPTY</subscriptionStatus></rbt>
		//String responseString = "<rbt><response>SUCEESS</response><subcriber><activatePrice>30</activatePrice><downloadPrice>15</downloadPrice></subcriber><subscriptionStatus>EMPTY</subscriptionStatus></rbt>";
		//getResponseForGetNextChargeClass(responseString, document);

		//Library Thingy
		//String responseString = "<rbt><response>SUCEESS</response><songDeatil><ExistingSelectionPack>RN_SEL_DEFAULT</selectionPack><songCategory>DEFAULT</songCategory><songId>15718648</songId><songName>Sooraj Dooba Hain Yaaron</songName><subscriptionEndDate>24 Mar 2015 08:17:05 GMT</subscriptionEndDate><subscriptionStartDate>24 Dec 2014 08:17:05 GMT</subscriptionStartDate></songDeatil><songDeatil><ExistingSelectionPack>RN_SEL_DEFAULT</selectionPack><songCategory>SpecialCaller</songCategory><songId>157186479</songId><songName>xxxxxxxxx</songName><subscriptionEndDate>24 Mar 2015 08:17:05 GMT</subscriptionEndDate><subscriptionStartDate>24 Dec 2014 08:17:05 GMT</subscriptionStartDate></songDeatil><subcriber><circleId>BH</circleId><isPrepaid>N</isPrepaid><status>NORMAL</status><subscriptionId>RN_ACT_DEFAULT</subscriptionId></subcriber><subscriptionStatus>EMPTY</subscriptionStatus></rbt>";

		//activate song
		String responseString = "<rbt><response>SUCEESS</response><NewSelectionPack>xxxxxxxxx</NewSelectionPack><ExistingSelectionPack>RN_SEL_DEFAULT</selectionPack><subcriber><circleId>BH</circleId><isPrepaid>N</isPrepaid><status>NORMAL</status><subscriptionId>RN_ACT_DEFAULT</subscriptionId></subcriber><subscriptionStatus>NORMAL</subscriptionStatus><transactionId>1501061647919708097420210</transactionId></rbt>";
		//getResponseForLibrary(responseString, document);
		//getResponseForSubscriber(responseString, document, "232232");
		getResponseForSelectionSet(responseString, document, "5435435435", "123", "MODE_");
		printDocument(document, System.out);
	}
	
	/*Get Next Charge Class - http://172.24.191.21:8085/packinfo.xml?msisdn=91xxxxxxxxx&channel=xxx
	 * <rbt>
	<response>SUCEESS</response>
	<subcriber>
	<activatePrice>30</activatePrice>
	<downloadPrice>15</downloadPrice>
	</subcriber>
	<subscriptionStatus>EMPTY</subscriptionStatus>
	</rbt>*/
	private static void getResponseForGetNextServiceCharge(
			String responseString, Document document) {
		Element rbtEle = null; 
		Element nextServiceChargeElement = null;
		NewChargeClass newChargeClass = new NewChargeClass();
		NewSubscriptionClass newSubscriptionClass = new NewSubscriptionClass();
		
		String errorResponse = null;
		try {
			Document responseDocument = getDocumentFromString(responseString);
			String errorMsg = getOptionalTagTextFromDocument("errorMsg",
					responseDocument);
			if (errorMsg != null) {
				errorResponse = RNPropertyUtils.getErrorResponse(errorMsg);
			} else {
				String amount = getOptionalTagTextFromDocument("downloadPrice",
						responseDocument);
				String validitiy = getOptionalTagTextFromDocument(
						"songValidity", responseDocument);
				// String subcriptionValidity =
				// getOptionalTagTextFromDocument("subcriptionValidity",
				// responseDocument);
				Boolean isRenewal = true;
				if (validitiy.equalsIgnoreCase("-1")) {
					isRenewal = false;
				}

				String renewalAmount = getOptionalTagTextFromDocument(
						"downloadRenewalPrice", responseDocument);
				String renewalValidity = getOptionalTagTextFromDocument(
						"songRenewalValidity", responseDocument);
				String subAmount = getOptionalTagTextFromDocument(
						"activatePrice", responseDocument);
				String subValiditiy = getOptionalTagTextFromDocument(
						"subscriptionValidity", responseDocument);
				Boolean isSubRenewal = true;
				if (validitiy.equalsIgnoreCase("-1")) {
					isSubRenewal = false;
				}
				
				String subRenewalAmount = getOptionalTagTextFromDocument(
						"activeRenewalPrice", responseDocument);
				String subRenewalValidity = getOptionalTagTextFromDocument(
						"subcriptionRenewalValidity", responseDocument);
				newChargeClass = getNewChargeClassObj(amount, validitiy, isRenewal, renewalAmount, renewalValidity);
				newSubscriptionClass = getNewSubClassObj(subAmount, subValiditiy, isSubRenewal, subRenewalAmount, subRenewalValidity);
				nextServiceChargeElement = generateNextChargeServiceElement(document, newChargeClass, newSubscriptionClass) ;
			}
		} catch (ParserConfigurationException e) {
			logger.error(e,e);
		} catch (SAXException e) {
			logger.error(e,e);
		} catch (IOException e) {
			logger.error(e,e);
		} catch (Exception e) {
			logger.error(e,e);
		}
		if (nextServiceChargeElement != null) {
			rbtEle = getRBTSuccessResponse(document);
			rbtEle.appendChild(nextServiceChargeElement);
		} else {
			getRBTFailureResponse(document, errorResponse);
		}
	}
	
	
	public static Element generateNextChargeServiceElement(Document document,
			NewChargeClass newChargeClass,
			NewSubscriptionClass newSubscriptionClass) {

		Element chargeClassesElement = document
				.createElement(WebServiceConstants.NEXTSERVICE_CHARGE);

		Element contentsElem = document
				.createElement(WebServiceConstants.CONTENTS);
		chargeClassesElement.appendChild(contentsElem);

		if (newChargeClass != null) {

			Element contentElem = document
					.createElement(WebServiceConstants.NEWCHARGE_CLASS);
			contentElem.setAttribute(WebServiceConstants.SERVICE_KEY,
					newChargeClass.getServiceKey());
			contentElem.setAttribute(WebServiceConstants.AMOUNT,
					newChargeClass.getAmount());
			contentElem.setAttribute(WebServiceConstants.VALIDITY,
					newChargeClass.getValiditiy());
			contentElem.setAttribute(WebServiceConstants.IS_RENEWAL,
					newChargeClass.getIsRenewal() + "");
			contentElem.setAttribute(WebServiceConstants.RENEWAL_AMOUNT,
					newChargeClass.getRenewalAmount());
			contentElem.setAttribute(WebServiceConstants.RENEWAL_VALIDITY,
					newChargeClass.getRenewalValidity());
			contentElem.setAttribute(WebServiceConstants.OFFER_ID,
					newChargeClass.getOfferID() + "");

			contentsElem.appendChild(contentElem);

		}

		if (newSubscriptionClass != null) {

			Element contentElem = document
					.createElement(WebServiceConstants.NEWSUBSCRIPTION_CLASS);
			contentElem.setAttribute(WebServiceConstants.SERVICE_KEY,
					newSubscriptionClass.getServiceKey());
			contentElem.setAttribute(WebServiceConstants.AMOUNT,
					newSubscriptionClass.getAmount());
			contentElem.setAttribute(WebServiceConstants.VALIDITY,
					newSubscriptionClass.getValiditiy());
			contentElem.setAttribute(WebServiceConstants.IS_RENEWAL,
					newSubscriptionClass.getIsRenewal() + "");
			contentElem.setAttribute(WebServiceConstants.RENEWAL_AMOUNT,
					newSubscriptionClass.getRenewalAmount());
			contentElem.setAttribute(WebServiceConstants.RENEWAL_VALIDITY,
					newSubscriptionClass.getRenewalValidity());
			contentElem.setAttribute(WebServiceConstants.OFFER_ID,
					newSubscriptionClass.getOfferID() + "");

			contentsElem.appendChild(contentElem);

		}

		return chargeClassesElement;
	}
	
	private static NewChargeClass getNewChargeClassObj(String amount,
			String validitiy, Boolean isRenewal, String renewalAmount,
			String renewalValidity) {
		NewChargeClass newChargeClass = new NewChargeClass();
		if(amount!= null){
			newChargeClass.setAmount(amount);
		}
		
		if(validitiy!= null){
			newChargeClass.setValiditiy(validitiy);
		}
		
		if(renewalAmount!= null){
			newChargeClass.setRenewalAmount(renewalAmount);
		}
		
		if(renewalValidity!= null){
			newChargeClass.setRenewalValidity(renewalValidity);
		}
		
		if(newChargeClass != null){
			newChargeClass.setOfferID(-1);
			newChargeClass.setIsRenewal(isRenewal);
		}
		
		
		return newChargeClass;
		

	}
	
	private static NewSubscriptionClass getNewSubClassObj(String amount,
			String validitiy, Boolean isRenewal, String renewalAmount,
			String renewalValidity) {
		NewSubscriptionClass newSubscriptionClass = null;
		if (amount != null) {
			newSubscriptionClass = new NewSubscriptionClass();
			newSubscriptionClass.setAmount(amount);
		} else {
			return null;
		}

		if (validitiy != null) {
			newSubscriptionClass.setValiditiy(validitiy);
		}

		if (renewalAmount != null) {
			newSubscriptionClass.setRenewalAmount(renewalAmount);
		}

		if (renewalValidity != null) {
			newSubscriptionClass.setRenewalValidity(renewalValidity);
		}

		if (newSubscriptionClass != null) {
			newSubscriptionClass.setOfferID(-1);
			newSubscriptionClass.setIsRenewal(isRenewal);
		}

		return newSubscriptionClass;

	}
	
}
