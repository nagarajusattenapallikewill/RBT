package com.onmobile.apps.ringbacktones.promotions.viral;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.apache.log4j.Logger;

/**
 * @author sridhar.sindiri
 *
 */
public class NMSLogFile implements TPLogFile 
{
	private static Logger logger = Logger.getLogger(NMSLogFile.class);
	
	private File file = null;
	private long linesToRead = 0;
	private int nmsCdrFactor = 0;
	private OSCnNMSLogExtracter extracter = null;

	public NMSLogFile(File file, long linesToRead, int nmsCdrFactor, OSCnNMSLogExtracter extracter) 	{
		this.file = file;
		this.linesToRead = linesToRead;
		this.nmsCdrFactor = nmsCdrFactor;
		this.extracter = extracter;
	}

	public void getLatestRecords() {
		String cdrLineSizeStr = RBTViralConfigManager.getInstance().getParameter("NMS_LINE_SIZE");

		long cdrLineSize = 130; 
		try {
			cdrLineSize = Long.parseLong(cdrLineSizeStr);
		} catch(Exception e) {
			cdrLineSize = 130;
		}

		if(nmsCdrFactor > 1)
			linesToRead = nmsCdrFactor * linesToRead;

		logger.info("Reading " +linesToRead + " from "+file.getName());

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
					String caller = null;
					String[] tokens = str.split(",");
					if(tokens.length < 20)
						continue;

					caller = RBTViralClient.getInstance().subID(tokens[3]);
					if(caller == null || caller.length() < 7)
						continue;

					String called = RBTViralClient.getInstance().subID(tokens[4]);
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
				extracter.writeRecordFromNMSFile(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
