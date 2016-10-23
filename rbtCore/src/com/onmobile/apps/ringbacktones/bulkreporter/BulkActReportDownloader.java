package com.onmobile.apps.ringbacktones.bulkreporter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPConnectMode;
import com.enterprisedt.net.ftp.FTPTransferType;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class BulkActReportDownloader
{

	private static Logger logger = Logger.getLogger(BulkActReportDownloader.class);
	
	private String user = null;
	private String password = null;
	private String host = null;
	private String port = null;

	private String from = null;
	private String to = null;
	private String cc = null;
	private String bcc = null;

	private String FTP_SERVER = "10.9.11.16";
	int FTP_PORT = 21;
	long FTP_WAITPERIOD = 1000;
	private String FTP_USER = "onmobile" ;
	private String FTP_PWD = "qwerty12#";
	int FTP_RETRIES = 2 ;
	private String FTP_DIR = "rbttest\\local\\uploads" ;
	int FTP_TIMEOUT = 7200000   ;
	String SDR_DOWNLOAD_FOLDER = ".";
	ResourceBundle bundle = null;

	public static void main(String[] a) throws Exception
	{
		new BulkActReportDownloader().downloader();
	}

	private void initialize()
	{
		bundle =  ResourceBundle.getBundle("BulkActReportDownloader",Locale.getDefault());
		FTP_SERVER = bundle.getString("FTP_SERVER");
		FTP_PORT= Integer.parseInt(bundle.getString("FTP_PORT"));
		FTP_WAITPERIOD= Long.parseLong(bundle.getString("FTP_WAITPERIOD"));
		FTP_USER = bundle.getString("FTP_USER");
		FTP_PWD = bundle.getString("FTP_PWD");
		FTP_RETRIES = Integer.parseInt(bundle.getString("FTP_RETRIES"));
		FTP_DIR = bundle.getString("FTP_DIR");
		FTP_TIMEOUT = Integer.parseInt(bundle.getString("FTP_TIMEOUT"));
		SDR_DOWNLOAD_FOLDER = bundle.getString("SDR_DOWNLOAD_FOLDER");
		System.out.println("***********Got*"+FTP_DIR);

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

	public void downloader() throws Exception 
	{
		initialize();
		Calendar calendar = Calendar.getInstance();
		String dateStr = calendar.get(Calendar.DATE) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ calendar.get(Calendar.YEAR);
		FTPClient ftp = null;
		for(int i=0;i<FTP_RETRIES;i++)
		{
			try
			{
				System.out.println("*****Starting*******");
				ftp = new FTPClient(FTP_SERVER, FTP_PORT);
				ftp.setTimeout(FTP_TIMEOUT);
				ftp.login(FTP_USER,FTP_PWD);
				ftp.setConnectMode(FTPConnectMode.PASV);
				ftp.setType(FTPTransferType.BINARY);
				try
				{
					String dirDate = getMON(calendar.get(Calendar.MONTH))+calendar.get(Calendar.DATE);
					ftp.chdir(FTP_DIR+"/"+dirDate);
				}
				catch(Exception exe)
				{
					exe.printStackTrace();
				}

				boolean sendMail = false;
				try
				{
					ftp.get(SDR_DOWNLOAD_FOLDER+"\\BulkDeactivation_"+dateStr+".zip","BulkDeactivation_"+dateStr+".zip");
					System.out.println("*****First file*******");
					sendMail = true;
				}
				catch(Exception exe)
				{
					System.out.println("*****Error First file*******");
				}
				try
				{
					ftp.get(SDR_DOWNLOAD_FOLDER+"\\BulkActivation_"+dateStr+".zip","BulkActivation_"+dateStr+".zip");
					System.out.println("*****Second file*******");
					sendMail = true;
				}
				catch(Exception exe)
				{
					System.out.println("*****Error Second file*******");
				}
				try
				{
					ftp.get(SDR_DOWNLOAD_FOLDER+"\\BulkActivated_Subscribers_"+dateStr+".zip","BulkActivated_Subscribers_"+dateStr+".zip");
					System.out.println("*****Third file*******");
					sendMail = true;
				}
				catch(Exception exe)
				{
					System.out.println("*****Error Third file*******");
				}
				try
				{
					ftp.get(SDR_DOWNLOAD_FOLDER+"\\Subscribers_Availed_Promo_"+dateStr+".zip","Subscribers_Availed_Promo_"+dateStr+".zip");
					System.out.println("*****Fourth file*******");
					sendMail = true;
				}
				catch(Exception exe)
				{
					System.out.println("*****Error Fourth file*******");
				}
				try
				{
					ftp.get(SDR_DOWNLOAD_FOLDER+"\\BulkActivation_"+dateStr+".html","BulkActivation_"+dateStr+".html");
					System.out.println("*****Fifth file*******");
					sendMail = true;
				}
				catch(Exception exe)
				{
					System.out.println("*****Error Fifth file*******");
				}
				try
				{
					ftp.get(SDR_DOWNLOAD_FOLDER+"\\BulkDeactivation_"+dateStr+".html","BulkDeactivation_"+dateStr+".html");
					System.out.println("*****Sixth file*******");
					sendMail = true;
				}
				catch(Exception exe)
				{
					System.out.println("*****Error Sixth file*******");
				}
				if(!sendMail)
					throw new Exception("Sleep and try");

				sendBulkActivationReport();
				break;

			}
			catch(Exception exe)
			{
				exe.printStackTrace();
				try
				{
					Thread.sleep(FTP_WAITPERIOD);
				}
				catch(Exception ex)
				{
				}
				continue;
			}
			finally
			{
				if(ftp!=null)
					ftp.quit();
			}
		}
	}

	public void sendBulkActivationReport()
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

		Calendar calendar = Calendar.getInstance();
		String dateStr = calendar.get(Calendar.DATE) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ calendar.get(Calendar.YEAR);
		File zippedActFile = new File(SDR_DOWNLOAD_FOLDER+"\\BulkActivation_"+dateStr+".zip");
		File zippedActSubsFile = new File(SDR_DOWNLOAD_FOLDER+"\\BulkActivated_Subscribers_"+dateStr+".zip");
		File zippedAvlFile = new File(SDR_DOWNLOAD_FOLDER+"\\Subscribers_Availed_Promo_"+dateStr+".zip");
		File htmlActFile = new File(SDR_DOWNLOAD_FOLDER+"\\BulkActivation_"+dateStr+".html");
		File zippedDctFile = new File(SDR_DOWNLOAD_FOLDER+"\\BulkDeactivation_"+dateStr+".zip");
		File htmlDctFile = new File(SDR_DOWNLOAD_FOLDER+"\\BulkDeactivation_"+dateStr+".html");

		if(zippedActFile.exists() || zippedAvlFile.exists())
		{
			args[argv.length] = "-S";
			args[argv.length+1] = "Bulk Activation Report";
			args[argv.length+2] = "-BT";
			args[argv.length+3] = getFileContent(htmlActFile);
			args[argv.length+4] = "-A";
			args[argv.length+5] = "";
			if(zippedActFile.exists())
				args[argv.length+5] = zippedActFile.getAbsolutePath() +";";
			if(zippedActSubsFile.exists())
				args[argv.length+5] += zippedActSubsFile.getAbsolutePath() +";";
			if(zippedAvlFile.exists())
				args[argv.length+5] += zippedAvlFile.getAbsolutePath();
			SendMail.main(args);
		}

		if(zippedDctFile.exists())
		{
			args[argv.length] = "-S";
			args[argv.length+1] = "Bulk Deactivation Report";
			args[argv.length+2] = "-BT";
			args[argv.length+3] = getFileContent(htmlDctFile);
			args[argv.length+4] = "-A";
			args[argv.length+5] = zippedDctFile.getAbsolutePath();

			SendMail.main(args);
		}

	}

	public String getFileContent(File file)
	{
		String fileContent = "";

		try 
		{
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);

			String line;
			while((line = bufferedReader.readLine()) != null)
			{
				fileContent = fileContent + line;
			}
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return fileContent;
	}

	/*public String[] getStringArray(String to)
	{
		StringTokenizer str = new StringTokenizer(to,",");
		String [] arr = new String[str.countTokens()];
		int i=0;
		while(str.hasMoreTokens()){
			arr [i]=str.nextToken();
			i++;
		}
		return arr;
	}*/

	public static String getMON(int i_mm)
	{
		String month [] = {"jan","feb","mar","apr","may","jun",
				"jul","aug","sep","oct","nov",
		"dec"};
		return month[i_mm];
	}
}