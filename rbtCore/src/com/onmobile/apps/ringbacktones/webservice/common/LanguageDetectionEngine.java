package com.onmobile.apps.ringbacktones.webservice.common;

import org.apache.log4j.Logger;

public class LanguageDetectionEngine
{
	private static Logger logger = Logger.getLogger(LanguageDetectionEngine.class);

	public static LangDetectionResult recognizeLang(char[] decodedData)
	{
		LangDetectionResult result = new LangDetectionResult();
		for (char c : decodedData)
		{
			//logger.info("********recognizeLang***** " + (int)c);
			LanguageCodeMap.doesMatch(c, result);
		}

		return result;
	}

	public static void main(String[] args)
	{
		logger.info("RBT:: " + recognizeLang(new char[] { 0x109, 0x23e, 0x234, 0x1345 }));
	}

	public static LangDetectionResult recognizeLang(String data,
			String charSetType)
	{
		if (charSetType == null)
			charSetType = "UTF-8";

		//Charset charSet = Charset.forName(charSetType);
		//char decodedData[] = charSet.decode(ByteBuffer.wrap(data.getBytes())).array();
		char decodedData[] = data.toCharArray();

		return recognizeLang(decodedData);
	}
}
