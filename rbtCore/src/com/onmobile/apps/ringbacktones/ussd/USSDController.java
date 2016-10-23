package com.onmobile.apps.ringbacktones.ussd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.servlets.DemoClip;
import com.onmobile.apps.ringbacktones.servlets.ResponseObj;
import com.onmobile.apps.ringbacktones.servlets.SiteURLDetails;
import com.onmobile.apps.ringbacktones.utils.XMLXPathParser;

public class USSDController implements ControllerInterface{
	/*
	 * author@Abhinav Anand
	 */
	private static Logger logger = Logger.getLogger(USSDController.class);
	
	protected static String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForCallDetails="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";
	protected static String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	private static String m_class="USSDController";
	public  USSDBasicFeatures ussdFeatures=null;
	public  static RBTDBManager rbtDBManager=null;
	public  USSDConstants info=null;
	public USSDBasicFeatures getUssdFeatures() {
		return ussdFeatures;
	}
	public void setUssdFeatures(USSDBasicFeatures ussdFeatures) {
		this.ussdFeatures = ussdFeatures;
	}
	public USSDConstants getInfo() {
		return info;
	}
	public void setInfo() {
		info=new USSDConstants();
	}
	public void init(){
		rbtDBManager = RBTDBManager.getInstance();
	}
//	implementation of abstract method
	public String processRequest(HashMap parametersMap) {

		String method="processRequest";
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
			logger.info("isSubAllowed=="+isSubAllowed);
			if(isSubAllowed){
				responseStr=processMainMenuRequest(subId,pageNo,circleId,USSDMenu);
			}

		}else if(request_type.equalsIgnoreCase("SubUnsub")){
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
			responseStr=processSubUnsubRequest(subId,circleId,processId,pageNo,request_value,destURL, subDet,isPrepaid,language,USSDMenu);
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
//			responseStr=responseStr+info.newLineCharString;
//		}
		return responseStr;
	}
	public String processMainMenuRequest(String subId,String pageNo,String circleId,HashMap USSDMenu){
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
						String responseString=ussdInfo.responseString;
						logger.info("responseString=="+responseString);
						String dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
						logger.info("dynamicResponseString=="+dynamicResponseString);
						if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
							response=getValidResponseString(responseString,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,true,info.newLineCharString,info.seperatorChar);
							logger.info("response=="+response);
							response=response+dynamicResponseString;
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
	public boolean isValidMSISDN(String subId){
		String method="isValidMSISDN";
//		logger.info("subId=="+subId);
		boolean validMsisdn=false;
		if(subId==null || subId.length()!=10){
			return false;
		}
		subId=USSDRequestHandler.subId(subId,getInfo().getCountryPrefix());
//		logger.info("subId=="+subId);
		long intMsisdn=-1;
		try {
			intMsisdn=Long.parseLong(subId);
//			logger.info("intMsisdn=="+intMsisdn);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			intMsisdn=-1;
//			logger.info("Caught number format exception, intMsisdn=="+intMsisdn );
			e.printStackTrace();
		}
		if(intMsisdn!=-1){
			validMsisdn= true;
		}
		if(subId==null || subId.length()!=10){
			return false;
		}
		logger.info("returning validMsisdn=="+validMsisdn);
		return validMsisdn;
	}
	public String processCopyRequest(String subId,String ans,String circleId,String processId,
			String request_value,String pageNo,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String defClipId,String defCatId,String copyNo,HashMap USSDMenu){
		String response=null;
		String method="processCopyRequest";
		if(ans!=null){
			ans=ans.trim();
		}
		int pageValue=0;
		try {
			pageValue=Integer.parseInt(pageNo);
		} catch (NumberFormatException e) {
			 pageValue=0;
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("ans=="+ans);
		if(request_value.equalsIgnoreCase("copyNumber")){
			if(ans==null || ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("null")){
				USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
				if(ussdInfo!=null){
					response=ussdInfo.responseString+ussdInfo.dynamicURLResponseString+"ansRequest";
				}
			}else{
				String copyNumber=ans;
				String strClipId=null;
				String strCatId=null;
				if(copyNumber!=null && isValidMSISDN(copyNumber)){
					String copyResponse=getCopyNoDetails(subId,copyNumber,null,defClipId,defCatId);
					if(copyResponse!=null && (copyResponse.equalsIgnoreCase("Try later") || copyResponse.equalsIgnoreCase("Invalid Response"))){
						response=copyResponse;
					}else if(copyResponse!=null ){
						StringTokenizer st=new StringTokenizer(copyResponse,":");
						int count=0;
						while(st.hasMoreElements()){
							String temp=st.nextToken();
							if(count==0 && temp!=null){
								strClipId=temp;
							}else if(count==1 && temp!=null){
								strCatId=temp;
							}
							count++;
						}
						if(strClipId==null || strCatId==null){
							if(strCatId==null){
								strCatId=defCatId;
							}
							if(strClipId==null){
								strClipId=defClipId;
							}
						}
						USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
						if(ussdInfo!=null){
							String childCircleId=ussdInfo.childProcessId;
							if(childCircleId!=null){
								USSDInfo childUSSDInfo=getUSSDInfo(childCircleId,circleId,USSDMenu);
								response=childUSSDInfo.responseString;
								response=getValidResponseString(response,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,false,info.newLineCharString,info.seperatorChar);
								String dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
								dynamicResponseString=Tools.findNReplace(dynamicResponseString,"$answer$",ans);
								response=response+dynamicResponseString+childUSSDInfo.dynamicURLResponseString+"&catId="+strCatId+"&clipId="+strClipId+"&copyNo="+copyNumber;
							}
						}
					}
				}
			}
		}else if(request_value.equalsIgnoreCase("copyCallerNoSelection")){
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			String tempString="&catId="+catId+"&clipId"+clipId+"&copyNo"+copyNo;
			if(ussdInfo!=null){
				String tempDynamicResponse=ussdInfo.dynamicURLResponseString.substring(0, ussdInfo.dynamicURLResponseString.indexOf("`2`"))+tempString+ussdInfo.dynamicURLResponseString.substring(ussdInfo.dynamicURLResponseString.indexOf("`2`"))+tempString;
			}
		}else if(request_value.equalsIgnoreCase("copySel")){
			if(ans==null || ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("null")){
				if(!callerNo.equalsIgnoreCase("all")){
					USSDInfo ussdInfo=getUSSDInfo(parentProcessId,circleId,USSDMenu);
					if(ussdInfo!=null){
						response=info.askForSpecialCallerMsg+info.mainMenuURL+"`*1`"+ussdInfo.URL+"&catId="+catId+"&clipId"+clipId+"&copyNo"+copyNo+"ansRequest";
					}
				}else{
					String copyURL = info.urlForCopySelection;
					copyURL=Tools.findNReplaceAll(copyURL, "%subId%", subId);
					copyURL=Tools.findNReplaceAll(copyURL, "%copyNo%", copyNo);
					copyURL=Tools.findNReplaceAll(copyURL, "%callerId%", "all");
					copyURL=Tools.findNReplaceAll(copyURL, "%catId%", catId);
					copyURL=Tools.findNReplaceAll(copyURL, "%clipId%", clipId);
					copyURL=Tools.findNReplaceAll(copyURL, "%ussdNo%", info.calledNo);
					makeHttpRequest(destURL, copyURL);
					ResponseObj responseobj=makeHttpRequest(destURL, copyURL);
					response=getBackEndResponse(responseobj,null);
				}
			}else{
				String callerNumber=ans;
				String copyURL = info.urlForCopySelection;
				copyURL=Tools.findNReplaceAll(copyURL, "%subId%", subId);
				copyURL=Tools.findNReplaceAll(copyURL, "%copyNo%", copyNo);
				copyURL=Tools.findNReplaceAll(copyURL, "%callerId%", callerNumber);
				copyURL=Tools.findNReplaceAll(copyURL, "%catId%", catId);
				copyURL=Tools.findNReplaceAll(copyURL, "%clipId%", clipId);
				copyURL=Tools.findNReplaceAll(copyURL, "%ussdNo%", info.calledNo);
				ResponseObj responseobj=makeHttpRequest(destURL, copyURL);
				response=getBackEndResponse(responseobj,null);
			}
		}
		return response;
	}
	public  String processSubUnsubRequest(String subId,String circleId,String processId,String pageNo,
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
	public  String processManageRequest(String subId,String circleId,String processId,String pageNo,
			String request_value,SiteURLDetails destURL,USSDSubDetails subDet,boolean isPrepaid,String language,
			ArrayList displaySubPack,
			ArrayList subPackTag,HashMap USSDMenu){
		String method="processManageRequest";
		String response=null;
		if(request_value==null || request_value.equalsIgnoreCase("")){
			logger.info("request_value==null || request_value.equalsIgnoreCase(\"\")");
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			String responseString=null;
			String dynamicResponseString=null;
			if(info.unsubscriptionAllowed || info.defaultSubscritpionAllowed || info.advanceSubscriptionAllowed){
				logger.info("info.unsubscriptionAllowed=="+info.unsubscriptionAllowed);
				logger.info("info.defaultSubscritpionAllowed=="+info.defaultSubscritpionAllowed);
				logger.info("info.advanceSubscriptionAllowed=="+info.advanceSubscriptionAllowed);
				int count=1;
				dynamicResponseString=info.mainMenuURL+"`*1`"+info.defaultURL+"&processId=0&pageNo=0";
				if(ussdInfo!=null){
					logger.info("ussdinfo!=null");
					logger.info("responseString=="+responseString);
					responseString="Manage your subscription";
					if(info.unsubscriptionAllowed && subDet!=null && subDet.active){
						logger.info("info.unsubscriptionAllowed && subDet!=null && subDet.active");
						responseString=responseString+info.newLineCharString+count+".Unsubscriber";
						logger.info("responseString=="+responseString);
						dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&request_value=unsubscribe";
						count++;
					}
					if(info.defaultSubscritpionAllowed && subDet!=null && subDet.deactive){
						logger.info("info.defaultSubscritpionAllowed && subDet!=null && subDet.deactive");
						if(responseString==null){
							responseString=count+".Monthly subscription pack";
						}else{
							responseString=responseString+info.newLineCharString+count+".Monthly subscription pack";
						}
						logger.info("responseString=="+responseString);
						dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&request_value=monthlySub&lang="+language+"&isPrepaid="+isPrepaid;
						count++;
					}
					if(info.advanceSubscriptionAllowed && subDet!=null && subDet.advanceSubscriptionAllowed && displaySubPack!=null && subPackTag!=null){
						logger.info("info.advanceSubscriptionAllowed && subDet!=null && subDet.advanceSubscriptionAllowed && displaySubPack!=null && subPackTag!=null");
						if(displaySubPack!=null && subPackTag!=null){
							for(int counter=0;counter<displaySubPack.size();counter++,count++){
								if(counter==0){
									count--;
									continue;
								}
								if(responseString==null){
									responseString=count+"."+(String)displaySubPack.get(counter);
								}else{
									responseString=responseString+info.newLineCharString+count+"."+(String)displaySubPack.get(counter);
								}
								logger.info("counter=="+counter);
								logger.info("responseString=="+responseString);
								dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&request_value="+(String)subPackTag.get(counter)+"&lang="+language+"&isPrepaid="+isPrepaid;
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
			if(responseString!=null){
				responseString=responseString.trim();
			}
			logger.info("responseString=="+responseString);
			if(responseString!=null && responseString.equalsIgnoreCase("Manage your subscription")){
				response="Invalid Request"+dynamicResponseString;
			}else{
			String dynamicResponseStringWithMoreOption=getDynamicURLWithMoreOption(ussdInfo,pageValue);
			if(dynamicResponseStringWithMoreOption!=null){
				dynamicResponseString=dynamicResponseString+dynamicResponseStringWithMoreOption;
			}
			if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
				response=getValidResponseString(responseString,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,true,info.newLineCharString,info.seperatorChar);
				response=response+dynamicResponseString;
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
			dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL;
			for(int counter=0;counter<subPackTag.size();counter++){
				logger.info("counter=="+counter);
				String temppack=(String)subPackTag.get(counter);
				logger.info("temppack=="+temppack);
				if(request_value!=null && request_value.equalsIgnoreCase(temppack)){
					logger.info("request_value!=null && request_value.equalsIgnoreCase(temppack)");
					SubscriptionClass subClass=CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(temppack);
					if(subClass!=null && subClass.getSubscriptionAmount()!=null && subClass.getSubscriptionPeriod()!=null){
						logger.info("subClass =="+subClass.getSubscriptionPeriod());
						try {
							String tempMsg=info.confirmSubMsg;
							logger.info("tempMsg=="+tempMsg);
							String subAmount=subClass.getSubscriptionAmount();
							tempMsg=Tools.findNReplace(tempMsg, "%amount%", subAmount);
							logger.info("tempMsg=="+tempMsg);
							String subPeriod=USSDServletListner.getDisplayForSubPeriodInMonth(subClass.getSubscriptionPeriod());
							tempMsg=Tools.findNReplace(tempMsg, "%period%", subPeriod);
							logger.info("tempMsg=="+tempMsg);
							dynamicResponseString=dynamicResponseString+"`1`"+ussdInfo.URL+"&request_value="+temppack+"Reconfirmation";
							response=tempMsg+dynamicResponseString;
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
					String advanceSubURL = info.urlForAdvancePackActivation;
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%subId%", subId);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%ussdNo%", info.calledNo);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%isPrepaid%", strIsPrepaid);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%lan%", language);
					advanceSubURL=Tools.findNReplaceAll(advanceSubURL, "%advancePack%", temppack);
					ResponseObj responseobj=makeHttpRequest(destURL, advanceSubURL);
					response=getBackEndResponse(responseobj,null);	
					response=response+dynamicResponseString;

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
				dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL;
				try {
					response=info.confirmUnsubcriptionMsg;
					dynamicResponseString=dynamicResponseString+"`1`"+ussdInfo.URL+"&request_value=unsubscribeReconfirmation";
					response=response+dynamicResponseString;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}else if(request_value!=null && request_value.equalsIgnoreCase("unsubscribeReconfirmation")){
				String unsubURL = info.urlForDeactivation;
				unsubURL=Tools.findNReplaceAll(unsubURL, "%subId%", subId);
				unsubURL=Tools.findNReplaceAll(unsubURL, "%ussdNo%", info.calledNo);
				ResponseObj responseobj=makeHttpRequest(destURL, unsubURL);
				response=getBackEndResponse(responseobj,null);
				response=response+"`*1`"+ussdInfo.URL;
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
				dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL;
				try {
					String tempMsg=info.confirmSubMsg;
					Tools.findNReplace(tempMsg, "%amount%", info.defaultSubCost);
					Tools.findNReplace(tempMsg, "%period%", "1 month");
					dynamicResponseString=dynamicResponseString+"`1`"+ussdInfo.URL+"&request_value=monthlySubReconfirmation";
					response=tempMsg+dynamicResponseString;
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}else if(request_value!=null && request_value.equalsIgnoreCase("monthlySubReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String monthlySubURL = info.urlForMonthlyActivation;
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%subId%", subId);
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%ussdNo%", info.calledNo);
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%isPrepaid%", strIsPrepaid);
				monthlySubURL=Tools.findNReplaceAll(monthlySubURL, "%lan%", language);
				ResponseObj responseobj=makeHttpRequest(destURL, monthlySubURL);
				response=getBackEndResponse(responseobj,null);
				response=response+"`*1`"+ussdInfo.URL;
			}
		}
		return response;
	}
	public String processSongSearchRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,
			SiteURLDetails destURL,String language,boolean isPrepaid,String pageNo,String giftAllowed,
			String specialCaller,String preListen,HashMap USSDMenu,String status,String isDeactive){
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
						dynamicResponseString=info.mainMenuURL+"`*1`"+info.defaultURL+"&processId=0&pageNo=0";
					}else{
						dynamicResponseString=info.mainMenuURL+"`*1`"+info.defaultURL+"&processId="+ussdInfo.parentProcessId+"&pageNo=0&request_type=childCatSearch";
					}
					//check for parentprocessId
					String tempCatid=ussdInfo.catId;
					Clips[] clipsArr=rbtDBManager.getClipsInCategory(tempCatid);
					if(clipsArr!=null){
						if(ussdInfo.processName!=null){
							response=ussdInfo.processName+info.newLineCharString;
						}
						for(int count=0;count<clipsArr.length && count<info.maxClipsNo;count++){
							Clips clip=clipsArr[count];
							if(clip!=null){
								String clipName=clip.name();
								if(response==null){
									response=(count+1)+"."+clipName;
								}else{
									response=response+(count+1)+"."+clipName;
								}
								dynamicResponseString=dynamicResponseString+"`"+(count+1)+"`"+ussdInfo.URL+"&clipId="+clip.id()+"&isPrepaid="+isPrepaid+"&isDeactive="+isDeactive;
							}
						}

						String dynamicResponseStringWithMoreOption=getDynamicURLWithMoreOption(ussdInfo,pageValue);
						if(dynamicResponseStringWithMoreOption!=null){
							dynamicResponseString=dynamicResponseString+dynamicResponseStringWithMoreOption;
						}
						if(response!=null && dynamicResponseString!=null && response.length()>0 && pageValue>=0){
							response=getValidResponseString(response,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,true,info.newLineCharString,info.seperatorChar);
							response=response+dynamicResponseString;
						}
					}
				}
			}else{
				if((giftAllowed==null || giftAllowed.equalsIgnoreCase("") || giftAllowed.equalsIgnoreCase("false")) 
						&& (specialCaller==null || specialCaller.equalsIgnoreCase("") || specialCaller.equalsIgnoreCase("false")) 
						&& (preListen==null || preListen.equalsIgnoreCase("")|| preListen.equalsIgnoreCase("false"))){
					response=processDefaultSelRequest(subId,ans,circleId,processId,"defaultSel",callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,true,status);
				}else{
					USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
					if(ussdInfo!=null && ussdInfo.catId!=null){
//						logger.info("ussdInfo!=null && ussdInfo.catId!=null");
						dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL;

						int count=1;
						logger.info("count=="+count);
						try {
							int clipid=Integer.parseInt(clipId);
							Clips clip=rbtDBManager.getClip(clipid);
							if(clip!=null){
								response=clip.name();
							}
//							logger.info("response=="+response);
							response=response+info.newLineCharString+ count+"."+info.defaultSelOption;
//							logger.info("response=="+response);
							dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=defaultSel";
							count++;
							if(specialCaller!=null && !specialCaller.equalsIgnoreCase("") && !specialCaller.equalsIgnoreCase("false")){
								response=response+info.newLineCharString+ count+"."+info.specialCallerSelOption;
//								logger.info("response=="+response);
								dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=specialCallerSel&ans=$answer$";
								count++;
							}
							if(giftAllowed!=null && !giftAllowed.equalsIgnoreCase("") && !giftAllowed.equalsIgnoreCase("false")){
								response=response+info.newLineCharString+ count+"."+info.giftOption;
//								logger.info("response=="+response);
								dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&request_value=gift&ans=$answer$";
								count++;
							}
							if(preListen!=null && !preListen.equalsIgnoreCase("") && !preListen.equalsIgnoreCase("false") && clip.nameFile()!=null){
								response=response+info.newLineCharString+ count+"."+info.prelistenOption;
//								logger.info("response=="+response);
								dynamicResponseString=dynamicResponseString+"`"+count+"`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&callerNo=all&request_value=preListen";
								count++;
							}
							response=getValidResponseString(response,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,true,info.newLineCharString,info.seperatorChar);
							response=response+dynamicResponseString;
						} catch (NumberFormatException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}else if(request_value.indexOf("preListen")!=-1){
			response=processPrelistenRequest(subId,circleId,processId,request_value,clipId,isPrepaid,preListen,USSDMenu);
		}else if(request_value.indexOf("defaultSel")!=-1){
			response=processDefaultSelRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,USSDMenu,false,status);
		}else if(request_value.indexOf("specialCallerSel")!=-1){
			response=processSpecialCallerSelRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen, USSDMenu,status);
		}else if(request_value.indexOf("gift")!=-1){
			response=processGiftRequest(subId,ans,circleId,processId,request_value,callerNo,catId,clipId,parentProcessId,destURL,language,isPrepaid,pageNo,giftAllowed,specialCaller,preListen,isDeactive,USSDMenu);
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
			dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
			if(request_value!=null && request_value.equalsIgnoreCase("preListen")){
				try {
					int clipid=Integer.parseInt(clipId);
					Clips clip=rbtDBManager.getClip(clipid);
					if(clip!=null){
						String clipNameWavFile=clip.name();
						response=Tools.findNReplaceAll(info.prelistenMsg, "%nameWavFile%", clipNameWavFile);
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
			boolean goToSongListing,String status){
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		if(ussdInfo!=null && ussdInfo.catId!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("defaultSel")){

				if(status!=null && status.equalsIgnoreCase("free")){
//					if(goToSongListing){
						dynamicResponseString="`*1`"+ussdInfo.URL+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
//					}else{
//						dynamicResponseString="`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
//					}
					String strIsPrepaid="y";
					if(!isPrepaid){
						strIsPrepaid="n";
					}
					String defaultSelURL = info.urlForSelection;
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", info.calledNo);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
					defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
					ResponseObj responseobj=makeHttpRequest(destURL, defaultSelURL);
					response=getBackEndResponse(responseobj,info.freeSongSelSuccessmsg);
					response=response+dynamicResponseString;
				}else{
					if(goToSongListing){
						dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL;
					}else{
						dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
					}
					try {
						response=info.selChargingMsg;
						dynamicResponseString=dynamicResponseString+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo=all"+"&request_value=defaultSelReconfirmation";
						response=response+dynamicResponseString;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}else if(request_value!=null && request_value.equalsIgnoreCase("defaultSelReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String defaultSelURL = info.urlForSelection;
				defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%subId%", subId);
				defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%ussdNo%", info.calledNo);
				defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%isPrepaid%", strIsPrepaid);
				defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%callerId%", "all");
				defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%clipId%", clipId);
				defaultSelURL=Tools.findNReplaceAll(defaultSelURL, "%catId%", catId);
				ResponseObj responseobj=makeHttpRequest(destURL, defaultSelURL);
				response=getBackEndResponse(responseobj,info.songSelSuccessmsg);
				response=response+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
			}
		}
		return response;
	}
	public String processSpecialCallerSelRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,SiteURLDetails destURL
			,String language,boolean isPrepaid,String pageNo,String giftAllowed,String specialCaller,
			String preListen,HashMap USSDMenu,String status){
		String method="processSpecialCallerSelRequest";
		String response=null;
		String dynamicResponseString=null;
		USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
		
		if(ans!=null){
			ans=ans.trim();
		}
		logger.info("ans=="+ans);
		if(ussdInfo!=null && ussdInfo.catId!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSel") && (ans==null || ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("null"))){
				dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
				response="Enter the mobile number."+dynamicResponseString+"ansRequest";
			}else if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSel") && (ans!=null && isValidMSISDN(ans))){
				dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
				if(status!=null && status.equalsIgnoreCase("free")){
					dynamicResponseString=dynamicResponseString+info.mainMenuURL+"`*1`"+ussdInfo.URL+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
					String strIsPrepaid="y";
					if(!isPrepaid){
						strIsPrepaid="n";
					}
					String SelURL = info.urlForSelection;
					SelURL=Tools.findNReplaceAll(SelURL, "%subId%", subId);
					SelURL=Tools.findNReplaceAll(SelURL, "%ussdNo%", info.calledNo);
					SelURL=Tools.findNReplaceAll(SelURL, "%isPrepaid%", strIsPrepaid);
					SelURL=Tools.findNReplaceAll(SelURL, "%callerId%",ans);
					SelURL=Tools.findNReplaceAll(SelURL, "%clipId%", clipId);
					SelURL=Tools.findNReplaceAll(SelURL, "%catId%", catId);
					ResponseObj responseobj=makeHttpRequest(destURL, SelURL);
					response=getBackEndResponse(responseobj,info.freeSongSelSuccessmsg);
					response=response+dynamicResponseString;
				}else{
					try {
						response=info.selChargingMsg;
						dynamicResponseString=dynamicResponseString+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=specialCallerSelReconfirmation";
						response=response+dynamicResponseString;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}else if(request_value!=null && request_value.equalsIgnoreCase("specialCallerSelReconfirmation")){
				String strIsPrepaid="y";
				if(!isPrepaid){
					strIsPrepaid="n";
				}
				String SelURL = info.urlForSelection;
				SelURL=Tools.findNReplaceAll(SelURL, "%subId%", subId);
				SelURL=Tools.findNReplaceAll(SelURL, "%ussdNo%", info.calledNo);
				SelURL=Tools.findNReplaceAll(SelURL, "%isPrepaid%", strIsPrepaid);
				SelURL=Tools.findNReplaceAll(SelURL, "%callerId%",callerNo);
				SelURL=Tools.findNReplaceAll(SelURL, "%clipId%", clipId);
				SelURL=Tools.findNReplaceAll(SelURL, "%catId%", catId);
				ResponseObj responseobj=makeHttpRequest(destURL, SelURL);
				response=getBackEndResponse(responseobj,info.songSelSuccessmsg);
				response=response+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId;
			}
		}
		return response;
	}
	public String processGiftRequest(String subId,String ans,String circleId,String processId,
			String request_value,String callerNo,String catId,String clipId,String parentProcessId,SiteURLDetails destURL,
			String language,boolean isPrepaid,String pageNo,String giftAllowed,String specialCaller,
			String preListen,String isDeactive,HashMap USSDMenu){
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
		logger.info("callerNo=="+callerNo);
		if(ussdInfo!=null && ussdInfo.catId!=null){
			if(request_value!=null && request_value.equalsIgnoreCase("gift") && ((ans==null || ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("null"))&& (callerNo==null || callerNo.equalsIgnoreCase("") || callerNo.equalsIgnoreCase("null")))){
				dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive;
				response="Enter the mobile number of ur friend."+dynamicResponseString+"ansRequest";
			}else if(request_value!=null && request_value.equalsIgnoreCase("gift") && (ans!=null || callerNo!=null)){
				if(ans!=null){
					ans=ans.trim();
					pageValue=0;
				}else{
					callerNo=callerNo.trim();
					ans=callerNo;
				}
				boolean isGifteeAllowed=false;
				String giftURL = info.urlForValidatingGiftee;
				giftURL=Tools.findNReplaceAll(giftURL, "%subId%", subId);
				giftURL=Tools.findNReplaceAll(giftURL, "%giftee%", ans);
				giftURL=Tools.findNReplaceAll(giftURL, "%clipId%", clipId);
				giftURL=Tools.findNReplaceAll(giftURL, "%catId%", catId);
				ResponseObj responseObj=makeHttpRequest(destURL, giftURL);
				if(responseObj.responseStatus && responseObj.response!=null && responseObj.response.length()>0){
					String subDetailsXML=responseObj.response.toString();
					String[] arrXPath=new String[1];
					arrXPath[0]="/rbt/response";
					XMLXPathParser xpathparser=new XMLXPathParser(subDetailsXML);
					String[] xpathResponse=xpathparser.getXPathResultStringArray(arrXPath);
					//&& xpathResponse[2].equalsIgnoreCase("new_user")
					logger.info("xpathResponse=="+xpathResponse[0]);
					if(xpathResponse!=null && xpathResponse.length==1 && xpathResponse[0].equalsIgnoreCase("valid")){
						isGifteeAllowed=true;
					}
				}
				if(isGifteeAllowed){
					dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive;
					try {
						response=info.giftChargingMsg;
						response=getValidResponseString(response,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,false,info.newLineCharString,info.seperatorChar);
						dynamicResponseString=dynamicResponseString+getDynamicURLWithMoreOption(ussdInfo,pageValue)+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=gift";
						dynamicResponseString=dynamicResponseString+"`1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&catId="+catId+"&callerNo="+ans+"&request_value=giftReconfirmation";
						response=response+dynamicResponseString;
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}else{
					dynamicResponseString=info.mainMenuURL+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive;
					response="Sorry,you can't gift song to this number"+info.seperatorChar+"Enter the mobile number of ur friend"+dynamicResponseString+"ansRequest";
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
				response=getBackEndResponse(responseobj,info.songSelSuccessmsg);
				response=response+"`*1`"+ussdInfo.URL+"&isPrepaid="+isPrepaid+"&clipId="+clipId+"&isDeactive="+isDeactive;
			}
		}
		return response;
	}
	public String processMiscellaneousRequest(String subId,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		String response=null;
		try {
			int pageValue=Integer.parseInt(pageNo);
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			if(ussdInfo!=null){
				String currURL=ussdInfo.URL;
				if(currURL!=null){
					String responseString=ussdInfo.processName+info.newLineCharString+ussdInfo.responseString;
					String dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
					if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
						response=getValidResponseString(responseString,pageValue,info.moreStr, null,info.maxStrlengthlimit,false,info.newLineCharString,info.seperatorChar);
						response=response+dynamicResponseString;
					}
				}
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return response;

		}
		return response;
	}
	public String processCatSearchRequest(String subId,String circleId,String processId,
			String request_value,SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		String response=null;
		try {
			int pageValue=Integer.parseInt(pageNo);
			USSDInfo ussdInfo=getUSSDInfo(processId,circleId,USSDMenu);
			if(ussdInfo!=null){
				String currURL=ussdInfo.URL;
				if(currURL!=null){
					String responseString=ussdInfo.responseString;
					String dynamicResponseString=getDynamicURLWithMoreOption(ussdInfo,pageValue);
					if(responseString!=null && dynamicResponseString!=null && responseString.length()>0 && pageValue>=0){
						response=getValidResponseString(responseString,pageValue,info.moreStr, info.enterChoiceStr,info.maxStrlengthlimit,true,info.newLineCharString,info.seperatorChar);
						response=response+dynamicResponseString;
					}
				}
			}

		} catch (NumberFormatException e) {
			e.printStackTrace();
			return response;
		}
		return response;
	}
	public String processMSearchRequest(String subId,String ans,String circleId,String processId,String request_value,
			SiteURLDetails destURL,String pageNo,HashMap USSDMenu){
		String method="processMSearchRequest";
		String response=null;
		String msearchURL=info.mSearchURL;
		if(ans==null || ans.equalsIgnoreCase("") || ans.equalsIgnoreCase("null")){
			response=info.msearchMsg+"ansRequest";
		}else{
			String attachMsg="?userNumber="+subId+"&smsText="+ans;
			msearchURL=msearchURL+attachMsg;
			ResponseObj responseobj=makeHttpRequest(msearchURL);
			logger.info("responseobj.responseStatus=="+responseobj.responseStatus);
			logger.info("responseobj.response=="+responseobj.response);
			if(responseobj.responseStatus){
				logger.info("inside responseobj.response=="+responseobj.response);
				if(responseobj.response!=null && responseobj.response.toString().length()>0){
					String tempStr=responseobj.response.toString().trim();
					logger.info("inside responseobj.response.toString()=="+tempStr);
					logger.info("responseobj.response!=null && responseobj.response.toString().length()>0");
					if(!tempStr.equalsIgnoreCase("null") || 
							!tempStr.equalsIgnoreCase("ERROR")){
						logger.info("responseobj.response!=null || responseobj.response!=error");
						if(tempStr.indexOf(info.msearchConfirmMsg)!=-1){
							response=tempStr+"Press # to go back to the Hello Tunes Menu";
						}else{
							response=tempStr+"ansRequest";
						}
					}else{
						logger.info("responseobj.response==null || responseobj.response==error");
						response=info.strInvalidRequestMsg;
					}
				}
			}
			if(response!=null ){
				if(response.indexOf("<br/>")!=-1){
					response=Tools.findNReplace(response, "<br/>", info.newLineCharString);
				}
				if(response.indexOf("\n")!=-1){
					response=Tools.findNReplace(response, "\n", info.newLineCharString);
				}
			}else{
				response=info.strInvalidRequestMsg;
			}
			response=response+info.mainMenuURL;
		}
		return response;
	}
	public  String getBackEndResponse(ResponseObj responseobj,String successMsg){
		String response=null;
		if(responseobj.responseStatus ==true){
			if(responseobj.response!=null && responseobj.response.toString().length()>0){
				if(responseobj.response.toString().indexOf(info.backEndSuccessMsg)!=-1){
					//strSuccessMsg
					if(successMsg==null){
						response=info.strSuccessMsg+info.mainMenuURL;
					}else{
						response=successMsg+info.mainMenuURL;
					}
				}else if(responseobj.response.toString().indexOf(info.backEndInvalidReponseMsg)!=-1){
					response=info.backEndInvalidReponseMsg+info.mainMenuURL;
				}else{
					response=info.backEndTechnicalDifficultyMsg+info.mainMenuURL;
				}
			}else{
				response=info.backEndTechnicalDifficultyMsg+info.mainMenuURL;
			}
		}else{
			response=info.strTechnicalDifficultyMsg+info.mainMenuURL;
		}
		return response;
	}
	public  USSDInfo getUSSDInfo(String processId,String circleId,HashMap ussdMenu){
		String method="getUSSDInfo";
		logger.info("processId=="+processId);
		logger.info("circleId=="+circleId);
		USSDInfo ussdInfo=null;
		if(ussdMenu!=null && circleId!=null && circleId.length()>0){
			logger.info("ussdMenu!=null && circleId!=null && circleId.length()>0");
			HashMap circleBasedMap=(HashMap)ussdMenu.get(circleId);
			if(circleBasedMap!=null){
				logger.info("circleBasedMap!=null");
				ussdInfo=(USSDInfo)circleBasedMap.get(processId);
				if(ussdInfo!=null){
					logger.info("got ussdInfo with processId=="+ussdInfo.processId);
				}
			}
		}
		return ussdInfo;
	} 
	public  String getCopyNoDetails(String subId,String copyNumber,HashMap site_url_details,String defClipId,String defCatId){
		String method="getCopyNoDetails";
		String response=null;
		boolean useProxy = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_PROXY", "FALSE");
		String proxyServerPort = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROXY_SERVER_PORT", null);

		String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
		int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_GUI", 30);
		RBTDBManager rbtdbManager=RBTDBManager.init(dbURL, n);

		String responseForCopy = rbtdbManager.getSubscriberVcodeCCC(
				copyNumber, subId, useProxy, proxyServerPort,"false",null,null,null,0);
		if(responseForCopy!=null){
			responseForCopy=responseForCopy.trim();
			logger.info("USSD::response =="+responseForCopy);
			logger.info("USSD::response length =="+responseForCopy.length());
			logger.info("USSD::index of ':' in response length =="+responseForCopy.indexOf(":"));
			if (responseForCopy.equalsIgnoreCase("NOT_VALID")
					|| responseForCopy.equalsIgnoreCase("NOT_FOUND")
					|| responseForCopy.equalsIgnoreCase("ALBUM")) {
				logger.info("USSD::check == no subscriber Detail available....Oops !!!! wrong number)");
				response="Invalid Response";
			} else if (responseForCopy.equalsIgnoreCase("ERROR")) {

				logger.info("USSD::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)");
				response="Try later";
			} else {
				if (responseForCopy.equalsIgnoreCase("DEFAULT")) {
					logger.info("USSD::inside copy with reponse DEFAULT)");
					response=defClipId+":"+defCatId;
				} else if (responseForCopy.indexOf(":") > -1) {
					//rbt_009110100004785_rbt:23
					logger.info("USSD::inside copy with reponse valid vcode and catID)");
					StringTokenizer st=new StringTokenizer(responseForCopy,":");
					String wavFile=null;
					String catId=null;
					int count=0;
					wavFile = st.nextToken();
					catId=st.nextToken();
					logger.info("USSD::inside copy with wavFile=="+wavFile+"and catId=="+catId);
					DemoClip defaultClip=null;
					String vcode=null;
					StringTokenizer st2=new StringTokenizer(wavFile,"_");

					logger.info("USSD::inside copy with vcod=="+vcode);
					com.onmobile.apps.ringbacktones.cache.content.ClipMinimal clip=rbtdbManager.getClipRBT(wavFile);
					if(clip!=null ){
						if(catId!=null){
							response=clip.getClipId()+":"+catId;
						}else{
							response=clip.getClipId()+":"+defCatId;
						}
					}else{
						response=defClipId+":"+defCatId;
					}
				}
			} 
		}
		logger.info("returning with response=="+response);
		return response;
	}
	public static String getValidResponseString(String responseString,int pageValue,
			String moreStr,String enterChoiceStr,int maxStrlengthlimit,boolean numberOrderingStr,String newLineCharString,String seperatorChar){
		String method="getValidResponseString";
		logger.info("responseString=="+responseString);
		logger.info("pageValue=="+pageValue);
		logger.info("moreStr=="+moreStr);
		logger.info("enterChoiceStr=="+enterChoiceStr);
		logger.info("maxStrlegthlimit=="+maxStrlengthlimit);
		logger.info("numberOrderingStr=="+numberOrderingStr);
//		Assumption:: if "responseString" contains "." then either its length should be less than (strlegthlimit-enterChoiceStr)if it contains only one "." OR if it contains two "." then distance between two "." must be <(strlegthlimit-enterChoiceStr)
		int count=0;
		int counter=-1;
		int inputKeyConter=1;
		int indexLength=1;
		if(seperatorChar!=null && seperatorChar.length()>0){
			indexLength=indexLength+seperatorChar.length();
		}
		int moreStrLength=0;
		if(moreStr!=null && moreStr.length()>0){
			moreStrLength=moreStr.length();
		}
		int enterChoiceStrLength=0;
		if(enterChoiceStr!=null && enterChoiceStr.length()>0){
			enterChoiceStrLength=enterChoiceStr.length();
		}
		int strlegthlimit=maxStrlengthlimit-moreStrLength-enterChoiceStrLength;
		String tempResponse=responseString;
		String tempCurrStr=null;
		boolean validPageValue=false;
		boolean flag=true;
		if(!numberOrderingStr && tempResponse.length()<maxStrlengthlimit+1){
			flag=false;
			if(pageValue==0){
				validPageValue=true;
				tempCurrStr=tempResponse;
			}
		}
		if(numberOrderingStr && tempResponse.length()<(maxStrlengthlimit-enterChoiceStrLength+1)){
			flag=false;
			if(pageValue==0){
				validPageValue=true;
				tempCurrStr=tempResponse+enterChoiceStr;
			}
		}
		boolean largeInitialMsg=true;
		logger.info("largeInitialMsg=="+largeInitialMsg);
		logger.info("tempResponse=="+responseString);
		String tempStrCheckInitialMsg=responseString.substring(0, responseString.indexOf(".")+1);
		logger.info("tempStr=="+tempStrCheckInitialMsg);
		String tempCheckInitialMsg=responseString.substring(responseString.indexOf(".")+1);
		logger.info("tempCheck=="+tempCheckInitialMsg);
		if(tempCheckInitialMsg!=null && tempCheckInitialMsg.indexOf(".")!=-1){
			tempStrCheckInitialMsg=tempStrCheckInitialMsg+tempCheckInitialMsg.substring(0, tempCheckInitialMsg.indexOf(".")-1-seperatorChar.length());
		}else{
			tempStrCheckInitialMsg=tempStrCheckInitialMsg+tempCheckInitialMsg;
		}
		if(tempStrCheckInitialMsg!=null && tempStrCheckInitialMsg.length()<(maxStrlengthlimit-enterChoiceStrLength+1)){
			largeInitialMsg=false;
		}
		while(flag && ((pageValue>count-1))&& responseString.length()>0 && responseString.length()>counter){
			tempCurrStr=null;
			if(counter==-1){
				counter=0;
			}
			logger.info("largeInitialMsg=="+largeInitialMsg);
			tempResponse=responseString.substring(counter);
			if(tempResponse.indexOf(".")!=-1 && numberOrderingStr){
				if(largeInitialMsg){
					logger.info("inside largeInitialMsg=="+largeInitialMsg);
					logger.info("tempResponse=="+tempResponse);
					String tempStr=tempResponse.substring(0, tempResponse.indexOf(".")-indexLength);
					logger.info("tempStr=="+tempStr);
					if( tempStr!=null && tempStr.length()<(maxStrlengthlimit-enterChoiceStrLength+1)){
						logger.info(" tempStr!=null && tempStr.length()<(maxStrlengthlimit-enterChoiceStrLength+1)");
						tempCurrStr=tempStr;
						logger.info("tempCurrStr=="+tempCurrStr);
						largeInitialMsg=false;
						logger.info("largeInitialMsg=="+largeInitialMsg);
					}else{
						logger.info(" tempStr==null || tempStr.length()>=(maxStrlengthlimit-enterChoiceStrLength+1)");
						tempCurrStr=tempStr.substring(0, (maxStrlengthlimit-enterChoiceStrLength+1));
						String tempStrValue=tempStr.substring(maxStrlengthlimit-enterChoiceStrLength+1);
						logger.info("tempStrValue=="+tempStrValue);
						if(tempStrValue!=null && tempStrValue.length()<(maxStrlengthlimit-enterChoiceStrLength+1)){
							largeInitialMsg=false;
						}
						logger.info("tempCurrStr=="+tempCurrStr);
						logger.info("largeInitialMsg=="+largeInitialMsg);
					}
				}else{
				int tempLimit=strlegthlimit+1;
				logger.info("count=="+count+" ,pageValue=="+pageValue+",tempLimit"+tempLimit);
				while(tempCurrStr==null || ((tempCurrStr!=null && tempCurrStr.length()<tempLimit))){
					logger.info("tempResponse=="+tempResponse);
					String tempStr=tempResponse.substring(0, tempResponse.indexOf(".")+1);
					logger.info("tempStr=="+tempStr);
					String temp=tempResponse.substring(tempResponse.indexOf(".")+1);
					logger.info("temp=="+temp);
					boolean jumpOut=false;
					++inputKeyConter;
					logger.info("inputKeyConter=="+inputKeyConter);
					logger.info("indexLength=="+indexLength);
					indexLength=getLatestIndexLength(inputKeyConter,indexLength);
					logger.info("indexLength=="+indexLength);
					if(temp!=null && temp.indexOf(".")!=-1){
						tempStr=tempStr+temp.substring(0, temp.indexOf(".")-indexLength);
					}else{
						tempStr=tempStr+temp;
						jumpOut=true;
					}
					logger.info("tempStr=="+tempStr);
					if(tempCurrStr==null && tempStr!=null && tempStr.length()<tempLimit && tempResponse!=null){
						tempCurrStr=tempStr;
						logger.info("tempCurrStr==*****null");
						logger.info("tempCurrStr=="+tempCurrStr);
						if(tempResponse!=null && tempResponse.indexOf(".")!=-1){
							logger.info("tempResponse=="+tempResponse);
							tempResponse=tempResponse.substring(tempResponse.indexOf(".")+1);
							logger.info("tempResponse=="+tempResponse);
						}
						if(tempResponse!=null && tempResponse.indexOf(".")!=-1){
							logger.info("tempResponse=="+tempResponse);
							tempResponse=tempResponse.substring(tempResponse.indexOf(".")-indexLength);
							logger.info("tempResponse=="+tempResponse);
						}
						if(jumpOut){
							break;
						}
					}else if(tempStr!=null && tempCurrStr!=null && (tempStr.length()+tempCurrStr.length()<tempLimit && tempResponse!=null)){
						tempCurrStr=tempCurrStr+tempStr;
						logger.info("tempCurrStr!=null");
						logger.info("tempCurrStr=="+tempCurrStr);
						if(tempResponse!=null && tempResponse.indexOf(".")!=-1){
							logger.info("tempResponse=="+tempResponse);
							tempResponse=tempResponse.substring(tempResponse.indexOf(".")+1);
							logger.info("tempResponse=="+tempResponse);
						}
						if(tempResponse!=null && tempResponse.indexOf(".")!=-1){
							logger.info("tempResponse=="+tempResponse);
							tempResponse=tempResponse.substring(tempResponse.indexOf(".")-indexLength);
							logger.info("tempResponse=="+tempResponse);
						}
						if(jumpOut){
							break;
						}
					}else{
						break;
					}
				}
			}
			}else{
				if(tempResponse.length()<maxStrlengthlimit+1){
					tempCurrStr=tempResponse;
				}else{
					tempCurrStr=tempResponse.substring(0, maxStrlengthlimit+1);
				}
			}
			logger.info("tempCurrStr=="+tempCurrStr);
			if(tempCurrStr!=null){
				counter=counter+tempCurrStr.length();
				logger.info("counter=="+counter);
			}else{
				break;
			}
			logger.info("count=="+count);
			if(pageValue!=count && (responseString.length()<counter+1)){
				validPageValue=false;
				logger.info("validPageValue==8888888"+validPageValue);
			}else if(pageValue==count){
				validPageValue=true;
				logger.info("validPageValue==******"+validPageValue);
			}
			count++;
		}
		logger.info("counter==******"+counter);
		logger.info("strlen==******"+responseString.length());
		if( tempCurrStr!=null && validPageValue && flag){
			if(numberOrderingStr){
				if((responseString!=null && responseString.length()>tempCurrStr.length()) && moreStr!=null && ((responseString.length()-counter)<moreStrLength+enterChoiceStrLength+1)){
					tempCurrStr=tempCurrStr+responseString.substring(counter)+newLineCharString+enterChoiceStr;
				}else{
					if(moreStr!=null && responseString.length()>counter){
						tempCurrStr=tempCurrStr+newLineCharString+moreStr;
					}if(enterChoiceStr!=null ){
						tempCurrStr=tempCurrStr+newLineCharString+enterChoiceStr;
					}
				}
			}else{
				if((responseString!=null && responseString.length()>tempCurrStr.length()) && ((responseString.length()-counter)<(moreStrLength+1))){
					tempCurrStr=tempCurrStr+responseString.substring(counter);
				}else if(moreStr!=null  && responseString.length()>counter){
					tempCurrStr=tempCurrStr+newLineCharString+moreStr;
				}
			}
		}
		if(tempCurrStr!=null && tempCurrStr.length()>0 && validPageValue){
			logger.info("tempCurrStr=="+tempCurrStr);
			return tempCurrStr;
		}else{
			logger.info("tempCurrStr==null");
			return null;
		}
	}
	public static int getLatestIndexLength(int inputKeyConter,int indexLength){
		String method="getLatestIndexLength";
		if(inputKeyConter==10 || inputKeyConter==100 || inputKeyConter==1000 ||inputKeyConter==10000){
			++indexLength;
		}
		logger.info("returning with indexLength=="+indexLength);
		return indexLength;
	}
	public static String conevrtNumberedInputToDefaultInput(String response,String seperatorChar){
		String returnString="";
		String method="conevrtNumberedInputToDefaultInput";
		int keyLength=3;
		int keyIndex=3;
		logger.info("keyLength=="+keyLength);
		StringTokenizer st=new StringTokenizer(response,".");
		while(st.hasMoreTokens()){
			String tempResponse=st.nextToken();
			logger.info("token=="+tempResponse);
			keyLength=3;
			keyIndex=3;
			int tempKeyValue=-1;
			int tempLength=tempResponse.length();
			if(tempLength==1){
				keyLength=1;
			}else if(tempLength==2){
				keyLength=2;
			}
			String steTempKeyValue=tempResponse.substring(tempResponse.length()-(keyLength));
			logger.info("steTempKeyValue=="+steTempKeyValue);
			try {
				tempKeyValue=Integer.parseInt(steTempKeyValue);
				logger.info("tempKeyValue=="+tempKeyValue);
			} catch (NumberFormatException e) {
				tempKeyValue=-1;
				--keyLength;
				if(keyLength>0){
				// TODO Auto-generated catch block
				logger.info("tempKeyValue=="+tempKeyValue);
				steTempKeyValue=tempResponse.substring(tempResponse.length()-(keyLength));
				logger.info("steTempKeyValue=="+steTempKeyValue);
				try {
					tempKeyValue=Integer.parseInt(steTempKeyValue);
					logger.info("tempKeyValue=="+tempKeyValue);
				} catch (NumberFormatException e1) {
					--keyLength;
					logger.info("keyLength=="+keyLength);
					tempKeyValue=-1;
					if(keyLength>0){
					// TODO Auto-generated catch block
					logger.info("tempKeyValue=="+tempKeyValue);
					steTempKeyValue=tempResponse.substring(tempResponse.length()-(keyLength));
					logger.info("steTempKeyValue=="+steTempKeyValue);
					try {
						tempKeyValue=Integer.parseInt(steTempKeyValue);
						logger.info("tempKeyValue=="+tempKeyValue);
						logger.info("keyLength=="+keyLength);
					} catch (NumberFormatException e2) {
						// TODO Auto-generated catch block
						tempKeyValue=-1;
						logger.info("tempKeyValue=="+tempKeyValue);
						e2.printStackTrace();
					}
					e1.printStackTrace();
					}
				}
				e.printStackTrace();
			}
			}
			logger.info("keyLength=="+keyLength);
			if(tempKeyValue!=-1 && tempKeyValue!=0 && !tempResponse.substring(tempResponse.length()-2).equalsIgnoreCase("*1") && !tempResponse.substring(tempResponse.length()-1).equalsIgnoreCase("*") && !tempResponse.substring(tempResponse.length()-1).equalsIgnoreCase("#")){
				returnString=returnString+tempResponse.substring(0, tempResponse.length()-keyLength-seperatorChar.length());
				logger.info("returnString=="+returnString);
			}else{
				logger.info("**********");
				returnString=returnString+tempResponse;
				logger.info("returnString=="+returnString);
			}
		}
		return returnString;
	}
	public  String getDynamicURLWithMoreOption(USSDInfo ussdInfo,int pageValue){
		String method="getDynamicURLWithMoreOption";
		String currURL=ussdInfo.URL;
		String dynamicResponseString=null;
		if(currURL!=null){
			logger.info("currURL!=null");
			if(currURL.substring(0, currURL.indexOf("pageNo")).length()+7==currURL.length()){
				currURL=currURL.substring(0,currURL.length())+(++pageValue);
			}else{
				currURL=currURL.substring(0, currURL.indexOf("pageNo")+7)+(++pageValue)+currURL.substring(currURL.indexOf("pageNo")+8);
			}
			String moreResponse="`"+info.seperatorChar+"0`"+currURL;
			dynamicResponseString=ussdInfo.dynamicURLResponseString+moreResponse;
			logger.info("dynamicResponseString=="+dynamicResponseString);
		}
		logger.info("dynamicResponseString=="+dynamicResponseString);
		return dynamicResponseString;
	}
	public static ResponseObj makeHttpRequest(SiteURLDetails destUrlDetail,String attachMsg){
		String method="makeHttpRequest";
		StringBuffer responseString=new StringBuffer();
		String strURL=destUrlDetail.URL+attachMsg;
		Integer statusCode=new Integer("0");
		boolean useProxy=destUrlDetail.use_proxy;
		String proxyHost=destUrlDetail.proxy_host;
		int proxyPort=destUrlDetail.proxy_port;
		int connectionTimeOut=destUrlDetail.connection_time_out;
		int timeOut=destUrlDetail.time_out;
		boolean responseStatus=Tools.callURL(strURL,statusCode, responseString, useProxy, proxyHost, proxyPort,true);
		logger.info("inside login:)_)_?==responseStatus=="+responseStatus+"and responseString=="+responseString.toString());
		return (new ResponseObj(responseStatus,responseString));
	}
	public static ResponseObj makeHttpRequest(String destURLWithAttachedMsg){
		String method="makeHttpRequest";
		StringBuffer responseString=new StringBuffer();
		String strURL=destURLWithAttachedMsg;
		Integer statusCode=new Integer("0");
		boolean useProxy=false;
		String proxyHost=null;
		int proxyPort=0;
		int connectionTimeOut=6000;
		int timeOut=6000;
		boolean responseStatus=Tools.callURL(strURL,statusCode, responseString, useProxy, proxyHost, proxyPort,true);
		logger.info("inside login:)_)_?==responseStatus=="+responseStatus+"and responseString=="+responseString.toString());
		return (new ResponseObj(responseStatus,responseString));
	}
	public boolean isSubscriberAllowedForUSSD(String subId,SiteURLDetails destURL){
		String method="isSubscriberAllowedForUSSD";
		logger.info("subId=="+subId);
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
		logger.info("returning with isSubscriberAllowedForUSSD=="+returnFlag);
		return returnFlag;
	}
	public USSDSubDetails getUSSDSubDetails(String subId,SiteURLDetails destURL){
		String method="getUSSDSubDetails";
		logger.info("subId=="+subId);
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
						logger.info("isPrepaid=="+xpathResponse[6]);
					}
					if(xpathResponse!=null && xpathResponse.length==7  && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[1].equalsIgnoreCase("y")){
						if(xpathResponse[2].equalsIgnoreCase("active")){
							subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],true,isPrepaid,xpathResponse[5],false);
//							String subClass,String userType,String status,boolean active,boolean isPrepaid,String language
						}else if(xpathResponse[2].equalsIgnoreCase("deactive")|| xpathResponse[2].equalsIgnoreCase("new_user")){
							subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],false,isPrepaid,xpathResponse[5],true);
						}else{
							subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],false,isPrepaid,xpathResponse[5],false);
						}
						logger.info("subscription_class=="+xpathResponse[4]);
						logger.info("user_type=="+xpathResponse[3]);
						logger.info("status=="+xpathResponse[2]);
						logger.info("subActive==true");
						logger.info("isPrepaid=="+isPrepaid);
						logger.info("language=="+xpathResponse[5]);
//						subDet=new USSDSubDetails(xpathResponse[4],xpathResponse[3],xpathResponse[2],true,isPrepaid,xpathResponse[5]);
////						String subClass,String userType,String status,boolean active,boolean isPrepaid,String language
//						}
					}
				}
			}
		}
		return subDet;
	}
	public String isSubscriberDeactivated(String subId,SiteURLDetails destURL){

		String returnFlag="false";
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
					if(xpathResponse!=null && xpathResponse.length==3 && xpathResponse[1].equalsIgnoreCase("y") && xpathResponse[0].equalsIgnoreCase("success") && (xpathResponse[2].equalsIgnoreCase("deactive")|| xpathResponse[2].equalsIgnoreCase("new_user"))){
						returnFlag="true";
					}
				}
			}
		}
		return returnFlag;
	
	}
	public boolean isSubscriberActivated(String subId,SiteURLDetails destURL){
		String method="isSubscriberActivated";
		logger.info("subId=="+subId);
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
					if(xpathResponse!=null && xpathResponse.length==3 && xpathResponse[1].equalsIgnoreCase("y") && xpathResponse[0].equalsIgnoreCase("success") && xpathResponse[2].equalsIgnoreCase("active")&& xpathResponse[2].equalsIgnoreCase("act_pending")){
						returnFlag=true;
					}
				}
			}
		}
		logger.info("returning with isSubActivated=="+returnFlag);
		return returnFlag;
	}
	public String getSubscriptionPack(String subId,SiteURLDetails destURL){
		String method="getSubscriptionPack";
		logger.info("subId=="+subId);
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
		logger.info("returning with subPack=="+returnFlag);
		return returnFlag;
	}
	public static HashMap<String , String> getAttributesMap(Element element)
    {
		String method="getAttributesMap";
          HashMap<String, String> attributeMap = new HashMap<String, String>();

          NamedNodeMap namedNodeMap = element.getAttributes();
          for (int i = 0; i < namedNodeMap.getLength(); i++)
          {
               Attr attr = (Attr) namedNodeMap.item(i);
               attributeMap.put(attr.getName(), attr.getValue());
               logger.info("key=="+attr.getName());
               logger.info("value=="+attr.getValue());
          }

          return attributeMap;
    }
public static List<HashMap<String, String>> populateContentList(Node node){
	String method="showNodeContent";
	
NodeList nodelist=	node.getChildNodes();
List<HashMap<String, String>> contentsList = new ArrayList<HashMap<String, String>>();
if(nodelist!=null){
	for(int count=0;count<nodelist.getLength();count++){
		Element tempNode=(Element)nodelist.item(count);
		if(tempNode.getTagName().equalsIgnoreCase("CONTENT")){
		 HashMap<String, String> propertiesMap = getAttributesMap(tempNode);
         propertiesMap.putAll(getPropertiesMap(tempNode));
         contentsList.add(propertiesMap);
		}
	}
}
return contentsList;
}
//    public static List<HashMap<String, String>> getContentsList(Element element)
//    {
//          List<HashMap<String, String>> contentsList = new ArrayList<HashMap<String, String>>();
//          
//          NodeList contentNodeList = element.getElementsByTagName("CONTENT");
//
//          for (int i = 0; i < contentNodeList.getLength(); i++)
//          {
//               Element contentElement = (Element) contentNodeList.item(i);
//               HashMap<String, String> propertiesMap = getAttributesMap(contentElement);
//               propertiesMap.putAll(getPropertiesMap(contentElement));
//               contentsList.add(propertiesMap);
//          }
//
//          return contentsList;
//    }

    public static HashMap<String, String> getPropertiesMap(Element element)
    {
    	String method="getPropertiesMap";
          HashMap<String, String> propertiesMap = new HashMap<String, String>();

          NodeList propertyNodeList = element.getElementsByTagName("property");
          for (int i = 0; i < propertyNodeList.getLength(); i++)
          {
               Element propertyElement = (Element) propertyNodeList.item(i);
               String key = propertyElement.getAttribute("name");
               logger.info("key=="+key);
               String value = propertyElement.getAttribute("value");
               logger.info("value=="+value);

               propertiesMap.put(key, value);
          }

          return propertiesMap;
    }

}