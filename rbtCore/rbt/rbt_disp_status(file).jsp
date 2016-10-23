		<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<head>
		<%@ include file = "javascripts/RBTValidate.js" %>
		</head>
		<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
		<%@ page import = "com.onmobile.apps.ringbacktones.content.Subscriber"%>
		<%@ page import = "com.onmobile.apps.ringbacktones.content.database.RBTDBManager"%>
		<%@ page import = "java.util.StringTokenizer,java.io.FileOutputStream,java.io.FileInputStream,java.io.FileReader,java.io.LineNumberReader, java.util.*,java.io.File,java.text.*"%>
		<%@ page language="java" import="com.jspsmart.upload.*" %>
		<%@ include file = "validate.jsp" %>
		<%
	
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		String [] subscriberStatus=null;
		String [] subscriberDeact=null;
		String strUser= null;
		subscriberStatus = null;
		subscriberDeact = null;

		strUser  = (String)(session.getAttribute("UserId"));
		if (validateUser(request, session, "rbt_subscriber_status.jsp", response)) {
		String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
		String strFile= null; String strResult= null; String strValue=  null;
		try
		{
			%>

		<%  
		String X_ONMOBILE_STATUS = null;
		String X_ONMOBILE_FAILURE_REASON = null;
		String FAILURE= "FAILURE";
		String SUCCESS= "SUCCESS";
		String strDate = null;
		String strReason= null;
		String strActby= null;
		String prepaid= null ;
		String sub= null; String str=null; String callerID= null; String category= null; String clip = null; String promoID= null; String setDate= null ; String maxSel= null; String classType= null; String status= null;
		String strAccess = null;
			FAILURE = "FAILURE";
			SUCCESS = "SUCCESS";
			strDate = null;
			strReason = null;
			strActby = null;
			prepaid = null;
	
			long maxfilesize = 20000000;
			SmartUpload mySmartUpload=null;
			mySmartUpload=new SmartUpload();
			mySmartUpload.initialize(pageContext); 
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();
			sub = mySmartUpload.getRequest().getParameter("SUB_ID");
			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
				{
					strFile = mySmartUpload.getFiles().getFile(0).getFileName();
					//mySmartUpload.save(pathDir);
					strFile = "View-Selection-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + File.separator + strFile);
				}
			}
			if(sub != null){
				
				session.setAttribute("Subscriber", sub);%>
				<table align="center" cols=2 border="0" cellpadding=6 width="100%" style="VERTICAL-ALIGN: top">
					<tr>
						<td align="center"> <jsp:include page="rbt_disp_status.jsp" />
					</tr>
				</table>
				
			<%}else{

			if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) {
				session.setAttribute("SubStatus","FAILURE");
				session.setAttribute("SubReason","subscriber file size is zero");%>
				<jsp:forward page="rbt_subscriber_status.jsp" />
			<%}
			System.out.println("Status Checked For a File By User " + strUser);
			StringBuffer sb = new StringBuffer();
			String[] subs = null;
			SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
			RBTSubUnsub rbtStatus = RBTSubUnsub.init();
			java.io.File file = new java.io.File(pathDir+java.io.File.separator+strFile);
			if(file == null){
				X_ONMOBILE_STATUS = FAILURE;
				X_ONMOBILE_FAILURE_REASON = "Invalid File";
				session.setAttribute("SubStatus", X_ONMOBILE_STATUS);
				session.setAttribute("SubReason", X_ONMOBILE_FAILURE_REASON);%>
				<jsp:forward page ="rbt_subscriber_status.jsp" />
					<%
			}
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddhhmmss");
				File reportFile = new File(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null)+File.separator+"Subscriber_Status_Report"+sdf.format(cal.getTime())+".csv");
				FileOutputStream fout = null;
				try {
					reportFile.createNewFile();
					fout = new FileOutputStream(reportFile);
				} catch (Exception e) {
					
				}

				List subList = new ArrayList();
				LineNumberReader fin = new LineNumberReader(new FileReader(file));
				while ((str = fin.readLine()) != null)
					subList.add(str.trim());
				
				file.delete();
				subs = (String[])subList.toArray(new String[0]);
				sb.append("Subscriber,Status,Subscriber Type,Activated By,Activated On,Deactivated By,Deactivated On,Last Access Date,CallerID,Category,Clip Name,Promo ID,ClassType,Set Time,End Time,Active\n");

				for(int i=0;i<subs.length;i++){
				Subscriber subscriber = rbtDBManager.getSubscriber(subs[i]);

				if(subscriber == null){
					sb.append(subs[i]+","+"Does not exist\n");
					continue;
				}
				else{
					if(subscriber.prepaidYes())
						prepaid = "Prepaid";
					else
						prepaid = "Postpaid";
					
					if(subscriber.accessDate() !=null)
						strAccess = df.format(subscriber.accessDate());
					else
						strAccess =" ";
					if(subscriber.deactivatedBy() != null && !subscriber.deactivatedBy().equalsIgnoreCase("AUX") &&  !subscriber.deactivatedBy().equalsIgnoreCase("NEFX"))
						sb.append(subs[i]+",Deactive,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+","+subscriber.deactivatedBy()+","+df.format(subscriber.endDate())+","+strAccess+"\n");
					else
					  {
						if(/*RBTCommonConfig.getInstance().useSubscriptionManager()*/true)
			                 {
                    			if(subscriber.subYes().equalsIgnoreCase("G")) 
                                { 
                                           if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE")) 
                                                                      prepaid = "Unknown"; 
                                                                                   sb.append(subs[i]+",Activation Grace,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+","); 
    
                                      } 
                                      else if(subscriber.subYes().equalsIgnoreCase("A") || (subscriber.activationDate() == null || subscriber.nextChargingDate() == null))
				                   {
                                        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE"))
					                           prepaid = "Unknown";
										sb.append(subs[i]+",Activation Pending,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+",");

                                   }
								   else if(subscriber.subYes().equalsIgnoreCase("Z") || subscriber.subYes().equalsIgnoreCase("z")) 
									   sb.append(subs[i]+",Suspended,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+","); 
								   else 
									sb.append(subs[i]+",Active,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+","); 

			                 }
						else
						 sb.append(subs[i]+",Active,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+",");
				      }
				}

				session.removeAttribute("SubStatus");
				session.removeAttribute("SubReason");
	
				if(subscriber.deactivatedBy() == null || subscriber.deactivatedBy().equalsIgnoreCase("AUX") || subscriber.deactivatedBy().equalsIgnoreCase("NEFX"))
				{
					StringTokenizer st_stat;
					subscriberStatus = rbtStatus.getSubscriberSelections(subs[i], subscriber);
					if(subscriberStatus != null){
						st_stat = new StringTokenizer(subscriberStatus[0], ",");
						callerID = st_stat.nextToken();
						category = st_stat.nextToken();
						clip = st_stat.nextToken();
						promoID = st_stat.nextToken();
						classType = st_stat.nextToken();
						setDate = st_stat.nextToken();
						for(int m=0; m<7; m++)
						{
							 if(st_stat.hasMoreTokens())
								  st_stat.nextToken();
						}
				        if(st_stat.hasMoreTokens())
							status = st_stat.nextToken();
                        sb.append(callerID+","+category+","+clip+","+promoID+","+classType+","+setDate+",,"+status+"\n");
						for(int j = 1; j < subscriberStatus.length; j++){
							st_stat = new StringTokenizer(subscriberStatus[j], ",");
							callerID = st_stat.nextToken();
							category = st_stat.nextToken();
							clip = st_stat.nextToken();
							promoID = st_stat.nextToken();
							classType = st_stat.nextToken();
							setDate = st_stat.nextToken();
							for(int n=0; n<7; n++)
							     {
									 if(st_stat.hasMoreTokens())
								          st_stat.nextToken();
							     }
                            	 if(st_stat.hasMoreTokens())
								status = st_stat.nextToken();
							sb.append(",,,,,,,,"+callerID+","+category+","+clip+","+promoID+","+classType+",,"+setDate+","+status+"\n");
						}
					}else 
						sb.append("No Active selections \n");
				
					subscriberDeact = rbtStatus.getSubscriberDeactivatedRecords(subs[i]);
					if(subscriberDeact != null){
						for(int k=0;k < subscriberDeact.length; k++){
							st_stat = new StringTokenizer(subscriberDeact[k], ",");
							callerID = st_stat.nextToken();
							category = st_stat.nextToken();
							clip = st_stat.nextToken();
							promoID = st_stat.nextToken();
							classType = st_stat.nextToken();
							setDate = st_stat.nextToken();
                            status = st_stat.nextToken();
							sb.append(",,,,,,,,"+callerID+","+category+","+clip+","+promoID+","+classType+",,"+setDate+","+status +"\n");
						}
					}
				}
			}
			
			try {
					fout.write(sb.toString().getBytes());
					fout.close();
					fin.close();
				} catch (Exception e) {
			
				}
				session.setAttribute("File",reportFile.getAbsolutePath());
				%>
			<jsp:forward page ="rbt_download.jsp" />		
		<%}
		}
		catch(Exception e)
		{
			System.out.println("Exception in rbt_disp(file) "+e.getMessage());
			session.setAttribute("SubStatus","FAILURE");
			session.setAttribute("SubReason","Internal Error");
		}
		finally
		{
			try
			{
				java.io.File temp = new java.io.File(pathDir + File.separator + strFile);
				if(temp.exists())
				{
					temp.delete();
				}
			}
			catch(Exception e)
			{
				System.out.println("Could not delete the file " + strFile + " from " + pathDir);
			}
		}
		}
		else{
			session.invalidate();%>
			<jsp:forward page="index.jsp" />
		<%}%>