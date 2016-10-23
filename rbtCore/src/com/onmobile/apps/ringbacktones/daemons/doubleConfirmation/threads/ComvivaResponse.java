package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="consent_resp") 
public class ComvivaResponse {
	private String trx_id;
	private String cg_id;
	private String msisdn;
	private String error_code;
	private String error_desc;
	private boolean is_synchronised;
	private String consnt_stat;
	private Date consnt_time;
	private String opt1;
	private String opt2;
	private String opt3;
	
	public String getTrx_id() {
		return trx_id;
	}
	
	@XmlElement
	public void setTrx_id(String trx_id) {
		this.trx_id = trx_id;
	}
	
	public String getCg_id() {
		return cg_id;
	}
	
	@XmlElement
	public void setCg_id(String cg_id) {
		this.cg_id = cg_id;
	}
	
	public String getMsisdn() {
		return msisdn;
	}
	
	@XmlElement
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}
	
	public String getError_code() {
		return error_code;
	}
	
	@XmlElement
	public void setError_code(String error_code) {
		this.error_code = error_code;
	}
	
	public String getError_desc() {
		return error_desc;
	}
	
	@XmlElement
	public void setError_desc(String error_desc) {
		this.error_desc = error_desc;
	}
	
	public boolean isIs_synchronised() {
		return is_synchronised;
	}
	
	@XmlElement
	public void setIs_synchronised(boolean is_synchronised) {
		this.is_synchronised = is_synchronised;
	}
	
	public String getConsnt_stat() {
		return consnt_stat;
	}
	
	@XmlElement
	public void setConsnt_stat(String consnt_stat) {
		this.consnt_stat = consnt_stat;
	}
	
	public Date getConsnt_time() {
		return consnt_time;
	}
	
	@XmlElement
	public void setConsnt_time(Date consnt_time) {
		this.consnt_time = consnt_time;
	}
	
	public String getOpt1() {
		return opt1;
	}
	
	@XmlElement
	public void setOpt1(String opt1) {
		this.opt1 = opt1;
	}
	
	public String getOpt2() {
		return opt2;
	}
	
	@XmlElement
	public void setOpt2(String opt2) {
		this.opt2 = opt2;
	}
	
	public String getOpt3() {
		return opt3;
	}
	
	@XmlElement
	public void setOpt3(String opt3) {
		this.opt3 = opt3;
	}
	
}
