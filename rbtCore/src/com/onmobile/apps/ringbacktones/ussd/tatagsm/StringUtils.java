package com.onmobile.apps.ringbacktones.ussd.tatagsm;

public class StringUtils {

	public static boolean isEmpty(String input) {
		if(null == input || input.length() <= 0) {
			return true;
		}
		return false;
	}

	public static boolean isNotEmpty(String input) {
		if(null != input && input.length() > 0) {
			return true;
		}
		return false;
	}
}
