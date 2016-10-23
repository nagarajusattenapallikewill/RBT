package com.onmobile.apps.ringbacktones.v2.dto;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class SelRequestDTO {
	
	private PlayRule playRule;
	private Subscriber subscriber;
	private String mode;
	private String callerId;	
	private String subscriberId;	
	private Subscription subscription;
	private Asset asset;
	private String firstName;
	private String lastName;
	private String rbtFile;
	
	public SelRequestDTO(PlayRule playRule, Subscriber subscriber, String mode,
			String callerId, String subscriberId) {
		
		this.playRule = playRule;
		this.subscriber = subscriber;
		this.mode = mode;
		this.callerId = callerId;
		this.subscriberId = subscriberId;
	}
	
	public SelRequestDTO(PlayRule playRule, Subscriber subscriber, String mode,
			String callerId, String subscriberId,String firstName,String lastName) {
		
		this.playRule = playRule;
		this.subscriber = subscriber;
		this.mode = mode;
		this.callerId = callerId;
		this.subscriberId = subscriberId;
		this.firstName = firstName;
		this.lastName = lastName;
	}
	
	public SelRequestDTO(PlayRule playRule, Subscriber subscriber, String mode,
			String callerId, String subscriberId,String firstName,String lastName,String rbtFile) {
		
		this.playRule = playRule;
		this.subscriber = subscriber;
		this.mode = mode;
		this.callerId = callerId;
		this.subscriberId = subscriberId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.rbtFile = rbtFile;
	}
	
	public PlayRule getPlayRule() {
		return playRule;
	}
	public void setPlayRule(PlayRule playRule) {
		this.playRule = playRule;
	}
	public Subscriber getSubscriber() {
		return subscriber;
	}
	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getCallerId() {
		return callerId;
	}
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}


	public String getSubscriberId() {
		return subscriberId;
	}


	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}


	public Subscription getSubscription() {
		return subscription;
	}


	public void setSubscription(Subscription subscription) {
		this.subscription = subscription;
	}


	public Asset getAsset() {
		return asset;
	}


	public void setAsset(Asset asset) {
		this.asset = asset;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getRbtFile() {
		return rbtFile;
	}

	public void setRbtFile(String rbtFile) {
		this.rbtFile = rbtFile;
	}
}
