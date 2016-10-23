package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.AssetSubType;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.livewiremobile.store.storefront.dto.rbt.Schedule;
import com.livewiremobile.store.storefront.dto.rbt.Schedule.ScheduleType;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.bean.SelectionReqBean;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.daemon.RBTPlayerUpdateDaemonWrapper;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dto.SelRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.ISelectionRequest;
import com.onmobile.apps.ringbacktones.v2.resolver.response.ISelectionResponse;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * 
 * @author koyel.mahata
 *
 */

@SuppressWarnings("deprecation")
public abstract class AbstractSelectionRequestResolver implements ISelectionRequest, Constants {

	private static Logger logger = Logger.getLogger(AbstractSelectionRequestResolver.class);
	
	protected ISelectionResponse responseResolver;
	
	public void setResponseResolver(ISelectionResponse responseResolver) {
		this.responseResolver = responseResolver;
	}

	@Autowired
	private ApplicationContext applicationContext;
	private ResponseErrorCodeMapping errorCodeMapping;
	
	@Autowired
	private Boolean isSupportDirectActDct = true;
	
	// Added for OI brazil inloop workaround soln
	private Boolean selectionInLoop = false;

	public Boolean getSelectionInLoop() {
		return selectionInLoop;
	}

	public void setSelectionInLoop(Boolean selectionInLoop) {
		this.selectionInLoop = selectionInLoop;
	}

	protected enum callingpartyType{		
		DEFAULT,CALLER,GROUP;		
	};

	
	@Override
	public Map<String, String> deactivateSong(String toneId, String subscriberID, String mode) throws UserException{

		logger.info("Deactivation Request reached: subscriberId: " + subscriberID + ", mode: " + mode );
		String cType = BeanConstant.DEFAULT_ASSET_UTIL_BUILDER;
		SelectionReqBean selectionReqBean = createSelReqBeanforDeactivateSong(subscriberID,mode,toneId);
		AbstractAssetUtilBuilder builder = (AbstractAssetUtilBuilder) ConfigUtil.getBean(cType.toLowerCase());
		SelectionRequest selectionRequest = builder.buildSelectionRequestForDeleteSelection(selectionReqBean);

		if (selectionRequest.getUdpId() != null)
			RBTClient.getInstance().processUDPDeactivation(selectionRequest);
		else
			RBTClient.getInstance().deleteSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse();

		Map<String, String> map = new HashMap<String, String>(2);
		map = responseResolver.prepareDeleteSongResponse(response);
		return map;
	}

	@Override
	public PlayRuleList getPlayRules(String type, String msisdn, String id, String status) throws UserException {
		// TODO Auto-generated method stub
		return null;
	}
	


	private SelectionReqBean createSelReqBeanforDeactivateSong(String subscriberId,String mode,String toneId){
		SelectionReqBean reqBean = new SelectionReqBean();
		reqBean.setSubscriberId(subscriberId);
		reqBean.setIsDtoCRequest(true);
		reqBean.setMode(mode);
		reqBean.setIsDirectDeactivation(isSupportDirectActDct);
		reqBean.setToneID(toneId);
		return reqBean;
	}

	

	protected String getCallerId(PlayRule playRule) throws UserException {
		callingpartyType callingPartyTypeEnum = null;
		String callerId = null;
		if(playRule != null && playRule.getCallingparty()!= null && playRule.getCallingparty().getType()!=null){

			String callingPartyType = playRule.getCallingparty().getType().toString();
			try{
				callingPartyTypeEnum = callingpartyType.valueOf(callingPartyType);
			}catch(IllegalArgumentException ie){
				logger.info("Invalid calling party type so throwing exception invalid_parameter."+ie.getMessage());
				ServiceUtil.throwCustomUserException(errorCodeMapping, INVALID_PARAMETER, MessageResource.INVALID_PARAMETER_MESSAGE);			
			}
		}
		else {
			logger.info("calling party type is null so throwing exception invalid_parameter.");
			ServiceUtil.throwCustomUserException(errorCodeMapping, INVALID_PARAMETER, MessageResource.INVALID_PARAMETER_MESSAGE);		
		}

		switch(callingPartyTypeEnum) {
		case CALLER :
			long tmpCallerId = playRule.getCallingparty().getId();
			if(tmpCallerId == -1){
				callerId = "PRIVATE";
			}else{
				callerId = String.valueOf(tmpCallerId);
			}
			break;
		case GROUP	:
			callerId = "G" + String.valueOf(playRule.getCallingparty().getId());
			break;
		}

		return callerId;
	}

