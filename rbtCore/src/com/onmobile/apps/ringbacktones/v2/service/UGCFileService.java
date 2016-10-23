package com.onmobile.apps.ringbacktones.v2.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.http.HttpHitUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.converter.GsonHttpMessageConverter;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;
import com.onmobile.apps.ringbacktones.v2.dto.GriffUgcResponseDto;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.common.URLBuilder;

/**
 * 
 *@author senthil.raja
 */
/*
 * UGCFileServie will have behavior of upload and download file.
 */
public class UGCFileService extends AbstractUGCFileService{
	
	Logger logger = Logger.getLogger(UGCFileService.class);
	
	private String filePath;
	
	/*
	 * Api to transfer binary to third party. Client can't access this api directly. Client can upload the binary through uploadBinary api 
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.v2.service.AbstractUGCFileService#transferFileToThirdParty(com.onmobile.apps.ringbacktones.v2.service.AbstractUGCFileService.StateClass)
	 * 
	 */
	@Override
	protected UgcFileUploadStatus transferFileToThirdParty(final StateClass stateObject) throws UserException {
		//Redirect Request
		
		Subscriber subscriber = stateObject.getSubscriber();
		String operatorName = ServiceUtil.getOperatorName(subscriber);
		String circleName = ServiceUtil.getCircleId(subscriber);
		
		operatorName = (operatorName != null ? operatorName.trim().toUpperCase() : operatorName);
		
		String configName = UGC_TRANSFER_THIRD_PARTY_URL + operatorName;
		
		String URL = ServiceUtil.getThirdPartyUrl(configName);
		
		//If url is not configured, then will throw the UserException with interal server error and delete the download file from the configured path. 
		if(URL == null) {
			logger.warn(configName + " is not configured in thirdParyUrl configuration. Please configured thrid party url");
			boolean isFileDeleted = stateObject.getUploadFile().delete();
			if(isFileDeleted) {
				logger.info("Deleting file because URL is not configured, " + stateObject.getUploadFile().getName() + " is deleted from " + stateObject.getUploadFile().getPath());
			}
			ServiceUtil.throwCustomUserException(getErrorCodeMapping(), Constants.INTERNAL_SERVER_ERROR, MessageResource.UGC_DOWNLOAD_SERVICE_ERROR);
		}
		
		URLBuilder urlBuilder = new URLBuilder(URL).replaceCircle(circleName).replaceFileName(stateObject.getUploadFile().getName());
		
		URL = urlBuilder.buildUrl();

		logger.info("System being make hit URL: " + URL );
		String serverResponse = null;
		
		//Make multi part api hit to third party
		try {
			serverResponse = HttpHitUtil.makePostMultiPartHit(URL, String.class, stateObject.getUploadFile());
		}
		catch(Exception e) {
			logger.error("Exception while transfer binary to third party server: ", e);
		}
		
		//Convert third party string response to json, if hit is successful. 
		GriffUgcResponseDto griffUgcResponseDto = null;
		try {
			if(serverResponse != null) {
				griffUgcResponseDto = GsonHttpMessageConverter.getRead_gsonBuilder().create().fromJson(serverResponse, GriffUgcResponseDto.class);
			}
		}
		catch(Exception e) {
			logger.error("Exception converting from response to json object", e);
		}
		logger.info("ServerResponse::: " + griffUgcResponseDto);
		
		String response = griffUgcResponseDto != null ?griffUgcResponseDto.getStatus() : "error";
		
		UgcFileUploadStatus ugcFileUploadStatus = UgcFileUploadStatus.TO_BE_PROCESS_STATE;
		
		//If server response is success, then delete the download file from the configured path and set the upload status as success.
		if("success".equalsIgnoreCase(response)) {
			ugcFileUploadStatus = UgcFileUploadStatus.SUCCESS_STATE;
			boolean isFileDeleted = stateObject.getUploadFile().delete();
			
			if(isFileDeleted) {
				logger.info(stateObject.getUploadFile().getName() + " is deleted from " + stateObject.getUploadFile().getPath());
			}			
		} else {
			logger.warn("Third party server error at the time of upload this file " + stateObject.getUploadFile().getAbsolutePath() + ", please check third party system, third party error: " +  griffUgcResponseDto);
		}
		
		return ugcFileUploadStatus;
	}

