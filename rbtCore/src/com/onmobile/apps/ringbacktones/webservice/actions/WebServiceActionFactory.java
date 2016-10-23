package com.onmobile.apps.ringbacktones.webservice.actions;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class WebServiceActionFactory implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(WebServiceActionFactory.class);

	private static Map<String, Class<? extends WebServiceAction>> webServiceActionClassesMap 
										= new HashMap<String, Class<? extends WebServiceAction>>();
	private static Map<Class<? extends WebServiceAction>, WebServiceAction> webServiceActionProcessorsMap 
										= new HashMap<Class<? extends WebServiceAction>, WebServiceAction>();

	private static ResourceBundle rb = null;
	
	static 
	{
		try 
		{
			initActionClassesMap();
		}
		catch(Exception e) 
		{
			logger.fatal("Exception initializing webServiceActionClassesMap ", e);
		}
	}
	
	/**
	 * 
	 */
	private static void initActionClassesMap()
	{
		if (webServiceActionClassesMap.size() != 0)
			return;

		initActionClassesMapByOperatorName("default");

		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON, "WERSERVICE_ACTION_CLASSES");
		if (param != null && param.getValue() != null)
		{
			String operator = param.getValue();
			initActionClassesMapByOperatorName(operator);
		}
		else 
		{
			logger.warn("Since the parameter WEBSERVICE_ACTION_CLASSES is not configured, using the default resource properties");
		}
	}

	/**
	 * 
	 */
	private static void initActionClassesMapByOperatorName(String operator)
	{
		String resourceFileName = "resources/webservice_action_classes_" + operator.toLowerCase();
		logger.info("Loading the web service action class from " + resourceFileName);
		try
		{
			rb = ResourceBundle.getBundle(resourceFileName);
		}
		catch (MissingResourceException e)
		{
			logger.error("ResourceBundle name " + resourceFileName + " is invalid");
			return;
		}

		Enumeration<String> actionsEnum = rb.getKeys();
		while (actionsEnum.hasMoreElements())
		{
			String actionName = actionsEnum.nextElement();
			String className = null;
			try 
			{
				className = rb.getString(actionName);
				@SuppressWarnings("unchecked")
				Class<? extends WebServiceAction> processorClass = (Class<? extends WebServiceAction>)Class.forName(className);

				webServiceActionClassesMap.put(actionName, processorClass);
			} 
			catch (ClassNotFoundException e) 
			{
				logger.error("Exception while creating the class " + className, e);
			}
		}
	}

	/**
	 * @param actionName
	 * @return
	 */
	public static WebServiceAction getWebServiceActionProcessor(String actionName)
	{
		if (actionName == null)
			return null;

		Class<? extends WebServiceAction> webServiceActionClass = webServiceActionClassesMap.get(actionName);
		if (webServiceActionClass == null)
			return null;

		WebServiceAction webServiceActionProcessor = webServiceActionProcessorsMap.get(webServiceActionClass);
		if (webServiceActionProcessor == null)
		{
			try
			{
				synchronized (WebServiceActionFactory.class) 
				{
					webServiceActionProcessor = webServiceActionProcessorsMap.get(webServiceActionClass);
					if(webServiceActionProcessor == null) 
					{
						webServiceActionProcessor = webServiceActionClass.newInstance();
						webServiceActionProcessorsMap.put(webServiceActionClass, webServiceActionProcessor);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("Unbale to create instance for " + webServiceActionClass.getName(), e);
			}
		}

		if (logger.isInfoEnabled())
			logger.info("webServiceActionProcessor: " + webServiceActionProcessor);
		return webServiceActionProcessor;
	}
}
