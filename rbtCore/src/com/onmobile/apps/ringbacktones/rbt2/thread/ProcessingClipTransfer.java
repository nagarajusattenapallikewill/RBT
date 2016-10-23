package com.onmobile.apps.ringbacktones.rbt2.thread;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.db.IClipStatusMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.db.IOperatorCircleMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.db.IWavFileMappingDAO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.PropertyConfig;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.bean.UDPResponseBean;
import com.onmobile.apps.ringbacktones.v2.common.URLParamConstants;
import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping;
import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping.CompositeKey;
import com.onmobile.apps.ringbacktones.v2.dao.bean.OperatorCircleMapping;
import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;
import com.onmobile.apps.ringbacktones.v2.service.IUDPService;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.URLBuilder;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


public class ProcessingClipTransfer implements Runnable,WebServiceConstants,URLParamConstants {

	
	private Subscriber subscriber;
	private String clipId;
	private String udpId;
	private String categoryId;
	private static RBTHttpClient rbtHttpClient = null;
	private static final Map<Integer, OperatorCircleMapping> operatorCircleMapById;
	private static final Map<String,Integer> operatorCircleMap;
	private static Logger logger = Logger.getLogger(ProcessingClipTransfer.class);
	private ClipStatusMapping statusMapping;
	private OperatorCircleMapping circleMapping;
	private Clip clip;
	private static ResourceBundle resourceBundle = null;

	public ProcessingClipTransfer() {
		
	}
	
	
	public ProcessingClipTransfer(ClipStatusMapping statusMapping,OperatorCircleMapping circleMapping, Clip clip) {
		this.statusMapping = statusMapping;
		this.circleMapping = circleMapping;
		this.clip = clip;
	}
	
		
	static {

		IOperatorCircleMappingDAO circleMappingDAO = (IOperatorCircleMappingDAO) ConfigUtil.getBean(BeanConstant.OPERATOR_CIRCLE_MAPPING_DAO);
		List<OperatorCircleMapping> operatorCircleMappings = circleMappingDAO.getOperatorCircleMapping();
		operatorCircleMapById = new HashMap<Integer, OperatorCircleMapping>(operatorCircleMappings.size());
		operatorCircleMap = new HashMap<String, Integer>(operatorCircleMappings.size());
		if(operatorCircleMappings != null && !operatorCircleMappings.isEmpty()) {
			for(OperatorCircleMapping circleMapping : operatorCircleMappings) {
				operatorCircleMapById.put(circleMapping.getId(), circleMapping);
				operatorCircleMap.put((circleMapping.getOperatorName().trim()+"_"+circleMapping.getCircleId().trim()).toUpperCase(), circleMapping.getId());
			}
			
		}
		
		PropertyConfig config = (PropertyConfig)ConfigUtil.getBean(BeanConstant.PROPERTY_CONFIG);
		resourceBundle = config.loadBundle("config");
		int connectionTimeout = Integer.parseInt(config.getValueFromResourceBundle(resourceBundle, "CONNECTION_TIMEOUT", "3000"));
		int socketTimeout = Integer.parseInt(config.getValueFromResourceBundle(resourceBundle, "SOCKET_TIMEOUT", "3000"));
		int maxTotalConnection = Integer.parseInt(config.getValueFromResourceBundle(resourceBundle, "MAX_TOTAL_CONNECTION", "5"));
		int maxHostConnection = Integer.parseInt(config.getValueFromResourceBundle(resourceBundle, "MAX_HOST_CONNECTION", "5"));
		int proxyPort = Integer.parseInt(config.getValueFromResourceBundle(resourceBundle, "PROXY_PORT", "-1"));
		String proxyHost = config.getValueFromResourceBundle(resourceBundle, "PROXY_HOST", null);
		
		HttpParameters httpParam = new HttpParameters();
		httpParam.setMaxTotalConnections(maxTotalConnection);
		httpParam.setMaxHostConnections(maxHostConnection);
		httpParam.setConnectionTimeout(connectionTimeout);
		httpParam.setSoTimeout(socketTimeout);
		httpParam.setProxyHost(proxyHost);
		httpParam.setProxyPort(proxyPort);

		rbtHttpClient = new RBTHttpClient(httpParam);
	}

	@Override
	public void run() {
		if(statusMapping != null && circleMapping != null && clip != null) {
			startClipTransferProcess(statusMapping, circleMapping, clip);
			} 
		else {
			processClipTransfer();
		}

	}

