package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
/**
 * <p>To Upload and Download files from server</p>
 * @author lakka.rameswarareddy
 *
 */

public class FileDownloadUpload extends NameTuneConstants {
	private static Logger logger = Logger.getLogger(FileDownloadUpload.class);
	Session sshSession = null;
	ChannelSftp sftp = null;
	Channel channel = null;
	public ChannelSftp connect(String host, int port, String username, String password) {
		try {
			JSch jsch = new JSch();
			sshSession = jsch.getSession(username, host, port);
			logger.debug("Session created. ");
			sshSession.setPassword(password);
			Properties sshConfig = new Properties();
			sshConfig.put("StrictHostKeyChecking", "no");
			sshSession.setConfig(sshConfig);
			sshSession.connect();
			logger.debug("Session connected. Opening Channel.");
			channel = sshSession.openChannel("sftp");
			channel.connect();
			sftp = (ChannelSftp) channel;
			logger.debug("Connected to " + host + ".");
		} catch (Exception e) {
			logger.error("ERROR WHILE UPLOADING FILES", e);
			e.printStackTrace();
		}
		return sftp;
	}

	public void disconnect(){
		if(sftp!=null && sftp.isConnected())
			sftp.disconnect();
		if(channel!=null && channel.isConnected())
			channel.disconnect();
		if(sshSession!=null && sshSession.isConnected())
			sshSession.disconnect();
	}
	/**
	 * Download the file * @param directory * @param downloadFile download
	 * directory file download * @param saveFile local path * @param sftp
	 */
	public static void download(String directory, String downloadFile, String saveFile, ChannelSftp sftp) {
		FileOutputStream fileOutputStream = null;
		try {
			sftp.cd(directory);
			File file = new File(saveFile);
			if(!file.exists()){
				file.createNewFile();
			}
			fileOutputStream = new FileOutputStream(file);
			sftp.get(downloadFile, fileOutputStream);
			fileOutputStream.close();
			logger.debug("DOWNLOADED SUCCESSFULLY AT:" + saveFile);
		} catch (Exception e) {
			logger.error("REMOTE DIR:"+directory+" , REMOTE File:"+downloadFile+", LOCAL SAVE FILE:"+saveFile);
			logger.error("ERROR WHILE DOWNLOADING FILE FROM FTP DIR : " + e.getMessage(), e);
			e.printStackTrace();
		}
	}

