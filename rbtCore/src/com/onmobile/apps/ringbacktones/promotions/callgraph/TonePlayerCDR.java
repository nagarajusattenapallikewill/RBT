/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions.callgraph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.common.Utility;

/**
 * @author vinayasimha.patil
 * 
 */
public class TonePlayerCDR
{
	private static Logger logger = Logger.getLogger(TonePlayerCDR.class);

	private String callerID = null;
	private String calledID = null;
	private Date calledTime = null;
	private int clipID;

	/**
	 * @param callerID
	 * @param calledID
	 * @param calledTime
	 * @param clipID
	 */
	public TonePlayerCDR(String callerID, String calledID, Date calledTime,
			int clipID)
	{
		super();
		this.callerID = callerID;
		this.calledID = calledID;
		this.calledTime = calledTime;
		this.clipID = clipID;
	}

	/**
	 * @return the callerID
	 */
	public String getCallerID()
	{
		return callerID;
	}

	/**
	 * @param callerID
	 *            the callerID to set
	 */
	public void setCallerID(String callerID)
	{
		this.callerID = callerID;
	}

	/**
	 * @return the calledID
	 */
	public String getCalledID()
	{
		return calledID;
	}

	/**
	 * @param calledID
	 *            the calledID to set
	 */
	public void setCalledID(String calledID)
	{
		this.calledID = calledID;
	}

	/**
	 * @return the calledTime
	 */
	public Date getCalledTime()
	{
		return calledTime;
	}

	/**
	 * @param calledTime
	 *            the calledTime to set
	 */
	public void setCalledTime(Date calledTime)
	{
		this.calledTime = calledTime;
	}

	/**
	 * @return the clipID
	 */
	public int getClipID()
	{
		return clipID;
	}

	/**
	 * @param clipID
	 *            the clipID to set
	 */
	public void setClipID(int clipID)
	{
		this.clipID = clipID;
	}

	public static TonePlayerCDR buildTonePlayerCDRFromLog(String cdrLog)
	{
		if (cdrLog == null)
			return null;

		try
		{
			/*
			 * @formatter:off
			 * 
			 * (non-Javadoc)
			 * TONE Player CDR Log Format:
			 * callerID,calledID,CallRef,calledTime,CIC,DTMFPressed,SongsPlayed;
			 * 
			 * CalledTime Format: yyyyMMddHHmmss-SSS
			 * SongsPlyed Format: <song1>.wav-[no of timelooped]-playedStatus;...;<songn>.wav-[no of timelooped]-playedStatus
			 */
			/* @formatter:on
			 */

			String[] logTokens = cdrLog.split(",");
			String callerID = Utility.trimCountryPrefix(logTokens[0]);
			String calledID = Utility.trimCountryPrefix(logTokens[1]);

			DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss-SSS");
			Date calledTime = dateFormat.parse(logTokens[3]);

			String[] playedSongs = logTokens[6].split(";");
			String rbtWavFile = playedSongs[playedSongs.length - 1].split("-")[0];
			rbtWavFile = rbtWavFile.replaceAll("\\.[w|W][a|A][v|V]$", "");

			int clipID = 0;
			Clip clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(
					rbtWavFile);
			if (clip != null)
				clipID = clip.getClipId();

			TonePlayerCDR tonePlayerCDR = new TonePlayerCDR(callerID, calledID,
					calledTime, clipID);
			return tonePlayerCDR;
		}
		catch (Exception e)
		{
			logger.error("Failed to parse tone player CDR: " + cdrLog, e);
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("TonePlayerCDR [callerID=");
		builder.append(callerID);
		builder.append(", calledID=");
		builder.append(calledID);
		builder.append(", calledTime=");
		builder.append(calledTime);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append("]");
		return builder.toString();
	}
}
