package com.onmobile.apps.ringbacktones.webservice.filters;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 *
 */
public class DontAllowFilter extends FilterCondition
{
	private static Logger logger = Logger.getLogger(DontAllowFilter.class);

	private List<Filter> childFilters;

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
			String childFiltersResponse = childFilter.filter(webServiceContext);
			if (childFiltersResponse != null)
			{
				if (logger.isDebugEnabled())
					logger.debug("Returning response : " + childFiltersResponse);

				return childFiltersResponse;
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
		builder.append("DontAllowFilter[childFilters = ");
		builder.append(childFilters);
		builder.append("]");

		return builder.toString();
	}
}
