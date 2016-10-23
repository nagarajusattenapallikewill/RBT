package com.onmobile.apps.ringbacktones.daemons;

import java.io.IOException;
import java.util.Date;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;

public class FtpDownloader {
	
	private static Logger logger = Logger.getLogger(FtpDownloader.class);

	// public static void main(String args[]){
	// FileDownLoader.FTPConnect(IP_ADD,PORT,USERNAME,PASSWORD,LOCAL_DIR,REMOTE_DIR,DEST_FILE);
	// }
	public FTPClient ftpConnect(String ipAdd, int port, String username, String password,
			 String ftpDir) {
		FTPClient ftpClient =null;
		// localDir is the complete filepath to which the file is sent from ftp
		// ftpDir is the dir of the file on the FTP which has to be
		// downloaded
		// destFile is the file in remoteDir which has to be downloaded

		// Tools.init("FtpDownloader", 6, true);
		String destFile = null;
		try {
			ftpClient = new FTPClient(ipAdd, port);
			// it is 21 ,by default it takes 21 also
			// ftpClient.connect(ftpOb.username,ftpOb.password);
			ftpClient.login(username, password);
			ftpClient.setConnectMode(FTPConnectMode.PASV);
			ftpClient.setType(FTPTransferType.BINARY);

			ftpClient.chdir(ftpDir);
		}catch (IOException e) {
			logger.error("", e);
		}
		catch (FTPException e) {
			logger.error("", e);
		}
		return ftpClient;
	}
	public String ftpConnect(String ipAdd, int port, String username, String password,
			String localDir, String ftpDir) {
		// localDir is the complete filepath to which the file is sent from ftp
		// ftpDir is the dir of the file on the FTP which has to be
		// downloaded
		// destFile is the file in remoteDir which has to be downloaded

		// Tools.init("FtpDownloader", 6, true);
		String destFile = null;

		try {
			FTPClient ftpClient = new FTPClient(ipAdd, port);
			// it is 21 ,by default it takes 21 also
			// ftpClient.connect(ftpOb.username,ftpOb.password);
			ftpClient.login(username, password);
			ftpClient.setConnectMode(FTPConnectMode.PASV);
			ftpClient.setType(FTPTransferType.BINARY);

			ftpClient.chdir(ftpDir);
			destFile = getLatestVoxFile(ftpClient);
			logger.info("RBT:: destFile->" + destFile);
			if(destFile != null) {
				logger.info("RBT:: localDir->" + localDir);
				StringBuffer ts = new StringBuffer();
				ts.append(localDir);
				ts.append(destFile);
				String file = ts.toString();
				logger.info("RBT:: file->" + file);
				ftpClient.get(file, destFile);
				ftpClient.delete(destFile);
			}
			ftpClient.quit();
		}
		catch (IOException e) {
			logger.error("", e);
		}
		catch (FTPException e) {
			logger.error("", e);
		}
		return destFile;
	}

	private String getLatestVoxFile(FTPClient ftpClient) throws IOException {
		String destFile = null;
		int iCount = 0, lastCount = 0;
		try {
			String[] fileList = ftpClient.dir();
			Date check1 = null;
			Date check2 = null;

			for (int i = 0; fileList != null && i < fileList.length; i++) {
				if (fileList[i].substring(fileList[i].indexOf(".") + 1).equalsIgnoreCase("vox")) {
					check2 = ftpClient.modtime(fileList[i]);
					if (check1 == null) {
						check1 = check2;
						destFile = fileList[i];
					}
					if (check2.after(check1)) {
						// logger.info("file no ::"+i+" is newer file");
						check1 = check2;
						destFile = fileList[i];
						lastCount = iCount;
						iCount = i;
						for (int j = lastCount; j < iCount; j++) {
							ftpClient.delete(fileList[j]);
						}

					}
					else {
						if (i == (fileList.length - 1)) {
							lastCount = iCount + 1;
							iCount = fileList.length;
							for (int j = lastCount; j < iCount; j++) {
								ftpClient.delete(fileList[j]);
								logger.info("deleting file==" + fileList[j]);
							}
						}
					} // end of modified time check else
				} // end of if vox file
			} // end of for loop
		} // end of try
		catch (FTPException e) {
			e.printStackTrace();
		}
		return destFile;
	}
}