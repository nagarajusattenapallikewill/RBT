package com.onmobile.apps.ringbacktones.v2.resolver.response;

import java.util.Map;

import com.livewiremobile.store.storefront.dto.payment.Purchase;
import com.livewiremobile.store.storefront.dto.rbt.AssetList;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public interface IDownloadResponse {
	
      public Purchase prepareLikeContentResponse(String msisdn,String response, Rbt rbt, SelectionRequest selectionRequest, AbstractOperatorUtility operatorUtility) throws UserException;
      
      public AssetList prepareGetLibraryResponse(Library library) throws UserException;
      
      public Map<String, String> prepareDeleteDownloadResponse(String response) throws UserException;

      public Map<String, String> prepareUpdateDownloadResponse(String response) throws UserException;
}
