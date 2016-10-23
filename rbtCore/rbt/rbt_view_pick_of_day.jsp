<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.cache.content.ClipMinimal"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.PickOfTheDay"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%>
<%@ page import = "java.util.*,java.text.SimpleDateFormat"%>

<%@ include file = "validate.jsp" %>
<% 
Clips[] clip;
ClipMinimal[] clipMinimal = null;
Hashtable htClipTable;
%>
<%
clip =null;
htClipTable = null;
RBTSubUnsub rbtLogin = RBTSubUnsub.init();



if (validateUser(request, session,  "rbt_view_pick_of_day.jsp", response)) {
	%>

<html>
<head>
<title> RBT Pick of the Day </title>
</head>
<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0" >
<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0" >
<table border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
	<tr>
		<td colspan=3 valign="top" height="15%">
			<jsp:include page="rbt_header_inc.jsp" /> 
		</td>
	</tr>
	<tr>
		<td valign="top" colspan=3 height="85%">
			<table border="0" width="100%" cellspacing="0" cellpadding="0" height="100%">
				<tr>
					<td width="23%" bgcolor="#ffdec8" valign="top">
						<jsp:include page="rbt_menu_inc.jsp" /> 
					</td>
					<td width="77%" bgcolor="#ffedd9" valign="top">
<br><br>


 

	<%
	//added for viewing circle specific potd from here..
	boolean isCircle=false;
		SitePrefix[] circle=null;
		String circleId;%>
	<%isCircle=rbtLogin.isCircleId();	
//			if(isCircle){%>
	
	<%
			circle=rbtLogin.getLocalSitePrefixes();
			
			System.out.print("in circle "+request.getParameter("circleId"));
			if (request.getParameter("circleId")== null){
				circleId=circle[0].getCircleID();
			}
			else {
				circleId=request.getParameter("circleId");
			}
			%>
			
	
	<% //} // .. till here.%>




<form name="pickOfTheDayForm" method="post" action="rbt_view_pick_of_day.jsp">
<%
	SimpleDateFormat format = new SimpleDateFormat("yyyy");
	java.util.Date currentDate = new java.util.Date(System.currentTimeMillis());
	int currentYear = Integer.parseInt(format.format(currentDate));
	format = new SimpleDateFormat("MM");
	int currentMonth = Integer.parseInt(format.format(currentDate));
	format = new SimpleDateFormat("MM/yyyy");
	String range = format.format(currentDate);
	String [] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};


	clipMinimal  = rbtLogin.getAllActiveClips();
	
	if(clipMinimal != null)
	{
		htClipTable = new Hashtable();
		for(int i=0; i< clipMinimal.length; i++)
		{
			htClipTable.put(String.valueOf(clipMinimal[i].getClipId()),clipMinimal[i].getClipName());
		}
	}


	String selYear = request.getParameter("year");
	String selMonth = request.getParameter("month");
	
	if(selYear != null && selMonth != null)
	{
		currentYear = Integer.parseInt(selYear);
		currentMonth = Integer.parseInt(selMonth);
		if(currentMonth < 10)
			selMonth = "0" + selMonth;
		range = selMonth.trim() + "/" + selYear;
	}

	if(request.getQueryString() != null)
	{
		selYear = request.getQueryString().trim().substring(6);
		selMonth = request.getQueryString().trim().substring(3, 5);
		currentYear = Integer.parseInt(selYear);
		currentMonth = Integer.parseInt(selMonth);
		if(currentMonth < 10)
			selMonth = "0" + selMonth;
		range = request.getQueryString().trim().substring(3);
	}
	
	//for getting circle specific pick of  the days. 
	PickOfTheDay[] pickOfTheDays = null;
	if(isCircle && circleId!=null)
	pickOfTheDays = rbtLogin.getPickOfTheDays(range,circleId);
	else{
	pickOfTheDays = rbtLogin.getPickOfTheDays(range);}
	
%>
	<p><font face="Verdana, Arial, Helvetica, sans-serif" size="2"><b><center>Pick Of The Day</center></b></font></p>
		<table  width="90%"  height="98">
		
		
		<tr>
			<td width="27%" height="19">Select a Circle</td>
			<td width="73%" height="19">
				<select name ="circleId">
				<% int l=0; while(l<circle.length){ if(circleId!=null&&circleId.equalsIgnoreCase(circle[l].getCircleID())){%>
				<option selected value='<%=circle[l].getCircleID()%>'><%=circle[l].getSiteName()%></option>
				<%}else{%>
				<option  value='<%=circle[l].getCircleID()%>'><%=circle[l].getSiteName()%></option>
				<%}l++;}%></select>
			</td>
		</tr>
		
		<tr>
			<td width="27%" height="19">Select a Year</td>
			<td width="73%" height="19">
				<select name ="year">
				<% int i=2005; while(i<=2037){ if(i==currentYear){%>
				<option value = <%=i%> selected><%=i%></option>
				<%}else{%>
				<option value = <%=i%>><%=i%></option>
				<%}i++;}%></select>
			</td>
		</tr>
		<tr></tr>
		<tr>
			<td width="27%" height="19">Select a Month</td>
			<td width="73%" height="19">
				<select name ="month">
	
				<% int j=0; while(j<12){ if(j==(currentMonth-1)){%>
	
				<option value = <%=j+1%> selected><%=months[j]%></option>
	
				<%}else{%>
	
				<option value = <%=j+1%>><%=months[j]%></option>
	
				<%}j++;}%></select>
	
			</td>
		</tr>
		
		<tr></tr>
		<tr></tr>
		<tr>
			<td><input type="submit" value="Submit!" name="submit" onClick = "return true;">
			</td>
		</tr>
	</table>
	<br><br>
	
	<%if(pickOfTheDays!=null){%>
	
	<center><font face="Verdana, Arial, Helvetica, sans-serif" size="2">Schedule for <%=months[currentMonth-1]%> <%=currentYear%></font></center>
	
	<table align="center" cols=1 border=1 cellpadding=6 width="90%" style="VERTICAL-ALIGN: top" >
	
		<tr bgcolor="#3875a4" >
			<th width="30%"> <font face=Arial size=2 color=white><b>Date</b></font></th>
			<th width="30%"> <font face=Arial size=2 color=white><b>Name</b></font></th>
			<th width="30%"> <font face=Arial size=2 color=white><b>Circle</b></font></th>
			<th width="30%"> <font face=Arial size=2 color=white><b>Profile</b></font></th>
		</tr>
	
		<%for(int k=0; k<pickOfTheDays.length; k++){%>
	
		<tr>
			<td width="30%" height="19"><%=pickOfTheDays[k].playDate()%></td>
			<%if(htClipTable != null)
	
			{%>
	
				<td width="30%" height="19"><%=htClipTable.get(String.valueOf(pickOfTheDays[k].clipID()))%></td>
				
			<%}%>
			<td width="30%" height="19"><%=pickOfTheDays[k].circleID()%></td>
			<td width="30%" height="19"><%=pickOfTheDays[k].profile()%></td>
			
		</tr>
	
	<%}%>
	
	</table>
	
	<%}else{ 
	
	if (isCircle){%>
	
	<center><font face="Verdana, Arial, Helvetica, sans-serif" size="2">No schedule available for the circle <%=circleId%> for <%=months[currentMonth-1]%> <%=currentYear%></font></center>
	
	<%}else{%>
	
	<center><font face="Verdana, Arial, Helvetica, sans-serif" size="2">No schedule available for <%=months[currentMonth-1]%> <%=currentYear%></font></center>
	
	<%}
	
	}%>
</form>
</td>
</tr>
</table>
</td>
</tr>
</table>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>




