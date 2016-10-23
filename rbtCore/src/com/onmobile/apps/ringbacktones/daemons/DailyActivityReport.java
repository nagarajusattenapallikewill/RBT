package com.onmobile.apps.ringbacktones.daemons;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.utils.KeyExchange;
import com.onmobile.apps.ringbacktones.utils.URLEncryptDecryptUtil;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;

public class DailyActivityReport extends Thread{


	private static Logger logger = Logger.getLogger(DailyActivityReport.class);
	private RBTDaemonManager m_mainDaemonThread = null; 
	private static RBTDBManager rbtDBManager = null;
	ParametersCacheManager rbtParamCacheManager = null;
	String activityMachine = null;
	String contentMachine = null;
	String server = null;
	int port ;
	String user = null;
	String pwd = null;
	String dir = null;
	int wait;
	int retry;
	int timeout;
	String contentReportFilePath = null;
	String activityReportFilePath = null;
	String activityLogWebservicePath = null;
	String activityLogCallBackPath = null;
	boolean contentdone = false;
	boolean activitydone = false;
	private static final String HOST_KEY_CHECKING = "StrictHostKeyChecking";
	
	protected DailyActivityReport (RBTDaemonManager mainDaemonThread)
	{
		try
		{
			logger.debug("Calling Daily Activity report");
			setName("DailyActivityReport");
			m_mainDaemonThread = mainDaemonThread;
			init();
		}
		catch(Exception e)
		{
			logger.error("Issue in creating DailyActivityReport", e);
		}
	}

	public void init()
	{
		logger.debug("Init() the daily activity report");
		rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();
		ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
		String m_dbURL = resourceBundle.getString("DB_URL");
		// Changes done for URL Encryption and Decryption
		try {
			if (resourceBundle.getString("ENCRYPTION_MODEL") != null
					&& resourceBundle.getString("ENCRYPTION_MODEL")
							.equalsIgnoreCase("yes")) {
				m_dbURL = URLEncryptDecryptUtil.decryptAndMerge(m_dbURL);
			}
		} catch (MissingResourceException e) {
			logger.error("resource bundle exception: ENCRYPTION_MODEL");
		}
		// End of URL Encryption and Decryption
		String poolSizeStr = resourceBundle.getString("DB_POOL_SIZE");
		activityMachine = getParam("COMMON","ACTIVITY_MACHINE_NAME",null);
		contentMachine = getParam("COMMON","CONTENT_MACHINE_NAME",null);
		server = getParam("COMMON","FTP_SERVER_IP",null);
		port = Integer.parseInt( getParam("COMMON","FTP_PORT","0"));
		user = getParam("COMMON","FTP_USERNAME",null);
		pwd = getParam("COMMON","FTP_PASSWORD",null);
		dir = getParam("COMMON","FTP_DIRECTORY",null);
		wait = Integer.parseInt(getParam("COMMON","FTP_WAIT_TIME",null));
		retry = Integer.parseInt(getParam("COMMON","FTP_RETRY_TIME",null));
		timeout = Integer.parseInt(getParam("COMMON","FTP_TIMEOUT",null));
		contentReportFilePath = getParam("COMMON","CONTENT_REPORT_FILE_PATH",null);
		activityReportFilePath = getParam("COMMON","ACTIVITY_REPORT_FILE_PATH",null);
		activityLogWebservicePath = getParam("COMMON","ACTIVITY_LOG_WEBSERVICE_FILE_PATH",null);
		activityLogCallBackPath = getParam("COMMON","ACTIVITY_LOG_CALL_BACK_FILE_PATH",null);
		logger.info("db_url is : " + m_dbURL+ " activity machine is : " + activityMachine + " content machine is : " + contentMachine + " server : " + server + " port : " + port + " user : " + user +
				" password : " + pwd + " directory : " + dir + " wait/retry/timeout :" + wait + retry + timeout + "contentReportFilePath:" +
				contentReportFilePath + " activityReportFilePath " + activityReportFilePath + "activityLogWebservicePath : "+ activityLogWebservicePath
				+ "activityLogCallBackPath : "+ activityLogCallBackPath);
		int poolSize = 4;
		if (poolSizeStr != null)
		{
			try
			{
				poolSize = Integer.parseInt(poolSizeStr);
			}
			catch (Exception e)
			{
				poolSize = 4;
			}
		}
		rbtDBManager = RBTDBManager.getInstance();
	}

