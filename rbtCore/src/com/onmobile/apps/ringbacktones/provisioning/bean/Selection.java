package com.onmobile.apps.ringbacktones.provisioning.bean;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "rbt_subscriber_selections")
public class Selection implements Serializable {

	private static final long serialVersionUID = 4413996161461149329L;

	@EmbeddedId
	private CompositeKey id;

	public CompositeKey getId() {
		return id;
	}

	public void setId(CompositeKey id) {
		this.id = id;
	}

	@Column(name = "CALLER_ID")
	private String caller_Id;

	public String getCaller_Id() {
		return caller_Id;
	}

	public void setCaller_Id(String caller_Id) {
		this.caller_Id = caller_Id;
	}

	@Embeddable
	public static class CompositeKey implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7025153613462249527L;

		public CompositeKey() {
			// TODO Auto-generated constructor stub
		}

		@Column(name = "internal_ref_id")
		private String internal_Ref_Id;

		@Column(name = "SUBSCRIBER_ID")
		private String subscriber_Id;

		@Column(name = "SUBSCRIBER_WAV_FILE")
		private String subscriber_Wav_File;

		public String getSubscriber_Wav_File() {
			return subscriber_Wav_File;
		}

		public void setSubscriber_Wav_File(String subscriber_Wav_File) {
			this.subscriber_Wav_File = subscriber_Wav_File;
		}

		public String getSubscriber_Id() {
			return subscriber_Id;
		}

		public void setSubscriber_Id(String subscriber_Id) {
			this.subscriber_Id = subscriber_Id;
		}

		public String getInternal_Ref_Id() {
			return internal_Ref_Id;
		}

		public void setInternal_Ref_Id(String internal_Ref_Id) {
			this.internal_Ref_Id = internal_Ref_Id;
		}

