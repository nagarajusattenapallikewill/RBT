package com.onmobile.apps.ringbacktones.daemons.inline.shutdown;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class RestoreState {
	private static final Logger logger = Logger.getLogger(RestoreState.class);

	public void init() {
		try {
			if(!RBTParametersUtils.getParamAsBoolean(iRBTConstant.PROVISIONING, WebServiceConstants.INLINE_PARAMETERS, "false"))
				return;
			logger.debug("Resetting inline flag...");
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			rbtDBManager.resetInlineDaemonFlag();
			logger.debug("Resetting inline flag done...");
		} catch (Throwable t) {
			logger.error("Exception while resetting inline flag: ", t);
		} finally {
		}
	}
}
