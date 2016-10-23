package com.onmobile.apps.ringbacktones.ussd.vodafoneRomania;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.servlets.ResponseObj;
import com.onmobile.apps.ringbacktones.servlets.SiteURLDetails;
import com.onmobile.apps.ringbacktones.ussd.ControllerInterface;
import com.onmobile.apps.ringbacktones.ussd.USSDBasicFeatures;
import com.onmobile.apps.ringbacktones.ussd.USSDClips;
import com.onmobile.apps.ringbacktones.ussd.USSDConstants;
import com.onmobile.apps.ringbacktones.ussd.USSDController;
import com.onmobile.apps.ringbacktones.ussd.USSDInfo;
import com.onmobile.apps.ringbacktones.ussd.USSDServletListner;
import com.onmobile.apps.ringbacktones.ussd.USSDSubDetails;
import com.onmobile.apps.ringbacktones.utils.XMLXPathParser;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Gift;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftInbox;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class VodafoneRomaniaUSSDController extends USSDController implements ControllerInterface{

	/*author@Abhinav Anand
	 *
	 * Note:- Changes as compared to USSDController:
	 * 
	 * a)There must not be 'msisdn=$msisdn$' or 'ans=$answer$' in the response dynamic url
	 * b)Before calling 'responseStr=getHTMLResponseBodyString(response,dynamicResponseString,isInputResponse)' make sure
	 * 	 response must be as---vodaRomaniaInfo.seperatorChar+"1.Browse"+vodaRomaniaInfo.seperatorChar+"2.Copy" and so on.
	 * c)Before calling 'getHTMLResponseBodyString(response,dynamicResponseString,isInputResponse)' make sure
	 * 	 dynamicResponseString must be as---"`"+vodaRomaniaInfo.seperatorChar+"1`Browse`"+vodaRomaniaInfo.seperatorChar+"2`Copy" and so on.
	 * d)Once proper 'response' and 'dynamicResponseString' are fetched call 'responseStr=getHTMLResponseBodyString(response,dynamicResponseString,isInputResponse)'
	 * e)'responseStr=getHTMLResponseBodyString()' return body of HTML response to be sent. It has all required <a href links>
	 * f)Now,call 'getHTMLResponse(responseStr)' to get head and body tag of response to be sent back.
	 */
    //Added giftinbox
	private static Logger logger = Logger.getLogger(VodafoneRomaniaUSSDController.class);
	private PropertyReader pr = new PropertyReader();
	private RBTClient rbtClient=null;
	protected static String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForCallDetails="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	//	"Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";
	//"rbt/Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	//"Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	public   USSDBasicFeatures ussdFeatures=null;
	public  RBTDBManager rbtDBManager=null;
	public   VodafoneRomanisUSSDConstants vodaRomaniaInfo=null;
	private static String m_class="VodaRomaniaUSSDController";
		public VodafoneRomaniaUSSDController(){
	      try {
			rbtClient=RBTClient.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
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
		return vodaRomaniaInfo;
	}
	public void setInfo() {
		//		super.setInfo();

		//		vodaRomaniaInfo=new VodafoneRomaniaUSSDConstants(super.info);
		String method="setInfo";
		super.setInfo();

		vodaRomaniaInfo=new VodafoneRomanisUSSDConstants(info);
		logger.info("vodaRomaniaInfo.ussdCatsFreeZone=="+vodaRomaniaInfo.ussdCatsFreeZone);


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
		String ans=(String)parametersMap.get("response");
		String defCatId=(String)parametersMap.get("DEFAULT_CAT_ID");
		Clips defClips=(Clips)parametersMap.get("DEFAULT_CLIP");
		SiteURLDetails destURL=(SiteURLDetails)parametersMap.get("DEST_URL");
		String circleId=(String)parametersMap.get("CIRCLE_ID");
		ArrayList displaySubPack=(ArrayList)parametersMap.get("DISPLAY_SUB_PACK");
		ArrayList subPackTag=(ArrayList)parametersMap.get("SUB_PACK_TAG");
		HashMap chargeClassMsg=((HashMap)parametersMap.get("USSDCHARGECLASS_MESSAGE"));
		
		String responseStr=null;
		logger.info("USSD::processId=="+processId);
		logger.info("USSD::pageNo=="+pageNo);
		logger.info("USSD::request_type=="+request_type);
		logger.info("USSD::request_value=="+request_value);
		if(processId!=null && processId.equalsIgnoreCase("0")){
			boolean isSubAllowed=isSubscriberAllowedForUSSD(subId,destURL);
			if(isSubAllowed){
				USSDSubDetails subDet=null;
				subDet=getUSSDSubDetails(subId,destURL);
				responseStr=processMainMenuRequest(subId,pageNo,circleId,USSDMenu,subDet);
			}
		}else if(request_type.equalsIgnoreCase("SubUnsub")){
			String strIsPrepaid=(String)parametersMap.get("isPrepaid");
			String language=(String)parametersMap.get("lang");
			boolean isPrepaid=true;
			if(strIsPrepaid!=null && strIsPrepaid.equalsIgnoreCase("false")){
				isPrepaid=false;
			}
			responseStr=processSubUnsubRequest(subId,circleId,processId,pageNo,request_value,destURL,isPrepaid,language,USSDMenu);
		}else if(request_type.equalsIgnoreCase("myfavorite")){
			String strIsPrepaid=(String)parametersMap.get("isPrepaid");
			String clipId=(String)parametersMap.get("clipId");
			String catId=(String)parametersMap.get("catId");
			String language=(String)parametersMap.get("lang");
			boolean isPrepaid=true;
			ArrayList  downloadList=null;
			downloadList=getUSSDSubLibraryDetails(subId,destURL);

			if(strIsPrepaid!=null && strIsPrepaid.equalsIgnoreCase("false")){
				isPrepaid=false;
			}
			logger.info("pageNo=="+pageNo);

			responseStr=processMyFavoriteRequest(subId,circleId,processId,pageNo,request_value,destURL, downloadList,isPrepaid,language,USSDMenu, catId, clipId);
			
		}//added for giftinbox
		else if(request_type.equalsIgnoreCase("giftinbox")){
			logger.info("coming to giftinbox ");
			String strIsPrepaid=(String)parametersMap.get("isPrepaid");
			
			String language=(String)parametersMap.get("lang");
			boolean isPrepaid=true;
			ArrayList  downloadList=null;
			if(strIsPrepaid!=null && strIsPrepaid.equalsIgnoreCase("false")){
				isPrepaid=false;
			}
			logger.info("pageNo=="+pageNo);
			ArrayList  songList=null;
			songList=getGiftInbox(subId);
			responseStr=processGiftInboxRequest(subId,circleId,processId,pageNo,request_value,destURL,songList,isPrepaid,language,USSDMenu,parametersMap);
		}
		else if(request_type.equalsIgnoreCase("manage")){
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
		}
		/*else if(request_type.equalsIgnoreCase("copy")){
			String parentProcessId=(String)parametersMap.get("parentProcessId");
			String callerNo=(String)parametersMap.get("callerNo");
			String catId=(String)parametersMap.get("catId");
			String clipId=(String)parametersMap.get("clipId");

			String copyNo=(String)parametersMap.get("copyNo");
			String defClipId=null;
			if(defClips!=null){
				defClipId=""+defClips.id();
			}
			responseStr=processCopyRequest(subId,ans,circleId,processId,request_value,
					callerNo,catId,clipId,parentProcessId,destURL,defClipId,defCatId,copyNo,USSDMenu);
		}*/
		else if(request_type.equalsIgnoreCase("songSearch")){
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
			String startTime=(String)parametersMap.get("startTime");
			String endTime=(String)parametersMap.get("endTime");
			String dayOfTheWeek=(String)parametersMap.get("dayOfTheWeek");
			String futureDate=(String)parametersMap.get("futureDate");

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
					giftAllowed,specialCaller,preListen,USSDMenu,status,startTime,endTime,isDeactive,dayOfTheWeek,futureDate,chargeClassMsg);

		}
		/*	else if(request_type.equalsIgnoreCase("whatshot") || request_type.equalsIgnoreCase("cricket") || request_type.indexOf("help")!=-1){
			responseStr=processMiscellaneousRequest(subId,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);

		}*/
		else if(request_type.equalsIgnoreCase("catSearch") || request_type.equalsIgnoreCase("childCatSearch")){
			responseStr=processCatSearchRequest(subId,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);
		}else if(request_type.equalsIgnoreCase("MSearch")){
			responseStr=processMSearchRequest(subId,ans,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);
		}else if(request_type.equalsIgnoreCase("tariff")){
			responseStr=prcessTariffRequest(subId,ans,circleId,processId,request_value,
					destURL,pageNo,USSDMenu);
		}
		if(responseStr!=null && responseStr.length()>0){
			getHTMLResponse(responseStr);
		}
		//		if(responseStr!=null && responseStr.length()>0 && !responseStr.equalsIgnoreCase("null") || !responseStr.equalsIgnoreCase("")){
		//		responseStr=responseStr+vodaRomaniaInfo.newLineCharString;
		//		}
		responseStr=Tools.findNReplace(responseStr, "", "");
		return responseStr;

	}
	public String processMainMenuRequest(String subId,String pageNo, String circleId,
			HashMap USSDMenu,USSDSubDetails subDet){
		String method="processMainMenuRequest";
		logger.info("entering");
		String response=null;
		if(pageNo!=null && pageNo.length()>0){
			logger.info("pageNo!=null && pageNo.length()>0");
			try {
				int pageValue=Integer.parseInt(pageNo);
				logger.info("pageValue=="+pageValue);
				USSDInfo ussdInfo=getUSSDInfo("0",circleId,USSDMenu);
				if(ussdInfo!=null){
					logger.info("ussdInfo!=null");
					String currURL=ussdInfo.URL;
					logger.info("currURL=="+currURL);
					if(currURL!=null){
						logger.info("currURL!=null");
						String dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
						if(pageValue>0){
							dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo="+(pageValue-1);
						}
						String responseString=pr.getPropertyValue("msg.callwait")+vodaRomaniaInfo.newLineCharString +ussdInfo.responseString;
						if(subDet!=null){
							logger.info("subDet == "+subDet.toString());
							if(subDet.active){
								logger.info("subDet.active == true");
								responseString=Tools.findNReplace(responseString, "Activate/Deactivate Services", pr.getPropertyValue("service.deactivate"));
								dynamicResponseString=Tools.findNReplace(dynamicResponseString, "request_value=monthlySub", "request_value=unsubscribe");
							}else if(subDet.deactive){
								logger.info("subDet.deactive == true");
								responseString=Tools.findNReplace(responseString, "Activate/Deactivate Services", pr.getPropertyValue("service.activate"));
								if(!subDet.isPrepaid){
									dynamicResponseString=Tools.findNReplace(dynamicResponseString, "request_value=monthlySub", "request_value=monthlySub&isPrepaid=false");
								}else{
									dynamicResponseString=Tools.findNReplace(dynamicResponseString, "request_value=monthlySub", "request_value=monthlySub&isPrepaid=true");
								}
							}else{
								logger.info("subDet.deactive == false && subDet.deactive== false");
								//								responseString=responseString.substring(0, responseString.indexOf("Activate/Deactivate Services")-2-vodaRomaniaInfo.newLineCharString.length()-vodaRomaniaInfo.seperatorChar.length())+responseString.substring(responseString.indexOf("Activate/Deactivate Services"));
								//								responseString=Tools.findNReplace(responseString, "Activate/Deactivate Services", "");
								responseString=decreaseExpectedOutputResposeKeyValueByOne(responseString,5,vodaRomaniaInfo.seperatorChar,vodaRomaniaInfo.newLineCharString);
								dynamicResponseString=decreaseExpectedInputResposeKeyValueByOne(ussdInfo.dynamicURLResponseString,5,vodaRomaniaInfo.seperatorChar);
							}
						}else{
							//							responseString=responseString.substring(0, responseString.indexOf("Activate/Deactivate Services")-2-vodaRomaniaInfo.newLineCharString.length()-vodaRomaniaInfo.seperatorChar.length())+responseString.substring(responseString.indexOf("Activate/Deactivate Services"));
							//							responseString=Tools.findNReplace(responseString, "Activate/Deactivate Services", "");
							responseString=decreaseExpectedOutputResposeKeyValueByOne(responseString,5,vodaRomaniaInfo.seperatorChar,vodaRomaniaInfo.newLineCharString);
							dynamicResponseString=decreaseExpectedInputResposeKeyValueByOne(ussdInfo.dynamicURLResponseString,5,vodaRomaniaInfo.seperatorChar);
						}
						logger.info("responseString=="+responseString);
						logger.info("dynamicResponseString=="+dynamicResponseString);
						if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
							response=getValidResponseString(responseString,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
							logger.info("response=="+response);
							response=getHTMLResponseBodyString(response,dynamicResponseString,false);
							logger.info("response=="+response);
						}
					}
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
				return response;

			}
		}
		return response;
	}
	public String processCopyRequest(String subId,String ans,String circleId,String processId,
			String request_value,String pageNo,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String defClipId,String defCatId,String copyNo,HashMap USSDMenu){
		return super.processCopyRequest(subId, ans, circleId, processId, request_value, pageNo,callerNo, catId,
				clipId, parentProcessId, destURL, defClipId, defCatId, copyNo,USSDMenu);
	}
	public  String processMyFavoriteRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,USSDSubDetails subDet,
			boolean isPrepaid,String language,HashMap USSDMenu){
		String method="processManageRequest";
		String response=null;
		if(request_value.indexOf("unsubscribe")!=-1){
			response=processUnsubRequest(subId,circleId, processId, request_value,destURL, pageNo, USSDMenu);
		}else if(request_value.indexOf("monthlySub")!=-1){
			response=processDefaultSubRequest(subId,circleId, processId, request_value,destURL, pageNo, USSDMenu,isPrepaid,language);
		}
		return response;
	}
	public  String processSubUnsubRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,
			boolean isPrepaid,String language,HashMap USSDMenu){
		String method="processManageRequest";
		String response=null;
		if(request_value.indexOf("unsubscribe")!=-1){
			response=processUnsubRequest(subId,circleId, processId, request_value,destURL, pageNo, USSDMenu);
		}else if(request_value.indexOf("monthlySub")!=-1){
			response=processDefaultSubRequest(subId,circleId, processId, request_value,destURL, pageNo, USSDMenu,isPrepaid,language);
		}
		return response;
	}
	public  String processGiftInboxRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,ArrayList  songList,
			boolean prepaid,String language,HashMap USSDMenu,HashMap parametersMap){
		
		String method="processGiftInboxRequest";
		String response=null;
		long dateVal=0;
		
		String subID=subId;
		String gifterID=(String) (parametersMap.get("gifterID")!=null?parametersMap.get("gifterID"):null);
		logger.info(" GifterID: "+gifterID);
		int toneID=0;
		try{
			toneID=Integer.parseInt((String)parametersMap.get("toneID"));
		}catch (Exception e) {
			// TODO: handle exception
		}
		 
		String toneType=(String) (parametersMap.get("toneType")!=null?parametersMap.get("toneType"):null);
		String catID=(String) (parametersMap.get("catID")!=null?parametersMap.get("catID"):null);
		Date giftSentTime=null;
		if((parametersMap.get("giftSentTime"))!=null){
			giftSentTime=new Date(new Long((String)(parametersMap.get("giftSentTime"))).longValue());
			if(giftSentTime!=null)
			      dateVal=giftSentTime.getTime();
		}
		String toneName=(String) (parametersMap.get("toneName")!=null?parametersMap.get("toneName"):null);
		String dynamicResponseString=null;
		int pageValue=0;
		if(pageNo!=null){
			pageNo=pageNo.trim();
			try {
				pageValue = Integer.parseInt(pageNo);
			} catch(NumberFormatException e) {
				pageValue=0;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("pageNo=="+pageNo);
		logger.info("pageValue=="+pageValue);
		USSDInfo ussdinfo=getUSSDInfo(processId, circleId, USSDMenu);
		if(request_value==null || request_value.equalsIgnoreCase("")){
			logger.info("request_value==null || request_value.equalsIgnoreCase(\"\")");
			String responseString=null;
			if(songList!=null&&songList.size()!=0){
				if(ussdinfo!=null){
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL;
					for(int count=0;count<songList.size();count++){
						Gift gift=(Gift)songList.get(count);
						if(gift!=null){	
							toneID=gift.getToneID();
							toneType=gift.getToneType();
							catID=gift.getCategoryID()+"";
							toneName=gift.getToneName();
							if(toneName!=null && toneName.indexOf(".")!=-1){
								StringTokenizer st=new StringTokenizer(toneName,".");
								String tempToneName="";
								while(st.hasMoreElements()){
									String temp=st.nextToken();
									tempToneName=tempToneName+temp;
								}
								toneName=tempToneName;
							}
							gifterID=gift.getSender();
							logger.info("sender name "+gifterID);
							giftSentTime=gift.getSentTime();
						    dateVal=0;
							if(giftSentTime!=null)
							      dateVal=giftSentTime.getTime();
							
							if(response==null){
								response=vodaRomaniaInfo.seperatorChar+(count+1)+"."+toneName+vodaRomaniaInfo.newLineCharString;
							}else{
								response=response+vodaRomaniaInfo.seperatorChar+(count+1)+"."+toneName+vodaRomaniaInfo.newLineCharString;
							}
							dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(count+1)+"`"+ussdinfo.URL+"&request_type=giftinbox&request_value=giftinboxoption&catID="+catID+"&toneID="+toneID+"&prepaid="+prepaid+"&language="+language+"&gifterID="+gifterID+"&toneType="+toneType+"&giftSentTime="+dateVal+vodaRomaniaInfo.endURLChar;
						}
					}
					int tempPageValue=pageValue;
					if(pageValue!=0){
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue-1)+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar;
						logger.info("tempPageValue=="+tempPageValue);
						logger.info("dynamicResponseString=="+dynamicResponseString);
					}else{
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+"0"+"&pageNo=0"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar;
						logger.info("tempPageValue=="+tempPageValue);
						logger.info("dynamicResponseString=="+dynamicResponseString);
					}
					if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
						//dynamicResponseString="`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
						response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
						response=getHTMLResponseBodyString( response,dynamicResponseString, true);
					}
				}
			}else{
			response=pr.getPropertyValue("giftinbox.nogift.msg");//"No gift song";
				dynamicResponseString="`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
				if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,false);
					response=getHTMLResponseBodyString( response,dynamicResponseString, false);
				}
			}
			
		}else{
			if(request_value.equalsIgnoreCase("giftinboxoption") && songList!=null && songList.size()>0){
				
				
			String s[]={pr.getPropertyValue("giftinbox.option.add2fav"),pr.getPropertyValue("giftinbox.option.accept"),pr.getPropertyValue("giftinbox.option.refuse")};
				for(int a=0;a<s.length;a++){
					if(response==null){
						response=vodaRomaniaInfo.seperatorChar+(a+1)+"."+s[a]+vodaRomaniaInfo.newLineCharString;
					}else{
						response=response+vodaRomaniaInfo.seperatorChar+(a+1)+"."+s[a]+vodaRomaniaInfo.newLineCharString;
					}
					if(a==0)
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(a+1)+"`"+ussdinfo.URL+"&request_type=giftinbox&request_value=giftinboxoption1&catID="+catID+"&toneID="+toneID+"&prepaid="+prepaid+"&language="+language+"&gifterID="+gifterID+"&toneType="+toneType+"&giftSentTime="+dateVal+vodaRomaniaInfo.endURLChar;
					else if(a==1)
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(a+1)+"`"+ussdinfo.URL+"&request_type=giftinbox&request_value=giftinboxoption2&catID="+catID+"&toneID="+toneID+"&prepaid="+prepaid+"&language="+language+"&gifterID="+gifterID+"&toneType="+toneType+"&giftSentTime="+dateVal+vodaRomaniaInfo.endURLChar;
					else if(a==2)
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(a+1)+"`"+ussdinfo.URL+"&request_type=giftinbox&request_value=giftinboxoption3&catID="+catID+"&toneID="+toneID+"&prepaid="+prepaid+"&language="+language+"&gifterID="+gifterID+"&toneType="+toneType+"&giftSentTime="+dateVal+vodaRomaniaInfo.endURLChar;
				}
				int tempPageValue=pageValue;
				if(pageValue!=0){
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue-1)+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=giftinbox&request_value=giftinboxoption"+vodaRomaniaInfo.endURLChar;
					logger.info("tempPageValue=="+tempPageValue);
					logger.info("dynamicResponseString=="+dynamicResponseString);
				}else{
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo=0&request_type=giftinbox"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=giftinbox&request_value=giftinboxoption"+vodaRomaniaInfo.endURLChar;
					logger.info("tempPageValue=="+tempPageValue);
					logger.info("dynamicResponseString=="+dynamicResponseString);
				}
				if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,false);
					response=getHTMLResponseBodyString( response,dynamicResponseString, true);
				}
			}else if(request_value.equalsIgnoreCase("giftinboxoption1")&& songList!=null && songList.size()>0){
				response=addFavorites(subID, gifterID, toneID, toneType, giftSentTime, catID, language, prepaid);
				if(response.equalsIgnoreCase("success"))
					response=pr.getPropertyValue("giftinbox.option.add2fav.sucess.msg");
				else
				response=pr.getPropertyValue("giftinbox.option.add2fav.failure.msg");
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo=0"+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar;
				if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,false);
					response=getHTMLResponseBodyString( response,dynamicResponseString, false);
				}
			}else if(request_value.equalsIgnoreCase("giftinboxoption2")&& songList!=null && songList.size()>0){
				response=acceptGift(subID, gifterID, toneID, toneType, giftSentTime, catID, language, prepaid);
				if(response.equalsIgnoreCase("success"))
					response=pr.getPropertyValue("giftinbox.option.accept.sucess.msg");
				else
					response=pr.getPropertyValue("giftinbox.option.accept.failure.msg");
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo=0"+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar;
				if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,false);
					response=getHTMLResponseBodyString( response,dynamicResponseString, false);
				}
			}else if(request_value.equalsIgnoreCase("giftinboxoption3") && songList!=null && songList.size()>0){
				response=rejectGift(subID, gifterID, toneID, toneType, giftSentTime, catID, language, prepaid);
				if(response.equalsIgnoreCase("success"))
				response=pr.getPropertyValue("giftinbox.option.refuse.sucess.msg");
				else
				response=pr.getPropertyValue("giftinbox.option.refuse.failure.msg");
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo=0"+"&request_type=giftinbox"+vodaRomaniaInfo.endURLChar;
				if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
					response=getHTMLResponseBodyString( response,dynamicResponseString, false);
				}
			}
		}
		
		return response;
	}
	public  String processMyFavoriteRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,ArrayList  downloadList,
			boolean isPrepaid,String language,HashMap USSDMenu,String catId,String clipId){
		String method="processMyFavoriteRequest";
		String response=null;
		int pageValue=0;
		if(pageNo!=null){
			pageNo=pageNo.trim();
			try {
				pageValue = Integer.parseInt(pageNo);
			} catch(NumberFormatException e) {
				pageValue=0;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("pageNo=="+pageNo);
		logger.info("pageValue=="+pageValue);
		if(request_value==null || request_value.equalsIgnoreCase("")){
			logger.info("request_value==null || request_value.equalsIgnoreCase(\"\")");
			String responseString=null;
			String dynamicResponseString=null;
			if(downloadList!=null && downloadList.size()>0){
				logger.info("downloadList!=null && downloadList.size()>0");
				try {
					USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
					if(ussdInfo!=null){
						String currURL=ussdInfo.URL;
						if(currURL!=null){
							responseString=ussdInfo.responseString;
							dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
							if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
								response=getValidResponseString(responseString,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
								response=getHTMLResponseBodyString(response,dynamicResponseString,false);
							}
						}
					}
				} catch (NumberFormatException e) {
					e.printStackTrace();
					return response;
				}
			}else{
				dynamicResponseString="`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
			response=pr.getPropertyValue("msg.emptyfavoritesection1");
				response=getHTMLResponseBodyString(response,dynamicResponseString,false);
			}
		}else{
			if(request_value.indexOf("viewfavorite")!=-1 && downloadList!=null && downloadList.size()>0){
				response=processViewFavorite(subId, circleId, processId, pageNo,
						request_value, destURL,  downloadList,
						isPrepaid, language, USSDMenu);
			}else if(request_value.indexOf("addfavorite")!=-1 && downloadList!=null && downloadList.size()>0){
				response=processAddFavorite(subId, circleId, processId, pageNo,
						request_value, destURL,  downloadList,
						isPrepaid, language, USSDMenu, catId, clipId);
			}else{
				String dynamicResponseString="`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
			response=pr.getPropertyValue("msg.emptyfavoritesection2");
				response=getHTMLResponseBodyString(response,dynamicResponseString,false);
				logger.info("request_value=="+request_value);
				if(downloadList==null || !(downloadList.size()>0)){
					logger.info("downloadList==null || !(downloadList.size()>0)");
				}
			}
		}
		return response;
	}
	public String processViewFavorite(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,ArrayList  downloadList,
			boolean isPrepaid,String language,HashMap USSDMenu){
		String method="processViewFavorite";
		String response=null;
		int pageValue=0;
		if(pageNo!=null){
			pageNo=pageNo.trim();
			try {
				pageValue = Integer.parseInt(pageNo);
			} catch(NumberFormatException e) {
				pageValue=0;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("pageNo=="+pageNo);
		logger.info("pageValue=="+pageValue);
		USSDInfo ussdinfo=getUSSDInfo(processId, circleId, USSDMenu);
		if(ussdinfo!=null){
			String dynamicResponseString=vodaRomaniaInfo.mainMenuURL;
			for(int count=0;count<downloadList.size();count++){
				USSDClips clip=(USSDClips)downloadList.get(count);
				if(clip!=null){
					String clipName=clip.name;
					String type=clip.type;
					if(response==null){
						response=vodaRomaniaInfo.seperatorChar+(count+1)+"."+replaceDotBySpace(clipName)+vodaRomaniaInfo.newLineCharString;
					}else{
						response=response+vodaRomaniaInfo.seperatorChar+(count+1)+"."+replaceDotBySpace(clipName)+vodaRomaniaInfo.newLineCharString;
					}
					//					if(type.equalsIgnoreCase("shuffle")){
					//					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(count+1)+"`"+ussdInfo.URL+"&request_value&clipId="+clip.id()+"&isPrepaid="+isPrepaid+"&request_value=timeOfTheDayOption";
					//					}else{

					//					}
				}
			}
			int tempPageValue=pageValue;
			if(pageValue!=0){
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue-1)+"&request_type=myfavorite&request_value=viewfavorite"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=myfavorite&request_value=viewfavorite"+vodaRomaniaInfo.endURLChar;
				logger.info("tempPageValue=="+tempPageValue);
				logger.info("dynamicResponseString=="+dynamicResponseString);
			}else{
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo=0&request_type=myfavorite"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdinfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=myfavorite&request_value=viewfavorite"+vodaRomaniaInfo.endURLChar;
				logger.info("tempPageValue=="+tempPageValue);
				logger.info("dynamicResponseString=="+dynamicResponseString);
			}
			if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
				response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
				response=conevrtNumberedInputToDefaultInput(response,vodaRomaniaInfo.seperatorChar);
				response=getHTMLResponseBodyString( response,dynamicResponseString, false);
			}
		}
		return response;
	}
	public String processAddFavorite(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,ArrayList  downloadList,
			boolean isPrepaid,String language,HashMap USSDMenu,String catId,String clipId){
		String method="processAddFavorite";
		String response=null;
		int pageValue=0;
		if(pageNo!=null){
			pageNo=pageNo.trim();

			try {
				pageValue = Integer.parseInt(pageNo);
			} catch(NumberFormatException e) {
				pageValue=0;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		logger.info("pageNo=="+pageNo);
		logger.info("pageValue=="+pageValue);
		USSDInfo ussdInfo=getUSSDInfo(processId, circleId, USSDMenu);
		if(ussdInfo!=null){
			String dynamicResponseString=vodaRomaniaInfo.mainMenuURL;
			if(request_value.equalsIgnoreCase("addfavorite")){

				for(int count=0;count<downloadList.size() && count<vodaRomaniaInfo.maxClipsNo;count++){
					USSDClips clip=(USSDClips)downloadList.get(count);
					if(clip!=null){
						String clipName=clip.name;
						String type=clip.type;
						if(response==null){
							response=vodaRomaniaInfo.seperatorChar+(count+1)+"."+replaceDotBySpace(clipName);
						}else{
							response=response+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.seperatorChar+(count+1)+"."+replaceDotBySpace(clipName);
						}
						if(type.equalsIgnoreCase("shuffle") && clip.catId!=null){
							dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(count+1)+"`"+ussdInfo.URL+"&request_type=myfavorite&request_value=addfavoriteconfirm&catId="+clip.catId+"&isPrepaid="+isPrepaid+"&language="+language+vodaRomaniaInfo.endURLChar;
						}else if(type.equalsIgnoreCase("clip") && clip.catId!=null && clip.clipId!=null){
							dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(count+1)+"`"+ussdInfo.URL+"&request_type=myfavorite&request_value=addfavoriteconfirm&catId="+clip.catId+"&clipId="+clip.clipId+"&isPrepaid="+isPrepaid+"&language="+language+vodaRomaniaInfo.endURLChar;
						}
					}
				}
				int tempPageValue=pageValue;
				if(pageValue!=0){
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdInfo.processId+"&pageNo="+(tempPageValue-1)+"&request_type=myfavorite&request_value=addfavorite"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdInfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=myfavorite&request_value=addfavorite"+vodaRomaniaInfo.endURLChar;
					logger.info("tempPageValue=="+tempPageValue);
					logger.info("dynamicResponseString=="+dynamicResponseString);
				}else{
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdInfo.processId+"&pageNo=0&request_type=myfavorite"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdInfo.processId+"&pageNo="+(tempPageValue+1)+"&request_type=myfavorite&request_value=addfavorite"+vodaRomaniaInfo.endURLChar;
					logger.info("tempPageValue=="+tempPageValue);
					logger.info("dynamicResponseString=="+dynamicResponseString);
				}
				if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
					response=getHTMLResponseBodyString( response,dynamicResponseString, false);
				}
			}else if(request_value.equalsIgnoreCase("addfavoriteconfirm")){
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdInfo.processId+"&pageNo=0&request_type=myfavorite&request_value=addfavorite"+vodaRomaniaInfo.endURLChar;
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String urlForSelectionInLoop = vodaRomaniaInfo.urlForSelection;
				urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%subId%", subId);
				urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%ussdNo%", vodaRomaniaInfo.calledNo);
				urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%isPrepaid%", strIsPrepaid);
				urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%lan%", language);
				urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%catId%", catId);
				urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%callerId%", "all");
				if(clipId!=null){
					urlForSelectionInLoop=Tools.findNReplaceAll(urlForSelectionInLoop, "%clipId%", clipId);
				}
				ResponseObj responseobj=makeHttpRequest(destURL, urlForSelectionInLoop);
				response=getBackEndResponse(responseobj,vodaRomaniaInfo.favoriteSelSuccessmsg);
				response=getHTMLResponseBodyString(response,vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1'"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar,false);
			}
		}
		return response;
	}

	public  String processManageRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,USSDSubDetails subDet,
			boolean isPrepaid,String language,ArrayList displaySubPack,
			ArrayList subPackTag,
			HashMap USSDMenu){

		String method="processManageRequest";
		String response=null;
		if(request_value==null || request_value.equalsIgnoreCase("")){
			logger.info("request_value==null || request_value.equalsIgnoreCase(\"\")");
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			String responseString=null;
			String dynamicResponseString=null;
			if(vodaRomaniaInfo.unsubscriptionAllowed || vodaRomaniaInfo.defaultSubscritpionAllowed || vodaRomaniaInfo.advanceSubscriptionAllowed){
				logger.info("vodaRomaniaInfo.unsubscriptionAllowed=="+vodaRomaniaInfo.unsubscriptionAllowed);
				logger.info("vodaRomaniaInfo.defaultSubscritpionAllowed=="+vodaRomaniaInfo.defaultSubscritpionAllowed);
				logger.info("vodaRomaniaInfo.advanceSubscriptionAllowed=="+vodaRomaniaInfo.advanceSubscriptionAllowed);
				int count=1;
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
				if(ussdInfo!=null){
					logger.info("ussdinfo!=null");
					logger.info("responseString=="+responseString);
				responseString=pr.getPropertyValue("msg.selectoption");

					if(vodaRomaniaInfo.defaultSubscritpionAllowed && subDet!=null && subDet.deactive){
						logger.info("vodaRomaniaInfo.defaultSubscritpionAllowed && subDet!=null && subDet.deactive");
						responseString=responseString+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.seperatorChar+count+".Activeaza serviciul";
						logger.info("responseString=="+responseString);
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+count+"`"+ussdInfo.URL+"&request_value=monthlySub&lang="+language+"&isPrepaid="+isPrepaid+vodaRomaniaInfo.endURLChar;
						count++;
					}
					if(vodaRomaniaInfo.unsubscriptionAllowed && subDet!=null && subDet.active){
						logger.info("vodaRomaniaInfo.unsubscriptionAllowed && subDet!=null && subDet.active");
					responseString=responseString+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.seperatorChar+count+"."+pr.getPropertyValue("service.deactivate");
						logger.info("responseString=="+responseString);
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+count+"`"+ussdInfo.URL+"&request_value=unsubscribe"+vodaRomaniaInfo.endURLChar;
						count++;
					}
					if(vodaRomaniaInfo.advanceSubscriptionAllowed && subDet!=null && subDet.advanceSubscriptionAllowed && displaySubPack!=null && subPackTag!=null){
						logger.info("vodaRomaniaInfo.advanceSubscriptionAllowed && subDet!=null && subDet.advanceSubscriptionAllowed && displaySubPack!=null && subPackTag!=null");
						if(subPackTag!=null && displaySubPack!=null){
							for(int counter=0;counter<displaySubPack.size();counter++,count++){
								if(counter==0){
									count--;
									continue;
								}
								if(responseString==null){
									responseString=vodaRomaniaInfo.seperatorChar+count+"."+(String)displaySubPack.get(counter);
								}else{
									responseString=responseString+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.seperatorChar+count+"."+(String)displaySubPack.get(counter);
								}
								logger.info("counter=="+counter);
								logger.info("responseString=="+responseString);
								dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+count+"`"+ussdInfo.URL+"&request_value="+(String)subPackTag.get(counter)+"&lang="+language+"&isPrepaid="+isPrepaid+vodaRomaniaInfo.endURLChar;
							}
						}
					}
				}
			}
			int pageValue=0;
			try {
				pageValue = Integer.parseInt(pageNo);
			} catch (NumberFormatException e) {
				pageValue=0;
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(responseString.equalsIgnoreCase(pr.getPropertyValue("msg.selectoption"))){
			response=pr.getPropertyValue("msg.invalidrequest")+dynamicResponseString;
			}else{
				String dynamicResponseStringWithMoreOption=getDynamicURLWithMoreOption(ussdInfo,pageValue);
				if(dynamicResponseStringWithMoreOption!=null){
					dynamicResponseString=dynamicResponseString+dynamicResponseStringWithMoreOption;
				}
				if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
					response=getValidResponseString(responseString,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
					response=getHTMLResponseBodyString(response,dynamicResponseString,false);
					//					response=response+dynamicResponseString;
				}
			}
		}else{
			if(request_value.indexOf("unsubscribe")!=-1){
				response=processUnsubRequest(subId,circleId, processId, request_value,destURL, pageNo, USSDMenu);
			}else if(request_value.indexOf("monthlySub")!=-1){
				response=processDefaultSubRequest(subId,circleId, processId, request_value,destURL, pageNo, USSDMenu,isPrepaid,language);
			}else{
				if(subPackTag!=null){
					for(int counter=0;counter<subPackTag.size();counter++){
						String temppack=(String)subPackTag.get(counter);
						if(request_value.indexOf(temppack)!=-1){
							response=processAdvanceSubRequest(subId, circleId, processId,
									request_value, destURL, pageNo, USSDMenu, isPrepaid, language,
									subPackTag);
							break;
						}
					}
				}
			}
		}
		return response;
	}
	public String processAdvanceSubRequest(String subId,String circleId,String processId,
			String request_value,SiteURLDetails destURL,
			String pageNo,HashMap USSDMenu,boolean isPrepaid,String language,
			ArrayList subPackTag){
		//subClass.subscriptionAmount();
		String method="processAdvanceSubRequest";
		logger.info("entering");
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null){
			logger.info("ussdInfo!=null");
			for(int counter=0;counter<subPackTag.size();counter++){
				logger.info("counter=="+counter);
				String temppack=(String)subPackTag.get(counter);
				logger.info("temppack=="+temppack);
				if(request_value!=null && request_value.equalsIgnoreCase(temppack)){
					logger.info("request_value!=null && request_value.equalsIgnoreCase(temppack)");
					SubscriptionClass subClass=CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(temppack);
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
					if(subClass!=null && subClass.getSubscriptionAmount()!=null && subClass.getSubscriptionPeriod()!=null){
						logger.info("subClass =="+subClass.getSubscriptionPeriod());
						try {
							String tempMsg=vodaRomaniaInfo.confirmSubMsg;
							logger.info("tempMsg=="+tempMsg);
							String subAmount=subClass.getSubscriptionAmount();
							tempMsg=Tools.findNReplace(tempMsg, "%amount%", subAmount);
							logger.info("tempMsg=="+tempMsg);
							String subPeriod=USSDServletListner.getDisplayForSubPeriodInMonth(subClass.getSubscriptionPeriod());
							tempMsg=Tools.findNReplace(tempMsg, "%period%", subPeriod);
							logger.info("tempMsg=="+tempMsg);
							dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL+"&request_value="+temppack+"Reconfirmation"+vodaRomaniaInfo.endURLChar;
							response=getHTMLResponseBodyString(tempMsg,dynamicResponseString,false);
							break;
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else if(request_value!=null && request_value.equalsIgnoreCase(temppack+"Reconfirmation")){
					String strIsPrepaid="y";
					if(!isPrepaid){
						strIsPrepaid="n";
					}
					String advanceSubURL = vodaRomaniaInfo.urlForAdvancePackActivation;
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%subId%", subId);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%isPrepaid%", strIsPrepaid);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%lan%", language);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%advancePack%", temppack);
					ResponseObj responseobj=makeHttpRequest(destURL, advanceSubURL);
					response=getBackEndResponse(responseobj,null);	
					response=getHTMLResponseBodyString(response,vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1'"+ussdInfo.URL+vodaRomaniaInfo.endURLChar,false);

				}
			}
		}
		return response;
	}
	public String processUnsubRequest(String subId,String circleId,String processId,
			String request_value,SiteURLDetails destURL,
			String pageNo,HashMap USSDMenu){
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("unsubscribe")){

				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
				try {
					response=vodaRomaniaInfo.confirmUnsubcriptionMsg;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+Tools.findNReplace(ussdInfo.URL, "request_value=monthlySub", "request_value=unsubscribeReconfirmation")+"`"+vodaRomaniaInfo.seperatorChar+"2`"+Tools.findNReplace(ussdInfo.URL, "request_value=monthlySub", "request_value=unsubscribeReconfirmationReject");
					response=getHTMLResponseBodyString(response,dynamicResponseString,false);
				}catch(NumberFormatException e) {
					e.printStackTrace();
				}	
			}else if(request_value!=null && request_value.equalsIgnoreCase("unsubscribeReconfirmation")){
				String unsubURL = vodaRomaniaInfo.urlForDeactivation;
				unsubURL=Tools.findNReplaceAll(unsubURL, "%subId%", subId);
				unsubURL=Tools.findNReplaceAll(unsubURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
				ResponseObj responseobj=makeHttpRequest(destURL, unsubURL);
				response=getBackEndResponse(responseobj,vodaRomaniaInfo.unSubSuccessMsg);
				response=getHTMLResponseBodyString(response,vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1'"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar,false);
			}else if(request_value!=null && request_value.equalsIgnoreCase("unsubscribeReconfirmationReject")){
			response=pr.getPropertyValue("msg.thanks");
			}
		}
		return response;
	}
	public String processDefaultSubRequest(String subId,String circleId,String processId,
			String request_value,SiteURLDetails destURL,
			String pageNo,HashMap USSDMenu,boolean isPrepaid,String language){
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("monthlySub")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
				try {
					String tempMsg=vodaRomaniaInfo.confirmSubMsg;
					//%amount%monthlySubReconfirmation
					tempMsg=Tools.findNReplace(tempMsg, "%amount%", vodaRomaniaInfo.defaultSubCost);
					tempMsg=Tools.findNReplace(tempMsg, "%period%", "1 month");
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+Tools.findNReplace(ussdInfo.URL, "request_value=monthlySub", "request_value=monthlySubReconfirmation")+"&isPrepaid="+isPrepaid+"&language="+language+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"2`"+Tools.findNReplace(ussdInfo.URL, "request_value=monthlySub", "request_value=monthlySubReconfirmationReject")+"&isPrepaid="+isPrepaid+"&language="+language+vodaRomaniaInfo.endURLChar;
					response=getHTMLResponseBodyString(tempMsg,dynamicResponseString,false);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}else if(request_value!=null && request_value.equalsIgnoreCase("monthlySubReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String monthlySubURL = vodaRomaniaInfo.urlForMonthlyActivation;
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%subId%", subId);
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%isPrepaid%", strIsPrepaid);
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%lan%", language);
				ResponseObj responseobj=makeHttpRequest(destURL, monthlySubURL);
				if(isPrepaid){
					response=getBackEndResponse(responseobj,vodaRomaniaInfo.subSuccessPrepaidMsg);
				}else{
					response=getBackEndResponse(responseobj,vodaRomaniaInfo.subSuccessPostpaidMsg);
				}
				response=getHTMLResponseBodyString(response,vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1'"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar,false);
			}else if(request_value!=null && request_value.equalsIgnoreCase("monthlySubReconfirmationReject")){

			response=pr.getPropertyValue("msg.thanks");
			}
		}
		return response;
	}
	public String processSongSearchRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,String pageNo,String giftAllowed,
			String specialCaller,String preListen,HashMap USSDMenu,String status,String startTime,String endTime,
			String isDeactive,String dayOfTheWeek,String futureDate,HashMap chargeClassMsg){
		//response=getHTMLResponseBodyString(response,dynamicResponseString,false)
		//"+vodaRomaniaInfo.seperatorChar+"
		String method="processSongSearchRequest";
		String response=null;
		String dynamicResponseString=null;
		int pageValue=0;
		try {
			pageValue = Integer.parseInt(pageNo);
		} catch (NumberFormatException e) {
			pageValue=0;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(request_value==null){
			if(clipId==null || clipId.equalsIgnoreCase("")){
				USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
				if(ussdInfo!=null && ussdInfo.catId!=null){
					if(status!=null && status.equalsIgnoreCase("free")){
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
					}else{
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId="+ussdInfo.parentProcessId+"&pageNo=0&request_type=childCatSearch"+vodaRomaniaInfo.endURLChar;
					}
					//check for parentprocessId
					String tempCatid=ussdInfo.catId;
					//Clips[] clipsArr=rbtDBManager.getClipsInCategory(tempCatid);
					int catIdInt=-1;
					try {
						catIdInt=Integer.parseInt(tempCatid);
					} catch (NumberFormatException e) {
						catIdInt=-1;
						e.printStackTrace();
					}
					Clips[] clipsArr=rbtDBManager.getAllClips(catIdInt);
					if(clipsArr!=null){
						if(ussdInfo.processName!=null){
							response=ussdInfo.processName+vodaRomaniaInfo.newLineCharString;
						}
						for(int count=0;count<clipsArr.length && count<vodaRomaniaInfo.maxClipsNo;count++){
							Clips clip=clipsArr[count];
							if(clip!=null){
								String clipName=clip.name();
								if(response==null){
									response=vodaRomaniaInfo.seperatorChar+(count+1)+"."+replaceDotBySpace(clipName);
								}else{
									response=response+vodaRomaniaInfo.seperatorChar+(count+1)+"."+replaceDotBySpace(clipName);
								}
								dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+(count+1)+"`"+ussdInfo.URL+"&clipId="+clip.id()+"&isPrepaid="+isPrepaid+"&request_value=listRBTOptions&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
							}
						}

						String dynamicResponseStringWithMoreOption=getDynamicURLWithMoreOption(ussdInfo,pageValue);
						if(dynamicResponseStringWithMoreOption!=null){
							dynamicResponseString=dynamicResponseString+dynamicResponseStringWithMoreOption;
						}
						if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
							response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
							response=getHTMLResponseBodyString( response, dynamicResponseString, false);
						}
					}
				}
			}
		}else if(request_value.indexOf("listRBTOptions")!=-1){
			response=processListRBTOptionsRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,isDeactive);
		}else if(request_value.indexOf("timeOfTheDay")!=-1){
			//timeOfTheDayOption
			response=prcessTimeOfTheDayRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,isDeactive);
		}else if(request_value.indexOf("futureDate")!=-1){
			//timeOfTheDayOption
			response=prcessFutureDateRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,isDeactive);
		}else if(request_value.indexOf("dayOfTheWeek")!=-1){
			//timeOfTheDayOption
			response=prcessDayOfTheWeekRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,isDeactive);
		}else if(request_value.indexOf("listOptions")!=-1){
			response=prcessListOptionsRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,startTime,endTime,isDeactive,dayOfTheWeek,futureDate);
		}else if(request_value.indexOf("preListen")!=-1){
			response=processPrelistenRequest(subId,circleId,processId,request_value,clipId,isPrepaid,preListen,USSDMenu);
		}else if(request_value.indexOf("defaultSel")!=-1){
			response=processDefaultSelRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status,startTime,endTime,isDeactive,dayOfTheWeek,futureDate,chargeClassMsg);
		}else if(request_value.indexOf("specialCallerSel")!=-1){
			response=processSpecialCallerSelRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen, USSDMenu,status,startTime,endTime,isDeactive,dayOfTheWeek,futureDate,chargeClassMsg);
		}else if(request_value.indexOf("gift")!=-1){
			response=processGiftRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,isDeactive,USSDMenu,chargeClassMsg);
		}
		if(response!=null){
			response=getHTMLResponse(response);
		}
		return response;
	}
	public String processListRBTOptionsRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,
			boolean goToSongListing,String status,int pageValue,String isDeactive){
		String method="processListRBTOptionsRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null && ussdInfo.catId!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("listRBTOptions")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
			response=pr.getPropertyValue("msg.selectoption")+"<br>";
				response=response+vodaRomaniaInfo.listRBTOptionMsg;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL+"&request_value=gift"+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&request_value=listRBTOptionsSel"+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;

				response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
				response=getHTMLResponseBodyString( response, dynamicResponseString, false);

			}else if(request_value!=null && request_value.equalsIgnoreCase("listRBTOptionsSel")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&request_value=listRBTOptions"+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				int count=1;
				if(response==null){
				response="@@"+count+"."+pr.getPropertyValue("set.normal");
				}else{
				response=response+"@@"+count+"."+pr.getPropertyValue("set.normal");
				}

				//				logger.info("vodaRomaniaInfo.dayOfTheWeekAllowed =="+vodaRomaniaInfo.dayOfTheWeekAllowed);
				//				logger.info("vodaRomaniaInfo.futureDayAllowed =="+vodaRomaniaInfo.futureDayAllowed);
				//				logger.info("vodaRomaniaInfo.timeOfTheDayAllowed =="+vodaRomaniaInfo.timeOfTheDayAllowed);

				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+
				""+count+"`"+ussdInfo.URL+"&request_value=listOptions"+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="
				+isDeactive+vodaRomaniaInfo.endURLChar;
				count++;
				if(vodaRomaniaInfo.dayOfTheWeekAllowed){
				response=response+"@@"+count+"."+pr.getPropertyValue("set.dayinweek")+"<br>";
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+
					""+count+"`"+ussdInfo.URL+"&request_value=dayOfTheWeekValue"+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="
					+isDeactive+vodaRomaniaInfo.endURLChar;
					count++;
				}
				//				logger.info("response =="+response);
				if(vodaRomaniaInfo.futureDayAllowed){
					response=response+"@@"+count+"."+pr.getPropertyValue("set.specificdate")+"<br>";
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+
					""+count+"`"+ussdInfo.URL+"&request_value=futureDateValue"+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="
					+isDeactive+vodaRomaniaInfo.endURLChar;
					count++;
				}
				//				logger.info("response =="+response);
				if(vodaRomaniaInfo.timeOfTheDayAllowed){
				response=response+"@@"+count+"."+pr.getPropertyValue("set.timeoftheday")+"<br>";
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+
					""+count+"`"+ussdInfo.URL+"&request_value=timeOfTheDayValue"+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="
					+isDeactive+vodaRomaniaInfo.endURLChar;
					count++;
				}
				//				logger.info("response =="+response);
				response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
				dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue)+"&request_value=listRBTOptionsSel&pageNo="+(++pageValue)+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				response=getHTMLResponseBodyString( response, dynamicResponseString, false);
			}
		}
		return response;
	}
	public String prcessDayOfTheWeekRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,
			boolean goToSongListing,String status,int pageValue,String isDeactive){
		String method="processTimeOfTheDayRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null && ussdInfo.catId!=null){


			//			try {
			int clipid=Integer.parseInt(clipId);
			Clips clip=rbtDBManager.getClip(clipid);
			if(clip!=null){
				response=clip.name();
				response=replaceDotBySpace(response);
			}

			if(request_value!=null && request_value.equalsIgnoreCase("dayOfTheWeekOption")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
				if(response!=null){
					response=response+vodaRomaniaInfo.newLineCharString;
				}
				response=response+vodaRomaniaInfo.timeOfTheDayOptionMsg+vodaRomaniaInfo.newLineCharString;
				response=response+vodaRomaniaInfo.seperatorChar+"1."+vodaRomaniaInfo.answerStr;
				response=response+vodaRomaniaInfo.seperatorChar+"2."+vodaRomaniaInfo.rejectStr;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL
				+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=dayOfTheWeekValue&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar+
				"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listOptions&startTime=0&endTime=23&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				//timeOfTheDayOption
			}else if(request_value!=null && request_value.equalsIgnoreCase("dayOfTheWeekValue")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
				+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=dayOfTheWeekCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				if(response!=null){
					response=response+vodaRomaniaInfo.newLineCharString;
				}
				response=response+vodaRomaniaInfo.dayOfTheWeekValueMsg;
				response=getHTMLResponseBodyString( response, dynamicResponseString, true);
			}else if(request_value!=null && request_value.equalsIgnoreCase("dayOfTheWeekCheck")){
				if(ans!=null){
					ans=ans.trim();
					logger.info("ans =="+ans);
					String dayOfTheWeek=ans;
					if(dayOfTheWeek.equalsIgnoreCase("1") || dayOfTheWeek.equalsIgnoreCase("2") ||dayOfTheWeek.equalsIgnoreCase("3")||
							dayOfTheWeek.equalsIgnoreCase("4")|| dayOfTheWeek.equalsIgnoreCase("5") ||dayOfTheWeek.equalsIgnoreCase("6")
							||dayOfTheWeek.equalsIgnoreCase("7")){
						response=prcessListOptionsRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,"0",giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,null,null,isDeactive,"W"+dayOfTheWeek,null);
					}
					else{
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
						+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=dayOfTheWeekCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						if(response!=null){
							response=response+vodaRomaniaInfo.newLineCharString+"Introducere invalida"+vodaRomaniaInfo.newLineCharString;
						}
						response=response+vodaRomaniaInfo.dayOfTheWeekValueMsg;
						response=getHTMLResponseBodyString( response, dynamicResponseString, true);
					}
				}else{
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
					+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=dayOfTheWeekCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
					if(response!=null){
						response=response+vodaRomaniaInfo.newLineCharString+"Introducere invalida"+vodaRomaniaInfo.newLineCharString;
					}
					response=response+vodaRomaniaInfo.dayOfTheWeekValueMsg;
					response=getHTMLResponseBodyString( response, dynamicResponseString, true);
				}
			}
		}
		return response;
	}
	public String prcessFutureDateRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,
			boolean goToSongListing,String status,int pageValue,String isDeactive){
		String method="processTimeOfTheDayRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null && ussdInfo.catId!=null){


			//			try {
			int clipid=Integer.parseInt(clipId);
			Clips clip=rbtDBManager.getClip(clipid);
			if(clip!=null){
				response=clip.name();
				response=replaceDotBySpace(response);
			}
			//			futureDateValue
			if(request_value!=null && request_value.equalsIgnoreCase("futureDateOption")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
				if(response!=null){
					response=response+vodaRomaniaInfo.newLineCharString;
				}
				response=response+vodaRomaniaInfo.timeOfTheDayOptionMsg+vodaRomaniaInfo.newLineCharString;
				response=response+vodaRomaniaInfo.seperatorChar+"1."+vodaRomaniaInfo.answerStr;
				response=response+vodaRomaniaInfo.seperatorChar+"2."+vodaRomaniaInfo.rejectStr;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL
				+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=timeOfTheDayValue&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar+
				"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listOptions&startTime=0&endTime=23&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				//timeOfTheDayOption
			}else if(request_value!=null && request_value.equalsIgnoreCase("futureDateValue")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
				+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=futureDateCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				if(response!=null){
					response=response+vodaRomaniaInfo.newLineCharString;
				}
				response=response+vodaRomaniaInfo.futureDateValueMsg;
				response=getHTMLResponseBodyString( response, dynamicResponseString, true);
			}else if(request_value!=null && request_value.equalsIgnoreCase("futureDateCheck")){
				if(ans!=null){
					ans=ans.trim();
					logger.info("ans =="+ans);
					String futureDate=ans;
					boolean validFutureDate=false;
					validFutureDate=isValidFutureDate(futureDate);
					logger.info("validFutureDate =="+validFutureDate);
					if(validFutureDate){
						response=prcessListOptionsRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,"0",giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,null,null,isDeactive,null,futureDate);
					}else{
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
						+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=futureDateCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						if(response!=null){
							response=response+vodaRomaniaInfo.newLineCharString;
						}
						response=response+vodaRomaniaInfo.newLineCharString+"Data invalida"+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.futureDateValueMsg;
						response=getHTMLResponseBodyString( response, dynamicResponseString, true);
					}
				}else{
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
					+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=futureDateCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
					if(response!=null){
						response=response+vodaRomaniaInfo.newLineCharString;
					}
					response=response+vodaRomaniaInfo.newLineCharString+"Data invalida"+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.futureDateValueMsg;
					response=getHTMLResponseBodyString( response, dynamicResponseString, true);
				}
			}
		}
		return response;
	}
	public boolean isValidFutureDate(String futureDate){
		String method="";
		boolean validFutureDate=false;
		if(futureDate!=null && futureDate.length()==8){
			char[] futureDateArray=futureDate.toCharArray();
			if(futureDateArray!=null){
				for(int count=0;count<futureDateArray.length;count++){
					char tempChar=futureDateArray[count];
					logger.info("Char=="+tempChar);
					if((tempChar=='0' || tempChar=='1' || tempChar=='2' || tempChar=='3' || tempChar=='4'
						|| tempChar=='5' || tempChar=='6' || tempChar=='7' || tempChar=='8' || tempChar=='9')){
						validFutureDate=true;
						logger.info("Invalid futureDate String");
						break;
					}
				}
			}
		}
		if(validFutureDate){
			Calendar cal=Calendar.getInstance();
			cal.set(Integer.parseInt(futureDate.substring(4)), Integer.parseInt(futureDate.substring(2,4)), Integer.parseInt(futureDate.substring(0,2)), 0, 0);
			Date futureDateObj=cal.getTime();
			Calendar calCurr=Calendar.getInstance();
			Date currDate=calCurr.getTime();
			if(currDate.before(futureDateObj)){
				validFutureDate=true;
				logger.info(" futureDate is before current date");
			}
		}
		return validFutureDate;
	}
	public String prcessTimeOfTheDayRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,
			boolean goToSongListing,String status,int pageValue,String isDeactive){
		String method="processTimeOfTheDayRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null && ussdInfo.catId!=null){


			//			try {
			int clipid=Integer.parseInt(clipId);
			Clips clip=rbtDBManager.getClip(clipid);
			if(clip!=null){
				response=clip.name();
				response=replaceDotBySpace(response);
			}

			if(request_value!=null && request_value.equalsIgnoreCase("timeOfTheDayOption")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
				if(response!=null){
					response=response+vodaRomaniaInfo.newLineCharString;
				}
				response=response+vodaRomaniaInfo.timeOfTheDayOptionMsg+vodaRomaniaInfo.newLineCharString;
				response=response+vodaRomaniaInfo.seperatorChar+"1."+vodaRomaniaInfo.answerStr;
				response=response+vodaRomaniaInfo.seperatorChar+"2."+vodaRomaniaInfo.rejectStr;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL
				+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=timeOfTheDayValue&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar+
				"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listOptions&startTime=0&endTime=23&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				//timeOfTheDayOption
			}else if(request_value!=null && request_value.equalsIgnoreCase("timeOfTheDayValue")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listRBTOptionsSel&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
				+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=timeOfTheDayCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				if(response!=null){
					response=response+vodaRomaniaInfo.newLineCharString;
				}
				response=response+vodaRomaniaInfo.timeOfTheDayValueMsg;
				response=getHTMLResponseBodyString( response, dynamicResponseString, true);
			}else if(request_value!=null && request_value.equalsIgnoreCase("timeOfTheDayCheck")){
				if(ans!=null && ans.length()==4){
					ans=ans.trim();
					logger.info("ans =="+ans);
					String tempInitInterval=ans.substring(0, 2);
					String tempFinalInterval=ans.substring(2);
					ans=tempInitInterval+" "+tempFinalInterval;
					logger.info("ans =="+ans);
					StringTokenizer st=new StringTokenizer(ans," ");
					int count=0; 
					int startTime=-1;
					int endTime=-1;
					while(st.hasMoreTokens()){
						String temp=st.nextToken();
						int tempTime=-1;
						if(count==0){
							String strtTimeStr=temp;
							try {
								tempTime=Integer.parseInt(strtTimeStr);
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								logger.error("", e);
								e.printStackTrace();
							}
							if(tempTime!=-1){
								startTime=tempTime;
							}
							tempTime=-1;
						}else if(count==1){
							String endTimeStr=temp;
							try {
								tempTime=Integer.parseInt(endTimeStr);

							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								logger.error("", e);
								e.printStackTrace();
							}
							if(tempTime!=-1){
								endTime=tempTime;
							}
						}
						count++;
					}
					if(startTime<endTime && endTime<25 && endTime>-1 && startTime<24 && startTime>-1){
						response=prcessListOptionsRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,"0",giftAllowed,specialCaller,preListen,USSDMenu,false,status,pageValue,""+startTime,""+(endTime),isDeactive,null,null);
					}else{
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=timeOfTheDayOption&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
						+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=timeOfTheDayCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						if(response!=null){
							response=response+vodaRomaniaInfo.newLineCharString+"Interval invalid"+vodaRomaniaInfo.newLineCharString;
						}
						response=response+vodaRomaniaInfo.timeOfTheDayValueMsg;
						response=getHTMLResponseBodyString( response, dynamicResponseString, true);
					}
				}else{
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=timeOfTheDayOption&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL
					+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=timeOfTheDayCheck&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
					if(response!=null){
						response=response+"Interval invalid"+vodaRomaniaInfo.newLineCharString;
					}
					response=response+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.timeOfTheDayValueMsg;
					response=getHTMLResponseBodyString( response, dynamicResponseString, true);
				}
			}
		}
		return response;
	}
	public String prcessListOptionsRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,
			boolean goToSongListing,String status,int pageValue,String startTime,String endTime,
			String isDeactive,String dayOfTheWeek,String futureDate){
		String method="prcessListOptionsRequest";
		String response=null;
		String dynamicResponseString=null;

		if((specialCaller==null || specialCaller.equalsIgnoreCase("") || specialCaller.equalsIgnoreCase("false")) 
				&& (preListen==null || preListen.equalsIgnoreCase("")|| preListen.equalsIgnoreCase("false"))){
			response=processDefaultSelRequest(subId,ans,circleId,processId,"defaultSel",callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,true,status,startTime,endTime,isDeactive,dayOfTheWeek,futureDate,null);
		}else{
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			if(ussdInfo!=null && ussdInfo.catId!=null){
				//				logger.info("ussdInfo!=null && ussdInfo.catId!=null");
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&clipId="+clipId+"&isPrepaid="+isPrepaid+"&isDeactive="+isDeactive+"&request_value=listRBTOptionsSel"+vodaRomaniaInfo.endURLChar;

				int count=1;
				logger.info("count=="+count);
				try {
					int clipid=Integer.parseInt(clipId);
					Clips clip=rbtDBManager.getClip(clipid);
					if(clip!=null){
						response=clip.name();
						response=replaceDotBySpace(response);
					}
					//					logger.info("response=="+response);
					//					response=response+vodaRomaniaInfo.newLineCharString+ vodaRomaniaInfo.seperatorChar+count+"."+vodaRomaniaInfo.defaultSelOption;
					//					logger.info("response=="+response);
					//					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+count+"`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&callerNo=all&request_value=defaultSel&isDeactive="+isDeactive;
					//					count++;
					if(specialCaller!=null && !specialCaller.equalsIgnoreCase("") && !specialCaller.equalsIgnoreCase("false")){
						response=response+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.specialCallerSelOption + vodaRomaniaInfo.newLineCharString;
						//						logger.info("response=="+response);
						response=response+vodaRomaniaInfo.seperatorChar+"1."+vodaRomaniaInfo.answerStr + vodaRomaniaInfo.newLineCharString;
						response=response+vodaRomaniaInfo.seperatorChar+"2."+vodaRomaniaInfo.rejectStr;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=defaultSel&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=specialCallerSel&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
						count++;
					}

					if(preListen!=null && !preListen.equalsIgnoreCase("") && !preListen.equalsIgnoreCase("false") && clip.nameFile()!=null){
						response=response+vodaRomaniaInfo.newLineCharString+vodaRomaniaInfo.seperatorChar+count+"."+vodaRomaniaInfo.prelistenOption;
						//						logger.info("response=="+response);
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+count+"`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=preListen"+vodaRomaniaInfo.endURLChar;
						count++;
					}
					response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
					dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue);
					response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return response;
	}
	public String processPrelistenRequest(String subId,String circleId,String processId,
			String request_value,String clipId,boolean isPrepaid,
			String preListen,HashMap USSDMenu){
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null && ussdInfo.catId!=null){
			dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+vodaRomaniaInfo.endURLChar;
			if(request_value!=null && request_value.equalsIgnoreCase("preListen")){
				try {
					int clipid=Integer.parseInt(clipId);
					Clips clip=rbtDBManager.getClip(clipid);
					if(clip!=null){
						String clipNameWavFile=clip.name();
						response=Tools.findNReplaceAll(vodaRomaniaInfo.prelistenMsg, "%nameWavFile%", replaceDotBySpace(clipNameWavFile));
						response=response+dynamicResponseString;
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
		}
		return response;
	}
	public String processDefaultSelRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,
			String pageNo,String giftAllowed,String specialCaller,String preListen,HashMap USSDMenu,
			boolean goToSongListing,String status,String startTime,String endTime,
			String isDeactive,String dayOfTheWeek,String futureDate,HashMap chargeClassMsg){
		String response=null;
		String dynamicResponseString=null;
		String method="processDefaultSelRequest";
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		int pageValue=0;
		if(pageNo!=null){
			try {
				pageValue=Integer.parseInt(pageNo);
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				pageValue=0;
				e.printStackTrace();
			}
		}
		if(startTime!=null){
			startTime=startTime.trim();
		}
		if(endTime!=null){
			endTime=endTime.trim();
		}
		if(ussdInfo!=null && ussdInfo.catId!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("defaultSel")){

				if(status!=null && status.equalsIgnoreCase("free")){
					if(goToSongListing){
						dynamicResponseString="`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
					}else{
						dynamicResponseString="`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					}
					String strIsPrepaid="y";
					if(!isPrepaid){
						strIsPrepaid="n";
					}
					String defaultSelURL =null;
					if(startTime==null || startTime.trim().equalsIgnoreCase("") || startTime.trim().indexOf("null")!=-1 || endTime==null || endTime.trim().equalsIgnoreCase("") || endTime.trim().indexOf("null")!=-1 || (startTime.trim().equalsIgnoreCase("0") && (endTime.trim().equalsIgnoreCase("23")|| endTime.trim().equalsIgnoreCase("24")))){

						if(dayOfTheWeek!=null && !dayOfTheWeek.trim().equalsIgnoreCase("") && !dayOfTheWeek.trim().equalsIgnoreCase("null")){
							defaultSelURL = vodaRomaniaInfo.urlForSelectionWithDayOfTheWeek;
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%lang%", language);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%day%", dayOfTheWeek);
						}else if(futureDate!=null && !futureDate.trim().equalsIgnoreCase("") && !futureDate.trim().equalsIgnoreCase("null")){
							defaultSelURL = vodaRomaniaInfo.urlForSelectionWithFutureDate;
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%lang%", language);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%futureDate%", "Y"+futureDate);
						}else{
							defaultSelURL = vodaRomaniaInfo.urlForSelection;
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
							defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
						}
					}else{
						defaultSelURL = vodaRomaniaInfo.urlForSelectionWithTimeOfTheDay;
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%startTime%", startTime);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%endTime%", endTime);
					}
					ResponseObj responseobj=makeHttpRequest(destURL, defaultSelURL);
					response=getBackEndResponse(responseobj,vodaRomaniaInfo.freeSongSelSuccessmsg);
					response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				}else{
					if(goToSongListing){
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+vodaRomaniaInfo.endURLChar;
					}else{
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					}
					try {
						if(clipId!=null && !clipId.equalsIgnoreCase("null") && !clipId.equalsIgnoreCase("")){
							Clips clip=rbtDBManager.getClip(Integer.parseInt(clipId));
							
							if(response!=null){
							response=response+pr.getPropertyValue("set.normal.confirm")+replaceDotBySpace(clip.name());
							}else{
								response=pr.getPropertyValue("set.normal.confirm")+replaceDotBySpace(clip.name());
							}
						}

						String selCharMsg=vodaRomaniaInfo.selChargingMsg;
						
						//Added
						logger.info(" catid == "+catId);
						if(catId!=null && !catId.equalsIgnoreCase("null") && !catId.equalsIgnoreCase("")&&chargeClassMsg!=null){
							int id=Integer.parseInt(catId);
							String chargeClass=null;
							com.onmobile.apps.ringbacktones.cache.content.Category cat=rbtDBManager.getCategory(id);
							if(cat!=null){
								   chargeClass=cat.getClassType();
									logger.info(" chargeclass == "+chargeClass);
									if(chargeClassMsg.containsKey(chargeClass)){
										selCharMsg=getChargeClassMsg(selCharMsg, (String)chargeClassMsg.get(chargeClass));
										logger.info(" selcharge == "+selCharMsg);
									}
							}
							
						}
						
						
						selCharMsg=Tools.findNReplaceAll(selCharMsg, "%selCharge%", vodaRomaniaInfo.defaultSelCost);
						selCharMsg=Tools.findNReplaceAll(selCharMsg, "%subCharge%", vodaRomaniaInfo.defaultSubCost);
						
						
						if(response!=null){
							response=response+selCharMsg;
						}
						else{
							response=selCharMsg;
						}

						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo=all"+"&request_value=defaultSelReconfirmation&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo=all"+"&request_value=defaultSelReconfirmationRej&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
						dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue)+"&request_value=defaultSel&clipId="+clipId+"&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
						response=getHTMLResponseBodyString( response, dynamicResponseString, false);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}else if(request_value!=null && request_value.equalsIgnoreCase("defaultSelReconfirmationRej")){
				response=pr.getPropertyValue("msg.thanks");
			}else if(request_value!=null && request_value.equalsIgnoreCase("defaultSelReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String defaultSelURL =null;
				if(startTime==null || startTime.trim().equalsIgnoreCase("") || startTime.trim().indexOf("null")!=-1 || endTime==null || endTime.trim().equalsIgnoreCase("") || endTime.trim().indexOf("null")!=-1 || (startTime.trim().equalsIgnoreCase("0") && (endTime.trim().equalsIgnoreCase("23")|| endTime.trim().equalsIgnoreCase("24")))){
					if(dayOfTheWeek!=null && !dayOfTheWeek.trim().equalsIgnoreCase("") && !dayOfTheWeek.trim().equalsIgnoreCase("null")){
						defaultSelURL = vodaRomaniaInfo.urlForSelectionWithDayOfTheWeek;
						logger.info("defaultSelURL=="+defaultSelURL);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%lang%", language);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%day%", dayOfTheWeek);
						logger.info("defaultSelURL=="+defaultSelURL);
					}else if(futureDate!=null && !futureDate.trim().equalsIgnoreCase("") && !futureDate.trim().equalsIgnoreCase("null")){
						defaultSelURL = vodaRomaniaInfo.urlForSelectionWithFutureDate;
						logger.info("defaultSelURL=="+defaultSelURL);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%lang%", language);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%futureDate%", "Y"+futureDate);
						logger.info("defaultSelURL=="+defaultSelURL);
					}else{
						defaultSelURL = vodaRomaniaInfo.urlForSelection;
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
						defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
						logger.info("defaultSelURL=="+defaultSelURL);
					}

				}else{
					defaultSelURL = vodaRomaniaInfo.urlForSelectionWithTimeOfTheDay;
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%startTime%", startTime);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%endTime%", endTime);
					logger.info("defaultSelURL=="+defaultSelURL);
				}
				ResponseObj responseobj=makeHttpRequest(destURL, defaultSelURL);
				String selCharMsg=vodaRomaniaInfo.songSelSuccessmsgDeactive;
				if(isDeactive!=null && isDeactive.equalsIgnoreCase("true")){
					selCharMsg=vodaRomaniaInfo.songSelSuccessmsgActive;
				}
				selCharMsg=Tools.findNReplaceAll(selCharMsg, "%maxSetHours%", vodaRomaniaInfo.maxSetHours);
				response=getBackEndResponse(responseobj,selCharMsg);
			}
		}
		return response;
	}
	public String processSpecialCallerSelRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,SiteURLDetails destURL
			,String language,boolean isPrepaid,String pageNo,String giftAllowed,String specialCaller,
			String preListen,HashMap USSDMenu,String status,String startTime,String endTime,
			String isDeactive,String dayOfTheWeek,String futureDate,HashMap chargeClassMsg){
		String method="processSpecialCallerSelRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		int pageValue=0;
		if(pageNo!=null){
			try {
				pageValue=Integer.parseInt(pageNo);
			} catch (RuntimeException e) {
				pageValue=0;
				e.printStackTrace();
			}
		}
		if(startTime!=null){
			startTime=startTime.trim();
		}
		if(endTime!=null){
			endTime=endTime.trim();
		}
		if(ussdInfo!=null && ussdInfo.catId!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSel")){
				dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
				dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
				"&clipId="+clipId+"&request_value=specialCallerSelValue&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
			response=pr.getPropertyValue("set.number.enter.msg1");
				response=getHTMLResponseBodyString( response, dynamicResponseString, true);
			}else if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSelValue") && (ans!=null || callerNo!=null)){

				//				if (ans!=null) {
				//					ans = ans.trim();
				//					pageValue=0;
				//				}			
				//				else{
				//					callerNo=callerNo.trim();
				//					ans=callerNo;
				//				}
				if(ans!=null && subId!=null && ans.equalsIgnoreCase(subId)){
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=specialCallerSelValue&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					response=vodaRomaniaInfo.strInvalidRequestMsg;
				response=response+pr.getPropertyValue("set.number.enter.msg1");
					response=getHTMLResponseBodyString( response, dynamicResponseString, true);
				}
				else if(callerNo!=null && subId!=null && callerNo.equalsIgnoreCase(subId)){
					dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
					"&clipId="+clipId+"&request_value=specialCallerSelValue&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					response=vodaRomaniaInfo.strInvalidRequestMsg;
				response=response+pr.getPropertyValue("set.number.enter.msg1");
					response=getHTMLResponseBodyString( response, dynamicResponseString, true);
				}else{
					if(callerNo!=null){
						callerNo=callerNo.trim();
						ans=callerNo;
					}else{
						ans = ans.trim();
						pageValue=0;
					}
					if(ans!=null && !ans.equalsIgnoreCase("") && !ans.equalsIgnoreCase("null")){
						logger.info("callerId=="+ans);
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
						if(status!=null && status.equalsIgnoreCase("free")){
							String strIsPrepaid="y";
							if(!isPrepaid){
								strIsPrepaid="n";
							}
							String selURL =null;
							if(startTime==null || startTime.trim().equalsIgnoreCase("") || startTime.trim().indexOf("null")!=-1 || endTime==null || endTime.trim().equalsIgnoreCase("") || endTime.trim().indexOf("null")!=-1 || (startTime.trim().equalsIgnoreCase("0") && (endTime.trim().equalsIgnoreCase("23")|| endTime.trim().equalsIgnoreCase("24")))){
								if(dayOfTheWeek!=null && !dayOfTheWeek.trim().equalsIgnoreCase("") && !dayOfTheWeek.trim().equalsIgnoreCase("null")){
									selURL = vodaRomaniaInfo.urlForSelectionWithDayOfTheWeek;
									logger.info("selURL=="+selURL);
									selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
									selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
									selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
									selURL=Tools.findNReplaceAll(selURL, "%callerId%", ans);
									selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
									selURL=Tools.findNReplaceAll(selURL, "%lang%", language);
									selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
									selURL=Tools.findNReplaceAll(selURL, "%day%", dayOfTheWeek);
									logger.info("selURL=="+selURL);
								}else if(futureDate!=null && !futureDate.trim().equalsIgnoreCase("") && !futureDate.trim().equalsIgnoreCase("null")){
									selURL = vodaRomaniaInfo.urlForSelectionWithFutureDate;
									logger.info("selURL=="+selURL);
									selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
									selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
									selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
									selURL=Tools.findNReplaceAll(selURL, "%callerId%", ans);
									selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
									selURL=Tools.findNReplaceAll(selURL, "%lang%", language);
									selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
									selURL=Tools.findNReplaceAll(selURL, "%futureDate%", "Y"+futureDate);
									logger.info("selURL=="+selURL);
								}else{
									selURL = vodaRomaniaInfo.urlForSelection;
									logger.info("selURL=="+selURL);
									selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
									selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
									selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
									selURL=Tools.findNReplaceAll(selURL, "%callerId%", ans);
									selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
									selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
									logger.info("selURL=="+selURL);
								}

							}else{
								selURL = vodaRomaniaInfo.urlForSelectionWithTimeOfTheDay;
								logger.info("selURL=="+selURL);
								selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
								selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
								selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
								selURL=Tools.findNReplaceAll(selURL, "%callerId%", ans);
								selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
								selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
								selURL=Tools.findNReplaceAll(selURL, "%startTime%", startTime);
								selURL=Tools.findNReplaceAll(selURL, "%endTime%", endTime);
								logger.info("selURL=="+selURL);
							}
							ResponseObj responseobj=makeHttpRequest(destURL, selURL);
							response=getBackEndResponse(responseobj,vodaRomaniaInfo.freeSongSelSuccessmsg);
							response=getHTMLResponseBodyString( response, dynamicResponseString, false);
						}else{
							try {
								if(clipId!=null && !clipId.equalsIgnoreCase("null") && !clipId.equalsIgnoreCase("")){
									Clips clip=rbtDBManager.getClip(Integer.parseInt(clipId));
									if(response!=null){
									response=response+pr.getPropertyValue("set.normal.confirm")+replaceDotBySpace(clip.name());
									}else{
									response=pr.getPropertyValue("set.normal.confirm")+replaceDotBySpace(clip.name());
									}
								}
								String selCharMsg=vodaRomaniaInfo.selChargingMsg;
								//Added
								if(catId!=null && !catId.equalsIgnoreCase("null") && !catId.equalsIgnoreCase("")&&chargeClassMsg!=null){
									int id=Integer.parseInt(catId);
									String chargeClass=null;
									com.onmobile.apps.ringbacktones.cache.content.Category cat=rbtDBManager.getCategory(id);
									if(cat!=null){
										   chargeClass=cat.getClassType();
											logger.info(" chargeclass == "+chargeClass);
											if(chargeClassMsg.containsKey(chargeClass)){
												selCharMsg=getChargeClassMsg(selCharMsg, (String)chargeClassMsg.get(chargeClass));
												logger.info(" selcharge == "+selCharMsg);
											}
									}
									
								}
								
								selCharMsg=Tools.findNReplaceAll(selCharMsg, "%selCharge%", vodaRomaniaInfo.defaultSelCost);
								selCharMsg=Tools.findNReplaceAll(selCharMsg, "%subCharge%", vodaRomaniaInfo.defaultSubCost);
								if(response!=null){
									response=response+selCharMsg;
								}
								else{
									response=selCharMsg;
								}
								dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=specialCallerSelReconfirmation&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=specialCallerSelReconfirmationRej&isDeactive="+isDeactive+"&startTime="+startTime+"&endTime="+endTime+vodaRomaniaInfo.endURLChar;
								response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
								dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue)+"&isPrepaid="+isPrepaid+"&request_value=specialCallerSelValue&callerNo="+ans+"&clipId="+clipId+"&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
								response=getHTMLResponseBodyString( response, dynamicResponseString, false);
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}	
						}
					}else{
						dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=listOptions&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+
						"&clipId="+clipId+"&request_value=specialCallerSelValue&startTime="+startTime+"&endTime="+endTime+"&isDeactive="+isDeactive+"&dayOfTheWeek="+dayOfTheWeek+"&futureDate="+futureDate+vodaRomaniaInfo.endURLChar;
					response=pr.getPropertyValue("msg.invalidcallernumber")+vodaRomaniaInfo.newLineCharString+pr.getPropertyValue("set.number.enter.msg2");
						response=getHTMLResponseBodyString( response, dynamicResponseString, true);
					}
				}
			}else if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSelReconfirmationRej")){
				response=pr.getPropertyValue("msg.thanks");
			}else if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSelReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String selURL =null;
				if(startTime==null || startTime.trim().equalsIgnoreCase("") || startTime.trim().indexOf("null")!=-1 || endTime==null || endTime.trim().equalsIgnoreCase("") || endTime.trim().indexOf("null")!=-1 || (startTime.trim().equalsIgnoreCase("0") && (endTime.trim().equalsIgnoreCase("23")|| endTime.trim().equalsIgnoreCase("24")))){
					if(dayOfTheWeek!=null && !dayOfTheWeek.trim().equalsIgnoreCase("") && !dayOfTheWeek.trim().equalsIgnoreCase("null")){
						selURL = vodaRomaniaInfo.urlForSelectionWithDayOfTheWeek;
						logger.info("selURL=="+selURL);
						selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
						selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
						selURL=Tools.findNReplaceAll(selURL, "%callerId%", callerNo);
						selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
						selURL=Tools.findNReplaceAll(selURL, "%lang%", language);
						selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
						selURL=Tools.findNReplaceAll(selURL, "%day%", dayOfTheWeek);
						logger.info("selURL=="+selURL);
					}else if(futureDate!=null && !futureDate.trim().equalsIgnoreCase("") && !futureDate.trim().equalsIgnoreCase("null")){
						selURL = vodaRomaniaInfo.urlForSelectionWithFutureDate;
						logger.info("selURL=="+selURL);
						selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
						selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
						selURL=Tools.findNReplaceAll(selURL, "%callerId%", callerNo);
						selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
						selURL=Tools.findNReplaceAll(selURL, "%lang%", language);
						selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
						selURL=Tools.findNReplaceAll(selURL, "%futureDate%", "Y"+futureDate);
						logger.info("selURL=="+selURL);
					}else{
						selURL = vodaRomaniaInfo.urlForSelection;
						logger.info("selURL=="+selURL);
						selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
						selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
						selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
						selURL=Tools.findNReplaceAll(selURL, "%callerId%", callerNo);
						selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
						selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
						logger.info("selURL=="+selURL);
					}
				}else{
					selURL = vodaRomaniaInfo.urlForSelectionWithTimeOfTheDay;
					logger.info("selURL=="+selURL);
					selURL=Tools.findNReplaceAll(selURL, "%subId%", subId);
					selURL=Tools.findNReplaceAll(selURL, "%ussdNo%", vodaRomaniaInfo.calledNo);
					selURL=Tools.findNReplaceAll(selURL, "%isPrepaid%", strIsPrepaid);
					selURL=Tools.findNReplaceAll(selURL, "%callerId%", callerNo);
					selURL=Tools.findNReplaceAll(selURL, "%clipId%", clipId);
					selURL=Tools.findNReplaceAll(selURL, "%catId%", catId);
					selURL=Tools.findNReplaceAll(selURL, "%startTime%", startTime);
					selURL=Tools.findNReplaceAll(selURL, "%endTime%", endTime);
					logger.info("selURL=="+selURL);
				}
				ResponseObj responseobj=makeHttpRequest(destURL, selURL);
				String selCharMsg=vodaRomaniaInfo.songSelSuccessmsgDeactive;
				if(isDeactive!=null && isDeactive.equalsIgnoreCase("true")){
					selCharMsg=vodaRomaniaInfo.songSelSuccessmsgActive;
				}
				selCharMsg=Tools.findNReplaceAll(selCharMsg, "%maxSetHours%", vodaRomaniaInfo.maxSetHours);
				response=getBackEndResponse(responseobj,selCharMsg);
			}
		}
		return response;
	}
	public String processGiftRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,SiteURLDetails destURL,
			String language,boolean isPrepaid,String pageNo,String giftAllowed,String specialCaller,
			String preListen,String isDeactive,HashMap USSDMenu,HashMap chargeClassMsg){
		String method="processGiftRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ans!=null){
			ans=ans.trim();
		}
		int pageValue=0;
		try {
			pageValue=Integer.parseInt(pageNo);
		} catch (NumberFormatException e1) {
			pageValue=0;
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		logger.info("ans=="+ans);
		if(ussdInfo!=null && ussdInfo.catId!=null){

			if(request_value!=null && request_value.equalsIgnoreCase("gift")){
				if(isDeactive!=null && isDeactive.equalsIgnoreCase("true")){
					dynamicResponseString=info.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+"&request_value=listRBTOptions"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"0`"+ussdInfo.URL+"&request_value=gift"+"&pageNo="+(pageValue+1)+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+vodaRomaniaInfo.endURLChar;
				response=pr.getPropertyValue("giftsel.failure.unlessactiveservice.msg");
					response=getValidResponseString(response,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,false,info.newLineCharString,info.seperatorChar);
					response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				}else{
					dynamicResponseString=info.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+"&request_value=listRBTOptions"+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=gift_value"+vodaRomaniaInfo.endURLChar;
				response=pr.getPropertyValue("giftsel.enter.number.msg1");
					response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				}
			}else if(request_value!=null && request_value.equalsIgnoreCase("gift_value") && (ans!=null || callerNo!=null)){
				//				if (ans!=null) {
				//					ans = ans.trim();
				//					pageValue=0;
				//				}
				//				else{
				//					callerNo=callerNo.trim();
				//					ans=callerNo;
				//				}
				if(callerNo!=null){
					callerNo=callerNo.trim();
					ans=callerNo;
				}else{
					ans = ans.trim();
					pageValue=0;
				}
				if(ans!=null && !ans.equalsIgnoreCase("") || !ans.equalsIgnoreCase("null")){
					boolean isGifteeAllowed=false;
					String gifteeStatus="giftee_new_user";
					String giftURL = info.urlForValidatingGiftee;
					giftURL=Tools.findNReplaceAll(giftURL, "%subId%", subId);
					giftURL=Tools.findNReplaceAll(giftURL, "%giftee%", ans);
					giftURL=Tools.findNReplaceAll(giftURL, "%clipId%", clipId);
					giftURL=Tools.findNReplaceAll(giftURL, "%catId%", catId);
					ResponseObj responseObj=makeHttpRequest(destURL, giftURL);
					if(responseObj.responseStatus && responseObj.response!=null && responseObj.response.length()>0){
						String subDetailsXML=responseObj.response.toString();
						String[] arrXPath=new String[1];
						arrXPath[0]="/rbt/response/@status";
						XMLXPathParser xpathparser=new XMLXPathParser(subDetailsXML);
						String[] xpathResponse=xpathparser.getXPathResultStringArray(arrXPath);
						//&& xpathResponse[2].equalsIgnoreCase("new_user")

						if(xpathResponse!=null && xpathResponse.length==1 && xpathResponse[0]!=null && (xpathResponse[0].trim().equalsIgnoreCase("giftee_active") || xpathResponse[0].trim().equalsIgnoreCase("giftee_new_user"))){
							isGifteeAllowed=true;
							if(xpathResponse[0].equalsIgnoreCase("giftee_active")){
								gifteeStatus="giftee_active";
							}
						}
					}
					if(isGifteeAllowed){
						dynamicResponseString=info.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+"&request_value=listRBTOptions";
						try {
							if(gifteeStatus.equalsIgnoreCase("giftee_active")){
								String selCharMsg=vodaRomaniaInfo.giftChargingMsgForSubGiftee;
								if(catId!=null && !catId.equalsIgnoreCase("null") && !catId.equalsIgnoreCase("")&&chargeClassMsg!=null){
									int id=Integer.parseInt(catId);
									String chargeClass=null;
									com.onmobile.apps.ringbacktones.cache.content.Category cat=rbtDBManager.getCategory(id);
									if(cat!=null){
										   chargeClass=cat.getClassType();
											logger.info(" chargeclass == "+chargeClass);
											if(chargeClassMsg.containsKey(chargeClass)){
												selCharMsg=getChargeClassMsg(selCharMsg, (String)chargeClassMsg.get(chargeClass));
												logger.info(" selcharge == "+selCharMsg);
											}
									}
									
								}
								response=Tools.findNReplace(vodaRomaniaInfo.giftChargingMsgForSubGiftee, "%selCharge%", vodaRomaniaInfo.defaultSelCost);
							}else{
								String selCharMsg=vodaRomaniaInfo.giftChargingMsgForUnsubGiftee;
								if(catId!=null && !catId.equalsIgnoreCase("null") && !catId.equalsIgnoreCase("")&&chargeClassMsg!=null){
									int id=Integer.parseInt(catId);
									String chargeClass=null;
									com.onmobile.apps.ringbacktones.cache.content.Category cat=rbtDBManager.getCategory(id);
									if(cat!=null){
										   chargeClass=cat.getClassType();
											logger.info(" chargeclass == "+chargeClass);
											if(chargeClassMsg.containsKey(chargeClass)){
												selCharMsg=getChargeClassMsg(selCharMsg, (String)chargeClassMsg.get(chargeClass));
												logger.info(" selcharge == "+selCharMsg);
											}
									}
									
								}
								response=Tools.findNReplace(Tools.findNReplace(vodaRomaniaInfo.giftChargingMsgForUnsubGiftee, "%selCharge%", vodaRomaniaInfo.defaultSelCost),"%subCharge%",vodaRomaniaInfo.defaultSubCost);
							}
							response=getValidResponseString(response,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,false,info.newLineCharString,info.seperatorChar);
							dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue)+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=gift"+vodaRomaniaInfo.endURLChar;
							dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=giftReconfirmation"+vodaRomaniaInfo.endURLChar+"`"+vodaRomaniaInfo.seperatorChar+"2`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=giftReconfirmationReject"+vodaRomaniaInfo.endURLChar;
							response=getHTMLResponseBodyString( response, dynamicResponseString, false);
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}	
					}else{
						dynamicResponseString=info.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+"&request_value=listRBTOptions"+vodaRomaniaInfo.endURLChar;
						dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=gift_value"+vodaRomaniaInfo.endURLChar;
					response=pr.getPropertyValue("giftsel.failure.msg")+vodaRomaniaInfo.seperatorChar+pr.getPropertyValue("giftsel.enter.number.msg2");
						response=getHTMLResponseBodyString( response, dynamicResponseString, false);
					}
				}else{
					dynamicResponseString=info.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive+"&request_value=listRBTOptions"+vodaRomaniaInfo.endURLChar;
					dynamicResponseString=dynamicResponseString+"`"+vodaRomaniaInfo.seperatorChar+"default`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=gift_value"+vodaRomaniaInfo.endURLChar;
				response=pr.getPropertyValue("giftsel.failure.msg")+vodaRomaniaInfo.newLineCharString+pr.getPropertyValue("giftsel.enter.number.msg2");
					response=getHTMLResponseBodyString( response, dynamicResponseString, false);
				}
			}else if(request_value!=null && request_value.equalsIgnoreCase("giftReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				//				urlForSendingGift="Gift.do?action=send_gift&gifterID=%subId%&gifteeID=%gifteeNo%&
				//toneID=%clipId%&categoryID=%catId%&calledNo=%ussdNo%&mode=USSD";
				String giftURL = info.urlForSendingGift;
				giftURL=Tools.findNReplaceAll(giftURL, "%subId%", subId);
				giftURL=Tools.findNReplaceAll(giftURL, "%ussdNo%", info.calledNo);
				giftURL=Tools.findNReplaceAll(giftURL, "%isPrepaid%", strIsPrepaid);
				giftURL=Tools.findNReplaceAll(giftURL, "%gifteeNo%",callerNo);
				giftURL=Tools.findNReplaceAll(giftURL, "%clipId%", clipId);
				giftURL=Tools.findNReplaceAll(giftURL, "%catId%", catId);
				ResponseObj responseobj=makeHttpRequest(destURL, giftURL);
				response=getBackEndResponse(responseobj,vodaRomaniaInfo.giftSelSuccessmsg);
				//				response=getHTMLResponseBodyString( response, dynamicResponseString, false);
			}else if(request_value!=null && request_value.equalsIgnoreCase("giftReconfirmationReject")){
			response=pr.getPropertyValue("msg.thanks");
			}
		}
		return response;
	}
	public String processMiscellaneousRequest(String subId,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		return super.processMiscellaneousRequest( subId, circleId, processId, request_value,
				destURL, pageNo,USSDMenu);
	}
	public String processCatSearchRequest(String subId,String circleId,String processId,
			String request_value,SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		String response=null;
		try {
			int pageValue=0;
			try {
				pageValue = Integer.parseInt(pageNo);
			} catch (RuntimeException e) {
				// TODO Auto-generated catch block
				pageValue=0;
				e.printStackTrace();
			}
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			if(ussdInfo!=null){
				String currURL=ussdInfo.URL;
				if(currURL!=null){
					String responseString=ussdInfo.responseString;
					String dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
					if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
						response=getValidResponseString(responseString,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,true);
						response=getHTMLResponseBodyString(response,dynamicResponseString,false);
					}
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return response;
		}
		return response;
	}
	public String prcessTariffRequest(String subId,String ans,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		String response=null;
		String dynamicResponseString=null;
		int pageValue=0;
		try {
			pageValue = Integer.parseInt(pageNo);
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			pageValue=0;
			e.printStackTrace();
		}
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null){	
			dynamicResponseString=vodaRomaniaInfo.mainMenuURL+"`"+vodaRomaniaInfo.seperatorChar+"*1`"+vodaRomaniaInfo.defaultURL+"&processId=0&pageNo=0"+vodaRomaniaInfo.endURLChar;
			dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue);
			try {
				response=ussdInfo.responseString;
				response=getValidResponseString(response,pageValue,vodaRomaniaInfo.moreStr, vodaRomaniaInfo.enterChoiceStr,vodaRomaniaInfo.maxStrlengthlimit,false);
				response=getHTMLResponseBodyString(response,dynamicResponseString,false);
			}catch(NumberFormatException e) {
				e.printStackTrace();
			}	
		}
		return response;
	}
	public String processMSearchRequest(String subId,String ans,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		return super.processMSearchRequest(subId,ans,circleId,processId,request_value,
				destURL,pageNo,USSDMenu);
	}
	public  String getBackEndResponse(ResponseObj responseobj,String successMsg){

		String response=null;
		if(responseobj.responseStatus ==true){
			if(responseobj.response!=null && responseobj.response.toString().length()>0){
				String tempRepStr=responseobj.response.toString().toLowerCase();
				if(tempRepStr.indexOf(vodaRomaniaInfo.backEndSuccessMsg)!=-1){
					//strSuccessMsg
					if(successMsg==null){
						response=vodaRomaniaInfo.strSuccessMsg;
					}else{
						response=successMsg;
					}
				}else if(tempRepStr.indexOf(vodaRomaniaInfo.backEndSongAlreadyExistMsg)!=-1){
					response=vodaRomaniaInfo.strInvalidSelRequestMsg;
				}else if(tempRepStr.indexOf(vodaRomaniaInfo.backEndInvalidReponseMsg)!=-1){
					response=vodaRomaniaInfo.strInvalidRequestMsg;
				}else{
					response=vodaRomaniaInfo.strTechnicalDifficultyMsg;
				}
			}else{
				response=vodaRomaniaInfo.strTechnicalDifficultyMsg;
			}
		}else{
			response=vodaRomaniaInfo.strTechnicalDifficultyMsg;
		}
		return response;

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
		//		Assumption:: if "responseString" contains "." then either its length should be less than (strlegthlimit-enterChoiceStr)if it contains only one "." OR if it contains two "." then distance between two "." must be <(strlegthlimit-enterChoiceStr)
		return super.getValidResponseString( responseString,pageValue,moreStr,enterChoiceStr, maxStrlegthlimit,numberOrderingStr,vodaRomaniaInfo.newLineCharString,vodaRomaniaInfo.seperatorChar);
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
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", vodaRomaniaInfo.calledNo);
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
	public ArrayList getUSSDSubLibraryDetails(String subId,SiteURLDetails destURL){
		ArrayList downloadList=null;
		String method="getUSSDSubLibraryDetails";
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForLibraryDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", vodaRomaniaInfo.calledNo);
			ResponseObj responseObj=makeHttpRequest(destURL,tempURLForSubDetails);
			if(responseObj!=null){
				if(responseObj.responseStatus && responseObj.response!=null && responseObj.response.length()>0){
					String subDetailsXML=responseObj.response.toString();
					String arrXPath="/rbt/library/downloads/*";
					logger.info("subDetailsXML=="+subDetailsXML);
					XMLXPathParser xpathparser=new XMLXPathParser(subDetailsXML);
					Node xpathResponse=xpathparser.getXPathResultNode(arrXPath);
					if(xpathResponse!=null){
						logger.info("xpathResponse=="+xpathResponse.toString());
						List propertyMap=populateContentList(xpathResponse);
						//&& xpathResponse[2].equalsIgnoreCase("new_user")

						logger.info("element!=null");
						if(propertyMap!=null && propertyMap.size()>0){
							logger.info("propertyMap!=null && propertyMap.size()>0");
							Iterator iter=propertyMap.iterator();
							while(iter.hasNext()){
								HashMap tempMap=(HashMap)iter.next();
								String type=null;
								String clipId=null;
								String catId=null;
								String downloadStatus=null;
								String name=null;
								type=(String)tempMap.get("type");
								name=(String)tempMap.get("name");
								catId=(String)tempMap.get("category_id");
								String shuffleId=(String)tempMap.get("shuffle_id");
								clipId=(String)tempMap.get("id");
								downloadStatus=(String)tempMap.get("download_status");
								logger.info("type=="+type);
								logger.info("name=="+name);
								logger.info("catId=="+catId);
								logger.info("shuffleId=="+shuffleId);
								logger.info("clipId=="+clipId);
								logger.info("downloadStatus=="+downloadStatus);
								if(type!=null){
									logger.info("type!=null");
									if(type.equalsIgnoreCase("clip") ){
										logger.info("type.equalsIgnoreCase(\"clip\")");

										if(downloadStatus!=null && downloadStatus.equalsIgnoreCase("active") && clipId!=null && catId!=null && name!=null){
											logger.info("valid clip entry");
											USSDClips tempClip=new USSDClips(type,clipId,catId,name,downloadStatus);
											if(downloadList==null){
												downloadList=new ArrayList();
											}
											downloadList.add(tempClip);
										}
									}else{
										logger.info("!type.equalsIgnoreCase(\"clip\") ");
										if( shuffleId!=null && name!=null && downloadStatus!=null && downloadStatus.equalsIgnoreCase("active")){
											logger.info("valid shuffle entry");
											USSDClips tempClip=new USSDClips(type,clipId,shuffleId,name,downloadStatus);
											if(downloadList==null){
												downloadList=new ArrayList();
											}
											downloadList.add(tempClip);
										}
									}
								}
							}
						}else{
							return null;
						}
					}
				}
			}
		}
		if(downloadList!=null && downloadList.size()>0){
			return downloadList;
		}
		return null;
	}
	public USSDSubDetails getUSSDSubDetails(String subId,SiteURLDetails destURL){
		USSDSubDetails subDet=null;
		//		SiteURLDetails destURL=getDestURL(sc,subId);
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", vodaRomaniaInfo.calledNo);
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
							logger.info("subDet=="+subDet.toString());
							//String subClass,String userType,String status,boolean active,boolean isPrepaid,String language
						}else if(xpathResponse[2].equalsIgnoreCase("deactive")|| xpathResponse[2].equalsIgnoreCase("new_user")){
							subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],false,isPrepaid,xpathResponse[5],true);
							logger.info("subDet=="+subDet.toString());
						}else{
							subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],false,isPrepaid,xpathResponse[5],false);
							logger.info("subDet=="+subDet.toString());
						}
					}
				}
			}
		}
		logger.info("returning with subDet=="+subDet.toString());
		return subDet;
	}
	public String isSubscriberDeactivated(String subId,SiteURLDetails destURL){

		String returnFlag="false";
		//		SiteURLDetails destURL=getDestURL(sc,subId);
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", vodaRomaniaInfo.calledNo);
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
					if(xpathResponse!=null && xpathResponse.length==3 && xpathResponse[1].equalsIgnoreCase("y") && xpathResponse[0].equalsIgnoreCase("success") && (xpathResponse[2].equalsIgnoreCase("deactive")|| xpathResponse[2].equalsIgnoreCase("new_user"))){
						returnFlag="true";
					}
				}
			}
		}
		return returnFlag;

	}
	public boolean isSubscriberActivated(String subId,SiteURLDetails destURL){
		boolean returnFlag=false;
		//		SiteURLDetails destURL=getDestURL(sc,subId);
		if (destURL!=null && destURL.URL!=null) {
			String tempURLForSubDetails=urlForSubDetails;
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%subId%", subId);
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", vodaRomaniaInfo.calledNo);
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
					if(xpathResponse!=null && xpathResponse.length==3 && xpathResponse[1].equalsIgnoreCase("y") && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[2].equalsIgnoreCase("active")&& xpathResponse[2].equalsIgnoreCase("act_pending")){
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
			tempURLForSubDetails=Tools.findNReplaceAll(tempURLForSubDetails, "%ussdNo%", vodaRomaniaInfo.calledNo);
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
	public String getHTMLResponse(String responseStr){
		String returnString=null;
		if(responseStr!=null){
			returnString="<HTML><HEAD></HEAD><BODY>"+responseStr+"</BODY></HTML>";
		}
		return returnString;
	}
	public static String decreaseExpectedInputResposeKeyValueByOne(String dynamicResponseString,int startPointKeyInputValue,
			String seperatorChar){
		//startPointKeyInputValue denotes key input value after which this reduction has to done 
		//along with removing this key value depending upon value of removestartPointKeyInputValue
		//excluding key input with value "#" and "0"
		String strStartPoint="`"+seperatorChar+startPointKeyInputValue+"`";
		String tempDynamicResponse=dynamicResponseString;
		String tempDynamicResponseStr=tempDynamicResponse.substring(0, tempDynamicResponse.indexOf(strStartPoint));
		String strAfterstartPointKeyInput=null;
		strAfterstartPointKeyInput=tempDynamicResponse.substring(tempDynamicResponse.indexOf(strStartPoint));
		strAfterstartPointKeyInput=strAfterstartPointKeyInput.substring(strStartPoint.length());
		if(strAfterstartPointKeyInput.indexOf("`")!=-1){
			strAfterstartPointKeyInput=strAfterstartPointKeyInput.substring(strAfterstartPointKeyInput.indexOf("`"));
			String strAfterstartPointKeyInputTemp="";
			StringTokenizer st=new StringTokenizer(strAfterstartPointKeyInput,"`");
			int count=1;
			while(st.hasMoreTokens()){
				String temp=st.nextToken();
				if(temp!=null && (temp.length()==seperatorChar.length()+1 || temp.length()==seperatorChar.length()+2)){

					temp=temp.substring(seperatorChar.length());
					try {
						int tempValue=Integer.parseInt(temp);
						if(tempValue!=0){
							tempValue--;
							strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+"`"+seperatorChar+tempValue+"`";
						}else{
							strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+"`"+seperatorChar+temp+"`";
						}
					} catch (NumberFormatException e) {
						strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+"`"+seperatorChar+temp+"`";
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+temp;
				}
				count++;
			}
			if(strAfterstartPointKeyInputTemp!=null && !strAfterstartPointKeyInputTemp.equalsIgnoreCase("")){
				strAfterstartPointKeyInput=strAfterstartPointKeyInputTemp;
			}
		}else{
			strAfterstartPointKeyInput="";
		}
		tempDynamicResponseStr=tempDynamicResponseStr+strAfterstartPointKeyInput;
		return tempDynamicResponseStr;
	}
	public static String decreaseExpectedOutputResposeKeyValueByOne(String responseString,int startPointKeyInputValue,
			String seperatorChar,String newLineChar){
		//startPointKeyInputValue denotes key input value after which this reduction has to done 
		//along with removing this key value depending upon value of removestartPointKeyInputValue
		//excluding key input with value "#" and "0"
		String method="decreaseExpectedOutputResposeKeyValueByOne";
		String strStartPoint=newLineChar+seperatorChar+startPointKeyInputValue+".";
		String tempResponse=responseString;
		logger.info("responseString=="+responseString);
		logger.info("strStartPoint=="+strStartPoint);
		String tempResponseStr=tempResponse.substring(0, tempResponse.indexOf(strStartPoint));
		logger.info("tempResponseStr=="+tempResponseStr);
		String strAfterstartPointKeyInput=null;
		strAfterstartPointKeyInput=tempResponse.substring(tempResponse.indexOf(strStartPoint));
		logger.info("strAfterstartPointKeyInput=="+strAfterstartPointKeyInput);
		strAfterstartPointKeyInput=strAfterstartPointKeyInput.substring(strStartPoint.length());
		logger.info("strAfterstartPointKeyInput=="+strAfterstartPointKeyInput);
		if(strAfterstartPointKeyInput.indexOf(".")!=-1){
			strAfterstartPointKeyInput=strAfterstartPointKeyInput.substring(strAfterstartPointKeyInput.indexOf(".")-1-seperatorChar.length()-newLineChar.length());
			logger.info("strAfterstartPointKeyInput=="+strAfterstartPointKeyInput);
			String strAfterstartPointKeyInputTemp="";
			StringTokenizer st=new StringTokenizer(strAfterstartPointKeyInput,seperatorChar);
			int count=1;
			while(st.hasMoreTokens()){
				logger.info("count=="+count);
				String temp=st.nextToken();
				logger.info("temp=="+temp);
				if(temp!=null && (temp.indexOf(".")!=-1)){
					logger.info("temp!=null && (temp.length()==seperatorChar.length()+1 )");
					String tempInt=temp.substring(0, 1);
					temp=temp.substring(1);
					logger.info("tempInt=="+tempInt);
					try {
						int tempValue=Integer.parseInt(tempInt);
						logger.info("tempValue=="+tempValue);
						if(tempValue!=0){
							tempValue--;
							logger.info("tempValue=="+tempValue);
							strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+seperatorChar+tempValue+temp;
							logger.info("strAfterstartPointKeyInputTemp=="+strAfterstartPointKeyInputTemp);
						}else{
							strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+seperatorChar+tempValue+temp;
							logger.info("strAfterstartPointKeyInputTemp=="+strAfterstartPointKeyInputTemp);
						}
					} catch (NumberFormatException e) {
						strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+seperatorChar+tempInt+temp;
						logger.info("strAfterstartPointKeyInputTemp=="+strAfterstartPointKeyInputTemp);
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{
					strAfterstartPointKeyInputTemp=strAfterstartPointKeyInputTemp+temp;
					logger.info("strAfterstartPointKeyInputTemp=="+strAfterstartPointKeyInputTemp);
				}
				count++;
			}
			if(strAfterstartPointKeyInputTemp!=null && !strAfterstartPointKeyInputTemp.equalsIgnoreCase("")){
				strAfterstartPointKeyInput=strAfterstartPointKeyInputTemp;
				logger.info("strAfterstartPointKeyInput=="+strAfterstartPointKeyInput);
			}
		}else{
			strAfterstartPointKeyInput="";
		}
		tempResponseStr=tempResponseStr+strAfterstartPointKeyInput;
		logger.info("tempResponseStr=="+tempResponseStr);
		return tempResponseStr;
	}
	public String getHTMLResponseBodyString(String response,String dynamicResponseString,boolean isInputResponse){
		String method="getHTMLResponseBodyString";
		String returnString=null;
		String moreResponseStr=null;
		String backResponseStr=null;
		String defaultResponseStr=null;
		logger.info("response=="+response);
		logger.info("dynamicResponseString=="+dynamicResponseString);
		logger.info("isInputResponse=="+isInputResponse);
		if(response!=null){
			StringTokenizer st=new StringTokenizer(response,vodaRomaniaInfo.seperatorChar);
			int count=0;
			String initialText=null;
			while(st.hasMoreTokens()){
				String tempStr=st.nextToken();
				logger.info("tempStr=="+tempStr);
				if(tempStr!=null){
					String firstChar=tempStr.substring(0,1);
					logger.info("firstChar=="+firstChar);
					if(count==0 && response.indexOf(vodaRomaniaInfo.seperatorChar)!=0){
						logger.info("count==0 && response.indexOf(vodaRomaniaInfo.seperatorChar)!=0");
						initialText=tempStr;
						returnString=initialText;
						logger.info("returnString=="+returnString);
					}else if(dynamicResponseString!=null && firstChar.equalsIgnoreCase("0")){
						logger.info("dynamicResponseString!=null && firstChar.equalsIgnoreCase(\"0\")");
						String tempKey="`"+vodaRomaniaInfo.seperatorChar+firstChar+"`";
						logger.info("tempKey=="+tempKey);
						if(tempKey!=null && tempKey.length()>0 && dynamicResponseString.indexOf(tempKey)!=-1){
							String tempDynamicStr=dynamicResponseString.substring(dynamicResponseString.indexOf(tempKey));
							if(tempDynamicStr!=null && tempDynamicStr.length()>0 && !tempDynamicStr.equalsIgnoreCase("")){
								tempDynamicStr=tempDynamicStr.substring(tempKey.length());
								if(tempDynamicStr!=null){
									String tempUrl=tempDynamicStr;
									if(tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar)!=-1){
										tempUrl=tempUrl.substring(0,tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar));
									}
									String tempRes=getHerfLinkResponseBody("Mai mult","0",tempUrl);
									if(tempRes!=null){
										moreResponseStr=tempRes;
										logger.info("moreResponseStr=="+moreResponseStr);
									}
								}
							}
						}
					}else if(dynamicResponseString!=null && !firstChar.equalsIgnoreCase("#") && !firstChar.equalsIgnoreCase("0") && !firstChar.equalsIgnoreCase("*") && tempStr.indexOf(".")!=-1){
						String accesskey=tempStr.substring(0,tempStr.indexOf("."));
						String text=tempStr.substring(tempStr.indexOf(".")+1);
						String tempKey="`"+vodaRomaniaInfo.seperatorChar+accesskey+"`";
						logger.info("tempKey=="+tempKey);
						if(tempKey!=null && tempKey.length()>0 && dynamicResponseString.indexOf(tempKey)!=-1){
							String tempDynamicStr=dynamicResponseString.substring(dynamicResponseString.indexOf(tempKey));
							if(tempDynamicStr!=null && tempDynamicStr.length()>0 && !tempDynamicStr.equalsIgnoreCase("")){
								tempDynamicStr=tempDynamicStr.substring(tempKey.length());
								if(tempDynamicStr!=null){
									String tempUrl=tempDynamicStr;
									if(tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar)!=-1){
										tempUrl=tempUrl.substring(0,tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar));
									}
									String tempRes=getHerfLinkResponseBody(text,accesskey,tempUrl);
									if(tempRes!=null){
										if(returnString!=null){
											returnString=returnString+tempRes;
										}else{
											returnString=tempRes;
										}
										logger.info("returnString=="+returnString);
									}
								}
							}
						}
					}
				}
				count++;
			}
			if(dynamicResponseString!=null && dynamicResponseString.length()>0 && dynamicResponseString.indexOf("`"+vodaRomaniaInfo.seperatorChar+"*1`")!=-1){
				String tempKey="`"+vodaRomaniaInfo.seperatorChar+"*1`";
				logger.info("tempKey=="+tempKey);
				if(tempKey!=null && tempKey.length()>0 && dynamicResponseString.indexOf(tempKey)!=-1){
					String tempDynamicStr=dynamicResponseString.substring(dynamicResponseString.indexOf(tempKey));
					if(tempDynamicStr!=null && tempDynamicStr.length()>0 && !tempDynamicStr.equalsIgnoreCase("")){
						tempDynamicStr=tempDynamicStr.substring(tempKey.length());
						if(tempDynamicStr!=null){
							String tempUrl=tempDynamicStr;
							if(tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar)!=-1){
								tempUrl=tempUrl.substring(0,tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar));
							}
							String tempRes=getHerfLinkResponseBody(vodaRomaniaInfo.newLineCharString + vodaRomaniaInfo.previous,"*",tempUrl);
							if(tempRes!=null){
								backResponseStr=tempRes;
								logger.info("backResponseStr=="+backResponseStr);
							}
						}
					}
				}
			}
			if(dynamicResponseString!=null && dynamicResponseString.length()>0 && dynamicResponseString.indexOf("`"+vodaRomaniaInfo.seperatorChar+"default`")!=-1){
				String tempKey="`"+vodaRomaniaInfo.seperatorChar+"default`";
				logger.info("tempKey=="+tempKey);
				if(tempKey!=null && tempKey.length()>0 && dynamicResponseString.indexOf(tempKey)!=-1){
					String tempDynamicStr=dynamicResponseString.substring(dynamicResponseString.indexOf(tempKey));
					if(tempDynamicStr!=null && tempDynamicStr.length()>0 && !tempDynamicStr.equalsIgnoreCase("")){
						tempDynamicStr=tempDynamicStr.substring(tempKey.length());
						if(tempDynamicStr!=null){
							String tempUrl=tempDynamicStr;
							if(tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar)!=-1){
								tempUrl=tempUrl.substring(0,tempUrl.indexOf("`"+vodaRomaniaInfo.seperatorChar));
							}
							String tempRes=getHerfLinkResponseBody("defaultOption","default",tempUrl);
							if(tempRes!=null){
								defaultResponseStr=tempRes;
								logger.info("defaultURL=="+defaultResponseStr);
							}
						}
					}
				}
			}
		}
		if(returnString!=null && returnString.length()>0){
			if(moreResponseStr!=null && moreResponseStr.length()>0){
				returnString=returnString+moreResponseStr;
			}
			if(backResponseStr!=null && backResponseStr.length()>0){
				returnString=returnString+backResponseStr;
			}
			if(defaultResponseStr!=null && defaultResponseStr.length()>0){
				returnString=returnString+defaultResponseStr;
			}	
		}
		logger.info("returning returnString=="+returnString);
		return returnString;
	}
	public String getHerfLinkResponseBody(String text,String accesskey,String tempUrl){
		String returnString=null;
		if(tempUrl!=null){
			//<a href="pin.html" default="yes"></a><br>
			if(accesskey!=null && accesskey.equalsIgnoreCase("default")){
				returnString="<a href=\""+tempUrl+"\" default=\"yes\" ></a>"+vodaRomaniaInfo.newLineCharString;
			}else if(text!=null){
//				if(text.endsWith(vodaRomaniaInfo.newLineCharString)){
//					text=text.substring(0, text.lastIndexOf(vodaRomaniaInfo.newLineCharString));
//				}
				returnString="<a href=\""+tempUrl+"\"";
				if(accesskey!=null && accesskey.length()>0 && !accesskey.equalsIgnoreCase("")){
					returnString=returnString+" accesskey="+accesskey+" >"+text+"</a>"+vodaRomaniaInfo.newLineCharString;
				}else{
					returnString=returnString+" >"+text+"</a>"+vodaRomaniaInfo.newLineCharString;
				}
			}
		}
		return returnString;
	}
	public ArrayList<Gift> getGiftInbox(String subID){
		String method="getGiftInbox";
		GiftInbox giftInbox=null;
		Gift[] gifts=null;
		ArrayList< Gift> list=new ArrayList<Gift>();
		logger.info(" getGiftInbox ");
		RbtDetailsRequest rbtDetailsRequest=new RbtDetailsRequest(subID);
		logger.info(" getGiftInbox req "+rbtDetailsRequest);
		
		giftInbox=rbtClient.getGiftInbox(rbtDetailsRequest);
		logger.info(" getGiftInbox giftinbox"+giftInbox);
		if(giftInbox!=null){
			gifts= giftInbox.getGifts();
			logger.info(" gifts "+gifts);
			if(gifts!=null&&gifts.length!=0){
				for(int i=0;i<gifts.length;i++){
					list.add(gifts[i]);
				}
			}
		}
		logger.info(" getGiftInbox list size "+list.size());
		if(list.size()!=0)
			return list;
		else{
			
		}
		return null;
	}
	//added for giftinbox
	public String addFavorites(String subID,String gifterID,int toneID,String toneType,Date giftSentTime,String catID,String language,boolean prepaid){
		String method="addFavorites";
		SelectionRequest selectionRequest =new SelectionRequest(subID);
		selectionRequest.setGifterID(gifterID);
		selectionRequest.setClipID(toneID+"");
		selectionRequest.setGiftSentTime(giftSentTime);
		selectionRequest.setMode("USSD");
		selectionRequest.setCategoryID(catID);
		selectionRequest.setIsPrepaid(prepaid);
		logger.info(" selrequest "+selectionRequest);
		
		rbtClient.downloadGift(selectionRequest );
		if(selectionRequest.getResponse().equalsIgnoreCase(WebServiceConstants.OVERLIMIT)){
			logger.info(" overlimit");
			rbtClient.overwriteDownloadGift(selectionRequest);
		}
		if(selectionRequest.getResponse().equalsIgnoreCase("Success")){
			return "success";
		}else
			return "fail";
		
	}
	//added for giftinbox
	public String acceptGift(String subID,String gifterID,int toneID,String toneType,Date giftSentTime,String catID,String language,boolean prepaid){
		SelectionRequest selectionRequest =new SelectionRequest(subID);
		String method="acceptGift";
		selectionRequest.setGifterID(gifterID);
		selectionRequest.setClipID(toneID+"");
		selectionRequest.setGiftSentTime(giftSentTime);
		selectionRequest.setMode("USSD");
		selectionRequest.setCategoryID(catID);
		selectionRequest.setIsPrepaid(prepaid);
		
		logger.info(" selrequest "+selectionRequest);
		if(toneType.equalsIgnoreCase("service")){
			logger.info(" service ");
			SubscriptionRequest subscriptionRequest =new SubscriptionRequest(subID);
			subscriptionRequest.setGifterID(gifterID);
			subscriptionRequest.setGiftSentTime(giftSentTime);
			subscriptionRequest.setMode("USSD");
			subscriptionRequest.setIsPrepaid(prepaid);
			rbtClient.acceptGiftService(subscriptionRequest);
			if(subscriptionRequest.getResponse().equalsIgnoreCase("Success")){
				return "success";
			}else
				return "fail";
		}else{
			logger.info(" aceptgift ");
			rbtClient.acceptGift(selectionRequest);
			if(selectionRequest.getResponse().equalsIgnoreCase("Success")){
				return "success";
			}else
				return "fail";
		}
	}
	//added for giftinbox
	public String rejectGift(String subID,String gifterID,int toneID,String toneType,Date giftSentTime,String catID,String language,boolean prepaid){
		String method="rejectGift";
		GiftRequest giftRequest = new GiftRequest(gifterID,subID,null,toneID+"",catID,"USSD",giftSentTime);
		logger.info(" giftreq "+giftRequest);
		try {
			rbtClient.rejectGift(giftRequest);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info(" response "+giftRequest.getResponse());
		if(giftRequest.getResponse().equalsIgnoreCase("Success")){
			return "success";
		}else
			return "fail";
		
	}
	public String getChargeClassMsg(String msg,String submsg){
		 String s1=msg.substring(msg.indexOf("<"));
		 String s2=msg.substring(0,msg.indexOf("<"));
		 msg=s2+"( "+submsg+" )"+s1;
		 return msg;
	}
	public String replaceDotBySpace(String inputStr){
		String outputStr=null;
		if(inputStr!=null){
			StringTokenizer st=new StringTokenizer(inputStr,".");
			int count=0;
			while(st.hasMoreTokens()){
				String tempStr=st.nextToken();
				if(count==0){
					outputStr=tempStr;
				}else{
					outputStr=outputStr+" "+tempStr;
				}
				count++;
			}
		}
		if(outputStr!=null){
			inputStr=outputStr;
		}
		return inputStr;
	}
}
