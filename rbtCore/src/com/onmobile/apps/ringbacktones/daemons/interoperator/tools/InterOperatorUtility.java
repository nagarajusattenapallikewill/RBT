package com.onmobile.apps.ringbacktones.daemons.interoperator.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.eventlogging.RDCEventLoggerPreMNP;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.subscriptions.Utility;
import com.onmobile.reporting.framework.capture.api.Configuration;
public class InterOperatorUtility implements Constants
{

	static Logger logger = Logger.getLogger(InterOperatorUtility.class);
	static Logger copyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestDao.class);
	static Logger oldCopyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestBean.class);
		
	private static DocumentBuilder documentBuilder = null;
	public static String localDir = null;
	
	private static Map<String, Integer> operatorMNPNameOperatorIDMap = new HashMap<String, Integer>();
	private static Map<String, String> operatorIdOperatorRBTNameMap = new HashMap<String, String>();	
	public static ArrayList<Integer> unidentifiedStatusList = new ArrayList<Integer>();
	public static SimpleDateFormat sdfLog = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static Object lock = new Object();
	public static ArrayList folders_to_zip = new ArrayList();
	public static SimpleDateFormat zileFileDateFormat = new SimpleDateFormat("yyyy-MMM-dd");
	private static String m_transDir = "./Trans";
	private static String m_eventLoggingDir = "./EventLogs";
	private static SimpleDateFormat m_format = new SimpleDateFormat("ddMMyyyy");
	static private HashMap<String, HashSet<String>> operatorCopyAllowedMap = new HashMap<String, HashSet<String>>();
	public static HashMap<String, String> operatorNameUrlMap = new HashMap<String, String>();
	public static RDCEventLoggerPreMNP eventLogger = null;
	public static HashMap<Integer, Integer> operatorIdsInterchangeMap = new HashMap<Integer, Integer>();
	public static HashMap<Integer, HashSet<String>> operatorIdInterchangeCirclesMap = new HashMap<Integer, HashSet<String>>();
	
	static
	{
		try
		{
			if (documentBuilder == null)
				documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

			localDir = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "LOCAL_DIRECTORY").getValue();
			File file = new File(InterOperatorUtility.localDir);
			if (!file.exists())
				file.mkdirs();
			Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "GATHERER_PATH", null);
			String 	m_gathererPath =  null;
			if(parameter != null)
				m_gathererPath =  parameter.getValue();
			
			m_eventLoggingDir = m_gathererPath + "/EventLogs";
			new File(m_eventLoggingDir).mkdirs();
			m_transDir = m_gathererPath + "/Trans";
			new File(m_transDir).mkdirs(); 
		     
			folders_to_zip.add(m_gathererPath + "/CopyTrans");
			folders_to_zip.add(m_gathererPath + "/EventLogs");
			unidentifiedStatusList.add(0);unidentifiedStatusList.add(1);
			initOperatorCopyAllowedMap();
			initOperatorNameAndUrlMap();
			initializeEventLogger();
			initializeInterchageOperatorIdCircleMap();
		}
		catch (ParserConfigurationException e)
		{
		}
	}
	
	
	/**
	 * @param file
	 */
	public static void processXmlFile(File file, String type) 
	{
		try 
		{
			Document document = null;
			synchronized(lock)
			{
				document = documentBuilder.parse(file);
			}

			Element rootElement = (Element) document.getElementsByTagName("MnpCustomerCircleInfo").item(0);
			Element successRecordsElem = (Element) rootElement.getElementsByTagName("SuccessRecords").item(0);
			NodeList successList = successRecordsElem.getElementsByTagName("SuccessRecord");
			for (int i = 0; i < successList.getLength(); i++)
			{
				try
				{
					Element successElem = (Element) successList.item(i);
					long msisdn = Long.parseLong(successElem.getElementsByTagName("msisdn").item(0).getFirstChild().getNodeValue());
					String operator = successElem.getElementsByTagName("customer").item(0).getFirstChild().getNodeValue();
					String circleId = successElem.getElementsByTagName("circle").item(0).getFirstChild().getNodeValue();
					int operatorID = getOperatorIDFromMNPOperatorName(operator);
					operatorID = getInterchangedOperatorId(operatorID, circleId);
					List<InterOperatorCopyRequestBean> pendingCopyRequestBeans = InterOperatorCopyRequestDao.listForCopierAndInStatus(msisdn, unidentifiedStatusList);
					if (pendingCopyRequestBeans != null)
					{
						for (InterOperatorCopyRequestBean requestBean : pendingCopyRequestBeans)
						{
							if(operatorID == 0)
							{
								requestBean.setCopierMdn(msisdn);
								requestBean.setStatus(3);
								requestBean.setMnpResponseTime(Calendar.getInstance().getTime());
								requestBean.setMnpResponseType(type);
								copyTransactionLogger.info(getLoggableBean(requestBean));
								oldCopyTransactionLogger.info(getTransLoggableBean(requestBean));
								InterOperatorCopyRequestDao.delete(requestBean.getCopyId());
							}
							else
							{
								requestBean.setCopierMdn(msisdn);
								requestBean.setCopierOperatorId(operatorID);
								requestBean.setStatus(2);
								requestBean.setMnpResponseTime(Calendar.getInstance().getTime());
								requestBean.setMnpResponseType(type);
								InterOperatorCopyRequestDao.update(requestBean);
							}
						}
					}
				}
				catch(Exception e)
				{
					logger.error("Exception", e);
				}
			}

			Element failureRecordsElem = (Element) rootElement.getElementsByTagName("FailureRecords").item(0);
			NodeList failureList = failureRecordsElem.getElementsByTagName("msisdn");
			for (int i = 0; i < failureList.getLength(); i++)
			{
				try
				{
					long msisdn = Long.parseLong(failureList.item(i).getFirstChild().getNodeValue());
				
					List<InterOperatorCopyRequestBean> pendingCopyRequestBeans = InterOperatorCopyRequestDao.listForCopierAndStatus(msisdn, 1);
					if (pendingCopyRequestBeans != null)
					{
						for (InterOperatorCopyRequestBean requestBean : pendingCopyRequestBeans)
						{
							requestBean.setCopierMdn(msisdn);
							requestBean.setStatus(3);
							requestBean.setMnpResponseTime(Calendar.getInstance().getTime());
							requestBean.setMnpResponseType(type);
							copyTransactionLogger.info(getLoggableBean(requestBean));
							oldCopyTransactionLogger.info(getTransLoggableBean(requestBean));
							InterOperatorCopyRequestDao.delete(requestBean.getCopyId());
						}
					}
				}
				catch(Exception e)
				{
					logger.error("Exception", e);
				}
			}

			String targetPath = file.getAbsolutePath();
			if (targetPath.endsWith(".tmp"))
				targetPath = targetPath.substring(0, targetPath.indexOf(".tmp"));
			
			copyFile(file, new File(targetPath + ".done"));
			file.delete();
		} 
		catch (IOException e) {
			logger.error("Exception", e);
		} catch (SAXException e) {
			logger.error("Exception", e);
		}
	}

	private static void initOperatorCopyAllowedMap()
	{
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "OPERATOR_COPY_ALLOWED_MAP", null);
		if(parameter == null || parameter.getValue() == null)
			return;
		String rawString = parameter.getValue();
		StringTokenizer stkParent = new StringTokenizer(rawString, ";");
		while(stkParent.hasMoreTokens())
		{
			StringTokenizer stkChild = new StringTokenizer(stkParent.nextToken(), ":");
			if(stkChild.countTokens() != 2)
				continue;
			String operatorMain = stkChild.nextToken();
			String operatorMapped = stkChild.nextToken();
			StringTokenizer innerMapTokens = new StringTokenizer(operatorMapped, ",");
			HashSet<String> innerMap  = new HashSet<String>();
			while(innerMapTokens.hasMoreTokens())
				innerMap.add(innerMapTokens.nextToken());
			operatorCopyAllowedMap.put(operatorMain, innerMap);
		}	
	}
	
	public static boolean isCopyAllowedBetweenOperator(InterOperatorCopyRequestBean copyRequest)
	{
		if(operatorCopyAllowedMap.size() == 0)
			return true;
		String copierOperatorName = getRBTOperatorNameFromOperatorID(copyRequest.getCopierOperatorId()+"");
		String copieeOperatorName = getRBTOperatorNameFromOperatorID(copyRequest.getCopieeOperatorId()+"");
		HashSet<String> mappedOperators = operatorCopyAllowedMap.get(copierOperatorName);
		if(mappedOperators == null || mappedOperators.size() ==0 || mappedOperators.contains(copieeOperatorName))
			return true;
		return false;
		
	}

	/**
	 * @param operator
	 * @return
	 */
	public static int getOperatorIDFromMNPOperatorName(String operator) 
	{
		int operatorID = 0;
		if (operatorMNPNameOperatorIDMap.isEmpty())
		{
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "OPERATOR_MNP_NAME_OPERATOR_ID_MAP");
			if (params != null && params.getValue() != null)
			{
				String opNameOpIDMapStr = params.getValue();
				String[] nameIDPairs = opNameOpIDMapStr.split(";");
				for (String eachPair : nameIDPairs)
				{
					String[] str = eachPair.split(",");
					operatorMNPNameOperatorIDMap.put(str[0], Integer.parseInt(str[1]));
				}
				logger.info("operatorMNPNameOperatorIDMap="+operatorMNPNameOperatorIDMap);
			}
		}

		if (operatorMNPNameOperatorIDMap.containsKey(operator))
			operatorID = operatorMNPNameOperatorIDMap.get(operator);

		return operatorID;
	}
	public static String getRBTOperatorNameFromOperatorID(String operatorId) 
	{
		String operatorRBTName = null;
		if (operatorIdOperatorRBTNameMap.isEmpty())
		{
			Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "OPERATOR_ID_OPERATOR_RBT_NAME_MAP");
			if (params != null && params.getValue() != null)
			{
				String opNameOpIDMapStr = params.getValue();
				String[] nameIDPairs = opNameOpIDMapStr.split(";");
				for (String eachPair : nameIDPairs)
				{
					String[] str = eachPair.split(",");
					operatorIdOperatorRBTNameMap.put(str[0], str[1]);
				}
				logger.info("operatorIdOperatorRBTNameMap="+operatorIdOperatorRBTNameMap);
			}
		}

		if (operatorIdOperatorRBTNameMap.containsKey(operatorId))
			operatorRBTName = operatorIdOperatorRBTNameMap.get(operatorId);

		return operatorRBTName;
	}	

	/**
	 * @param source
	 * @param destination
	 * @throws IOException
	 */
	public static void copyFile(File source, File destination) throws IOException
	{
		FileChannel sourceFileChannel = null;
		FileChannel destinationFileChannel = null;
		try
		{
			sourceFileChannel = (new FileInputStream(source)).getChannel();
			destinationFileChannel = (new FileOutputStream(destination)).getChannel();
			sourceFileChannel.transferTo(0, source.length(), destinationFileChannel);
		}
		catch (IOException e)
		{
			throw e;
		}
		finally
		{
			try
			{
				if (sourceFileChannel != null)
					sourceFileChannel.close();
				if (destinationFileChannel != null)
					destinationFileChannel.close();
			}
			catch (IOException e)
			{
				logger.error(e);
			}
		}
	}
	
	public static String getLoggableBean(InterOperatorCopyRequestBean cR)
	{
		StringBuilder strB = new StringBuilder();
		strB.append(getFotmattedDate(Calendar.getInstance().getTime()));strB.append(",");
		strB.append(cR.getCopyId());strB.append(",");
		strB.append(cR.getStatus());strB.append(",");
		strB.append(cR.getCopierMdn());strB.append(",");
		strB.append(cR.getCopierOperatorId());strB.append(",");
		strB.append(cR.getTargetContentId());strB.append(",");
		strB.append(getFotmattedDate(cR.getRequestTime()));strB.append(",");
		strB.append(cR.getCopieeMdn());strB.append(",");
		strB.append(cR.getCopieeOperatorId());strB.append(",");
		strB.append(cR.getSourceContentId());strB.append(",");
		strB.append(cR.getCopyType());strB.append(",");
		strB.append(cR.getKeyPressed());strB.append(",");
		strB.append(getFotmattedDate(cR.getMnpRequestTime()));strB.append(",");
		strB.append(getFotmattedDate(cR.getMnpResponseTime()));strB.append(",");
		strB.append(getFotmattedDate(cR.getContentResolveTime()));strB.append(",");
		strB.append(getFotmattedDate(cR.getRequestTransferTime()));strB.append(",");
		strB.append(cR.getTransferRetryCount());strB.append(",");
		strB.append(cR.getMnpRequestType());strB.append(",");
		strB.append(cR.getMnpResponseType());strB.append(",");
		strB.append(cR.getSourceContentDetails());strB.append(",");
		strB.append(cR.getSourceSongName());strB.append(",");
		strB.append(cR.getSourcePromoCode());strB.append(",");
		strB.append(cR.getInfo());strB.append(",");
		return strB.toString();
	}
	
	
	//writeTrans(vst.subID(), vst.callerID(), getWavFile(vst.clipID()), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), 
	//Utility.getSubscriberOperator(Utility.subID(vst.callerID()), null), " - ", "-",updateType, optInCopy,keyPressed);
	//writeTrans(vst.subID(), vst.callerID(), getWavFile(vst.clipID()), "-", Tools.getFormattedDate(vst.sentTime(),"yyyy-MM-dd HH:mm:ss"), 
	//Utility.getSubscriberOperator(Utility.subID(vst.callerID()), null)," - ", "-",updateType, optInCopy,keyPressed);
	//writeTrans(vst.subID(), vst.callerID(), getWavFile(vst.clipID()), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), 
	//Utility.getSubscriberOperator(Utility.subID(vst.callerID()), null), " - ", "-",updateType, optInCopy,keyPressed);
	//writeTrans(vst.subID(), vst.callerID(), getWavFile(vst.clipID()), "-", Tools.getFormattedDate(vst.sentTime(),"yyyy-MM-dd HH:mm:ss"),  
	//Utility.getSubscriberOperator(Utility.subID(vst.callerID()), null)," - ", "-",updateType, optInCopy,keyPressed);
	
	public static String getTransLoggableBean(InterOperatorCopyRequestBean cR)
	{
		StringBuilder strB = new StringBuilder();
		strB.append(cR.getCopieeMdn());strB.append(",");
		strB.append(cR.getCopierMdn());strB.append(",");
		strB.append(cR.getTargetContentId());strB.append(",");
		strB.append("-");strB.append(",");
		strB.append(getFotmattedDate(cR.getRequestTime()));strB.append(",");
		strB.append(getRBTOperatorNameFromOperatorID(cR.getCopierOperatorId()+""));strB.append(",");
		strB.append("-");strB.append(",");
		strB.append("-");strB.append(",");
		
		String copyType = "DIRECTCOPY";
		String keyPressed = "s";
		String updateType = null;
		if(cR.getCopyType() != null && cR.getCopyType().equalsIgnoreCase("COPYSTAR"))
		{
			copyType = "OPTINCOPY";
			keyPressed = "s9";
		}
		if(cR.getKeyPressed() != null)
			keyPressed = cR.getKeyPressed();
		if(cR.getStatus()== 6)
			updateType = "COPYTRANSFERRED";
		else if(cR.getStatus()== 7)
			updateType = "COPYTRANSFERFAILED";
		else 
			updateType = "COPYFAILED";
		strB.append(updateType);strB.append(",");
		strB.append(copyType);strB.append(",");
		strB.append(keyPressed);strB.append(",");
		
		return strB.toString();
	}
	
	public static String getFotmattedDate(Date date)
	{
		if(date == null)
			return null;
		else 
			return sdfLog.format(date);
	}
	
	public static String addInterOperatorCopyRequestToDB(HttpServletRequest request) throws Exception
	{
		
		String response = cross_copy_Resp_Err;
		HashMap<String, String> requestMap = new HashMap<String, String>();
		if(request != null && request.getParameterMap() != null)
		{
			Enumeration<String> paramNames = request.getParameterNames();
			while(paramNames.hasMoreElements())
			{
				String key = paramNames.nextElement();
				String value = (String)request.getParameter(key);
				if(key != null && value != null)
					requestMap.put(key.toUpperCase(), value);	
			}	
		}
		logger.error("requestMap="+requestMap);
		String subscriberID = requestMap.get(param_SUBID);
		String callerID = requestMap.get(param_CALLERID);
		String clipID = requestMap.get(param_CLIPID);
		String smsType = requestMap.get(param_SMSTYPE);
		String selBy = requestMap.get(param_SELBY);
		String songName = requestMap.get(param_SONGNAME);
		String toneCode = requestMap.get(param_TONECODE);
		String keyPressed = requestMap.get(param_KEYPRESSED);
		String sourceOp = requestMap.get(param_SOURCEOP);
		
		if(subscriberID == null || callerID == null || sourceOp == null || clipID == null)
		{
			logger.error("Insufficient parameters");
			return response;
		}
		try
		{	
			InterOperatorCopyRequestBean reqBean=new InterOperatorCopyRequestBean();
			
			reqBean.setStatus(0);
			reqBean.setCopierMdn(Long.parseLong(callerID));
			reqBean.setCopierOperatorId(0);
			reqBean.setRequestTime(Calendar.getInstance().getTime());
			reqBean.setCopieeMdn(Long.parseLong(subscriberID));
			reqBean.setCopieeOperatorId(Integer.parseInt(sourceOp));
			if(clipID!=null)
				reqBean.setSourceContentId(clipID.split(":")[0]);
			
			reqBean.setSourceContentDetails(clipID);
			reqBean.setCopyType(smsType);
			reqBean.setKeyPressed(keyPressed);
			reqBean.setTransferRetryCount(0);
			reqBean.setSourceSongName(songName);
			reqBean.setSourcePromoCode(toneCode);
			if(sourceOp != null)
			{
				String sourceOperatorName = InterOperatorUtility.getRBTOperatorNameFromOperatorID(sourceOp);
				if(sourceOperatorName != null)
					selBy = sourceOperatorName + "_XCOPY";
			}	
			reqBean.setSourceMode(selBy);
			InterOperatorCopyRequestDao.save(reqBean);
			response = cross_copy_Resp_Success;
			
		}
		catch(Exception e)
		{
			logger.error("Exception while adding interoperator copy request to DB.",e);
			response=cross_copy_Resp_Err;
		}
		return response;
	}
	
	public static void makeReportingFiles()
	{
		logger.info("Entering");
		int gather_hour = getParameterAsInt("RDC", "GATHERER_HOUR", 1);
		Calendar cal = Calendar.getInstance();
		int current_hour = cal.get(Calendar.HOUR_OF_DAY);
		if (current_hour >= gather_hour)
		{
			String zip_file = getZipFileName();
			if (!(new File(zip_file).exists()))
			{
				logger.info("yesterday's zip " + zip_file + " not found. So calling gather()");
				gather();
			}
		}
		logger.info("Exiting");
	}

	private static void gather()
	{
		logger.info("Entering");
		try
		{
			collectCopyTransFiles();
			collectEventLoggingFiles();

			String zipfilename = createzip(null);
			logger.info("zipfilename " + zipfilename);
			if (zipfilename == null)
			{
				logger.info("No Zip file created");
				return;
			}
			uploadZipFilesToSpiderDir(zipfilename);

		}
		catch (Exception e)
		{
			logger.error("", e);
		}
		logger.info("Exiting");
	}

	private static void uploadZipFilesToSpiderDir(String zipfile)
	{
		File file_to_copy = new File(zipfile);
		String m_spider_dir = getParameterAsString("RDC", "SPIDER_DIR", null);
		Tools.moveFile(m_spider_dir, file_to_copy);
		File[] zip_files = getZipFileList();
		if (zip_files == null || zip_files.length <= 0)
			return;
		for (int i = 0; i < zip_files.length; i++)
		{
			String date = "";
			StringTokenizer tokens = new StringTokenizer(zip_files[i].getName(), "_");
			while (tokens.hasMoreTokens())
				date = tokens.nextToken();
			Date zipFileDate = Tools.getdate(date.substring(0, date.indexOf(".")), "yyyy-MMM-dd");
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -2);
			Date compDate = Tools.getdate(Tools.getChangedFormatDate(cal.getTime()), "yyyy-MM-dd");
			logger.info(" zipfile date " + zipFileDate + " cal.getTime() "+ cal.getTime());
			if (compDate.after(zipFileDate))
				zip_files[i].delete();
		}
	}

	private static String getZipFileName()
	{
		String cust = getParameterAsString("RDC","CUST_NAME",null);
		String site = getParameterAsString("RDC","SITE_NAME",null);
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);// yesterday
		String datename = Tools.getDateAsName(cal.getTime());
		String 	m_gathererPath = getParameterAsString("RDC", "GATHERER_PATH", null);
		return m_gathererPath + "/RBTGatherer_" + cust + "_" + site + "_"+ datename + ".zip";
	}

	private static File[] getZipFileList()
	{
		String 	m_gathererPath = getParameterAsString("RDC", "GATHERER_PATH", null);
		
		File[] list = new File(m_gathererPath + "/").listFiles(new FilenameFilter()
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

	public static String createzip(String fileNamePrefix)
	{

		if (folders_to_zip.size() == 0)
			return null;
		
		boolean zipped = false;
		String zipFileName = "";
		if (fileNamePrefix != null)
			zipFileName = zipFileName.trim() + fileNamePrefix + "-";

		String m_cust = getParameterAsString("RDC", "CUST_NAME", null);
	    String m_site = getParameterAsString("RDC", "SITE_NAME", null);
		Calendar cal = Calendar.getInstance();
	    cal.add(Calendar.DATE, -1);//yesterday
	    String m_datename = zileFileDateFormat.format(cal.getTime());
	    
	    String 	m_gathererPath = getParameterAsString("RDC", "GATHERER_PATH", null);
		zipFileName = m_gathererPath + "/" + zipFileName.trim() + "RBTGatherer_" + m_cust + "_" + m_site + "_" + m_datename + ".zip";

		try
		{
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
			out.setLevel(Deflater.BEST_COMPRESSION);

			if (fileNamePrefix == null)
			{
				File[] files = new File(m_gathererPath).listFiles();
				if (files != null && files.length > 0)
				{
					for (int j = 0; j < files.length; j++)
					{
						if (files[j].getName().endsWith(".htm") || files[j].getName().endsWith(".cfg") || files[j].getName().endsWith(".log")
								|| files[j].getName().endsWith(".xml"))
						{
							add2zip(out, m_gathererPath + "/" + files[j].getName(), null);
							files[j].delete();
						}
					}
				}
				for (int i = 0; i < folders_to_zip.size(); i++)
				{
					String zip_folder = String.valueOf(folders_to_zip.get(i));
					logger.info("Adding to zip folder : " + zip_folder);
					zipped = dir2zip(out, zip_folder);
					if (zipped)
						delete(zip_folder);
					zipped = false;
				}

			}
			else
			{
				zipped = dir2zip(out, m_gathererPath + "/" + "db-Full");
				if (zipped)
					delete(m_gathererPath + "/" + "db-Full");
			}
			out.close();
		}
		catch (FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		return zipFileName;
	}

	private static void delete(String dir)
	{
		File delete_folder = new File(dir);
		String[] files_to_delete = delete_folder.list();
		for (int i = 0; i < files_to_delete.length; i++)
		{
			new File(dir + File.separator + files_to_delete[i]).delete();
		}
		delete_folder.delete();
	}

	public static boolean add2zip(ZipOutputStream out, String fname,String dname)
	{
		try
		{
			String name = null;
			if (dname != null)
				name = new File(dname).getName() + "/" + new File(fname).getName();
			else
				name = new File(fname).getName();
			
			out.putNextEntry(new ZipEntry(name));
			FileInputStream in = new FileInputStream(new File(fname));
			int len;
			byte[] buffer = new byte[18024];
			while ((len = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, len);
			}
			out.closeEntry();
			in.close();
			return true;
		}
		catch (IllegalArgumentException iae)
		{
			iae.printStackTrace();
		}
		catch (FileNotFoundException fnfe)
		{
			fnfe.printStackTrace();
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
		}
		return false;
	}

	public static boolean dir2zip(ZipOutputStream out, String dname)
	{
		File dir = new File(dname);
		String[] filesToZip = dir.list();
		if (filesToZip == null || filesToZip.length <= 0)
		{
			logger.info(dname + " does not have any files");
			return false;
		}
		filesToZip = dir.list();
		File tmp_file;
		for (int i = 0; i < filesToZip.length; i++)
		{
			String fname = dname + File.separator + filesToZip[i];
			logger.info("RBTGatherer zip file trying to be zipped " + fname);
			tmp_file = new File(fname);
			if (tmp_file.isDirectory())
				dir2zip(out, fname);
			else
				add2zip(out, fname, dname);
		}
		return true;
	}
	
	public static String getParameterAsString(String paramType, String paramName, String defaultValue)
	{
		
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(paramType, paramName, defaultValue);
		if(parameter == null || parameter.getValue() == null || parameter.getValue().trim().length() == 0)
			return defaultValue;
		return parameter.getValue().trim();
	}
		
	public static boolean getParameterAsBoolean(String paramType, String paramName, boolean defaultValue) 
	{
		String paramValue = getParameterAsString(paramType, paramName, defaultValue+"");
		return paramValue.equalsIgnoreCase("true") || paramValue.equalsIgnoreCase("on") || paramValue.equalsIgnoreCase("yes");
	}

	public static int getParameterAsInt(String paramType, String paramName, int defaultValue) 
	{
		String paramValue = getParameterAsString(paramType, paramName, defaultValue+"");
		int returnValue = defaultValue;
		try
		{
			returnValue = Integer.parseInt(paramValue);
		}
		catch(Exception e)
		{
		}
		return returnValue;
	}

	private static void collectCopyTransFiles()
	{

		File copyTrans_dir = null;
		String m_gathererPath = getParameterAsString("RDC", "GATHERER_PATH", null);
		m_transDir = m_gathererPath + "/Trans";
		new File(m_transDir).mkdirs();
		if (m_transDir != null)
			copyTrans_dir = new File(m_transDir);
		if (copyTrans_dir.exists())
		{
			File[] copyTrans_list = copyTrans_dir.listFiles(new FilenameFilter()
			{
				public boolean accept(File file, String name)
				{
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.DATE, -1);
					Date today = cal.getTime();
					cal.add(Calendar.DATE, -getParameterAsInt("RDC", "DATA_COLLECTION_DAYS", 5));
					Date yest = cal.getTime();
					if (name.startsWith("COPY_TRANS"))
					{
						try
						{
							String dateStr = name.substring(11, 19);
							Date file_date = m_format.parse(dateStr);
							if (file_date.before(yest))
								return false;
							else if (file_date.before(today))
								return true;
							else
								return false;
						}
						catch (Exception e)
						{
							return false;
						}
					}
					else
					{
						return false;
					}
				}
			});
			if (copyTrans_list != null && copyTrans_list.length > 0)
			{
				logger.info("Copy Trans File collection Started...");
				File copy = new File(m_gathererPath + "/copyTrans");
				if (!copy.exists())
					copy.mkdirs();

				for (int i = 0; i < copyTrans_list.length; i++)
				{
					Tools.moveFile(m_gathererPath + "/copyTrans", copyTrans_list[i]);
				}
				logger.info("CopyTrans File collection Ended...");
			}
		}
	}

	private static void collectEventLoggingFiles()
	{
	 
        File eventLogs_dir = null;

        if (m_eventLoggingDir != null)
       	 eventLogs_dir = new File(m_eventLoggingDir);

        if (eventLogs_dir.exists())
        {
            File[] eventLogs_list = eventLogs_dir.listFiles(new FilenameFilter()
            {
                public boolean accept(File file, String name)
                {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DATE, -1);
                    Date today = cal.getTime();
                    cal.add(Calendar.DATE, -getParameterAsInt("RDC", "DATA_COLLECTION_DAYS", 5));
                    Date yest = cal.getTime();
                    if (name.startsWith("copytrans"))
                    {
                        try
                        {
                            String dateStr = name.substring(11,19);
                            Date file_date = m_format.parse(dateStr);
                            if (file_date.before(yest))
                                return false;
                            else if (file_date.before(today))
                                return true;
                            else
                                return false;
                        }
                        catch(Exception e)
                        {
                            return false;
                        }
                    }
                    else
                         return false;
                  }

            });

            String m_gathererPath = getParameterAsString("RDC", "GATHERER_PATH", null);
            if (eventLogs_list != null && eventLogs_list.length > 0)
            {
                logger.info("EventLogs File collection Started...");
                File copy = new File(m_gathererPath + "/EventLogs");

                if (!copy.exists())
                {
                    copy.mkdirs();
                }

                for (int i = 0; i < eventLogs_list.length; i++)
                {
                    Tools.moveFile(m_gathererPath + "/EventLogs", eventLogs_list[i]);
                }
                logger.info("EventLogs File collection Ended...");
            }
        }
	}
	
	private static void initOperatorNameAndUrlMap()
	{
		List<SitePrefix> sitePrefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if(sitePrefixes != null && sitePrefixes.size() > 0)
		{
			for(int i = 0; i < sitePrefixes.size(); i++)
				operatorNameUrlMap.put(sitePrefixes.get(i).getSiteName(), sitePrefixes.get(i).getSiteUrl());
		}
		logger.info("operatorNameUrlMap="+operatorNameUrlMap);
		
	}
	private static void initializeEventLogger()
	{
    	try
		{
			Configuration cfg = new Configuration(InterOperatorUtility.getParameterAsString("RDC", "GATHERER_PATH", ".")+"/EventLogs");
			eventLogger = new RDCEventLoggerPreMNP(cfg);
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
    }
	
	private static void initializeInterchageOperatorIdCircleMap()
	{
		Parameters params = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "INTERCHANGE_OPERATOR_ID_MAP");
		if (params == null || params.getValue() == null)
			return;
		
		String opNameOpIDMapStr = params.getValue();
		String[] nameIDPairs = opNameOpIDMapStr.split(";");
		for (String eachPair : nameIDPairs)
		{
			String[] str = eachPair.split(":");
			if(str.length != 3)
				continue;
			operatorIdsInterchangeMap.put(new Integer(str[0]), new Integer(str[1]));
			String[] circles = str[2].split(",");
			HashSet<String> circlesSet = new HashSet<String>();
			for (String string : circles)
				circlesSet.add(string);
			operatorIdInterchangeCirclesMap.put(new Integer(str[0]), circlesSet);
		}
		logger.info("operatorIdsInterchangeMap="+operatorIdsInterchangeMap+", operatorIdInterchangeCirclesMap="+operatorIdInterchangeCirclesMap);
	}
	
	public static int getInterchangedOperatorId(int initialOperatorId, String circleId)
	{
		if(operatorIdsInterchangeMap.containsKey(initialOperatorId) && operatorIdInterchangeCirclesMap.containsKey(initialOperatorId))
		{
			int finalOperatorId = operatorIdsInterchangeMap.get(initialOperatorId);
			HashSet<String> circlesSet = operatorIdInterchangeCirclesMap.get(initialOperatorId);
			if(circlesSet.contains(circleId))
				return finalOperatorId;
		}
		return initialOperatorId;
	}
	
	public static void writeEventLog(InterOperatorCopyRequestBean copyRequest, String copyResult)
	{
		try
		{
			eventLogger.copyTransaction(copyRequest.getCopieeMdn()+"", getRBTOperatorNameFromOperatorID(copyRequest.getCopieeOperatorId()+""),
				copyRequest.getSourceContentId(), "NR", "NR", copyRequest.getCopierMdn()+"", 
				getRBTOperatorNameFromOperatorID(copyRequest.getCopierOperatorId()+""), copyRequest.getTargetContentId(), "NR", "NR",
				Calendar.getInstance().getTime(), copyRequest.getKeyPressed(), copyResult);
		}
		catch(Exception e)
		{
			logger.error("Exception caught", e);
		}
		/*
	    * eventLogger.copyTransaction(vst.subID(), Utility.getSubscriberOperator(Utility.subID(vst.subID()), null), vst.clipID(), 
					"NR", "NR", vst.callerID(), Utility.getSubscriberOperator(Utility.subID(vst.callerID()),null),
					"MISSING", "NR", "NR", Calendar.getInstance().getTime(), keyPressed, "CONTENT_MAPPING_MISSING");	
	    */
	}
	
}
