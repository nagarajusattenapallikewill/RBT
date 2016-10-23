package com.onmobile.apps.ringbacktones.webservice.client;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RBTParser implements IXMLParser, WebServiceConstants {

	
	@Override
	public Rbt getRBT(Parser parser) {
		Document document = parser.getDocument();
		Request request = parser.getRequest();
		Element responseElem = (Element) document.getElementsByTagName(
				RESPONSE).item(0);
		Text responseText = (Text) responseElem.getFirstChild();
		String response = responseText.getNodeValue();

		request.setResponse(response); // set response text in
		// Request

		// object
		Element rbtElem1 = (Element) document.getElementsByTagName(
				RBT).item(0); //?

		Element rbtElem = null;
		// RBT-16238 Getting NullPointerException while doing song selection
		if (response.equalsIgnoreCase(SUCCESS) || response.equalsIgnoreCase(SUCCESS_DOWNLOAD_EXISTS)
				|| response
				.equalsIgnoreCase(LITE_USER_PREMIUM_BLOCKED)) {
			rbtElem = (Element) document
					.getElementsByTagName(RBT).item(0);

		}
		return XMLParser.getRBT(rbtElem, request);

	}
}
