package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.onmobile.rbt.Ticket;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;

public class GNOCAlarmAggregatorSM {
	
	private static final Logger LOGGER = Logger.getLogger(GNOCAlarmAggregatorSM.class);
	private static GNOCAlarmAggregatorSM gnoc = new GNOCAlarmAggregatorSM();
	private static GNOCURLUtility urlUtility = new GNOCURLUtility();
	final static String OLD_FORMAT = "EEE MMM d HH:mm:ss z yyyy";
	private static String NEW_FORMAT = null;
	private static SimpleDateFormat formatter = new SimpleDateFormat();
	
	public static void main(String[] args) {
		LOGGER.info("time before the process starts. " + formatter.format(System.currentTimeMillis()));
		gnoc.process();
	}
	
	private static List<String> bucketList = null;
	private static List<String> site1DeviceList = null;
	private static List<String> site2DeviceList = null;
	private static List<String> severityNumberList = null;
	private static Map<String,String> severityNumberMapping = null;
	private static String toWriteRaise;
	private static String toWriteClear;
	
	public void process() {
		LOGGER.info("time when entering the process method. " + formatter.format(System.currentTimeMillis()));
		if (GNOCPropertyReaderSM.checkNullValues()) {
			try {
				site1DeviceList = Arrays.asList(GNOCPropertyReaderSM.siteName1_devices.split(","));
				LOGGER.info("site 1 device list is " + site1DeviceList);
				site2DeviceList = Arrays.asList(GNOCPropertyReaderSM.siteName2_devices.split(","));
				LOGGER.info("site 2 device list is " + site2DeviceList);
				bucketList = Arrays.asList(GNOCPropertyReaderSM.buckets.split(","));
				LOGGER.info("bucket list is " + bucketList);
				severityNumberList = Arrays.asList(GNOCPropertyReaderSM.severityMapping.split(","));
				severityNumberMapping = new HashMap<String, String>();
				for (String element : severityNumberList) {
					String[] keyValuePair = element.split(":");
					
					severityNumberMapping.put(keyValuePair[0], keyValuePair[1]);
				}
				LOGGER.info("time after reading devices and buckets. " + formatter.format(System.currentTimeMillis()));
				while (true) {
//					gnoc.writeRaiseTickets();
//					gnoc.writeClearTickets();
					
					GNOCRaiseTicketThread raiseTicket = new GNOCRaiseTicketThread();
					//GNOCClearTicketThread clearTicket = new GNOCClearTicketThread();
					LOGGER.info("going to start raise ticket thread.");
					raiseTicket.start();
					raiseTicket.join();
//					LOGGER.info("going to start clear ticket thread");
//					clearTicket.start();
					Thread.sleep(GNOCPropertyReaderSM.sleepDuration);
				}
			} catch (InterruptedException e) {
				LOGGER.error("problem in thread", e);
			}
		} else {
			LOGGER.info("corrupt or improper .properties file.");
		}
	}
	public boolean copyToFTP(String fileToBeUploaded){
		boolean response = false;
		String hostname = GNOCPropertyReaderSM.ftpUrl; //Remote FTP server
		LOGGER.info("hostname is " + hostname);
		String username = GNOCPropertyReaderSM.ftpUsername; //Remote user name
		LOGGER.info("username is " + username);
		String password = GNOCPropertyReaderSM.ftpPassword; //Remote user password
		LOGGER.info("password is " + password);
		File upfile = new File(fileToBeUploaded); //File to upload 
		String upFileName = upfile.getName();
		LOGGER.info("file to be uploaded is " + fileToBeUploaded);
		LOGGER.info("for FTP server the file is " + upFileName);
		String remdir = GNOCPropertyReaderSM.ftpDirectory; //Remote directory for file upload
		LOGGER.info("directory is " + remdir);
		//FtpClient ftp =  FtpClient.create();
		FTPClient ftp = new FTPClient();
		LOGGER.info("created new FTPClient Object " + ftp);
		FileInputStream fis = null;
		try {
			LOGGER.info("inside copyToFTP try block");
			ftp.connect(hostname); //Connect to FTP server
			LOGGER.info("connection established.");
			ftp.login(username, password); //Login
			LOGGER.info("logged in.");
			
			if(ftp.isConnected()){
				LOGGER.info("initial working directory "+ ftp.printWorkingDirectory());
				ftp.changeWorkingDirectory(remdir);
				LOGGER.info("current working directory "+ ftp.printWorkingDirectory());
				fis = new FileInputStream(fileToBeUploaded);
				ftp.storeFile(upFileName, fis);
				OutputStream out = ftp.appendFileStream(upFileName);
				byte c[] = new byte[4096];
				int read = 0;
				while ((read = fis.read(c)) != -1 ) {
					out.write(c, 0, read);
				} //Upload finished
				fis.close();
				out.close();
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
					ftp.disconnect();
				}
			} catch (IOException e1) {
				LOGGER.info("IO Exception in FTP disconnect.");
				//e1.printStackTrace();
			} //Close connection
			LOGGER.info("Error: " + e.getMessage());
			//e.printStackTrace();
		}
		LOGGER.info("returning response for copy " + response);
		return response;
	}
	
	public void writeRaiseTickets(){
		GNOCURLUtility urlUtility = new GNOCURLUtility();
		//urlUtility.setHTTPParameterValues();
		LOGGER.info("time after setting HTTP Parameters "
				+ formatter.format(System.currentTimeMillis()));
		String raiseTicketUrl = GNOCPropertyReaderSM.raiseTicketUrl;
		String raiseAckUrl = GNOCPropertyReaderSM.raiseAckUrl;
		List<Ticket> responseTicketList = null;
		BufferedWriter brSite1 = null;
		BufferedWriter brSite2 = null;
		BufferedWriter currentWriter = null;
		String fileNameSite1 = null;
		String fileNameSite2 = null;
		boolean writeFileSuccess = false;
		try {
			fileNameSite1 = GNOCPropertyReaderSM.configPath
			+ GNOCPropertyReaderSM.siteName1 + "_SORIA.txt";
			
			fileNameSite2 = GNOCPropertyReaderSM.configPath
			+ GNOCPropertyReaderSM.siteName2 + "_SORIA.txt";
			
			brSite1 = new BufferedWriter(new FileWriter(fileNameSite1));
			
			brSite2 = new BufferedWriter(new FileWriter(fileNameSite2));
			
			responseTicketList = urlUtility.parseJSONResponse(raiseTicketUrl);	
			LOGGER.info("iterating ticket list " + responseTicketList);
			if(responseTicketList != null  && !responseTicketList.isEmpty() ){
				for(int i=0 ; i < responseTicketList.size(); i++) {
					Ticket ticket = responseTicketList.get(i);
					String deviceStr = ticket.getIp().split("/")[0];
					LOGGER.info("device obtained from ticket is " + deviceStr);
					if (site1DeviceList.contains(deviceStr))
						currentWriter = brSite1;
					else if (site2DeviceList.contains(deviceStr))
						currentWriter = brSite2;
					else
						continue;
					
					String eventClass = ticket.getItems();
					LOGGER.info("event class is " + eventClass);
					if (!bucketList.contains(eventClass)) {
						LOGGER.debug(eventClass + " not present in the bucket list, ignoring the row");
						continue;
					}
					int alarmId = ticket.getId();
					String groups = ticket.getGroups();
					String alarmCreationTime = ticket.getTimestamp();
					Date date = null;
					String newDateString = null;
					NEW_FORMAT = GNOCPropertyReaderSM.timestamp_format.trim();
					try {
						formatter = new SimpleDateFormat(OLD_FORMAT);
						date = formatter.parse(alarmCreationTime);
						formatter.applyPattern(NEW_FORMAT);
						newDateString = formatter.format(date);
					} catch (ParseException e) {
						LOGGER.info("exception in the parsing of date format obtained from SM." + e.getMessage());
						//e.printStackTrace();
					}
					String severity = ticket.getSeverity();
					String severityValue = severityNumberMapping.get(severity);
					String alarm_text = ticket.getSummary();
					String additional_info = ticket.getSummary();
					
					toWriteRaise = alarmId + "#" + deviceStr + "," + groups + "#"
					+ newDateString + "#" + severityValue
					+ "#" + alarm_text + "#" + additional_info;
					
					currentWriter.write(toWriteRaise);
					currentWriter.newLine();
					currentWriter.flush();
				}
				writeFileSuccess = true;
				LOGGER.info("file is written successfully.");
			}else{
				LOGGER.info("responseTicketList is empty or null.");
			}
			
		} catch (IOException e1) {
			LOGGER.error("IO Exception in bufferedwriter.", e1);
		}  finally {
			try {
				brSite1.close();
			} catch (IOException e) {
				LOGGER.info("error in closing buffered writer 1", e);
			}
			try {
				brSite2.close();
			} catch (IOException e) {
				LOGGER.info("error in closing buffered writer 2", e);
			}
			
			try {
				if (null != currentWriter) {
					currentWriter.close();
				}
			} catch (IOException e) {
				LOGGER.info("error in closing current writer", e);
			}
		}
		//code for FTP File pushing.
		if(writeFileSuccess){
		boolean isCopySuccessFirstFile = gnoc.copyToFTP(fileNameSite1);
		boolean isCopySuccessSecondFile = gnoc.copyToFTP(fileNameSite2);
			if (raiseAckUrl != null && !raiseAckUrl.equals("")) {
				LOGGER.info("raise ack url is defined in the config. going to hit it.");
				gnoc.isRaiseTicketAckSent(isCopySuccessFirstFile,isCopySuccessSecondFile, fileNameSite1, raiseAckUrl);
				
			}else{
				LOGGER.info("raise ack url is not defined in the config.So not sending any ack.");
			}
		}else{
			LOGGER.info("file is not written successfully");
		}
		//gnoc.isRaiseTicketAckSent(isCopySuccessSecondFile, fileNameSite2, raiseAckUrl);
	}
	
	public void writeClearTickets(){
		
		//urlUtility.setHTTPParameterValues();
		LOGGER.info("time after setting HTTP Parameters "
				+ formatter.format(System.currentTimeMillis()));
		String clearTicketUrl = GNOCPropertyReaderSM.clearTicketUrl;
		String clearAckUrl = GNOCPropertyReaderSM.clearAckUrl;
		List<Ticket> responseTicketList = null;
		BufferedWriter brSite1 = null;
		BufferedWriter brSite2 = null;
		BufferedWriter currentWriter = null;
		String fileNameSite1 = null;
		String fileNameSite2 = null;
		try {
			fileNameSite1 = GNOCPropertyReaderSM.configPath
			+ GNOCPropertyReaderSM.siteName1 + "_SORIA_CLEAR.txt";
			
			fileNameSite2 = GNOCPropertyReaderSM.configPath
			+ GNOCPropertyReaderSM.siteName2 + "_SORIA_CLEAR.txt";
			
			brSite1 = new BufferedWriter(new FileWriter(fileNameSite1));
			
			brSite2 = new BufferedWriter(new FileWriter(fileNameSite2));
			
			responseTicketList = urlUtility.parseJSONResponse(clearTicketUrl);	
			LOGGER.info("iterating ticket list " + responseTicketList);
			
			for(int i=0 ; i < responseTicketList.size(); i++) {
				Ticket ticket = responseTicketList.get(i);
				String deviceStr = ticket.getIp().split("/")[0];
				LOGGER.info("device obtained from ticket is " + deviceStr);
				if (site1DeviceList.contains(deviceStr))
					currentWriter = brSite1;
				else if (site2DeviceList.contains(deviceStr))
					currentWriter = brSite2;
				else
					continue;
				
				String eventClass = ticket.getItems();
				LOGGER.info("event class is " + eventClass);
				if (!bucketList.contains(eventClass)) {
					LOGGER.debug(eventClass + " not present in the bucket list, ignoring the row");
					continue;
				}
				int alarmId = ticket.getId();
				String groups = ticket.getGroups();
				Date alarmCreationTime = ticket.getCreate_ts();
				long alarmCreationTimeInMilliSeconds = alarmCreationTime.getTime();
				String severity = ticket.getSeverity();
				String severityValue = severityNumberMapping.get(severity);
				String alarm_text = ticket.getSummary();
				String additional_info = ticket.getSummary();
				
				toWriteClear = alarmId + "#" + deviceStr + "," + groups + "#"
				+ alarmCreationTimeInMilliSeconds + "#" + severityValue
				+ "#" + alarm_text + "#" + additional_info;
				
				currentWriter.write(toWriteClear);
				currentWriter.newLine();
				currentWriter.flush();
			}
			
		} catch (IOException e1) {
			LOGGER.error("IO Exception in bufferedwriter.", e1);
		}  finally {
			try {
				brSite1.close();
			} catch (IOException e) {
				LOGGER.info("error in closing buffered writer 1", e);
			}
			try {
				brSite2.close();
			} catch (IOException e) {
				LOGGER.info("error in closing buffered writer 2", e);
			}
			
			try {
				if (null != currentWriter) {
					currentWriter.close();
				}
			} catch (IOException e) {
				LOGGER.info("error in closing current writer", e);
			}
		}
		//code for FTP File pushing.
		boolean isCopySuccessFirstFile = gnoc.copyToFTP(fileNameSite1);
		boolean isCopySuccessSecondFile = gnoc.copyToFTP(fileNameSite2);
		gnoc.isClearTicketAckSent(isCopySuccessFirstFile, fileNameSite1, clearAckUrl);
		gnoc.isClearTicketAckSent(isCopySuccessSecondFile, fileNameSite2, clearAckUrl);
	}
	
	public void isRaiseTicketAckSent(boolean isCopySuccessFirstFile,boolean isCopySuccessSecondFile, String fileName,String raiseAckUrl){
		if(isCopySuccessFirstFile && isCopySuccessSecondFile){
			LOGGER.info(fileName + " pushed to FTP server successfully.");
			boolean raiseAckResponse = false;
			String ackResponse = null;
			int i = 0;
			while(i < 10){
				if(raiseAckResponse){
					LOGGER.info("ack sent successfully for " + fileName +" " + raiseAckResponse);
					break;
				}else{
					 ackResponse = urlUtility.getResponseFromURL(raiseAckUrl);
					 LOGGER.info("ack response from raise ack is " + ackResponse);
					 if(ackResponse != null && !ackResponse.isEmpty()){
					raiseAckResponse = Boolean.parseBoolean(ackResponse.trim());
					LOGGER.info("raise ack response " + fileName +" "+ raiseAckResponse);
					 }else{
						 LOGGER.info("no response from the URL. May be server is down");
					 }
					i++;
					if(i == 10){
						LOGGER.info("tried 10 times..ack send failed for raise tickets..giving up...");
					}
				}
			}
		}
	}
	
	public void isClearTicketAckSent(boolean isCopySuccess, String fileName,String clearAckUrl){
		if(isCopySuccess){
			LOGGER.info(fileName + " pushed to FTP server successfully.");
			boolean clearAckResponse = false;
			String ackResponse = null;
			int i = 0;
			while(i < 10){
				if(clearAckResponse){
					LOGGER.info("ack sent successfully for " + fileName +" " + clearAckResponse);
					break;
				}else{
					 ackResponse = urlUtility.getResponseFromURL(clearAckUrl);
					clearAckResponse = Boolean.parseBoolean(ackResponse.trim());
					LOGGER.info("clear ack response " + fileName +" "+ clearAckResponse);
					i++;
					if(i == 10){
						LOGGER.info("tried 10 times..ack send failed for clear tickets..giving up...");
					}
				}
			}
		}
	}
}








							

						
							
								
						


						


	