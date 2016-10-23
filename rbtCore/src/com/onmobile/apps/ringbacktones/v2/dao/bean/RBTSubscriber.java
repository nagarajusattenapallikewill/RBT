package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="rbt_subscriber")
public class RBTSubscriber implements Serializable{

	private static final long serialVersionUID = 1L;

	public RBTSubscriber() {
	}
	
	@Id
	private String subscriber_id;
	private String activated_by;
	private String deactivated_by;
	private Date start_date;
	private Date end_date;
	private char prepaid_yes;
	private Date last_access_date;
	private Date next_charging_date;
	private int num_voice_access;
	private String activation_info;
	private String subscription_class;
	private String subscription_yes;
	private String last_deactivation_info;
	private Date last_deactivation_date;
	private Date activation_date;
	private String old_class_type;
	private int num_max_selections;
	private String cos_id;
	private String activated_cos_id;
	private int rbt_type;
	private char player_status;
	private String language;
	private int age;
	private String gender;
	private String extra_info;
	private String circle_id;
	private String internal_ref_id;
	private String retry_count;
	private Date next_retry_time;
	private Date next_billing_date;
	


	public String getSubscriber_id() {
		return subscriber_id;
	}
	public void setSubscriber_id(String subscriber_id) {
		this.subscriber_id = subscriber_id;
	}
	public String getActivated_by() {
		return activated_by;
	}
	public void setActivated_by(String activated_by) {
		this.activated_by = activated_by;
	}
	public String getDeactivated_by() {
		return deactivated_by;
	}
	public void setDeactivated_by(String deactivated_by) {
		this.deactivated_by = deactivated_by;
	}
	public Date getStart_date() {
		return start_date;
	}
	public void setStart_date(Date start_date) {
		this.start_date = start_date;
	}
	public Date getEnd_date() {
		return end_date;
	}
	public void setEnd_date(Date end_date) {
		this.end_date = end_date;
	}
	public char getPrepaid_yes() {
		return prepaid_yes;
	}
	public void setPrepaid_yes(char prepaid_yes) {
		this.prepaid_yes = prepaid_yes;
	}
	public Date getLast_access_date() {
		return last_access_date;
	}
	public void setLast_access_date(Date last_access_date) {
		this.last_access_date = last_access_date;
	}
	public Date getNext_charging_date() {
		return next_charging_date;
	}
	public void setNext_charging_date(Date next_charging_date) {
		this.next_charging_date = next_charging_date;
	}
	public int getNum_voice_access() {
		return num_voice_access;
	}
	public void setNum_voice_access(int num_voice_access) {
		this.num_voice_access = num_voice_access;
	}
	public String getActivation_info() {
		return activation_info;
	}
	public void setActivation_info(String activation_info) {
		this.activation_info = activation_info;
	}
	public String getSubscription_class() {
		return subscription_class;
	}
	public void setSubscription_class(String subscription_class) {
		this.subscription_class = subscription_class;
	}
	public String getSubscription_yes() {
		return subscription_yes;
	}
	public void setSubscription_yes(String subscription_yes) {
		this.subscription_yes = subscription_yes;
	}
	public String getLast_deactivation_info() {
		return last_deactivation_info;
	}
	public void setLast_deactivation_info(String last_deactivation_info) {
		this.last_deactivation_info = last_deactivation_info;
	}
	public Date getLast_deactivation_date() {
		return last_deactivation_date;
	}
	public void setLast_deactivation_date(Date last_deactivation_date) {
		this.last_deactivation_date = last_deactivation_date;
	}
	public Date getActivation_date() {
		return activation_date;
	}
	public void setActivation_date(Date activation_date) {
		this.activation_date = activation_date;
	}
	public String getOld_class_type() {
		return old_class_type;
	}
	public void setOld_class_type(String old_class_type) {
		this.old_class_type = old_class_type;
	}
	public int getNum_max_selections() {
		return num_max_selections;
	}
	public void setNum_max_selections(int num_max_selections) {
		this.num_max_selections = num_max_selections;
	}
	public String getCos_id() {
		return cos_id;
	}
	public void setCos_id(String cos_id) {
		this.cos_id = cos_id;
	}
	public String getActivated_cos_id() {
		return activated_cos_id;
	}
	public void setActivated_cos_id(String activated_cos_id) {
		this.activated_cos_id = activated_cos_id;
	}
	public int getRbt_type() {
		return rbt_type;
	}
	public void setRbt_type(int rbt_type) {
		this.rbt_type = rbt_type;
	}
	public char getPlayer_status() {
		return player_status;
	}
	public void setPlayer_status(char player_status) {
		this.player_status = player_status;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getExtra_info() {
		return extra_info;
	}
	public void setExtra_info(String extra_info) {
		this.extra_info = extra_info;
	}
	public String getCircle_id() {
		return circle_id;
	}
	public void setCircle_id(String circle_id) {
		this.circle_id = circle_id;
	}
	public String getInternal_ref_id() {
		return internal_ref_id;
	}
	public void setInternal_ref_id(String internal_ref_id) {
		this.internal_ref_id = internal_ref_id;
	}
	public String getRetry_count() {
		return retry_count;
	}
	public void setRetry_count(String retry_count) {
		this.retry_count = retry_count;
	}
	public Date getNext_retry_time() {
		return next_retry_time;
	}
	public void setNext_retry_time(Date next_retry_time) {
		this.next_retry_time = next_retry_time;
	}
	public Date getNext_billing_date() {
		return next_billing_date;
	}
	public void setNext_billing_date(Date next_billing_date) {
		this.next_billing_date = next_billing_date;
	}
	
	public RBTSubscriber(String subscriber_id, String activated_by, String deactivated_by, Date start_date,
			Date end_date, char prepaid_yes, Date last_access_date, Date next_charging_date, int num_voice_access,
			String activation_info, String subscription_class, String subscription_yes, String last_deactivation_info,
			Date last_deactivation_date, Date activation_date, String old_class_type, int num_max_selections,
			String cos_id, String activated_cos_id, int rbt_type, char player_status, String language, int age,
			String gender, String extra_info, String circle_id, String internal_ref_id, String retry_count,
			Date next_retry_time, Date next_billing_date) {
		this.subscriber_id = subscriber_id;
		this.activated_by = activated_by;
		this.deactivated_by = deactivated_by;
		this.start_date = start_date;
		this.end_date = end_date;
		this.prepaid_yes = prepaid_yes;
		this.last_access_date = last_access_date;
		this.next_charging_date = next_charging_date;
		this.num_voice_access = num_voice_access;
		this.activation_info = activation_info;
		this.subscription_class = subscription_class;
		this.subscription_yes = subscription_yes;
		this.last_deactivation_info = last_deactivation_info;
		this.last_deactivation_date = last_deactivation_date;
		this.activation_date = activation_date;
		this.old_class_type = old_class_type;
		this.num_max_selections = num_max_selections;
		this.cos_id = cos_id;
		this.activated_cos_id = activated_cos_id;
		this.rbt_type = rbt_type;
		this.player_status = player_status;
		this.language = language;
		this.age = age;
		this.gender = gender;
		this.extra_info = extra_info;
		this.circle_id = circle_id;
		this.internal_ref_id = internal_ref_id;
		this.retry_count = retry_count;
		this.next_retry_time = next_retry_time;
		this.next_billing_date = next_billing_date;
	}
	

}
