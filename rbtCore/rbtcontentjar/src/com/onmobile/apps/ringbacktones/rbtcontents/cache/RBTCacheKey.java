package com.onmobile.apps.ringbacktones.rbtcontents.cache;

import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.utils.RBTContentUtils;

public class RBTCacheKey {

	
	public static String getCategoryIdCacheKey(int categoryId) {
		return getCategoryIdLanguageCacheKey(categoryId, null);
	}
	public static String getCategoryIdLanguageCacheKey(int categoryId, String language) {
		return getCategoryIdLanguageCacheKey(categoryId, language, null);
	}
	public static String getCategoryIdLanguageCacheKey(int categoryId, String language, String appName) {
		String returnString = null;
		if (language == null || language.equals("")) {
			if (appName != null && !appName.trim().isEmpty()) {
				String rbtContentJarKey = RBTContentUtils.appNameDefaultLanguageKey(appName);
				String appDefaultLanguage = RBTContentJarParameters.getInstance().getParameter(rbtContentJarKey);
				if (appDefaultLanguage != null
						&& !appDefaultLanguage.trim().isEmpty()) {
					returnString = "categoryid_" + categoryId + "_" + appDefaultLanguage.toUpperCase();
				}
			}
			if (returnString == null) {
				language = RBTContentJarParameters.getInstance().getParameter("default_language");
				returnString = "categoryid_" + categoryId + "_" + language.toUpperCase();
			}
		} else {
			returnString = "categoryid_" + categoryId + "_" + language.toUpperCase();
		}
		return returnString;
	}

	public static String getClipsInCategoryCacheKey(int categoryId) {
		return "clipsincategory" + categoryId;
	}
	
	public static String getActiveClipsInCategoryCacheKey(int categoryId) {
		return "activeclipsincategory" + categoryId;
	}
	
	public static String getCategoriesInCircleCacheKey(String circleId, int parentCategoryId, char prepaidYes, String language) {
		StringBuilder result = new StringBuilder();
		result.append(circleId);
		result.append("-");
		result.append(prepaidYes);
		result.append("-");
		result.append(parentCategoryId);
		result.append("-");
		result.append((language == null || language.equalsIgnoreCase("DEFAULT") ? null : language));
		return result.toString();
	}

	public static String getActiveCategoriesInCircleCacheKey(String circleId, int parentCategoryId, char prepaidYes, String language) {
		StringBuilder result = new StringBuilder();
		result.append("active");
		result.append(circleId);
		result.append("-");
		result.append(prepaidYes);
		result.append("-");
		result.append(parentCategoryId);
		result.append("-");
		result.append((language == null || language.equalsIgnoreCase("DEFAULT") ? null : language));
		return result.toString();
	}
	
	public static String getClipIdCacheKey(int clipId) {
		return getClipIdLanguageCacheKey(Integer.toString(clipId), null);
	}
	
	public static String getClipIdLanguageCacheKey(int clipId, String language, String appName) {
		return getClipIdLanguageCacheKey(Integer.toString(clipId), language, appName);
	}
	
	public static String getClipIdLanguageCacheKey(int clipId, String language) {
		return getClipIdLanguageCacheKey(Integer.toString(clipId), language);
	}
	
	public static String getClipIdLanguageCacheKey(String clipId, String language) {
		return getClipIdLanguageCacheKey(clipId, language, null);
	}

	public static String getClipIdLanguageCacheKey(String clipId, String language, String appName) {
		String returnString = null;
		if (language == null || language.equals("")) {
			if (appName != null && !appName.trim().isEmpty()) {
				String rbtContentJarKey = RBTContentUtils.appNameDefaultLanguageKey(appName);
				String appDefaultLanguage = RBTContentJarParameters.getInstance().getParameter(rbtContentJarKey);
				if (appDefaultLanguage != null
						&& !appDefaultLanguage.trim().isEmpty()) {
					returnString = "clipId_" + clipId + "_" + appDefaultLanguage.toUpperCase();
				}
			}
			if (returnString == null) {
				language = RBTContentJarParameters.getInstance().getParameter("default_language");
				returnString = "clipId_" + clipId + "_" + language.toUpperCase();
			}
		} else {
			returnString = "clipId_" + clipId + "_" + language.toUpperCase();
		}
		return returnString;
	}
	
