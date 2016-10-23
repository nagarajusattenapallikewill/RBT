package com.onmobile.apps.ringbacktones.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

public class StringTools
{

	public static ArrayList<String> tokenizeAsArraylist(String input, String delimiter)
	{
		if(input == null)
			return null;
		if(delimiter == null)
			delimiter = ",";
		ArrayList<String> list = new ArrayList<String>();
		StringTokenizer stk = new StringTokenizer(input, delimiter);
		while(stk.hasMoreTokens())
			list.add(stk.nextToken().trim());
		return list;
	}
	
	public static HashMap<String, ArrayList<String>> mapManytoOne(String input, String primaryDelimiter, String secondaryDelimiter)
	{
		/*
		 * input : the string to tokenize
		 * primaryDelimiter : the delimiter between pairs of keys and values.Default value is semicolon(;)
		 * secondaryDelimiter : the delimiter between values of a key. Default value is comma(,)
		 */
		
		if(input == null)
			return null;
		if(primaryDelimiter == null)
			primaryDelimiter = ";";
		if(secondaryDelimiter == null)
			secondaryDelimiter = ",";
		if(primaryDelimiter.equalsIgnoreCase(secondaryDelimiter))
			return null;

		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		StringTokenizer stkParent = new StringTokenizer(input, primaryDelimiter);
		StringTokenizer stkChild = null;
		String key = null;
		while(stkParent.hasMoreTokens())
		{
			
			stkChild = new StringTokenizer(stkParent.nextToken().trim(), secondaryDelimiter);
			key = stkChild.nextToken().trim();
			ArrayList<String> list = new ArrayList<String>();
			while(stkChild.hasMoreTokens())
				list.add(stkChild.nextToken().trim());
			if(list.size() > 0)
				map.put(key, list);
		}
		return map;
	}
	
	public static HashMap<String, String> mapOnetoOne(String input, String primaryDelimiter, String secondaryDelimiter)
	{
		/*
		 * input : the string to tokenize
		 * primaryDelimiter : the delimiter between pairs of keys and values.Default value is semicolon(;)
		 * secondaryDelimiter : the delimiter between values of a key. Default value is comma(,)
		 */
		
		if(input == null)
			return null;
		if(primaryDelimiter == null)
			primaryDelimiter = ";";
		if(secondaryDelimiter == null)
			secondaryDelimiter = ",";
		if(primaryDelimiter.equalsIgnoreCase(secondaryDelimiter))
			return null;

		HashMap<String, String> map = new HashMap<String, String>();
		StringTokenizer stkParent = new StringTokenizer(input, primaryDelimiter);
		StringTokenizer stkChild = null;
		while(stkParent.hasMoreTokens())
		{
			
			stkChild = new StringTokenizer(stkParent.nextToken().trim(), secondaryDelimiter);
			String key = stkChild.nextToken().trim();
			String value = null;
			while(stkChild.hasMoreTokens())
				value = stkChild.nextToken().trim();
			if(key != null && value != null)
				map.put(key, value);
		}
		return map;
	}
	
	public static boolean isTrue(String str, boolean defaultValue)
	{
		if(str == null)
			return defaultValue;
		str = str.toUpperCase();
		if(str.equals("YES") || str.equals("TRUE") || str.equals("ON"))
			return true;
		return false;
	}
	
	
}
