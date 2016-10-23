package com.onmobile.apps.ringbacktones.webservice.common;

import java.util.ArrayList;
import java.util.HashMap;

public class LangDetectionResult
{
	private HashMap<String, Match> matches = new HashMap<String, Match>();
	ArrayList<MatchResult> matchResult = new ArrayList<MatchResult>();

	public HashMap<String, Match> getMatches()
	{
		return matches;
	}

	public void setMatches(HashMap<String, Match> matches)
	{
		this.matches = matches;
	}

	public Match getBestMatch()
	{
		return bestMatch;
	}

	public void setBestMatch(Match bestMatch)
	{
		this.bestMatch = bestMatch;
	}

	private Match bestMatch = null;

	public void addMatch(LanguageCodeMap codeMap)
	{
		Match match = matches.get(codeMap.getLocale());

		if (match == null)
		{
			match = new Match(codeMap);
			matches.put(codeMap.getLocale(), match);
			MatchResult matchRes = new MatchResult(codeMap.getLocale(), match.getNumberOfMatches(), codeMap.getLocaleKey());
			matchResult.add(matchRes);
		}
		else
		{
			match.incrementNumberOfMatches();
		}

		if (bestMatch == null)
			bestMatch = match;
		else if (match != bestMatch && match.isBetterThan(bestMatch))
			bestMatch = match;
	}

	public ArrayList<MatchResult> getMatchResult()
	{
		return matchResult;
	}

	public void setMatchResult(ArrayList<MatchResult> matchResult)
	{
		this.matchResult = matchResult;
	}

	@Override
	public String toString()
	{
		return bestMatch + "\n" + matches;
	}
}
