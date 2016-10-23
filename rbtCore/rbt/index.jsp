<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.*"%>
<% String strIP = request.getRemoteAddr();
System.out.println("RBT: IP address " + strIP);
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
//rbtLogin.initTools();
String strStatus = (String)session.getAttribute("LoginStatus");
String strReason = (String)session.getAttribute("Permission");

if(strReason == null || strReason.equals("FAILURE"))
{
	session.setAttribute("UserType","VALID");
	session.setAttribute("LoginStatus", "FAILURE");%>
	<jsp:forward page ="rbt_user_login.jsp" />
<%
}
	else
	%><jsp:forward page ="rbt_login.jsp" /><%
%>
<%! public void jspInit(){
  //application.setAttribute("inittime",""+System.currentTimeMillis());
  System.out.println("in jspInit()");
  javax.servlet.ServletConfig servletConfig = getServletConfig();
  servletConfig.getServletContext().setAttribute("inittime",""+System.currentTimeMillis());
} // end jspInit() method
%>


<%
System.out.println("Accessed using localhost");
session.setAttribute("UserType","UNKNOWN");
session.setAttribute("Permission","0");
strStatus = (String)session.getAttribute("LoginStatus");
strReason = (String)session.getAttribute("LoginReason");
if(strStatus == "FAILURE")
	session.invalidate();
%>
<HTML>
<head>
<title>RBT Subscription</title>
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
						<jsp:include page="rbt_login_inc.jsp" />
					</td>
					<!--Menu End -->
					
					<!--Console Start  -->
					<!-- initialise global variable fix for weblogic 6.0 -->
					
					<td width="77%" bgcolor="#ffedd9" valign="top">
			      			<br>
			      			<table align="center" cols=1 border=0 cellpadding=6 width="90%" style="VERTICAL-ALIGN: top">
			      				<tr>
			      					<td align="center">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<IMG border=0 src="images/welcome.gif"> </td>
			      				</tr>
			      			</table>
			      			<!--Welcome Image -->
			      
						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<tr>
								<td align="center"> <img src="images/welcome.jpg"></td>
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

