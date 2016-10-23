package com.onmobile.apps.ringbacktones.daemons.genericftp.beans;

import java.util.Map;

/**
 * @author sridhar.sindiri
 *
 */
public class ChargeClassMap
{
	private String subClassMapId;
	private String action;
	private String circle;
	private String map;
	private Map<String, String> chargeClassMap = null;

	/**
	 * @return the subClassMapId
	 */
	public String getSubClassMapId() {
		return subClassMapId;
	}

	/**
	 * @param subClassMapId the subClassMapId to set
	 */
	public void setSubClassMapId(String subClassMapId) {
		this.subClassMapId = subClassMapId;
	}

	/**
	 * @return the action
	 */
	public String getAction() {
		return action;
	}

	/**
	 * @param action the action to set
	 */
	public void setAction(String action) {
		this.action = action;
	}

	/**
	 * @return the chargeClassMap
	 */
	public Map<String, String> getChargeClassMap()
	{
		if (chargeClassMap == null)
		{
			if (map != null)
			{
				String[] maps = map.split(",");
				for (String eachMap : maps)
				{
					String[] tokens = eachMap.split(":");
					chargeClassMap.put(tokens[0].trim(), tokens[1].trim());
				}
			}
		}

		return chargeClassMap;
	}

	/**
	 * @param chargeClassMap the chargeClassMap to set
	 */
	public void setChargeClassMap(Map<String, String> chargeClassMap) {
		this.chargeClassMap = chargeClassMap;
	}

	/**
	 * @return the circle
	 */
	public String getCircle() {
		return circle;
	}

	/**
	 * @param circle the circle to set
	 */
	public void setCircle(String circle) {
		this.circle = circle;
	}

	/**
	 * @return the map
	 */
	public String getMap() {
		return map;
	}

	/**
	 * @param map the map to set
	 */
	public void setMap(String map) {
		this.map = map;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ChargeClassMap [subClassMapId = ");
		builder.append(subClassMapId);
		builder.append(", action = ");
		builder.append(action);
		builder.append(", circle = ");
		builder.append(circle);
		builder.append(", map = ");
		builder.append(map);
		builder.append(", chargeClassMap = ");
		builder.append(chargeClassMap);
		builder.append("] ");

		return builder.toString();
	}
}
