
<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
<%@ page import = "com.onmobile.apps.ringbacktones.common.Tools"%>
<%@ page import = "java.util.StringTokenizer"%>
<%@ page import = "java.util.*"%>
<%@ include file = "validate.jsp" %>


<%
try
{
String enableSMSHistory  = (String)(session.getAttribute("SMSHistory"));
String strUserPermission  = (String)(session.getAttribute("Permission"));
String strUser  = (String)(session.getAttribute("UserId"));
if (validateUser(request, session, "rbt_subscriber_status.jsp", response)) {
	System.out.println("session validated");
    System.out.println("Status Checked For A Subscriber By User " + strUser);
%>

<% String strSubID, strResult, strValue; 
String X_ONMOBILE_STATUS;
String X_ONMOBILE_FAILURE_REASON;
String FAILURE;
String SUCCESS;
String strDate;
String strReason;
String strActby;
String prepaid;
String lastChargeDate;
String amountCharged;
String introPromptStatus;
String strSubInfo;
Subscriber dispSub;
%>

<p><b><center>View Subscriber Details</center></b></p>	

<%	

X_ONMOBILE_STATUS = null;
X_ONMOBILE_FAILURE_REASON = null;
FAILURE = "FAILURE";
SUCCESS = "SUCCESS";
strDate = null;
strReason = null;
strActby = null;
prepaid = null;
lastChargeDate = null;
amountCharged = "-";
introPromptStatus = "-";
strSubInfo = "-";
dispSub = null;

RBTSubUnsub rbtStatus = RBTSubUnsub.init();
%>

<%
strSubID = (String)session.getAttribute("Subscriber");
int count=0;
%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
		<%
		long a = 0;
		count+=1;
		if (count>1 && RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_VALIDITY_CC", "FALSE")){
		a=rbtStatus.getNoOfDaysLeftPrompt(strSubID);
		}
		%>
		alert ("Number of days left for <%=strSubID%> is <%=a%>" );
}
function getHuaweiStatus()
{
	<%
		String huaweiStatus = rbtStatus.getHuaweiStatusForSub(strSubID);
	%>
	alert ("Status in Huawei for subscriber <%=strSubID%> is \"<%=huaweiStatus%>\"" );
}
</script>
</head>
</html>
<%
	dispSub = rbtStatus.getSubscriber(strSubID);
	String strValidSub = rbtStatus.isValidSub(strSubID, dispSub);
//if(!strValidSub.equals("success"))
	String smsImpl = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_IMPLEMENTATION", null); 
   if(strValidSub.equals("failure") || (strValidSub.equals("blacklisted") && (smsImpl == null || !smsImpl.equalsIgnoreCase("ESIASMSImpl")))) 

{
    session.removeAttribute("Subscriber");
	X_ONMOBILE_STATUS = FAILURE;
	if(strValidSub.equals("failure"))
		X_ONMOBILE_FAILURE_REASON = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(strSubID);
	else if (strValidSub.equals("blacklisted"))
        X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is blacklisted.";
	//else if (strValidSub.equals("suspended"))
      //  X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is suspended.";
	session.setAttribute("SubStatus", X_ONMOBILE_STATUS);
	session.setAttribute("SubReason", X_ONMOBILE_FAILURE_REASON);
%>
    <jsp:forward page ="rbt_subscriber_status.jsp" />
<%
}
else
{
    strResult = rbtStatus.subscriberStatus(strSubID, dispSub); 
	StringTokenizer st = new StringTokenizer(strResult, ",");
	if (st.hasMoreTokens())
    {
        strValue = st.nextToken();
		if(strValue.equalsIgnoreCase("Subscriber does not exists: "))
        {
            session.setAttribute("SubStatus","FAILURE");
			session.setAttribute("SubReason", strValue+strSubID);
%>
        <jsp:forward page ="rbt_subscriber_status.jsp" />
<%
        }
		strDate = strValue;
        if(st.hasMoreTokens())
            strActby = st.nextToken();
        if(st.hasMoreTokens())
            strReason = st.nextToken();
        if(st.hasMoreTokens())
            prepaid = st.nextToken();
        if(st.hasMoreTokens())
            lastChargeDate = st.nextToken();
		if(st.hasMoreTokens())
			strSubInfo = st.nextToken(); 
    }
}
%>

<table cols = "7" align="center" border="1" cellpadding="5" cellspacing="1">
<tr>
<th colspan="7" align="center">Subscriber Status</th>
</tr>
<tr>
    <th>Subscriber ID</th>
<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_SUB_CHARGE_DATE", "TRUE")) { %>

    <th>Subscriber Type</th>
	<%}%>
    <th>Subscribed On</th>
    <th>Activated By</th>
    <th>Status</th>
