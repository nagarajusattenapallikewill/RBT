package com.onmobile.apps.ringbacktones.v2.service;

import java.io.File;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.converter.GsonHttpMessageConverter;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;
import com.onmobile.apps.ringbacktones.v2.dto.GriffUgcResponseDto;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public abstract class AbstractUGCFileService implements IFileDownloadService, IFileUploadService{
	
	@Autowired
	protected ResponseErrorCodeMapping errorCodeMapping;
	
	Logger logger = Logger.getLogger(AbstractUGCFileService.class);

	public abstract StateClass downloadUGCFile(MultipartFile file, String fileName) throws UserException;
	public abstract boolean allowedUserStatus(Subscriber subscriber);
	
	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}
	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	protected UgcFileUploadStatus transferFileToThirdParty(StateClass stateObject) throws UserException {
		return null;
	}
	
	/*
	 * Api to download File. 
	 * 1. Will check subscriber to allowed to download file or not
	 * 2. Generate the server file name
	 * 3. Download the UGC file
	 * 4. If Redirect request is true, then upload the binary file to third party
	 * 5. If generate UGC is true, then make UGC entry in rbt_ugc_wav_file table and generate the unique ugc id and share it to client. 
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.v2.service.IFileDownloadService#downloadFile(long, java.lang.String, org.springframework.web.multipart.MultipartFile, java.lang.String)
	 */
	@Override
	public final Object downloadFile(long subscriberId, String mode, MultipartFile file, String fileName) throws UserException{
		logger.info("Controller called downloadFile: subscriberId: " + subscriberId + ", mode: " + mode + ", fileName: " +  fileName);
		StateClass stateObject = null;
		try {			
			
			long ugcId = -1;
			
			
			subscriberId = Long.parseLong(RBTDBManager.getInstance().subID(subscriberId + ""));
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId + "");
			
			if(!allowedUserStatus(subscriber)) {
				if(subscriber == null) {
					logger.warn("Rquest is not accepted because subscriber not exist subscriberId: " + subscriberId);
					ServiceUtil.throwCustomUserException(errorCodeMapping, Constants.SUB_DONT_EXIST, MessageResource.UGC_DOWNLOAD_SERVICE_ERROR);					
				}
				else {
					String subStatus = SubscriptionStatus.getSubscriptionStatus(subscriber.subYes());
					logger.warn("Subscriber not allowed to download UGC subscriberId " + subscriberId + " subscriber status: " + subStatus);
					ServiceUtil.throwCustomUserException(errorCodeMapping, subStatus.toLowerCase(), MessageResource.UGC_DOWNLOAD_SERVICE_ERROR);
				}				
			}
			
			fileName  = getServerGenerateFileName(fileName, subscriberId);
			logger.info("Server Generated fileName is:: " + fileName);
			
			stateObject = downloadUGCFile(file, fileName);
			stateObject.setSubscriber(subscriber);
			
			UgcFileUploadStatus uploadStatus = null;
			if(isRedirectRequestRequired()){
				logger.info("Forwarding binary to third party");
				uploadStatus = transferFileToThirdParty(stateObject);
			}
			
			RBTUgcWavfile ugcWavFile = null;
			
			if(isGenerateUgcId() && uploadStatus != null) {
				ugcWavFile = generateUgcId(subscriberId, fileName, mode, uploadStatus);
				ugcId = ugcWavFile.getUgcId();
				logger.info("Successfully ugcId generated ugcId: " + ugcId);
			}
			
			Map<String, String> returnObject = new HashMap<String, String>(1);
			returnObject.put("id", ugcId+"");
			return returnObject;
		}
		finally {
			if(stateObject != null) {
				stateObject.makeObjectNull();
				stateObject = null;
			}
		}
	}
	
	protected boolean isGenerateUgcId() {
		return true;
	}
	
	protected boolean isRedirectRequestRequired() {
		return true;
	}
	
	/**
	 * 
	 * @param fileName
	 * @param subscriberId
	 * @return
	 */
	protected String getServerGenerateFileName(String fileName, long subscriberId) {
		String returnFileName = subscriberId + "_" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
		return returnFileName + ".wav";
	}
	
	/**
	 * 
	 * @param subscriberId
	 * @param fileName
	 * @param mode
	 * @param uploadStatus
	 * @return
	 * @throws UserException
	 * 
	 * Make the UGC details in rbt_ugc_wav_file table and return the unique ugc id.
	 */
	private RBTUgcWavfile generateUgcId(long subscriberId, String fileName, String mode, UgcFileUploadStatus uploadStatus) throws UserException{
		
		IRbtUgcWavfileDao rbtUgcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
		
		
		RBTUgcWavfile ugcWavFile = new RBTUgcWavfile();
		if(fileName.indexOf(".wav") != -1) {
			fileName = fileName.substring(0, fileName.indexOf(".wav"));		
		}
		ugcWavFile.setUgcWavFile(fileName);
		ugcWavFile.setMode(mode);
		ugcWavFile.setUploadStatus(uploadStatus);
		ugcWavFile.setSubscriberId(subscriberId);
		
		try {
			rbtUgcWavfileDao.saveUgcWavfile(ugcWavFile);
		} catch (DataAccessException e) {
			logger.error(e.getMessage(), e);
			ServiceUtil.throwCustomUserException(getErrorCodeMapping(), Constants.DB_OPERATION_FAILED, MessageResource.UGC_DOWNLOAD_SERVICE_ERROR);
		}
		
		return ugcWavFile;
		
		
	}
	
	/**
	 * 
	 * @author senthil.raja
	 * StateClass to maintain the subscriber and file object state.
	 */
	protected class StateClass{
		private Subscriber subscriber;
		private File uploadFile;

		public Subscriber getSubscriber() {
			return subscriber;
		}

		public void setSubscriber(Subscriber subscriber) {
			this.subscriber = subscriber;
		}

		public File getUploadFile() {
			return uploadFile;
		}

		public void setUploadFile(File uploadFile) {
			this.uploadFile = uploadFile;
		}
		
		public void makeObjectNull(){
			subscriber = null;
			uploadFile = null;
		}
		
	}
}
