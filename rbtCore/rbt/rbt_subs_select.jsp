<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Categories"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.cache.content.ClipMinimal"%>
<%@ page import = "java.util.*,java.text.SimpleDateFormat"%>
<%@ include file = "validate.jsp" %>
<% 
int category_id;
String strSubID,subType,strValue,strResult;
%>
<%
String browse  = (String)(session.getAttribute("Browse"));
strSubID = null;subType = null;strValue = null;strResult = null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();

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
	   if(document.frmStatus.SUB_ID.disabled == false) { 
		   if(frmStatus.callerID.value == frmStatus.SUB_ID.value ){ 
				   alert("Enter valid Caller ID. Sub ID and Caller ID can't be same."); 
				   frmStatus.callerID.focus(); 
				   return false; 
		   } 
	   } 
    }
    if(document.frmStatus.fromTime.disabled == false){
    	if(document.frmStatus.toTime.disabled == false){
    		var frmTime = document.frmStatus.fromTime.value;
    		var tTime = document.frmStatus.toTime.value;

    		if(frmStatus.fromTime.value.length == 1)
    		{

    			frmTime = "0".concat(frmTime);
    		}
    		if(frmStatus.toTime.value.length == 1)
    		{

    			tTime = "0".concat(tTime);

    		}  

    		if(frmTime >= tTime){
    	
    			alert("The fromTime for a selection cannot be greater than equal to toTime.");
    			frmStatus.fromTime.focus();
    			return false;
    		}
    	}
    }
    <%if(rbtLogin.m_allowDayOfWeekFutureDate){%>
    	if(document.frmStatus.playDate.disabled == false){
    		if (isEmpty(frmStatus.playDate.value))
    		{
    			alert("Enter a Date in calendar");
		     	frmStatus.playDate.focus();
		     	return false;
    		}
   			else
    		{
    			var months = new Array();
    		    var curDate = new Date();
                var stDate = new Date();
                
                var stDateStr = frmStatus.playDate.value;

                var temp = new Array();
                temp = stDateStr.split('/');
                stDate.setFullYear(temp[2]);
                stDate.setMonth(temp[1]-1);
                stDate.setDate(temp[0]);

                if(stDate <= curDate)
                {
                                alert("Calendar Date should not be less or equal to the Current date");
                                frmStatus.playDate.focus();
                                return false;
                }
                
           	}
    		
    	
    	}
    <%}%>
    if(document.frmStatus.ADD_OR_GIFT[0].disabled == false){
    	if(document.frmStatus.ADD_OR_GIFT[0].checked == true){
    		if (isEmpty(frmStatus.SUBSCRIBER_ID.value))
		    {
			 alert("Enter a Giftee Number");
		     frmStatus.SUBSCRIBER_ID.focus();
		     return false;
			}
		 	else {
				if(!isFieldAPhoneNumber(frmStatus.SUBSCRIBER_ID.value)){
					alert("Enter valid giftee phone no.");
					frmStatus.SUBSCRIBER_ID.focus();
					return false;
				}
			}
			if(frmStatus.SUB_ID.value == frmStatus.SUBSCRIBER_ID.value ){ 
				   alert("Enter valid GIFTEE ID. Sub ID and GIFTEE ID can't be same."); 
				   frmStatus.SUBSCRIBER_ID.focus(); 
				   return false; 
		   } 
		}
    }
	document.frmStatus.UPDATE.value='Processing in progress...DO NOT REFRESH';
	return true;
}

function updateGift()
{
	document.frmStatus.SUBSCRIBER_ID.disabled=false;
	document.frmStatus.sub[1].disabled=true;
	document.frmStatus.subFile.disabled=true;
	document.frmStatus.sub[0].checked=true;
	document.frmStatus.user[0].disabled=true;
	document.frmStatus.user[1].disabled=true;
	document.frmStatus.SUB_ID.disabled=false;
	document.frmStatus.callerID.disabled=true;
	<%if(rbtLogin.m_allowGroupSelection){%>
		document.frmStatus.user[2].disabled=true;
		document.frmStatus.group.disabled=true;
	<%}%>
	<%if(rbtLogin.m_allowDayOfWeekFutureDate){%>
	
		document.frmStatus.dayAndDate[0].disabled=true;
		document.frmStatus.dayAndDate[1].disabled=true;
		document.frmStatus.dayAndDate[2].disabled=true;
		document.frmStatus.dayOfWeek.disabled=true;
		document.frmStatus.playDate.disabled=true;
		document.frmStatus.date.disabled=true;
	<%}%>
	<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE")){%>
		document.frmStatus.fromTime.disabled=true;
		document.frmStatus.toTime.disabled=true;
		document.frmStatus.Time[0].disabled=true;
		document.frmStatus.Time[1].disabled=true;
	<%}%>
	<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGING_MODEL_IN_GUI", "FALSE")){%>
	
		document.frmStatus.ChargingModel[0].disabled=true;
		document.frmStatus.ChargingModel[1].disabled=true;
	<%}%>
	<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBSCRIPTION_TYPE_IN_GUI", "FALSE")) { %>
		
		document.frmStatus.SubscriptionType[0].disabled=true;
		document.frmStatus.SubscriptionType[1].disabled=true;
	<%}%>
	<% if(rbtLogin.m_allowLooping){ %>
		document.frmStatus.ADDTOLOOP.disabled=true;
	<%}%>
	<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE")){%>
		document.frmStatus.blackout_sms.disabled=true;
	<%}%>

}