<%

String failureMessage="FAILURE_MESSAGE";
			String statusMessage=null;
			HashMap h = rbtStatus.getExtraInfoMap(strSubID, dispSub);
			if (h !=null && h.get(failureMessage)!=null && h.get(failureMessage).toString().trim().length()>0) {
				if (!(((String)h.get(failureMessage)).equalsIgnoreCase("OK")))	
					strReason=(String)h.get(failureMessage);
			}
if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true && RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_SUB_CHARGE_DATE", "TRUE"))
{
%>
    <th>Last Charged Date</th> 
<% 
} 


%> 
<th>Info</th>
<%
if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_VALIDITY_CC", "FALSE"))
{
%>
    <th>Validity</th>
<%
}
if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISPLAY_HUAWEI_STATUS", "FALSE"))
{
%>
    <th>Check Huawei Status</th>
<%
}
%>

</tr>
<tr>
    <td><% out.print(strSubID); %></td>
	<% if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_SUB_CHARGE_DATE", "TRUE")) { %>
    <td><% out.print(prepaid); %></td>
	<%}%>
    <td><% out.print(strDate); %></td>
    <td><% out.print(strActby); %></td>
    <td><% out.print(strReason); %></td>
<%
if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true && RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_SUB_CHARGE_DATE", "TRUE"))
{
%>
    <td><% out.print(lastChargeDate); %></td>
<%
}
%>
<td><% out.print(strSubInfo); %></td>
<%
if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_VALIDITY_CC", "FALSE"))
{
%>
    <td><A HREF="#" onclick="update()" >Validity</A>
										</td>
<%
}
if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISPLAY_HUAWEI_STATUS", "FALSE"))
{
%>
    <td><A HREF="#" onclick="getHuaweiStatus()">Huawei Status</A></td>
<%
}
%>

</tr>
</table>
<table><tr><td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr></table>
<%
session.removeAttribute("SubStatus");
session.removeAttribute("SubReason");

