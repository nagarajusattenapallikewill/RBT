package com.onmobile.apps.ringbacktones.promotions.viral;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * @author sridhar.sindiri
 *
 */
public class OSCLogFile implements TPLogFile {
	private static Logger logger = Logger.getLogger(OSCLogFile.class);
	
	private File file = null;
	private long linesToRead = 0;
	private OSCnNMSLogExtracter extracter = null;
	private long processRecordsAfterTime = 0;
	
	public static Set<String> processedCallerIds = new HashSet<String>();

	public OSCLogFile(File file, long linesToRead, OSCnNMSLogExtracter extracter, long processRecordsAfterTime) {
		this.file = file;
		this.linesToRead = linesToRead;
		this.extracter = extracter;
		this.processRecordsAfterTime = processRecordsAfterTime;
	}

	public void getLatestRecords() {
		String cdrLineSizeStr = RBTViralConfigManager.getInstance().getParameter("OSCCDR_LINE_SIZE");

		long cdrLineSize = 130; 
		try {
			cdrLineSize = Long.parseLong(cdrLineSizeStr);
		} catch(NumberFormatException nfe) {
			cdrLineSize = 130;
		}
		logger.info("Reading " +RBTViralClient.m_linesToRead + " from "+file.getName());

		long lastRead = 0;
		RandomAccessFile accessFile = null;
		try {
			accessFile = new RandomAccessFile(file, "r");
			long cdrSize = accessFile.length();
			logger.info("Initial CDR File Size " + cdrSize);

			long lines = cdrSize / cdrLineSize;
			if (lines <= linesToRead)
				accessFile.seek(0);
			else
				accessFile.seek(cdrSize - (linesToRead * cdrLineSize));

			String str = null;
			int linesRead = 0;
			
			while (linesRead < linesToRead && (str = accessFile.readLine()) != null) {
				try {
	        		String[] tokens = str.split(",");
	        		if(tokens.length < 7)
	        			continue;
	        		
	        		String caller = RBTViralClient.getInstance().subID(tokens[0]);
	        		if(caller == null || caller.length() < 7) {
						continue;
	        		}

	        		String called = RBTViralClient.getInstance().subID(tokens[1]);
	        		//added by Sreekar to skip RRBT records
					if (RBTViralClient._playerMultipleCallLegPolicy
							&& RBTViralClient._rrbtCalledPartyPrefix != null
							&& called.startsWith(RBTViralClient._rrbtCalledPartyPrefix)) {
						continue;
					}
					
	        		String time = tokens[3].substring(0, tokens[3].indexOf("-"));
	        		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	        		long cdrTime = sdf.parse(time).getTime();
	        		
	        		if(cdrTime < processRecordsAfterTime){
	        			continue;
	        		}
	        		// end of RRBT changes

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
	
					long size = accessFile.length();
					long diff = cdrSize - size;
					if (diff > cdrLineSize) {
						cdrSize = size;
						lastRead = lastRead + diff;
						logger.info("RBTViral::Seeking "+ lastRead +" in file " + file.getName());
						accessFile.seek(lastRead);
					}
					linesRead++;
				} catch(Throwable t) {
					logger.error("", t);
				}
			}
			accessFile.close();
		} catch(Throwable t) {

		} finally {
			if(null != accessFile) {
					try {
						accessFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		}
		logger.info("RBTViral::Finished processing file " + file.getName());
	}
	
	private void writeToFile(String s) {
		try {
			if (extracter != null) {
				extracter.writeRecordFromOSCFile(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
 