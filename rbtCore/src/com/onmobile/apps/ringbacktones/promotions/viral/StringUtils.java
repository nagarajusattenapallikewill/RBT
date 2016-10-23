package com.onmobile.apps.ringbacktones.promotions.viral;

/**
 * @author sridhar.sindiri
 *
 */
public class StringUtils 
{

	public static boolean isEmpty(String input) {
		if(null == input || input.trim().length() <= 0) {
			return true;
		}
		return false;
	}

	public static boolean isNotEmpty(String input) {
		return !isEmpty(input);
	}
}
