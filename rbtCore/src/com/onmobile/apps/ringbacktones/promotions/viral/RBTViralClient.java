package com.onmobile.apps.ringbacktones.promotions.viral;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Processes the last n records of both OSC & NMS CDR files and 
 * writes those records to an output file
 * Deployment: 
 * This sould be run in the TP DB server only
 * 
 * @author sridhar.sindiri
 *
 */
public class RBTViralClient extends Thread{

	private static Logger logger = Logger.getLogger(RBTViralClient.class);
	
	private static boolean m_continue = true;
	private static long m_nextInitTime = -1;
	public static final String m_paramType = "VIRAL";
	private static HashMap<String,String> m_playerCdrPathMap = new HashMap<String, String>();
	private static Hashtable<String, Integer> m_week = new Hashtable<String, Integer>();
	private static ArrayList<ArrayList<String>> m_blackListTimes = new ArrayList<ArrayList<String>>();

	public static long m_linesToRead = 1000;

	//player xml params need to ignore RRBT entries
	protected static String _rrbtCalledPartyPrefix = null;
	protected static boolean _playerMultipleCallLegPolicy = true;

	/**
	 * Sync object
	 */
	private static Object syncObject = new Object();

	private static RBTViralClient instance = null;

	public static void main(String[] args){
		RBTViralClient viral = RBTViralClient.getInstance();
		String isViralClientScheduled = System.getenv("VIRAL_CLIENT_SCHEDULED");
		if("true".equals(isViralClientScheduled)) {
			viral.process();
		}
		else
		{
			viral.setName("VIRAL_CLIENT");
			viral.start();
		}
	}

	private RBTViralClient() {
		m_week.put("SUN", new Integer(1));
		m_week.put("MON", new Integer(2));
		m_week.put("TUE", new Integer(3));
		m_week.put("WED", new Integer(4));
		m_week.put("THU", new Integer(5));
		m_week.put("FRI", new Integer(6));
		m_week.put("SAT", new Integer(7));

		for(int k=0; k<=7; k++) {
			m_blackListTimes.add(new ArrayList<String>());
		}
	}

	public static RBTViralClient getInstance() {
		if (instance == null) {
			synchronized (syncObject) {
				if (instance == null) {
					try {
						instance = new RBTViralClient();
					}
					catch (Throwable e) {
						logger.error("", e);
						instance = null;
					}
				}
			}
		}
		return instance;
	}

	public void process() {
		if(isTimeNowBlackOut()) {
			logger.info(" Not processing as blackOutPeriod ");
			return;
		}

		Iterator<String> osc = sortIteratorDesc(m_playerCdrPathMap.keySet().iterator());
		while(osc.hasNext()) {
			try {
				long time = (2 * 60 * 1000L);
				try {
					String readLastNMinutesRecords = RBTViralConfigManager.getInstance().getParameter("READ_LAST_N_MINUTES_RECORDS");
					int interval = Integer.parseInt(readLastNMinutesRecords);
					time = interval * 60 * 1000;
				} catch(NumberFormatException e) {
					time = (2 * 60 * 1000);
				}
				long processRecordsAfterTime = System.currentTimeMillis() - time;
				logger.info("Oldest record to be processed in this run " + new Date(processRecordsAfterTime));
		
				String oscPath = osc.next();
				String nmsPath = m_playerCdrPathMap.get(oscPath);
		
				File[] oscCdrs = getLatestFiles(oscPath, processRecordsAfterTime);
				File[] nmsCdrs = getLatestFiles(nmsPath, processRecordsAfterTime);
		
				if(oscCdrs == null || nmsCdrs == null || oscCdrs.length <=0 || nmsCdrs.length <=0)
				{
					logger.info("Valid CDRs not obtained for oscPath "+oscPath + "- length [" + oscCdrs.length+ "] nmsPath " +nmsPath + " - length ["+nmsCdrs.length + "]");
					continue;
				}
		
				logger.info("Valid CDRs obtained for oscPath "+oscPath + "- length [" + oscCdrs.length+ "] nmsPath " +nmsPath + " - length ["+nmsCdrs.length + "]");
		
				String linesToRead = RBTViralConfigManager.getInstance().getParameter("LINES_TO_READ");
		
				try {
					m_linesToRead = Long.parseLong(linesToRead);
				} catch(Exception e) {
					//safety check
					m_linesToRead = 1000;
				}
		
				//read only 200 lines from each cdr file if there are 5 cdr files and lines to read is 1000
				m_linesToRead = m_linesToRead / oscCdrs.length;
		
				int nn = oscCdrs.length / nmsCdrs.length;
		
				OSCnNMSLogExtracter logFileExtractor = new OSCnNMSLogExtracter(); 
				for(int i=0; i<oscCdrs.length;i++)
				{
					TPLogFile logFile = new OSCLogFile(oscCdrs[i], m_linesToRead, logFileExtractor, processRecordsAfterTime);
					logFile.getLatestRecords();
				}
		
				for(int i=0; i<nmsCdrs.length;i++)
				{
					TPLogFile logFile = new NMSLogFile(nmsCdrs[i], m_linesToRead, nn, logFileExtractor);
					logFile.getLatestRecords();
				}
				
				OSCLogFile.processedCallerIds.clear();

				logFileExtractor.close();
				//writes onto the ftp server
				logFileExtractor.ftpUploadFile();
			} catch(Exception e) {
				
			}
		}
	}
	
