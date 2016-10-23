package com.onmobile.apps.ringbacktones.webservice.common;

public class MatchResult implements Comparable<MatchResult>
{
	public String locale = null;
	public String loclaeKey = null;
	public int match = 0;

	public String getLoclaeKey()
	{
		return loclaeKey;
	}
	public void setLoclaeKey(String loclaeKey)
	{
		this.loclaeKey = loclaeKey;
	}

	public MatchResult(String locale, int match, String localeKey)
	{
		this.locale = locale;
		this.match = match;
		this.loclaeKey = localeKey;
	}

	public int compareTo(MatchResult incomingMatch)
	{
		if (this.match > incomingMatch.match)
			return -1;
		else if (this.match < incomingMatch.match)
			return 1;
		else
			return 0;
	}
}
