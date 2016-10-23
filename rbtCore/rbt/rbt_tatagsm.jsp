<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTTataGSMImpl"%>
<%@ page import = "com.onmobile.apps.ringbacktones.common.Tools"%>
<%@ page import = "java.util.Map"%>
<%
	Map<String, String[]> paramMap = request.getParameterMap();
	String responseStr = "8";
	try {
		responseStr = RBTTataGSMImpl.getInstance().processRequest(paramMap);
	}
	catch(Throwable t) {
		Tools.logException("rbt_tatagsm.jsp", "rbt_tatagsm.jsp", t);
		responseStr = "8";
	}
	out.println(response);
%>