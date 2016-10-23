package com.onmobile.apps.ringbacktones.tools;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Toolbox
{
	public static String getStackTrace(Throwable ex)
	{
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if (ex instanceof Exception)
		{
			Exception exception = (Exception) ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0, trace.length() - 2);
			trace = System.getProperty("line.separator") + " \t" + trace;
		}
		return trace;
	}
	
	public static ArrayList<Integer> reorderNumbers(ArrayList<Integer> list)
	{
		if(list == null || list.size() == 0)
			return null;
		ArrayList<Integer> negativeList= new ArrayList<Integer>();
		negativeList.add(-1);
		if(list.removeAll(negativeList));
		if(list.size() == 0)
			return null;
		Collections.sort(list);
		return list;
	}
}
