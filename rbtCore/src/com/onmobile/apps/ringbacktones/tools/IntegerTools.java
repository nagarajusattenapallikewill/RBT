package com.onmobile.apps.ringbacktones.tools;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class IntegerTools
{
	public static int getInteger(String str)
	{
		int num = -1;
		if(str == null)
			return num;
		try
		{
			num = Integer.parseInt(str);
		}
		catch(Exception e)
		{
			num = -1;
		}
		return num;
	}
	
	public static int getInteger(String str, int defaultValue)
	{
		int num = -1;
		if(str == null)
			return defaultValue;
		try
		{
			num = Integer.parseInt(str);
		}
		catch(Exception e)
		{
			num = defaultValue;
		}
		return num;
	}
	
	public static ArrayList<Integer> getIntegers(String str, String defaultValue, String delimiter)
	{
		int num = -1;
		if(str == null)
			str = defaultValue;
		if(str == null)
			return null;
		if(delimiter == null)
			delimiter = ",";
		ArrayList<Integer> list = new ArrayList<Integer>();
		StringTokenizer stk = new StringTokenizer(str, delimiter);
		while(stk.hasMoreTokens())
		{
			num = getInteger(stk.nextToken().trim());
			if(num != -1)
				list.add(num);
		}
		return list;
	}
	
}


