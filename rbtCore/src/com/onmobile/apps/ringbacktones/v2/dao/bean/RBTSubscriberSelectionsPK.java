package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Embeddable
public class RBTSubscriberSelectionsPK implements Serializable {

	private static final long serialVersionUID = 1L;

	public RBTSubscriberSelectionsPK() {
	}

	public RBTSubscriberSelectionsPK(String subscriber_id, String internal_ref_id) {
		this.subscriber_id = subscriber_id;
		this.internal_ref_id = internal_ref_id;
	}

	private String subscriber_id;
	private String internal_ref_id;

	public String getSubscriber_id() {
		return subscriber_id;
	}

	public void setSubscriber_id(String subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	public String getInternal_ref_id() {
		return internal_ref_id;
	}

	public void setInternal_ref_id(String internal_ref_id) {
		this.internal_ref_id = internal_ref_id;
	}

}
