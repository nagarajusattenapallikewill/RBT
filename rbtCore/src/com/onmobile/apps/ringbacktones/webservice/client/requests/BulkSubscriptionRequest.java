/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class BulkSubscriptionRequest extends SubscriptionRequest
{
	private String bulkTaskFile = null;

	/**
	 * @param bulkTaskFile
	 */
	public BulkSubscriptionRequest(String bulkTaskFile)
	{
		super(null);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile
	 * @param isPrepaid
	 * @param mode
	 * @param modeInfo
	 * @param subscriptionClass
	 * @param rentalPack
	 * @param freePeriod
	 * @param rbtType
	 * @param isDirectActivation
	 * @param subscriptionPeriod
	 */
	public BulkSubscriptionRequest(String bulkTaskFile, Boolean isPrepaid,
			String mode, String modeInfo, String subscriptionClass,
			String rentalPack, Integer freePeriod, Integer rbtType,
			Boolean isDirectActivation, String subscriptionPeriod)
	{
		super(null, isPrepaid, mode, modeInfo, subscriptionClass, rentalPack,
				freePeriod, rbtType, isDirectActivation, subscriptionPeriod);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile
	 * @param mode
	 * @param modeInfo
	 * @param checkSubscriptionClass
	 */
	public BulkSubscriptionRequest(String bulkTaskFile, String mode,
			String modeInfo, Boolean checkSubscriptionClass)
	{
		super(null, mode, modeInfo, checkSubscriptionClass);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @return the bulkTaskFile
	 */
	public String getBulkTaskFile()
	{
		return bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile the bulkTaskFile to set
	 */
	public void setBulkTaskFile(String bulkTaskFile)
	{
		this.bulkTaskFile = bulkTaskFile;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.SubscriptionRequest#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (bulkTaskFile != null) requestParams.put(param_bulkTaskFile, bulkTaskFile);

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String superString = super.toString();
		superString = superString.substring(superString.indexOf('[') + 1);

		StringBuilder builder = new StringBuilder();
		builder.append("BulkSubscriptionRequest[bulkTaskFile = ");
		builder.append(bulkTaskFile);
		builder.append(", ");
		builder.append(superString);
		return builder.toString();
	}
}
