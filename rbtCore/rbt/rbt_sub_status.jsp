<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub, java.net.*"%>
<%@ include file = "validate.jsp" %>
<%
String browse  = (String)(session.getAttribute("Browse"));
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
if (validateUser(request, session, "rbt_subscriber_status.jsp", response)) {%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
	if(document.forms[0].SUB_ID.disabled == false){
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
	if(document.forms[0].SUB_ID.disabled)
		alert('WARNING:Please do not refresh the page while your request is getting processed');
	return true;
}
</script>
<title>RBT Subscription</title>
</head>
<body onLoad = 'return fnSetFocus(frmStatus,0)'>
<form action="rbt_display_status.jsp" onsubmit='return update()' method="post" enctype="multipart/form-data" name="frmStatus">
	<p><b><center>View Subscriber Details</center></b></p>
<table align = "center" width ="100%">
<tr>
<td></td>
<td>
<% String strStatus, strReason;%>
<%  
	 
	String actionname = (String)session.getAttribute("Action");
    strStatus = (String)session.getAttribute("SubStatus");
	String strSubID = (String) session.getAttribute("Subscriber");
if(actionname != null){
    String caller = (String) session.getAttribute("Caller");
	String tmp = (String) session.getAttribute("STATUS");
	String initialtype = (String) session.getAttribute("INITIALTYPE");
	String finaltype = (String) session.getAttribute("FINALTYPE");
	String fromTimeTmp = (String) session.getAttribute("FROMTIME");
	if(fromTimeTmp == null || fromTimeTmp.trim().equalsIgnoreCase("-"))
         fromTimeTmp ="0";
	int fromTime = Integer.parseInt(fromTimeTmp);
	String toTimeTmp = (String) session.getAttribute("TOTIME");
	if(toTimeTmp == null || toTimeTmp.trim().equalsIgnoreCase("-"))
         toTimeTmp ="2359";
	int toTime = Integer.parseInt(toTimeTmp);
	String wavFile = (String) session.getAttribute("WAV_FILE"); 
    if(wavFile!= null && wavFile.trim().equalsIgnoreCase("null")) 
		   wavFile = null; 
    session.removeAttribute("WAV_FILE"); 


if(actionname.equalsIgnoreCase("REMOVE"))
	{
	int status = -1;
	try
	{
		status = Integer.parseInt(tmp);
	}
	catch(Exception e)
	{
		status = -1;
	}
	if(caller != null && status != -1)
	{

		if(caller.equalsIgnoreCase("ALL"))
			caller = null;

		rbtLogin.deactivateSubscriberRecords(strSubID, caller, status,fromTime,toTime,wavFile);
		
		out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Selection Removed Successfully.</b></font></center>");

		session.removeAttribute("Caller");
		session.removeAttribute("STATUS");
	}
 }
if(actionname.equalsIgnoreCase("MODIFY"))
	{
	if( initialtype != null && finaltype != null )
	{
	rbtLogin.convertSelectionClassType(strSubID, initialtype, finaltype);
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"left\"><b>charge duration converted successfully.</b></font></center>");
	session.removeAttribute("INITIALTYPE");
	session.removeAttribute("FINALTYPE");
	
	}
  }
}
else if(strStatus == "FAILURE"){
	strReason = (String)session.getAttribute("SubReason");
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>ERROR : "+strReason+".</b></font></center>");
}
else if(strStatus == "SUCCESS")
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Request completed successfully.</b></font></center>");
session.removeAttribute("SubStatus");
%>
</td>
</tr>
<tr>
</tr>
<tr></tr>
<tr>
<td width="30%"><%
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%><input type="radio" name="user" checked value ="subscriber" onClick = "document.frmStatus.SUB_ID.value='';document.frmStatus.subFile.value='';document.frmStatus.SUB_ID.disabled=false;document.frmStatus.subFile.disabled=true;"><%}%>Subscriber</td>
<td width="60%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
</tr>
<tr></tr>
<%
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%><tr>
<td width="30%"><input type="radio" name="user" value ="File" onClick = "document.frmStatus.SUB_ID.value='';document.frmStatus.subFile.value='';document.frmStatus.SUB_ID.disabled=true;document.frmStatus.subFile.disabled=false;">Subscriber File</td>
<td width="60%"><input type=file name="subFile" size = 32 maxlength=32 disabled></td>
</tr>
<%}%>
<tr></tr>
<tr></tr>
<tr></tr>
<tr></tr>
<tr></tr>
<tr>
<td></td>
<td><input type=submit border = "0" value="Submit" name="UPDATE"></td>
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