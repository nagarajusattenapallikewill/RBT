package com.onmobile.apps.ringbacktones.cache;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UpdateCacheServlet extends HttpServlet{
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		String type = request.getParameter("type");
		updateCache(type);
	}

	private boolean updateCache(String type) {
		// TODO Auto-generated method stub
		return false;
	}

}
