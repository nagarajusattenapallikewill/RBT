package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;

/**
 * 
 * This class responds with the UMP response 
 * 
 * @author Sreekar
 *
 */
public class USSDResponse {
	public static void main(String[] args) {
		String source = "What is Hot ? -\nYou can call 543215 (call charge RS. 1/min ) Say the song movie name to search for your favorite hello tunes\n-Not showing0.More\n*.Back\n`0`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=1`*`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=mainmenu&next=-1`3`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`4`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`5`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`6`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`7`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`8`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`9`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`10`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&isprepaid=true&action=whatIsHot&next=0&invalidResp=invalidresponse`#`http://172.16.24.53:8080/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$";
//		String source = "Following is Main Menu1.What Is Hot ?\n2.M Search\n3.Popular Song\n4.TOP 10\n5.Free Zone\n6.Hello Tunes Cricket Pack\n7.Manage your subscription\n8.Copy a Hello Tune\n0.More\n`1`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=whatIsHot&isprepaid=true`2`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=search&isprepaid=true`3`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=popularSong&isprepaid=true`4`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=top10&isprepaid=true`5`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=freeZone&isprepaid=true`6`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=cricPack&isprepaid=true`7`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=manageSub&isprepaid=true`8`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=copyTune&isprepaid=true`0`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=mainmenu&next=8`9`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=mainmenu&next=0&invalidResp=invalidresponse`10`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$&action=mainmenu&next=0&invalidResp=invalidresponse`#`http://172.16.29.229:6789/airtelussd/airtelussd.do?subscriber=$subscriber$&answer=$answer$\n";
//		String source = "Hi just sample message";
		System.out.println(new USSDResponse().getUMPResponse(source));
	}
	
	private static Logger _logger = Logger.getLogger(USSDResponse.class);
	
	private String _prefix = "<umpcnv:menu xmlns:umpcnv=\"http://www.onmobile.com/ump/ConvApp\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.onmobile.com/ump/ConvApp ConvApp.xsd \">";
	private String _separator = "<umpcnv:auto-number><umpcnv:onoff>false</umpcnv:onoff><umpcnv:postfix-text></umpcnv:postfix-text></umpcnv:auto-number>";
	private String _postfix = "</umpcnv:menu>";
	private String _resposeTypeRequest = " <umpcnv:ussd><umpcnv:type>REQUEST</umpcnv:type></umpcnv:ussd>";
	private String _resposeTypeNotify = " <umpcnv:ussd><umpcnv:type>NOTIFY</umpcnv:type></umpcnv:ussd>";
	
	private String _replyText = "  <umpcnv:header><umpcnv:text>%TEXT%</umpcnv:text></umpcnv:header>";
	private String _replyOption = "<umpcnv:item><umpcnv:display><umpcnv:text>%OPTIONTEXT%</umpcnv:text></umpcnv:display><umpcnv:responseKey><umpcnv:text>%OPTION%</umpcnv:text></umpcnv:responseKey><umpcnv:action><umpcnv:externalmenu><umpcnv:url><![CDATA[%URL%]]></umpcnv:url><umpcnv:retries>3</umpcnv:retries><umpcnv:type>UMP_MENU</umpcnv:type></umpcnv:externalmenu></umpcnv:action></umpcnv:item>";
	
	public void sendResponse(HttpServletResponse response, String responseStr) throws IOException {
		if(USSDConfigParameters.getInstance().isUMPResponse())
			response.getWriter().println(new USSDResponse().getUMPResponse(responseStr));
		else {
			int urlStartIndex = responseStr.indexOf("`");
			if(urlStartIndex == -1)
				response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
			else
				response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
			response.getWriter().println(responseStr);
		}
	}
	
	protected String getUMPResponse(String strResponse) {
		int urlStartIndex = strResponse.indexOf("`");
		if(urlStartIndex == -1) {
			Map<String, String> optionsMap = new LinkedHashMap<String, String>();
			optionsMap.put("response", strResponse);
			return getFinalResponse(optionsMap, new LinkedHashMap<String, String>());
		}
		String reponseAndURLs = strResponse.substring(0, urlStartIndex);

		String responseText = getResponseString(reponseAndURLs);
		Map<String, String> optionsMap = populateResponseOptions(reponseAndURLs.substring(responseText.length()));
		
		responseText = responseText.replaceAll("\\n", "\\\\\\\\n");
		optionsMap.put("response", responseText);
		
		Map<String, String> optionURLsMap = populateResponseURLs(strResponse.substring(urlStartIndex), optionsMap);
		String finalResponse = getFinalResponse(optionsMap, optionURLsMap);
		_logger.info("inputText->" + strResponse + "\nfinalResponse->" + finalResponse);
		return finalResponse;
	}
	
