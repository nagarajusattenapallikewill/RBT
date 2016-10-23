package com.onmobile.apps.ringbacktones.rbt2.bean;

import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;



public class ExtendedSubStatus implements SubscriberStatus{
	
	private String m_subscriberID;
	private String m_callerID;
	private int m_categoryID = -1;
	private String m_subscriberWavFile;
	private Date m_setTime;
	private Date m_startTime;
	private Date m_endTime;
	private int m_status;
	private String m_classType;
	private String m_selectedBy;
	private String m_selectionInfo;
	private Date m_nextChargingDate;
	private String m_prepaid;
	private int m_fromTime;
	private int m_toTime;
	private String m_sel_status;
	private String m_deselected_by;
	private String m_old_class_type;
	private int m_category_type;
	private char m_loopStatus;
	private int m_sel_type;
	private String m_sel_interval;
	private String m_refID;
	private String m_extraInfo;
	private String m_circleId;
	private String retryCount;
	private Date nextRetryTime;
	private Date requestTime;
	private String udpId = null;
	private String type = null;
	
	@Override
	public String subID()
	{
		return m_subscriberID;
	}
	
	public void setSubId(String subId) {
		this.m_subscriberID = subId;
	}
	@Override
	public String callerID()
	{
		return m_callerID;
	}
	
	public void setCallerId(String callerId) {
		this.m_callerID = callerId;
	}
	
	@Override
	public int selType() 
    { 
            return m_sel_type; 
    } 
	
	public void setSelType(int selType) {
		this.m_sel_type = selType;
	}
	
	@Override
	public int categoryID()
	{
		return m_categoryID;
	}

	public void setCategoryID(int catId)
	{
		this.m_categoryID = catId;
	}
	
	@Override
	public String subscriberFile()
	{
		return m_subscriberWavFile;
	}
	

	@Override
	public Date setTime()
	{
		return m_setTime;
	}
	
	public void setTym(Date setTime) {
		this.m_setTime = setTime;
	}

	@Override
	public Date startTime()
	{
		return m_startTime;
	}

	public void setStartTime(Date startTime) {
		this.m_startTime = startTime;
	}
	
	@Override
	public Date endTime()
	{
		return m_endTime;
	}
	
	public void setEndTime(Date endTime) {
		this.m_endTime = endTime;
	}

	@Override
	public int status()
	{
		return m_status;
	}
	
	public void setStatus(int status) {
		this.m_status = status;
	}

	@Override
	public String classType()
	{
		return m_classType;
	}

	public void setClassType(String classType) {
		this.m_classType = classType;
	}
	
	@Override
	public String selectedBy()
	{
		return m_selectedBy;
	}

	public void setSelectedBy (String selectedBy) {
		this.m_selectedBy = selectedBy;
	}
	
	@Override
	public String selectionInfo()
	{
		return m_selectionInfo;
	}
	
	public void setSelectionInfo(String selectionInfo) {
		this.m_selectionInfo = selectionInfo;
	}
	
	@Override
	public String selInterval()
	{
		return m_sel_interval;
	}

	public void setSelInterval (String selInterval) {
		this.m_sel_interval = selInterval;
	}
	
	@Override
	public String refID()
	{
		return m_refID;
	}
	
	public void setRefId(String refId) {
		this.m_refID = refId;
	}
	
	@Override
	public String extraInfo()
	{
		return m_extraInfo;
	}

	public void setExtraInfo(String extraInfo) {
		this.m_extraInfo = extraInfo;
	}
	
	@Override
	public Date nextChargingDate()
	{
		return m_nextChargingDate;
	}

	
	@Override
	public boolean prepaidYes()
	{
		if(m_prepaid!= null)
			return m_prepaid.equalsIgnoreCase("y");
		else
			//logger.info("RBT:: prepaid column is null" +m_subscriberID);

		return false;
	}
	
	public void setPrepaidYes(String prepaidYes) {
		this.m_prepaid = prepaidYes;
	}

	@Override
	public int fromTime()
	{
		return m_fromTime;
	}

	public void setFromTime(int fromTime) {
		this.m_fromTime = fromTime;
	}
	
	@Override
	public int toTime()
	{
		return m_toTime;
	}

	public void setToTime(int toTime) {
		this.m_toTime = toTime;
	}
	
	@Override
	public String selStatus()
	{
		return m_sel_status;
	}

	public void setSelStatus(String selStatus) {
		this.m_sel_status = selStatus;
	}
	
	@Override
	public String deSelectedBy()
	{
		return m_deselected_by;
	}

	public void setDeselectedBy(String deselectedBy) {
		this.m_deselected_by = deselectedBy;
	}
	
	@Override
	public String oldClassType()
	{
		return m_old_class_type;
	}

	public void setOldClassType (String oldClassType) {
		this.m_old_class_type = oldClassType;
	}
	
	@Override
	public int categoryType()
	{
		return m_category_type;
	}
	
	public void setCategoryType (int categoryType) {
		this.m_category_type = categoryType;
	}
	
	@Override
	public char loopStatus()
	{
		return m_loopStatus;
	}

	public void setLoopStatus(char loopStatus) {
		this.m_loopStatus = loopStatus;
	}
	

	@Override
	public String circleId()
	{
		return m_circleId;
	}
	
	public void setCircleId (String circleId) {
		this.m_circleId = circleId;
	}
	
	@Override
	public void setSubscriberFile(String subscriberWavFile)
	{
		m_subscriberWavFile = subscriberWavFile;
	}

	@Override
	public void setNextChargingDate(Date date)
	{
		m_nextChargingDate = date;
	}

	@Override
	public void setPrepaidYes(boolean prepaid)
	{
		m_prepaid = "n";
		if(prepaid)
			m_prepaid = "y";
	}
	
	
	@Override
	public String retryCount()
	{
		return retryCount;
	}

	@Override
	public Date nextRetryTime()
	{
		return nextRetryTime;
	}

	@Override
	public String date(Date date)
	{
		DateFormat sqlTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	
		return sqlTimeFormat.format(date);
	}

	@Override
	public Date getRequestTime() {
		return requestTime;
	}

	@Override
	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}

	@Override
	public void update(Connection conn) {
		
		
	}

	@Override
	public String udpId() {
		// TODO Auto-generated method stub
		return udpId;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}
}
