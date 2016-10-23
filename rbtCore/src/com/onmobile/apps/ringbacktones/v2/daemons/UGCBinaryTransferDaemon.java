package com.onmobile.apps.ringbacktones.v2.daemons;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;
import com.onmobile.apps.ringbacktones.rbt2.thread.ThreadExecutor;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;

public class UGCBinaryTransferDaemon implements Runnable{

	Logger logger = Logger.getLogger(UGCBinaryTransferDaemon.class);
	
	private static UGCBinaryTransferDaemon ugcBinaryTransferDaemon;
	private ThreadExecutor executor;
	private int sleepTime = (60 * 1000);
	private ResourceBundle resourceBundle = null;
	
	private boolean isThreadAlive = false;
	
	static {
		new ClassPathXmlApplicationContext("bean_spring.xml");
	}
	
	private UGCBinaryTransferDaemon(){
		isThreadAlive = true;
		executor = (ThreadExecutor) ConfigUtil.getBean(BeanConstant.UGC_THREAD_EXECUTOR);		
		
		PropertyConfig config = (PropertyConfig) ConfigUtil.getBean(BeanConstant.PROPERTY_CONFIG);
		resourceBundle = config.loadBundle("daemonConfig");
		try {
			sleepTime = (Integer.parseInt(config.getValueFromResourceBundle(
					resourceBundle, "UGC_FILE_TRANSFER_DAEMON_SLEEP_TIME", sleepTime+"")) * 60 * 1000);
		} catch (Exception e) {
			logger.warn("UGC_FILE_TRANSFER_DAEMON_SLEEP_TIME is not configured, and consider default sleep time " + sleepTime + " in mis" );
		}
	}
	
	
	public static UGCBinaryTransferDaemon getInBinaryTransferDaemon() {
		
		if(ugcBinaryTransferDaemon == null) {
			synchronized (UGCBinaryTransferDaemon.class) {
				if(ugcBinaryTransferDaemon == null) {
					ugcBinaryTransferDaemon = new UGCBinaryTransferDaemon();
				}
			}
		}
		
		return ugcBinaryTransferDaemon;
	}
	
	@Override
	public void run() {
		while(isThreadAlive) {
			try {
				process();
			} catch (Exception e) {
				logger.error("Exception. UGCBinaryTransferDaemon will be killed, due to Exception",e);
				stopThread();
			}
			try {
				logger.info("Deamon being sleep in " + (sleepTime / (60 * 1000)) + " mins..");
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				//Ignore
			}
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 * 
	 * Query RbtUgcWavfile list from rbt_ugc_wav_file table where status is to_be_process_state(0) and 
	 * retry_count < configured value (which is configured in  RBTUgcWavFileDaoImpl bean in bean xml) and iterate RbtUgcWavFile and call upload api to transfer binary
	 * to third party server.
	 * If third party server is down / any third party server internal issue, then increase the retry_count and next_retry_time and update the RbtUgcWavfile record.
	 * RBTUgcWafile will processed though Executor framework. 
	 */
	private void process() throws Exception{
		IRbtUgcWavfileDao rbtUgcWavFileDao = null;
		try {
			rbtUgcWavFileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
		}
		catch(BeansException e) {
			logger.error(e.getMessage(), e);
		}
		if(rbtUgcWavFileDao == null) {
			logger.warn("RbtUgcWavFileDao bean is not confiugred. Please configure bean id " + BeanConstant.UGC_WAV_FILE_DAO);
			throw new IllegalAccessException("RbtUgcWavFileDao bean is not confiugred. Please configure bean id " + BeanConstant.UGC_WAV_FILE_DAO);
		}
		List<RBTUgcWavfile> rbtUgcWavFileList = null;
		try {
			rbtUgcWavFileList = rbtUgcWavFileDao.getUgcWavFilesToTransfer(UgcFileUploadStatus.TO_BE_PROCESS_STATE);
			
			if(rbtUgcWavFileList == null || rbtUgcWavFileList.size() == 0) {
				logger.info("RBTUgcWafile no records found");
				return;
			}
			
			for(RBTUgcWavfile rbtUgcWavFile : rbtUgcWavFileList) {
				WorkerThread thread = new UGCBinaryTransferWorkerThread(this);
				thread.setObject(rbtUgcWavFile);
				executor.getExecutor().execute(thread);
			}
			
			
		} catch (DataAccessException e) {
			logger.warn("Not able to access Data, please check with data base", e);
			throw new IllegalAccessException("Not able to access Data, please check with data base");
		}
	}
	
	public void stopThread(){
		isThreadAlive = false;
	}
	
	public boolean isThreadAlive() {
		return isThreadAlive;
	}


	/**
	 * 
	 * @param args
	 * 
	 * To support to run standlone daemon.
	 */
	public static void main(String[] args) {
		
		Thread thread = new Thread(UGCBinaryTransferDaemon.getInBinaryTransferDaemon());
		thread.setName("UGC_BINARY_TRANSFER_DAEMON");
		thread.start();
	}

}
