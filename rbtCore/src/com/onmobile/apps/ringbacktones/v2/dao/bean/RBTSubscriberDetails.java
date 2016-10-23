package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.util.List;

public class RBTSubscriberDetails {

	public RBTSubscriberDetails() {
	}

	public RBTSubscriberDetails(RBTSubscriber rbtSubscriber, List<RBTSubscriberSelection> rbtSubscriberSelections) {
		super();
		this.rbtSubscriber = rbtSubscriber;
		this.rbtSubscriberSelections = rbtSubscriberSelections;
	}
	
	public RBTSubscriberDetails(RBTSubscriber rbtSubscriber, List<RBTSubscriberSelection> rbtSubscriberSelections, List<RBTProvisioningRequests> rbtProvisioningRequests) {
		super();
		this.rbtSubscriber = rbtSubscriber;
		this.rbtSubscriberSelections = rbtSubscriberSelections;
		this.rbtProvisioningRequests = rbtProvisioningRequests;
	}

	private RBTSubscriber rbtSubscriber;
	private List<RBTSubscriberSelection> rbtSubscriberSelections;
	private  List<RBTProvisioningRequests> rbtProvisioningRequests;
	
	public RBTSubscriber getRbtSubscriber() {
		return rbtSubscriber;
	}

	public void setRbtSubscriber(RBTSubscriber rbtSubscriber) {
		this.rbtSubscriber = rbtSubscriber;
	}

	public List<RBTSubscriberSelection> getRbtSubscriberSelections() {
		return rbtSubscriberSelections;
	}

	public void setRbtSubscriberSelections(List<RBTSubscriberSelection> rbtSubscriberSelections) {
		this.rbtSubscriberSelections = rbtSubscriberSelections;
	}

	public List<RBTProvisioningRequests> getRbtProvisioningRequests() {
		return rbtProvisioningRequests;
	}

	public void setRbtProvisioningRequests(List<RBTProvisioningRequests> rbtProvisioningRequests) {
		this.rbtProvisioningRequests = rbtProvisioningRequests;
	}
	
	

}
