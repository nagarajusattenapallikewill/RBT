package com.onmobile.apps.ringbacktones.daemons;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.sshtools.j2ssh.SftpClient;
import com.sshtools.j2ssh.SshClient;
import com.sshtools.j2ssh.authentication.AuthenticationProtocolState;
import com.sshtools.j2ssh.authentication.PasswordAuthenticationClient;
import com.sshtools.j2ssh.configuration.ConfigurationException;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.configuration.SshConnectionProperties;
import com.sshtools.j2ssh.sftp.SftpFile;

public class SFTPDownloader  implements iRBTConstant {
	
private static Logger logger = Logger.getLogger(SFTPDownloader.class);

Calendar calendar = Calendar.getInstance();
private String user = null;
private String pass = null;
private String host = null;
private String port = "22";
private String sftpDir = null;
private String localDirSFTP = null;
private boolean m_useVoxToWavConvertor=false;
private boolean m_useSFTP=false;


public static void main(String[] args){
	Tools.init("SFTPDownloader", true);
//	SFTPDownloader instance = new SFTPDownloader(CRICKET);
//	instance.start();
}
public SFTPDownloader(boolean useSftp,String sftpDir,boolean useVoxToWavConvertor,String user,String password,String host,String port,String localDir) {
	this.m_useSFTP=useSftp;
	this.sftpDir=sftpDir;
	this.m_useVoxToWavConvertor=useVoxToWavConvertor;
	this.port=port;
	this.user = user;
	this.pass = password;
	this.host = host;
	this.localDirSFTP = localDir;
}
public String start() {
	
		String destFile=null;
		if(m_useSFTP){
			destFile=connectToSFTPServer();
		}
		logger.info("RBT::going to sleep....." );
		// sleep
		return destFile;
}

public String connectToSFTPServer() {
	logger.info("entering..."); 
	System.out.println("Connecting to the SFTP server..");
	SftpClient sftp =null;
	SshClient ssh =null;
	SftpFile destFile=null;
	String returnString=null;
	//FileWriter logWriter=null;
	try{
			String hostName=host;
			String sftpUser=user;
			String sftpPassword=pass;
			String sftpLocation=sftpDir;
			String socketTimeOut="5000";
	
			ConfigurationLoader.initialize(false);
			ssh = new SshClient();
			// Connect to the host
			SshConnectionProperties prop=new SshConnectionProperties();
			prop.setHost(hostName);
			prop.setPort(Integer.parseInt(port));
			prop.setUsername(sftpUser);
		
			ssh.setSocketTimeout(Integer.parseInt(socketTimeOut));
			logger.info("Connecting to the SFTP server.."); 
			ssh.connect(prop);
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
				// The connection is authenticated we can now do some real work!
				logger.info("Opening SFTPClient");
		        sftp = ssh.openSftpClient();
		        logger.info("Opened SFTPClient");
		        try{
		        	logger.info("Going to dest dir "+sftpLocation+" in SFTP");
		        	sftp.cd(sftpLocation);
		        	logger.info("Entered into dest dir"+sftpLocation+" in SFTP");
		        }
		        catch(IOException ioe){
		        	logger.info("Either the folder does not exist or its not a directory ");
		        	logger.info("Could not get into the remote folder due to "+ioe.getMessage()+"\n");
		        }
		        logger.info("Existing sftp dir=="+sftp.pwd().toString());
		       List contentListTemp=(List)sftp.ls();
		        if(contentListTemp!=null && contentListTemp.size()>0){
		        	logger.info("File array, in dest dir, is of size"+contentListTemp.size());
		        	String fileExtension="wav";
		        	if(m_useVoxToWavConvertor){
		        		fileExtension="vox";
		        	}
		        	List contentList = null;
		        	for(int i=0;i<contentListTemp.size();i++){
		        		logger.info("File no. "+i+"== "+((SftpFile)contentListTemp.get(i)).getFilename());
		        		logger.info("its modified date is == "+((SftpFile)contentListTemp.get(i)).getAttributes().getModTimeString());
		        		if(((SftpFile)contentListTemp.get(i)).getFilename().substring(((SftpFile)contentListTemp.get(i)).getFilename().indexOf(".") + 1).equalsIgnoreCase(fileExtension)){
		        			if(contentList == null){
		        				contentList = new ArrayList();
		        			}
		        			contentList.add((SftpFile)contentListTemp.get(i));
		        		}
		        	}
		        	if(contentList != null ){
		        		logger.info("Valid File array, in dest dir, is of size"+contentList.size());
		        		destFile=getLatestVoxFile(contentList);
		        	}
		        	//SftpFile[] fileList=(SftpFile[])(contentList.toArray(new SftpFile[0]));
			        
			        logger.info("latest file, in dest dir, is "+destFile.getFilename());
			        logger.info("Transferring "+destFile.getFilename()+" from dest dir to local folder== "+sftp.lpwd());
			        sftp.lcd(localDirSFTP);
			        sftp.get(destFile.getFilename());
			        logger.info("Transferred "+destFile.getFilename()+" from dest dir to local folder== "+sftp.lpwd());
			        if(destFile!=null){
			        returnString= destFile.getFilename();
			        try {
			        	logger.info("deleting destfile==" + destFile.getFilename());
						destFile.delete();
						logger.info("deleted destfile==" + destFile.getFilename());
					} catch (RuntimeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.info(e.getMessage());
					}
			        }
		        }
		        
			}
		}
		catch(ConfigurationException ce){
			logger.info(ce.getMessage());
			ce.printStackTrace();
		}
		catch(IOException ioe){
			logger.info(ioe.getMessage());
			ioe.printStackTrace();
		}
		catch(Exception ioe){
			logger.info(ioe.getMessage());
			ioe.printStackTrace();
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
			if(destFile!=null){
			returnString= destFile.getFilename();
			}
		}
		return returnString;
}
private SftpFile getLatestVoxFile(List fileList) throws IOException {
	SftpFile destFile = null;
	int iCount = 0, lastCount = 0;
	String fileExtension="wav";
	if(m_useVoxToWavConvertor){
		fileExtension="vox";
	}
	try {
		
		Date check1 = null;
		Date check2 = null;
		SimpleDateFormat sdf=new SimpleDateFormat("MMM dd HH:mm");
		for (int i = 0; fileList != null && i < fileList.size(); i++) {
			if (((SftpFile)fileList.get(i)).getFilename().substring(((SftpFile)fileList.get(i)).getFilename().indexOf(".") + 1).equalsIgnoreCase(fileExtension)) {
				logger.info("i==" + i);
				logger.info("current file==" + ((SftpFile)fileList.get(i)).getFilename());
				check2 =sdf.parse(((SftpFile)fileList.get(i)).getAttributes().getModTimeString());
				logger.info("check2 ::"+check2.toString());
				
				if (check1 == null) {
					logger.info("check1 is null");
					check1 = check2;
					destFile =((SftpFile)fileList.get(i));
				}else{
					logger.info("check1 is not null ::"+check1.toString());
				}
				logger.info("check1 ::"+check1.toString());
				if (check2.after(check1)) {
					logger.info("file no ::"+i+" is newer file");
					check1 = check2;
					destFile = ((SftpFile)fileList.get(i));
					lastCount = iCount;
					iCount = i;
					
						for (int j = lastCount; j < iCount; j++) {
							try {
								logger.info("deleting file==" + ((SftpFile)fileList.get(j)).getFilename());
								if(((SftpFile)fileList.get(j)).getFilename().substring(((SftpFile)fileList.get(j)).getFilename().indexOf(".") + 1).equalsIgnoreCase(fileExtension)){
									((SftpFile)fileList.get(j)).delete();
								}
								logger.info("deleted file==" + ((SftpFile)fileList.get(j)).getFilename());
							} catch (RuntimeException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								logger.info(e.getMessage());
								continue;
							}
						}
				}
				else {
					if (i == (fileList.size() - 1)) {
						lastCount = iCount + 1;
						iCount = fileList.size();
							for (int j = lastCount; j < iCount; j++) {
								try {
									logger.info("deleting file==" + ((SftpFile)fileList.get(j)).getFilename());
									if(((SftpFile)fileList.get(j)).getFilename().substring(((SftpFile)fileList.get(j)).getFilename().indexOf(".") + 1).equalsIgnoreCase(fileExtension)){
										((SftpFile)fileList.get(j)).delete();
									}
									logger.info("deleted file==" + ((SftpFile)fileList.get(j)).getFilename());
								} catch (RuntimeException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									logger.info(e.getMessage());
									continue;
								}
								
							}
						
					}
				} // end of modified time check else
			} // end of if vox file
		} // end of for loop
	} // end of try
	catch (Exception e) {
		logger.info(e.getMessage());
		e.printStackTrace();
	}
	return destFile;
}
//private String downloadSFTP(){
//	String method = "downloadSFTP";
//	logger.info("RBT::entering..." );
//	try {
//		copyRemoteFile(user,pass,host);
//	} catch (IOException e1) {
//		e1.printStackTrace();
//	}
//	long tempFileName=0;
//	String tempFileNameStr=null;
//	String fileExtension="wav";
//	if(m_useVoxToWavConvertor){
//		fileExtension="vox";
//	}
//	File localDir=new File(this.localDirSFTP);
//	if(localDir.exists()  && localDir.isDirectory()){
//		String[] filesOnLocalSystem=localDir.list();
//		boolean temp=false;
//		for(int j=0;j<filesOnLocalSystem.length;j++){
//			if(filesOnLocalSystem[j].indexOf("."+fileExtension)>0 || filesOnLocalSystem[j].indexOf("."+fileExtension.toUpperCase())>0 ){
//				temp=true;
//				tempFileNameStr=filesOnLocalSystem[j].substring(0, filesOnLocalSystem[j].lastIndexOf(".")-1);
//
//				try {
//					Long tempFileNameInLong=new Long(tempFileNameStr);
//					long checkFileLong=tempFileNameInLong.longValue();
//					if(checkFileLong>=tempFileName){
//						tempFileName=checkFileLong;
//					}
//
//				} catch (NumberFormatException e) {
//					e.printStackTrace();
//					continue;
//				}
//
//			}
//		}
//		if(temp){
//			
//			try {
//				if(tempFileName==0){
//					deleteFromSFTP(tempFileNameStr+"."+fileExtension,user,pass,host);
//					return tempFileNameStr;
//				}else{
//					deleteFromSFTP(new Long(tempFileName).toString()+"."+fileExtension,user,pass,host);
//					return tempFileNameStr;
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}else{
//			return null;
//		}
//	}else{
//		return null;
//	}
//
//	return null;
//}
//public  void copyRemoteFile(String user, String password,
//		String host) throws IOException {
//	String method="copyRemoteFile";
//	logger.info("RBT::entering..." );
//	// create a new filesystem manager
//	DefaultFileSystemManager fsManager=null;
//	FileObject fo=null;
//	if (sftpFileObj==null) {
//		FileSystemOptions fsOptions = new FileSystemOptions();
//		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
//				fsOptions, "no");
//		fsManager = (DefaultFileSystemManager) VFS.getManager();
//		// the url is of form sftp://user:pass@host/remotepath/
//		String uri = "sftp://" + user + ":" + password + "@" + host + "/"
//				+ sftpDir;
//		//
//		logger.info("RBT::uri==" + uri);
//		fo = fsManager.resolveFile(uri, fsOptions);
//		sftpFileObj=fo;
//	}else{
//		fo=sftpFileObj;
//	}	
//	
//	System.out.println("Name "+fo.exists());
//	logger.info("RBT:: fo.exists()"+fo.exists() );
//	FileObject destFileListPar=fo.getParent();
//	logger.info("parent file name in cricket folder is=="+destFileListPar.getName().getPath()); 
//	System.out.println("parent file name in cricket folder is=="+destFileListPar.getName().getPath());
//	FileObject[] destFileList=fo.getChildren();
////	for(int j=0;j<destFileListTemp.length;j++){
////		System.out.println("file name=="+destFileListTemp[j].getName().getPath());
////		if(destFileListTemp[j].getName().getPath().indexOf(ftpDir)>0){
////			FileObject[] destFileList=destFileListTemp[j].getChildren();
//			FileObject temp1=null;
//			FileName filename=null;
//			String ext=null;
//			String filePath=null;
//			String fileExtension="wav";
//			if(m_useVoxToWavConvertor){
//				fileExtension="vox";
//			}
//			for(int i=0;i<destFileList.length;i++){
//				logger.info("file name in cricket folder is=="+destFileList[i].getName().getPath());
//				System.out.println("file name in cricket folder is=="+destFileList[i].getName().getPath());
//				temp1=destFileList[i];
//				filename=temp1.getName();
//				ext=filename.getExtension();
//
//				if(ext.equalsIgnoreCase(fileExtension)){
//					filePath=filename.getPath(); 
//					logger.info("filepath=="+filePath);
//					if(filePath.indexOf("/")!=-1){
//						filePath=filePath.substring(filePath.lastIndexOf("/")+1);
//					}else{
//						filePath=filePath.substring(filePath.lastIndexOf("\\")+1);
//					}
//					logger.info("filepath=="+filePath);
//					//filePath=filePath.substring(filePath.lastIndexOf(File.separator)+1);
//					// open input stream from the remote file
//					BufferedInputStream is = new BufferedInputStream(temp1.getContent()
//							.getInputStream());
//					//File file=new File("e:\\songs");
//					// open output stream to local file
//					File destFileTemp=new File(localDirSFTP+ File.separator + filePath);
//					logger.info("destFileTemp=="+destFileTemp.getAbsolutePath());
//					OutputStream os = new FileOutputStream(destFileTemp);
//
//					byte[] buf = new byte[1024];
//					int len;
//
//					while ((len = is.read(buf)) > 0) 
//						os.write(buf, 0, len);
//					os.close();
//					is.close();
//					fo.close();
//					//fsManager.close();
//					logger.info("Finished copying the file");
//					System.out.println("Finished copying the file");
//				}
//			}
//
////		}
//	//}
//			//fsManager.close();
//}
//private void deleteFromSFTP(String fileNotBeDeletedFromSFTP,String user,String password,String host)throws IOException {
//
//	FileObject fo =null;
//	if (sftpFileObj==null) {
//		FileSystemOptions fsOptions = new FileSystemOptions();
//		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(
//				fsOptions, "no");
//		DefaultFileSystemManager fsManager = (DefaultFileSystemManager) VFS
//				.getManager();
//		
//		String uri = "sftp://" + user + ":" + password + "@" + host + "/"
//				+ sftpDir;
//		 fo=fsManager.resolveFile(uri, fsOptions);
//		 sftpFileObj=fo;
//	}	
//	else{
//	fo=sftpFileObj;
//	}	
//	System.out.println("Name "+fo.exists());
//
//	FileObject[] destFileList=fo.getChildren();
//	FileObject temp1=null;
//	FileName filename=null;
//	String ext=null;
//	String filePath=null;
//	String fileExtension="wav";
//	if(m_useVoxToWavConvertor){
//		fileExtension="vox";
//	}
//	for(int i=0;i<destFileList.length;i++){
//		temp1=destFileList[i];
//		filename=temp1.getName();
//
//		ext=filename.getExtension();
//
//		if(ext.equalsIgnoreCase(fileExtension)){
//			filePath=filename.getPath();  
//			filePath=filePath.substring(filePath.lastIndexOf(File.separator)+1);
//			if(!filePath.equalsIgnoreCase(fileNotBeDeletedFromSFTP)){
//				destFileList[i].delete();
//			}
//			System.out.println("Finished deleting the file");
//		}
//
//	}
//	//fsManager.close();
//}
}
