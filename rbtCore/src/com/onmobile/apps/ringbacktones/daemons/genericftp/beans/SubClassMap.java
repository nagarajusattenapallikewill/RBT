package com.onmobile.apps.ringbacktones.daemons.genericftp.beans;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sridhar.sindiri
 *
 */
public class SubClassMap
{
	private String id;
	private String action;
	private String circle;
	private String map;
	private Map<String, String> subClassMapping = null;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the subClassMapping
	 */
	public Map<String, String> getSubClassMapping()
	{
		if (subClassMapping == null)
		{
			subClassMapping = new HashMap<String, String>();
			if (map != null)
			{
				String[] maps = map.split(",");
				for (String eachMap : maps)
				{
					String[] tokens = eachMap.split(":");
					subClassMapping.put(tokens[0].trim(), tokens[1].trim());
				}
			}
		}

		return subClassMapping;
	}

	/**
	 * @param subClassMapping the subClassMapping to set
	 */
	public void setSubClassMapping(Map<String, String> subClassMap) {
		this.subClassMapping = subClassMap;
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
		builder.append("SubClassMap [id = ");
		builder.append(id);
		builder.append(", action = ");
		builder.append(action);
		builder.append(", circle = ");
		builder.append(circle);
		builder.append(", map = ");
		builder.append(map);
		builder.append(", subClassMapping = ");
		builder.append(subClassMapping);
		builder.append("] ");

		return builder.toString();
	}
}
