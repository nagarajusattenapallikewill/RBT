package com.onmobile.apps.ringbacktones.v2.resolver.request.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.model.NotFoundException;
import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.converter.ConverterHelper;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.v2.dto.SelRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.handler.IComboReqHandler;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;

/**
 * 
 * @author Vikrant.Verma
 *
 */

public class ComboCutRbtRequestResolver extends ComboRequestResolver {

	@Autowired
	@Qualifier(value = BeanConstant.SUB_PURCHASE_SET_COMBO_REQUEST_HANDLER)
	private IComboReqHandler comboReqHandler;

	private static Logger logger = Logger.getLogger(ComboCutRbtRequestResolver.class);
	public static ParametersCacheManager parameterCacheManager = null;

	static {
		parameterCacheManager = CacheManagerUtil.getParametersCacheManager();

	}

	public void setOperatorsAllowed(Set<String> operatorsAllowed) {
	}

	@Override
	public PurchaseCombo processComboReq(String msisdn, PurchaseCombo purchaseCombo, String mode) throws UserException {
		logger.info("Processing Cut Rbt Combo Request");
		String firstName = null;
		String lastName = null;
		String starttime = null;
		String newFileDirectory = null;
		String newFileName = null;
		boolean s3file = false;

		String url;
		Asset asset = purchaseCombo.getAsset();
		if (asset == null)
			ServiceUtil.throwCustomUserException(errorCodeMapping, ASSET_IS_REQUIRED, COMBO_REQUEST_MESSAGE_FOR);
		if (purchaseCombo.getPurchase() == null)
			ServiceUtil.throwCustomUserException(errorCodeMapping, PURCHASE_IS_REQUIRED, COMBO_REQUEST_MESSAGE_FOR);

		Song songasset = (Song) purchaseCombo.getAsset();
		starttime = songasset.getCutStartDuration();
		logger.info("\n\t:---> starttime : " + starttime);
		if (starttime == null) {
			logger.info("Start time is null");
			return super.processComboReq(msisdn, purchaseCombo, mode);
		}

		url = makeS3Url(purchaseCombo);
		File wavFile = null;
		if (url != null && url.toLowerCase().startsWith("s3")) {
			wavFile = getWavFromS3(url);
			s3file = true;

		}

		else
			wavFile = new File(url);
		logger.info("wavFile: " + wavFile.getTotalSpace());
		newFileDirectory = param(iRBTConstant.COMMON, "CUTRBT_DIR", "/");
		/*
		 * url = makeS3Url(purchaseCombo);
		 * 
		 * File wavFile = getWavFromS3(url);
		 * 
		 * File wavFile = new
		 * File("/var/wavesong/Zindagi_Pyar_Ka_Geet_Hai.wav"); File wavFile =
		 * new File("D:\\wavesong\\Zindagi_Pyar_Ka_Geet_Hai.wav"); logger.info(
		 * "wavFile: "+wavFile.getTotalSpace());
		 * 
		 * newFileDirectory = param(iRBTConstant.COMMON, "CUTRBT_DIR",
		 * "D:\\wavesong\\");
		 */
		newFileName = getwavFileName(purchaseCombo);
		if (newFileName != null)
			newFileName = newFileName + "_cut_" + starttime + ".wav";
		logger.info("\n\t:--->newFileName" + newFileName);
		cutrbt(wavFile, starttime, newFileDirectory, newFileName, s3file);
		logger.info("Going to hit RBTClient to get Subscriber Detail");
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(new RbtDetailsRequest(msisdn));

		PlayRule playRule = purchaseCombo.getPlayrule();
		if (playRule != null && playRule.getCallingparty() != null) {
			firstName = playRule.getCallingparty().getFirstname();
			lastName = playRule.getCallingparty().getLastname();
		}
		String rbtFile = null;
		if (newFileName != null)
			rbtFile = newFileName.split(".wav")[0];
		String callerId = getCallerId(playRule);
		SelRequestDTO selRequestDTO = new SelRequestDTO(playRule, subscriber, mode, callerId,
				subscriber.getSubscriberID(), firstName, lastName, rbtFile);
		selRequestDTO.setSubscription(purchaseCombo.getSubscription());
		selRequestDTO.setAsset(asset);
		validateEphemeralSelectionForCaller(playRule);
		SelectionRequest selectionRequest = getSelectionRequest(selRequestDTO);
		comboReqHandler.applyRequest(subscriber, purchaseCombo, selectionRequest);
		uploadRBTtoTonePlayer(msisdn, newFileName);

		return purchaseCombo;
	}

