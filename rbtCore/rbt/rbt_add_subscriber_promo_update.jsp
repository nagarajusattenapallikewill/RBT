<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "java.util.*,java.io.*"%>
<%@ page language="java" import="com.jspsmart.upload.*" %>

<%@ include file = "validate.jsp" %>
<%
String strUser=null;
String file=null;
java.io.File statusFile;
String actInfo=null;
%>
<%
RBTSubUnsub	rbtLogin = RBTSubUnsub.init();
file = null;
String strIP  = request.getRemoteAddr();
strUser  = (String)(session.getAttribute("UserId"));
if(strUser == null)
	actInfo = strIP + ":Direct"; 
else
	actInfo = strIP + ":" + strUser;

if (validateUser(request, session,  "rbt_add_subscriber_promo_manager.jsp", response)) { %>


		<%
			String subType=null;
			String promoType = "ICARD";
			boolean isPrepaid=false;
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
			
			subType = mySmartUpload.getRequest().getParameter("comboSubType");

			isPrepaid = false;
			if(subType !=null && subType.equalsIgnoreCase("prepaid"))
				isPrepaid = true;

			//prepaid-postpaid change by gautam
			if(RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "USE_DEFAULT_TYPE_GUI_PROCESSING", "TRUE"))
			{
				if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Prepaid"))
					isPrepaid = true;
				if(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("Postpaid"))
					isPrepaid=false;
			}
			promoType = mySmartUpload.getRequest().getParameter("comboPromoType");
			if(promoType == null)
				promoType = "ICARD";
			if(mySmartUpload.getFiles().getCount() > 0){
				if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
				{
					//file = mySmartUpload.getFiles().getFile(0).getFileName();
					//mySmartUpload.save(pathDir);
					file = "Subscriber-Promo-" + System.currentTimeMillis() + ".txt";
					mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
				}
			}
			

				if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0) {
				session.setAttribute("flag","Subscriber file size is zero");%>
					<jsp:forward page="rbt_add_subscriber_promo_manager.jsp" />
				<%}
				else{
					System.out.println("Bulk Promotion Done for a File by User "+strUser);
					FileReader fr = new FileReader(pathDir+java.io.File.separator+file);
			        BufferedReader br = new BufferedReader(fr);
            
				    String line = br.readLine();
            
					while(line != null)
					{
		               line = line.trim();
		               if(rbtLogin.isValidSub(line).equals("success"))
							rbtLogin.addSubscriberPromo(line, 30, isPrepaid, promoType, promoType);
					   line = br.readLine();
					}

					br.close();
					fr.close();

					session.setAttribute("flag","true");
				}
	}
	catch(Exception e)
	{
		System.out.println("Exception in add_subscriber_promo_update "+e.getMessage());
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
					<jsp:forward page="rbt_add_subscriber_promo_manager.jsp" />
<%}else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}
%>