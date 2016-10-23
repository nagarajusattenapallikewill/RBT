package com.onmobile.apps.ringbacktones.v2.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
@Service(value = BeanConstant.OPERATOR_BSNL)
@Scope(value = Constants.SCOPE_PROTOTYPE)
public class BsnlUtility extends DefaultOperatorUtility{

	private static Set<String> cvCircleId = null;
	
	private static Logger logger = Logger.getLogger(DefaultOperatorUtility.class);
	
	static {
		try {
			resourceBundle = ResourceBundle.getBundle("bsnl_config");
			initilizeCVCircle(cvCircleId);

		
		} catch(MissingResourceException e) {
			logger.error("Exception Occured: "+e,e);
		}
	}
	
	public Set<String> getCvCircleId() {
		return cvCircleId;
	}

	
}
