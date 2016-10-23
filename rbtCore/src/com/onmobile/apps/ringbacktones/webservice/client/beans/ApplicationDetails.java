/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vinayasimha.patil
 *
 */
public class ApplicationDetails
{
	private Parameter[] parameters = null;
	private SubscriptionClass[] subscriptionClasses = null;
	private ChargeClass[] chargeClasses = null;
	private SMSText[] smsTexts = null;
	private PickOfTheDay[] pickOfTheDays = null;
	private RBTLoginUser[] rbtLoginUsers = null;
	private Site[] sites = null;
	private ChargeSms[] chargeSmses = null;
	private Cos[] coses = null;
	private Retailer[] retailers = null;
	private FeedStatus[] feedStatuses = null;
	private Feed[] feeds = null;
	private PredefinedGroup[] predefinedGroups = null;

	/**
	 * 
	 */
	public ApplicationDetails()
	{

	}

	/**
	 * @param parameters
	 * @param subscriptionClasses
	 * @param chargeClasses
	 * @param smsTexts
	 * @param pickOfTheDays
	 * @param rbtLoginUsers
	 * @param sites
	 * @param chargeSmses
	 * @param coses
	 * @param retailers
	 * @param feedStatuses
	 * @param feeds
	 * @param predefinedGroups
	 */
	public ApplicationDetails(Parameter[] parameters,
			SubscriptionClass[] subscriptionClasses,
			ChargeClass[] chargeClasses, SMSText[] smsTexts,
			PickOfTheDay[] pickOfTheDays, RBTLoginUser[] rbtLoginUsers,
			Site[] sites, ChargeSms[] chargeSmses, Cos[] coses,
			Retailer[] retailers, FeedStatus[] feedStatuses, Feed[] feeds,
			PredefinedGroup[] predefinedGroups)
	{
		this.parameters = parameters;
		this.subscriptionClasses = subscriptionClasses;
		this.chargeClasses = chargeClasses;
		this.smsTexts = smsTexts;
		this.pickOfTheDays = pickOfTheDays;
		this.rbtLoginUsers = rbtLoginUsers;
		this.sites = sites;
		this.chargeSmses = chargeSmses;
		this.coses = coses;
		this.retailers = retailers;
		this.feedStatuses = feedStatuses;
		this.feeds = feeds;
		this.predefinedGroups = predefinedGroups;
	}

	/**
	 * @return the parameters
	 */
	public Parameter[] getParameters()
	{
		return parameters;
	}

	/**
	 * @return the subscriptionClasses
	 */
	public SubscriptionClass[] getSubscriptionClasses()
	{
		return subscriptionClasses;
	}

	/**
	 * @return the chargeClasses
	 */
	public ChargeClass[] getChargeClasses()
	{
		return chargeClasses;
	}

	/**
	 * @return the smsTexts
	 */
	public SMSText[] getSmsTexts()
	{
		return smsTexts;
	}

	/**
	 * @return the pickOfTheDays
	 */
	public PickOfTheDay[] getPickOfTheDays()
	{
		return pickOfTheDays;
	}

	/**
	 * @return the rbtLoginUsers
	 */
	public RBTLoginUser[] getRbtLoginUsers()
	{
		return rbtLoginUsers;
	}

	/**
	 * @return the sites
	 */
	public Site[] getSites()
	{
		return sites;
	}

	/**
	 * @return the chargeSmses
	 */
	public ChargeSms[] getChargeSmses()
	{
		return chargeSmses;
	}

	/**
	 * @return the coses
	 */
	public Cos[] getCoses()
	{
		return coses;
	}

	/**
	 * @return the retailers
	 */
	public Retailer[] getRetailers()
	{
		return retailers;
	}

	/**
	 * @return the feedStatuses
	 */
	public FeedStatus[] getFeedStatuses()
	{
		return feedStatuses;
	}


	/**
	 * @return the feeds
	 */
	public Feed[] getFeeds()
	{
		return feeds;
	}

