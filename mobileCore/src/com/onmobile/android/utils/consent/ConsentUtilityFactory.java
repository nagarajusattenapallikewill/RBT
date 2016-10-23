package com.onmobile.android.utils.consent;

import org.apache.log4j.Logger;

import com.onmobile.android.configuration.PropertyConfigurator;

public class ConsentUtilityFactory {
	private static Logger logger = Logger.getLogger(ConsentUtilityFactory.class);

	public static ConsentUtility getConsentUtlityObject() {
		String operatorName = PropertyConfigurator.getOperatorName();
		if (operatorName != null) {
			if (operatorName.toUpperCase().startsWith("AIRTEL")) {
				logger.debug("Returning AirtelConsentUtility.");
				return new AirtelConsentUtility();
			} else if (operatorName.toUpperCase().startsWith("IDEA")) {
				logger.debug("Returning IdeaConsentUtility.");
				return new IdeaConsentUtility();
			} else if (operatorName.toUpperCase().startsWith("VODAFONE")) {
				logger.debug("Returning VodafoneConsentUtility.");
				return new VodafoneConsentUtility();
			}
		} 
		logger.debug("Returning AirtelConsentUtility.");
		return new AirtelConsentUtility();

	}
	
	
	public static ConsentUtility getComvivaConsentUtlityObject() {
		String operatorName = PropertyConfigurator.getOperatorName();
		if (operatorName != null) {
			if (operatorName.toUpperCase().startsWith("AIRTEL")) {
				logger.debug("Returning AirtelConsentUtility.");
				return new AirtelComvivaConsentUtility();
			}
		}
		logger.debug("Returning AirtelConsentUtility.");
		return new AirtelConsentUtility();
	}
}
