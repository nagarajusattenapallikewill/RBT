package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

class CDRReporter 
{
	private static Logger logger = Logger.getLogger(CDRReporter.class);
	
//	private ParametersCacheManager rbtParamCacheManager = null;
	private RBTDBManager rbtDBManager = null;
	private RbtGenericCacheWrapper rbtGenericCacheWrapper=null;
	private static boolean POINT_CODE_STATUS = false;
	private static int m_min_threads = 5;
	private static int m_max_threads = 10;
	private static int m_max_file_count_per_thread = 400;
	private static ArrayList fileList =null;
	private static final String PREFIX_IN_CIRCLE = "CIRCLE";
	private static final String PREFIX_OUT_CIRCLE = "OPERATOR";
	private static final String PREFIX_MOBILE = "MOBILE";
	private static final String PARAMETER_TYPE = "GATHERER";
	private static String COUNTRY_PREFIX="91";
	private static ArrayList ipList =new ArrayList();
	private static SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
	private ArrayList cdrReporterObjects=null;//this corresponds to no of threads(hence no of CDRReporter
	private static Date m_start_time;
	private static Date m_end_time;

//	static private HashMap m_params = null;
	private HashMap m_hmTime_Map = null;
	private HashMap m_hmTime_pointcodeMap = null;

	private static String m_processing_interval = "HOURLY";

//	private static final String _FILENAME = RBTGatherer.m_gathererPath+ "/cdr/CDR_Report";
	public static String temp=null;
	private static HashMap m_prefixes = new HashMap();
	private static RBTGatherer m_parentThread=null;
	
