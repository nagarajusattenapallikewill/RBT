<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
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
		String file = null;
		String timeString = null;
		String chargingModel = null;
		String subscriptionType = null;
		String regexType = null;
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
		songName = mySmartUpload.getRequest().getParameter("songName");
		user = mySmartUpload.getRequest().getParameter("user");
		callerID = mySmartUpload.getRequest().getParameter("callerID");
		timeString = mySmartUpload.getRequest().getParameter("Time");
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
		if(mySmartUpload.getRequest().getParameter("ADDTOLOOP")	 != null)
			addToLoop = true;	


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

	if( sub != null && (!sub.equals(""))){
		String strValidSub = rbtLogin.isValidSub(sub);
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
  subscriber = rbtLogin.getSubscriber(sub);
  if(subscriber == null)
  {
   strStatus = "FAILURE";
   strReason = "Subscriber does not exists: " + sub;
  }
  
  else {
   if(rbtLogin.isSubActive(subscriber))
    strStatus="SUCCESS";
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
		session.setAttribute("reason",strReason);%>
		<jsp:forward page="rbt_subs_selections.jsp" />
<%	}
		try
		{
			categoryID = Integer.parseInt(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_PROMOTION_CATEGORY_ID", null));
		}
		catch(Exception e)
		{
				categoryID = 7;
		}
		
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
			if(fromTime<=toTime && subscriber != null)
				res = rbtLogin.addSelections(sub, caller_ID, subscriber.prepaidYes(), false, categoryID, songName, null, status, 0, "CC", actInfo, fromTime, toTime, null, "GUI", regexType, subYes, subscriber.maxSelections(), subscriber.subscriptionClass(),OptIn, subscriber,addToLoop,null);

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
					rbtLogin.addSelections(sub, caller, subscriber.prepaidYes(), false, categoryID, songName, null, 1, 0, "CC", actInfo, 0, 23, null, "GUI", regexType, subYes, subscriber.maxSelections(), subscriber.subscriptionClass(),OptIn, subscriber,null);
				}
				line = br.readLine();
			}

			fr.close();
			br.close();
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