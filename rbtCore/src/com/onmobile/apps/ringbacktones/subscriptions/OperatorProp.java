package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.Hashtable;

public class OperatorProp
{
	String name = null;
	boolean isLive = false;
	String url = null;
	boolean useProxy = false;
	String proxyHost = null;
	int proxyPort = -1;
	Hashtable prefixList = new Hashtable();
	boolean transferMissingContent = false;
	boolean sendMissingContentSMS = false;
	String missingContentSMSText = "The content copied is not available currently. Plz try later.";
	String classType = "DEFAULT";
	ArrayList ipList = null;
	boolean copyDefault = false;
	String senderNo = "123456";
	
	public OperatorProp(String name, boolean isLive, String url, boolean useProxy, String proxyHost, int proxyPort, Hashtable prefixList, boolean transferMissingContent, boolean sendMissingContentSMS, String missingContentSMSText, String classType, ArrayList ipList, boolean copyDefault, String senderNo)
	{
		this.name = name;
		this.isLive = isLive;
		this.url = url;
		this.useProxy = useProxy;
		this.proxyHost = proxyHost;
		this.proxyPort = proxyPort;
		this.prefixList = prefixList;
		this.transferMissingContent =transferMissingContent;
		this.sendMissingContentSMS = sendMissingContentSMS;
		this.missingContentSMSText = missingContentSMSText;
		this.classType = classType;
		this.ipList = ipList;
		this.copyDefault = copyDefault;
		this.senderNo = senderNo;
	}
	
	public String toString()
	{
		String returnStr  = null;
		
		returnStr = "[name is "+name+"] ";
		returnStr += "[isLive is "+isLive+"] ";
		returnStr += "[url is "+url+"] ";
		returnStr += "[useProxy is "+useProxy+"] ";
		returnStr += "[proxyHost is "+proxyHost+"] ";
		returnStr += "[proxyPort is "+proxyPort+"] ";
		returnStr += "[prefixList is "+prefixList+"] ";
		returnStr += "[transferMissingContent is "+transferMissingContent+"] ";
		returnStr += "[sendMissingContentSMS is "+sendMissingContentSMS+"] ";
		returnStr += "[missingContentSMSText is "+missingContentSMSText+"] ";
		returnStr += "[classType is "+classType+"] ";
		returnStr += "[ipList is "+ipList+"] ";
		returnStr += "[copyDefault is "+copyDefault+"] ";
		returnStr += "[senderNo is "+senderNo+"] ";
		
		return returnStr;
	}
}