	public static String getClipIdCacheKey(String clipId) {
		return getClipIdLanguageCacheKey(clipId, null);
	}

	public static String getPromoIdCacheKey(String promoId) {
		return "promoid" + promoId.toLowerCase();
	}

	public static String getCategoryPromoIdCacheKey(String promoId) {
		return "catpromoid" + promoId.toLowerCase();
	}
	
	public static String getArtistNameInitialCacheKey(Character artistInitial){
		return "artistInitial_" + artistInitial;
	}

	public static String getCategoryMMNumberCacheKey(String mmNumber) {
		return "mmnumber" + mmNumber;
	}
	
	public static String getRbtWavFileCacheKey(String rbtWavFile) {
		return "rbtwavfile" + rbtWavFile;
	}
	
	public static String getSmsAliasCacheKey(String smsAlias) {
		return "clipsmsalias" + smsAlias.toLowerCase();
	}

	public static String getCircleIdCacheKey(String circleId) {
		return "circleid" + circleId;
	}

	public static String getCategorySmsAliasCacheKey(String smsAlias) {
		return "catsmsalias" + smsAlias.toLowerCase();
	}
	
	public static String getCategoryNameCacheKey(String categoryName) {
		return "catname" + categoryName;
	}
	
	public static String getAlbumCacheKey(String albumName){
		return "album" + albumName;
	}
	
	public static String getActiveAlbumCacheKey(String albumName){
		return "activealbum" + albumName;
	}
	
	public static String getPromoMasterCacheKey(String promoCode, String promoType){
		return "prmomaster" + promoCode.toLowerCase() + "_" + promoType.toLowerCase();
	}
	
	public static String getPromoCodeCacheKey(String promoCode){
		return "promocode"+promoCode.toLowerCase();
	}

	public static String getCategoryTypeCacheKey(String categoryType){
		return "categorytype"+categoryType.trim();
	}

	public static String getTypePrepadiCircleIdCacheKey(String circleId, char prepaidYes, String categoryType){
		return "typeprepaidcircleid"+circleId+prepaidYes+categoryType;
	}

	public static String getPromoIdPrepadiCircleIdCacheKey(String circleId, char prepaidYes, String catPromoId){
		return "promoidprepaidcircleid"+circleId+prepaidYes+catPromoId.toLowerCase();
	}
	
	public static String getClipNameLanguageKey(String language){
		return "CLIP_NAME_"+language.toUpperCase();
	}
	
	public static String getClipGrammarLanguageKey(String language){
		return "CLIP_GRAMMAR_"+language.toUpperCase();
	}
	
	public static String getClipArtistLanguageKey(String language){
		return "ARTIST_"+language.toUpperCase();
	}
	
	public static String getClipAlbumLanguageKey(String language){
		return "ALBUM_"+language.toUpperCase();
	}
	
	public static String getClipInfoLanguageKey(String language){
		return "CLIP_INFO_"+language.toUpperCase();
	}
	
	public static String getClipSMSAliasLanguageKey(String language){
		return "CLIP_SMS_ALIAS_"+language.toUpperCase();
	}
	
	public static String getClipLanguageLanguageKey(String language){
		return "LANGUAGE_"+language.toUpperCase();
	}
	
	public static String getCategoryNameLanguageKey(String language){
		return "CATEGORY_NAME_"+language.toUpperCase();
	}
	public static String getCategoryGrammarLanguageKey(String language){
		return "CATEGORY_GRAMMAR_"+language.toUpperCase();
	}
	public static String getCategoryInfoLanguageKey(String language){
		return "CATEGORY_INFO_"+language.toUpperCase();
	}
	public static String getCategoryInfoDescKey(String language){
		return "CAT_DESC_"+language.toUpperCase();
	}

	public static String getVcodeCacheKey(String clipVcode) {
		return "clipVcode" + clipVcode;
	}
	
	public static String getAlbumNameByLanguageKey(String language, Character character) {
		return "ALBUM_" + language.toUpperCase() + "_" + String.valueOf(Character.toUpperCase(character));
	}
	
}
