/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.dnctoservice.exception.DNCTOException;
import com.onmobile.dnctoservice.plugin.Merger;

/**
 * RBT implementation class for the DNCTO {@link Merger}. This class has the
 * method to merge the new RBT data to the existing aggregated DNCTO data file.
 * 
 * @author vinayasimha.patil
 */
public class RBTMerger implements Merger
{
	/**
	 * Log4j logger object
	 */
	private static Logger logger = Logger.getLogger(RBTMerger.class);

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.dnctoservice.plugin.Merger#combineData(java.lang.String,
	 * org.json.JSONObject, org.json.JSONObject)
	 */
	/**
	 * This method will be called by the DNCTO framework to merge the RBT data
	 * of a subscriber to the aggregated DNCTO data file.
	 * 
	 * @param key
	 *            channel name of RBT assigned by DNCTO framework
	 * @param firstObject
	 *            JSONObject of a subscriber from the aggregated DNCTO data file
	 * @param secondObject
	 *            JSONObject of the subscriber to be merged
	 * @return the combined JSONObject
	 * @throws DNCTOException
	 *             if unable to merge the RBt information
	 */
	@Override
	public JSONObject combineData(String key, JSONObject firstObject,
			JSONObject secondObject) throws DNCTOException
	{
		JSONObject combinedData = new JSONObject();
		try
		{
			if (logger.isDebugEnabled())
			{
				logger.info("firstObject: " + firstObject + ", secondObject: "
						+ secondObject);
			}

			JSONObject rbtFirstObject = firstObject.optJSONObject(key);
			JSONObject rbtSecondObject = secondObject.optJSONObject(key);

			if (rbtFirstObject == null)
				rbtFirstObject = new JSONObject();

			if (rbtSecondObject == null)
				rbtSecondObject = new JSONObject();

			@SuppressWarnings("unchecked")
			Iterator<String> iterator = rbtSecondObject.keys();
			while (iterator.hasNext())
			{
				String status = iterator.next();
				JSONArray actionTime = rbtSecondObject.optJSONArray(status);
				rbtFirstObject.append(status, actionTime.getLong(0));
			}

			combinedData.put(key, rbtFirstObject);
		}
		catch (JSONException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(e.getMessage());
			dnctoException.initCause(e);
			throw dnctoException;
		}

		if (logger.isDebugEnabled())
			logger.info("combinedData: " + combinedData);
		return combinedData;
	}
}
