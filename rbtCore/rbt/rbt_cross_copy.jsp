<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%
String sResponse = "ERROR";
try
{
	RBTMOHelper rbtMO = RBTMOHelper.init();
	RBTDBManager rbtDBManager = RBTDBManager.getInstance();
	String subscriberId = request.getParameter("subscriber_id");
	if( subscriberId != null && subscriberId.equalsIgnoreCase("null"))
		subscriberId = null;
	String callerID = request.getParameter("caller_id");
	String clipId = request.getParameter("clip_id");
	String smsType = request.getParameter("sms_type");
	String selBy = request.getParameter("sel_by");
	if(selBy == null || selBy.length() <= 0 || selBy.equalsIgnoreCase("null"))
		selBy = null;
	if(smsType == null || smsType.equalsIgnoreCase("null"))
		smsType = "COPY";

	if(rbtDBManager == null)
		sResponse = "TECHNICAL DIFFICULTY ERROR";
	else if(subscriberId == null || callerID == null)
		sResponse = "INSUFFICIENT PARAMETERS";
	else
	{
		if(clipId != null && clipId.equalsIgnoreCase("null"))
			clipId = null;
		rbtDBManager.insertViralSMSTableMap(subscriberId, null, smsType, callerID, clipId, 0, selBy, null, null);
		sResponse = "0";
	}
}
catch(Throwable e)
{
	Tools.logException("rbt_copy.jsp", "RBT::Exception caught ", e);
	sResponse = "-1";
}
%><%=sResponse%>