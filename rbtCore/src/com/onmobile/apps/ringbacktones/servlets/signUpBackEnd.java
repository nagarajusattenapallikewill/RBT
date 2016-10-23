package com.onmobile.apps.ringbacktones.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class signUpBackEnd extends HttpServlet{
	
	private static Logger logger = Logger.getLogger(signUpBackEnd.class);
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		System.out.println("*******************inside signUpBackEnd**************************");
		HttpSession session=request.getSession();
		ServletContext sc=getServletContext();
		String action=(String)request.getParameter("action");
		System.out.println("*******************action==="+action+"**************************");
		String passtype=(String)request.getParameter("passType");
		String password=(String)request.getParameter("password");
		String toPage=(String)request.getParameter("toPage");
		String source=(String)request.getParameter("source");
		String queryString=(String)request.getParameter("queryString");
		String finaltype=(String)request.getParameter("finaltype");
		String newlang=(String)request.getParameter("newlang");
	
		String askPasswd=(String)session.getAttribute("ASK_PASSWD");
		if(askPasswd==null){
			askPasswd="true";
		}
		String username=(String)session.getAttribute("UserName");
		String pwd=(String)session.getAttribute("PWD");
		String superpwd=(String)session.getAttribute("Super_PWD");
		
		System.out.println("*****************************");
		System.out.println("inside checkpaswword.do!!! Hahahahaha");
		System.out.println("*****************************source====="+source+"toPage=="+toPage+"queryString=="+queryString);
		System.out.println("*****************************");
		System.out.println("pwd=="+pwd);
		System.out.println("superpwd=="+superpwd);
		System.out.println("password entered=="+password);
		System.out.println("queryString=="+queryString);
		ServletContext context=getServletContext();
			if (action!=null) {
							if (action.equalsIgnoreCase("checkPass")) {
				if(askPasswd.equalsIgnoreCase("false") || password.equals(pwd)){
					System.out.println("Yahoo!!!!  password matched");
						try {
							
							RequestDispatcher view=null;
								System.out.println("inside  unsubscribe");
								System.out.println("toPage==="+toPage);
								System.out.println("source=="+source);
						//'/ccc/subscriber/signUpFlow.jsp?source=signUp&queryString='+queryString+'&flag=signUp'
								System.out.println("inside toPage!=null && source!=null"+"/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString+"&flag=resultSuccess");
								
								if(askPasswd.equalsIgnoreCase("false")){
									response.encodeURL("/subscriber/signUpFlow.jsp");
									view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?source=signUp&queryString="+queryString+"&flag=signUp");
								}else{
									response.encodeURL("/subscriber/Result.jsp");
								view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString+"&flag=resultSuccess");
								}
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
						try {
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?flag=resultFailed&source=signUp");
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
			else if(action.equalsIgnoreCase("processRequest")){
				
			
				String sub_typeTemp=(String)request.getParameter("subsType4Airtel");
				String pwd1=(String)request.getParameter("pwd1");
				String lang_type=(String)request.getParameter("langType");

				//SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
				SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
				String subID=((String)(subDet).subId);
				String pwdSet=(String)session.getAttribute("PWD");
				logger.info("sub_typeTempIndex=="+sub_typeTemp); 
				logger.info("lang_type=="+lang_type); 
				logger.info("subID=="+subID); 
				ArrayList tagSubPack=(ArrayList)context.getAttribute("SUB_PACK_TAG");
				String sub_type=null;
				int lang=new Integer(lang_type).intValue();
				int temp=new Integer(sub_typeTemp).intValue();
				logger.info("subIndex=="+temp); 
				if (tagSubPack!=null) {
					for (int i = 0; i < tagSubPack.size(); i++) {
						logger.info("choice count=="+i); 
						//System.out.println("choice count=="+i);
						if (i == temp) {
							sub_type = (String) tagSubPack.get(i);
							logger.info("choice final"+sub_type); 
							//System.out.println("choice final"+sub_type);
							break;
						}

					}
				}			
				String[] langs=null;
				if(lang!=0){
					langs=(String[])context.getAttribute("LANG");
					lang_type=langs[lang-1];
				}
				else{
					lang_type="eng";
				}
//				String askPasswd=(String)session.getAttribute("ASK_PASSWD");
//				if(askPasswd==null){
//					askPasswd="true";
//				}
				if(askPasswd.equalsIgnoreCase("false") || pwd1.equals(pwdSet)){
					SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
					String circleId=destUrlDetail.circle_id; 
                    circleId=circleId.trim(); 
                    circleId=circleId.toUpperCase(); 
                    String strPromo=circleId+"_PROMOTIONS"; 
                    ArrayList arrPromotions=(ArrayList)(sc.getAttribute(strPromo)); 

					logger.info("RBT::check == false and url is not null"); 
					  UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
					String attachMsg="?request_value=signUp&SUB_ID="+subID+"&sub_type="+sub_type+"&lang_type="+lang_type;
					ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
					if(responseobj.responseStatus ==true){
						if(!responseobj.response.toString().equalsIgnoreCase("error")){
							  if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                                   CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                           }
							SubscriberDetails subDetails=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
							subDetails.subActive=true;
							subDetails.lang=lang_type;
							subDetails.subType=sub_type;
							subDetails.blackList=false;
							subDetails.subYes="A";
							Calendar cal=Calendar.getInstance();
							Date dat=cal.getTime();
							String strLastBillDate=null;
							strLastBillDate=""+dat.getDate()+"-";
							strLastBillDate=strLastBillDate+getMonth(dat.getMonth())+"-";
							strLastBillDate=strLastBillDate+dat.getYear();
							subDetails.lastBillDate=strLastBillDate;
							subDetails.selCount=0;
							subDetails.giftCount=0;
							if(arrPromotions!=null && arrPromotions.size()>0){ 
                                for(int count=0;count<arrPromotions.size();count++){ 
                                        String strTemp=(String)arrPromotions.get(count); 
                                        if(strTemp.equalsIgnoreCase("FIRST_SEL_FREE_AFTER_SUB")){ 
                                                session.setAttribute("FREE_SONG", "TRUE"); 
                                                session.setAttribute("FREE_SONG_MSISDN",subID); 
                                        } 
                                } 
							} 
							try {
								//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
								RequestDispatcher view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?flag=success&source=signUp");
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
                                  CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                          }
							logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
							try {
								//response.encodeURL("/subscriber/systemDown.jsp");
								RequestDispatcher view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?flag=failure&source=signUp");
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
						 if(userDetails!=null && attachMsg!=null){
                              CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
                      }
						logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
						try {
							//response.encodeURL("/subscriber/systemDown.jsp");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?flag=failure&source=signUp");
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
					try {
						//response.encodeURL("/subscriber/Reject.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/signUpFlow.jsp?flag=reject&source=signUp");
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
			}
	public static String getMonth(int month){
		if(month==0){
			return "JAN";
		}
		else if(month==1){
			return "FEB";
		}
		else if(month==2){
			return "MAR";
		}
		else if(month==3){
			return "APR";
		}
		else if(month==4){
			return "MAY";
		}
		else if(month==5){
			return "JUN";
		}
		else if(month==6){
			return "JUL";
		}
		else if(month==7){
			return "AUG";
		}
		else if(month==8){
			return "SEP";
		}
		else if(month==9){
			return "OCT";
		}
		else if(month==10){
			return "NOV";
		}
		else{
			return "DEC";
		}
	}
}
