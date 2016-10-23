<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.*,com.onmobile.apps.ringbacktones.common.Tools,java.util.*,java.io.*,java.util.zip.GZIPOutputStream,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber,com.onmobile.apps.ringbacktones.provisioning.AdminFacade"%><%

//To prevent getOutputStream() has already been called error, as the generated servlet will have out . write statements for each free space or line.
%><%
String strSubID = null;
String strPrepaid = null;
String strRequestType = null;
String strRequestValue = null;
String callerID = null;
String categoryID= null;
String categoryType = null;
String rbt = null;
String clipId = null;
String selInterval = null;
int frmTime = 0;
int toTime = 2359;
String strActivatedBy = null;
String strActInfo = null;
String strDeactivatedBy = null;
String strSelectedBy = null;
String strSelectionInfo = null;
String strSubClass = null;
String strChargeClass = null;
String strChannel = null;
StringBuffer strBuffer = new StringBuffer();
String str = null;
String strLTPPoints = null;
boolean isprepaid = false;
String gifter = null; 
String giftee = null; 
String wavFile = null; 
String contentId = null; 
String strSelInterval = null;
String strCallerID = null;
String strSetTime = null;
String strFromTime = null;
String strToTime = null;
String strNewSubID = null;
String statusInfo = null;

RBTChannelHelper rbtChannel = null;
RBTSubUnsub rbtSubUnsub = null;

try
{
	rbtChannel = new RBTChannelHelper();
	rbtSubUnsub = RBTSubUnsub.init();
}
catch(Exception e)
{
	Tools.logException("RBT_Channel","RBT_Channel.jsp",e);
}
System.out.println("RBT: Channel INCOMING REQUEST = " + request.getParameter("REQUEST_TYPE") + " from SubscriberID = " + request.getParameter("SUB_ID"));
gifter = request.getParameter("GIFTER"); 
giftee = request.getParameter("GIFTEE"); 
wavFile = request.getParameter("WAV_FILE"); 
contentId = request.getParameter("CONTENT_ID"); 

strSubID = request.getParameter("SUB_ID");
strNewSubID = request.getParameter("NEW_SUB_ID");
strCallerID = request.getParameter("CALLER_ID");
strSetTime = request.getParameter("SET_TIME");
strFromTime = request.getParameter("FROM_TIME");
strToTime = request.getParameter("TO_TIME");
strSelInterval = request.getParameter("SEL_INTERVAL");
strPrepaid = request.getParameter("SUB_TYPE");
strRequestType = request.getParameter("REQUEST_TYPE");
strRequestValue = request.getParameter("REQUEST_VALUE");
strActivatedBy = request.getParameter("ACTIVATED_BY");
strActInfo = request.getParameter("ACT_INFO");
strDeactivatedBy = request.getParameter("DEACTIVATED_BY");
strSelectedBy = request.getParameter("SELECTED_BY");
strSelectionInfo = request.getParameter("SELECTION_INFO");
strSubClass = request.getParameter("SUB_CLASS");
strChargeClass = request.getParameter("CHARGE_CLASS");
strChannel = request.getParameter("CHANNEL");
strLTPPoints = request.getParameter("POINTS");
categoryID = request.getParameter("CATEGORY_ID");
categoryType = request.getParameter("CATEGORY_TYPE");
statusInfo = request.getParameter("STATUS");


Tools.logDetail("rbt_channel ", "testing ", "request type " + strRequestType + " subscriber id " + strSubID + " caller id " + strCallerID );