	protected SelectionRequest getSelectionRequest(SelRequestDTO selRequestDTO) {

		String subscriberID = null;
		PlayRule playRule = selRequestDTO.getPlayRule();
		String mode = selRequestDTO.getMode();
		String callerId = selRequestDTO.getCallerId();
		if(selRequestDTO.getSubscriber() != null) {
			subscriberID = selRequestDTO.getSubscriber().getSubscriberID();
		} else {
			subscriberID = selRequestDTO.getSubscriberId();
		}
		AssetType type = playRule.getAsset().getType();
		AssetSubType subType = playRule.getAsset().getSubtype();
		SelectionReqBean selectionReqBean = createSelReqBeanforActivateSong(subscriberID,playRule,mode,callerId);
		if(selRequestDTO.getAsset() != null) {
			type = selRequestDTO.getAsset().getType();
			selectionReqBean.setToneID(selRequestDTO.getAsset().getId()+"");
			subType = selRequestDTO.getAsset().getSubtype();
		}else if(playRule.getAsset() != null){
			selectionReqBean.setToneID(playRule.getAsset().getId()+"");
		}
		
		// added for cut rbt selection
		if(selRequestDTO.getRbtFile() != null && selRequestDTO.getRbtFile().contains("_cut_")){
			selectionReqBean.setToneID(selRequestDTO.getRbtFile());
		}
		
		setSpecialSelections(type,selectionReqBean,playRule);
		setEphimeralRBTSelection(selectionReqBean,playRule);

		if(selRequestDTO.getFirstName()!=null)
			selectionReqBean.setFirstName(selRequestDTO.getFirstName());
		if(selRequestDTO.getLastName()!=null)
			selectionReqBean.setLastName(selRequestDTO.getLastName());
		
		
		AbstractAssetUtilBuilder builder = (AbstractAssetUtilBuilder) getAssetBuilder(type,subType);
		SelectionRequest selectionRequest = builder.buildSelectionRequestForAddSelection(selectionReqBean);
		return selectionRequest;
	}


	protected SelectionReqBean createSelReqBeanforActivateSong(String subscriberId,PlayRule plarule,String mode,String callerId){
		SelectionReqBean reqBean = new SelectionReqBean();
		reqBean.setSubscriberId(subscriberId);
		reqBean.setIsDtoCRequest(true);
		reqBean.setMode(mode);
		reqBean.setIsSelDirectActivation(isSupportDirectActDct);
		if (isSupportDirectActDct) {
			reqBean.setSelectionStartTime(new Date());
		}
		if(callerId != null){
			reqBean.setCallerID(callerId);
		}
		reqBean.setToneID(plarule.getAsset().getId()+"");
		// Added for OI brazil inloop workaround soln
		reqBean.setInLoop(selectionInLoop);
		return reqBean;
	}

	private void setSpecialSelections(AssetType type,
			SelectionReqBean selectionReqBean, PlayRule playRule) {

		Schedule schedule = playRule.getSchedule();
		if (schedule.getType().equals(ScheduleType.DATETIMERANGE)) {
			setTODSelection(selectionReqBean,schedule);
		}else if(schedule.getType().equals(ScheduleType.DATETIMECONTINUOUSRANGE)){
			setFutureDateSelection(selectionReqBean,schedule);
		}else if (schedule.getType().equals(ScheduleType.PLAYRANGE)){
			setProfileHours(selectionReqBean,schedule);
		}		
	}