	public void run(){

		while(m_continue)
		{
			try {
				if(m_nextInitTime == -1 || (System.currentTimeMillis()  + 10000 >= m_nextInitTime))
				{
					initialize();
					m_nextInitTime = getnexttime(1);
					if(m_playerCdrPathMap.size() <= 0) {
						logger.info("Telephony IPs could not be retrieved exiting !!!!!");
						System.exit(-1);
					}

					logger.info("Telephony servers resolved to "+m_playerCdrPathMap.toString());
				}
				process();
				String sleepTime = RBTViralConfigManager.getInstance().getParameter("SLEEP_INTERVAL_MIN");

				try {
					logger.info("Sleeping for " + sleepTime + " minutes ");
					Thread.sleep(Integer.parseInt(sleepTime) * 60 * 1000);
				} catch(Exception e) {
					//ignore
				}
			} catch(Exception e) {
				logger.error("", e);
			}
		}

	}

	private boolean initialize(){

		List<String> playerIPs = parsePlayerXML();
		if(playerIPs != null) {

			logger.info("Player IPs got from Player XML "+playerIPs.toString());

			String oscCDRPaths = RBTViralConfigManager.getInstance().getParameter("OSCCDR_PATH");
			if(StringUtils.isNotEmpty(oscCDRPaths)) {
				String[] cdrPath = oscCDRPaths.split(",");
				for(int i=0; i<cdrPath.length; i++) {
					String[] s = cdrPath[i].split(";");
					if(s.length < 2) {
						logger.info("OSCCDR config not in right format " + cdrPath[i]);
					} else {
						if(!playerIPs.remove(s[0])) {
							logger.info("Telephony ip "+s[0]+ " not present in Player XMLs");
						}
						String nmsIP = getNMSCDRPath(s[0], s[1]);
						if(nmsIP == null) {
							logger.info("Could not find NMS CDR path for "+s[0]);
							continue;
						}

						String ivmPath = s[1].substring(0, s[1].lastIndexOf("osccdr")) ;
						m_playerCdrPathMap.put(s[1], ivmPath.replaceFirst(s[0], nmsIP) + "cdr");
					}

				}

			}
			initializeBlockOutPeriod();
			return true;
		}
		else
		{
			logger.info("No Player IPs found from Player XMLs");
		}
		return false;
	}

	private String getNMSCDRPath(String ip, String path) {
		String ivmPath = path.substring(0, path.lastIndexOf("osccdr")) ;
		String ivmFile = ivmPath + "config/ivm.xml";
//		String xml = getXMLAsString(configPath);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new FileInputStream(ivmFile));

			Element nodeList = (Element)document.getElementsByTagName("Config").item(0);
			NodeList list = nodeList.getElementsByTagName("Component");
			Element agentElement = null;
			Element nmsElement = null;
			for (int i = 0; i < list.getLength(); i++) {
				Element ele = (Element) list.item(i);
				String name = ele.getAttribute("name");
				if(name.equalsIgnoreCase("agent"))
					agentElement = ele;
				if(name.equalsIgnoreCase("nms"))
					nmsElement = ele;
			}
			if(agentElement != null) {
				Element controllerEle = (Element)agentElement.getElementsByTagName("Controller").item(0);
				String type = controllerEle.getAttribute("type");
				if(type != null && type.equalsIgnoreCase("msp")) {
					logger.info("Telephony "+ip + " has MSP cards");
					return ip;
				}
			}
			if(nmsElement != null) {
				Element ele = (Element)nmsElement.getElementsByTagName("General").item(0);
				if(ele != null) {
					String mode = ele.getAttribute("SignallingMode");
					if(mode == null || (!mode.equalsIgnoreCase("Standalone") && !mode.equalsIgnoreCase("Primary")))
					{
						ele = (Element)nmsElement.getElementsByTagName("Signalling").item(0);
						ele = (Element)ele.getElementsByTagName("Controllers").item(0);
						ele = (Element)ele.getElementsByTagName("Primary").item(0);
						if(ele != null)
							return ele.getAttribute("ip");
					}
					else
						return ip;
				}
			}

		} catch (Throwable t) {
			logger.error("", t);
		}

