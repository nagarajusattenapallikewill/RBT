package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.httpclient.HttpException;
import org.apache.commons.vfs.FileName;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.provider.sftp.SftpFileSystemConfigBuilder;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;

public class WavFileDispatcher extends Thread implements iRBTConstant {
	private static Logger logger = Logger.getLogger(WavFileDispatcher.class);

	//private final String conversionStr = "vox2wav.exe %FILE% 8";
	private static String OperatingSystemType="Windows";
	private final String conversionStr ="sox -r 8000 -U -1 %FILE% %FILE1%";
	private final String conversionStrWav = "wavconvert %FILE% %FILE1% mulaw  riff";
	private String m_countryPrefix = null;
	private boolean m_useVoxToWavConvertor=false;
	private boolean m_useSFTP=false;
	private String[] m_circleIDToUploadCricketFiles=null;
	private String m_destVoxFolder=null;// used when m_useSFTP=false....
	private String m_sftArgument=null;
	private static boolean m_startCricketFileUploader = true;
	private static boolean m_startNnBFeedFileUploader = false;
	//psftp -b::-bc -v -be -l Administrator -pw password ip

	private static final String LOCAL_DIR = "LOCAL_DIR";
	private static final String LOCAL_FILE_COUNT = "LOCAL_FILE_COUNT";
	private static final String VOICE_PORTAL_PATH = "VOICE_PORTAL_PATH";
	private static final String SLEEP_MIN = "SLEEP_MIN";
	private static final String FEED_OFF_MIN = "FEED_OFF_MIN";
	private static final String OUT_FILE_EXT = ".wav";

	private String user = null;
	private String pass = null;
	private String host = null;
	private String sftpDir = null;
	private String port = "22";
	
	private String ftpDir = null;
	private String localDir = null;
	private int localFileCount = 5;
	private String voicePortalpath = null;
	private String feedURL = null;
	private int sleepMins = 5;
	private int feedOffMin = 30;

	long lastFeedDownload = System.currentTimeMillis();
	Calendar calendar = Calendar.getInstance();
	
	private static WavFileDispatcher wavFileDispatcher = null;
	private static NewsAndBeautyfeedUploader newsAndBeautyfeedUploader = null;
	private static boolean isLive = true;

