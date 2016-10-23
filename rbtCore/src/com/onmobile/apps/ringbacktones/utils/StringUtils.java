package com.onmobile.apps.ringbacktones.utils;

import java.util.StringTokenizer;


import org.apache.log4j.Logger;


public class StringUtils {

	private static Logger logger = Logger.getLogger(ListUtils.class);
	
	public static String[] toStringArray(String str, String delimiter) {
		if (null != str && null != delimiter) {
			StringTokenizer st = new StringTokenizer(str, delimiter);
			String[] arr = new String[st.countTokens()];
			int i = 0;
			while (st.hasMoreTokens()) {
				arr[i] = st.nextToken().trim();
				i++;
			}
			logger.debug("Converted str: " + str + ", to array: " + arr.length);
			return arr;
		}
		logger.warn("Failed to converted str: " + str+", delimiter: "+delimiter);
		return null;
	}
}
