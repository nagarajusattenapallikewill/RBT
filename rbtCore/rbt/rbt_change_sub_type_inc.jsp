<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
<%@ page import = "java.util.*"%>

<%@ include file = "validate.jsp" %>
<%	
String strSubID=null;String subType=null; String strValue=null; String strResult=null;
Subscriber dispSub = null;%>
<%
strSubID = null;subType = null;strValue = null;strResult = null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
if (validateUser(request, session,  "rbt_change_sub_type.jsp", response)) {%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
	document.frmStatus.SUB_ID.disabled = false;
	document.frmStatus.updated.value = "false";
	document.frmStatus.submit();
	return true;
}
</script>
<title>RBT Subscription</title>
</head>
<body>
<form action ="rbt_update_subscriber.jsp" onsubmit='return fnSubmit(frmStatus)' method="post" name="frmStatus">
	<p><b><center>Prepaid <--> Postpaid</center></b></p>	
<table  width="100%" align = "center">
<tr>

<td><input type=hidden name="updated" size = 32 maxlength=32 value ="true"></td>
</tr>
<tr>
<td align="center" colspan=3>
<% String strStatus = null;String strReason=null ;String strValidSub=null;%>
<% 

	strSubID = subType=strValue=strResult = strReason = strStatus = null;
	strSubID = 	request.getParameter("SUB_ID");
		dispSub = rbtLogin.getSubscriber(strSubID);
	if( strSubID != null && (!strSubID.equals(""))){
		strValidSub = rbtLogin.isValidSub(strSubID, dispSub);
        if(!strValidSub.equals("success")){
			strStatus =  "FAILURE";
			if(strValidSub.equals("failure"))
				strReason=  "Invalid prefix " + RBTSubUnsub.init().getSubscriberPrefix(strSubID);
			else if (strValidSub.equals("blacklisted"))
	            strReason = "No. " + strSubID + " is blacklisted.";
			else if (strValidSub.equals("suspended"))
	            strReason = "No. " + strSubID + " is suspended.";

		}
		else{
			strResult = rbtLogin.subscriberStatus(strSubID, dispSub); 
			StringTokenizer st = new StringTokenizer(strResult, ",");
			if (st.hasMoreTokens()){
				strValue = st.nextToken();
				if(strValue.equalsIgnoreCase("Subscriber does not exists: ")){
					strStatus =  "FAILURE";
					strReason=  strValue + strSubID;
				}else {
					if(st.hasMoreTokens())
						st.nextToken();
					if(st.hasMoreTokens())
						strReason = st.nextToken();
					if(strReason.equalsIgnoreCase("Active"))
						strStatus="SUCCESS";
					else{
						strStatus ="FAILURE";
						strReason ="Customer "+strReason;
					}
					if(st.hasMoreTokens())
						subType = st.nextToken();				}
			}
		}
	} 
%>
<%if(strStatus !=null && strStatus.equalsIgnoreCase("FAILURE")){
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>ERROR : "+strReason+".</b></font></center>");
}
%>
</td>
</tr>
<tr>
</tr>
<tr>
<td width = "35%">Subscriber ID</td>
<% if(strSubID !=null && strStatus !=null && (!strStatus.equals("FAILURE")))
		{%>
			<td width ="65%"><input type=text name="SUB_ID" size = 32 maxlength=32 disabled value =<%=strSubID%> ></td>
		<%}else	{%>
			<td width = "65%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
		<%}
%>
<td></td>
</tr>
<tr></tr>
<tr></tr>
<%if(subType != null && strStatus !=null && (!strStatus.equals("FAILURE"))){
		%>
	<tr>
	<td width ="35%">Current Subscription Type</td>
	<td width ="65%"><b><%=subType.toUpperCase()%></b></td>
	</tr>
	<tr>
	<td>
	<tr></tr>
	<tr></tr><tr>
	<td width ="35%">Change Subscription To</td>
	<td align="left" >
	<select name="subType" width = "65%">
		<%	if(subType.equalsIgnoreCase("prepaid")){%>
			<option name="postpaid" value ="Postpaid">Postpaid</option>
		<%} else {%>
				<option name="prepaid" value ="Prepaid">Prepaid</option>
		<%}%>
	</select>
	</td>
	</tr>
	<tr>
	</tr>
	<tr>
	<tr></tr>
	<tr><td colspan="3" align="center"><input type=image border = "0" name="UPDATE" src = "images/update1.gif" onClick='update()'></td>
	</tr>

<%} else {%>
	<tr>
	</tr>
	<tr></tr>
	<tr></tr>
	<tr>
	<td colspan="3" align="center"><input type=image border = "0" name="SUB_STATUS" src = "images/list_details.gif"></td>
	</tr>
	<%}%>
</table>
</form>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>