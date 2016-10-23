package com.onmobile.apps.ringbacktones.ussd.common;

import java.util.ResourceBundle;
/*
 * This class provides some methods to read the properties file
 */
public class USSDConfigParameters {

	private static USSDConfigParameters instance = new USSDConfigParameters();

	private ResourceBundle bundle = null;

	private USSDConfigParameters() {
		bundle = ResourceBundle.getBundle("ussdconfig");
	}

	public static USSDConfigParameters getInstance() {
		return instance;
	}

	public int getMessageLength() {
		String messageLength = USSDConfigParameters.getInstance().getParameter("MESSAGE_LENGTH");
		int msgLength = 150;

		if(StringUtils.isNotEmpty(messageLength)) {
			try {
				msgLength = Integer.parseInt(messageLength);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}
		return msgLength;
	}

	public String getMessageNewLine() {
		String newLine = USSDConfigParameters.getInstance().getParameter("MESSAGE_NEW_LINE");

		if(StringUtils.isEmpty(newLine)) {
			return "\n";
		}
		return newLine;
	}

	public String getParameter(String paramName) {
		return bundle.getString(paramName);
	}

	public String getUSSDHostURL() {
		String hostURL = USSDConfigParameters.getInstance().getParameter("USSD_HOST_URL");
		if(StringUtils.isEmpty(hostURL)) {
			hostURL = "";
		}

		return hostURL;
	}

	public boolean isUMPResponse() {
		String umpResponse = USSDConfigParameters.getInstance().getParameter("UMP_RESPONSE");
		if(StringUtils.isEmpty(umpResponse)) {
			umpResponse = "false";
		}
		return umpResponse.equalsIgnoreCase("true");
	}
}
