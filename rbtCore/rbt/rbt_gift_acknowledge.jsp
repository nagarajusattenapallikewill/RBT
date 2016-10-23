
<%@page import="com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager"%><%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTGiftDaemon"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%>
<%@ page import = "com.onmobile.apps.ringbacktones.common.Tools"%>
<%@ page import = "com.onmobile.apps.ringbacktones.rbtcontents.beans.Category"%>
<%@ page import = "com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.ViralSMSTable"%>
<%@ page import = "java.util.Date"%>
<%
String sResponse = "ERROR";
try
{
	RBTGiftDaemon giftDaemon = new RBTGiftDaemon();
	String subscriberId = request.getParameter("subscriber_id");
	String giftedTo = request.getParameter("gifted_to");
	String clipId = request.getParameter("clip_id");
	String status = request.getParameter("status");
	String requestedTimestamp = request.getParameter("requested_timestamp");
	
	ViralSMSTable viralSMS = null;

	if(subscriberId == null || giftedTo == null || clipId == null || status == null || requestedTimestamp == null)
	{
		sResponse = "ERROR";
	}
	else if(giftDaemon.isRemoteSub(subscriberId))
	{	
		sResponse = giftDaemon.connectToRemote(subscriberId, "rbt_gift_acknowledge.jsp?subscriber_id=" + subscriberId + "&gifted_to=" + giftedTo + "&clip_id=" + clipId + "&requested_timestamp=" + requestedTimestamp);
	}
	else
	{
		try
		{

			String acceptSmsText = "Your gift of %C has been accepted by %S. You have been charged Rs.%A for the gifting";
			String rejectSmsText = "Your gift of %C has been rejected by %S. You have been charged Rs.%A for the gifting";
			String serviceAcceptSmsText = "Your service gift has been accepted by %S. You have been charged Rs.%A for the gifting";
			String serviceRejectSmsText = "Your serivce gift has been rejected by %S. You have been charged Rs.%A for the gifting";

			acceptSmsText = giftDaemon.getParamAsString("GIFT","GIFT_ACCEPT_SMS_TEXT", acceptSmsText);
			rejectSmsText = giftDaemon.getParamAsString("GIFT","GIFT_REJECT_SMS_TEXT", rejectSmsText);
			serviceAcceptSmsText = giftDaemon.getParamAsString("GIFT","GIFT_SERVICE_ACCEPT_SMS_TEXT", serviceAcceptSmsText);
			serviceRejectSmsText = giftDaemon.getParamAsString("GIFT","GIFT_SERVICE_REJECT_SMS_TEXT", serviceRejectSmsText);

			String dbURL = giftDaemon.getParamAsString("COMMON","DB_URL", null);
			int m_nConn = 4;
			m_nConn = giftDaemon.getParamAsInt("SMS","NUM_CONN", 4);
	
			String clipName = null;
			RBTDBManager rbtDBManager = RBTDBManager.init(dbURL, m_nConn);
			//construct the sms
			if(clipId != null && clipId.startsWith("C"))
			{
				Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(clipId.substring(1)));
				clipName = category.getCategoryName();
			}
			else if(clipId != null && !clipId.equalsIgnoreCase("null"))
			{
				Clip clips = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
				clipName = clips.getClipName();
			}

			if(!clipId.equalsIgnoreCase("null") && (clipName == null || clipName.trim().equalsIgnoreCase("")))
			{
				sResponse = "ERROR";
			}
			else
			{
				if(clipId.equalsIgnoreCase("null")) {
					acceptSmsText = serviceAcceptSmsText;
					rejectSmsText = serviceRejectSmsText;
				}
				acceptSmsText = Tools.findNReplaceAll(acceptSmsText,"%S",giftedTo);
				acceptSmsText = Tools.findNReplaceAll(acceptSmsText,"%C", clipName);
				
				rejectSmsText = Tools.findNReplaceAll(rejectSmsText,"%S",giftedTo);
				rejectSmsText = Tools.findNReplaceAll(rejectSmsText,"%C", clipName);
				

				String senderNumber = giftDaemon.getParamAsString("GIFT", "GIFT_ACKNOWLEDGE_SENDER_NUMBER", giftedTo);
	
				if(status.equalsIgnoreCase("ACCEPT_ACK"))
				{
					rbtDBManager.updateViralPromotion(subscriberId, giftedTo, new Date(new Long(requestedTimestamp).longValue()), "GIFTED", "ACCEPTED", new Date(System.currentTimeMillis()), null, null);
					rbtDBManager.updateViralPromotion(subscriberId, giftedTo, new Date(new Long(requestedTimestamp).longValue()), "ACCEPT_ACK", "ACCEPTED", new Date(System.currentTimeMillis()), null, null);

					viralSMS = rbtDBManager.getViralPromotion(subscriberId, giftedTo, new Date(new Long(requestedTimestamp).longValue()), "ACCEPTED");

					String amount = "10";
					if(viralSMS != null && viralSMS.selectedBy() != null && viralSMS.selectedBy().indexOf(":") > 0)
						amount = viralSMS.selectedBy().substring (viralSMS.selectedBy().indexOf(":") + 1);
					
					acceptSmsText = Tools.findNReplaceAll(acceptSmsText,"%A", amount);
					Tools.sendSMS(senderNumber, subscriberId, acceptSmsText, false);
				}
				else if(status.equalsIgnoreCase("REJECT_ACK"))
				{
					rbtDBManager.updateViralPromotion(subscriberId, giftedTo, new Date(new Long(requestedTimestamp).longValue()), "GIFTED", "REJECTED", new Date(System.currentTimeMillis()), null, null);
					rbtDBManager.updateViralPromotion(subscriberId, giftedTo, new Date(new Long(requestedTimestamp).longValue()), "REJECT_ACK", "REJECTED", new Date(System.currentTimeMillis()), null, null);

					viralSMS = rbtDBManager.getViralPromotion(subscriberId, giftedTo, new Date(new Long(requestedTimestamp).longValue()), "REJECTED");

					String amount = "10";
					if(viralSMS != null && viralSMS.selectedBy() != null)
						amount = viralSMS.selectedBy().substring (viralSMS.selectedBy().indexOf(":") + 1);
					
					rejectSmsText = Tools.findNReplaceAll(rejectSmsText,"%A", amount);

					Tools.sendSMS(senderNumber, subscriberId, rejectSmsText, false);
				}
				
				sResponse = "SUCCESS";
			}
		}
		catch(Exception e)
		{
			Tools.logException("rbt_gift.jsp", "RBT::Exception caught ", e);
			sResponse = "ERROR";
		}
	}
}
catch(Exception e)
{
	Tools.logException("rbt_gift.jsp", "RBT::Exception caught ", e);
	sResponse = "ERROR";
}
%><%=sResponse%>