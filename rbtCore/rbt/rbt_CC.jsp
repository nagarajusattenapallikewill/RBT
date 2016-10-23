<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper" %>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<%@ page import = "java.util.*,java.io.*,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber,com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants"%>

<%@ include file = "validate.jsp" %>
<%
String strUser=null;String actInfo=null;
String strResult=null;String file=null;String act_by=null;String strSubID=null;String strButton=null;
boolean bPrepaid=false;boolean bSendToHLR=false;boolean bBlackoutSMS=false;
String X_ONMOBILE_STATUS=null;
String X_ONMOBILE_FAILURE_REASON=null;
String FAILURE=null;
String SUCCESS=null;
String subClass=null;
%>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
RBTMOHelper rbtMO = RBTMOHelper.init();
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser; 
if (validateUser(request, session,  "rbt_subscribe_manager.jsp", response)) {
			
			String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
			try
			{
			X_ONMOBILE_STATUS = null;
			X_ONMOBILE_FAILURE_REASON = null;
			FAILURE = "FAILURE";
			SUCCESS = "SUCCESS";
	
			long maxfilesize = 20000000;
			SmartUpload mySmartUpload=null;
			mySmartUpload=new SmartUpload();
			mySmartUpload.initialize(pageContext); 
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();
			
			strSubID = mySmartUpload.getRequest().getParameter("SUB_ID");
			act_by = mySmartUpload.getRequest().getParameter("comboActBy");
			subClass = mySmartUpload.getRequest().getParameter("upgrade");
			if(subClass == null || subClass.equals(""))
				subClass = "DEFAULT";
			bPrepaid = false;
			
			if(mySmartUpload.getRequest().getParameter("comboUserProfAccLvl") != null && mySmartUpload.getRequest().getParameter("comboUserProfAccLvl").equalsIgnoreCase("Prepaid"))
				bPrepaid = true;
			//prepaid-postpaid change by gautam
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "USE_DEFAULT_TYPE_GUI_PROCESSING", "TRUE"))
			{
				if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
					bPrepaid = true;
				if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Postpaid"))
					bPrepaid=false;
			}

			bSendToHLR = true;
			
			if(mySmartUpload.getRequest().getParameter("SEND_HLR") == null)
				bSendToHLR = false;

			if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
				bSendToHLR = true;
			
			bBlackoutSMS = false;
			if(mySmartUpload.getRequest().getParameter("blackout_sms") != null)
				bBlackoutSMS = true;
			if(bBlackoutSMS)
				actInfo = "BULK:" + actInfo;
			
			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
				{
					//file = mySmartUpload.getFiles().getFile(0).getFileName();
					//mySmartUpload.save(pathDir);

					file = "Activation-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
				}
			}

	if(strSubID != null){
		System.out.println("Activation Done For A Subscriber By User " + strUser);
		Subscriber subscriber = Processor.getSubscriber(strSubID);
		strSubID = subscriber.getSubscriberID();
		String circleID = 	subscriber.getCircleID();
		
		if(!subscriber.isCanAllow())
		{
			X_ONMOBILE_STATUS = FAILURE;
			X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is blacklisted.";
		}
		else if(subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
		{
			X_ONMOBILE_STATUS = FAILURE;
			X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is suspended.";
		}
		else if(!subscriber.isValidPrefix())
		{
			X_ONMOBILE_STATUS = FAILURE;
			X_ONMOBILE_FAILURE_REASON = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(strSubID);
		}
		else{
				strResult = rbtLogin.actSubscriber(strSubID, act_by, bPrepaid, bSendToHLR, actInfo, subClass, circleID);
				if(!(strResult.equalsIgnoreCase(SUCCESS))){
					X_ONMOBILE_STATUS = FAILURE;
					X_ONMOBILE_FAILURE_REASON = strResult+"--"+strSubID;
				}
			else
				X_ONMOBILE_STATUS = SUCCESS;
		}
	}else {
				if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) 
				{
					session.setAttribute("flag","Subscriber file size is zero");%>
						<jsp:forward page ="rbt_subscribe_manager.jsp" />
				<%}
				else{
					System.out.println("Activation Done For a File of Subs By User " + strUser);

					FileReader fr = new FileReader(pathDir+java.io.File.separator+file);
			        BufferedReader br = new BufferedReader(fr);
            
				    String line = br.readLine();
            
					while(line != null)
					{
		               line = line.trim();
		               Subscriber subscriber = Processor.getSubscriber(line);
					   line = subscriber.getSubscriberID();
					   String circleID = 	subscriber.getCircleID();
			    	   if(subscriber.isValidPrefix() && subscriber.isCanAllow() && !subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
						{
							strResult = rbtLogin.actSubscriber(line, act_by, bPrepaid, bSendToHLR, actInfo, subClass,circleID);
						}
					   line = br.readLine();
					}

					br.close();
					fr.close();
				}

				X_ONMOBILE_STATUS = SUCCESS;
	}
session.setAttribute("Status", X_ONMOBILE_STATUS);
session.setAttribute("Reason", X_ONMOBILE_FAILURE_REASON);
	}
	catch(Throwable e)
	{
		System.out.println("Exception in rbt_CC "+rbtMO.getStackTrace(e));
		session.setAttribute("flag","Internal Error");
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
	<jsp:forward page ="rbt_subscribe_manager.jsp" />

<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>
