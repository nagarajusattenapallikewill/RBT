<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTUGCHelper,com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*"%>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<form name="feed" >
<%
String strSubID = null;
String strResponse = "FAILURE";
String strTransID = null;
String promoIdList = null;
String sessionId = null;
try
{

String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);

RBTUGCHelper rbtUGC = RBTUGCHelper.init();
RBTSubUnsub rbtLogin = RBTSubUnsub.init();

strSubID = request.getParameter("SUB_ID");
promoIdList = request.getParameter("PROMO_ID");
strTransID = request.getParameter("TRANS_ID");
sessionId = session.getId();

System.out.println("RBT: UGC INCOMING REQUEST FOR SUB_ID" + strSubID + " and PROMO_ID " + promoIdList + " and FILE " +strTransID + " and SESSION-ID "+sessionId) ;

if(rbtUGC == null || rbtLogin == null )
{
	Tools.logStatus("RBT_UGC","rbt_ugc_expire.jsp","rbtUGC is "+rbtUGC + " and rbtLogin is "+rbtLogin);
	strResponse = "FAILURE";
}
else
{
	boolean isTransExist = false;
	if (strTransID != null && strSubID != null)
	{
		isTransExist = rbtLogin.checkTransIDExist(strTransID,
											  "UGC_EXPIRE");
		if (!isTransExist)
		{
			rbtLogin.addTransData(strTransID, strSubID,
							  "UGC_EXPIRE");
		}
	}
	if (isTransExist)
		strResponse = "Invalid Request. Already recieved a request with the same TransID : "
			+ strTransID
			+ " and subscriber ID : "
			+ strSubID + " and promoIDList "+promoIdList;
	else
	{
		boolean success = false;
		if(promoIdList != null)
			success = rbtUGC.expireUGCClipsForPromoIDs(promoIdList);
		else if (strSubID != null)
			success = rbtUGC.expireUGCClipsOfCreator(strSubID);
		if(success)
		{
/*			String url = "http://localhost:8080/rbt/rbt_mo_cache.jsp?ACTION=REMOVE&PROMO_ID="+promoIdList;
			StringBuffer strBuffer = new StringBuffer();
			Integer resInt = new Integer(-1);
			Tools.callURL(url, resInt, strBuffer);
			Tools.logDetail("RBT_UGC", "rbt_ugc_expire.jsp", 
				   "RBT::Putting UGC clip in cache with response " 
						   + strBuffer);
*/						   
			strResponse = "SUCCESS";
   
		}
		else
			strResponse = "FAILURE";
		}
}
}
catch(Exception e)
{
	System.out.println("Exception in rbt_ugc_expire "+e.getMessage());
	Tools.logException("RBT_UGC", "rbt_ugc_expire.jsp", e);
	strResponse = "FAILURE";
}

try
{
	out.flush();
	out.write(strResponse);
//	out.close();
}
catch(Exception e)
{
	Tools.logException("RBT_UGC", "rbt_ugc_expire.jsp", e);
}
%>
</form>