	public String getParam(String type, String param, String defaultVal)
	{
		try{
			logger.debug("Getting param : " + type + " , " + param );
			String paramVal = rbtParamCacheManager.getParameter(type, param, defaultVal+"").getValue();
			return paramVal ;   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param );
			return defaultVal;
		}
	}

	public boolean isProcessingAllowedNow(String path,String operation) {
		
		logger.debug("Checking if processing is allowed now");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat ascFileSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		try {
			File localfile = new File(path); 
			logger.info("Getting all teh .done files in the location "+localfile.getAbsolutePath());
			boolean fileProcessed = false;
			File[] files = localfile.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if(name.toLowerCase().endsWith(".done")) {
						return true;
					}

					return false;
				}	});
			logger.debug("got all .done files , file length is "+files.length);

			for(int i=0;i<files.length;i++)
			{
				logger.info("Processing the done file "+ files[i].getAbsolutePath());
				Date fileDate;

				String filesplit[] = files[i].getName().split("_");
				String splitDate = filesplit[filesplit.length-1];
				String filedate = splitDate.substring(0,splitDate.length()-5);
				fileDate = ascFileSdf.parse(filedate);
				logger.info("Checking done file "+ files[i].getName());
				if(sdf.format(fileDate).equals(sdf.format(new Date())))
				{
					logger.info("Files amtch ");
					fileProcessed = true;
					logger.info(operation + " processing done for the day");
					if(operation.equalsIgnoreCase("content"))
					{
						contentdone = true;
					}
					else
					{
						activitydone = true;
					}
					return true;

				}


			}
			logger.info("Returning "+fileProcessed+" activity done = "+ activitydone + " content done = "+ contentdone);
			return fileProcessed;
		} catch (Exception e) {
			logger.error("Error in is processing allowed now",e);
			return false;
		}
	}

	public void createContentFile() 
	{
		FileWriter fw = null;
		BufferedWriter bw = null;
		try{
		logger.info("Creating content file");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS0");
		File file = new File(contentReportFilePath + File.separator + contentMachine + "_RBT_0003_"+sdf.format(new Date()) +".asc");
		logger.info("creating file "+file.getAbsolutePath());
		fw = new FileWriter(file);
		bw = new BufferedWriter(fw);
		String header = "Id.ER|NameMel|Artist|CatPadre|Cat|Rate|Interfaces|Master|SndProvId|Segment";
		bw.write(header);
		bw.newLine();
		bw.flush();
		List<Clip> cliplist = new ArrayList<Clip>(); 
		int startlist = 1;
		logger.info("Getting all clips from DB");
		cliplist = ClipsDAO.getAllClipsA(1,1000);
		logger.info("Cliplist size is "+cliplist.size());
		while(cliplist.size()>0)
		{
			for(int i=0;i<cliplist.size();i++)
			{ 	

				Clip clip = (Clip)cliplist.get(i);
				if(clip!=null)
				{
					int clipId = clip.getClipId();
					logger.debug(clipId);
					int catId = rbtDBManager.getCatIDsForClipId(clipId);
					logger.debug(catId);
					Integer parentCatId = rbtDBManager.getParentCategoryIdfcategoryCatId(catId);
					logger.debug(parentCatId);
					String s = clip.getClipId()+"|"+clip.getClipName()+"|"+clip.getArtist()+"|"+parentCatId+"|"+catId+"|1.5|ALL|NA|NA|NA";
					logger.info(s);
					bw.write(s);
					bw.newLine();
					bw.flush();
				}
			}
			startlist = startlist + 1000;
			cliplist = ClipsDAO.getAllClipsA(startlist ,1000);
		}
		logger.info("Done processing the content log ");
		}catch(Exception e){
			logger.error("Error", e);
		}finally{
			
			try {
				fw.close();
				bw.close();
			} catch (Exception e) {
				logger.error("Error while closing writer", e);
			}
			
		}
	}

	public String processActivityLog(String sourcePath) throws IOException {

		logger.info("Processing activity log " + sourcePath);
		File file1 = new File(sourcePath);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		File[] files = file1.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if(name.toLowerCase().contains(".log.")) {
					return true;
				}

				return false;
			}	});
		
		logger.info("Getting .log. files " + files.length);
		
		for(int i=0;i<files.length;i++)
		{
			String temp[] = files[i].getName().split("\\.");
			String date = temp [temp.length -1];
			logger.debug("file date is "+ date);
			try {

				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.HOUR_OF_DAY,Calendar.HOUR_OF_DAY-24);
				Date calDate = cal.getTime();
				String calendarDate = sdf.format(calDate);
				cal.set(Calendar.HOUR_OF_DAY,Calendar.HOUR_OF_DAY-120);
				String deleteDate = sdf.format(cal.getTime());
				logger.debug("Looking for files with date to delete : "+deleteDate);
				if(sdf.parse(date).before(sdf.parse(deleteDate)))
				{	
					logger.warn("Deleting file "+ files[i].getAbsolutePath());
					files[i].delete();
				}
							
				logger.debug("Looking for files with date to match : "+ calendarDate + " with "+date +" comparison"+ date.trim().equals(calendarDate.trim()) );

				if((sdf.parse(date.trim())).equals(sdf.parse(calendarDate.trim())))
				{
					logger.info(" Found match Returning" + files[i].getAbsolutePath());
					return files[i].getAbsolutePath();
				}
				
					
			} catch (ParseException e) {
				logger.error("",e);
			}catch (ArrayIndexOutOfBoundsException e) {
				logger.error("",e);
			}
		}
		logger.info("Dint find a match returning null");
		return null;

	}
	
	private static void copyfile(String srFile, String dtFile){
	    try{
	      File f1 = new File(srFile);
	      File f2 = new File(dtFile);
	      InputStream in = new FileInputStream(f1);
	      
	      //For Append the file.
//	      OutputStream out = new FileOutputStream(f2,true);

	      //For Overwrite the file.
	      OutputStream out = new FileOutputStream(f2,true);

	      byte[] buf = new byte[1024];
	      int len;
	      while ((len = in.read(buf)) > 0){
	        out.write(buf, 0, len);
	      }
	      in.close();
	      out.close();
	      logger.info("copied "+srFile + " to "+dtFile);
	    }
	    catch(Exception ex){
	     logger.error(ex.getMessage() + " in the specified directory.");
	    }
	  }
	
	public void processReport(String sourcePath,String type) throws IOException {
		
		logger.info("Processing the "+ type + " report " + sourcePath);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat ascFileSdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");


		if(!isProcessingAllowedNow(sourcePath,type))
		{
			logger.info("Processing is allowed now ");
			File localfile = new File(sourcePath); 
			logger.info("Getting all teh .asc files in the location "+localfile.getAbsolutePath());
			File[] files = localfile.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if(name.toLowerCase().endsWith(".asc")) {
						return true;
					}

					return false;
				}	});
		
			boolean ascFileCreated = false;
			
			for(int i=0;i<files.length;i++)
			{
				logger.info("asc file is already created");
				String fileDate[] = files[i].getName().split("_");
				String date = fileDate[fileDate.length-1].substring(0,fileDate[fileDate.length-1].length()-4);
				Date ascfileDate;
				try {
					ascfileDate = ascFileSdf.parse(date);
					String ascParsedDate = sdf.format(ascfileDate);
					Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY,Calendar.HOUR_OF_DAY-120);
					Date calDate = cal.getTime();
					if(sdf.parse(ascParsedDate).before(calDate))
					{
						files[i].delete();
						logger.warn("Deleting file " + files[i].getAbsolutePath());
					}
					if(ascParsedDate.equalsIgnoreCase(sdf.format(new Date())))
					{
						ascFileCreated = true;
						logger.info("The asc file has been created today ");

					}
				} catch (ParseException e) {
					logger.error("Error while parsing date"+e.getStackTrace());
				}

			}	

			if(!ascFileCreated)
			{
				logger.info(".asc file is not created today hence creating it again ");
				try{
					
					if(type.equalsIgnoreCase("content"))
					createContentFile();
					else
					createActivityFile();	

				}catch(Exception ioe){
					logger.error("Error while creating the asc file type "+type ,ioe );
					return;
				}
			}

			File[] ascfiles = localfile.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if(name.toLowerCase().endsWith(".asc")) {
						return true;
					}

					return false;
				}	});
			
			for(int i=0;i<ascfiles.length;i++)
			{
				String ascFilePath = ascfiles[i].getAbsolutePath();
				logger.info("Creating dummyFile " +ascFilePath.substring(0,ascFilePath.length()-3)+"0");
				File dummyFile = new File(ascFilePath.substring(0,ascFilePath.length()-3)+"0");
				try {
					dummyFile.createNewFile();
				} catch (IOException e) {
					logger.error(e);
				}
				logger.info("Created dummy File");
				String filePath = ascFilePath;
				String dummyFilePath = dummyFile.getAbsolutePath();
				boolean useJSCHlogic = RBTParametersUtils.getParamAsBoolean("DAEMON", "ENABLE_COPY_TO_SERVER_USING_JSCH", "FALSE");
				if(useJSCHlogic){
					filePath = ascfiles[i].getName();
					dummyFilePath = dummyFile.getName();
				}
				if(connectToSFTPServerToUpload(filePath,dummyFilePath))
				{
					copyFile(ascfiles[i], new File(ascFilePath.substring(0, ascFilePath.length() - 3) + "done"));
					ascfiles[i].delete();
					dummyFile.delete();
				}
			}
		}
	}



	public boolean connectToSFTPServerToUpload(String filepath, String dummyFile) {
		logger.info("entering...Connecting to the SFTP server..");

		boolean useJSCHlogic = RBTParametersUtils.getParamAsBoolean("DAEMON", "ENABLE_COPY_TO_SERVER_USING_JSCH", "FALSE");
		if (useJSCHlogic) {
			
			ChannelSftp sftpChannel = null;
			Session session = null;
			try {
				session = openSftpSession(server, user, pwd, port, "no");
				Channel channel = session.openChannel("sftp");
				if(channel == null){
					logger.info("Could not connect to the SFTP server");
					return false;
				}
				String sftpLocation = dir;
				channel.connect();
				sftpChannel = (ChannelSftp) channel;
				sftpChannel.cd(sftpLocation);
				logger.info("Entered into dest dir" + sftpLocation
						+ " in SFTP & writing the file");
				sftpChannel.put(filepath);
				sftpChannel.put(dummyFile);
				logger.info("File has been written using jsch");
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					sftpChannel.disconnect();
					session.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {

			SftpClient sftp = null;
			SshClient ssh = null;
			// FileWriter logWriter=null;
			try {
				String hostName = server;
				String sftpUser = user;
				String sftpPassword = pwd;
				String sftpLocation = dir;
				String socketTimeOut = "5000";

				ConfigurationLoader.initialize(false);
				ssh = new SshClient();
				// Connect to the host
				SshConnectionProperties prop = new SshConnectionProperties();
				prop.setHost(hostName);
				prop.setPort(port);
				prop.setUsername(sftpUser);

				ssh.setSocketTimeout(Integer.parseInt(socketTimeOut));
				logger.info("Connecting to the SFTP server..");
				ssh.connect(prop, new KeyExchange());
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
					logger.info(" password Authenticated:)");
					// The connection is authenticated we can now do some real
					// work!
					sftp = ssh.openSftpClient();
					logger.info("Opened SFTPClient");
					try {
						logger.info("Going to dest dir " + sftpLocation + " in SFTP");
						sftp.cd(sftpLocation);
						logger.info("Entered into dest dir" + sftpLocation
								+ " in SFTP & writing the file");
						sftp.put(filepath);
						sftp.put(dummyFile);
						logger.info("File has been written");

					} catch (IOException ioe) {

						logger.info("Either the folder does not exist or its not a directory Could not get into the remote folder due to "
								+ ioe.getMessage() + "\n");
						return false;
					}
					logger.info("Existing sftp dir==" + sftp.pwd().toString());

				}
			} catch (ConfigurationException ce) {
				logger.error(ce.getStackTrace());
				return false;
			} catch (IOException ioe) {
				logger.info(ioe.getMessage());
				return false;
			} catch (Exception ioe) {
				logger.error(ioe.getStackTrace());
				return false;
			} finally {
				ssh.disconnect();
				if (sftp != null) {
					if (!sftp.isClosed()) {
						logger.info("SSH not closed. Trying again..");
						ssh.disconnect();
					}
				} else {
					logger.info("sftp is not initialized yet...");
				}
			}
		}
		return true;
	}


	private static void copyFile(File source, File destination) throws IOException {
		FileChannel sourceFileChannel = null;
		FileChannel destinationFileChannel = null;
		try {
			sourceFileChannel = (new FileInputStream(source)).getChannel();
			destinationFileChannel = (new FileOutputStream(destination)).getChannel();
			sourceFileChannel.transferTo(0, source.length(), destinationFileChannel);
		} finally {
			try {
				if (sourceFileChannel != null)
					sourceFileChannel.close();
				if (destinationFileChannel != null)
					destinationFileChannel.close();
			} catch (IOException e) {
				//Ignore
			}
		}
	}

	public void createActivityFile() throws IOException
	{
		String activitylog1 = processActivityLog(activityLogWebservicePath);
		String activitylog2 = processActivityLog(activityLogCallBackPath);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS0");
		File file = new File(activityReportFilePath + File.separator + activityMachine + "_RBT_0002_"+sdf.format(new Date()) +".asc");
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		String header = "MSISDN|TimeStamp|Action|ID_melody|Access_Channel|RateType|Customization|Criteria|Result";
		bw.write(header);
		bw.newLine();
		bw.flush();
		if(activitylog1!=null)
		copyfile(activitylog1, file.getAbsolutePath()); 
		if(activitylog2!=null)
		copyfile(activitylog2, file.getAbsolutePath());
		
	}

	public void run()
	{

		while(m_mainDaemonThread != null && m_mainDaemonThread.isAlive()) 
		{
			try{
				
				processReport(contentReportFilePath,"content");
				processReport(activityReportFilePath,"activity");
				if(activitydone&&contentdone)
				{
					try {
						Thread.sleep(Integer.parseInt(getParam("DAEMON", "SLEEP_INTERVAL_MINUTES_ACTIVITY_REPORT", "10")) * 60 * 1000);
					} catch (NumberFormatException e) {
						
					} catch (InterruptedException e) {
						
					}
				}

			}catch(IOException ioe){
				logger.error(ioe.getStackTrace());
				try {
					Thread.sleep(90000);
				} catch (InterruptedException e) {

				}
				continue;
			}
		}
	}
	
	public static Session openSftpSession(String host, String username,String password, int port, String hostKey) {
	    // Connect and logon to SFTP Server
	    JSch jsch = new JSch();
	    Session session = null;
	    try {
	        session = jsch.getSession(username, host, port);
	        session.setPassword(password);
	        session.setConfig(HOST_KEY_CHECKING, hostKey);
	        session.connect();
	        // check if connect was successful
	        if (session.isConnected()) {
	            logger.info("Connected sucessfully to server :" + host);
	        } else {
	            logger.info("Connection Failed" + host);
	        }
	    } catch (JSchException e) {
	    	logger.info("Connection Failed" + host + " Error:" + e.getMessage());
	        e.printStackTrace();
	    } catch (Exception e) {
	    	logger.info("Connection Failed" + host + " Error:" + e.getMessage());
	        e.printStackTrace();
	    }
	    return session;
	}
	
}
