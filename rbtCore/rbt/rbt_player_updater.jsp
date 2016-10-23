<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTPlayerHelper"%>
<%
String sResponse = "SUCCESS";
try{
	String strIP = null;//request.getRemoteAddr();
	String action = request.getParameter("ACTION");
	String details = request.getParameter("DETAILS");	
	String extraparams = request.getParameter("EXTRA_PARAMS");
	System.out.println("ACTION=" + action + " DETAILS=" + details + " EXTRA_PARAMS=" + extraparams + " from " + strIP);
	boolean status = RBTPlayerHelper.getInstance().processRecord(action, details,extraparams,strIP);
	if(!status)
		sResponse = "ERROR";
}
catch(Exception e){
	sResponse = "ERROR";
}
%>
<%=sResponse%>