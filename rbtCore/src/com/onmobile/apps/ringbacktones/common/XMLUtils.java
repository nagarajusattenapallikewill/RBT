/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * @author vinayasimha.patil
 *
 */
public class XMLUtils
{
	private static Logger logger = Logger.getLogger(XMLUtils.class);
	
	private static DocumentBuilder documentBuilder = null;
	private static TransformerFactory transformerFactory = null;

	public static DocumentBuilder getDocumentBuilder()
	{
		if(documentBuilder == null)
		{
			synchronized(XMLUtils.class)
			{
				if(documentBuilder == null)
				{
					try
					{
						documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					}
					catch (ParserConfigurationException e)
					{
						logger.error("", e);
					}
				}
			}
		}

		return documentBuilder;
	}

	public static TransformerFactory getTransformerFactory()
	{
		if(transformerFactory == null)
		{
			synchronized(XMLUtils.class)
			{
				if(transformerFactory == null)
					transformerFactory = TransformerFactory.newInstance();
			}
		}

		return transformerFactory;
	}

	public static String getStringFromDocument(Document document)
	{
		try
		{
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory transformerFactory = getTransformerFactory();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		}
		catch(TransformerException e)
		{
			logger.error("", e);
		}

		return null;
	}

	public static Document getDocumentFromString(String xml)
	{
		if (xml == null)
			return null;

		try
		{
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(xml.trim().getBytes("UTF-8"));
			return getDocumentFromInputStream(byteArrayInputStream);
		}
		catch (UnsupportedEncodingException e)
		{
			logger.error("", e);
		}

		return null;
	}

	public static Document getDocumentFromInputStream(InputStream inputStream)
	{
		if (inputStream == null)
			return null;

		DocumentBuilder documentBuilder = getDocumentBuilder();
		Document document = null;

		try
		{
			synchronized (documentBuilder)
			{
				document = documentBuilder.parse(inputStream);
			}
		}
		catch (SAXException e)
		{
			logger.error("", e);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}

		return document;
	}

	public static Document newDocument()
	{
		DocumentBuilder documentBuilder = getDocumentBuilder();
		return documentBuilder.newDocument();
	}
	
	public static Document createDocument() {
		Document document = null;
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			document = documentBuilder.newDocument();
		} catch (Exception e) {

		}
		return document;
	}

	public static Element createElement(Document document,
			String elementName) {
		Element element = document.createElement(elementName);
		document.appendChild(element);
		return element;
	}

	public static void createElementAndSetAttribute(Document document,
			Element ele, String elementName, String attribute, String attributeValue) {
		Element element = document.createElement(elementName);
		element.setAttribute(attribute, attributeValue);
		ele.appendChild(element);
	}
	
	public static void createElementAndSetAttributes(Document document,
			Element ele,String elementName, Map<String, String> attributes) {
		Element element = document.createElement(elementName);
		if (null != attributes) {
			for (Entry<String, String> attribute : attributes.entrySet()) {
				element.setAttribute(attribute.getKey(), attribute.getValue());
			}
		}
		ele.appendChild(element);
	}
}
