package com.onmobile.apps.ringbacktones.servlets;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.RBTLogin;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.subscriptions.ClipCacher;
import com.onmobile.apps.ringbacktones.subscriptions.ClipGui;
import com.onmobile.apps.ringbacktones.content.Subscriber;



public class login extends HttpServlet{
	
	private static Logger logger = Logger.getLogger(login.class);
	
	public static String subMgrUrlForDeactivationDaysLeft=null;
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		String subscriberID=null;
		HttpSession session=request.getSession(true);
		Enumeration enum1=request.getHeaderNames();
		while(enum1.hasMoreElements()){
			String temp=enum1.nextElement().toString();
			System.out.println("enum name is=="+temp);
			String temp1=request.getHeader(temp);
			System.out.println("enum value is=="+temp1);
		}
		
		if(session.isNew()){
			System.out.println("this is new session with sessionId=="+(String)session.getId());
		}
		else{
			System.out.println("this is old session with sessionId=="+(String)session.getId());
		}
		session.getId();
		ServletContext context=getServletContext();
		String requestSource=((String)request.getParameter("source")); 
		logger.info("RBT::entering sourceParameter =="+requestSource); 
		System.out.println("****************source==="+requestSource);
		if (requestSource!=null) {
			if (requestSource.equalsIgnoreCase("login")) {

				responseToReuqestStatusLogin(request, response, session,
						context);

			} else if (requestSource.equalsIgnoreCase("logout")) {
				  UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
                   if(userDetails!=null){
                           CCCAccountingManager.addToCCCGUIAccounting(userDetails,"null","LOGGING OUT ", "SUCCESS",true);

                   }
				session.invalidate();
				try {

					RequestDispatcher view = request
					.getRequestDispatcher("index.jsp");
					view.forward(request, response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if(requestSource.equalsIgnoreCase("crm")){
				responseToReuqestStatusCRM(request, response, session,
						context);
			}else if(requestSource.equalsIgnoreCase("buyingHistory")){
				responseToReuqestStatusBuyingHistory(request, response, session,
						context);
			}else {
				responseToReuqestStatusStart(request, response, session,
						context);
			}
		}
		else{

			session.invalidate();
			try {
				RequestDispatcher view = request
				.getRequestDispatcher("index.jsp");
				view.forward(request, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	private void responseToReuqestStatusBuyingHistory(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		
	}
	private void responseToReuqestStatusCRM(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		System.out.println("inside CRMMMMM**********************");
	
		String defaultUsername=(String)sc.getAttribute("DEFAULT_USER");
		String defaultPassword=(String)sc.getAttribute("DEFAULT_PASSWORD");
		
		String msisdn=(String)request.getParameter("msisdn");
		String wstid=null;
		wstid=(String)request.getParameter("wstid");
         String udid=null;
         udid=(String)request.getParameter("udid");
         String opParam=null;
         opParam=(String)request.getParameter("opParam");
		System.out.println("*************msisdn"+msisdn);
		 System.out.println("*************wstid"+wstid);
          System.out.println("*************udid"+udid);
          System.out.println("*************opParam"+opParam);
          
               session.setAttribute("USER_DETAILS", new UserDetails(udid,wstid,wstid,defaultUsername));
               logger.info("RBT::going to track the user with udid=="+udid+",wstid="+wstid+",opParam="+opParam+",userName=="+defaultUsername);
       
          
		session.setAttribute("BASE_USER", "TRUE");
		session.setAttribute("Entry_PWD", defaultPassword);
		session.setAttribute("PWD", defaultPassword);
		session.setAttribute("Super_PWD",defaultPassword);
		session.setAttribute("VALIDATE", "true");
		session.setAttribute("USER_NAME",defaultUsername);
		session.setAttribute("UserName", defaultUsername);
		session.setAttribute("READ_ONLY_USER", "FALSE");
		session.setAttribute("msisdnTemp", msisdn);
		session.setAttribute("ASK_PASSWD", "false");
//		String[] displaymenu = (String[]) session.getAttribute("MENU_DISPLAY");
//		String destLink = null;

//		destLink = displaymenu[1];
//		destLink = destLink.substring(5);
//		destLink="/ccc/"+destLink;
		System.out.println("set all d attributes...nw ready to dipatch in login.do");
		String destLink=null;
		if(msisdn!=null){
			//destLink="/subscriber/timepass1.jsp";
			//;jsessionid="+(String)session.getId()
			destLink="/login.do?mode_subscriber=subs&sCategory=subscriber&source=search&msisdn="+msisdn;
		}else{
			//destLink="/subscriber/timepass1.jsp;jsessionid="+(String)session.getId();
			destLink="subscriber_info.jsp";
		}
		//response.encodeURL(destLink);
		//destLink=encodeURL(destLink,session);
		RequestDispatcher view=request.getRequestDispatcher(destLink);
		try {
			view.forward(request,response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String encodeURL(String url,HttpSession session) {
		int q=0;
		   String sid = (String)session.getId();
		   q = url.indexOf('?');
		   
		   if(q != -1) {
		      return url.substring(0, q) + ";jsessionid=" + sid + url.substring(q, url.length());
		   }
		   return url + ";jsessionid=" + sid;
		}
	private void responseToReuqestStatusLogin(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String name=null;
		String password=null;
		String subscriberID=null;
//		name=(String)request.getAttribute("LOGIN_ID");
//		if(name==null){
		name = request.getParameter("loginid");
		if(name!=null){
			name=name.trim();
		}
		password = request.getParameter("pwd");
		if(password!=null){
			password=password.trim();
		}

		//		}
		//		else{
		//		password=(String)request.getAttribute("PASSWORD");
		//		subscriberID=((String)request.getParameter("msisdn")).trim(); 
		//		session.setAttribute("MSISDN_ENTERED",subscriberID);
		//		}
		//		session.setAttribute("LOGIN_ID", name);
		String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
		int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_GUI", 30);
		
		RBTLogin[] user = RBTDBManager.init(dbURL, n).checkGUIPwd(name, password);
		//		int userType1=1;
		//		Integer temp1=new Integer(userType1);
		String typeUser1 = ("" + 1).trim();
		//		int userType2=2;
		//		Integer temp2=new Integer(userType2);
		String typeUser2 = ("" + 2).trim();
		//		int userType3=3;
		//		Integer temp3=new Integer(userType3);
		String typeUser3 = ("" + 3).trim();
		if (user != null) {


			for (int i = 0; i < user.length; i++) {
				if ((user[i].userType().equalsIgnoreCase(typeUser1))
						&& (user[i].user().equalsIgnoreCase(name))) {
					session.setAttribute("Entry_PWD", user[i].pwd());
					session.setAttribute("MENU_ORDER_TEMP", user[i].menuOrder());
				} else if ((user[i].userType().equalsIgnoreCase(typeUser2))
						&& (user[i].user().equalsIgnoreCase(name))) {
					session.setAttribute("PWD", user[i].pwd());
				} else if ((user[i].userType().equalsIgnoreCase(typeUser3))
						&& (user[i].user().equalsIgnoreCase(name))) {
					session.setAttribute("Super_PWD", user[i].pwd());
				}
			}
			session.setAttribute("ASK_PASSWD", user[0].askPassword());
			session.setAttribute("VALIDATE", "true");
			session.setAttribute("USER_NAME", name);
			session.setAttribute("UserName", name);
			if(!name.equals("CCC")){
				session.setAttribute("READ_ONLY_USER", "FALSE");
			}
			else{
				session.setAttribute("READ_ONLY_USER", "TRUE");
			}
			try {
				String wstid=null;
				wstid=(String)request.getParameter("wstid");
                 String udid=null;
                 udid=(String)request.getParameter("udid");
                 String opParam=null;
                 opParam=(String)request.getParameter("opParam");
                 System.out.println("*************wstid"+wstid);
                 System.out.println("*************udid"+udid);
                 System.out.println("*************opParam"+opParam);

                         session.setAttribute("USER_DETAILS", new UserDetails(udid,wstid,wstid,name));
                         logger.info("RBT::going to track the user with udid=="+udid+",wstid="+wstid+",opParam="+opParam+",userName=="+name);
                 
				int menuindex = 0;
				menuindex = initializeMenuOrder(session, sc);
				/*
				 * temp1 =temp1 + displaymenu1[i].substring(0,displaymenu1[i].lastIndexOf(":"))  + "\",\"";
			 temp2=temp2+displaymenu1[i].substring(displaymenu1[i].lastIndexOf(":")+1)+ "\",\"";

				 * 
				 * 
				 * **/
				String[] displaymenu = (String[]) session
				.getAttribute("MENU_DISPLAY");
				String destLink = null;

				destLink = displaymenu[menuindex];
				destLink = destLink.substring(5);
				//response.encodeURL(destLink);
				
				RequestDispatcher view = request.getRequestDispatcher(destLink);
				view.forward(request, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				session.invalidate();
				RequestDispatcher view = request.getRequestDispatcher("index.jsp");
				view.forward(request, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}


	}
	private int initializeMenuOrder(HttpSession session,ServletContext sc){
		String[] arrdisplaymenu=null;
		String[] arrmenuorder=null;
		ArrayList displaymenu=new ArrayList();
		String[] menuorder=(String[])session.getAttribute("MENU_ORDER_TEMP");
		HashMap displaymenuMap=(HashMap)sc.getAttribute("DISPLY_MENU_MAP");
		for(int i=0;i<menuorder.length;i++){
			if(displaymenuMap.containsKey(menuorder[i])){
				displaymenu.add(displaymenuMap.get(menuorder[i]));
			}
		}
		/**
		 * populate ArrayList "displaymenu" from a hashmap whose key is A,B,C... 
		 * and values are like "/ccc/subscriber/subscriber_info.jsp?sCategory=subscriber&caller_tab=1:subscriber"
		 */
//		displaymenu.add("/ccc/subscriber/subscriber_info.jsp?sCategory=subscriber&caller_tab=1:subscriber" );
//		displaymenu.add("/ccc/subscriber/easy_charge.jsp?sCategory=easycharge:easycharge");
		ArrayList displaymenu1=new ArrayList();
		ArrayList displaymenu2=new ArrayList();
		for(int i=0;i<displaymenu.size();i++){
			displaymenu1.add(((String)displaymenu.get(i)).substring(0,((String)displaymenu.get(i)).lastIndexOf(":")));
			displaymenu2.add(((String)displaymenu.get(i)).substring(((String)displaymenu.get(i)).lastIndexOf(":")+1));
		}

		arrdisplaymenu=(String[])displaymenu1.toArray(new String[0]);
		arrmenuorder=(String[])displaymenu2.toArray(new String[0]);

		session.setAttribute("MENU_DISPLAY", arrdisplaymenu);
		session.setAttribute("MENU_ORDER", arrmenuorder);
		String strMenu=null;
		int menuIndexSub=0,menuIndexEasy=0,menuIndexBlackList=0,menuIndexHlr=0,menuIndexNotice=0,menuIndexBilling=0;
		for(int k=0;k<arrmenuorder.length;k++){
			if(arrmenuorder[k].equalsIgnoreCase("subscriber")){
				menuIndexSub=k;
				strMenu="subscriber";
			}
			else if(arrmenuorder[k].equalsIgnoreCase("notice")){
				menuIndexNotice=k;
				strMenu="notice";
			}
			else if(arrmenuorder[k].equalsIgnoreCase("easycharge")){
				menuIndexEasy=k;
				strMenu="easycharge";
			}
			else if(arrmenuorder[k].equalsIgnoreCase("billingHistory")){
				menuIndexBilling=k;
				strMenu="billingHistory";
			}
			else if(arrmenuorder[k].equalsIgnoreCase("hlr")){
				menuIndexHlr=k;
				strMenu="hlr";
			}
			else if(arrmenuorder[k].equalsIgnoreCase("blackList")){
				menuIndexBlackList=k;
				strMenu="blackList";
			}

		}
		if(menuIndexSub!=0 ||strMenu.equalsIgnoreCase("subscriber"))
			return menuIndexSub;
		else if(menuIndexEasy!=0||strMenu.equalsIgnoreCase("easycharge"))
			return menuIndexEasy;
		else if(menuIndexBlackList!=0||strMenu.equalsIgnoreCase("blackList"))
			return menuIndexBlackList;
		else if(menuIndexHlr!=0||strMenu.equalsIgnoreCase("hlr"))
			return menuIndexHlr;
		else if(menuIndexBilling!=0||strMenu.equalsIgnoreCase("billingHistory"))
			return menuIndexBilling;
		else if(menuIndexNotice!=0||strMenu.equalsIgnoreCase("notice"))
			return menuIndexNotice;
		else
			return 0;


	}
	private void responseToReuqestStatusStart(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){

		String subscriberID=null;
		String previousMSISDN=(String)session.getAttribute("MSISDN_ENTERED");
		if(previousMSISDN!=null)
			session.removeAttribute("MSISDN_ENTERED");
		String numStatus=(String)session.getAttribute("INVALID_NUMBER");
		if(numStatus!=null){
			session.removeAttribute("INVALID_NUMBER");
		}
		SubscriberDetails subDet1=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
		if(subDet1!=null){
			session.removeAttribute("SUB_DETAILS");
		}
		logger.info("RBT::entering MSISDN "); 
		subscriberID=((String)request.getParameter("msisdn"));
		if(subscriberID!=null){
			subscriberID=subscriberID.trim();
			logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
		}
		else{
			logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
		}
		SiteURLDetails destUrlDetail=getDestURL(sc,subscriberID);
		 if(destUrlDetail==null || subscriberID.length()!=10){
			session.setAttribute("INVALID_NUMBER", "TRUE");
			session.setAttribute("MSISDN_ENTERED",subscriberID);
			logger.info("RBT::check == false and url is null"); 
			try {
				//response.encodeURL("/subscriber/subscriber_info.jsp?number_status=invalid&sCategory=subscriber");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?sCategory=subscriber&number_status=invalid");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			logger.info("RBT::check ==dseturlDetail is not null"); 

			String attachMsg="?request_value=sub_detail&SUB_ID="+subscriberID;
			String northCircleID=(String)sc.getAttribute("NORTH_CIRCLE_ID");
			String northWestCircleId=(String)sc.getAttribute("NORTH_WEST_CIRCLE_ID");
			String testStatus=(String)sc.getAttribute("TEST_STATUS");
			ArrayList testNumbers=(ArrayList)sc.getAttribute("TEST_NUMBERS");
			String testCircleId=(String)sc.getAttribute("TEST_CIRCLE_ID");
			boolean isToMakeHttpHit=false;
			if(testStatus!=null && testStatus.equalsIgnoreCase("true")){
				logger.info("CCC:: TEST_STATUS=="+testStatus );
				if(testNumbers!=null && testNumbers.contains(subscriberID)){
					logger.info("CCC:: TEST_NUMBERS size=="+testNumbers.size() );
					isToMakeHttpHit=false;
					HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
					if(testCircleId!=null){
						 logger.info("CCC:: TEST_CIRCLE_ID =="+testCircleId );
					destUrlDetail=(SiteURLDetails)(site_url_details.get(testCircleId));
					}else{
						isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
					}
				}else{
					isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
				}
			
			}else{
				isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
			}
			//ResponseObj responseobj=new ResponseObj();
			if(!isToMakeHttpHit){
			ResponseObj responseobj=makeHttpRequest(destUrlDetail,attachMsg);
			UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
			if(responseobj.responseStatus ==true){
				if(!responseobj.response.toString().equalsIgnoreCase("error")){
					 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                          CCCAccountingManager.addToCCCGUIAccounting(userDetails,subscriberID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                  }
					session.setAttribute("CIRCLE_ID",new String(destUrlDetail.circle_id));
					session.setAttribute("MSISDN_ENTERED",subscriberID);
					session.setAttribute("DEST_URL_DETAILS", destUrlDetail);
					session.setAttribute("INVALID_NUMBER", "FALSE");
					SubscriberDetails subInfo=populateSubInfo(responseobj.response,subscriberID);
					long noOfSubsDaysLeft=-1;
					noOfSubsDaysLeft=getNoOfDaysLeftPrompt(subscriberID, sc);
					subInfo.noOfSubsDaysLeft=noOfSubsDaysLeft;
					System.out.println("**********GIFT OUTBOX POPULATED**********");
					if(subInfo!=null){
					subInfo.giftOutbox=getGiftOutbox(destUrlDetail, subscriberID);
					}
					System.out.println("**********GIFT OUTBOX POPULATED**********"+subInfo.giftOutbox+"and SIZE="+subInfo.giftOutbox.size());
					session.setAttribute("SUB_DETAILS", subInfo);
					try {
						//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?caller_tab=1&sCategory=subscriber");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
	                     CCCAccountingManager.addToCCCGUIAccounting(userDetails,subscriberID,attachMsg.substring(1),"ERROE",responseobj.responseStatus);
	             }
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else{
				if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
	                  CCCAccountingManager.addToCCCGUIAccounting(userDetails,subscriberID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
	          }
				
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			 
			session.setAttribute("INVALID_NUMBER", "TRUE");
			session.setAttribute("MSISDN_ENTERED",subscriberID);
			logger.info("RBT::check == false and url is null"); 
			try {
				//response.encodeURL("/subscriber/subscriber_info.jsp?number_status=invalid&sCategory=subscriber");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?sCategory=subscriber&number_status=invalid");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}

//		if(check==false){
//		Tools.logDetail("Login", "checking MSISDN", 
//		"RBT::check == false"+ check); 
//		String circelId = RBTMOHelper.init().getCircleID(subscriberID.substring(0, 4));
//		String url = RBTMOHelper.init().getURL(subscriberID.substring(0, 4));
//		if(url != null) {
//		HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
//		SiteURLDetails remote=(SiteURLDetails)(site_url_details.get(circelId));
//		session.setAttribute("CIRCLE_ID",new String(circelId));

//		Tools.logDetail("Login", "checking MSISDN", 
//		"RBT::check == false"+ check+" and url is not null"); 

////		request.setAttribute("LOGIN_ID", (String)session.getAttribute("LOGIN_ID"));
////		request.setAttribute("PASSWORD", (String)session.getAttribute("Entry_PWD"));
////		request.setAttribute("REQUEST_SOURCE","START");
//		session.invalidate();
//		try {
//		RequestDispatcher view=request.getRequestDispatcher(url);
//		view.forward(request,response);
//		} catch (ServletException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
//		}
//		else{

//		}
//		}
//		else{
//		Tools.logDetail("Login", "checking MSISDN", 
//		"RBT::check == true");
//		SiteURLDetails local=(SiteURLDetails)(sc.getAttribute("LOCAL_URL"));
//		session.setAttribute("CIRCLE_ID", new String(local.circle_id));
//		session.setAttribute("MSISDN_ENTERED",subscriberID);
//		try {
//		RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp");
//		view.forward(request,response);
//		} catch (ServletException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//		}
//		}

	}

	public static ArrayList getGiftOutbox(SiteURLDetails dest,String subscriberID){
		String url="?request_value=gift_outbox&SUB_ID="+subscriberID; 
		ResponseObj responseobj=makeHttpRequest(dest,url);
		ArrayList giftList = new ArrayList();
		if(responseobj.responseStatus ==true){
			if(!responseobj.response.toString().equalsIgnoreCase("error")){
				String resp= responseobj.response.toString();
				String[] gifts = resp.split(";");
				for(int i=0;i<gifts.length;i++){
					String[] giftdetails = gifts[i].split("::");
					if(giftdetails.length>=8){
						GiftDetails gd = new GiftDetails(giftdetails[0],giftdetails[6],giftdetails[7],giftdetails[5],giftdetails[4],giftdetails[2],giftdetails[3]);
						giftList.add(gd);
					}
				}	
			}
		}
		
		return giftList;
	}
	public static void responseToReuqestCopy(String copynumber,HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){


		String subscriberID=null;
		String previousMSISDN=(String)session.getAttribute("COPY_NUMBER");
		if(previousMSISDN!=null)
			session.removeAttribute("COPY_NUMBER");
		logger.info("RBT::entering MSISDN "); 
		subscriberID=((String)((SubscriberDetails)session.getAttribute("SUB_DETAILS")).subId);
		if(subscriberID!=null){
			subscriberID=subscriberID.trim();
			logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
		}
		else{
			logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
		}
		SiteURLDetails destUrlDetail=getDestURL(sc,subscriberID);
//		if(destUrlDetail==null){
//			Tools.logDetail("Login", "checking MSISDN", 
//			"RBT::check == false and url is null"); 
//
//			Tools.logDetail("Login", "checking MSISDN", 
//			"RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
//			try {
//				//response.encodeURL("/subscriber/wrongNumber.jsp");
//				RequestDispatcher view=request.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
//				view.forward(request,response);
//			} catch (ServletException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
//		}
//		else{
			boolean useProxy = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_PROXY", "FALSE");
			String proxyServerPort = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROXY_SERVER_PORT", null);
			
			String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
			int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_GUI", 30);
			RBTDBManager rbtdbManager=RBTDBManager.init(dbURL, n);
			
			try {
				logger.info("RBT::sending parameter subscriberID=="+subscriberID+";copynumber=="+copynumber+";useProxy=="+useProxy+";proxyServerPort=="+proxyServerPort);
				
				String testStatus=(String)sc.getAttribute("TEST_STATUS");
				ArrayList testNumbers=(ArrayList)sc.getAttribute("TEST_NUMBERS");
				String testCicrleId=(String)sc.getAttribute("TEST_CIRCLE_ID");
				HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
				subscriberID=rbtdbManager.subID(subscriberID);
                 Subscriber subscriber=rbtdbManager.getSubscriber(subscriberID);
				String responseForCopy = rbtdbManager.getSubscriberVcodeCCC(
						copynumber, subscriberID, useProxy, proxyServerPort,testStatus,testNumbers,testCicrleId,site_url_details,subscriber.rbtType());
				
				responseForCopy=responseForCopy.trim();
				UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
                 if(userDetails!=null ){
                         CCCAccountingManager.addToCCCGUIAccounting(userDetails,subscriberID,"Checking if "+subscriberID+" can copy from"+ copynumber,responseForCopy,true);
                 }
				logger.info("RBT::response =="+responseForCopy);
				logger.info("RBT::response length =="+responseForCopy.length());
				logger.info("RBT::index of ':' in response length =="+responseForCopy.indexOf(":"));
				if (responseForCopy.equalsIgnoreCase("NOT_VALID")
						|| responseForCopy.equalsIgnoreCase("NOT_FOUND")
						|| responseForCopy.equalsIgnoreCase("ALBUM")) {
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! wrong number)");
					try {
						//response.encodeURL("/subscriber/wrongNumber.jsp");
						RequestDispatcher view = request
								.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
						view.forward(request, response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (responseForCopy.equalsIgnoreCase("ERROR")) {

					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)");
					try {
						//response.encodeURL("/subscriber/systemDown.jsp?copy=copy");
						RequestDispatcher view = request
								.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&copy=copy");
						view.forward(request, response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					if (responseForCopy.equalsIgnoreCase("DEFAULT")) {
						logger.info("RBT::inside copy with reponse DEFAULT)");
						SubscriberDetails subInfo = new SubscriberDetails(true,
								copynumber, "null", "hindi", "DEFAULT", 0,
								null, null, null, 0, false, "B");
						session.setAttribute("COPY_NUMBER", copynumber);
						session.setAttribute("COPY_SUB_DETAILS", subInfo);
						System.out
								.println("inside login for copy and returning wid"
										+ "/subscriber/copycontroller.jsp?copynumber="
										+ copynumber);
						
						//response.encodeURL("/subscriber/copycontroller.jsp?copynumber="+copynumber);
						RequestDispatcher view = null;
						view = request
								.getRequestDispatcher("/subscriber/copycontroller.jsp?copynumber="
										+ copynumber);
						try {
							view.forward(request, response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
					} else if (responseForCopy.indexOf(":") > -1) {
						//rbt_009110100004785_rbt:23
						logger.info("RBT::inside copy with reponse valid vcode and catID)");
						StringTokenizer st=new StringTokenizer(responseForCopy,":");
						String wavFile=null;
						String catId=null;
						int count=0;
						
								wavFile = st.nextToken();
								
							catId=st.nextToken();
							logger.info("RBT::inside copy with wavFile=="+wavFile+"and catId=="+catId);
											
							
						DemoClip defaultClip=null;
						String vcode=null;
						StringTokenizer st2=new StringTokenizer(wavFile,"_");
						int count1=0;
						while(st2.hasMoreElements()){
							String temp=st2.nextToken();
							if(count1==1){
								vcode=temp;
							}
							count1++;
						}
						logger.info("RBT::inside copy with vcod=="+vcode);
						String defClipId=ClipCacher.init().m_VcodeIDMap.get(wavFile).toString();
						ClipGui clipTemp=ClipCacher.init().getClip(new Integer(defClipId).intValue());
						try {
							defaultClip=new DemoClip("DEFAULT",clipTemp.getClipName(),new Integer(defClipId).intValue(),new Integer(catId).intValue(),clipTemp.getArtist(),wavFile);
							ArrayList arrDefaultSong = new ArrayList();
							arrDefaultSong.add(defaultClip);
							SubscriberDetails subInfo = new SubscriberDetails(true,
									copynumber, "null", "hindi", "DEFAULT", 0,
									arrDefaultSong, null, null, 0, false, "B");
							session.setAttribute("COPY_NUMBER", copynumber);
							session.setAttribute("COPY_SUB_DETAILS", subInfo);
							System.out
									.println("inside login for copy and returning wid"
											+ "/subscriber/copycontroller.jsp?copynumber="
											+ copynumber);
							//response.encodeURL("/subscriber/copycontroller.jsp?copynumber="+copynumber);
							RequestDispatcher view = null;
							view = request
									.getRequestDispatcher("/subscriber/copycontroller.jsp?copynumber="
											+ copynumber);
							try {
								view.forward(request, response);
							} catch (ServletException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} catch (RuntimeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							try {
								//response.encodeURL("/subscriber/systemDown.jsp?copy=copy");
								RequestDispatcher view = request
										.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&copy=copy");
								view.forward(request, response);
							} catch (ServletException se) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException ie) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					} else {

						logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)");
						try {
							//response.encodeURL("/subscriber/systemDown.jsp?copy=copy");
							RequestDispatcher view = request
									.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&copy=copy");
							view.forward(request, response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}
			} catch (Exception e) {
				// TODO: handle exception


				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)");
				try {
					//response.encodeURL("/subscriber/systemDown.jsp?copy=copy");
					RequestDispatcher view = request
							.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&copy=copy");
					view.forward(request, response);
				} catch (ServletException se) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException ie) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			
			}			
			
			
			logger.info("RBT::exiting"); 

			
	
//		}
	
	}
	public static void responseToReuqestCopy1(String copynumber,HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){

		String subscriberID=null;
		String previousMSISDN=(String)session.getAttribute("COPY_NUMBER");
		if(previousMSISDN!=null)
			session.removeAttribute("COPY_NUMBER");
		logger.info("RBT::entering MSISDN "); 
		subscriberID=copynumber;
		if(subscriberID!=null){
			subscriberID=subscriberID.trim();
			logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
		}
		else{
			logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
		}
		SiteURLDetails destUrlDetail=getDestURL(sc,subscriberID);
		if(destUrlDetail==null){
			logger.info("RBT::check == false and url is null"); 

			logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
			try {
				//response.encodeURL("/subscriber/wrongNumber.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else{
		
			
			
			logger.info("RBT::check == false and url is not null"); 

			String attachMsg="?request_value=copydetail&SUB_ID="+subscriberID;
			String northCircleID=(String)sc.getAttribute("NORTH_CIRCLE_ID");
			String northWestCircleId=(String)sc.getAttribute("NORTH_WEST_CIRCLE_ID");
			
			String testStatus=(String)sc.getAttribute("TEST_STATUS");
			ArrayList testNumbers=(ArrayList)sc.getAttribute("TEST_NUMBERS");
			String testCircleId=(String)sc.getAttribute("TEST_CIRCLE_ID");
			boolean isToMakeHttpHit=false;
			if(testStatus!=null && testStatus.equalsIgnoreCase("true")){

				if(testNumbers!=null && testNumbers.contains(subscriberID)){
					isToMakeHttpHit=false;
					HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
					if(testCircleId!=null){
					destUrlDetail=(SiteURLDetails)(site_url_details.get(testCircleId));
					}else{
						isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
					}
				}else{
					isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
				}
			
			}else{
				isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
			}
			
			//ResponseObj responseobj=new ResponseObj();
			if(!isToMakeHttpHit){
			ResponseObj responseobj=makeHttpRequest(destUrlDetail,attachMsg);
			if(responseobj.responseStatus ==true){
				if(!responseobj.response.toString().equalsIgnoreCase("error")){
					SubscriberDetails subInfo=populateSubInfo(responseobj.response,subscriberID);
//					session.setAttribute("COPY_SUB_DETAILS", subInfo);

					logger.info("subInfo.subActive=="+subInfo.subActive); 
						Clips defaultClip=(Clips)sc.getAttribute("DEFAULT_CLIP");
					if(subInfo.subActive){
						if(defaultClip!=null){
						if(subInfo.defaultSong==null || (!(subInfo.defaultSong.size()>1))){
							session.setAttribute("COPY_NUMBER", subscriberID);
							session.setAttribute("COPY_SUB_DETAILS", subInfo);
							RequestDispatcher view=null;
							//int clipId=((DemoClip)((ArrayList)subInfo.specialCallerSongs.get(0)).get(0)).clipId;
							
							session.setAttribute("COPY_NUMBER", subscriberID);
							session.setAttribute("COPY_SUB_DETAILS", subInfo);
							
							
							SubscriberDetails subDet=(SubscriberDetails)(session.getAttribute("COPY_SUB_DETAILS"));
							//response.encodeURL("/subscriber/copycontroller.jsp");
							System.out.println("inside login for copy and returning wid"+"/subscriber/copycontroller.jsp?copynumber="+copynumber);
							//response.encodeURL("/subscriber/copycontroller.jsp?copynumber="+copynumber);
							view=request.getRequestDispatcher("/subscriber/copycontroller.jsp?copynumber="+copynumber);
							try {
								view.forward(request,response);
							} catch (ServletException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
//					
						else{
							logger.info("RBT::check == no subscriber Detail available....Oops !!!! wrong number)"); 
							try {
								//response.encodeURL("/subscriber/wrongNumber.jsp");
								RequestDispatcher view=request.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
								view.forward(request,response);
							} catch (ServletException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}else{

						if(subInfo.defaultSong!=null && (!(subInfo.defaultSong.size()>1))){
							session.setAttribute("COPY_NUMBER", subscriberID);
							session.setAttribute("COPY_SUB_DETAILS", subInfo);
							RequestDispatcher view=null;
							SubscriberDetails subDet=(SubscriberDetails)(session.getAttribute("COPY_SUB_DETAILS"));
							//response.encodeURL("/subscriber/copycontroller.jsp");
							System.out.println("inside login for copy and returning wid"+"/subscriber/copycontroller.jsp?copynumber="+copynumber);
							//response.encodeURL("/subscriber/copycontroller.jsp?copynumber="+copynumber);
							view=request.getRequestDispatcher("/subscriber/copycontroller.jsp?copynumber="+copynumber);
							try {
								view.forward(request,response);
							} catch (ServletException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
//					
						else{
							logger.info("RBT::check == no subscriber Detail available....Oops !!!! wrong number)"); 
							try {
								//response.encodeURL("/subscriber/wrongNumber.jsp");
								RequestDispatcher view=request.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
								view.forward(request,response);
							} catch (ServletException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					
					}
					}
					else{
						logger.info("RBT::check == no subscriber Detail available....Oops !!!! wrong number)"); 
						try {
							//response.encodeURL("/subscriber/wrongNumber.jsp");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}else{
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp?copy=copy");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&copy=copy");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else{
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp?copy=copy");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&copy=copy");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			logger.info("RBT::check == false and url is null"); 

			logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
			try {
				//response.encodeURL("/subscriber/wrongNumber.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/wrongNumber.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}
	public static SubscriberDetails populateSubInfo(StringBuffer resp,String subID){
		String res=null;
		SubscriberDetails subDetail=null;
		if(resp!=null)
		{
			res=resp.toString();
		}
		if (!res.equalsIgnoreCase("error")) {
			boolean subActive = false;
			String subId = null;
			String lastBillDate = null;
			String lang = null;
			String subType = null;
			int selCount = 0;
			DemoClip defaultSong = null;
			ArrayList defaultSongArr=new ArrayList();
			ArrayList specialCallerSongs = new ArrayList();
			ArrayList giftSongs = new ArrayList();
			int giftCount = 0;
			int giftCounter = 1;
			int selCounter = 1;
			boolean blackList=false;
			String subYes=null;
			StringTokenizer st1 = new StringTokenizer(res, ";");
			int i = 0;
			while (st1.hasMoreElements()) {
				String temp = st1.nextToken();
				StringTokenizer st2 = null;
				if (temp.indexOf(",") >= 0) {
					st2 = new StringTokenizer(temp, ",");
				} else {
					st2 = new StringTokenizer(temp, "=");
				}
				if (i == 0) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{
					//					System.out.println("using ;");
					//				}
					String ch1 = st2.nextToken();
					//				System.out.println("ch1=="+ch1);
					String subactiveTemp = st2.nextToken();
					//				System.out.println("subActive=="+subactiveTemp);
					if (subactiveTemp.equalsIgnoreCase("false")) {

						return (new SubscriberDetails(false, subID));
					} else
						subActive = true;
				} else if (i == 1) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{
					//					System.out.println("using ;");
					//				}
					String ch1 = st2.nextToken();
					//				System.out.println("ch1=="+ch1);
					subId = st2.nextToken();
					//				System.out.println("subID=="+subId);
				} else if (i == 2) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{
					//					System.out.println("using ;");
					//				}
					st2.nextToken();
					lastBillDate = st2.nextToken();
					//				System.out.println("lastBillDate=="+lastBillDate);
				} else if (i == 3) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{

					//					System.out.println("using ;");
					//				}
					st2.nextToken();
					lang = st2.nextToken();
					//				System.out.println("lang=="+lang);
				} else if (i == 4) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{
					//					System.out.println("using ;");
					//				}
					st2.nextToken();
					subType = st2.nextToken();
					//				System.out.println("subType=="+subType);
				}else if (i == 5) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{
					//					System.out.println("using ;");
					//				}
					st2.nextToken();
					temp = st2.nextToken();
					if(temp.equalsIgnoreCase("true"))
						blackList=true;
					else{
						blackList=false;
					}
					//				System.out.println("subType=="+subType);
				}  
				else if(i==6){
					st2.nextToken();
					subYes=st2.nextToken();
				}
				else if (i == 7) {
					//				System.out.println(i);
					//				if(temp.indexOf(",")>=0){
					//					System.out.println("using ,");
					//				}
					//				else{
					//					System.out.println("using ;");
					//				}
					st2.nextToken();
					selCount = new Integer(st2.nextToken()).intValue();

					//				System.out.println("selCount=="+selCount);
				} else {
					if (selCount == 0) {
						if (i == 8) {
							//						System.out.println(i);
							//						if(temp.indexOf(",")>=0){
							//							System.out.println("using ,");
							//						}
							//						else{
							//							System.out.println("using ;");
							//						}
							st2.nextToken();
							giftCount = new Integer(st2.nextToken()).intValue();
							if (giftCount == 0) {
								subDetail = new SubscriberDetails(subActive,
										subId, lastBillDate, lang, subType,
										selCount, null, null, null, giftCount,blackList,subYes);

							}
						} else {
							if (i < (9+ giftCount)) {
								//							System.out.println(i);
								//							if (temp.indexOf(",") >= 0) {
								//								System.out.println("using ,");
								//							} else {
								//								System.out.println("using ;");
								//							}
								int k = 0;
								st2.nextToken();
								String caller = null;
								String songName = null;
								int clipId = 0;
								int catId = 0;
								String artist = null;
								String wavfile=null;
								String senttime=null;
								while (st2.hasMoreElements()) {
									//								System.out.println("k==" + k);
									String temp1 = st2.nextToken();
									//								System.out.println(temp1);
									StringTokenizer st3 = new StringTokenizer(
											temp1, "=");
									st3.nextToken();
									if (k == 0) {
										caller = st3.nextToken();
									} else if (k == 1) {
										songName = st3.nextToken();
										if(songName==null || songName.equalsIgnoreCase("null")){
											songName="--";
										}else{



											if(songName.indexOf("/")>0){
												StringTokenizer st5=new StringTokenizer(songName,"/");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=","+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												songName=temp6;
												}
											} if(songName.indexOf("\\")>0){
												StringTokenizer st5=new StringTokenizer(songName,"\\");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=";"+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												songName=temp6;
												}
											}
										
										
										
										}
									} else if (k == 2) {
										clipId = new Integer(st3.nextToken())
										.intValue();
									} else if (k == 3) {
										catId = new Integer(st3.nextToken())
										.intValue();
									} else if(k==4){
										artist = st3.nextToken();
										if(artist.equalsIgnoreCase("null") || artist==null){
											artist="--";
										}else{

											if(artist.indexOf("/")>0){
												StringTokenizer st5=new StringTokenizer(artist,"/");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=","+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												artist=temp6;
												}
											} if(artist.indexOf("\\")>0){
												StringTokenizer st5=new StringTokenizer(artist,"\\");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=";"+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												artist=temp6;
												}
											}
										
										
										
										
										}
									}else if(k==5){
										wavfile = st3.nextToken();
									}else{
										senttime=st3.nextToken();

										giftSongs
										.add(new DemoClip(caller,
												songName, clipId,
												catId, artist,wavfile,senttime));
										if (i == (8+ giftCount)) {
											subDetail = new SubscriberDetails(
													subActive, subId,
													lastBillDate, lang,
													subType, selCount, null,
													null, giftSongs, giftCount,blackList,subYes);

										}
									}

									k++;
								}
							}

						}
					} else {
						if (i < 8 + selCount) {

							//							System.out.println(i);
							//							if(temp.indexOf(",")>=0){
							//								System.out.println("using ,");
							//							}
							//							else{
							//								System.out.println("using ;");
							//							}
							int k = 0;
							String selInfo=null;
							selInfo=st2.nextToken();
							StringTokenizer st3 = new StringTokenizer(
									selInfo, "=");
							st3.nextToken();
							String caller = null;
							String songName = null;
							int clipId = 0;
							int catId = 0;
							String artist = null;
							String wavfile=null;
							String setTime=null;
							int shuffleCount=0;
							boolean flag=true;
							ArrayList specialCallerArr=new ArrayList();;
							int p=0;
							if(st3.nextToken().equalsIgnoreCase("selShuffle")){
								//System.out.println("*******************");
								//System.out.println("inside shuffle selections");

								while (st2.hasMoreElements()&& p<2) {
									//System.out.println("index p=="+p);
									String temp1 = st2.nextToken();
									st3 = new StringTokenizer(
											temp1, "=");
									st3.nextToken();
									if (p == 0) {
										caller = st3.nextToken();
										//	System.out.println("caller=="+caller);
									}
									if(p==1){
										shuffleCount=new Integer(st3.nextToken()).intValue();
										//System.out.println("cshuffleCount=="+shuffleCount);
									}
									p++;
								}


								p=0;
								while (st2.hasMoreElements()&& p<(6*shuffleCount)) {
									//System.out.println("index p==hahah"+p);
									String temp1 = st2.nextToken();
									st3 = new StringTokenizer(
											temp1, "=");
									st3.nextToken();
									int q=p%6;
									if (q == 0) {
										songName = st3.nextToken();
										if(songName==null || songName.equalsIgnoreCase("null")){
											songName="--";
										}else{

											if(songName.indexOf("/")>0){
												StringTokenizer st5=new StringTokenizer(songName,"/");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=","+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												songName=temp6;
												}
											} if(songName.indexOf("\\")>0){
												StringTokenizer st5=new StringTokenizer(songName,"\\");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=";"+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												songName=temp6;
												}
											}
										
										}
										//System.out.println("songname=="+songName);
									} else if (q == 1) {
										clipId = new Integer(st3.nextToken())
										.intValue();
										//System.out.println("clipId=="+clipId);
									} else if (q == 2) {
										catId = new Integer(st3.nextToken())
										.intValue();
										//System.out.println("catId=="+catId);
									} else if (q == 3) {
										artist = st3.nextToken();
										if(artist.equalsIgnoreCase("null") || artist==null ){
											artist="--";
										}else{


											if(artist.indexOf("/")>0){
												StringTokenizer st5=new StringTokenizer(artist,"/");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=","+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												artist=temp6;
												}
											} if(artist.indexOf("\\")>0){
												StringTokenizer st5=new StringTokenizer(artist,"\\");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=";"+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												artist=temp6;
												}
											}
										
										
										}
										//System.out.println("artist=="+artist);
									} else if(q == 4) {
										wavfile = st3.nextToken();
									}else{
										setTime=st3.nextToken();
										//System.out.println("wavfile=="+wavfile);
										if(caller.equalsIgnoreCase("DEFAULT")){
											defaultSong = new DemoClip(caller,
													songName, clipId, catId, artist,wavfile,setTime);
											defaultSongArr.add(defaultSong);
											flag=false;
											//System.out.println("defaultarrayCount=="+defaultSongArr.size());
											//System.out.println("flag=="+flag);

										}
										else{

											specialCallerArr.add(new DemoClip(
													caller, songName, clipId,
													catId, artist,wavfile,setTime));
											//System.out.println("specialCallerArr=="+specialCallerArr.size());
											//	System.out.println("flag=="+flag);
										}
									}
									p++;
								}
								//System.out.println("*******************");
								if(flag){
									specialCallerSongs.add(specialCallerArr);
									specialCallerArr=new ArrayList();
									//System.out.println("specialCallerSongs=="+specialCallerSongs.size());
								}

							}else{
								//							System.out.println("initializing default song");
								while (st2.hasMoreElements()) {
									//								System.out.println("k=="+k);
									String temp1 = st2.nextToken();
									//								System.out.println(temp1);
									st3 = new StringTokenizer(
											temp1, "=");
									st3.nextToken();
									if (k == 0) {
										caller = st3.nextToken();
									} else if (k == 1) {
										songName = st3.nextToken();
										if(songName.equalsIgnoreCase("null") || songName==null){
											songName="--";
										}else{


											if(songName.indexOf("/")>0){
												StringTokenizer st5=new StringTokenizer(songName,"/");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=","+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												songName=temp6;
												}
											} if(songName.indexOf("\\")>0){
												StringTokenizer st5=new StringTokenizer(songName,"\\");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=";"+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												songName=temp6;
												}
											}
										
										
										}
									} else if (k == 2) {
										clipId = new Integer(st3.nextToken())
										.intValue();
									} else if (k == 3) {
										catId = new Integer(st3.nextToken())
										.intValue();
									} else if(k==4){
										artist = st3.nextToken();
										if(artist.equalsIgnoreCase("null")|| artist==null){
											artist="--";
										}else{
											if(artist.indexOf("/")>0){
												StringTokenizer st5=new StringTokenizer(artist,"/");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=","+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
													artist=temp6;
												}
												
											} if(artist.indexOf("\\")>0){
												StringTokenizer st5=new StringTokenizer(artist,"\\");
												String temp5=null;String temp6=null;
												int count=0;
												while(st3.hasMoreElements()){
													if(count==0){
														temp5=st3.nextToken();
														temp6=temp5;
													
													}else{
														temp5=";"+st3.nextToken();	
														temp6=temp6+temp5;
													}
													count++;
												}
												if(temp6!=null){
												artist=temp6;
												}
											}
										
										
										
										}
									}else if(k==5){
										wavfile = st3.nextToken();
									}else{
										setTime=st3.nextToken();
										if(caller.equalsIgnoreCase("DEFAULT")){
											defaultSong = new DemoClip(caller,
													songName, clipId, catId, artist,wavfile,setTime);
											defaultSongArr.add(defaultSong);
										}else{

											//	System.out.println(defaultSong.artist+" "+defaultSong.caller+" "+defaultSong.catId+" "+defaultSong.clipId+" "+defaultSong.songName);
											specialCallerArr.add(new DemoClip(
													caller, songName, clipId,
													catId, artist,wavfile,setTime));
											specialCallerSongs.add(specialCallerArr);

										}
									}
									k++;
								}
							}

						} else {
							if (i == 8 + selCount) {
								//							System.out.println(i);
								//							if(temp.indexOf(",")>=0){
								//								System.out.println("using ,");
								//							}
								//							else{
								//								System.out.println("using ;");
								//							}
								st2.nextToken();
								giftCount = new Integer(st2.nextToken())
								.intValue();
								if (giftCount == 0) {
									subDetail = new SubscriberDetails(
											subActive, subId, lastBillDate,
											lang, subType, selCount,
											defaultSongArr, specialCallerSongs,
											null, giftCount,blackList,subYes);

								}
							} else {
								if (i < (9 + selCount + giftCount)) {
									//								System.out.println(i);
									//								if (temp.indexOf(",") >= 0) {
									//									System.out.println("using ,");
									//								} else {
									//									System.out.println("using ;");
									//								}
									int k = 0;
									st2.nextToken();
									String caller = null;
									String songName = null;
									int clipId = 0;
									int catId = 0;
									String artist = null;
									String wavfile=null;
									String senttime=null;
									while (st2.hasMoreElements()) {
										//									System.out.println("k==" + k);
										String temp1 = st2.nextToken();
										//									System.out.println(temp1);
										StringTokenizer st3 = new StringTokenizer(
												temp1, "=");
										st3.nextToken();
										if (k == 0) {
											caller = st3.nextToken();
										} else if (k == 1) {
											songName = st3.nextToken();
											if(songName==null || songName.equalsIgnoreCase("null")){
												songName="--";
											}else{



												if(songName.indexOf("/")>0){
													StringTokenizer st5=new StringTokenizer(songName,"/");
													String temp5=null;String temp6=null;
													int count=0;
													while(st3.hasMoreElements()){
														if(count==0){
															temp5=st3.nextToken();
															temp6=temp5;
														
														}else{
															temp5=","+st3.nextToken();	
															temp6=temp6+temp5;
														}
														count++;
													}
													if(temp6!=null){
													songName=temp6;
													}
												} if(songName.indexOf("\\")>0){
													StringTokenizer st5=new StringTokenizer(songName,"\\");
													String temp5=null;String temp6=null;
													int count=0;
													while(st3.hasMoreElements()){
														if(count==0){
															temp5=st3.nextToken();
															temp6=temp5;
														
														}else{
															temp5=";"+st3.nextToken();	
															temp6=temp6+temp5;
														}
														count++;
													}
													if(temp6!=null){
													songName=temp6;
													}
												}
											
											
											
											}
										} else if (k == 2) {
											clipId = new Integer(st3
													.nextToken()).intValue();
										} else if (k == 3) {
											catId = new Integer(st3.nextToken())
											.intValue();
										} else if(k==4){
											artist = st3.nextToken();
											if(artist.equalsIgnoreCase("null") || artist==null){
												artist="--";
											}else{

												if(artist.indexOf("/")>0){
													StringTokenizer st5=new StringTokenizer(artist,"/");
													String temp5=null;String temp6=null;
													int count=0;
													while(st3.hasMoreElements()){
														if(count==0){
															temp5=st3.nextToken();
															temp6=temp5;
														
														}else{
															temp5=","+st3.nextToken();	
															temp6=temp6+temp5;
														}
														count++;
													}
													if(temp6!=null){
													artist=temp6;
													}
												} if(artist.indexOf("\\")>0){
													StringTokenizer st5=new StringTokenizer(artist,"\\");
													String temp5=null;String temp6=null;
													int count=0;
													while(st3.hasMoreElements()){
														if(count==0){
															temp5=st3.nextToken();
															temp6=temp5;
														
														}else{
															temp5=";"+st3.nextToken();	
															temp6=temp6+temp5;
														}
														count++;
													}
													if(temp6!=null){
													artist=temp6;
													}
												}
											
											
											
											
											}
										}else if(k==5){
											wavfile = st3.nextToken();
										}else{
											senttime=st3.nextToken();

											giftSongs
											.add(new DemoClip(caller,
													songName, clipId,
													catId, artist,wavfile,senttime));
											//										System.out.println("adding gift no "+(8+selCount-i));
											if (i == (8 + selCount + giftCount)) {
												subDetail = new SubscriberDetails(
														subActive, subId,
														lastBillDate, lang,
														subType, selCount,
														defaultSongArr,
														specialCallerSongs,
														giftSongs, giftCount,blackList,subYes);
												//System.out.println("initializin subDetail");
											}
										}

										k++;
									}
								}

							}
						}
					}
				}
				i++;
				//			System.out.println("coming out of main while loop");
			}
		}		
		return subDetail;
	}
	public static long getNoOfDaysLeftPrompt(String subscriberID,ServletContext sc){
		logger.info("entering....");
		long noOfDaysleftForDeactivation=-1;
		logger.info("noOfDaysleftForDeactivation=="+noOfDaysleftForDeactivation);
		if(subMgrUrlForDeactivationDaysLeft==null){
			logger.info("subMgrUrlForDeactivationDaysLeft=="+null);
			 subMgrUrlForDeactivationDaysLeft=(String)sc.getAttribute("SUB_MGR_URL_UNSUB_DAYS_LEFT");
		}
		if(subMgrUrlForDeactivationDaysLeft!=null){
			logger.info("subMgrUrlForDeactivationDaysLeft is not null");
		//"subMgrUrlForDeactivationDaysLeft" should of format "http://loclahots:8080/rbt/ListSubscriptions?user=<USER>&pass=<PASS>"
			String strURL= subMgrUrlForDeactivationDaysLeft+"&msisdn="+subscriberID+"&output=xml";
			logger.info("strURL=="+strURL);
			StringBuffer responseString=new StringBuffer();
			Integer statusCode=new Integer("0");
			boolean reply=false;
			//get a XML reply through a http hit to subMgr URL
			logger.info("goign to make http hit to subMgr for sub days left");
			reply=Tools.callURL(strURL, statusCode, responseString, false, null, 80);
			logger.info("reply=="+reply);
			if(reply && responseString!=null && responseString.length()>0 && (responseString.toString().indexOf("error")==-1) && (responseString.toString().indexOf("ERROR")==-1)&&(responseString.toString().indexOf("Error")==-1)){
				logger.info("reply is not null reply=="+reply +" and responseString=="+responseString.toString());
				
				String nextChargingDate=null;
				nextChargingDate=parseXmlString(responseString.toString());
					//nextChargingDate = parseDocument(dom);
					logger.info("nextChargingDate=="+nextChargingDate);
					if (nextChargingDate != null) {
						logger.info("nextChargingDate is not null");
						// "nextChargingdate" is of format "yyyy-MM-dd HH:mm:ss"
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Date deactivationDate = null;
						try {
							logger.info("inside try block");
							deactivationDate = sdf.parse(nextChargingDate);
							logger.info("deactivationDate=="+deactivationDate.toString());
							Date currDate = Calendar.getInstance().getTime();
							logger.info("currDate=="+currDate.toString());
							if (deactivationDate.after(currDate)) {
								logger.info("deactivationDate is after currDate");
								//deactivationDate-currDate;
								long timeDiff = deactivationDate.getTime()- currDate.getTime();
								logger.info("timeDiff=="+timeDiff);
								if (timeDiff != 0) {
									logger.info("timeDiff != 0");
									if (timeDiff > 0) {
										logger.info("timeDiff > 0");
										noOfDaysleftForDeactivation = (timeDiff / (1000 * 60 * 60 * 24));
									}
								} else {
									logger.info("timeDiff ==0");
									noOfDaysleftForDeactivation = 0;
								}
							}
						} catch (ParseException e) {
							logger.info("inside catch block");
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
							
			}
		}
		logger.info("returning with noOfDaysleftForDeactivation=="+noOfDaysleftForDeactivation);
		//ListSubscriptions?user=<USER>&pass=<PASS> ---subMgrUrlForDeactivationDaysLeft must contains user and pass parameter
		return noOfDaysleftForDeactivation;
	}
	private static String parseXmlString(String tempXML){
		logger.info("tempXML=="+tempXML);
		String returnString=null;
		Document dom=null;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
//			String tempXML="<?xml version=\"1.0\" encoding=\"UTF-8\"?><Personnel><ROOT><SERVICE><SVCID>KEYWORD</SVCID><SVCDESC>DESC</SVCDESC><STATUS>STATUS</STATUS><NEXTCHARGEDATE>yyyy-MM-dd HH:mm:ss</NEXTCHARGEDATE></SERVICE></ROOT></Personnel>"
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tempXML.getBytes());
			dom=db.parse(byteArrayInputStream);	
		}catch(ParserConfigurationException pce) {
			logger.info("got ParserConfigurationException");
			pce.printStackTrace();
		}catch(SAXException se) {
			logger.info("got SAXException");
			se.printStackTrace();
		}catch(IOException ioe) {
			logger.info("got IOException");
			ioe.printStackTrace();
		}
		String tempReturnStr=parseDocument(dom);
				if(tempReturnStr!=null){
					logger.info("tempReturnStr=="+tempReturnStr);
					returnString=tempReturnStr;
				}else{
					logger.info("tempReturnStr==null");
				}
			return returnString;
	}
	private static String parseDocument(Document dom){
		logger.info("entering");
		Element docEle = dom.getDocumentElement();
		String nextchargingdate = null;
		// "nextChargingDate" should be of format "yyyy-MM-dd HH:mm:ss"
		NodeList nl = docEle.getElementsByTagName("SERVICE");
		if(nl != null && nl.getLength() > 0) {
			logger.info("nl != null && nl.getLength() > 0");
				Element el = (Element)nl.item(0);
					nextchargingdate=getTextValue(el,"NEXTCHARGEDATE");
					logger.info("nextchargingdate=="+nextchargingdate);
		}
		logger.info("returning nextchargingdate=="+nextchargingdate);
		return nextchargingdate ;
	}
	private static String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}
//	public static SubscriberDetails populateSubInfo(StringBuffer resp,String subID){
//	String res=null;
//	SubscriberDetails subDetail=null;
//	if(resp!=null)
//	{
//	res=resp.toString();
//	}
//	boolean subActive=false;
//	String subId=null;
//	String lastBillDate=null;
//	String lang=null;
//	String subType=null;
//	int selCount=0;
//	DemoClip defaultSong=null;
//	ArrayList specialCallerSongs=new ArrayList();
//	ArrayList giftSongs=new ArrayList();
//	int giftCount=0;
//	int giftCounter=1;
//	int selCounter=1;
//	StringTokenizer st1=new StringTokenizer(res,";");
//	int i=0;
//	while(st1.hasMoreElements()){
//	String temp=st1.nextToken();
//	StringTokenizer st2=null;
//	if(temp.indexOf(",")>=0){
//	st2=new StringTokenizer(temp,",");
//	}
//	else{
//	st2=new StringTokenizer(temp,"=");
//	}
//	if(i==0){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	String ch1=st2.nextToken();
//	System.out.println("ch1=="+ch1);
//	String subactiveTemp=st2.nextToken();
//	System.out.println("subActive=="+subactiveTemp);
//	if(subactiveTemp.equalsIgnoreCase("false")){

//	return (new SubscriberDetails(false,subID));
//	}
//	else
//	subActive=true;
//	}
//	else if(i==1){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	String ch1=st2.nextToken();
//	System.out.println("ch1=="+ch1);
//	subId=st2.nextToken();
//	System.out.println("subID=="+subId);
//	}
//	else if(i==2){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	st2.nextToken();
//	lastBillDate=st2.nextToken();
//	System.out.println("lastBillDate=="+lastBillDate);
//	}
//	else if(i==3){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	st2.nextToken();
//	lang=st2.nextToken();
//	System.out.println("lang=="+lang);
//	}
//	else if(i==4){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	st2.nextToken();
//	subType=st2.nextToken();
//	System.out.println("subType=="+subType);
//	}
//	else if(i==5){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	st2.nextToken();
//	selCount=new Integer(st2.nextToken()).intValue();

//	System.out.println("selCount=="+selCount);
//	}
//	else{
//	if(selCount==0){
//	if(i==6){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	st2.nextToken();
//	giftCount=new Integer(st2.nextToken()).intValue();
//	if(giftCount==0){
//	subDetail=new SubscriberDetails(subActive, subId, lastBillDate,lang,subType,selCount, null, null, null,giftCount);

//	}
//	}
//	else{
//	if (i < (7 + giftCount)) {
//	System.out.println(i);
//	if (temp.indexOf(",") >= 0) {
//	System.out.println("using ,");
//	} else {
//	System.out.println("using ;");
//	}
//	int k = 0;
//	st2.nextToken();
//	String caller = null;
//	String songName = null;
//	int clipId = 0;
//	int catId = 0;
//	String artist = null;
//	while (st2.hasMoreElements()) {
//	System.out.println("k==" + k);
//	String temp1 = st2.nextToken();
//	System.out.println(temp1);
//	StringTokenizer st3 = new StringTokenizer(
//	temp1, "=");
//	st3.nextToken();
//	if (k == 0) {
//	caller = st3.nextToken();
//	} else if (k == 1) {
//	songName = st3.nextToken();
//	} else if (k == 2) {
//	clipId = new Integer(st3.nextToken())
//	.intValue();
//	} else if (k == 3) {
//	catId = new Integer(st3.nextToken())
//	.intValue();
//	} else {
//	artist = st3.nextToken();
//	giftSongs.add(new DemoClip(caller,
//	songName, clipId, catId, artist));
//	if (i == (6 + giftCount)) {
//	subDetail = new SubscriberDetails(
//	subActive, subId, lastBillDate,
//	lang, subType, selCount, null,
//	null, giftSongs, giftCount);

//	}
//	}

//	k++;
//	}
//	}						

//	}
//	}
//	else{
//	if(i<6+selCount){
//	if(i==6){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	int k=0;
//	st2.nextToken();
//	String caller=null;
//	String songName=null;
//	int clipId=0;
//	int catId=0;
//	String artist=null;
//	System.out.println("initializing default song");
//	while(st2.hasMoreElements()){
//	System.out.println("k=="+k);
//	String temp1=st2.nextToken();
//	System.out.println(temp1);
//	StringTokenizer st3=new StringTokenizer(temp1,"=");
//	st3.nextToken();
//	if(k==0){
//	caller=st3.nextToken();
//	}
//	else if(k==1){
//	songName=st3.nextToken();
//	}
//	else if(k==2){
//	clipId=new Integer(st3.nextToken()).intValue();
//	}
//	else if(k==3){
//	catId=new Integer(st3.nextToken()).intValue();
//	}
//	else {
//	artist=st3.nextToken();
//	defaultSong=new DemoClip(caller, songName ,clipId,catId,artist);
//	System.out.println(defaultSong.artist+" "+defaultSong.caller+" "+defaultSong.catId+" "+defaultSong.clipId+" "+defaultSong.songName);
//	}
//	k++;
//	}
//	}
//	else{
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	int k = 0;
//	st2.nextToken();
//	String caller = null;
//	String songName = null;
//	int clipId = 0;
//	int catId = 0;
//	String artist = null;
//	while (st2.hasMoreElements()) {
//	System.out.println("k=="+k);
//	String temp1 = st2.nextToken();
//	System.out.println(temp1);
//	StringTokenizer st3 = new StringTokenizer(
//	temp1, "=");
//	st3.nextToken();
//	if (k == 0) {
//	caller = st3.nextToken();
//	} else if (k == 1) {
//	songName = st3.nextToken();
//	} else if (k == 2) {
//	clipId = new Integer(st3.nextToken())
//	.intValue();
//	} else if (k == 3) {
//	catId = new Integer(st3.nextToken())
//	.intValue();
//	} else {
//	artist = st3.nextToken();
//	specialCallerSongs.add(new DemoClip(caller,
//	songName, clipId, catId, artist));
//	}

//	k++;
//	}
//	}						


//	}
//	else{
//	if(i==6+selCount){
//	System.out.println(i);
//	if(temp.indexOf(",")>=0){
//	System.out.println("using ,");
//	}
//	else{
//	System.out.println("using ;");
//	}
//	st2.nextToken();
//	giftCount=new Integer(st2.nextToken()).intValue();
//	if(giftCount==0){
//	subDetail=new SubscriberDetails(subActive, subId, lastBillDate,lang,subType,selCount, defaultSong, specialCallerSongs, null,giftCount);

//	}
//	}
//	else{
//	if (i <(7 + selCount + giftCount)) {
//	System.out.println(i);
//	if (temp.indexOf(",") >= 0) {
//	System.out.println("using ,");
//	} else {
//	System.out.println("using ;");
//	}
//	int k = 0;
//	st2.nextToken();
//	String caller = null;
//	String songName = null;
//	int clipId = 0;
//	int catId = 0;
//	String artist = null;
//	while (st2.hasMoreElements()) {
//	System.out.println("k==" + k);
//	String temp1 = st2.nextToken();
//	System.out.println(temp1);
//	StringTokenizer st3 = new StringTokenizer(
//	temp1, "=");
//	st3.nextToken();
//	if (k == 0) {
//	caller = st3.nextToken();
//	} else if (k == 1) {
//	songName = st3.nextToken();
//	} else if (k == 2) {
//	clipId = new Integer(st3.nextToken())
//	.intValue();
//	} else if (k == 3) {
//	catId = new Integer(st3.nextToken())
//	.intValue();
//	} else {
//	artist = st3.nextToken();
//	giftSongs
//	.add(new DemoClip(caller,
//	songName, clipId,
//	catId, artist));
//	System.out.println("adding gift no "+(8+selCount-i));
//	if (i == (6 + selCount + giftCount)) {
//	subDetail = new SubscriberDetails(
//	subActive, subId,
//	lastBillDate, lang,
//	subType, selCount, defaultSong,
//	specialCallerSongs, giftSongs, giftCount);
//	System.out.println("initializin subDetail");
//	}
//	}

//	k++;
//	}
//	}							


//	}
//	}
//	}
//	}
//	i++;
//	System.out.println("coming out of main while loop");
//	}
//	return subDetail;
//	}
	public static ResponseObj makeHttpRequest(SiteURLDetails destUrlDetail,String attachMsg){
		StringBuffer responseString=new StringBuffer();
		String strURL=destUrlDetail.URL+attachMsg;
		Integer statusCode=new Integer("0");
		boolean useProxy=destUrlDetail.use_proxy;
		String proxyHost=destUrlDetail.proxy_host;
		int proxyPort=destUrlDetail.proxy_port;
		int connectionTimeOut=destUrlDetail.connection_time_out;
		int timeOut=destUrlDetail.time_out;
		boolean responseStatus=Tools.callURL(strURL, statusCode, responseString, useProxy, proxyHost, proxyPort, connectionTimeOut, timeOut);
		System.out.println("inside login:)_)_?==responseStatus=="+responseStatus+"and responseString=="+responseString.toString());
		return (new ResponseObj(responseStatus,responseString));
	}

	public static SiteURLDetails getDestURL(ServletContext sc,String subscriberID){
		SiteURLDetails destURLDetail=null;
		String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
		int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_GUI", 0);
		
		boolean check=RBTDBManager.init(dbURL, n).isValidPrefix(subscriberID);
		if(check==false){
			logger.info("RBT::check == false"+ check); 
			
			String circelId = ClipCacher.init().getCircleID(subscriberID.substring(0, 4));
			logger.info("circleId string is=="+circelId);
			String url = ClipCacher.init().getURL(subscriberID.substring(0, 4));
			logger.info("url from MOHelper=="+url);
			if(url!= null) {
				HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
				//Iterator prefixIteror = site_url_details.keySet().iterator();
				//int i=0;
				//while(prefixIteror.hasNext())
				//{
					//String prefixKey = (String)prefixIteror.next();
					
					
					//SiteURLDetails siteURLTemp = (SiteURLDetails)site_url_details.get(prefixKey);
					//Tools.logDetail("Login", "checking MSISDN", 
						//	"hashMap details****  index is"+  i  +"==circleId=="+siteURLTemp.circle_id+" ***url=="+siteURLTemp.URL);
						//i++;
				//}
				destURLDetail=(SiteURLDetails)(site_url_details.get(circelId));
				//Tools.logDetail("Login", "checking MSISDN", 
					//	"destURLDetails==circleId=="+destURLDetail.circle_id+" ***url=="+destURLDetail.URL);

				logger.info("RBT::check == false"+ check+" and url is not null"); 
				return destURLDetail;
			}
			else{
				return null;
			}
		}
		else{
			logger.info("RBT::check == true");
			SiteURLDetails local=(SiteURLDetails)(sc.getAttribute("LOCAL_URL"));

			return local;
		}
	}
}
