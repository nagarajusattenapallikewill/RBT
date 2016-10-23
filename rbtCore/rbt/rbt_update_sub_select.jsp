<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Categories"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Clips"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Groups"%>
<%@ page import = "com.onmobile.apps.ringbacktones.cache.content.ClipMinimal"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.database.ClipsImpl"%>
<%@ page import = "java.util.*,java.io.*"%>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub" %>
<%@ include file = "validate.jsp" %>
	<% 
	String strUser = null;
	String actInfo = null;
	String strStatus = null;
	String strReason = null;
	String strValue = null;
	String strResult = null;
	boolean bBlackoutSMS;
	boolean addToLoop = false;
	String choice=null;
	%>
	<%
		RBTSubUnsub rbtLogin = RBTSubUnsub.init();
		String strIP  = request.getRemoteAddr();
		strUser  = (String)(session.getAttribute("UserId"));
		if(strUser == null)
		actInfo = strIP + ":Direct"; 
		else
		actInfo = strIP + ":" + strUser; 

		if (validateUser(request, session, "rbt_subs_selections.jsp", response)) { %>
	
		<% String caller_ID = null;
		String sub = null;
		String songName = null;
		String categoryName = null;
		String callerID = null;
		String user = null;
		String groupName = null;
		String file = null;
		String timeString = null;
		String chargingModel = null;
		String subscriptionType = null;
		String regexType = null;
		String dayAndDate = null;
		String selInterval = null;
		Subscriber subscriber;
		int categoryID = -1;
		int fromTime,toTime,status;
		%>
		<%
		subscriber = null;
		String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
		try
			{
		long maxfilesize = 20000000;
		SmartUpload mySmartUpload=null;
		mySmartUpload=new SmartUpload();
		mySmartUpload.initialize(pageContext); 
		mySmartUpload.setTotalMaxFileSize(maxfilesize);
		mySmartUpload.upload();

		sub = mySmartUpload.getRequest().getParameter("SUB_ID");
		songName = (String)session.getAttribute("song");
		String giftToId=mySmartUpload.getRequest().getParameter("SUBSCRIBER_ID");		
		if (mySmartUpload.getRequest().getParameter("ADD_OR_GIFT")!= null && mySmartUpload.getRequest().getParameter("ADD_OR_GIFT").equals("GIFT")) {

		System.out.println("i mm in gift");
		if(sub!= null && giftToId!= null )
		{
			int clipId=0;
			System.out.println("i m above getcliprbt");
			ClipMinimal dummy=rbtLogin.getClipRBT(songName);
			System.out.println("i m above get clip ");
			clipId=dummy.getClipId();	
			System.out.println("i m above can be gifted");			
			String canBeGifted= rbtLogin.canBeGifted(sub,giftToId,clipId+"");
			System.out.println("the value of can be gifted " +canBeGifted);
			if(canBeGifted!=null && canBeGifted.substring(5,6).equals("S")){
			
			System.out.println("inside if not null is " + giftToId);
			rbtLogin.insertGiftRecord(sub,giftToId,clipId,"CC");
			strStatus="SUCCESS";
			session.setAttribute("updated",strStatus);
			%>
				 
			
			<%}else{
				strStatus="FAILURE";
				strReason=canBeGifted;
				
				if (canBeGifted.equals("GIFT_FAILURE_GIFTER_NOT_ACT"))
				strReason="Gift failed because gifter is not active";
				
				if (canBeGifted.equals("GIFT_FAILURE_ACT_GIFT_PENDING"))
				strReason="Gift failed because giftee has activation pending";
				
				if (canBeGifted.equals("GIFT_FAILURE_GIFT_IN_USE"))
				strReason="Gift failed because giftee is already using a gift";

				if (canBeGifted.equals("GIFT_FAILURE_ACT_PENDING"))
				strReason="Gift failed because giftee is pending for activation";

				if (canBeGifted.equals("GIFT_FAILURE_DEACT_PENDING"))
				strReason="Gift failed because giftee is pending for de-activation";

				if (canBeGifted.equals("GIFT_FAILURE_TECHNICAL_DIFFICULTIES"))
				strReason="Gift failed due to technical difficulties. Please try again later";

				if (canBeGifted.equals("GIFT_FAILURE_SONG_PRESENT_IN_DOWNLOADS"))
				strReason="Gift failed because the giftee already has this song.";

				if (canBeGifted.equals("GIFT_FAILURE_SONG_GIFT_PENDING"))
				strReason="Gift failed because the giftee has this gift pending .";

				if (canBeGifted.equals("GIFT_FAILURE_SONG_IN_USE"))
				strReason="Gift failed because the giftee is already using this song.";

				if (canBeGifted.equals("GIFT_FAILURE_GIFTEE_INVALID"))
				strReason="Gift failed because the giftee is invalid.";
			
				session.setAttribute("updated",strStatus);
				session.setAttribute("reason",strReason);
			%>
				
			<%}
			
			}
		else
		{
			response.sendRedirect("rbt_index.jsp");
		}		
		
		
		}
		
		else{
		
		categoryName = (String)session.getAttribute("category");
		user = mySmartUpload.getRequest().getParameter("user");
		callerID = mySmartUpload.getRequest().getParameter("callerID");
		timeString = mySmartUpload.getRequest().getParameter("Time");
		dayAndDate = mySmartUpload.getRequest().getParameter("dayAndDate");
		chargingModel = mySmartUpload.getRequest().getParameter("ChargingModel");
		if(chargingModel!=null)
			chargingModel=chargingModel.toUpperCase();
		subscriptionType = mySmartUpload.getRequest().getParameter("SubscriptionType");
		if(subscriptionType!=null)
			subscriptionType=subscriptionType.toUpperCase();
		if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_TOD_IN_GUI", "FALSE"))
			regexType = "ESIA " + chargingModel + " " + subscriptionType;
		bBlackoutSMS = false;
		if(mySmartUpload.getRequest().getParameter("blackout_sms") != null)
			bBlackoutSMS = true;
		if(bBlackoutSMS)
			actInfo = "BULK:" + actInfo;
		if(mySmartUpload.getRequest().getParameter("ADDTOLOOP")  != null) 
			addToLoop = true; 
		else 
			addToLoop = false; 

		fromTime=0;
		toTime=23;
		status=1;
		if(timeString != null && timeString.equalsIgnoreCase("TimeOfTheDay"))
		{
			String time1=mySmartUpload.getRequest().getParameter("fromTime");
			String time2=mySmartUpload.getRequest().getParameter("toTime");
			fromTime=Integer.parseInt(time1);
			toTime=Integer.parseInt(time2)-1;
			if (!(fromTime==0 && toTime==23))
				status=80;
		}
		if(user != null && user.equals("groupName"))
		{
			groupName = mySmartUpload.getRequest().getParameter("group").trim();
			Groups group = rbtLogin.getActiveGroupByGroupName(groupName, sub);
			if(group == null)
			{
				strStatus = "FAILURE";
				strReason = "Group "+groupName+" does not exist for subscriber";
				session.setAttribute("updated",strStatus);
				session.setAttribute("reason",strReason); %>
				<jsp:forward page="rbt_subs_selections.jsp" />
				
			<%}else if(group.status() != null && (group.status().equals("D") || group.status().equals("X")))
			{
				strStatus = "FAILURE";
				strReason = "Group "+groupName+" is deactivated for subscriber";
				session.setAttribute("updated",strStatus);
				session.setAttribute("reason",strReason);%>
				<jsp:forward page="rbt_subs_selections.jsp" />
			<%}
			else
			{
				callerID = "G"+group.groupID();
			}
			
		}
		
		if(dayAndDate !=null && dayAndDate.equalsIgnoreCase("DayOfTheWeek"))
		{
			selInterval = mySmartUpload.getRequest().getParameter("dayOfWeek");
		
		}
		
		else if(dayAndDate !=null && dayAndDate.equalsIgnoreCase("FutureDate"))
		{
			String calendar = mySmartUpload.getRequest().getParameter("playDate");
			String cal = calendar.replaceAll("/","");
			selInterval = "Y" + cal;
		}
		else
		{
			selInterval = null;
		}
		
		session.removeAttribute("song");
		session.removeAttribute("category");

		if(mySmartUpload.getFiles().getCount() > 0)
		{
			if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
			{
				//file = mySmartUpload.getFiles().getFile(0).getFileName();
				//mySmartUpload.save(pathDir);
				file = "Subscriber-Selection-" + System.currentTimeMillis() + ".txt";
				mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
			}
		}

		
		Date endDate = null;
		try
		{
			categoryID = Integer.parseInt(categoryName);
		}
		catch(Exception e)
		{
		
		}
		System.out.println("sub is " + sub);
	if( sub != null && (!sub.equals(""))) {
		subscriber = rbtLogin.getSubscriber(sub);
		String strValidSub = rbtLogin.isValidSub(sub, subscriber);
		if(!strValidSub.equals("success")){
			strStatus = "FAILURE";
			if (strValidSub.equals("failure"))
				strReason = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(sub);
			else if (strValidSub.equals("blacklisted"))    
				strReason = "No. "+sub + " is blacklisted.";
			else if (strValidSub.equals("suspended"))    
				strReason = "No. "+sub + " is suspended.";
		}
		else{
			if(subscriber == null)
			{
				strStatus = "FAILURE";
				strReason = "Subscriber does not exists: " + sub;
			}
			else {
				if(rbtLogin.isSubActive(subscriber)) {					
					Categories category = rbtLogin.getCategory(categoryID, rbtLogin.getCircleID(sub), subscriber.prepaidYes() ? 'y' : 'n');
					if(category != null)
						strStatus="SUCCESS";
					else {
						strStatus="FAILURE";
						strReason ="Invalid Circle Subscriber";
					}
				}
				else{
					strStatus ="FAILURE";
					strReason ="Customer Deactive";
				}
			}
		}
	}

 if(strStatus != null && strStatus.equalsIgnoreCase("FAILURE"))
	{
		session.setAttribute("updated",strStatus);
		session.setAttribute("reason",strReason); %>
		<jsp:forward page="rbt_subs_selections.jsp" />
<%	}
		
		if(user.equalsIgnoreCase("All"))
		{
			caller_ID = null;
		}
		else
		{
			caller_ID = callerID;
		}
		if(sub != null)
		{
			System.out.println("Selection Added for a subscriber by "+strUser);
			subscriber = rbtLogin.getSubscriber(sub);
			String subYes = null;
			boolean OptIn = false;
			if(subscriber != null){
				subYes = subscriber.subYes();
				if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                    OptIn = true; 
			}
			String res=null;
			System.out.println("sub" + sub + " caller_ID " +  caller_ID + " subscriber.prepaidYes() " + subscriber.prepaidYes()+  " false " +  " categoryID " + categoryID +  "songName " + songName +  "null" + " status " +  status +  "0" +  "CC" + " actInfo " + actInfo +  "fromTime "+ fromTime +  " toTime " + toTime +  "null" +  " GUI " +  " regexType " + regexType + " subYes " +   subYes +  " subscriber.maxSelections() " + subscriber.maxSelections() + "  subscriber.subscriptionClass() " +  " subscriber.subscriptionClass() " +  subscriber.subscriptionClass() + " OptIn " +  OptIn + " subscriber " +  subscriber + " addToLoop " +  addToLoop + " selInterval "+ selInterval);
			if(fromTime<=toTime && subscriber != null)
				res = rbtLogin.addSelections(sub, caller_ID, subscriber.prepaidYes(), false, categoryID, songName, null, status, 0, "CC", actInfo, fromTime, toTime, null, "GUI", regexType, subYes, subscriber.maxSelections(), subscriber.subscriptionClass(),OptIn, subscriber,addToLoop,selInterval); 
			if(res == null)
				session.setAttribute("updated","SUCCESS");
			else if(res.equalsIgnoreCase("corp"))
			{
				strStatus ="FAILURE";
				strReason ="Song selection for all callers not allowed for corporate subscribers";
				session.setAttribute("updated",strStatus);
				session.setAttribute("reason",strReason);
			}
						
		}
		else
		{
			if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) 
			{
			session.setAttribute("updated","FAILURE");
			session.setAttribute("reason","File size is Zero");%>
			<jsp:forward page="rbt_subs_selections.jsp" />
		<%	}	
			else{
			System.out.println("Selection Added For a File of Subs By User " + strUser);
			
			session.setAttribute("updated","SUCCESS");

			FileReader fr = new FileReader(pathDir+java.io.File.separator+file);
		            BufferedReader br = new BufferedReader(fr);
            
			String line = br.readLine();
            
            
			while(line != null)
			{
				line = line.trim();
				StringTokenizer token = new StringTokenizer(line, ",");
				sub = null;
				String caller = null;
				if(token.hasMoreTokens())
				sub = token.nextToken();
				if(token.hasMoreTokens())
				caller = token.nextToken();
				if(caller != null && caller.length() <7)
				{
				caller = null;
				}
				try
				{
				if(caller != null)
					Long.parseLong(caller);
				}
				catch(Exception e)
				{
				caller = null;
				}
	
				subscriber = rbtLogin.getSubscriber(sub);
				String subYes = null;
				boolean OptIn = false;
				if(subscriber != null){
					subYes = subscriber.subYes();
					if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                        OptIn = true; 

				}
				if(subscriber != null && rbtLogin.isSubActive(subscriber))
				{
					rbtLogin.addSelections(sub, caller, subscriber.prepaidYes(), false, categoryID, songName, null, 1, 0, "CC", actInfo, 0, 23, null, "GUI", regexType, subYes, subscriber.maxSelections(), subscriber.subscriptionClass(),OptIn, subscriber,selInterval);
				}
				line = br.readLine();
			}

			fr.close();
			br.close();
			}

		}
			}
		}
		catch(Exception e)
		{
				System.out.println("Exception in rbt_update_sub_select "+e.getMessage());
				session.setAttribute("updated","FAILURE");
				session.setAttribute("reason","Internal Error");
		}
		finally
		{
			try
			{
				java.io.File temp = new java.io.File(pathDir + java.io.File.separator + file);
				if(temp.exists())
				{
					temp.delete();
				}
			}
			catch(Exception e)
			{
				System.out.println("Could not delete the file " + file + " from " + pathDir);
			}
		}
		%>
			<jsp:forward page="rbt_subs_selections.jsp" />

<%
}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>