	private void setProfileHours(SelectionReqBean selectionReqBean,
			Schedule schedule) {
		Period playduration = new Period(schedule.getPlayDuration());
		int hours = playduration.getHours();
		int days = playduration.getDays();
		int minutes = playduration.getMinutes();
		int profileMinutes = (days * 24 * 60) + (hours * 60) + minutes;
		selectionReqBean.setProfileHours("M" + profileMinutes);
		selectionReqBean.setCategoryID("99");
	}

	
	private void setFutureDateSelection(SelectionReqBean selreqBean,
			Schedule schedule) {

		Date startDate = schedule.getDateRange().getStartDate();
		Date endDate = schedule.getDateRange().getEndDate();

		Date fromTime = schedule.getTimeRange().getFromTime();
		Date toTime = schedule.getTimeRange().getToTime();

		startDate.setHours(fromTime.getHours());
		startDate.setMinutes(fromTime.getMinutes());
		startDate.setSeconds(fromTime.getSeconds());

		endDate.setHours(toTime.getHours());
		endDate.setMinutes(toTime.getMinutes());
		endDate.setSeconds(toTime.getSeconds());

		selreqBean.setSelectionStartTime(startDate);
		selreqBean.setSelectionEndTime(endDate);

	}	

	//RBT-16269 added for profile setting
	private void setTODSelection(SelectionReqBean selreqBean,Schedule schedule) {

		Date startDate = schedule.getDateRange().getStartDate();
		Date endDate = schedule.getDateRange().getEndDate();

		selreqBean.setSelectionStartTime(startDate);
		selreqBean.setSelectionEndTime(endDate);

		Date fromTime = schedule.getTimeRange().getFromTime();

		if (fromTime != null) {
			selreqBean.setFromTime(fromTime.getHours());
			selreqBean.setFromTimeMinutes(fromTime.getMinutes());
		}

		Date toTime = schedule.getTimeRange().getToTime();

		if (toTime != null) {
			selreqBean.setToTime(toTime.getHours());
			selreqBean.setToTimeMinutes(toTime.getMinutes());
		}

	}
	
	
	//Added for ephemeral rbt
		private void setEphimeralRBTSelection(SelectionReqBean selectionReqBean,PlayRule playRule){
			if(playRule != null && playRule.getPlayruleinfo()!= null){
			 int playCount = playRule.getPlayruleinfo().getPlayCount();
			 if(playCount > 0){
			  selectionReqBean.setPlayCount(String.valueOf(playCount));
			  selectionReqBean.setStatus(200);
			  Date endDate = new Date();
			  selectionReqBean.setSelectionStartTime(new Date());
			  String days = applicationContext.getMessage("DAYS_FOR_EPHEMERAL_RBT", null, LocaleContextHolder.getLocale());
			  logger.info("Configured days for ephemeral rbt: "+days);
			  if(days != null){
				  endDate.setDate(endDate.getDate()+Integer.parseInt(days));
				  selectionReqBean.setSelectionEndTime(endDate);
			  }
			 }
			}
		}
		
		
		
		public String deleteEphemeralRBTSelection(String subscriberId, String callerId, String wavFileName , String categoryId, int status, String mode) {

			String response = "faliure";
			logger.info("deleteEphemeralRBTSelection in DeleteEphemeralServiceImpl called with subscriberId: "+ subscriberId
					+ " ,callerId : "+ callerId+ " ,wavFileName: "+ wavFileName+ " ,categoryId: "+ categoryId+ " ,status: "+ status +" , mode: "+mode);

			try {
					SubscriberSelection subscriberSelection = (SubscriberSelection) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
					ExtendedSubStatus extendedSubStatus = prepareExtendedSubStatus(subscriberId,callerId, wavFileName, categoryId,status, mode);
					boolean ephemeralDeleted = subscriberSelection.deactivateSubSelection(extendedSubStatus, isSupportDirectActDct);
					if(ephemeralDeleted){
							response = "success";
				// Toneplayer updation
				// vikrant

				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
				com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber = RBTClient
						.getInstance().getSubscriber(rbtDetailsRequest);
				ConverterHelper helper = (ConverterHelper) ConfigUtil.getBean(BeanConstant.CONVERTER_HELPER_UTIL);
				Subscriber subscriber = helper.convertClientSubscriberToContentSubscriber(clientsubscriber);

						//	Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
							if(isSupportDirectActDct && !RBTDBManager.getInstance().isSubscriberActivationPending(subscriber)){
								RBTPlayerUpdateDaemonWrapper.getInstance().removeSelectionsFromTonePlayer(subscriber);
							}
					 }
			}catch(Exception e){
				logger.info("Exception occured while deleteing ephemeral selection : "+e);
			}
			
			return response;
		}

		
		private ExtendedSubStatus prepareExtendedSubStatus(String subscriberId, String callerId, String wavFileName , String categoryId, int status, String mode){
			ExtendedSubStatus extendedSubStatus = new ExtendedSubStatus();
			if(subscriberId != null && !subscriberId.isEmpty()){
				extendedSubStatus.setSubId(subscriberId);
			}
			if(callerId != null && !callerId.isEmpty()){
				extendedSubStatus.setCallerId(callerId);
			}
			if(categoryId != null && !categoryId.isEmpty()){
				extendedSubStatus.setCategoryID(Integer.parseInt(categoryId));
			}
			if(wavFileName != null && !wavFileName.isEmpty()){
				extendedSubStatus.setSubscriberFile(wavFileName);
			}
			extendedSubStatus.setStatus(status);
			
			String deselectedBy = null;
			if(mode != null && !mode.isEmpty()){
				deselectedBy = mode;
			}else{		
				deselectedBy = RBTParametersUtils.getParamAsString("COMMON", "MODE_FOR_DELETE_EPHEMERAL_RBT", "TP");
			}
			extendedSubStatus.setDeselectedBy(deselectedBy);
			
			return extendedSubStatus;
		}

	
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	public Boolean getIsSupportDirectActDct() {
		logger.info("Debuggggg123::: " + isSupportDirectActDct);
		return isSupportDirectActDct;
	}

