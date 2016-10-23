<%@ page import = "java.util.*"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.UserRights"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>

<%@ include file = "validate.jsp" %>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
if (validateUser(request, session,  "rbt_login.jsp", response)) {

String strUser  = (String)(session.getAttribute("UserId"));
System.out.println("User Logged in " + strUser);
String user = (String)session.getAttribute("Permission");
%>
<HTML>
<head>
<title>RBT Subscriber Manager</title>
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
			      			<table align="center" cols=1 border=0 cellpadding=6 width="90%" style="VERTICAL-ALIGN: top">
			      				<tr>
			      					<td align="center">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<IMG border=0 src="images/subscriptions.jpg"> </td>
			      				</tr>
			      			</table>
			      			<!--Welcome Image -->
			      
						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<tr>
								<%
								HashMap hMap = (HashMap) session.getAttribute("UserRights");
								if(hMap == null){
									session.invalidate();%>
										<jsp:forward page="index.jsp" />
								<%}
								String userType = (String)session.getAttribute("Permission");
								if(userType == null){
									session.invalidate();%>
									<jsp:forward page="index.jsp" />
								<%}

								String userRights = rbtLogin.getUserRights(userType.trim());
								if(userRights == null){
									session.invalidate();%>
										<jsp:forward page="index.jsp" />
								<%}
								StringTokenizer st = new StringTokenizer(userRights.trim(), ",");
								String key = st.nextToken().trim();
								if(key.equalsIgnoreCase("0"))
									key = st.nextToken().trim();
								if(hMap.containsKey(key))
								{
									String value = (String)hMap.get(key);
									StringTokenizer token = new StringTokenizer(value.trim(), ",");
									String name = token.nextToken().trim();
									String jsp = token.nextToken().trim();%>

									<jsp:forward page="<%=jsp%>" />

								<%}else{
									session.invalidate();%>
										<jsp:forward page="index.jsp" />
								<%}%>


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