	public void initialize(RBTGatherer parentThread)  // CHECK
	{
		rbtDBManager = RBTDBManager.getInstance();
		rbtGenericCacheWrapper=RbtGenericCacheWrapper.getInstance();
		m_parentThread=parentThread;
		
		POINT_CODE_STATUS = getParamAsBoolean("GATHERER","POINT_CODE_STATUS", "FALSE");
		
		m_min_threads = getParamAsInt("GATHERER", "MIN_THREADS" , 5);
		logger.info("m_min_threads=="+m_min_threads);
		
		m_max_threads = getParamAsInt("GATHERER", "MAX_THREADS" , 10);
		logger.info("m_max_threads=="+m_max_threads);
	
		m_max_file_count_per_thread = getParamAsInt("GATHERER", "MAX_FILE_COUNT", 400);
		logger.info("m_max_file_count_per_thread=="+m_max_file_count_per_thread);

		
		String tmp = null;
		try
		{
			
				tmp = getParamAsString("GATHERER","TELEPHONY_SERVERS", null);
				if (tmp != null && tmp.length() > 0)
				{
					StringTokenizer tkns = new StringTokenizer(tmp, ",");
					int i=0;
					while (tkns.hasMoreTokens())
					{
						if ((temp=tkns.nextToken())!=null) {
							String ip = (String)temp;
							//ip = ip.replaceAll(":","$");
							String cdrlocation = ip.trim(); 
							ipList.add(cdrlocation);
							//									File cdrdir = new File(cdrlocation);
							//									if (cdrdir.isDirectory()){
							//										Tools.addToLogFile("getting fileList from cdr dir " + cdrdir
							//												+ ".**adding into master filelist");
							//									    File[] CdrFile =junk.getFiles(cdrdir);
							//									    for(int count=0;count<CdrFile.length;count++){
							//									    	fileList.add(CdrFile[count]);
							//									    }
							//									    Tools.addToLogFile("got fileList from cdr dir " + cdrdir
							//												+ ".**added "+CdrFile.length+" into master filelist");
							//									}else{
							//										Tools.addToLogFile("cdr dir " + cdrdir
							//												+ " could not be located");
							//									}
							i++;
							////							m_servers.add(temp);
							//							i++;
							//							logger.info("creating cdrReporter object no."+i+" and adding to arrayList");
							//							cdrReporterObjects.add(new CDRReporter(temp));
						}   
					}
					logger.info("TELEPHONY_SERVERS=="+tmp);	
				}

		
		}
		catch (Exception E)
		{
			logger.error("", E);
		}
		
		addprefixes(m_prefixes, PREFIX_OUT_CIRCLE, getParamAsString("GATHERER", "OPERATOR_PREFIX", null));
		
		String tmp1 = "";
		List<SitePrefix> prefix = CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();
		if(prefix != null && prefix.size() > 0)
		{
			for(int i=0; i<prefix.size(); i++)
				tmp1 += "," + prefix.get(i).getSitePrefix().trim();
		}
		logger.info("LocalSitePrefixes=="+tmp1);
		tmp1 = tmp1.substring(1);

		addprefixes(m_prefixes, PREFIX_IN_CIRCLE, tmp1);
		tmp1 = getParamAsString("COMMON","COUNTRY_PREFIX", "91");
		if(tmp1!=null && tmp1.length()>0){
			COUNTRY_PREFIX=tmp1;
		}
		tmp1=null;
		logger.info("COUNTRY_PREFIX=="+COUNTRY_PREFIX);
		tmp1 = getParamAsString("COMMON","MOBILE_PREFIXES", " ");
		addprefixes(m_prefixes, PREFIX_MOBILE, tmp1);
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
public static void initializeStaticVariablesforCDRThread(){
	CDRThread.m_countryCodePrefix=COUNTRY_PREFIX;
	CDRThread.m_processing_interval=m_processing_interval;
	CDRThread.m_end_time=m_end_time;
	CDRThread.m_start_time=m_start_time;
	CDRThread.m_prefixes=m_prefixes;
	CDRThread.POINT_CODE_STATUS=POINT_CODE_STATUS;
	CDRThread.m_parentThread=m_parentThread;
	logger.info("initializing cdrthread....m_countryCodePrefix=="+CDRThread.m_countryCodePrefix);
	logger.info("initializing cdrthread....m_processing_interval=="+CDRThread.m_processing_interval);
	logger.info("initializing cdrthread....m_end_time=="+CDRThread.m_end_time);
	logger.info("initializing cdrthread....m_start_time=="+CDRThread.m_start_time);
	logger.info("initializing cdrthread....m_prefixes=="+CDRThread.m_prefixes);
	logger.info("initializing cdrthread....POINT_CODE_STATUS=="+CDRThread.POINT_CODE_STATUS);
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
					logger.info("creating CDRReprter object's thread no."+((CDRThread)(cdrReporterObjects.get(i))).ThreadCount+" and adding to arrayList.This CDRReporterObject has "+((CDRThread)(cdrReporterObjects.get(i))).arrFileList.size()+" files to be parsed");
					thread.start();
					logger.info("starting CDRReprter object's thread no."+((CDRThread)(cdrReporterObjects.get(i))).ThreadCount);
				}
			}

