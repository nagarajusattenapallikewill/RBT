package com.onmobile.apps.ringbacktones.ussd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;
import com.onmobile.apps.ringbacktones.servlets.SiteURLDetails;

public class USSDRequestHandler extends HttpServlet{
	/*author@Abhinav Anand
	 */
	private static Logger logger = Logger.getLogger(USSDRequestHandler.class);
	
	protected  static RBTDBManager rbtDBManager=null;
	private static final long serialVersionUID = 1L;
	private  USSDController ussdController=null;
	private static String m_class="USSDRequestHandler"; 
	

	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		String method="doGet";
		logger.info("entering");

		ServletContext context=getServletContext();
		ussdController=USSDServletListner.ussdController;
		if(ussdController!=null){
			USSDBasicFeatures features=ussdController.getUssdFeatures();
			if(ussdController!=null && features!=null){			
				logger.info("ussdController!=null && features!=null");
				Enumeration enumValues=request.getParameterNames();
				HashMap parametersMap=getRequestMap(enumValues,request,ussdController.getInfo().getResponseKeyWithMissingSeperator());
				if(parametersMap!=null && parametersMap.size()>0){
					String requestSource=((String)parametersMap.get("source")); 
					logger.info("USSD::entering request coming for=="+requestSource);
					String subId=(String)parametersMap.get("msisdn");
					if(rbtDBManager==null){
						String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_USSD", null);
						rbtDBManager=RBTDBManager.init(dbURL, 4);
					}
					subId=subId(subId,ussdController.getInfo().getCountryPrefix());
					parametersMap.put("msisdn", subId);
					logger.info("USSD::subId=="+subId);
					String processId=(String)parametersMap.get("processId");
					String pageNo=(String)parametersMap.get("pageNo");
					String request_type=(String)parametersMap.get("request_type");
					String request_value=(String)parametersMap.get("request_value");
					logger.info("USSD::processId=="+processId);
					logger.info("USSD::pageNo=="+pageNo);
					logger.info("USSD::request_type=="+request_type);
					logger.info("USSD::request_value=="+request_value);
					String responseStr=null;
					String circleId=getCircleId(subId,context);
					SiteURLDetails destURL=getDestURL(context,subId);
					ArrayList displaySubPack=null;
					displaySubPack=(ArrayList)context.getAttribute("DISPLAY_SUB_PACK");
					ArrayList subPackTag=null;
					subPackTag=(ArrayList)context.getAttribute("SUB_PACK_TAG");
					logger.info("USSD::circleId=="+circleId);
					if(destURL!=null){
						logger.info("USSD::destURL=="+destURL.URL);
					}
					//added
					parametersMap.put("USSDCHARGECLASS_MESSAGE", (HashMap)context.getAttribute("USSDCHARGECLASS_MESSAGE"));

					String defCatId=null;
					defCatId=(String)context.getAttribute("DEFAULT_CAT_ID");
					Clips defClips=null;
					defClips=(Clips)context.getAttribute("DEFAULT_CLIP");
					parametersMap.put("DEST_URL", destURL);
					parametersMap.put("CIRCLE_ID", circleId);
					parametersMap.put("DEFAULT_CLIP", defClips);
					parametersMap.put("DISPLAY_SUB_PACK", displaySubPack);
					parametersMap.put("SUB_PACK_TAG", subPackTag);
					parametersMap.put("DEFAULT_CAT_ID", defCatId);
//					parametersMap.put("SITE_URL_DETAILS", (HashMap)(context.getAttribute("SITE_URL_MAP")));
//					parametersMap.put("LOCAL_URL_DETAILS",(SiteURLDetails)(context.getAttribute("LOCAL_URL")));
					//HashMap site_url_details=
					//SiteURLDetails local
					if(processId!=null && pageNo!=null && subId!=null && (request_type!=null || processId.equalsIgnoreCase("0")) && destURL!=null && circleId!=null){
						logger.info("USSD::going to call processRequest in Controller");
						System.out.println("USSD::going to call processRequest in Controller");
						responseStr=ussdController.processRequest( parametersMap);
						System.out.println("USSD::came back from processRequest in Controller with response=="+responseStr);
//						logger.info("USSD::returned from Controller with responseStr"+responseStr);
						sendReponse(responseStr,response);
					}else{
						sendReponse("Invalid Response",response);
						logger.info("parameters are not valid"); 

					}
				}else{
					sendReponse("Invalid Response",response);
					logger.info("parametersMap==null || parametersMap.size()=<0"); 

				}
			}
			else{
				sendReponse("Invalid Response",response);
				logger.info("Technical difficulty:: ussdController Class is null OR ussdController.ussdFeatures is null"); 
			}
		}

	}
	public HashMap getRequestMap(Enumeration enumValues,HttpServletRequest request,String tempResponseKeyWithMissingSeperator){
		String method="getRequestMap";
		logger.info("entering");
		ArrayList responseKeyWithMissingSeperator=getResponseKeysWithMissingSeperator(tempResponseKeyWithMissingSeperator);
		
		HashMap returnMap=new HashMap();
		while(enumValues.hasMoreElements()){
			String tempKey=(String)enumValues.nextElement();
			logger.info("tempKey=="+tempKey);
			String value=request.getParameter(tempKey);
			logger.info("value=="+value);
		boolean flag=true;
			if(responseKeyWithMissingSeperator!=null && responseKeyWithMissingSeperator.size()>0 && responseKeyWithMissingSeperator.contains(tempKey)){
				logger.info("responseKeyWithMissingSeperator!=null && responseKeyWithMissingSeperator.size()>0 && responseKeyWithMissingSeperator.contains(tempKey)");
				//assumptions---all suck response keys are together and only any of these keys is not preceded with '&'
				for(int count=0;count<responseKeyWithMissingSeperator.size();count++){
					String temp=(String)responseKeyWithMissingSeperator.get(count);
					logger.info("populating returnMap with temp=="+temp);
					if(value.indexOf(temp)!=-1){
						flag=false;
						logger.info("value.indexOf(temp)!=-1");
						String tempValue=value.substring(0, value.indexOf(temp));
						logger.info("tempValue=="+tempValue);
						logger.info("populating returnMap with key=="+tempKey+"value=="+tempValue);
						returnMap.put(tempKey, tempValue);
						String nextKey=value.substring(value.indexOf(temp));
						logger.info("nextKey=="+nextKey);
						StringTokenizer st=new StringTokenizer(nextKey,"=");
						int index=0;
						while(st.hasMoreTokens()){
							logger.info("index=="+index);
							String tempNextValue=st.nextToken();
							logger.info("tempNextValue=="+tempNextValue);
							if(index==1){
								logger.info("populating returnMap with key=="+temp+"value=="+tempNextValue);
								returnMap.put(temp, tempNextValue);
							}
							index++;
						}
						count=responseKeyWithMissingSeperator.size();
					}
				}
				if(flag){
					logger.info("populating returnMap with key=="+tempKey+"value=="+value);
					returnMap.put(tempKey, value);
				}
			}else{
			returnMap.put(tempKey, value);
			}
		}
		if(returnMap!=null && returnMap.size()>0){
			logger.info("returnMap!=null && returnMap.size()>0");
			String subId=(String)returnMap.get("msisdn");
			logger.info("USSD::subId=="+subId);
			return returnMap;
		}else{
			return null;
		}
	}
	public void sendReponse(String responseStr,HttpServletResponse response){
		String method="sendReponse";
		boolean toEndSession=false;
		logger.info("entering");
		//"Try later"
//		logger.info("sent response, responseStr=="+responseStr);
		if(responseStr!=null){
			responseStr=responseStr.trim();
		}
		if( responseStr==null || responseStr.equalsIgnoreCase("null") || responseStr.equalsIgnoreCase("NULL") || responseStr.equalsIgnoreCase("Null") || responseStr.equalsIgnoreCase("")){
			logger.info("got responseStr=="+responseStr);
			responseStr="Invalid Response";
		}
		if(responseStr!=null && (responseStr.indexOf("Invalid Response")!=-1 || responseStr.indexOf("Invalid response")!=-1 || responseStr.indexOf("invalid response")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeNotification);
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}
		}else if(responseStr!=null && (responseStr.indexOf("Cerere Invalida")!=-1 || responseStr.indexOf("cerere invalida")!=-1 || responseStr.indexOf("cerere invalida")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeNotification);
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}
		}else if(responseStr!=null && (responseStr.indexOf("error")!=-1 || responseStr.indexOf("Error")!=-1 || responseStr.indexOf("ERROR")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeAnswer);
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}
			responseStr=responseStr.toLowerCase();
			responseStr=Tools.findNReplace(responseStr, "error", "Invalid response");
		}else if(responseStr!=null && (responseStr.indexOf("Eroare")!=-1 || responseStr.indexOf("eroare")!=-1 || responseStr.indexOf("EROARE")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeAnswer);
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}
			responseStr=responseStr.toLowerCase();
			responseStr=Tools.findNReplace(responseStr, "Eroare", "Cerere Invalida");
		}else if(responseStr!=null && (responseStr.indexOf("Try later")!=-1 || responseStr.indexOf("Try Later")!=-1 || responseStr.indexOf("try later")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeAnswer);
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}
		}else if(responseStr!=null && (responseStr.indexOf("incerci mai tarziu")!=-1 || responseStr.indexOf("Incerci mai tarziu")!=-1 || responseStr.indexOf("Incerci Mai Tarziu")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeAnswer);
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}
		}else if(responseStr!=null && (responseStr.indexOf("ansRequest")!=-1 || responseStr.indexOf("ansrequest")!=-1 || responseStr.indexOf("AnsRequest")!=-1) && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeAnswer!=null){
			response.setContentType(ussdController.info.contentTypeAnswer);
			responseStr=responseStr.substring(0, responseStr.indexOf("ansRequest"));
			if(responseStr.indexOf("`")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("`"));
			}
			if(responseStr.indexOf("#")!=-1){
				responseStr=responseStr.substring(0, responseStr.indexOf("#")-1);
			}

			logger.info("content type=="+ussdController.info.contentTypeAnswer);
		}else if(responseStr!=null && responseStr.equalsIgnoreCase("EXIT")){
			logger.info("Ending session");
			responseStr=null;
			toEndSession=true;
		}
		else if(ussdController!=null && ussdController.info!=null && ussdController.info.contentType!=null){
			response.setContentType(ussdController.info.contentType);
			System.out.println("content type=="+ussdController.info.contentType);
			logger.info("content type=="+ussdController.info.contentType);
		}
