package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.util.List;

public class USSDResponseBuilder {

	public static String convertToResponse(String welcomeMessage, List<USSDNode> menu, boolean moreOptionRequired, String moreOptionURL, int startIndex) {
		int messageLength = USSDConfigParameters.getInstance().getMessageLength();
		if(moreOptionRequired) {
			//reducing the length to insert 0.More string in the menu
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
