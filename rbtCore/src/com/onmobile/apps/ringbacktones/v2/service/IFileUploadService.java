package com.onmobile.apps.ringbacktones.v2.service;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IFileUploadService {
	
	public static final String UGC_TRANSFER_THIRD_PARTY_URL = "UGC_TRANSFER_THIRD_PARTY_URL_";
	
	public Object uploadBinary(String fileName, Subscriber subscriber) throws UserException;
//Added for deleting cutrbt
	public void deleteBinary(String file, Subscriber subscriber);
}
