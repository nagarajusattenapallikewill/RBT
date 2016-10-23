package com.onmobile.apps.ringbacktones.ussd.airtelUSSD;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.servlets.ResponseObj;
import com.onmobile.apps.ringbacktones.servlets.SiteURLDetails;
import com.onmobile.apps.ringbacktones.ussd.USSDConstants;
import com.onmobile.apps.ringbacktones.ussd.ControllerInterface;
import com.onmobile.apps.ringbacktones.ussd.USSDBasicFeatures;
import com.onmobile.apps.ringbacktones.ussd.USSDController;
import com.onmobile.apps.ringbacktones.ussd.USSDInfo;
import com.onmobile.apps.ringbacktones.ussd.USSDServletListner;
import com.onmobile.apps.ringbacktones.ussd.USSDSubDetails;
import com.onmobile.apps.ringbacktones.utils.XMLXPathParser;

public class AirtelUSSDController extends USSDController implements ControllerInterface{
	/*author@Abhinav Anand
	 */
	private static Logger logger = Logger.getLogger(AirtelUSSDController.class);
	protected static String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForCallDetails="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";

	public   USSDBasicFeatures ussdFeatures=null;
	public  static RBTDBManager rbtDBManager=null;
	public   AirtelUSSDConstants airtelInfo=null;
	private static String m_class="AirtelUSSDController"; 
//	public AirtelUSSDController(){
//		init();
//		setInfo();
//	}
	@Override
	public USSDBasicFeatures getUssdFeatures() {
		// TODO Auto-generated method stub
		return ussdFeatures;
	}
	@Override
	public void setUssdFeatures(USSDBasicFeatures ussdFeatures) {
		super.setUssdFeatures(ussdFeatures);
		// TODO Auto-generated method stub
		this.ussdFeatures = ussdFeatures;
	}
	public USSDConstants getInfo() {
		return airtelInfo;
	}
	public void setInfo() {
//		super.setInfo();
//		AirtelInfo=new AirtelUSSDConstants(super.info);
		String method="setInfo";
		super.setInfo();
		
		airtelInfo=new AirtelUSSDConstants(info);
		logger.info("info.ussdCatsFreeZone=="+info.ussdCatsFreeZone);
		
		
	}
	public void init(){
		super.init();
		rbtDBManager = RBTDBManager.getInstance();
	}
//	implementation of abstract methodS
	public String processRequest(HashMap parametersMap) {
		String method="processRequest";
		// TODO Auto-generated method stub		
		USSDBasicFeatures features=USSDServletListner.ussdFeatures;
		HashMap USSDMenu=features.getUSSDMenu();
		
		String requestSource=((String)parametersMap.get("source")); 
		logger.info("RBT::entering request coming for=="+requestSource); 
		String subId=(String)parametersMap.get("msisdn");
		String processId=(String)parametersMap.get("processId");
		String pageNo=(String)parametersMap.get("pageNo");
		String request_type=(String)parametersMap.get("request_type");
		String request_value=(String)parametersMap.get("request_value");
		String status=(String)parametersMap.get("status");
		String ans=(String)parametersMap.get("ans");
		String defCatId=(String)parametersMap.get("DEFAULT_CAT_ID");
		Clips defClips=(Clips)parametersMap.get("DEFAULT_CLIP");
		SiteURLDetails destURL=(SiteURLDetails)parametersMap.get("DEST_URL");
		String circleId=(String)parametersMap.get("CIRCLE_ID");
		ArrayList displaySubPack=(ArrayList)parametersMap.get("DISPLAY_SUB_PACK");
		ArrayList subPackTag=(ArrayList)parametersMap.get("SUB_PACK_TAG");
		String responseStr=null;
		logger.info("USSD::processId=="+processId);
		logger.info("USSD::pageNo=="+pageNo);
		logger.info("USSD::request_type=="+request_type);
		logger.info("USSD::request_value=="+request_value);
		if(processId!=null && processId.equalsIgnoreCase("0")){
			boolean isSubAllowed=isSubscriberAllowedForUSSD(subId,destURL);
			if(isSubAllowed){
			responseStr=processMainMenuRequest(subId,pageNo,circleId,USSDMenu);
			}
			
		}else if(request_type.equalsIgnoreCase("manage")){
			String strIsPrepaid=(String)parametersMap.get("isPrepaid");
			String language=(String)parametersMap.get("lang");
			boolean isPrepaid=true;
			USSDSubDetails subDet=null;
			if(request_value==null){
				subDet=getUSSDSubDetails(subId,destURL);
			}else{
				if(strIsPrepaid!=null && strIsPrepaid.equalsIgnoreCase("false")){
					isPrepaid=false;
				}
			}
			responseStr=processManageRequest(subId,circleId,processId,pageNo,request_value,destURL, subDet,isPrepaid,language,displaySubPack,subPackTag,USSDMenu);
		}else if(request_type.equalsIgnoreCase("copy")){
			String parentProcessId=(String)parametersMap.get("parentProcessId");
			String callerNo=(String)parametersMap.get("callerNo");
			String catId=(String)parametersMap.get("catId");
			String clipId=(String)parametersMap.get("clipId");
		
			String copyNo=(String)parametersMap.get("copyNo");
			String defClipId=null;
			if(defClips!=null){
				defClipId=""+defClips.id();
			}
			responseStr=processCopyRequest(subId,ans,circleId,processId,request_value,pageNo,
					callerNo,catId,clipId,parentProcessId,destURL,defClipId,defCatId,copyNo,USSDMenu);
		}else if(request_type.equalsIgnoreCase("songSearch")){
			String strIsPrepaid=(String)parametersMap.get("isPrepaid");
			String language=(String)parametersMap.get("lang");
			String catId=(String)parametersMap.get("catId");
			String clipId=(String)parametersMap.get("clipId");
			boolean isPrepaid=true;
			if(strIsPrepaid!=null && strIsPrepaid.equalsIgnoreCase("false")){
				isPrepaid=false;
			}
			//giftAllowed=false&specialCaller=false&preListen=false
			String giftAllowed=(String)parametersMap.get("giftAllowed");
			String specialCaller=(String)parametersMap.get("specialCaller");
			String preListen=(String)parametersMap.get("preListen");
			String callerNo=(String)parametersMap.get("callerNo");
			String parentProcessId=(String)parametersMap.get("parentProcessId");
			String isDeactive="true";
			 isDeactive=(String)parametersMap.get("isDeactive");
			if(request_value==null && clipId==null){
				 isDeactive=isSubscriberDeactivated( subId, destURL);
			USSDSubDetails subDet=getUSSDSubDetails(subId,destURL);
			if(subDet!=null && !subDet.isPrepaid){
				isPrepaid=false;
			}
			}
			responseStr=processSongSearchRequest(subId,ans,circleId,processId,request_value,
					callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,
					giftAllowed,specialCaller,preListen,USSDMenu,status,isDeactive);
			
		}else if(request_type.equalsIgnoreCase("whatshot") || request_type.equalsIgnoreCase("cricket") || request_type.indexOf("help")!=-1){
			responseStr=processMiscellaneousRequest(subId,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);
			
		}else if(request_type.equalsIgnoreCase("catSearch") || request_type.equalsIgnoreCase("childCatSearch")){
			responseStr=processCatSearchRequest(subId,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);
			
		}else if(request_type.equalsIgnoreCase("MSearch")){
			responseStr=processMSearchRequest(subId,ans,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);
		}
//		if(responseStr!=null && responseStr.length()>0 && !responseStr.equalsIgnoreCase("null") || !responseStr.equalsIgnoreCase("")){
//			responseStr=responseStr+airtelInfo.newLineCharString;
//		}
		return responseStr;
	}
	public String processMainMenuRequest(String subId,String pageNo, String circleId,
			HashMap USSDMenu){
		return super.processMainMenuRequest(subId, pageNo,  circleId,USSDMenu);
	}
	public String processCopyRequest(String subId,String ans,String circleId,String processId,
			String request_value,String pageNo,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String defClipId,String defCatId,String copyNo,HashMap USSDMenu){
		return super.processCopyRequest(subId, ans, circleId, processId, request_value,pageNo, callerNo, catId,
				clipId, parentProcessId, destURL, defClipId, defCatId, copyNo,USSDMenu);
	}
	public  String processManageRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,USSDSubDetails subDet,
			boolean isPrepaid,String language,ArrayList displaySubPack,
			ArrayList subPackTag,
			HashMap USSDMenu){
		return super.processManageRequest(subId, circleId, processId, pageNo, request_value, 
				destURL,subDet,  isPrepaid, language,displaySubPack, subPackTag,USSDMenu);
	}
	public String processSongSearchRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,String pageNo,String giftAllowed,
			String specialCaller,String preListen,HashMap USSDMenu,String status,String isDeactive){
		return super.processSongSearchRequest(subId, ans, circleId, processId, request_value, 
				callerNo, catId, clipId, parentProcessId, destURL, language, isPrepaid, pageNo, 
				giftAllowed, specialCaller, preListen,USSDMenu,status,isDeactive);
	}
	public String processPrelistenRequest(String subId,String circleId,String processId,
			String request_value,String clipId,boolean isPrepaid,
			String preListen,HashMap USSDMenu){
		return super.processPrelistenRequest(subId, circleId, processId, request_value, clipId, isPrepaid, 
				preListen,USSDMenu);
	}
	public String processDefaultSelRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,boolean goToSongListing,String status){
		return super.processDefaultSelRequest( subId, ans, circleId, processId,
				 request_value, callerNo, catId, clipId, parentProcessId, destURL,
				 language, isPrepaid, pageNo, giftAllowed, specialCaller,
				 preListen,USSDMenu,goToSongListing,status);
	}
	public String processSpecialCallerSelRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,SiteURLDetails destURL
			,String language,boolean isPrepaid,String pageNo,String giftAllowed,String specialCaller,
			String preListen,HashMap USSDMenu,String status){
		return super.processSpecialCallerSelRequest( subId, ans, circleId, processId,
				 request_value, callerNo, catId, clipId, parentProcessId, destURL,
				 language, isPrepaid, pageNo, giftAllowed, specialCaller,
				 preListen,USSDMenu,status);
	}
	public String processGiftRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,SiteURLDetails destURL,
			String language,boolean isPrepaid,String pageNo,String giftAllowed,String specialCaller,
			String preListen,String isDeactive,HashMap USSDMenu){
		return super.processGiftRequest( subId, ans, circleId, processId,
				 request_value, callerNo, catId, clipId, parentProcessId, destURL,
				 language, isPrepaid, pageNo, giftAllowed, specialCaller,
				 preListen,isDeactive,USSDMenu);
	}
	public String processMiscellaneousRequest(String subId,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		return super.processMiscellaneousRequest( subId, circleId, processId, request_value,
				 destURL, pageNo,USSDMenu);
	}
	public String processCatSearchRequest(String subId,String circleId,String processId,
			String request_value,SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		return super.processCatSearchRequest( subId, circleId, processId,
				 request_value, destURL, pageNo,USSDMenu);
	}
	public String processMSearchRequest(String subId,String ans,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		return super.processMSearchRequest(subId,ans,circleId,processId,request_value,
				destURL,pageNo,USSDMenu);
	}
	public  String getBackEndResponse(ResponseObj responseobj,String successMsg){
		return super.getBackEndResponse(responseobj,successMsg);
	}
	public  USSDInfo getUSSDInfo(String processId,String circleId,HashMap ussdMenu){
		String method="getUSSDInfo";
		logger.info("processId=="+processId);
		logger.info("circleId=="+circleId);
		return super.getUSSDInfo(processId, circleId, ussdMenu);
		/*HashMap ussdMenu=USSDMenu;
		USSDInfo ussdInfo=null;
		if(ussdMenu!=null && circleId!=null && circleId.length()>0){
			HashMap circleBasedMap=(HashMap)ussdMenu.get(circleId);
			if(circleBasedMap!=null){
				ussdInfo=(USSDInfo)circleBasedMap.get(processId);
			}
		}
		return ussdInfo;*/
	} 
	public  String getCopyNoDetails(String subId,String copyNumber,HashMap site_url_details,String defClipId,String defCatId){
		return super.getCopyNoDetails(subId, copyNumber,site_url_details, defClipId, defCatId);
	}
	public  String getValidResponseString(String responseString,int pageValue,String moreStr,String enterChoiceStr,int maxStrlegthlimit,boolean numberOrderingStr){
//Assumption:: if "responseString" contains "." then either its length should be less than (strlegthlimit-enterChoiceStr)if it contains only one "." OR if it contains two "." then distance between two "." must be <(strlegthlimit-enterChoiceStr)
		return super.getValidResponseString( responseString,pageValue,moreStr,enterChoiceStr, maxStrlegthlimit,numberOrderingStr,airtelInfo.newLineCharString,airtelInfo.seperatorChar);
	}
	public  String getDynamicURLWithMoreOption(USSDInfo ussdInfo,int pageValue){
		return (super.getDynamicURLWithMoreOption(ussdInfo,pageValue));
		
	}
	public boolean isSubscriberAllowedForUSSD(String subId,SiteURLDetails destURL){
		boolean returnFlag=false;
//		SiteURLDetails destURL=getDestURL(sc,subId);subscriber_details
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", info.calledNo);
		ResponseObj responseObj=makeHttpRequest(destURL,tempURLForSubDetails);
		if(responseObj!=null){
			if(responseObj.responseStatus && responseObj.response!=null && responseObj.response.length()>0){
				String subDetailsXML=responseObj.response.toString();
				String[] arrXPath=new String[4];
				arrXPath[0] = "/rbt/response";
				arrXPath[1]="/rbt/subscriber/@can_allow";
				arrXPath[2]="/rbt/subscriber/@is_valid_prefix";
				arrXPath[3]="/rbt/subscriber/@user_type";
				XMLXPathParser xpathparser=new XMLXPathParser(subDetailsXML);
				String[] xpathResponse=xpathparser.getXPathResultStringArray(arrXPath);
				if(xpathResponse!=null && xpathResponse.length==4 && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[1].equalsIgnoreCase("y") && xpathResponse[2].equalsIgnoreCase("y") && xpathResponse[3].equalsIgnoreCase("normal")){
					returnFlag=true;
				}
			}
		}
	}
		return returnFlag;
	}
	public USSDSubDetails getUSSDSubDetails(String subId,SiteURLDetails destURL){
		USSDSubDetails subDet=null;
//		SiteURLDetails destURL=getDestURL(sc,subId);
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", info.calledNo);
		ResponseObj responseObj=makeHttpRequest(destURL,tempURLForSubDetails);
		if(responseObj!=null){
			if(responseObj.responseStatus && responseObj.response!=null && responseObj.response.length()>0){
				String subDetailsXML=responseObj.response.toString();
				String[] arrXPath=new String[7];
				arrXPath[0] = "/rbt/response";
				arrXPath[1]="/rbt/subscriber/@is_valid_prefix";
				arrXPath[2]="/rbt/subscriber/@status";
				arrXPath[3]="/rbt/subscriber/@user_type";
				arrXPath[4] = "/rbt/subscriber/@subscription_class";
				arrXPath[5] = "/rbt/subscriber/@language";
				arrXPath[6] = "/rbt/subscriber/@is_prepaid";
				XMLXPathParser xpathparser=new XMLXPathParser(subDetailsXML);
				String[] xpathResponse=xpathparser.getXPathResultStringArray(arrXPath);
				//&& xpathResponse[2].equalsIgnoreCase("new_user")
				boolean isPrepaid=true;
				if(xpathResponse[6]!=null && xpathResponse[6].equalsIgnoreCase("n")){
					isPrepaid=false;
				}
				if(xpathResponse!=null && xpathResponse.length==7  && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[1].equalsIgnoreCase("y")){
					if(xpathResponse[2].equalsIgnoreCase("active")){
						subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],true,isPrepaid,xpathResponse[5],false);
//String subClass,String userType,String status,boolean active,boolean isPrepaid,String language
					}else if(xpathResponse[2].equalsIgnoreCase("deactive")|| xpathResponse[2].equalsIgnoreCase("new_user")){
						subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],false,isPrepaid,xpathResponse[5],true);
					}else{
						subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],false,isPrepaid,xpathResponse[5],false);
					}
				}
			}
		}
	}
		return subDet;
	}
	public boolean isSubscriberActivated(String subId,SiteURLDetails destURL){
		boolean returnFlag=false;
//		SiteURLDetails destURL=getDestURL(sc,subId);
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", info.calledNo);
		ResponseObj responseObj=makeHttpRequest(destURL,tempURLForSubDetails);
		if(responseObj!=null){
			if(responseObj.responseStatus && responseObj.response!=null && responseObj.response.length()>0){
				String subDetailsXML=responseObj.response.toString();
				String[] arrXPath=new String[3];
				arrXPath[0] = "/rbt/response";
				arrXPath[1]="/rbt/subscriber/@is_valid_prefix";
				arrXPath[2]="/rbt/subscriber/@status";
				XMLXPathParser xpathparser=new XMLXPathParser(subDetailsXML);
				String[] xpathResponse=xpathparser.getXPathResultStringArray(arrXPath);
				//&& xpathResponse[2].equalsIgnoreCase("new_user")
				if(xpathResponse!=null && xpathResponse.length==3 && xpathResponse[1].equalsIgnoreCase("y") && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[1].equalsIgnoreCase("active")&& xpathResponse[1].equalsIgnoreCase("act_pending")){
					returnFlag=true;
				}
			}
		}
	}
		return returnFlag;
	}
	public String getSubscriptionPack(String subId,SiteURLDetails destURL){
		String returnFlag=null;
//		SiteURLDetails destURL=getDestURL(sc,subId);
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", info.calledNo);
			ResponseObj responseObj = makeHttpRequest(destURL, tempURLForSubDetails);
			if (responseObj != null) {
				if (responseObj.responseStatus && responseObj.response != null
						&& responseObj.response.length() > 0) {
					String subDetailsXML = responseObj.response.toString();
					String[] arrXPath = new String[3];
					arrXPath[0] = "/rbt/response";
					arrXPath[1]="/rbt/subscriber/@is_valid_prefix";
					arrXPath[2] = "/rbt/subscriber/@subscription_class";
					XMLXPathParser xpathparser = new XMLXPathParser(subDetailsXML);
					String[] xpathResponse = xpathparser.getXPathResultStringArray(arrXPath);
					
					if (xpathResponse != null && xpathResponse.length == 4 && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[1].equalsIgnoreCase("y")) {
						returnFlag = arrXPath[2];
					}
				}
			}
		}		
		return returnFlag;
	}
}
