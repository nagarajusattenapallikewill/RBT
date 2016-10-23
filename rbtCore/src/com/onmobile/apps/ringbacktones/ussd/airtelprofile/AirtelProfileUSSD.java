package com.onmobile.apps.ringbacktones.ussd.airtelprofile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.ussd.airtel.USSDResponse;
import com.onmobile.apps.ringbacktones.ussd.common.StringUtils;
import com.onmobile.apps.ringbacktones.ussd.common.USSDCacheManager;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDNode;
import com.onmobile.apps.ringbacktones.ussd.common.USSDWebServiceController;
public class AirtelProfileUSSD {
	private static USSDCacheManager cacheManager=null;
	private static USSDWebServiceController ussdWebserviceController=null;
	private static Logger basicLogger = Logger.getLogger(AirtelProfileUSSD.class);
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	//HashMap<String, String> languageMap=new HashMap<String, String>();
    StringBuilder responseBuilder=new StringBuilder();
    
	public AirtelProfileUSSD(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
		cacheManager=new USSDCacheManager();
		ussdWebserviceController=new USSDWebServiceController();
		basicLogger.info(" AirtelProfileUSSD : "+input);
	}


	public void processProfileMenu(){
		basicLogger.debug(" processProfileMenu Entered");
		String language=input.get("language");
		String isPrepaid=input.get("isprepaid");
		String lang = USSDConfigParameters.getInstance().getParameter("LANGUAGE_NAME");
		String langval = USSDConfigParameters.getInstance().getParameter("LANGUAGE_VALUE");
        ArrayList<String> langList=StringUtils.tokenizeArrayList(lang,null);
		ArrayList<String> langvalList=StringUtils.tokenizeArrayList(langval,null);
		
		ArrayList<AirtelProfileBean> profileList=cacheManager.populateLanguageProfileMap(langList,langvalList);
		ArrayList<AirtelProfilesClip> profileClip=null;
		if(profileList!=null){
			for(int a=0;a<profileList.size();a++){
				if(profileList.get(a).getLangvalue().equalsIgnoreCase(language)){
					profileClip=profileList.get(a).getProfileClips();
					break;
				}
			}
		}else
			basicLogger.debug(" processProfileMenu profileList is NULL ");
		//for Testing

		/*String s[]={"Lunch","Meeting","Busy","Dinner","Sleep","ETC"};
		int s1[]={1,2,3,4,5,6};
		profileClip =new ArrayList<AirtelProfilesClip>();
		for(int b=0;b<s1.length;b++){
			AirtelProfilesClip p=new AirtelProfilesClip();
			p.setSongName(s[b]);
			p.setClipId(s1[b]);
			profileClip.add(p);
		}*/

		if(profileClip==null) {
			basicLogger.error(" processProfileMenu profile does not exists");
			responseBuilder=new StringBuilder();
			responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
			//responseBuilder.append("&actionType=noofhrs&action=");
			String resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("NO_PROFILE"),responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
			basicLogger.debug(" No profile resp : "+resp);
			try {
				/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
				response.getWriter().println(resp);*/
				new USSDResponse().sendResponse(response, resp);
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		for(int i=0; i<profileClip.size(); i++) {
			responseBuilder=new StringBuilder();
			responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
			responseBuilder.append("&actionType=noofhrs&action=rbtprofile&"+"language=");
			responseBuilder.append(language);
			responseBuilder.append("&isprepaid=");
			responseBuilder.append(isPrepaid);
			responseBuilder.append("&clipName=");
			responseBuilder.append(profileClip.get(i).getSongName());
			responseBuilder.append("&clipId=");
			responseBuilder.append(profileClip.get(i).getClipId());
			responseBuilder.append("&clipalias=");
			responseBuilder.append(profileClip.get(i).getSmsAlias());
			if(isProfileOfDayType(profileClip.get(i).getSmsAlias())){
				responseBuilder.append("&durationType=");
				responseBuilder.append("day");
			}else{
				responseBuilder.append("&durationType=");
				responseBuilder.append("hour");
				
			}
			menu.add(new USSDNode(i, 0, profileClip.get(i).getSongName(), responseBuilder.toString()));
		}
		try {
			/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
			response.getWriter().println(getResponse());*/
			new USSDResponse().sendResponse(response, getResponse());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getResponse() {
		String nextNodeId = input.get("next");
		int startIndex = 0;
		String backUrl=null;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}
		if(startIndex<0)
			startIndex=0;
		if(startIndex==0){
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=languageprofile"+"&isprepaid="+input.get("isprepaid");
		}else{
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=rbtprofile&language="+input.get("language")+"&isprepaid="+input.get("isprepaid");;
		}
		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("RBT_PROFILE");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelProfileUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=rbtprofile&language="+input.get("language")+"&isprepaid="+input.get("isprepaid"), true,backUrl,startIndex);
	}

	public void processSpecialCallerId(){
	
		String answer=input.get(USSDConfigParameters.getInstance().getParameter("ANS_PARAM"));
		basicLogger.debug(" processSpecialCallerId : "+answer);
		if(answer!=null&&!answer.equalsIgnoreCase("$answer$")&&answer.length()>8){
			input.put("callerid",answer);
			processShowChargeMessage();
			return;
		}
		responseBuilder=new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		responseBuilder.append("&actionType=callerid&action=rbtprofile&language=");
		responseBuilder.append(input.get("language"));
		responseBuilder.append("&isprepaid=");
		responseBuilder.append(input.get("isprepaid"));
		responseBuilder.append("&clipName=");
		responseBuilder.append(input.get("clipName"));
		responseBuilder.append("&clipId=");
		responseBuilder.append(input.get("clipId"));
		responseBuilder.append("&clipalias=");
		responseBuilder.append(input.get("clipalias"));
		responseBuilder.append("&hours=");
		responseBuilder.append(input.get("hours"));
		responseBuilder.append("&durationType=");
		responseBuilder.append(input.get("durationType"));
		
		String resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("CALLER_ID"),responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		basicLogger.debug(" processSpecialCallerId resp : "+resp);
		try {
			/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
			response.getWriter().println(resp);*/
			new USSDResponse().sendResponse(response, resp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void processDurationInput(){
		String resp=null;
		int hrs=0;
		String answer=input.get(USSDConfigParameters.getInstance().getParameter("ANS_PARAM"));
		basicLogger.debug(" processDurationInput ans : "+answer);
		if(answer!=null&&answer.length()>=1&&!answer.equalsIgnoreCase("$answer$")){
			if(input.get("durationType")!=null&&input.get("durationType").equalsIgnoreCase("day")){
				basicLogger.debug(" processDurationInput day type ");
				 try{
				    	hrs=Integer.parseInt(answer.trim());
				    	hrs=24*hrs;
				    }catch(Exception e){
				    	
				    }
			}else{
			    try{
			    	hrs=Integer.parseInt(answer.trim());
			    }catch(Exception e){
			    	
			    }
			}
			
			try {
				basicLogger.debug(" processDurationInput hrs "+hrs);
				input.put("hours",hrs+"");
				processCallerType();
				return;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		responseBuilder=new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		responseBuilder.append("&action=rbtprofile&language=");
		responseBuilder.append(input.get("language"));
		responseBuilder.append("&isprepaid=");
		responseBuilder.append(input.get("isprepaid"));
		responseBuilder.append("&clipName=");
		responseBuilder.append(input.get("clipName"));
		responseBuilder.append("&clipId=");
		responseBuilder.append(input.get("clipId"));
		responseBuilder.append("&clipalias=");
		responseBuilder.append(input.get("clipalias"));
		responseBuilder.append("&durationType=");
		responseBuilder.append(input.get("durationType"));
		if(isProfileOfDayType(input.get("clipalias"))){
			basicLogger.debug(" processDurationInput clipalias Day type ");
			responseBuilder.append("&durationType=");
			responseBuilder.append("day");
			resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("DURATION_MESSAGE_DAY"), responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		}else{
			responseBuilder.append("&durationType=");
			responseBuilder.append("hour");
			resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("DURATION_MESSAGE"), responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		}
			 
		basicLogger.debug(" processDurationInput resp : "+resp);
		try {
			/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_PLAIN_REQUEST);
			response.getWriter().println(resp);*/
			new USSDResponse().sendResponse(response, resp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void processConfirmSelection(){
		
		String callerid=input.get("callerid");
		String durationType=input.get("durationType");
		if(input.get("callerType").equalsIgnoreCase("ALL"))
			callerid="ALL";
		basicLogger.debug(" processConfirmSelection callerId : "+callerid);
		String resp=null;
		/*String lang = USSDConfigParameters.getInstance().getParameter("LANGUAGE_NAME");
		String langval = USSDConfigParameters.getInstance().getParameter("LANGUAGE_VALUE");
		ArrayList<String> langList=StringUtils.tokenizeArrayList(lang,null);
		ArrayList<String> langvalList=StringUtils.tokenizeArrayList(langval,null);
		for(int i=0;i<langList.size();i++){
			languageMap.put(langvalList.get(i).toLowerCase(),langList.get(i).toLowerCase());
			
		}*/
		
		String result=ussdWebserviceController.addProfileSel(null,callerid, input.get("subscriber"),input.get("isprepaid"),input.get("clipId"), input.get("hours").trim());
		responseBuilder=new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		responseBuilder.append("action=rbtprofile&language=");
		responseBuilder.append(input.get("language"));
		responseBuilder.append("&isprepaid=");
		responseBuilder.append(input.get("isprepaid"));
		
		if(result.equalsIgnoreCase("success")){
			if(durationType!=null&&durationType.equalsIgnoreCase("day")){
				int days=0;
				try{
					days=Integer.parseInt(input.get("hours").trim());
					days=days/24;
				}catch(Exception e){
					
				}
				resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("CONFIRM_MESSAGE1")+" "+input.get("clipName")+" "+USSDConfigParameters.getInstance().getParameter("CONFIRM_MESSAGE2")+days+" days",responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
			}else
			   resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("CONFIRM_MESSAGE1")+" "+input.get("clipName")+" "+USSDConfigParameters.getInstance().getParameter("CONFIRM_MESSAGE2")+input.get("hours")+" hours",responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		}else{
			
			resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("CONFIRM_MESSAGE_FAIL"), responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		}

		basicLogger.debug(" processConfirmSelection result & resp : "+result+" : "+resp);
		// resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("CHARGE_MESSAGE1")+" "+amount+" "+USSDConfigParameters.getInstance().getParameter("CHARGE_MESSAGE2"), USSDConfigParameters.getInstance().getUSSDHostURL() + "&actionType=callerid&&action=rbtprofile&language="+language+"&isprepaid="+isPrepaid+"&clipName="+clipname+"&clipId="+clipId+"&hours="+hrs+"&callerType="+callerType, (String)input.get("next"));

		try {
			/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
			response.getWriter().println(resp);*/
			new USSDResponse().sendResponse(response, resp);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void processShowChargeMessage(){
		basicLogger.debug(" processShowChargeMessage : ");
		String amount=cacheManager.getchargeAmount();
		if(amount==null)
			amount=USSDConfigParameters.getInstance().getParameter("DEFAULT_AMOUNT");
		responseBuilder =new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL()).append("&actionType=callerid&action=rbtprofile&language=").append(input.get("language")).append("&isprepaid=").append(input.get("isprepaid")).append("&clipName=").append(input.get("clipName"))
		               .append("&clipId=").append(input.get("clipId")).append("&hours=").append(input.get("hours")).append("&callerType=").append(input.get("callerType"));
		responseBuilder.append("&clipalias=");
		responseBuilder.append(input.get("clipalias"));
		responseBuilder.append("&durationType=");
		responseBuilder.append(input.get("durationType"));
		String resp=null;
		if(amount.equalsIgnoreCase("0")){
			resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("FREE_PROFILE")+" "+USSDConfigParameters.getInstance().getParameter("CHARGE_MESSAGE3"),responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		}else
		    resp=AirtelProfileUSSDResponseBuilder.getNormalResponse(USSDConfigParameters.getInstance().getParameter("CHARGE_MESSAGE1")+" "+amount+" "+USSDConfigParameters.getInstance().getParameter("CHARGE_MESSAGE2"),responseBuilder.toString(), (String)input.get("next"),processInputParameters(input));
		responseBuilder =new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL()).append("&actionType=confirm&action=rbtprofile&language=").append(input.get("language")).append("&isprepaid=").append(input.get("isprepaid")).append("&clipName=").append(input.get("clipName"))
		               .append("&clipId=").append(input.get("clipId")).append("&hours=").append(input.get("hours")).append("&callerType=").append(input.get("callerType")).append("&amount=").append(amount).append("&callerid=").append(input.get("callerid"));
		responseBuilder.append("&clipalias=");
		responseBuilder.append(input.get("clipalias"));
		responseBuilder.append("&durationType=");
		responseBuilder.append(input.get("durationType"));
		resp=resp+("`1`")+responseBuilder.toString();
		try {
			basicLogger.debug(" processShowChargeMessage resp : "+resp);
			/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
			response.getWriter().println(resp);*/
			new USSDResponse().sendResponse(response, resp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void processCallerType() throws IOException {

		basicLogger.debug(" processCallerType : ");
		String mainMenu = USSDConfigParameters.getInstance().getParameter("CALLERID_OPTION");
		if(StringUtils.isEmpty(mainMenu)) {
			basicLogger.error("CallerType menu is not configured in the ussdconfig.properties file");
			return;
		}
		String []mainMenuItems = mainMenu.split(",");
		for(int i=0; i<mainMenuItems.length; i++) {
			responseBuilder=new StringBuilder();
			responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL()+"&").append("action=rbtprofile&"+"language=").append(input.get("language")).append("&isprepaid=").append(input.get("isprepaid")).append("&actionType=chargemsg").append("&hours=")
			               .append(input.get("hours")).append("&callerType=").append(mainMenuItems[i]).append("&clipName=").append(input.get("clipName")).append("&clipId=").append(input.get("clipId"));
			responseBuilder.append("&clipalias=");
			responseBuilder.append(input.get("clipalias"));
			responseBuilder.append("&durationType=");
		    responseBuilder.append(input.get("durationType"));
			menu.add(new USSDNode(i, 0, mainMenuItems[i], responseBuilder.toString()));
		}
		/*response.setContentType(AirtelProfileUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getCallerTypeResponse());*/
		new USSDResponse().sendResponse(response, getCallerTypeResponse());
	}

	public String getCallerTypeResponse() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=true;
		String backUrl=null;
		String moreUrl;
		int startIndex = 0;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}
		if(startIndex<0)
			startIndex=0;
		responseBuilder=new StringBuilder();
		responseBuilder.append(USSDConfigParameters.getInstance().getUSSDHostURL()+"&").append("action=rbtprofile&"+"language=").append(input.get("language")).append("&isprepaid=").append(input.get("isprepaid")).append("&hours=")
		               .append(input.get("hours")).append("&clipName=").append(input.get("clipName")).append("&clipId=").append(input.get("clipId"));
		responseBuilder.append("&clipalias=");
		responseBuilder.append(input.get("clipalias"));
		responseBuilder.append("&durationType=");
		responseBuilder.append(input.get("durationType"));
		if(startIndex==0){
			backUrl=responseBuilder.toString()+"&actionType=noofhrs";
		}else{
			backUrl=responseBuilder.toString() + "&actionType=callerid";
		}
        moreUrl=responseBuilder.toString()+"&actionType=callerid";
		if(startIndex<0)
			startIndex=0;

		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = USSDConfigParameters.getInstance().getParameter("CALLERID_MSG");
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}
		return AirtelProfileUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				moreUrl, backOptionRequired,backUrl,startIndex);
	}

	public String processInputParameters(Map<String, String> input) {
		StringBuilder resp=new StringBuilder();
		resp.append(USSDConfigParameters.getInstance().getUSSDHostURL());
		Set<String> keySet = input.keySet();
		for (String key : keySet)
		{
			if(!(key.equalsIgnoreCase("subscriber") ||key.equalsIgnoreCase("answer")|| key.equalsIgnoreCase("invalidResp")))
			          resp.append("&").append(key).append("=").append(input.get(key));
		}
		basicLogger.debug(" processInputParameters resp "+resp.toString());
		return resp.toString();
	}
	
	public boolean isProfileOfDayType(String alias){
		if(alias==null)
			return false;
		ArrayList<String> aliasList=StringUtils.tokenizeArrayList(USSDConfigParameters.getInstance().getParameter("PROFILE_ALIAS_DAYS"), ",");
		ArrayList<String> clipalisList=StringUtils.tokenizeArrayList(alias, ",");
		if(aliasList!=null&&clipalisList!=null){
			for(int i=0;i<aliasList.size();i++){
				 for(int j=0;j<clipalisList.size();j++){
					 if(clipalisList.get(j).equalsIgnoreCase(aliasList.get(i))){
						 return true;
					 }
					
				}
			}
		}
		return false;
	}
}
