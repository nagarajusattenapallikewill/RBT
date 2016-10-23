package com.onmobile.apps.ringbacktones.webservice.filters;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 *
 */
public class FilterCondition implements Filter
{
	private static Logger logger = Logger.getLogger(FilterCondition.class);

	@FilterAttribute
	private String name;

	@FilterAttribute
	private ConditionType conditionType;

	private List<Filter> childFilters;

	/**
	 * @return
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return
	 */
	public ConditionType getConditionType()
	{
		return conditionType;
	}

	/**
	 * @param conditionType
	 */
	public void setConditionType(String conditionType)
	{
		this.conditionType = ConditionType.valueOf(conditionType);
	}

	/**
	 * @return
	 */
	public List<Filter> getChildFilters()
	{
		return childFilters;
	}

	/**
	 * @param childFilters
	 */
	public void setChildFilters(List<Filter> childFilters)
	{
		this.childFilters = childFilters;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.Filter#filter()
	 */
	public String filter(WebServiceContext webServiceContext)
	{
		boolean isANDOperation = conditionType == ConditionType.AND;
		boolean isOROperation = conditionType == ConditionType.OR;;

		String response = null;

		boolean isValidRequest = false;
		for (Filter childFilter : childFilters)
		{
			response = childFilter.filter(webServiceContext);
			isValidRequest = (response != null);
			if (isANDOperation && !isValidRequest)
				break;
			else if (isOROperation && isValidRequest)
				break;
		}

		if (logger.isDebugEnabled())
			logger.debug("Returning response : " + response);

		return response;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("FilterCondition[name = ");
		builder.append(name);
		builder.append(", conditionType = ");
		builder.append(conditionType);
		builder.append(", childFilters = ");
		builder.append(childFilters);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * @author sridhar.sindiri
	 *
	 */
	public enum ConditionType
	{
		/**
		 * Represents the AND operation.
		 */
		AND,

		/**
		 * Represents the OR operation.
		 */
		OR
	}
}