			for(int i=0;i<cdrReporterThreads.size();i++){
				try {
					logger.info("waiting  for CDRReprter object's thread no."+(i+1)+"to terminate.waiting....");
					((Thread)cdrReporterThreads.get(i)).join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if (POINT_CODE_STATUS == true) {
			merge_m_hmTimePointCode_Map();
		}  
		else
			merge_m_hmTime_Map();
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
										logger.info("updating cdrdata object for time=="+tempSdf+ "with thread no"+i+" with static thread j");
										logger.info("updated ch.call_volume value=="+ch.call_volume);
										logger.info("updated ch.num_incircle_calls value=="+ch.num_incircle_calls);
										logger.info("updated ch.num_mobile_calls value=="+ch.num_mobile_calls);
										logger.info("updated ch.num_outcircle_calls value=="+ch.num_outcircle_calls);
										logger.info("updated ch.number_of_calls  value=="+ch.number_of_calls );
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
					//				logger.info("updating merged hashMap with that of hashMap corresponding to \n server\\"+((CDRReprter)cdrReporterObjects.get(i)).IP);

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
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd-HH:mm:ss");
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
		
		tmp = getParamAsString("GATHERER", "START_TIME", null);
		if (tmp != null && tmp.length() > 0)
			time = tmp;
		if (time != null)
			m_start_time = Tools.getFormattedCDRDate(time, m_processing_interval);


		time = null;
		tmp = getParamAsString("GATHERER", "END_TIME", null);
			if (tmp != null && tmp.length() > 0)
				time = tmp;
		if (time != null)
			m_end_time = Tools.getFormattedCDRDate(time, m_processing_interval);

		
		time = null;
		tmp = getParamAsString("GATHERER", "PROCESSING_INTERVAL", null);
		
		if (tmp != null && tmp.length() > 0)
				time = tmp;
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

	/*
	 * private void callparse(File Cdrdir,HashMap m_hmTime_Map) {
	 * 
	 * File[] list = Cdrdir.listFiles(); if( list != null && list.length>0){
	 * Tools.logStatus(_class, "callparse", "Processing Location, "+Cdrdir);
	 * parse(Cdrdir,m_hmTime_Map); for(int i=0;i <list.length;i++) {
	 * if(list[i].isDirectory())
	 * callparse(list[i],m_hmTime_Map,cricket,smsToAll); } } else{
	 * Tools.logWarning(_class, "callparse", Cdrdir+" is empty");
	 * Tools.addToLogFile("cdr dir "+Cdrdir+" is empty"); return; } }
	 */
//	private String locateCDRFiles(String server_ip)
//	{
//	char DOLLAR = '$';
//	char COLON = ':';
//	server_ip = server_ip.replace(COLON, DOLLAR);
//	String full_cdr_path = "\\\\" + server_ip; 
//	+ "\\" + cdrroot;
//	return full_cdr_path;
//	}
	private static File[] getFiles(File Cdrdir)
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
		String dir = getParamAsString("GATHERER", "GATHERER_PATH", null) + "/cdr";
		HashMap localMap=null;

		if (!new File(dir).exists())
			new File(dir).mkdir();

		int port_count = getPortFromIVM();

		Set keys;boolean check=true;
		if (getParamAsBoolean("GATHERER","POINT_CODE_STATUS", "TRUE")==true) {
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
			if (getParamAsBoolean("GATHERER","POINT_CODE_STATUS", "TRUE")==true) {
				Integer temp1=(Integer)iter.next();
				tempJunk=temp1.intValue();
				logger.info("file writing for POINT_CODE=="+temp1);
				reportFile = new File(getParamAsString("GATHERER", "GATHERER_PATH", null)+ "/cdr/CDR_Report_" + temp1 + "_"+ getParamAsInt("GATHERER","DATA_COLLECTION_DAYS",5) + ".csv");
				localMap=(HashMap)(m_hmTime_pointcodeMap.get(temp1));
			}
			else{
				check=false;
				reportFile = new File(getParamAsString("GATHERER", "GATHERER_PATH", null)+ "/cdr/CDR_Report_" + "_" +  getParamAsInt("GATHERER","DATA_COLLECTION_DAYS",5) + ".csv");
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
		cal.add(Calendar.DATE, -getParamAsInt("GATHERER","DATA_COLLECTION_DAYS",5));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}

	private static int getPortFromIVM()
	{

		String _method = "getPortFromIVM";
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

		String _method = "readNMSTag";
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

		
	public boolean getParamAsBoolean(String type, String param, String defaultVal)
    {
    	try{
    		return rbtGenericCacheWrapper.getParameter(type, param, defaultVal).equalsIgnoreCase("TRUE");
    	}catch(Exception e){
    		logger.info("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal.equalsIgnoreCase("TRUE");
    	}
    }
	
	public int getParamAsInt(String type, String param, int defaultVal)
    {
    	try{
    		String paramVal = rbtGenericCacheWrapper.getParameter(type, param, defaultVal+"");
    		return Integer.valueOf(paramVal);   		
    	}catch(Exception e){
    		logger.info("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal;
    	}
    }
	
	  public String getParamAsString(String type, String param, String defualtVal)
	    {
	    	try{
	    		return rbtGenericCacheWrapper.getParameter(type, param, defualtVal);
	    	}catch(Exception e){
	    		logger.info("Unable to get param ->"+param +"  type ->"+type);
	    		return defualtVal;
	    	}
	    }

}