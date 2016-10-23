package com.onmobile.apps.ringbacktones.dncto;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.onmobile.apps.ringbacktones.dncto.rules.Rule;
import com.onmobile.apps.ringbacktones.dncto.rules.RulesParser;
import com.onmobile.apps.ringbacktones.dncto.rules.VFSRBTRule;
import com.onmobile.dnctoservice.exception.DNCTOException;
import com.onmobile.dnctoservice.plugin.RuleEngine;
import com.onmobile.dnctoservice.plugin.util.DNCTOPluginUtil;

public class VFSRBTRuleEngine implements RuleEngine {
	
	private static Logger logger = Logger.getLogger(RBTRuleEngine.class);

	@Override
	public boolean applyRules(File sourceFile, File filteredFile) throws DNCTOException {
		System.out.println("applyRule of VFSRBTRuleEngine");
		if (sourceFile == null)
		{
			logger.error("sourceFile is null");
			return false;
		}
		if (filteredFile == null)
		{
			logger.error("filteredFile is null");
			return false;
		}

		Rule rule = RulesParser.parseRulesXML(VFSRBTRule.class);
		if (logger.isDebugEnabled())
			logger.debug("RBT DNCTO Rule: " + rule);
		if (rule == null)
			return false;

		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try
		{
			bufferedReader = new BufferedReader(new FileReader(sourceFile));
			bufferedWriter = new BufferedWriter(new FileWriter(filteredFile));

			String line = null;
			while ((line = bufferedReader.readLine()) != null)
			{
				if (logger.isDebugEnabled())
					logger.debug("Line: " + line);

				line = line.trim();
				if (line.length() == 0)
					continue;

				String subscriberID = line.substring(0, line
						.indexOf(DNCTOPluginUtil.delimiter));
				DNCTOContext dnctoContext = new DNCTOContext(subscriberID,
						null);
				dnctoContext.setLine(line);
				
				if (logger.isDebugEnabled())
					logger.debug("dnctoContext: " + dnctoContext);

				if (!rule.applyRule(dnctoContext))
				{
					if (logger.isDebugEnabled())
					{
						logger.debug("Adding subscriber: " + subscriberID
								+ " to filtered file");
					}

					bufferedWriter.write(subscriberID + "," + dnctoContext.getReason());
					bufferedWriter.newLine();
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
