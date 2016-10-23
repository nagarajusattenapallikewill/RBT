<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.StringTokenizer"%>

<%@ include file = "validate.jsp" %>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
SubscriptionClass[] subClasses = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClassesGUI(true).toArray(new SubscriptionClass[0]);
ChargeClass[] chargeClasses = CacheManagerUtil.getChargeClassCacheManager().getChargeClassesGUI(true).toArray(new ChargeClass[0]);
if (validateUser(request, session,  "rbt_bulk_selection_manager.jsp", response)) { %>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{   
	if(isEmpty(frmBulk.subFile.value)){
		 alert("Enter a Subscriber File");
	     frmBulk.subFile.focus();
	     return false;
	}else{
			if(frmBulk.subFile.value.indexOf(".txt") == -1){
			alert("Enter only text file");
			frmBulk.subFile.focus();
			return false;
		}
	}
	alert('WARNING:Please do not refresh the page while your request is getting processed');
	return true;
}	

</script>
<title>RBT Bulk Selections</title>
</head>
<body>
<form action ="rbt_bulk_selection_update.jsp"   method="post" enctype="multipart/form-data" name="frmBulk">
	<p><b><center>Add Bulk Selections</center></b></p>	
					<table align=center width="100%">
					<tr>
					<td align ="center" colspan =2>
			      <% 
					/*	String flag =(String)(session.getAttribute("flag"));
					System.out.println("from bulk flag "+flag);
					  if( flag !=null && flag.equals("bulk")){
						out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request completed successfully.</b></font>");
						}else if(flag !=null){
						out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>"+flag+"</b></font>");
						
						}
					session.removeAttribute("flag");*/
					%>
					</td>
					</tr>
					</table>

<table cols = 3 align = "center">
<tr>
<td colspan=3>
</td>
</tr>
<%
	String browse  = (String)(session.getAttribute("Browse"));
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>
<tr>
<td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
</tr>
<tr>
<td width = "35%">&nbsp;&nbsp;&nbsp;Subscriber File</td>
<td width="65%"><input type=file name="subFile" size = 32 maxlength=32 >
</td>
</tr>
<%}%>
<tr>
</tr>

<tr></tr>
<tr></tr>
<tr>	
		<td width = "35%">&nbsp;&nbsp;&nbsp;Activated By</td>
		<td align="left" width ="65%">
		<select name="comboActBy" width=150 >
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
<tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "FREE_ACTIVATION", "FALSE")) { %>
<tr></tr>
<tr>
		<td width = "35%">&nbsp;&nbsp;&nbsp;Activation Class</td>
		<td align="left" width ="65%">

<% if(subClasses == null || subClasses.length <= 0)
	{ %>
		<select name="SUBCLASS" disabled>
			<option >No Subscription Class</option>
	<%} 
	else 
	{ %>
		<select name="SUBCLASS">
		<%
		for( int i = 0; i < subClasses.length; i++ )
		{ %>
			<option  value = <%=subClasses[i].getSubscriptionClass()%> >  <%=rbtLogin.getActivationClassdetails(subClasses[i].getSubscriptionClass())%> </option>
		<% }
	}%>
		</select></td>
</tr>
<tr></tr>
 <tr></tr>
<tr>
		<td width = "35%">&nbsp;&nbsp;&nbsp;Selection Class</td>
		<td align="left" width ="65%">

<% if(chargeClasses == null || chargeClasses.length <= 0)
	{ %>
		<select name="CHARGECLASS" disabled>
			<option >No Selection Class</option>
	<%} 
	else 
	{ %>
		<select name="CHARGECLASS">
		<%
		for( int i = 0; i < chargeClasses.length; i++ )
		{ try{%>
			<option  value = <%=chargeClasses[i].getChargeClass()%> >  <%=rbtLogin.getChargeClassDetails(chargeClasses[i].getChargeClass())%> </option>
		<% }catch(Exception e)
			{System.out.println(e);}}
	}%>
		</select></td>
</tr>
<tr></tr>

<% } %>

</tr>
<tr>
</tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE"))
	{
%>
<tr>
<td colspan =2>&nbsp;&nbsp;&nbsp;<input type=checkbox name="blackout_sms" checked = true >Do not send SMS during Blackout period</td>
</tr>
<%
	}
%>
<tr></tr>
<tr></tr>
<tr>
<td colspan ="3" align="center"><input type=submit border = "0" value="Submit" name="UPDATE" onClick = 'return update()'></td>
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