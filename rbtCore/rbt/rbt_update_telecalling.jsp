<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
<%@ page import = "java.util.*,java.io.*"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants" %>

<%@ include file = "validate.jsp" %>
	<% 
	String strUser = null;
	String actInfo = null;
	String strStatus = null;
	String strReason = null;
	String strValue = null;
	String strResult = null;
	String type = null;
	boolean bPrepaid;
	%>

	<%
		RBTSubUnsub rbtLogin = RBTSubUnsub.init();
		String strIP  = request.getRemoteAddr();
		strUser  = (String)(session.getAttribute("UserId"));
		if(strUser == null)
		actInfo = strIP + ":Direct"; 
		else
		actInfo = strIP + ":" + strUser; 

		if (validateUser(request, session,  "rbt_telecalling.jsp", response)) { %>

		<% String caller_ID = null;
		String sub = null;
		String songName = null;
		String categoryName = null;
		String callerID = null;
		String user = null;
		String file = null;
		int categoryID = -1;
		%>
		<%
		try
			{
		bPrepaid = false;
		sub = (String)session.getAttribute("Telsub");
		songName = (String)session.getAttribute("Telsong");
		categoryName = (String)session.getAttribute("Telcategory");
		callerID =  (String)session.getAttribute("Telcallerid");
		type =   (String)session.getAttribute("Teltype");
		session.removeAttribute("Telsong");
		session.removeAttribute("Telcategory");
		session.removeAttribute("Telsub");
		session.removeAttribute("Telcallerid");
		session.removeAttribute("Teltype");

		if(type != null && type.equalsIgnoreCase("Prepaid"))
			bPrepaid = true;

		//prepaid-postpaid change by gautam
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "USE_DEFAULT_TYPE_GUI_PROCESSING", "TRUE"))
			{
				if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
					bPrepaid = true;
				if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Postpaid"))
					bPrepaid=false;
			}

		Date endDate = null;

		if( sub != null && (!sub.equals("")))
		{
			com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriberP = Processor.getSubscriber(sub);
			sub = subscriberP.getSubscriberID();
			String circleID = 	subscriberP.getCircleID();

			
			if(!subscriberP.isCanAllow())
			{		
				strStatus = "FAILURE";
				strReason = "No. "+sub + " is blacklisted.";
			}
			else if (subscriberP.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
			{
				strStatus = "FAILURE";
				strReason = "No. "+sub + " is suspended.";
			}
			else if(!subscriberP.isValidPrefix())
			{
				strStatus = "FAILURE";
				strReason = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(sub);
			}
		

		try
		{
			categoryID = Integer.parseInt(categoryName);
			if(callerID == null || callerID.length() <7)
			{
				callerID = null;
			}
		}
		catch(Exception e)
		{
		
		}
		
		System.out.println("Selection Added for a subscriber by "+strUser);
		
		rbtLogin.actSubscriber(sub, "CC", bPrepaid, true, actInfo, circleID);
		Subscriber subscriber = rbtLogin.getSubscriber(sub);

		String subYes = null,subClass = null;
		int max = 0;
		boolean OptIn = false;
		if(subscriber != null)
		{
			subYes = subscriber.subYes();
			if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                OptIn = true; 
			max = subscriber.maxSelections();
		}
			
		rbtLogin.addSelections(sub, callerID, subscriber.prepaidYes(), false, categoryID, songName, null, 1, 0, "CC", actInfo, 0, 23, null, null, null, subYes, max, subClass,OptIn, subscriber,null);
		session.setAttribute("updated","SUCCESS");

		}
		
		}
		catch(Exception e)
		{
				System.out.println("Exception in rbt_update_telecalling "+e.getMessage());
				e.printStackTrace();
				session.setAttribute("updated","FAILURE");
				session.setAttribute("reason","Internal Error");
		}
		%>
			<jsp:forward page="rbt_telecalling.jsp" />


<%
}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>