<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,
	com.jspsmart.upload.*,java.io.File,
	com.onmobile.apps.ringbacktones.common.Tools"%><%

//To prevent getOutputStream() has already been called error, as the generated servlet will have out . write statements for each free space or line.
%><%

String result=null;
long maxfilesize = 20000000;
SmartUpload mySmartUpload=null;
mySmartUpload=new SmartUpload();
mySmartUpload.initialize(pageContext); 
mySmartUpload.setTotalMaxFileSize(maxfilesize);
mySmartUpload.upload();
RBTSubUnsub subUnsub = RBTSubUnsub.init();
String file=null;
Tools.logDetail("rbt_gui_blacklist.jsp.jsp","rbt_gui_blacklist.jsp","RBT: Channel INCOMING REQUEST = " + mySmartUpload.getRequest().getParameter("request_value")+"***");
result=mySmartUpload.getRequest().getParameter("request_value");
Tools.logDetail("rbt_gui_blacklist.jsp","rbt_gui_blacklist.jsp","RBT :: result is "+result);
String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);


	if(result.equalsIgnoreCase("black_list"))
	{
	
		
		if(mySmartUpload.getFiles().getCount() > 0)
		{
			if(mySmartUpload.getFiles().getFile(0).getSize() > 0)
			{
				 file = "BlacklistServer-" + System.currentTimeMillis() + ".txt";
				mySmartUpload.getFiles().getFile(0).saveAs(pathDir + java.io.File.separator + file);
			}
		}
		if(mySmartUpload.getFiles().getCount() <= 0 || mySmartUpload.getFiles().getFile(0).getSize() <= 0)
		{
			session.setAttribute("flag","Subscriber file size is zero");
		} else {
			Tools.logDetail("rbt_gui_blacklist.jsp","rbt_gui_blacklist.jsp","Blacklist selection done for a file by user with blacklist type TOTAL");
		}
		File filepath=new File(pathDir+java.io.File.separator+file);
		Tools.logDetail("rbt_gui_blacklist.jsp","rbt_gui_blacklist.jsp","the file sent to addBlackListFile is "+file +" and the filePath is "+filepath );
		 boolean addboolean=subUnsub.addBlackListFile(file,"TOTAL");
		 Tools.logDetail("rbt_gui_blacklist.jsp","rbt_gui_blacklist.jsp","the value of boolean addboolean is "+addboolean);
		//if(addboolean){
			//filepath.delete();
		//}
	}


%>