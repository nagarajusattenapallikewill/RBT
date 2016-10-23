package com.onmobile.apps.ringbacktones.rbt2.builder.impl;

import java.util.Map;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class UDPShuffleListAssetUtilBuilder extends AbstractAssetUtilBuilder{

	@Override
	public SelectionRequest buildSelectionRequestForDeleteSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteSelection(selectionReqBean);
		selReq.setUdpId(selectionReqBean.getToneID());
		return selReq;
	}

	@Override
	public SelectionRequest buildSelectionRequestForAddSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForAddSelection(selectionReqBean);
		selReq.setUdpId(selectionReqBean.getToneID());
		return selReq;
	}
	
	@Override
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id){
			return RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(subscriberID, id, "UDP_ID");
	}
	
	@Override
	public Map<String,String> getWhereClauseForGettingLatestActiveSelection(PurchaseCombo purchaseCombo, String callerId, String clipId){
		String udpId = null;
		if(purchaseCombo != null){
		  udpId = String.valueOf(purchaseCombo.getAsset().getId());
		}
		Map<String, String> map = super.getWhereClauseForGettingLatestActiveSelection(purchaseCombo, callerId, clipId);
		if(udpId != null){
		  map.put("UDP_ID", udpId);
		}
		return map;
	}
}
