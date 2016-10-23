package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SitePrefixCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.UgcClip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.UgcClipDAO;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;

public class UGCContentUploadDaemon extends Thread implements iRBTConstant{

	private static Logger logger = Logger.getLogger(UGCContentUploadDaemon.class);
	
	private ParametersCacheManager m_ParametersCacheManager = null;
	private RBTDBManager m_rbtDbManager = null;
	private Calendar calendar = Calendar.getInstance();
	private String feedURL = null;
	private String[] m_circleIDToUploadMeraHTFiles=null;
	private int sleepMins = 1;
	private HashMap circleIdToPlayerUrlMap=new HashMap();
	private RBTClient rbtClient = null;
	public boolean createdSuccessfully=false;
	
	public  UGCContentUploadDaemon() {

		Tools.init("UGCContentUploadDaemon", true);

		logger.info("inside UGCContentUploadDaemon  constructor");
		
		m_ParametersCacheManager = CacheManagerUtil.getParametersCacheManager();
		sleepMins = Integer.parseInt(m_ParametersCacheManager.getParameter("COMMON", "UGCDAEMON_SLEEPMINS").getValue());

		m_rbtDbManager = RBTDBManager.getInstance();
		
		SitePrefixCacheManager sitePrefixCacheMgr = CacheManagerUtil.getSitePrefixCacheManager();
		
		List<SitePrefix> allSitePrefixes = sitePrefixCacheMgr.getAllSitePrefix();
		logger.info("Site prefixes->"+allSitePrefixes);
		m_circleIDToUploadMeraHTFiles = new String[allSitePrefixes.size()];
		
		for(int sitesLen = 0;sitesLen<allSitePrefixes.size();sitesLen++){
			m_circleIDToUploadMeraHTFiles[sitesLen] = allSitePrefixes.get(sitesLen).getCircleID();
			logger.info("m_circleIDToUpload->"+m_circleIDToUploadMeraHTFiles[sitesLen]);
		}
	
		logger.info("m_circleIDToUploadMeraHTFiles=="+m_circleIDToUploadMeraHTFiles.toString()+"length->"+m_circleIDToUploadMeraHTFiles.length);
		
		if(m_circleIDToUploadMeraHTFiles!=null && m_circleIDToUploadMeraHTFiles.length>0 ){
			feedURL=getFeedUrl(m_circleIDToUploadMeraHTFiles);
			logger.info("feedURL=="+feedURL);
		}
		
		if(getParamAsString("COMMON", "LOCAL_UGC_DIR", null)!=null && this.feedURL!=null && getParamAsString("COMMON", "LOCAL_UPLOADED_UGC_DIR", null)!=null){
			logger.info("RBT::localDir== " + getParamAsString("COMMON", "LOCAL_UGC_DIR", null) + ", feedURL == "+ this.feedURL);
			createdSuccessfully=true;
		}else{
			if(getParamAsString("COMMON", "LOCAL_UGC_DIR", null)==null){
				logger.info("localDir is null");
			}
			if(this.feedURL==null){
				logger.info("feedURL is null");
			}
		}
		logger.info("exiting UGCContentUploadDaemon constructor");
	}
	
