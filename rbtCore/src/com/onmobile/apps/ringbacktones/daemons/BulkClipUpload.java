package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class BulkClipUpload extends Thread
{
	private static Logger logger = Logger.getLogger(BulkClipUpload.class);
	
	private static SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
	private static SimpleDateFormat fileNameformatter = new SimpleDateFormat("yyyyMMdd");
	
	String httpLink;
	String songInfoPage;
	String operatorAccount;
	String operatorPassword;
	String operator;
	int sleepMinutes;
	String songDaemonLogPath;
	int rotationSize;
	String infPath = null;
	
	public BulkClipUpload()
	{
		Tools.init("CLIP_DAEMON", true);
		httpLink = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
		songInfoPage = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_SONG_INFO_PAGE", "");
		operatorAccount = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "");
		operatorPassword = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "");
		operator = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");
		sleepMinutes = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "SLEEP_MINUTES", 0);
		infPath = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SONG_INF_FILE_PATH", "");
		
		songDaemonLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SONG_DAEMON_LOG_PATH", null);
		rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
	}
	
	public void run()
	{
		RBTDBManager dbManager = RBTDBManager.getInstance();
		while(infPath != null)
		{
			try
			{
				logger.info("RBT::inside while starting the loop");
				String thisLine;
				
				File f = new File(infPath);
				File[] allFiles = f.listFiles();
				
				for(int i = 0; i < allFiles.length && allFiles.length > 0; i++)
				{
					File oneFile = allFiles[i];
					if(oneFile.isFile())
					{
						FileInputStream fis = new FileInputStream(oneFile);
						BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
						
						while ((thisLine = reader.readLine()) != null)
						{
							int k = 0;
							StringTokenizer st = new StringTokenizer(thisLine,"&");
							while(st.hasMoreTokens())
							{
								k++;
								String nextTkn = st.nextToken();
								if(k==3)
								{
									logger.info("RBT::Third Token is.....  "+nextTkn);
									String songName = nextTkn;
									String songURL = httpLink + songInfoPage + operatorAccount + "&" + operatorPassword + "&songname=" + songName + "&" + operator;
									logger.info("RBT::url is.....  " + songURL);
									songURL = songURL.replace(' ', '+');
									RBTHTTPProcessing rbt = RBTHTTPProcessing.getInstance();
									
									Date requestedTimeStamp = new Date();
									String outputString = rbt.makeRequest1(songURL, songName, "SONG_DAEMON");
									Date responseTimeStamp = new Date();
									
									long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());
									String requestedTimeString = formatter.format(requestedTimeStamp);
									
									if(outputString != null)
									{
										outputString = outputString.trim();
										StringTokenizer stz = new StringTokenizer(outputString,"|",true);
										String returnCode=stz.nextToken();
										stz.nextToken(); //to ignore delimeter "|"
										logger.info("RBT::returnCode is....   " + returnCode);
									
										if(returnCode.equals("0") || returnCode.equals("4"))
										{
											WriteSDR.addToAccounting(songDaemonLogPath, rotationSize, "RBT_QUERY_SONG", songName, "song", "query_song", "success", requestedTimeString, differenceTime+"", "song_daemon", songURL, outputString);
											String toneCode =stz.nextToken();
											logger.info("RBT::toneCode is....   " + toneCode);
											
											String album = null; 
                                            int delimeterCount = 1; 
                                            while(stz.hasMoreTokens()) 
                                            { 
                                                    String token = stz.nextToken(); 
                                                    if(token.equalsIgnoreCase("|")) 
                                                    { 
                                                            delimeterCount++; 
                                                            continue; 
                                                    } 

                                                    if(delimeterCount == 13) 
                                                    { 
                                                            album = token; 
                                                            break; 
                                                    } 
                                            } 

											
											ClipMinimal clip = null;
											clip = dbManager.getClipByName(songName);
											
											if(clip == null)
											{
												clip = dbManager.createClipWithID(Integer.parseInt(toneCode), songName, songName, toneCode, songName, null, null, "y", toneCode, "DEFAULT", null, null, null, album, null,null,null);
												if(clip != null)
													logger.info("RBT:: successfully inserted clip");
												else
												{
													logger.info("RBT:: failed to insert clip");
													new File(infPath+"/MissedSongs").mkdirs();
													Tools.writeTFile(new File(infPath+"/MissedSongs/insert_failed_songs" + fileNameformatter.format(requestedTimeStamp) + ".txt"), songName);
												}
											}
											else
											{
												clip = dbManager.updateClip(clip.getClipId(), songName, songName, toneCode, songName, null, null, "y", toneCode, "DEFAULT_CLIP", null, null, null, album, clip.getDemoFile(),clip.getArtist(),clip.getClipInfo());
												if(clip != null)
													logger.info("RBT:: successfully inserted clip");
												else
												{
													logger.info("RBT:: failed to insert clip");
													new File(infPath+"/MissedSongs").mkdirs();
													Tools.writeTFile(new File(infPath+"/MissedSongs/missed_songs" + fileNameformatter.format(requestedTimeStamp) + ".txt"), songName);
												}
											}
										}
										else
										{
											WriteSDR.addToAccounting(songDaemonLogPath, rotationSize, "RBT_QUERY_SONG", songName, "song", "query_song", "error_response", requestedTimeString, differenceTime+"", "song_daemon", songURL, outputString);
											new File(infPath+"/MissedSongs").mkdirs();
											Tools.writeTFile(new File(infPath+"/MissedSongs/missed_songs" + fileNameformatter.format(requestedTimeStamp) + ".txt"), songName);
				
										}
									}
									else
									{
										WriteSDR.addToAccounting(songDaemonLogPath, rotationSize, "RBT_QUERY_SONG", songName, "song", "query_song", "null_error_response", requestedTimeString, differenceTime+"", "song_daemon", songURL, outputString);
									}
									break;
								}
							}
						}
						//closing the file reader and the input stream
						try
						{
							if(reader != null)
								reader.close();
						}
						catch(Exception e)
						{
							logger.error("", e);
						}
						try
						{
							if(fis != null)
								fis.close();
						}
						catch(Exception e)
						{
							logger.error("", e);
						}
						
						//moving the processed file to a different path
						String completedFilePath = infPath + "/completed/";
						File completedFilesDir = new File(completedFilePath);
						if(!completedFilesDir.exists())
							completedFilesDir.mkdirs();
						logger.info("RBT::moving file " + oneFile.getName());
						if(oneFile.renameTo(new File(completedFilePath + oneFile.getName())))
							logger.info("RBT::moving of file " + oneFile.getName() + " is successful");
						else
							logger.info("RBT::moving of file " + oneFile.getName() + " failed");
					}
					else
					{
						logger.info("RBT::" + oneFile.getName() + " is not a file");
					}
				}
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
			finally
			{
				try
				{
					logger.info("RBT::sleeping for " + sleepMinutes + " minute(s)");
					Thread.sleep(sleepMinutes*60*1000);
				}
				catch(Exception e)
				{
					logger.error("", e);
				}
			}
		}
	}
	
	public static void main(String args[])
	{
		BulkClipUpload bulkClipUploadThread = new BulkClipUpload();
		bulkClipUploadThread.start();
	}
}