	/*
	 * Api to download file. File path is confiugred in the bean xml.
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.v2.service.AbstractUGCFileService#downloadUGCFile(org.springframework.web.multipart.MultipartFile, java.lang.String)
	 */
	@Override
	public StateClass downloadUGCFile(MultipartFile file, String fileName)
			throws UserException {
		logger.info("Received call fileName: " + fileName);
		if (file != null &&  !file.isEmpty()) {
			BufferedOutputStream stream = null;
			try {
				File newFile = new File(filePath + File.separator + fileName);
				byte[] bytes = file.getBytes();
				stream = new BufferedOutputStream(
						new FileOutputStream(newFile));
				stream.write(bytes);
				stream.close();
				logger.info("Successfully file stored filePath: " + newFile.getAbsolutePath());
				
				StateClass stateObject = new StateClass();
				stateObject.setUploadFile(newFile);
				return stateObject;
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				ServiceUtil.throwCustomUserException(getErrorCodeMapping(), Constants.INTERNAL_SERVER_ERROR, MessageResource.UGC_DOWNLOAD_SERVICE_ERROR);
			}
			finally{
				closeStreaming(stream);
			}
		} else {
			logger.warn("MultiPart file object is null");
			ServiceUtil.throwCustomUserException(getErrorCodeMapping(), Constants.NO_FILE_OBJECT_IN_REQUEST, MessageResource.UGC_DOWNLOAD_SERVICE_ERROR);
		}
		
		return null;
	}

	/*
	 * Closing the stream
	 */
	private void closeStreaming(OutputStream stream) {
		
		if(stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				//handle dummy catch block
			}
		}
	}
	
	/*
	 * Get file path from configuration bean xml
	 */
	public String getFilePath() {
		return filePath;
	}

	/*
	 * Read the file path from configuration and set it into instance level variable
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
	/*
	 * Api to upload binary file to third party.
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.v2.service.IFileUploadService#uploadBinary(java.lang.String, com.onmobile.apps.ringbacktones.content.Subscriber)
	 */
	@Override
	public UgcFileUploadStatus uploadBinary(String fileName, Subscriber subscriber) throws UserException{
		logger.info("uploadBinary called to uploadBinary to server fileName: " + fileName);
		File uploadFile = new File(filePath + File.separator + fileName + ".wav");
		
		//Initialize the state object, which contains subscriber and upload file object.
		StateClass stateObject = new StateClass();
		stateObject.setSubscriber(subscriber);
		stateObject.setUploadFile(uploadFile);
		
		UgcFileUploadStatus fileUploadStatus = null;
		
		//Call transferFileToThirdParty api to transfer file to server.
		try {
			fileUploadStatus = transferFileToThirdParty(stateObject);
		}
		finally{
			//Make the null object for GC. 
			stateObject.makeObjectNull();
			uploadFile = null;
		}
		logger.info("uploadBinary returns UgcFileUploadStatus: " + fileUploadStatus);
		return fileUploadStatus;
	}

	/*
	 * Api to return boolean true value, if subsciber is allowed user to download UGC. 
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.v2.service.AbstractUGCFileService#allowedUserStatus(com.onmobile.apps.ringbacktones.content.Subscriber)
	 */			
	@Override
	public boolean allowedUserStatus(Subscriber subscriber) {
		if(subscriber == null) {
			return false;
		}
		AllowSubscriberStatus[] array = AllowSubscriberStatus.values();
		for(AllowSubscriberStatus obj : array) {
			if(subscriber.subYes().equals(obj.toString())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * AllowSubscriberStatus enum allow only for TO_BE_ACTIVATED(A), ACTIVATION_PENDING(N), ACTIVE(B), CHANGE(C), ACTIVATION_ERROR(E)
	 * SUSPENDED(Z,z), Grace(G)
	 * @author senthil.raja
	 *
	 */
	private enum AllowSubscriberStatus {
		A, N, B, C, E, Z, z, G;
	}
	
	//Added to delete Cutrbt file
	public void deleteBinary(String fileName, Subscriber subscriber){
		logger.info("deleteBinary fileName: " + fileName);
		File uploadFile = new File(filePath + File.separator + fileName + ".wav");
		StateClass stateObject = new StateClass();
		stateObject.setSubscriber(subscriber);
		stateObject.setUploadFile(uploadFile);
		
		try{
		boolean isFileDeleted = stateObject.getUploadFile().delete();
		
		if(isFileDeleted) {
			logger.info(stateObject.getUploadFile().getName() + " is deleted from " + stateObject.getUploadFile().getPath());
		}
		}	
		
		finally{
			//Make the null object for GC. 
			stateObject.makeObjectNull();
			uploadFile = null;
		}
		
		
	}

}
