package com.onmobile.apps.ringbacktones.daemons.adrbt;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * @author sridhar.sindiri
 *
 */
public class AdRBTPromptUploadDaemon extends Thread
{
	private static Logger logger = Logger
			.getLogger(AdRBTPromptUploadDaemon.class);

	private static AdRBTPromptUploadDaemon adRBTPromptUploadDaemon = null;
	private static Object syncObj = new Object();

	public static boolean runDaemon = true;

	private String promptsDir = null;
	private String incompletePromptsDir = null;
	private String completedPromptsDir = null;

	private String playerURIToUploadFiles = null;
	private List<String> playerUrlList = new ArrayList<String>();

	public static boolean readPlayerUrlsFromDB = false;

	private static final String TABLE_NAME			= "RBT_SITE_PREFIX";
	private static final String SITE_NAME_COL		= "SITE_NAME";
	private static final String SITE_PREFIX_COL		= "SITE_PREFIX";
	private static final String SITE_URL_COL		= "SITE_URL";
	private static final String CIRCLE_ID_COL		= "CIRCLE_ID";
	private static final String ACCESS_ALLOWED_COL	= "ACCESS_ALLOWED";
	private static final String SUPPORTED_LANG_COL	= "SUPPORTED_LANG";
	private static final String PLAYER_URL_COL		= "PLAYER_URL";
	private static final String PLAY_UNCHARGED_COL	= "PLAY_UNCHARGED";

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		adRBTPromptUploadDaemon = AdRBTPromptUploadDaemon.getInstance();
		adRBTPromptUploadDaemon.setName("ADRBT_PROMPT_UPLOAD_DAEMON");
		adRBTPromptUploadDaemon.start();
	}

	/**
	 * 
	 */
	private AdRBTPromptUploadDaemon() 
	{
		init();
	}

	/**
	 * @return the Instance of AdRBTPromptUploadDaemon
	 */
	public static AdRBTPromptUploadDaemon getInstance() 
	{
		if (adRBTPromptUploadDaemon == null)
		{
			synchronized (syncObj) 
			{
				if (adRBTPromptUploadDaemon == null) 
				{
					try 
					{
						adRBTPromptUploadDaemon = new AdRBTPromptUploadDaemon();
					}
					catch (Throwable e) 
					{
						logger.error("", e);
						adRBTPromptUploadDaemon = null;
					}
				}
			}
		}
		return adRBTPromptUploadDaemon;
	}

	/**
	 * 
	 */
	private void init()
	{
		if (AdRBTConfigManager.getInstance() == null || !runDaemon)
		{
			logger.warn("Initialization failed as properties file is not configured");
			return;
		}
		
		readPlayerUrlsFromDB = AdRBTConfigManager.getInstance().getParameter("GET_PLAYER_URLS_FROM_DB").equalsIgnoreCase("true");
		promptsDir = AdRBTConfigManager.getInstance().getParameter("PROMPTS_DIR");
		incompletePromptsDir = AdRBTConfigManager.getInstance().getParameter("UNPROCESSED_PROMPTS_DIR");
		completedPromptsDir = AdRBTConfigManager.getInstance().getParameter("PROCESSED_PROMPTS_DIR");
		playerURIToUploadFiles = AdRBTConfigManager.getInstance().getParameter("PLAYER_URI_TO_UPLOAD_FILES");
		
		if (promptsDir == null || incompletePromptsDir == null 
				|| completedPromptsDir == null || playerURIToUploadFiles == null)
		{
			logger.warn("Stopping the daemon since one of the prompts directories have not been configured");
			stopThread();
		}

		initPromptsLocalDirs();
		populatePlayerUrlList();
	}
	
	/**
	 * 
	 */
	private void initPromptsLocalDirs()
	{
		File promptsDirectory = new File(promptsDir);
		File unProcessedPromptsDirectory = new File(incompletePromptsDir);
		File processedPromptsDirectory = new File(completedPromptsDir);
		if (!promptsDirectory.exists())
		{
			logger.info("Stopping the daemon since the prompts directory is not a valid path");
			stopThread();
		}
		else if (!unProcessedPromptsDirectory.exists())
		{
			unProcessedPromptsDirectory.mkdirs();
		}
		else if (!processedPromptsDirectory.exists())
		{
			processedPromptsDirectory.mkdirs();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		while (runDaemon)
		{
			try
			{
				File promptsDirectory = new File(promptsDir);
				File[] filesList = promptsDirectory
						.listFiles(new FilenameFilter()
				{
					public boolean accept(File dir, String name)
					{
						if (!name.endsWith(".wav"))
							return false;
						else
							return true;
					}
				});

				Arrays.sort(filesList, new Comparator<File>()
				{
					public int compare(File f1, File f2)
					{
						return Long.valueOf(f2.lastModified()).compareTo(
								f1.lastModified());
					}
				});

				if (filesList.length == 0)
					logger.info("No files in the directory, so not processing");

				for (File promptFile : filesList)
				{
					if (promptFile.length() == 0)
					{
						// As file size is zero, directly moving to completed
						// folder without uploading to player.
						moveFileToCompletedFolder(promptFile);
					}

					List<String> playerUrls = null;
					File incompleteUploadedFile = new File(incompletePromptsDir, promptFile.getName() + ".tmp");
					if (incompleteUploadedFile.exists())
						playerUrls = getPlayerUrlsByPromptFile(incompleteUploadedFile);
					else
						playerUrls = playerUrlList;

					List<String> failurePlayerUrlsList = new ArrayList<String>();
					for (String playerUrl : playerUrls)
					{
						Map<String, String> requestParams = new HashMap<String, String>();
						Map<String, File> fileParams = new HashMap<String, File>();
						fileParams.put(playerUrl, promptFile);

						if (!postPromptFileToTonePlayer(playerUrl, requestParams, fileParams))
						{
							logger.warn("Upload failed for the prompt file : "
									+ promptFile.getName() + " playerUrl : "
									+ playerUrl);

							failurePlayerUrlsList.add(playerUrl);
						}
					}

					if (failurePlayerUrlsList.size() == 0)
					{
						moveFileToCompletedFolder(promptFile);
					}
					else
					{
						writeFailureRecordToFile(promptFile.getName(),
								failurePlayerUrlsList);
					}
				}

				String sleepTimeStr = AdRBTConfigManager.getInstance().getParameter("SLEEP_TIME_IN_MINS");
				logger.info("Sleeping for " + sleepTimeStr + " minutes ");
				Thread.sleep(Integer.parseInt(sleepTimeStr) * 60 * 1000);
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * 
	 */
	private void populatePlayerUrlList()
	{
		if (readPlayerUrlsFromDB)
		{
			List<SitePrefix> prefix = getAllSitePrefixesFromDB();
			for (SitePrefix sitePrefix : prefix) 
			{
				if (sitePrefix.getCircleID() == null)
				{
					logger.warn("circleID is null, so skipping the entry: " + sitePrefix);
					continue;
				}
				String playerUrlsStr = sitePrefix.getPlayerUrl();
				logger.info("RBT:: playerUrlsStr :" + playerUrlsStr);

				if (playerUrlsStr != null && sitePrefix.getSiteUrl() == null)
				{
					playerUrlsStr = playerUrlsStr.trim();
					String[] playerUrls = playerUrlsStr.split(",");
					initPlayerURLs(playerUrls);
				}
			}
		}
		else
		{
			String playerUrlsStr = AdRBTConfigManager.getInstance().getParameter("PLAYER_URLS_FOR_PROMPTS_UPLOAD");
			if(playerUrlsStr == null || playerUrlsStr.length() <= 0) 
			{
				logger.error("Missing configuration parameter: PLAYER_URLS_FOR_PROMPTS_UPLOAD");
				stopThread();
				return;
			}
			String[] playerUrls = playerUrlsStr.split(",");
			initPlayerURLs(playerUrls);
		}
	}
	
	/**
	 * @return the list of all SitePrefixes
	 */
	private List<SitePrefix> getAllSitePrefixesFromDB()
	{
		List<SitePrefix> sitePrefixesList = new ArrayList<SitePrefix>();
		Connection conn = null;
		try 
		{
			String dbUrl = AdRBTConfigManager.getInstance().getParameter("DB_URL");
			if (dbUrl == null || dbUrl.length() == 0)
			{
				logger.warn("DB URL not configured, so stopping the daemon");
				stopThread();
				return sitePrefixesList;
			}
			
			String userName = AdRBTConfigManager.getInstance().getParameter("DB_USERNAME");
			String password = AdRBTConfigManager.getInstance().getParameter("DB_PASSWORD");
			ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
			// Changes done for URL Encryption and Decryption
			try {
				if (resourceBundle.getString("ENCRYPTION_MODEL") != null
						&& resourceBundle.getString("ENCRYPTION_MODEL")
								.equalsIgnoreCase("yes")) {
					userName = URLEncryptDecryptUtil
							.decryptUserNamePassword(userName);
					password = URLEncryptDecryptUtil
							.decryptUserNamePassword(password);
				}
			} catch (MissingResourceException e) {
				logger.error("resource bundle exception: ENCRYPTION_MODEL");
			}
			// End of URL Encryption and Decryption
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(dbUrl, userName, password);

			String query = "SELECT * FROM " + TABLE_NAME;
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(query);
			while(rs.next())
			{
				SitePrefix sitePrefix = new SitePrefix();
				sitePrefix.setSiteName(rs.getString(SITE_NAME_COL));
				sitePrefix.setSitePrefix(rs.getString(SITE_PREFIX_COL));
				sitePrefix.setSiteUrl(rs.getString(SITE_URL_COL));
				sitePrefix.setCircleID(rs.getString(CIRCLE_ID_COL));
				sitePrefix.setAccessAllowed(rs.getString(ACCESS_ALLOWED_COL));
				sitePrefix.setSupportedLanguage(rs.getString(SUPPORTED_LANG_COL));
				sitePrefix.setPlayerUrl(rs.getString(PLAYER_URL_COL));
				sitePrefix.setPlayerUncharged(rs.getString(PLAY_UNCHARGED_COL));
				
				sitePrefixesList.add(sitePrefix);
			}
		} 
		catch (ClassNotFoundException e) 
		{
			logger.error("", e);
		} 
		catch (SQLException e) 
		{
			logger.error("", e);
		}
		finally
		{
			try 
			{
				if (conn != null)
					conn.close();
			} 
			catch (SQLException e) 
			{
			}
		}
		
		return sitePrefixesList;
	}
	
	/**
	 * @param playerUrls
	 */
	private void initPlayerURLs(String[] playerUrls) 
	{
		for (String playerUrl : playerUrls)
		{
			playerUrl = playerUrl.substring(0, playerUrl.lastIndexOf("/"));
			playerUrl += "/" + playerURIToUploadFiles + "?";
			playerUrlList.add(playerUrl);
		}
	}
	
	/**
	 * @return the list of playerUrls 
	 */
	private List<String> getPlayerUrlsByPromptFile(File promptFile) 
	{
		List<String> playerUrlList = new ArrayList<String>();
		BufferedReader br = null;
		try 
		{
			br = new BufferedReader(new FileReader(promptFile));
			String line = null;
			while ((line = br.readLine()) != null)
			{
				line = line.trim();
				if (line.length() == 0)
					continue;
				playerUrlList.add(line);
			}
		} 
		catch (IOException e) 
		{
			logger.error("", e);
		}
		finally
		{
			try 
			{
				if (br != null)
					br.close();
			} 
			catch (IOException e) 
			{
			}
		}
		logger.info("RBT:: playerUrlsList from temp file :" + playerUrlList);
		return playerUrlList;
	}

	/**
	 * @param url
	 * @param requestParams
	 * @param fileParams
	 * @return the status for posting the prompt file
	 */
	private boolean postPromptFileToTonePlayer(String url, Map<String, String> requestParams, Map<String, File> fileParams) 
	{
		String response = "ERROR";
		logger.info("RBT:: player url: " + url + " Request params : " + requestParams);
		try 
		{
			HttpParameters httpParameters = new HttpParameters(url);
			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, requestParams, fileParams);
			logger.info("RBT:: httpResponse: " + httpResponse);
			response = httpResponse.getResponse().trim();
		}
		catch (Exception e) 
		{
			logger.error("RBT:: " + e.getMessage(), e);
		}

		return (response.equalsIgnoreCase("SUCCESS"));
	}

	/**
	 * @param promptFile
	 */
	private void moveFileToCompletedFolder(File promptFile)
	{
		File destination = new File(completedPromptsDir, promptFile.getName() + ".done");
		try
		{
			copyFile(promptFile, destination);
			if (!promptFile.delete())
				logger.warn("Failed to delete the file: " + promptFile.getAbsolutePath());
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	/**
	 * @param promptName
	 * @param playerUrl
	 */
	private void writeFailureRecordToFile(String promptName, List<String> playerUrlList) 
	{
		BufferedWriter bufferedWriter = null;
		try
		{
			File resultFile = new File(incompletePromptsDir, promptName + ".tmp");

			FileWriter fileWriter = new FileWriter(resultFile, true);
			bufferedWriter = new BufferedWriter(fileWriter);

			for (String playerUrl : playerUrlList)
			{
				bufferedWriter.write(playerUrl);
				bufferedWriter.newLine();
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		finally
		{
			if (bufferedWriter != null)
			{
				try
				{
					bufferedWriter.close();
				}
				catch (IOException e)
				{
					logger.error("", e);
				}
			}
		}
	}

	/**
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	private void copyFile(File source, File destination) throws IOException
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
				logger.error("", e);
			}
		}
	}

	/**
	 * stops the daemon thread
	 */
	public void stopThread()
	{
		runDaemon = false;
	}

	/**
	 * starts the daemon thread
	 */
	public void startThread()
	{
		runDaemon = true;
	}
}
