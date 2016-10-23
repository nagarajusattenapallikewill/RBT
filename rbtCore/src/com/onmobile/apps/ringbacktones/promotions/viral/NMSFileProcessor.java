package com.onmobile.apps.ringbacktones.promotions.viral;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

/**
 * @author sridhar.sindiri
 *
 */
public class NMSFileProcessor implements Runnable {
	private static Logger logger = Logger.getLogger(NMSFileProcessor.class);

	private FileInfo fileInfo = null;
	private OSCnNMSLogExtracter extracter = null;
	
	public NMSFileProcessor(FileInfo fileInfo, OSCnNMSLogExtracter logFileExtracter) {
		super();
		this.fileInfo = fileInfo;
		this.extracter = logFileExtracter;
	}
	private String readLine(RandomAccessFile randomAccessFile, long loc,
			long fileLength) throws IOException {
		StringBuffer buff = new StringBuffer();
		
		for(;true;loc++)
		{
			int data = randomAccessFile.read();
			if(loc>=fileLength)
			{
				return null;
			}
			if(data ==-1  )
			{
				break;
			}
			buff.append((char)data);
			if(data == '\n')
			{
				break;
			}
		}
		String result =  buff.toString();
		
		return result;
	}
	public void run() {
		long fileLength = 0;
		try {
			RandomAccessFile randomAccessFile =null;
			try {
				String cdrFile =  fileInfo.getFileName();
				fileLength = new File(cdrFile).length();
				randomAccessFile = new RandomAccessFile(cdrFile, "r");
				long loc = fileInfo.getLocation();
				randomAccessFile.seek(loc);
				while(true)
				{
					String data = readLine(randomAccessFile, loc, fileLength);;
					if(data == null)
					{
						break;
					}
					loc += data.length();// Including \n
					
//					logger.info("Read fileName:" + fileInfo.getFileName() + ", location:" + loc + ", TotalFileSize:" + fileLength);
					
					String caller = null;
					data = stripLineReturns(data);
					String[] tokens = data.split(",");
					if(tokens.length < 20)
						continue;
					
					
					fileInfo.setLocation(loc);

					caller = RBTViralTestingClient.getInstance().subID(tokens[3]);
					if(caller == null || caller.length() < 7 || !(RBTViralTestingClient.configNumbersList.contains(caller)))
						continue;

					String called = RBTViralTestingClient.getInstance().subID(tokens[4]);
					String time = null;
					if(tokens[8].indexOf("-") > -1) 
						time = tokens[8].substring(0, tokens[8].indexOf("-"));
					else 
						continue;

					long dur = Long.parseLong(tokens[9].substring(0,tokens[9].indexOf(".")));

					StringBuilder sb = new StringBuilder();
					sb.append(caller).append("|").append(called).append("|").append(time).append("|").append(dur);
					writeToFile(sb.toString());
					sb = null;
				}
				
			} catch (Exception e) {
				logger.error("", e);
			}
			finally
			{
				try {
					randomAccessFile.close();
				} catch (Exception e) {
					// ignore
				}
				fileInfo.setFileSize(fileLength);
				fileInfo.save();
			}
		} catch (Throwable e) {
			logger.error("", e);
		}
		
	}
	private String stripLineReturns(String result) {
		if(result.length()!=0 && result.charAt(result.length()-1) == '\r')
		{
			result = result.substring(0,result.length()-1);
		}
		if(result.length()!=0 && result.charAt(result.length()-1) == '\n')
		{
			result = result.substring(0,result.length()-1);
		}
		return result;
	}
	private void writeToFile(String s) {
		try {
			if (extracter != null) {
				extracter.writeRecordFromNMSFile(s);
			}
		} catch (IOException e) {
			logger.error("", e);
		}
	}

}