	/**
	 * Upload file * @param directory upload directory * @param uploadFile file
	 * to upload * @param sftp
	 */
	public static void upload(String directory, File file, ChannelSftp sftp) {
		try {
			sftp.cd(directory);
			FileInputStream fileInputStream = new FileInputStream(file);
			sftp.put(fileInputStream, file.getName());
			fileInputStream.close();
			logger.debug("SUCCESSFULLY UPLOADED FILE :" + file.getCanonicalPath());
		} catch (Exception e) {
			logger.error("ERROR WHILE UPLOADING FILES", e);
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		FileDownloadUpload sftpOpr=new FileDownloadUpload();
		sftpOpr.downloadProcessedRequests();
		//sftpOpr.uploadNewReqFiles();
		//sftpOpr.uploadStatusReports();
	}

	public void downloadProcessedRequests() {
		ChannelSftp channelSftp = connect(
				FTP_DOWNLOAD_PROCESSED_FILE_SERVER_IP, 22,
				FTP_DOWNLOAD_PROCESSED_FILE_SERVER_USERNAME,
				FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PASSWORD);
		if (channelSftp != null) {
			String locDownPath = LOCAL_BASE_DIRECTORY + File.separator
					+ NEW_REQ_PROCESSED_DIR;
			File file = new File(locDownPath);
			if (!file.exists()) {
				file.mkdir();
			}
			FileDownloadUpload.download(FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PATH,
					FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME, locDownPath +File.separator
							+ FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME,
					channelSftp);
			try {
				channelSftp.cd(FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PATH);
				channelSftp.rm(FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME);
				logger.debug("REMOTE FILE DELETED SUCCESSFULLY."+FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME);
			} catch (SftpException e) {
				logger.error("FAILED TO DELETE REMOTEPATH:"+FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PATH+" | REMOTE FILE:"+FTP_DOWNLOAD_PROCESSED_FILE_SERVER_FILENAME +" ERROR MESSGE:"+e.getMessage(),e);
			}
			disconnect();
		} else {
			logger.fatal("UNABLE TO CONNECT FTP SERVER WITH DETAILS: IP :"
					+ FTP_DOWNLOAD_PROCESSED_FILE_SERVER_IP + " , USERNAME:"
					+ FTP_DOWNLOAD_PROCESSED_FILE_SERVER_USERNAME
					+ " , PASSWORD: "
					+ FTP_DOWNLOAD_PROCESSED_FILE_SERVER_PASSWORD);
		}
	}

	public void uploadNewReqFiles() {
		ChannelSftp channelSftp = connect(FTP_UPLOAD_NEW_REQ_SERVER_IP, 22, FTP_UPLOAD_NEW_REQ_SERVER_USERNAME,
				FTP_UPLOAD_NEW_REQ_SERVER_PASSWORD);
		if (channelSftp != null) {
			String baseDir = LOCAL_BASE_DIRECTORY + File.separator + NEW_REQ_DIR;

			File baseDirFileObj = new File(baseDir);
			if (baseDirFileObj.exists()) {
				if (baseDirFileObj.isDirectory()) {
					File[] files = baseDirFileObj.listFiles();
					for (File file : files) {
						upload(FTP_UPLOAD_NEW_REQ_SERVER_PATH, file, channelSftp);
					}
					if (channelSftp.isConnected())
						channelSftp.disconnect();
				} else {
					logger.error(baseDir + " : IS NOT A FOLDER");
				}

			} else {
				logger.error("NEW REQ FOLDER DOESN'T EXIST IN THIS PATH:" + baseDir);
			}
		} else {
			logger.fatal("UNABLE TO CONNECT FTP SERVER WITH DETAILS: IP :" + FTP_UPLOAD_NEW_REQ_SERVER_IP
					+ " , USERNAME:" + FTP_UPLOAD_NEW_REQ_SERVER_USERNAME + " , PASSWORD: "
					+ FTP_UPLOAD_NEW_REQ_SERVER_PASSWORD);
		}
	}

	public void uploadStatusReports() {
		ChannelSftp channelSftp = connect(FTP_UPLOAD_REPORTS_SERVER_IP, 22, FTP_UPLOAD_REPORTS_SERVER_USERNAME,
				FTP_UPLOAD_REPORTS_SERVER_PASSWORD);
		if (channelSftp != null) {

			/* UPLOADING FAILURE LOG FILES */
			String completeDir = LOCAL_BASE_DIRECTORY + File.separator + COMPLETED_REPORT_LOG_DIR;
			File baseDirFileObj = new File(completeDir);
			if (baseDirFileObj.exists()) {
				if (baseDirFileObj.isDirectory()) {
					File[] files = baseDirFileObj.listFiles();
					for (File file : files) {
						upload(FTP_UPLOAD_COMPLETED_REPORTS_SERVER_PATH, file, channelSftp);
					}
				} else {
					logger.error(completeDir + " : IS NOT A FOLDER");
				}

			} else {
				logger.error("NEW REQ FOLDER DOESN'T EXIST IN THIS PATH:" + completeDir);
			}

			/* UPLOADING FAILURE LOG FILES */
			String failureDir = LOCAL_BASE_DIRECTORY + File.separator + FAILURE_REPORT_LOG_DIR;
			File failureFileObj = new File(failureDir);
			if (failureFileObj.exists()) {
				if (failureFileObj.isDirectory()) {
					File[] files = failureFileObj.listFiles();
					for (File file : files) {
						upload(FTP_UPLOAD_FAILURE_REPORTS_SERVER_PATH, file, channelSftp);
					}
				} else {
					logger.error(failureDir + " : IS NOT A FOLDER");
				}
			} else {
				logger.error("NEW REQ FOLDER DOESN'T EXIST IN THIS PATH:" + failureDir);
			}

			if (channelSftp.isConnected())
				channelSftp.disconnect();

		} else {
			logger.fatal("UNABLE TO CONNECT FTP SERVER WITH DETAILS: IP :" + FTP_UPLOAD_REPORTS_SERVER_IP
					+ " , USERNAME:" + FTP_UPLOAD_REPORTS_SERVER_USERNAME + " , PASSWORD: "
					+ FTP_UPLOAD_REPORTS_SERVER_PASSWORD);
		}
	}
}
