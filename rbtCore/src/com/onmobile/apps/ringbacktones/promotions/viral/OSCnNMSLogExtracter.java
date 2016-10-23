package com.onmobile.apps.ringbacktones.promotions.viral;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.onmobile.apps.ringbacktones.Gatherer.FTPConfig;
import com.onmobile.apps.ringbacktones.Gatherer.FTPHandler;

/**
 * @author sridhar.sindiri
 *
 */
public class OSCnNMSLogExtracter 
{
	private BufferedWriter bw = null;
	private String fileName = null;
	private String outputDirectory = null;
	private boolean fileClosed = false;
	
	public static final String outputFileFormatWithDate = "yyyyMMddHHmmssSSSSSS"; 
	public static final String outputFileFormat = "OSC_NMS_" + outputFileFormatWithDate;
	public static final String OSC_RECORD_MARKER = "OSC:";
	public static final String NMS_RECORD_MARKER = "NMS:";
	
	
	public OSCnNMSLogExtracter() throws IOException {
		String outputDir = RBTViralConfigManager.getInstance().getParameter("OSC_NMS_OUTPUT_DIRECTORY");
		java.io.File outputDir1 = new java.io.File(outputDir); 
		if(! outputDir1.exists()) 
		{
			outputDir1.mkdirs();
		}
		//init the output directory
		setOutputDirectory(outputDir);
		
		SimpleDateFormat sdf = new SimpleDateFormat(outputFileFormatWithDate); 
		String fileFormatWithDate = sdf.format(new Date());
		
		String siteName = RBTViralConfigManager.getInstance().getParameter("SITE_NAME");
		fileName = outputFileFormat.replace("OSC_NMS_", "OSC_NMS_" + siteName + "_");
		fileName = fileName.replace(outputFileFormatWithDate, fileFormatWithDate);
		
		bw = new BufferedWriter(new FileWriter(getAbsolutePath()));
	}
	
	public synchronized void writeRecordFromOSCFile(String input) throws IOException {
		bw.write(OSC_RECORD_MARKER + input + System.getProperty("line.separator"));
		bw.flush();
	}

	public synchronized void writeRecordFromNMSFile(String input) throws IOException {
		bw.write(NMS_RECORD_MARKER + input + System.getProperty("line.separator"));
		bw.flush();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		
		this.fileName = fileName;
	}

	public String getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(String outputDirectory) {
		if(! outputDirectory.endsWith(File.separator)) {
			outputDirectory = outputDirectory + File.separator;
		}
		this.outputDirectory = outputDirectory;
	}
	
	public String getAbsolutePath() {
		return outputDirectory + fileName + ".txt";
	}
	
	public void close() throws IOException {
		bw.close();
		fileClosed = true;
	}
	
	public void ftpUploadFile() throws IOException {
		if(!fileClosed) 
			close();

		//read the ftp details from properties file, create ftp config object
		String server = RBTViralConfigManager.getInstance().getParameter("FTP_SERVER_IP");
		int port = Integer.parseInt(RBTViralConfigManager.getInstance().getParameter("FTP_PORT"));
		String user = RBTViralConfigManager.getInstance().getParameter("FTP_USERNAME");
		String pwd = RBTViralConfigManager.getInstance().getParameter("FTP_PASSWORD");
        String dir = RBTViralConfigManager.getInstance().getParameter("FTP_DIRECTORY");
        int wait = Integer.parseInt(RBTViralConfigManager.getInstance().getParameter("FTP_WAIT_TIME"));
        int retry = Integer.parseInt(RBTViralConfigManager.getInstance().getParameter("FTP_RETRY_TIME"));
        int timeout = Integer.parseInt(RBTViralConfigManager.getInstance().getParameter("FTP_TIMEOUT"));
		
        String ftpFilePath = getAbsolutePath();
        FTPConfig ftpConfig = new FTPConfig(server, port, user, pwd, dir, wait, retry, timeout);
		FTPHandler ftpHandler = new FTPHandler(ftpConfig);
		ftpHandler.upload(ftpFilePath);
		
		File file = new File(ftpFilePath);
		file.delete();
	}
}
