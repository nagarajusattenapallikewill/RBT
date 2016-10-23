package com.onmobile.apps.ringbacktones.promotions;

import java.io.File;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

class GetCDRRecords extends Thread{
	private static Logger logger = Logger.getLogger(GetCDRRecords.class);

	private Hashtable<String, Long> nms = null;
	private Vector<String> osc = null;
	private File file = null;
	private int nmsCdrFactor = 1;
	private boolean isNMSCDR = false;
	public static Vector<String> v = new Vector<String>();
	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	
	GetCDRRecords(File file, Hashtable<String, Long> nms, int factor)
	{
		this.nms = nms;
		this.file = file;
		nmsCdrFactor = factor;
		this.isNMSCDR = true;
	}

	GetCDRRecords(File file, Vector<String> osc)
	{
		this.osc = osc;
		this.file = file;
	}

	public void run()
	{
		if(isNMSCDR)
			getNMSRecords();
		else
			getOSCRecords();
		
		return;
	}
	
	private void getOSCRecords()
	{
		Parameters param = RBTViralMain.m_rbtParamCacheManager.getParameter(RBTViralMain.m_paramType, "OSCCDR_LINE_SIZE", "130");
		long cdrSize = 130; 
		try{
			cdrSize = Long.parseLong(param.getValue());
		}
		catch(Exception e){
			cdrSize = 130;
		}
		
		logger.info("Reading " +RBTViralMain.m_linesToRead + " from "+file.getName());
		populateCDRFile(cdrSize, RBTViralMain.m_linesToRead);
	}
	
	private void getNMSRecords()
	{
        Parameters param = RBTViralMain.m_rbtParamCacheManager.getParameter(RBTViralMain.m_paramType, "NMS_LINE_SIZE", "175");
		long cdrSize = 130; 
		try{
			cdrSize = Long.parseLong(param.getValue());
		}
		catch(Exception e){
			cdrSize = 130;
		}

		long linesToRead = RBTViralMain.m_linesToRead;
		if(nmsCdrFactor > 1)
			linesToRead = nmsCdrFactor * RBTViralMain.m_linesToRead;
		
		logger.info("Reading " +linesToRead + " from "+file.getName());
		
		populateCDRFile(cdrSize, linesToRead);
	}
    private void populateCDRFile(long cdrLineSize, long linesToRead)
    {
        try
        {
        	long lastRead = 0;
            RandomAccessFile accessFile = new RandomAccessFile(file, "r");
            long cdrSize = accessFile.length();
            logger.info("Initial CDR File Size " + cdrSize);

            long lines = cdrSize / cdrLineSize;
            if (lines <= linesToRead)
                accessFile.seek(0);
            else
                accessFile.seek(cdrSize - (linesToRead * cdrLineSize));
     
            String str = null;
            int linesRead = 0;
            while (linesRead < linesToRead
                    && (str = accessFile.readLine()) != null)
            {
            	try
            	{
					String caller = null;
	            	if(isNMSCDR)
	            	{
	            		String[] tokens = str.split(",");
	            		if(tokens.length < 20)
	            			continue;
	            		caller = RBTViralMain.subID(tokens[3]);
	
	            		if(caller == null || caller.length() < 7)
	    					continue;
	            		
	            		String called = RBTViralMain.subID(tokens[4]);
	            		String time = null;
						if(tokens[8].indexOf("-") > -1)
							time = tokens[8].substring(0, tokens[8].indexOf("-"));
						else
							continue;
	            		long dur = Long.parseLong(tokens[9].substring(0,tokens[9].indexOf(".")));
	            		try
	            		{
		            		nms.put(caller+"|"+called+"|"+time, dur);
	            		}
	            		catch(Throwable t)
	            		{
	            			logger.error("", t);
	            		}
	            	}
	            	else
	            	{
	            		String[] tokens = str.split(",");
	            		if(tokens.length < 7)
	            			continue;
	            		caller = RBTViralMain.subID(tokens[0]);
	            		String called = RBTViralMain.subID(tokens[1]);
	            		String time = tokens[3].substring(0, tokens[3].indexOf("-"));
	            		long cdrTime = m_sdf.parse(time).getTime();
	            		if(cdrTime < RBTViralMain.m_oldestFile){
//	            			logger.info("Line "+str + " is past the oldestTime for processing");
	            			continue;
	            		}
	            		//added by Sreekar to skip RRBT records
						if (RBTViralMain._playerMultipleCallLegPolicy
								&& RBTViralMain._rrbtCalledPartyPrefix != null
								&& called.startsWith(RBTViralMain._rrbtCalledPartyPrefix))
							continue;
	            		// end of RRBT changes
	            		String song = tokens[6];
	            		String[] s = song.split("-\\[");
	            		song="";
	            		for(int i=0; i<s.length;i++)
	            		{
	            			if(s[i].indexOf(";") > -1)
	            			{
	            				String g = s[i].substring(s[i].indexOf(";")+1);
	            				if(g.length() >1)
	            					song +="|"+g.replaceAll(".wav", "");
	            			}
	            			else
	            				song +=s[i].replaceAll(".wav", "");
	            		}

	            		if(caller == null || caller.length() < 7)
	    					continue;
	            		try
	            		{
	            			if(!v.contains(caller))
	            			{
	            				osc.add(caller+";"+called+";"+time+";"+song);
	            				v.add(caller);
	            			}
	            		}
	            		catch(Throwable t)
	            		{
	            			logger.error("", t);
	            		}
	            	}
	
	                long size = accessFile.length();
	                long diff = cdrSize - size;
	
	                if (diff > cdrLineSize)
	                {
	                    cdrSize = size;
	                    lastRead = lastRead + diff;
						logger.info("RBTViral::Seeking "+ lastRead +" in file " + file.getName());
	                    accessFile.seek(lastRead);
	                }
	
	                linesRead++;
            	}
            	catch(Throwable t)
            	{
            		logger.error("", t);
            	}
            }

            accessFile.close();
            logger.info("RBTViral::Finished processing file " + file.getName());
        }
        catch (Exception e)
        {
        	logger.error("", e);
        }
    }


    
}