	public static void main(String[] args) {
		
		Tools.init("CricketDaemon", true);
		
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","START_CRICKET_FILE_UPLOADER");
		if(param != null && param.getValue() != null)
		{
			m_startCricketFileUploader = param.getValue().trim().equalsIgnoreCase("TRUE");
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","START_NnB_FEED_FILE_UPLOADER");
		if(param != null && param.getValue() != null)
		{
			m_startNnBFeedFileUploader = param.getValue().trim().equalsIgnoreCase("TRUE");
		}
		
		if(m_startCricketFileUploader)
		{
			logger.info("i m starting cricket daemon");
			WavFileDispatcher instance = new WavFileDispatcher(CRICKET);
			instance.start();
		}
		if(m_startNnBFeedFileUploader)
		{
			logger.info("i m starting NnB daemon");
			NewsAndBeautyfeedUploader instance = new NewsAndBeautyfeedUploader();
			instance.start();
		}
	}
	
	public static void startThreads()
	{
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","START_CRICKET_FILE_UPLOADER");
		if(param != null && param.getValue() != null)
		{
			m_startCricketFileUploader = param.getValue().trim().equalsIgnoreCase("TRUE");
		}
		
		param = CacheManagerUtil.getParametersCacheManager().getParameter("DAEMON","START_NnB_FEED_FILE_UPLOADER");
		if(param != null && param.getValue() != null)
		{
			m_startNnBFeedFileUploader = param.getValue().trim().equalsIgnoreCase("TRUE");
		}
		
		isLive = true;
		if(m_startCricketFileUploader)
		{
			logger.info("i m starting cricket daemon");
			wavFileDispatcher = new WavFileDispatcher(CRICKET);
			wavFileDispatcher.start();
		}
		if(m_startNnBFeedFileUploader)
		{
			logger.info("i m starting NnB daemon");
			newsAndBeautyfeedUploader = new NewsAndBeautyfeedUploader();
			newsAndBeautyfeedUploader.start();
		}
	}
	
	public static void stopThreads()
	{
		isLive = false;
		if(newsAndBeautyfeedUploader != null)
			newsAndBeautyfeedUploader.stopThread();
	}

	public WavFileDispatcher(String type) {
		init(type);
	}

	public void init(String appName) {
		OperatingSystemType=getOperatingSystemType();
		logger.info("OperatingSystemType=="+OperatingSystemType);
		m_useSFTP = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SFTP", "FALSE");
		m_useVoxToWavConvertor = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_VOX_TO_WAV_CONVERTOR", "FALSE");
		
		String circleIDs = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CIRCLE_ID_TO_UPLOAD_FILES", "");
		m_circleIDToUploadCricketFiles = circleIDs.split(",");
		m_destVoxFolder = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DEST_VOX_FOLDER", null);
		
		m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
		m_sftArgument = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SFTP_ARGUMENT", null);
		if(m_sftArgument!=null && m_sftArgument.length()>0){
		StringTokenizer st1=new StringTokenizer(m_sftArgument,";");
		String temp=null;int i=0;
		user = null;
		pass = null;
		host = null;
		while(st1.hasMoreElements()){
			temp=st1.nextToken();
			if(i==0){
				user=temp;
				logger.info("user=="+user);
			}else if(i==1){
				pass=temp;
				logger.info("pass=="+pass);
			}else{
				host=temp;
				logger.info("host=="+host);
			}
			i++;
		}
		}
		logger.info("m_circleIDToUploadCricketFiles=="+m_circleIDToUploadCricketFiles);
		if(m_circleIDToUploadCricketFiles!=null && m_circleIDToUploadCricketFiles.length>0){
			logger.info("going inside feed URL==");
			feedURL=getFeedUrl(m_circleIDToUploadCricketFiles);
		}
		HashMap parameters = getParams();
		if (parameters == null) {
			logger.info("RBT::no parameters available");
			return;
		}
		else {
			logger.info("RBT:: parameters available");
			localDir = (String) parameters.get(LOCAL_DIR);
			if(parameters.containsKey(LOCAL_FILE_COUNT))
				localFileCount = Integer.parseInt((String) parameters.get(LOCAL_FILE_COUNT));
			voicePortalpath = (String) parameters.get(VOICE_PORTAL_PATH);
			if(parameters.containsKey(SLEEP_MIN))
				sleepMins = Integer.parseInt((String) parameters.get(SLEEP_MIN));
			if(parameters.containsKey(FEED_OFF_MIN))
				feedOffMin = Integer.parseInt((String) parameters.get(FEED_OFF_MIN));
		}
	}

	public void run() {
		while (isLive) {
			// download vox file and process it
			downloadAndProcessVoxFile();
			// turn feed off
			boolean status=turnFeedOff();
			String reply="failed";
			if(status){
				reply="successful";
			}
			logger.info("RBT::feedoff -> " + reply);
			// sleep
			sleep();
		}
	}

	/*
	 * downloads the vox file from ftp and conerts it to wav file and makes hit
	 * to feed jsp's
	 */
	private void downloadAndProcessVoxFile() {
//		String destFile = new FtpDownloader().ftpConnect(ftpIP, port, userName, password, localDir,
//		ftpDir); // "test3.vox";
		String destFile =null;
		if(m_useSFTP){
			sftpDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SFTP_DIR", null);
			port = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SFTP_PORT", null);
			if (sftpDir != null && port != null && user != null && port != null
					&& host != null && localDir != null) {
				SFTPDownloader instance = new SFTPDownloader(true, sftpDir,
						m_useVoxToWavConvertor, user, pass, host, port,
						localDir);
				destFile = instance.start();
			} 
		}else{
			destFile=downloadVoxFiles();
		}
		if (destFile != null) {
			System.out.println("RBT::file downloaded -> " + destFile);
			logger.info("RBT::file downloaded -> " + destFile);
			boolean check = true; 
            if(m_useVoxToWavConvertor){ 
            	check=convertVox2Wav(destFile); 
            } 
			logger.info("RBT::check -> " + check);
			System.out.println("RBT::check -> " + check);
			if (check == true) {
				try {
//					boolean check1 = convertWav2Wav(destFile);
//					if (check1) {


					//wavconvert E:/test.wav E:/hello.wav mulaw  riff
					lastFeedDownload = System.currentTimeMillis();
					System.out.println("calling deleteFilesFromLocal");
					deleteFilesFromLocal();
					String strDestFileFinal= destFile.substring(0, destFile.lastIndexOf("."))+"_temp"+ OUT_FILE_EXT;
					File destFileFinal=new File(localDir+File.separator+strDestFileFinal);
					if(destFileFinal.exists()){
						destFileFinal.renameTo(new File(localDir+File.separator+destFile.substring(0, destFile.lastIndexOf(".")) + OUT_FILE_EXT));
					}
					String wavFileName = destFile.substring(0, destFile
							.indexOf("."));
					wavFileName = wavFileName + OUT_FILE_EXT;
					System.out.println("going to upload to sites");
					makeFeedFileRequest(localDir + File.separator
							+ wavFileName);
					if (voicePortalpath != null) {
						StringTokenizer stk = new StringTokenizer(
								voicePortalpath, ",");
						while (stk.hasMoreTokens()) {
							try {
								copyFile(stk.nextToken(), wavFileName
										.toString(), localDir);
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						} // end of while all voice portal paths
					}
//					}else{
//					logger.info("RBT:: no vox file to process");
//					}
				}
				catch (Exception e) {
					logger.error("", e);
				}
			}
			else {
				logger.info("VoxToWav conversion of " + destFile
						+ " was interrupted.");
			}
		}
		else
			logger.info("RBT:: no vox file to process");
	}
	private String downloadVoxFiles(){
		String destFile=null;
		int iCount = 0, lastCount = 0;
		//int index=0;
		long check1=0;
		long check2=0;
		File chgFilePath=null;
		ArrayList voxFileList=new ArrayList();
		File file=new File(m_destVoxFolder);
		File tempFile=null;
		File tempFile1=null;
		if(file.exists()&& file.isDirectory()){
			String[] fileList = file.list();
			if (m_useVoxToWavConvertor) {
				for (int count = 0; count < fileList.length; count++) {
					if (fileList[count].indexOf(".vox") >= 0
							|| fileList[count].indexOf(".VOX") >= 0) {
						voxFileList.add(fileList[count]);
					}
				}
			} else {
				for (int count = 0; count < fileList.length; count++) {
					if (fileList[count].indexOf(".wav") >= 0
							|| fileList[count].indexOf(".WAV") >= 0) {
						voxFileList.add(fileList[count]);
					}
				}
			} 			
			for (int i = 0; voxFileList != null && i < voxFileList.size(); i++) {
				logger.info("this is file =="+(String)voxFileList.get(i));
				//System.out.println("this is file =="+(String)voxFileList.get(i));
				//if (((String)voxFileList.get(i)).substring(((String)voxFileList.get(i)).indexOf(".") + 1).equalsIgnoreCase("vox")) {
					tempFile=new File(m_destVoxFolder+ File.separator +(String)voxFileList.get(i));
					check2 = tempFile.lastModified();
					logger.info("check1=="+check1);
					logger.info("check2=="+check2);
					//System.out.println("check1=="+check1);
					//System.out.println("check2=="+check2);
					if (i == 0) {
						check1 = check2;
						destFile = (String)voxFileList.get(i);
					}
					if (check2>(check1)) {
						//System.out.println("check2>check2");
						logger.info("check2>"+check2);
						logger.info("file no ::"+i+" is newer file and file name is=="+(String)voxFileList.get(i));
						 //System.out.println("file no ::"+i+" is newer file and file name is=="+(String)voxFileList.get(i));
						check1 = check2;
						destFile = (String)voxFileList.get(i);
						lastCount = iCount;
						iCount = i;
						logger.info("icount=="+iCount+"  lastCount=="+lastCount);
						//System.out.println("icount=="+iCount+"  lastCount=="+lastCount);
						for (int j = lastCount; j < iCount; j++) {
							logger.info("deleting file between last new n new new file");
							//System.out.println("deleting file between last new n new new file");
							tempFile1=new File(m_destVoxFolder+ File.separator +(String)voxFileList.get(j));
							System.out.println("deletig file=="+tempFile1.getAbsolutePath());
							tempFile1.delete();
						}

					}
					else {
						if (i == (voxFileList.size()- 1)) {
							lastCount = iCount + 1;
							iCount = voxFileList.size();
							for (int j = lastCount; j < iCount; j++) {
								tempFile1=new File(m_destVoxFolder+ File.separator +(String)voxFileList.get(j));
								logger.info("deleting file****=="+tempFile1.getAbsolutePath());
								//System.out.println("deleting file****=="+tempFile1.getAbsolutePath());
								tempFile1.delete();
								logger.info("deleting file==" + (String)voxFileList.get(j));
							}
						}
					} // end of modified time check else
				//} // end of if vox file
			} // end of for loop
			
		}
		if(destFile!=null){
			logger.info("dest file is not null" );
			if(new File(localDir + File.separator + destFile).exists()){ 
                new File(localDir + File.separator + destFile).delete(); 
			} 
			chgFilePath=new File(m_destVoxFolder+ File.separator +destFile);
			chgFilePath.renameTo(new File(localDir + File.separator + destFile));
		}
		logger.info("returning file****=="+destFile);
		return destFile;
	}
	private String downloadSFTP(){
		try {
			copyRemoteFile(user,pass,host);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		long tempFileName=0;
		String tempFileNameStr=null;
		File localDir=new File(this.localDir);
		if(localDir.exists()  && localDir.isDirectory()){
			String[] filesOnLocalSystem=localDir.list();
			boolean temp=false;
			for(int j=0;j<filesOnLocalSystem.length;j++){
				if(filesOnLocalSystem[j].indexOf(".vox")>0 || filesOnLocalSystem[j].indexOf(".VOX")>0 ){
					temp=true;
					tempFileNameStr=filesOnLocalSystem[j].substring(0, filesOnLocalSystem[j].lastIndexOf(".")-1);

					try {
						Long tempFileNameInLong=new Long(tempFileNameStr);
						long checkFileLong=tempFileNameInLong.longValue();
						if(checkFileLong>=tempFileName){
							tempFileName=checkFileLong;
						}

					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						continue;
					}

				}
			}
			if(temp){
				String fileExtension="wav"; 
                if(m_useVoxToWavConvertor){ 
                        fileExtension="vox"; 
                } 
				try {
					if(tempFileName==0){
						deleteFromSFTP(tempFileNameStr+"."+fileExtension,user,pass,host);
						return tempFileNameStr;
					}else{
						deleteFromSFTP(new Long(tempFileName).toString()+"."+fileExtension,user,pass,host);
						return tempFileNameStr;
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				return null;
			}
		}else{
			return null;
		}

		return null;
	}
	private void deleteFromSFTP(String fileNotBeDeletedFromSFTP,String user,String password,String host)throws IOException {
		FileSystemOptions fsOptions = new FileSystemOptions();
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
				fsOptions, "no");
		DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS
		.getManager();
		String uri = "sftp://" + user + ":" + password + "@" + host
		+ "/" + ftpDir;
		FileObject fo = fsManager.resolveFile(uri, fsOptions);
		System.out.println("Name "+fo.exists());

		FileObject[] destFileList=fo.getChildren();
		FileObject temp=null;
		FileObject temp1=null;
		String fileName=null;
		FileName filename=null;
		String ext=null;
		String filePath=null;
		String fileExtension="wav"; 
        if(m_useVoxToWavConvertor){ 
                fileExtension="vox"; 
        } 
		for(int i=0;i<destFileList.length;i++){
			temp1=destFileList[i];
			filename=temp1.getName();

			ext=filename.getExtension();

			if(ext.equalsIgnoreCase(fileExtension)){
				filePath=filename.getPath();  
				filePath=filePath.substring(filePath.lastIndexOf(File.separator)+1);
				if(!filePath.equalsIgnoreCase(fileNotBeDeletedFromSFTP)){
					destFileList[i].delete();
				}
				System.out.println("Finished copying the file");
			}

		}

	}
	private ArrayList createBatchFileAndDownload(String fileNotBeDeletedFromSFTP){
		String conversionStr=null;
		String destFile=null;
		String batFilePath=null;
		ArrayList voxFileList=null;
		if(fileNotBeDeletedFromSFTP.equalsIgnoreCase("timepass")){
			destFile="cricket";
			conversionStr="cd "+this.ftpDir+System.getProperty("line.separator")+" dir"+ System.getProperty("line.separator")+"quit";
//			String tempSFT=null;
//			Process process = Runtime.getRuntime().exec(tempSFT, null, new File(localDir));
//			InputStream in = process.getInputStream();
//			InputStreamReader ipSt=new InputStreamReader(in);
//			BufferedReader buff=new BufferedReader(ipSt);
//			String temp=null;
//			StringTokenizer st=null;
//			voxFileList=new ArrayList();
//			while((temp=buff.readLine())!=null){

//			if(temp.indexOf(".vox")>0 || (temp.indexOf(".VOX")>0)){
//			st=new StringTokenizer(temp," ");
//			String lastToken=null;
//			while(st.hasMoreElements()){
//			lastToken=st.nextToken();
//			}
//			voxFileList.add(lastToken);
//			}
//			}


		}else{
			destFile="deleteFromSFTP";
			File localDir=new File(this.localDir);
			if(localDir.exists()  && localDir.isDirectory()){
				String[] filesOnLocalSystem=localDir.list();
				boolean temp=false;
				conversionStr="cd "+this.ftpDir;
				for(int j=0;j<filesOnLocalSystem.length;j++){
					if(filesOnLocalSystem[j].indexOf(".vox")>0 ||filesOnLocalSystem[j].indexOf(".VOX")>0 ){
						String tempFile=filesOnLocalSystem[j];
						if(!(tempFile.equalsIgnoreCase(fileNotBeDeletedFromSFTP))){

							conversionStr=conversionStr+System.getProperty("line.separator")+"del "+tempFile;

							temp=true;
						}
					}
				}
				conversionStr=conversionStr+System.getProperty("line.separator")+"quit";
			}
		}
		batFilePath=createBatchFileForConversion(destFile, conversionStr);
		StringTokenizer st=new StringTokenizer(m_sftArgument,"::");
		String tempSFT=null;
		int i=0;
		while(st.hasMoreElements()){
			if(i==0){
				tempSFT=st.nextToken()+" "+batFilePath+" ";
			}else{
				tempSFT=tempSFT+st.nextToken();
			}
			i++;
		}
		logger.info("RBT:: batFilePath -> " + batFilePath);
		File batFile = new File(batFilePath);
		if (batFile.exists()) {
			logger.info("RBT:: batch file exists");
			try {
				char y = 'y';
				/*
				 * Properties envMap = System.getProperties(); Set temp =
				 * envMap.keySet(); Iterator itr = temp.iterator(); ArrayList
				 * arrEnv = new ArrayList(); String check2 = null; String check3 =
				 * null; while (itr.hasNext()) { StringBuffer strBuff1 = new
				 * StringBuffer(); check2 = (String) itr.next();
				 * strBuff1.append(check2); strBuff1.append("=");
				 * strBuff1.append(envMap.get(check2)); check3 =
				 * strBuff1.toString(); strBuff1 = null;
				 * System.out.println("Adding from sys properties " + check3);
				 * arrEnv.add(check3); } String[] strEnv = (String[])
				 * arrEnv.toArray(new String[0]);
				 */
				Process process = Runtime.getRuntime().exec(tempSFT, null, new File(localDir));
				// If the file already exist, process asks the question: y/n
				// below is the way to enter the key-stroke.
				process.getOutputStream().write((int) y);

				InputStream is = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = br.readLine();
//				while (line != null) {
//				System.out.println("!!!!");
//				System.out.println(line);
//				line = br.readLine();
//				}
				if(fileNotBeDeletedFromSFTP.equalsIgnoreCase("timepass")){
					destFile="cricket";
					conversionStr="cd "+this.ftpDir+System.getProperty("line.separator")+" dir"+System.getProperty("line.separator")+"quit";
					//String tempSFT=null;
					//Process process = Runtime.getRuntime().exec(tempSFT, null, new File(localDir));
//					InputStream in = process.getInputStream();
//					InputStreamReader ipSt=new InputStreamReader(in);
//					BufferedReader buff=new BufferedReader(ipSt);
					String temp=null;
					StringTokenizer st1=null;
					voxFileList=new ArrayList();
					while((temp=br.readLine())!=null){

						if(temp.indexOf(".vox")>0 || (temp.indexOf(".VOX")>0)){
							st1=new StringTokenizer(temp," ");
							String lastToken=null;
							while(st1.hasMoreElements()){
								lastToken=st1.nextToken();
							}
							voxFileList.add(lastToken);
						}
					}


				}
				process.waitFor();
				System.out.println("here...........");
			}
			catch (java.lang.InterruptedException io) {
				logger.error("", io);
				return voxFileList;
			}
			catch (java.io.IOException ioe) {
				logger.error("", ioe);
				return voxFileList;
			}
			finally {
				if (batFile.exists())
					batFile.delete();
			}
		}
		return voxFileList;
	}
	private void copyFile(String destinationPath, String fileName, String localDir)
	throws FileNotFoundException, IOException {
		Tools.copyFile(localDir + File.separator + fileName, destinationPath + File.separator
				+ fileName);
	}


	private void deleteFilesFromLocal() {
		System.out.println("inside deleteFilesFromLocal");
		File file = new File(localDir);
		File[] files = file.listFiles();
		ArrayList voxFileList = new ArrayList();
		for (int i = 0; files != null && i < files.length; i++) {
			String fileName = files[i].getName();
			if (fileName.endsWith(".vox") || fileName.endsWith(".vox")) {
				String tempFilePath = (files[i].getAbsolutePath().substring(0,
						files[i].getAbsolutePath().lastIndexOf(".")).concat(".WAV")).toUpperCase();
//				voxFileList.add(new SortFilesByLastModDate(new File(tempFilePath)));
				logger.info("RBT::deleting vox file->"
						+ files[i].getAbsolutePath());
				System.out.println("RBT::deleting vox file->"
						+ files[i].getAbsolutePath());
				files[i].delete();
			}
			if (fileName.endsWith(".wav") || fileName.endsWith(".wav")) {
				String tempFilePath = (files[i].getAbsolutePath().substring(0,
						files[i].getAbsolutePath().lastIndexOf(".")).concat(".WAV")).toUpperCase();
				logger.info("RBT::adding wav file->"
						+ files[i].getAbsolutePath());
				voxFileList.add(new SortFilesByLastModDate(new File(tempFilePath)));
			}
		}
//		MyComparator comparator=new MyComparator();
//		Collections.sort(voxFileList, comparator);
		Collections.sort(voxFileList);
		if (voxFileList.size() > localFileCount) {
			for (int i = 0; i < (voxFileList.size() - localFileCount); i++) {
				SortFilesByLastModDate fileToDel = ((SortFilesByLastModDate) voxFileList.get(i));
				logger.info("RBT::deleting file->"
						+ fileToDel.file.getAbsolutePath());
				System.out.println("RBT::deleting wav file->"+ fileToDel.file.getAbsolutePath());
				fileToDel.file.delete();
			}
		}
	}

	private void makeFeedFileRequest(String destFile) throws HttpException, IOException, RBTException,
	Exception {
			StringTokenizer stk = new StringTokenizer(feedURL, ",");
			while (stk.hasMoreTokens()) {
				String url = stk.nextToken();
				HttpParameters httpParam = Tools.getHttpParamsForURL(url+"|6000|15000", null);
				httpParam.setParamsAsParts(true);
				url = httpParam.getUrl();

				//make one more http hit to turn the feed on
				HashMap onParams = new HashMap();
				onParams.put(FEED, CRICKET);
				onParams.put(STATUS, "ON");
				String response = RBTHTTPProcessing.postFile(httpParam, onParams, null);
				
				if(response != null) {
					//response=response.substring(56,response.indexOf("</form>"));
					//response = response.trim();
					if(response.indexOf("SUCCESS")!=-1) {
						File[] arrfile = new File[1];
						arrfile[0] = new File(destFile);
						if(arrfile[0].exists())
							logger.info("RBT:: verifying file -> " +arrfile[0].getAbsolutePath()+", it exists");
						logger.info("RBT:: sending file -> " +arrfile[0].getAbsolutePath());
						HashMap params = new HashMap();
						params.put(FEED, CRICKET);
						String fileResponse = RBTHTTPProcessing.postFile(httpParam, params, arrfile);
						if(fileResponse!=null){
							//fileResponse=fileResponse.substring(56,fileResponse.indexOf("</form>"));
							fileResponse = fileResponse.trim();
						}
						logger.info("RBT:: url -> " + url + ", Response -> "
								+ fileResponse);
						System.out.println( "RBT:: url -> " + url + ", Response -> "
								+ fileResponse);
					}
					else{
						logger.info("RBT:: got response=="+response+" while turning feed on for url -> "
										+ httpParam.getUrl());
						System.out.println("RBT:: got response=="+response+" while turning feed on for url -> "
										+ httpParam.getUrl());
					}
				}
				else
					logger.info("RBT:: got null response while turning feed on for url -> "
									+ httpParam.getUrl());
					System.out.println("RBT:: got null response while turning feed on for url -> "
									+ httpParam.getUrl());
			}
		
	}

	private boolean makeFeedOffRequest() {
		System.out.println("going to make feed off");

		boolean retVal = true;
		if(feedURL!=null && feedURL.length()>0){
			System.out.println("feedURL=="+feedURL);
			StringTokenizer stk = new StringTokenizer(feedURL, ",");
		while (stk.hasMoreTokens() && retVal) {
			String url = stk.nextToken();
			HttpParameters httpParam = Tools.getHttpParamsForURL(url+"|6000|15000", null);
			url = httpParam.getUrl();

			HashMap params = new HashMap();
			params.put(FEED, CRICKET);
			params.put(STATUS, "OFF");
			httpParam.setParamsAsParts(true);
			String response = null;
			try {
				response = RBTHTTPProcessing.postFile(httpParam, params, null);
				System.out.println("response=="+response);
			}
			catch (HttpException e) {
				logger.error("", e);
			}
			catch (IOException e) {
				logger.error("", e);
			}
			catch (RBTException e) {
				logger.error("", e);
			}
			catch (Exception e) {
				logger.error("", e);
			}
			logger.info("RBT:: url -> " + url + ", Response -> " + response);
			retVal = retVal && (response != null && (response.indexOf("SUCCESS") != -1));
		}
	}
		System.out.println("retVal=="+retVal);
		return retVal;
	}

//	private boolean turnFeedOn() {
//		String method = "turnFeedOn";
//		boolean result = false;
//		RBTDBManager dbManager = RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix);
//		FeedStatus feedStatus = dbManager.getFeedStatus(CRICKET);
//		if (feedStatus != null) {
//			String status = feedStatus.status();
//			// logger.info("Sree:: status is -> " + status);
//			if (status.equalsIgnoreCase("on"))
//				result = true;
//			else if (status.equalsIgnoreCase("OFF")) {
//				result = dbManager.setStatus(CRICKET, "ON");
//			}
//			else
//				Tools.logWarning(_class, method, "RBT::status not in known ones");
//		}
//		else
//			Tools.logFatalError(_class, method, "RBT:: no entry in RBT_FEED_STATUS for CRICKET");
//		return result;
//	}

	private boolean turnFeedOff() {
		long timeMillis = System.currentTimeMillis();
		boolean result = false;
		// turning feed off if last download time > configured time
		if ((timeMillis - lastFeedDownload) > (feedOffMin * 60 * 1000)) {
			/*RBTDBManager dbManager = RBTDBManager.init(m_dbURL, m_usePool, m_countryPrefix);
			FeedStatus feedStatus = dbManager.getFeedStatus(CRICKET);
			if (feedStatus != null) {
				String status = feedStatus.status();
				if (status.equalsIgnoreCase("off"))
					result = true;
				else if (status.equalsIgnoreCase("on")) {*/
					if (makeFeedOffRequest()) {
						lastFeedDownload = System.currentTimeMillis();
//						result = dbManager.setStatus(CRICKET, "OFF");
					}
				/*}
			}
			else
				Tools.logFatalError(_class, method,
				"RBT:: no entry in RBT_FEED_STATUS for CRICKET");*/
		}
		return result;
	}
//	private boolean convertWav2Wav(String destFile){
//		String method = "convertWav2Wav";
//
//		// deleting the wav file if exists
//		String wavFileName = destFile.substring(0, destFile.lastIndexOf(".")) + OUT_FILE_EXT;
//		File wavFile = new File(localDir +File.separator+ wavFileName);
////		if (wavFile.exists()) {
////		if (!wavFile.delete()) {
////		Tools.logWarning(_class, method, "RBT:: not able to delete old wav file "
////		+ wavFile.getAbsolutePath());
////		return false;
////		}
////		}
//
//		String batFilePath = createBatchFileForConversion(wavFileName,conversionStrWav);
//		logger.info("RBT:: batFilePath -> " + batFilePath);
//		File batFile = new File(batFilePath);
//		if (batFile.exists()) {
//			logger.info("RBT:: batch file exists");
//			try {
//				char y = 'y';
//				/*
//				 * Properties envMap = System.getProperties(); Set temp =
//				 * envMap.keySet(); Iterator itr = temp.iterator(); ArrayList
//				 * arrEnv = new ArrayList(); String check2 = null; String check3 =
//				 * null; while (itr.hasNext()) { StringBuffer strBuff1 = new
//				 * StringBuffer(); check2 = (String) itr.next();
//				 * strBuff1.append(check2); strBuff1.append("=");
//				 * strBuff1.append(envMap.get(check2)); check3 =
//				 * strBuff1.toString(); strBuff1 = null;
//				 * System.out.println("Adding from sys properties " + check3);
//				 * arrEnv.add(check3); } String[] strEnv = (String[])
//				 * arrEnv.toArray(new String[0]);
//				 */
//				Process process = Runtime.getRuntime().exec(batFilePath, null, new File(localDir));
//				// If the file already exist, process asks the question: y/n
//				// below is the way to enter the key-stroke.
//				process.getOutputStream().write((int) y);
//
//				InputStream is = process.getInputStream();
//				BufferedReader br = new BufferedReader(new InputStreamReader(is));
//				String line = br.readLine();
//				while (line != null) {
//					System.out.println("!!!!");
//					System.out.println(line);
//					line = br.readLine();
//				}
//				process.waitFor();
//				System.out.println("here...........");
//			}
//			catch (java.lang.InterruptedException io) {
//				logger.error("", io);
//				return false;
//			}
//			catch (java.io.IOException ioe) {
//				logger.error("", ioe);
//				return false;
//			}
//			finally {
//				if (batFile.exists())
//					batFile.delete();
//			}
//		}
//		return true;
//	}

	private boolean convertVox2Wav(String destFile) {
		// deleting the wav file if exists
		String wavFileName = destFile.substring(0, destFile.lastIndexOf(".")) + OUT_FILE_EXT;
		File wavFile = new File(localDir +File.separator+ wavFileName);
		if (wavFile.exists()) {
			if (!wavFile.delete()) {
				logger.info("RBT:: not able to delete old wav file "
						+ wavFile.getAbsolutePath());
				return false;
			}
		}

		String batFilePath = createBatchFileForConversion(destFile,conversionStr);
		logger.info("RBT:: batFilePath -> " + batFilePath);
		File batFile = new File(batFilePath);
		if (batFile.exists()) {
			logger.info("RBT:: batch file exists");
			try {
				char y = 'y';
				/*
				 * Properties envMap = System.getProperties(); Set temp =
				 * envMap.keySet(); Iterator itr = temp.iterator(); ArrayList
				 * arrEnv = new ArrayList(); String check2 = null; String check3 =
				 * null; while (itr.hasNext()) { StringBuffer strBuff1 = new
				 * StringBuffer(); check2 = (String) itr.next();
				 * strBuff1.append(check2); strBuff1.append("=");
				 * strBuff1.append(envMap.get(check2)); check3 =
				 * strBuff1.toString(); strBuff1 = null;
				 * System.out.println("Adding from sys properties " + check3);
				 * arrEnv.add(check3); } String[] strEnv = (String[])
				 * arrEnv.toArray(new String[0]);
				 */
				Process process = Runtime.getRuntime().exec(batFilePath, null, new File(localDir));
				// If the file already exist, process asks the question: y/n
				// below is the way to enter the key-stroke.
				process.getOutputStream().write((int) y);

				InputStream is = process.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String line = br.readLine();
				while (line != null) {
					System.out.println("!!!!");
					System.out.println(line);
					line = br.readLine();
				}
				process.waitFor();
				System.out.println("here...........");
			}
			catch (java.lang.InterruptedException io) {
				logger.error("", io);
				return false;
			}
			catch (java.io.IOException ioe) {
				logger.error("", ioe);
				return false;
			}
			finally {
				if (batFile.exists())
					batFile.delete();
			}
		}
		return true;
	}

	private String createBatchFileForConversion(String destFile,String conversionStr1) {
		String tool=null;
		String batFilePath=null;
		String wavFileName = destFile.substring(0, destFile.lastIndexOf(".")) + OUT_FILE_EXT;
//		if(conversionStr1.equals(conversionStrWav)){
//			tool = conversionStr1;
//			tool = Tools.findNReplace(tool, "%FILE%", destFile.substring(0, destFile.lastIndexOf(".")) + OUT_FILE_EXT);
//			tool = Tools.findNReplace(tool, "%FILE1%", destFile.substring(0, destFile.lastIndexOf("."))+"_temp"+ OUT_FILE_EXT);
//			if(OperatingSystemType.equalsIgnoreCase("Unix")){
//				batFilePath = localDir + File.separator+destFile + ".sh";
//			}else{
//				batFilePath = localDir + File.separator+destFile + ".bat";
//			}
//		}else 
		if(conversionStr1.equals(conversionStr)){
			tool = conversionStr1;
			tool = Tools.findNReplace(tool, "%FILE%", destFile);
			tool = Tools.findNReplace(tool, "%FILE1%", wavFileName);
			if(OperatingSystemType.equalsIgnoreCase("Unix")){
				batFilePath = localDir + File.separator+destFile + ".sh";
			}else{
				batFilePath = localDir + File.separator+destFile + ".bat";
			}
		}else {
			tool=conversionStr1;
			if(OperatingSystemType.equalsIgnoreCase("Unix")){
				batFilePath = localDir+File.separator+destFile + ".sh";
			}else{
				batFilePath = localDir+File.separator+destFile + ".bat";
			}
		}
		//String batFilePath = localDir + destFile + ".bat";
		try {
			File batFile = new File(batFilePath);
			Tools.writeTFile(batFile, tool);
			if (!batFile.exists())
				throw new Exception("cannot create batch file");
		}
		catch (Exception e) {
			logger.info("RBT:: error in creating batch file -> "+ batFilePath);
		}
		return batFilePath;
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
	int count=0;
	for(int i=0;i<prefix.size();i++){
		logger.info("RBT:: count "+i);
		logger.info("RBT:: looking for circle id in site prefix table -> " + prefix.get(i).getCircleID());
		if(arrSites.contains(prefix.get(i).getCircleID())){
			logger.info("RBT:: circle id " + prefix.get(i).getCircleID()+"exist in sites array");
			temp=prefix.get(i).getSiteUrl();
			logger.info("RBT:: url=="+temp);
			if(temp!=null && temp.indexOf("rbt_sms.jsp")!=-1){
				logger.info("RBT:: url is not null");
				temp=temp.trim();
				logger.info("RBT:: temp value is "+temp);
				temp=temp.substring(0,temp.indexOf("rbt_sms.jsp"));
				logger.info("RBT:: temp value is "+temp);
				temp=temp+"rbt_feed.jsp?";
				logger.info("RBT:: temp value is "+temp);
				if(count==0){
					feedUrl=temp;
					logger.info("RBT:: FeedUrl is "+feedUrl);
				}else{
					feedUrl=feedUrl+","+temp;
					logger.info("RBT:: FeedUrl is "+feedUrl);
				}
				count++;
			}
		}
	}
	logger.info("RBT:: inside getFeedUrl with feedurl=="+feedUrl);
	return feedUrl;
}
	private HashMap getParams() {
		List<Parameters> allParams = CacheManagerUtil.getParametersCacheManager().getParameters("CRICKET_DAEMON");

		if (allParams == null)
			return null;
		HashMap paramMap = new HashMap();
		for (int i = 0; allParams != null && i < allParams.size(); i++) {
			paramMap.put(allParams.get(i).getParam(), allParams.get(i).getValue());
		}
		return paramMap;
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
	public  void copyRemoteFile(String user, String password,
			String host) throws IOException {
		FileSystemOptions fsOptions = new FileSystemOptions();
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
				fsOptions, "no");
		// create a new filesystem manager
		DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS
		.getManager();
		// the url is of form sftp://user:pass@host/remotepath/
		String uri = "sftp://" + user + ":" + password + "@" + host
		+ "/";
		//
		FileObject fo = fsManager.resolveFile(uri, fsOptions);
		System.out.println("Name "+fo.exists());
//		FileObject destFileListPar=fo.getParent();
//		System.out.println("parent file name in cricket folder is=="+destFileListPar.getName().getPath());
		FileObject[] destFileListTemp=fo.getChildren();
		for(int j=0;j<destFileListTemp.length;j++){
			System.out.println("file name=="+destFileListTemp[j].getName().getPath());
			if(destFileListTemp[j].getName().getPath().indexOf(ftpDir)>0){
				FileObject[] destFileList=destFileListTemp[j].getChildren();
				FileObject temp=null;
				FileObject temp1=null;
				String fileName=null;
				FileName filename=null;
				String ext=null;
				String filePath=null;
				String fileExtension="wav"; 
                if(m_useVoxToWavConvertor){ 
                        fileExtension="vox"; 
                } 
				for(int i=0;i<destFileList.length;i++){
					System.out.println("file name in cricket folder is=="+destFileList[i].getName().getPath());
					temp1=destFileList[i];
					filename=temp1.getName();

					ext=filename.getExtension();

					if(ext.equalsIgnoreCase(fileExtension)){
						filePath=filename.getPath();  
						filePath=filePath.substring(filePath.lastIndexOf(File.separator)+1);
						// open input stream from the remote file
						BufferedInputStream is = new BufferedInputStream(temp1.getContent()
								.getInputStream());
						//File file=new File("e:\\songs");
						// open output stream to local file
						OutputStream os = new FileOutputStream(new File(localDir+ File.separator + filePath));

						byte[] buf = new byte[1024];
						int len;

						while ((len = is.read(buf)) > 0) 
							os.write(buf, 0, len);
						os.close();
						is.close();
						fo.close();
						fsManager.close();
						System.out.println("Finished copying the file");
					}
				}

			}
		}

	}
	private String getOperatingSystemType(){
		String OSType="Windows";
		if(isWindows()){
			System.out.println("This is Windows");
			OSType="Windows";
		}else if(isMac()){
			System.out.println("This is Mac");
			OSType="Mac";
		}else if(isUnix()){
			System.out.println("This is Unix or Linux");
			OSType="Unix";
		}else{
			System.out.println("Your OS is not support!!");
			OSType="Unknown OS";
		}
		return OSType;
	}
	public static boolean isWindows(){
		 
		String os = System.getProperty("os.name").toLowerCase();
		//windows
	    return (os.indexOf( "win" ) >= 0); 
 
	}
 
	public static boolean isMac(){
 
		String os = System.getProperty("os.name").toLowerCase();
		//Mac
	    return (os.indexOf( "mac" ) >= 0); 
 
	}
 
	public static boolean isUnix(){
 
		String os = System.getProperty("os.name").toLowerCase();
		//linux or unix
	    return (os.indexOf( "nix") >=0 || os.indexOf( "nux") >=0);
 
	}

}