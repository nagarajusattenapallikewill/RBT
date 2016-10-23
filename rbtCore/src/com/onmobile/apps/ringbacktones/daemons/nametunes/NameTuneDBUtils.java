package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.RbtNameTuneTrackingDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;
import com.onmobile.apps.ringbacktones.v2.dao.impl.RbtNameTuneTrackingDaoImpl;
import com.onmobile.apps.ringbacktones.v2.dto.RbtNameTuneLoggerDTO;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
public class NameTuneDBUtils {

	public static RbtNameTuneTrackingDao nameTuneTrackingDao = null;
	public static NameTuneDBUtils nm_DBUtils = null;
	private static Logger logger = Logger.getLogger(NameTuneDBUtils.class);
	
	static {
		nameTuneTrackingDao = RbtNameTuneTrackingDaoImpl.getInstance();
	}
	
	public static NameTuneDBUtils getInstance() {
		synchronized (NameTuneDBUtils.class) {
			if (nm_DBUtils == null) {
				return new NameTuneDBUtils();
			}
		}
		return nm_DBUtils;
	}
	
	public String callProcessSelectionRequests(RbtNameTuneTracking nameTuneRequest){
		SelectionRequest selRequest = null;
		Rbt rbt =null;
		String response = null;		
		selRequest = new SelectionRequest(nameTuneRequest.getMsisdn(), NameTuneConstants.DEFAULT_CATEGORY_ID,
				nameTuneRequest.getClipId());
		selRequest.setMode(NameTuneConstants.SELECTION_MODE);
		rbt = RBTClient.getInstance()
				.addSubscriberSelection(selRequest);
		if(selRequest!=null)
			response = selRequest.getResponse();
		return response;
	}
	
	public static List<RbtNameTuneTracking> getNmRecordsByProperty(String key, String value) {
		List<RbtNameTuneTracking> nameTuneTrackings = null;
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.put(key, value);
		try {
			nameTuneTrackings = nameTuneTrackingDao.findByProperty(
					RbtNameTuneTracking.class, parameter);
		} catch (DataAccessException e) {
			 logger.error("Error Trace:"+ExceptionUtils.getFullStackTrace(e));
		}
		return nameTuneTrackings;
	}
	
	public static boolean updateNameTuneTrackingObject(RbtNameTuneTracking nm_toUpdate){
		boolean updated = false;
		try {
			nameTuneTrackingDao.saveOrUpdateEntity(nm_toUpdate);
			updated = true;
		} catch (DataAccessException e) {
			 logger.error("Error Trace:"+ExceptionUtils.getFullStackTrace(e));			
		}
		return updated;
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
	

}
