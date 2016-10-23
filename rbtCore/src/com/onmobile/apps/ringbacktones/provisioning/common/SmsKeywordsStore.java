package com.onmobile.apps.ringbacktones.provisioning.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;

public class SmsKeywordsStore implements iRBTConstant,Constants
{
	static Logger logger = Logger.getLogger(SmsKeywordsStore.class);
	public static ArrayList<HashSet<String>> allKeyWords= new ArrayList<HashSet<String>>();
	
	public static HashSet<String> retailerActNSelKeywordsSet = null;
	public static HashSet<String> rbtKeywordsSet = null;
	public static HashSet<String> activationKeywordsSet = null;
	public static HashSet<String> specificCategorySearchKeywordsSet = null;
	public static HashSet<String> confirmSubscriptionAndCopyKeywordsSet = null;
	public static HashSet<String> scratchCardKeywordsSet = null;
	public static HashSet<String> giftCopyKeywordsSet = null;
	public static HashSet<String> doubleConfirmationKeywordSet = null;
	public static HashSet<String> newsAndBeautyKeywordsSet = null;
	public static HashSet<String> selectionTypeOneKeywordsSet = null;
	public static HashSet<String> selectionTypeTwoKeywordsSet = null;
	public static HashSet<String> cricketKeywordsSet = null;
	public static HashSet<String> promotionTypeOneKeywordsSet = null;
	public static HashSet<String> promotionTypeTwoKeywordsSet = null;
	public static HashSet<String> songPromotionTypeOneKeywordsSet = null;
	public static HashSet<String> songPromotionTypeTwoKeywordsSet = null;
	public static HashSet<String> helpKeywordsSet = null;
	public static HashSet<String> deactivateBaseKeywordsSet = null;
	public static HashSet<String> referKeywordsSet = null;
	public static HashSet<String> deactivateSelectionKeywordsSet = null;
	public static HashSet<String> deactivateProfileKeywordsSet = null;
	public static HashSet<String> deactivateOverrideShuffleKeywordsSet = null;
	public static HashSet<String> deactivateManageSelectionsKeywordsSet = null;
	public static HashSet<String> deactivateDownloadKeywordsSet = null;
	public static HashSet<String> searchMoreContentKeywordsSet = null;
	public static HashSet<String> searchClipsKeywordsSet = null;
	public static HashSet<String> searchCategoriesKeywordsSet = null;
	public static HashSet<String> searchOptinKeywordsSet = null;
	public static HashSet<String> topClipsKeywordsSet = null;
	public static HashSet<String> topCategoriesKeywordsSet = null;
	public static HashSet<String> listCategoriesKeywordsSet = null;
	public static HashSet<String> copyConfirmKeywordsSet = null;
	public static HashSet<String> copyCancelTypeOneKeywordsSet = null;
	public static HashSet<String> copyKeywordsSet = null;
	public static HashSet<String> giftKeywordsSet = null;
	public static HashSet<String> copyCancelTypeTwoKeywordsSet = null;
	public static HashSet<String> initGiftKeywordsSet = null;
	public static HashSet<String> initGiftConfirmKeywordsSet = null;
	public static HashSet<String> pollOnKeywordsSet = null;
	public static HashSet<String> pollOffKeywordsSet = null;
	public static HashSet<String> newsletterOnKeywordsSet = null;
	public static HashSet<String> newsletterOffKeywordsSet = null;
	public static HashSet<String> disableIntroKeywordsSet = null;
	public static HashSet<String> disableOverlayKeywordsSet = null;
	public static HashSet<String> enableOverlayKeywordsSet = null;
	public static HashSet<String> weeklyToMonthlyConversionKeywordSet = null;
	public static HashSet<String> renewSelectionKeywordsSet = null;
	public static HashSet<String> tnbKeywordsSet = null;
	public static HashSet<String> viralKeywordsSet = null;
	public static HashSet<String> webRequestKeywordsSet = null;
	public static HashSet<String> mgmAcceptKeywordsSet = null;
	public static HashSet<String> retailerRequestKeywordsSet = null;
	public static HashSet<String> listenSongKeywordsSet = null;
	public static HashSet<String> songOfTheMonthKeywordsSet = null;
	public static HashSet<String> listDownloadsKeywordsSet = null;
	public static HashSet<String> manageKeywordsSet = null;
	public static HashSet<String> listProfilesKeywordsSet = null;
	public static HashSet<String> nextProfilesKeywordsSet = null;
	public static HashSet<String> mgmRequestKeywordsSet = null;
	public static HashSet<String> suspensionKeywordsSet = null;
	public static HashSet<String> resumptionKeywordsSet = null;
	public static HashSet<String> blockKeywordsSet = null;
	public static HashSet<String> unblockKeywordsSet = null;
	public static HashSet<String> songPackKeywordsSet = null;
	public static HashSet<String> packKeywordsSet = null;
	public static HashSet<String> meridhunKeywordsSet = null;
	public static HashSet<String> confirmChargeKeywordsSet = null;
	public static HashSet<String> lockKeywordsSet = null;
	public static HashSet<String> unlockKeywordsSet = null;
	public static HashSet<String> emotionKeywordsSet = null;
	public static HashSet<String> emotionExtendKeywordsSet = null;
	public static HashSet<String> emotionDeactivateKeywordsSet = null;
	public static HashSet<String> tryAndBuyKeywordsSet = null;
	public static HashSet<String> enableUDSKeywordsSet = null;
	public static HashSet<String> disableUDSKeywordsSet = null;
	public static HashSet<String> churnKeywordsSet = null;
	public static HashSet<String> downloadOptinKeywordsSet = null;
	public static HashSet<String> rdcSelectionKeywordsSet = null;
	public static HashSet<String> discountedSelectionKeywordsSet = null;
	public static HashSet<String> contestInfluencerKeywordsSet = null;
	public static HashSet<String> consentYesKeywordsSet = null;
	public static HashSet<String> consentNoKeywordsSet = null;
	public static HashSet<String> cpSelectionConfirmKeywordsSet = null;
	public static HashSet<String> comboPackKeywordsSet = null;
	public static HashSet<String> voucherKeywordsSet = null;
	public static HashSet<String> upgradeSelKeywordsSet = null;
	public static HashSet<String> giftAcceptKeywordSet = null;
	public static HashSet<String> giftDownloadKeywordSet = null;
	public static HashSet<String> giftRejectKeywordSet = null;
	public static HashSet<String> musicPackKeywordSet = null;
	public static HashSet<String> rechargeSmsOptOutKeywordSet = null;
	public static HashSet<String> baseUpgradationKeywordSet = null;
	public static HashSet<String> preGiftKeywordsSet = null;
	public static HashSet<String> preGiftConfirmKeywordsSet = null;
	public static HashSet<String> viralStartKeywordSet = null;
	public static HashSet<String> viralStopKeywordSet = null;
	public static HashSet<String> lotteryListKeywordsSet = null;
	public static HashSet<String> randomizeKeywordSet = null;
	public static HashSet<String> unrandomizeKeywordSet = null;
	public static HashSet<String> songCodeRequestKeywordSet = null;
	public static HashSet<String> viralOptOutRequestKeywordSet = null;
	public static HashSet<String> downloadSetRequestKeywordSet = null;
	public static HashSet<String> viewSubscriptionStatisticsKeywordSet = null;
	public static HashSet<String> viralOptInRequestKeywordSet = null;
	public static HashSet<String> initRandomizeRequestKeywordSet = null;
	public static HashSet<String> resubscriptionRequestKeywordSet = null;
	public static HashSet<String> supressPreRenewalSmsRequestKeywordSet = null;
	public static HashSet<String> ouiSmsRequestKeywordSet = null;
	public static HashSet<String> cancelDeactivationKeywordSet = null;
	public static HashSet<String> likeKeywordsSet = new HashSet<String>();
	public static HashSet<String> likeConfirmKeywordsSet = new HashSet<String>();
	public static HashSet<String> upgradeBaseKeywordSet = new HashSet<String>();
	public static HashSet<String> mobileUidRegistrationSet = new HashSet<String>();
	public static HashSet<String> timeOfDaySettingSet = null;
	public static HashSet<String> smsChurnOfferSet = null;
	public static HashSet<String> upgradeOnDeactDelaySet = null;
	public static HashSet<String> recommendedSongsKeywordsSet = null;
	public static HashSet<String> multipleSelectionKeywordSet = null;
	public static HashSet<String> deactBaseSongChurnKeywordSet = null;
	public static HashSet<String> baseSongDeactKeywordSet = null;
	public static HashSet<String> directSongDeactKeywordSet = null;
	public static HashSet<String> manageDefaultSettingsKeywordSet = null;
	//RBT-12195 - User block - unblock feature.
	public static HashSet<String> blockUsrKeywordsSet = null;
	public static HashSet<String> unblockUsrKeywordsSet = null;
	public static HashSet<String> premiumSelectionConfirmationKeywordsSet = null;
	public static HashSet<String> doubleOptinConfirmationKeywordsSet = null;
	public static HashSet<String> singledoubleConfirmationKeywordSet = null;
	// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
	public static HashSet<String> azaanSearchKeywordsSet = null;
	public static HashSet<String> azaanSearchMoreContentKeywordsSet = null;
	public static HashSet<String> doubleConfirmationForXbiPack = null;
	public static HashSet<String> baseAndCosUpgradationKeywordSet = null;
	//Added for VB-380
	public static HashSet<String> azaanDeactKeywordSet = null;
	
