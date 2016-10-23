package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.onmobile.admin.common.OnvXmlApi;
import com.onmobile.apps.ringbacktones.common.Tools;

class CDRReprterForTonePlayers   
{
	private static Logger logger = Logger.getLogger(CDRReprterForTonePlayers.class);
	
	public static ArrayList folders_to_zip = new ArrayList();
	public static String m_gathererPath = null;
	private static boolean POINT_CODE_STATUS = false;
	private static int m_db_collection_days = 5;
	private static int m_min_threads = 5;
	private static int m_max_threads = 10;
	private static boolean m_daemon_mode = false;
	private static int m_gathererInterval = 5;
	private static int m_max_file_count_per_thread = 400;
	private static ArrayList fileList = null;
	private static final String PREFIX_IN_CIRCLE = "CIRCLE";
	private static final String PREFIX_OUT_CIRCLE = "OPERATOR";
	private static final String PREFIX_MOBILE = "MOBILE";
	private static String m_resource_file="resources/CDRForTonePlayers";
	private static ResourceBundle m_bundle=null;
	private static String m_spider_dir;
	private static ArrayList ipList =new ArrayList();
	private static SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
	private static Date m_start_time;
	private static Date m_end_time;
	private static String m_processing_interval = "HOURLY";
	private static String _FILENAME =null;
	private static HashMap m_prefixes = new HashMap();
	private HashMap m_hmTime_Map = null;
	private HashMap m_hmTime_pointcodeMap = null;
	private ArrayList cdrReporterObjects=null;//this corresponds to no of threads(hence no of CDRReprterForTonePlayers

	private static String COUNTRY_PREFIX="91";
	static boolean m_usePool = true;
	static String m_dbURL = null;
	static public HashMap m_params = null;
	

//	public CDRReprterForTonePlayers()
//	{
//
//		this.arrFileList=filelist;
//		this.ThreadCount=count;
//	}

