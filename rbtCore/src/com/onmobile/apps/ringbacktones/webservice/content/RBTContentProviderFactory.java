/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


/**
 * @author vinayasimha.patil
 *
 */
public abstract class RBTContentProviderFactory implements WebServiceConstants
{
	private static Logger logger = Logger.getLogger(RBTContentProviderFactory.class);

	private static RBTContentProvider rbtContentProvider = null;
	public static RBTContentProvider getRBTContentProvider()
	{
		if (rbtContentProvider == null)
		{
			synchronized (RBTContentProviderFactory.class)
			{
				if (rbtContentProvider == null)
				{
					try
					{
						RBTDBManager.getInstance();

						Parameters contentProviderParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.WEBSERVICE,
								"CONTENT_PROVIDER_CLASS", "com.onmobile.apps.ringbacktones.webservice.content.BasicRBTContentProvider");
						String contentProviderClass = contentProviderParam.getValue().trim();

						logger.info("RBT:: contentProviderClass: " + contentProviderClass);
						Class<?> rbtContentProviderClass = Class.forName(contentProviderClass);
						rbtContentProvider = (RBTContentProvider) rbtContentProviderClass.newInstance();
					}
					catch (ClassNotFoundException e)
					{
						logger.error("", e);
					}
					catch (InstantiationException e)
					{
						logger.error("", e);
					}
					catch (IllegalAccessException e)
					{
						logger.error("", e);
					}
				}
			}
		}

		return rbtContentProvider;
	}

	public static String processContentRequest(WebServiceContext task)
	{
		if (!Utility.isValidIP(task.getString(param_ipAddress)))
			return Utility.getInvalidIPXML();

		String response = null;
		String action = task.getString(param_action);
		if (action != null && action.equalsIgnoreCase(action_add))
			response = getRBTContentProvider().addContent(task);
		else if (action != null && action.equalsIgnoreCase(action_recommendation_music))
			response = getRBTContentProvider().getRecommendationMusicFromThirdParty(task);
		else if (action != null && action.equalsIgnoreCase(circle_top_ten))
			response = getRBTContentProvider().getCircleTopTen(task);
		else if (action != null && action.equalsIgnoreCase(api_Search))
			response = getRBTContentProvider().getSearchContentXML(task);
		else if (action != null && action.equalsIgnoreCase(action_re_recommendation_music))
			response = getRBTContentProvider().getRecommendationMusicFromThirdParty(task);
		else if (action != null && action.equalsIgnoreCase(action_memcache))
			response = getRBTContentProvider().getClipCategoryFromMecache(task);
		else
			response = getRBTContentProvider().getContentXML(task);

		return response;
	}
}
