<%@page session="false"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%! public void jspInit(){
  System.out.println("in jspInit()");
//  javax.servlet.ServletConfig servletConfig = getServletConfig();
//  servletConfig.getServletContext().setAttribute("inittime",""+System.currentTimeMillis());
try
{
	RBTMOHelper.init(); 
	RBTSubUnsub.init();

}
catch(Exception e)
	{
	}
} // end jspInit() method
%>