	public static void initialize()
	{
		
		try{
            m_bundle=ResourceBundle.getBundle(m_resource_file);
        }catch(Exception e){
            System.out.println("error in getting bundle:"+m_resource_file+" !!!");
            e.printStackTrace();
        }

		String tmp = null;

		tmp=m_bundle.getString("COUNTRY_PREFIX");
		if(tmp!=null){
				m_params.put("COUNTRY_PREFIX",tmp);
				COUNTRY_PREFIX=tmp;
	        tmp=null;
		}
		
		tmp=m_bundle.getString("DAEMON_MODE");
		if(tmp!=null){
				m_params.put("DAEMON_MODE",tmp);
	            if (tmp != null && tmp.length() > 0)
	                m_daemon_mode = (tmp.equalsIgnoreCase("true") || tmp
	                        .equalsIgnoreCase("on"));
	        tmp=null;
		}
		tmp=m_bundle.getString("DB_COLLECTION_DAYS");
		if(tmp!=null){
				m_params.put("DB_COLLECTION_DAYS",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
	                try
	                {
	                    m_db_collection_days = Integer.parseInt(tmp);
	                }
	                catch (Exception e)
	                {
	                    m_db_collection_days = 5;
	                }
	            }
	        
	        tmp=null;
		}
		tmp=m_bundle.getString("SPIDER_DIR");
		if(tmp!=null){
				m_params.put("SPIDER_DIR",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
	            	 m_spider_dir = tmp;
	            }
	        
	        tmp=null;
		}
		tmp=m_bundle.getString("MIN_THREADS");
		if(tmp!=null){
				m_params.put("MIN_THREADS",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
					try
					{
						m_min_threads = Integer.parseInt(tmp);
					}
					catch (Exception e)
					{
						m_min_threads =5;    
					}
				}
	        
	        tmp=null;
		}
		tmp=m_bundle.getString("MAX_THREADS");
		if(tmp!=null){
				m_params.put("MAX_THREADS",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
					try
					{
						m_max_threads = Integer.parseInt(tmp);
					}
					catch (Exception e)
					{
						m_max_threads =10;    
					}
				}
	        
	        tmp=null;
		}
		tmp=m_bundle.getString("MAX_FILE_COUNT");
		if(tmp!=null){
				m_params.put("MAX_FILE_COUNT",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
					try
					{
						m_max_file_count_per_thread = Integer.parseInt(tmp);
					}
					catch (Exception e)
					{
						m_max_file_count_per_thread = 400;
					}
				}
	        
	        tmp=null;
		}
		
		tmp=m_bundle.getString("CUST_NAME");
		if(tmp!=null){
				m_params.put("CUST_NAME",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("SITE_NAME");
		if(tmp!=null){
				m_params.put("SITE_NAME",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("GATHERER_HOUR");
		if(tmp!=null){
				m_params.put("GATHERER_HOUR",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("DATABASE_CLEANUP_HOUR");
		if(tmp!=null){
				m_params.put("DATABASE_CLEANUP_HOUR",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("AUTODEACTIVATION_HOUR");
		if(tmp!=null){
				m_params.put("AUTODEACTIVATION_HOUR",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("GATHERER_SLEEP_INTERVAL");
		if(tmp!=null){
				m_params.put("GATHERER_SLEEP_INTERVAL",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("DB_COLLECTION_DAYS");
		if(tmp!=null){
				m_params.put("DB_COLLECTION_DAYS",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
					try
					{
						m_db_collection_days = Integer.parseInt(tmp);
					}
					catch (Exception e)
					{
						m_db_collection_days = 5;
					}
				}
	        
	        tmp=null;
		}
		tmp=m_bundle.getString("POINT_CODE_STATUS");
		if(tmp!=null){
				m_params.put("POINT_CODE_STATUS",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {

					if(tmp.equalsIgnoreCase("TRUE"))
						POINT_CODE_STATUS = true;
					else if(tmp.equalsIgnoreCase("false"))
						POINT_CODE_STATUS=false;
					else
						logger.info("parameter POINT_CODE_STATUS in DB is not appropriate");
				}
	        
	        tmp=null;
		}
		
		try
		{
//			m_servers = new ArrayList();
			tmp=m_bundle.getString("TELEPHONY_SERVERS");
			if(tmp!=null){
					m_params.put("TELEPHONY_SERVERS",tmp);
		            if (tmp != null && tmp.length() > 0)
		            {
						StringTokenizer tkns = new StringTokenizer(tmp, ",");
						int i=0;String temp=null;
						while (tkns.hasMoreTokens())
						{
							if ((temp=tkns.nextToken())!=null) {
								String ip = (String)temp;
								logger.info("ip=="+ip);
								//ip = ip.replace(':','$');
								String cdrlocation = ip.trim(); 
								ipList.add(cdrlocation);
								i++;
							}   
						}

					}
		        
		        tmp=null;
			}
			
		}
		catch (Exception E)
		{
			logger.error("", E);
		}
		tmp=m_bundle.getString("PROCESSING_INTERVAL");
		if(tmp!=null){
				m_params.put("PROCESSING_INTERVAL",tmp);
	        tmp=null;
		}
		tmp=m_bundle.getString("OPERATOR_PREFIX");
		if(tmp!=null){
				m_params.put("OPERATOR_PREFIX",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
	            	addprefixes(m_prefixes, PREFIX_OUT_CIRCLE,
						tmp);
	            	}
	        
	        tmp=null;
		}
		String tmp1 = "";
		tmp=m_bundle.getString("LOCAL_SITE_PREFIX");
		if(tmp!=null){
				m_params.put("LOCAL_SITE_PREFIX",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
	            	tmp1=tmp;
	            }
	        tmp=null;
		}

		tmp1 = tmp1.substring(1);

		addprefixes(m_prefixes, PREFIX_IN_CIRCLE, tmp1);
		tmp=m_bundle.getString("MOBILE_PREFIXES");
		if(tmp!=null){
				m_params.put("MOBILE_PREFIXES",tmp);
	            if (tmp != null && tmp.length() > 0)
	            {
	            	tmp1=tmp;
	            	addprefixes(m_prefixes, PREFIX_MOBILE, tmp1);
	            	}
	        
	        tmp=null;
		}
	}
	private static int getNoOfThreads(int fileCount){
		logger.info("entering");
		int count=m_min_threads;
		int temp=0;
		double temp1=0.0;
		boolean check=false;
		for(count=m_min_threads;count<(m_max_threads+1);count++){
			temp=fileCount/count;
			temp1=fileCount%count;
			if(temp1!=0){
				temp++;
			}
			if(temp>m_max_file_count_per_thread){
				continue;
			}else{
				check=true;
				break;
			}
		}
		if(check){

			logger.info("exiting with no of threads=="+count);

			return count;
		}else{

			logger.info("exiting with no of threads=="+m_max_threads);
			return m_max_threads;
		}
	}
	private void createAndAddCDRThreadObject(ArrayList fileList,int noOfThreads){
		logger.info("entering");
		ArrayList arrFile=new ArrayList();
		int counter=0;
		ArrayList temp=null;
		
		for(int arrCount=0;arrCount<noOfThreads;arrCount++){
			arrFile.add(new ArrayList());
		}
		for(int countstart =0; countstart <fileList.size() ; countstart++){
			counter= countstart%noOfThreads;
			temp=(ArrayList)arrFile.get(counter);
			temp.add(fileList.get(countstart));
		}
		for(int start=0;start<arrFile.size();start++){
			temp=(ArrayList)arrFile.get(start);
			cdrReporterObjects.add(new CDRThread(temp,start));
		}
		
		logger.info("exiting with cdrReporterObjects of size=="+cdrReporterObjects.size());

	}
	public void process(){
		String cdrlocation=null;
		getDates();
		fileList=new ArrayList();
		for(int intCount=0;intCount<ipList.size();intCount++){
			cdrlocation=(String)ipList.get(intCount);
			File cdrdir = new File(cdrlocation);
			if (cdrdir.isDirectory()){
				Tools.addToLogFile("getting fileList from cdr dir " + cdrdir
						+ ".**adding into master filelist");
				logger.info("getting fileList from cdr dir " + cdrdir
						+ ".**adding into master filelist");
				File[] CdrFile =getFiles(cdrdir,true);
				for(int count=0;count<CdrFile.length;count++){
					fileList.add(CdrFile[count]);
					logger.info("adding file " + CdrFile[count]+" with count=="+count);
				}
				Tools.addToLogFile("got fileList from cdr dir " + cdrdir
						+ ".**added "+CdrFile.length+" into master filelist");
				logger.info("got fileList from cdr dir " + cdrdir
						+ ".**added "+CdrFile.length+" into master filelist");
			}else{
				Tools.addToLogFile("cdr dir " + cdrdir
						+ " could not be located");
				logger.info("cdr dir " + cdrdir
						+ " could not be located");
			}
		}
		logger.info("total file count in master fileArrayList=="+fileList.size());
		cdrReporterObjects=new ArrayList();
		if(fileList!=null && fileList.size()>0){
			
			int noOfThreads=getNoOfThreads(fileList.size());
			if(noOfThreads>0){
				
				initializeStaticVariablesforCDRThread();
				
				createAndAddCDRThreadObject(fileList,noOfThreads);
			}else{
				Tools.addToLogFile("no cdrReporterObjects exist" );
				logger.info("no cdrReporterObjects exist" );
			}
		}else{
			Tools.addToLogFile("no file exist to be parsed " );
			logger.info("no file exist to be parsed " );
		}



		if (cdrReporterObjects.size()!=0) {
			ArrayList cdrReporterThreads = new ArrayList();
			for (int i = 0; i < cdrReporterObjects.size(); i++) {

				if (cdrReporterObjects.get(i) != null) {
					Thread thread = new Thread((CDRThread)(cdrReporterObjects.get(i)));  
					cdrReporterThreads.add(thread);
					logger.info("creating CDRReprterForTonePlayers object's thread no."+((CDRThread)(cdrReporterObjects.get(i))).ThreadCount+" and adding to arrayList.This CDRReporterObject has "+((CDRThread)(cdrReporterObjects.get(i))).arrFileList.size()+" files to be parsed");
					thread.start();
					logger.info("starting CDRReprterForTonePlayers object's thread no."+((CDRThread)(cdrReporterObjects.get(i))).ThreadCount);
				}
			}

			for(int i=0;i<cdrReporterThreads.size();i++){
				try {
					logger.info("waiting  for CDRReprterForTonePlayers object's thread no."+(i+1)+"to terminate.waiting....");
					((Thread)cdrReporterThreads.get(i)).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (POINT_CODE_STATUS==true) {
			merge_m_hmTimePointCode_Map();
		}  
		else
			merge_m_hmTime_Map();
	}
	public static void initializeStaticVariablesforCDRThread(){
	CDRThread.m_countryCodePrefix=COUNTRY_PREFIX;
	CDRThread.m_processing_interval=m_processing_interval;
	CDRThread.m_end_time=m_end_time;
	CDRThread.m_start_time=m_start_time;
	CDRThread.m_prefixes=m_prefixes;
	CDRThread.POINT_CODE_STATUS=POINT_CODE_STATUS;
	}
	private void merge_m_hmTimePointCode_Map(){
		m_hmTime_pointcodeMap=new HashMap(); 
		logger.info("inside merging hashMap");
		Iterator iter=null;
		logger.info("cdrReporterObjects size = "+cdrReporterObjects.size());
		for(int j=0;(j<cdrReporterObjects.size());j++){
			HashMap temp=(HashMap)(((CDRThread)(cdrReporterObjects.get(j))).m_hmTime_pointcodeMap_local);
			Set keys=temp.keySet();
			iter=keys.iterator();
			ArrayList a1 = new ArrayList();
			while (iter.hasNext())
				a1.add(iter.next());
			Collections.sort(a1);
			iter = a1.iterator();
			while(iter.hasNext()){
				Integer temp1=(Integer)iter.next();
				if(!m_hmTime_pointcodeMap.containsKey(temp1)){
					HashMap hm=createHashMap();
					Date cdr_date = m_start_time;
					logger.info("start time = "+cdr_date);
					logger.info("end time = "+m_end_time);
					logger.info("creating merged hash map for pointcode format. \n creating......");
					while (cdr_date.equals(m_start_time) || (cdr_date.after(m_start_time) && cdr_date.before(m_end_time)) || cdr_date.equals(m_end_time))
					{
						logger.info("cdr_date = "+cdr_date);

						HashMap temphashmap=null;
						String tempSdf=null;
						CDRData tempcdrdata=null;
						if (hm.containsKey(sdf.format(cdr_date)))
						{
								    	 logger.info("merged hashmap contains this cdr_date as key ");

							CDRData ch = (CDRData) hm.get(sdf.format(cdr_date));
							for(int i=j;(i<cdrReporterObjects.size());i++){

									      	 logger.info("updating merged hashMap with that of hashMap corresponding to \n thread no. "+((CDRThread)cdrReporterObjects.get(i)).ThreadCount);
								HashMap temp2=(HashMap)(((CDRThread)(cdrReporterObjects.get(i))).m_hmTime_pointcodeMap_local);
								if (temp2 != null && temp2.containsKey(temp1)) {
									temphashmap = (HashMap)temp2.get(temp1);
									tempSdf = sdf.format(cdr_date);
									tempcdrdata = (CDRData) (temphashmap
											.get(tempSdf));
									if(tempcdrdata != null)
									{
										ch.call_volume = ch.call_volume
										+ (tempcdrdata).call_volume;
										ch.NS_count = ch.NS_count
										+ (tempcdrdata).NS_count;
										ch.NU_count = ch.NU_count
										+ (tempcdrdata).NU_count;
										ch.num_incircle_calls = ch.num_incircle_calls
										+ (tempcdrdata).num_incircle_calls;
										ch.num_landline_calls = ch.num_landline_calls
										+ (tempcdrdata).num_landline_calls;
										ch.num_mobile_calls = ch.num_mobile_calls
										+ (tempcdrdata).num_mobile_calls;
										ch.num_outcircle_calls = ch.num_outcircle_calls
										+ (tempcdrdata).num_outcircle_calls;
										ch.number_of_calls = ch.number_of_calls
										+ (tempcdrdata).number_of_calls;
										ch.total_duration = ch.total_duration
										+ (tempcdrdata).total_duration;
										ch.US_count = ch.US_count
										+ (tempcdrdata).US_count;
										ch.UU_count = ch.UU_count
										+ (tempcdrdata).UU_count;
									}
								}    					        	 
										logger.info("updated (completed) merged hashMap with that of hashMap corresponding to \n server\\"+((CDRThread)cdrReporterObjects.get(i)).ThreadCount);

							}
						}
						cdr_date = Tools.getNextInterval(cdr_date, m_processing_interval);
					}
					m_hmTime_pointcodeMap.put(temp1, hm);
					
				}
			}
		}


		writeFile();
	}

	private void merge_m_hmTime_Map(){
		m_hmTime_Map=createHashMap();
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
		Date cdr_date = m_start_time;
		logger.info("start time = "+cdr_date);
		logger.info("end time = "+m_end_time);
		logger.info("creating merged hash map. \n creating......");
		while (cdr_date.equals(m_start_time) || (cdr_date.after(m_start_time) && cdr_date.before(m_end_time)) || cdr_date.equals(m_end_time))
		{
			//	logger.info("cdr_date = "+cdr_date);

			HashMap temphashmap=null;String tempSdf=null;CDRData tempcdrdata=null;
			if (m_hmTime_Map.containsKey(sdf.format(cdr_date)))
			{
//				logger.info("merged hashmap contains this cdr_date as key ");

				CDRData ch = (CDRData) m_hmTime_Map.get(sdf.format(cdr_date));
				for(int i=0;(i<cdrReporterObjects.size());i++){
					//
					//				logger.info("updating merged hashMap with that of hashMap corresponding to \n server\\"+((CDRReprterForTonePlayers)cdrReporterObjects.get(i)).IP);

					temphashmap=((CDRThread)cdrReporterObjects.get(i)).m_hmTime_Map_local;
					tempSdf=sdf.format(cdr_date);
					tempcdrdata=(CDRData)(temphashmap.get(tempSdf));
					if(tempcdrdata != null)
					{
						ch.call_volume			=ch.call_volume+(tempcdrdata).call_volume;
						ch.NS_count			=ch.NS_count+(tempcdrdata).NS_count;
						ch.NU_count			=ch.NU_count+(tempcdrdata).NU_count;
						ch.num_incircle_calls	=ch.num_incircle_calls+(tempcdrdata).num_incircle_calls;
						ch.num_landline_calls	=ch.num_landline_calls+(tempcdrdata).num_landline_calls;
						ch.num_mobile_calls	=ch.num_mobile_calls+(tempcdrdata).num_mobile_calls;
						ch.num_outcircle_calls	=ch.num_outcircle_calls+(tempcdrdata).num_outcircle_calls;
						ch.number_of_calls		=ch.number_of_calls+(tempcdrdata).number_of_calls;
						ch.total_duration		=ch.total_duration+(tempcdrdata).total_duration;
						ch.US_count			=ch.US_count+(tempcdrdata).US_count;
						ch.UU_count			=ch.UU_count+(tempcdrdata).UU_count;
					}
					logger.info("updated (completed) merged hashMap with that of hashMap corresponding to \n thread no. "+((CDRThread)cdrReporterObjects.get(i)).ThreadCount);

				}
			}
			cdr_date = Tools.getNextInterval(cdr_date, m_processing_interval);
		}
		writeFile();
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

	private void getDates()
	{

		m_start_time = null;
		m_end_time = null;

		String time = null;
		String tmp = null;
		if (m_params.containsKey("START_TIME"))
		{
			tmp = (String) m_params.get("START_TIME");
			if (tmp != null && tmp.length() > 0)
				time = tmp;
		}

		if (time != null)
			m_start_time = Tools.getFormattedCDRDate(time,
					m_processing_interval);

		time = null;
		if (m_params.containsKey("END_TIME"))
		{
			tmp = (String) m_params.get("END_TIME");
			if (tmp != null && tmp.length() > 0)
				time = tmp;
		}

		if (time != null)
			m_end_time = Tools.getFormattedCDRDate(time, m_processing_interval);

		time = null;
		if (m_params.containsKey("PROCESSING_INTERVAL"))
		{
			tmp = (String) m_params.get("PROCESSING_INTERVAL");
			if (tmp != null && tmp.length() > 0)
				time = tmp;
		}
		if (time != null)
			m_processing_interval = time;

		if (m_start_time == null || m_end_time == null)
		{
			m_start_time = StartOfDataCollection();
			m_end_time = EndOfDataCollection();
		}
		else if (m_start_time.after(m_end_time))
		{
			m_start_time = StartOfDataCollection();
			m_end_time = EndOfDataCollection();
		}
	}

	private static File[] getFiles(File Cdrdir,boolean temp)
	{
		logger.info("cdrdir parsed is " + Cdrdir);
		File[] list = Cdrdir.getAbsoluteFile().listFiles(new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				if (name.endsWith(".txt"))
				{
					if (name.startsWith("C"))
					{
						File ftemp = new File(dir + "\\" + name);
						if (ftemp.isDirectory())
						{
							getFiles(ftemp,true);
							return false;
						}
						Date cdr_date = Tools.getFileDate(name);
						Date last_date = new Date(ftemp.lastModified());
						//logger.info("checking date for
						// cdr file found " +ftemp.getName());
						//logger.info("Start time " +
						// m_start_time + " End time " + m_end_time + " cdr_date
						// " + cdr_date +" last modified date "+last_date);
						//logger.info("last modified date
						// "+last_date);
						if (cdr_date.before(m_start_time)
								&& last_date.after(m_end_time))
						{
							return true;
						}
						if (cdr_date.equals(m_start_time)
								|| cdr_date.equals(m_end_time)
								|| (cdr_date.after(m_start_time) && cdr_date
										.before(m_end_time)))
						{
							return true;
						}
						else
						{
							if (last_date.equals(m_start_time)
									|| last_date.equals(m_end_time)
									|| (last_date.after(m_start_time) && last_date
											.before(m_end_time)))
								return true;
							else
								return false;
						}
					}
					else
						return false;
				}
				else
					return false;
			}
		});
		return list;
	}	
	private void writeFile()
	{
		String dir = m_gathererPath + "/cdr";
		HashMap localMap=null;

		if (!new File(dir).exists())
			new File(dir).mkdir();

		int port_count = getPortFromIVM();

		Set keys;boolean check=true;
		if (POINT_CODE_STATUS==true) {
			keys = m_hmTime_pointcodeMap.keySet();
		}
		else{
			keys = m_hmTime_Map.keySet();
		}

		Iterator iter=keys.iterator();
		ArrayList a1 = new ArrayList();
		while (iter.hasNext())
			a1.add(iter.next());
		Collections.sort(a1);
		iter = a1.iterator();
		int tempJunk=0;
		while ((iter.hasNext())&&(check==true)) {
			File reportFile;
			if (POINT_CODE_STATUS==true) {
				Integer temp1=(Integer)iter.next();
				tempJunk=temp1.intValue();
				logger.info("file writing for POINT_CODE=="+temp1);
				reportFile = new File(_FILENAME + "_" + temp1 + "_"+ m_db_collection_days + ".csv");
				localMap=(HashMap)(m_hmTime_pointcodeMap.get(temp1));
			}
			else{
				check=false;
				reportFile = new File(_FILENAME + "_" +  m_db_collection_days + ".csv");
				localMap = (HashMap) m_hmTime_Map;
			}

			FileOutputStream fout = null;
			try {
//				System.out.println();
				logger.info("report file is"+reportFile.toString());
				reportFile.createNewFile();

				fout = new FileOutputStream(reportFile);
			} catch (Exception e) {
				logger.error("", e);
			}
			StringBuffer sbCdrTemp = new StringBuffer();
			sbCdrTemp
			.append("Hour, Call Volume, Call Count, Incircle Calls, OutCircle Calls, Mobile Calls, Other Calls, Total Duration, NS_Count, NU_Count, UU_Count, US_Count, Ports\n");
//			if (POINT_CODE_STATUS==true) {
//			localMap=(HashMap)(m_hmTime_pointcodeMap.get(temp));
//			}			
//			else {
//			localMap = (HashMap) m_hmTime_Map;
//			}			
			Iterator i = localMap.keySet().iterator();
			ArrayList a = new ArrayList();
			while (i.hasNext())
				a.add(i.next());
			Collections.sort(a);
			i = a.iterator();
			while (i.hasNext()) {
				
				String key = (String) i.next();
				logger.info("file writing for POINT_CODE=="+tempJunk+" and time=="+key);
				CDRData ch = (CDRData) localMap.get(key);
				sbCdrTemp.append(key + ",");
				logger.info("call vol=="+ch.call_volume);
				sbCdrTemp.append(ch.call_volume + ",");
				sbCdrTemp.append(ch.number_of_calls + ",");
				sbCdrTemp.append(ch.num_incircle_calls + ",");
				sbCdrTemp.append(ch.num_outcircle_calls + ",");
				sbCdrTemp.append(ch.num_mobile_calls + ",");
				sbCdrTemp.append(ch.num_landline_calls + ",");
				sbCdrTemp.append(ch.total_duration + ",");
				sbCdrTemp.append(ch.NS_count + ",");
				sbCdrTemp.append(ch.NU_count + ",");
				sbCdrTemp.append(ch.UU_count + ",");
				sbCdrTemp.append(ch.US_count + ",");
				sbCdrTemp.append(port_count);
				sbCdrTemp.append("\n");
			}
			try {
				fout.write(sbCdrTemp.toString().getBytes());
				fout.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}        
	}


	private Date StartOfDataCollection()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -m_db_collection_days);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	private static int getPortFromIVM()
	{
		String path = null;
		String ip=null;

		Iterator iterator = ipList.iterator();

		String xml = "ivm.xml";
		Node config = null;
		String strContent = null;
		int ports = 0;
		try
		{
			
			while (iterator.hasNext())
			{
				ip=(String)iterator.next();
				path =  ip.substring(0, ip.indexOf("cdr"));
				path = path + "config" + File.separator + xml;
				logger.info("RBT::ivm.xml file " + path);
				if (!new File(path).exists())
				{
					logger.info("ivm.xml path " + path
							+ " could not be located");
					continue;
				}
				FileInputStream fis = new FileInputStream(path);
				int iLength = fis.available();
				byte bTemp[] = new byte[iLength];
				fis.read(bTemp);
				fis.close();

				// Converting byte array to string
				strContent = new String(bTemp); // Converts the byte array to
				// String

				DocumentBuilderFactory factory = DocumentBuilderFactory
				.newInstance();
				DocumentBuilder builder = factory.newDocumentBuilder();
				InputSource is = new InputSource(new StringReader(strContent));
				Document document = builder.parse(is);

				NodeList nodeList = document.getElementsByTagName("Config");
				config = nodeList.item(0);

				OnvXmlApi xmlApi = new OnvXmlApi(config);

				if (config != null)
				{
					NodeList nodeListComp = null;
					nodeListComp = ((Element) config)
					.getElementsByTagName("Component");

					for (int i = 0; i < nodeListComp.getLength(); i++)
					{
						Element node = (Element) nodeListComp.item(i);

						String name = xmlApi.getStrAttr(node, "name");

						if (name != null && name.equalsIgnoreCase("nms"))
						{
							ports += readNMSTag(node);
							break;
						}
					}
				}

			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			return 0;
		}

		return ports;
	}

	private static int readNMSTag(Node nms)
	{
		int n = 0;

		try
		{
			OnvXmlApi xmlApi = new OnvXmlApi(nms);

			NodeList nodeListMedia, nodeListCIC = null;

			Node nodeGeneral = xmlApi.getNode(nms, "General");
			String signalling = xmlApi
			.getStrAttr(nodeGeneral, "SignallingMode");
			if (!signalling.equalsIgnoreCase("Standalone")
					&& !signalling.equalsIgnoreCase("Primary"))
			{
				return 0;
			}

			nodeListMedia = ((Element) nms).getElementsByTagName("Signalling");

			for (int i = 0; i < nodeListMedia.getLength(); i++)
			{
//				Node node = nodeListMedia.item(i);
				nodeListCIC = ((Element) nms).getElementsByTagName("Circuits");
				for (int j = 0; j < nodeListCIC.getLength(); j++)
				{
					Node nodeCIC = nodeListCIC.item(j);
					int start = xmlApi.getIntAttr(nodeCIC, "FirstCIC");
					int end = xmlApi.getIntAttr(nodeCIC, "LastCIC");
					n += (end - start) + 1;
				}
			}
			return n;
		}
		catch (Exception e)
		{
			logger.error("", e);
			return 0;
		}
	}

	private Date EndOfDataCollection()
	{
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		return cal.getTime();
	}

	private static void addprefixes(HashMap m, String type, String prefixes)
	{
//		logger.info("prefixes " + prefixes);
		StringTokenizer tkn = new StringTokenizer(prefixes, ",");
		while (tkn.hasMoreTokens())
		{
			String temp = tkn.nextToken();
			m.put(temp, type);
		}
	}
	private Date roundToNearestInterVal(int interval)
	{
		Calendar cal = Calendar.getInstance();
		int n = 60 / interval;
		for (int i = 1; i <= n; i++)
		{
			if (cal.get(Calendar.MINUTE) < (interval * (i))
					&& cal.get(Calendar.MINUTE) >= (interval * (i - 1)))
			{
				cal.set(Calendar.SECOND, 0);
				if (i < n)
					cal.set(Calendar.MINUTE, (interval * (i)));
				else
				{
					cal.set(Calendar.MINUTE, 0);
					cal.add(Calendar.HOUR_OF_DAY, 1);
				}
				break;
			}
		}
		return cal.getTime();
	}
	private String getZipFileName()
	{
		String cust =null;
		//RBTCommonConfig.getInstance().getParameter("CUST_NAME");
		String site =null;
		//RBTCommonConfig.getInstance().getParameter("SITE_NAME");
		String tmp=null;
		
		try {
			if (m_params.containsKey("CUST_NAME"))
			{
				tmp = (String) m_params.get("CUST_NAME");
				if (tmp != null && tmp.length() > 0)
				{
					cust=tmp;
					}
			}
			if (m_params.containsKey("SITE_NAME"))
			{
				tmp = (String) m_params.get("SITE_NAME");
				if (tmp != null && tmp.length() > 0)
				{
					site=tmp;
					}
			}
		} catch (Throwable E)
		{
			logger.error("", E);
			E.printStackTrace();
		}
		String hostname=null;
		 try {
				InetAddress localHost=InetAddress.getLocalHost();
				hostname=localHost.getHostName();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);//yesterday
		String datename = Tools.getDateAsName(cal.getTime());

		return m_gathererPath + "\\RBTGatherer_" + cust + "_" + site + "_"+hostname+"_"
		+ datename + ".zip";
	}
	private long getSleepTime(Date date)
	{
		return (date.getTime() - System.currentTimeMillis());
	}
	public void processCDR()
	{
		String _method = "processCDR";
		int gather_hour = 1;
		String tmp = null;
		try{
			if (m_params.containsKey("GATHERER_HOUR"))
			{
				tmp = (String) m_params.get("GATHERER_HOUR");
				if (tmp != null && tmp.length() > 0)
				{
					try
					{
						gather_hour = Integer.parseInt(tmp);
					}
					catch (Exception e)
					{
						gather_hour = 1;
					}
				}
			}
			if (m_params.containsKey("GATHERER_SLEEP_INTERVAL"))
			{
				tmp = (String) m_params.get("GATHERER_SLEEP_INTERVAL");
				if (tmp != null && tmp.length() > 0)
				{
					try
					{
						m_gathererInterval = Integer.parseInt(tmp);
					}
					catch (Exception e)
					{
						m_gathererInterval = 5;
					}
				}
			}

			if (!m_daemon_mode)
			{
				gather();
				return;
			}

			Calendar cal;
			while (true)
			{
				cal = Calendar.getInstance();
				int current_hour = cal.get(Calendar.HOUR_OF_DAY);

				if (current_hour >= gather_hour)
				{
					String zip_file = getZipFileName();//get yesterday's zip
					// file
					if (new File(zip_file).exists() == false)
					{
						logger.info("yesterday's zip " +zip_file+ " not found");
						gather();
					}
				}
				Date next_run_time = roundToNearestInterVal(m_gathererInterval);
				if (cal.get(Calendar.HOUR_OF_DAY) != 0)
				{
					logger.info("zip file for yesterday already exists, Sleeping till "
							+ next_run_time + "!!!!");
				}
				else
				{
					logger.info("Time of the day is 0 hours , Sleeping till "
							+ next_run_time + "!!!!");
				}
				long sleeptime = getSleepTime(next_run_time);
				try
				{
					Thread.sleep(sleeptime);
				}
				catch (Throwable E)
				{
					logger.error("", E);
				}

			}
		}
			catch (Throwable th)
			{
				logger.error("", th);
				logger.info("" + th);
			}
		}
	 private void gather()
	    {
		 logger.info("callind reporter process()");
		 process();
     	Tools.writeLogFile(m_gathererPath);

        zipcreator zc = new zipcreator();
        String zipfilename = zc.createzipForTonePlayers(null);
        logger.info("zipfilename " + zipfilename);
        if (zipfilename == null)
        {
            logger.info("No Zip file created");
            return;
        }
        
        
            uploadZipFilesToSpiderDir(zipfilename);
        
	    }
	 private File[] getZipFileList()
	    {

	        File[] list = new File(m_gathererPath + "/")
	                .listFiles(new FilenameFilter()
	                {
	                    public boolean accept(File dir, String name)
	                    {
	                        if (name.endsWith(".zip"))
	                            return true;
	                        else
	                            return false;
	                    }
	                });

	        return list;
	    }
	 private void uploadZipFilesToSpiderDir(String zipfile)
	    {
	        File file_to_copy = new File(zipfile);

	        Tools.moveFile(m_spider_dir, file_to_copy);

	        File[] zip_files = getZipFileList();

	        if (zip_files == null || zip_files.length <= 0)
	            return;

	        for (int i = 0; i < zip_files.length; i++)
	        {
	            String date = "";
	            StringTokenizer tokens = new StringTokenizer(
	                    zip_files[i].getName(), "_");
	            while (tokens.hasMoreTokens())
	            {
	                date = tokens.nextToken();
	            }

	            Date zipFileDate = Tools.getdate(date.substring(0, date
	                    .indexOf(".")), "yyyy-MM-dd");

	            Calendar cal = Calendar.getInstance();
	            cal.add(Calendar.DATE, -2);

	            Date compDate = Tools.getdate(Tools.getChangedFormatDate(cal
	                    .getTime()), "yyyy-MM-dd");
	            logger.info(" zipfile date " + zipFileDate + " cal.getTime() "
	                                    + cal.getTime());

	            if (compDate.after(zipFileDate))
	                zip_files[i].delete();
	        }

	    }

		public static void main(String[] args){
			Tools.init("RBTGatherer", true);
			try{
	            m_bundle=ResourceBundle.getBundle(m_resource_file);
	        }catch(Exception e){
	            System.out.println("error in getting bundle:"+m_resource_file+" !!!");
	            e.printStackTrace();
	        }
	        m_params=new HashMap();
	        
	        String tmp=null;
	        tmp=m_bundle.getString("GATHERER_PATH");
			if(tmp!=null){
					m_params.put("GATHERER_PATH",tmp);
					m_gathererPath=tmp;
			}
			_FILENAME = m_gathererPath
			+ "/cdr/CDR_Report";
			folders_to_zip.add(m_gathererPath + "\\cdr");
			CDRReprterForTonePlayers junkTemp=new CDRReprterForTonePlayers();
			logger.info("callind reporter initilize()");
			initialize();
			junkTemp.processCDR();
		}
	}