	static
	{
		logger.info("Initializing SMS Keywords");
		initializeAllKeywords();
		logger.info("SMS Keywords initialized. allKeyWords="+allKeyWords);
	}
	
	private static void initializeAllKeywords()
	{
		retailerActNSelKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,REATILER_ACT_N_SEL_FEATURE,null));
		rbtKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RBT_KEYWORD,null));
		activationKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,ACTIVATION_KEYWORD,null));
		specificCategorySearchKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SPECIFIC_CATEGORIES_SEARCH_KEYWORD,null));
		confirmSubscriptionAndCopyKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CONFIRM_SUBSCRIPTION_N_COPY_FEATURE,null));
		scratchCardKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SCRATCH_CARD_FEATURE,null));
		giftCopyKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,GIFTCOPY_FEATURE,null));
		doubleConfirmationKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SMS_DOUBLE_CONFIRMATION_CONFIRM_KEYWORD,null));
		newsAndBeautyKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,NEWS_AND_BEAUTY_FEED_KEYWORD,null));
		selectionTypeOneKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SEL_KEYWORD1,null));
		selectionTypeTwoKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SEL_KEYWORD2,null));
		cricketKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CRICKET_KEYWORD,null));
		helpKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,HELP_KEYWORD,null));
		deactivateBaseKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DEACTIVATION_KEYWORD,null));
		deactivateSelectionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RMVCALLERID_KEYWORD,null));
		deactivateProfileKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,TEMPORARY_OVERRIDE_CANCEL_MESSAGE,null));
		deactivateOverrideShuffleKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,NAV_DEACT_KEYWORD,null));
		deactivateManageSelectionsKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,MANAGE_DEACT_KEYWORD,null)); 
		deactivateDownloadKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DEACT_DOWNLOAD_KEYWORD,null));
		searchMoreContentKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,REQUEST_MORE_KEYWORD,null));
		azaanSearchMoreContentKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,AZAAN_REQUEST_MORE,null));
		searchClipsKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,REQUEST_RBT_KEYWORD,null));
		searchCategoriesKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CATEGORY_SEARCH_KEYWORD,null));
		searchOptinKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,REQUEST_OPTIN_RBT_KEYWORD,null));
		topClipsKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,TOP_CLIPS_KEYWORD,null));
		topCategoriesKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,TOP_CATEGORIES_KEYWORD,null));
		listCategoriesKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,LIST_CATEGORIES_KEYWORD,null));
		copyConfirmKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,COPY_CONFIRM_KEYWORD,null));
		copyCancelTypeOneKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,COPY_CANCEL_KEYWORD,null));
		copyKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,COPY_KEYWORDS,null));
		giftKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,GIFT_KEYWORD,null));
		copyCancelTypeTwoKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CANCELCOPY_KEYWORD,null));
		initGiftKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,INIT_GIFT_KEYWORD,null));
		initGiftConfirmKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,INIT_GIFT_CONFIRM_KEYWORD,null));
		pollOnKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,POLLON_KEYWORD,null));
		pollOffKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,POLLOFF_KEYWORD,null));
		newsletterOnKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SET_NEWSLETTER_ON_KEYWORDS,null));
		newsletterOffKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SET_NEWSLETTER_OFF,null));
		disableIntroKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DISABLE_INTRO,null));
		disableOverlayKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DISABLE_OVERLAY_KEYWORD,null));
		enableOverlayKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,ENABLE_OVERLAY_KEYWORD,null));
		weeklyToMonthlyConversionKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,WEEKLY_TO_MONTHLY_CONVERSION,null));
		renewSelectionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RENEW_KEYWORD,null));
		tnbKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,TNB_KEYWORDS,null));
		referKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,REFER_KEYWORDS,null));
		webRequestKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,WEB_REQUEST_KEYWORD,null));
		mgmAcceptKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,MGM_ACCEPT_KEY,null));
		retailerRequestKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RETAILER_REQ_RESPONSE,null));
		listenSongKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,LISTEN_KEYWORD,null));
		songOfTheMonthKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SONGOFMONTH,null));
		listDownloadsKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DOWNLOADS_LIST_KEYWORD,null));
		manageKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,MANAGE_KEYWORD,null));
		listProfilesKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,LIST_PROFILE_KEYWORD,null));
		nextProfilesKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,NEXT_PROFILE_KEYWORD,null));
		mgmRequestKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,MGM_FEATURE,null));
		suspensionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SUSPENSION_KEYWORD,null));
		resumptionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RESUMPTION_KEYWORD,null));
		blockKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,BLOCK_KEYWORD,null));
		unblockKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UNBLOCK_KEYWORD,null));
		songPackKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SONG_PACK_KEYWORD,null));
		packKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,PACK_KEYWORD,null));
		meridhunKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,MERIDHUN_KEYWORD,null));
		confirmChargeKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CONFIRM_CHARGE_KEYWORD,null));
		lockKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,LOCK_KEYWORD,null));
		unlockKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UNLOCK_KEYWORD,null));
		emotionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,EMOTION_KEYWORD,null));
		emotionExtendKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,EMOTION_EXTEND_KEYWORD,null));
		emotionDeactivateKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,EMOTION_DCT_KEYWORD,null));
		tryAndBuyKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,TNB_KEYWORD,null));
		enableUDSKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UDS_ENABLE,null));
		disableUDSKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UDS_DISABLE,null));
		churnKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CHURN_OFFER,null));
		downloadOptinKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DOWNLOAD_OPTIN_RENEWAL_KEYWORD,null));
		rdcSelectionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RDC_SEL_KEYWORD,null));
		discountedSelectionKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DISCOUNTED_SEL_KEYWORD,null));
		contestInfluencerKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,CONTEST_INFLUENCER_KEYWORD,null));
		consentYesKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, CONSENT_YES_KEYWORD, null));
		consentNoKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, CONSENT_NO_KEYWORD, null));
		cpSelectionConfirmKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, CP_SEL_CONFIRM_KEYWORD, null));
		comboPackKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, COMBO_PACK_KEYWORD, null));
		voucherKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, VOUCHER_KEYWORD, null));
		upgradeSelKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, UPGRADE_SEL_KEYWORD, null));
		giftAcceptKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, GIFT_ACCEPT_KEYWORD, null));
		giftRejectKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, GIFT_REJECT_KEYWORD, null));
		giftDownloadKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, GIFT_DOWNLOAD_KEYWORD, null));
		musicPackKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, MUSIC_PACK_KEYWORD, null));
		rechargeSmsOptOutKeywordSet=tokenizeAsSet(DBConfigTools.getParameter(SMS,RECHARGE_SMS_OPTOUT_KEYWORD,null));
		baseUpgradationKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,BASE_UPGRADATION_KEYWORD,null));
		preGiftKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,PRE_GIFT_KEYWORD,null));
		preGiftConfirmKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,PRE_GIFT_CONFIRM_KEYWORD,null));
		viralStartKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,VIRAL_START_KEYWORD,null));
		viralStopKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,VIRAL_STOP_KEYWORD,null));
		lotteryListKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, LOTTERY_LIST_KEYWORD, null));
		randomizeKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RANDOMIZE_KEYWORD,null));
		unrandomizeKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UNRANDOMIZE_KEYWORD,null));
		songCodeRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SONG_CODE_REQUEST_KEYWORD,null));
		viralOptOutRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,VIRAL_OPTOUT_KEYWORD,null));
		downloadSetRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,DOWNLOAD_SET_KEYWORD,null));
		viewSubscriptionStatisticsKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,VIEW_SUBSCRIPTION_STATISTICS_KEYWORD,null));
		viralOptInRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,VIRAL_OPTIN_KEYWORD,null));
		initRandomizeRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,INIT_RANDOMIZE_KEYWORD,null));
		resubscriptionRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RESUBSCRIPTION_FEATURE_KEYWORD,null));
		supressPreRenewalSmsRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SUPRESS_PRERENEWAL_SMS_KEYWORD,null));
		ouiSmsRequestKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,OUI_SMS_KEYWORD,null));
		cancelDeactivationKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SMS_CANCEL_DEACTIVATION_KEYWORD,null));
		upgradeBaseKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SMS_BASE_SONG_UPGRADE_KEYWORD,null));
		likeKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RBT_LIKE_KEYS,""));
		likeConfirmKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,RBT_LIKE_CONFIRM_KEYWORD,""));
		mobileUidRegistrationSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,MOBILE_REGISTRATION,""));
		timeOfDaySettingSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,TIME_OF_DAY_SETTING_KEYWORD,""));
		smsChurnOfferSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SMS_CHURN_OFFER_KEYWORD,""));
		upgradeOnDeactDelaySet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UPGRADE_ON_DELAY_DEACTIVATION_KEYWORD,""));
		recommendedSongsKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SMS_RECOMMEND_SONGS_KEYWORD,""));
		multipleSelectionKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, CALLER_BASED_MULTIPLE_SELECTION_KEYWORD, null));
		deactBaseSongChurnKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, SMS_CANCELLAR_KEYWORD, null));
		baseSongDeactKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, DEACT_BASE_SONG_CHURN_KEYWORD, null));
		directSongDeactKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, DIRECT_SONG_DEACT_KEYWORD, null));
		manageDefaultSettingsKeywordSet =  tokenizeAsSet(DBConfigTools.getParameter(SMS, MANAGE_DEFAULT_SETTINGS_KEYWORD, null)); 		
		//RBT-12195 - User block - unblock feature.
		blockUsrKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,BLOCK_SUB_KEYWORD,null));
		unblockUsrKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,UNBLOCK_SUB_KEYWORD,null));
		premiumSelectionConfirmationKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,PREMIUM_SELECTION_CONFIRMATION_KEYWORD,null));
		doubleOptinConfirmationKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS, DOUBLE_OPT_IN_CONFIRMATION_KEYWORD, null));
		//RBT-15149
		singledoubleConfirmationKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,SINGLE_KEYWORD_FOR_DOUBLE_CONFIRMATION,null));
		baseAndCosUpgradationKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,BASE_AND_COS_UPGRADATION_KEYWORD,null));		
		// Jira :RBT-15026: Changes done for allowing the multiple Azaan pack.
		azaanSearchKeywordsSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,
				AZAAN_REQUEST_RBT_KEYWORD, null));
		doubleConfirmationForXbiPack = tokenizeAsSet(DBConfigTools.getParameter(SMS,
				DOUBLE_CONFIRMATION_FOR_XBI_PACK, null));
		String promotion1String = DBConfigTools.getParameter(SMS,PROMOTION1,null);
		if(promotion1String != null)
			promotionTypeOneKeywordsSet = tokenizeAsSet(promotion1String.split(",")[0] );
		
		String promotion2String = DBConfigTools.getParameter(SMS,PROMOTION2,null);
		if(promotion2String != null)
			promotionTypeTwoKeywordsSet = tokenizeAsSet(promotion2String.split(",")[0] );
		
		String songPromotion1String = DBConfigTools.getParameter(SMS,SONG_PROMOTION1,null);
		if(songPromotion1String != null)
			songPromotionTypeOneKeywordsSet = tokenizeAsSet(songPromotion1String.split(",")[0] );
		
		String songPromotion2String = DBConfigTools.getParameter(SMS,SONG_PROMOTION2,null);
		if(songPromotion2String != null)
			songPromotionTypeTwoKeywordsSet = tokenizeAsSet(songPromotion2String.split(",")[0] );
		
		String viralString = DBConfigTools.getParameter(SMS,VIRAL_KEYWORD,null);
		if(viralString != null){
			viralKeywordsSet = tokenizeAsSet(viralString.split(",")[0] );
		}
		//Added for VB-380
		azaanDeactKeywordSet = tokenizeAsSet(DBConfigTools.getParameter(SMS,
				AZAAN_REQUEST_DCT_KEYWORD, null));
		
	}
	
	private static HashSet<String> tokenizeAsSet(String stringToTokenize)
	{
		if (stringToTokenize == null)
			return null;
		stringToTokenize = stringToTokenize.toLowerCase();
		HashSet<String> result = new HashSet<String>();
		
		String[] keywords = stringToTokenize.split(",");
		Collections.addAll(result, keywords);

		allKeyWords.add(result);
		return result;
	}
}