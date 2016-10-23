/**
 * 
 */
package com.onmobile.apps.ringbacktones.content.database;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

/**
 * @author vinayasimha.patil
 *
 */
public class DBUtility
{
	public static HashMap<String, String> getAttributeMapFromXML(String xml)
	{
		if (xml == null || (xml = xml.trim()).length() == 0 || xml.equalsIgnoreCase("null"))
			return null;

		HashMap<String, String> attributeMap = new HashMap<String, String>();
		Document document = XMLUtils.getDocumentFromString(xml);

		Element rootElem = document.getDocumentElement();
		NamedNodeMap namedNodeMap = rootElem.getAttributes();
		for(int i = 0; i < namedNodeMap.getLength(); i++)
			attributeMap.put(namedNodeMap.item(i).getNodeName(), namedNodeMap.item(i).getNodeValue());

		return attributeMap;
	}

	public static String getAttributeXMLFromMap(Map<String, String> attributeMap)
	{
		if (attributeMap == null || attributeMap.size() == 0)
			return null;

		String responseXML = null;

		Document document = XMLUtils.newDocument();
		Element rootElem = document.createElement("r");
		document.appendChild(rootElem);

		Set<String> keySet = attributeMap.keySet();
		for (String key : keySet)
		{
			if(attributeMap.get(key) != null && attributeMap.get(key).trim().length() > 0)
				rootElem.setAttribute(key, attributeMap.get(key));
		}

		responseXML = XMLUtils.getStringFromDocument(document);
		return responseXML;
	}

	public static String setXMLAttribute(String xml, String name, String value)
	{
		String responseXML = xml;

		Document document = null;
		Element rootElem = null;
		if (xml == null)
		{
			document = XMLUtils.newDocument();
			rootElem = document.createElement("r");
			document.appendChild(rootElem);
		}
		else
		{
			document = XMLUtils.getDocumentFromString(xml);
			rootElem = document.getDocumentElement();
		}

		rootElem.setAttribute(name, value);
		responseXML = XMLUtils.getStringFromDocument(document);

		return responseXML;
	}

	public static String removeXMLAttribute(String xml, String name)
	{
		if (xml == null)
			return null;

		String responseXML = xml;

		Document document = XMLUtils.getDocumentFromString(xml);
		Element rootElem = document.getDocumentElement();

		rootElem.removeAttribute(name);

		responseXML = XMLUtils.getStringFromDocument(document);
		return responseXML;
	}
	
	public static Integer secondsToBeAddedInRequestTime(String circleId, String selectedBy) {
		if (selectedBy == null) {
			return -1;
		}
		String param = "DELAY_IN_CG_REQUEST_" + selectedBy.toUpperCase();
		int secondsToBeAddedInRequestTime = -1;
		if (circleId != null) {
			String circleParam =  param + "_" + circleId.toUpperCase();
			secondsToBeAddedInRequestTime = RBTParametersUtils.getParamAsInt(iRBTConstant.WEBSERVICE, circleParam, -1);
			if (secondsToBeAddedInRequestTime != -1) {
				return secondsToBeAddedInRequestTime;
			}
		}
		secondsToBeAddedInRequestTime = RBTParametersUtils.getParamAsInt(iRBTConstant.WEBSERVICE, param, -1);
		return secondsToBeAddedInRequestTime;
	}
}
