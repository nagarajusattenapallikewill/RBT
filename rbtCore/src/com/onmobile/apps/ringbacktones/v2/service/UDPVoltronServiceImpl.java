package com.onmobile.apps.ringbacktones.v2.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.AssetList;
import com.livewiremobile.store.storefront.dto.rbt.Pager;
import com.livewiremobile.store.storefront.dto.rbt.RBTUGC;
import com.livewiremobile.store.storefront.dto.rbt.Shuffle;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.bean.UDPResponseBean;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.factory.ShuffleAsset;
import com.onmobile.apps.ringbacktones.v2.factory.SongAsset;
import com.onmobile.apps.ringbacktones.v2.factory.UGCSongAsset;
import com.onmobile.apps.ringbacktones.v2.webservice.client.request.UDPRestRequest;

public class UDPVoltronServiceImpl implements IUDPService, Constants{
	
	
	
	private Logger logger = Logger.getLogger(UDPVoltronServiceImpl.class);
	private Locale locale = LocaleContextHolder.getLocale();
	
	@Autowired
	private ApplicationContext applicationContext;
	private ResponseErrorCodeMapping errorCodeMapping;
	
	
	private IUDPService updService;
	
	public void setUpdService(IUDPService updService) {
		this.updService = updService;
	}

	@Override
	public Asset createUDP(String msisdn, String name,
			String mode,String extrainfo) throws UserException{
		logger.info("inside createUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn + ",name: "+ name +", mode: " + mode + ", extrainfo: " + extrainfo);
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setUdpName(name);
		udpRestRequest.setMode(mode);
		udpRestRequest.setExtraInfo(extrainfo);
		udpRestRequest.setIsDtoCRequest(true);
		
//		UDPResponseBean udpResponseBean = RBTClient.getInstance().createUDP(udpRestRequest);
		
		UDPResponseBean udpResponseBean = UDPResponseBean.class.cast(updService.createUDP(msisdn, name, mode, extrainfo));
		
		
		ShuffleAsset shuffleAsset = new ShuffleAsset().setId(udpResponseBean.getId())
				.setName(udpResponseBean.getName())
				.setExtraInfo(udpResponseBean.getExtraInfo());
		
		logger.info("Returning response: "+ shuffleAsset.buildShuffle());
		return shuffleAsset.buildShuffle();
	}
	
	@Override
	public Map<String, String> deleteUDP(String msisdn, String udpId)
			throws UserException {
		logger.info("inside deleteUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn + " udpId: "+udpId);
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setUdpId(udpId);
		udpRestRequest.setIsDtoCRequest(true);
		
//		String response = RBTClient.getInstance().removeUDP(udpRestRequest);
		
		String response = String.class.cast(updService.deleteUDP(msisdn, udpId));
		
		if(!response.equalsIgnoreCase("SUCCESS")) {
			
			String errorCode = errorCodeMapping.getErrorCode(response.toLowerCase()).getCode();
			String message = applicationContext.getMessage(MessageResource.DELETE_SETTING_MESSAGE + response.toLowerCase(), null, locale);
			throw new UserException(errorCode, message);
		}
		
		return constructSuccessMap(MessageResource.UDP_DELETE_MESSAGE_FOR,response);
		
	}

	@Override
	public Asset updateUDP(String msisdn, String name,
			String mode, String extrainfo, String udpId) throws UserException {
		logger.info("inside updateUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn + ",name: "+ name +", mode: " + mode + ", extrainfo: " + extrainfo + " udpId: "+udpId);
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setUdpName(name);
		udpRestRequest.setMode(mode);
		udpRestRequest.setExtraInfo(extrainfo);
		udpRestRequest.setIsDtoCRequest(true);
		
//		UDPResponseBean udpResponseBean = RBTClient.getInstance().updateUDP(udpRestRequest);
		
		UDPResponseBean udpResponseBean = UDPResponseBean.class.cast(updService.updateUDP(msisdn, name, mode, extrainfo, udpId));
		
		ShuffleAsset shuffleAsset = new ShuffleAsset().setId(udpResponseBean.getId())
				.setName(udpResponseBean.getName())
				.setExtraInfo(udpResponseBean.getExtraInfo());
		
		logger.info("Returning response: "+ shuffleAsset.buildShuffle());
		return shuffleAsset.buildShuffle();
	}


	@Override
	public AssetList getAllUDP(String msisdn, int offset, int pageSize) throws UserException {
		logger.info("inside updateUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn);
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setIsDtoCRequest(true);
		
//		List<UDPResponseBean> udpResponseBeanList = RBTClient.getInstance().getAllUDP(udpRestRequest);
		
		List<UDPResponseBean> udpResponseBeanList = (List<UDPResponseBean>)updService.getAllUDP(msisdn, offset, pageSize);
		
		int size = udpResponseBeanList.size();
		
		List<Asset> assets = new ArrayList<Asset>(size);
		for(UDPResponseBean udpResponseBean : udpResponseBeanList) {
			ShuffleAsset shuffleAsset = new ShuffleAsset().setId(udpResponseBean.getId())
					.setName(udpResponseBean.getName())
					.setExtraInfo(udpResponseBean.getExtraInfo());
			assets.add(shuffleAsset.buildShuffle());
		}
		
		AssetList assetList = new AssetList(assets);
		
		assetList.setCount(udpResponseBeanList.size());
		assetList.setPager(new Pager(0, size));
		
		logger.info("User " + msisdn + " has " +  size + "asset (shufflelist) ");
		
		return assetList;
	
	}

	@Override
	public Map<String, String> deleteContentFromUDP(String msisdn, String udpId, String toneId, String ctype) throws UserException{
		logger.info("inside addContentToUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn + " udpId: "+udpId);
		
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setClipId(toneId);
		udpRestRequest.setUdpId(udpId);
		udpRestRequest.setIsDtoCRequest(true);
		
//		String response = RBTClient.getInstance().removeContentFromUDP(udpRestRequest);
		
		String response = String.class.cast(updService.deleteContentFromUDP(msisdn, udpId, toneId,ctype));
		
		if(!response.equalsIgnoreCase("SUCCESS")) {
			
			String errorCode = errorCodeMapping.getErrorCode(response.toLowerCase()).getCode();
			String message = applicationContext.getMessage(MessageResource.DELETE_SETTING_MESSAGE + response.toLowerCase(), null, locale);
			throw new UserException(errorCode, message);
		}
		
		return constructSuccessMap(MessageResource.UDP_CONTENT_DELETE_MESSAGE_FOR,response);
	}

	@Override
	public Shuffle getContentsFromUDP(String msisdn, String udpId, int offset, int pageSize) throws UserException {
		logger.info("inside addContentToUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn + " udpId: "+udpId);
		
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setUdpId(udpId);
		udpRestRequest.setIsDtoCRequest(true);
		
//		UDPResponseBean udpResponseBean = RBTClient.getInstance().getAllContentsFromUDP(udpRestRequest);
		UDPResponseBean udpResponseBean = UDPResponseBean.class.cast(updService.getContentsFromUDP(msisdn, udpId, offset, pageSize));
		
		if(udpResponseBean == null) {
			
			String errorCode = errorCodeMapping.getErrorCode("").getCode();
			String message = applicationContext.getMessage(MessageResource.DELETE_SETTING_MESSAGE + "", null, locale);
			throw new UserException(errorCode, message);
		}
		
		
		ShuffleAsset shuffleAsset = new ShuffleAsset().setId(udpResponseBean.getId())
				.setName(udpResponseBean.getName())
				.setExtraInfo(udpResponseBean.getExtraInfo());
		
		
		List<Clip> mapedSongList = udpResponseBean.getClips();
		
		if(mapedSongList != null && mapedSongList.size() > 0) {
			for(Clip clip : mapedSongList){
				
				if(clip.getAlbum() != null && clip.getAlbum().equalsIgnoreCase("RBTUGC")) {
					RBTUGC rbtugc = (RBTUGC) new UGCSongAsset().setId(clip.getClipId()+"").buildAsset();
					shuffleAsset = shuffleAsset.setSong(rbtugc);
					
				} else {
					Song song = new SongAsset().setTitle(clip.getClipName()).setId(clip.getClipId()).buildSong();
					shuffleAsset = shuffleAsset.setSong(song);
				}

			}
			shuffleAsset = shuffleAsset.setCount(mapedSongList.size());
			shuffleAsset = shuffleAsset.setPager(-1, mapedSongList.size(), mapedSongList.size());
		}
		
		Shuffle shuffle = shuffleAsset.buildShuffle();
				
		return shuffle;
	}

	@Override
	public Map<String,String> addContentToUDP(String msisdn, String udpId, String toneId, String type)
			throws UserException {
		logger.info("inside addContentToUDP in UDPVoltronServiceImpl..subscriberId: " + msisdn + " udpId: "+udpId);
		
		
		if((!type.equalsIgnoreCase(AssetType.SONG.toString()) && !type.equalsIgnoreCase(AssetType.RBTUGC.toString()))) {
			UserException e = new UserException();
			e.setStatusCode(errorCodeMapping.getErrorCode(INVALID_CONTENT_TYPE).getStatusCode());
			e.setCode(errorCodeMapping.getErrorCode(INVALID_CONTENT_TYPE).getCode());
			e.setResponse(MessageResource.UDP_CONTENT_ADD_MESSAGE_FOR+"type.not.supported");
			throw e;
		}
		
		UDPRestRequest udpRestRequest = new UDPRestRequest(msisdn);
		udpRestRequest.setClipId(toneId);
		udpRestRequest.setUdpId(udpId);
		udpRestRequest.setIsDtoCRequest(true);
		
//		String response = RBTClient.getInstance().addContentToUDP(udpRestRequest);
		
		String response = String.class.cast(updService.addContentToUDP(msisdn, udpId, toneId, type));
		
		if(!response.equalsIgnoreCase("SUCCESS")) {
			
			String errorCode = errorCodeMapping.getErrorCode(response.toLowerCase()).getCode();
			String message = applicationContext.getMessage(MessageResource.UDP_CONTENT_ADD_MESSAGE_FOR + response.toLowerCase(), null, locale);
			throw new UserException(errorCode, message);
		}
		
		return constructSuccessMap(MessageResource.UDP_CONTENT_ADD_MESSAGE_FOR,response);
	}
	
	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	
	private Map<String, String> constructSuccessMap(String responseMessage, String response){
		Map<String,String> returnMap = new HashMap<String, String>(2);
		String defaultMessage = responseMessage + response.toLowerCase() +" is not configured.";
		returnMap.put("message",applicationContext.getMessage(responseMessage + response.toLowerCase(), null,defaultMessage, locale));		
		returnMap.put("code",errorCodeMapping.getErrorCode(response.toLowerCase()).getCode());
		return returnMap;
		
	}
}
