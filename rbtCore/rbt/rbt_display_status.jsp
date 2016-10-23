<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.onmobile.apps.ringbacktones.content.Subscriber,com.onmobile.apps.ringbacktones.content.database.RBTDBManager,java.util.StringTokenizer,java.io.FileOutputStream,java.io.FileInputStream,java.io.FileReader,java.io.LineNumberReader, java.util.*,java.io.File,java.text.*,java.io.PrintWriter,com.jspsmart.upload.*" %><%@ include file = "validate.jsp" %><%
String [] subscriberStatus = null,subscriberDeact = null;
String strUser = null, sub =  null, strFile =  null, strResult =  null, strValue =  null, X_ONMOBILE_STATUS =  null, X_ONMOBILE_FAILURE_REASON =  null, FAILURE =  null, SUCCESS =  null, strDate =  null, strReason =  null, strActby =  null, prepaid =  null, str =  null, callerID =  null, category =  null, clip =  null, promoID =  null, setDate =  null, maxSel =  null, classType =  null, status =  null, strAccess =   null, strFileName = null;
%><%long maxfilesize = 20000000;
String pathDir = null;
SmartUpload mySmartUpload=null;
try
{
mySmartUpload=new SmartUpload();
mySmartUpload.initialize(pageContext); 
mySmartUpload.setTotalMaxFileSize(maxfilesize);
mySmartUpload.upload();
sub = mySmartUpload.getRequest().getParameter("SUB_ID");
pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
if(mySmartUpload.getFiles().getCount() > 0){
if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
{
strFile = mySmartUpload.getFiles().getFile(0).getFileName();
mySmartUpload.save(pathDir);
strFile = "View-Selection-" + System.currentTimeMillis() + ".txt";
mySmartUpload.getFiles().getFile(0).saveAs(pathDir + File.separator + strFile);
}
}
}
catch(Exception e)
{

}
if(sub == null){
	Calendar cal = Calendar.getInstance();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddhhmmss");
	strFileName = "Subscriber_Status_Report"+sdf.format(cal.getTime())+".csv";
	response.setContentType("application/octet-stream");
	response.setHeader("Content-Disposition","attachment; filename="+strFileName+";");
}
if (validateUser(request, session,  "rbt_subscriber_status.jsp", response)) {%><%if(sub != null){%>
<HTML>
<head>
<title>RBT Subscriber Status</title>
</head>

<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0">
<table border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
	<tr>
		<td colspan=3 valign="top" height="15%">
			<jsp:include page="rbt_header_inc.jsp"/>
		</td>
	</tr>

	<tr>
		<td colspan=3 valign="top" height="85%">
			<table border="0" width="100%" cellspacing="0" cellpadding="0" height="100%">
				<tr>
					<!--Menu Start -->
					<td width="23%" bgcolor="#ffdec8" valign="top">
						<jsp:include page="rbt_menu_inc.jsp" />
					</td>
					<!--Menu End -->
					
					<!--Console Start  -->
					<!-- initialise global variable fix for weblogic 6.0 -->
					
					<td width="77%" bgcolor="#ffedd9" valign="top">
			      			<br>
			      			<!--Welcome Image -->
						<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<tr>
								<td align="center">
<%}
		subscriberStatus = null;
		subscriberDeact = null;
		try
		{
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			X_ONMOBILE_STATUS = null;
			X_ONMOBILE_FAILURE_REASON = null;
			FAILURE = "FAILURE";
			SUCCESS = "SUCCESS";
			strDate = null;
			strReason = null;
			strActby = null;
			prepaid = null;
	
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
				session.setAttribute("SubReason","subscriber file size is zero");%><jsp:forward page="rbt_subscriber_status.jsp" /><%}

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
				session.setAttribute("SubReason", X_ONMOBILE_FAILURE_REASON);%><jsp:forward page ="rbt_subscriber_status.jsp" /><%
			}
				File reportFile = new File(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null)+File.separator+strFileName);
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
				sb.append("Subscriber,Status,Subscriber Type,Activated By,Activated On,Deactivated By,Deactivated On,Last Access Date,CallerID,Category,Clip Name,Promo ID,ClassType,Set Time,End Time,Selection Status\n");

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
						
					String failureMessage="FAILURE_MESSAGE";
					String statusMessage=null;
					HashMap h = rbtStatus.getExtraInfoMap(subs[i], subscriber);
					if (h !=null && h.get(failureMessage)!=null && h.get(failureMessage).toString().trim().length()>0 && !(((String)h.get(failureMessage)).equalsIgnoreCase("OK"))) {
					

							sb.append(subs[i]+","+(String)h.get(failureMessage)+","+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",");
							if (subscriber.deactivatedBy()!=null)
							sb.append (subscriber.deactivatedBy());
							sb.append(",");
							if (subscriber.endDate()!=null)
							sb.append(df.format(subscriber.endDate()));
							sb.append(","+strAccess+"\n");		

					}
					else {	
					if(subscriber.deactivatedBy() != null && !subscriber.deactivatedBy().equalsIgnoreCase("AUX") &&  !subscriber.deactivatedBy().equalsIgnoreCase("NEFX"))
					{
						if(subscriber.subYes().equals("X"))			sb.append(subs[i]+",Deactive,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+","+subscriber.deactivatedBy()+","+df.format(subscriber.endDate())+","+strAccess+"\n");
						else
						sb.append(subs[i]+",Deactivation Pending,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+","+subscriber.deactivatedBy()+","+df.format(subscriber.endDate())+","+strAccess+"\n");
					}
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
                                           else if(subscriber.subYes().equalsIgnoreCase("A") || subscriber.activationDate() == null || subscriber.nextChargingDate() == null) 
				                   {
                                        if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_SUBTYPE_UNKNOWN", "FALSE"))
					                           prepaid = "Unknown";
										sb.append(subs[i]+",Activation Pending,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+",");

                                   }
								   else if (subscriber.subYes().equalsIgnoreCase("z") || subscriber.subYes().equalsIgnoreCase("Z") ) 
										sb.append(subs[i]+",Suspended,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+","); 

		   						else
					 sb.append(subs[i]+",Active,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+",");

			                 }
						else
						 sb.append(subs[i]+",Active,"+prepaid+","+subscriber.activatedBy()+","+df.format(subscriber.startDate())+",,,"+strAccess+",");
				      }
				     } 
				}

				session.removeAttribute("SubStatus");
				session.removeAttribute("SubReason");
	
				if(subscriber.deactivatedBy() == null || subscriber.deactivatedBy().equalsIgnoreCase("AUX") || subscriber.deactivatedBy().equalsIgnoreCase("NEFX"))
				{
					StringTokenizer st_stat;
					subscriberStatus = rbtStatus.getSubscriberSelections(subs[i], subscriber);
					if(subscriberStatus != null && subscriberStatus.length > 0){
						st_stat = new StringTokenizer(subscriberStatus[0], ",");
						callerID = st_stat.nextToken();
						category = st_stat.nextToken();
						clip = st_stat.nextToken();
						promoID = st_stat.nextToken();
						st_stat.nextToken();
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
							st_stat.nextToken();
							classType = st_stat.nextToken();
							setDate = st_stat.nextToken();
							for(int n=0; n<7; n++)
							     {
									 if(st_stat.hasMoreTokens())
								          st_stat.nextToken();
							     }
                            	 if(st_stat.hasMoreTokens()){
									status = st_stat.nextToken();
								}
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
				%><jsp:forward page ="rbt_download.jsp" /><%}
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
									</tr>
						</table>
				
						<!--Welcome Image -->

					</td>
					<!--Console End -->
				</tr>
			</table>
		</td>
	</tr>

</table>
</body>
</HTML>
