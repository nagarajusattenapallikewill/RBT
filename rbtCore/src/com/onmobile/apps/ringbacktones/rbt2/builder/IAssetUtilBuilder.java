package com.onmobile.apps.ringbacktones.rbt2.builder;

import java.util.Map;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public interface IAssetUtilBuilder {
	
	public SelectionRequest buildSelectionRequestForAddSelection(SelectionReqBean selectionReqBean);
	
	public SelectionRequest buildSelectionRequestForDeleteSelection(SelectionReqBean selectionReqBean);
	
	public SelectionRequest buildSelectionRequestForAddToDownload(SelectionReqBean selectionReqBean) throws UserException;
	
	public SelectionRequest buildSelectionRequestForDeleteFromDownload(SelectionReqBean selectionReqBean);

	public long getToneID(String subscriberId, String wavFileName)  throws UserException;
	
	public SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(String subscriberId, String id);
	
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id);
	
	public Map<String,String> getWhereClauseForGettingLatestActiveSelection(PurchaseCombo purchaseCombo, String callerId, String clipId);
}
