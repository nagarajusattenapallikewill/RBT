<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper" %>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<%@ page import = "java.util.*,java.io.*"%>

<%@ include file = "validate.jsp" %>
<%
String strUser = null;
String strResult = null;
String file = null;
String strSubID = null ;
boolean disablePrompt = true;
String X_ONMOBILE_STATUS;
String X_ONMOBILE_FAILURE_REASON;
String FAILURE;
String SUCCESS;
%>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
RBTMOHelper rbtMO = RBTMOHelper.init();
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if (validateUser(request, session,  "rbt_intro_prompt.jsp", response)) {
			
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
			
			if(mySmartUpload.getRequest().getParameter("updateType") != null && mySmartUpload.getRequest().getParameter("updateType").equalsIgnoreCase("Enable"))
				disablePrompt = false;
			else
				disablePrompt = true;
			System.out.println("Intro Prompt Update Type is: "+mySmartUpload.getRequest().getParameter("updateType"));
			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
				{
					file = "IntroPrompt-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
					System.out.println("Intro Prompt File is: "+pathDir + java.io.File.separator + file);
				}
			}

	if(strSubID != null){
		System.out.println("IntroPrompt Update Done For A Subscriber By User " + strUser);
		String strValidSub = rbtLogin.isValidSub(strSubID);
		if(strValidSub.equals("blacklisted") || strValidSub.equals("suspended") || strValidSub.equals("success")) { 
				if(disablePrompt)
					strResult = rbtLogin.disablePressStarIntro(strSubID);
				else
					strResult = rbtLogin.enablePressStarIntro(strSubID);
				if(!(strResult.equalsIgnoreCase(SUCCESS))){
					X_ONMOBILE_STATUS = FAILURE;
					X_ONMOBILE_FAILURE_REASON = strResult+"--"+strSubID;
				}
			else
				X_ONMOBILE_STATUS = SUCCESS;
		}
		else{ 
			   X_ONMOBILE_STATUS = FAILURE; 
			   if(strValidSub.equals("failure")) 
				   X_ONMOBILE_FAILURE_REASON = "Invalid prefix "+RBTSubUnsub.init().getSubscriberPrefix(strSubID); 
			   else if (strValidSub.equals("blacklisted")) 
			   X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is blacklisted."; 
				   else if (strValidSub.equals("suspended")) 
			   X_ONMOBILE_FAILURE_REASON = "No. " + strSubID + " is suspended."; 
	    } 
	}else {
				if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) 
				{
					session.setAttribute("flag","Subscriber file size is zero");%>
						<jsp:forward page ="rbt_intro_prompt.jsp" />
				<%}
				else{
					System.out.println("Intro Prompt Update Done For a File of Subs By User " + strUser);

					FileReader fr = new FileReader(pathDir+java.io.File.separator+file);
			        BufferedReader br = new BufferedReader(fr);
            
				    String line = br.readLine();
            
					while(line != null)
					{
		               line = line.trim();
		               if(rbtLogin.isValidSub(line).equals("success"))
						{
							if(disablePrompt)
								strResult = rbtLogin.disablePressStarIntro(line);
							else
								strResult = rbtLogin.enablePressStarIntro(line);
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
		System.out.println("Exception in rbt_intro_prompt2.jsp "+rbtMO.getStackTrace(e));
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
	<jsp:forward page ="rbt_intro_prompt.jsp" />

<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>
