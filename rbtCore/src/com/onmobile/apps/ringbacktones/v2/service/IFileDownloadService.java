package com.onmobile.apps.ringbacktones.v2.service;

import org.springframework.web.multipart.MultipartFile;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IFileDownloadService {

	public Object downloadFile(long subscriberId, String mode, MultipartFile file, String fileName) throws UserException;
	
}
