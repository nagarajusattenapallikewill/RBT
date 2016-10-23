<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<%@ page import = "java.util.*,java.io.*"%>

<%@ include file = "validate.jsp" %>
<%
String strUser=null; String actInfo = null;
String strResult=null; String file=null ;String deact_by=null ;String strSubID=null ; String strButton = null;String  corpSplFeatureStr=null;
boolean bPrepaid=false;boolean bSendToHLR=false;boolean bBlackoutSMS=false;boolean corpSplFeatureBln=false;
String X_ONMOBILE_STATUS=null;
String X_ONMOBILE_FAILURE_REASON=null;
String FAILURE=null;
String SUCCESS=null;
%>

<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser; 
if (validateUser(request, session,  "rbt_subscribe_manager_deact.jsp", response)) {
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
			//added by eswar 
	        deact_by = mySmartUpload.getRequest().getParameter("comboDeactBy"); 
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
			
			if(mySmartUpload.getRequest().getParameter("corpSplFeature") == null) 
                corpSplFeatureBln = true; 
	        else 
                corpSplFeatureBln = false; 

			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
				{
					//file = mySmartUpload.getFiles().getFile(0).getFileName();
					//mySmartUpload.save(pathDir);

					file = "Deactivation-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
				}
			}

		if(strSubID != null){
			System.out.println("Deactivation For a Subscriber Done By User " + strUser);

			 if(!rbtLogin.isValidSub(strSubID).equals("success") && !rbtLogin.isValidSub(strSubID).equals("suspended")){
				X_ONMOBILE_STATUS = FAILURE;
				if(rbtLogin.isValidSub(strSubID).equals("failure"))
					X_ONMOBILE_FAILURE_REASON = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(strSubID);
				else if (rbtLogin.isValidSub(strSubID).equals("blacklisted"))
	                X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is blacklisted.";
				//else if (rbtLogin.isValidSub(strSubID).equals("suspended"))
	              //  X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is suspended.";
			}
			else{
					//updated the following statement by eswar 
					strResult = rbtLogin.deactSubscriber(strSubID, deact_by, bSendToHLR, actInfo, corpSplFeatureBln); 
					if(!(strResult.equalsIgnoreCase(SUCCESS))){
						X_ONMOBILE_STATUS = FAILURE;
						X_ONMOBILE_FAILURE_REASON = strResult+"--"+strSubID;
					}
				else
					X_ONMOBILE_STATUS = SUCCESS;
			}
		}else {
					if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) {
				session.setAttribute("flag","Subscriber file size is zero");%>
					<jsp:forward page ="rbt_subscribe_manager_deact.jsp" />
				<%}
				else{
					System.out.println("Deactivation For a File of Subs Done By User " + strUser);

					FileReader fr = new FileReader(pathDir+java.io.File.separator+file);
			        BufferedReader br = new BufferedReader(fr);
            
				    String line = br.readLine();
            
					while(line != null)
					{
		               line = line.trim();
		               if(rbtLogin.isValidSub(line).equals("success") || rbtLogin.isValidSub(line).equals("suspended"))				
		            	   strResult = rbtLogin.deactSubscriber(line, deact_by, bSendToHLR, actInfo, corpSplFeatureBln);
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
	catch(Exception e)
	{
		System.out.println("Exception in rbt_CC_deact "+e.getMessage());
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
	<jsp:forward page ="rbt_subscribe_manager_deact.jsp" />

<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>
