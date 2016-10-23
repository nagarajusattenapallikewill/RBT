package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

import com.onmobile.apps.ringbacktones.common.ResourceReader;
import com.onmobile.apps.ringbacktones.content.database.RBTPrimitive;
import com.onmobile.apps.ringbacktones.genericcache.dao.DomainObject;

public class RBTSocialUpdate extends DomainObject {

	private static String m_databaseType = getDBSelectionString();
	
	private Long id = null;

	private String callerId;

	private long eventType;

	private int clipId = -1;

	private int catId = -1;

	private String msisdn;

	private Timestamp startTime;

	private Timestamp endTime;

	private String RBTType;

	private int status;
	public String toString(){
		String returnStr="null";
		if(this!=null){
			returnStr="id="+id+";status="+status+";callerId="+callerId+";eventType="+eventType+";clipId="+clipId+";catId="+catId+";msisdn="+msisdn+";startTime="+startTime+";endTime="+endTime+";RBTType="+RBTType;
		}
		return returnStr;
	}
	public String getCallerId() {
		return callerId;
	}

	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}

	public long getEventType() {
		return eventType;
	}

	public void setEventType(long eventType) {
		this.eventType = eventType;
	}

	public int getClipId() {
		return clipId;
	}

	public void setClipId(int clipId) {
		this.clipId = clipId;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getRBTType() {
		return RBTType;
	}

	public void setRBTType(String type) {
		RBTType = type;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;

	}
	public static String getDBSelectionString(){
		return ResourceReader.getString("rbt", "DB_TYPE", "MYSQL");
	}
	public static List<RBTSocialUpdate> getRBTSocialUpdate(String msisdn) {
		String sql = "select * from rbt_social_update where MSISDN='" + msisdn+"'";

		List<RBTSocialUpdate> updatelist = load(RBTSocialUpdate.class, sql);
		return updatelist;

	}

	public static List<RBTSocialUpdate> getRBTSocialUpdate(String msisdn,
			int status) {
		String sql = "select * from rbt_social_update where MSISDN='" + msisdn
				+ "' and STATUS=" + status;

		List<RBTSocialUpdate> updatelist = load(RBTSocialUpdate.class, sql);
		return updatelist;

	}

	public void saveRBTSocialUpdate(RBTSocialUpdate update) {
		update.insert();
	}

	public void deleteRBTSocialUpdate(RBTSocialUpdate update) {
		update.delete();
	}

	public RBTSocialUpdate(long id, String callerId, long eventType,
			int clipId, int catId, String msisdn, Timestamp startTime,
			Timestamp endTime, String type, int status) {

		this.id = id;
		this.callerId = callerId;
		this.eventType = eventType;
		this.clipId = clipId;
		this.catId = catId;
		this.msisdn = msisdn;
		this.startTime = startTime;
		this.endTime = endTime;
		RBTType = type;
		this.status = status;
	}

	public RBTSocialUpdate(String callerId, long eventType, int clipId,
			int catId, String msisdn, String type, int status) {

		this.callerId = callerId;
		this.eventType = eventType;
		this.clipId = clipId;
		this.catId = catId;
		this.msisdn = msisdn;
		RBTType = type;
		this.status = status;
	}

	public RBTSocialUpdate() {

	}

	public static RBTSocialUpdate getRBTSocialUpdate(long sequenceId,
			String msisdn) {
		// TODO Auto-generated method stub

		String sql = "select * from rbt_social_update where SEQUENCE_ID = "
				+ sequenceId + " and MSISDN = '" + msisdn+"'";
		RBTSocialUpdate update = loadSingle(RBTSocialUpdate.class, sql);
		return update;

	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public static String getUpdateQueryString(int status, long eventType,
			int fetchSize, long presentSequenceId) {
		StringBuffer result = new StringBuffer("select * from rbt_social_update where STATUS = "+status+" and SEQUENCE_ID>"+presentSequenceId);
		if(eventType != -1)
		{
			result.append(" and EVENT_TYPE="+eventType);
		}
        if(m_databaseType.equalsIgnoreCase(RBTPrimitive.DB_SAPDB))
        {
            if(fetchSize >= 0)
            {
                result.append(" and rownum < "+fetchSize);
            }
            result.append(" order by SEQUENCE_ID");
        }
        else
        {
            result.append(" order by SEQUENCE_ID");
            if(fetchSize >= 0)
            {
                result.append(" limit "+fetchSize);
            }
        }

		return result.toString();
	}

}
