package com.onmobile.apps.ringbacktones.monitor;

import java.util.HashMap;

/**
 * This is the interface used for RBT Monitoring. Any class which monitors the delays occuring @
 * different nodes of RBT should implement this interface
 * 
 * @author Sreekar
 * @since 2010-01-07
 */
public interface iRBTMonitor {
	// all methods related to node
	public String startMonitor(String msisdn, String traceType);
	public String endMonitor(String msisdn);
	public void startNode(String msisdn, RBTNode node);
	public void endNode(String msisdn, RBTNode node);
	public boolean canMonitor(String msisdn, String traceType, StringBuffer reason);
	public boolean validWebServiceNode(String nodeName);
	public boolean isThirdPartyRequest(String traceType);
	// all methods related to processing
	public String processCopyMonitor(HashMap<String, String> map);
	public String processSMSMonitor(HashMap<String, String> map);
	public String processIVRMonitor(HashMap<String, String> map);
	public String processThirdPartyMonitor(HashMap<String, String> map);
	public String processWebServiceMonitor(HashMap<String, String> map);
	public String getGenericErrorResponse();
}