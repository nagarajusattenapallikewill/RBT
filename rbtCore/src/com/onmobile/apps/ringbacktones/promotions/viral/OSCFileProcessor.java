package com.onmobile.apps.ringbacktones.promotions.viral;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;
import java.io.File;

import org.apache.log4j.Logger;

/**
 * @author sridhar.sindiri
 *
 */
public class OSCFileProcessor implements Runnable {
	private static Logger logger = Logger.getLogger(OSCFileProcessor.class);

	private FileInfo fileInfo = null;
	private OSCnNMSLogExtracter extracter = null;
	public static Set<String> processedCallerIds = new HashSet<String>();

	public OSCFileProcessor(FileInfo fileInfo, OSCnNMSLogExtracter logFileExtracter) {
		super();
		this.fileInfo = fileInfo;
		this.extracter = logFileExtracter;
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
					String data = readLine(randomAccessFile, loc, fileLength);
					if(data == null)
					{
						break;
					}

					loc += data.length();// Including \n
					
//					logger.info("OSC File Read fileName:" + fileInfo.getFileName() + ", location:" + loc + ", TotalFileSize:" + fileLength);
					
					data = stripLineReturns(data);
					String[] tokens = data.split(",");
					if(tokens.length < 7)
						continue;
					
					fileInfo.setLocation(loc);

					String caller = RBTViralTestingClient.getInstance().subID(tokens[0]);
					if(caller == null || caller.length() < 7 || !(RBTViralTestingClient.configNumbersList.contains(tokens[0]))) {
						continue;
					}

					String called = RBTViralTestingClient.getInstance().subID(tokens[1]);
					if (RBTViralTestingClient._playerMultipleCallLegPolicy
							&& RBTViralTestingClient._rrbtCalledPartyPrefix != null
							&& called.startsWith(RBTViralTestingClient._rrbtCalledPartyPrefix)) {
						continue;
					}
					String time = tokens[3].substring(0, tokens[3].indexOf("-"));

					String song = tokens[6];
					String[] s = song.split("-\\[");
					song="";
					for(int i=0; i<s.length;i++)
					{
						if(s[i].indexOf(";") > -1) {
							String g = s[i].substring(s[i].indexOf(";")+1);
							if(g.length() >1)
								song +="|"+g.replaceAll(".wav", "");
						} else
							song +=s[i].replaceAll(".wav", "");
					}

					if(!processedCallerIds.contains(caller)) {
						StringBuilder sb = new StringBuilder();
						sb.append(caller).append(";").append(called).append(";").append(time).append(";").append(song);
						processedCallerIds.add(caller);
						writeToFile(sb.toString());
						sb = null;
					}
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

	private void writeToFile(String s) {
		try {
			if (extracter != null) {
				extracter.writeRecordFromOSCFile(s);
			}
		} catch (IOException e) {
			logger.error("", e);
		}
	}

}