	public void processClipTransfer() {
		logger.info("Processing Clip Transfer");
		try {
			String operatorName = ServiceUtil.getOperatorName(subscriber).trim();
			String circleId = ServiceUtil.getCircleId(subscriber).trim();
			logger.info("CircleId: "+circleId+", SubcriberId: "+subscriber.subID()+", Operator: "+operatorName);
			Integer operatorId = operatorCircleMap.get((operatorName+"_"+circleId).toUpperCase());
			if(operatorId == null) {
				logger.info("Operator circlemapping " + (operatorName+"_"+circleId).toUpperCase() + "not exist");
				return;
			}
			OperatorCircleMapping circleMapping = operatorCircleMapById.get(operatorId);
			
			if(circleMapping == null) {
				logger.info("Operator circlemapping operatorId: " + operatorId + " not exist");
				return;
			}
			ClipStatusMapping statusMapping = null;
			if(udpId != null && !udpId.isEmpty()) {
				IUDPService udpService = (IUDPService) ConfigUtil.getBean(BeanConstant.UDP_RBT_SERVICE_IMPL);
				UDPResponseBean responseBean = (UDPResponseBean) udpService.getContentsFromUDP(subscriber.subID(), udpId, -1, -1);
				List<Clip> clips = responseBean.getClips();
				if(clips != null && !clips.isEmpty()) {
					for(Clip clip: clips) {
						statusMapping = getClipStatusMappingObj(circleMapping.getId(),clip.getClipId()+"");
						if(isClipTransferRequired(statusMapping)) {
							startClipTransferProcess(statusMapping, circleMapping, clip);
						}
					}
				}
			} else {
				if(categoryId != null && !categoryId.isEmpty()) {
					Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryId));
					if(Utility.isShuffleCategory(category.getCategoryTpe())) {
						Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(categoryId));
						if(clips != null && clips.length != 0) {
							for(Clip clip: clips) {
								statusMapping = getClipStatusMappingObj(circleMapping.getId(),clip.getClipId()+"");
								if(isClipTransferRequired(statusMapping)) {
									startClipTransferProcess(statusMapping, circleMapping, clip);
								}
							}
						}
					} else {
						if(clipId != null && !clipId.isEmpty()) {
							statusMapping = getClipStatusMappingObj(circleMapping.getId(),clipId);
							Clip clipObj = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
							if(isClipTransferRequired(statusMapping)) {
								startClipTransferProcess(statusMapping, circleMapping, clipObj);
							}
						} 
					}
				} else if(clipId != null && !clipId.isEmpty()) {
					statusMapping = getClipStatusMappingObj(circleMapping.getId(),clipId);
					Clip clipObj = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
					if(isClipTransferRequired(statusMapping)) {
						startClipTransferProcess(statusMapping, circleMapping, clipObj);
					}
				} 
			}
		}catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (Exception e) {
			logger.error("Exception Occured: "+e,e);
		}
	}

	private ClipStatusMapping getClipStatusMappingObj(int operatorId, String clipId) {
		IClipStatusMappingDAO statusMappingDAO = (IClipStatusMappingDAO) ConfigUtil.getBean(BeanConstant.CLIP_STATUS_MAPPING_DAO);
		return statusMappingDAO.getClipStatusMappingByOperatorId(operatorId,Integer.parseInt(clipId));
	}

	private boolean isClipTransferRequired(ClipStatusMapping statusMapping) {
		boolean isTransferReqd = true;
		if(statusMapping != null && statusMapping.getStatus() == 1) {
			logger.info("Clip "+statusMapping.getCompositeKey().getClipId()+" already transferred");
			isTransferReqd = false;
		}
		return isTransferReqd;
	}

	public void startClipTransferProcess(ClipStatusMapping statusMapping,OperatorCircleMapping circleMapping, Clip clip) {


		if (clip == null) {
			logger.info("Clip not found or wrong clip ID: response : " + FAILED);
			return;
		}

		String circleId = circleMapping.getCircleId();
		String operatorName = circleMapping.getOperatorName();
		Map<String, String> params = new HashMap<String, String>();
		params.put(CIRCLE, circleId);
		params.put(OPERATOR_NAME, operatorName);
		params.put(WAV_FILE_NAME, clip.getClipRbtWavFile());			
		params.put(param_clipID, clip.getClipId()+"");
		prepareRequest(params, statusMapping, circleMapping);
	}


	public void prepareRequest(Map<String, String> params, ClipStatusMapping statusMapping,OperatorCircleMapping circleMapping) {

		String operatorName = params.get(OPERATOR_NAME);
		String response = null;
		String wavFileVerTwo = params.get(WAV_FILE_NAME);
		String clipId = params.remove(param_clipID);

		params.put(PRESENT, "false");
		IWavFileMappingDAO wavFileMappingDAO = (IWavFileMappingDAO) ConfigUtil.getBean(BeanConstant.WAV_FILE_MAPPING_DAO);
		WavFileMapping wavFileMapping = wavFileMappingDAO.getWavFileVerOne(wavFileVerTwo, operatorName);
		if(wavFileMapping != null && wavFileMapping.getWavFileVerOne() != null && !wavFileMapping.getWavFileVerOne().isEmpty()) {
			logger.info("Mapping found in WavFileMapping table. WavFileVerTwo: "+wavFileVerTwo+" is mapped with: "+wavFileMapping.getWavFileVerOne());
			params.put(WAV_FILE_NAME, wavFileMapping.getWavFileVerOne());
			params.put(PRESENT, "true");
		}
		String url = ServiceUtil.getClipTransferUrl(operatorName);
		url = buildClipTransferUrl(params, url);
		response = makePostRequest(url);			
		logger.info("Response got after post request: "+response);
		processDBQuery(statusMapping, circleMapping, response, clipId);
	}

	private void processDBQuery(ClipStatusMapping statusMapping, OperatorCircleMapping circleMapping, String response, String clipId) {
		logger.info("Going to process ClipStatusMapping query");
		IClipStatusMappingDAO statusMappingDAO = (IClipStatusMappingDAO) ConfigUtil.getBean(BeanConstant.CLIP_STATUS_MAPPING_DAO);
		CompositeKey key = null;
		int status = 0;
		if(response != null && response.toUpperCase().contains("SUCCESS")) {
			status = 1;
		}
		else {
			status = 0;
		}
		
		if(statusMapping == null) {
			statusMapping = new ClipStatusMapping();
			statusMapping.setStatus(status);
			key = getCompositeKey(circleMapping, clipId);
			statusMapping.setCompositeKey(key);
			logger.info("Going to insert ClipStatusMapping with clip status as: "+statusMapping.getStatus());

			statusMappingDAO.saveClipStatusMapping(statusMapping);
		}else {												
			logger.info("Going to update ClipStatusMapping clip Status");
			statusMapping.setStatus(status);
			statusMappingDAO.updateClipStatusMapping(statusMapping);
		}
	}

	private String makePostRequest(String url) {
		String response = null;
		logger.info("URL: "+url);
		if(url != null && !url.isEmpty()) {

			logger.info("Going to make Post Request");
			HttpResponse httpResponse = null;
			try {
				httpResponse = rbtHttpClient.makeRequestByPost(
						url, null, null);
			} catch (HttpException e) {
				logger.error("Exception Occured: "+e,e);
			} catch (IOException e) {
				logger.error("Exception Occured: "+e,e);
			}
			if(httpResponse != null)
				response = httpResponse.getResponse();
		}
		return response;
	}


	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}

	public void setClipId(String clipId) {
		this.clipId = clipId;
	}

	public void setUdpId(String udpId) {
		this.udpId = udpId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	private String buildClipTransferUrl(Map<String,String> params, String url) {
		URLBuilder urlBuilder = new URLBuilder(url);
		urlBuilder = urlBuilder.replaceOperator(params.get(OPERATOR_NAME))
				.replacePresentFlag(params.get(PRESENT))
				.replaceCircle(params.get(CIRCLE))
				.replaceWavFileName(params.get(WAV_FILE_NAME));
		return urlBuilder.buildUrl();
	}


	private CompositeKey getCompositeKey(OperatorCircleMapping circleMapping, String clipId) {
		CompositeKey key = new CompositeKey();
		key.setOperatorCircleMapping(circleMapping);
		key.setClipId(Integer.parseInt(clipId));
		return key;
	}


	public static Map<Integer, OperatorCircleMapping> getOperatorcirclemapbyid() {
		return operatorCircleMapById;
	}


	public static Map<String, Integer> getOperatorcirclemap() {
		return operatorCircleMap;
	}

	
}
