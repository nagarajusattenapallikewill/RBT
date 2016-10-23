package com.onmobile.apps.ringbacktones.ussd.tatagsm;

public class USSDNode {

	public static final int MAX_NODES = 10;
	
	private int nodeId;
	
	private int parentNodeId;
	
	private String nodeText;
	
	private String nodeURL;

	public USSDNode() {
	}
	
	public USSDNode(int nodeId, int parentNodeId, String nodeText, String nodeURL) {
		this.nodeId = nodeId;
		this.parentNodeId = parentNodeId;
		this.nodeText = nodeText;
		this.nodeURL = nodeURL;
	}
	
	public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getParentNodeId() {
		return parentNodeId;
	}

	public void setParentNodeId(int parentNodeId) {
		this.parentNodeId = parentNodeId;
	}

	public String getNodeText() {
		return nodeText;
	}

	public void setNodeText(String nodeText) {
		this.nodeText = nodeText;
	}

	public String getNodeURL() {
		return nodeURL;
	}

	public void setNodeURL(String nodeURL) {
		this.nodeURL = nodeURL;
	}
}
