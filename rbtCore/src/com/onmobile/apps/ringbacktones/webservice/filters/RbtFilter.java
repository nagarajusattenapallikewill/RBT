package com.onmobile.apps.ringbacktones.webservice.filters;

import java.util.Map;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 * 
 */
public class RbtFilter
{
	/**
	 * Contains all the filter objects with key as 'name' attribute in the xml
	 */
	private Map<String, Filter> filtersMap = null;

	/**
	 * @return the filtersMap
	 */
	public Map<String, Filter> getFiltersMap()
	{
		return filtersMap;
	}

	/**
	 * @param filtersMap
	 *            the filtersMap to set
	 */
	public void setFiltersMap(Map<String, Filter> filtersMap)
	{
		this.filtersMap = filtersMap;
	}

	/**
	 * @param webServiceContext
	 * @return
	 */
	public String filterSelection(WebServiceContext webServiceContext)
	{
		if (filtersMap == null)
			return null;

		Filter selectionFilter = filtersMap.get("selection");
		if (selectionFilter == null)
			return null;

		return selectionFilter.filter(webServiceContext);
	}
}
