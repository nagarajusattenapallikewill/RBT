<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>

<%@ page import = "java.util.*"%>
<%@ include file = "validate.jsp" %>
<%
String browse  = (String)(session.getAttribute("Browse"));
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
if (validateUser(request, session,  "rbt_subscribe_manager.jsp", response)) {%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
    if(document.frmRBT.UPDATE.value != "Activate")
    {
		 alert("Processing in progress. Please do not refresh");
	     return false;
	}

 	    if(document.forms[0].SUB_ID.disabled == false){
		if (isEmpty(frmRBT.SUB_ID.value))
		    {
			 alert("Enter a Subscriber Number");
		     frmRBT.SUB_ID.focus();
		     return false;
			}
		 else {
				if(!isFieldAPhoneNumber(frmRBT.SUB_ID.value)){
					alert("Enter valid phone no.");
					frmRBT.SUB_ID.focus();
					return false;
				}
		 }
	}else {
		if(isEmpty(frmRBT.subFile.value)){
			 alert("Enter a Subscriber File");
		     frmRBT.subFile.focus();
		     return false;
		}else{
				if(frmRBT.subFile.value.indexOf(".txt") == -1){
				alert("Enter only text file");
				frmRBT.subFile.focus();
				return false;
				}
		}
	}
	
	if(document.frmRBT.shownUpgrade.value=='true' && document.frmRBT.SUB_ID.value != '') {
		if(!confirm('Do you want to activate/upgrade user to ' + document.frmRBT.upgrade.value)) {
			return false;
		}
	}
	document.frmRBT.UPDATE.value='Processing in progress...DO NOT REFRESH';
	return true;
}
</script>

<title>RBT Subscription</title>
</head>
<body>
<form action="rbt_CC.jsp"  method="post" enctype="multipart/form-data" name="frmRBT">
	<p><b><center>Activation</center></b></p>

<table align = "center" width ="100%">
<tr>
<td></td>
<td>
<% String strStatus = null ;String strReason = null;%>
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
<tr></tr><tr></tr><tr></tr><tr></tr>
<tr>
<td width="30%"><%if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%><input type="radio" name="user" checked value ="subscriber" onClick = "document.frmRBT.SUB_ID.value='';document.frmRBT.subFile.value='';document.frmRBT.SUB_ID.disabled=false;document.frmRBT.subFile.disabled=true;"><%}%>Subscriber</td>
<td width="60%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
</tr>
<tr></tr>
<%	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%><tr>
<td width="30%"><input type="radio" name="user" value ="File" onClick = "document.frmRBT.SUB_ID.value='';document.frmRBT.subFile.value='';document.frmRBT.SUB_ID.disabled=true;document.frmRBT.subFile.disabled=false;">Subscriber File</td>
<td width="60%"><input type=file name="subFile" size = 32 maxlength=32 disabled></td>
</tr>
<%}%>
<tr>
</tr>
<tr></tr>
<tr>
		<td width = "30%">Subscriber Type</td>
			<td width ="60%">
				<select name="comboUserProfAccLvl">
						<option name="cmbProfileEle1" value ="Postpaid">Postpaid</option>
						<option name="cmbProfileEle2" value ="Prepaid">Prepaid</option>
				</select>
			</td>

</tr>
<tr>
<tr></tr>
<tr>	<td width = "30%">Activated By</td>
		<td width ="60%">
		<select name="comboActBy">
<%
		String act_by = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "ACTIVATED_BY", null);
		
		if(act_by != null){
			StringTokenizer stk = new StringTokenizer(act_by,",");

			while(stk.hasMoreTokens()){
				String tmp = stk.nextToken();%>
						<option name="<%=tmp%>" value ="<%=tmp%>"><%=tmp%></option>
				<%}
		}else {%> 
			<option name="NA" value ="NA" disabled> NA </option>
		<%}%>
		</select>
		</td>

</tr>
<tr></tr><tr></tr><tr></tr>
<%
	SubscriptionClass[] subClasses = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClassesGUI(true).toArray(new SubscriptionClass[0]);
	if(subClasses != null && RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_UPGRADE", "FALSE")) {%>
	<input type="hidden" name="shownUpgrade" value="true">
	<tr><td>Upgradation Packs</td><td>
	<select name="upgrade">
	<option name="<%="DEFAULT"%>" value="<%="DEFAULT"%>"><%=rbtLogin.getActivationClassdetails("DEFAULT")%></option>
		<%
			for(int j = 0; j < subClasses.length; j++) {%>
				<option name="<%=subClasses[j].getSubscriptionClass()%>" value ="<%=subClasses[j].getSubscriptionClass()%>"><%=rbtLogin.getActivationClassdetails(subClasses[j].getSubscriptionClass())%></option>
			<%}
		%>
		</select></td>
		</tr><tr></tr>
	<%}
		else {%>
			<input type="hidden" name="shownUpgrade" value="false">
<%		}
	if (!true) //if(!RBTCommonConfig.getInstance().useSubscriptionManager()) 
   { %> 
<tr>
<td colspan=2><input type=checkbox name="SEND_HLR" checked = true>Send Request to HLR</td>
</tr>
<% } %>
<tr></tr><tr></tr><tr></tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE"))
	{
%>
<tr>
<td colspan =2><input type=checkbox name="blackout_sms" checked = true >Do not send SMS during Blackout period</td>
</tr>
<%
	}
%>
<tr>
<td></td>
<td><input type=submit border = "0" value="Activate" name="UPDATE" onClick = 'return update()'></td>
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