package com.onmobile.apps.ringbacktones.v2.resolver.response.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.i18n.LocaleContextHolder;

import com.livewiremobile.store.storefront.dto.payment.Purchase;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.AssetList;
import com.livewiremobile.store.storefront.dto.rbt.Pager;
import com.livewiremobile.store.storefront.dto.rbt.ThirdPartyConsent;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class DownloadResponeResolver extends AbstractDownloadResponeResolver {

	@Override
	public Purchase prepareLikeContentResponse(String msisdn,String response, Rbt rbt, SelectionRequest selectionRequest, AbstractOperatorUtility operatorUtility) throws UserException {
		if(!response.equalsIgnoreCase("success")) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, response, MessageResource.LIKE_CONTENT_MESSAGE);
		}
		Download download = getDownload(rbt);
		Purchase purchase = getPurchase(download);
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(msisdn);
		//Added for logger
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtRequest);
		String operatorName = ServiceUtil.getOperatorName(subscriber);
		String cgUrl = makeConsentCGUrl(operatorUtility, rbt, selectionRequest, operatorName, subscriber);
		String rUrl = makeRUrl(operatorUtility);
		ThirdPartyConsent  thirdPartyConsent = buildThirdPartyConsent(operatorUtility, cgUrl, rUrl);
		if(thirdPartyConsent != null)			
			purchase.setThirdpartyconsent(thirdPartyConsent);
		return purchase;
	}

	@Override
	public AssetList prepareGetLibraryResponse(Library library) throws UserException {
		AssetList assetList = new AssetList();

		List<Asset> assets = null;

		if(library != null && library.getDownloads() != null) {
			Download[] downloads = library.getDownloads().getDownloads();
			if(downloads != null) {
				assets = new ArrayList<Asset>(downloads.length);
			}

			for(Download download : downloads) {

				if(download == null) {
					continue;
				}

				IRbtUgcWavfileDao rbtUgcWavfileDao = null;
				if(download.getToneType().equalsIgnoreCase(WebServiceConstants.CATEGORY_RECORD)) {
					try {
						rbtUgcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
						String wavFile = download.getUgcRbtFile();
						if(wavFile.contains(".wav")) {
							wavFile = wavFile.substring(0, wavFile.indexOf(".wav"));
						}
						RBTUgcWavfile ugcWavfile = rbtUgcWavfileDao.getUgcWavFile(Long.parseLong(download.getSubscriberID()),wavFile);
						download.setToneID((int)ugcWavfile.getUgcId());
					} catch (Exception e) {
						logger.error("Exception Occured: "+e, e);
					}
				}
				Asset tempAsset = getAsset(download);
				if(tempAsset != null) {
					assets.add(tempAsset);
				}

			}
		}

		if(assets != null) {
			assetList.setAssets(assets);

			Pager pager = new Pager();
			pager.setOffset(0L);
			pager.setPagesize(assets.size());
			pager.setTotalresults(assets.size());

			assetList.setPager(pager);

			assetList.setCount(assets.size());
		}

		return assetList;
	}

	@Override
	public Map<String, String> prepareDeleteDownloadResponse(String response)
			throws UserException {
		if(!response.equalsIgnoreCase("success")) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, response, MessageResource.LIBRARY_DELETE_MESSAGE);
		}
		
		Map<String, String> map = new HashMap<String, String>();
		Locale locale = LocaleContextHolder.getLocale();
		
		map.put("message",applicationContext.getMessage(MessageResource.LIBRARY_DELETE_MESSAGE + response.toLowerCase(), null, locale));
		map.put("code",errorCodeMapping.getErrorCode(response.toLowerCase()).getCode());
		return map;
	}

	@Override
	public Map<String, String> prepareUpdateDownloadResponse(String response)
			throws UserException {
		if(!response.equalsIgnoreCase("success")) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, response, MessageResource.LIBRARY_UPDATE_MESSAGE);
		}
		
		Map<String, String> map = new HashMap<String, String>();
		Locale locale = LocaleContextHolder.getLocale();
		
		map.put("message",applicationContext.getMessage(MessageResource.LIBRARY_UPDATE_MESSAGE + response.toLowerCase(), null, locale));
		map.put("code",errorCodeMapping.getErrorCode(response.toLowerCase()).getCode());
		return map;
	}
}
