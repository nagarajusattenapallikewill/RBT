package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "rbt_provisioning_requests")
public class RBTProvisioningRequests implements Serializable {

	private static final long serialVersionUID = -4902671292909391562L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long request_id;
	private String subscriber_id;
	private int type;
	private int status;
	private String extra_info;
	private String mode;
	private String mode_info;
	private String trans_id;
	private String charging_class;
	private Date creation_time;
	private Date next_retry_time;
	private String retry_count;
	private int num_max_selections;
	private int sm_subscription_validity_status;

	public long getRequest_id() {
		return request_id;
	}

	public void setRequest_id(long request_id) {
		this.request_id = request_id;
	}

	public String getSubscriber_id() {
		return subscriber_id;
	}

	public void setSubscriber_id(String subscriber_id) {
		this.subscriber_id = subscriber_id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getExtra_info() {
		return extra_info;
	}

	public void setExtra_info(String extra_info) {
		this.extra_info = extra_info;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public String getMode_info() {
		return mode_info;
	}

	public void setMode_info(String mode_info) {
		this.mode_info = mode_info;
	}

	public String getTrans_id() {
		return trans_id;
	}

	public void setTrans_id(String trans_id) {
		this.trans_id = trans_id;
	}

	public String getCharging_class() {
		return charging_class;
	}

	public void setCharging_class(String charging_class) {
		this.charging_class = charging_class;
	}

	public Date getCreation_time() {
		return creation_time;
	}

	public void setCreation_time(Date creation_time) {
		this.creation_time = creation_time;
	}

	public Date getNext_retry_time() {
		return next_retry_time;
	}

	public void setNext_retry_time(Date next_retry_time) {
		this.next_retry_time = next_retry_time;
	}

	public String getRetry_count() {
		return retry_count;
	}

	public void setRetry_count(String retry_count) {
		this.retry_count = retry_count;
	}

	public int getNum_max_selections() {
		return num_max_selections;
	}

	public void setNum_max_selections(int num_max_selections) {
		this.num_max_selections = num_max_selections;
	}

	public int getSm_subscription_validity_status() {
		return sm_subscription_validity_status;
	}

	public void setSm_subscription_validity_status(int sm_subscription_validity_status) {
		this.sm_subscription_validity_status = sm_subscription_validity_status;
	}

	public RBTProvisioningRequests(long request_id, String subscriber_id, int type, int status, String extra_info,
			String mode, String mode_info, String trans_id, String charging_class, Date creation_time,
			Date next_retry_time, String retry_count, int num_max_selections, int sm_subscription_validity_status) {
		super();
		this.request_id = request_id;
		this.subscriber_id = subscriber_id;
		this.type = type;
		this.status = status;
		this.extra_info = extra_info;
		this.mode = mode;
		this.mode_info = mode_info;
		this.trans_id = trans_id;
		this.charging_class = charging_class;
		this.creation_time = creation_time;
		this.next_retry_time = next_retry_time;
		this.retry_count = retry_count;
		this.num_max_selections = num_max_selections;
		this.sm_subscription_validity_status = sm_subscription_validity_status;
	}

	
}
