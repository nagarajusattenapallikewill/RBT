package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.daemons.UGCBinaryTransferWorkerThread;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.IFileUploadService;

public class UploadDeamon extends Thread {
	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	Logger logger = Logger.getLogger(UploadDeamon.class);

	private Integer maxRetryCount = RBTParametersUtils.getParamAsInt(iRBTConstant.DAEMON,
			"CUTRBT_UPLOADDEAMON_MAX_RETRYCOUNT", 5);
	private String msisdn;
	private String file;
	long slptime = RBTParametersUtils.getParamAsLong(iRBTConstant.DAEMON, "CUTRBT_UPLOADDEAMON_SLEEPTIME", 5000);
	UgcFileUploadStatus ugcFileUploadStatus = null;
	IFileUploadService fileUploadService = null;
	int count = 0;

	@Override
	public void run() {

		while (count < maxRetryCount) {

			try {
				Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
				try {
					// Need to add bean and file path in bean? koyel
					fileUploadService = (IFileUploadService) ConfigUtil
							.getBean(BeanConstant.FILE_DOWNLOAD_SERVICE + "cutrbt");
				} catch (BeansException e) {
					logger.error(e.getMessage(), e);
				}

				ugcFileUploadStatus = (UgcFileUploadStatus) fileUploadService.uploadBinary(file, subscriber);
			} catch (UserException e) {
				
				logger.error("Exception in uploadBinary", e);


			}

			if (ugcFileUploadStatus != null) {
				if (ugcFileUploadStatus != UgcFileUploadStatus.SUCCESS_STATE) {
					
					try {
						logger.info("count:"+count);
						Thread.sleep(slptime);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else {
					break;
				}

			}
			count = count + 1;

		}
		if(count==maxRetryCount)
		{
			logger.info(":--->  MaxRetryCount is over");
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
			fileUploadService = (IFileUploadService) ConfigUtil
					.getBean(BeanConstant.FILE_DOWNLOAD_SERVICE + "cutrbt");
		fileUploadService.deleteBinary(file, subscriber);
		}
	}
}
