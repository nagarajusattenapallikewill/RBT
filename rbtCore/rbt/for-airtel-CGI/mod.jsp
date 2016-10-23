<%@ page import = "com.onmobile.apps.ringbacktones.thirdparty.AirtelThirdPartyRequestHandler"%>
<%@ page import = "java.util.HashMap"%>
<%@ page import = "java.util.Map"%>
<%
	Map<String, String[]> paramMap = request.getParameterMap();
	String strIP = request.getRemoteAddr();
	String subscriberID = request.getParameter("msisdn");

	Map<String, String> responseMap = AirtelThirdPartyRequestHandler.processThirdPartyRequest(subscriberID, paramMap, strIP, "mod");

	int status = 200;
	try {
		status = Integer.parseInt(responseMap.get("status"));
	}
	catch(Exception e) {

	}
	String responseStr = responseMap.get("response");
	response.setStatus(status);
	out.println(responseStr);
%>