package com.onmobile.apps.ringbacktones.v2.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.bean.UDPResponseBean;
import com.onmobile.apps.ringbacktones.v2.common.CommonValidation;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.converter.UDPDOToResponseBeanConverter;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap.UDPContentKeys;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap.UDPContentKeys.Type;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPResponseBeanDO;
import com.onmobile.apps.ringbacktones.v2.dao.impl.UDPDaoImpl.UDPType;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public class UDPRBTServiceImpl implements IUDPService, Constants{
	
	
	
	private static Logger logger = Logger.getLogger(UDPRBTServiceImpl.class);
	
	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	private IUDPDao udpDao = null;
	private RBTCacheManager rbtCacheManager = null;
	private boolean isValidated = false;	
	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private CommonValidation commonValidation;
	
	
	
	public void setCommonValidation(CommonValidation commonValidation) {
		this.commonValidation = commonValidation;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}

	
	@Override
	public UDPResponseBean createUDP(String msisdn, String name,
			String mode,String extrainfo) throws UserException{

		logger.info("inside createUDP in UDPRBTServiceImpl..subscriberId: " + msisdn + ",name: "+ name +", mode: " + mode + ", artwork: " + extrainfo);
		UDPBean createUDPBean = null;
		try {
			if (!commonValidation.isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}
			UDPBean udpBean = new UDPBean();
			udpBean.setUdpName(name);
			udpBean.setExtraInfo(extrainfo);
			udpBean.setSubscriberId(msisdn);
			udpBean.setMode(mode);
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			createUDPBean = udpDao.createUDP(udpBean);
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (Exception e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(), MessageResource.UDP_CREATE_MESSAGE_FOR);
		}
		UDPResponseBean responseBean = UDPDOToResponseBeanConverter.getUDPResponseBeanFromUDPBean(createUDPBean);
		logger.info("create UDP response: "+responseBean);
		return responseBean;

	}
	
	@Override
	public String deleteUDP(String msisdn, String udpId)
			throws UserException {
		logger.info("inside deleteUDP in UDPRBTServiceImpl..subscriberId: " + msisdn + " udpId: "+udpId);
		String deleteUDPResponse = FAILURE;
		try {
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			if (!commonValidation.isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}
			int udpid = Integer.parseInt(udpId);
			boolean validUDPId = udpDao.isValidUDPId(udpid , msisdn);
			logger.info("The udp id validity status is:" + validUDPId);

			if(validUDPId){
			boolean udpActive = udpDao.isUDPActive(udpid);
			if (udpActive) {
				throw new Exception(UDP_ALREADY_ACTIVE);
			}

			boolean deleteUDP = udpDao.deleteUDP(udpid);
			if (deleteUDP) {
				deleteUDPResponse = SUCCESS;
			}
			}else{
				throw new Exception(INVALID_UDP_ID);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (Exception e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(),
					MessageResource.UDP_DELETE_MESSAGE_FOR);
		}
		logger.info("delete UDP response: "+deleteUDPResponse);
		return deleteUDPResponse;
	}


	@Override
	public UDPResponseBean updateUDP(String msisdn, String name,
			String mode, String extrainfo, String udpId) throws UserException {
		logger.info("inside updateUDP in UDPRBTServiceImpl..subscriberId: " + msisdn + ",name: "+ name +", mode: " + mode + ", extrainfo: " + extrainfo);
		
		UDPResponseBean responseBean = null;
		int udpid = -1;
		
		try {
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			if(extrainfo == null && name == null){
				throw new Exception(INVALID_PARAMETER);
			}
			
			if(!commonValidation.isSubscriberActive(msisdn)){
				throw new Exception(SUB_DONT_EXIST);
			}
		
			udpid = Integer.parseInt(udpId);
			boolean validUDPId = udpDao.isValidUDPId(udpid , msisdn);
			logger.info("The udp id validity status is:" + validUDPId);
			if (validUDPId) {
				UDPBean udpBean = udpDao.getUDPById(udpid);
				if (name != null) {
					udpBean.setUdpName(name);
				}
				if (extrainfo != null) {
					udpBean.setExtraInfo(extrainfo);
				}
				udpBean.setMode(mode);
				UDPBean updateUDPBean = udpDao.updateUDP(udpBean);
				responseBean = UDPDOToResponseBeanConverter
						.getUDPResponseBeanFromUDPBean(updateUDPBean);
			}else{
			 throw new Exception(INVALID_UDP_ID);
		    }
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (Exception e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping,e.getMessage(),
					MessageResource.UDP_UPDATE_MESSAGE_FOR);
		}
		
		logger.info("update UDP response: "+responseBean);
		return responseBean;
	}


	@Override
	public List<UDPResponseBean> getAllUDP(String msisdn, int offset,
			int pageSize) throws UserException {
		logger.info("inside getAllUDP in UDPRBTServiceImpl..subscriberId: "
				+ msisdn);
		List<UDPResponseBean> udpResponseBeanList = new ArrayList<UDPResponseBean>();
		
		try {
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			/*if (!commonValidation.isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}*/

			List<UDPBean> allUDP = udpDao.getAllUDP(msisdn, offset, pageSize);
			if (allUDP != null && allUDP.size() > 0) {
				Iterator<UDPBean> iterator = allUDP.iterator();
				while (iterator.hasNext()) {
					UDPBean udpBean = iterator.next();
					UDPResponseBean udpResponseBean = UDPDOToResponseBeanConverter
							.getUDPResponseBeanFromUDPBean(udpBean);
					udpResponseBeanList.add(udpResponseBean);
				}
			}

		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (Exception e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getMessage(), MessageResource.GET_ALL_UDP_MESSAGE_FOR);
		}
		logger.info("Response for getAll udp is :"
				+ udpResponseBeanList.toString());
		return udpResponseBeanList;
	}


	@Override
	public String addContentToUDP(String msisdn, String udpId,
			String toneId, String type) throws UserException {
		logger.info("addContentToUDP method invoked");
		String addContentResponse = FAILURE;
		rbtCacheManager = RBTCacheManager.getInstance();
		//udpDao = DAOManager.getUDPDao(DAOConstants.UDP_DAO_IMPL);

		try{
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			
			int udpid = Integer.parseInt(udpId);
			boolean validUDPId = udpDao.isValidUDPId(udpid , msisdn);
			logger.info("The udp id validity status is:" + validUDPId);
			
			if (validUDPId) {
				Clip clip = validate(udpid, msisdn, toneId,type);

				Map<String, String> whereClauseMap = new HashMap<String, String>();
				whereClauseMap.put("SUBSCRIBER_WAV_FILE",
						clip.getClipRbtWavFile());
				int rowCount = RBTDBManager.getInstance().getSubActDwnldsCount(
						msisdn, whereClauseMap);
				if (rowCount == 0) {
					throw new UserException(CLIP_NOT_IN_LIBRARY);
				}

				UDPContentMap udpContentMap = new UDPContentMap();
				UDPContentKeys contentKeys = new UDPContentKeys();
				contentKeys.setClipId(clip.getClipId());
				UDPBean udpBean = udpDao.getUDPById(udpid);
				contentKeys.setType(Type.valueOf(type));
				contentKeys.setUdpBean(udpBean);
				udpContentMap.setContentKeys(contentKeys);
				boolean isContentAdded = udpDao.addContentToUDP(udpContentMap,
						msisdn, udpBean.isSelActivated());
				if (isContentAdded) {
					addContentResponse = SUCCESS;
				}
			} else {
				throw new UserException(INVALID_UDP_ID);
			}

		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (DataAccessException e) {
			throw new UserException(e.getMessage());
		} 
		logger.info("Add content to UDP returning success: " + addContentResponse);
		return addContentResponse;

	}

	@Override
	public String deleteContentFromUDP(String msisdn,
			String udpId, String toneId, String ctype) throws UserException{
		logger.info("deleteContentFromUDP invoked");
		String deleteContentResponse = FAILURE;
		
		int udpid = -1;
		try {
			
			try{
				if(ctype == null || ctype.isEmpty() || (ctype!=null && UDPType.valueOf(ctype.toUpperCase()) == null)){
					throw new UserException(INVALID_CONTENT_TYPE);
				}
			}catch(Exception e){
				throw new UserException(INVALID_CONTENT_TYPE);
			}
				
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
			
			if (!commonValidation.isSubscriberActive(msisdn)) {
				throw new UserException(SUB_DONT_EXIST);
			}
			
			
			
			udpid = Integer.parseInt(udpId);
			boolean validUDPId = false;
			try {
				validUDPId = udpDao.isValidUDPId(udpid, msisdn);
			} catch (Exception e) {
				throw new UserException(e.getMessage());
			}
			logger.info("The udp id validity status is:" + validUDPId);
			int clipId = -1;
			if (validUDPId) {
				clipId = Integer.parseInt(toneId);
				
				boolean removeContentUDP = false;
				try {
					removeContentUDP = udpDao.removeContentUDP(msisdn,clipId,UDPType.valueOf(ctype.toUpperCase()));
				} catch (Exception e) {
					throw new UserException(e.getMessage());
				}

				if (removeContentUDP) {				
					deleteContentResponse = SUCCESS;
				}
			}else{
				throw new UserException(INVALID_UDP_ID);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} 
		
		logger.info("delete content from UDP response: "+deleteContentResponse);
		return deleteContentResponse;
	}

	@Override
	public UDPResponseBean getContentsFromUDP(String msisdn, String udpId, int offset, int pageSize) throws UserException{
		UDPResponseBean udpResponseBean = new UDPResponseBean();

		try{
			
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(msisdn);
			if(subscriber == null || RBTDBManager.getInstance().isSubscriberDeactivated(subscriber)) {
				logger.debug("Subscriber not found returning error resonse " + SUB_DONT_EXIST);
				throw new UserException(SUB_DONT_EXIST);
			}
			udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);			
			int udpid = Integer.parseInt(udpId);
			boolean validUDPId = udpDao.isValidUDPId(udpid, msisdn);
			logger.info("The udp id validity status is:" + validUDPId);
			if (validUDPId) {
				UDPResponseBeanDO responseBean = udpDao.getUDPById(udpid, true);
				List<UDPContentMap> contentMaps = responseBean.getUdpContentMaps();
				if (responseBean != null) {

					if(pageSize > 0 && offset > 0){
						contentMaps = ServiceUtil.paginatedSubList(contentMaps, pageSize, offset);
						responseBean.setUdpContentMaps(contentMaps);
					}

					udpResponseBean = UDPDOToResponseBeanConverter
							.getUDPResponseBeanFromUDPResponseBeanDO(responseBean);
					if(udpResponseBean ==null){
						throw new Exception(INVALID_UDP_ID);
					}
				}
			} else{
				throw new Exception(INVALID_UDP_ID);
			}
		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
			
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
			
		} catch(Exception e){
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(),
					MessageResource.GET_CONTENT_OF_UDP_MESSAGE_FOR);
		}

		return udpResponseBean;
	}
	
	public Clip validate(int udpId, String msisdn, String toneId, String type) throws UserException {
		rbtCacheManager = RBTCacheManager.getInstance();
		if (isValidated) {
			int clipId = -1;
			clipId = Integer.parseInt(toneId);			
			Clip clip = null;
			try {
				clip = ServiceUtil.getClip(clipId,type);
			} catch (DataAccessException e) {
				throw new UserException(CLIP_NOT_EXIST);
			}
			return clip;
		}
		logger.info("Validating UDP_ID: "+udpId+" MSISDN: "+msisdn+" TONE_ID: "+toneId);
		
		if (!commonValidation.isSubscriberActive(msisdn)) 
			throw new UserException(SUB_DONT_EXIST);
		
		udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
		boolean validUDPId = false;
		try {
			validUDPId = udpDao.isValidUDPId(udpId , msisdn);
		} catch (Exception e) {
			throw new UserException(e.getMessage());
		}
		logger.info("The udp id validity status is:" + validUDPId);
		
		int clipId = -1;
		clipId = Integer.parseInt(toneId);
		
		Clip clip = null;
		try {
			clip = ServiceUtil.getClip(clipId,type);
		} catch (DataAccessException e) {
			logger.info("Not a valid clip.");
		}
		if (clip == null) {
			logger.info("Clip not found");
			throw new UserException(CLIP_NOT_EXIST);
		}
		if (clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().before(new Date())) {
			logger.info("Clip expired");
			throw new UserException(CLIP_EXPIRED);
		}
		isValidated = true;
		return clip;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}
	
}
