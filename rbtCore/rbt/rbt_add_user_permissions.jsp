<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>

<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session,  "rbt_add_user_permissions.jsp", response)) { %>
<html>
<head>
<title>RBT Add User Permissions</title>
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
					<!--Menu End -->
					
					<!--Console Start  -->
					<!-- initialise global variable fix for weblogic 6.0 -->
					
					<td width="77%" bgcolor="#ffedd9" valign="top">
			      			<br>
			      			<!--table align="center" cols=1 border=0 cellpadding=6 width="90%" style="VERTICAL-ALIGN: top">
			      				<tr>
			      					<td align="center">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<IMG border=0 src="images/subscriptions.jpg"> </td>
			      				</tr>
			      			</table-->
			      			<!--Welcome Image -->
			      
						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<tr>
								<td align="center"> <jsp:include page="rbt_add_user_permissions_inc.jsp" />
							</tr>
						</table>
						<!--Welcome Image -->

					</td>
					<!--Console End -->
				</tr>
			</table>
		</td>
	</tr>

</table>
</body>
</HTML>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>
