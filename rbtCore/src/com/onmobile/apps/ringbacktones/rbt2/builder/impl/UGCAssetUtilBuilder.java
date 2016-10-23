package com.onmobile.apps.ringbacktones.rbt2.builder.impl;

import java.util.Map;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class UGCAssetUtilBuilder extends AbstractAssetUtilBuilder{
	
	Logger logger = Logger.getLogger(UGCAssetUtilBuilder.class);
	
	private String categoryId;

	
	public String getCategoryId() {
		return categoryId;
	}
	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}
	@Override
	public SelectionRequest buildSelectionRequestForAddSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForAddSelection(selectionReqBean);
		try {
			IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(selectionReqBean.getToneID()));
			selReq.setClipID(ugcWavFile.getUgcWavFile());
			selReq.setCategoryID(categoryId);
		}catch (Exception e) {
			logger.info("Exception occured in buildSelectionRequestForAddSelection : "+e);
		}
		
		return selReq;
	}
	
	
	@Override
	public SelectionRequest buildSelectionRequestForDeleteSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteSelection(selectionReqBean);
		try {
			IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(selectionReqBean.getToneID()));
			if(ugcWavFile != null){
			 selReq.setRbtFile(ugcWavFile.getUgcWavFile());
			}
			selReq.setCategoryID(categoryId);
		}catch (Exception e) {
			logger.info("Exception occured in buildSelectionRequestForDeleteSelection : "+e);
		}
		return selReq;
	}
	
	
	
	@Override
	public SelectionRequest buildSelectionRequestForAddToDownload(
			SelectionReqBean selectionReqBean) throws UserException {
		SelectionRequest selReq = super.buildSelectionRequestForAddToDownload(selectionReqBean);
		
		try {
			IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(selectionReqBean.getToneID()));
			if(ugcWavFile != null){
			  selReq.setClipID(String.valueOf(ugcWavFile.getUgcId()));
			  selReq.setRbtFile(ugcWavFile.getUgcWavFile());
			}
			selReq.setCategoryID(categoryId);
		}catch (Exception e) {
			throw new UserException(e.getMessage());
		}
		return selReq;
	}
	
	@Override
	public SelectionRequest buildSelectionRequestForDeleteFromDownload(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteFromDownload(selectionReqBean);
		try {
			IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(selectionReqBean.getToneID()));
			if(ugcWavFile != null){
			  //selReq.setClipID(String.valueOf(ugcWavFile.getUgcId()));
			  selReq.setRbtFile(ugcWavFile.getUgcWavFile());
			}
			selReq.setCategoryID(categoryId);
		}catch (Exception e) {
			logger.info("Exception occured in buildSelectionRequestForDeleteSelection : "+e);
		}
		return selReq;
	}
	
	@Override
	public long getToneID(String subscriberId, String wavFileName) throws UserException {
		if(subscriberId == null || !subscriberId.isEmpty())
			throw new UserException(Constants.INVALID_SUBSCRIBER);
		long ugcId = -1;
		try {
		    IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(subscriberId), wavFileName);
			return ugcWavFile.getUgcId();
		} catch (Exception e) {
			logger.info("Exception occured in getToneID: "+e);
		} 
		return ugcId;
	}
	
	@Override
	public SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(String subscriberId, String id){
		IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
		String ugcWavFileName = id;
		try {
			RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(id));
			if(ugcWavFile != null){
			  ugcWavFileName = ugcWavFile.getUgcWavFile();
			}
		} catch (Exception e) {
			logger.info("Exception occured while getting UGC");
		}
		return RBTDBManager.getInstance().getActiveSubscriberDownloadByCatIdOrPromoId(subscriberId, ugcWavFileName, false);
	}
	
	@Override
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id){
			return RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(subscriberID, id, "SUBSCRIBER_WAV_FILE");
	}
	
	@Override
	public Map<String,String> getWhereClauseForGettingLatestActiveSelection(PurchaseCombo purchaseCombo, String callerId, String clipId){
		String ugcId = null;
		if(purchaseCombo != null){
		  ugcId = String.valueOf(purchaseCombo.getAsset().getId());
		}
		Map<String, String> map = super.getWhereClauseForGettingLatestActiveSelection(purchaseCombo, callerId, clipId);
		 String ugcWavFileName = null;
		if(ugcId != null){
			IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
			try {
				RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(ugcId));
				if(ugcWavFile != null){
				   ugcWavFileName = ugcWavFile.getUgcWavFile();
				}
			} catch (Exception e) {
				logger.info("Exception occured while getting UGC");
			}	
			if(ugcWavFileName != null){			
		       map.put("SUBSCRIBER_WAV_FILE", ugcWavFileName);
			}
		}
		return map;
	}

}
