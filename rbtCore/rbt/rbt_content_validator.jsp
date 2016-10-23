<%@ page import = "java.util.*,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper,com.onmobile.apps.ringbacktones.common.Tools"%><%
String strIP  = request.getRemoteAddr();
String actInfo = strIP + ":SMS";
String strName  = request.getRemoteHost();
String strMessage = null;
String strSubID = null;
String strResult = null;
String X_ONMOBILE_REASON = null;
RBTMOHelper rbtMO = RBTMOHelper.init(); 
System.out.println("RBT: SMS Text = " + request.getParameter("SMS_TEXT") + ", SUB_ID = " + request.getParameter("SUB_ID") + " from " + strIP);
//rbtMO.addToAccounting("request", request.getParameter("SUB_ID"), request.getParameter("SMS_TEXT"), null, strIP);
//try
//{
if (rbtMO.isValidIP(strIP) || rbtMO.isValidIP(strName))
	{
		strMessage = request.getParameter("SMS_TEXT");
		strSubID = request.getParameter("SUB_ID");

		if(strMessage != null && strSubID != null)
		{
			if(rbtMO.isRemoteSub(strSubID.trim()))
			{
				X_ONMOBILE_REASON = rbtMO.connectToRemoteForSM(strSubID.trim(), strMessage.trim(), null);
			}
			else
			{
			
				boolean valid = rbtMO.isValidSub(strSubID.trim());
				if(!valid || rbtMO.isVodafoneOCGInvalidSMS(strMessage.trim()))
				{
					X_ONMOBILE_REASON = "NOTVALID";
				}
				else
				{
					Hashtable result = new Hashtable() ;
					int retcode = rbtMO.parseSMSText(strSubID.trim(), strMessage.trim(), result, false, actInfo, "VALIDATE");
					String sms = (String)result.get("Reason");

					if(result.containsKey("OCG_CHARGE_ID"))
					{
						X_ONMOBILE_REASON = "VALID:" + (String)result.get("OCG_CHARGE_ID");
					}
					else
					{
						X_ONMOBILE_REASON = "NOTVALID";
					}
				}
			}
		}
		else
		{
			X_ONMOBILE_REASON = "Insufficient Parameters";
		}
	}
	else
	{
		X_ONMOBILE_REASON = "Invalid IP Address";
	}

	System.out.println("RBT::Sub ID->" + strSubID + " SMS Text->" + strMessage + " SMS Response->" +X_ONMOBILE_REASON);
	out.flush();
	out.write(X_ONMOBILE_REASON);
//}
//catch(Throwable t)
//{
//	System.out.println ("RBT::Caught exception in rbt_sms.jsp " +t.getMessage());
//}
%>