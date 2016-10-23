package com.onmobile.apps.ringbacktones.v2.resolver.request;

import java.util.Map;

import com.livewiremobile.store.storefront.dto.payment.Purchase;
import com.livewiremobile.store.storefront.dto.rbt.AssetList;
import com.onmobile.apps.ringbacktones.v2.dto.LibrayRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;



public interface IDownloadRequest {
	
	public Purchase likeContent(String msisdn, 
			String mode, LibrayRequestDTO dtoResource) throws UserException;
	
	public AssetList  getLibrary(String msisdn, String mode) throws UserException;
	
	public Map<String, String> deleteSongFromLibrary(String msisdn, String mdoe, String toneId, String cType) throws UserException;

	public Map<String, String> updateLibrary(String subscriberId, LibrayRequestDTO librayRequestDTO) throws UserException;
}
