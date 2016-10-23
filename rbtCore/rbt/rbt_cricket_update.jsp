<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ include file = "validate.jsp" %>
<%
	String strUser = null;
	String actInfo = null;
%>
<%
	RBTSubUnsub rbtLogin = RBTSubUnsub.init();
	String strIP  = request.getRemoteAddr();
	strUser  = (String)(session.getAttribute("UserId"));
	if(strUser == null)
		actInfo = strIP + ":Direct"; 
	else
		actInfo = strIP + ":" + strUser; 
	if (validateUser(request, session,  "rbt_cricket.jsp", response)) {
		String strSub = request.getParameter("SUB_ID");
		String actBy = request.getParameter("comboActBy");
		String pass = request.getParameter("comboPack");
		String status = request.getParameter("comboCricStatus");
		boolean bPrepaid = false;
		if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
			bPrepaid = true;
		
		if(status == null)
			status = "ON";
		String result = rbtLogin.processFeed(strSub, status, pass, actBy, actInfo, bPrepaid, true);
		session.setAttribute("cricFlag", result);
	%>
		<jsp:forward page="rbt_cricket.jsp" />
	<% } 
	else{
		session.invalidate();%>
		<jsp:forward page="index.jsp" />
	<% } %>
