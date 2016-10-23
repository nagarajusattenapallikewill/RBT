<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,com.jspsmart.upload.*,java.util.*,java.text.*"%><%@ include file = "validate.jsp" %><%if (validateUser(request, session,  "rbt_blacklist.jsp", response)) {
Calendar cal = Calendar.getInstance();
SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMMddhhmmss");
String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
	String file = null;

	boolean addboolean = false;
	boolean removeboolean = false;
	if(request.getMethod().equals("POST"))
	{
		try  
		{
			long maxfilesize = 20000000;
			SmartUpload mySmartUpload=null;
			mySmartUpload=new SmartUpload();
			mySmartUpload.initialize(pageContext); 
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();
			RBTSubUnsub subUnsub = RBTSubUnsub.init();
			
			String option = mySmartUpload.getRequest().getParameter("adddeleteview");
			String blacklistType="VIRAL";
			if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_BLACKLIST_TYPE", "FALSE"))
			{
				blacklistType= mySmartUpload.getRequest().getParameter("blacklistType");
			}
 
		    if((option.equals("1")) || (option.equals("2")) || (option.equals("3")))
			{	
				if(mySmartUpload.getFiles().getCount() > 0)
				{
					if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
					{
						file = "Blacklist-" + System.currentTimeMillis() + ".txt";
						mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
					}
				}
			    if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0)
				{
					session.setAttribute("flag","Subscriber file size is zero");%>
					<jsp:forward page="rbt_blacklist.jsp" />
                
				<%}
				else
				{
					System.out.println("Blacklist selection done for a file by user with blacklist type " + blacklistType);		
				}
			  	if(option.equals("1"))
			    { 
					addboolean = subUnsub.addBlackListFile(file, blacklistType);      
                }
                if(option.equals("2"))
				{
				    removeboolean = subUnsub.removeBlackListFile(file, blacklistType);
                }
                if(option.equals("3"))
			    {   
					String strFileName = "BlackList"+sdf.format(cal.getTime())+".txt";
					response.setContentType("application/octet-stream");
					response.setHeader("Content-Disposition","attachment; filename="+strFileName+";");
					java.io.File reportFile = subUnsub.ViewBlackListFile(file, blacklistType);
					session.setAttribute("File", reportFile.getAbsolutePath());%><jsp:forward page ="rbt_download.jsp" /><%}
			}%><%}
    catch(Exception e)
    {
		e.printStackTrace();
	} 
  }
  %>
<HTML>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function add()
{   
		if(isEmpty(frmRBT.subFile.value))
		{
			alert("Enter a Subscriber File");
		    frmRBT.subFile.focus();
			return false;
		}
		else 
		{
			if(frmRBT.subFile.value.indexOf(".txt") == -1)
	        {  
				alert("Enter only text file");
				frmRBT.subFile.focus();
				return false; 
			}
            else
			{ 
				document.frmRBT.adddeleteview.value = '1';
				return true; 
			}
		}
		
}

function del()
{   

		if(isEmpty(frmRBT.subFile.value))
		{
			 alert("Enter a Subscriber File");
		     frmRBT.subFile.focus();
		     return false;
		}
		else 
	    {
			if(frmRBT.subFile.value.indexOf(".txt") == -1)
	        {  
				alert("Enter only text file");
				frmRBT.subFile.focus();
				return false;
			}
            else
			{ 
				 document.frmRBT.adddeleteview.value = '2';
				 return true;
			}
		}
}

function viewdata()
{   

		if(isEmpty(frmRBT.subFile.value))
		{
			 alert("Enter a Subscriber File");
		     frmRBT.subFile.focus();
		     return false;
		}
		else 
	    {
			if(frmRBT.subFile.value.indexOf(".txt") == -1)
			{
				alert("Enter only text file");
				frmRBT.subFile.focus();
			    return false;
		    }
            else
			{ 
				 document.frmRBT.adddeleteview.value = '3';
				 return true;	 
			}
	    }
}
</script>
<title>RBT Blacklist Manager</title>
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
					        <%if(addboolean == true) {%>
								            <br><div align=center> <% out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Completed Successfully.</b></font>");%><br></div>
					                        <%} %>
                             <%if(removeboolean == true) {%>
								            <br><div align=center> <% out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Completed Successfully.</b></font>");%><br></div>
					                        <%} %>
					        
			      			
			      			<!--Welcome Image -->
			      
					<table align="center" cols=2 border="0" cellpadding=6 width="75%" style="VERTICAL-ALIGN: top">
							<tr></tr>
								<title>RBT Blacklist</title>
</head>
<body>
<form action="rbt_blacklist.jsp" enctype="multipart/form-data"  method="post"  name="frmRBT">
<br><p><p><b><div align=center>Blacklist</div></b></p><p>
<table align = "center" width ="100%">
<tr></tr><tr></tr><tr></tr><tr></tr>
<tr></tr>
<tr>
<td width="30%">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Subscriber File</td><td width="60%"><input type=file name="subFile" size = 32 maxlength=32></td></tr>
<tr></tr>
<%
if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "SHOW_BLACKLIST_TYPE", "FALSE"))
{
%>
<tr>
<td width="30%">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Blacklist Type</td>
<td width="60%">
<select name="blacklistType" width=150 >
<option name="viral" value ="VIRAL">Viral BlackList</option>
<option name="total" value ="TOTAL">Total BlackList</option>
</select></td></tr>
<%
}
%>
<tr></tr>
<tr></tr>
<tr><td width="30%"></td><td><input type=submit border = "0"  name="Add" value=Add onClick = 'return add()'>
<input type=submit border = "0"  name="Delete" value=Delete onClick = 'return del()'>
<input type=submit border = "0"  name="view" value=View onClick = 'return viewdata()'></td></tr>
</table>
<input type="hidden" name="adddeleteview" value ="">
</form>
</body>
</html>

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

<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<% }%>
