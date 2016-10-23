package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;


import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class ClipStatusDaemon extends Thread implements Constants, iRBTConstant {
	private static Logger logger = Logger.getLogger(ClipStatusDaemon.class);
	RBTDaemonManager mainDaemonThread = null;
	private static Long sleepTime = 4000L;
	private static int connectionTimeout = 3000;
	private static int socketTimeout = 3000;
	private static RBTDBManager rbtDBManager = null;
	private static RBTCacheManager rbtCacheManager = null;
	private static ParametersCacheManager rbtParamCacheManager = null;
//	private static ThreadPoolExecutor executor = null;
	final static String STATUS_ERROR = "ERROR";
	final static String STATUS_RETRY = "RETRY";
	private static String sourcePath = null;
	private static String fullTrackSongPath = null;
	private static Map<String, String> circleIdsMap = null;
	private static HashMap<String, ArrayList<String>> circleIdToPlayerUrlMap = new HashMap<String, ArrayList<String>>();

	private static RBTHttpClient rbtHttpClient = null;
	private boolean isAlive = false; 
	private static int limit = 1;
	
	static {
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		sourcePath = rbtParamCacheManager.getParameterValue("COMMON",
				"CENTRAL_CLIP_STORAGE_PATH", null);
		
		fullTrackSongPath = rbtParamCacheManager.getParameterValue("COMMON",
				"CENTRAL_FULL_TRACK_CLIP_STORAGE_PATH", null);
		
//       String limitStr = rbtParamCacheManager.getParameterValue("COMMON",
//				"CLIP_STATUS_DAEMON_CLIPS_FETCH_LIMIT", null);
//		if (null != limitStr) {
//			limit = Integer.parseInt(limitStr);
//		}
	}
	
	public ClipStatusDaemon() {
        setName("ClipStatusDaemonOzonized");
        isAlive = true;
        init();
		getCircleIdMap();
		getFeedUrl();
        logger.info("Starting ClipStatuskDaemon as Ozonized..");
	}

	ClipStatusDaemon(RBTDaemonManager mainDaemonThread) {
		try {
			this.mainDaemonThread = mainDaemonThread;
			setName("ClipStatusDaemon");
			init();
			getCircleIdMap();
			getFeedUrl();
		} catch (Exception e) {
			logger.error("Issue in creating ClipStatusDaemon", e);
		}
	}

	private void init() {
		rbtDBManager = RBTDBManager.getInstance();
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		sleepTime = Long.valueOf(rbtParamCacheManager.getParameterValue(
				"DAEMON", "CLIP_STATUS_SLEEP_TIME_MILLI", "10000"));
		rbtCacheManager = RBTCacheManager.getInstance();
	}

	// not to be sent == record my own clip,karaoke
	// to be sent===== corporate,shuffle.,profile,ugc,cricket
	// request date,processed date to be implemented later
	@Override
	public void run() {
		logger.debug("Going to start. mainDaemonThread" + mainDaemonThread
				+ ", isAlive: " + isAlive);
		while ((mainDaemonThread != null && mainDaemonThread.isAlive()) || isAlive) {
			try {
				int startFrom = 0;
				
				Map<String, String> unprocessedCirclesMap = rbtDBManager
						.getCirclesOfUnprocessedClips(startFrom, limit);

				if(null == unprocessedCirclesMap || (null != unprocessedCirclesMap && unprocessedCirclesMap.size() == 0)) {
					logger.warn("No records found. " + unprocessedCirclesMap);
				}
				
				while(null != unprocessedCirclesMap && unprocessedCirclesMap.size() > 0) {
					
					logger.debug("Fetched un processed records: " + unprocessedCirclesMap);
					
					process(unprocessedCirclesMap);
					
					try {
						logger.debug("Thread: " + this.getClass().getName()
								+ " is going to sleep for " + sleepTime);
						Thread.sleep(sleepTime);
						logger.info("Continue after sleep.");
					} catch (Exception e) {
						logger.error("Exception while running daemon. Exception: "
								+ e.getMessage(), e);
					}

					logger.debug("Processed records and fetching again. "
							+ "processsedCircles: " + unprocessedCirclesMap);

					startFrom = startFrom + limit;
					
					unprocessedCirclesMap = rbtDBManager
							.getCirclesOfUnprocessedClips(startFrom, limit);
					logger.debug("Next fetch un processed records: " + unprocessedCirclesMap);
				}
				
			} catch(Exception e) {
				logger.error("Exception while running daemon. Exception: "
						+ e.getMessage(), e);
			} catch(Throwable t) {
				logger.error("Throwable while running daemon. Throwable: "
						+ t.getMessage(), t);
			}
			
			try {
				logger.debug("Thread: " + this.getClass().getName()
						+ " is going to sleep.");
				Thread.sleep(sleepTime);
				logger.info("Continue after sleep.");
			} catch (InterruptedException e) {
				logger.error("InterruptedException: "
						+ e.getMessage(), e);
			}

		}
	}

	private void process(Map<String, String> unprocessedCirclesMap) {
		Set<Entry<String, String>> unprocessedCirclesSet = unprocessedCirclesMap
				.entrySet();
		for (Iterator<Entry<String, String>> itrSub = unprocessedCirclesSet
				.iterator(); itrSub.hasNext();) {
			Entry<String, String> eachEntrySub = itrSub.next();
			String clipWavFile = eachEntrySub.getKey();
			String unprocessedAndProcessedCircles = eachEntrySub
					.getValue();
			String processedCircles = null;
			String unprocessedCircles = null;
			if (unprocessedAndProcessedCircles != null) {
				String cir[] = unprocessedAndProcessedCircles
						.split("\\_");
				unprocessedCircles = cir[0];
				processedCircles = cir[1];
				if (unprocessedCircles == null
						|| unprocessedCircles.equalsIgnoreCase("null")
						|| unprocessedCircles.equalsIgnoreCase("")) {
					unprocessedCircles = null;
				}
				if (processedCircles == null
						|| processedCircles.equalsIgnoreCase("null")
						|| processedCircles.equalsIgnoreCase("")) {
					processedCircles = null;
				}
			}
			ExecutorService executor = Executors.newFixedThreadPool(1);
			Future<?> future = executor.submit(new TransferClipsToCircles(clipWavFile,
					unprocessedCircles, processedCircles));
			executor.shutdown();
			
			logger.info("Going to shut down ThreadPool. isProcessed: "
					+ future.isDone() + ", clipWavFile: " + clipWavFile);
		}
	}

	public static class TransferClipsToCircles implements Runnable {

		String clipWavFile = null;
		String pendingCircles = null;
		String transferredCircles = null;

		TransferClipsToCircles(String clipWavFile, String unprocessedCircles,
				String processedCircles) {
			this.clipWavFile = clipWavFile;
			this.pendingCircles = unprocessedCircles;
			this.transferredCircles = processedCircles;
		}

		@Override
		public void run() {
			try {
				String circleIds[] = null;
				if (pendingCircles != null)// NULL
					circleIds = pendingCircles.split(",");
				logger.info("Started processing clips. clipWavFile: "
						+ clipWavFile + ", pendingCircles: " + pendingCircles
						+ ", transferredCircles: " + transferredCircles);
				Clip clip = getClipByRBTWavFile(clipWavFile);
				if (clip == null) {
					boolean success = rbtDBManager.updateClipStatusByWavFile("2", clipWavFile);
					logger.warn("Clip not found, not processing and updating clip status to 2 clip: "
							+ clipWavFile + ", isUpdated: " + success);
					return;
				}
				if (circleIds != null) {
					Map<String, Boolean> tonePlayerUploadedMap = new HashMap<String, Boolean>();
					for (int i = 0; i < circleIds.length; i++) {
						String centralStoragePath = sourcePath;
						String fullTrackPath = fullTrackSongPath;
						ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
						String tempCircleID = circleIdsMap.get(circleIds[i]);
						logger.info("Processing for circle: " + circleIds[i]
								+ ", mapping circleId: " + tempCircleID
								+ ", in circleIdsMap: "+circleIdsMap);
						applicationDetailsRequest.setCircleID(tempCircleID);
						Site site = RBTClient.getInstance().getSite(
								applicationDetailsRequest); // CHECK NULL POINTER
						if (site == null) {
							logger.warn("Unable to process. No site configured"
									+ " in Site prefix for circleId: "
									+ tempCircleID);
							return;
						}

						if (centralStoragePath == null) {
							logger.warn("Unable to process. CENTRAL_CLIP_STORAGE_PATH"
									+ " is not configured in RBT_PARAMETERS table");
							return;
						}
						// TODO: Appending the file to the sourcePath for each
						// iteration of circleIds.
						// So, added if check for
						// !centralStoragePath.contains(clipWavFile).
						
						
						if (clip.getClipPreviewWavFilePath() != null) {
							if (!centralStoragePath.endsWith(File.separator))
								centralStoragePath = centralStoragePath
										+ File.separator;
							if (clip.getClipPreviewWavFilePath().startsWith(
									File.separator))
								centralStoragePath = centralStoragePath
										+ clip.getClipPreviewWavFilePath()
												.substring(1);
							else
								centralStoragePath = centralStoragePath
										+ clip.getClipPreviewWavFilePath();
						}
						
						
						String sliceWavFilePath = null;
						String fullTrackSong = clip.getClipInfo(ClipInfoKeys.CLIP_FULL_TRACK);
						if (clipWavFile.indexOf("rbt_slice") != -1 && fullTrackSong != null) {

							if (centralStoragePath.endsWith(File.separator))
								sliceWavFilePath = centralStoragePath + clipWavFile 
										+ ".wav";
							else
								sliceWavFilePath = centralStoragePath + File.separator
										+ clipWavFile + ".wav";
							
							//Get the full track song path from clip info table.
							if (!fullTrackPath.endsWith(File.separator)) {
								fullTrackPath = fullTrackPath
										+ File.separator;
							}
							if (clip.getClipInfo(ClipInfoKeys.CLIP_FULL_TRACK).startsWith(
									File.separator)) {
								fullTrackPath = fullTrackPath
										+ clip.getClipInfo(ClipInfoKeys.CLIP_FULL_TRACK)
												.substring(1);
							}
							else {
								fullTrackPath = fullTrackPath
										+ clip.getClipInfo(ClipInfoKeys.CLIP_FULL_TRACK);
							}
							
						}
                        logger.info("fullTrackPath = "+fullTrackPath + "fullTrackSong = "+fullTrackSong);
						if (centralStoragePath.endsWith(File.separator))
							centralStoragePath = centralStoragePath + clip.getClipRbtWavFile() + ".wav";
						else
							centralStoragePath = centralStoragePath + File.separator + clip.getClipRbtWavFile()
									+ ".wav";
						
						File file = new File(centralStoragePath);
						if(clipWavFile.indexOf("rbt_slice") != -1 && fullTrackSong != null) {
							file = new File(fullTrackPath);
						}
						logger.info("CentralStorage path = "+centralStoragePath);
						if (!file.exists() || (clipWavFile.indexOf("rbt_slice") != -1 && fullTrackSong == null)) {
							boolean success = rbtDBManager.updateClipStatusByWavFile("2", clipWavFile);
							logger.warn("CentralStoragePath = "+file+" not found / Fulltracksong not available , not processing and updating clip status to 2 clip: "
									+ clipWavFile + ", isUpdated: " + success);
//							if (success)
//								logger.info("Moving to ERROR(\"2\") STATE as clipWavFile "
//												+ clipWavFile + " does not exist in Central Storage Path");
							return;
						}
						File sliceFile = null;
						if(sliceWavFilePath!=null){
						     sliceFile = new File(sliceWavFilePath);
						}
						logger.info("sliceWavFilePath = "+sliceWavFilePath);
						if (clipWavFile.indexOf("rbt_slice") != -1 && sliceFile!=null && !sliceFile.exists()) {
							int duration = getDuration(clipWavFile);
							String ffmpgCommandPath = RBTParametersUtils.getParamAsString("COMMON",
									"CLIP_STATUS_FFMPEG_COMMAND", null);
							String min = "00";
							String sec = "00";
							if (duration != -1) {
								if (duration / 60 > 0)
									min = "0" + duration / 60;
								if (duration % 60 >= 10)
									sec = duration % 60 + "";
								else
									sec = "0" + duration % 60;
							}
							ffmpgCommandPath = ffmpgCommandPath.replaceAll("%min%", min);
							ffmpgCommandPath = ffmpgCommandPath.replaceAll("%sec%", sec);
							ffmpgCommandPath = ffmpgCommandPath.replaceAll("%source%",
									fullTrackPath);
							ffmpgCommandPath = ffmpgCommandPath.replaceAll("%destination%",
									sliceWavFilePath);
							logger.info("ffmpgCommandPath =  " + ffmpgCommandPath);
							boolean result = processCmd(ffmpgCommandPath);
						}
						if (sliceFile != null) {
							if (sliceFile.exists()) {
								logger.info("Slice Wav File Exists");
								file = sliceFile;
							} else {
								rbtDBManager.updateClipStatusByWavFile("2", clipWavFile);
								logger.info("Trimming of Slice Wav File Failed.So , not "
										+ "transferring file and updating the status to 2");
								return;
							}
						}
						
						ArrayList<String> playerURLList = null;
						HttpResponse httpResponse = null;
						if (circleIds[i] != null) {
							playerURLList = circleIdToPlayerUrlMap.get(tempCircleID
									.trim());
							if (playerURLList == null
									|| playerURLList.size() == 0) {
								logger.warn("Not processing to circleIds: "
										+ circleIds[i]
										+ ", playerURLList is not present in "
										+ "" + "circleIdToPlayerUrlMap: "
										+ circleIdToPlayerUrlMap);
								continue;
							}
							logger.debug("Processing to playerURLList :"
									+ playerURLList + ", circleIds: "
									+ circleIds[i] + ", centralStoragePath: "
									+ centralStoragePath +" , sliceWavFilePath = "+sliceWavFilePath);						
							for (String playerURL : playerURLList) {

								try {
									// Setting request Params
									HashMap<String, String> params = new HashMap<String, String>();
									params.put(FEED, UGCFILE);

									HashMap<String, File> fileParams = new HashMap<String, File>();
									logger.info("Hitting file=  "+file.getAbsolutePath());
									fileParams.put(file.getName(), file);

									try {
										HttpParameters httpParam = new HttpParameters();
										httpParam.setMaxTotalConnections(2);
										httpParam.setMaxHostConnections(2);
										httpParam.setConnectionTimeout(connectionTimeout);
										httpParam.setSoTimeout(socketTimeout);
										logger.info("Making http request. Processing to"
												+ " circleIds: "
												+ circleIds[i]
												+ ", player url: " + playerURL + ",HTTPParameters = "+httpParam);
										rbtHttpClient = new RBTHttpClient(httpParam);
										httpResponse = rbtHttpClient.makeRequestByPost(
												playerURL, params, fileParams);
										logger.info("Successfully made http request. httpResponse: "
												+ httpResponse.getResponse()+", to circleId: "
												+ circleIds[i] + ", player url: "
												+ playerURL);
									} catch (Exception e) {
										logger.error("Exception during processing circleIds: "
												+ circleIds[i] + ", player url: "
												+ playerURL + ", httpResponse: "
												+ httpResponse, e);
									} catch (Throwable e) {
										logger.error("Exception during processing circleIds: "
												+ circleIds[i] + ", player url: "
												+ playerURL + ", httpResponse: "
												+ httpResponse, e);
									}
									
									if (httpResponse != null
											&& httpResponse.getResponse().indexOf(
													"SUCCESS") != -1) {
										tonePlayerUploadedMap.put(circleIds[i],
												true);
									} else {
										logger.warn("File not uploaded to circleIds: "
												+ circleIds[i]
												+ ", toneplayer: " + playerURL);
										tonePlayerUploadedMap.put(circleIds[i],
												false);
									}
									
									logger.info("After processing circleId: "
											+ circleIds[i]
											+ " updated tonePlayerUploadedMap: "
											+ tonePlayerUploadedMap);

								} catch (Exception e) {
									logger.error(
											"Exception when processing to circle: "
													+ circleIds[i]
													+ ", clipWavFile: "
													+ clipWavFile
													+ ", Exception: "
													+ e.getMessage(), e);
								} catch (Throwable e) {
									logger.error(
											"Exception when processing to circle: "
													+ circleIds[i]
													+ ", clipWavFile: "
													+ clipWavFile
													+ ", Throwable: "
													+ e.getMessage(), e);
								}
							}
						} // if block for process individual circle id
					} // End CircleIds for loop
                    
					String toBeProcessCircles = null;
					for (String circleId : circleIds) {
						boolean isWavUploadedTP = tonePlayerUploadedMap
								.get(circleId);
						if (isWavUploadedTP) {
							if (transferredCircles == null
									|| transferredCircles.equalsIgnoreCase("")
									|| transferredCircles.equalsIgnoreCase("NULL"))
								transferredCircles = circleId;
							else
								transferredCircles += "," + circleId;

							logger.debug("The clip: " + clipWavFile
									+ " successfully uploaded to circle: "
									+ circleId + ", transferredCircles: "
									+ transferredCircles);
						} else {
							if (toBeProcessCircles == null
									|| toBeProcessCircles.equalsIgnoreCase("")
									|| toBeProcessCircles.equalsIgnoreCase("NULL"))
								toBeProcessCircles = circleId;
							else
								toBeProcessCircles += "," + circleId;
							logger.debug("The clip: " + clipWavFile
									+ " not uploaded to circle: "
									+ circleId + ", toBeProcessCircles: "
									+ toBeProcessCircles);
						}
						
					}
					String status = "1";
					if (toBeProcessCircles != null
							&& toBeProcessCircles.length() != 0) {
						status = "0";
						logger.info("Still there are circles to process, so updating status to 0, for the clip: "
								+ clipWavFile
								+ ", toBeProcessCircles: "
								+ toBeProcessCircles
								+ ", transferredCircles: " + transferredCircles);
					
					} else {
						logger.info("Since to be ProcessCircles is null, updating status to 1, for the clip: " + clipWavFile
								+ ", toBeProcessCircles: " + toBeProcessCircles
								+ ", transferredCircles: " + transferredCircles
								+ ", status: " + status);
					}
					
					rbtDBManager.updateStatusAndCircleIds(status,
							toBeProcessCircles, transferredCircles, clipWavFile);
				} else {
					logger.warn("Not processing clip: " + clipWavFile
							+ ", pendingCircles: " + pendingCircles
							+ ", transferedCircles: " + transferredCircles);
				}
			} catch(Throwable t) {
				logger.error(
						"Unable to process daemon. "+clipWavFile+", Throwable: "
								+ t.getMessage(), t);
			}
			logger.info("Completed processing clip: " + clipWavFile
					+ ", pendingCircles: " + pendingCircles
					+ ", transferedCircles: " + transferredCircles);
	
		}
	}

	private void getFeedUrl() {
		List<SitePrefix> prefix = CacheManagerUtil.getSitePrefixCacheManager()
				.getAllSitePrefix();
		logger.info("Getting site prefix: " + prefix);
		if (prefix == null || prefix.size() == 0) {
			return;
		}
		String palyerUrl = null;
		String feedUrl = null;
		StringBuffer siteFeedUrl = new StringBuffer();
		for (SitePrefix sitePrefix : prefix) {
			String circleIdToUpload = sitePrefix.getCircleID();
			palyerUrl = sitePrefix.getPlayerUrl();
			logger.debug("sitePrefix: " + prefix + ", circleIdToUpload: "
					+ circleIdToUpload + ", palyerUrl: " + palyerUrl);
			if (palyerUrl != null) {
				populateSiteFeedUrl(palyerUrl, circleIdToUpload, siteFeedUrl);
				logger.info("RBT:: feedurl == " + siteFeedUrl.toString());
				if (siteFeedUrl != null && siteFeedUrl.length() > 0) {
					feedUrl = siteFeedUrl.toString();
				}
			}
		}
		logger.info("feedUrl:" + feedUrl);

	}

	private void populateSiteFeedUrl(String playerUrls, String circleId,
			StringBuffer feedUrlBuff) {
		if (circleId == null || circleId.length() == 0 || playerUrls == null
				|| playerUrls.length() == 0) {
			logger.debug("Circle or Player URL is null circleId: " + circleId
					+ ", playerURLs: " + playerUrls);
			return;
		}
		if (feedUrlBuff == null) {
			feedUrlBuff = new StringBuffer();
		}
		ArrayList<String> urlList = null;
		playerUrls = playerUrls.trim();
		StringTokenizer st = new StringTokenizer(playerUrls, ",");
		String tempStr = null;
		while (st.hasMoreElements()) {
			String playerUrl = st.nextToken();
			if (playerUrl != null) {
				if (urlList == null) {
					urlList = new ArrayList<String>();
				}
				playerUrl = playerUrl.substring(0, playerUrl.lastIndexOf("/"));
				playerUrl = playerUrl
						+ "/RecordOwnDownloader/rbt_downloadFile.jsp?";
				logger.debug("RBT:: playerUrl " + playerUrl);
				if (tempStr == null) {
					tempStr = playerUrl;
				} else {
					tempStr = tempStr + ";" + playerUrl;
				}
				urlList.add(playerUrl);
			}
		}
		if (urlList != null && urlList.size() > 0) {
			if (circleId != null) {
				circleIdToPlayerUrlMap.put(circleId.trim(), urlList);
				if (!(feedUrlBuff.length() > 0)) {
					feedUrlBuff.append(tempStr);
					logger.info("RBT:: FeedUrl is " + feedUrlBuff.toString());
				} else {
					feedUrlBuff.append("," + tempStr);
					logger.info("RBT:: FeedUrl is " + feedUrlBuff.toString());
				}
			}
		}
	}

	private static void getCircleIdMap() {
		circleIdsMap = new HashMap<String, String>();
		String circleIdsIntStr = RBTParametersUtils.getParamAsString("COMMON",
				"CIRCLES_INTEGER_MAPPING_FOR_CLIP_STATUS", null);
		if (circleIdsIntStr != null) {
			String str[] = circleIdsIntStr.split(";");
			for (int i = 0; i < str.length; i++) {
				String circleInt[] = str[i].split(":");
				circleIdsMap.put(circleInt[1], circleInt[0]);
			}
		}
	}
	
	public void stopThread() {
		isAlive = false;
	}

	class RejectedExecutionHandlerImpl implements RejectedExecutionHandler {
		@Override
		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
			logger.info(r.toString() + " is rejected");
		}
	}
	
	private static Clip getClipByRBTWavFile(String rbt_wav) 
	{
		if(rbt_wav!=null && rbt_wav.indexOf("rbt_slice_")!=-1){
			String str[] = rbt_wav.split("rbt_slice_");
			String clipId = str[1].substring(0, str[1].indexOf("_"));
			return rbtCacheManager.getClip(clipId);
		}
		return rbtCacheManager.getClipByRbtWavFileName(rbt_wav);
	}

	private static int getDuration(String rbt_wav) 
    {   //rbt_slice_clipID_duration_rbt
		int duration = -1;
		try {
			if (rbt_wav != null && rbt_wav.indexOf("rbt_slice_") != -1) {
				String str[] = rbt_wav.split("rbt_slice_");
				// clipID_duration_rbt
				String durationStr = str[1].substring(str[1].indexOf("_") + 1,
						str[1].lastIndexOf("_"));
				logger.info("Duration === "+durationStr);
				duration=Integer.parseInt(durationStr);
				return duration;
			}
		} catch (Exception ex) {
			duration = -1;
			ex.printStackTrace();
		}
		logger.info("Duration === "+duration);
		return duration;
	}

	private static boolean processCmd(String command) {
		//basicLogger.info("Command "+command);
		Process process = null;
		String errorMessage = null;
		boolean success = false;
		try {
			Runtime runtime = Runtime.getRuntime();
			logger.info("Runtime : "+runtime);                    	
			process = Runtime.getRuntime().exec(command);
			process.waitFor();
			logger.info("Command has been Successfull processed");
			logger.info("command " + command + " result: " + process.exitValue());
			
		} catch(Exception e) {
			// basicLogger.error(e.getMessage(),e);
			System.out.println(e);
		}

		if(0 == process.exitValue()) 
		{
			success = true;
			//successfully converted
			logger.info("Successfully trimmed");

		} 
		else 
		{
			Exception e = new Exception("Process exited with errors.");
			logger.info(e.getMessage());
		}
		
		return success;
	}

}
