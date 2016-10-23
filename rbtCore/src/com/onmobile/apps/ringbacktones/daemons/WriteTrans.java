/*
 * Created on Nov 21, 2004
 *  
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author mohsin
 *  
 */
public class WriteTrans
{
	private static Logger logger = Logger.getLogger(WriteTrans.class);
	
	private String m_transDir = "./Trans";
	private String m_trans_file = null;
	private String m_file_prefix = null;
	private BufferedWriter m_transbufferWriter = null;
	private SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");
	private SimpleDateFormat m_TransFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private long m_nextWakeUpTimeForTrans = 0;
	
	public WriteTrans(String dir, String filePrefix){
		
		m_transDir = dir;
		m_file_prefix = filePrefix;
		m_nextWakeUpTimeForTrans = getnexttime(0);
		
		File trans = new File(m_transDir + "/" + "Trans");
		if(!trans.exists())
			trans.mkdirs();

	}

	private WriteTrans()
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
			fileName = null;
			if(m_trans_file == null || !new File(m_trans_file+".LOG").isFile())
			{
				fileName =  m_transDir + File.separator  +m_file_prefix + "_" + date ;
				m_trans_file = fileName;
				boolean newFile = false;
				if(!(new File(fileName + ".LOG").exists()))
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
					m_transbufferWriter = new BufferedWriter(new FileWriter(fileName + ".LOG", true));
				else
				{
					synchronized(m_transbufferWriter)
					{
						m_transbufferWriter = new BufferedWriter(new FileWriter(fileName + ".LOG", true));
					}
				}
				synchronized(m_transbufferWriter)
				{
					if(newFile)
					{		
						m_transbufferWriter.write("REQUEST TYPE|REQUEST URL|REPONSE CODE|RESPONSE STRING|RESPONSE DELAY|REQUEST TIME");
					}
					m_transbufferWriter.flush();
				}
			}
			
			logger.info("*** RBT::writing Transaction of file with name = "+fileName + ".LOG");
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
					e.printStackTrace();
				}  
			}

			try
			{
				logger.info("*** RBT::checking to create new Trans " + new Date (System.currentTimeMillis() + 10000) + " wakeUp " + new Date (m_nextWakeUpTimeForTrans));
				if( (System.currentTimeMillis()  + 10000) >= m_nextWakeUpTimeForTrans)
				{
					m_trans_file = null;
					m_nextWakeUpTimeForTrans = getnexttime(0);
				}
			}
			catch(Exception e)
			{
				logger.error("", e);  
				e.printStackTrace();
			}
		}
	}

					

	public void writeTrans(String reqType, String url,
			int statusCode, String response, long delay){
    	
		synchronized(m_transbufferWriter)
		{
			try
			{
				m_transbufferWriter.newLine();
				m_transbufferWriter.write(reqType);
				m_transbufferWriter.write("|"+url);
				m_transbufferWriter.write("|"+statusCode);
				m_transbufferWriter.write("|"+response);
				m_transbufferWriter.write("|"+delay);
				m_transbufferWriter.write("|"+m_TransFormat.format(new Date(System.currentTimeMillis())));
				m_transbufferWriter.flush();
			}
			catch(Exception e)
			{
				logger.error("", e);  
			}
		}
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
