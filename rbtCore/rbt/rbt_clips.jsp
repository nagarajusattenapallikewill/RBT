<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.StringTokenizer, com.onmobile.apps.ringbacktones.content.Clips, com.onmobile.apps.ringbacktones.content.Categories"%>

<%@ include file = "validate.jsp" %>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
if (validateUser(request, session,  "rbt_all_clips.jsp", response)) { %>
<html>
<head>
<title> RBT Clips </title>
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
<form name="ClipsForm" method=post>
<%

	String categoryID = request.getParameter("categoryID");
	String circleIDStr = request.getParameter("circleID");
	String prepaidYesStr = request.getParameter("prepaidYes");
	Clips [] clips = null;

	if(categoryID != null)
	{
		Categories category = rbtLogin.getCategory(Integer.parseInt(categoryID), circleIDStr, prepaidYesStr.charAt(0));
		clips = rbtLogin.getAllClips(categoryID);
%>
	<p><b><center>Clips under <%=category.name()%> </center></b></p>
	<%}%>
	<% if(clips == null){%>
		&nbsp;&nbsp;&nbsp;No clips for the selected category.
		<%}else{ %>
	<table align="center" cols=2 border=1 cellpadding=6 width="40%" style="VERTICAL-ALIGN: top" >
		<tr bgcolor="#3875a4" >
			<th width="5%"> <b>Order</b></th>
			<th width="35%"> <b>Name</b></th>
			<th width="25%"> <b>Promo ID</b></th>
		</tr>
			<%for(int j=0; j<clips.length; j++){ 
			String promoID = " - ";
			if(clips[j].promoID() != null && !clips[j].promoID().trim().equalsIgnoreCase("?"))
			{
				promoID = clips[j].promoID();
			}
			%>
			<tr>
				<td width="5%" align="center"><%=j+1%></td>
				<td width="35%" height="19" align="center"><%=clips[j].name()%></td>
				<td width="25%" height="19" align="center"><%=promoID%></td>
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
<%}else{
		session.invalidate();
		response.sendRedirect("index.jsp"); 
	}%>