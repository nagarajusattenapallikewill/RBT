package com.onmobile.apps.ringbacktones.servlets;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Floater  extends HttpServlet{
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		ServletContext context=getServletContext();
		System.out.println("****************page===Floater");
		
		context.setAttribute("FLAG","TRUE");
		RequestDispatcher view = request
		.getRequestDispatcher("/subscriber/subscriber_info.jsp?sCategory=subscriber&caller_tab=1");
		System.out.println("****************dispatching to page===/ccc/subscriber/subscriber_info.jsp?sCategory=subscriber&caller_tab=1");
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
