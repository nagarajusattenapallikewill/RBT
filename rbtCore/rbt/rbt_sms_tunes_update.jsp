<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.*,java.io.*,com.onmobile.apps.ringbacktones.provisioning.Processor,com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber,com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants"%>
<%@ page language="java" import="com.jspsmart.upload.*" %>
<%@ include file = "validate.jsp" %>
<%
String strUser = null;

String actInfo = null;
%>
<%
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser; 
if (validateUser(request, session,  "rbt_sms_promo_tunes.jsp", response)) { %>

		<%
			String strSub = null;
			String song = null;
			String file = null;
			String actBy,strClassType,strSubClass;
			String subscriptionType = null;
			boolean bPrepaid, bIgnoreAct, bFreeAct, bFreeSel, bReact, bFreeSMAct, bFreeSMSel, bBlackoutSMS;
			int trial_period;
		%>
	

		<%
		String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
		try
		{
			long maxfilesize = 20000000;
			SmartUpload mySmartUpload=null;
			mySmartUpload=new SmartUpload();
			mySmartUpload.initialize(pageContext); 
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();
			
			strSub = mySmartUpload.getRequest().getParameter("SUB_ID");
			song = mySmartUpload.getRequest().getParameter("SongName");
			actBy = mySmartUpload.getRequest().getParameter("comboActBy");

			bPrepaid = false;
			bIgnoreAct = false;
			bFreeAct = false;
			bFreeSel = false;
			bReact = false;
			bFreeSMAct = false;
			bFreeSMSel = false;
			strClassType = "DEFAULT";
			strSubClass = "DEFAULT";
			trial_period = 0;

			if(mySmartUpload.getRequest().getParameter("IGNORE_ACTIVE") != null)
				bIgnoreAct = true;

			if(mySmartUpload.getRequest().getParameter("FREE_ACT") != null)
				bFreeAct = true;
			
			if(mySmartUpload.getRequest().getParameter("FREE_SEL") != null)
				bFreeSel = true;

			if(mySmartUpload.getRequest().getParameter("REACT") != null)
				bReact = true;

			String classTypes = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "FREE_ACT_SUB_SEL_CLASS", null);
			String sub= null,sel = null;
			StringTokenizer stk = null;
			if(classTypes != null)
			{
				stk = new StringTokenizer(classTypes, ",");
				sub = stk.nextToken();
				sel = stk.nextToken();
			}
			if(mySmartUpload.getRequest().getParameter("FREEACT") != null)
			{
				if(classTypes != null)
					strSubClass = sub;
				bFreeSMAct = true;
			}

			if(mySmartUpload.getRequest().getParameter("FREESEL") != null)
			{
				if(classTypes != null)
					strClassType = sel;
				bFreeSMSel = true;
			}

			if(mySmartUpload.getRequest().getParameter("CHARGED")!= null &&  mySmartUpload.getRequest().getParameter("CHARGED").equals("UNCHARGED"))
			{
				if(rbtLogin.freeSelectionClass() != null)
					strClassType = rbtLogin.freeSelectionClass();
			}
			
			if(mySmartUpload.getRequest().getParameter("TIME_PERIOD") != null)
				trial_period = Integer.parseInt(mySmartUpload.getRequest().getParameter("TIME_PERIOD"));

			if(mySmartUpload.getRequest().getParameter("SubscriptionType") != null)
				subscriptionType = mySmartUpload.getRequest().getParameter("SubscriptionType");

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
					file = "SMS-Selection-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
				}
			}
			

			if(strSub != null){

				System.out.println("Promotion Done for a Subscriber by User "+strUser);
				Subscriber subscriber = Processor.getSubscriber(strSub);
				strSub = subscriber.getSubscriberID();
				String circleID = 	subscriber.getCircleID();

				if(subscriber.isValidPrefix() && subscriber.isCanAllow() && !subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
				{
					String ret = rbtLogin.addSMSSelection(strSub, song, bPrepaid, actBy, bIgnoreAct, bFreeAct, bFreeSel, bReact, trial_period, actInfo, strClassType, strSubClass, subscriptionType, circleID);

					if(ret == null )
						session.setAttribute("flag","true");
					else if (ret.equalsIgnoreCase("corp"))
						session.setAttribute("flag","Song selection for all callers not allowed for corporate subscribers");
					else
						session.setAttribute("flag",ret);
				}
				else if (!subscriber.isCanAllow())
					session.setAttribute("flag","No. "+strSub + " is blacklisted.");
				else if (subscriber.getStatus().equalsIgnoreCase(WebServiceConstants.SUSPENDED))
					session.setAttribute("flag","No. "+strSub + " is suspended.");
				else if (!subscriber.isValidPrefix())
					session.setAttribute("flag","Invalid Prefix-"+RBTSubUnsub.init().getSubscriberPrefix(strSub));

				%>
					<jsp:forward page="rbt_sms_promo_tunes.jsp" />
			<%}
			else
			{
				if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) {
				session.setAttribute("flag","Subscriber file size is zero");%>
					<jsp:forward page="rbt_sms_promo_tunes.jsp" />
				<%}
				else{
					System.out.println("Promotion Done for a File of Subs by User "+strUser);
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
						   rbtLogin.addSMSSelection(line, song, bPrepaid, actBy, bIgnoreAct, bFreeAct, bFreeSel, bReact, trial_period, actInfo, strClassType, strSubClass, subscriptionType,circleID);
					   line = br.readLine();
					}

					br.close();
					fr.close();
				}
				session.setAttribute("flag","true");
			}
	}
	catch(Exception e)
	{
		System.out.println("Exception in rbt_sms_tunes_update "+e.getMessage());
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
					<jsp:forward page="rbt_sms_promo_tunes.jsp" />
<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>