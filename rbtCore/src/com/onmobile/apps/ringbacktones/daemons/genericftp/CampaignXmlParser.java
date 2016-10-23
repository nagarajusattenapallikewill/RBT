package com.onmobile.apps.ringbacktones.daemons.genericftp;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.StringUtil;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.BaseConfig;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.FTPConfig;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.SelectionConfig;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.SubClassMap;

/**
 * @author sridhar.sindiri
 *
 */
public class CampaignXmlParser
{
	private static Logger logger = Logger.getLogger(CampaignXmlParser.class);
	private static String CAMPAIGNS_PACKAGE = "com.onmobile.apps.ringbacktones.daemons.genericftp";

	/**
	 * 
	 */
	public static List<FTPCampaign> getCampaignsFromXml()
	{
		try
		{
			Document document = getDocumentFromCampaignsXml();
			if (document == null)
			{
				logger.warn("Document is null, so not processing");
				return null;
			}

			Element rootElem = document.getDocumentElement();
			FTPCampaignManager.setSleepTime(Long.parseLong(rootElem.getAttribute("sleepTime")));

			List<FTPCampaign> ftpCampaignsList = new ArrayList<FTPCampaign>();
			NodeList campaignsList = rootElem.getChildNodes();
			for (int i = 0; i < campaignsList.getLength(); i++)
			{
				if (campaignsList.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;

				Element campaignElement = (Element) campaignsList.item(i);
				FTPCampaign ftpCampaign = getCampaignFromElement(campaignElement);
				if (ftpCampaign != null)
					ftpCampaignsList.add(ftpCampaign);
			}

			logger.info("Campaigns parsed from the xml : " + ftpCampaignsList);
			return ftpCampaignsList;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/**
	 * @param campaignElement
	 * @return
	 * @throws Exception 
	 */
	private static FTPCampaign getCampaignFromElement(Element campaignElement) throws Exception
	{
		FTPCampaign ftpCampaign = new FTPCampaign();

		NodeList nodeList = campaignElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element childElem = (Element) nodeList.item(i);
			String elementName = childElem.getTagName();
			if (elementName.equals("FtpConfig"))
			{
				populateFtpConfigForCampaign(ftpCampaign, childElem);
			}
			else if (elementName.equals("InputFileFormat"))
			{
				populateInputFileFormatForCampaign(ftpCampaign, childElem);
			}
			else if (elementName.equals("ApplicationConfig"))
			{
				populateApplicationConfigForCampaign(ftpCampaign, childElem);
			}
			else
			{
				logger.warn("Invalid xml tag in the campaigns xml");
			}
		}

		return ftpCampaign;
	}

	/**
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document getDocumentFromCampaignsXml()
	{
		Document document = null;
		try
		{
			InputStream inputStream = CampaignXmlParser.class.getClassLoader().getResourceAsStream("RbtFtpCampaigns.xml");
			document = XMLUtils.getDocumentFromInputStream(inputStream);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return document;
	}

	/**
	 * @param ftpCampaign
	 * @param ftpConfigElem
	 * @throws Exception 
	 */
	private static void populateFtpConfigForCampaign(FTPCampaign ftpCampaign, Element ftpConfigElem) throws Exception
	{
		try
		{
			String className = CAMPAIGNS_PACKAGE + ".beans.FTPConfig";

			@SuppressWarnings("unchecked")
			Class<FTPConfig> ftpConfigClass = (Class<FTPConfig>) Class.forName(className);
			FTPConfig ftpConfig = ftpConfigClass.newInstance();

			NamedNodeMap namedNodeMap = ftpConfigElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();

				Method method = ftpConfigClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(fieldName),
						String.class);
				method.invoke(ftpConfig, attr.getValue());
			}

			ftpCampaign.setFtpConfig(ftpConfig);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * @param ftpCampaign
	 * @param inputFileFormatElem
	 */
	private static void populateInputFileFormatForCampaign(FTPCampaign ftpCampaign, Element inputFileFormatElem) throws Exception
	{
		try
		{
			String className = CAMPAIGNS_PACKAGE + ".FTPCampaign";

			@SuppressWarnings("unchecked")
			Class<FTPCampaign> ftpCampaignClass = (Class<FTPCampaign>) Class.forName(className);

			NamedNodeMap namedNodeMap = inputFileFormatElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();

				Method method = ftpCampaignClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(fieldName),
						String.class);

				method.invoke(ftpCampaign, attr.getValue());
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * @param ftpCampaign
	 * @param applicationConfigElem
	 * @throws Exception 
	 */
	private static void populateApplicationConfigForCampaign(FTPCampaign ftpCampaign, Element applicationConfigElem) throws Exception
	{
		try
		{
			@SuppressWarnings("unchecked")
			Class<FTPCampaign> ftpCampaignClass = (Class<FTPCampaign>) ftpCampaign.getClass();

			NamedNodeMap namedNodeMap = applicationConfigElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();

				Method method = ftpCampaignClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(fieldName),
						String.class);
				method.invoke(ftpCampaign, attr.getValue());
			}

			NodeList nodeList = applicationConfigElem.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;

				Element childElem = (Element) nodeList.item(i);
				String elementName = childElem.getTagName();
				if (elementName.equals("Base"))
				{
					populateBaseConfigForCampaign(ftpCampaign, childElem);
				}
				else if (elementName.equals("Selection"))
				{
					populateSelectionConfigForCampaign(ftpCampaign, childElem);
				}
				else if (elementName.equals("SubClassMaps"))
				{
					populateSubClassMapsForCampaign(ftpCampaign, childElem);
				}
				else if (elementName.equals("ChargeClassMaps"))
				{
					populateChargeClassMapsForCampaign(ftpCampaign, childElem);
				}
				else
				{
					logger.warn("Invalid xml tag in the campaigns xml");
				}
			}
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private static void populateBaseConfigForCampaign(FTPCampaign ftpCampaign,
			Element childElem) throws Exception
	{
		try
		{
			String className = CAMPAIGNS_PACKAGE + ".beans.BaseConfig";

			@SuppressWarnings("unchecked")
			Class<BaseConfig> baseConfigClass = (Class<BaseConfig>) Class.forName(className);
			BaseConfig baseConfig = baseConfigClass.newInstance();

			NamedNodeMap namedNodeMap = childElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();

				Method method = baseConfigClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(fieldName),
						String.class);
				method.invoke(baseConfig, attr.getValue());
			}

			NodeList propertyNodeList = childElem.getElementsByTagName("property");
			for (int i = 0; i < propertyNodeList.getLength(); i++)
			{
				Element propertyElement = (Element) propertyNodeList.item(i);
				String key = propertyElement.getAttribute("name");
				String value = propertyElement.getAttribute("value");

				Method method = baseConfigClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(key),
						String.class);
				method.invoke(baseConfig, value);
			}

			ftpCampaign.setBaseConfig(baseConfig);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private static void populateSelectionConfigForCampaign(FTPCampaign ftpCampaign,
			Element childElem) throws Exception
	{
		try
		{
			String className = CAMPAIGNS_PACKAGE + ".beans.SelectionConfig";

			@SuppressWarnings("unchecked")
			Class<SelectionConfig> selConfigClass = (Class<SelectionConfig>) Class.forName(className);
			SelectionConfig selConfig = selConfigClass.newInstance();

			NamedNodeMap namedNodeMap = childElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();

				Method method = selConfigClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(fieldName),
						String.class);
				method.invoke(selConfig, attr.getValue());
			}

			NodeList propertyNodeList = childElem.getElementsByTagName("property");
			for (int i = 0; i < propertyNodeList.getLength(); i++)
			{
				Element propertyElement = (Element) propertyNodeList.item(i);
				String key = propertyElement.getAttribute("name");
				String value = propertyElement.getAttribute("value");

				Method method = selConfigClass.getDeclaredMethod("set"
						+ StringUtil.toUpperCaseFirstChar(key),
						String.class);
				method.invoke(selConfig, value);
			}

			ftpCampaign.setSelConfig(selConfig);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private static void populateSubClassMapsForCampaign(FTPCampaign ftpCampaign,
			Element childElem) throws Exception
	{
		try
		{
			List<SubClassMap> subClassMapList = new ArrayList<SubClassMap>();
			String className = CAMPAIGNS_PACKAGE + ".beans.SubClassMap";

			@SuppressWarnings("unchecked")
			Class<SubClassMap> subClassMapClass = (Class<SubClassMap>) Class.forName(className);

			NodeList subNodeList = childElem.getElementsByTagName("SubClassMap");
			for (int i = 0; i < subNodeList.getLength(); i++)
			{
				Element subClassMapElement = (Element) subNodeList.item(i);
				SubClassMap subClassMap = subClassMapClass.newInstance();

				NamedNodeMap namedNodeMap = subClassMapElement.getAttributes();
				for (int j = 0; j < namedNodeMap.getLength(); j++)
				{
					Attr attr = (Attr) namedNodeMap.item(j);
					String fieldName = attr.getName();

					Method method = subClassMapClass.getDeclaredMethod("set"
							+ StringUtil.toUpperCaseFirstChar(fieldName),
							String.class);
					method.invoke(subClassMap, attr.getValue());
				}
				subClassMapList.add(subClassMap);
			}

			ftpCampaign.setSubClassMappingList(subClassMapList);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	private static void populateChargeClassMapsForCampaign(FTPCampaign ftpCampaign,
			Element childElem) throws Exception
	{
		try
		{
			List<ChargeClassMap> chargeClassMapList = new ArrayList<ChargeClassMap>();
			String className = CAMPAIGNS_PACKAGE + ".beans.ChargeClassMap";

			@SuppressWarnings("unchecked")
			Class<ChargeClassMap> chargeClassMapClass = (Class<ChargeClassMap>) Class.forName(className);

			NodeList chargeClassNodeList = childElem.getElementsByTagName("ChargeClassMap");
			for (int i = 0; i < chargeClassNodeList.getLength(); i++)
			{
				Element chargeClassMapElement = (Element) chargeClassNodeList.item(i);
				ChargeClassMap chargeClassMap = chargeClassMapClass.newInstance();

				NamedNodeMap namedNodeMap = chargeClassMapElement.getAttributes();
				for (int j = 0; j < namedNodeMap.getLength(); j++)
				{
					Attr attr = (Attr) namedNodeMap.item(j);
					String fieldName = attr.getName();

					Method method = chargeClassMapClass.getDeclaredMethod("set"
							+ StringUtil.toUpperCaseFirstChar(fieldName),
							String.class);
					method.invoke(chargeClassMap, attr.getValue());
				}
				chargeClassMapList.add(chargeClassMap);
			}

			ftpCampaign.setChargeClassMappingList(chargeClassMapList);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}
}
