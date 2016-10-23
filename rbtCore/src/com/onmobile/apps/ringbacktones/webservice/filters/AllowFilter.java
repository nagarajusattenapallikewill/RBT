package com.onmobile.apps.ringbacktones.webservice.filters;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 *
 */
public class AllowFilter extends FilterCondition
{
	private static Logger logger = Logger.getLogger(AllowFilter.class);

	@FilterAttribute
	private String response;

	private List<Filter> childFilters;

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

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.FilterCondition#getChildFilters()
	 */
	public List<Filter> getChildFilters()
	{
		return childFilters;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.FilterCondition#setChildFilters(java.util.List)
	 */
	public void setChildFilters(List<Filter> childFilters)
	{
		this.childFilters = childFilters;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.FilterCondition#filter()
	 */
	@Override
	public String filter(WebServiceContext webServiceContext)
	{
		for (Filter childFilter : childFilters)
		{
			if (childFilter.filter(webServiceContext) == null)
			{
				if (logger.isDebugEnabled())
					logger.debug("Returning response : " + response);

				return response;
			}
		}

		if (logger.isDebugEnabled())
			logger.debug("Returning response : null");

		return null;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.FilterCondition#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("AllowFilter[childFilters = ");
		builder.append(childFilters);
		builder.append(", response = ");
		builder.append(response);
		builder.append("]");

		return builder.toString();
	}
}
