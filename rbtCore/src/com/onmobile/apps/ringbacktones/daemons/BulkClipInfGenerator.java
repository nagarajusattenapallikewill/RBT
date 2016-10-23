/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

/**
 * @author Sreekar
 *
 * vsreekar@onmobile.com
 */
public class BulkClipInfGenerator extends Thread
{
	private static Logger logger = Logger.getLogger(BulkClipInfGenerator.class);
	
	private static SimpleDateFormat fileNameformatter = new SimpleDateFormat("yyyyMMdd");
	
	String csvPath = null;
	int sleepMinutes = 1;
	
	public BulkClipInfGenerator()
	{
		Tools.init("INF_GENERATOR", true);
		csvPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SONG_INF_SOURCE_CSV_FILE_PATH", "");
		String sleepTimeStr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SLEEP_MINUTES", null);
		if(sleepTimeStr != null)
			sleepMinutes = Integer.parseInt(sleepTimeStr);
	}
	
	public void run()
	{
		try
		{
			while(csvPath != null)
			{
				logger.info("RBT::starting the loop");
				File csvDir = new File(csvPath);
				if(csvDir.exists())
				{
					File [] allFiles = csvDir.listFiles();
					for(int fileCount = 0; fileCount < allFiles.length; fileCount++)
					{
						File oneFile = allFiles[fileCount];
						if(oneFile.isFile())
						{
							FileInputStream fis = new FileInputStream(oneFile);
							BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
							
							String fileName = oneFile.getName();
							if(fileName.endsWith(".csv"))
							{
								String thisLine = reader.readLine();
								while(thisLine != null)
								{
									boolean validLine = true;
									thisLine = thisLine.trim();
									StringTokenizer stk = new StringTokenizer(thisLine, ",");
									StringBuffer sb = new StringBuffer();
									
									sb.append("&");
									
									int tokenCount = 0;
									tokenLoop:while(stk.hasMoreTokens())
									{
										tokenCount++;
										String token = stk.nextToken().trim();
										logger.info("RBT:: tokenCount = " + tokenCount + " & token = " + token);
										if(tokenCount == 3)
										{
											//special characters are not allowed in the song name checking for the same
											for(int c=0;c<token.length();c++)
											{
												char ch = token.charAt(c);
												if((ch>='A' && ch<='Z') || ((ch>='a' && ch<='z') || (ch>='0' && ch<='9')) || (ch=='\''))	
												{
												}
												else
												{
													logger.info("RBT::invalid song name " + token + " adding to failed file & will continue with next record");
													String failedPath = csvDir + File.separator + "failed";
													File failedDir = new File(failedPath);
													if(!failedDir.exists())
														failedDir.mkdirs();
													String failedFileName = failedPath + File.separator + fileNameformatter.format(new Date()) + ".txt";
													Tools.writeTFile(new File(failedFileName), token);
													validLine = false;
													break tokenLoop;
												}
											}
										}
										//date should be in the format yyyy-mm-dd modifying the same
										if(tokenCount == 9)
										{
											String day = token.substring(0,token.indexOf("/"));
											String month = token.substring(token.indexOf("/")+1,(token.lastIndexOf("/")));
											String year = token.substring(token.lastIndexOf("/")+1);
											if((day != null) && (month != null) && (year != null))
												token = year+"-"+month+"-"+day;
										}
										sb.append(token);
										sb.append("&");
									}
									String infPath = csvDir.getAbsolutePath() + File.separator + "INF";
									File infDir = new File(infPath);
									if(!infDir.exists())
										infDir.mkdirs();
									
									String infFileName = infPath + File.separator + fileNameformatter.format(new Date()) + ".inf";
									if(validLine)
										Tools.writeTFile(new File(infFileName), sb.toString());
									
									//reading the next line
									thisLine = reader.readLine();
								}
							}
							else
								logger.info("RBT::file " + fileName + " is not a csv file cannot process. Continuing with the next file");
							
							try
							{
								if(reader != null)
									reader.close();
							}
							catch(IOException e)
							{
								logger.error("", e);
							}
							try
							{
								if(fis != null)
									fis.close();
							}
							catch(IOException e)
							{
								logger.error("", e);
							}
							if(fileName.endsWith(".csv"))
							{
								logger.info("RBT::moving file " + fileName);
								String completedDirPath = csvPath + File.separator + "completed";
								File completedDir = new File(completedDirPath);
								if(!completedDir.exists())
									completedDir.mkdirs();
								String completedFilePath = completedDirPath + File.separator + fileName;
								if(oneFile.renameTo(new File(completedFilePath)))
									logger.info("RBT::moving the file " + fileName + " successful");
								else
									logger.info("RBT::moving the file " + fileName + " failed");
							}
						}
						else
							logger.info("RBT::" + oneFile.getName() + " is not a file");
					}
				}
				else
				{
					logger.info("RBT::root folder " + csvDir.getName() + " missing cannot process stopping the inf file generation");
					break;
				}
				//sleeping
				try
				{
					logger.info("RBT::sleeping for " + sleepMinutes + " minute(s)");
					BulkClipInfGenerator.sleep(sleepMinutes*60*1000);
					logger.info("RBT::after sleep");
				}
				catch(Throwable th)
				{
					logger.error("", th);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		BulkClipInfGenerator infGenerator = new BulkClipInfGenerator();
		infGenerator.start();
	}
}
