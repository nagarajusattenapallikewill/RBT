<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "java.util.StringTokenizer"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>

<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session,  "rbt_pre_promotion.jsp", response)) {
session.removeAttribute("Status");
session.removeAttribute("Reason");
%>
<HTML>
<head>
<title>RBT PRE Promotion</title>
</head>
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
	
	if(isEmpty(frmBulk.preFile.value)){
		 alert("Enter a Prepaid File");
	     frmBulk.preFile.focus();
	     return false;
	}else{
			if(frmBulk.preFile.value.indexOf(".txt") == -1){
			alert("Enter only text file");
			frmBulk.preFile.focus();
			return false;
		}
	}
	alert('WARNING:Please do not refresh the page while your request is getting processed');
	return true;
}	
</script>
<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0">
<table border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
	<tr>
		<td colspan=3 valign="top" height="15%">
			<jsp:include page="rbt_header_inc.jsp"/>
		</td>
	</tr>

	<tr>
		<td colspan=3 valign="top" height="85%">
			<table border="0" width="100%" cellspacing="0" cellpadding="0" height="100%">
				<tr>
					<!--Menu Start -->
					<td width="23%" bgcolor="#ffdec8" valign="top">
						<jsp:include page="rbt_menu_inc.jsp" />
					</td>
					<!--Menu End -->
					
					<!--Console Start  -->
					<!-- initialise global variable fix for weblogic 6.0 -->
					
					<td width="77%" bgcolor="#ffedd9" valign="top">
			      			<br><br>
							<b><center>Add Promotion</center></b>

						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<form action ="rbt_pre_promotion_update.jsp"   method="post" enctype="multipart/form-data" name="frmBulk">
							<table align=center width="77%">
			      <% /*
						String flag =(String)(session.getAttribute("flag"));
	
					  if( flag !=null && flag.equals("pre")){
						out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request completed successfully.</b></font>");
						}else if(flag !=null){
						out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>"+flag+"</b></font>");
						
						}
					session.removeAttribute("flag");
					*/%>
					<br><br>
<%
	String browse  = (String)(session.getAttribute("Browse"));
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>

<tr>
<td width = "30%">Prepaid File</td>
<td width="60%"><input type=file name="preFile" size = 32 maxlength=32 >
</td>
</tr>
<tr>
</tr>
<tr>
<td width = "30%">Subscriber File</td>
<td width="60%"><input type=file name="subFile" size = 32 maxlength=32 >
</td>
</tr>
<tr>
<%}%>
</tr>
<tr></tr>
<tr></tr>
<tr>	
		<td width = "30%">Activated By</td>
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
<tr>
</tr>
<tr>
</tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE"))
	{
%>
<tr>
<td colspan =2><input type=checkbox name="blackout_sms" checked = true >Do not send SMS during Blackout period</td>
</tr>
<%
	}
%>
<tr></tr>
<tr></tr>
<tr>
<td></td>
<td><input type=submit border = "0" value="Submit" name="UPDATE" onClick = 'return update()'></td>
</tr>
</table>
</form>

							</tr>
						</table>
						<!--Welcome Image -->

					</td>
					<!--Console End -->
				</tr>
			</table>
		</td>
	</tr>

</table>
</body>
</HTML>
<%
}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>
