<%@ page import="com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub" %>
<%@ include file="validate.jsp"%>
<%
if (validateUser(request, session,  "rbt_tnb.jsp", response)) {
	session.removeAttribute("Status");
	session.removeAttribute("Reason");
%>
<HTML>
	<HEAD>
		<TITLE>RBT Try and Buy</TITLE>
	</HEAD>
	<BODY topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0">
		<TABLE border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
			<TR>
				<TD colspan=3 valign="top" height="15%">
					<jsp:include page="rbt_header_inc.jsp"/>
				</TD>
			</TR>
			<TR>
				<!-- Menu Start -->
				<TD width="23%" bgcolor="#ffdec8" valign="top">
					<jsp:include page="rbt_menu_inc.jsp" />
				</TD>
				<!--Menu End -->
				<TD width="77%" bgcolor="#ffedd9" valign="top">
					<BR>
					<TABLE align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN:top">
						<TR>
							<TD><jsp:include page="rbt_tnb_select.jsp"/></TD>
						</TR>
					</TABLE>
				</TD>
			</TR>
		</TABLE>
	</BODY>
</HTML>
<%
}
else {
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>