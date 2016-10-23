package com.onmobile.apps.ringbacktones.rbt2.service;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IUserDetailsService {

	public Object getUserDetails(String msisdn) throws UserException;
	public Object putUserDetails(String msisdn, String serviceKey, String status, String operatorName, String circleID) throws UserException;
	public Object updateUserDetails(String msisdn, String serviceKey, String status, String operatorName, String circleID) throws UserException;
	public Object removeUserDetails(String msisdn) throws UserException;

}
