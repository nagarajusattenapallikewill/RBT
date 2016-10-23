package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

public class DaemonUtility {
	private static final Logger LOGGER = Logger
	.getLogger(DaemonUtility.class);

	public static boolean readZIPFileFromFTP(String fileName,String IP,String username,String password,String workingDirectoryInFTP,String localFilePath){
		boolean response = false;
			
			FTPClient ftp = new FTPClient();
			LOGGER.info("created new FTPClient Object " + ftp);
			FileOutputStream fos = null;
			//String fileName = "test-file.zip";
			try {
				LOGGER.info("inside copyToFTP try block");
				ftp.connect(IP); //Connect to FTP server
				LOGGER.info("connection established.");
				ftp.login(username, password); //Login
				LOGGER.info("logged in.");
				
				if(ftp.isConnected()){
					LOGGER.debug("initial working directory "+ ftp.printWorkingDirectory());
					ftp.changeWorkingDirectory(workingDirectoryInFTP);
					LOGGER.info("current working directory "+ ftp.printWorkingDirectory());
					fos = new FileOutputStream(new File(localFilePath, fileName));
					LOGGER.info("File output stream is " + fos);
					InputStream is = null;
					try {
						is = ftp.retrieveFileStream(fileName);
						LOGGER.info("File input stream is " + is);
						if (null == is) {
							LOGGER.error("Could not get stream for file name " + fileName);
							return response;
						}
						byte c[] = new byte[4096];
						int read = 0;
						while ((read = is.read(c)) != -1) {
							fos.write(c, 0, read);
						}
					} finally {
						if (is != null) {
							is.close();
						}
					}
					response = true;
					LOGGER.info("returning response for copy " + response);
					return response;
					
				}else{
					LOGGER.info("FTP server not connected.");
					response = false;
					LOGGER.info("returning response for copy " + response);
					return response;
				}
			} catch (Exception e) {
				try {
					LOGGER.info("going to close ftp connection.");
					if(ftp.isConnected()){
						LOGGER.info("ftp server is connected");
						ftp.disconnect();
					}
				} catch (IOException e1) {
					LOGGER.error("IO Exception in FTP disconnect.", e1);
				} //Close connection
				LOGGER.error("Error: " + e.getMessage(), e);
			}finally{
				if(fos !=null){
					try {
						fos.close();
					} catch (IOException e) {
						LOGGER.error("Error: " + e.getMessage(), e);
					}
				}
				
			}
			LOGGER.info("returning response for copy " + response);
			return response;
	}
	
//	public static void unzipFile(String localFilePath , String fileNameToBeUnzipped , String targetFolder){
	public static boolean unzipFile(String localFilePath , String fileNameToBeUnzipped, String downloadFilePath){
        
        FileInputStream fis = null;
        ZipInputStream zipIs = null;
        ZipEntry zEntry = null;
        boolean response = false;
        File zipFile = new File(downloadFilePath, fileNameToBeUnzipped);
        LOGGER.info("inside method unzipFile");
        try {
            fis = new FileInputStream(zipFile);
            zipIs = new ZipInputStream(new BufferedInputStream(fis));
            while((zEntry = zipIs.getNextEntry()) != null){
            	if(zEntry.isDirectory()){
            		LOGGER.info("This is a directory.");
            	}else{
            		LOGGER.info("nope its a file.");
            	}
            	FileOutputStream fos = null;
                try {
                    byte[] tmp = new byte[4*1024];
                   // String opFilePath = "E:";
//                    String[] opFileFull = zEntry.getName().split("/");
                    //String directoryName = opFileFull[0];
                    //String directoryName = "parul-test";
//                  String fileName = opFileFull[0];
                    String fileName = zEntry.getName();
//                    File targetFile = new File(fileName);
                    LOGGER.info("target file object made.");
//                    File targetDirectory = new File(localFilePath, targetFolderName);
                    File targetDirectory = new File(localFilePath);
                    if(!targetDirectory.exists()){
                    	targetDirectory.mkdir();
                    	LOGGER.info("making new directory");
                    }else{
                    	LOGGER.info("directory already there.");
                    }
                    File finalFile = new File(localFilePath, fileName);
                    LOGGER.info("Extracting file to " + finalFile.getAbsolutePath());
                    
                    
//                    if(!targetFile.exists()){
//                    	boolean created = targetFile.createNewFile();
//                    	LOGGER.info("boolean create value is " + created);
//                    	if(created){
//                    		LOGGER.info("file created successfully");
//                    		
//                    	}else{
//                    		LOGGER.info("file creation failed.");
//                    	}
//                    }else{
//                    	LOGGER.info("file already exists");
//                    }
                    fos = new FileOutputStream(finalFile);
                    int size = 0;
                    while((size = zipIs.read(tmp)) != -1){
                        fos.write(tmp, 0 , size);
                    }
                    fos.flush();
                    response = true;
                } catch(Exception ex){
                	LOGGER.info("no specific exception", ex);
                } finally {
                	if (fos != null) {
                		fos.close();
                	}
                }
            }
        } catch (FileNotFoundException e) {
        	LOGGER.error("file not found exception", e);
        } catch (IOException e) {
        	LOGGER.info("IO exception", e);
        } finally {
        	if (zipIs != null) {
            	try {
					zipIs.close();
				} catch (IOException e) {
		        	LOGGER.info("IO exception", e);
				}
        	}
        }
        LOGGER.info("value of response from unzipFile() is " + response);
        return response;
    }

	public static boolean renameFile(File file) {
		String newFileName = null;
		boolean isRenamed = false;

		if (file.exists()) {
			newFileName = file.toString().replace(".csv", ".processed");
			File newFile = new File(newFileName);
			isRenamed = file.renameTo(newFile);
			LOGGER.info("value of file rename boolean is " + isRenamed);
			LOGGER.info("Is exists " + file.exists());
			if (file.exists() && isRenamed) {
				LOGGER.info("Still exists so going to delete " + file.exists());
				file.delete();
			}
		} else {
			LOGGER.info("file doesnt exist.sorry");
		}

		LOGGER.info("new file name is " + newFileName);
		return isRenamed;
	}
}
