
<%@page import="com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager"%><%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,
com.onmobile.apps.ringbacktones.subscriptions.RBTGiftDaemon,com.onmobile.apps.ringbacktones.content.database.RBTDBManager,
com.onmobile.apps.ringbacktones.common.Tools,java.util.ResourceBundle,
com.onmobile.apps.ringbacktones.rbtcontents.beans.Category,com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip,
com.onmobile.apps.ringbacktones.common.RBTMultimodal,com.onmobile.apps.ringbacktones.genericcache.*,
java.net.InetAddress,java.util.Date,java.util.Calendar"%><%
String sResponse = "ERROR";
try
{
	RBTGiftDaemon giftDaemon = new RBTGiftDaemon();
	ResourceBundle resourceBundle = ResourceBundle.getBundle("rbt");
	String dbURL = resourceBundle.getString("DB_URL");
	String poolSizeStr = resourceBundle.getString("DB_POOL_SIZE");
	int poolSize = 4;
	if (poolSizeStr != null)
	{
		try
		{
			poolSize = Integer.parseInt(poolSizeStr);
		}
		catch (Exception e)
		{
			poolSize = 4;
		}
	}
	RBTDBManager rbtDBManager = RBTDBManager.init(dbURL, poolSize);

	String subscriberId = request.getParameter("subscriber_id");
	String giftedBy = request.getParameter("gifted_by");
	String clipId = request.getParameter("clip_id");
	String requestedTimestamp = request.getParameter("requested_timestamp");
	String strIP = request.getRemoteAddr();	
	System.out.println("subscriber_id- "+subscriberId+" gifted_by- "+giftedBy+ " clip_id- "+clipId+" requested_timestamp "+requestedTimestamp);
	
	if(clipId == null || clipId.trim().equals("null"))
	{
		String smsText = "I have gifted a RBT to you.";
		String senderNumber = giftedBy;
		
		senderNumber = giftDaemon.getParamAsString("GIFT", "GIFT_SENDER_NUMBER", giftedBy);
		smsText = giftDaemon.getParamAsString("GIFT", "GIFT_SERVICE_SMS_TEXT", "I have gifted a RBT to you");

		smsText = Tools.findNReplaceAll(smsText,"%S",giftedBy);

		try
		{
			Tools.sendSMS(senderNumber, subscriberId, smsText, false);
		}
		catch(Exception e)
		{
		}
		sResponse = "SUCCESS";	
	}
	else if(subscriberId == null || giftedBy == null || clipId == null || requestedTimestamp == null)
	{
		sResponse = "ERROR";
	}
	else if(giftDaemon.isRemoteSub(subscriberId))
	{	
		sResponse = giftDaemon.connectToRemote(subscriberId, "rbt_gift.jsp?subscriber_id=" + subscriberId + "&gifted_by=" + giftedBy + "&clip_id=" + clipId + "&requested_timestamp=" + requestedTimestamp);
	}
	else
	{
		try
		{
			//add to context table
			//take Sender configuration, sms text from config file
			String APPID="COM.ONMOBILE.APPS.RINGBACKTONES.VOICE.SORBTMANAGER";
			String senderNumber = giftedBy;
			
			senderNumber = giftDaemon.getParamAsString("GIFT", "GIFT_SENDER_NUMBER", giftedBy);

			//rbtDB = RBTDBManager.init(dbURL, usePool, RBTCommonConfig.getInstance().countryPrefix());
			//String[] sListSubscribers = new String[1];
			//sListSubscribers[0] = subscriberId;
			
			//Calendar cal = Calendar.getInstance();
  	        //Date curDate = cal.getTime();
  	        boolean bNational = true;
  	        if(rbtDBManager.isValidPrefix(giftedBy) && rbtDBManager.isValidPrefix(subscriberId))
  	         {
	  	         bNational = false;
             }
			String contextKey = "RBT_GIFT:" + subscriberId + ":" + giftedBy + ":" + clipId + ":" + requestedTimestamp;
			String contextData = contextKey;
			String poolKey = "RBT";
		
			String smsText = "I have gifted a RBT to you. To accept this gift call XXXXXX";
			smsText = giftDaemon.getParamAsString("GIFT", "GIFT_SMS_TEXT", "I have gifted a RBT to you. To accept this gift call XXXXXX");
			
			String clipName = null;
			String promoId = null;

			//construct the sms
			if(clipId.startsWith("C"))
			{
				Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(clipId.substring(1)));
				clipName = category.getCategoryName();
				promoId = category.getCategoryPromoId();
				
			}
			else
			{
				Clip clips = RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
				clipName = clips.getClipName();
				promoId = clips.getClipPromoId();
			}
			
			if(clipName == null || clipName.trim().equalsIgnoreCase(""))
			{
				sResponse = "ERROR";
			}
			else
			{
				smsText = Tools.findNReplaceAll(smsText,"%S",giftedBy);
				smsText = Tools.findNReplaceAll(smsText,"%C", clipName);
				smsText = Tools.findNReplaceAll(smsText, "%P", promoId);

				//code changed to support INBOX/MM models
				String ret = "";
				if(giftDaemon.getParamAsBoolean("GIFT", "GIFT_MM_MODEL", "TRUE")) {

					String url =giftDaemon.getParamAsString("GIFT", "SMS_DB_URL", null);
					RBTMultimodal multimodal = RBTMultimodal.getInstance(url);
					ret = multimodal.getMultimodalNumber(subscriberId, APPID, contextKey, contextData, poolKey);
				}
				
				if(ret != null)
				{
					//code changed to support INBOX/MM models
					if(giftDaemon.getParamAsBoolean("GIFT", "GIFT_MM_MODEL", "TRUE"))
						smsText = Tools.findNReplaceAll(smsText,"%N", ret);
					try
					{
						Tools.sendSMS(senderNumber, subscriberId, smsText, false);
					}
					catch(Exception e)
					{

					}
					  /*InetAddress ip = java.net.InetAddress.getLocalHost();
					  String IPAddress =  ip.getHostAddress();
			   		  if(strIP.equalsIgnoreCase(IPAddress) || strIP.equalsIgnoreCase("127.0.0.1"))
					  {
					
					  }
		   			  else	*/
		   			if(bNational)
					{
						//RBTDBManager.init(dbURL, usePool, RBTCommonConfig.getInstance().countryPrefix()).insertViralSMSTable(giftedBy, null, "GIFTED", subscriberId, clipId, 0, "SMS", null);
						long lTime = new Long(requestedTimestamp).longValue();
		   				rbtDBManager.insertViralSMSTableMap(giftedBy, new Date(lTime), "GIFTED", subscriberId, clipId, 0, "SMS", null, null);
					}
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