	private void uploadRBTtoTonePlayer(String msisdn, String newFileName) {
		logger.info("Going to upload to toneplayer");
		UploadDeamon tx = new UploadDeamon();
		tx.setMsisdn(msisdn);
		tx.setFile(newFileName.split(".wav")[0]);
		tx.start();

	}

	private void cutrbt(File wavFile, String starttime, String newFileDirectory, String newFileName, Boolean s3file)
			throws UserException {
		String downloadFilePath = wavFile.getAbsolutePath();
		logger.info(":---> downloadFilePath" + downloadFilePath);
		String previewTime = param(iRBTConstant.COMMON, "CUTRBT_DURATION", "3");
		HashMap<String, String> previewFileList = new HashMap<String, String>();
		AudioAttributes audio = new AudioAttributes();
		audio.setBitRate(new Integer("32000"));
		audio.setChannels(new Integer("1"));
		audio.setSamplingRate(new Integer("8000"));
		EncodingAttributes attrs = new EncodingAttributes();
		attrs.setOffset(new Float(starttime));
		attrs.setDuration(new Float(previewTime));
		attrs.setFormat("wav");
		attrs.setAudioAttributes(audio);
		Encoder encoder = new Encoder();
		logger.info("Converting " + downloadFilePath + " file to " + newFileDirectory + newFileName);
		try {
			encoder.encode(new File(downloadFilePath), new File(newFileDirectory + newFileName), attrs);
			if (s3file) {
				logger.info("Downloading file from aws s3 downnload path");
				new File(downloadFilePath).delete();
			}

		} catch (IllegalArgumentException e) {
			logger.error("Error" + e, e);
			e.printStackTrace();
			ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, COMBO_REQUEST_MESSAGE_FOR);

		} catch (InputFormatException e) {
			logger.error("Error" + e, e);
			e.printStackTrace();
			ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, COMBO_REQUEST_MESSAGE_FOR);

		} catch (EncoderException e) {
			logger.error("Error" + e, e);
			e.printStackTrace();
			ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, COMBO_REQUEST_MESSAGE_FOR);

		}
		String cutFilePath = newFileDirectory + newFileName;
		logger.info(":---> cutFilePath" + cutFilePath);
		previewFileList.put(newFileDirectory, newFileName);

	}

	private File getWavFromS3(String url) throws UserException {
		// url format : "s3://rbtqa-bakcup/preview/rbt_36342681_rbt.mp3";
		// This is where the downloaded file will be saved
		String downloadPath = param(iRBTConstant.COMMON, "CUTRBT_AWS_S3_DOWNLOAD_PATH", "/var/");
		if (!url.toLowerCase().startsWith("s3://")) {
			logger.error("Wrong s3 url format");
			ServiceUtil.throwCustomUserException(errorCodeMapping, INTERNAL_SERVER_ERROR, COMBO_REQUEST_MESSAGE_FOR);

		}
		String bucketName = url.substring(5, url.indexOf("/", 5));
		String key = url.split(bucketName)[1].substring(1);
		String filename = url.substring(url.lastIndexOf("/") + 1);
		if (downloadPath.endsWith("/"))
			downloadPath = downloadPath + filename;
		else
			downloadPath = downloadPath + "/" + filename;

		File wavFile = new File(downloadPath);
		try {

			logger.info("\n\t:---> bucketName : " + bucketName);
			logger.info("\n\t:---> key : " + key);
			AmazonS3Client s3Client = new AmazonS3Client();
			ObjectMetadata object = s3Client.getObject(new GetObjectRequest(bucketName, key), wavFile);
			boolean success = wavFile.exists() && wavFile.canRead();
			logger.info("\n\t:---> success status s3 download : " + success);

		} catch (AmazonServiceException ase) {
			if (ase.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				// throw new NotFoundException(url);
				logger.error("NotFoundException", ase);
				ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, AWS_ERROR);

			} else {
				// throw new RuntimeException(ase.getMessage(), ase);
				logger.error("RuntimeException", ase);
				ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, AWS_ERROR);

			}
		} catch (AmazonClientException ace) {
			// throw new RuntimeException(ace.getMessage(), ace);
			logger.error("RuntimeException", ace);
			ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, AWS_ERROR);

		} catch (Throwable e) {
			logger.error("Exception", e);
			logger.info("Throws exception while getting file from S3... "+e);
			ServiceUtil.throwCustomUserException(errorCodeMapping, THIRD_PARTY_SERVER_ERROR, AWS_ERROR);

		}
		return wavFile;

	}

	private String makeS3Url(PurchaseCombo purchaseCombo) throws UserException {
		// s3://rbtqa-bakcup/preview/
		String fulltrackName = getfulltrackname(purchaseCombo);
		String url = param(iRBTConstant.COMMON, "CUTRBT_AWS_S3_URL", null);
		if (url == null) {
			logger.error("S3 Url is not configured in CUTRBT_AWS_S3_URL parameter");
			ServiceUtil.throwCustomUserException(errorCodeMapping, INTERNAL_SERVER_ERROR, COMBO_REQUEST_MESSAGE_FOR);

		} else {
		    url = url + fulltrackName;
		}
		logger.info("\n :---> s3url : " + url);
		return url;
	}

	private String getwavFileName(PurchaseCombo purchaseCombo) throws UserException {

		String wavefile = null;
		int clipID = (int) purchaseCombo.getAsset().getId();
		logger.info(":---> clipID" + clipID);

		Clip clip = RBTCacheManager.getInstance().getClip(clipID);
		if (clip == null) {
			logger.error("Clip is null so throwing exception.");
			ServiceUtil.throwCustomUserException(errorCodeMapping, CLIP_NOT_EXIST, COMBO_REQUEST_MESSAGE_FOR);
		}
		wavefile = clip.getClipRbtWavFile();
		logger.info(":---> wavefile" + wavefile);
		return wavefile;
	}

	private String getfulltrackname(PurchaseCombo purchaseCombo) throws UserException {

		String fulltrackName = null;
		int clipID = (int) purchaseCombo.getAsset().getId();
		logger.info("\n\t:---> clipID" + clipID);
		String key = param(iRBTConstant.COMMON, "CUTRBT_FULLTRACKWAVNAME_KEY", "fullTrackWavName");
		Clip clip2 = RBTCacheManager.getInstance().getClip(clipID);
		Map<String, String> clipInfoMap = MapUtils.convertIntoMap(clip2.getClipInfo(), "|", "=", null);
		fulltrackName = clipInfoMap.get(key);
		logger.info("\n\t:---> fulltrackName" + fulltrackName);
		if (fulltrackName == null) {
			logger.error("fulltrackName is null so throwing exception...");
			ServiceUtil.throwCustomUserException(errorCodeMapping, CUT_RBT_NOT_SUPPORTED, COMBO_REQUEST_MESSAGE_FOR);
		}
		return fulltrackName;
	}

	/*private Map<String, String> parseClipInfo(String clipInfo) {
		Map<String, String> clipInfoMap = new HashMap<String, String>();
		if (clipInfo != null) {
			StringTokenizer stk = new StringTokenizer(clipInfo, "|");
			while (stk.hasMoreTokens()) {
				String token = stk.nextToken();
				String[] split = token.split(":", -1);
				clipInfoMap.put(split[0], split[1]);
			}
		}
		return clipInfoMap;
	}*/

	public static String param(String type, String paramName, String defaultVal) {
		Parameters param = parameterCacheManager.getParameter(type, paramName, defaultVal);
		if (param != null) {
			String value = param.getValue();
			if (value != null)
				return value.trim();
		}
		return defaultVal;
	}

	public static int param(String type, String paramName, int defaultVal) {
		Parameters param = parameterCacheManager.getParameter(type, paramName, String.valueOf(defaultVal));
		if (param != null) {
			try {
				String value = param.getValue();
				if (value != null)
					return Integer.parseInt(value.trim());
			} catch (Exception e) {
				return defaultVal;
			}
		}
		return defaultVal;

	}
}
