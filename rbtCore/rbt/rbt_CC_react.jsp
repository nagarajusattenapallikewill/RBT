<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub, java.util.StringTokenizer" %>

<%@ include file = "validate.jsp" %>
<%
String actInfo=null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
String strIP  = request.getRemoteAddr();
String strUser  = (String)(session.getAttribute("UserId"));
String X_ONMOBILE_STATUS=null;
String X_ONMOBILE_FAILURE_REASON=null;
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser; 
if(true){
if (validateUser(request, session,  "rbt_subscribe_manager_react.jsp", response)) {
	System.out.println("Reactivation Done By User " + strUser);
 String strResult=null; String strSubID=null;String strButton=null;
	boolean bPrepaid=false;

	String FAILURE=null;
	String SUCCESS=null;
%>
<%
	X_ONMOBILE_STATUS = null;
	X_ONMOBILE_FAILURE_REASON = null;
	FAILURE = "FAILURE";
	SUCCESS = "SUCCESS";

	strSubID = request.getParameter("SUB_ID");
	bPrepaid = false;
	if(request.getParameter("comboUserProfAccLvl") != null && request.getParameter("comboUserProfAccLvl").equalsIgnoreCase("Prepaid"))
		bPrepaid = true;
	if(!rbtLogin.isValidSub(strSubID).equals("success")){
		X_ONMOBILE_STATUS = FAILURE;
		if(rbtLogin.isValidSub(strSubID).equals("failure"))
			X_ONMOBILE_FAILURE_REASON = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(strSubID);
		else if (rbtLogin.isValidSub(strSubID).equals("blacklisted"))
            X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is blacklisted.";
		else if (rbtLogin.isValidSub(strSubID).equals("suspended"))
            X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is suspended.";
	}
	else{
			//strResult = rbtLogin.reActSubscriber(strSubID, "CC", bPrepaid, actInfo);
			if(!(strResult.equalsIgnoreCase(SUCCESS))){
				X_ONMOBILE_STATUS = FAILURE;
				X_ONMOBILE_FAILURE_REASON = strResult+"--"+strSubID;
			}
			else
				X_ONMOBILE_STATUS = SUCCESS;
		}
}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}

session.setAttribute("Status", X_ONMOBILE_STATUS);
session.setAttribute("Reason", X_ONMOBILE_FAILURE_REASON);
%>
	<jsp:forward page ="rbt_subscribe_manager_react.jsp" />
<%}%>