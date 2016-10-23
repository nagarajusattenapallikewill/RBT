<%@page import="com.onmobile.apps.ringbacktones.common.iRBTConstant"%>
<%@page import="com.onmobile.apps.ringbacktones.common.RBTParametersUtils"%>
<%@ page import = "java.util.*,java.net.InetAddress"  autoFlush="true" session="true"%>
<%
java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
String strHostName =  ip.getHostName();
String m_logo = "images/onmobile_logo.gif";
String m_name = "O n M o b i l e &nbsp;&nbsp;&nbsp;R i n g B a c k T o n e s";
m_logo = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "LOGO", "images/onmobile_logo.gif");
m_name = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "APP_NAME", "O n M o b i l e &nbsp;&nbsp;&nbsp;R i n g B a c k T o n e s");
%>
<!--Header Start -->
<table cols=3 border="0" width="100%" height="8" cellspacing="0" cellpadding="0">
  <tr>
    <td width="19%" align="right" valign="top" height="45" bgcolor="ffb365"><img src="<%=m_logo%>" width="200" height="74"></td>
    <td width="64%" height="45" bgcolor="ffb365" align="center" valign="center" bottommargin=15>
        <br>
	  <font face="Verdana, Arial, Helvetica, sans-serif" size="4"><b><%=m_name%></b></font>
    </td>
    <td width="17%" align="right" valign="top" height="45" bgcolor="ffb365"><img src="images/image_right.jpg" width="175" height="75" border="0"></td>
  </tr>
<!--
</table>
<table border="0" width="100%" cellspacing="0" cellpadding="0">
-->
	<tr bgcolor="#3875a4">
		<td align="left"><font face="Verdana" size=2 color="white"><b> &nbsp;&nbsp;Host : <%= strHostName%></b></font></td>
		<td align="right" colspan=2><font face="Verdana" size=2 color="white"><b> <%= new Date(System.currentTimeMillis())%> &nbsp;&nbsp;</b></font></td>
	</tr>
	</table>
<!--HostTime End -->