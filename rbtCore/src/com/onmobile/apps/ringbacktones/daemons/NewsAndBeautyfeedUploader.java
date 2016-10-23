package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class NewsAndBeautyfeedUploader extends Thread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(NewsAndBeautyfeedUploader.class);

	private Calendar calendar = Calendar.getInstance();
	private int sleepMins = 5;
	public String m_dateFormat = "yyyyMMdd_HHmmss";
	private String localDir = null;
	public boolean createdSuccessfully=false;
	public int m_feedUploadTime = 2;
	private ArrayList allPlayerIPConfig = null;
	private static String playerContentPage = null;
	private NewsAndBeautyFeedFileDownloaderFromFtp feedDownloaderFromFTP = null;

	private static  int m_nConn = 4;
	
	private boolean isLive = true;

	public static void main(String[] args) {
		Tools.init("NewsAndBeautyfeedUploader", true);

		logger.info("inside main");

		NewsAndBeautyfeedUploader instance = new NewsAndBeautyfeedUploader();
		logger.info("going to start the thread");
		instance.start();
		logger.info("exiting main");
	}
	
	public  NewsAndBeautyfeedUploader() {
		logger.info("inside NewsAndBeautyfeedUploader  constructor");
		
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_LOCAL_FILES_DIR_FOR_DOWNLOAD");
		if(param != null && param.getValue() != null)
		{
			localDir = param.getValue().trim();
		}
		logger.info("localDir=="+localDir);
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_DATE_FORMAT");
		if(param != null && param.getValue() != null)
		{
			m_dateFormat = param.getValue().trim();
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","NnB_FEED_UPLOAD_TIME");
		if(param != null && param.getValue() != null)
		{
			m_feedUploadTime = Integer.parseInt(param.getValue().trim());
		}
		CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();
		allPlayerIPConfig = RBTDBManager.getInstance().getLocalPlayerIP();
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "PLAYER_CONTENT_URL_PAGE");
		if(param != null)
        	playerContentPage = param.getValue();
        
		if(this.localDir!=null){
			logger.info("RBT::localDir== " + this.localDir);
			createdSuccessfully=true;
			feedDownloaderFromFTP=NewsAndBeautyFeedFileDownloaderFromFtp.initialize();
		}else{
			if(this.localDir==null){
				logger.info("local dir is null exiting daemon");
			}
		}
		logger.info("exiting NewsAndBeautyfeedUploader constructor");
	}
	
	public void run() {
		logger.info("inside start");
		if (createdSuccessfully) {
			if(feedDownloaderFromFTP!=null && feedDownloaderFromFTP.ftpClient!=null){
				logger.info("going to start NewsAndBeautyFeedFileDownloaderFromFtp thread");
				feedDownloaderFromFTP.start();
			}
			while(isLive)
			{
				logger.info("going inside uploadWavFilesToPlayers");
				uploadWavFilesToPlayers();
				logger.info("came out of uploadWavFilesToPlayers");
			
			// sleep
				sleep();
			}
		}
		
	}
	
	public void stopThread()
	{
		isLive = false;
		interrupt();
	}
	
	private void uploadWavFilesToPlayers() {
		
		logger.info("inside uploadWavFilesToPlayers");
		File localFile=new File(localDir);
		File[] files=localFile.listFiles();
		SimpleDateFormat sdf = new SimpleDateFormat(m_dateFormat);
		for(int count=0;count<files.length;count++){
			logger.info("current filename=="+files[count].getName());
			String playerURL=null;
			if(!files[count].isDirectory()){
				logger.info("current filename=="+files[count].getName()+" is not a directory");
				String fileName=files[count].getName();
				if((fileName.indexOf(".wav")==-1 && fileName.indexOf(".WAV")==-1) || fileName.indexOf("done") != -1)continue;
				String wavFile = null;
				String dateString = null;
				StringTokenizer stk = new StringTokenizer(fileName,"-");
				if(stk.hasMoreTokens())
				{
					wavFile = stk.nextToken();
				}
				if(stk.hasMoreTokens())
				{
					String next = stk.nextToken();
					dateString = next.substring(0,next.length()-4); 
				}
				Date date = null;
				try {
					date = sdf.parse(dateString);
				} catch (ParseException e1) {
					e1.printStackTrace();
				}
				Calendar cal = calendar.getInstance();
				cal.add(Calendar.HOUR_OF_DAY,-m_feedUploadTime);
				Date compareDate = cal.getTime();
				if(date.after(compareDate))
				{
					File[] arrfile = new File[1];
					arrfile[0] =files[count];
					HashMap params = new HashMap();
					params.put("TYPE", wavFile);
					params.put("STATUS","ON");
					
					if(allPlayerIPConfig == null)
		        	{
		        		logger.info("RBT::no player URL's to update");
		        		return;
		        	}
					else
		        		logger.info("RBT::total player url's to update = " + allPlayerIPConfig.size());
					if(allPlayerIPConfig != null)
					for(int i = 0;i < allPlayerIPConfig.size(); i++) {
						
		        		HttpParameters httpParams = Tools.getHttpParamsForURL(
		        				(String)allPlayerIPConfig.get(i), playerContentPage);
		        		httpParams.setParamsAsParts(true);
		            	logger.info("RBT::posting feed file to " + httpParams.getUrl());
		            	String response = null;
						
						try {
							response=RBTHTTPProcessing.postFile(httpParams, params, arrfile);
						} catch (HttpException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.info("got HttpException " +e.getMessage());
							continue;
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.info("got IOException " +e.getMessage());
							continue;
						} catch (RBTException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.info("got RBTException " +e.getMessage());
							continue;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							logger.info("got Exception " +e.getMessage());
							continue;
						}
						
						logger.info("RBT::response for posted feed file to " +
		            			httpParams.getUrl() + " is " + response);
					}
				}
				
				String newFileName = localDir + File.separator + fileName.substring(0,fileName.length()-4) + "-done" + fileName.substring(fileName.length()-4);
				
				File newFile = new File(newFileName);
				boolean success = files[count].renameTo(newFile);
				logger.info("the file is renamed "+success);
			}
		}
		logger.info("exiting uploadWavFilesToPlayers");		
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
}