	public void setIsSupportDirectActDct(Boolean isSupportDirectActDct) {
		logger.info("Debuggggg123::: " + isSupportDirectActDct);
		this.isSupportDirectActDct = isSupportDirectActDct;
	}


	protected void validateEphemeralSelectionForCaller(PlayRule playRule) throws UserException {
		int playCount = -1;
		if(playRule != null && playRule.getPlayruleinfo()!= null){
			 playCount = playRule.getPlayruleinfo().getPlayCount();
		}
		if (playCount == 1) {
			logger.info("validating caller for ephemeral selection...");
			callingpartyType callingPartyTypeEnum = null;
			if (playRule != null && playRule.getCallingparty() != null
					&& playRule.getCallingparty().getType() != null) {

				String callingPartyType = playRule.getCallingparty().getType()
						.toString();
				try {
					callingPartyTypeEnum = callingpartyType
							.valueOf(callingPartyType);
				} catch (IllegalArgumentException ie) {
					logger.info("Invalid calling party type so throwing exception invalid_parameter."
							+ ie.getMessage());
					ServiceUtil.throwCustomUserException(errorCodeMapping,
							INVALID_PARAMETER,
							MessageResource.INVALID_PARAMETER_MESSAGE);
				}
			} else {
				logger.info("calling party type is null so throwing exception invalid_parameter.");
				ServiceUtil.throwCustomUserException(errorCodeMapping,
						INVALID_PARAMETER,
						MessageResource.INVALID_PARAMETER_MESSAGE);
			}

			if (!callingPartyTypeEnum.equals(callingpartyType.CALLER) || (callingPartyTypeEnum.equals(callingpartyType.CALLER)
					&& playRule.getCallingparty().getId() == -1)) {
				ServiceUtil.throwCustomUserException(errorCodeMapping,EPHEMERAL_NOT_SUPPORTED, MessageResource.ACT_SETTING_MESSAGE);
			}
		}
	}
	
	private AbstractAssetUtilBuilder getAssetBuilder(AssetType type, AssetSubType assetSubType){
		try{
		  AbstractAssetUtilBuilder assetBuilder = (AbstractAssetUtilBuilder)ConfigUtil.getBean(assetSubType.getType().toString().toLowerCase());
		  return assetBuilder;
		}catch(Exception e){
			 logger.info("Exception occured while getting asset builder by sub type so returning by type");
			 AbstractAssetUtilBuilder assetBuilder = (AbstractAssetUtilBuilder)ConfigUtil.getBean(type.toString().toLowerCase());
			 return assetBuilder;
		}
	}
	
	@Override
	public PlayRule activateSong(PlayRule playRule, String subscriberID,
			String mode) throws UserException {
		// TODO Auto-generated method stub
		return null;
	}
}
