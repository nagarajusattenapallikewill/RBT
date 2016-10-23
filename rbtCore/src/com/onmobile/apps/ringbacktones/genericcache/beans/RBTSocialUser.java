package com.onmobile.apps.ringbacktones.genericcache.beans;

import java.sql.Timestamp;
import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.dao.DomainObject;

public class RBTSocialUser extends DomainObject {

	private Long id = null;

	private String userId;

	private int socialType;

	private String msisdn;

	private Timestamp startTime;

	private Timestamp endTime;

	private String RBTType;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getSocialType() {
		return socialType;
	}

	public void setSocialType(int socialType) {
		this.socialType = socialType;
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

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;

	}

	public static RBTSocialUser getRBTSocialUser(String userId, int socialType) {
		String sql = "select * from rbt_social_user where USER_ID= '" + userId+"' "
				+ "  and SOCIAL_TYPE=" + socialType;

		RBTSocialUser user = loadSingle(RBTSocialUser.class, sql);
		return user;

	}

	public static List<RBTSocialUser> getRBTSocialUser(String msisdn) {
		String sql = "select * from rbt_social_user where MSISDN= '" + msisdn + "' ";

		List<RBTSocialUser> userlist = load(RBTSocialUser.class, sql);
		return userlist;

	}

	public void saveRBTSocialUser(RBTSocialUser user) {
		user.insert();
	}

	public void deleteRBTSocialUser(RBTSocialUser user) {
		user.delete();
	}

	public RBTSocialUser(Long id, String userId, int socialType, String msisdn,
			Timestamp startTime, Timestamp endTime, String type) {

		this.id = id;
		this.userId = userId;
		this.socialType = socialType;
		this.msisdn = msisdn;
		this.startTime = startTime;
		this.endTime = endTime;
		RBTType = type;
	}

	public RBTSocialUser(String userId, int socialType, String msisdn,
			String type) {

		this.userId = userId;
		this.socialType = socialType;
		this.msisdn = msisdn;
		RBTType = type;
	}

	public RBTSocialUser() {

	}

}
