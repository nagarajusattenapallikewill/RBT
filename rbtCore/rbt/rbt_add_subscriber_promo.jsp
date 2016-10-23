<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.StringTokenizer"%>

<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session,  "rbt_add_subscriber_promo_manager.jsp", response)) { %>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{   
    if(document.frmBulk.UPDATE.value != "Submit")
    {
		 alert("Processing in progress. Please do not refresh");
	     return false;
	}

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

	document.frmBulk.UPDATE.value='Processing in progress...DO NOT REFRESH';
	return true;
}	
</script>
<title>RBT Bulk Promotion</title>
</head>
<body>
<form action ="rbt_add_subscriber_promo_update.jsp"   method="post" enctype="multipart/form-data" name="frmBulk">
	<p><b><center>Add To Subscriber Promo</center></b></p>	
<table align = "center" width ="100%">
<tr>
<td></td>
<td>
<% 
	String flag =(String)(session.getAttribute("flag"));
	
	if( flag !=null && flag.equals("true"))
	{
		out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request completed successfully.</b></font>");
	}
	else if(flag !=null)
	{
		out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>"+flag+"</b></font>");
	}
	session.removeAttribute("flag");
%>
</td>
</tr>
<tr>
</tr>
<tr>
</tr>
<%
	String browse  = (String)(session.getAttribute("Browse"));
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>
<tr>
<td width = "30%">Subscriber File</td>
<td width="60%"><input type=file name="subFile" size = 32 maxlength=32 >
</td>
</tr>
<%}%>
<tr>
</tr>

<tr></tr>
<tr></tr>
<tr>	
		<td width = "30%">Subscriber Type</td>
		<td width ="60%">
		<select name="comboSubType">
		
		<option name="PostPaid" value ="PostPaid">Postpaid</option>
		<option name="PrePaid" value ="PrePaid">Prepaid</option>
		</select>
		</td>

</tr><tr>	
		<td width = "30%">Promo Type</td>
		<td width ="60%">
		<select name="comboPromoType">
		
		<option name="ICard" value ="ICARD">ICard</option>
		<option name="Youth Card" value ="YOUTHCARD">Youth Card</option>
		</select>
		</td>

</tr>
<tr>
</tr>
<tr>
</tr>
<tr></tr>
<tr></tr>
<tr>
<td></td>
<td><input type=submit border = "0" value="Submit" name="UPDATE" onClick = 'return update()'></td>
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