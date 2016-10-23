package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.FileLastModifiedTimeComparator;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.CategoriesDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.utils.KeyExchange;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.SftpFile;

/**
 * @author vasipalli.sreenadh
 * Gets feed clips from content cache (CONTENT_TYPE=FEED)
 * Read the clip file path from parameters cache (For each clip path will be defined )
 * Download the file clip into local path with file name as clipRbtWavFile and publishes to the tone player.
 * If publishing is success, delete file from local path and also from clip file path
 *
 */
public class FeedClipDownloadNuploader extends Thread implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(FeedClipDownloadNuploader.class);
	
	private HashMap<String, ArrayList<String>> circleIdToPlayerUrlMap = new HashMap<String, ArrayList<String>>();
	private String playerContentPage = null;
	private boolean sftpDownloadClips = false;

	
	public static FeedClipDownloadNuploader feedClipDownloadNuploader= null;
	private static boolean m_Continue = true;
	
	private long lastDbAccessTime = -1;
	private List<Clip> feedClipList = new ArrayList<Clip>();
	private List<Category> feedCategoryList = new ArrayList<Category>();
	private ResourceBundle resourceBundle = null;
	private String server = null;
	private String port = null;
	private String user = null;
	private String pwd = null;
	private String timeout = null;
	private String proxyPort = null;
	private String proxyHost = null;
	private String proxyUsername = null;
	private String proxyPassword = null;


	private FileLastModifiedTimeComparator fileLastModifiedTimeComparator = new FileLastModifiedTimeComparator(); 
	public FeedClipDownloadNuploader() 
	{
		init();
	}

	public void run()
	{
		logger.info("Starting the thread "+m_Continue);
		while(m_Continue)
		{
			try
			{
//				resourceBundle = ResourceBundle.getBundle("feedConfig");

				if (lastDbAccessTime == -1 || ((lastDbAccessTime + 2*60*60*1000) > System.currentTimeMillis()))
				{
					// Goes to DB every 2 Hours
					feedClipList = getFeedClips();
					feedCategoryList = getFeedShuffleCategories();
				}

				if ((feedClipList != null && feedClipList.size() != 0) || (feedCategoryList != null && feedCategoryList.size() != 0))
				{	
					if(feedClipList != null && feedClipList.size() > 0)
					{
						for (Clip clip : feedClipList) 
							processClip(clip);
					}
					if(feedCategoryList != null && feedCategoryList.size() > 0)
					{
						for (Category category : feedCategoryList) 
						{
							processShuffle(category);
						}	
					}
					
				}
				else
					logger.info("No Feed clips or categories exists in DB");
				
				try 
				{
					int sleepTime = 10;
					try
					{
						sleepTime = Integer.parseInt(resourceBundle.getString("SLEEP_TIME_MIN"));
					}
					catch (Exception e) 
					{
						sleepTime = 10;
					}
					Thread.sleep(sleepTime*60*1000);
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
		resourceBundle = ResourceBundle.getBundle("feedConfig");
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "PLAYER_CONTENT_URL_PAGE");
		Parameters param2 = CacheManagerUtil.getParametersCacheManager().getParameter(COMMON, "USE_SFTP_FILE_DOWNLOAD","FALSE");

		server = resourceBundle.getString("SFTP_SERVER_IP");
		port = resourceBundle.getString("SFTP_PORT");
		user = resourceBundle.getString("SFTP_USERNAME");
		pwd = resourceBundle.getString("SFTP_PASSWORD");
		timeout = resourceBundle.getString("SFTP_TIMEOUT");
		proxyHost = resourceBundle.getString("SFTP_PROXY_HOST");
		proxyPassword = resourceBundle.getString("SFTP_PROXY_PASSWORD");
		proxyPort = resourceBundle.getString("SFTP_PROXY_PORT");
		proxyUsername = resourceBundle.getString("SFTP_PROXY_USERNAME");

		if(param != null)
        	playerContentPage = param.getValue();
		if(param2.getValue().equalsIgnoreCase("true"))
			sftpDownloadClips = true ;
		
		populateCircleIDPlayerMap();
		logger.info("circleIDPlayerUrlMap >"+circleIdToPlayerUrlMap);
	}

	public void populateCircleIDPlayerMap()
	{
		List<SitePrefix> prefix=CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();

		for (SitePrefix sitePrefix : prefix) 
		{
			logger.info("RBT:: looking for circle id in site prefix table :" + sitePrefix.getCircleID());
			if(sitePrefix.getCircleID()!= null)
			{
				String playerUrlsStr = sitePrefix.getPlayerUrl();
				logger.info("RBT:: playerUrlsStr :"+playerUrlsStr);

				if(playerUrlsStr != null && sitePrefix.getSiteUrl() == null)
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
								tempUrl=tempUrl+"/"+playerContentPage;
								logger.info("RBT:: temp url value is :"+tempUrl);

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
						playerUrlsStr = playerUrlsStr+"/"+playerContentPage+"?";

						logger.info("RBT:: playerUrlString is :"+playerUrlsStr);
						urlList.add(playerUrlsStr);
						circleIdToPlayerUrlMap.put(sitePrefix.getCircleID().trim(), urlList);
					}
				}
			}
		}
	}

	/**
	 * @return
	 */
	private List<Clip> getFeedClips()
	{
		List<Clip> clipList = new ArrayList<Clip>();
		String selectUGCClips = "from Clip where contentType = 'FEED'";
		try 
		{ 	 
			clipList = ClipsDAO.getClips(selectUGCClips); 	 
	    }
		catch (DataAccessException e) 	 
	    { 	 
		  logger.error("", e);
	    }
		
		if (clipList != null && clipList.size() > 0)
			lastDbAccessTime = System.currentTimeMillis();
		
		return clipList;
	}

	private List<Category> getFeedShuffleCategories()
	{
		List<Category> categoryList = new ArrayList<Category>();
		try 
		{ 	 
			categoryList = CategoriesDAO.getCategoryType(Integer.toString(iRBTConstant.FEED_SHUFFLE)); 	 
	    }
		catch (DataAccessException e) 	 
	    { 	 
		  logger.error("", e);
	    }
		
		if (categoryList != null && categoryList.size() > 0)
			lastDbAccessTime = System.currentTimeMillis();
		
		return categoryList;
	}
	/**
	 * @param fileToBeUploaded
	 * @return
	 */
	private boolean uploadFileToTonePlayer(File fileToBeUploaded)
	{
		Set<String> circleIDSet = circleIdToPlayerUrlMap.keySet(); 

		boolean isClipUploadedToAll = true;
		for (String circleID : circleIDSet) 
		{
			logger.info("circleID "+circleID);
			ArrayList<String> playerUrlList = circleIdToPlayerUrlMap.get(circleID);
			if(playerUrlList == null || playerUrlList.size() == 0) 
			{
				logger.info("player url list for circle :"+circleID +" is null");
				continue;
			}
			boolean isFeedClipUploaded = false;
			for (String playerUrl : playerUrlList) 
			{
				logger.info("circleID :"+circleID +" & Player url :"+playerUrl);
				try
				{
					HttpParameters httpParameters = new HttpParameters();
					httpParameters.setUrl(playerUrl);
					httpParameters.setConnectionTimeout(6000);

					HashMap<String, String> requestParameters = new HashMap<String, String>();
					String clipRBTName = fileToBeUploaded.getName();
					if(clipRBTName !=  null && clipRBTName.endsWith(".wav"))
						clipRBTName = clipRBTName.substring(0,clipRBTName.length()-4);
					requestParameters.put("TYPE", clipRBTName); 
					requestParameters.put("STATUS","ON"); 

					HashMap<String, File> fileParameters = new HashMap<String, File>();
					fileParameters.put(fileToBeUploaded.getName(), fileToBeUploaded);
					
					HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, requestParameters, fileParameters);
					logger.info("HttpResponse :"+httpResponse);
					if (httpResponse != null && httpResponse.getResponse() != null && httpResponse.getResponse().indexOf("SUCCESS") != -1)
					{
						logger.info("Feed clip uploaded Successfully");
						isFeedClipUploaded = true;
					}
					else
						isFeedClipUploaded = false;
				}
				catch(Exception e)
				{
					logger.error("", e);
					isFeedClipUploaded = false;
				}
				isClipUploadedToAll = (isClipUploadedToAll && isFeedClipUploaded) ;
			}
		}
		return isClipUploadedToAll;
	}

	/**
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public void copyFile(File source, File destination) throws IOException
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

	private File getLatestModifiedFile(File[] files)
	{
		File lastModifiedFile = files[0];
		long lastModifiedTime = lastModifiedFile.lastModified();
		for (File file : files) 
		{
			if (file.lastModified() > lastModifiedTime)
			{
				lastModifiedTime = file.lastModified();
				lastModifiedFile = file;
			}
		}
		return lastModifiedFile;
	}

	public void stopThread()
	{
		if (feedClipDownloadNuploader != null && feedClipDownloadNuploader.isAlive())
		{
			try
			{
				m_Continue = false;
				feedClipDownloadNuploader.interrupt();
			}
			catch(Throwable e)
			{
				logger.error("", e);
			}
		}
	}
	
	
	private void copySFTPClips(String sftpPath , String localPath)
	{
		logger.info("Trying to connect to SFTP server at path " + sftpPath );
		SftpClient sftp =null;
		SshClient ssh =null;
		//FileWriter logWriter=null;
		try{
			String hostName=server;
			String sftpUser=user;
			String sftpPassword=pwd;
			String socketTimeOut=timeout;

			ConfigurationLoader.initialize(false);
			ssh = new SshClient();
			// Connect to the host
			SshConnectionProperties prop=new SshConnectionProperties();
			if(hostName != null)
				prop.setHost(hostName);
			if(sftpUser != null)
				prop.setUsername(sftpUser);
			if(port != null)
				prop.setPort(Integer.parseInt(port));
			if(proxyHost != null)
				prop.setProxyHost(proxyHost);
			if(proxyPassword != null)
				prop.setProxyPassword(proxyPassword);
			if(proxyPort != null)
				prop.setProxyPort(Integer.parseInt(proxyPort));
			if(proxyUsername != null)
				prop.setProxyUsername(proxyUsername);
			
			

			ssh.setSocketTimeout(Integer.parseInt(socketTimeOut));
			logger.info("Connecting to the SFTP server.."); 
			ssh.connect(prop,new KeyExchange());
			// Create a password authentication instance
			logger.info("Connected to the SFTP server.."); 
			PasswordAuthenticationClient pwd = new PasswordAuthenticationClient();
			pwd.setUsername(sftpUser);
			// Get the password
			pwd.setPassword(sftpPassword);
			// Try the authentication
			logger.info("Authenticating password to the SFTP server.."); 
			int result = ssh.authenticate(pwd);
			// Evaluate the result
			if (result == AuthenticationProtocolState.COMPLETE) {
				logger.info(" password Authenticated:) Opening SFTPClient"); 
				// The connection is authenticated we can now do some real work!
				sftp = ssh.openSftpClient();
				logger.info("Opened SFTPClient Going to dest dir "+sftpPath+" in SFTP");
				try{
					sftp.cd(sftpPath);
					logger.info("Entered into dest dir"+sftpPath+" in SFTP");
				}
				catch(IOException ioe){
					logger.info("Either the folder does not exist or its not a directory ");
					logger.info("Could not get into the remote folder due to "+ioe.getMessage()+"\n");
					return ;
				}
				logger.info("Existing sftp dir=="+sftp.pwd().toString());
				List<SftpFile> contentList=(List<SftpFile>)sftp.ls();
				if(contentList!=null && contentList.size()>0){
					logger.info("File array, in dest dir, is of size"+contentList.size());

					File file = new File(localPath);
					if(!file.exists())
						file.mkdirs();
					sftp.lcd(localPath);
					for(int t=0;t<contentList.size();t++){
						try
						{
							if(contentList.get(t).isFile()){
								logger.info("Transferring "+contentList.get(t).getFilename()+" from dest dir to local folder== "+localPath);
								sftp.get(contentList.get(t).getFilename());
								contentList.get(t).delete();
								logger.info("Transferred "+contentList.get(t).getFilename()+" from dest dir to local folder== "+localPath);
							}
						}
						catch (IOException e) 
						{
							logger.error(e);
//							e.printStackTrace();
						}
					}

				}
			}
		}
		catch(ConfigurationException ce){
			logger.info(ce.getMessage());
			logger.error(ce);
//			ce.printStackTrace();
		}
		catch(IOException ioe){
			logger.info(ioe.getMessage());
			logger.error(ioe);
//			ioe.printStackTrace();
		}
		catch(Exception ioe){
			logger.info(ioe.getMessage());
			logger.error(ioe);
//			ioe.printStackTrace();
		}
		finally{
			ssh.disconnect();
			if(sftp!=null ){
				if(!sftp.isClosed()){
					logger.info("SSH not closed. Trying again..");
					ssh.disconnect();
				}
			}else{
				logger.info("sftp is not initialized yet...");
			}
		}
	}
	
	
	private void processClip(Clip clip)
	{
		try
		{
			String clipPath = resourceBundle.getString(clip.getClipId()+"_PATH");
			if (clipPath != null)
			{
				if(sftpDownloadClips){
					String sftpPath = resourceBundle.getString(clip.getClipId()+"_SFTP_PATH");
					if(sftpPath != null)
						copySFTPClips(sftpPath,clipPath);
					else
						logger.info("sftp path doesn't exists in config for clipID :"+clip.getClipId());
				}
				
				logger.info("clipPath for clipID :"+clip.getClipId() + " is :"+clipPath);
				File fileClip = new File(clipPath); 	// Clip directory
				File[] files = fileClip.listFiles();    // Check for clips in Directory
				
				logger.info("");
				if (files == null || files.length == 0)
				{
					logger.info("Path is not accessible or no clips in path");
					return;
				}
				File latestModifiedFile = getLatestModifiedFile(files); // get the latestModified file
				File destFile = new File(clip.getClipRbtWavFile()+".wav"); // Creating file with clipRbtWavFile name
				try
				{
					copyFile(latestModifiedFile, destFile); // Copying the latestModified file to local path
					if (uploadFileToTonePlayer(destFile))   // Uploading file to Tone player
					{
						if (destFile.exists())
							destFile.delete();
						if (latestModifiedFile.exists())
							latestModifiedFile.delete();
					}
				}
				catch (IOException e) 
				{
					logger.error("", e);
				}
			}
			else
				logger.info("clip path doesn't exists in config for clipID :"+clip.getClipId());
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
	private void processShuffle(Category  category)
	{
		try
		{
			String categoryPath = resourceBundle.getString(category.getCategoryId()+"_SHUFFLE_PATH");
			if (categoryPath != null)
			{
				if(sftpDownloadClips){
					String sftpPath = resourceBundle.getString(category.getCategoryId()+"_SFTP_SHUFFLE_PATH");
					if(sftpPath != null)
						copySFTPClips(sftpPath,categoryPath);
					else
						logger.info("sftp path doesn't exists in config for categID :"+category.getCategoryId());
						
				}
				logger.info("categoryPath for categoryId :"+category.getCategoryId() + " is :"+categoryPath);
				File fileClip = new File(categoryPath); 	// Clip directory
				File[] files = fileClip.listFiles();    // Check for clips in Directory
				List<Clip> mappedClips = ClipsDAO.getActiveClipsInCategory(category.getCategoryId());
				
				logger.info("");
				if (files == null || files.length == 0)
				{
					logger.info("Path is not accessible or no clips in path");
					return;
				}
				if(mappedClips == null && mappedClips.size() == 0)
				{
					logger.info("No lcips mapped to feed shuflle "+category.getCategoryId());
					return;
				}
				List<File> allFilesinFolder = Arrays.asList(files);
				Collections.sort(allFilesinFolder, fileLastModifiedTimeComparator);
				int counter = 0;
				for(int i = allFilesinFolder.size() -1; i >= 0; i--)
				{
					try
					{
						File latestModifiedFile = allFilesinFolder.get(i);
						if(counter < mappedClips.size())
						{
							File destFile = new File(mappedClips.get(counter).getClipRbtWavFile()+".wav"); // Creating file with clipRbtWavFile name
							copyFile(latestModifiedFile, destFile); // Copying the latestModified file to local path
							if (uploadFileToTonePlayer(destFile))   // Uploading file to Tone player
							{
								if (destFile.exists())
									destFile.delete();
								if (latestModifiedFile.exists())
									latestModifiedFile.delete();
							}
						}
						else
						{
							if (latestModifiedFile.exists())
								latestModifiedFile.delete();
						}	
					}
					catch (IOException e) 
					{
						logger.error("", e);
					}
					counter++;
				}
			}
			else
				logger.info("category path doesn't exists in config for clipID :"+category.getCategoryId());
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
}
