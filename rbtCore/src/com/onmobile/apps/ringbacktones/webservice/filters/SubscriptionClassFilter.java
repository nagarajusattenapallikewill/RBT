package com.onmobile.apps.ringbacktones.webservice.filters;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author sridhar.sindiri
 *
 */
public class SubscriptionClassFilter implements Filter
{
	private static Logger logger = Logger.getLogger(SubscriptionClassFilter.class);

	@FilterAttribute
	private String classType;

	@FilterAttribute
	private AllowFilter allowFilter;

	@FilterAttribute
	private DontAllowFilter dontAllowFilter;

	/**
	 * @return
	 */
	public String getClassType()
	{
		return classType;
	}

	/**
	 * @param classType
	 */
	public void setClassType(String classType)
	{
		this.classType = classType;
	}

	/**
	 * @return
	 */
	public AllowFilter getAllowFilter()
	{
		return allowFilter;
	}

	/**
	 * @param allowFilter
	 */
	public void setAllowFilter(AllowFilter allowFilter)
	{
		this.allowFilter = allowFilter;
	}

	/**
	 * @return
	 */
	public DontAllowFilter getDontAllowFilter()
	{
		return dontAllowFilter;
	}

	/**
	 * @param dontAllowFilter
	 */
	public void setDontAllowFilter(DontAllowFilter dontAllowFilter)
	{
		this.dontAllowFilter = dontAllowFilter;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.filters.Filter#filter()
	 */
	public String filter(WebServiceContext webServiceContext)
	{
		Subscriber subscriber = null;
		try
		{
			subscriber = DataUtils.getSubscriber(webServiceContext);
		}
		catch (RBTException e)
		{
		}

		String response = null;
		if (subscriber != null && subscriber.subscriptionClass().equalsIgnoreCase(classType))
		{
			if (allowFilter != null)
			{
				response = allowFilter.filter(webServiceContext);
			}
			else if (dontAllowFilter != null)
			{
				response = dontAllowFilter.filter(webServiceContext);
			}
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
		builder.append("SubscriptionClassFilter[classType = ");
		builder.append(classType);
		builder.append(", allowFilter = ");
		builder.append(allowFilter);
		builder.append(", dontAllowFilter = ");
		builder.append(dontAllowFilter);
		builder.append("]");

		return builder.toString();
	}

}
