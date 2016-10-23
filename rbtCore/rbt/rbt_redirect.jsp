<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
<%@page	import="com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%>
<%@page	import="com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>

<%@page import="com.onmobile.apps.ringbacktones.content.*"%>
<%@ page import="com.onmobile.apps.ringbacktones.subscriptions.*,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*,java.util.zip.GZIPOutputStream"%><%



//To prevent getOutputStream() has already been called error, as the generated servlet will have out . write statements for each free space or line.
%><%

String strSubID = null;
String requestValue=null;
String sResponse = "FAILURE";
boolean m_usePoolDefault = true;
boolean m_usePool = m_usePoolDefault;

RBTMOHelper rbtMO = RBTMOHelper.init();

boolean isprepaid = false;

System.out.println("RBT: rbt_redirect.jsp INCOMING REQUEST = " + request.getParameter("request_value") + " from SubscriberID = " + request.getParameter("SUB_ID"));

requestValue=request.getParameter("request_value");
strSubID = request.getParameter("SUB_ID");

if(requestValue.equalsIgnoreCase("vcode")) 
{ 
	try
	{
		if(strSubID == null)
		{
			sResponse = "ERROR";
		}
		else if (rbtMO.isValidSub(strSubID))
		{
			sResponse = rbtMO.m_rbtDBManager.getSubscriberDefaultVcode(strSubID,0);
		}
		else
		{	
			sResponse = rbtMO.connectToRemote(strSubID, "rbt_redirect.jsp?request_value=vCode&SUB_ID="+strSubID, true);
		}
	}
	catch(Exception e)
	{
		Tools.logException("rbt_redirect","rbt_redirect.jsp",e);
		sResponse = "ERROR";
	}
}

out.write(sResponse);


%>