StringTokenizer st_stat;
StringTokenizer st_token;
String [] subscriberStatus = null;
subscriberStatus = rbtStatus.getSubscriberSelections(strSubID, dispSub);
%>
<%
if(subscriberStatus != null)
{
    String chargeDuration;
    boolean weeklyFlag = false;
    boolean monthlyFlag = false;
    boolean dispValidity = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "DISP_VALIDITY_CC", "FALSE");
	String wavFileName = null;
    if(dispValidity)
    {
%>
        <table width ="70%" cols="14" align="center" border="1" cellpadding="1" cellspacing="1">
        <tr></tr>
        <tr>
            <td colspan = 14 align="center" ><b> Active Selections </b></td>
        </tr>
        <tr></tr>
        <tr>
            <th>Caller ID</th>
            <th>Category Name</th>
            <th>Clip Name </th>
            <th>Promo ID </th>
            <th>Selection Type </th>
            <th>Set-Time </th>
            <th>Validity </th>
            <th>Selected By </th>
<%
        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE"))
        {
%>
            <th>From Time </th>
            <th>To Time </th>
<%
        }
        if(rbtStatus.m_allowDayOfWeekFutureDate)
        {
%>
			<th>Weekly / FutureDate</th>
<%
		}
        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGE_DURATION_IN_GUI", "FALSE"))
        {
%>
            <th>Charge Duration</th>
<%
        }
        if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
        {
%>
            <th>Last Charged Date</th>
            <th>Selection Status</th>
<%
        }
%>
		<th>Info </th>
		<%if(rbtStatus.m_showClipExpiryDateInViewSubscrberDetails){%>
		<th>Clip Expiry Date</th>
		<%}%>
        </tr>
<%
        for(int i = 0; i < subscriberStatus.length; i++)
        {
            st_stat = new StringTokenizer(subscriberStatus[i], ",");
            String callerID = st_stat.nextToken();
            //String st = st_stat.nextToken();
%>
            <tr>
                <td><%out.print(callerID);%></td>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken().replaceAll(";",","));%></td>
				<% wavFileName = st_stat.nextToken();%>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
                
                
                <td><%out.print(st_stat.nextToken());%></td>
<%
            String fromTime = st_stat.nextToken();
            String toTime = st_stat.nextToken();
			String selInterval = st_stat.nextToken();
			
            if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE"))
            {
%>
                <td><%out.print(fromTime);%></td>
                <td><%out.print(toTime);%></td>
<%
            }
            if(rbtStatus.m_allowDayOfWeekFutureDate)
            {
%>
				<td><%out.print(selInterval);%></td>
<%            
            }
            if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGE_DURATION_IN_GUI", "FALSE"))
            {
                chargeDuration=st_stat.nextToken();
                if(chargeDuration.equalsIgnoreCase("weekly"))
                {
                    weeklyFlag = true;
                }
                if(chargeDuration.equalsIgnoreCase("monthly"))
                {
                    monthlyFlag = true;
                }
%>
                <td><%=chargeDuration%></td>
<%
            }
            else
            {
                st_stat.nextToken();
            }
            if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
            {
%>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
<%
            }
            else
            {
                st_stat.nextToken();
                st_stat.nextToken();
            }
            String st = st_stat.nextToken();
%>            
            <td><%out.print(st_stat.nextToken());%></td>
			<%if(rbtStatus.m_showClipExpiryDateInViewSubscrberDetails){%>
			<td><%out.print(st_stat.nextToken());%></td>
			<%}%>
<%            
           if(strUserPermission != null && strUserPermission.equalsIgnoreCase("0"))
            {
                if(st.equalsIgnoreCase("0"))
                    callerID = "ALL";
%>
                <td align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href="rbt_subscriber_status.jsp?CALLER_ID=<%=callerID%>&STATUS=<%=st%>&FROMTIME=<%=fromTime%>&TOTIME=<%=toTime%>&ACTION=REMOVE&WAV_FILE=<%=wavFileName%>">Remove</A></td>
<%
            }
%>
			
            </tr>
<%
        }
    }
    else
    {
%>
        <table width ="70%" cols="14" align="center" border="1" cellpadding="1" cellspacing="1">
        <tr></tr>
        <tr>
            <td colspan = 14 align="center" ><b> Active Selections </b></td>
        </tr>
        <tr></tr>
        <tr>
            <th>Caller ID</th>
            <th>Category Name</th>
            <th>Clip Name </th>
            <th>Promo ID </th>
            <th>Selection Type </th>
            <th>Set-Time </th>
            <th>Selected By </th>
<%

			
		

        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE"))
        {
%>
            <th>From Time </th>
            <th>To Time </th>
<%
        }
        if(rbtStatus.m_allowDayOfWeekFutureDate)
        {
%>
			<th>Weekly / FutureDate</th>
<%
		}
        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGE_DURATION_IN_GUI", "FALSE"))
        {
%>
            <th>Charge Duration</th>
<%
        }
        if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
        {
%>
            <th>Last Charged Date</th>
            <th>Selection Status</th>
<%
        }
%>
		<th>Info </th>
		<%if(rbtStatus.m_showClipExpiryDateInViewSubscrberDetails){%>
		<th>Clip Expiry Date</th>
		<%}%>
        </tr>
<%
        for(int i = 0; i < subscriberStatus.length; i++)
        {
            st_stat = new StringTokenizer(subscriberStatus[i], ",");
            String callerID = st_stat.nextToken();
            String st;
%>
            <tr>
                <td><%out.print(callerID);%></td>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken().replaceAll(";",","));%></td>
				<% wavFileName = st_stat.nextToken();%>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
<%

	
            st_stat.nextToken();
%>
                <td><%out.print(st_stat.nextToken());%></td>
<%
            String fromTime = st_stat.nextToken();
            String toTime = st_stat.nextToken();
            String selInterval = st_stat.nextToken();
            
            if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE"))
            {
%>
                <td><%out.print(fromTime);%></td>
                <td><%out.print(toTime);%></td>
<%
            }
            if(rbtStatus.m_allowDayOfWeekFutureDate)
            {
%>
				<td><%out.print(selInterval);%></td>
<%            
            }
            if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_CHARGE_DURATION_IN_GUI", "FALSE"))
            {
                chargeDuration=st_stat.nextToken(); 
                if(chargeDuration.equalsIgnoreCase("weekly"))
                {
                    weeklyFlag = true;
                }
                if(chargeDuration.equalsIgnoreCase("monthly"))
                {
                    monthlyFlag = true;
                }
%>
                <td><%=chargeDuration%></td>
<%
            }
            else
            {
                st_stat.nextToken();
            }
            if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
            {
%>
                <td><%out.print(st_stat.nextToken());%></td>
                <td><%out.print(st_stat.nextToken());%></td>
<%
            }
            else
            {
                st_stat.nextToken();
                st_stat.nextToken();
            }
            st = st_stat.nextToken();
	
			//  added here.
%>	
			<td><%out.print(st_stat.nextToken());%></td>
			<%if(rbtStatus.m_showClipExpiryDateInViewSubscrberDetails){%>
			<td><%out.print(st_stat.nextToken());%></td>
			<%}%>

<%            if(strUserPermission != null && strUserPermission.equalsIgnoreCase("0"))
            {
                if(st.equalsIgnoreCase("0"))
                    callerID = "ALL";
%>
                <td align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href="rbt_subscriber_status.jsp?CALLER_ID=<%=callerID%>&STATUS=<%=st%>&FROMTIME=<%=fromTime%>&TOTIME=<%=toTime%>&ACTION=REMOVE&WAV_FILE=<%=wavFileName%>">Remove</A></td>
<%
            }
%>
			</tr>
<%
        }
    }
