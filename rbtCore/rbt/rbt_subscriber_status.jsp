<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ include file = "validate.jsp" %>
<%
if (validateUser(request, session, "rbt_subscriber_status.jsp", response)) {
	if(request.getParameter("ACTION") != null)
	{
		session.setAttribute("Action",request.getParameter("ACTION"));
		if ((request.getParameter("ACTION")).equalsIgnoreCase("REMOVE"))
		{
			if(request.getParameter("CALLER_ID") != null)
			{
				String calerid = request.getParameter("CALLER_ID");
				session.setAttribute("Caller",request.getParameter("CALLER_ID"));
			}
			if(request.getParameter("STATUS") != null)
			{
				String status = request.getParameter("STATUS");
				session.setAttribute("STATUS",request.getParameter("STATUS"));
		
			}
			if(request.getParameter("FROMTIME") != null)
			{
				String fromTime = request.getParameter("FROMTIME");
				session.setAttribute("FROMTIME",request.getParameter("FROMTIME"));
		
			}
			if(request.getParameter("TOTIME") != null)
			{
				String toTime = request.getParameter("TOTIME");
				session.setAttribute("TOTIME",request.getParameter("TOTIME"));
		
			}
			if(request.getParameter("WAV_FILE") != null) 
		    { 
				   String wavFile = request.getParameter("WAV_FILE"); 
				   session.setAttribute("WAV_FILE",request.getParameter("WAV_FILE")); 
		    } 

		}
		if ((request.getParameter("ACTION")).equalsIgnoreCase("MODIFY"))
		{
			if(request.getParameter("INIT") != null)
			{
				String inittype = request.getParameter("INIT");
				session.setAttribute("INITIALTYPE",request.getParameter("INIT"));
			}
			if(request.getParameter("FINL") != null)
			{
				String finaltype = request.getParameter("FINL");
				session.setAttribute("FINALTYPE",request.getParameter("FINL"));
			}
		}
	
		session.removeAttribute("Status");
		session.removeAttribute("Reason");
	}
%>
<HTML>
<head>
<title>RBT Subscriber Status</title>
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
			      			<!--Welcome Image -->
			      
						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<tr>
								<td align="center"> <jsp:include page="rbt_sub_status.jsp" />
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

