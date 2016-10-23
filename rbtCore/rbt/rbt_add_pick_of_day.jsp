<%@page import="com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil"%>
<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.PickOfTheDay"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%>
<%@ page import = "com.onmobile.apps.ringbacktones.cache.content.ClipMinimal"%>
<%@ page import = "java.util.*,java.text.SimpleDateFormat"%>

<%@ include file = "validate.jsp" %>
<%
Clips[] clip=null;
ClipMinimal[] clipMinimal=null;
%>
<%
RBTSubUnsub	rbtLogin = RBTSubUnsub.init();


String strUser  = (String)(session.getAttribute("UserId"));
String circleId=request.getParameter("circleID");
String profile=request.getParameter("profile");
if( profile==null || profile.equals("NULL")){profile=null;}


if (validateUser(request, session,  "rbt_add_pick_of_day.jsp", response)) {
	System.out.println("Pick of the day Added by User "+strUser);
	%>

	<html>
	<head>
	<title> Add Pick of the Day </title>
	<script language="JavaScript" src="javascripts/DatePicker.js"></script>
	<!--script type="text/javascript" src="javascripts/form.js"></script-->
	<script language="JavaScript">
	function insertForm()
	{
		if(document.addPickOfTheDayForm.clips.options[document.addPickOfTheDayForm.clips.selectedIndex].value == "0")
		{
			alert('Select a clip');
			return false;
		}
		document.addPickOfTheDayForm.playDate.disabled = false;
		return true;
	}
	</script>
	</head>
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
	<form name="addPickOfTheDayForm" method="post" action="rbt_add_pick_of_day.jsp">
	<%
		ClipMinimal [] clipMinimals = rbtLogin.getAllActiveClips();
		
		SitePrefix[] circle= CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes().toArray(new SitePrefix[0]);
		Date currentDate = new Date(System.currentTimeMillis());
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		int l=0;
		circleId=request.getParameter("circleId");
		String clipID = request.getParameter("clips");
		String date = request.getParameter("playDate");
		System.out.println("chosen profile is " + profile);
		if(clipID != null && date != null)
		{
			PickOfTheDay pick;
			pick = rbtLogin.insertPickOfTheDay(4,Integer.parseInt(clipID),date,circleId,profile);
						
			if(pick != null)
			{
				response.sendRedirect("rbt_view_pick_of_day.jsp?"+date); 
			}
			else
			{%>
				<script language="javascript">
				alert("Insertion of Pick of the Day for <%=date%> failed");
				</script>
			<%}
			
			}
		else
		{
			clipID = "-1";
			date = format.format(currentDate);
		}
	
	%>
	<%if(clipMinimals == null){%>
			<font face="Verdana, Arial, Helvetica, sans-serif" size="2">&nbsp;&nbsp;&nbsp;No clips available.
			</font>
	<%}else{ %>
	<p><font face="Verdana, Arial, Helvetica, sans-serif" size="2"><b><center>Add Pick of the Day</center></b></font></p>
	
		<table  width="90%"  height="98">
	 	
	 			<tr>
				<td width="27%" height="19">Select a Circle</td>
				<td width="73%" height="19">
					<select name ="circleId">
					<% l=0; while(l<circle.length){ if(circleId!=null&&circleId.equalsIgnoreCase(circle[l].getCircleID())){%>
					<option selected value='<%=circle[l].getCircleID()%>'><%=circle[l].getSiteName()%></option>
					<%l++;}else{%>
					<option  value='<%=circle[l].getCircleID()%>'><%=circle[l].getSiteName()%></option>
					<%l++;}
					}%></select>
				</td>
			</tr>
		 
	  	<tr>
		    <td width="27%" height="19">Clip Name</td>
			<td width="73%" height="19"><select name="clips" style="width:225px">
			<option value="0" selected>---------------Select a Clip-----------------</option>
			<%for(int i=0; i<clipMinimals.length; i++)
				{if(clipMinimals[i].getClipId() == Integer.parseInt(clipID)) {%>
					<option value="<%=clipMinimals[i].getClipId()%>" selected><%=clipMinimals[i].getClipName()%></option>
					<%}else{%>
					<option value="<%=clipMinimals[i].getClipId()%>"><%=clipMinimals[i].getClipName()%></option>
				<%}
			}%>
			</select></td>
	  		</tr>
	  	<tr>
				<td width="27%" height="19">Play Date</td>
				<td width="73%" height="19">
				<input type="text" name="playDate" value="<%=date%>" style='text-align:left;' size=10 maxlength=10 disabled="true">
				<input type="button" name="date" value="Calendar" onClick="javascript:show_calendar('addPickOfTheDayForm.playDate');" onMouseOver="self.status='Get the calendar';return true" onmouseout="window.status='';return true;">
				</td>
	 	</tr>
	 	
	 		<tr>
		    <td width="27%" height="19">Profile</td>
			<td width="73%" height="19">
                                  <select name="profile">
                                              <option value="NULL">NONE</option>
                                              <option value="ben">Bengali</option>
                                              <option value="bho">Bhojpuri</option>
                                              <option value="eng">English</option>
                                              <option value="guj">Gujarati</option>
                                              <option value="hin">Hindi</option>
                                              <option value="kan">Kannada</option>
                                              <option value="mar">Marathi</option>
                                              <option value="mal">Malayalam</option>
                                              <option value="ori">Oriya</option>
                                              <option value="pun">Punjabi</option>
                                              <option value="tam">Tamil</option>
                                              <option value="tel">Telugu</option>
                                      </select>
			</td>
	  		</tr>
	 
		</table>
	<p>
		<input type="submit" value="Insert!" name="insert" onClick = "return insertForm()">
		<input type="reset" value="Reset" name="reset">
	</p>
	<br><br>	<br><br>	<br><br>	<br><br>	

	<%}%>

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
