/**
 * 
 */
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

/**
 * @author vinayasimha.patil
 *
 */
public class RecommendationReportDownloader
{
	private static Logger logger = Logger.getLogger(RecommendationReportDownloader.class);
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
	private String FTP_USER = "onmobile" ;
	private String FTP_PWD = "qwerty12#";
	private String FTP_DIR = "rbttest\\local\\uploads" ;
	int FTP_TIMEOUT = 7200000   ;
	String SDR_DOWNLOAD_FOLDER = ".";
	ResourceBundle bundle = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		new RecommendationReportDownloader().downloader();
	}

	private void initialize()
	{
		bundle =  ResourceBundle.getBundle("BulkActReportDownloader",Locale.getDefault());
		FTP_SERVER = bundle.getString("FTP_SERVER");
		FTP_PORT= Integer.parseInt(bundle.getString("FTP_PORT"));
		FTP_USER = bundle.getString("FTP_USER");
		FTP_PWD = bundle.getString("FTP_PWD");
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

		FTPClient ftp = null;
		try
		{
			ftp = new FTPClient(FTP_SERVER, FTP_PORT);
			ftp.setTimeout(FTP_TIMEOUT);
			ftp.login(FTP_USER,FTP_PWD);
			ftp.setConnectMode(FTPConnectMode.PASV);
			ftp.setType(FTPTransferType.BINARY);
			ftp.chdir(FTP_DIR);
			Calendar calendar = Calendar.getInstance();
			for(int i = 0; i < 3; i++)
			{
				String dateStr = calendar.get(Calendar.DATE) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ calendar.get(Calendar.YEAR);
				String dirDate = getMON(calendar.get(Calendar.MONTH))+calendar.get(Calendar.DATE);

				try
				{
					ftp.chdir(dirDate);
					boolean sendMail = false;

					ftp.get(SDR_DOWNLOAD_FOLDER+"\\Auto_Recommendation_"+ dateStr +".zip", "Auto_Recommendation_"+ dateStr +".zip");
					System.out.println("*****Auto_Recommendation First file*******");
					ftp.delete("Auto_Recommendation_"+ dateStr +".zip");
					sendMail = true;

					ftp.get(SDR_DOWNLOAD_FOLDER+"\\Auto_Recommendation_"+ dateStr +".html", "Auto_Recommendation_"+ dateStr +".html");
					System.out.println("*****Auto_Recommendation Second file*******");
					ftp.delete("Auto_Recommendation_"+ dateStr +".html");
					sendMail = true;

					if(sendMail)
						sendRecommendationReport(calendar, "CONTENT");
				}
				catch(Exception exe)
				{
					exe.printStackTrace();
				}

				ftp.chdir("..");
				try
				{
					ftp.chdir(dirDate);
					boolean sendMail = false;

					ftp.get(SDR_DOWNLOAD_FOLDER+"\\Pick_Of_Day_Recommendation_"+ dateStr +".zip", "Pick_Of_Day_Recommendation_"+ dateStr +".zip");
					System.out.println("*****Pick_Of_Day_Recommendation First file*******");
					ftp.delete("Pick_Of_Day_Recommendation_"+ dateStr +".zip");
					sendMail = true;

					ftp.get(SDR_DOWNLOAD_FOLDER+"\\Pick_Of_Day_Recommendation_"+ dateStr +".html", "Pick_Of_Day_Recommendation_"+ dateStr +".html");
					System.out.println("*****Pick_Of_Day_Recommendation Second file*******");
					ftp.delete("Pick_Of_Day_Recommendation_"+ dateStr +".html");
					sendMail = true;

					if(sendMail)
						sendRecommendationReport(calendar, "PICK_OF_THE_DAY");
				}
				catch(Exception exe)
				{
					exe.printStackTrace();
				}
				ftp.chdir("..");
				calendar.add(Calendar.DAY_OF_YEAR, -1);
			}
		}
		catch(Exception exe)
		{
			exe.printStackTrace();
		}
		finally
		{
			if(ftp!=null)
				ftp.quit();
		}
	}

	public void sendRecommendationReport(Calendar calendar, String mode)
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

		String dateStr = calendar.get(Calendar.DATE) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ calendar.get(Calendar.YEAR);

		String zipFileName = SDR_DOWNLOAD_FOLDER+"\\Auto_Recommendation_"+dateStr+".zip";
		String htmlFileName = SDR_DOWNLOAD_FOLDER+"\\Auto_Recommendation_"+dateStr+".html";
		if(mode.equals("PICK_OF_THE_DAY"))
		{
			zipFileName = SDR_DOWNLOAD_FOLDER+"\\Pick_Of_Day_Recommendation_"+dateStr+".zip";
			htmlFileName = SDR_DOWNLOAD_FOLDER+"\\Pick_Of_Day_Recommendation_"+dateStr+".html";
		}

		File zippedRecFile = new File(zipFileName);
		File htmlRecFile = new File(htmlFileName);

		if(zippedRecFile.exists() && htmlRecFile.exists())
		{
			args[argv.length] = "-S";
			args[argv.length+1] = "TATA RBT: Auto Recommendation Report";
			if(mode.equals("PICK_OF_THE_DAY"))
				args[argv.length+1] = "TATA RBT: Pick Of The Recommendation Report";
			args[argv.length+2] = "-BT";
			args[argv.length+3] = getFileContent(htmlRecFile);
			args[argv.length+4] = "-A";
			args[argv.length+5] = zippedRecFile.getAbsolutePath();
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

	public static String getMON(int i_mm)
	{
		String month [] = {"jan","feb","mar","apr","may","jun",
				"jul","aug","sep","oct","nov",
		"dec"};
		return month[i_mm];
	}

}
