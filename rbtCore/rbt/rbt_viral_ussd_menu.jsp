<%@page session="false"%><%@page import="com.onmobile.apps.ringbacktones.promotions.RBTViralMain"%><%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%><%

	String msisdn = null;
	String menuType= null;

	msisdn = request.getParameter("msisdn");
	menuType = request.getParameter("menuType");
	RBTViralMain.getInstance(true);
	if (menuType == null)
	{
		out.print(RBTViralMain.getViralUSSDMenu(msisdn));
	}
	else if (menuType.equalsIgnoreCase("rating"))
	{
		out.print(RBTViralMain.getRatingMenu(msisdn));
	}
	else if (menuType.equalsIgnoreCase("recomm"))
	{
		out.print(RBTViralMain.getRecommendationMenu(msisdn));
	}

%>