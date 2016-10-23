package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * 
 * @author md.alam
 *
 */

public class ComVivaConfigurations {

	private static ComVivaConfigurations comVivaConfigurations = null;
	private ResourceBundle resourceBundle = null;
	private Set<String> northHubCircleId = null;
	private Set<String> eastHubCircleId = null;
	private Set<String> westHubCircleId = null;
	private Map<String, String> responseMapping = null;
	private static Logger logger = Logger
			.getLogger(ComVivaConfigurations.class);

	public ComVivaConfigurations() {

		resourceBundle = ResourceBundle.getBundle("comvivaConfigurations");

		initializeNorthHub();
		initializeEastHub();
		initializeWestHub();
		initializeResponseMapping();
	}

	private void initializeNorthHub() {
		String circleIds = getValueFromResourceBundle("CV_CIRCLE_ID_NORTH");
		if (circleIds != null && !circleIds.isEmpty()) {
			northHubCircleId = new HashSet<String>();
			String[] arrCircledId = circleIds.split(",");
			for (String circleId : arrCircledId) {
				northHubCircleId.add(circleId);
			}
		}
	}

	private void initializeEastHub() {
		String circleIds = getValueFromResourceBundle("CV_CIRCLE_ID_EAST");
		if (circleIds != null && !circleIds.isEmpty()) {
			eastHubCircleId = new HashSet<String>();
			String[] arrCircledId = circleIds.split(",");
			for (String circleId : arrCircledId) {
				eastHubCircleId.add(circleId);
			}
		}
	}

	private void initializeWestHub() {
		String circleIds = getValueFromResourceBundle("CV_CIRCLE_ID_WEST");
		if (circleIds != null && !circleIds.isEmpty()) {
			westHubCircleId = new HashSet<String>();
			String[] arrCircledId = circleIds.split(",");
			for (String circleId : arrCircledId) {
				westHubCircleId.add(circleId);
			}
		}
	}
	
	public String getUrl(String urlKey, String circleId) {
		String url = null;
		if(circleId != null && !circleId.isEmpty()) {
			if(northHubCircleId.contains(circleId.toUpperCase())) {
				url = getValueFromResourceBundle(urlKey+".NORTH");
			} else if(eastHubCircleId.contains(circleId.toUpperCase())) {
				url = getValueFromResourceBundle(urlKey+".EAST");
			} else if(westHubCircleId.contains(circleId.toUpperCase())) {
				url = getValueFromResourceBundle(urlKey+".WEST");
			}
		}
		return url;
	}

	public String getValueFromResourceBundle(String key) {
		String value = null;

		try {
			value = resourceBundle.getString(key).trim();
		} catch (MissingResourceException e) {
			logger.info("RBT:: " + e.getMessage());
		}

		return value;
	}
	
	private void initializeResponseMapping() {
		responseMapping = new HashMap<String, String>();
		responseMapping.put("0", "SUCCESS");
		responseMapping.put("46", "SUCCESS");
	}

	
	public Map<String, String> getResponseMapping() {
		return responseMapping;
	}

	public static ComVivaConfigurations getInstance() {
		if (comVivaConfigurations == null) {
			synchronized (ComVivaConfigurations.class) {
				if (comVivaConfigurations == null)
					comVivaConfigurations = new ComVivaConfigurations();
			}
		}
		return comVivaConfigurations;
	}

}
