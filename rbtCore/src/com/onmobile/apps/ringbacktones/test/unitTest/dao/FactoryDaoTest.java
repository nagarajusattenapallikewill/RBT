package com.onmobile.apps.ringbacktones.test.unitTest.dao;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.RbtNameTuneTrackingDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking.Status;
import com.onmobile.apps.ringbacktones.v2.dao.impl.RbtNameTuneTrackingDaoImpl;
/**
 * 
 * @author lakka.rameswarareddy
 *<p>To TEST perform basic CURD operation on any table using hibernate.
 * And Get data based on the specific column and delete based on specific column data </p> 
 */
public class FactoryDaoTest {
	private static Logger logger = Logger.getLogger(FactoryDaoTest.class);
	
	RbtNameTuneTrackingDao rbtNameTuneTrackingDao=RbtNameTuneTrackingDaoImpl.getInstance();
	RbtNameTuneTracking nameTuneTracking = null;
	String transactionId = RandomStringUtils.randomNumeric(20);
	String clipId = "123";
	Timestamp createdDate = new Timestamp(System.currentTimeMillis());
	String language = "eng";
	String msisdn = "8904827131";
	String nameTune = "Hello Ram";
	int retryCount = 0;
	String status = "";
		
	//@Test
	public void findAll(){
		List<RbtNameTuneTracking> nameTuneTrackings = null;
		try {
			nameTuneTrackings = rbtNameTuneTrackingDao.findAll(RbtNameTuneTracking.class,1,12);
			logger.info("findAll:"+nameTuneTrackings);
			Assert.assertTrue(nameTuneTrackings!=null);
			logger.info("findAll Size:"+nameTuneTrackings.size());
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void saveOrUpdateEntity(){
		nameTuneTracking = new RbtNameTuneTracking();
		nameTuneTracking.setClipId(clipId);
		nameTuneTracking.setLanguage(language);
		nameTuneTracking.setCreatedDate(createdDate);
		nameTuneTracking.setMsisdn(msisdn);
		nameTuneTracking.setNameTune(nameTune);
		nameTuneTracking.setRetryCount(retryCount);
		nameTuneTracking.setStatus(Status.NEW_REQUEST.name());
		nameTuneTracking.setTransactionId(transactionId);
		try {
			rbtNameTuneTrackingDao.saveOrUpdateEntity(nameTuneTracking);
			logger.info("saveOrUpdateEntity:"+nameTuneTracking);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	public void findEntityById(){
		try {
			if(nameTuneTracking==null){
				saveOrUpdateEntity();
			}
			nameTuneTracking = rbtNameTuneTrackingDao.findEntityById(RbtNameTuneTracking.class,nameTuneTracking.getTransactionId());
			logger.info("findEntityById:"+nameTuneTracking);
			Assert.assertTrue(nameTuneTracking!=null);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
	
	
	
	//@Test
	public void deletEntityById(){
		try {
			if(nameTuneTracking==null){
				findEntityById();
			}
			boolean isDeleted = rbtNameTuneTrackingDao.deletEntityById(RbtNameTuneTracking.class, nameTuneTracking.getTransactionId());
			Assert.assertTrue(isDeleted);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	public void findByProperty(){
		List<RbtNameTuneTracking> nameTuneTrackings = null;
		Map<String, Object> propertyName = new HashMap<String, Object>();
		try {
			propertyName.put("retryCount", "0");
			nameTuneTrackings = rbtNameTuneTrackingDao.findByProperty(RbtNameTuneTracking.class, propertyName,1,3);
			logger.info("findByProperty: size:"+nameTuneTrackings.size()+" List : "+nameTuneTrackings);
			Assert.assertTrue(nameTuneTrackings!=null);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
	
	//@Test
	public void deleteByProperty(){
		Map<String, Object> propertyName = new HashMap<String, Object>();
		try {
			propertyName.put("retryCount", "0");
			int delCount = rbtNameTuneTrackingDao.deleteByProperty(RbtNameTuneTracking.class, propertyName);
			logger.info("deleteByProperty deleted count:"+delCount);
			Assert.assertTrue(delCount>0);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
	}
}
