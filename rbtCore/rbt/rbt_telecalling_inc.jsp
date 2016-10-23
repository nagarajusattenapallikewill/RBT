<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Categories"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "java.util.*,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants"%>
<%@ include file = "validate.jsp" %>
<% 
int i,category_id;
Categories[] category;
Clips[] clip;
String categoryName, songName, sub, updated, callerID, type;
%>

<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
category = null;
clip = null;

if (validateUser(request, session, "rbt_telecalling.jsp", response)) { %>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">

function update()
{

	if(document.frmTel.SongName.value.length == 0)
	{
		alert("Cannot Add Selection As No Songs in this category");
		return false;
		
	}
	
	document.frmTel.SUB_ID.disabled = false;
	if(document.frmTel.SUB_ID.value.length == 0)
	{
		alert("Enter a Subscriber Number");
		return false;
	}

	if(document.frmTel.callerID.disabled == false){
		if (isEmpty(frmTel.callerID.value))
		    {
			 alert("Enter specific number");
		     frmTel.callerID.focus();
		     return false;
			}
		 else {
				if(!isFieldACallerID(frmTel.callerID.value)){
					alert("Enter valid Caller ID");
					frmTel.callerID.focus();
					return false;
				}
		 }
	}

	document.frmTel.updated.value = "false";
	return true;
}

</script>
<title>RBT Subscription</title>
</head>
<body>
<form action ="rbt_telecalling.jsp"  method="post" name="frmTel">
	<p><b><center>Telecalling</center></b></p>	
<table align = "center" width ="100%">
<tr>
<td width ="10%"></td>
<td colspan =2>
<%	String strStatus = null;
	strStatus = (String)session.getAttribute("updated");
	String strReason = (String)session.getAttribute("reason");
	sub = request.getParameter("SUB_ID");
	callerID = request.getParameter("callerID");
	type = request.getParameter("comboUserProfAccLvl");
	if((sub != null || callerID != null || type != null ) && (strStatus == null ||( !strStatus.equalsIgnoreCase("FAILURE") && !strStatus.equalsIgnoreCase("SUCCESS"))))
		strStatus = "";	

	if(sub != null && !sub.equals("") && !strStatus.equalsIgnoreCase("FAILURE") && !strStatus.equalsIgnoreCase("SUCCESS"))
	{
		com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriberP = Processor.getSubscriber(sub);
		sub = subscriberP.getSubscriberID();
		if (sub.length() < 7 || sub.length() > 15)
		{
			strStatus =  "FAILURE";
			strReason=  "Invalid Subscriber Number " + sub;
		}
		else if(!subscriberP.isCanAllow())
        {        
			strStatus =  "FAILURE";         
			strReason=  "No. " + sub + " is blacklisted.";
        }
		else if(subscriberP.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
        {        
			strStatus =  "FAILURE";         
			strReason=  "No. " + sub + " is suspended.";
        }
		else if(!subscriberP.isValidPrefix())
		{
			strStatus =  "FAILURE";
			strReason=  "Invalid prefix " + RBTSubUnsub.init().getSubscriberPrefix(sub);
		}
		else if (rbtLogin.isSubActive(sub))
		{
			strStatus =  "FAILURE";
			strReason=  "Subscriber Already Active " + sub;
		}
	}

	if(strStatus == "SUCCESS")
	{
		out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Completed Successfully.</b></font></center>");
	}
	
	session.removeAttribute("updated");
	session.removeAttribute("reason");
 
if(strStatus !=null && strStatus.equalsIgnoreCase("FAILURE"))
{
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>ERROR : "+strReason+".</b></font></center>");
}
%>
</td>
</tr>
<tr>
<td><input type=hidden name="updated" size = 32 maxlength=32 value ="true"></td>
</tr>
<tr></tr>
<tr></tr>
<tr></tr>
<tr></tr>
<%	categoryName = request.getParameter("categoryName");
	songName = request.getParameter("SongName");
	updated =  request.getParameter("updated");
	if(categoryName != null && songName != null && sub != null && type!= null && updated.equalsIgnoreCase("false"))
	{
		if(strStatus == null || (!strStatus.equalsIgnoreCase("FAILURE") && !strStatus.equalsIgnoreCase("SUCCESS")))
		{
			session.setAttribute("Telsub", sub);
			session.setAttribute("Telsong", songName);
			session.setAttribute("Telcategory", categoryName);
			session.setAttribute("Telcallerid", callerID);
			session.setAttribute("Teltype", type);
			%>
			<jsp:forward page="rbt_update_telecalling.jsp" />		
		<%
		}
	}
	category = rbtLogin.getAllCategories();
%>
<tr>
<td width="10%">Subscriber</td>
<% 
	if(sub !=null && strStatus !=null && !strStatus.equals("FAILURE") && !strStatus.equals("SUCCESS"))
		{%>
			<td width="20%"></td>
			<td width ="60%" ><input type=text name="SUB_ID" size = 32 maxlength=32 disabled value =<%=sub%> ></td>
		<%}else	{%>
			<td width="20%"></td>
			<td width = "60%" ><input type=text name="SUB_ID" size = 32 maxlength=32></td>
		<%}
%>
</tr>
<tr></tr>
<tr></tr>
<tr>
		<td width = "10%">Subscriber Type</td>
		<td width="20%"></td>
			<td width ="60%">
				<select name="comboUserProfAccLvl">
				<%	if(type !=null && type.equalsIgnoreCase("prepaid")&&strStatus !=null && !strStatus.equals("FAILURE") && !strStatus.equals("SUCCESS"))
	{%>
						<option name="cmbProfileEle2" value ="Prepaid">Prepaid</option>
						<option name="cmbProfileEle1" value ="Postpaid">Postpaid</option>
	<%} else {%>
						<option name="cmbProfileEle1" value ="Postpaid">Postpaid</option>
						<option name="cmbProfileEle2" value ="Prepaid">Prepaid</option>
	<%}%>
				</select>
			</td>

</tr>
<tr></tr>
<tr></tr>
<tr>
</tr>
<tr> 
<td width="10%" ><input type="radio" name="user" checked value ="all" onClick = "document.frmTel.callerID.value='';document.frmTel.callerID.disabled=true;">All</td>
<td width="20%"><input type="radio" name="user" value ="specific" onClick = "document.frmTel.callerID.disabled=false;">Specific No</td>
<%if(callerID != null && strStatus !=null && !strStatus.equals("FAILURE") && !strStatus.equals("SUCCESS"))
	{%>
<td width="60%"><input type=text name="callerID" size = 32 maxlength=32 value = <%=callerID%> disabled ></td>
<%} else {%>
<td width="60%"><input type=text name="callerID" size = 32 maxlength=32 disabled ></td>
<%}%>
</tr>
<tr></tr>
<tr></tr>
<tr>
<td width="10%" >Category</td>
<td width="20%"></td>
<td width="60%" >	
<%
		if (category != null)
	 	{			
%>
		   <select name="categoryName" onChange="fnChangeTel(frmTel)">
<%	 	   
		   for(i=0; i <category.length; i++){
			   if(categoryName !=null && Integer.parseInt(categoryName) == category[i].id()){
				   
				   if(category[i].classType() != null && category[i].classType().equalsIgnoreCase("DEFAULT"))
					{ %>
				<option selected value='<%=category[i].id()%>'><%=category[i].name()%></option>
				<%}
					else {%>
						<option selected value='<%=category[i].id()%>'>* <%=category[i].name()%></option>
					<%}
						
					   continue;
				}
			 if(category[i].type() !=6 && category[i].type() !=3 && category[i].type() !=4  && category[i].id() != 1 && category[i].id() != 2 && category[i].id() != 3 && category[i].id() != 4)  {
				   if(category[i].classType() != null && category[i].classType().equalsIgnoreCase("DEFAULT"))
					{%>
				<option value='<%=category[i].id()%>'><%=category[i].name()%></option>
				<%}
					else {%>
						<option value='<%=category[i].id()%>'>* <%=category[i].name()%></option>
					<%}}
			}
	 	}
	 	
	 	else
	 	{
%>
          <select name="categoryName" disabled>
		 <option value=0 disabled>No Catgories Present</option>
<%
	 	 }

%>
</select>
</td>
</tr>

<tr>
</tr>

<tr>

<td width="10%" >Song</td>
<td width="20%"></td>
<td width="60%" >
		
<%
	 	if (categoryName != null)
	 	{
			clip  = rbtLogin.getAllClips(categoryName);
		}
		else
		{	
			if(category != null)
			clip = rbtLogin.getAllClips(String.valueOf(category[0].id()));
		}
			if(clip !=null){
%> 
	 			<select name="SongName">
<%				for(i=0; i < clip.length; i++) {
				if(songName !=null && songName.equalsIgnoreCase(clip[i].wavFile())){%>
				   
				<option selected value='<%=clip[i].wavFile()%>'><%=clip[i].name()%>
						<%
					   continue;
				}%>	
					<option value='<%=clip[i].wavFile()%>'><%=clip[i].name()%>
<%				} 
	 		}else
	 			{
%>
   				<select name="SongName" disabled>
				<option value="" disabled>No Songs
<%
	 			}
%>
</select>
</td>
<tr>
</tr>
<tr></tr><tr></tr><tr></tr>

<table width ="58%" cols="1" align="center">
<tr>
<td></td>
<td align="center"><input type=image border = "0" name="SUB_STATUS" src = "images/update1.gif" onClick = 'return update()'></td>
</tr>
</table>
<tr>
</tr>
<tr></tr>
<tr></tr>
<tr></tr>
<tr></tr>
<tr></tr>
<table width ="48%" cols="1" align="center" border="1">
<tr>
<td align="left">* against category represents premium category</td>
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