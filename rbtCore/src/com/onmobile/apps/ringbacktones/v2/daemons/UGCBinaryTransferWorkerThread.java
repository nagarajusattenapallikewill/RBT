package com.onmobile.apps.ringbacktones.v2.daemons;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.IFileUploadService;

/**
 * 
 * @author senthil.raja
 *
 *WorkerThread, It will process RBTUgcWavfile and transfer binary to third party server.
 */
public class UGCBinaryTransferWorkerThread extends WorkerThread{
	
	Logger logger = Logger.getLogger(UGCBinaryTransferWorkerThread.class);
	
	public UGCBinaryTransferWorkerThread(UGCBinaryTransferDaemon daemon){
		super(daemon);
	}

	/**
	 * call upload api to transfer binary
	 * to third party server.
	 * If third party server is down / any third party server internal issue, then increase the retry_count and next_retry_time and update the RbtUgcWavfile record.
	 * Daemon will kill, in-case of Dao bean is not configured / Third party URL is not configured 
	 */
	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.v2.daemons.WorkerThread#processExecute(java.lang.Object)
	 */
	@Override
	protected void processExecute(Object ugcWavFile) throws IllegalAccessException{
		logger.info("Calling processExecute ugcWavFile: " + ugcWavFile);

		IRbtUgcWavfileDao rbtUgcWavFileDao = null;
		try {
			rbtUgcWavFileDao  = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
		}
		catch(BeansException e) {
			logger.error(e.getMessage(), e);
			throw new IllegalAccessException("Illegal access excception. Configuration issue in your bean configuration. Is " + BeanConstant.UGC_WAV_FILE_DAO + " configured in bean configuration file");
		}
	
		IFileUploadService fileUploadService = null;
		try {
			fileUploadService = (IFileUploadService) ConfigUtil.getBean(BeanConstant.FILE_DOWNLOAD_SERVICE + "ugc");
		}
		catch(BeansException e) {
			logger.error(e.getMessage(), e);
		}
		if(fileUploadService == null) {
			throw new IllegalAccessException("Illegal access excception. Configuration issue in your bean configuration. Is " + BeanConstant.FILE_DOWNLOAD_SERVICE + "ugc" + " configured in bean configuration file");
		}
		
		RBTUgcWavfile rbtUgcWavFile = RBTUgcWavfile.class.cast(ugcWavFile);
		
		Subscriber subscriber =  RBTDBManager.getInstance().getSubscriber(rbtUgcWavFile.getSubscriberId() + "");
		String fileName = rbtUgcWavFile.getUgcWavFile();
		
		UgcFileUploadStatus ugcFileUploadStatus = null;
		try {
			ugcFileUploadStatus = (UgcFileUploadStatus) fileUploadService.uploadBinary(fileName, subscriber);
		} catch (UserException e) {
			logger.error("Exception in uploadBinary", e);
			throw new IllegalAccessException("Upload Binary failed, subscriberId: " + rbtUgcWavFile.getSubscriberId() + ", wavFileName: " + rbtUgcWavFile.getUgcWavFile());
		}
		
		if(ugcFileUploadStatus != null) {
			if(ugcFileUploadStatus != UgcFileUploadStatus.SUCCESS_STATE) {
				rbtUgcWavFile.setRetryCount(rbtUgcWavFile.getRetryCount()+1);
			}
			
			rbtUgcWavFile.setUploadStatus(ugcFileUploadStatus);
			
		}
		
		try {
			rbtUgcWavFileDao.updateUgcWavfile(rbtUgcWavFile);
		} catch (DataAccessException e) {
			logger.error("Exception while udpate UGC wavfile " + rbtUgcWavFile, e);
		}
	}	
}
