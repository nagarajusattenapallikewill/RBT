package com.onmobile.apps.ringbacktones.webservice.common;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

public class LanguageIndentifier
{
	private static Logger logger = Logger.getLogger(LanguageIndentifier.class);

	private static String deafultLeastPreferredMatch = "eng";
	private static ParametersCacheManager parametersCacheManager = null;

	static
	{
		try
		{
			init();
		}
		catch (Exception e)
		{
			logger.error("RBT:: " + e.getMessage(), e);
		}
	}

	private static void init()
	{

		if (LanguageCodeMap.getCodeMaps() == null || LanguageCodeMap.getCodeMaps().size() == 0)
		{
			Parameter param = RBTClient.getInstance().getParameter(new ApplicationDetailsRequest("COMMON", "LEAST_PREFFERED_LANGUAGE", (String)null));
			if (param != null && param.getValue() != null && param.getValue().length() > 0)
				deafultLeastPreferredMatch = param.getValue();

			try
			{
				if (LanguageCodeMap.getCodeMaps() == null || LanguageCodeMap.getCodeMaps().size() == 0)
				{
					LanguageCodeMap.loadAllLocales();
					logger.info("All locales initialized");
				}
			}
			catch (IOException e)
			{
				logger.error("RBT:: " + e.getMessage(), e);
			}
			catch (Exception e)
			{
				logger.error("RBT:: " + e.getMessage(), e);
			}
		}
	}

	public static String getLanguage(String data, String charSetType)
	{
		String retValue = deafultLeastPreferredMatch;
		if (data != null)
		{
			if (charSetType != null)
				charSetType = "UTF-8";

			LangDetectionResult langDeteResult = LanguageDetectionEngine.recognizeLang(data, charSetType);
			Match bestMatch = langDeteResult.getBestMatch();

			if (bestMatch != null && bestMatch.getLang() != null
					&& bestMatch.getLang().getLocale() != null)
			{
				if (bestMatch.getNumberOfMatches() == data.length()
						&& bestMatch.getLang().getLocaleKey() != null
						&& bestMatch.getLang().getLocale().trim()
						.equalsIgnoreCase(deafultLeastPreferredMatch.trim()))
				{
					retValue = bestMatch.getLang().getLocaleKey().trim();
				}
				else if (bestMatch.getLang().getLocaleKey().trim().equalsIgnoreCase(deafultLeastPreferredMatch.trim()))
				{
					retValue = getSecondBestMatch(langDeteResult);
				}
				else
				{
					retValue = bestMatch.getLang().getLocaleKey().trim();
				}
			}
		}

		return retValue;
	}

	public static String getSecondBestMatch(LangDetectionResult langDeteResult)
	{
		List<MatchResult> matchResult = langDeteResult.getMatchResult();
		if (matchResult != null)
		{
			Collections.sort(matchResult);
			if (matchResult.size() > 1)
			{
				return matchResult.get(1).getLoclaeKey();
			}
		}
		return deafultLeastPreferredMatch;
	}
	
	public static String identifyLanguage(String searchQuery){
		String language = null;
		language = LanguageIndentifier.getLanguage(searchQuery, "UTF-8");
		Logger.getLogger(LanguageIndentifier.class).info("Language identified by the LanguageIdentifier for the query "+searchQuery+" is "+language);
		return language;
	}
}
