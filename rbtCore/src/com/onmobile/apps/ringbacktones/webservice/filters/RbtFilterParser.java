package com.onmobile.apps.ringbacktones.webservice.filters;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

/**
 * @author sridhar.sindiri
 *
 */
public class RbtFilterParser
{
	private static Logger logger = Logger.getLogger(RbtFilterParser.class);
	private static final String FILTERS_PACKAGE = "com.onmobile.apps.ringbacktones.webservice.filters";

	private static RbtFilter rbtFilter = null;

	static
	{
		rbtFilter = new RbtFilter();
		initializeFilters();
	}

	/**
	 * @return
	 */
	public static RbtFilter getRbtFilter()
	{
		return rbtFilter;
	}

	/**
	 * 
	 */
	private static void initializeFilters()
	{
		Document document = getDocumentFromFilterXml();
		if (document == null)
		{
			logger.warn("Document is null, so not processing");
			return;
		}

		Map<String, Filter> filtersMap = new HashMap<String, Filter>();

		Element rootElem = document.getDocumentElement();
		NodeList filtersList = rootElem.getChildNodes();
		for (int i = 0; i < filtersList.getLength(); i++)
		{
			if (filtersList.item(i).getNodeType() != Node.ELEMENT_NODE)
				continue;

			Element filterElement = (Element) filtersList.item(i);
			Filter filter = getFilterFromElement(filterElement);
			filtersMap.put(((FilterCondition)filter).getName().toLowerCase(), filter);
		}
		logger.info("Filters parsed from the xml : " + filtersMap);
		rbtFilter.setFiltersMap(filtersMap);
	}

	/**
	 * @return
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private static Document getDocumentFromFilterXml()
	{
		Document document = null;
		try
		{
			InputStream inputStream = RbtFilterParser.class.getClassLoader().getResourceAsStream("RbtFilters.xml");
			document = XMLUtils.getDocumentFromInputStream(inputStream);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}

		return document;
	}

	/**
	 * @param filterElem
	 * @return
	 */
	private static Filter getFilterFromElement(Element filterElem)
	{
		String filterName = filterElem.getTagName();
		try
		{
			String className = FILTERS_PACKAGE + "." + filterName;

			@SuppressWarnings("unchecked")
			Class<Filter> filterClass = (Class<Filter>) Class.forName(className);
			Filter filter = filterClass.newInstance();

			NamedNodeMap namedNodeMap = filterElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();

				Field field = filterClass.getDeclaredField(fieldName);
				FilterAttribute annotation = field.getAnnotation(FilterAttribute.class);
				if (annotation != null)
				{
					// Making sure that field is representing the filter attribute
					Method method = filterClass.getDeclaredMethod("set"
							+ StringUtil.toUpperCaseFirstChar(fieldName),
							String.class);
					method.invoke(filter, attr.getValue());
				}
				else
				{
					logger.warn("Unknown Attribute: " + attr.getName() + ", Element Name : " + filterName);
				}
			}

			List<Filter> childFilters = new ArrayList<Filter>();
			NodeList nodeList = filterElem.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++)
			{
				if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
					continue;

				Element childFilterElem = (Element) nodeList.item(i);
				Filter childFilter = getFilterFromElement(childFilterElem);
				if (childFilter instanceof AllowFilter)
				{
					Method method = filterClass.getDeclaredMethod("setAllowFilter", AllowFilter.class);
					method.invoke(filter, childFilter);
				}
				else if (childFilter instanceof DontAllowFilter)
				{
					Method method = filterClass.getDeclaredMethod("setDontAllowFilter", DontAllowFilter.class);
					method.invoke(filter, childFilter);
				}
				childFilters.add(childFilter);
			}

			if (filter instanceof FilterCondition) //check if the filter is an instance of FilterCondition
			{
				Method method = filterClass.getDeclaredMethod("setChildFilters", List.class);
				method.invoke(filter, childFilters);
			}

			return filter;
		}
		catch (ClassNotFoundException e)
		{
			logger.error(e.getMessage(), e);
		}
		catch (NoSuchFieldException e)
		{
			logger.error(e.getMessage(), e);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return null;
	}
}
