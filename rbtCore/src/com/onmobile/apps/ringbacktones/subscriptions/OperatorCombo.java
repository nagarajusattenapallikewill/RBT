package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.Hashtable;

public class OperatorCombo
{
	String name = null;
	boolean isLive = false;
	Hashtable copierList = new Hashtable();
	Hashtable copieeList = new Hashtable();
	boolean isTestOn = true;
	ArrayList testNumList = new ArrayList();
	String classType = null;
	
	public OperatorCombo(String name, boolean isLive , Hashtable copierList, Hashtable copieeList,  boolean isTestOn, ArrayList testNumList, String classType)
	{
		this.name = name;
		this.isLive = isLive;
		this.copierList = copierList;
		this.copieeList = copieeList;
		this.isTestOn = isTestOn;
		this.testNumList = testNumList;
		this.classType = classType;
	}
	
	public String toString()
	{
		String returnStr = null;
		returnStr = "[name is "+name+"] ";
		returnStr += "[isLive is "+isLive+"] ";
		returnStr += "[copierList is "+copierList+"] ";
		returnStr += "[copieeList is "+copieeList+"] ";
		returnStr += "[isTestOn is "+isTestOn+"] ";
		returnStr += "[testNumList is "+testNumList+"] ";
		returnStr += "[classType is "+classType+"] ";
		return returnStr;
	}
}

