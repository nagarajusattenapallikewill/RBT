package com.onmobile.apps.ringbacktones.rbt2.builder;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public abstract class AbstractAssetUtilBuilder implements IAssetUtilBuilder{
	
		
	public SelectionRequest buildSelectionRequestForAddSelection(
			SelectionReqBean selectionReqBean) {
		
		SelectionRequest selectionRequest = new SelectionRequest(selectionReqBean.getSubscriberId());
		selectionRequest.setIsDtoCRequest(selectionReqBean.getIsDtoCRequest());
		selectionRequest.setMode(selectionReqBean.getMode());
		selectionRequest.setIsSelDirectActivation(selectionReqBean.getIsSelDirectActivation());
		selectionRequest.setSelectionStartTime(selectionReqBean.getSelectionStartTime());
		if(selectionReqBean.getCallerID() != null){
			selectionRequest.setCallerID(selectionReqBean.getCallerID());
		}
		
		if(selectionReqBean.getSelectionStartTime() !=null){
		selectionRequest.setSelectionStartTime(selectionReqBean.getSelectionStartTime());
		}
		if(selectionReqBean.getSelectionEndTime() !=null){
		selectionRequest.setSelectionEndTime(selectionReqBean.getSelectionEndTime());
		}
		if(selectionReqBean.getFromTime() !=null){
		selectionRequest.setFromTime(selectionReqBean.getFromTime());
		}
		if(selectionReqBean.getFromTimeMinutes() !=null){
		selectionRequest.setFromTimeMinutes(selectionReqBean.getFromTimeMinutes());
		}
		if(selectionReqBean.getToTime() !=null){
		selectionRequest.setToTime(selectionReqBean.getToTime());
		}
		if(selectionReqBean.getToTimeMinutes() !=null){
		selectionRequest.setToTimeMinutes(selectionReqBean.getToTimeMinutes());
		}
		
		//Added for ephemeral rbt
		if(selectionReqBean.getStatus() > 0){
			selectionRequest.setStatus(selectionReqBean.getStatus());
		}
		HashMap<String, String> selInfoMap = new HashMap<String, String>();
		
		if(selectionReqBean.getPlayCount() !=null){
			selInfoMap.put("PLAYCOUNT", selectionReqBean.getPlayCount());
		}
		
		if(selectionReqBean.getFirstName()!=null)
			selInfoMap.put(WebServiceConstants.CALLER_FIRST_NAME, selectionReqBean.getFirstName());
		if(selectionReqBean.getLastName()!=null)
			selInfoMap.put(WebServiceConstants.CALLER_LAST_NAME, selectionReqBean.getLastName());
		
		if(selInfoMap.size()>0)
			selectionRequest.setSelectionInfoMap(selInfoMap);
		
		// Added for OI brazil inloop workaround soln
		if(selectionReqBean.getInLoop() != null && selectionReqBean.getInLoop().booleanValue()){
		    selectionRequest.setInLoop(selectionReqBean.getInLoop());
		}
		
		return selectionRequest;
	}

	public SelectionRequest buildSelectionRequestForDeleteSelection(
			SelectionReqBean selectionReqBean) {
		
		SelectionRequest selectionRequest = new SelectionRequest(selectionReqBean.getSubscriberId());
		selectionRequest.setIsDtoCRequest(selectionReqBean.getIsDtoCRequest());
		selectionRequest.setMode(selectionReqBean.getMode());
		selectionRequest.setIsDirectDeactivation(selectionReqBean.getIsDirectDeactivation());
		return selectionRequest;
	}

	public SelectionRequest buildSelectionRequestForAddToDownload(
			SelectionReqBean selectionReqBean) throws UserException{
		SelectionRequest selectionRequest = new SelectionRequest(selectionReqBean.getSubscriberId());
		selectionRequest.setMode(selectionReqBean.getMode());
		selectionRequest.setIsDtoCRequest(selectionReqBean.getIsDtoCRequest());
		selectionRequest.setIsSelDirectActivation(selectionReqBean.getIsSelDirectActivation());
		if(selectionReqBean.getSelectionStartTime() != null){
		selectionRequest.setSelectionStartTime(selectionReqBean.getSelectionStartTime());
		}
		return selectionRequest;
	}

	public SelectionRequest buildSelectionRequestForDeleteFromDownload(
			SelectionReqBean selectionReqBean) {
		SelectionRequest selectionRequest = new SelectionRequest(selectionReqBean.getSubscriberId());
		selectionRequest.setMode(selectionReqBean.getMode());
		selectionRequest.setIsDtoCRequest(selectionReqBean.getIsDtoCRequest());
		selectionRequest.setIsDirectDeactivation(selectionReqBean.getIsDirectDeactivation());
		return selectionRequest;
	}
	
	public long getToneID(String subscriberId, String wavFileName) throws UserException{
		return -1;
	}
	
	public SubscriberDownloads getActiveSubscriberDownloadByCatIdOrPromoId(String subscriberId, String id){
		return null;
	}
	
	public SubscriberStatus getSubscriberActiveSelectionsBySubIdorCatIdorWavFileorUDPId(String subscriberID, String id){return null;}
	
	public Map<String,String> getWhereClauseForGettingLatestActiveSelection(PurchaseCombo purchaseCombo, String callerId, String clipId){
		Map<String, String> map = new HashMap<String, String>();
		map.put("CALLER_ID", callerId);
		return map;
	}
}
