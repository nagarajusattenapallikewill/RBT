<%@page import="com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix"%>
<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.subscriptions.RBTGiftDaemon,com.onmobile.apps.ringbacktones.content.database.RBTDBManager,com.onmobile.apps.ringbacktones.common.Tools,com.onmobile.apps.ringbacktones.content.Categories,com.onmobile.apps.ringbacktones.content.Clips,com.onmobile.apps.ringbacktones.common.RBTMultimodal"%><%
String sResponse = "ERROR";
String PARAMETER_TYPE = "GATHERER";

try
{
	String action = request.getParameter("ACTION");
	String param = request.getParameter("PARAM");
	String value = request.getParameter("VALUE");
	String circleID = request.getParameter("CIRCLE_ID");
	Tools.logDetail("rbt_prefix_sync.jsp", "RBT::Action is ", action);
	Tools.logDetail("rbt_prefix_sync.jsp", "RBT::Value is ", value);
	if(action == null || (action.equalsIgnoreCase("UPDATE") && value == null))
	{
		sResponse = "ERROR";
	}
	if(circleID == null)
		circleID = "";
	else if(action.equalsIgnoreCase("GET")) 
	{
		try
		{
				String circlePrefix = "";
				SitePrefix[] prefix = RBTSubUnsub.init().getLocalSitePrefixes();
				if(prefix != null && prefix.length > 0)
				{
					for(int i=0; i<prefix.length; i++)
					{
						if(prefix[i].getCircleID().equalsIgnoreCase(circleID)) // if prefix circleID matches the circleID
							circlePrefix += "," + prefix[i].getSitePrefix().trim();
					}
				}
				sResponse = circlePrefix.substring(1);
		}
		catch(Exception e)
		{
			Tools.logException("rbt_prefix_sync.jsp", "RBT::Exception caught ", e);
			sResponse = "ERROR";
		}
	}
	else if (action.equalsIgnoreCase("UPDATE") && value != null && param != null)
	{
		try
		{
			 RBTSubUnsub.init().updateParameter(PARAMETER_TYPE,param,value); 
			 sResponse="SUCCESS";
		}
		catch(Exception e)
		{
			Tools.logException("rbt_prefix_sync.jsp", "RBT::Exception caught ", e);
			sResponse = "ERROR";
		}
	}
}
catch(Exception e)
{
	Tools.logFatalError("rbt_prefix_sync.jsp", "RBT::Exception caught ", Tools.getStackTrace(e));
	sResponse = "ERROR";
}
	Tools.logDetail("rbt_prefix_sync.jsp", "RBT::strResponse ", sResponse);
%>
<%=sResponse%>