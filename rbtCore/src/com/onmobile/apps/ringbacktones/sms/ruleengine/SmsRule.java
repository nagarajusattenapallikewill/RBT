package com.onmobile.apps.ringbacktones.sms.ruleengine;

import org.easyrules.core.BasicRule;

import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;

public class SmsRule extends BasicRule implements Constants{

    private String taskAction;

    Processor processor;
    
    Task task;
    
    public String getTaskAction() {
		return taskAction;
	}

	public void setTaskAction(String taskAction) {
		this.taskAction = taskAction;
	}

	public Processor getProcessor() {
		return processor;
	}

	public void setProcessor(Processor processor) {
		this.processor = processor;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	@Override
    public boolean evaluate() {
    	if (taskAction.equalsIgnoreCase(REATILER_ACT_N_SEL_FEATURE))
			processor.processRetailerActnSel(task);
		if (taskAction
				.equalsIgnoreCase(CONFIRM_SUBSCRIPTION_N_COPY_FEATURE))
			processor.processConfirmSubscriptionNCopy(task);
		if (taskAction.equalsIgnoreCase(ACTIVATE_N_SELECTION))
			processor.processActNSel(task);
		else if (taskAction.equalsIgnoreCase(ACTIVATION_KEYWORD))
			processor.processActivationRequest(task);
		else if (taskAction.equalsIgnoreCase(CRICKET_KEYWORD))
			processor.processCricket(task);
		else if (taskAction.equalsIgnoreCase(SONG_CODE_REQUEST_KEYWORD))
			processor.processSongCodeRequest(task);
		else if (taskAction.equalsIgnoreCase(REFERRAL_KEYWORD))
			processor.processReferral(task);
		else if (taskAction.equalsIgnoreCase(NEWS_AND_BEAUTY_FEED_KEYWORD))
			processor.processNewFeed(task);
		else if (taskAction
				.equalsIgnoreCase(VIEW_SUBSCRIPTION_STATISTICS_KEYWORD))
			processor.viewSubscriptionStatistics(task);
		else if (taskAction.equalsIgnoreCase(PROMOTION1))
			processor.processPromotion1(task);
		else if (taskAction.equalsIgnoreCase(PROMOTION2))
			processor.processPromotion2(task);
		else if (taskAction.equalsIgnoreCase(SONG_PROMOTION1))
			processor.processSongPromotion1(task);
		else if (taskAction.equalsIgnoreCase(SONG_PROMOTION2))
			processor.processSongPromotion2(task);
		else if (taskAction.equalsIgnoreCase(SEL_KEYWORD1))
			processor.processSel1(task);
		else if (taskAction.equalsIgnoreCase(SEL_KEYWORD2))
			processor.processSel2(task);
		else if (taskAction.equalsIgnoreCase(DEACTIVATION_KEYWORD))
			processor.processDeactivation(task);
		else if (taskAction.equalsIgnoreCase(UPGRADE_ON_DELAY_DEACTIVATION_KEYWORD))
			processor.processDelayDeactivation(task);
		else if (taskAction.equalsIgnoreCase(RMVCALLERID_KEYWORD))
			processor.processremoveCallerIDSel(task);
		else if (taskAction
				.equalsIgnoreCase(TEMPORARY_OVERRIDE_CANCEL_MESSAGE))
			processor.processRemoveTempOverride(task);
		else if (taskAction.equalsIgnoreCase(NAV_DEACT_KEYWORD))
			processor.processRemoveNavraatri(task);
		else if (taskAction.equalsIgnoreCase(MANAGE_DEACT_KEYWORD))
			processor.processManageRemoveSelection(task);
		else if (taskAction.equalsIgnoreCase(SMS_DOUBLE_CONFIRMATION))
			processor.confirmActNSel(task);
		else if (taskAction.equalsIgnoreCase(PROCESS_DOUBLE_CONFIRMATION))
			processor.processDoubleConfirmation(task);
		else if (taskAction.equalsIgnoreCase(DEACT_DOWNLOAD_KEYWORD))
			processor.processDeactivateDownload(task);
		else if (taskAction.equalsIgnoreCase(CATEGORY_SEARCH_KEYWORD))
			processor.processCategorySearch(task);
		else if (taskAction.equalsIgnoreCase(REQUEST_RBT_KEYWORD))
			processor.processREQUEST(task);
		else if (taskAction.equalsIgnoreCase(AZAAN_REQUEST_RBT_KEYWORD))
			processor.processAzaanSearchRequest(task);
		else if (taskAction.equalsIgnoreCase(REQUEST_OPTIN_RBT_KEYWORD))
			processor.confirmRequestActNSel(task);
		else if (taskAction.equalsIgnoreCase(REQUEST_MORE_KEYWORD))
			processor.getMoreClips(task);
		else if (taskAction.equalsIgnoreCase(AZAAN_REQUEST_MORE))
			processor.getMoreAzaan(task);
		else if (taskAction.equalsIgnoreCase(CONTEST_INFLUENCER_KEYWORD))
			processor.processInfluencerOptin(task);
		else if (taskAction.equalsIgnoreCase(CANCELCOPY_KEYWORD))
			processor.processCancelCopyRequest(task);
		else if (taskAction.equalsIgnoreCase(COPY_CONFIRM_KEYWORD))
			processor.processConfirmCopyRequest(task);
		else if (taskAction.equalsIgnoreCase(COPY_CANCEL_KEYWORD))
			processor.processCancelOptInCopy(task);
		else if (taskAction.equalsIgnoreCase(COPY_KEYWORDS))
			processor.processCOPY(task);
		else if (taskAction.equalsIgnoreCase(GIFT_KEYWORD))
			processor.getGift(task);
		else if (taskAction.equalsIgnoreCase(POLLON_KEYWORD))
			processor.proceesPOLLON(task);
		else if (taskAction.equalsIgnoreCase(POLLOFF_KEYWORD))
			processor.processPollOFF(task);
		else if (taskAction.equalsIgnoreCase(SET_NEWSLETTER_ON_KEYWORDS))
			processor.setNewsletterOn(task);
		else if (taskAction.equalsIgnoreCase(SET_NEWSLETTER_OFF))
			processor.setNewsLetterOff(task);
		else if (taskAction.equalsIgnoreCase(DISABLE_INTRO))
			processor.processDisableIntro(task);
		else if (taskAction.equalsIgnoreCase(DISABLE_OVERLAY_KEYWORD))
			processor.processDisableOverlay(task);
		else if (taskAction.equalsIgnoreCase(ENABLE_OVERLAY_KEYWORD))
			processor.processEnableOverlay(task);
		else if (taskAction.equalsIgnoreCase(WEEKLY_TO_MONTHLY_CONVERSION))
			processor.processWeekToMonthConversion(task);
		else if (taskAction.equalsIgnoreCase(RENEW_KEYWORD))
			processor.processRenew(task);
		else if (taskAction.equalsIgnoreCase(TNB_KEYWORDS))
			processor.processTNB(task);
		else if (taskAction.equalsIgnoreCase(VIRAL_KEYWORD))
			processor.processViralAccept(task);
		else if (taskAction.equalsIgnoreCase(WEB_REQUEST_KEYWORD))
			processor.processWebRequest(task);
		else if (taskAction.equalsIgnoreCase(MGM_ACCEPT_KEY))
			processor.processMgmAccept(task);
		else if (taskAction.equalsIgnoreCase(RETAILER_REQ_RESPONSE))
			processor.processRetailerAccept(task);
		else if (taskAction.equalsIgnoreCase(RETAILER_FEATURE))
			processor.processRetailer(task);
		else if (taskAction.equalsIgnoreCase(MGM_FEATURE))
			processor.processMGM(task);
		else if (taskAction.equalsIgnoreCase(LISTEN_KEYWORD))
			processor.processListen(task);
		else if (taskAction.equalsIgnoreCase(HELP_KEYWORD))
			processor.processHelp(task);
		else if (taskAction.equalsIgnoreCase(SONGOFMONTH))
			processor.processSongOfMonth(task);
		else if (taskAction.equalsIgnoreCase(DOWNLOADS_LIST_KEYWORD))
			processor.processDownloadsList(task);
		else if (taskAction.equalsIgnoreCase(TNB_KEYWORD))
			processor.processTNBActivation(task);
		else if (taskAction.equalsIgnoreCase(MANAGE_KEYWORD))
			processor.processManage(task);
		else if (taskAction.equalsIgnoreCase(LIST_PROFILE_KEYWORD))
			processor.processListProfile(task);
		else if (taskAction.equalsIgnoreCase(NEXT_PROFILE_KEYWORD))
			processor.getNextProfile(task);
		else if (taskAction.equalsIgnoreCase(SCRATCH_CARD_FEATURE))
			processor.processScratchCard(task);
		else if (taskAction.equalsIgnoreCase(GIFTCOPY_FEATURE))
			processor.processGiftCopy(task);
		else if (taskAction.equalsIgnoreCase(MERIDHUN_KEYWORD))
			processor.processMeriDhun(task);
		else if (taskAction.equalsIgnoreCase(DOWNLOAD_OPTIN_RENEWAL))
			processor.processDownloadOptinRenewal(task);
		else if (taskAction.equalsIgnoreCase(INIT_GIFT_KEYWORD))
			processor.processInitGift(task);
		else if (taskAction.equalsIgnoreCase(INIT_GIFT_CONFIRM_KEYWORD))
			processor.processInitGiftConfirm(task);
		else if (taskAction.equalsIgnoreCase(action_retailer_request))
			processor.processRetailerRequest(task);
		else if (taskAction.equalsIgnoreCase(action_retailer_search))
			processor.processRetailerSearch(task);
		else if (taskAction.equalsIgnoreCase(action_retailer_accept))
			processor.processRetailerAccept(task);
		else if (taskAction.equalsIgnoreCase(action_activate))
			processor.processActivation(task);
		else if (taskAction.equalsIgnoreCase(action_deactivate))
			processor.processDeactivation(task);
		else if (taskAction.equalsIgnoreCase(action_selection))
			processor.processSelection(task);
		else if (taskAction.equalsIgnoreCase(action_help))
			processor.processHelpRequest(task);
		else if (taskAction.equalsIgnoreCase(action_cat_search))
			processor.processCategorySearch(task);
		else if (taskAction.equalsIgnoreCase(action_viral))
			processor.processViralRequest(task);
		else if (taskAction.equalsIgnoreCase(action_loop))
			processor.processLoop(task);
		else if (taskAction.equalsIgnoreCase(action_delete))
			processor.processDelete(task);
		else if (taskAction.equalsIgnoreCase(action_obd))
			processor.processOBDRequest(task);
		else if (taskAction.equalsIgnoreCase(action_feed))
			processor.processFeed(task);
		else if (taskAction.equalsIgnoreCase(action_profile))
			processor.processProfile(task);
		else if (taskAction.equalsIgnoreCase(action_list_profiles))
			processor.processListProfiles(task);
		else if (taskAction.equalsIgnoreCase(action_list_next_profiles))
			processor.getNextProfile(task);
		else if (taskAction.equalsIgnoreCase(action_remove_profile))
			processor.processRemoveProfile(task);
		else if (taskAction.equalsIgnoreCase(action_clip_promo))
			processor.processClipByPromoID(task);
		else if (taskAction.equalsIgnoreCase(action_category_promo))
			processor.processCategoryByPromoID(task);
		else if (taskAction.equalsIgnoreCase(action_clip_alias))
			processor.processClipByAlias(task);
		else if (taskAction.equalsIgnoreCase(action_category_alias))
			processor.processCategoryByAlias(task);
		else if (taskAction.equalsIgnoreCase(action_default_search))
			processor.processDefaultSearch(task);
		else if (taskAction.equalsIgnoreCase(action_copy_cancel))
			processor.processCancelCopyRequest(task);
		else if (taskAction.equalsIgnoreCase(action_copy_confirm))
			processor.processConfirmCopyRequest(task);
		else if (taskAction.equalsIgnoreCase(action_optin_copy_cancel))
			processor.processCancelOptInCopy(task);
		//wrong key press get the recommended songs
		else if (taskAction.equalsIgnoreCase(SMS_RECOMMEND_SONGS_KEYWORD))
			processor.processSMSRecommendSongs(task);
		// RBT Like Feature
		else if (taskAction.equalsIgnoreCase(RBT_LIKE_CONFIRM_KEYWORD))
			processor.processConfirmLikeRequest(task);
		else if (taskAction.equalsIgnoreCase(SUSPENSION_KEYWORD))
			// Added for IDEA volunary suspension
			processor.processSuspensionRequest(task);
		else if (taskAction.equalsIgnoreCase(RESUMPTION_KEYWORD))
			processor.processResumptionRequest(task);
		else if (taskAction.equalsIgnoreCase(BLOCK_KEYWORD))
			// Added for Vodafone Block feature
			processor.processBlockRequest(task);
		else if (taskAction.equalsIgnoreCase(UNBLOCK_KEYWORD))
			processor.processUnblockRequest(task);
		else if (taskAction.equalsIgnoreCase(PACK_KEYWORD))
			processor.processSongPackRequest(task);
		else if (taskAction.equalsIgnoreCase(SONG_PACK_KEYWORD))
			// Added for BSNL song packs
			processor.processSpecialSongPackRequest(task);
		else if (taskAction.equalsIgnoreCase(CONFIRM_CHARGE_KEYWORD))
			// Added by Sreekar for ACWM opt-in
			processor.processConfirmCharge(task);
		else if (taskAction.equalsIgnoreCase(LOCK_KEYWORD))
			processor.processLockRequest(task);
		else if (taskAction.equalsIgnoreCase(UNLOCK_KEYWORD))
			processor.processUnlockRequest(task);
		else if (taskAction.equalsIgnoreCase(LIST_CATEGORIES_KEYWORD))
			// Added for SuperHit Album List
			processor.processListCategories(task);
		else if (taskAction.equalsIgnoreCase(EMOTION_KEYWORD))
			processor.processEmotionSongRequest(task);
		else if (taskAction.equalsIgnoreCase(EMOTION_DCT_KEYWORD))
			processor.processDeactEmotionRbtService(task);
		else if (taskAction.equalsIgnoreCase(EMOTION_EXTEND_KEYWORD))
			processor.processExtendEmotionRequest(task);
		// added by sreekar for tata cdma
		else if (taskAction.equalsIgnoreCase(action_trial))
			processor.processTrial(task);
		else if (taskAction.equalsIgnoreCase(action_trialReply))
			processor.processTrialReply(task);
		else if (taskAction.equalsIgnoreCase(UDS_ENABLE))
			processor.processEnableUdsRequest(task);
		else if (taskAction.equalsIgnoreCase(UDS_DISABLE))
			processor.processDisableUdsRequest(task);
		else if (taskAction.equalsIgnoreCase(CHURN_OFFER))
			processor.processChurnOffer(task);
		else if (taskAction.equalsIgnoreCase(RDC_SEL_KEYWORD))
			processor.processRDCViralSelection(task);
		else if (taskAction.equalsIgnoreCase(DISCOUNTED_SEL_KEYWORD))
			processor.processDiscountedSelection(task);
		else if (taskAction.equals(CONSENT_YES_KEYWORD)
				|| taskAction.equals(CONSENT_NO_KEYWORD))
			processor.processChargingConsentRequest(task);
		else if (taskAction.equalsIgnoreCase(CP_SEL_CONFIRM_KEYWORD))
			processor.processCPSelectionConfirm(task);
		else if (taskAction.equalsIgnoreCase(VOUCHER_KEYWORD))
			processor.processVoucherRequest(task);
		else if (taskAction.equalsIgnoreCase(UPGRADE_SEL_KEYWORD))
			processor.processUpgradeSelRequest(task);
		else if (taskAction.equalsIgnoreCase(GIFT_ACCEPT_KEYWORD))
			processor.processGiftAccept(task);
		if (taskAction.equalsIgnoreCase(MOBILE_REGISTRATION))
			processor.processRegistraionSMS(task);
		// else if (taskAction.equalsIgnoreCase(GIFT_DOWNLOAD_KEYWORD))
		// processor.processGiftDownload(task);
		else if (taskAction.equalsIgnoreCase(GIFT_REJECT_KEYWORD)) {
			processor.processGiftReject(task);
		} else if (taskAction.equalsIgnoreCase(MUSIC_PACK_KEYWORD)) {
			// RBT-4549: SMS Activation of music pack implemented for the
			// Tf-Spain
			processor.processMusicPack(task);
		} else if (taskAction.equalsIgnoreCase(RECHARGE_SMS_OPTOUT_KEYWORD)) {
			processor.processOptOutRequest(task);
		} else if (taskAction.equalsIgnoreCase(BASE_UPGRADATION_KEYWORD)) {
			processor.processBaseUpgradationRequest(task);
		} else if (taskAction.equalsIgnoreCase(PRE_GIFT_KEYWORD)) {
			processor.processPreGift(task);
		} else if (taskAction.equalsIgnoreCase(PRE_GIFT_CONFIRM_KEYWORD)) {
			processor.processPreGiftConfirm(task);
		} else if (taskAction.equalsIgnoreCase(VIRAL_START_KEYWORD)) {
			processor.processViralStart(task);
		} else if (taskAction.equalsIgnoreCase(VIRAL_STOP_KEYWORD)) {
			processor.processViralStop(task);
		} else if (taskAction.equalsIgnoreCase(LOTTERY_LIST_KEYWORD)) {
			processor.processLotteryListRequest(task);
		} else if (taskAction.equalsIgnoreCase(RANDOMIZE_KEYWORD)) {
			processor.enableRandomization(task);
		} else if (taskAction.equalsIgnoreCase(UNRANDOMIZE_KEYWORD)) {
			processor.disableRandomization(task);
		} else if (taskAction.equalsIgnoreCase(VIRAL_OPTOUT_KEYWORD)) {
			processor.processViralOptOutRequest(task);
		} else if (taskAction.equalsIgnoreCase(DOWNLOAD_SET_KEYWORD)) {
			processor.processDownloadSetRequest(task);
		} else if (taskAction.equalsIgnoreCase(VIRAL_OPTIN_KEYWORD)) {
			processor.processViralOptInRequest(task);
		} else if (taskAction.equalsIgnoreCase(INIT_RANDOMIZE_KEYWORD)) {
			processor.processInitRandomizeRequest(task);
		} else if (taskAction
				.equalsIgnoreCase(RESUBSCRIPTION_FEATURE_KEYWORD)) {
			processor.processResubscriptionRequest(task);
		} else if (taskAction
				.equalsIgnoreCase(SUPRESS_PRERENEWAL_SMS_KEYWORD)) {
			processor.processSupressPreRenewalSmsRequest(task);
		} else if (taskAction.equalsIgnoreCase(OUI_SMS_KEYWORD)) {
			processor.processOUISmsRequest(task);
		} else if (taskAction
				.equalsIgnoreCase(SMS_CANCEL_DEACTIVATION_KEYWORD)) {
			processor.processCancelDeactvation(task);
		} else if (taskAction
				.equalsIgnoreCase(SMS_BASE_SONG_UPGRADE_KEYWORD)) {
			processor.processBaseSongUpgradationRequest(task);
		} else if (taskAction
				.equalsIgnoreCase(TIME_OF_DAY_SETTING_KEYWORD)) {
			processor.processTimeBasedSettingRequest(task);
		} else if (taskAction
				.equalsIgnoreCase(SMS_CHURN_OFFER_KEYWORD)) {
			processor.processSMSChurnOfferOrDeact(task);
		} else if (taskAction.equalsIgnoreCase(CALLER_BASED_MULTIPLE_SELECTION_KEYWORD)){
			processor.processMultipleSelection(task);
		} else if (taskAction.equalsIgnoreCase(DEACT_BASE_SONG_CHURN_KEYWORD)){
			processor.processCancellar(task);
		} else if (taskAction.equalsIgnoreCase(SMS_CANCELLAR_KEYWORD)){
			processor.processSongManageDeact(task);
		} else if (taskAction.equalsIgnoreCase(DIRECT_SONG_DEACT_KEYWORD)){
			processor.processSongDeactivationConfirm(task);
		}else if(taskAction.equalsIgnoreCase(MANAGE_DEFAULT_SETTINGS_KEYWORD)){
			processor.getOnlyAllCallerSettings(task);
		} else if (taskAction.equalsIgnoreCase(BLOCK_SUB_KEYWORD)) {//RBT-12195 - User block - unblock feature.
			processor.processBlockSubRequest(task);
		} else if (taskAction
				.equalsIgnoreCase(UNBLOCK_SUB_KEYWORD)) {
			processor.processUnBlockSubRequest(task);
		} else if (taskAction.equalsIgnoreCase(PREMIUM_SELECTION_CONFIRMATION_KEYWORD)) {
			processor.processPremiumSelectionConfirmation(task);
		} else if (taskAction.equalsIgnoreCase(DOUBLE_OPT_IN_CONFIRMATION_KEYWORD)) {
			processor.processDoubleOptInConfirmation(task);
		}else if(taskAction.equalsIgnoreCase(DOUBLE_CONFIRMATION_FOR_XBI_PACK)){
			processor.processXbiPack(task);
		} else if (taskAction.equalsIgnoreCase(BASE_AND_COS_UPGRADATION_KEYWORD)) {
			processor.processBaseAndCosUpgradationRequest(task);
		}
		//Added for VB-380
		else if(taskAction.equalsIgnoreCase(AZAAN_REQUEST_DCT_KEYWORD)){
				processor.processDeactivateAzaan(task);
		}
    	return true;
    }

}