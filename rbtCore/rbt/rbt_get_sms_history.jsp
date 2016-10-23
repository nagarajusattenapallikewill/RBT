<head>
<%@ include file = "javascripts/RBTValidate.js" %>
</head>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.StringTokenizer"%>
<%@ page import = "java.util.Date"%>
<%@ page import = "java.util.Calendar"%>
<%@ page import = "java.lang.Integer"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
<%@ include file = "validate.jsp" %>
<%
String strSubID = null; String  strResult= null; String strValue= null; 
String strUserPermission  = (String)(session.getAttribute("Permission"));
String strUser  = (String)(session.getAttribute("UserId"));
if (validateUser(request, session,  "rbt_subscriber_status.jsp", response)) {
	System.out.println("Status Checked For A Subscriber By User " + strUser);

	%>

<%
String X_ONMOBILE_STATUS;
String X_ONMOBILE_FAILURE_REASON;
String FAILURE;
String SUCCESS;
String strDate;
String strReason;
String strActby;
String prepaid;
Subscriber dispSub;
%>
	<p><b><center>View Subscriber Details</center></b></p>	
<%	
X_ONMOBILE_STATUS = null;
X_ONMOBILE_FAILURE_REASON = null;
FAILURE = "FAILURE";
SUCCESS = "SUCCESS";
strDate = null;
strReason = null;
strActby = null;
prepaid = null;
dispSub = null;
RBTSubUnsub rbtStatus = RBTSubUnsub.init();
	strSubID = (String)session.getAttribute("Subscriber");
	dispSub = rbtStatus.getSubscriber(strSubID);
	String strValidSub = rbtStatus.isValidSub(strSubID, dispSub);
    if(!strValidSub.equals("success"))
	{
		session.removeAttribute("Subscriber");
		X_ONMOBILE_STATUS = FAILURE;
		if(strValidSub.equals("failure"))
			X_ONMOBILE_FAILURE_REASON = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(strSubID);
		else if (strValidSub.equals("blacklisted"))
	    	X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is blacklisted.";
		else if (strValidSub.equals("suspended"))
			X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is suspended.";
		session.setAttribute("SubStatus", X_ONMOBILE_STATUS);
		session.setAttribute("SubReason", X_ONMOBILE_FAILURE_REASON);%>
		<jsp:forward page ="rbt_subscriber_status.jsp" />
			<%
	}
	else{

		strResult = rbtStatus.subscriberStatus(strSubID, dispSub); 
		StringTokenizer st = new StringTokenizer(strResult, ",");
		if (st.hasMoreTokens()){
			strValue = st.nextToken();
			if(strValue.equalsIgnoreCase("Subscriber does not exists: ")){
				session.setAttribute("SubStatus","FAILURE");
				session.setAttribute("SubReason", strValue+strSubID);%>
				<jsp:forward page ="rbt_subscriber_status.jsp" />
			<%
			}
		}
	}

		String [] smsHistory = null;
		smsHistory = rbtStatus.getSmsHistoryNewTrans(strSubID);
		if (smsHistory != null){
		%>
		<table width ="70%" cols="6" align="center" border="1" cellpadding="1" cellspacing="1">
		<tr></tr>
		<tr>
			<td colspan = 6 align="center" ><b> SMS HISTORY </b></td>
		</tr>
		<tr></tr>
		<tr><th>SMS TYPE</th>
		<th>SMS TEXT</th>
		<th>TIME</th></tr>
		<%
			for(int i = 0; i < smsHistory.length; i++){
				if ( smsHistory[i]!= null){
			
			String sms_type = smsHistory[i].substring(0,smsHistory[i].indexOf(","));
     		String sms_text = smsHistory[i].substring(smsHistory[i].indexOf(",")+1,smsHistory[i].lastIndexOf(","));
            String time = smsHistory[i].substring(smsHistory[i].lastIndexOf(",")+1);
			Calendar cal = Calendar.getInstance();
            cal.clear();
            cal.set(Integer.parseInt(time.substring(0,4)),Integer.parseInt(time.substring(4,6))-1,           Integer.parseInt(time.substring(6,8)),Integer.parseInt(time.substring(8,10)),
            Integer.parseInt(time.substring(10,12)),Integer.parseInt(time.substring(12,14)));
	 		Date date = cal.getTime();
			%>
		<tr>
		<td><%out.print(sms_type);%></td>
		<td><%out.print(sms_text);%></td>
		<td><%out.print(date);%></td>
		</tr>
		<%}}%>
		</table>
		<%}
		else{
			out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>No SMS History </b></font></center>");
	}%>
		
		
		<table align="center">
<tr><td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
<tr><td colspan = "3" align = "center"><input type="image" border = "0" name="BACK" src = "images/previous.gif" onclick = "javascript:location.href='rbt_subscriber_status.jsp'">
</td>
</tr>
</table>

<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />

<%}%>