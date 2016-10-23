<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTGiftDaemon"%>
<%String subscriberId = request.getParameter("subscriber_id");
RBTGiftDaemon giftDaemon = new RBTGiftDaemon();
String sResponse = null;
if(subscriberId == null)
{
	sResponse = "ERROR";
}
else
{
	if(giftDaemon.isRemoteSub(subscriberId))
	{	
		sResponse = giftDaemon.connectToRemote(subscriberId, "rbt_status.jsp?subscriber_id=" + subscriberId);
	}
	else
	{
		RBTSubUnsub rbtLogin = RBTSubUnsub.init();
		if(rbtLogin.isSubActive(subscriberId))
		{
			sResponse = "ACTIVE";
		}
		else
		{
			sResponse = "INACTIVE";
		}
	}
}
%><%=sResponse%>