function addToSelection()
{
	document.frmStatus.SUBSCRIBER_ID.disabled=true;
	document.frmStatus.user[0].disabled=false;
	document.frmStatus.sub[1].disabled=false;
	document.frmStatus.sub[0].checked=true;
	document.frmStatus.user[1].disabled=false;
	document.frmStatus.SUB_ID.disabled=false;
	document.frmStatus.user[0].checked=true;
	<%if(rbtLogin.m_allowDayOfWeekFutureDate){%>
	
		document.frmStatus.dayAndDate[0].disabled=false;
		document.frmStatus.dayAndDate[1].disabled=false;
		document.frmStatus.dayAndDate[2].disabled=false;
		document.frmStatus.dayAndDate[0].checked=true;
	<%}%>
	<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE")){%>
		
		document.frmStatus.Time[0].disabled=false;
		document.frmStatus.Time[1].disabled=false;
		document.frmStatus.Time[0].checked=true;
	<%}%>
	<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGING_MODEL_IN_GUI", "FALSE")){%>
	
		document.frmStatus.ChargingModel[0].disabled=false;
		document.frmStatus.ChargingModel[1].disabled=false;
	<%}%>
	<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBSCRIPTION_TYPE_IN_GUI", "FALSE")) { %>
		
		document.frmStatus.SubscriptionType[0].disabled=false;
		document.frmStatus.SubscriptionType[1].disabled=false;
	<%}%>
	<%if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "BLACKOUT_SMS", "FALSE")){%>
		document.frmStatus.blackout_sms.disabled=false;
	<%}%>
}


</script>
<title>RBT Subscription</title>
</head>
<body>
<% String selected, strStatus, strReason, categoryName, songName;
	%>
<%
	categoryName = "";
	songName = "";

	String song = (String)session.getAttribute("song");
	String category = (String)session.getAttribute("category");
	String circleID = (String)session.getAttribute("circle");
	String prepaidYesStr = (String)session.getAttribute("prepaidyes");
	int catID = 0;
	try
	{
		catID = Integer.parseInt(category);
	}
	catch(Exception e)
	{
		catID = 0;
	}
	if(catID != 0 && song != null)
	{
		Categories cat = rbtLogin.getCategory(catID, circleID, (prepaidYesStr.equalsIgnoreCase("prepaid")) ? 'y' : 'n'); 
		ClipMinimal clip = rbtLogin.getClipRBT(song);
		System.out.println("The value of clip is:" +clip);
		if(cat != null && clip != null)
		{
			categoryName = cat.name();
			songName = clip.getClipName();
		}
	}
%>
<script language="JavaScript" src="javascripts/DatePicker.js"></script>
<form action ="rbt_update_sub_select.jsp"  method="post" enctype="multipart/form-data" name="frmStatus" >
	<p><b><center>Add Subscriber Selections</center></b></p>	
