package com.onmobile.apps.ringbacktones.monitor;

import java.util.Date;

/**
 * This class stores and manipulates the monitoring of a particular node
 * 
 * @author Sreekar
 * @since 2010-01-07
 */
public class RBTNode {
	public static final String RESPONSE_SUCCESS = "SUCCESS";
	public static final String RESPONSE_FAILURE = "FAILURE";
	//All Nodes
	public static final String NODE_PLAYER_UPDATER			= "PLAYER_UPDATER";
	public static final String NODE_COPY_PROCESSOR			= "COPY_PROCESSOR";
	public static final String NODE_SM_DAEMON_ACT			= "SM_DAEMON_ACT";
	public static final String NODE_SM_DAEMON_DCT			= "SM_DAEMON_DCT";
	public static final String NODE_SM_DAEMON_SEL_ACT		= "SM_DAEMON_SELECTION_ACT";
	public static final String NODE_SM_DAEMON_SEL_DCT		= "SM_DAEMON_SELECTION_DCT";
	public static final String NODE_SM_CALLBACK_SUB			= "SM_CALLBACK_SUB";
	public static final String NODE_SM_CALLBACK_SEL			= "SM_CALLBACK_SELECTION";
	public static final String NODE_SM_CALLBACK_PACK		= "SM_CALLBACK_PACK";
	public static final String NODE_PLAYER_DAEMON_SUB		= "PLAYER_DAEMON_SUB";
	public static final String NODE_PLAYER_DAEMON_SEL		= "PLAYER_DAEMON_SEL";
	public static final String NODE_END						= "END";
	public static final String NODE_IVR						= "IVR";
	public static final String NODE_SMS						= "SMS";
	public static final String NODE_CCC						= "CCC";
	public static final String NODE_USSD					= "USSD";
	public static final String NODE_ENVIO					= "ENVIO";
	public static final String NODE_EC						= "EC";
	public static final String NODE_MOD						= "MOD";
	public static final String NODE_AUTODIAL				= "AUTODIAL";
	public static final String NODE_WEBSERVICE				= "WEBSERVICE";
	
	private String _name;
	private String _response = null;
	private String _message = null;
	private Date _startTime = null;
	
	public RBTNode(String name) {
		_name = name;
	}
	
	public RBTNode(String name, String response, String message) {
		_name = name;
		_response = response;
		_message = message;
	}
	
	public String getNodeName() {
		return _name;
	}
	
	public String getNodeRespone() {
		return _response;
	}
	
	public String getNodeMessage() {
		return _message;
	}
	
	public Date getStartTime() {
		return _startTime;
	}
	
	public void setNodeResponse(String response) {
		_response = response;
	}
	
	public void setNodeMessage(String message) {
		_message = message;
	}
	
	public void setNodeStartTime(Date startTime) {
		if(startTime != null)
			_startTime = startTime;
	}
	
	public String toString() {
		return _name;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof RBTNode) {
			RBTNode otherNode = (RBTNode)obj;
			return _name.equals(otherNode.getNodeName()) && (hashCode() == otherNode.hashCode());
		}
		return false;
	}
	
	public int hashCode() {
		return _name.hashCode();
	}
	
	public static RBTNode getDummyNode() {
		return new RBTNode("dummy");
	}
}