		return null;
	}

	/**
	 * @return
	 */
	private List<String> parsePlayerXML() {
		String xmlPaths = RBTViralConfigManager.getInstance().getParameter("PLAYER_XML_PATH");
		if(StringUtils.isEmpty(xmlPaths)) {
			return null;
		}

		List<String> result = new ArrayList<String>();

//		if(xmlPaths != null && xmlPaths.getValue() != null)
		String[] paths = xmlPaths.split(",");

		for(int i=0; i<paths.length; i++) {
			String xmlPath = paths[i] + "/" + "rbtplayer.xml";
			List<String> list = getTelephonyIPs(xmlPath);
			if(list != null && list.size() > 0)
				result.addAll(list);
		}
		return result;
	}

	private List<String> getTelephonyIPs(String xmlPath) {
		List<String> result = new ArrayList<String>();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
//			InputSource is = new InputSource(new FileInputStream(xmlPath));
			Document document = builder.parse(new FileInputStream(xmlPath));

			Element nodeList = (Element)document.getElementsByTagName("RBT").item(0);
			Element element = (Element)nodeList.getElementsByTagName("memcached").item(0);
			//			element = (Element)nodeList.getElementsByTagName("pool").item(0);
			NodeList list = element.getElementsByTagName("pool");
			if(list !=  null && list.getLength() > 0) {
				for(int j=0; j<list.getLength(); j++) {
					element = (Element) list.item(j);
					if (element != null) {
						NodeList propertyNodeList = element.getElementsByTagName("server");
						for (int i = 0; i < propertyNodeList.getLength(); i++) {
							Element propertyElement = (Element) propertyNodeList.item(i);
							String ip = propertyElement.getAttribute("ip");
							result.add(ip);
						}
					}
				}
			}
			//populating RRBTParams added by Sreekar
			Element commonElement = (Element)nodeList.getElementsByTagName("Common").item(0);
			NodeList propertyElementList = commonElement.getElementsByTagName("property");
			if(propertyElementList != null && propertyElementList.getLength() > 0) {
				Element tempElement;
				for(int i = 0; i < propertyElementList.getLength(); i++) {
					tempElement = (Element) propertyElementList.item(i);
					String name = tempElement.getAttribute("name");
					if(name != null && name.equals("RRBT_CALLED_PARTY_PREFIX"))
						_rrbtCalledPartyPrefix = tempElement.getAttribute("value");
					else if(name != null && name.equals("MULTIPLE_CALL_LEG_POLICY"))
						_playerMultipleCallLegPolicy = tempElement.getAttribute("value").equalsIgnoreCase("true");
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}

		return result;
	}

	private void initializeBlockOutPeriod() {
		String blockOutPeriod = RBTViralConfigManager.getInstance().getParameter("BLOCK_OUT_PERIOD");
		if(StringUtils.isEmpty(blockOutPeriod)) {
			logger.info("No BlackOut Configured");
			return;
		}

		String[] black = blockOutPeriod.split(",");
		for(int i=0; i<black.length; i++) {
			if(black[i].indexOf("[") <= -1) {
				logger.info("No BlackOut Time Configured" + black[i]);
				continue;
			}
			ArrayList<Integer> days = getDays(black[i].substring(0, black[i].indexOf("[")));
			if(days != null && days.size() > 0) {
				ArrayList<String> times = getTimes(black[i].substring(black[i].indexOf("[")));
				for(int j=0;j<days.size(); j++)
					m_blackListTimes.add(days.get(j).intValue(), times);
			}
		}

		logger.info("BlackListTimes initialized "+m_blackListTimes.toString());

	}

	private ArrayList<Integer> getDays(String s) {
		ArrayList<Integer> array = new ArrayList<Integer>();
		if(s.indexOf("-") > -1) {
			try {
				String day1 =  s.substring(0, s.indexOf("-"));
				String day2 = s.substring(s.indexOf("-") + 1);
				if(!m_week.containsKey(day1) || !m_week.containsKey(day2))
				{
					logger.info("Invalid week specified !!!!" + s);
					return null;
				}

				int startDay = m_week.get(day1).intValue();
				int endDay = m_week.get(day2).intValue();

				if(endDay > startDay) {
					for(int t = startDay; t<=endDay; t++)
						array.add(new Integer(startDay++));
				} else {
					for(int t = startDay; t<=7; t++)
						array.add(new Integer(startDay++));

					for(int t = 1; t<=endDay; t++)
						array.add(new Integer(t));
				}

			} catch(Throwable t){

			}
		} else {
			if(m_week.containsKey(s))
				array.add(m_week.get(s));
			else
				logger.info("Invalid week specified !!!!" + s);

		}

		logger.info("" + array.toString());
		return array;
	}

	private ArrayList<String> getTimes(String s)
	{
		ArrayList<String> array = new ArrayList<String>();
		s = s.substring(1, s.length() -1);
		String[] ss = s.split(";");
		for(int i =0; i<ss.length; i++)
		{
			if(ss[i].indexOf("-") > -1)
			{
				try
				{
					int startTime = Integer.parseInt(ss[i].substring(0, ss[i].indexOf("-")));
					int endTime = Integer.parseInt(ss[i].substring(ss[i].indexOf("-") + 1));

					if(startTime > 23 || endTime > 23)
					{
						logger.info("Invalid time specified !!!!" + s);
						continue;
					}
					else if(endTime > startTime)
						for(int t = startTime; t<=endTime; t++)
							array.add(""+startTime++);
					else
					{
						for(int t = startTime; t<=23; t++)
							array.add(""+startTime++);

						for(int t = 0; t<=endTime; t++)
							array.add(""+t);

					}

				}
				catch(Throwable t){

				}
			}
			else
			{
				try
				{
					int n = Integer.parseInt(ss[i]);
					if(n >=0 && n<=23)
						array.add(ss[i]);
					else
						logger.info("Invalid time specified !!!!" + s);
				}
				catch(Throwable t){
					logger.info("Invalid time specified !!!!" + s);
				}
			}

		}

		return array;

	}

	private long getnexttime(int hour)
	{
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, hour);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);

		long nexttime = now.getTime().getTime();
		if (nexttime < System.currentTimeMillis()) {
			nexttime = nexttime + (24 * 3600 * 1000);
		}
		return nexttime;
	}

	private File[] getLatestFiles(String cdr, final long processRecordsAfterTime) {
		File cdrDir = new File(cdr);
		logger.info("RBTViral::CDR directory parsed " + cdrDir);

		File[] list = cdrDir.getAbsoluteFile().listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				if(!name.endsWith(".txt")) {
					return false;
				}
				if(!name.startsWith("C")) {
					return false;
				}
				File f = new File(dir + File.separator + name);
				if(f.lastModified() > processRecordsAfterTime) {
					return true;
				}
				return false;
			}
		});
		
		logger.info("RBTViral::Files in CDR directory parsed " + cdrDir +" : " + list.length);
		return list;
	}

	private static boolean isTimeNowBlackOut()
	{
		Calendar cal = Calendar.getInstance();
		ArrayList<String> black = m_blackListTimes.get(cal.get(Calendar.DAY_OF_WEEK));
		logger.info("BlackOut Chkd against "+black.toString());
		if(black.contains(""+cal.get(Calendar.HOUR_OF_DAY)))
			return true;

		return false;
	}


	public void stopThread()
	{
		m_continue = false;
	}

	private Iterator<String> sortIteratorDesc(Iterator<String> i) {
		ArrayList<String> a = new ArrayList<String>();
		while (i.hasNext())
			a.add(i.next());

		Collections.sort(a, new Comparator<Object>() {
			public int compare(Object a, Object b)
			{
				String aStr, bStr;
				aStr = (String) a;
				bStr = (String) b;
				int val = aStr.compareTo(bStr);
				if (val != 0)
					return (-val);
				else
					return val;
			}
		});
		return (a.iterator());
	}
	
	public String subID(String subscriberID)
	{
		String calledPrefix = RBTViralConfigManager.getInstance().getParameter("CALLED_PREFIX");
		if(StringUtils.isNotEmpty(calledPrefix) && subscriberID != null){
			if(subscriberID.length() > calledPrefix.length() && subscriberID.startsWith(calledPrefix)) {
				subscriberID = subscriberID.substring(calledPrefix.length());
			}
		}
		return subscriberID;
	}
}
