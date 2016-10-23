package com.onmobile.apps.ringbacktones.rbt2.builder.impl;

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

public class ShuffleCategoryAssetUtilBuilder extends AbstractAssetUtilBuilder{

	@Override
	public SelectionRequest buildSelectionRequestForDeleteSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteSelection(selectionReqBean);
		selReq.setCategoryID(selectionReqBean.getToneID());
		return selReq;
	}
	
	@Override
	public SelectionRequest buildSelectionRequestForAddSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForAddSelection(selectionReqBean);
		selReq.setCategoryID(selectionReqBean.getToneID());
		return selReq;
	}
	
	
	@Override
	public SelectionRequest buildSelectionRequestForAddToDownload(
			SelectionReqBean selectionReqBean) throws UserException {
		SelectionRequest selReq = super.buildSelectionRequestForAddToDownload(selectionReqBean);
		selReq.setCategoryID(selectionReqBean.getToneID());
		return selReq;
	}

	@Override
	public SelectionRequest buildSelectionRequestForDeleteFromDownload(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteFromDownload(selectionReqBean);
		selReq.setCategoryID(selectionReqBean.getToneID());
		return selReq;
	}
	
	@Override
	public SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(String subscriberId, String id){
		return RBTDBManager.getInstance().getActiveSubscriberDownloadByCatIdOrPromoId(subscriberId, id, true);
	}
	
	@Override
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id){
		return RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(subscriberID, id, "CATEGORY_ID");
	}

	
	@Override
	public Map<String,String> getWhereClauseForGettingLatestActiveSelection(PurchaseCombo purchaseCombo, String callerId, String clipId){
		Map<String, String> map = super.getWhereClauseForGettingLatestActiveSelection(purchaseCombo, callerId, clipId);
		if(purchaseCombo != null && purchaseCombo.getAsset() != null){
		  map.put("CATEGORY_ID", String.valueOf(purchaseCombo.getAsset().getId()));
		}
		return map;
	}
}