%>
        </table>
<%
    if(strUserPermission != null && strUserPermission.equalsIgnoreCase("0"))
    {
        if(weeklyFlag)
        {
%>
            <table align="center">
            <tr><td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
            <tr><td align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href="rbt_subscriber_status.jsp?INIT=WEEKLY&FINL=MONTHLY&ACTION=MODIFY">CONVERT WEEKLY TO MONTHLY</A></td></tr>
            </table>
<%
        }
%>
<% 
        if(monthlyFlag)
        {
%>
            <table align="center">
            <tr><td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
            <tr><td align="left">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<A href="rbt_subscriber_status.jsp?INIT=MONTHLY&FINL=WEEKLY&ACTION=MODIFY">CONVERT MONTHLY TO WEEKLY</A></td></tr>
            </table>
<%
        }
    }
}
else if(strReason != null && strReason.startsWith("Act"))
{
    out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>No Active selections </b></font></center>");
}

%>
   <% 

if (rbtStatus.m_showDownloadsOnCCGui){

%>
<table width ="70%" cols="9" align="center" border="1" cellpadding="1" cellspacing="1">
    <tr></tr>
    <tr>
        <td colspan = 9 align="center" ><b> Active Downloads </b></td>
    </tr>
    <tr></tr>
	<tr>
<%
HashMap activeDownloadsMap= new HashMap();
		activeDownloadsMap=rbtStatus.getActiveSubscriberDownloads(strSubID);

	if (activeDownloadsMap!=null && activeDownloadsMap.size()>0){	


		Set keys = activeDownloadsMap.keySet();
		int size=keys.size();
		int k=0;
		ArrayList[] columnValues=new ArrayList[size];
		for(Iterator it=keys.iterator();it.hasNext();){
			
			String columnName=it.next().toString();
			columnValues[k]=(ArrayList)activeDownloadsMap.get(columnName);
			k++;
			
			
	%>
	
	<th><%=columnName%></th>
	
	<%}%>
    </tr>	
    <%
			int length=0;
			if (columnValues!=null && columnValues[0]!=null)
			length=columnValues[0].size();
			
			
		    for (int l=0;l<length;l++){%>
    		<tr>	
    			<%
    		for (int m=0;m<columnValues.length;m++){
    		if (columnValues[m]!=null){
			if (columnValues[m].size() > l && columnValues[m].get(l)!=null){
				%>
    			<td><%= columnValues[m].get(l).toString() %></td>
    		<%	}
    		else {%>
    				<td>-</td>
    			<%}
    		  }
    		}%>
    		</tr>
    	<%}
	}
	
	else {
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>No Active Downloads </b></font></center>");	
	}
}
%>				

<tr></tr>
<tr></tr>
</table>

