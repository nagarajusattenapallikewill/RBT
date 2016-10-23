package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.RbtNameTuneTrackingDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking.Status;
import com.onmobile.apps.ringbacktones.v2.dao.impl.RbtNameTuneTrackingDaoImpl;
import com.onmobile.apps.ringbacktones.v2.dto.RbtNameTuneLoggerDTO;

public class NameTunesNewRequestThread extends Thread{

	private static Logger logger = Logger.getLogger(NameTunesNewRequestThread.class);
	
	RbtNameTuneTrackingDao nameTuneTrackingDao=null;
	FileDownloadUpload ftpOprations= null;
	
	
	public NameTunesNewRequestThread(){		
		nameTuneTrackingDao = RbtNameTuneTrackingDaoImpl.getInstance();		
		ftpOprations=new FileDownloadUpload();
	}
	
	@Override
	public void run() {
		try{
		logger.info("Entering NameTunesNewRequestThread Sniffer...");
		Thread.currentThread().setName("NAME_TUNE_NEW_REQ_GENARATE_DEAMON");
		while (true) {
			this.getNewRequests();
			
			try {
				Thread.sleep(NameTuneConstants.NEW_REQ_THREAD_SLEEP_TIME*60*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		}catch(Throwable e){
			logger.error("Sniffer "+ e);
			e.printStackTrace();
			
		}
	}
	
	void getNewRequests(){
		List<RbtNameTuneTracking> nameTuneTrackings=null;
		Map<String,Object> parameter=new HashMap<String, Object>();
		parameter.put("status", Status.NEW_REQUEST.name());
		try {
			nameTuneTrackings = nameTuneTrackingDao.findByProperty(RbtNameTuneTracking.class,parameter);
			List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs = converEntityToDto(nameTuneTrackings);
			if(nameTuneLoggerDTOs!=null && nameTuneLoggerDTOs.size()>0){
				NameTuneFileUtils.logIntoFile(nameTuneLoggerDTOs,true,false,false);			
				for(RbtNameTuneTracking nameTuneTracking: nameTuneTrackings){
					nameTuneTracking.setStatus(Status.REQUEST_SENT.name());
					nameTuneTracking.setModifiedDate(new Timestamp(System.currentTimeMillis()));
					nameTuneTrackingDao.saveOrUpdateEntity(nameTuneTracking);
				}
				
				ftpOprations.uploadNewReqFiles();
				NameTuneFileUtils.deleteLocalFiles(true,false);
			}else{
				logger.debug("NO DATA TO CREATE NAME TUNE REQUEST FILES");
			}
		} catch (DataAccessException e) {
			logger.error("Error Trace:"+ExceptionUtils.getFullStackTrace(e));
		}
	}
	
	public List<RbtNameTuneLoggerDTO> converEntityToDto(List<RbtNameTuneTracking> nameTuneTrackings){
		List<RbtNameTuneLoggerDTO> nameTuneLoggerDTOs = null;
		RbtNameTuneLoggerDTO nameTuneLoggerDTO = null;
		if(nameTuneTrackings!=null){
			nameTuneLoggerDTOs= new ArrayList<RbtNameTuneLoggerDTO>(); 
			for(RbtNameTuneTracking nameTuneTracking: nameTuneTrackings){
				nameTuneLoggerDTO = new RbtNameTuneLoggerDTO();
				nameTuneLoggerDTO.setClipId(nameTuneTracking.getClipId());
				nameTuneLoggerDTO.setCreatedDate(nameTuneTracking.getCreatedDate());
				nameTuneLoggerDTO.setLanguage(nameTuneTracking.getLanguage());
				nameTuneLoggerDTO.setMsisdn(nameTuneTracking.getMsisdn());
				nameTuneLoggerDTO.setNameTune(nameTuneTracking.getNameTune());
				nameTuneLoggerDTO.setRetryCount(nameTuneTracking.getRetryCount());
				nameTuneLoggerDTO.setStatus(nameTuneTracking.getStatus());
				nameTuneLoggerDTO.setTransactionId(nameTuneTracking.getTransactionId());
				nameTuneLoggerDTOs.add(nameTuneLoggerDTO);
			}
		}
		return nameTuneLoggerDTOs;
	}
	
	
	
	
	public static void main(String[] args) {
		Thread newRequestThread=new Thread(new NameTunesNewRequestThread());
		newRequestThread.start();
		Thread processThread=new NameTunesProcessingThread();
		processThread.start();
		
	}

	
}
