	<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
	<%@ page import = "java.util.StringTokenizer, com.onmobile.apps.ringbacktones.content.Categories"%>
	<%@ include file = "validate.jsp" %>
	<%
	RBTSubUnsub rbtLogin = RBTSubUnsub.init();

	if (validateUser(request, session,  "rbt_categories.jsp", response)) { %>

	<html>
	<head>
		<%@ include file = "javascripts/RBTValidate.js" %>
	<title> RBT Categories </title>
	</head>
	<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0">
	<table border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
		<tr>
			<td colspan=3 valign="top" height="15%">
				<jsp:include page="rbt_header_inc.jsp"/>
			</td>
		</tr>

		<tr>
			<td colspan=3 valign="top" height="85%">
				<table border="0" width="100%" cellspacing="0" cellpadding="0" height="100%">
					<tr>
						<!--Menu Start -->
						<td width="23%" bgcolor="#ffdec8" valign="top">
							<jsp:include page="rbt_menu_inc.jsp" />
						</td>
						<td width="77%" bgcolor="#ffedd9" valign="top">
	<br><br>
	<%boolean isCircle=false;
		SitePrefix[] circle=null;
		String circleId=null;%>
	<%isCircle=rbtLogin.isCircleId();	
			if(isCircle){%>
	<form action ="rbt_categories.jsp"  method="post" name="frmStatus">
	<%circleId=request.getParameter("circleId");
			System.out.print("in cirrr "+request.getParameter("circleId"));
			circle=rbtLogin.getLocalSitePrefixes();%>
			<%
				if (circle != null)
				{			
		%>
				   <select name="circleId" onChange="fnChange1(frmStatus)">
		<%	 	 int i;  
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

	</form>

	<%}%>
	<form name="CategoriesForm" method=post>
	<% String categoryName = null ;String  songName = null; String updated=null;%>
	<%
		System.out.print("out cirrr "+circleId);
		String categoryID = request.getParameter("categoryID");
		String circleIDStr = request.getParameter("circleID");
		String prepaidYesStr = request.getParameter("prepaidYes");
		Categories [] categories = null;
		Categories category = null;
		 if(categoryID != null)
			{
				category = rbtLogin.getCategory(Integer.parseInt(categoryID), circleIDStr, prepaidYesStr.charAt(0));
				if(category.type() == 3)
					categories = rbtLogin.getActiveBouquet(Integer.parseInt(categoryID), category.circleID(), category.prepaidYes());
				else
					categories = rbtLogin.getSubCategories(Integer.parseInt(categoryID), category.circleID(), category.prepaidYes());
			%>
			<p><b><center>Categories under <%=category.name()%> </center></b></p>
			<%
			}
		else
			{
				if(circleId!=null){
					categories=rbtLogin.getActiveCategories(circleId);
					System.out.println("actcat ="+categories);
				}else
					categories=rbtLogin.getActiveCategories(circle[0].getCircleID());
			System.out.println("Circle Id ="+circleId);
			 %>
			<p><b><center>Main categories</center></b></p>
			<%}%>
		<% if(categories == null){%>
			&nbsp;&nbsp;&nbsp;No categories in the database.

		<%}else{ %>
		<table align="center" cols=2 border=1 cellpadding=6 width="40%" style="VERTICAL-ALIGN: top" >
			<tr bgcolor="#3875a4" >
				<th width="5%"> <b>Order</b></th>
				<th width="20%"> <b>Name</b></th>
				<th width="15%"> <b>Promo ID</b></th>
				<th width="15%"> <b>Prepaid</b></th>
			</tr>
				<% String circleID="";
					
			for(int j=0; j<categories.length; j++){ 
					String promoID = " - ";
					char prepaidYes='u';
					boolean flag=false;
					if(categories[j].promoID() != null && !categories[j].promoID().trim().equalsIgnoreCase("?"))
					{
						promoID = categories[j].promoID();
					}
					if(isCircle)
					if(categories[j].circleID()!=null&&!categories[j].circleID().trim().equalsIgnoreCase("?")&&!circleID.equalsIgnoreCase(categories[j].circleID()))
					{	
						circleID=categories[j].circleID();
						flag=true;
					}
					
						prepaidYes=categories[j].prepaidYes();
						
					
									%>
				<tr>
					<td align="center"><%=j+1%></td>
					<%if((categories[j].type() == 6)||(categories[j].type() == 3)){%>
					<td width="20%" height="19" bgcolor=#DDDDDD align ="center">
					<a href='rbt_categories.jsp?categoryID=<%=categories[j].id()%>&circleID=<%=categories[j].circleID()%>&prepaidYes=<%=categories[j].prepaidYes()%>' onMouseOver="self.status='To get the sub category under this category, click here';return true"><%=categories[j].name()%></a></td></td><td width="15%" height="19" align ="center"><%=promoID%></td><td width="15%" height="19" align ="center"><%=prepaidYes%></td><%}else if(categories[j].type() == 4){%><td width="20%" height="19" align="center"><%=categories[j].name()%></td><%}else{%><td width="20%" height="19" align ="center"><a href="rbt_clips.jsp?categoryID=<%=categories[j].id()%>&circleID=<%=categories[j].circleID()%>&prepaidYes=<%=categories[j].prepaidYes()%>" onMouseOver="self.status='To get the clips under this category, click here';return true"><%=categories[j].name()%></a></td><td width="15%" height="19" align ="center"><%=promoID%></td><td width="15%" height="19" align ="center"><%=prepaidYes%></td><%}%>
				</tr>
			<% }%>
			</table>
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
	<%
	}else{
		session.invalidate();
		response.sendRedirect("index.jsp"); 
	}%>