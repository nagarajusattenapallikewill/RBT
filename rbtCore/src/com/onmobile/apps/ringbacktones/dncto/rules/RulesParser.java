/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto.rules;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

import com.onmobile.apps.ringbacktones.common.StringUtil;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.dnctoservice.exception.DNCTOException;

/**
 * This class parses the <i>RBTDNCTORules.xml</i> file and creates the {@link Rule}
 * objects hierarchy. <i>RBTDNCTORules.xml</i> file will be searched in the
 * CLASSPATH.
 * 
 * <pre>
 * <b>Example:</b>
 * &lt;?xml version="1.0" encoding="UTF-8"?&gt;
 * &lt;Rules&gt;
 * 	&lt;Condition conditionType="OR"&gt;
 * 		&lt;Condition conditionType="AND"&gt;
 * 			&lt;RbtStatusRule subscriberStatus="NEW" period="M1" noOfContacts="1" /&gt;
 * 			&lt;RbtStatusRule subscriberStatus="NEW" noOfContacts="10" /&gt;
 * 		&lt;/Condition&gt;
 * 		&lt;Condition conditionType="AND"&gt;
 * 			&lt;RbtStatusRule subscriberStatus="DCT" period="M1" noOfContacts="1" /&gt;
 * 			&lt;RbtStatusRule subscriberStatus="DCT" period="Y6" noOfContacts="20" /&gt;
 * 		&lt;/Condition&gt;
 * 		&lt;Condition conditionType="AND"&gt;
 * 			&lt;RbtStatusRule subscriberStatus="ACT|GRC" period="M2" noOfContacts="1" /&gt;
 * 		&lt;/Condition&gt;
 * 		&lt;Condition conditionType="AND"&gt;
 * 			&lt;RbtStatusRule subscriberStatus="SUS|ERR" noOfContacts="0" /&gt;
 * 		&lt;/Condition&gt;
 * 	&lt;/Condition&gt;
 * &lt;/Rules&gt;
 * </pre>
 * 
 * @author vinayasimha.patil
 */
public class RulesParser
{
	public static final String RULES_PACKAGE = "com.onmobile.apps.ringbacktones.dncto.rules";
	private static Logger logger = Logger.getLogger(RulesParser.class);

	public static Rule parseRulesXML() throws DNCTOException
	{
		try
		{
			InputStream inputStream = RulesParser.class.getClassLoader()
					.getResourceAsStream("RBTDNCTORules.xml");
			Document document = XMLUtils
					.getDocumentFromInputStream(inputStream);

			Element rootRuleElem = (Element) document.getElementsByTagName(
					Condition.class.getSimpleName()).item(0);
			return getRuleFromElement(rootRuleElem);
		}
		catch (Exception e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(
					"Error in creating the rbt rules");
			dnctoException.initCause(e);
			throw dnctoException;
		}
	}

	private static Rule getRuleFromElement(Element ruleElem)
			throws DNCTOException
	{
		String ruleName = ruleElem.getTagName();
		try
		{
			String className = RULES_PACKAGE + "." + ruleName;

			@SuppressWarnings("unchecked")
			Class<Rule> ruleClass = (Class<Rule>) Class.forName(className);
			Rule rule = ruleClass.newInstance();

			NamedNodeMap namedNodeMap = ruleElem.getAttributes();
			for (int i = 0; i < namedNodeMap.getLength(); i++)
			{
				Attr attr = (Attr) namedNodeMap.item(i);
				String fieldName = attr.getName();
				Field field = ruleClass.getDeclaredField(fieldName);
				RuleAttribute annotation = field
						.getAnnotation(RuleAttribute.class);
				if (annotation != null)
				{
					// Making sure that field is representing the rule attribute
					Method method = ruleClass.getDeclaredMethod("set"
							+ StringUtil.toUpperCaseFirstChar(fieldName),
							String.class);
					method.invoke(rule, attr.getValue());
				}
				else
				{
					if (logger.isDebugEnabled())
						logger.debug("Unknown Attribute: " + attr.getName());

					throw new DNCTOException("");
				}
			}

			if (rule.getClass() == Condition.class)
			{
				List<Rule> rules = new ArrayList<Rule>();
				NodeList nodeList = ruleElem.getChildNodes();
				for (int i = 0; i < nodeList.getLength(); i++)
				{
					if (nodeList.item(i).getNodeType() != Node.ELEMENT_NODE)
						continue;

					Element childRuleElem = (Element) nodeList.item(i);
					Rule childRule = getRuleFromElement(childRuleElem);
					rules.add(childRule);
				}

				Condition condition = (Condition) rule;
				condition.setRules(rules);
			}

			return rule;
		}
		catch (ClassNotFoundException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException("Unknown Rule: "
					+ ruleName);
			dnctoException.initCause(e);
			throw dnctoException;
		}
		catch (NoSuchFieldException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(
					"Unknown Attribute");
			dnctoException.initCause(e);
			throw dnctoException;
		}
		catch (Exception e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(
					"Error in creating the rbt rules");
			dnctoException.initCause(e);
			throw dnctoException;
		}
	}
	
	//RBT-10224
	public static Rule parseRulesXML(Class className) throws DNCTOException
	{
		try
		{
			if(className==null)
				return null;
			InputStream inputStream = RulesParser.class.getClassLoader()
					.getResourceAsStream("RBTDNCTORules.xml");
			Document document = XMLUtils
					.getDocumentFromInputStream(inputStream);
			Element rootRuleElem = (Element) document.getElementsByTagName(
						className.getSimpleName()).item(0);
			return getRuleFromElement(rootRuleElem);
		}
		catch (Exception e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(
					"Error in creating the rbt rules");
			dnctoException.initCause(e);
			throw dnctoException;
		}
	}
}
