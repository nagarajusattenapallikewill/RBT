package com.onmobile.apps.ringbacktones.v2.bean;

import java.util.Date;

public class AssetBean {

	private String toneName;
	private int categoryId;
	private int id;
	private String refrenceId;
	private String serviceKey;
	private String udpId;
	private String udpName;
	private String cutStartDuration;
	private Date validDate;
	private String chargeClass;
	/*RBT - 16269
	To identify Asset Subtype*/
	private int status;
	
	
	public AssetBean(int id, String refrenceId, String toneName, int categoryId, String serviceKey)  {
		
		this.id = id;
		this.refrenceId = refrenceId;
		this.toneName = toneName;
		this.categoryId = categoryId;
		this.serviceKey = serviceKey;
	}
	
    public AssetBean(int id, String refrenceId, String toneName, int categoryId, String serviceKey, String cutStartDuration)  {
		this.id = id;
		this.refrenceId = refrenceId;
		this.toneName = toneName;
		this.categoryId = categoryId;
		this.serviceKey = serviceKey;
		this.cutStartDuration = cutStartDuration;
	}

    public AssetBean(int id, String refrenceId, String toneName, int categoryId, String serviceKey,Date validDate,String chargeClass)  {
		
		this.id = id;
		this.refrenceId = refrenceId;
		this.toneName = toneName;
		this.categoryId = categoryId;
		this.serviceKey = serviceKey;
		this.validDate = validDate;
		this.chargeClass = chargeClass;
	}
/*	
	public AssetBean(int id, String refrenceId, String toneName, int categoryId, String serviceKey, String udpId, String udpName)  {
		
		this.id = id;
		this.refrenceId = refrenceId;
		this.toneName = toneName;
		this.categoryId = categoryId;
		this.serviceKey = serviceKey;
		this.udpId = udpId;
		this.udpName = udpName;
	}
	*/
	public AssetBean(int id, String refrenceId, String toneName, int categoryId, String serviceKey, String udpId, String udpName,
			int status) {
		this.toneName = toneName;
		this.categoryId = categoryId;
		this.id = id;
		this.refrenceId = refrenceId;
		this.serviceKey = serviceKey;
		this.udpId = udpId;
		this.udpName = udpName;
		this.status = status;
	}
	
	public AssetBean(int id, String refrenceId, String toneName, int categoryId, String serviceKey, String udpId, String udpName,
			int status,String cutStartDuration ) {
		this.toneName = toneName;
		this.categoryId = categoryId;
		this.id = id;
		this.refrenceId = refrenceId;
		this.serviceKey = serviceKey;
		this.udpId = udpId;
		this.udpName = udpName;
		this.status = status;
		this.setCutStartDuration(cutStartDuration);
	}

	public String getToneName() {
		return toneName;
	}
	public void setToneName(String toneName) {
		this.toneName = toneName;
	}
	public int getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getRefrenceId() {
		return refrenceId;
	}
	public void setRefrenceId(String refrenceId) {
		this.refrenceId = refrenceId;
	}
	public String getServiceKey() {
		return serviceKey;
	}
	public void setServiceKey(String serviceKey) {
		this.serviceKey = serviceKey;
	}

	public String getUdpId() {
		return udpId;
	}

	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}

	public String getUdpName() {
		return udpName;
	}

	public void setUdpName(String udpName) {
		this.udpName = udpName;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


	public String getCutStartDuration() {
		return cutStartDuration;
	}

	public void setCutStartDuration(String cutStartDuration) {
		this.cutStartDuration = cutStartDuration;
	}


	public Date getValidDate() {
		return validDate;
	}

	public void setValidDate(Date validDate) {
		this.validDate = validDate;
	}

	public String getChargeClass() {
		return chargeClass;
	}

	public void setChargeClass(String chargeClass) {
		this.chargeClass = chargeClass;
	}
	
}
