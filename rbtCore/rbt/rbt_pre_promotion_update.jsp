<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,java.util.*,java.io.*,com.jspsmart.upload.*,java.text.*" %><%@ include file = "validate.jsp" %><%
String strUser;
String file1, file2;
java.io.File statusFile;
String actInfo;
ArrayList prepaid = null;
%><%
Calendar cal = Calendar.getInstance();
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddhhmmss");
String strFileName = "Pre_Promotion"+sdf.format(cal.getTime())+".txt";
response.setContentType("application/octet-stream");
response.setHeader("Content-Disposition","attachment; filename="+strFileName+";");
RBTSubUnsub rbtLogin = RBTSubUnsub.init();
prepaid = new ArrayList();
file1 = file2 = null;
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser;

if (validateUser(request, session,  "rbt_pre_promotion.jsp", response)) {%><% 
			String actBy;
			boolean bBlackoutSMS;
		%><%
		String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);	
		try
			{
			long maxfilesize = 20000000;
			SmartUpload mySmartUpload=null;
			mySmartUpload=new SmartUpload();
			mySmartUpload.initialize(pageContext); 
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();
			
			actBy = mySmartUpload.getRequest().getParameter("comboActBy");
			bBlackoutSMS = false;
			if(mySmartUpload.getRequest().getParameter("blackout_sms") != null)
				bBlackoutSMS = true;
			if(bBlackoutSMS)
				actInfo = "BULK:" + actInfo;
			
			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0 && mySmartUpload.getFiles().getFile(1).getSize() > 0)
				{
					file1 = "Prepaid-Promotion-1-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file1);
					file2 = "Prepaid-Promotion-2-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(1).saveAs(pathDir + java.io.File.separator + file2);
				}
			}
			
				if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) {
				session.setAttribute("flag","Subscriber file size is zero");%>
					<jsp:forward page="rbt_pre_promotion.jsp" />
				<%}
				else{
					System.out.println("Promotion With Prepaid File Done for a File by User "+strUser);
					FileReader fr = new FileReader(pathDir+java.io.File.separator+file1);
			        BufferedReader br = new BufferedReader(fr);
            
				    String line = br.readLine();
            
					while(line != null)
					{
		               line = line.trim();
						prepaid.add(line);
					   line = br.readLine();
					}

					br.close();
					fr.close();

					statusFile = rbtLogin.processSelections(file2, prepaid, actBy, actInfo, "DEFAULT",null);
//					System.out.println("Status File "+statusFile);
					session.setAttribute("File",statusFile.getAbsolutePath());
					//session.setAttribute("flag","pre");%><jsp:forward page ="rbt_download.jsp" /><%}
	}
	catch(Exception e)
	{
		System.out.println("Exception in rbt_pre_promotion_update "+e.getMessage());
		session.setAttribute("flag","Internal Error");
	}
	finally
	{
		try
		{
			java.io.File temp1 = new java.io.File(pathDir + java.io.File.separator + file1);
			if(temp1.exists())
			{
				temp1.delete();
			}
		}
		catch(Exception e)
		{
			System.out.println("Could not delete the file " + file1 + " from " + pathDir);
		}
		try
		{
			java.io.File temp2 = new java.io.File(pathDir + java.io.File.separator + file2);
			if(temp2.exists())
			{
				temp2.delete();
			}
		}
		catch(Exception e)
		{
			System.out.println("Could not delete the file " + file2 + " from " + pathDir);
		}
	}
			
			%><jsp:forward page="rbt_pre_promotion.jsp" /><%
	}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>