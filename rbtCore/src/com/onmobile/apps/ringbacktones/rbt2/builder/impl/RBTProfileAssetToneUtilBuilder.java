package com.onmobile.apps.ringbacktones.rbt2.builder.impl;

import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class RBTProfileAssetToneUtilBuilder extends SongAssetUtilBuilder {

	
	
	@Override
	public SelectionRequest buildSelectionRequestForAddSelection(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selReq = super.buildSelectionRequestForAddSelection(selectionReqBean);
		selReq.setClipID(selectionReqBean.getToneID());
		selReq.setSelectionType(99);
		selReq.setCategoryID("99");
		
		if(selectionReqBean.getProfileHours() != null){
			selReq.setProfileHours(selectionReqBean.getProfileHours());
		}
		return selReq;
	}
}
