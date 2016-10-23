package com.onmobile.apps.ringbacktones.v2.dao.bean;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rbt_subscriber_selections")
public class RBTSubscriberSelection implements Serializable {

	private static final long serialVersionUID = 1L;

	public RBTSubscriberSelection() {
	}

	@EmbeddedId
	private RBTSubscriberSelectionsPK RBTSubscriberSelectionsPK;
	private String caller_id;
	private int category_id;
	private String subscriber_wav_file;
	private Date set_time;
	private Date end_time;
	private int status;
	private String class_type;
	private String selected_by;
	private String selection_info;
	private Date next_charging_date;
	private char prepaid_yes;
	private int from_time;
	private int to_time;
	private char sel_status;
	private String deselected_by;
	private String old_class_type;
	private int category_type;
	private char loop_status;
	private String sel_interval;
	private String extra_info;
	private int sel_type;
	private String circle_id;
	private String retry_count;
	private Date next_retry_time;
	private Date start_time;
	
	public RBTSubscriberSelectionsPK getRBTSubscriberSelectionsPK() {
		return RBTSubscriberSelectionsPK;
	}

	public void setRBTSubscriberSelectionsPK(RBTSubscriberSelectionsPK rBTSubscriberSelectionsPK) {
		RBTSubscriberSelectionsPK = rBTSubscriberSelectionsPK;
	}

	public String getCaller_id() {
		return caller_id;
	}

	public void setCaller_id(String caller_id) {
		this.caller_id = caller_id;
	}

	public int getCategory_id() {
		return category_id;
	}

	public void setCategory_id(int category_id) {
		this.category_id = category_id;
	}

	public String getSubscriber_wav_file() {
		return subscriber_wav_file;
	}

	public void setSubscriber_wav_file(String subscriber_wav_file) {
		this.subscriber_wav_file = subscriber_wav_file;
	}

	public Date getSet_time() {
		return set_time;
	}

	public void setSet_time(Date set_time) {
		this.set_time = set_time;
	}

	public Date getEnd_time() {
		return end_time;
	}

	public void setEnd_time(Date end_time) {
		this.end_time = end_time;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getClass_type() {
		return class_type;
	}

	public void setClass_type(String class_type) {
		this.class_type = class_type;
	}

	public String getSelected_by() {
		return selected_by;
	}

	public void setSelected_by(String selected_by) {
		this.selected_by = selected_by;
	}

	public String getSelection_info() {
		return selection_info;
	}

	public void setSelection_info(String selection_info) {
		this.selection_info = selection_info;
	}

	public Date getNext_charging_date() {
		return next_charging_date;
	}

	public void setNext_charging_date(Date next_charging_date) {
		this.next_charging_date = next_charging_date;
	}

	public char getPrepaid_yes() {
		return prepaid_yes;
	}

	public void setPrepaid_yes(char prepaid_yes) {
		this.prepaid_yes = prepaid_yes;
	}

	public int getFrom_time() {
		return from_time;
	}

	public void setFrom_time(int from_time) {
		this.from_time = from_time;
	}

	public int getTo_time() {
		return to_time;
	}

	public void setTo_time(int to_time) {
		this.to_time = to_time;
	}

	public char getSel_status() {
		return sel_status;
	}

	public void setSel_status(char sel_status) {
		this.sel_status = sel_status;
	}

	public String getDeselected_by() {
		return deselected_by;
	}

	public void setDeselected_by(String deselected_by) {
		this.deselected_by = deselected_by;
	}

	public String getOld_class_type() {
		return old_class_type;
	}

	public void setOld_class_type(String old_class_type) {
		this.old_class_type = old_class_type;
	}

	public int getCategory_type() {
		return category_type;
	}

	public void setCategory_type(int category_type) {
		this.category_type = category_type;
	}

	public char getLoop_status() {
		return loop_status;
	}

	public void setLoop_status(char loop_status) {
		this.loop_status = loop_status;
	}

	public String getSel_interval() {
		return sel_interval;
	}

	public void setSel_interval(String sel_interval) {
		this.sel_interval = sel_interval;
	}

	public String getExtra_info() {
		return extra_info;
	}

	public void setExtra_info(String extra_info) {
		this.extra_info = extra_info;
	}

	public int getSel_type() {
		return sel_type;
	}

	public void setSel_type(int sel_type) {
		this.sel_type = sel_type;
	}

	public String getCircle_id() {
		return circle_id;
	}

	public void setCircle_id(String circle_id) {
		this.circle_id = circle_id;
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

	public Date getStart_time() {
		return start_time;
	}

	public void setStart_time(Date start_time) {
		this.start_time = start_time;
	}

	public RBTSubscriberSelection(String subscriber_id, String caller_id, int category_id, String subscriber_wav_file,
			Date set_time, Date end_time, int status, String class_type, String selected_by, String selection_info,
			Date next_charging_date, char prepaid_yes, int from_time, int to_time, char sel_status,
			String deselected_by, String old_class_type, int category_type, char loop_status, String sel_interval,
			String internal_ref_id, String extra_info, int sel_type, String circle_id, String retry_count,
			Date next_retry_time, Date start_time) {
		this.RBTSubscriberSelectionsPK = new RBTSubscriberSelectionsPK(subscriber_id, internal_ref_id);
		this.caller_id = caller_id;
		this.category_id = category_id;
		this.subscriber_wav_file = subscriber_wav_file;
		this.set_time = set_time;
		this.end_time = end_time;
		this.status = status;
		this.class_type = class_type;
		this.selected_by = selected_by;
		this.selection_info = selection_info;
		this.next_charging_date = next_charging_date;
		this.prepaid_yes = prepaid_yes;
		this.from_time = from_time;
		this.to_time = to_time;
		this.sel_status = sel_status;
		this.deselected_by = deselected_by;
		this.old_class_type = old_class_type;
		this.category_type = category_type;
		this.loop_status = loop_status;
		this.sel_interval = sel_interval;
		this.extra_info = extra_info;
		this.sel_type = sel_type;
		this.circle_id = circle_id;
		this.retry_count = retry_count;
		this.next_retry_time = next_retry_time;
		this.start_time = start_time;
	}

}
