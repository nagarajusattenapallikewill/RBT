package com.onmobile.apps.ringbacktones.servlets;

import java.io.IOException;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class Easycharge_forward extends HttpServlet{
	
	private static Logger logger = Logger.getLogger(Easycharge_forward.class);
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){

		String strCustomer=null;

		HttpSession session=request.getSession();
		String sessionId=(String)session.getId();
		String strIP  = request.getRemoteAddr();
		ServletContext sc=getServletContext();

		RBTDBManager rbtDBManager=RBTDBManager.getInstance();
		strCustomer = request.getParameter("msisdn");
		strCustomer=rbtDBManager.subID(strCustomer);
		SiteURLDetails destUrlDetail=login.getDestURL(sc,strCustomer);
		session.setAttribute("DEST_URL_DETAILS",destUrlDetail);
		if(destUrlDetail==null)
		{
			logger.info("RBT::check == false and url is null"); 

//			response.encodeURL("easy_charge.jsp?flag=0" );
			RequestDispatcher view = request.getRequestDispatcher("/subscriber/easy_charge.jsp?jsessionid="+sessionId+"&flag=0" );
			try {
				view.forward(request, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		else{
			String attachMsg="?request_value=easyCharge&msisdn="+strCustomer;
			UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
			String northCircleID=(String)sc.getAttribute("NORTH_CIRCLE_ID");
			boolean isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID);
			if(!isToMakeHttpHit){
			ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
			if(responseobj!=null&&responseobj.responseStatus ==true)
			{
				System.out.println("response received....");
				if(!responseobj.response.toString().equalsIgnoreCase("error"))
				{
					if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                         CCCAccountingManager.addToCCCGUIAccounting(userDetails,strCustomer,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                 }
					String resp =responseobj.response.toString();
					//StringTokenizer strToken=new StringTokenizer(resp,";");
					String[] strArray=resp.split(";");
					String[][] strArrays=new String[strArray.length][5];
					System.out.println("length="+strArray.length);
					for(int i=0;i<strArray.length;i++)
					{	
						logger.info(strArray[i]+"**"+i);
						StringTokenizer strTokens=new StringTokenizer(strArray[i],",");
						logger.info("strTokens.hasMoreTokens()"+strTokens.hasMoreTokens());
						int j=0;
						while(strTokens.hasMoreTokens())
						{
							if(j==1){
								strArrays[i][0]=strTokens.nextToken();
								System.out.println(strArrays[i][0]);
							}else if(j==2){
								strArrays[i][1]=strTokens.nextToken();
								System.out.println(strArrays[i][1]);
							}else if(j==3){
								strArrays[i][2]=strTokens.nextToken();
								System.out.println(strArrays[i][2]);
							}else if(j==4){
								strArrays[i][3]=strTokens.nextToken();
								System.out.println(strArrays[i][3]);
							}else if(j==5){
								strArrays[i][4]=strTokens.nextToken();
								System.out.println(strArrays[i][4]);
							}j++;
						}	
					}
					getServletContext().setAttribute("object1",strArrays);


//					response.encodeURL("easy_charge.jsp?flag=1");
					RequestDispatcher view = request.getRequestDispatcher("/subscriber/easy_charge.jsp?flag=1");
					try {
						view.forward(request, response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else if(responseobj.response.toString().equalsIgnoreCase("error"))
				{
					if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                         CCCAccountingManager.addToCCCGUIAccounting(userDetails,strCustomer,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                 }
					logger.info("value of strCustomer="+strCustomer);

//					response.encodeURL("easy_charge.jsp?flag=0&msisdn="+strCustomer);
					RequestDispatcher view = request.getRequestDispatcher("/subscriber/easy_charge.jsp?flag=0&msisdn="+strCustomer);
					try {
						view.forward(request, response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


				}
			}else if(responseobj==null)
			{
				if(userDetails!=null && attachMsg!=null){
                     CCCAccountingManager.addToCCCGUIAccounting(userDetails,strCustomer,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
             }
				logger.info("value of strCustomer="+strCustomer);

//				response.encodeURL("easy_charge.jsp?flag=0&msisdn="+strCustomer);
				RequestDispatcher view = request.getRequestDispatcher("/subscriber/easy_charge.jsp?flag=0&msisdn="+strCustomer);
				try {
					view.forward(request, response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}else
			{
				 if(userDetails!=null && attachMsg!=null){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,strCustomer,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
              }
				logger.info("value of strCustomer="+strCustomer);

				RequestDispatcher view = request.getRequestDispatcher("/subscriber/easy_charge.jsp?flag=0&msisdn="+strCustomer);
				try {
					view.forward(request, response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}else{
			logger.info("value of strCustomer="+strCustomer);
			RequestDispatcher view = request.getRequestDispatcher("/subscriber/easy_charge.jsp?flag=0&msisdn="+strCustomer);
			try {
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
	}

}
