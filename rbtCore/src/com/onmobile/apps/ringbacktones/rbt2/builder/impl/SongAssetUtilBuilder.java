package com.onmobile.apps.ringbacktones.rbt2.builder.impl;

import java.util.HashMap;
import java.util.Map;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class SongAssetUtilBuilder extends AbstractAssetUtilBuilder{
	 
	


	@Override
	public SelectionRequest buildSelectionRequestForDeleteSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteSelection(selectionReqBean);
		selReq.setClipID(selectionReqBean.getToneID());
		return selReq;
	}

	
	@Override
	public SelectionRequest buildSelectionRequestForAddSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForAddSelection(selectionReqBean);
		selReq.setClipID(selectionReqBean.getToneID());
		if(selectionReqBean.getCategoryID() == null){
		    selReq.setCategoryID("3");
		}else{
			selReq.setCategoryID(selectionReqBean.getCategoryID());
		}
		if(selectionReqBean.getSelectionType() != null){
			selReq.setSelectionType(selectionReqBean.getSelectionType());
		}
		if(selectionReqBean.getProfileHours() != null){
			selReq.setProfileHours(selectionReqBean.getProfileHours());
		}
		return selReq;
	}

	@Override
	public long getToneID(String subscriberId, String wavFileName) throws UserException {
		long toneId = -1;
		Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFileName);
		if(clip != null){
			toneId = (long) clip.getClipId();
		}
		return toneId;
	}
	
	@Override
	public SelectionRequest buildSelectionRequestForAddToDownload(
			SelectionReqBean selectionReqBean) throws UserException {
		SelectionRequest selReq = super.buildSelectionRequestForAddToDownload(selectionReqBean);
		selReq.setClipID(selectionReqBean.getToneID());
		selReq.setCategoryID("3");
		return selReq;
	}
	
	@Override
	public SelectionRequest buildSelectionRequestForDeleteFromDownload(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteFromDownload(selectionReqBean);
		selReq.setClipID(selectionReqBean.getToneID());
		return selReq;
	}

	@Override
	public SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(
			String subscriberId, String id) {

		Clip clip = RBTCacheManager.getInstance().getClip(id);
		if (clip != null && clip.getClipRbtWavFile() != null) {
			return RBTDBManager.getInstance()
					.getActiveSubscriberDownloadByCatIdOrPromoId(subscriberId,clip.getClipRbtWavFile(), false);
		}
		return null;

	}
	
	@Override
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id){
		Clip clip = RBTCacheManager.getInstance().getClip(id);
		if (clip != null && clip.getClipRbtWavFile() != null) {
		return RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(subscriberID, clip.getClipRbtWavFile(), "SUBSCRIBER_WAV_FILE");
		}
		return null;
	}
	
	@Override
	public Map<String,String> getWhereClauseForGettingLatestActiveSelection(PurchaseCombo purchaseCombo, String callerId, String clipId){
		Map<String, String> map = super.getWhereClauseForGettingLatestActiveSelection(purchaseCombo, callerId, clipId);
		
		// Added for cut rbt changes
		if(clipId != null && clipId.contains("_cut_")){
			 map.put("SUBSCRIBER_WAV_FILE",clipId);
			 return map;
		}
		
		Clip clip = null;
		if(purchaseCombo != null){
		  clip = RBTCacheManager.getInstance().getClip(String.valueOf(purchaseCombo.getAsset().getId()));
		}
		
		if(clip != null){
		  map.put("SUBSCRIBER_WAV_FILE", clip.getClipRbtWavFile());
		}
		return map;
	}
}
