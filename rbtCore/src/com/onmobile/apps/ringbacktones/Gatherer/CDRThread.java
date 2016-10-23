package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;


public class CDRThread implements Runnable{
	private static Logger logger = Logger.getLogger(CDRThread.class);
	
	static boolean POINT_CODE_STATUS = false;
	static Date m_start_time;
	static Date m_end_time;
	static String m_processing_interval = "HOURLY";
	static String m_countryCodePrefix=null;
	static HashMap m_prefixes = new HashMap();
	static RBTGatherer m_parentThread = null;
	
	private static final int FIELD_INDEX_CALLER = 5;
	private static final int FIELD_INDEX_CALLED = 4;
	private static final int FIELD_INDEX_TIME = 8;
	private static final String PREFIX_IN_CIRCLE = "CIRCLE";
	private static final String PREFIX_OUT_CIRCLE = "OPERATOR";
	private static final String PREFIX_MOBILE = "MOBILE";
	
	ArrayList arrFileList=null;
	int ThreadCount=0;
	HashMap m_hmTime_Map_local = null;
	HashMap m_hmTime_pointcodeMap_local = null;
	
	private static SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
	
	
	public CDRThread(ArrayList filelist,int count)
	{

		this.arrFileList=filelist;
		this.ThreadCount=count;
	}
	public void run(){
		logger.info("entering...");
		this.collectCDRFiles();
		logger.info("exiting...");
	}
	private static HashMap createHashMap()
	{
		HashMap hm = new HashMap();
		Date tempdate = m_start_time;
		while (tempdate.before(m_end_time))
		{
			hm.put(sdf.format(tempdate),new CDRData());
			tempdate = Tools.getNextInterval(tempdate, m_processing_interval);
		}

		return hm;
	}
	public void collectCDRFiles()
	{
		logger.info("entering...");

		if(POINT_CODE_STATUS==true)//point code-Hashmap; date:Report
			m_hmTime_pointcodeMap_local=new HashMap();
		else
			m_hmTime_Map_local = createHashMap();


//		Iterator i = m_servers.iterator();

		Tools.addToLogFile("CDR collections started...");
		logger.info("CDR collections started...");

		int threadCount = (int)this.ThreadCount;
//		ip = ip.replaceAll(":","$");
//		String cdrlocation = ip.trim(); 
//		String cdrlocation = locateCDRFiles(ip.trim());
//		Tools.logDetail(_class, "CollectCDRFiles",
//		"locating CDRFiles on thread  no."
//		+ threadCount);
		logger.info("threadNo.==" + threadCount);
		ArrayList fileList=(ArrayList)this.arrFileList;
//		File cdrdir = new File(cdrlocation);

		if (fileList!=null && fileList.size()>0)
		{
			parse(fileList);
		}

		else{
			logger.info("couldn't locate cdr dir: because master FileList is euther null or with size==0"
			);
			Tools.addToLogFile("couldn't locate cdr dir: because master FileList is either null or with size==0");
			logger.info("couldn't locate cdr dir: because master FileList is either null or with size==0");
		}



	}
	public void parse(ArrayList Cdrdir)
	{
		Date cdr_date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");//20080615135922
		//(SubscriberStatus[]) objList
		//.toArray(new SubscriberStatus[0])
		File[] CdrFile =(File[])(Cdrdir.toArray(new File[0]));
//		getFiles(Cdrdir);
		int threadCount = (int)this.ThreadCount;
		if (CdrFile == null || CdrFile.length <= 0)
		{
			logger.info("No valid files found at filePath for thread no. "
					+ threadCount);
			Tools.addToLogFile("No valid files found at filePath for thread no. "
					+ threadCount);
			return;
		}
		logger.info("Got files to be proceessed.@ location :"
				+ Cdrdir);

		logger.info("Got files to be proceessed. No of file "
				+ CdrFile.length);

		Tools.addToLogFile("Cdr Files found ");
		Tools.addToLogFile("Processing Started");
		String str = null;
		LineNumberReader fin = null;
		for (int i = 0; i < CdrFile.length; i++)
		{
//			logger.info("Analyzing Files "+i);
			try
			{
				fin = new LineNumberReader(new FileReader(CdrFile[i]));
				 //logger.info("started to read file "+CdrFile[i].getAbsolutePath());
				//logger.info("starting time "+Calendar.getInstance().getTime().toString());
				while ((str = fin.readLine()) != null)
				{
					try
					{
					//	logger.info("read new line as "+str);
						StringTokenizer tokens = new StringTokenizer(str, ",",
								true);
						int j = 0;

						while (j < FIELD_INDEX_CALLED)
						{
							j++;
							if (tokens.nextToken().equals(","))
								continue;
							tokens.nextToken();
						}
						String called_num = tokens.nextToken();
						if (called_num.equals(","))
						{
							j++;
						}

						while (j < FIELD_INDEX_CALLER)
						{
							j++;
							if (tokens.nextToken().equals(","))
								continue;
							tokens.nextToken();
						}
						String subscriber_string = tokens.nextToken();
						if (subscriber_string.equals(","))
						{
						//									logger.info("Subscriber null in cdr file "+CdrFile[i]);
							j++;
							//continue;
						}

						while (j < FIELD_INDEX_TIME)
						{
							j++;
							if (tokens.nextToken().equals(","))
								continue;
							tokens.nextToken();
						}
						String TmpString = tokens.nextToken();

						if (TmpString.equals(","))
						{
							//							logger.info("Date null in cdr file "+CdrFile[i]);
							continue;
						}

						tokens.nextToken();
						String temp = tokens.nextToken();

						if (temp.equals(","))
						{
							continue;
						}
						double dur;
						try {
							dur = Double.parseDouble(temp);
						} catch (RuntimeException e) {
							dur=0.0;
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						cdr_date = Tools.getdate(TmpString.substring(0, 14),
						"yyyyMMddHHmmss");
//						logger.info("Date  in cdr file "+CdrFile[i]+" is "+cdr_date);
						tokens.nextToken();
						String hangUp = tokens.nextToken();
						if (hangUp.equals(","))
						{
							continue;
						}

						tokens.nextToken();
						temp = tokens.nextToken();
						if (temp.equals(","))
						{
							continue;
						}
						double tot_dur;
						try {
							tot_dur = Double.parseDouble(temp);
						} catch (RuntimeException e) {
							tot_dur=0.0;
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						int m=0; 
						temp=tokens.nextToken();

						while(m<5){
							m++;
							if ((temp=tokens.nextToken()).equals(","))
							{
								continue;
							}
							temp=tokens.nextToken(); 	            
						}
						temp = tokens.nextToken();
						if((temp.equals(",")))
							continue;
						StringTokenizer tokensPointCode = new StringTokenizer(temp, ":");
						String strpoint_code=tokensPointCode.nextToken();
						Integer point_code=Integer.valueOf(strpoint_code);
						HashMap m_hmTimeHashMap=null;

						if (POINT_CODE_STATUS==true) {
							if (m_hmTime_pointcodeMap_local.containsKey(point_code)) {
								m_hmTimeHashMap=(HashMap)m_hmTime_pointcodeMap_local.get(point_code);
	//							logger.info("for this line point code hashmap already exist");
							}
							else{
								addtoPointCodeHashMap(point_code);
								m_hmTimeHashMap=(HashMap)m_hmTime_pointcodeMap_local.get(point_code);
		//						logger.info("for this line point code hashmap dont exist creating hashmap for this point code");
							}
						} 
						else{
							m_hmTimeHashMap=m_hmTime_Map_local;
						}


						if (cdr_date.equals(m_start_time)
								|| cdr_date.equals(m_end_time)
								|| (cdr_date.after(m_start_time) && cdr_date
										.before(m_end_time)))
						{
							//logger.info("Date  in cdr file "+cdr_date+" is valid");
							cdr_date = Tools
							.getFormattedCDRDate(TmpString,
									m_processing_interval);
							if (m_hmTimeHashMap.containsKey(sdf.format(cdr_date)))
							{
								CDRData ch = (CDRData) m_hmTimeHashMap.get(sdf
										.format(cdr_date));
								ch.call_volume += dur;
								ch.number_of_calls++;
								ch.total_duration += tot_dur;
								if (hangUp.equalsIgnoreCase("NU"))
									ch.NU_count++;
								else if (hangUp.equalsIgnoreCase("NS"))
									ch.NS_count++;
								else if (hangUp.equalsIgnoreCase("UU"))
									ch.UU_count++;
								else if (hangUp.equalsIgnoreCase("US"))
									ch.US_count++;
								addSubCount(ch, subscriber_string);
							}
						}
					}
					catch (Exception e)
					{
						logger.info("line checked " + str);
						logger.error("", e);
						e.printStackTrace();
					}
				}
				 //logger.info("end time "+Calendar.getInstance().getTime().toString());
				fin.close();
			}
			catch (Exception e)
			{
				logger.info("file parsed "
						+ CdrFile[i].getAbsolutePath());
				logger.error("", e);
				e.printStackTrace();

				continue;
			}

			finally
			{
				try
				{
					fin.close();
				}
				catch (Exception e)
				{

				}
			}
		}

		//	Tools.addToLogFile("Cdr processing for " + Cdrdir.getName() + " ended");
	}
	private void addtoPointCodeHashMap(Integer pointcode)
	{
		HashMap m_hmTime_Map_local=createHashMap();
		m_hmTime_pointcodeMap_local.put(pointcode,m_hmTime_Map_local);
	}
	public String subID(String subscriberID)
	{
		
		if (m_countryCodePrefix != null)
			m_countryCodePrefix = m_countryCodePrefix.replace(';', ',');
		if (subscriberID != null)
		{
			if (m_countryCodePrefix != null)
			{
				StringTokenizer stk = new StringTokenizer(m_countryCodePrefix,
				",");
				while (stk.hasMoreTokens())
				{
					String token = stk.nextToken();
					if (subscriberID.startsWith("00"))
					{
						subscriberID = subscriberID.substring(2);
					}
					if (subscriberID.startsWith("+")
							|| subscriberID.startsWith("0"))
					{
						subscriberID = subscriberID.substring(1);
					}
					if (subscriberID.startsWith(token))
					{
						subscriberID = subscriberID.substring(token.length());
						break;
					}
				}
			}
		}
		return subscriberID;
	}
	
	private void addSubCount(CDRData ch, String subscriber)
	{

		subscriber = subID(subscriber);
		if (subscriber.length() < 4)
		{
			ch.num_landline_calls++;
			return;
		}

		String sub, type;
		for (int i = 5; i > 1; i--)
		{
			sub = subscriber.substring(0, i - 1);
			if (m_prefixes.containsKey(sub))
			{
				type = (String) m_prefixes.get(sub);
				if (type.equals(PREFIX_IN_CIRCLE))
				{
					ch.num_incircle_calls++;
					return;
				}
				else if (type.equals(PREFIX_OUT_CIRCLE))
				{
					ch.num_outcircle_calls++;
					return;
				}
				else if (type.equals(PREFIX_MOBILE))
				{
					ch.num_mobile_calls++;
					return;
				}
			}
		}
		ch.num_landline_calls++;
	}
}
