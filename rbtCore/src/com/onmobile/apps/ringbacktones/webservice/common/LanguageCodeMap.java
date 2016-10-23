package com.onmobile.apps.ringbacktones.webservice.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.apache.log4j.Logger;

public class LanguageCodeMap
{
	private static Logger logger = Logger.getLogger(LanguageCodeMap.class);

	private int startCodeId = -1;
	private int endCodeId = -1;

	private String locale = null;
	private String localeKey = null;

	private static final String PropFilePath = "LanguageCodeMap.csv";

	private static Vector<LanguageCodeMap> codeMaps = null;

	static
	{
		try
		{
			loadAllLocales();
		}
		catch (IOException e)
		{
			logger.error("RBT:: " + e.getMessage(), e);
		}
	}

	public LanguageCodeMap()
	{

	}

	public LanguageCodeMap(int startCodeId, int endCodeid, String locale)
	{
		this.startCodeId = startCodeId;
		this.endCodeId = endCodeid;
		this.locale = locale;
	}

	public LanguageCodeMap(int startCodeId, int endCodeid, String locale,
			String localeKey)
	{
		this.startCodeId = startCodeId;
		this.endCodeId = endCodeid;
		this.locale = locale;
		this.localeKey = localeKey;
	}

	public String getLocaleKey()
	{
		return localeKey;
	}

	public void setLocaleKey(String localeKey)
	{
		this.localeKey = localeKey;
	}

	public static Vector<LanguageCodeMap> getCodeMaps()
	{
		return codeMaps;
	}

	public static void setCodeMaps(Vector<LanguageCodeMap> codeMaps)
	{
		LanguageCodeMap.codeMaps = codeMaps;
	}

	public static void loadAllLocales() throws IOException
	{
		
		InputStream inputStream = LanguageCodeMap.class.getClassLoader().getResourceAsStream(PropFilePath);
		BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
		codeMaps = new Vector<LanguageCodeMap>();
		try
		{
		String lineData=null;
		int i=0;
		while((lineData=bufferedReader.readLine())!=null){

			String details[] = lineData.split(",");
			boolean error = false;
			if (details == null || details.length != 4)
			{
				error = true;
			}
			else
			{
				try
				{
					int start = Integer.parseInt(details[0]);
					int end = Integer.parseInt(details[1]);
					codeMaps.add(new LanguageCodeMap(start, end, details[2], details[3]));
				}
				catch (Exception e)
				{
					error = true;
				}
			}
			i++;
			if (error)
			{
				throw new IOException( "Missing LanguageCodeMap details. Error at line No" + (i) + ":" + lineData);
			}

		}
		}
		finally
		{
			try
			{
				bufferedReader.close();
				inputStream.close();
			}
			catch (Exception e)
			{
				logger.error("RBT:: " + e.getMessage(), e);
			}
		}
	}

	public boolean doesMatch(char data)
	{
		return data >= startCodeId && data <= endCodeId;

	}

	public int getEndCodeId()
	{
		return endCodeId;
	}

	public String getLocale()
	{
		return locale;
	}

	public int getStartCodeId()
	{
		return startCodeId;
	}

	public void setEndCodeId(int endCodeid)
	{
		this.endCodeId = endCodeid;
	}

	public void setLocale(String locale)
	{
		this.locale = locale;
	}

	public void setStartCodeId(int startCodeId)
	{
		this.startCodeId = startCodeId;
	}

	public static void doesMatch(char data, LangDetectionResult result)
	{
		for (LanguageCodeMap codeMap : codeMaps)
		{
			if (codeMap.doesMatch(data))
				result.addMatch(codeMap);
		}
	}
}
