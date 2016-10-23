<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%
String sResponse = "ERROR";
try
{
	RBTMOHelper rbtMO = RBTMOHelper.init();
	RBTDBManager rbtDBManager = RBTDBManager.getInstance();
	String subscriberId = request.getParameter("subscriber_id");
	String callerID = request.getParameter("caller_id");
	String clipId = request.getParameter("clip_id");
	String smsType = request.getParameter("sms_type");
	if(smsType == null || smsType.equalsIgnoreCase("null"))
		smsType = "COPY";
	String selBy = request.getParameter("sel_by");
	if(selBy == null || selBy.length() <= 0 || selBy.equalsIgnoreCase("null"))
		selBy = null;
	
	//System.out.println("subscriber_id- "+subscriberId+" caller_id- "+callerID+ " clip_id- "+clipId);
	if(subscriberId == null)
	{
		sResponse = "ERROR";
	}
	else if (rbtMO.isValidSub(callerID))
	{
		if(clipId != null && clipId.equalsIgnoreCase("null"))
			clipId = null;
		rbtDBManager.insertViralSMSTableMap(subscriberId, null, smsType, callerID, clipId, 0, selBy, null, null);
		sResponse = "SUCCESS";
	}
	else
	{	
		sResponse = rbtMO.connectToRemote(callerID, "rbt_copy.jsp?subscriber_id=" + subscriberId + "&caller_id=" + callerID + "&clip_id=" + clipId+"&sms_type="+smsType+"&sel_by="+selBy, true);
		if(sResponse != null && sResponse.indexOf("SUCCESS") != -1)
		{
			sResponse = "SUCCESS";
		}
		else
			sResponse = "FAILURE";
	}
}
catch(Exception e)
{
	Tools.logException("rbt_copy.jsp", "RBT::Exception caught ", e);
	sResponse = "ERROR";
}
%><%=sResponse%>