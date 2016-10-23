package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.service.IRBTSelectionService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.CommonValidation;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

@Service(value=BeanConstant.RBT_SELECTION_SERVICE_IMPL)
@Lazy(value = true)
public class RBTSelectionServiceImpl implements IRBTSelectionService,Constants {

	@Autowired
	private CommonValidation commonValidation;
	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	private static Logger logger = Logger.getLogger(RBTSelectionServiceImpl.class);
		
	
	@Override
	public List<Setting> getSettings(String type, String msisdn, String id, String status) throws UserException {

		List<Setting> settingList = null;
		ExtendedSubStatus extendedSubStatus = null;
		try {
			if (!commonValidation.isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}		

			SubscriberSelection subscriberSelection = (SubscriberSelection) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
			extendedSubStatus = new ExtendedSubStatus();
			extendedSubStatus.setType(type);
			extendedSubStatus.setSubId(msisdn);

			if (id != null && !id.isEmpty() && (type == null || type.isEmpty())) 
				throw new Exception(INVALID_PARAMETER);
			else if(id != null){
				if (type.equalsIgnoreCase(CallingPartyType.GROUP.toString()))	
					id = "G"+id;

				extendedSubStatus.setCallerId(id);
			}

			//Adding status in query
			if(status != null && !status.isEmpty() && status.equalsIgnoreCase("deactive")){
				extendedSubStatus.setSelStatus("deactive");
			}else if(status != null && !status.isEmpty() && status.equalsIgnoreCase("all")){
				extendedSubStatus.setSelStatus("all");
			}else {
				extendedSubStatus.setSelStatus("active");
			}
			
			List<ExtendedSubStatus> extendedSubStatusList = subscriberSelection.getAllSelectionsByRestrictions(extendedSubStatus);
			settingList = getSettingList(extendedSubStatusList);

			if (settingList == null)
				throw new Exception(PLAY_RULE_DONT_EXIST);

		} catch (IllegalArgumentException e) {
			logger.error("Exception Occured: "+e,e);
		} catch (NoSuchBeanDefinitionException e) {
			logger.error("Exception Occured while getting bean Object from Spring: "+e,e);
			ServiceUtil.throwCustomUserException(errorCodeMapping,
					e.getBeanName(), MessageResource.BEAN_CONFIGURATION_ERROR_MESSAGE);
		} catch (Exception e) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, e.getMessage(), MessageResource.LIST_PLAY_RULE_MESSAGE);
		}
		return settingList;
	}
	
	private List<Setting> getSettingList(List<ExtendedSubStatus> subscriberStatusList) throws Exception {
		
		List<Setting> settingList = null;
		if (subscriberStatusList != null && !subscriberStatusList.isEmpty()) {
			ConverterHelper helper = (ConverterHelper) ConfigUtil
					.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
			settingList = helper
					.convertSubStatusListToSettingList(subscriberStatusList);
			
			return settingList;
		} else {
			throw new Exception(PLAY_RULE_DONT_EXIST);
		}
		
	}
	
}