		@Override
		public String toString() {
			return "CompositeKey [internal_Ref_Id=" + internal_Ref_Id
					+ ", subscriber_Id=" + subscriber_Id
					+ ", subscriber_Wav_File=" + subscriber_Wav_File + "]";
		}

	}

	@Column(name = "SEL_STATUS")
	private String sel_Status;

	public String getSel_Status() {
		return sel_Status;
	}

	public void setSel_Status(String sel_Status) {
		this.sel_Status = sel_Status;
	}

	@Column(name = "SET_TIME", nullable = false)
	private Timestamp set_Time;

	@Column(name = "END_TIME", nullable = false)
	private Timestamp end_Time;

	public Timestamp getSet_Time() {
		return set_Time;
	}

	public void setSet_Time(Timestamp set_Time) {
		this.set_Time = set_Time;
	}

	public Timestamp getEnd_Time() {
		return end_Time;
	}

	public void setEnd_Time(Timestamp end_Time) {
		this.end_Time = end_Time;
	}

	@Column(name = "NEXT_CHARGING_DATE")
	private Timestamp next_Chargin_Date;

	public Timestamp getNext_Chargin_Date() {
		return next_Chargin_Date;
	}

	public void setNext_Chargin_Date(Timestamp next_Chargin_Date) {
		this.next_Chargin_Date = next_Chargin_Date;
	}

	@Column(name = "CATEGORY_ID")
	private int category_Id;

	@Column(name = "START_TIME", nullable = false)
	private Timestamp start_Time;

	@Column(name = "STATUS")
	private int status;

	@Column(name = "CLASS_TYPE")
	private String class_Type;

	@Column(name = "SELECTED_BY")
	private String selected_By;

	@Column(name = "SELECTION_INFO")
	private String selection_info;

	@Column(name = "PREPAID_YES")
	private String prepaid_Yes;

	@Column(name = "FROM_TIME")
	private int from_Time;

	@Column(name = "TO_TIME")
	private int to_Time;

	@Column(name = "DESELECTED_BY")
	private String deselected_By;

	@Column(name = "OLD_CLASS_TYPE")
	private String old_Class_Type;

	@Column(name = "CATEGORY_TYPE")
	private int category_Type;

	@Column(name = "LOOP_STATUS")
	private String loop_Status;

	@Column(name = "SEL_INTERVAL")
	private String sel_Interval;

	@Column(name = "SEL_TYPE")
	private int sel_Type;

	@Column(name = "CIRCLE_ID", nullable = false)
	private String circle_Id;

	@Column(name = "RETRY_COUNT")
	private String retry_Count;

	@Column(name = "EXTRA_INFO")
	private String extra_Info;

	@Column(name = "NEXT_RETRY_TIME")
	private Timestamp next_Retry_Time;

	public int getCategory_Id() {
		return category_Id;
	}

	public void setCategory_Id(int category_Id) {
		this.category_Id = category_Id;
	}

	public Timestamp getStart_Time() {
		return start_Time;
	}

	public void setStart_Time(Timestamp start_Time) {
		this.start_Time = start_Time;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getClass_Type() {
		return class_Type;
	}

	public void setClass_Type(String class_Type) {
		this.class_Type = class_Type;
	}

	public String getSelected_By() {
		return selected_By;
	}

	public void setSelected_By(String selected_By) {
		this.selected_By = selected_By;
	}

	public String getSelection_info() {
		return selection_info;
	}

	public void setSelection_info(String selection_info) {
		this.selection_info = selection_info;
	}

	public String getPrepaid_Yes() {
		return prepaid_Yes;
	}

	public void setPrepaid_Yes(String prepaid_Yes) {
		this.prepaid_Yes = prepaid_Yes;
	}

	public int getFrom_Time() {
		return from_Time;
	}

	public void setFrom_Time(int from_Time) {
		this.from_Time = from_Time;
	}

	public int getTo_Time() {
		return to_Time;
	}

	public void setTo_Time(int to_Time) {
		this.to_Time = to_Time;
	}

	public String getDeselected_By() {
		return deselected_By;
	}

	public void setDeselected_By(String deselected_By) {
		this.deselected_By = deselected_By;
	}

	public String getOld_Class_Type() {
		return old_Class_Type;
	}

	public void setOld_Class_Type(String old_Class_Type) {
		this.old_Class_Type = old_Class_Type;
	}

	public int getCategory_Type() {
		return category_Type;
	}

	public void setCategory_Type(int category_Type) {
		this.category_Type = category_Type;
	}

	public String getLoop_Status() {
		return loop_Status;
	}

	public void setLoop_Status(String loop_Status) {
		this.loop_Status = loop_Status;
	}

	public String getSel_Interval() {
		return sel_Interval;
	}

	public void setSel_Interval(String sel_Interval) {
		this.sel_Interval = sel_Interval;
	}

	public int getSel_Type() {
		return sel_Type;
	}

	public void setSel_Type(int sel_Type) {
		this.sel_Type = sel_Type;
	}

	public String getCircle_Id() {
		return circle_Id;
	}

	public void setCircle_Id(String circle_Id) {
		this.circle_Id = circle_Id;
	}

	public String getRetry_Count() {
		return retry_Count;
	}

	public void setRetry_Count(String retry_Count) {
		this.retry_Count = retry_Count;
	}

	public String getExtra_Info() {
		return extra_Info;
	}

	public void setExtra_Info(String extra_Info) {
		this.extra_Info = extra_Info;
	}

	public Timestamp getNext_Retry_Time() {
		return next_Retry_Time;
	}

	public void setNext_Retry_Time(Timestamp next_Retry_Time) {
		this.next_Retry_Time = next_Retry_Time;
	}

	@Override
	public String toString() {
		return id.subscriber_Id + "," + caller_Id + "," + category_Id + ","
				+ id.subscriber_Wav_File + "," + set_Time + "," + start_Time
				+ "," + end_Time + "," + status + "," + class_Type + ","
				+ selected_By + "," + selection_info + "," + next_Chargin_Date
				+ "," + prepaid_Yes + "," + from_Time + "," + to_Time + ","
				+ sel_Status + "," + deselected_By + "," + old_Class_Type + ","
				+ category_Type + "," + loop_Status + "," + sel_Interval + ","
				+ id.internal_Ref_Id + "," + extra_Info + "," + sel_Type + ","
				+ circle_Id + "," + retry_Count + "," + next_Retry_Time;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Selection other = (Selection) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
