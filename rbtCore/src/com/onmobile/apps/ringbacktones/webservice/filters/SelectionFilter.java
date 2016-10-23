package com.onmobile.apps.ringbacktones.webservice.filters;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 *
 */
public class SelectionFilter implements Filter
{
	private static Logger logger = Logger.getLogger(SelectionFilter.class);

	@FilterAttribute
	private String response;

	@FilterAttribute
	private SelectionType selectionType;

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
	public SelectionType getSelectionType()
	{
		return selectionType;
	}

	/**
	 * @param selectionType
	 */
	public void setSelectionType(String selectionType)
	{
		this.selectionType = SelectionType.valueOf(selectionType);
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.Filter#filter()
	 */
	public String filter(WebServiceContext webServiceContext)
	{
		switch (selectionType)
		{
		case SPECIAL_CALLER_SELECTION:
			String callerID = webServiceContext.getString(WebServiceConstants.param_callerID);
			if (callerID != null && !callerID.equalsIgnoreCase(WebServiceConstants.ALL))
			{
				if (logger.isDebugEnabled())
					logger.debug("Returning response : " + response);

				return response;
			}
			break;

		case LOOP_SELECTION:
			webServiceContext.put(WebServiceConstants.param_inLoop, WebServiceConstants.NO);
			break;
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
		builder.append("SelectionFilter[selectionType = ");
		builder.append(selectionType);
		builder.append(", response = ");
		builder.append(response);
		builder.append("]");

		return builder.toString();
	}

	/**
	 * @author sridhar.sindiri
	 *
	 */
	public enum SelectionType
	{
		/**
		 * represents the special caller selection
		 */
		SPECIAL_CALLER_SELECTION,

		/**
		 * represents the loop selection
		 */
		LOOP_SELECTION;
	}
}
