package com.onmobile.apps.ringbacktones.ussd.common;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

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
	public static ArrayList<String> tokenizeArrayList(String stringToTokenize, String delimiter) {
		if (stringToTokenize == null)
			return null;
		String delimiterUsed = ",";

		if (delimiter != null)
			delimiterUsed = delimiter;

		ArrayList<String> result = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(stringToTokenize, delimiterUsed);
		while (tokens.hasMoreTokens())
			result.add(tokens.nextToken().toLowerCase());
		return result;
	}
	/*
	 * Make URL by appending key-value
	 */
	public static String processInputParameters(Map<String, String> input) {
		StringBuilder resp=new StringBuilder();
		resp.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		Set<String> keySet = input.keySet();
		for (String key : keySet)
		{
			if(!(key.equalsIgnoreCase("subscriber") ||key.equalsIgnoreCase("answer")|| key.equalsIgnoreCase("invalidResp")))
				resp.append("&").append(key).append("=").append(input.get(key));
		}

		return resp.toString();
	}
	/*
	 * Get all the parameters in hash map input
	 * and make the current URL
	 */
	public static String addInputParameters(Map<String, String> input) {
		StringBuilder resp=new StringBuilder();
		resp.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		Set<String> keySet = input.keySet();
		for (String key : keySet)
		{
			if(!(key.equalsIgnoreCase("next") || key.equalsIgnoreCase("subscriber") || key.equalsIgnoreCase("action") ||key.equalsIgnoreCase("answer")|| key.equalsIgnoreCase("invalidResp")))
				resp.append("&").append(key).append("=").append(input.get(key));
		}

		return resp.toString();
	}
	/*
	 * To get no of rows of texts according to the  
	 * maximum no of char can be shown in a screen
	 * @param String s,int maxlen
	 * @return string[]
	 */
	public static String[] getTextRows(String s, int maxLen)
	{
		int j1 = 0;
		int k1 = s.indexOf(' ');

		Vector vector = new Vector();
		StringBuffer stringbuffer = new StringBuffer();
		String s2 = "";
		while(j1 != -1) 
		{
			int l1 = j1 != 0 ? j1 + 1 : j1;
			String s1="";

			if(k1 != -1){
				s1 = s.substring(l1, k1);
			}

			else
				s1 = s.substring(l1);
			j1=k1;
			k1 = s.indexOf(' ', j1 + 1);

			if(s1.length() != 0)
			{
				if(stringbuffer.length() > 0)
					stringbuffer.append(' ');
				stringbuffer.append(s1);
				if((stringbuffer.toString().length()) > maxLen)
				{
					if(s2.length() > 0)
						vector.addElement(s2);
					stringbuffer.setLength(0);
					stringbuffer.append(s1);
				}
				s2 = stringbuffer.toString();

			}
		}

		if(s2.length() > 0)
			vector.addElement(s2);
		String as[] = new String[vector.size()];
		vector.copyInto(as);
		return as;
	}
}
