<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Categories"%>
<%@ page import = "java.util.*"%>
<%@ include file = "validate.jsp" %>
<%
int i,category_id;
String strSubID = null;
String subType = null;
String strValue = null;
String strResult = null;
SortedMap clip;
String callid = null;
String tune = null;
String flag = null;
%>
<%
String browse  = (String)(session.getAttribute("Browse"));
strSubID=null;subType=null;strValue=null;strResult=null;
clip = null;
callid = null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
RBTMOHelper rbtMO = RBTMOHelper.init();
SubscriptionClass[] subClasses = null;
ChargeClass[] chaClasses = null;
subClasses = rbtLogin.getSubscriptionClassesGUI();
chaClasses = rbtLogin.getChargeClassesGUI();

if (validateUser(request, session,  "rbt_sms_promo_tunes_bulk.jsp", response)) { %>
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
	document.frmStatus.UPDATE.value='Processing in progress...DO NOT REFRESH';
	return true;
}	
</script>
<title>RBT Subscription</title>
</head>
<body>
<form action ="rbt_sms_tunes_update_bulk.jsp"   method="post" enctype="multipart/form-data" name="frmStatus">
	<p><b><center>Add Bulk SMS Promotion Selections</center></b></p>	
					<table align=center width="100%">
					<tr>
					<td></td>
					<td>
			      <% 
						flag =(String)(session.getAttribute("flag"));
						tune =(String)(session.getAttribute("tune"));

					if(tune !=null && tune.equals("false"))
						{out.print("<center><font face=\"Arial\" size=\"3\"  color = \"red\" align =\"center\"\t \t> <b> No tunes under this category.</b></font></center>");
						}
					  if( flag !=null && flag.equals("true")){
						out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request completed successfully.</b></font>");
						}else if(flag !=null){
						out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>"+flag+"</b></font>");
						
						}
					session.removeAttribute("flag");
					session.removeAttribute("tune");
					%>
					</td>
					</tr>
<tr>
</tr>
<tr>
</tr>
<tr></tr>
<tr>
<td width="30%">
<%
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>

<input type="radio" name="user" checked value ="subscriber" onClick = "document.frmStatus.SUB_ID.value='';document.frmStatus.subFile.value='';document.frmStatus.SUB_ID.disabled=false;document.frmStatus.subFile.disabled=true;"><%}%>Subscriber</td>
<td width="60%"><input type=text name="SUB_ID" size = 32 maxlength=32></td>
</tr>
<tr></tr>
<%
	if(browse != null && browse.equalsIgnoreCase("TRUE"))
	{%>

<tr>
<td width="30%"><input type="radio" name="user" value ="File" onClick = "document.frmStatus.SUB_ID.value='';document.frmStatus.subFile.value='';document.frmStatus.SUB_ID.disabled=true;document.frmStatus.subFile.disabled=false;">Subscriber File</td>
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
<tr>
</tr>
<tr>

<td width="30%" >SMS Promo Selection</td>
<td width="60%" > 
		
<%
	 
			clip  = rbtMO.getSMSPromoClips();
			if(clip !=null || !clip.isEmpty())
			{
				Iterator it = clip.keySet().iterator();
		
%>
	 			<select name="SongName">
				<option value='null'>No Selection
<%				while(it.hasNext()) { String tmp = (String) it.next();%>
					
					<option value='<%=clip.get(tmp)%>'><%=tmp%>
<%				} 
	 		}else
	 			{

	%>
   				<select name="SongName" disabled>
				<option value="" disabled>No SMS Promo Selections
			<%}%>
	 			
</select>
</td>

</tr>
<tr></tr>
<tr></tr>
<tr>
<td colspan =2><input type=checkbox name="IGNORE_ACTIVE" checked = true>Ignore Active Subscribers</td>
</tr>
<tr></tr>

<%

if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "FREE_ACTIVATION", "FALSE")) { %>
<tr></tr>
<tr>
<td colspan=2> Activation Class
<% if(subClasses == null || subClasses.length <= 0)
	{ %>
		<select name="SUBCLASS" disabled>
			<option >No Subscription Class</option>
	<%} 
	else 
	{ %>
		<select name="SUBCLASS">
		<%
		for( i = 0; i < subClasses.length; i++ )
		{ %>
			<option  value = <%=subClasses[i].getSubscriptionClass()%> >  <%=rbtLogin.getActivationClassdetails(subClasses[i].getSubscriptionClass())%> </option>
		<% }
	}%>
		</select></td>
</tr>
<tr></tr>
<tr>
<td colspan=2> Selection Class
<% if(chaClasses == null || chaClasses.length <= 0 )
   { %>
		<select name="CHARGECLASS" disabled>
			<option>No Charge Class </option>
   <% }
   else 
   { %>
		<select name="CHARGECLASS" >
		<% for( i = 0; i < chaClasses.length; i++ )
		{ %>
			<option value = <%=chaClasses[i].getChargeClass()%> ><%=rbtLogin.getChargeClassDetails(chaClasses[i].getChargeClass())%></option> 
		<% }
   }%>
		</select></td>
</tr>
<tr></tr>

<% } %>
<tr></tr>
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