package com.onmobile.apps.ringbacktones.ussd.airtelprofile;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.ussd.common.*;

public class AirtelProfileUSSDResponseBuilder {

	private static Logger basicLogger = Logger.getLogger(AirtelProfileUSSDResponseBuilder.class);
	public static String convertToResponse(String welcomeMessage, List<USSDNode> menu, boolean moreOptionRequired, String moreOptionURL, boolean backOptionRequired ,String backOptionUrl,int startIndex) {
	    basicLogger.debug(" convertToResponse "+" welcomemsg : "+welcomeMessage+" moreopturl : "+moreOptionURL+" backurl : "+backOptionUrl);
		int messageLength = USSDConfigParameters.getInstance().getMessageLength();
		String defaultUrl=null;
		if(startIndex<0)
			startIndex=0;
		if(moreOptionRequired) {
			//reducing the length to insert 0.More string in the menu
			messageLength = messageLength - 6;
		}

		if(backOptionRequired){
			//reducing the length to insert *.Back string in the menu
			messageLength = messageLength - 6;
		}
		StringBuilder message = new StringBuilder();
		StringBuilder url = new StringBuilder();
		message.append(welcomeMessage);
		message.append(USSDConfigParameters.getInstance().getMessageNewLine());
		int i=0;
		for(; i<menu.size(); i++) {
			USSDNode node = menu.get(i);
			StringBuilder sb = new StringBuilder();
//			sb.append(node.getNodeId());
			sb.append(i+1);
			sb.append(".");
			sb.append(node.getNodeText());

			sb.append(USSDConfigParameters.getInstance().getMessageNewLine());

			if(moreOptionRequired && i+1==menu.size()) {
				messageLength = messageLength + 6;
//				System.out.println("messageLength " + messageLength);
			}
			if( (message.length() + sb.length()) <= messageLength) {
				message.append(sb);
				sb = null;
				url.append('`');
//				url.append(node.getNodeId());
				url.append(i+1);
				url.append('`');
				url.append(node.getNodeURL());
				defaultUrl=node.getNodeURL();
			} else {
				break;
			}
		}
		if(moreOptionRequired && i!=menu.size()) {
			message.append("0.More");
			message.append(USSDConfigParameters.getInstance().getMessageNewLine());
			url.append("`0`");
			url.append(moreOptionURL);
			url.append("&next=");
			url.append(startIndex+i);
		}

		if(backOptionRequired){
			message.append("*.Back");
			message.append(USSDConfigParameters.getInstance().getMessageNewLine());
			url.append("`*`");
			url.append(backOptionUrl);
			url.append("&next=");
			url.append(startIndex-i);
		}
		for(int j=i;j<10;j++){
			url.append('`');
			url.append(j+1);
			url.append('`');
			url.append(moreOptionURL);
			url.append("&next=");
			url.append(startIndex);
			url.append("&invalidResp=invalidresponse");
		}
		//appending the # for main menu option in the urls
		url.append(optionGotoMainMenu());
		message.append(url);
		url = null;
		return message.toString();
	}
	public static String getNormalResponse(String msg,String backUrl,String index,String currentUrl){
		basicLogger.debug(" getNormalResponse "+" msg : "+msg+" backurl : "+backUrl+" index : "+index);
		StringBuilder message = new StringBuilder();
		StringBuilder url = new StringBuilder();
		message.append(msg);
		message.append(USSDConfigParameters.getInstance().getMessageNewLine());
		message.append("*.Back");
		message.append(USSDConfigParameters.getInstance().getMessageNewLine());
		url.append("`*`");
		url.append(backUrl);
		url.append("&next=");
		url.append(index);
		for(int j=1;j<10;j++){
			url.append('`');
			url.append(j+1);
			url.append('`');
			url.append(currentUrl);
			url.append("&next=");
			url.append(index);
			url.append("&invalidResp=invalidresponse");
		}
		//appending the # for main menu option in the urls
		url.append(optionGotoMainMenu());
		message.append(url);
		url = null;
		return message.toString();
	}
	public static String optionGotoMainMenu() {
		StringBuilder optionMainMenu = new StringBuilder(32);
		optionMainMenu.append("`#`").append(USSDConfigParameters.getInstance().getUSSDHostURL());
		return optionMainMenu.toString();
	}
}
