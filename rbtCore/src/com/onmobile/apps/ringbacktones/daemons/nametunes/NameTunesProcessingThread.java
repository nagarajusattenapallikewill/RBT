package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.RbtNameTuneTrackingDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking.Status;
import com.onmobile.apps.ringbacktones.v2.dao.impl.RbtNameTuneTrackingDaoImpl;
import com.onmobile.apps.ringbacktones.v2.dto.RbtNameTuneLoggerDTO;

public class NameTunesProcessingThread extends Thread {
	private static Logger logger = Logger.getLogger(NameTunesProcessingThread.class);
	public RbtNameTuneTrackingDao nameTuneTrackingDao = null;
	public PropertiesProvider propertyObject = null;
	private boolean isFirstTime=true;
	
	void init() {
		nameTuneTrackingDao = RbtNameTuneTrackingDaoImpl.getInstance();
	}

	public void run() {
		try{
		Thread.currentThread().setName("NAME_TUNE_PROCESS_THREAD_DAEMON");
		FileDownloadUpload ftpOperations=new FileDownloadUpload();
		while (true) {
			init();
			ftpOperations.downloadProcessedRequests();
			updateClipFromFilesToDB();
			makeSelectionForNameTunes();
			this.generateCompletedReport();
			this.generateFailureReport();
			ftpOperations.uploadStatusReports();
			NameTuneFileUtils.deleteLocalFiles(false,true);
			try {
				Thread.sleep(NameTuneConstants.PROCESSING_THREAD_SLEEP_TIME * 60 * 1000);
			} catch (InterruptedException e) {
				logger.error("EXCEPTION OCCURRED IN "+Thread.currentThread().getName(), e);
			}			
		}
		}catch(Exception e){
			logger.error("EXCEPTION OCCURRED IN PROCESSING DAEMON ", e);
		}
	}

	public List<RbtNameTuneTracking> updateClipFromFilesToDB() {
		logger.info("Using Directory " +NameTuneConstants.LOCAL_BASE_DIRECTORY+ NameTuneConstants.NEW_REQ_PROCESSED_DIR);
		File toProcessDirectory = new File(NameTuneConstants.LOCAL_BASE_DIRECTORY, NameTuneConstants.NEW_REQ_PROCESSED_DIR);
		File[] localFiles = toProcessDirectory.listFiles();
		List<RbtNameTuneTracking> toProcessList = new ArrayList<RbtNameTuneTracking>();
		List<RbtNameTuneTracking> failureNameTuneTracking = new ArrayList<RbtNameTuneTracking>();
		for (int i = 0; i < localFiles.length; i++) {
			updateClipFromFileToDB(localFiles[i], toProcessList,failureNameTuneTracking);
		}		
		logger.debug("Failure Records "+ failureNameTuneTracking);
		List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs = null;
		nameTuneLoggerDTOs= NameTuneDBUtils.getInstance().converEntityToDto(failureNameTuneTracking);
		NameTuneFileUtils.logIntoFile(nameTuneLoggerDTOs,false,false,true);	
		return toProcessList;
	}

	public void makeSelectionForNameTunes() {
		List<RbtNameTuneTracking> selectionList = null;
		selectionList = getToBeProcessedRecordsFromDB();
		processSelectionForNameTunes(selectionList);
	}

	public void updateClipFromFileToDB(File file, List<RbtNameTuneTracking> list,List<RbtNameTuneTracking> failureNameTuneTracking ) {
		BufferedReader _bufferedReader = null;
		String line = null;
		String[] contents = null;
		RbtNameTuneTracking nm_object = null;
		List<RbtNameTuneTracking> nameTuneTrackings = null;
		try {
			_bufferedReader = new BufferedReader(new FileReader(file));
			String transId = null;
			String msisdn = null;
			String nameTuneName = null;
			_bufferedReader.readLine();
			Map<String,Object> parameter=new HashMap<String, Object>();
			while ((line = _bufferedReader.readLine()) != null) {				
				if (line != null) {
					contents = line.split(",");
					transId = contents[4];
					if (transId != null) {
						parameter.put("transactionId", transId);
						nameTuneTrackings = nameTuneTrackingDao.findByProperty(RbtNameTuneTracking.class,parameter);
						if (nameTuneTrackings != null) {
							if (nameTuneTrackings.size() == 0) {
								logger.error("NAME TUNE REQUESTS NOT PRESENT FOR TRANSID "
										+ transId);
								failureNameTuneTracking.add(process(contents));
								continue;
							} else {
								nm_object = nameTuneTrackings.get(0);
								if(Status.REQUEST_SENT.name().equalsIgnoreCase(nm_object.getStatus())){
									msisdn = contents[2];
									nameTuneName = contents[1];
									if (!nm_object.getMsisdn().equalsIgnoreCase(
											msisdn)
											|| !nm_object.getNameTune()
													.equalsIgnoreCase(nameTuneName)) {
										nm_object.setStatus(Status.FAILURE.name());
										nm_object.setModifiedDate(new Timestamp(new Date().getTime()));
									} else {
										nm_object.setClipId(contents[5]);
										nm_object.setModifiedDate(new Timestamp(new Date().getTime()));
										nm_object.setStatus(Status.TOBE_PROCESSED
												.name());
									}
									NameTuneDBUtils
											.updateNameTuneTrackingObject(nm_object);
								}else{
									logger.debug("RECORD IS AVAILABLE WITH TRANSACTION ID:"+transId+", BUT STATUS IS:"+nm_object.getStatus()+"  SO WE ARE NOT PROCESSING.");
								}
							}
						} else {
							logger
									.error("DB Exception occurred while fetching Name Tunes for transId "
											+ transId);
						}
					}
				}

			}

		} catch (IOException e) {
			logger
			.error("IO Exception while reading file "+file
					+ e.toString());

		} catch (Exception e) {
			logger
			.error("Fatal Exception while reading file "+file
					+ e.toString());	
		}
	}

