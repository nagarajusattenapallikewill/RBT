	<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
	<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub" %>
<%@ include file = "validate.jsp" %>
<% 
String strUser;
%>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
strUser  = (String)(session.getAttribute("UserId"));
if (validateUser(request, session, "rbt_change_sub_type.jsp", response)) {
	
	System.out.println("Pre<->Post Conversion Done By User "+strUser);
%>

	<% Subscriber subscriber;
	%>
		<%
		subscriber = null;
		boolean isPrepaid = false;
		if(request.getParameter("updated").equalsIgnoreCase("false")){%>
		<%
			subscriber = rbtLogin.getSubscriber(request.getParameter("SUB_ID"));
			if(request.getParameter("subType").equalsIgnoreCase("Prepaid")){
			
				isPrepaid = true;
			}
			else{
				
				isPrepaid = false;
			}
		
				rbtLogin.setPrepaidYes(request.getParameter("SUB_ID"), isPrepaid);
	}%>
			<jsp:forward page="rbt_change_sub_type.jsp" />


<%
	}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>