	private String getResponseString(String actualStr) {
		int index = actualStr.indexOf("1.");
		if(index != -1)
			return actualStr.substring(0, index);
		index = actualStr.indexOf("0.");
		if(index != -1)
			return actualStr.substring(0, index);
		index = actualStr.indexOf("*.");
		if(index != -1)
			return actualStr.substring(0, index);
		index = actualStr.indexOf("#.");
		if(index != -1)
			return actualStr.substring(0, index);
		return actualStr;
	}
	
	private String getFinalResponse(Map<String, String> optionsMap, Map<String, String> optionURLsMap) {
		StringBuilder sb = new StringBuilder();
		sb.append(_prefix);
		if(optionURLsMap.size() > 0) {
			sb.append(_separator);
			sb.append(_resposeTypeRequest);
			sb.append(_replyText.replaceAll("%TEXT%", optionsMap.get("response")));
			appendOptions(optionsMap, optionURLsMap, sb);
		}
		else {
			sb.append(_resposeTypeNotify);
			sb.append(_replyText.replaceAll("%TEXT%", optionsMap.get("response")));
		}
		sb.append(_postfix);
		return sb.toString();
	}
	
	private void appendOptions(Map<String, String> optionsMap, Map<String, String> optionURLsMap, StringBuilder sb) {
		for(Map.Entry<String, String> entry: optionsMap.entrySet()) {
			String key = entry.getKey();
			if(!optionURLsMap.containsKey(key))
				continue;
			
			String replyOption = _replyOption.replaceAll("%OPTION%", key);
			if(entry.getValue() != null && !entry.getValue().equals(""))
				replyOption = replyOption.replaceAll("%OPTIONTEXT%", key + ". " + entry.getValue());
			else
				replyOption = replyOption.replaceAll("%OPTIONTEXT%", "");
			
			String url = optionURLsMap.get(key);
			/*url = url.replaceAll("\\$subscriber\\$", "<msisdn>");
			url = url.replaceAll("\\$answer\\$", "<text>");*/
			url = url.replaceAll("\\$", "\\\\\\$");
			
			replyOption = replyOption.replaceAll("%URL%", url);
			sb.append(replyOption);
		}
	}
	
	private Map<String, String> populateResponseURLs(String strResponse, Map<String, String> optionsMap) {
		Map<String, String> optionURLsMap = new LinkedHashMap<String, String>();
		if(optionsMap.size() <= 0)
			return optionURLsMap;
		
		Iterator<String> itr = optionsMap.keySet().iterator();
		while(itr.hasNext()) {
			String thisOption = itr.next();
			int keyIndex = strResponse.indexOf("`" + thisOption + "`");
			if(keyIndex == -1)
				continue;
			int startIndex = keyIndex + 3;
			int endIndex = strResponse.indexOf("`", startIndex);
			if(endIndex == -1)
				endIndex = strResponse.length();
			optionURLsMap.put(thisOption, strResponse.substring(startIndex, endIndex).trim());
		}
		return optionURLsMap;
	}
	
	private Map<String, String> populateResponseOptions(String strResponse) {
		Map<String, String> optionsMap = new LinkedHashMap<String, String>();
		int thisIndexOption = getNextOptionIndex(-1, strResponse, optionsMap);
		int nextOptionIndex = -1;
		/*if(thisIndexOption == -1)
			optionsMap.put("response", strResponse);*/
		while(nextOptionIndex < strResponse.length() && thisIndexOption != -1) {
			if (thisIndexOption != -1) 
				nextOptionIndex = getNextOptionIndex(thisIndexOption + 2, strResponse, optionsMap);
			if (nextOptionIndex == -1)
				nextOptionIndex = strResponse.length();
			
			String fullOption = strResponse.substring(thisIndexOption, nextOptionIndex);
			int dotIndex = fullOption.indexOf(".");
			if(dotIndex != -1)
				optionsMap.put(fullOption.substring(0, dotIndex), fullOption.substring(dotIndex+1).trim());
			thisIndexOption = nextOptionIndex;
		}
		if(!optionsMap.containsKey("*"))
			optionsMap.put("*", "");
		if(!optionsMap.containsKey("#"))
			optionsMap.put("#", "");
		return optionsMap;
	}
	
	private int getNextOptionIndex(int fromIndex, String str, Map<String, String> optionsMap) {
		int index = 0;
		if (fromIndex != -1)
			index = fromIndex;
		int dotIndex = str.indexOf(".", index);
		try {
			do {
				int digitIndex = dotIndex-1;
				char digit = str.charAt(digitIndex);
				try {
					if(digit == '*' || digit == '#')
						return digitIndex;
					Integer.parseInt(digit+"");
					/*if(index == 0 && (digitIndex - index) > 3)
						optionsMap.put("response", str.substring(index, digitIndex).trim());*/
					return digitIndex;
				}
				catch(Exception e) {
//					optionsMap.put("response", str.substring(index, dotIndex+1));
				}
			}
			while ((dotIndex = str.indexOf(".", dotIndex + 1)) != -1);
		}
		catch (StringIndexOutOfBoundsException e) {
			index = -1;
		}
		return index;
	}
}