package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.UgcClip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.UgcClipDAO;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author vasipalli.sreenadh
 * 
 * Thread reads all ugc clips which have content type as UGCCLIP and clip grammar as TO_BE_DOWNLOADED
 * Each clip will be downloaded from Altruist and same will be uploaded to Tone players.
 * After tone player updation, clip grammar will be updated to DOWNLOADED 
 *
 */
public class AltruistUgcClipDownloader extends Thread implements iRBTConstant 
{
	private static Logger logger = Logger.getLogger(AltruistUgcClipDownloader.class);
	private  HashMap<String,String> publisherNameIdMap = new HashMap<String, String>();
	private  HashMap<String,String> publisherUrlIdMap = new HashMap<String, String>();

	private RBTDaemonManager m_mainDaemonThread;
	private HashMap<String, ArrayList<String>> circleIdToPlayerUrlMap = new HashMap<String, ArrayList<String>>();
	
	private String urlIdValues = null;
	private String nameIdValues = null;
	private String delimiters = "=;";
	
	public AltruistUgcClipDownloader(RBTDaemonManager mainDaemonThread) 
	{
		urlIdValues = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "UGC_WAVFILE_URL", "").getValue();
		nameIdValues = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "UGC_PUBLISHER_MAP", "").getValue();
		m_mainDaemonThread = mainDaemonThread;
	}
	public void run()
	{
		init();
		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive())
		{
			try
			{
				List<Clip> clipList = getUgcClipsToBeDownloaded();
				
				if (clipList == null || clipList.size() == 0)
				{
					logger.info("Clips list is null or zero size");
				}
				else
				{	
					for (Clip clip : clipList) 
					{
						File file = downloadUgcClip(clip);
						if (file != null && file.length() > 2048) // greater than 2 KB size
						{
							populateUgcClipToTonePlayer(file, clip);
						}
						else
							logger.info("file is not downloaded for clipname >"+clip.getClipRbtWavFile());
					}
				}
				try 
				{
					Thread.sleep(5*60*1000);
				}
				catch (InterruptedException e) 
				{
				}
			}
			catch(Throwable e)
			{
				logger.error("", e);
			}
		}
	}
	
	public void init()
	{
		populateCircleIDPlayerMap();
		parseIdName(nameIdValues);
		parseIdUrl(urlIdValues, delimiters);
		logger.info("circleIDPlayerUrlMap >"+circleIdToPlayerUrlMap);
	}
	
	
	/**
	 * @param wavFile
	 * @param clip
	 */
	public void populateUgcClipToTonePlayer(File wavFile, Clip clip)
	{
		String originalFileName = wavFile.getName();
        String renameToFileName = "rbt_"+originalFileName.replaceFirst(".wav", "_rbt.wav");
        File newFileToBeUploaded = new File(wavFile.getParentFile() + File.separator + renameToFileName);
        
        try 
        {
			copyFile(wavFile, newFileToBeUploaded);
		}
        catch (IOException e1) 
        {
		}
        
        Set<String> circleIDSet = circleIdToPlayerUrlMap.keySet(); 
        
        boolean isClipUploadedToAll = true;
        for (String circleID : circleIDSet) 
        {
        	logger.info("circleID "+circleID);
        	ArrayList<String> playerUrlList = circleIdToPlayerUrlMap.get(circleID);
        	if(playerUrlList == null || playerUrlList.size() == 0) 
        	{
        		logger.info("player url list for circle "+circleID +" is null");
        		continue;
        	}
        	boolean isUgcClipUploaded = false;
        	for (String playerUrl : playerUrlList) 
        	{
        		logger.info("circleID "+circleID +" & Player url "+playerUrl);
        		try
        		{
        			HttpParameters httpParameters = new HttpParameters();
        			httpParameters.setUrl(playerUrl);
        			httpParameters.setConnectionTimeout(6000);
        			//	httpParameters.setUseProxy(useProxy);

        			HashMap<String, String> requestParameters = new HashMap<String, String>();
        			requestParameters.put(FEED, UGCFILE);

        			HashMap<String, File> fileParameters = new HashMap<String, File>();
        			fileParameters.put(newFileToBeUploaded.getName(), newFileToBeUploaded);

        			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, requestParameters, fileParameters);
        			logger.info("HttpResponse "+httpResponse);
        			if (httpResponse != null && httpResponse.getResponse() != null && httpResponse.getResponse().indexOf("SUCCESS") != -1)
        			{
        				logger.info("UGC clip uploaded Successfully");
        				isUgcClipUploaded = true;
        			}
        			else
        				isUgcClipUploaded = false;
        		}
        		catch(Exception e)
        		{
        			logger.error("", e);
        			isUgcClipUploaded = false;
        		}
        		isClipUploadedToAll = (isClipUploadedToAll && isUgcClipUploaded) ;
        	}
        }
        if (isClipUploadedToAll)
    	{
    		logger.info("UGC clip uploaded all tone player urls Successfully");
    		boolean isClipMarked = markUgcClipToDownloaded(clip);
    		if (isClipMarked)
    			logger.info("Clip is uploaded marked clip grammar to downloaded");
    		try
    		{
    			if (wavFile.exists())
    				wavFile.delete();
    		}
    		catch(Exception e){}
    	}
	}
	
	
	/**
	 * @param clip
	 * @return
	 */
	public File downloadUgcClip(Clip clip)
	{
		String rbtWavFile = clip.getClipRbtWavFile();
		if (rbtWavFile.startsWith("rbt_"))
			rbtWavFile = rbtWavFile.substring(4, rbtWavFile.length());

    	if (rbtWavFile.indexOf("_rbt") != -1)
    		rbtWavFile= rbtWavFile.substring(0, rbtWavFile.indexOf("_"));
    	logger.info("rbt wav file from clips table is " + rbtWavFile); 
		UgcClip ugcClip = null;
		try {
			ugcClip = UgcClipDAO.getUgcClipByWavFile(rbtWavFile);
		} catch (DataAccessException e1) {
			logger.info("data access exception" + e1);
			
		}
		String publisherName = ugcClip.getPublisher();
		if (publisherName == null || publisherName.equals(""))
			{
			logger.info("cannot find publisher name."); 
			return null;
			}
		String publisherId = publisherNameIdMap.get(publisherName).toString();
		if(publisherId == null || publisherId.equals("") ){
			logger.info("cannot find publisher id."); 
			return null;
		}
		String urlValue = publisherUrlIdMap.get(publisherId).toString();
		if(urlValue == null || urlValue.equals("") ){
			logger.info("cannot find publisher id."); 
			return null;
		}
		//get ugc clip corresponding to above clip
		// get publisher name from ugc clip object
		// get url for that ugc clip from the config COMMON, UGC_PUBLISHER_NAME_URL_MAP in variable altruistUrl 
		// X String altruistUrl = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "UGC_WAVFILE_URL", "").getValue();
		String ugcWavFileUrl = generateWavFileUrl(urlValue, clip.getClipRbtWavFile()); 
		
		logger.info("ugcWavFileUrl >"+ugcWavFileUrl);
		
		File file = null;
		try 
		{

			HttpParameters httpParameters = new HttpParameters();
			httpParameters.setUrl(ugcWavFileUrl);
			
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			if (httpResponse != null && httpResponse.getResponse() != null)
			{
				file = new File(httpResponse.getResponse().trim());
			}
		}
		catch (Exception e) 
		{
			logger.error("", e);
		}
		return file;
	}
	
	/**
	 * @param clip
	 * @return
	 */
	public boolean markUgcClipToDownloaded(Clip clip)
	{
		boolean isClipMarkedToDownloaded = false;
		try 
		{
			clip.setClipGrammar("DOWNLOADED");
			ClipsDAO.updateClip(clip);
			isClipMarkedToDownloaded = true;
		}
		catch (DataAccessException e) 
		{
			logger.error("", e);
			isClipMarkedToDownloaded = false;
		}
		
		return isClipMarkedToDownloaded;
	}
	
	/**
	 * @return
	 */
	public List<Clip> getUgcClipsToBeDownloaded()
	{
		String selectUGCClips = "from Clip where clipGrammar='TO_BE_DOWNLOADED' and contentType = 'UGCCLIP'";
		try 
		{ 	 
            return ClipsDAO.getClips(selectUGCClips); 	 
	    }
		catch (DataAccessException e) 	 
		{ 	 
			logger.error("", e);
			return null; 	 
		}
		
	}
	
	public void populateCircleIDPlayerMap()
	{
		List<SitePrefix> prefix=CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();

		for (SitePrefix sitePrefix : prefix) 
		{
			logger.info("RBT:: looking for circle id in site prefix table  " + sitePrefix.getCircleID());
			if(sitePrefix.getCircleID()!= null)
			{
				String playerUrlsStr = sitePrefix.getPlayerUrl();
				logger.info("RBT:: playerUrlsStr >"+playerUrlsStr);

				if(playerUrlsStr != null)
				{
					playerUrlsStr = playerUrlsStr.trim();
					ArrayList<String> urlList = new ArrayList<String>();

					if(playerUrlsStr.indexOf(",")!= -1)
					{
						// If more than one playerUrl exists for Circle
						StringTokenizer st=new StringTokenizer(playerUrlsStr, ",");
						String tempStr=null;
						while(st.hasMoreElements())
						{
							String tempUrl=st.nextToken();
							if(tempUrl!=null)
							{
								tempUrl=tempUrl.substring(0, tempUrl.lastIndexOf("/"));
								tempUrl=tempUrl+"/RecordOwnDownloader/rbt_downloadFile.jsp?";
								logger.info("RBT:: temp url value is "+tempUrl);

								if(tempStr==null)
								{
									tempStr=tempUrl;
								}
								else
								{
									tempStr=tempStr+";"+tempUrl;
								}
								urlList.add(tempUrl);
							}
						}
						if(urlList.size() > 0)
						{
							circleIdToPlayerUrlMap.put(sitePrefix.getCircleID(), urlList);
						}
					}
					else
					{
						playerUrlsStr = playerUrlsStr.substring(0, playerUrlsStr.lastIndexOf("/"));
						playerUrlsStr = playerUrlsStr+"/RecordOwnDownloader/rbt_downloadFile.jsp?";

						logger.info("RBT:: playerUrlString is "+playerUrlsStr);
						urlList.add(playerUrlsStr);
						circleIdToPlayerUrlMap.put(sitePrefix.getCircleID().trim(), urlList);
					}
				}
			}
		}
	}
	
	
    /**
     * @param ugcWavFileUrl
     * @param wavFile
     * @return String
     * Format of url : http://10.89.45.229:8080/vox/0091/900/00/00/33/009190000003301.wav 
     */
    private String generateWavFileUrl(String ugcWavFileUrl,String wavFile) 
    {
    	StringBuffer wavFileUrl = new StringBuffer(ugcWavFileUrl);
    	if (wavFile.startsWith("rbt_"))
    		wavFile = wavFile.substring(4, wavFile.length());

    	if (wavFile.indexOf("_rbt") != -1)
			 wavFile = wavFile.substring(0, wavFile.indexOf("_"));

    	wavFileUrl.append("/"+wavFile.substring(0, 4));
    	wavFileUrl.append("/"+wavFile.substring(4,7));
    	wavFileUrl.append("/"+wavFile.substring(7,9));
    	wavFileUrl.append("/"+wavFile.substring(9,11));
    	wavFileUrl.append("/"+wavFile.substring(11,13));
    	wavFileUrl.append("/"+wavFile);
    	logger.info("WavFile >"+wavFileUrl);
    	return (wavFileUrl.toString()+".wav");
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
    private void parseIdName(String s) {
		String array[] = s.split(",");
		for (int i = 0; i < array.length; i++) {
			logger.info(array[i]);
			String array1[] = array[i].split("=");
			String key = array1[1];
			String value = array1[0];
			logger.info("key = " + array1[1]);
			logger.info("value = "+ array1[0]);
			publisherNameIdMap.put(key.trim(), value.trim());
			logger.info("adding value to map.");
		}
		logger.info("size of Name-Id map : " + publisherNameIdMap.size());
		Iterator iter = publisherNameIdMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next().toString();
			String value = publisherNameIdMap.get(key).toString();
			logger.info(key + " and " + value);
		}
	}
    private void parseIdUrl(String s, String delimiters) {
		StringTokenizer token = new StringTokenizer(s, delimiters);
		String urlValue = null;
		while (token.hasMoreTokens()) {
			String temp = token.nextToken();
 			logger.info("token : " + temp);
			if (temp.indexOf(",") != -1) {
				
				String array[] = temp.split(",");
				for(int i = 0; i < array.length; i++){
					publisherUrlIdMap.put(array[i].trim(), urlValue.trim());
				}
			}else{
				urlValue = temp;
				logger.info("url : " + urlValue);
			}


		}
		logger.info("size of Url-Id map : " + publisherUrlIdMap.size());
		Iterator iter = publisherUrlIdMap.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next().toString();
			String value = publisherUrlIdMap.get(key).toString();
			logger.info(key + " and " + value);
		}
    }
}

