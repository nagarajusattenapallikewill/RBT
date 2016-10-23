package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Access;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryClipMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoryClipMapDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;


public class UGCProcessor extends Thread implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(UGCProcessor.class);
	String _class = "UGCProcessor";
	RBTConnector rbtConnector = null;
	private RBTDBManager rbtDBManager = null;

	private RBTGatherer m_parentGathererThread = null;
	private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");

	String TELEPHONY_SERVER = "TELEPHONY_SERVER";
	String CONTENT_WEB_SERVER = "CONTENT_WEB_SERVER";
	private int m_UGCMonth = 1;

	Date m_UGCNextTopCountInitializeTime = null;

	SimpleDateFormat ugcSMSDateFormat = new SimpleDateFormat("'tgl 'dd-MM-yyyy 'jam' HH:mm:ss"); 
	String m_UGCSMS = "Your recorded voice at %DATE has transformed as RBT successfully. The content id is %PROMOID. Ask your friends to buy your content by dialing *888%MDN or by sending SMS to 888: RING %PROMOID"; 
	private HttpParameters m_httpParams;
	public static final String COMMON = "COMMON";
	public static final String WEB_SERVER_CONTENT_URL_DETAILS = "WEB_SERVER_CONTENT_URL_DETAILS";
	public static final String FAILURE = "FAILURE";
	public static SimpleDateFormat webServerSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
	private static List<String> allPlayerUrlsList = new ArrayList<String>();

	public UGCProcessor(RBTGatherer rbtGathererThread) throws Exception {
		m_parentGathererThread = rbtGathererThread;
		if (init())
			start();
		else
			throw new Exception(" In UGCProcessor: Cannot init Parameters");
	}

	private boolean init() {
		logger.info("Entering");
		rbtConnector=RBTConnector.getInstance();
		rbtDBManager = RBTDBManager.getInstance();

		m_UGCMonth =  getParamAsInt("UGC_MONTH", 1); //m_parentGathererThread.getParameterAsInt("UGC_MONTH", 1);
		m_UGCSMS = getParamAsString("GATHERER", "UGC_SMS_TEXT", "Your recorded voice at %DATE has transformed as RBT successfully. The content id is %PROMOID. Ask your friends to buy your content by dialing *888%MDN or by sending SMS to 888: RING %PROMOID");

		try
		{
			m_UGCNextTopCountInitializeTime =  m_format.parse(getParamAsString("GATHERER", "UGC_NEXT_TOP_COUNT_INITIALIZE_TIME", null));
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		logger.info("RBT::UGC_NEXT_TOP_COUNT_INITIALIZE_TIME is "+ m_UGCNextTopCountInitializeTime);


		initilizePlayerContentURL();

		List<SitePrefix> sitePrefixes = CacheManagerUtil.getSitePrefixCacheManager()
                .getAllSitePrefix();
		for (SitePrefix sitePrefix : sitePrefixes)
		{
			String playerUrls = sitePrefix.getPlayerUrl();
			String[] playerUrlTokens = playerUrls.split(",");
			for (String eachPlayerUrlToken : playerUrlTokens)
			{
				 String tempUrl = eachPlayerUrlToken.substring(0, eachPlayerUrlToken.lastIndexOf("/"));
                 tempUrl = tempUrl + "/RecordOwnDownloader/rbt_downloadFile.jsp?";
                 logger.info("RBT:: temp url value is " + tempUrl);
                 allPlayerUrlsList.add(tempUrl);
			}
		}

		return true;
	}

	public void run() 
	{
		logger.info("Entering");
		while (m_parentGathererThread.isAlive()) {
			try {
				logger.info("Entering while loop");
				boolean newMonth = isNewMonth();
				if (newMonth)
					carryOverLastMonthsCount();
				updateTopUGCCategories();
				updateLatestCategory();
				boolean reInitializeCount = isReInitializeCountNeeded();
				if (reInitializeCount)
					updateUGCCount();
				transferUGCWavFiles(TELEPHONY_SERVER);
				logger.info("RBT::transfered with mode TELEPHONY_SERVER");
				transferUGCWavFiles(CONTENT_WEB_SERVER);
				logger.info("RBT::transfered with mode CONTENT_WEB_SERVER");
				Calendar ugcExpireTime = Calendar.getInstance();
				int currentHourUGC = ugcExpireTime.get(Calendar.HOUR_OF_DAY);

				int ugcExpireHourStart = 0;
				int ugcExpireHourEnd = 2;
				String ugcHourStr = getParamAsString("GATHERER", "UGC_EXPIRE_HOURS", null);
				try
				{
					if (ugcHourStr != null && ugcHourStr.length() > 0)
					{
						StringTokenizer stk = new StringTokenizer(ugcHourStr, ",");
						ugcExpireHourStart = Integer.parseInt(stk.nextToken());
						ugcExpireHourEnd = Integer.parseInt(stk.nextToken());
					}
				}
				catch(Exception e)
				{
					logger.error("", e);
					ugcExpireHourStart = 0;
					ugcExpireHourEnd = 2;
				}


				if (ugcExpireHourStart < ugcExpireHourEnd) {
					if (currentHourUGC >= ugcExpireHourStart
							&& currentHourUGC < ugcExpireHourEnd)
						setDefaultTuneForExpiredUGCSelections();
				} else {
					if (currentHourUGC >= ugcExpireHourStart
							|| currentHourUGC < ugcExpireHourEnd)
						setDefaultTuneForExpiredUGCSelections();
				}
			} catch (Exception e) {
				logger.error("", e);
			}
			try {
				Date next_run_time = m_parentGathererThread.roundToNearestInterVal(getParamAsInt("GATHERER_SLEEP_INTERVAL", 5));
				long sleeptime = m_parentGathererThread.getSleepTime(next_run_time);
				if(sleeptime < 100)
					sleeptime = 500;
				logger.info(_class + " Thread : sleeping for "+sleeptime + " mSecs.");
				Thread.sleep(sleeptime);
				logger.info(_class + " Thread : waking up.");Thread.sleep(sleeptime);
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info("Exiting");
	}

	private boolean isNewMonth()
	{
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH) + 1;

		if(month > m_UGCMonth || ( month == 1 &&  m_UGCMonth == 12) )
		{
			m_UGCMonth = month;
			rbtConnector.getRbtGenericCache().updateParameter(GATHERER, "UGC_MONTH", ""+month);
//			rbtDBManager.updateParameter(GATHERER,"UGC_MONTH", ""+month);
			return true;
		}
		return false;
	}

	private void carryOverLastMonthsCount()
	{
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		DateFormat yearFormat = new SimpleDateFormat("yyyy");
		DateFormat monthFormat = new SimpleDateFormat("MM");
		String presentYear = yearFormat.format(date);
		String presentMonth = monthFormat.format(date);

		cal.add(Calendar.MONTH,-1);
		date = cal.getTime();
		String lastYear = yearFormat.format(date);
		String lastMonth = monthFormat.format(date);

		Iterator<Integer> clipIDs = rbtDBManager.getUGCClipIDsFromAccessTable(lastYear, lastMonth);
		if(clipIDs == null )
		{
			logger.info("No entries added in RBT_CLIP_ACCESS for this month.");
			return;
		}
		while(clipIDs.hasNext())
		{
			int clipId = ((Integer)clipIDs.next()).intValue();
			Access accessPresent = rbtDBManager.getAccessifPresent(clipId, null, presentYear, presentMonth);
			Access  accessLast = rbtDBManager.getAccessifPresent(clipId, null, lastYear, lastMonth);
			if (accessLast == null)
				continue;
			else if(accessPresent == null)
				rbtDBManager.insertAccess(clipId, accessLast.clipName(),presentYear, presentMonth, 0, 0, accessLast.noOfPlays());
			else
				rbtDBManager.updateAccess(clipId, accessLast.clipName(), presentYear, presentMonth, 0, 0, accessLast.noOfPlays() + accessPresent.noOfPlays());
		}
	}


	private void updateTopUGCCategories()
	{
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		DateFormat yearFormat = new SimpleDateFormat("yyyy");
		DateFormat monthFormat = new SimpleDateFormat("MM");
		String presentYear = yearFormat.format(date);
		String presentMonth = monthFormat.format(date);


		ArrayList<String> m_topUGCCatgeoryIds = Tools.tokenizeArrayList(getParamAsString("GATHERER", "UGC_TOP_CATEGORIES", null), null);

		Access[] accessInit = rbtDBManager.getTopUGCAccesses(presentYear, presentMonth);
		Access[] accessFinal = null;
		ArrayList<Access> accessList = new ArrayList<Access>();
		if(m_topUGCCatgeoryIds == null || accessInit == null)
			return;
		int counter = 0;
		for(int k = 0; k < accessInit.length; k++)
		{
			if(counter >= getParamAsInt("UGC_TOP_LIST_SIZE", 20))
				break;
			Clip clip  = rbtConnector.getMemCache().getClip(accessInit[k].clipID());
			if(clip != null && clip.getClipEndTime() != null && clip.getClipEndTime().getTime() > System.currentTimeMillis())
			{
				accessList.add(accessInit[k]);
				counter += 1;
			}
		}
		if(accessList.size() > 0)
			accessFinal = (Access[])accessList.toArray(new Access[0]);
		if(accessFinal == null)
			return;
		for(int i = 0; i < m_topUGCCatgeoryIds.size(); i++)
		{
			try
			{
				int ugcCategoryID = Integer.valueOf((String)m_topUGCCatgeoryIds.get(i));
				CategoryClipMapDAO.deleteAllCategoryClipMap(ugcCategoryID);

				ArrayList<CategoryClipMap> categoryMap = new ArrayList<CategoryClipMap>();
				for(int j = 0; j < accessFinal.length; j++)
				{
					CategoryClipMap categoryClipMap = new CategoryClipMap();
					categoryClipMap.setCategoryId(ugcCategoryID);
					categoryClipMap.setClipId(accessFinal[j].clipID());
					categoryClipMap.setClipInList('y');
					categoryClipMap.setClipIndex(j+1);
					categoryClipMap.setPlayTime(null);

					categoryMap.add(categoryClipMap);

				}
				CategoryClipMapDAO.saveCategoryClipMap((CategoryClipMap[])categoryMap.toArray(new CategoryClipMap[0]));
			}
			catch(DataAccessException e)
			{
				logger.info("Exception >"+e.getMessage());
			}
		}
	}

	private boolean isReInitializeCountNeeded()
	{

		if(m_UGCNextTopCountInitializeTime == null)
			return false;
		Date presentDate = new Date(System.currentTimeMillis());
		if(presentDate.after(m_UGCNextTopCountInitializeTime))
		{
			Calendar cal = Calendar.getInstance(); 
			cal.setTime(presentDate); 
			cal.add(Calendar.DATE, getParamAsInt("UGC_TOP_DOWNLOADS_UPDATE_PERIOD_DAYS", 7)); 
			presentDate = cal.getTime();
			m_UGCNextTopCountInitializeTime = presentDate;
			rbtConnector.getRbtGenericCache().updateParameter(GATHERER, "UGC_NEXT_TOP_COUNT_INITIALIZE_TIME", m_format.format(presentDate));
			return true;
		}
		return false;
	}

	private void updateUGCCount()
	{
		Calendar cal = Calendar.getInstance();
		Date date = cal.getTime();
		DateFormat yearFormat = new SimpleDateFormat("yyyy");
		DateFormat monthFormat = new SimpleDateFormat("MM");
		String presentYear = yearFormat.format(date);
		String presentMonth = monthFormat.format(date);

		Iterator<Integer> clipIDs = rbtDBManager.getUGCClipIDsFromAccessTable(presentYear, presentMonth);
		if(clipIDs == null )
		{
			logger.info("No entries added in RBT_CLIP_ACCESS for this month.");
			return;
		}
		while(clipIDs.hasNext())
		{
			int clipId = ((Integer)clipIDs.next()).intValue();
			Access access = rbtDBManager.getAccessifPresent(clipId, null, presentYear, presentMonth);
			if (access == null)
				continue;
			else
				rbtDBManager.updateAccess(clipId, access.clipName(),presentYear, presentMonth, access.noOfPreviews(), access.noOfAccess(), 0);
		}
	}

	private void updateLatestCategory()
	{
		Clips[] clips = rbtDBManager.getLatestUGCClips(getParamAsInt("UGC_LATEST_LIST_SIZE", 20));

		String m_UGCLatestCategory = getParamAsString("GATHERER", "UGC_LATEST_CATEGORY", null);
		if(clips == null || m_UGCLatestCategory == null)
			return;

		int ugcCategoryID = Integer.parseInt(m_UGCLatestCategory);
		try 
		{
			CategoryClipMapDAO.deleteAllCategoryClipMap(ugcCategoryID);
			ArrayList<CategoryClipMap> categoryMap = new ArrayList<CategoryClipMap>();
			for(int j = 0; j < clips.length; j++)
			{
				CategoryClipMap categoryClipMap = new CategoryClipMap();
				categoryClipMap.setCategoryId(Integer.valueOf(m_UGCLatestCategory));
				categoryClipMap.setClipId(clips[j].id());
				categoryClipMap.setClipInList('y');
				categoryClipMap.setClipIndex(j+1);
				categoryClipMap.setPlayTime(null);

				categoryMap.add(categoryClipMap);
			}
			CategoryClipMapDAO.saveCategoryClipMap((CategoryClipMap[])categoryMap.toArray(new CategoryClipMap[0]));
		}
		catch (DataAccessException e) 
		{
			logger.error("", e);
		}
	} 

	private void transferUGCWavFiles(String serverType)      
	{ 
		logger.info("RBT:: entered with serverType " + serverType);
		
		List<Clip> clips = new ArrayList<Clip>();
			
		if("TELEPHONY_SERVER".equalsIgnoreCase(serverType))
			clips = rbtDBManager.getUGCFilesToTransferToTelephonyServers();
		else if("CONTENT_WEB_SERVER".equalsIgnoreCase(serverType))
			clips = rbtDBManager.getUGCFilesToTransferToContentWebServers();

		if(clips == null || clips.size() <= 0)
		{
			logger.info("RBT:: no clips to process......");
			return; 
		}
		logger.info("RBT:: no. of clips to process is " + clips.size());
		
		for (Clip clip : clips) 
		{ 
			boolean successRBTWav = false;
			//copy clips to Clips dir
			if("TELEPHONY_SERVER".equalsIgnoreCase(serverType))
				successRBTWav = transferUGCToClipsDir(clip);
			else if("CONTENT_WEB_SERVER".equalsIgnoreCase(serverType))
				successRBTWav = true;

			boolean successPreviewWav = false;
			//copy clips to Previews dir
			if("TELEPHONY_SERVER".equalsIgnoreCase(serverType))
				successPreviewWav = transferUGCToPreviewDir(clip);
			else if("CONTENT_WEB_SERVER".equalsIgnoreCase(serverType))
				successPreviewWav = true;

			if (successRBTWav)
			{
				//copy clips to web server
				boolean successWebServer = transferUGCToWebServer(clip);
				if(!successWebServer) 
				{
					//copy clips to web server failed
					if (!"CONTENT_WEB_SERVER".equalsIgnoreCase(serverType))
					{
						rbtDBManager.makeUGCClipSemiLive(clip.getClipPromoId().trim());
					}
					return;
				}
			//	Clip clip = RBTCacheManager.getInstance().getClipByPromoId(clip.getClipPromoId().trim());
				if(clip == null) {
					return;
				}
				Calendar calendar = Calendar.getInstance();
				calendar.set(2037, Calendar.JANUARY, 1, 0, 0, 0);
				Date endTime = calendar.getTime();
				clip.setClipEndTime(endTime);
				try
				{
					ClipsDAO.updateClip(clip);
				}
				catch (DataAccessException e)
				{
					logger.error("", e);
					return;
				}
				String sms = prepareUGCSMS(clip); 
				String[] smsList = parseText(sms);
				if(smsList != null && smsList.length > 0)
				{
					for(int j = 0; j < smsList.length; j++)
						sendSMS(clip.getAlbum(), smsList[j]);
				}
			} 
		} 
	}

	private boolean transferUGCToClipsDir(Clip clip) 
	{ 
		File ugcFile = new File(getParamAsString("SMS", "DEFAULT_REPORT_PATH", null) + File.separator + clip.getClipRbtWavFile()+".wav"); 
		if (!ugcFile.exists() || ugcFile.length() <= 0) 
		{ 
			logger.info("RBT::file missing or file size is zero "+ clip.getClipRbtWavFile()); 
			return false; 
		} 
//		StringTokenizer tokens = new StringTokenizer(getParamAsString("COMMON", "CLIPS_DIR", null), ","); 
//		while (tokens.hasMoreTokens()) 
//		{ 
//			String directory = tokens.nextToken(); 
//			String rbtWavFile = null;
//			
//			if (clip.getContentType() != null && clip.getContentType().equalsIgnoreCase("EMOTION_UGC"))
//			{
//				rbtWavFile = clip.getClipName()+".wav";
//			}
//			else
//			{
//				rbtWavFile = "rbt_ugc_"+clip.getClipPromoId()+"_rbt.wav"; 
//				if(new File(directory + File.separator + rbtWavFile).exists()) 
//					continue;
//			}
//			File f = new File(directory + File.separator + clip.getClipName()); 
//			if(f.exists()) 
//				f.delete(); 

			try
			{
//				File destFile = new File(directory + File.separator+ rbtWavFile);
//				copyFile(ugcFile, destFile);
				uploadWavFilesToPlayers(ugcFile);
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
//			Tools.moveFile(directory, ugcFile); 
//			new File(directory + File.separator + ugcFile.getName()).renameTo(new File(directory + File.separator+ rbtWavFile)); 
//		} 
		return true; 
	} 

	private boolean transferUGCToPreviewDir(Clip clip) 
	{ 
		File ugcFile = new File(getParamAsString("SMS", "DEFAULT_REPORT_PATH", null) + File.separator + clip.getClipRbtWavFile()+".wav"); 
		if (!ugcFile.exists() || ugcFile.length() <= 0) 
		{ 
			logger.info("RBT::file missing or file size is zero "+ clip.getClipName()); 
			return false; 
		} 
		StringTokenizer tokens = new StringTokenizer(getParamAsString("COMMON", "CLIPS_PREVIEW_DIR", null), ","); 
		while (tokens.hasMoreTokens()) 
		{ 
			String directory = tokens.nextToken(); 
			String rbtWavFile = null;
			if (clip.getContentType() != null && clip.getContentType().equalsIgnoreCase("EMOTION_UGC"))
			{
				rbtWavFile = clip.getClipName()+"_preview.wav";
				
				File previewFile = new File(directory + File.separator+ rbtWavFile);
				if (previewFile.exists())
				{
					logger.info("preview file exists.. deleting"+previewFile.getAbsolutePath());
					previewFile.delete();
				}
			}
			else
			{
				rbtWavFile = "rbt_ugc_"+clip.getClipPromoId()+"_preview.wav"; 
				if(new File(directory + File.separator + rbtWavFile).exists()) 
					continue; 
			}
			
			File f = new File(directory + File.separator + clip.getClipName()); 
			if(f.exists()) 
				f.delete(); 
			
			try
			{
				File destFile = new File(directory + File.separator+ rbtWavFile);
				copyFile(ugcFile, destFile);

//				uploadWavFilesToPlayers(ugcFile);
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
	
		//	Tools.moveFile(directory, ugcFile); 
		//	new File(directory + File.separator + ugcFile.getName()).renameTo(new File(directory + File.separator+ rbtWavFile)); 
		} 
		return true; 
	} 

	private boolean transferUGCToWebServer(Clip ugcClip)
	{
		if(m_httpParams == null)
			return true;

		String clipDetailsXml = getClipDetailsXml(ugcClip);
		if(clipDetailsXml == null || clipDetailsXml.length() <= 0)
			return false;
		HashMap<String, String> hm = new HashMap<String, String>();
		hm.put("CLIP_DETAILS",clipDetailsXml);

		File ugcFile = new File(getParamAsString("SMS", "DEFAULT_REPORT_PATH", null) + File.separator + ugcClip.getClipName()+".wav"); 
		if (!ugcFile.exists() || ugcFile.length() <= 0) 
		{ 
			logger.info("RBT::file missing or file size is zero "+ ugcClip.getClipName()); 
			return false; 
		}
		File ugcFileArray[] = new File[1];
		ugcFileArray[0] = ugcFile;
		try
		{
			String response = RBTHTTPProcessing.postFile(m_httpParams, hm, ugcFileArray);
			logger.info("RBT::response for " + hm.get("CLIP_DETAILS") + " is " + response); 
			if(response==null || response.trim().equalsIgnoreCase(FAILURE))
				return false;
			else
				return true;
		}
		catch(Exception e)
		{
			logger.error("", e);
			logger.info("Exception thrown while transferUGCToWebServer "+ugcClip.getClipId());
			return false;
		}
	}

	private String prepareUGCSMS(Clip clip) 
	{ 
		String subId = clip.getAlbum(); 
		String promoId = clip.getClipPromoId(); 
		Date startTime = clip.getClipStartTime(); 
		String setTime = ugcSMSDateFormat.format(startTime); 
		
		String sms = getParamAsString("GATHERER","UGC_SMS_TEXT", m_UGCSMS);
		if ("EMOTION_UGC".equalsIgnoreCase(clip.getContentType()))
			sms = CacheManagerUtil.getSmsTextCacheManager().getSmsText("UGC_EMOTION_SMS_TEXT");
		 
		sms = Tools.findNReplace(sms, "%MDN", subId); 
		sms = Tools.findNReplaceAll(sms, "%PROMOID", promoId); 
		sms = Tools.findNReplaceAll(sms, "%DATE", setTime); 
		
		return sms; 
	}

	public String[] parseText(String s)
	{
		int index = 160;
		ArrayList<String> list = new ArrayList<String>();
		String t = null;
		while (s.length() != 0)
		{
			index = 160;
			if (s.length() <= 160)
			{
				t = s;
				s = "";
			}
			else
			{
				while (index >= 0 && s.charAt(index) != ' ')
					index--;
				t = s.substring(0, index);
				s = s.substring(index + 1);
			}
			list.add(t);
		}

		if (list.size() > 0)
		{
			String[] smsTexts = (String[]) list.toArray(new String[0]);
			return smsTexts;
		}
		else
		{
			return null;
		}
	}
	private void initilizePlayerContentURL()
	{
		logger.info("Entering");
		try
		{
			//	String strPlayerContentURL = m_parentGathererThread.getParameterAsString(WEB_SERVER_CONTENT_URL_DETAILS, null, m_parentGathererThread.m_commonParams);
			String strPlayerContentURL = getParamAsString("COMMON", "WEB_SERVER_CONTENT_URL_DETAILS", null);
			if(strPlayerContentURL != null)
			{
				StringTokenizer tokens = new StringTokenizer(strPlayerContentURL,";");
				m_httpParams = new HttpParameters();
				if (tokens.hasMoreTokens())
					m_httpParams.setUrl(tokens.nextToken());
				if (tokens.hasMoreTokens())
					m_httpParams.setConnectionTimeout(Integer.parseInt(tokens.nextToken()));
				if (tokens.hasMoreTokens())
					m_httpParams.setDataTimeout(Integer.parseInt(tokens.nextToken()));
				if (tokens.hasMoreTokens())
					m_httpParams.setHasProxy(Boolean.getBoolean(tokens.nextToken()));
				if (tokens.hasMoreTokens())
					m_httpParams.setProxyHost(tokens.nextToken());
				if (tokens.hasMoreTokens())
					m_httpParams.setProxyPort(Integer.parseInt(tokens.nextToken()));
			}
		}
		catch(Exception exe)
		{
			logger.error("", exe);
		}
		logger.info("Exiting");
	}

	private String getClipDetailsXml(Clip clip)
	{
		if(clip == null)
			return null;

		String clipDetails = "<CLIP_DATA><CLIP ID=\""+clip.getClipId()+"\" ";

		String name = clip.getClipName();
		if(name != null && name.length() > 0 )
			clipDetails = clipDetails + "NAME=\""+name+"\" ";

		String previewFile = clip.getClipPreviewWavFile();
		if(previewFile != null && previewFile.length() > 0 )
			clipDetails = clipDetails + "PREVIEW_WAV_FILE=\""+previewFile+"\" ";

		String rbtWavFile = clip.getClipRbtWavFile();
		if(rbtWavFile != null && rbtWavFile.length() > 0 )
			clipDetails = clipDetails + "RBT_WAV_FILE=\""+rbtWavFile+"\" ";

		String grammar = clip.getClipGrammar();
		if(grammar != null && grammar.length() > 0 )
			clipDetails = clipDetails + "GRAMMAR=\""+grammar+"\" ";

		String promoID = clip.getClipPromoId();
		if(promoID != null && promoID.length() > 0 )
			clipDetails = clipDetails + "PROMO_ID=\""+promoID+"\" ";

		Date startTime = clip.getClipStartTime();
		if(startTime != null)
			clipDetails = clipDetails + "START_TIME=\""+webServerSdf.format(startTime)+"\" ";

		Date endTime = clip.getClipEndTime();
		if(endTime != null)
			clipDetails = clipDetails + "END_TIME=\""+webServerSdf.format(endTime)+"\" ";

		String album = clip.getAlbum();
		if(album != null && album.length() > 0 )
			clipDetails = clipDetails + "ALBUM=\""+album+"\" ";

		String language = clip.getLanguage();
		if(language != null && language.length() > 0 )
			clipDetails = clipDetails + "LANGUAGE=\""+language+"\" ";

		String classType = clip.getClassType();
		if(classType != null && classType.length() > 0 )
			clipDetails = clipDetails + "CLASS_TYPE=\""+classType+"\" ";

		clipDetails = clipDetails + " /></CLIP_DATA>";

		return clipDetails;

	}

	private void setDefaultTuneForExpiredUGCSelections()
	{
		Clips[] expiredClips = rbtDBManager.getExpiredUGCClips();
		if(expiredClips == null || expiredClips.length <= 0)
		{	
			logger.info("No expiredUGC clips found.");
			return;
		}
		logger.info("No. of expired ugc clips found = "+expiredClips.length);
		boolean success = false;
		for(int i = 0; i <= expiredClips.length ; i++)
		{
			success = rbtDBManager.expireUGCSelections(expiredClips[i].wavFile());
			if(success)
				rbtDBManager.unmarkUGCExpiredClip(expiredClips[i].id());
		}

	}
	private void sendSMS(String subscriber, String sms)
	{
		try
		{
			Tools.sendSMS(com.onmobile.apps.ringbacktones.provisioning.common.Utility.getSenderNumber("GATHERER", subscriber, "SENDER_NO"), subscriber, sms, false);
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

	}
	public static void copyFile(File source, File destination) throws IOException
	{
		FileChannel sourceFileChannel = null;
		FileChannel destinationFileChannel = null;
		try
		{
			sourceFileChannel = (new FileInputStream(source)).getChannel();
			destinationFileChannel = (new FileOutputStream(destination)).getChannel();
			sourceFileChannel.transferTo(0, source.length(), destinationFileChannel);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (sourceFileChannel != null)
					sourceFileChannel.close();
				if (destinationFileChannel != null)
					destinationFileChannel.close();
			}
			catch (IOException e)
			{
			}
		}
	}
	private String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return rbtConnector.getRbtGenericCache().getParameter(type, param, defualtVal);
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}
	private int getParamAsInt(String param, int defaultVal)
	{
		try{
			String paramVal = rbtConnector.getRbtGenericCache().getParameter("GATHERER", param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}

	private void uploadWavFilesToPlayers(File wavFile)
	{
		try
		{
			if (wavFile.isDirectory())
			{
				logger.info("The wavfile passed to be uploaded is a directory : " + wavFile.getAbsolutePath());
				return;
			}

			String fileName = wavFile.getName();
			logger.info("file name ;" + fileName);
			if (fileName == null || (fileName.indexOf(".wav") == -1 && fileName.indexOf(".WAV") == -1))
			{
				logger.info("The wavfile passed to be uploaded does not end with .wav : " + wavFile.getAbsolutePath());
				return;
			}

			if (allPlayerUrlsList == null || allPlayerUrlsList.size() == 0)
			{
				logger.info("No playerUrls to upload : " + allPlayerUrlsList);
				return;
			}

			boolean uploadedToAllURL = true;
			for (String playerURL : allPlayerUrlsList)
			{
				HttpResponse httpResponse = null;
				try
				{
					com.onmobile.apps.ringbacktones.webservice.common.HttpParameters httpParam = new com.onmobile.apps.ringbacktones.webservice.common.HttpParameters();
					httpParam.setUrl(playerURL);
					httpParam.setConnectionTimeout(6000);
					httpParam.setSoTimeout(6000);

					// Setting request Params
					Map<String, String> params = new HashMap<String, String>();
					params.put(FEED, UGCFILE);
					// Setting File Params
					Map<String, File> fileParams = new HashMap<String, File>();
					fileParams.put(fileName, wavFile);
					logger.info("WavFile : " + wavFile.getAbsolutePath() + ", playerURL : " + playerURL + ", HttpParams : " + httpParam);
					httpResponse = RBTHttpClient.makeRequestByPost(
							httpParam, params, fileParams);
				}
				catch (Exception e)
				{
					logger.error(e.getMessage(), e);
				}
				logger.info("RBT:: url -> " + playerURL + ", Response -> " + httpResponse);

				if (httpResponse == null
						|| httpResponse.getResponse().indexOf("SUCCESS") == -1)
					uploadedToAllURL = uploadedToAllURL && false;
			}

		} catch (Throwable t) {
			logger.error("", t);
		}
		logger.info("exiting uploadWavFilesToPlayers");
	}
}
