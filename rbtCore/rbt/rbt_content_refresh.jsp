<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%
String contentRefresh=null;
boolean bDisplayed=false;
bDisplayed = false;
contentRefresh = RBTMOHelper.init().refreshContent();
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
			      			<!--Welcome Image -->
			      
						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
								<% if(contentRefresh.equalsIgnoreCase("true")) {%>
								<%while(!bDisplayed)
								{
									if(RBTMOHelper.init().isInitializationDone()){%>
									<tr>
									<td align="center"> <%out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Content Refresh Done</b></font>");%>
									</tr>
									<%
										bDisplayed = true;
									}
								}
						} else if(contentRefresh.equalsIgnoreCase("false")){%>
						<td align="center"> <%out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>Content Refresh Already Done</b></font>");
						}else if(contentRefresh.equalsIgnoreCase("later")){%>
						<td align="center"> <%out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>Pls retry later</b></font>");
						}%>
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