<p> The Category selected is <b><%=categoryName%></b> and Song selected is <b><%=songName%></b> <p>
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
<td width="20%" ><input type="radio" name="user" checked value ="all" onClick = "document.frmStatus.callerID.value='';document.frmStatus.callerID.disabled=true;document.frmStatus.group.disabled=true;">All</td>
<td width="20%"><input type="radio" name="user" value ="specific" onClick = "document.frmStatus.callerID.disabled=false;document.frmStatus.group.disabled=true;">Specific No</td>
<%if(rbtLogin.m_allowGroupSelection){%>
<td><input type="radio" name="user" value ="groupName" onClick = "document.frmStatus.group.disabled=false;document.frmStatus.callerID.disabled=true;">Group Name</td>
</tr>
<tr>
<td></td>
<%}%>
<td width="60%"><input type=text name="callerID" size = 32 maxlength=32 disabled ></td>
<%if(rbtLogin.m_allowGroupSelection){%>
<td><input type=text name="group" size = 32 maxlength=32 disabled ></td>
<%}%>
</tr>
<tr>
</tr>
<tr>
</tr>
<%
	if(rbtLogin.m_allowDayOfWeekFutureDate)
	{ 
%>
<tr>
<td width="20%" ><input type="radio" name="dayAndDate" checked value ="All Days" onClick = "document.frmStatus.dayOfWeek.disabled=true;document.frmStatus.playDate.disabled=true;document.frmStatus.date.disabled=true;document.frmStatus.Time[0].checked=true;document.frmStatus.Time[1].checked=false;document.frmStatus.Time[0].disabled=false;document.frmStatus.Time[1].disabled=false;">All Days</td>
<td width="30%" ><input type="radio" name="dayAndDate" value ="DayOfTheWeek" onClick = "document.frmStatus.dayOfWeek.disabled=false;document.frmStatus.playDate.disabled=true;document.frmStatus.date.disabled=true;document.frmStatus.fromTime.disabled=true;document.frmStatus.toTime.disabled=true;document.frmStatus.Time[0].disabled=true;document.frmStatus.Time[1].disabled=true;">Specific Day</td>
<td width="30%" ><input type="radio" name="dayAndDate" value ="FutureDate" onClick = "document.frmStatus.dayOfWeek.disabled=true;document.frmStatus.playDate.disabled=false;document.frmStatus.date.disabled=false;document.frmStatus.fromTime.disabled=true;document.frmStatus.toTime.disabled=true;document.frmStatus.Time[0].disabled=true;document.frmStatus.Time[1].disabled=true;">Future Date</td>
</tr>
<tr>
<td width="20%" ></td>
<td width="30%">
<select name="dayOfWeek" disabled>
<option value="W1">SUNDAY</option>
<option value="W2">MONDAY</option>
<option value="W3">TUESDAY</option>
<option value="W4">WEDNESDAY</option>
<option value="W5">THURSDAY</option>
<option value="W6">FRIDAY</option>
<option value="W7">SATURDAY</option>
</select>
</td>
<td width="30%" >
<%
	String date = null;
	Date currentDate = new Date(System.currentTimeMillis());
	SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
	date = format.format(currentDate);
%>
<input type="text" name="playDate" value="<%=date%>" style='text-align:left;' size=10 maxlength=10 disabled="false">
<input type="button" name="date" value="Calendar" onClick="javascript:show_calendar2('frmStatus.playDate');" onMouseOver="self.status='Get the calendar';return true" onmouseout="window.status='';return true;" disabled="false">
</td>
</tr>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE")) { %>
<tr> 
<td width="20%" ><input type="radio" name="Time" checked value ="FullDay" onClick = "document.frmStatus.fromTime.disabled=true;document.frmStatus.toTime.disabled=true;" >Full Day</td>
<td width="20%"><input type="radio" name="Time" value ="TimeOfTheDay" onClick = "document.frmStatus.fromTime.disabled=false;document.frmStatus.toTime.disabled=false;" >Time Of Day</td>
<td width="60%">
<select name="fromTime" disabled>
<% for(int i=0; i<=22; i++) {%> 
<option value=<%=i%>><%=i%></option>
<% } %>
</select>
<select name="toTime" disabled>
<% for(int i=1; i<=23; i++) {%> 
<option value=<%=i%>><%=i%></option>
<% } %>
</select>
</td>
</tr>
<%}} %>

<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE") && !rbtLogin.m_allowDayOfWeekFutureDate) { %>
<tr> 
<td width="20%" ><input type="radio" name="Time" checked value ="FullDay" onClick = "document.frmStatus.fromTime.disabled=true;document.frmStatus.toTime.disabled=true;" >Full Day</td>
<td width="20%"><input type="radio" name="Time" value ="TimeOfTheDay" onClick = "document.frmStatus.fromTime.disabled=false;document.frmStatus.toTime.disabled=false;" >Time Of Day</td>
<td width="60%">
<select name="fromTime" disabled>
<% for(int i=0; i<=22; i++) {%> 
<option value=<%=i%>><%=i%></option>
<% } %>
</select>
<select name="toTime" disabled>
<% for(int i=1; i<=23; i++) {%> 
<option value=<%=i%>><%=i%></option>
<% } %>
</select>
</td>
</tr>
<%} %>

<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGING_MODEL_IN_GUI", "FALSE")) { %>
<tr> 
<td width="20%" ><input type="radio" name="ChargingModel" checked value ="Monthly" >Monthly</td>
<td width="20%"><input type="radio" name="ChargingModel" value ="Weekly" >Weekly</td>
<td width="60%"> &nbsp; </td>
</tr>
<%}%>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBSCRIPTION_TYPE_IN_GUI", "FALSE")) { %>
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
<%
	if (rbtLogin.m_showGiftOptionInAddSelections){
%>
<tr>
<td width="30%"><input type="radio" name="ADD_OR_GIFT" value="GIFT" onClick = "javascript:updateGift();">Gift to : Subscriber Id</td>
<td width="60%"><input type="text" name="SUBSCRIBER_ID"  size = 32 maxlength=32 disabled></td>
<td width="30%"><input type="radio" name="ADD_OR_GIFT" value="ADD" checked  onClick = "javascript:addToSelection();" >Add to Selections</td>
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