	public List<RbtNameTuneTracking> getToBeProcessedRecordsFromDB() {
		return NameTuneDBUtils.getNmRecordsByProperty("status",
				Status.TOBE_PROCESSED.name());
	}

	public void processSelectionForNameTunes(
			List<RbtNameTuneTracking> toBeProcessedNameTunes) {
		logger.debug("NUMBER OF NAME TUNE REQUESTS TO BE PROCESSED "+ toBeProcessedNameTunes.size());
		String response = null;
		NameTuneRetryThread retryThread = null;
		for (RbtNameTuneTracking nm_Object : toBeProcessedNameTunes) {
			nm_Object.setModifiedDate(new Timestamp(System.currentTimeMillis()));
			if (isFirstTime || nm_Object.getRetryCount() == 0) {
				isFirstTime = false;
				response = NameTuneDBUtils.getInstance()
						.callProcessSelectionRequests(nm_Object);
				logger.debug("REPONSE FROM WEBSERVICE FOR TRANSID "+nm_Object.getTransactionId()+" is "+ response);
				if (response != null) {
					if (response.indexOf("success") != -1) {
						nm_Object.setStatus(Status.COMPLETED.name());
						
					} else if (response.indexOf("clip_not_exists") != -1) {
						nm_Object.setRetryCount(nm_Object.getRetryCount()+1);
						retryThread = new NameTuneRetryThread(nm_Object);
						retryThread.start();
					} else {
						nm_Object.setStatus(Status.FAILURE.name());
					}
				} else {
					nm_Object.setStatus(Status.FAILURE.name());
				}
				try {
					NameTuneDBUtils.updateNameTuneTrackingObject(nm_Object);
				} catch (Exception e) {
					logger.error(e);
				}
			}
//end
		}

	}

	public RbtNameTuneTracking process(String[] contents) {
		RbtNameTuneTracking nm_object = null;
		if (contents != null) {
			nm_object = new RbtNameTuneTracking();
			nm_object.setCreatedDate(null);
			nm_object.setNameTune(contents[1]);
			nm_object.setMsisdn(contents[2]);
			nm_object.setLanguage(contents[3]);
			nm_object.setTransactionId(contents[4]);
			nm_object.setClipId(contents[5]);
		}
		logger.debug(""+nm_object);
		return nm_object;
	}
	public static void main(String[] args) {
		NameTunesProcessingThread thread = new NameTunesProcessingThread();
		thread.start();
	}
	public void generateCompletedReport(){
		List<RbtNameTuneTracking> nameTuneTrackings=null;
		Map<String,Object> parameter=new HashMap<String, Object>();
		parameter.put("status", Status.COMPLETED.name());
		try {
			nameTuneTrackings = nameTuneTrackingDao.findByProperty(RbtNameTuneTracking.class,parameter);
			if(nameTuneTrackings!=null && nameTuneTrackings.size()>0){
				List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs = NameTuneDBUtils.getInstance().converEntityToDto(nameTuneTrackings);
				NameTuneFileUtils.logIntoFile(nameTuneLoggerDTOs,false,true,false);			
				nameTuneTrackingDao.deleteByProperty(RbtNameTuneTracking.class, parameter);
			}else{
				logger.fatal("NO DATA AVAILABLE TO CREATE COMPLETED LOG");
			}
		} catch (DataAccessException e) {
			logger.error("Error Trace:"+ExceptionUtils.getFullStackTrace(e));
		}
	}
	
	public void generateFailureReport(){
		List<RbtNameTuneTracking> nameTuneTrackings=null;
		Map<String,Object> parameter=new HashMap<String, Object>();
		parameter.put("status", Status.FAILURE.name());
		try {
			nameTuneTrackings = nameTuneTrackingDao.findByProperty(RbtNameTuneTracking.class,parameter);
			if(nameTuneTrackings!=null && nameTuneTrackings.size()>0){
				List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs = NameTuneDBUtils.getInstance().converEntityToDto(nameTuneTrackings);
				NameTuneFileUtils.logIntoFile(nameTuneLoggerDTOs,false,false,true);			
				nameTuneTrackingDao.deleteByProperty(RbtNameTuneTracking.class, parameter);
			}else{
				logger.fatal("NO DATA AVAILABLE TO CREATE FAILURE LOG");
			}
		} catch (DataAccessException e) {
			logger.error("Error Trace:"+ExceptionUtils.getFullStackTrace(e));
		}
	}

}