	private String getParamAsString(String type, String param, String defualtVal)
	{
		try{
			return m_ParametersCacheManager.getParameter(type, param, defualtVal).getValue();
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +"  type ->"+type);
			return defualtVal;
		}
	}
	
	public void run(){
		logger.info("inside start");
		while (createdSuccessfully) {
			try {
				logger.info("going inside uploadWavFilesToPlayers");
				uploadMeraHTWavFilesToPlayers();
				logger.info("came out of uploadWavFilesToPlayers");
				sleep();
			} catch(Exception e) {
				logger.error(e.getMessage(), e);
				e.printStackTrace();
			}
		}
		logger.info("Exiting the run method of the UGCContentUploadDaemon");
	}
	
	private void sleep() {
		long nexttime = getnexttime(sleepMins);
		calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date(nexttime));
		logger.info("RBT::Sleeping till " + calendar.getTime()
				+ " for next processing !!!!!");
		long diff = (calendar.getTime().getTime() - Calendar.getInstance().getTime().getTime());
		try {
			if (diff > 0)
				Thread.sleep(diff);
			else
				Thread.sleep(sleepMins * 60 * 1000);
		}
		catch (InterruptedException e) {
			logger.error("", e);
			createdSuccessfully=false;
		}
	}
	
	public long getnexttime(int sleep) {
		Calendar now = Calendar.getInstance();
		now.setTime(new java.util.Date(System.currentTimeMillis()));
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);

		long nexttime = now.getTime().getTime();
		while (nexttime < System.currentTimeMillis()) {
			nexttime = nexttime + (sleep * 60 * 1000);
		}

		logger.info("RBT::getnexttime" + new Date(nexttime));
		return nexttime;
	}


	private void uploadMeraHTWavFilesToPlayers() {
		logger.info("inside uploadWavFilesToPlayers");
		File localDirectory=new File(getParamAsString("COMMON", "LOCAL_UGC_DIR", null));
		File[] files=localDirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				name = name.toLowerCase();
				if(name.endsWith(".wav") && name.length() > 10 && !name.startsWith("rbt_")) {
					return true;
				}
				return false;
			}
		});
		for(int count=0;count<files.length;count++){
			logger.info("current filename=="+files[count].getName());
			ArrayList playerURLList=null;
			if(files[count].isDirectory()){ 
				//hey, return from here;
				continue;
			}
			copyUgcClipToAllCircles(localDirectory, files[count]);
		}
		logger.info("exiting uploadMeraHTWavFilesToPlayers");		

	}
	
	private void copyUgcClipToAllCircles(File localDirectory, File file) {
		boolean uploadedToAllURL=true;
		String originalFileName = file.getName();
		String renameToFileName = "rbt_"+originalFileName.replaceFirst(".wav", "_rbt.wav");
        File newFile = new File(file.getParentFile() + File.separator + renameToFileName);
		for(int k=0;m_circleIDToUploadMeraHTFiles!=null && k<m_circleIDToUploadMeraHTFiles.length;k++){
			ArrayList playerURLList=null;
			String circleId = m_circleIDToUploadMeraHTFiles[k];
			logger.info("CirleId ="+circleId);
			if(circleId == null) {
				continue;
			}
			playerURLList=(ArrayList)circleIdToPlayerUrlMap.get(circleId.trim());
			logger.info("Player URL List ="+playerURLList);
			if(playerURLList == null || playerURLList.size() <= 0) {
				continue;
			}
			for(int countVar=0;countVar<playerURLList.size();countVar++){
				String playerURL=(String)playerURLList.get(countVar);
				HttpParameters httpParam = Tools.getHttpParamsForURL(playerURL+"|6000|15000", null);
				httpParam.setParamsAsParts(true);
				playerURL = httpParam.getUrl();
				File[] arrfile = new File[1];
                file.renameTo(newFile);
				arrfile[0] = newFile;

				HashMap params = new HashMap();
				params.put(FEED, UGCFILE);
				String fileResponse =null;
				try {
					fileResponse=RBTHTTPProcessing.postFile(httpParam, params, arrfile);
				} catch (Exception e) {
					e.printStackTrace();
					logger.info("got Exception " +e.getMessage());
					uploadedToAllURL=false;
					continue;
				}

				if(fileResponse!=null){
					fileResponse = fileResponse.trim();
				}
				logger.info("RBT:: url -> " + playerURL + ", Response -> " + fileResponse);
				if(fileResponse!=null && fileResponse.indexOf("SUCCESS")!=-1 ){
					logger.info("going to move file " +file.getName()+  " from  "+localDirectory.getAbsolutePath()+" to "+ new File(getParamAsString("COMMON", "LOCAL_UPLOADED_UGC_DIR", null)).getAbsolutePath());
				}else{
					uploadedToAllURL=false;
				}
			}
			logger.info("RBT:: circleId -> " + circleId + ", uploadedToAllURL -> " + uploadedToAllURL);
		}
		if(uploadedToAllURL){
			logger.info(" uploadedToAllURL == true");
			//logger.info("fileName=="+fileNameTemp);
			String uploadedFilesDirectory = getParamAsString("COMMON", "LOCAL_UPLOADED_UGC_DIR", null); 
			File uploadedFile = new File(uploadedFilesDirectory + File.separator+renameToFileName);
			newFile.renameTo(uploadedFile);

			//Modify the ExtraInfo column of the RBT_UGC_CLIPS table
			String wavFile = originalFileName.substring(0, originalFileName.indexOf(".wav"));
			logger.info("WAV file ->"+wavFile);
			UgcClip ugcClip=null;
			try {
				ugcClip = UgcClipDAO.getUgcClipByWavFile(wavFile);
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(ugcClip!=null) {
				ugcClip.setClipExtraInfo("FILE_DOWNLOADED");
				try {
					UgcClipDAO.updateClip(ugcClip);
				} catch (DataAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				logger.info("Ugc clip is not present"+ugcClip);
			}
			logger.info("WAV file ->"+wavFile + " - extra info updated to FILE_DOWNLOADED");
		} else {
			if(newFile.exists()) {
				logger.error(newFile.getAbsolutePath() + " renaming to " + file.getAbsolutePath());
				newFile.renameTo(file);
			}
		}

	}
	private String getFeedUrl(String[] sites){
		logger.info("RBT:: inside getFeedUrl");
		ArrayList arrSites=new ArrayList();
		for(int count=0;count<sites.length;count++){
			arrSites.add(sites[count]);
		}
		List<SitePrefix> prefix=CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		String temp=null;
		String feedUrl=null;
		for(int i=0;i<prefix.size();i++){
			logger.info("RBT:: count "+i);
			logger.info("RBT:: looking for circle id in site prefix table -> " + prefix.get(i).getCircleID());
			if(prefix.get(i).getCircleID()!=null){

				logger.info("RBT:: circle id " + prefix.get(i).getCircleID()+"exist in sites array");
				temp=prefix.get(i).getPlayerUrl();
				logger.info("RBT:: url=="+temp);
				if(temp!=null){
					ArrayList urlList=null;
					logger.info("RBT:: url is not null");
					temp=temp.trim();
					if(temp!=null && temp.indexOf(",")!=-1){
						StringTokenizer st=new StringTokenizer(temp,",");
						String tempStr=null;
						while(st.hasMoreElements()){
							String tempUrl=st.nextToken();
							if(tempUrl!=null){
								if(urlList==null){
									urlList=new ArrayList();
								}
								tempUrl=tempUrl.substring(0, tempUrl.lastIndexOf("/"));
								logger.info("RBT:: temp value is "+tempUrl);
								tempUrl=tempUrl+"/RecordOwnDownloader/rbt_downloadFile.jsp?";
								logger.info("RBT:: temp value is "+tempUrl);
								if(tempStr==null){
									tempStr=tempUrl;
								}else{
									tempStr=tempStr+";"+tempUrl;
								}
								urlList.add(tempUrl);
							}
						}
						if(urlList!=null && urlList.size()>0){
							if(prefix.get(i).getCircleID()!=null ){
								circleIdToPlayerUrlMap.put(prefix.get(i).getCircleID().trim(), urlList);
								if(i==0){
									feedUrl=tempStr;
									logger.info("RBT:: FeedUrl is "+feedUrl);
								}else{
									feedUrl=feedUrl+","+tempStr;
									logger.info("RBT:: FeedUrl is "+feedUrl);
								}
							}
						}
					}else{
						temp=temp.substring(0, temp.lastIndexOf("/"));
						logger.info("RBT:: temp value is "+temp);
						temp=temp+"/RecordOwnDownloader/rbt_downloadFile.jsp?";
						logger.info("RBT:: temp value is "+temp);
						if(prefix.get(i).getCircleID()!=null && temp!=null){
							urlList=new ArrayList();
							urlList.add(temp);
							circleIdToPlayerUrlMap.put(prefix.get(i).getCircleID().trim(), urlList);
							if(i==0){
								feedUrl=temp;
								logger.info("RBT:: FeedUrl is "+feedUrl);
							}else{
								feedUrl=feedUrl+","+temp;
								logger.info("RBT:: FeedUrl is "+feedUrl);
							}
						}
					}
				}
			}
		}
		logger.info("RBT:: inside getFeedUrl with feedurl=="+feedUrl);
		return feedUrl;

	}

	
}