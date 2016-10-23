package com.onmobile.apps.ringbacktones.rbt2.builder.impl;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class DefaultAssetUtilBuilder extends AbstractAssetUtilBuilder{

	@Override
	public SelectionRequest buildSelectionRequestForDeleteSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForDeleteSelection(selectionReqBean);
		selReq.setRefID(selectionReqBean.getToneID());
		return selReq;
	}
	
	@Override
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id){
		return RBTDBManager.getInstance().getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(subscriberID, id, "REF_ID");
	}

}
