/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.dncto.DNCTOConstants.SubscriberStatus;
import com.onmobile.dnctoservice.exception.DNCTOException;
import com.onmobile.dnctoservice.plugin.Translator;
import com.onmobile.dnctoservice.plugin.util.DNCTOPluginUtil;

/**
 * RBT implementation class for the DNCTO {@link Translator}. This class parses
 * the TLOGs and generates the output file in the format required by DNCTO
 * framework.
 * 
 * @author vinayasimha.patil
 */
public class RBTTranslator implements Translator
{
	/**
	 * Log4j logger object
	 */
	private static Logger logger = Logger.getLogger(RBTTranslator.class);

	/**
	 * Holds the channel name of the RBT set by DNCTO framework.
	 */
	private String channelName = null;

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.dnctoservice.plugin.Translator#getChannelName()
	 */
	/**
	 * Returns the channel name of RBT.
	 * 
	 * @return the channel name of RBT.
	 */
	@Override
	public String getChannelName()
	{
		return channelName;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.dnctoservice.plugin.Translator#setChannelName(java.lang.
	 * String)
	 */
	/**
	 * Sets the channel name. This method will be called by DNCTO framework
	 * while initializing the translator for RBT.
	 * 
	 * @param channelName
	 *            the channel name for RBT
	 */
	@Override
	public void setChannelName(String channelName)
	{
		this.channelName = channelName;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.dnctoservice.plugin.Translator#translate(java.io.File,
	 * java.io.File)
	 */
	/**
	 * Translates the TLOGs to the format required by the DNCTO framework i.e.
	 * subscriberID and data in JSONObject notation. Format:
	 * <tt>subscriberID,{"rbtChannelName":{"subscriberStatus":[time]}}</tt>.
	 * 
	 * @param sourceFile
	 *            file reference of TLOG to be translated
	 * @param translatedFile
	 *            file reference where the translated data has to be written
	 * @return returns true if successfully translated, otherwise false
	 * @throws DNCTOException
	 *             if unable to translate the data
	 */
	@Override
	public boolean translate(File sourceFile, File translatedFile)
			throws DNCTOException
	{
		if (sourceFile == null)
		{
			logger.error("sourceFile is null");
			return false;
		}
		if (translatedFile == null)
		{
			logger.error("translatedFile is null");
			return false;
		}

		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try
		{
			DateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss,SSS:");

			bufferedReader = new BufferedReader(new FileReader(sourceFile));
			bufferedWriter = new BufferedWriter(new FileWriter(translatedFile));

			String line = null;
			while ((line = bufferedReader.readLine()) != null)
			{
				if (logger.isDebugEnabled())
					logger.debug("Line to be translated: " + line);

				line = line.trim();
				if (line.length() == 0)
					continue;

				try
				{
					/*
					 * TLOG File Format:
					 * TIMESTAMP:
					 * |THREAD|SITE_ID|MSISDN|SUB_TYPE|SBN_ID/EVT_ID|SRV_KEYWORD
					 * |CHARGE_TYPE
					 * |PARENT_KEYWORD|AMOUNT|MODE|USER|REQUEST_DATE|INVOICE_DATE
					 * |EXPIRY_DATE
					 * |RETRY_COUNT|CYCLE_STATUS|GRACE_COUNT|NEW_SRV_KEYWORD
					 * |INFER_SUB_STATUS
					 * |CHARGE_KEYWORD|TRIGGER_ID|PACK_KEY[STCK=NEW_TYPE
					 * ,MESSAGE]|[CBAL
					 * =STATUS,BAL_AMOUNT,CHGMODE,BILLING_REFID,RETCODE
					 * ,RETMSG]|[CHRG
					 * =PMT_STATUS,BILL_AMOUNT,CHGMODE,BILLING_REFID,RETCODE
					 * ,RETMSG]|
					 * [REMT=REMOTE_STATUS,RETCODE,RETMSG]|[CBCK=STATUS,RETCODE
					 * ,RETMSG]
					 */
					String[] tokens = line.split("\\|");
					if (!tokens[6].toUpperCase().startsWith("RBT_ACT"))
					{
						// We are interested in only RBT Base requests.
						continue;
					}

					String subscriberID = DNCTOPluginUtil.trimMDN(tokens[3]);
					if (!DNCTOPluginUtil.isValidMDN(subscriberID))
					{
						if (logger.isDebugEnabled())
							logger.debug(subscriberID + " is invalid");
						continue;
					}

					long processedTimeInMillis = dateFormat.parse(
							tokens[0].trim()).getTime();
					SubscriberStatus subscriberStatus = Utility
							.getSubscriberStatus(line, tokens);
					if (subscriberStatus == null)
					{
						// Its not the request we are looking for.
						continue;
					}

					String dnctoFormattedData = Utility.getDNCTOFormattedData(
							subscriberID, channelName, subscriberStatus,
							processedTimeInMillis);

					bufferedWriter.write(dnctoFormattedData);
					bufferedWriter.newLine();
				}
				catch (Exception e)
				{
					logger.error("Error in parsing the line: " + line, e);
				}
			}
		}
		catch (FileNotFoundException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(e.getMessage());
			dnctoException.initCause(e);
			throw dnctoException;
		}
		catch (IOException e)
		{
			if (logger.isDebugEnabled())
				logger.error(e.getMessage(), e);

			DNCTOException dnctoException = new DNCTOException(e.getMessage());
			dnctoException.initCause(e);
			throw dnctoException;
		}
		finally
		{
			if (bufferedReader != null)
			{
				try
				{
					bufferedReader.close();
				}
				catch (IOException e)
				{
				}
			}

			if (bufferedWriter != null)
			{
				try
				{
					bufferedWriter.close();
				}
				catch (IOException e)
				{
				}
			}
		}

		return true;
	}
}
