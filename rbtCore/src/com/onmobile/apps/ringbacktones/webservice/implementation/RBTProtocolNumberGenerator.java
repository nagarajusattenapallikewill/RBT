package com.onmobile.apps.ringbacktones.webservice.implementation;

import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocol;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocolDao;

public enum RBTProtocolNumberGenerator {

	INSTANCE;

	private Logger logger = Logger.getLogger(RBTParametersUtils.class);

	private String protocolNumberStaticText = null;

	private String protocolNumberSequenceLength = null;

	private RBTProtocolNumberGenerator() {
		protocolNumberStaticText = RBTParametersUtils.getParamAsString(
				"WEBSERVICE", "PROTOCOL_NUMBER_STATIC_TEXT", null);
		protocolNumberSequenceLength = RBTParametersUtils.getParamAsString(
				"WEBSERVICE", "PROTOCOL_NUMBER_SEQUENCE", "8");
		logger.error("PROTOCOL_NUMBER_STATIC_TEXT is not configured.");
	}

	public String generateDBNo(String subscriberId) {

		StringBuffer result = new StringBuffer();
		String prefix = protocolNumberStaticText;
		RBTProtocol rbtProtocol = new RBTProtocol();

		// append prefix and applies padding to the result
		if (null != prefix) {
			int year = Calendar.getInstance().get(Calendar.YEAR);
			prefix = prefix.replace("<year>", String.valueOf(year));
			result.append(prefix);
		}

		rbtProtocol.setStaticText(prefix);
		rbtProtocol.setSubscriberId(subscriberId);

		RBTProtocolDao.getInstance().save(rbtProtocol);

		String paddingFormat = "%0" + protocolNumberSequenceLength + "d";

		String paddedProtocolNo = String.format(paddingFormat, rbtProtocol.getProtocolId());

		result.append(paddedProtocolNo);

		logger.info("Returning protocolNo1: " + result.toString());
		return result.toString();
	}

}