	public PredefinedGroup[] getPredefinedGroups()
	{
		return predefinedGroups;
	}

	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Parameter[] parameters)
	{
		this.parameters = parameters;
	}

	/**
	 * @param subscriptionClasses the subscriptionClasses to set
	 */
	public void setSubscriptionClasses(SubscriptionClass[] subscriptionClasses)
	{
		this.subscriptionClasses = subscriptionClasses;
	}

	/**
	 * @param chargeClasses the chargeClasses to set
	 */
	public void setChargeClasses(ChargeClass[] chargeClasses)
	{
		this.chargeClasses = chargeClasses;
	}

	/**
	 * @param smsTexts the smsTexts to set
	 */
	public void setSmsTexts(SMSText[] smsTexts)
	{
		this.smsTexts = smsTexts;
	}

	/**
	 * @param pickOfTheDays the pickOfTheDays to set
	 */
	public void setPickOfTheDays(PickOfTheDay[] pickOfTheDays)
	{
		this.pickOfTheDays = pickOfTheDays;
	}

	/**
	 * @param rbtLoginUser the rbtLoginUser to set
	 */
	public void setRbtLoginUsers(RBTLoginUser[] rbtLoginUsers)
	{
		this.rbtLoginUsers = rbtLoginUsers;
	}

	/**
	 * @param sites the sites to set
	 */
	public void setSites(Site[] sites)
	{
		this.sites = sites;
	}

	/**
	 * @param chargeSmses the chargeSmses to set
	 */
	public void setChargeSmses(ChargeSms[] chargeSmses)
	{
		this.chargeSmses = chargeSmses;
	}

	/**
	 * @param coses the coses to set
	 */
	public void setCoses(Cos[] coses)
	{
		this.coses = coses;
	}

	/**
	 * @param retailers the retailers to set
	 */
	public void setRetailers(Retailer[] retailers)
	{
		this.retailers = retailers;
	}

	/**
	 * @param feedStatuses the feedStatuses to set
	 */
	public void setFeedStatuses(FeedStatus[] feedStatuses)
	{
		this.feedStatuses = feedStatuses;
	}


	/**
	 * @param feeds the feeds to set
	 */
	public void setFeeds(Feed[] feeds)
	{
		this.feeds = feeds;
	}

	public void setPredefinedGroups(PredefinedGroup[] predefinedGroups)
	{
		this.predefinedGroups = predefinedGroups;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(chargeClasses);
		result = prime * result + Arrays.hashCode(chargeSmses);
		result = prime * result + Arrays.hashCode(coses);
		result = prime * result + Arrays.hashCode(feedStatuses);
		result = prime * result + Arrays.hashCode(feeds);
		result = prime * result + Arrays.hashCode(parameters);
		result = prime * result + Arrays.hashCode(pickOfTheDays);
		result = prime * result + Arrays.hashCode(predefinedGroups);
		result = prime * result + ((rbtLoginUsers == null) ? 0 : rbtLoginUsers.hashCode());
		result = prime * result + Arrays.hashCode(retailers);
		result = prime * result + Arrays.hashCode(sites);
		result = prime * result + Arrays.hashCode(smsTexts);
		result = prime * result + Arrays.hashCode(subscriptionClasses);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ApplicationDetails))
			return false;
		ApplicationDetails other = (ApplicationDetails) obj;
		if (!Arrays.equals(chargeClasses, other.chargeClasses))
			return false;
		if (!Arrays.equals(chargeSmses, other.chargeSmses))
			return false;
		if (!Arrays.equals(coses, other.coses))
			return false;
		if (!Arrays.equals(feedStatuses, other.feedStatuses))
			return false;
		if (!Arrays.equals(feeds, other.feeds))
			return false;
		if (!Arrays.equals(parameters, other.parameters))
			return false;
		if (!Arrays.equals(pickOfTheDays, other.pickOfTheDays))
			return false;
		if (!Arrays.equals(predefinedGroups, other.predefinedGroups))
			return false;
		if (rbtLoginUsers == null)
		{
			if (other.rbtLoginUsers != null)
				return false;
		}
		else if (!rbtLoginUsers.equals(other.rbtLoginUsers))
			return false;
		if (!Arrays.equals(retailers, other.retailers))
			return false;
		if (!Arrays.equals(sites, other.sites))
			return false;
		if (!Arrays.equals(smsTexts, other.smsTexts))
			return false;
		if (!Arrays.equals(subscriptionClasses, other.subscriptionClasses))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ApplicationDetails[chargeClasses = ");
		builder.append(Arrays.toString(chargeClasses));
		builder.append(", chargeSmses = ");
		builder.append(Arrays.toString(chargeSmses));
		builder.append(", coses = ");
		builder.append(Arrays.toString(coses));
		builder.append(", feedStatuses = ");
		builder.append(Arrays.toString(feedStatuses));
		builder.append(", feeds = ");
		builder.append(Arrays.toString(feeds));
		builder.append(", parameters = ");
		builder.append(Arrays.toString(parameters));
		builder.append(", pickOfTheDays = ");
		builder.append(Arrays.toString(pickOfTheDays));
		builder.append(", predefinedGroups = ");
		builder.append(Arrays.toString(predefinedGroups));
		builder.append(", rbtLoginUsers = ");
		builder.append(rbtLoginUsers);
		builder.append(", retailers = ");
		builder.append(Arrays.toString(retailers));
		builder.append(", sites = ");
		builder.append(Arrays.toString(sites));
		builder.append(", smsTexts = ");
		builder.append(Arrays.toString(smsTexts));
		builder.append(", subscriptionClasses = ");
		builder.append(Arrays.toString(subscriptionClasses));
		builder.append("]");
		return builder.toString();
	}
}
