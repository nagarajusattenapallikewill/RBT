package com.onmobile.apps.ringbacktones.bulkreporter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPException;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.BulkPromoSMS;

/**
 * 
 */

/**
 * @author vinayasimha.patil
 *
 */
public class SendBulkActivationReports
{
	private static Logger logger = Logger.getLogger(SendBulkActivationReports.class);
	
	private String user = null;
	private String password = null;
	private String host = null;
	private String port = null;

	private String from = null;
	private String to = null;
	private String cc = null;
	private String bcc = null;

	private String ftpServer = "10.9.11.16";
	private int ftpPort = 21;
	private long ftpWaitPeriod = 300000l;
	private String ftpUser = "onmobile";
	private String ftpPassword = "qwerty12#";
	private int ftpRetries = 1;
	private String ftpDir = "spider\\local\\uploads";
	private int ftpTimeout = 7200000;
	private String transferMode = "SPIDER";

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Tools.init("BulkActivationReporter", true);

		SendBulkActivationReports sendBulkActivationReports = new SendBulkActivationReports();
		sendBulkActivationReports.setParameters();
		try 
		{
			sendBulkActivationReports.generateBulkActivationReportMail();
		}
		catch (IOException e) 
		{
			logger.error("", e);
		}
	}

	public void setParameters()
	{
		transferMode = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "TRANSFER_MODE", null);
		if(transferMode.equalsIgnoreCase("MAIL"))
		{
			user = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_USER", null);
			password = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_USER_PASSWORD", null);
			host = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_HOST", null);
			port = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_HOST_PORT", null);
			from = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_FROM", null);
			to = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_TO", null);
			cc = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_CC", null);
			bcc = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "MAIL_BCC", null);

			if(user == null || password == null || from == null || to == null)
			{
				logger.info("RBT:: One or more required parameters are null");
				System.exit(0);
			}
		}
		else
		{
			ftpServer = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_SERVER", null);
			ftpPort = RBTParametersUtils.getParamAsInt(iRBTConstant.REPORTER, "FTP_PORT", -1);
			ftpWaitPeriod = RBTParametersUtils.getParamAsLong(iRBTConstant.REPORTER, "FTP_WAIT", 3000);
			ftpUser = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_USER", null);
			ftpPassword = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_PWD", null);
			ftpRetries = RBTParametersUtils.getParamAsInt(iRBTConstant.REPORTER, "FTP_RETRIES", -1);
			ftpDir = RBTParametersUtils.getParamAsString(iRBTConstant.REPORTER, "FTP_DIR", null);
			ftpTimeout = RBTParametersUtils.getParamAsInt(iRBTConstant.REPORTER, "FTP_TIMEOUT", 10000);
		}
	}

	public void generateBulkActivationReportMail() throws IOException
	{
		String reportPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null);
		File reportFolder = new File(reportPath);
		String promoToolPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROMOTOOL_PATH", null);
		File promoToolFolder = new File(promoToolPath);

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();

		Calendar calendar = Calendar.getInstance();
		String dateStr = calendar.get(Calendar.DATE) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ calendar.get(Calendar.YEAR);

		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		calendar.add(Calendar.DAY_OF_YEAR, -1);

		BulkPromo[] activeBulkPromos = rbtDBManager.getActiveBulkPromos();
		BulkPromo[] bulkPromosAct = rbtDBManager.getBulkPromosByStartDate(calendar.getTime());
		BulkPromo[] bulkPromosDct = rbtDBManager.getBulkPromosByEndDate(calendar.getTime());

		File[] filesToBeZippedAvl = null;
		File[] filesToBeZippedAct = null;
		File[] filesToBeZippedActSubs = null;
		File[] filesToBeZippedDct = null;
		File zippedAvlFile = null;
		File zippedActFile = null;
		File zippedActSubsFile = null;
		File zippedDctFile = null;

		File mailReport = new File("mail_reports");
		if(!mailReport.exists())
			mailReport.mkdirs();

		if(activeBulkPromos != null)
		{
			ArrayList<File> filesList = new ArrayList<File>();
			for (int i = 0; i < activeBulkPromos.length; i++)
			{
				BulkPromo bulkPromo = activeBulkPromos[i];
				logger.info("RBT:: Bulk Promo ID for last Activation = "+bulkPromo.bulkPromoId());
				//BulkPromoSMS bulkPromoSMS = rbtDBManager.getBulkPromoSMSForDate(bulkPromo.bulkPromoId(), calendar.getTime());
				DateFormat format = new SimpleDateFormat("yyyyMMdd");
				String smsDateString = format.format(calendar.getTime());
				BulkPromoSMS bulkPromoSMS = CacheManagerUtil.getBulkPromoSMSCacheManager().getBulkPromoSMSForDate(bulkPromo.bulkPromoId(), smsDateString);
				if(bulkPromoSMS != null)
				{
					File availedSubFile = getBulkPromoAvailedSubscribers(bulkPromo.bulkPromoId());
					if(availedSubFile != null)
						filesList.add(availedSubFile);
				}
			}
			filesToBeZippedAvl = filesList.toArray(new File[0]);
			logger.info("RBT:: Total files To Be Zipped = "+filesToBeZippedAvl.length);
			if(filesToBeZippedAvl.length > 0)
			{
				zippedAvlFile = ZipFiles.zipFiles("mail_reports\\Subscribers_Availed_Promo_"+dateStr+".zip", filesToBeZippedAvl, "availed");
			}
		}

		if(bulkPromosAct != null)
		{
			ArrayList<File> filesList = new ArrayList<File>();
			ArrayList<File> filesListActSubs = new ArrayList<File>();

			for (int i = 0; i < bulkPromosAct.length; i++)
			{
				BulkPromo bulkPromo = bulkPromosAct[i];
				logger.info("RBT:: Bulk Promo ID for last Activation = "+bulkPromo.bulkPromoId());
				File[] folders = reportFolder.listFiles(new BulkPromoFileFilter("activation", bulkPromo.bulkPromoId()));

				if(folders != null && folders.length > 0)
				{
					for (int j = 0; j < folders.length; j++)
					{
						logger.info("RBT:: Folder = "+folders[j].getAbsolutePath());
						File smssdrFile = new File(folders[j], "smssdr");
						File[] files = smssdrFile.listFiles();
						for (int k = 0; k < files.length; k++) 
						{
							filesList.add(files[k]);
						}
					}
				}

				File activatedSubFile = getBulkPromoActivatedSubscribers(bulkPromo.bulkPromoId());
				if(activatedSubFile != null)
					filesListActSubs.add(activatedSubFile);
			}
			filesToBeZippedAct = filesList.toArray(new File[0]);
			logger.info("RBT:: Total files To Be Zipped = "+filesToBeZippedAct.length);
			if(filesToBeZippedAct.length > 0)
			{
				zippedActFile = ZipFiles.zipFiles("mail_reports\\BulkActivation_"+dateStr+".zip", filesToBeZippedAct, "activation");
			}

			filesToBeZippedActSubs = filesListActSubs.toArray(new File[0]);
			logger.info("RBT:: Total files To Be Zipped = "+filesToBeZippedActSubs.length);
			if(filesToBeZippedActSubs.length > 0)
			{
				zippedActSubsFile = ZipFiles.zipFiles("mail_reports\\BulkActivated_Subscribers_"+dateStr+".zip", filesToBeZippedActSubs, "activatedSubs");
			}
		}

		if(bulkPromosDct != null)
		{
			ArrayList<File> filesList = new ArrayList<File>();
			for (int i = 0; i < bulkPromosDct.length; i++)
			{
				BulkPromo bulkPromo = bulkPromosDct[i];
				logger.info("RBT:: Bulk Promo ID for last Deactivation = "+bulkPromo.bulkPromoId());

				File incomingFolder = new File(promoToolFolder, "incoming");
				File[] incomingFiles = incomingFolder.listFiles(new BulkPromoFileFilter("dectivation", bulkPromo.bulkPromoId()));
				for (int k = 0; k < incomingFiles.length; k++) 
				{
					filesList.add(incomingFiles[k]);
				}

				File promotionFolder = new File(promoToolFolder, "promotion");
				File[] promotionFiles = promotionFolder.listFiles(new BulkPromoFileFilter("dectivation", bulkPromo.bulkPromoId()));
				for (int k = 0; k < promotionFiles.length; k++) 
				{
					filesList.add(promotionFiles[k]);
				}

				File completedFolder = new File(promoToolFolder, "completed");
				File[] completedFiles = completedFolder.listFiles(new BulkPromoFileFilter("dectivation", bulkPromo.bulkPromoId()));
				for (int k = 0; k < completedFiles.length; k++) 
				{
					filesList.add(completedFiles[k]);
				}

				File availedSubFile = getBulkPromoAvailedSubscribers(bulkPromo.bulkPromoId());
				if(availedSubFile != null)
					filesList.add(availedSubFile);
			}

			filesToBeZippedDct = filesList.toArray(new File[0]);
			logger.info("RBT:: Total files To Be Zipped = "+filesToBeZippedDct.length);
			if(filesToBeZippedDct.length > 0)
			{
				zippedDctFile = ZipFiles.zipFiles("mail_reports\\BulkDeactivation_"+dateStr+".zip", filesToBeZippedDct, "deactivation");
			}
		}

		if(transferMode.equalsIgnoreCase("MAIL"))
		{
			ArrayList<String> argsList = new ArrayList<String>();

			argsList.add("-u");
			argsList.add(user);
			argsList.add("-p");
			argsList.add(password);
			if(host != null)
			{
				argsList.add("-h");
				argsList.add(host);
			}
			if(port != null)
			{
				argsList.add("-t");
				argsList.add(port);
			}
			argsList.add("-d");
			argsList.add("true");
			argsList.add("-F");
			argsList.add("OnMobile");
			argsList.add("-T");
			argsList.add(to);
			if(cc != null)
			{
				argsList.add("-C");
				argsList.add(cc);
			}
			if(bcc != null)
			{
				argsList.add("-B");
				argsList.add(bcc);
			}
			argsList.add("-M");
			argsList.add("text/html");

			String[] argv = argsList.toArray(new String[0]);
			String[] args = new String[argv.length + 6];
			for (int i = 0; i < argv.length; i++)
			{
				args[i] = argv[i];
			}

			if(zippedActFile != null || zippedAvlFile != null)
			{
				args[argv.length] = "-S";
				args[argv.length+1] = "Bulk Activation Report";
				args[argv.length+2] = "-BT";
				args[argv.length+3] = generateBulkActivationReport(bulkPromosAct, filesToBeZippedAct, activeBulkPromos, filesToBeZippedAvl);
				args[argv.length+4] = "-A";
				if(zippedActFile != null)
					args[argv.length+5] = zippedActFile.getAbsolutePath() +";";
				if(zippedActSubsFile != null)
					args[argv.length+5] = zippedActSubsFile.getAbsolutePath() +";";
				if(zippedAvlFile != null)
					args[argv.length+5] += zippedAvlFile.getAbsolutePath();
				SendMail.main(args);
			}

			if(zippedDctFile != null)
			{
				args[argv.length] = "-S";
				args[argv.length+1] = "Bulk Deactivation Report";
				args[argv.length+2] = "-BT";
				args[argv.length+3] = generateBulkDeactivationReport(bulkPromosDct, filesToBeZippedDct);
				args[argv.length+4] = "-A";
				args[argv.length+5] = zippedDctFile.getAbsolutePath();

				SendMail.main(args);
			}
		}
		else
		{
			if(zippedActFile != null || zippedAvlFile != null)
			{
				String Achtml = generateBulkActivationReport(bulkPromosAct, filesToBeZippedAct, activeBulkPromos, filesToBeZippedAvl);
				logger.info("RBT:: Creating file BulkActivation_"+dateStr+".html");
				FileWriter bulkActivation = new FileWriter(new File(".\\mail_reports\\BulkActivation_"+dateStr+".html"));
				BufferedWriter  bulkActivationBW = new BufferedWriter(bulkActivation);
				bulkActivation.write(Achtml);
				bulkActivation.flush() ;
				bulkActivationBW.flush();
				bulkActivation.close();
				bulkActivationBW.close();
			}

			if(zippedDctFile != null)
			{
				String Dehtml = generateBulkDeactivationReport(bulkPromosDct, filesToBeZippedDct);
				logger.info("RBT:: Creating file BulkDeactivation_"+dateStr+".html");
				FileWriter bulkDeactivation = new FileWriter(new File(".\\mail_reports\\BulkDeactivation_"+dateStr+".html"));
				BufferedWriter  bulkDeactivationBW = new BufferedWriter(bulkDeactivation);
				bulkDeactivation.write(Dehtml);
				bulkDeactivation.flush() ;
				bulkDeactivationBW.flush();
				bulkDeactivation.close();
				bulkDeactivationBW.close();
			}

			FTPClient ftp = null;
			for(int i = 0; i < ftpRetries; i++)
			{
				try
				{
					logger.info("RBT:: Creating FTP Connection");
					ftp = new FTPClient(ftpServer, ftpPort);
					ftp.setTimeout(ftpTimeout);
					ftp.login(ftpUser, ftpPassword);
					ftp.setConnectMode(FTPConnectMode.PASV);
					ftp.setType(FTPTransferType.BINARY);
					ftp.chdir(ftpDir);
					Calendar curcal = Calendar.getInstance();
					String dirDate = getMONTH(curcal.get(Calendar.MONTH))+curcal.get(Calendar.DATE);
					try
					{
						ftp.mkdir(dirDate);
						ftp.chdir(dirDate);
					}
					catch(Exception exe)
					{
						ftp.chdir(dirDate);
					}

					if(zippedActFile != null)
					{
						ftp.put(zippedActFile.getAbsolutePath(),"BulkActivation_"+dateStr+".zip");
						logger.info("BulkActivation_"+dateStr+".zip uploaded successfully...!");
					}
					if(zippedActSubsFile != null)
					{
						ftp.put(zippedActSubsFile.getAbsolutePath(),"BulkActivated_Subscribers_"+dateStr+".zip");
						logger.info("BulkActivated_Subscribers_"+dateStr+".zip uploaded successfully...!");
					}
					if(zippedAvlFile != null)
					{
						ftp.put(zippedAvlFile.getAbsolutePath(),"Subscribers_Availed_Promo_"+dateStr+".zip");
						logger.info("Subscribers_Availed_Promo_"+dateStr+".zip uploaded successfully...!");
					}
					if(zippedDctFile != null)
					{
						ftp.put(zippedDctFile.getAbsolutePath(),"BulkDeactivation_"+dateStr+".zip");
						logger.info("BulkDeactivation_"+dateStr+".zip uploaded successfully...!");
					}

					File htmlActFile = new File("mail_reports\\BulkActivation_"+dateStr+".html");
					File htmlDctFile = new File("mail_reports\\BulkDeactivation_"+dateStr+".html");
					if(htmlActFile.exists())
					{
						ftp.put(htmlActFile.getAbsolutePath(),"BulkActivation_"+dateStr+".html");
						logger.info("BulkActivation_"+dateStr+".html uploaded successfully...!");
					}
					if(htmlDctFile.exists())
					{
						ftp.put(htmlDctFile.getAbsolutePath(),"BulkDeactivation_"+dateStr+".html");
						logger.info("BulkDeactivation_"+dateStr+".html uploaded successfully...!");
					}

					break;
				}
				catch(Exception exe)
				{
					exe.printStackTrace();
					logger.error("",exe);
					try
					{
						Thread.sleep(ftpWaitPeriod);
					}
					catch(Exception ex)
					{
					}
					continue;
				}
				finally
				{
					if(ftp!=null)
					{
						try 
						{
							ftp.quit();
						}
						catch (FTPException e)
						{
							e.printStackTrace();
						} 
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	private String generateBulkActivationReport(BulkPromo[] bulkPromosAct, File[] filesAct, BulkPromo[] activeBulkPromos, File[] filesAvl) throws IOException
	{
		String htmlReportAct = "<HTML><BODY style=\"font-family: 'tahoma'\">";

		if(bulkPromosAct != null && bulkPromosAct.length > 0 && filesAct!= null && filesAct.length > 0)
		{
			logger.info("RBT:: Generating bulk activation report");

			String[] bulkPromoID = new String[bulkPromosAct.length];
			int[] insertSuc = new int[bulkPromosAct.length];
			int[] insertFail = new int[bulkPromosAct.length];
			int[] openFail = new int[bulkPromosAct.length];
			int[] pollFail = new int[bulkPromosAct.length];
			int[] downloadFail = new int[bulkPromosAct.length];
			int[] songSettingFail = new int[bulkPromosAct.length];

			for (int i = 0; i < bulkPromosAct.length; i++)
			{
				bulkPromoID[i] = bulkPromosAct[i].bulkPromoId();

				logger.info("RBT:: Generating bulk activation report for promo "+ bulkPromoID[i]);

				insertSuc[i] = insertFail[i] = openFail[i] = pollFail[i] = downloadFail[i] = songSettingFail[i] = 0;
				for (int j = 0; j < filesAct.length; j++)
				{
					String parentName = filesAct[j].getParentFile().getParentFile().getName().toLowerCase();
					if(parentName.startsWith(bulkPromoID[i].toLowerCase()))
					{
						if(parentName.indexOf(bulkPromoID[i].toLowerCase()+"_act_insert_success") >= 0)
						{
							insertSuc[i] += (noOfLinesInFile(filesAct[j]) - 2);
						}
						else if(parentName.indexOf(bulkPromoID[i].toLowerCase()+"_act_insert_failure") >= 0)
						{
							insertFail[i] += (noOfLinesInFile(filesAct[j]) - 2);
						}
						else if(parentName.indexOf(bulkPromoID[i].toLowerCase()+"_act_failure") >= 0)
						{
							int count[] = parseActivationFailedFile(filesAct[j]);
							openFail[i] += count[0];
							pollFail[i] += count[1];
						}
						else if(parentName.indexOf(bulkPromoID[i].toLowerCase()+"_sel_failure") >= 0)
						{
							int count[] = parseSelectionFailedFile(filesAct[j]);
							songSettingFail[i] += count[0];
							downloadFail[i] += count[1];
						}
					}
				}
			}
			htmlReportAct += "<H4 align=\"center\" style=\"color:'#0055AA'\"><U>Bulk Activation Report</U></H4>";
			htmlReportAct += "<TABLE border=\"2\" width=\"100%\">";
			htmlReportAct += "<TR bgcolor=\"#0055AA\" style=\"font-size: 12\">";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Promo ID</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Total</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Insert Success</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Insert Failure</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Open Account Failure</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Polling Failure</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Activation Success</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Download Request</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Download Success</FONT></TH>";
			htmlReportAct += "<TH width=\"10%\"><FONT color=\"#FFFFFF\">Song Setting</FONT></TH>";
			htmlReportAct += "</TR>";
			for(int i = 0; i < bulkPromoID.length; i++)
			{
				int activationSuc = (insertSuc[i] - openFail[i] - pollFail[i]);

				htmlReportAct += "<TR style=\"font-size: 12\">";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ bulkPromoID[i] +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ (insertSuc[i] + insertFail[i]) +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ insertSuc[i] +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ insertFail[i] +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ openFail[i] +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ pollFail[i] +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ activationSuc +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ activationSuc +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ (activationSuc - downloadFail[i]) +"</TD>";
				htmlReportAct += "<TD width=\"10%\" align=\"center\">"+ (activationSuc - downloadFail[i] - songSettingFail[i]) +"</TD>";
				htmlReportAct += "</TR>";
			}
			htmlReportAct += "</TABLE>";
		}

		if(activeBulkPromos != null && activeBulkPromos.length > 0 && filesAvl!= null && filesAvl.length > 0)
		{
			logger.info("RBT:: Generating bulk availed subscribers report");

			String[] bulkPromoID = new String[activeBulkPromos.length];
			String[] PromoStartDate = new String[activeBulkPromos.length];
			int[] avlCount = new int[activeBulkPromos.length];
			int totalPromos = 0;

			for (int i = 0; i < activeBulkPromos.length; i++)
			{
				bulkPromoID[totalPromos] = activeBulkPromos[i].bulkPromoId();

				logger.info("RBT:: Generating bulk availed subscribers report for promo "+ bulkPromoID[totalPromos]);

				Calendar calendar = Calendar.getInstance();
				calendar.setTime(activeBulkPromos[i].promoStartDate());
				PromoStartDate[totalPromos] = calendar.get(Calendar.DATE) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ calendar.get(Calendar.YEAR);
				avlCount[totalPromos] = 0;
				for (int j = 0; j < filesAvl.length; j++)
				{
					String fileName = filesAvl[j].getName().toLowerCase();
					if(fileName.startsWith(bulkPromoID[totalPromos].toLowerCase()))
					{
						avlCount[totalPromos] += (noOfLinesInFile(filesAvl[j]) - 1);						
						totalPromos++;
						break;
					}
				}
			}

			if(totalPromos > 0)
			{
				htmlReportAct += "<BR>";
				htmlReportAct += "<H4 align=\"center\" style=\"color:'#0055AA'\"><U>Subscribers Availed Bulk Promo</U></H4>";
				htmlReportAct += "<TABLE border=\"2\" width=\"100%\">";
				htmlReportAct += "<TR bgcolor=\"#0055AA\" style=\"font-size: 12\">";
				htmlReportAct += "<TH width=\"14%\"><FONT color=\"#FFFFFF\">Promo ID</FONT></TH>";
				htmlReportAct += "<TH width=\"14%\"><FONT color=\"#FFFFFF\">Promo Start Date</FONT></TH>";
				htmlReportAct += "<TH width=\"14%\"><FONT color=\"#FFFFFF\">Subscribers Availed Promo</FONT></TH>";
				htmlReportAct += "</TR>";

				for(int i = 0; i < totalPromos; i++)
				{
					htmlReportAct += "<TR style=\"font-size: 12\">";
					htmlReportAct += "<TD width=\"33%\" align=\"center\">"+ bulkPromoID[i] +"</TD>";
					htmlReportAct += "<TD width=\"33%\" align=\"center\">"+ PromoStartDate[i] +"</TD>";
					htmlReportAct += "<TD width=\"34%\" align=\"center\">"+ avlCount[i] +"</TD>";
					htmlReportAct += "</TR>";
				}

				htmlReportAct += "</TABLE>";
			}
		}
		htmlReportAct += "</BODY></HTML>";

		return htmlReportAct;
	}

	private String generateBulkDeactivationReport(BulkPromo[] bulkPromosDct, File[] filesDct) throws IOException
	{
		String htmlReportDct = "<HTML><BODY style=\"font-family: 'tahoma'\">";

		if(bulkPromosDct != null && bulkPromosDct.length > 0 && filesDct!= null && filesDct.length > 0)
		{
			String[] bulkPromoID = new String[bulkPromosDct.length];
			int[] dctCount = new int[bulkPromosDct.length];
			int[] avlCount = new int[bulkPromosDct.length];

			for (int i = 0; i < bulkPromosDct.length; i++)
			{
				bulkPromoID[i] = bulkPromosDct[i].bulkPromoId();
				dctCount[i] = 0;
				avlCount[i] = 0;
				for (int j = 0; j < filesDct.length; j++)
				{
					String fileName = filesDct[j].getName().toLowerCase();
					if(fileName.startsWith(bulkPromoID[i].toLowerCase()))
					{
						if(fileName.indexOf(bulkPromoID[i].toLowerCase()+"_availed_subscribers") >= 0)
						{
							avlCount[i] += (noOfLinesInFile(filesDct[j]) - 1);
						}
						else
						{
							dctCount[i] += (noOfLinesInFile(filesDct[j]) - 1);
						}

					}
				}
			}
			htmlReportDct += "<H4 align=\"center\" style=\"color:'#0055AA'\"><U>Bulk Deactivation Report</U></H4>";
			htmlReportDct += "<TABLE border=\"2\" width=\"100%\">";
			htmlReportDct += "<TR bgcolor=\"#0055AA\" style=\"font-size: 12\">";
			htmlReportDct += "<TH width=\"33%\"><FONT color=\"#FFFFFF\">Promo ID</FONT></TH>";
			htmlReportDct += "<TH width=\"33%\"><FONT color=\"#FFFFFF\">Total Deactivation</FONT></TH>";
			htmlReportDct += "<TH width=\"34%\"><FONT color=\"#FFFFFF\">Subscribers Availed Promo</FONT></TH>";
			htmlReportDct += "</TR>";
			for(int i = 0; i < bulkPromoID.length; i++)
			{
				htmlReportDct += "<TR style=\"font-size: 12\">";
				htmlReportDct += "<TD width=\"33%\" align=\"center\">"+ bulkPromoID[i] +"</TD>";
				htmlReportDct += "<TD width=\"33%\" align=\"center\">"+ dctCount[i] +"</TD>";
				htmlReportDct += "<TD width=\"34%\" align=\"center\">"+ avlCount[i] +"</TD>";
				htmlReportDct += "</TR>";
			}
			htmlReportDct += "</TABLE>";
		}

		htmlReportDct += "</BODY></HTML>";

		return htmlReportDct;
	}

	private int noOfLinesInFile(File file) throws IOException
	{
		int noOfLines;		

		FileReader fileReader = new FileReader(file);
		LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
		lineNumberReader.skip(file.length());
		noOfLines = lineNumberReader.getLineNumber() + 1;
		lineNumberReader.close();
		fileReader.close();

		return noOfLines;
	}

	private int[] parseActivationFailedFile(File file) throws IOException
	{
		int[] count = new int[2];
		count[0] = count[1] = 0;

		FileReader fileReader = new FileReader(file);
		BufferedReader br = new BufferedReader(fileReader);

		String line = null;
		while((line = br.readLine()) != null)
		{
			line = line.trim();
			if(line.startsWith("RBT_ACTIVATION_POLLING"))
				count[1]++;
			else if(line.startsWith("RBT_ACTIVATION"))
				count[0]++;
		}

		return count;
	}

	private int[] parseSelectionFailedFile(File file) throws IOException
	{
		int[] count = new int[2];
		count[0] = count[1] = 0;

		FileReader fileReader = new FileReader(file);
		BufferedReader br = new BufferedReader(fileReader);

		String line = null;
		br.readLine(); //Ignoring Header
		while((line = br.readLine()) != null)
		{
			line = line.trim();
			if(!line.equalsIgnoreCase(""))
			{
				if(line.startsWith("RBT_ADD_SETTING"))
					count[0]++;
				else
					count[1]++;
			}
		}

		return count;
	}

	private File getBulkPromoActivatedSubscribers(String bulkPromoID) throws IOException
	{
		File file = null;

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber[] subscribers = rbtDBManager.getBulkPromoSubscribers(bulkPromoID);
		if(subscribers != null && subscribers.length > 0)
		{
			file = new File("mail_reports\\"+ bulkPromoID +"_activated_subscribers.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < subscribers.length; i++)
			{
				bw.write(subscribers[i].subID());
				bw.newLine();
			}
			bw.flush();
			bw.close();
			fw.close();
		}

		return file;
	}

	private File getBulkPromoAvailedSubscribers(String bulkPromoID) throws IOException
	{
		File file = null;
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String[] subscribers = rbtDBManager.getBulkPromoAvailedSubscribers(bulkPromoID);
		if(subscribers != null && subscribers.length > 0)
		{
			file = new File("mail_reports\\"+ bulkPromoID +"_availed_subscribers.txt");
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < subscribers.length; i++)
			{
				bw.write(subscribers[i]);
				bw.newLine();
			}
			bw.flush();
			bw.close();
			fw.close();
		}

		return file;
	}

	private class BulkPromoFileFilter implements FileFilter
	{
		private String mode = null;
		private String flNameForSearch = null;

		public BulkPromoFileFilter(String mode, String flNameForSearch)
		{
			this.mode = mode;
			this.flNameForSearch = flNameForSearch;
		}

		public boolean accept(File pathname)
		{
			logger.info("RBT:: File to be filtered = "+pathname.getAbsolutePath());

			String fileName = pathname.getName().toLowerCase();

			if(mode.equalsIgnoreCase("activation"))
			{
				String keyAct = (flNameForSearch+"_act").toLowerCase();
				String keySel = (flNameForSearch+"_sel").toLowerCase();

				if(!pathname.isDirectory())
					return false;

				if(fileName.startsWith(keyAct) || fileName.startsWith(keySel))
					return true;
			}
			else
			{
				String keyDct = (flNameForSearch+"_deactivated").toLowerCase();

				if(!pathname.isFile())
					return false;

				if(fileName.startsWith(keyDct))
					return true;
			}

			return false;
		}
	}

	public static String getMONTH(int i_mm)
	{
		String month [] = {"jan","feb","mar","apr","may","jun",
				"jul","aug","sep","oct","nov",
		"dec"};
		return month[i_mm];
	}

}