<%
subscriberStatus = rbtStatus.getSubscriberDeactivatedRecords(strSubID);
if(subscriberStatus != null)
{
%>
    <table width ="70%" cols="7" align="center" border="1" cellpadding="1" cellspacing="1">
    <tr></tr>
    <tr>
        <td colspan = 7 align="center" ><b> DeActivated Selections </b></td>
    </tr>
    <tr></tr>
    <tr>
        <th>Caller ID</th>
        <th>Category Name</th>
        <th>Clip Name</th>
        <th>Promo ID</th>
        <th>Selection Type</th>
        <th>End Time</th>
<%
    if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
    {
%>
        <th>Selection Status</th>
<%
    }
%>
	<th>Info </th>
    </tr>
<%
    for(int i = 0; i < subscriberStatus.length; i++)
    {
        st_stat = new StringTokenizer(subscriberStatus[i], ",");
%>
        <tr>
            <td><%out.print(st_stat.nextToken());%></td>
            <td><%out.print(st_stat.nextToken());%></td>
            <td><%out.print(st_stat.nextToken());%></td>
            <td><%out.print(st_stat.nextToken().replaceAll(";",","));%></td>
            <td><%out.print(st_stat.nextToken());%></td>
            <td><%out.print(st_stat.nextToken());%></td>
<%
        if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
        {
%>
            <td><%out.print(st_stat.nextToken());%></td>
<%
        }
		else 
			st_stat.nextToken(); 
%>
		<td><%out.print(st_stat.nextToken());%></td>
        </tr>
<%
    }
%>
    </table>
<%
}
%>
<% if (enableSMSHistory != null && enableSMSHistory.equalsIgnoreCase("true")) { %>
<table align="center">
<tr><td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
<tr><td colspan = "3" align = "center"><A href="rbt_get_sms_history(file).jsp">GET SMS HISTORY</A></td></tr>
</table>
<% } %>
 
   <% 

if (rbtStatus.m_showDownloadsOnCCGui){

%>

   <table width ="70%" colspan="10" align="center" border="1" cellpadding="1" cellspacing="1">
    
    <tr>
        <td colspan=10 align="center" ><b> Deactive Downloads </b></td>
    </tr>
    
	
<%
	
		HashMap deactiveDownloadsMap= new HashMap();
		deactiveDownloadsMap=rbtStatus.getDeactiveSubscriberDownloads(strSubID);
		
		if (deactiveDownloadsMap!=null && deactiveDownloadsMap.size()>0) {
		int size=deactiveDownloadsMap.size();
		Set keys = deactiveDownloadsMap.keySet();
		ArrayList[] columnValues=new ArrayList[size];
		int j=0;
		%>
		<tr>
		<% for(Iterator it=keys.iterator();it.hasNext();){
			
			String columnName=it.next().toString();
			columnValues[j]=(ArrayList)deactiveDownloadsMap.get(columnName);
			j++;
			
	%>
	<td>
	<%=columnName%>
	</td>
	<%
		} %>
    </tr>
		<%
			int length=0;
			if (columnValues!=null && columnValues[0]!=null)
			length=columnValues[0].size();
		
    	for (int l=0;l<length;l++){%>
    		<tr>
    			<%
    		for (int m=0;m<columnValues.length;m++){
    		if (columnValues[m]!=null){
			if (columnValues[m].size() > l && columnValues[m].get(l)!=null){
	%>
    			<td><%=columnValues[m].get(l).toString()%></td>
    		<%	} else {%>
    				<td>-</td>
    			<%} 
    		  }
    		}%>
    		</tr>
    	<%}
    	
    	
    	
    	
    	
		}
	else {
	out.print("<center><font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>No Deactive Downloads </b></font></center>");

	}
%>				
<tr>
</tr>
<tr></tr>
</table>
<%
	}
%>
<table align="center">
<tr><td colspan = 3>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
<tr><td colspan = "3" align = "center"><input type="image" border = "0" name="BACK" src = "images/previous.gif" onclick = "javascript:location.href='rbt_subscriber_status.jsp'"></td></tr>
</table>

<%
}
else
{
	System.out.println("session invalidated");
    session.invalidate();
%>
    <jsp:forward page="index.jsp" />
<%
}
}
catch(Throwable e)
{
	Tools.logException("rbt_disp_status","rbt_disp_status",e);
}
%>