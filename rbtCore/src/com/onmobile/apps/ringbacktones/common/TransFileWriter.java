/*
 * Created on Nov 21, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * @author mohsin
 *  
 */
public class TransFileWriter
{
	private static Logger logger = Logger.getLogger(TransFileWriter.class);
	
	private String m_transDir = "./Trans";
	private String m_trans_file = null;
	private String m_file_prefix = null;
	private BufferedWriter m_transbufferWriter = null;
	private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");
	private long m_nextWakeUpTimeForTrans = 0;
	private ArrayList<String> m_strHeaders = new ArrayList<String> ();
	private static String _class = "TransFileWriter";

	public TransFileWriter(String dir, String filePrefix, ArrayList<String> strHeaders){
		
		m_transDir = dir;
		m_file_prefix = filePrefix;
		m_nextWakeUpTimeForTrans = getnexttime(0);
		
		File trans = new File(m_transDir);
		if(!trans.exists())
			trans.mkdirs();

		m_strHeaders = strHeaders;
		openTrans();
	}

	private TransFileWriter()
	{
	
	}
	
	public void openTrans()
	{
		try
		{
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new java.util.Date(System.currentTimeMillis()));
			String date = m_format.format(calendar.getTime());
			String fileName = null;
			if(m_trans_file == null || !new File(m_trans_file+".csv").isFile())
			{
				fileName =  m_transDir + File.separator  +m_file_prefix + "_" + date ;
				m_trans_file = fileName;
				boolean newFile = false;
				if(!(new File(fileName + ".csv").exists()))
				{
					newFile = true;
				}
				logger.info("*** RBT::writing Transaction file (append) : " +m_trans_file);
				if(m_transbufferWriter != null)
				{
					synchronized(m_transbufferWriter)
					{
						try
						{
							m_transbufferWriter.close();
						}
						catch(Exception e)
						{
						
						}
					}
				}
				if(m_transbufferWriter == null)
					m_transbufferWriter = new BufferedWriter(new FileWriter(fileName + ".csv", true));
				else
				{
					synchronized(m_transbufferWriter)
					{
						m_transbufferWriter = new BufferedWriter(new FileWriter(fileName + ".csv", true));
					}
				}
				synchronized(m_transbufferWriter)
				{
					if(newFile)
					{		
						String str = "";
						
						for(int i=0; i<m_strHeaders.size(); i++)
							str += "," + m_strHeaders.get(i);
	
						if(str.length() > 1)
							m_transbufferWriter.write(str.substring(1));
					}
					m_transbufferWriter.flush();
				}
			}
			
			logger.info("*** RBT::writing Transaction of file with name = "+fileName + ".csv");
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}

    public void closeTrans()
	{
		synchronized(m_transbufferWriter)
		{
			if(m_transbufferWriter != null)
			{
				try
				{
					logger.info("*** RBT::closing Trans files file ");
					m_transbufferWriter.flush();
				}
				catch(Exception e)
				{
					logger.error("", e);
				}  
			}

			try
			{
				logger.info("*** RBT::checking to create new Trans " + new Date (System.currentTimeMillis() + 10000) + " wakeUp " + new Date (m_nextWakeUpTimeForTrans));
				if( (System.currentTimeMillis()  + 10000) >= m_nextWakeUpTimeForTrans)
				{
					m_trans_file = null;
					m_nextWakeUpTimeForTrans = getnexttime(0);
					openTrans();
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
	}

					

	public void writeTrans(HashMap<String,String> map){
    	
		synchronized(m_transbufferWriter)
		{
			try
			{
				m_transbufferWriter.newLine();
				String str = "";
				for(int i=0; i<m_strHeaders.size(); i++)
				{
					str += "," + map.get(m_strHeaders.get(i));
				}
				if(str.length() > 1)
				{
					m_transbufferWriter.write(str.substring(1));
					m_transbufferWriter.flush();
				}
			}
			catch(Exception e)
			{
				logger.error("", e);  
			}
		}

		closeTrans();
	}

    	public long getnexttime(int hour)
        {
            Calendar now = Calendar.getInstance();
            now.set(Calendar.HOUR_OF_DAY, hour);
            now.set(Calendar.MINUTE, 0);
            now.set(Calendar.SECOND, 0);

            long nexttime = now.getTime().getTime();
            if (nexttime < System.currentTimeMillis())
            {
                nexttime = nexttime + (24 * 3600 * 1000);
            }
            return nexttime;
        }

}
