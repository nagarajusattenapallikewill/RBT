package com.onmobile.apps.ringbacktones.rbt2.service;

public interface IOperatorUserDetailsCallbackService {

	public void setOperatorUserInfo(String subscriberId, String serviceKey, String status, String operatorName, String circleID ) throws Throwable;

	public void removeOperatorUserInfo(String subscriberId) throws Throwable;
}
