package com.onmobile.apps.ringbacktones.webservice.filters;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 *
 */
public class ContentFilter implements Filter, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(ContentFilter.class);

	@FilterAttribute
	private String response;

	@FilterAttribute
	private CategoryType categoryType;

	@FilterAttribute
	private ContentType contentType;

	/**
	 * @return
	 */
	public String getResponse()
	{
		return response;
	}

	/**
	 * @param response
	 */
	public void setResponse(String response)
	{
		this.response = response;
	}

	/**
	 * @return
	 */
	public CategoryType getCategoryType()
	{
		return categoryType;
	}

	/**
	 * @param categoryType
	 */
	public void setCategoryType(String categoryType)
	{
		this.categoryType = CategoryType.valueOf(categoryType);
	}

	/**
	 * @return
	 */
	public ContentType getContentType()
	{
		return contentType;
	}

	/**
	 * @param contentType
	 */
	public void setContentType(String contentType)
	{
		this.contentType = ContentType.valueOf(contentType);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.Filter#filter(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public String filter(WebServiceContext webServiceContext)
	{
		if (categoryType != null && categoryType == CategoryType.SHUFFLE)
		{
			Category category = null;
			if (webServiceContext.containsKey(param_categoryID))
				category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(webServiceContext.getString(param_categoryID)));
			else if (webServiceContext.containsKey(param_categoryPromoID))
				category = RBTCacheManager.getInstance().getCategoryByPromoId(webServiceContext.getString(param_categoryPromoID));
			else if (webServiceContext.containsKey(param_categorySmsAlias))
				category = RBTCacheManager.getInstance().getCategoryBySMSAlias(webServiceContext.getString(param_categorySmsAlias));
			
			if (category != null)
			{
				if (Utility.isShuffleCategory(category.getCategoryTpe()))
				{
					if (logger.isDebugEnabled())
						logger.debug("Returning response : " + response);

					return response;
				}
			}
		}
		if (logger.isDebugEnabled())
			logger.debug("Returning response : null");

		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ContentFilter[categoryType = ");
		builder.append(categoryType);
		builder.append(", contentType = ");
		builder.append(contentType);
		builder.append(", response = ");
		builder.append(response);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * @author sridhar.sindiri
	 *
	 */
	public enum CategoryType
	{
		/**
		 * represents the shuffle categoryType
		 */
		SHUFFLE;
	}

	/**
	 * @author sridhar.sindiri
	 *
	 */
	public enum ContentType
	{

	}
}
