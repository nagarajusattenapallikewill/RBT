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

import com.onmobile.apps.ringbacktones.content.RBTLogin;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

public class CheckPassword extends HttpServlet{
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		String passtype=(String)request.getParameter("passType");
		String password=(String)request.getParameter("password");
		
		String toPage=(String)request.getParameter("toPage");
		String source=(String)request.getParameter("source");
		String queryString=(String)request.getParameter("queryString");
		String finaltype=(String)request.getParameter("finaltype");
		String newlang=(String)request.getParameter("newlang");
		HttpSession session=request.getSession();
		String username=(String)session.getAttribute("UserName");
		String askPasswd=(String)session.getAttribute("ASK_PASSWD");
		if(askPasswd==null){
			askPasswd="true";
		}
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
	
			if(askPasswd.equalsIgnoreCase("false")|| password.equals(pwd)){
			System.out.println("Yahoo!!!!  password matched");
				try {
					
					RequestDispatcher view=null;
					if(source.equalsIgnoreCase("editCaller")){
						System.out.println("in checkpassword==/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString);
						//response.encodeURL("/subscriber/Result.jsp");
						if(askPasswd.equalsIgnoreCase("false")){
							view=request.getRequestDispatcher("/subscriber/backEnd.jsp?source="+source+"&queryString="+queryString+"&toPage="+toPage);
						}else{
							view=request.getRequestDispatcher("/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString);
						}
						view.forward(request,response);
					}
					else if(source.equalsIgnoreCase("copy")){
						StringTokenizer st1=new StringTokenizer(queryString,":::");
						st1.nextToken();
						String copynumber=st1.nextToken();
						ServletContext sc=getServletContext();
						login.responseToReuqestCopy(copynumber,request,response,session, sc);
//						SubscriberDetails subDet=(SubscriberDetails)(session.getAttribute("COPY_SUB_DETAILS"));
//						response.encodeURL("/subscriber/copycontroller.jsp");
//						System.out.println("inside checkpwd for copy and returning wid"+"/subscriber/copycontroller.jsp?copynumber="+copynumber);
//						view=request.getRequestDispatcher("/subscriber/copycontroller.jsp?copynumber="+copynumber);
//							
					}
//					else if(source.equalsIgnoreCase("gift")){
//						response.encodeURL("/subscriber/Result.jsp");
//						view=request.getRequestDispatcher("/subscriber/gift.jsp?source="+source+"&queryString="+queryString);
//						view.forward(request,response);	
//					}
					else if(source.equalsIgnoreCase("signUp")||source.equalsIgnoreCase("unsubscribe")){
						System.out.println("inside  unsubscribe");
						System.out.println("toPage==="+toPage);
						System.out.println("source=="+source);
//				
						System.out.println("inside toPage!=null && source!=null"+"/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString);
						//response.encodeURL("/subscriber/Result.jsp");
						if(askPasswd.equalsIgnoreCase("false")){
							if(source.equalsIgnoreCase("unsubscribe")){
							view=request.getRequestDispatcher("controller.do?source="+source);
							}else{
								/***
								 * now,for signUp call never comes to this servlet (:...
								 * **/
							}
							
						}else{
						view=request.getRequestDispatcher("/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString);
					}	
					view.forward(request,response);	
					}
					else if(source.equalsIgnoreCase("update")){
						System.out.println("inside update");
						System.out.println("finaltype=="+finaltype);
						System.out.println("new lang=="+newlang);
						//response.encodeURL("/subscriber/Result.jsp");
						if(askPasswd.equalsIgnoreCase("false")){
							view=request.getRequestDispatcher("controller.do?source="+source+"&finaltype="+finaltype+"&newlang="+newlang);	
						}else{
							view=request.getRequestDispatcher("/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&finaltype="+finaltype+"&newlang="+newlang);
						}
						
						view.forward(request,response);
					}
					else if(source.equalsIgnoreCase("selection")){
						//response.encodeURL("/controller.do?source="+source+"&toPage="+toPage+"&queryString="+queryString);
						view=request.getRequestDispatcher("/controller.do?source="+source+"&toPage="+toPage+"&queryString="+queryString);
						view.forward(request,response);
					}
					else{
						if(askPasswd.equalsIgnoreCase("false")){
							if(toPage==null){
								view=request.getRequestDispatcher("/controller.do?source="+source+"&queryString="+queryString);
							}else{
								view=request.getRequestDispatcher("/subscriber/backEnd.jsp?source="+source+"&queryString="+queryString+"&toPage="+toPage);
							}
						}else{
							System.out.println("in checkpassword==/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString);
							view=request.getRequestDispatcher("/subscriber/Result.jsp?source="+source+"&toPage="+toPage+"&queryString="+queryString);
						}
						//response.encodeURL("/subscriber/Result.jsp");
						view.forward(request,response);
					}
					
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
					//response.encodeURL("/subscriber/Reject.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/Reject.jsp");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		
//		if(passtype.equalsIgnoreCase("3")){
//			if(password.equals(superpwd)){
//				try {
//					response.encodeURL("/subscriber/subscriber_info.jsp");
//					RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp");
//					view.forward(request,response);
//				} catch (ServletException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			else{
//				try {
//					response.encodeURL("/subscriber/subscriber_info.jsp");
//					RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp");
//					view.forward(request,response);
//				} catch (ServletException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
	}

}
