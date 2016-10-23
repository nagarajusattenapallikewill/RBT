<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.StringTokenizer"%>

<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session,  "rbt_subscribe_manager_react.jsp", response)) {%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<title>RBT Subscription</title>
</head>
<body>
<form action="rbt_CC_react.jsp"  onsubmit = 'return fnSubmit(this)' method="post" name="frmRBT">
	<p><b><center>Reactivation</center></b></p>
<table cols = 3 align = "center">
<tr>
<td colspan = 3 align = "center">
<% String strStatus = null; String strReason= null;%>
<% strStatus = (String)session.getAttribute("Status");
if(strStatus == "FAILURE"){
	strReason = (String)session.getAttribute("Reason");
	out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>ERROR : "+strReason+"</b></font>");
}
else if(strStatus == "SUCCESS")
	out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Completed Successfully.</b></font>");
	session.removeAttribute("Status");
%>
</td>
</tr>
<tr>
<td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
</tr>
<tr>
<td width = "30%">Subscriber ID</td>
<td width ="70%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
</tr>
<tr></tr>
<tr>
		<td width = "35%">Subscriber Type</td>
			<td align="left" width ="65%">
				<select name="comboUserProfAccLvl" width=150 >
						<option name="cmbProfileEle1" value ="Postpaid">Postpaid</option>
						<option name="cmbProfileEle2" value ="Prepaid">Prepaid</option>
				</select>
			</td>

		</tr>
<tr>
<td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
</tr>
<tr>
<td></td>
<td>
<input type="image" border="0" name="RBT_SUB" src="images/activate.gif">
</td>
</tr>
</table>
</form>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>