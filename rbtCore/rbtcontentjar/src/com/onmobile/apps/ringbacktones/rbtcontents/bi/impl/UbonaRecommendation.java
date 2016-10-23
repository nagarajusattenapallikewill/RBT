package com.onmobile.apps.ringbacktones.rbtcontents.bi.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfoAction;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class UbonaRecommendation implements BIInterface {

	private static Logger logger = Logger.getLogger(UbonaRecommendation.class);

	public UbonaRecommendation() {
		logger.info("Instantiating UbonaRecommendation class");
	}

	@Override
	public Object[] process(Category category, String subscriberId,
			String circleId, boolean doReturnActiveClips, String language,
			String appName, boolean isFromCategory,
			ClipInfoAction clipInfoAction) {
		String responseString = null;

		String strUrl = RBTContentJarParameters.getInstance().getParameter(
				"BI_URL_" + category.getCategoryTpe());
		logger.info("Ubona recommendation url from rbtContentJar.properties file : "
				+ strUrl);
		try {

			if (subscriberId != null) {
				strUrl = strUrl.replaceAll("%msisdn%", subscriberId);
			}

			if (circleId != null) {
				strUrl = strUrl.replaceAll("%circleId%", circleId);
			}

			if (clipInfoAction != null) {
				if (clipInfoAction.getBrowsingLanguage() != null) {
					strUrl = strUrl.replaceAll("%language%",
							clipInfoAction.getBrowsingLanguage());

				}

				strUrl = strUrl.replaceAll("%isUserLanguageSelected%",
						clipInfoAction.isUserLanguageSelected() + "");

				strUrl = strUrl.replaceAll("%isSubscribed%",
						clipInfoAction.isSubscribed() + "");

				if (clipInfoAction.getBrowsingLanguage() != null) {
					strUrl = strUrl.replaceAll("%totalSize%",
							clipInfoAction.getTotalSize() + "");
				}

				if (clipInfoAction.getBrowsingLanguage() != null) {
					strUrl = strUrl.replaceAll("%sessionID%",
							clipInfoAction.getSessionID() + "");
				}
			}

			logger.info("RE recommendation url after replacing the parameters : "
					+ strUrl);

			HttpParameters httpParameters = new HttpParameters(strUrl);
			logger.info("RBT:: httpParameters: " + httpParameters);

			HttpResponse httpResponse;

			httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			logger.info("RBT:: httpResponse: " + httpResponse);

			responseString = httpResponse.getResponse();

			if (responseString == null) {
				logger.info("RBT:: responseString : " + responseString);
				return null;
			}

			JSONObject json = new JSONObject(responseString.trim());
			int value = Integer.parseInt(json.get("responseCode") + "");
			String contentID = "";
			ArrayList<Category> subCategoryList = new ArrayList<Category>();
			ArrayList<Clip> clipList = new ArrayList<Clip>();
			Date now = new Date();
			if (value == 0) {
				JSONObject contentIdInfo = json
						.getJSONObject("recommendedContent");
				// String totalSize = contentIdInfo.getString("contentIdInfo");
				JSONArray contentIds = contentIdInfo.getJSONArray("contentIds");
				if (contentIds != null && contentIds.length() > 0) {

					// if category browsing checking flag and returning sub
					// categories of parent catId

					if (isFromCategory) {
						for (int i = 0; i < contentIds.length(); i++) {
							contentID = (String) contentIds.get(i);

							if (null != contentID && !contentID.isEmpty()) {
								int cetegoryObj = Integer.parseInt(contentID);
								Category subCategory = RBTCacheManager
										.getInstance().getCategoryByPromoId(cetegoryObj+"",
												language);
								logger.info("list of subcategories :"
										+ subCategory);
								if (subCategory == null
										|| (!subCategory.getCategoryStartTime()
												.before(now) || !subCategory
												.getCategoryEndTime()
												.after(now))) {
									continue;
								}
								subCategoryList.add(subCategory);
							}

						}

						return (Category[]) subCategoryList
								.toArray(new Category[0]);
					}
					// else clips
					else {
						for (int i = 0; i < contentIds.length(); i++) {
							contentID = (String) contentIds.get(i);

							Clip clip = RBTCacheManager.getInstance().getClipByPromoId(contentID, language);
							if (clip == null
									|| (doReturnActiveClips && (!clip
											.getClipStartTime().before(now) || !clip
											.getClipEndTime().after(now)))) {
								continue;
							}
							clipList.add(clip);

						}
						return (Clip[]) clipList.toArray(new Clip[0]);
					}

				}

				return null;
			} else {
				logger.info("RBT:: returning null as the responseCode is  "
						+ value);
				return null;
			}

		} catch (HttpException e) {
			logger.error("", e);
			responseString = "ERROR";
		} catch (IOException e) {
			logger.error("", e);
			responseString = "ERROR";
		} catch (JSONException e) {
			logger.error("", e);
			responseString = "ERROR";
		}

		return null;
	}

	@Override
	public boolean processHitBIForPurchase(String subscriberId, String refId,
			String mode, String toneId) {
		// TODO Auto-generated method stub
		return false;
	}

}
