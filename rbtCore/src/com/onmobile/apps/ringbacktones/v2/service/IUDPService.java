package com.onmobile.apps.ringbacktones.v2.service;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;



public interface IUDPService {
	
	public Object createUDP(String msisdn, String name,
			String mode,String extrainfo) throws UserException;
	
	public Object deleteUDP(String msisdn, String udpId) throws UserException;
	
	public Object updateUDP(String msisdn, String name,
			String mode,String extrainfo, String udpId) throws UserException;
	
	public Object getAllUDP(String msisdn, int offset, int pageSize) throws UserException;
	
	public Object addContentToUDP(String msisdn, String udpId, String toneId, String type) throws UserException;
	
	public Object deleteContentFromUDP(String msisdn, String udpId, String toneId,String ctype)throws UserException;
	
	public Object getContentsFromUDP(String msisdn, String udpId, int offset, int pageSize) throws UserException;
	
	
	
}
