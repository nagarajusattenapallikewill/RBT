package com.onmobile.apps.ringbacktones.webservice.common;

public class Match
{
	private int numberOfMatches = 1;
	private LanguageCodeMap lang = null;

	public Match(LanguageCodeMap lang)
	{
		this.lang = lang;
	}

	public LanguageCodeMap getLang()
	{
		return lang;
	}

	public int getNumberOfMatches()
	{
		return numberOfMatches;
	}

	public void setLang(LanguageCodeMap lang)
	{
		this.lang = lang;
	}

	public void setNumberOfMatches(int numberOfMatches)
	{
		this.numberOfMatches = numberOfMatches;
	}

	public String getLocale()
	{
		return lang.getLocale();
	}

	public void incrementNumberOfMatches()
	{
		numberOfMatches++;

	}

	public boolean isBetterThan(Match bestMatch)
	{
		return getNumberOfMatches() > bestMatch.getNumberOfMatches();
	}

	@Override
	public String toString()
	{
		return getLocale() + "-" + numberOfMatches;
	}
}
