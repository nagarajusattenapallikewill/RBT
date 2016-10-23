/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

/**
 * @author vinayasimha.patil
 *
 */
public class BulkUpdateDetailsRequest extends UpdateDetailsRequest
{
	private String bulkTaskFile = null;
	private String info = null;

	/**
	 * 
	 */
	public BulkUpdateDetailsRequest()
	{
		super(null);
	}

	/**
	 * @param bulkTaskFile
	 */
	public BulkUpdateDetailsRequest(String bulkTaskFile)
	{
		super(null);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile
	 * @param isPressStarIntroEnabled
	 */
	public BulkUpdateDetailsRequest(String bulkTaskFile, Boolean isNewsLetterOn)
	{
		super(null, isNewsLetterOn);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile
	 * @param isPrepaid
	 * @param isPressStarIntroEnabled
	 */
	public BulkUpdateDetailsRequest(String bulkTaskFile, Boolean isPrepaid, Boolean isPressStarIntroEnabled)
	{
		super(null, isPrepaid, isPressStarIntroEnabled);
		this.bulkTaskFile = bulkTaskFile;
	}

	/**
	 * @param bulkTaskFile
	 * @param isBlacklisted
	 * @param blacklistType
	 */
	public BulkUpdateDetailsRequest(String bulkTaskFile, Boolean isBlacklisted,
			String blacklistType)
	{
		super(null, isBlacklisted, blacklistType);
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

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.UpdateDetailsRequest#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (bulkTaskFile != null) requestParams.put(param_bulkTaskFile, bulkTaskFile);
		if (info != null) requestParams.put(param_info, info);

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
		builder.append("BulkUpdateDetailsRequest[bulkTaskFile = ");
		builder.append(bulkTaskFile);
		builder.append(", ");
		builder.append(superString);
		return builder.toString();
	}
}
