package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.webservice.client.Parser;
import com.onmobile.apps.ringbacktones.webservice.client.RBTParser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;

public class ThirdPartyLoggerUtil {
	static String resp = null;

	public static Rbt getRbtObject(HttpServletRequest req, String response,
			String subscriberID) {
		DocumentBuilder documentBuilder;
		Rbt rbt = new Rbt();
		try {
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();

			Document document = null;
			if (response != null) {
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
						response.getBytes("UTF-8"));

				synchronized (documentBuilder) {
					document = documentBuilder.parse(byteArrayInputStream);
				}
			}
			if (document != null) {
				Element responseElem = (Element) document.getElementsByTagName(
						"response").item(0);
				Text responseText = (Text) responseElem.getFirstChild();
				resp = responseText.getNodeValue();
			}
			Parser parser = new Parser();
			Request request = new Request(subscriberID) {
			};
			request.setSubscriberID(subscriberID);
			parser.setDocument(document);
			parser.setRequest(request);
			parser.setParser(new RBTParser());
			if (parser.getRequest() == null)
				parser.setRequest(request);

			rbt = parser.getParser().getRBT(parser);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rbt;
	}

	public static String getRefIds(HttpServletRequest req, String responseStr) {
		StringBuffer sb = new StringBuffer();
		String action = null;
		String mode = null;
		Enumeration<String> params = req.getParameterNames();
		String refId = null, linkedRefId = null, consentRefId = null;
		int response = 0;
		StringBuffer requestURL = req.getRequestURL();
		String reqType = null;
		String subscriberID = null;
		if (requestURL.indexOf("/Selection.do") != -1) {
			reqType = "Selection";
		} else if (requestURL.indexOf("/Subscription.do") != -1) {
			reqType = "Base";
		}
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			if (key.equalsIgnoreCase("ACTION")) {
				action = req.getParameter(key).trim();
			} else if (key.equalsIgnoreCase("mode")) {
				mode = req.getParameter(key).trim();
			} else if (key.equalsIgnoreCase("subscriberID")) {
				subscriberID = req.getParameter(key).trim();
			}
		}
		Rbt rbt = new Rbt();
		if (responseStr.contains("user_info")) {
			responseStr = responseStr.substring(0,
					responseStr.indexOf("user_info"))
					+ responseStr.substring(
							responseStr.indexOf("user_type"),
							responseStr.length());
		}
		rbt = getRbtObject(req, responseStr, subscriberID);
		if (rbt != null) {
			Subscriber sub = rbt.getSubscriber();
			Library library = rbt.getLibrary();
			Consent consent = rbt.getConsent();
			Setting[] cnt = null;
			if (mode == null || mode.isEmpty()) {
				if(sub!=null)
				mode = sub.getActivatedBy();
			}

			if (resp.equalsIgnoreCase("SUCCESS")) {
				response = 1;
			}

			if (rbt.getLibrary() != null && library.getSettings() != null
					&& library.getSettings().getSettings() != null)
				cnt = library.getSettings().getSettings();
			if (sub != null && cnt != null) {
				if (reqType.equals("Selection")) {
					reqType = "Combo";
				}
			}
			if (consent == null) {
				if (sub != null && cnt != null) {
					if (null != sub) {
						linkedRefId = sub.getRefID();
					}
					if (null != cnt) {
						for (int i = 0; i < cnt.length; i++) {
							refId = cnt[i].getRefID();
						}
					}
				} else if (sub != null && cnt == null) {
					if (null != sub) {
						refId = sub.getRefID();
					}
				} else if (sub == null && cnt != null) {
					if (null != cnt) {
						for (int i = 0; i < cnt.length; i++) {
							refId = cnt[i].getRefID();
						}
					}
				}
			} else {
				consentRefId = consent.getTransId();
			}
			sb.append(", "+reqType).append(", " + action).append(", " + refId)
					.append(", " + linkedRefId).append(", " + consentRefId)
					.append(", " + mode).append(", " + response)
					.append(", 200");
		} else {
			sb.append(", "+reqType).append(", " + action).append(", " + refId)
			.append(", " + linkedRefId).append(", " + consentRefId)
			.append(", " + mode).append(", " + response)
			.append(", 200");
		}
		return sb.toString();
	}
}
