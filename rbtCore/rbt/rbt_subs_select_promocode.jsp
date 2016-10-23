<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.*"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Categories"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.cache.content.ClipMinimal"%>
<%@ page import = "java.util.*"%>
<%@ include file = "validate.jsp" %>
<% 
int category_id;
String strSubID,subType,strValue,strResult;
%>
<%
String browse  = (String)(session.getAttribute("Browse"));
strSubID = null;subType = null;strValue = null;strResult = null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
RBTMOHelper rbtMO = RBTMOHelper.init();
String promoCode = request.getParameter("promoCode");
if (validateUser(request, session,  "rbt_subs_selections.jsp", response)) { %>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">

function update()
{
    if(document.frmStatus.UPDATE.value != "Submit")
    {
		 alert("Processing in progress. Please do not refresh");
	     return false;
	}

	if(document.frmStatus.callerID.disabled == false){
		if (isEmpty(frmStatus.callerID.value))
		    {
			 alert("Enter specific number");
		     frmStatus.callerID.focus();
		     return false;
			}
		 else {
				if(!isFieldACallerID(frmStatus.callerID.value)){
					alert("Enter valid Caller ID");
					frmStatus.callerID.focus();
					return false;
				}
		 }
	}

	if(document.frmStatus.SUB_ID.disabled == false)
	{
		if (isEmpty(frmStatus.SUB_ID.value))
		    {
			 alert("Enter a Subscriber Number");
		     frmStatus.SUB_ID.focus();
		     return false;
			}
		 else {
				if(!isFieldAPhoneNumber(frmStatus.SUB_ID.value)){
					alert("Enter valid phone no.");
					frmStatus.SUB_ID.focus();
					return false;
				}
		 }
	}else {
		if(isEmpty(frmStatus.subFile.value)){
			 alert("Enter a Subscriber File");
		     frmStatus.subFile.focus();
		     return false;
		}else{
				if(frmStatus.subFile.value.indexOf(".txt") == -1){
				alert("Enter only text file");
				frmStatus.subFile.focus();
				return false;
				}
		}
	}
	
	if(document.frmStatus.callerID.disabled == false){
		if(document.frmStatus.SUB_ID.disabled == false)
		{
			if(frmStatus.callerID.value == frmStatus.SUB_ID.value ){
				alert("Enter valid Caller ID. Sub ID and Caller ID can't be same.");
				frmStatus.callerID.focus();
				return false;
			}
		}
	}
	document.frmStatus.UPDATE.value='Processing in progress...DO NOT REFRESH';
	return true;
}

</script>
<title>RBT Subscription</title>
</head>
<body>
<% String selected, strStatus, strReason,  songName;
	%>
<%
	songName = null;
	if(promoCode != null)
	{
		ClipMinimal cMin = rbtMO.getClipPromoID(promoCode);
		if(cMin != null)
			songName = cMin.getWavFile();
	}
	if(songName == null){
				session.setAttribute("updated","FAILURE");
				session.setAttribute("reason","Internal Error.");
			
		%>
		<jsp:forward page="rbt_subs_selections.jsp" />
		<%
	}
%>
<form action ="rbt_update_sub_select_promocode.jsp"  method="post" enctype="multipart/form-data" name="frmStatus" >
	<p><b><center>Add Subscriber Selections</center></b></p>	
<p> The  Song selected is <b><%=songName%></b> <p>
<table align = "center" width ="100%">
<tr></tr>
<tr></tr>
<tr> 
<td width="40%">
<%
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>

<input type="radio" name="sub" checked value ="subscriber" onClick = "document.frmStatus.SUB_ID.value='';document.frmStatus.subFile.value='';document.frmStatus.SUB_ID.disabled=false;document.frmStatus.subFile.disabled=true;"><%}%>Subscriber</td>
<td width="60%" colspan=2><input type=text name="SUB_ID" size = 32 maxlength=32></td>
</tr>
<tr></tr>
<%
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>

<tr> 
<td width="40%"><input type="radio" name="sub" value ="File" onClick = "document.frmStatus.SUB_ID.value='';document.frmStatus.subFile.value='';document.frmStatus.SUB_ID.disabled=true;document.frmStatus.subFile.disabled=false;">Subscriber File</td>
<td width="60%" colspan=2><input type=file name="subFile" size = 32 maxlength=32 disabled></td>
</tr>
<%}%>
<table width ="100%" align="center">
<tr>
</tr>
<tr> 
<td width="20%" ><input type="radio" name="user" checked value ="all" onClick = "document.frmStatus.callerID.value='';document.frmStatus.callerID.disabled=true;">All</td>
<td width="20%"><input type="radio" name="user" value ="specific" onClick = "document.frmStatus.callerID.disabled=false;">Specific No</td>
<td width="60%"><input type=text name="callerID" size = 32 maxlength=32 disabled ></td>
</tr>
<tr>
</tr>
<tr>
</tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE")) { %>
<tr> 
<td width="20%" ><input type="radio" name="Time" checked value ="FullDay" onClick = "document.frmStatus.fromTime.disabled=true;document.frmStatus.toTime.disabled=true;">Full Day</td>
<td width="20%"><input type="radio" name="Time" value ="TimeOfTheDay" onClick = "document.frmStatus.fromTime.disabled=false;document.frmStatus.toTime.disabled=false;">Time Of Day</td>
<td width="60%">
<select name="fromTime" disabled>
<% for(int i=0; i<=24; i++) {%> 
<option value=<%=i%>><%=i%></option>
<% } %>
</select>
<select name="toTime" disabled>
<% for(int i=0; i<=24; i++) {%> 
<option value=<%=i%>><%=i%></option>
<% } %>
</select>
</td>
</tr>
<% } %>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGING_MODEL_IN_GUI", "FALSE")) { %>
<tr> 
<td width="20%" ><input type="radio" name="ChargingModel" checked value ="Monthly" >Monthly</td>
<td width="20%"><input type="radio" name="ChargingModel" value ="Weekly" >Weekly</td>
<td width="60%"> &nbsp; </td>
</tr>
<tr> 
<td width="20%" ><input type="radio" name="SubscriptionType" value ="OPTIN" >Optin</td>
<td width="20%"><input type="radio" name="SubscriptionType" checked value ="OPTOUT" >Optout</td>
<td width="60%"> &nbsp; </td>
</tr>
<% } %>
<tr></tr>
<% if(rbtLogin.m_allowLooping) { %>
<tr> 
<td colspan =3>  <input type="checkbox" name = "ADDTOLOOP" >  Add selection in loop</td>
</tr>
<% } %>
<tr></tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE"))
	{
%>
<tr>
<td colspan =3><input type=checkbox name="blackout_sms" checked = true >Do not send SMS during Blackout period</td>
</tr>
<%
	}
%>
</table>
<tr>
</tr>
<tr>
</tr>
<tr></tr>
<tr></tr>
<tr></tr>
<tr></tr>
<table width ="58%" cols="1" align="center">
<tr>
<td align="center"><input type=submit value="Submit" name="UPDATE" onClick = 'return update()'></td>
<td align="center"><input type=hidden value=<%=songName%> name="songName" onClick = 'return update()'></td>
</tr>
</table>
</table>
</form>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>