if(strRequestType == null || rbtChannel == null)
{
	Tools.logStatus("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp","strRequestType is " + strRequestType + " and rbtChannel is " + rbtChannel);
	strBuffer = new StringBuffer();
	strBuffer.append("Error");
}
else
{
	boolean isPrepaid = false;
	if(strPrepaid != null && strPrepaid.equalsIgnoreCase("true"))
	{
		isPrepaid = true;
	}
//	 change 'status' to 'selections' later when every one is done with their changes.
	if(strRequestType.equalsIgnoreCase("subYes"))
	{
		try
		{
			strBuffer = rbtChannel.getSubscriberSubYes(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		
	}

	if(strRequestType.equalsIgnoreCase("status"))
	{
		try
		{
			strBuffer = rbtChannel.getSubscriber(strSubID.trim());
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		
	}

//	 to use this method later when everyone else is done with the changes. 
    if(strRequestType.equalsIgnoreCase("selections")) 
    { 
            try 
            { 
                    strBuffer = rbtChannel.getSubscriberSelections(strSubID.trim()); 
            } 
            catch(Exception e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
    // to be removed later on and use 'status' instead with the underlying code. 
    if(strRequestType.equalsIgnoreCase("sub_status")) 
    { 
            try 
            { 
                    strBuffer = rbtChannel.getSubscriptionStatus(strSubID.trim()); 
            } 
            catch(Exception e) 
            { 
                    Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
                    strBuffer = new StringBuffer(); 
                    strBuffer.append("Error"); 
            } 

    } 
    
    if(strRequestType.equalsIgnoreCase("changeMSISDN"))
    {
    		try
    		{
    			RequestDispatcher rd = getServletContext().getRequestDispatcher("/ChangeMsisdn.do?SUBSCRIBER_ID="+strSubID+"&NEW_SUBSCRIBER_ID="+strNewSubID);
    			rd.forward(request, response); 
    		}
    		catch(Exception e)
    		{
    			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e); 
            }
    
    }
    
    if(strRequestType.equalsIgnoreCase("updateSelection"))
    {
    	try
    	{
    		frmTime = Integer.parseInt(strFromTime);
    		toTime = Integer.parseInt(strToTime);

			if(strCallerID != null && strCallerID.equalsIgnoreCase("ALL"))
				strCallerID = null;
    		strBuffer = rbtChannel.updateSubscriberSelection(strSubID.trim(),strCallerID,wavFile,strSetTime,frmTime,toTime,strSelInterval,strSelectedBy);
    	}
    	catch(Exception e)
    	{
    		Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
    	}
 			   	
    }

	if(strRequestType.equalsIgnoreCase("act"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp","strRequestValue is " + strRequestType);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			Subscriber subscriber = Processor.getSubscriber(strSubID.trim());
			strSubID = subscriber.getSubscriberID();
			String circleId = subscriber.getCircleID();
			boolean pre = true;
			if(strRequestValue.equalsIgnoreCase("Prepaid"))
			{
				pre = true;
			}
			else if(strRequestValue.equalsIgnoreCase("Postpaid")) 
            { 
                    pre = false; 
            } 
			try
			{
				strBuffer = rbtChannel.activateSubscriber(strSubID.trim(), pre, strActivatedBy, strActInfo, strSubClass,circleId);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	if(strRequestType.equalsIgnoreCase("deact"))
	{
		try
		{
			
			strBuffer = rbtChannel.deactivateSubscriber(strSubID.trim(), strDeactivatedBy,"");
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("search"))
	{
		try
		{
			strBuffer = rbtChannel.searchSong(strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	
	if(strRequestType.equalsIgnoreCase("sms"))
	{
		try
		{
			strBuffer = rbtChannel.sendSMS(strSubID.trim(), strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}

	if(strRequestType.equalsIgnoreCase("set"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp","strRequestValue is " + strRequestValue);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			StringTokenizer token = new StringTokenizer(strRequestValue, " ");
			if(token.hasMoreTokens())
			{
				callerID = token.nextToken().trim();
				if(callerID.equalsIgnoreCase("ALL"))
				{
					callerID = null;
				}

			}
			if(token.hasMoreTokens())
			{
				categoryID = token.nextToken().trim();
			}
			if(token.hasMoreTokens())
			{
				rbt = token.nextToken().trim();
			}

			try
			{
				frmTime = Integer.parseInt(token.nextToken().trim());
				toTime = Integer.parseInt(token.nextToken().trim());
			}
			catch(Exception e)
			{
				frmTime = 0;
				toTime = 2359;
			}
			if(token.hasMoreTokens())
			{
				selInterval = token.nextToken().trim();
			}
			try
			{
				strBuffer = rbtChannel.addSelections(strSubID, callerID, Integer.parseInt(categoryID), rbt, frmTime, toTime, strSelectedBy, strSelectionInfo, strChargeClass,selInterval);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	
	 if(strRequestType.equalsIgnoreCase("setByName"))
       {
               if(strRequestValue == null)
               {
                       Tools.logStatus("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp","strRequestValue is " + strRequestValue);
                       strBuffer = new StringBuffer();
                       strBuffer.append("Error");
               }
               else
               {
                       StringTokenizer token = new StringTokenizer(strRequestValue, " ");
                       if(token.hasMoreTokens())
                       {
                               callerID = token.nextToken().trim();
                               if(callerID.equalsIgnoreCase("ALL"))
                               {
                                       callerID = null;
                               }

                       }
                       if(token.hasMoreTokens())
                       {
                               categoryID = token.nextToken().trim();
                       }
                       if(token.hasMoreTokens())
                       {
                               rbt = token.nextToken().trim();
                       }

                       try
                       {
                               frmTime = Integer.parseInt(token.nextToken().trim());
                               toTime = Integer.parseInt(token.nextToken().trim());
                       }
                       catch(Exception e)
                       {
                               frmTime = 0;
                               toTime = 2359;
                       }

                       try
                       {
                               strBuffer = rbtChannel.addSelectionsByName(strSubID, callerID, Integer.parseInt(categoryID), rbt, frmTime, toTime, strSelectedBy, strSelectionInfo, strChargeClass,null);
                       }
                       catch(Exception e)
                       {
                               Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
                               strBuffer = new StringBuffer();
                               strBuffer.append("Error");
                       }
               }
       }
	
	if(strRequestType.equalsIgnoreCase("setById"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp","strRequestValue is " + strRequestValue);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			Subscriber subscriber = Processor.getSubscriber(strSubID.trim());
			strSubID = subscriber.getSubscriberID();
			String circleId = subscriber.getCircleID();
			
			StringTokenizer token = new StringTokenizer(strRequestValue, " ");
			if(token.hasMoreTokens())
			{
				callerID = token.nextToken().trim();
				if(callerID.equalsIgnoreCase("ALL"))
				{
					callerID = null;
				}

			}
			if(token.hasMoreTokens())
			{
				categoryID = token.nextToken().trim();
			}
			if(token.hasMoreTokens())
			{
				clipId = token.nextToken().trim();
			}

			try
			{
				frmTime = Integer.parseInt(token.nextToken().trim());
				toTime = Integer.parseInt(token.nextToken().trim());
			}
			catch(Exception e)
			{
				frmTime = 0;
				toTime = 2359;
			}
			if(token.hasMoreTokens())
			{
				selInterval = token.nextToken().trim();
			}

			try
			{
				strBuffer = rbtChannel.addSelections(strSubID, callerID, Integer.parseInt(categoryID),Integer.parseInt(clipId), frmTime, toTime, strSelectedBy, strSelectionInfo, strChargeClass,selInterval, circleId);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}
	
	if(strRequestType.equalsIgnoreCase("del"))
	{
		if(strRequestValue == null)
		{
			Tools.logStatus("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp","strRequestType is " + strRequestType);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
		else
		{
			if(strRequestValue.equalsIgnoreCase("ALL"))
			{
				strRequestValue = null;
			}

			try
			{
				strBuffer = rbtChannel.removeSelection(strSubID, strRequestValue, strChannel);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		}
	}

	if(strRequestType.equalsIgnoreCase("removeSelection"))
	{
		
			if(strCallerID!=null && strCallerID.equalsIgnoreCase("ALL"))
			{
				strCallerID = null;
			}

			int fromTime =0;
			toTime = 2359;
			int status = 1;
			if (statusInfo!=null)
			status=Integer.parseInt(statusInfo);
			if (strFromTime!=null)
			fromTime = Integer.parseInt(strFromTime);
			if (strToTime!=null)
			toTime = Integer.parseInt(strToTime);
			try
			{
				strBuffer = rbtChannel.removeSelectionForWebWap(strSubID, strCallerID, status, fromTime, toTime, strChannel,wavFile);
			}
			catch(Exception e)
			{
				Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
				strBuffer = new StringBuffer();
				strBuffer.append("Error");
			}
		
	}
	if(strRequestType.equalsIgnoreCase("cat"))
	{
		try
		{
			strBuffer = rbtChannel.getActiveCategories(rbtChannel.getCircleID(strSubID), 'b');
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("redeemLTP"))
	{
		try
		{
			strBuffer = rbtChannel.redeemLTPPoints(strSubID, strLTPPoints);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("addGift"))
	{
		try
		{
			strBuffer = rbtChannel.addGift(giftee, gifter, strChannel, contentId);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("getSubscriberDownloads"))
	{
		try
		{
			strBuffer = rbtChannel.getDownloads(strSubID);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("removeSubscriberDownload"))
	{
		try
		{
			strBuffer = rbtChannel.removeDownload(strSubID, wavFile, strChannel, categoryID,categoryType );
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("newsletter"))
	{
		try
		{
			strBuffer = rbtChannel.setNewsletter(strSubID, strRequestValue);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("getnewsletter"))
	{
		try
		{
			strBuffer = rbtChannel.getNewsletter(strSubID);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	
	if(strRequestType.equalsIgnoreCase("addDownload"))
	{
		try
		{
			if(categoryID == null || categoryID.equalsIgnoreCase("null"))
				categoryID = "2";
			if(categoryType == null || categoryType.equalsIgnoreCase("null"))
				categoryType = "5";
			strBuffer = rbtChannel.addDownload(strSubID, wavFile,categoryID,categoryType,strChannel);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
	if(strRequestType.equalsIgnoreCase("SUBINFO"))
	{
		try
		{
			strBuffer = rbtChannel.getSubscriberInfo(strSubID);
		}
		catch(Exception e)
		{
			Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
			strBuffer = new StringBuffer();
			strBuffer.append("Error");
		}
	}
}
try
{
	if(strRequestType != null && strRequestType.equalsIgnoreCase("cat"))
	{
		response.setHeader("Content-Encoding", "gzip");
		String s = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+strBuffer.toString();
		ByteArrayOutputStream b = new ByteArrayOutputStream(s.length());
		GZIPOutputStream gzos = new GZIPOutputStream(b);
		gzos.write(s.getBytes());
		gzos.flush();
		gzos.close();
		response.getOutputStream().write(b.toByteArray(),0,b.toByteArray().length);
		response.getOutputStream().close();
	}
	else
		out.write(strBuffer.toString());
	//out.close();
}
catch(Exception e)
{
	Tools.logException("RBT_CHANNEL_"+strChannel,"RBT_CHANNEL.jsp",e);
}

%>