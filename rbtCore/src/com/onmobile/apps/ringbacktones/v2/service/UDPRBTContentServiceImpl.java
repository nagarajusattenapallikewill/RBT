package com.onmobile.apps.ringbacktones.v2.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.i18n.LocaleContextHolder;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.daemon.RBTPlayerUpdateDaemonWrapper;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.logger.BasicCDRLogger;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.CommonValidation;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.processor.ISelectionProcessor;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class UDPRBTContentServiceImpl extends UDPRBTServiceImpl implements ApplicationContextAware {

	private static Logger logger = Logger.getLogger(UDPRBTServiceImpl.class);

	private ResponseErrorCodeMapping errorCodeMapping;	
	private ApplicationContext context;
	private CommonValidation commonValidation;
	private Set<String> allowedContentType;
	private Locale locale = LocaleContextHolder.getLocale();
	
	private Boolean isSupportDirectActDct = true;
	
	public Boolean getIsSupportDirectActDct() {
		return isSupportDirectActDct;
	}

	public void setIsSupportDirectActDct(Boolean isSupportDirectActDct) {
		this.isSupportDirectActDct = isSupportDirectActDct;
	}

	@Override
	public String addContentToUDP(String msisdn, String udpId,
			String toneId, String type) throws UserException { 
		logger.info("addContentToUDP method invoked");
		
		//RBT-16279
		/**
		 * It will block the unwanted type contented to get
		 * added into the UDP. Example, Profile
		 */
		if (!allowedContentType.contains(type.toUpperCase())) {
			String errorCode = errorCodeMapping.getErrorCode(INVALID_PARAMETER).getCode();
			String message = context.getMessage(MessageResource.UDP_CONTENT_ADD_MESSAGE_FOR	+ "type.not.supported", null, locale);
			throw new UserException(errorCode, message);
		}
		
		String addContentResponse = FAILURE;
		
		try {
			IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			boolean isActiveUDPId = false;
			try {
				isActiveUDPId = udpDao.isUDPActive(Integer.parseInt(udpId));
			} catch (Exception e) {
				throw new UserException(e.getMessage());
			}
			validate(Integer.parseInt(udpId), msisdn, toneId, type);
			
			if (isActiveUDPId) {
				Map<String, String> whereClauseMap = new HashMap<String,String>(2); 
				whereClauseMap.put(WebServiceConstants.param_subscriberID, msisdn);
				whereClauseMap.put(WebServiceConstants.param_udpId, udpId);
				List<SubscriberStatus> subscriberStatusList = RBTDBManager.getInstance().getDistinctActiveSelections(whereClauseMap);
				Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
				for(SubscriberStatus subStatus : subscriberStatusList) {
					HashMap<String, String> map = new HashMap<String, String>();
					int fromTimeHrs = ServiceUtil.getHrs(subStatus.fromTime()+"");
					int fromTimeMins = ServiceUtil.getMins(subStatus.fromTime()+"");
					int toTimeHrs = ServiceUtil.getHrs(subStatus.toTime()+"");
					int toTimeMins = ServiceUtil.getMins(subStatus.toTime()+"");
					
					if (msisdn != null) map.put(WebServiceConstants.param_subscriberID, msisdn);
					map.put(WebServiceConstants.param_udpId, udpId);
					map.put(WebServiceConstants.param_clipID, toneId);
					map.put(WebServiceConstants.param_selDirectActivation, isSupportDirectActDct ? WebServiceConstants.YES:WebServiceConstants.NO);
					map.put(WebServiceConstants.param_dtocRequest, WebServiceConstants.YES);
					map.put(WebServiceConstants.param_categoryID, 3+"");
					map.put(WebServiceConstants.param_inLoop, WebServiceConstants.YES);
					map.put(WebServiceConstants.param_action, WebServiceConstants.action_set);
					map.put(WebServiceConstants.param_status, subStatus.status()+"");
					map.put(WebServiceConstants.param_callerID, subStatus.callerID());
					map.put(WebServiceConstants.param_fromTime, fromTimeHrs+"");
					map.put(WebServiceConstants.param_fromTimeMinutes, fromTimeMins+"");
					map.put(WebServiceConstants.param_toTime, toTimeHrs+"");
					map.put(WebServiceConstants.param_toTimeMinutes, toTimeMins+"");
					
					WebServiceContext task = Utility.getTask(map);
					RBTAdminFacade.initialize();
					RBTProcessor rbtProcessor = RBTAdminFacade.getRBTProcessorObject(task);
					addContentResponse = rbtProcessor.processSelection(task);
					
					//Tone player add
					if(!addContentResponse.equalsIgnoreCase(SUCCESS)) {
						break;				
					}
					
					if(isSupportDirectActDct && !RBTDBManager.getInstance().isSubscriberActivationPending(subscriber)) {
						try{
							ISelectionProcessor selectionProcessor = (ISelectionProcessor) ConfigUtil.getBean(BeanConstant.SELECTION_PROCESSOR_BEAN); 
							if(selectionProcessor !=null){
							 selectionProcessor.startProcessing(task, subscriber);
						     }
						}catch(Exception e){
							logger.info("No such bean exception for :"+BeanConstant.SELECTION_PROCESSOR_BEAN);
						}
					}
				}
				
				logger.info("Add Content Response: "+addContentResponse);				
				
				
				// Added for CDR logging
				LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
				BasicCDRLogger selectionActLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.SELECTION_ACT_CDR_LOGGER_BEAN);

				if (!addContentResponse.equalsIgnoreCase(SUCCESS)) {
					logger.info("Adding content to RBT_SUBSCRIBER_SELECTIONS table failed. Response: "+addContentResponse);
					
				
					// Added for CDR logging
					SelectionRequest selectionRequest = new SelectionRequest(msisdn);
					selectionRequest.setClipID(toneId);
					selectionRequest.setUdpId(udpId);
					selectionRequest.setCategoryID("3");
					selectionRequest.setCircleID(subscriber.circleID());
					loggerDTO = selectionActLogger.getLoggerDTOForSelACTFailure(loggerDTO, msisdn, selectionRequest, AssetType.SHUFFLELIST);
					loggerDTO.setResponesStatus(addContentResponse);
					selectionActLogger.writeCDRLog(loggerDTO);
					
					throw new UserException(addContentResponse);
				}else{
					
					// Added for CDR logging
					SubscriberStatus subscriberStatus = ServiceUtil.getSubscriberLatestSelection(msisdn, null);
					loggerDTO = selectionActLogger.getLoggerDTOForSelACTSuccess(loggerDTO, subscriberStatus);
					loggerDTO.setResponesStatus(addContentResponse);
					selectionActLogger.writeCDRLog(loggerDTO);
					
				}
			}
			addContentResponse = super.addContentToUDP(msisdn, udpId, toneId, type);

		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (Exception e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getMessage(), MessageResource.UDP_CONTENT_ADD_MESSAGE_FOR);
		}
		return addContentResponse;

	}
	
	@Override
	public String deleteContentFromUDP(String msisdn,
			String udpId, String toneId,String ctype) throws UserException{
		logger.info("deleteContentFromUDP invoked");
		String deleteContentResponse = FAILURE;
		
		int udpid = -1;
		try {
			if (!commonValidation.isSubscriberActive(msisdn))
				throw new UserException(SUB_DONT_EXIST);
			
			IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			
			udpid = Integer.parseInt(udpId);
			boolean isUDPIdActive = udpDao.isUDPActive(udpid);
			logger.info("The udp id status is:" + isUDPIdActive);
			
			if (isUDPIdActive) {
				int clipId = -1;
				clipId = Integer.parseInt(toneId);
				SubscriberSelection subscriberSelection = (SubscriberSelection) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
				SubscriberStatus subscriberStatus = subscriberSelection.getSelectionByUdpIdAndClipId(udpId, clipId,msisdn, ctype);
				if (subscriberStatus != null) {
					HashMap<String, String> map = new HashMap<String, String>();

					if (msisdn != null) map.put(WebServiceConstants.param_subscriberID, msisdn);

					if (subscriberStatus.callerID() != null) map.put(WebServiceConstants.param_callerID, subscriberStatus.callerID());

					map.put(WebServiceConstants.param_udpId, udpId);
					if(ctype != null && ctype.equalsIgnoreCase("RBTUGC"))
						map.put(WebServiceConstants.param_rbtFile, subscriberStatus.subscriberFile());
					else
						map.put(WebServiceConstants.param_clipID, toneId);
					
					map.put(WebServiceConstants.param_isDirectDeactivation, isSupportDirectActDct ? WebServiceConstants.YES:WebServiceConstants.NO);
					map.put(WebServiceConstants.param_dtocRequest, WebServiceConstants.YES);
					map.put(WebServiceConstants.param_categoryID, subscriberStatus.categoryID()+"");
					map.put(WebServiceConstants.param_action, WebServiceConstants.action_deleteSetting);
					WebServiceContext task = Utility.getTask(map);
					RBTAdminFacade.initialize();
					RBTProcessor rbtProcessor = RBTAdminFacade.getRBTProcessorObject(task);
					deleteContentResponse = rbtProcessor.deleteSetting(task);
					
					
					if (!deleteContentResponse.equalsIgnoreCase(SUCCESS)) {				
						throw new Exception(deleteContentResponse);
					}else{
						//Tone player code 			
						Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
						if(isSupportDirectActDct && !RBTDBManager.getInstance().isSubscriberActivationPending(subscriber)){
							RBTPlayerUpdateDaemonWrapper.getInstance().removeSelectionsFromTonePlayer(subscriber);
						}
					}
					// Added for CDR logging
					LoggerDTO loggerDTO = (LoggerDTO) ConfigUtil.getBean(BeanConstant.CDR_LOGGER_DTO_BEAN);
					BasicCDRLogger selectionDctLogger = (BasicCDRLogger) ConfigUtil.getBean(BeanConstant.SELECTION_DCT_CDR_LOGGER_BEAN);
					loggerDTO = selectionDctLogger.getLoggerDTOForSelectionDCT(loggerDTO, subscriberStatus, msisdn);
					loggerDTO.setResponesStatus(deleteContentResponse);
					selectionDctLogger.writeCDRLog(loggerDTO);
				}
			}
			deleteContentResponse = super.deleteContentFromUDP(msisdn, udpId, toneId,ctype);
			
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch(Exception e){
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(),
					MessageResource.UDP_CONTENT_DELETE_MESSAGE_FOR);
		}
		
		logger.info("delete content from UDP response: "+deleteContentResponse);
		return deleteContentResponse;
	}
	

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		super.setErrorCodeMapping(errorCodeMapping);
		this.errorCodeMapping = errorCodeMapping;
	}
	

	@Override
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		super.setContext(context);
		this.context = context;
		
	}

	public void setCommonValidation(CommonValidation commonValidation) {
		super.setCommonValidation(commonValidation);
		this.commonValidation = commonValidation;
	}

	public Set<String> getAllowedContentType() {
		return allowedContentType;
	}

	public void setAllowedContentType(Set<String> allowedContentType) {
		this.allowedContentType = allowedContentType;
	}
	
	
	
}
