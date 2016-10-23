package com.onmobile.apps.ringbacktones.Gatherer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.tcp.supporters.ViralPromotion;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.hunterFramework.management.HttpPerformanceMonitor;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitorFactory;
import com.onmobile.apps.ringbacktones.hunterFramework.management.PerformanceMonitor.PerformanceDataType;
import com.onmobile.apps.ringbacktones.provisioning.common.CopyProcessorUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class RBTLikeDaemon implements iRBTConstant, Runnable {
	private static Logger logger = Logger.getLogger(RBTLikeDaemon.class);
	private static Logger cdr_logger = Logger.getLogger("CDR_LOGGER_HASH_LIKE");
	private static Logger cdr_error_logger = Logger
			.getLogger("CDR_LOGGER_HASH_LIKE_ERROR");
	private final ViralSMSTable viralData;
	private RBTConnector rbtConnector = null;
	public RBTDBManager rbtDBManager = null;
	private RBTCopyLikeUtils m_rbtCopyLikeUtils;
	String defaultClipWavName = null;
	public static List<String> subStatusesBlockedForCopy = null;
	private static String subscriptionClassOperatorNameMap = null;
	public static List<String> m_blockedCategoryInfoList = null;
	private static Map<String, List<String>> m_vrbtCatIdSubSongSrvKeyMap = null;
	private Map<String, String> confAzaanCopticDoaaCosIdSubTypeMap = null;
	private static Map<String, String> confAzaanCopticDoaaSubTpeContentNameMap = null;
	private static String azaanWavFileName = null;
	private static String azaanCategoryId = null;
	static HashSet<String> copyVirtualNumbers = null;
	private RBTHttpClient rbtHttpClient = null;
	private static List<List<Integer>> blackoutTimesList = null;

	public RBTLikeDaemon(ViralSMSTable viralData) throws Exception {
		if (viralData == null) {
			throw new IllegalArgumentException("viralData can not be null");
		}
		this.viralData = viralData;
		if (init())
			logger.info("RBTLikeDaemon init() done");
		else
			throw new Exception(" In RBTLikeDaemon: Cannot init Parameters");
	}

	static {
		String subStatusesBlockedForCopyString = RBTParametersUtils
				.getParamAsString("GATHERER", "SUB_STATUSES_BLOCKED_FOR_COPY",
						null);
		logger.info("subStatusesBlockedForCopyString = "
				+ subStatusesBlockedForCopyString);
		subStatusesBlockedForCopy = ListUtils.convertToList(
				subStatusesBlockedForCopyString, ",");
		String blockedCatTypeStr = RBTParametersUtils.getParamAsString(
				"GATHERER", "BLOCKED_CATEGORY_INFO", null);
		m_blockedCategoryInfoList = ListUtils.convertToList(blockedCatTypeStr,
				",");
		logger.info("m_blockedCategoryInfoList = " + m_blockedCategoryInfoList);
		String subTypeContentNameMapStr = RBTParametersUtils.getParamAsString(
				"COMMON", "SUBTYPE_CONTENT_NAME_MAPPING_FOR_AZAAN", "");
		confAzaanCopticDoaaSubTpeContentNameMap = MapUtils.convertToMap(
				subTypeContentNameMapStr, ";", ":", ",");
		logger.info("subStatusesBlockedForCopy = " + subStatusesBlockedForCopy);
		subscriptionClassOperatorNameMap = RBTParametersUtils.getParamAsString(
				"COMMON", "SUBSCRIPTION_CLASS_OPERATOR_NAME_MAP", null);
		logger.info("subscriptionClassOperatorNameMap = "
				+ subscriptionClassOperatorNameMap);
		m_vrbtCatIdSubSongSrvKeyMap = com.onmobile.apps.ringbacktones.provisioning.common.Utility
				.getVrbtCatSubSongSrvMap();
		azaanWavFileName = RBTParametersUtils.getParamAsString("GATHERER",
				"AZAAN_WAV_FILE_NAME", null);
		azaanCategoryId = RBTParametersUtils.getParamAsString("GATHERER",
				"AZAAN_CATEGORY_ID", null);
		copyVirtualNumbers = new HashSet<String>();
		List<Parameters> virtualNoParameters = CacheManagerUtil
				.getParametersCacheManager().getParameters("VIRTUAL_NUMBERS");
		if (virtualNoParameters != null) {
			for (Parameters virtualNoParameter : virtualNoParameters) {
				copyVirtualNumbers.add(virtualNoParameter.getParam());
			}
			logger.info("The set of copy virtual numbers are : "
					+ copyVirtualNumbers);
		}
		initializeBlackOut();
	}

	protected boolean init() {
		rbtDBManager = RBTDBManager.getInstance();
		rbtConnector = RBTConnector.getInstance();
		m_rbtCopyLikeUtils = new RBTCopyLikeUtils();
		HttpPerformanceMonitor httpPerformanceMonitor = null;
		Parameters parameter = CacheManagerUtil.getParametersCacheManager()
				.getParameter("GATHERER", "pir.httpHits.enable", "false");
		if (parameter.getValue().equalsIgnoreCase("true")) {
			String componentName = CopyBootstrapOzonized.COMPONENT_NAME;
			httpPerformanceMonitor = PerformanceMonitorFactory
					.newHttpPerformanceMonitor(componentName,
							"Http Performance Monitor",
							PerformanceDataType.LONG, "Milliseconds");
		}
		initRBTHttpClient(httpPerformanceMonitor);
		return true;
	}

	@Override
	public void run() {
		String keyPressed = "NA";
		boolean isProcessed = false;
		String extraInfoStr = viralData.extraInfo();
		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(extraInfoStr);
		String m_localType = "INCIRCLE";
		int cat = 26;
		int status = 1;
		String wavFile = null;
		StringBuffer wavFileBuf = new StringBuffer();
		StringBuffer catTokenBuf = new StringBuffer();
		StringBuffer catNameBuf = new StringBuffer();
		StringBuffer classTypeBuffer = new StringBuffer();
		StringBuffer statusBuf = new StringBuffer();
		StringBuffer setForCallerBuf = new StringBuffer();
		StringBuffer vrbtBuf = new StringBuffer();
		boolean isVirtualNo = false;
		String clipID = viralData.clipID();
		Clip clip = null;
		Category category = null;
		String catInfo = null;
		if (copyVirtualNumbers != null && copyVirtualNumbers.size() > 0) {
			if (copyVirtualNumbers.contains(viralData.subID())) {
				isVirtualNo = true;
				logger.info("match found");
			}
		}
		Subscriber subscriber = m_rbtCopyLikeUtils.getSubscriber(viralData
				.subID());
		String selectedBy = viralData.selectedBy();
		Subscriber callerSub = m_rbtCopyLikeUtils.getSubscriber(viralData
				.callerID());
		boolean isBlockedSub = rbtDBManager.isTotalBlackListSub(viralData
				.callerID());
		if (isBlockedSub) {
			logger.debug(viralData.callerID()
					+ " user is a block listed user so we are dropping the request");
			//removeLikeViralrecord(viralData);
			return;
		} else {
			if (viralInfoMap != null
					&& viralInfoMap.containsKey(iRBTConstant.KEYPRESSED_ATTR))
				keyPressed = viralInfoMap.get(iRBTConstant.KEYPRESSED_ATTR);
			logger.debug("Validating non copy contents for like key pressed");
			if (clipID != null)
				cat = getClipCopyDetails(clipID, wavFileBuf, catTokenBuf,
						catNameBuf, classTypeBuffer, statusBuf,
						setForCallerBuf, isVirtualNo, vrbtBuf);
			logger.info("Got the category, category: " + cat + ", for clipID: "
					+ clipID);
			if (wavFileBuf.toString().trim().length() > 0
					&& wavFileBuf.toString().trim().indexOf(">") != -1) {
				cat = Integer.parseInt(wavFileBuf.toString().substring(
						wavFileBuf.indexOf(">") + 1));
				wavFileBuf.delete(wavFileBuf.indexOf(">"), wavFileBuf.length());
				category = rbtConnector.getMemCache().getCategory(cat);
				if (category != null) {
					catNameBuf = new StringBuffer(category.getCategoryName());
				}
			} else {
				category = rbtConnector.getMemCache().getCategory(cat);
			}
			logger.debug("Got the category: " + category);
			if ((clipID == null || (clipID.toUpperCase().indexOf("DEFAULT") != -1 && clipID
					.toUpperCase().indexOf("MISSING") == -1))
					&& defaultClipWavName != null) {
				wavFileBuf = new StringBuffer(defaultClipWavName);
				logger.info("Settinf wavFileBuffer as default :"
						+ defaultClipWavName);
			}

			try {
				status = Integer.parseInt(statusBuf.toString().trim());
				if (RBTParametersUtils.getParamAsBoolean("DAEMON",
						"CALLER_ID_HIGHER_PRIORITY", "FALSE")) {
					if (status == 91) {
						status = 1;
					} else if (status == 92) {
						status = 80;
					}
				}
			} catch (Exception e) {
				status = 1;
			}
			wavFile = wavFileBuf.toString().trim();
			if (wavFile != null && wavFile.length() > 0 && status != 90
					&& status != 99)
				clip = rbtConnector.getMemCache().getClipByRbtWavFileName(
						wavFile);
			String called = viralData.subID();

			if (clip != null && clip.getContentType() != null) {
				List<String> contentTypes = Arrays.asList(RBTParametersUtils
						.getParamAsString("GATHERER",
								"COPY_NON_SUPPORTED_CONTENT_TYPES", "")
						.toUpperCase().split(","));
				if (contentTypes.contains(clip.getContentType().toUpperCase())) {
					logger.info("Like is send by the user for the non copy content types");
					writeCDRLogForLike(viralData, "LIKEFAILED", keyPressed,
							"LIKEFAILED", m_localType, false,
							subscriber.getCircleID(), clip.getClipId());
					//removeLikeViralrecord(viralData);
					isProcessed = true;
				}
			}

			if (null != category) {
				catInfo = category
						.getCategoryInfo(CategoryInfoKeys.CONTENT_TYPE);
			}
			if (!isProcessed) {
				boolean isNonCopyContent = isNonCopyContent(clipID,
						catTokenBuf.toString(), clip, status, wavFile,
						isVirtualNo, callerSub, subscriber, keyPressed,
						category.getCategoryTpe(), catInfo, selectedBy);
				Category category1 = category;
				boolean isShuffleCategory = m_rbtCopyLikeUtils
						.isShuffleCategory(String.valueOf(category1
								.getCategoryId()));
				String subscriberStatus = callerSub.getStatus();
				if (isNonCopyContent || subscriberStatus != null
						&& subStatusesBlockedForCopy.contains(subscriberStatus)) {
					logger.info("The content is non copy content.");
					logger.info("Like is send by the user for the non copy content");
					writeCDRLogForLike(viralData, "LIKEFAILED", keyPressed,
							"LIKEFAILED", m_localType, false,
							subscriber.getCircleID(), clip.getClipId());
					//removeLikeViralrecord(viralData);
					return;
				} else {
					long likedSubsriberSongcount = rbtDBManager
							.getLikedSubsciberSongCount(
									subscriber.getSubscriberID(),
									clip.getClipId(), -1);
					logger.info("likedSubsriberSongcount value: "
							+ likedSubsriberSongcount);
					boolean isLocalCircle = false;
					boolean isSmsSend = false;
					boolean useDNDSmsUrl = RBTParametersUtils
							.getParamAsBoolean("GATHERER", "USE_DND_SMS_URL",
									"FALSE");
					/****** Start:Logic for Sending B party SMS. ****/
					isLocalCircle = isLocalCircle(subscriber);
					if (isLocalCircle) {
						try {
							isSmsSend = validateSMSSend(clip, category,
									subscriber, called, category1,
									likedSubsriberSongcount,
									"LIKE_SMS_FOR_CALLEDSUB", useDNDSmsUrl,
									isVirtualNo);
							if (!isSmsSend) {
								logger.info("message hasn't  been successfully send for B party :"
										+ subscriber.getSubscriberID());
								rbtDBManager.insertViralSMSTable(
										viralData.subID(),
										viralData.sentTime(), "LIKEERROR",
										viralData.callerID(),
										viralData.clipID(), 1,
										viralData.selectedBy(),
										viralData.setTime(),
										viralData.extraInfo());
								logger.info("Successfully added viraldata. "
										+ ", viralData: " + viralData);
								writeCDRLogForLike(viralData, "LIKED",
										keyPressed, "SMS_CALLEDLIKEERROR",
										m_localType, true,
										subscriber.getCircleID(),
										clip.getClipId());
								logger.info(" written CDR logs record will not be removed from Viral retry happens");
								return;
							}
							logger.info("Like is accepted and send a message to the caller is Successful to B paty: "
									+ subscriber.getSubscriberID());
							if (!updateLikeCountForSubscriber(clip, category,
									subscriber, isShuffleCategory)) {
								writeCDRLogForLike(viralData, "LIKED",
										keyPressed, "DB_LIKEERROR",
										m_localType, true,
										subscriber.getCircleID(),
										clip.getClipId());
								logger.info(" written CDR logs record will not be removed from Viral retry happens");
							}
							logger.info("Like is accepted and send a message to called updated count and written CDR also");
							writeCDRLogForLike(viralData, "LIKED", keyPressed,
									"LIKED", m_localType, false,
									subscriber.getCircleID(), clip.getClipId());
						} catch (Exception e) {
							logger.info("message hasn't  been successfully send for B party :"
									+ subscriber.getSubscriberID());
							rbtDBManager.insertViralSMSTable(viralData.subID(),
									viralData.sentTime(), "LIKEERROR",
									viralData.callerID(), viralData.clipID(),
									1, viralData.selectedBy(),
									viralData.setTime(), viralData.extraInfo());
							logger.info("Successfully added viraldata. "
									+ ", viralData: " + viralData);
							writeCDRLogForLike(viralData, "LIKED", keyPressed,
									"SMS_CALLEDLIKEERROR", m_localType, true,
									subscriber.getCircleID(), clip.getClipId());
							logger.info(" written CDR logs record will not be removed from Viral retry happens");
							return;
						}
					}
					/**** End :Logic for Sending B party SMS. ***/
					/****** Start:Logic for Sending A party SMS. ****/
					if (callerSub.getCircleID() != null
							&& !callerSub.getCircleID().isEmpty()) {
						isLocalCircle = isLocalCircle(callerSub);
						if (isLocalCircle) {
							try {
								isSmsSend = validateSMSSend(clip, category,
										callerSub, called, category1,
										likedSubsriberSongcount,
										"LIKE_SMS_FOR_CALLERSUB", useDNDSmsUrl,
										false);
								if (!isSmsSend) {
									logger.info("message hasn't  been successfully send for A party :"
											+ callerSub.getSubscriberID());
									writeCDRLogForLike(viralData, "LIKED",
											keyPressed, "SMS_CALLERLIKEERROR",
											m_localType, true,
											subscriber.getCircleID(),
											clip.getClipId());
									return;
								}
								logger.info("Like is accepted and send a message to the caller is Successful A Party:"
										+ callerSub.getSubscriberID());
							} catch (Exception e) {
								logger.info("message hasn't  been successfully send for A party :"
										+ callerSub.getSubscriberID());
								writeCDRLogForLike(viralData, "LIKED",
										keyPressed, "SMS_CALLERLIKEERROR",
										m_localType, true,
										subscriber.getCircleID(),
										clip.getClipId());
							}
						} else {
							// redirection for circle
							String type = CopyProcessorUtils
									.findSMSTypeFromDTMF(keyPressed, clipID);
							logger.info("only like key is pressed then we need to do redirection");
							if (type != null && type.equalsIgnoreCase(LIKE)) {
								redirectLikeRequestToCircle(callerSub);
							}
						}
					} else {
						logger.info("Caller is not a local operator so we are not sending any SMS");
					}
					//removeLikeViralrecord(viralData);
					/****** End:Logic for Sending A party SMS. ****/
				}
			}
		}
	}

	/**
	 * This function will validate and send the sms to the user.
	 * 
	 * @param clip
	 * @param category
	 * @param subscriber
	 * @param called
	 * @param category1
	 * @param language
	 * @param likedSubsriberSongcount
	 * @param isBlockOutPeriod
	 * @return
	 * @throws Exception
	 */
	protected boolean validateSMSSend(Clip clip, Category category,
			Subscriber subscriber, String called, Category category1,
			long likedSubsriberSongcount, String smsTextType,
			boolean useDNDSmsUrl, boolean isVirtualNo) throws Exception {
		boolean isSmsSend = false;
		if (ViralPromotion.isBlackOutPeriodNow()
				|| checkDNDURL(subscriber.getSubscriberID()) || isVirtualNo) {
			logger.info("Blackout Period, so not processing the request or DND is Enabled for the subscriber:"
					+ subscriber.getSubscriberID());
			return true;
		}
		if (useDNDSmsUrl) {
			isSmsSend = sendSMSviaPromoTool(
					subscriber,
					m_rbtCopyLikeUtils.getSubstituedSMS(
							RBTCopyLikeUtils
									.getSMSText(
											"GATHERER",
											smsTextType,
											"You’ve listened & liked %CONTENT_NAME tune. This song has %LIKECOUNT LIKES in Hello Tunes.",
											subscriber.getLanguage()),
							category1.getCategoryName() == null ? ""
									: category1.getCategoryName(), called,
							null, null, null, clip, category, null,
							(likedSubsriberSongcount + 1),null,null));
		} else {
			isSmsSend = sendSMS(
					subscriber,
					m_rbtCopyLikeUtils.getSubstituedSMS(
							RBTCopyLikeUtils
									.getSMSText(
											"GATHERER",
											smsTextType,
											"You’ve listened & liked %CONTENT_NAME tune. This song has %LIKECOUNT LIKES in Hello Tunes.",
											subscriber.getLanguage()),
							category1.getCategoryName() == null ? ""
									: category1.getCategoryName(), called,
							null, null, null, clip, category, null,
							(likedSubsriberSongcount + 1),null,null));
		}
		return isSmsSend;
	}

	/**
	 * This redirect to the different circle
	 * 
	 * @param viralInfoMap
	 * @param callerSub
	 */
	protected void redirectLikeRequestToCircle(Subscriber callerSub) {
		RBTHttpClient rbtHttpClient = this.rbtHttpClient;
		logger.info("Processing non local copy. subID: " + viralData.subID()
				+ ", callerID: " + viralData.callerID() + ", clipID: "
				+ viralData.clipID() + ", sentTime: " + viralData.sentTime());
		String circleId = callerSub.getCircleID();
		String url = getURL(circleId);
		url = Tools.findNReplaceAll(url, "rbt_sms.jsp", "");
		url = Tools.findNReplaceAll(url, "?", "");
		url = url + "rbt_cross_copy.jsp?subscriber_id=" + viralData.subID()
				+ "&caller_id=" + viralData.callerID() + "&clip_id="
				+ viralData.clipID() + "&sel_by=" + viralData.selectedBy()
				+ "&sms_type=" + viralData.type();

		HashMap<String, String> viralInfoMap = DBUtility
				.getAttributeMapFromXML(viralData.extraInfo());
		if (viralInfoMap != null && viralInfoMap.containsKey(KEYPRESSED_ATTR)
				&& url != null) {
			String keypressed = viralInfoMap.get(KEYPRESSED_ATTR);
			if (keypressed != null && keypressed.length() > 0
					&& !keypressed.equalsIgnoreCase("null"))
				url = url + "&keypressed=" + keypressed;
		}
		String sourceClipName = "";
		if (viralInfoMap != null
				&& viralInfoMap.containsKey(SOURCE_WAV_FILE_ATTR))
			sourceClipName = viralInfoMap.get(SOURCE_WAV_FILE_ATTR);
		if (url != null && sourceClipName != null
				&& !sourceClipName.equalsIgnoreCase("")
				&& url.indexOf("songname") == -1)
			url = url + "&songname=" + sourceClipName;

		HttpResponse httpResponse = null;
		try {
			httpResponse = rbtHttpClient.makeRequestByGet(url, null);
		} catch (Exception e) {
			logger.info("like request failed to hit for the following url "
					+ url);
		}
		String response = null;
		if (httpResponse != null)
			response = httpResponse.getResponse();

		if (response != null
				&& (response.indexOf("SUCCESS") != -1 || response
						.indexOf("SUCESS") != -1)) {
			logger.info("like request successful for the following url " + url);
		}
	}

	/**
	 * This method will update the like count for the song & subscriber
	 * 
	 * @param clip
	 * @param category
	 * @param subscriber
	 * @param likedSubsriberSongcount
	 */
	protected boolean updateLikeCountForSubscriber(Clip clip,
			Category category, Subscriber subscriber, boolean isShuffleCategory) {
		synchronized (RBTLikeDaemon.class) {
			boolean isUpdated = false;
			long likedSubsriberSongcount = rbtDBManager
					.getLikedSubsciberSongCount(subscriber.getSubscriberID(),
							clip.getClipId(), -1);

			long likedShuffleSongcount = rbtDBManager
					.getLikedSubsciberSongCount(subscriber.getSubscriberID(),
							clip.getClipId(), category.getCategoryId());

			if (likedShuffleSongcount == 0) {
				logger.info("insertSubscriberLikedSongCount count ");
				isUpdated = rbtDBManager.insertSubscriberLikedSongCount(
						subscriber.getSubscriberID(), clip.getClipId(),
						category.getCategoryId(), 1);
			}

			if (likedSubsriberSongcount > 0) {
				logger.info("updateSubscriberLikedSongCount count: ");
				isUpdated = rbtDBManager.updateSubscriberLikedSongCount(
						subscriber.getSubscriberID(), clip.getClipId(), -1,
						(likedSubsriberSongcount + 1));
			}
			if (isUpdated) {
				long likedSongcount = rbtDBManager.getLikedSongCount(clip
						.getClipId());
				if (likedSongcount > 0) {
					logger.info("updateLikedSongCount count:"
							+ (likedSongcount + 1));
					isUpdated = rbtDBManager.updateLikedSongCount(
							clip.getClipId(), (likedSongcount + 1));
				} else {
					logger.info("insertLikedSong count ");
					isUpdated = rbtDBManager.insertLikedSong(clip.getClipId(),
							1);
				}
			}
			return isUpdated;
		}
	}

	private boolean sendSMS(Subscriber subscriber, String sms) throws Exception {
		boolean isSMSSend = false;
		if (sms != null) {
			String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility
					.getSenderNumberbyType("GATHERER",
							subscriber.getCircleID(), "SENDER_NO");
			String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility
					.getBrandName(subscriber.getCircleID());
			if (subscriptionClassOperatorNameMap != null) {
				sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.findNReplaceAll(sms, "%NO_SENDER", senderNumber);
				sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.findNReplaceAll(sms, "%BRAND_NAME", brandName);
			}
			isSMSSend = Tools.sendSMS(senderNumber,
					subscriber.getSubscriberID(), sms, false);
		}
		return isSMSSend;
	}

	private boolean sendSMSviaPromoTool(Subscriber subscriber, String sms)
			throws Exception {
		boolean isSMSSend = false;
		if (sms != null) {
			String senderNumber = com.onmobile.apps.ringbacktones.provisioning.common.Utility
					.getSenderNumberbyType("GATHERER",
							subscriber.getCircleID(), "STAR_OBTAIN_SENDER_NO");
			String brandName = com.onmobile.apps.ringbacktones.provisioning.common.Utility
					.getBrandName(subscriber.getCircleID());
			if (subscriptionClassOperatorNameMap != null) {
				sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.findNReplaceAll(sms, "%NO_SENDER", senderNumber);
				sms = com.onmobile.apps.ringbacktones.provisioning.common.Utility
						.findNReplaceAll(sms, "%BRAND_NAME", brandName);
			}

			isSMSSend = Tools.sendSMS(senderNumber,
					subscriber.getSubscriberID(), sms);
		}
		return isSMSSend;
	}

	private boolean isNonCopyContent(String clipID, String catID, Clip clip,
			int status, String wavFile, boolean isVirtualNo,
			Subscriber callerSub, Subscriber calledSub, String keyPressed,
			int catType, String catInf, String selectedBy) {
		logger.info("Entered with params: clipID = " + clipID + ", catID = "
				+ catID + ", status = " + status + ", wavFile = " + wavFile
				+ ", clip = " + clip + ",catInf= " + catInf + ",catType= "
				+ catType);
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String currentTime = sdf.format(currentDate);
		boolean isNotCopyShuffle = false;
		String catTypes = RBTParametersUtils.getParamAsString("GATHERER",
				"COPY_BLOCKED_SHUFFLE_CATEGORY_TYPES", null);
		isNotCopyShuffle = m_rbtCopyLikeUtils.isBlockedCatTypeOrCatInfo(
				isNotCopyShuffle, catTypes, catType, catInf);
		String subscriptionClass = callerSub.getSubscriptionClass();
		if (wavFile != null && wavFile.equalsIgnoreCase("MISSING")) {
			logger.info("Missing content.");
			return true;
		}
		if (isNotCopyShuffle) {
			logger.info("Like failed for blocked category type");
			return true;
		}
		if (subscriptionClass != null
				&& m_rbtCopyLikeUtils.isTNBuser(subscriptionClass)) {
			List<String> modes = ListUtils.convertToList(
					CacheManagerUtil
							.getParametersCacheManager()
							.getParameterValue(iRBTConstant.COMMON,
									"VODAFONE_UPGRADE_CONSENT_MODES", "")
							.toUpperCase(), ",");
			if (modes.contains("COPY")) {
				return true;
			}
			return false;
		}
		if (clipID != null
				&& clipID.toUpperCase().indexOf("DEFAULT_" + currentTime) != -1) {
			if (!RBTParametersUtils.getParamAsBoolean("GATHERER",
					"ALLOW_POLL_COPY", "FALSE")) {
				logger.info("Polling RBT copy failed");
				return true;
			} else {
				return false;
			}
		}

		/*
		 * if ((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1)
		 * && !(m_rbtCopyLikeUtils.allowDefaultCopyUserHasSelection( callerSub,
		 * calledSub))) { logger.info("default song copy failed"); return true;
		 * }
		 */

		if ((clipID == null || clipID.toUpperCase().indexOf("DEFAULT") != -1)
				&& !(RBTParametersUtils.getParamAsBoolean("GATHERER",
						"COPY_DEFAULT", "FALSE") || RBTParametersUtils
						.getParamAsBoolean("GATHERER", "INSERT_DEFAULT_SEL",
								"FALSE"))) {
			logger.info("default song copy failed");
			return true;
		}

		String contentName = confAzaanCopticDoaaSubTpeContentNameMap
				.get(wavFile);

		if (azaanWavFileName != null
				&& azaanWavFileName.equalsIgnoreCase(wavFile)
				&& rbtDBManager.azaanDefaultCosId != null) {
			logger.info("user copied azaan in cos model, activating on azaan cos="
					+ rbtDBManager.azaanDefaultCosId);
			return false;
		} else if (contentName != null
				&& confAzaanCopticDoaaCosIdSubTypeMap.get(contentName) != null) {
			logger.info("user copied either COPTIC in cos model ,activating on coptic cos ="
					+ confAzaanCopticDoaaCosIdSubTypeMap.get(contentName));
			return false;
		}

		if (azaanWavFileName != null
				&& azaanWavFileName.equalsIgnoreCase(wavFile)
				&& null != azaanCategoryId) {

			logger.info("Content is valid, subscriber copied azaan"
					+ " in category model. Calling SubscriberId: "
					+ callerSub.getSubscriberID() + ", Called SubscriberId: "
					+ calledSub.getSubscriberID());
			return false;
		}

		if ("RADIO".equalsIgnoreCase(wavFile)) {

			logger.info("Content is valid, subscriber copied RADIO"
					+ " in category model. Calling SubscriberId: "
					+ callerSub.getSubscriberID() + ", Called SubscriberId: "
					+ calledSub.getSubscriberID());
			return false;
		}

		if (clip == null && status != 90) {
			logger.info("Clip is null for wavFile " + wavFile);
			return true;
		}
		if (Arrays.asList(
				RBTParametersUtils.getParamAsString("GATHERER",
						"COPY_BLOCKED_CATEGORY_IDS", "1,99").split(","))
				.contains(catID)) {
			return true;
		}
		if (clip != null
				&& Arrays.asList(
						RBTParametersUtils.getParamAsString("GATHERER",
								"COPY_BLOCKED_CLIP_IDS", "").split(","))
						.contains("" + clip.getClipId()))
			return true;
		// allow expired clips to send the sms for like.
		else if (clipID != null
				&& !m_rbtCopyLikeUtils.isShuffleCategory(catID)
				&& !RBTParametersUtils.getParamAsBoolean("GATHERER",
						"IS_NORMAL_COPY_ALLOWED", "TRUE"))
			return true;
		else if (clip != null
				&& "EMOTION_UGC".equalsIgnoreCase(clip.getContentType())) {
			logger.info("User tried to copy EmotionUgc content, Not allowed");
			return true;
		} else if (status != 1 && status != 75 && status != 79 && status != 80
				&& status != 90 && status != 91 && status != 92 && status != 95
				&& status != 81)
			return true;
		return false;
	}

	private int getClipCopyDetails(String clipID, StringBuffer wavFileBuf,
			StringBuffer catTokenBuf, StringBuffer catNameBuf,
			StringBuffer classTypeBuffer, StringBuffer statusBuf,
			StringBuffer setForCallerbuf, boolean isVirtualNo,
			StringBuffer vrbtBuf) {
		logger.info("Getting category id. clipIDToken: " + clipID);
		int cat = 26;
		StringTokenizer stk = new StringTokenizer(clipID, ":");
		if (stk.hasMoreTokens()) {
			wavFileBuf.append(stk.nextToken());
			logger.debug("Got wavFileBuf: " + wavFileBuf.toString());
		}

		if (stk.hasMoreTokens()) {
			String catToken = stk.nextToken();
			if (catToken.toUpperCase().startsWith("S")) {
				catToken = catToken.substring(1);
			}

			boolean isShuffleCategory = m_rbtCopyLikeUtils
					.isShuffleCategory(catToken);
			logger.debug("Got catToken: " + catToken + ", wavFileBuf: "
					+ wavFileBuf.toString() + ", isShuffleCatetory: "
					+ isShuffleCategory);

			// Return the category if the category type is shuffle.
			if (isShuffleCategory) {
				logger.debug("Category id: " + catToken
						+ " is shuffle.  Checking wav file: "
						+ wavFileBuf.toString());
				// catToken = catToken.substring(1);
				if ("RADIO".equalsIgnoreCase(wavFileBuf.toString())) {
					cat = Integer.parseInt(catToken);
					logger.debug("Returning Category id: " + catToken
							+ " is shuffle and wav file is RADIO. ");
				}

				if (isVirtualNo
						|| !RBTParametersUtils.getParamAsBoolean("GATHERER",
								"COPY_SHUFFLE_SONG_ONLY", "FALSE"))
					try {
						cat = Integer.parseInt(catToken);
					} catch (Exception e) {
						cat = 0;
					}
			} else {
				logger.warn("Category is not shuffle type. " + catToken
						+ ", so not returing category id.");
			}

			// Return the category if the wavFile is azaan wav file.
			String azaanWavFile = RBTParametersUtils.getParamAsString(
					"GATHERER", "AZAAN_WAV_FILE_NAME", null);
			if (null != azaanWavFile
					&& azaanWavFile.equals(wavFileBuf.toString())) {
				if (null != azaanCategoryId) {
					cat = Integer.parseInt(azaanCategoryId);
					catToken = azaanCategoryId;
					logger.info("WavFile is Azaan WavFile, updating azaan catTokenBuf: "
							+ catToken + ", cat: " + cat);
				} else {
					logger.warn("Azaan Category is not configured. Please configure "
							+ "GATHERER, AZAAN_CATEGORY_ID");
				}
			} else {
				logger.debug("AZAAN_WAV_FILE_NAME is not configured"
						+ ", so not returing azaan category id.");
			}

			catTokenBuf.append(catToken);
		}

		if (stk.hasMoreTokens()) {
			StringTokenizer stkStatus = new StringTokenizer(stk.nextToken(),
					"|");
			if (stkStatus.hasMoreTokens())
				statusBuf.append(stkStatus.nextToken());
			if (stkStatus.hasMoreTokens())
				setForCallerbuf.append(stkStatus.nextToken());
		}

		if (stk.hasMoreTokens()) {
			String vrbt = stk.nextToken();
			if (vrbt.equalsIgnoreCase("VRBT")
					&& m_vrbtCatIdSubSongSrvKeyMap.containsKey(catTokenBuf
							.toString())) {
				vrbtBuf.append(vrbt);
			}
		}
		logger.info("Returning cat: " + cat);
		return cat;
	}

	public void getDefaultClip() {
		logger.info("Entering");
		int defaultClipId = -1;
		Clip clip = null;
		defaultClipId = RBTParametersUtils.getParamAsInt("COMMON",
				"DEFAULT_CLIP", -1);
		if (defaultClipId > -1)
			clip = rbtConnector.getMemCache().getClip(defaultClipId);
		if (clip != null)
			defaultClipWavName = clip.getClipRbtWavFile();
		logger.info("Exiting. defaultClipWavName is " + defaultClipWavName);
	}

	public void writeCDRLogForLike(ViralSMSTable vst, String reason,
			String keyPressed, String likeType, String subType,
			boolean isErrorLog, String circleId, int clipId) {
		logger.info("writeCDRLog for like feature entered with reason = "
				+ reason + ", for subscriber = " + vst.callerID());
		// TIMESTAMP,TRANSTYPE,CALLED__MSISDN,CALLER__MSISDN,CALLER__TYPE,LIKE__TIME,KEY__PRESSED,LIKE_TYPE,SONG,CALLED_OPERATOR,CALLED_CIRCLEID,CLIP_ID
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				iRBTConstant.kDateFormatwithTime);
		String currentTime = dateFormat.format(new Date());
		if (vst.callerID() == null)
			subType = "UNKNOWN";
		if (RBTParametersUtils.getParamAsBoolean("GATHERER", "WRITE_TRANS",
				"FALSE")) {
			if (isErrorLog) {
				cdr_error_logger.info(currentTime
						+ ",LIKE,"
						+ vst.subID()
						+ ","
						+ vst.callerID()
						+ ","
						+ subType
						+ ","
						+ Tools.getFormattedDate(vst.sentTime(),
								"yyyy-MM-dd HH:mm:ss") + "," + keyPressed + ","
						+ likeType + "," + vst.clipID() + ",Airtel," + circleId
						+ "," + clipId);
			} else {
				cdr_logger.info(currentTime
						+ ",LIKE,"
						+ vst.subID()
						+ ","
						+ vst.callerID()
						+ ","
						+ subType
						+ ","
						+ Tools.getFormattedDate(vst.sentTime(),
								"yyyy-MM-dd HH:mm:ss") + "," + keyPressed + ","
						+ likeType + "," + vst.clipID() + ",Airtel," + circleId
						+ "," + clipId);
			}
		}
	}

	private boolean removeLikeViralrecord(ViralSMSTable vst) {
		return RBTDBManager.getInstance().removeViralPromotion(vst.subID(),
				vst.callerID(), vst.sentTime(), vst.type());
	}

	private boolean isLocalCircle(Subscriber sub) {
		if (sub.getCircleID() != null && !sub.getCircleID().isEmpty()) {
			SitePrefix sitePrefix = CacheManagerUtil
					.getSitePrefixCacheManager().getSitePrefixes(
							sub.getCircleID());
			if (sitePrefix != null && sitePrefix.getSiteUrl() == null
					|| (sitePrefix.getSiteUrl().isEmpty())) {
				return true;
			}
		}
		return false;
	}

	private String getURL(String circleId) {
		SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager()
				.getSitePrefixes(circleId);
		return (sitePrefix != null) ? sitePrefix.getSiteUrl() : null;
	}

	private void initRBTHttpClient(HttpPerformanceMonitor httpPerformanceMonitor) {
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setHttpPerformanceMonitor(httpPerformanceMonitor);

		rbtHttpClient = new RBTHttpClient(httpParameters);
	}

	public boolean checkDNDURL(String callerID) {
		String umpDNDUrl = RBTParametersUtils.getParamAsString("VIRAL",
				"UMP_DND_URL_FOR_VIRAL_PROMOTION", null);
		if (umpDNDUrl != null) {
			umpDNDUrl = umpDNDUrl.replaceFirst("%SUBSCRIBER_ID%", callerID);
			StringBuffer response = new StringBuffer();
			boolean success = Tools.callURL(umpDNDUrl, new Integer(-1),
					response, false, null, -1, false, 2000);

			if (response.toString().trim().equalsIgnoreCase("TRUE") || !success) {
				// if UMP url returns 'TRUE' or error status code or if UMP
				// server is down, number is considered as DND
				logger.info("Not promoting as the number is DND in UMP");
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private static void initializeBlackOut() {
		blackoutTimesList = new ArrayList<List<Integer>>();
		for (int i = 0; i <= 7; i++)
			blackoutTimesList.add(new ArrayList<Integer>());

		String blackoutTimes = RBTParametersUtils.getParamAsString("VIRAL",
				"BLACK_OUT_PERIOD", null);
		if (blackoutTimes == null) {
			logger.info("No BlackOut Configured");
			return;
		}

		String[] blackoutTokens = blackoutTimes.split(",");
		for (String blackout : blackoutTokens) {
			if (!blackout.contains("[")) {
				logger.info("No BlackOut Time Configured" + blackout);
				continue;
			}

			List<Integer> days = getDays(blackout.substring(0,
					blackout.indexOf("[")));
			if (days != null && days.size() > 0) {
				List<Integer> times = getTimes(blackout.substring(blackout
						.indexOf("[")));
				for (int j = 0; j < days.size(); j++)
					blackoutTimesList.set(days.get(j).intValue(), times);
			}
		}

		logger.info("blackoutTimesList initialized " + blackoutTimesList);
	}

	private static List<Integer> getDays(String string) {
		List<Integer> daysList = new ArrayList<Integer>();

		Map<String, Integer> days = new HashMap<String, Integer>();
		days.put("SUN", 1);
		days.put("MON", 2);
		days.put("TUE", 3);
		days.put("WED", 4);
		days.put("THU", 5);
		days.put("FRI", 6);
		days.put("SAT", 7);

		if (string.contains("-")) {
			try {
				String day1 = string.substring(0, string.indexOf("-"));
				String day2 = string.substring(string.indexOf("-") + 1);
				if (!days.containsKey(day1) || !days.containsKey(day2)) {
					logger.info("Invalid week specified !!!!" + string);
					return null;
				}

				int startDay = days.get(day1);
				int endDay = days.get(day2);

				if (endDay > startDay) {
					for (int t = startDay; t <= endDay; t++)
						daysList.add(t);
				} else {
					for (int t = startDay; t <= 7; t++)
						daysList.add(t);

					for (int t = 1; t <= endDay; t++)
						daysList.add(t);
				}
			} catch (Throwable e) {
				logger.debug(e.getMessage(), e);
			}
		} else {
			if (days.containsKey(string))
				daysList.add(days.get(string));
			else
				logger.info("Invalid week specified !!!!" + string);
		}

		logger.info("DaysList" + daysList);
		return daysList;
	}

	private static List<Integer> getTimes(String string) {
		List<Integer> timesList = new ArrayList<Integer>();

		string = string.substring(1, string.length() - 1);
		String[] tokens = string.split(";");
		for (String token : tokens) {
			if (token.contains("-")) {
				try {
					int startTime = Integer.parseInt(token.substring(0,
							token.indexOf("-")));
					int endTime = Integer.parseInt(token.substring(token
							.indexOf("-") + 1));

					if (startTime > 23 || endTime > 23) {
						logger.info("Invalid time specified !!!!" + string);
						continue;
					} else if (endTime > startTime)
						for (int t = startTime; t <= endTime; t++)
							timesList.add(t);
					else {
						for (int t = startTime; t <= 23; t++)
							timesList.add(t);

						for (int t = 0; t <= endTime; t++)
							timesList.add(t);
					}
				} catch (Throwable e) {
					logger.debug(e.getMessage(), e);
				}
			} else {
				try {
					int n = Integer.parseInt(token);
					if (n >= 0 && n <= 23)
						timesList.add(n);
					else
						logger.info("Invalid time specified !!!!" + string);
				} catch (Throwable t) {
					logger.info("Invalid time specified !!!!" + string);
				}
			}
		}
		return timesList;
	}

	public static boolean isBlackOutPeriodNow() {
		Calendar calendar = Calendar.getInstance();
		List<Integer> blackout = blackoutTimesList.get(calendar
				.get(Calendar.DAY_OF_WEEK));

		if (logger.isDebugEnabled())
			logger.debug("BlackOut checked against " + blackout);

		return (blackout.contains(calendar.get(Calendar.HOUR_OF_DAY)));
	}
}
