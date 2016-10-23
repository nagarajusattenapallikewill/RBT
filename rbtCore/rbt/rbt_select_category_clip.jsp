		<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
		<%@ page import = "com.onmobile.apps.ringbacktones.content.Categories"%>
		<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
		<%@ page import = "java.util.*"%>

		<%@ include file = "validate.jsp" %>
		<% 
		int i,category_id;
		String circleId = null;
		SitePrefix[] circle = null;
		Categories[] category = null;
		Clips[] clip = null;
		boolean isCircle=false;
		String categoryName = null;
		String songName = null;
		String updated = null;
		String prepaidYesStr = null;
		
		%>
		<%
		RBTSubUnsub rbtLogin = RBTSubUnsub.init();
		circle=null;
		category = null;
		clip = null;
		isCircle=rbtLogin.isCircleId();
		session.setAttribute("song", songName);
		session.setAttribute("category", categoryName);
		session.setAttribute("circle", circleId);

		if (validateUser(request, session,  "rbt_subs_selections.jsp", response)) { %>
		<html>
		<head>
		<%@ include file = "javascripts/RBTValidate.js" %>
		<script language="JavaScript">

		function update()
		{
			if(document.frmStatus.SongName.value.length == 0)
			{
				alert("Cannot Add Selection As No Songs in this category");
				return false;
			}
			document.frmStatus.updated.value = "false";
			return true;
		}

		</script>
		<title>RBT Subscription</title>
		</head>
		<body>
		<form action ="rbt_select_category_clip.jsp"  method="post" name="frmStatus">
			<p><b><center>Add Subscriber Selections</center></b></p>	
		<table align = "center" width ="100%">
		<tr>
		<td></td>
		<td>
		<%	String strStatus = null;
			strStatus = (String)session.getAttribute("updated");
			String strReason = (String)session.getAttribute("reason");
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
		<tr></tr>
		<tr></tr>
		<tr></tr>
		<tr></tr>
		
		<%	
			//if(isCircle)
			circleId=request.getParameter("circleId");
			categoryName = request.getParameter("categoryName");
			songName = request.getParameter("SongName");
			updated =  request.getParameter("updated");
			prepaidYesStr = request.getParameter("prepaidyes");

			if(circleId != null && prepaidYesStr != null && categoryName != null && songName != null && updated.equalsIgnoreCase("false"))
			{
				session.setAttribute("song", songName);
				session.setAttribute("category", categoryName);
				session.setAttribute("circle", circleId);
				session.setAttribute("prepaidyes", prepaidYesStr);
				session.setAttribute("selected", "true");%>
					<jsp:forward page="rbt_subs_selections.jsp" />		
				<%
			}
				circle=rbtLogin.getLocalSitePrefixes();
		%>

		<tr>

		<td width="30%" >Circle Name</td>
		<td width="60%">	
		<%
				if (circle != null)
				{			
		%>
				   <select name="circleId" onChange="fnChange(frmStatus)">
		<%	 	   
				   for(i=0; i <circle.length; i++)
			{
						if(circleId!=null&&circleId.equalsIgnoreCase(circle[i].getCircleID()))
							{
							%>
						<option selected value='<%=circle[i].getCircleID()%>'><%=circle[i].getSiteName()%></option>
						
						<%	continue;
						}
						%><option  value='<%=circle[i].getCircleID()%>'><%=circle[i].getSiteName()%></option>
						<%
						}
				}else{
					out.println("No Circles to show");
				}
			
		%>
		</select>
		</td>
		</tr>
		<tr>
			<td width="30%">Subscriber Type</td>
			<td width="60%">
				<select name="prepaidyes" onchange="fnChange(frmStatus)">
				<% if(prepaidYesStr == null || prepaidYesStr.equalsIgnoreCase("prepaid")) { %>
					<option selected value='prepaid'>Prepaid</option>
					<option value='postpaid'>PostPaid</option>
				<%}
					else  {%>
					<option selected value='postpaid'>PostPaid</option>
					<option value='prepaid'>Prepaid</option>
				<%}%>
				</select>
			</td>
		</tr>
		<%
			category=null;
			if(circleId!=null && prepaidYesStr != null)
				category=rbtLogin.getActiveCategories(circleId, (prepaidYesStr.equalsIgnoreCase("prepaid")) ? 'y' : 'n');
			else if (circleId!=null && prepaidYesStr == null)
				category=rbtLogin.getActiveCategories(circleId, 'y');
			else
				category=rbtLogin.getActiveCategories(circle[0].getCircleID(), 'y');
		%>
		<td width="30%" >Category</td>
		<td width="60%">	
		<%
				if (category != null)
				{			
		%>
				   <select name="categoryName" onChange="fnChange(frmStatus)">
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

		<td width="30%" >Song</td>
		<td width="60%">
				
		<%
				clip=null;
				if (categoryName != null&&category!=null)
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
		<tr>
		<td><input type=hidden name="updated" size = 32 maxlength=32 value ="true"></td>
		</tr>
		<tr></tr><tr></tr><tr></tr>

		<table width ="58%" cols="1" align="center">
		<tr>
		<td></td>
		<td align="center"><input type=image border = "0" name="SUB_STATUS" src = "images/list_details.gif" onClick = 'return update()'></td>
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
		<%
				}else{
			session.invalidate();%>
			<jsp:forward page="index.jsp" />
		<%}%>