//		logger.info("sent response, responseStr=="+responseStr);
//		System.out.println("responseStr=="+responseStr);
		
		if(responseStr!=null && responseStr.length()>0 && !responseStr.equalsIgnoreCase("null") && responseStr.indexOf("Invalid Request")==-1 && responseStr.indexOf("Invalid request")==-1){
			try {
				PrintWriter out=response.getWriter();
				System.out.println("sending response=="+responseStr);
//				logger.info("sent response, responseStr=="+responseStr);
				out.println(responseStr);
				logger.info("sent response, responseStr=="+responseStr);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(!toEndSession && ussdController!=null && ussdController.info!=null && ussdController.info.contentTypeNotification!=null){
			response.setContentType(ussdController.info.contentTypeNotification);
			logger.info("content type=="+ussdController.info.contentTypeAnswer);
			responseStr="Invalid response"; 
			try {
				PrintWriter out=response.getWriter();
				System.out.println("sending response=="+responseStr);
//				logger.info("sent response, responseStr=="+responseStr);
				out.println(responseStr);
				logger.info("sent response, responseStr=="+responseStr);
				out.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public  SiteURLDetails getDestURL(ServletContext sc,String subId){
		String method="getDestURL";
		SiteURLDetails destURLDetail=null;

		if(rbtDBManager==null){
			String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_USSD", null);
			int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_USSD", 30);
			rbtDBManager=RBTDBManager.init(dbURL, n);
		}
		if(rbtDBManager==null){
//			System.out.println("could nt initialize DBManager");
			return null;
		}
		boolean check=false;
		check=rbtDBManager.isValidPrefix(subId);
		if(check==false){
//			Tools.logDetail(m_class, method, 
//			"RBT::check == false"+ check); 
//			System.out.println("check==false");
			
			
//			Tools.logDetail(m_class, method, 
//			"circleId string is=="+circleId);
//			Prefix prefix = rbtDBManager.getURL(subId.substring(0, 4));	
			SitePrefix prefix = Utility.getPrefix(Utility.trimCountryPrefix(subId));	
			
			if(prefix!= null && prefix.getSiteUrl()!= null) {
				String circleId = prefix.getCircleID();
				logger.info("circle id=="+circleId);
				HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
				destURLDetail=(SiteURLDetails)(site_url_details.get(circleId));
//				Tools.logDetail(m_class, method, 
//				"RBT::check == false"+ check+" and url is not null"); 
				return destURLDetail;
			}
			else{
				return null;
			}
		}
		else{
			//HashMap site_url_details=
			//SiteURLDetails local
//			Tools.logDetail(m_class, method, 
//			"RBT::check == true");
//			System.out.println("check==true");
//			System.out.println("circle id==local");
			SiteURLDetails local=(SiteURLDetails)(sc.getAttribute("LOCAL_URL"));
			return local;
		}
	}
	public String getCircleId(String subId,ServletContext sc){
		String method="getCircleId";
		logger.info("RBT::entering with subId=="+subId);
		String returnString=null;
		if(rbtDBManager==null){
			String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_USSD", null);
			int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_USSD", 30);
			rbtDBManager=RBTDBManager.init(dbURL, n);
		}

		boolean check=rbtDBManager.isValidPrefix(subId);
		logger.info("RBT::check=="+check);
		if(!check){
			logger.info("RBT::entering with subId=="+subId);
			SitePrefix prefix = Utility.getPrefix(Utility.trimCountryPrefix(subId));	
			if(prefix!=null){
				returnString = prefix.getCircleID();
			}else{
				SiteURLDetails local=(SiteURLDetails)(sc.getAttribute("LOCAL_URL"));
				returnString=local.circle_id;
			}
			
		}else{
			SiteURLDetails local=(SiteURLDetails)(sc.getAttribute("LOCAL_URL"));
			returnString=local.circle_id;
		}
		logger.info("RBT::returning with circleId=="+returnString);
		return returnString;
	}
	public static String subId(String subscriberID,String countryPrefix){
		String method="subId";
//		logger.info("subscriberID=="+subscriberID);
//		logger.info("countryPrefix=="+countryPrefix);
		if (subscriberID != null) {
			subscriberID=subscriberID.trim();
			try {
				if(countryPrefix == null || countryPrefix.trim().equals("")) 
                    countryPrefix = "91"; 
				if (countryPrefix != null)
				{
					StringTokenizer stk = new StringTokenizer(countryPrefix,
					",");
					while (stk.hasMoreTokens()) {
						String token = stk.nextToken();
//						logger.info("token=="+token);
//						logger.info("token length=="+token.length());
//						logger.info("subscriberID.length() =="+subscriberID.length());
//						logger.info("subscriberID.startsWith(token) =="+subscriberID.startsWith(token));
						if (subscriberID.startsWith("00")){
							subscriberID = subscriberID.substring(2);
//							logger.info("subscriberID=="+subscriberID);
						}
						if (subscriberID.startsWith("+")
								|| subscriberID.startsWith("0")|| subscriberID.startsWith("-")){
							subscriberID = subscriberID.substring(1);
//							logger.info("subscriberID=="+subscriberID);
						}
						 if (subscriberID.startsWith(token) && (subscriberID.length() >= (10+ token.length()))){
							subscriberID = subscriberID.substring(token.length());
//							logger.info("subscriberID=="+subscriberID);
							break;
						}
					}
				}
			}
			finally {
				if (subscriberID.startsWith("00")){
					subscriberID = subscriberID.substring(2);
//					logger.info("subscriberID=="+subscriberID);
				}
				if (subscriberID.startsWith("+")
						|| subscriberID.startsWith("0")|| subscriberID.startsWith("-")){
					subscriberID = subscriberID.substring(1);
//					logger.info("subscriberID=="+subscriberID);
				}
			}
		}
		logger.info("returning with subscriberID=="+subscriberID);
		return subscriberID;
	}
	public ArrayList getResponseKeysWithMissingSeperator(String responseKeyWithMissingSeperator){
		ArrayList responseArrayWithMissingSeperator=null;
		String strResponse=responseKeyWithMissingSeperator;
		StringTokenizer st=new StringTokenizer(strResponse,",");
		while(st.hasMoreElements()){
			String temp=st.nextToken();
			if(temp!=null && responseArrayWithMissingSeperator==null){
				responseArrayWithMissingSeperator=new ArrayList();
				
			}
			if(temp!=null){
				responseArrayWithMissingSeperator.add(temp);
			}
		}
		return responseArrayWithMissingSeperator;
	}
}
