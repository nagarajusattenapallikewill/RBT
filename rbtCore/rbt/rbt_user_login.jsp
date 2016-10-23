<!-- OnMobile  Login page
*     Gets the UserId and password and if Valid takes to the
*     main Page of OnMobile Admin Console
*     Addds the UserId to Session for authentication
*     Session Attribute is "UserId"
-->

<%@ page import="com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub" autoFlush="true" session="true" buffer = "100kb" %>

<% String strMessage,strColor,strParam,strUser,strPwd;
 %>
<html>
<head>
<%@ include file = "javascripts/OnMobileValidate.js" %>
<%@ include file = "javascripts/onmobileLogin.js" %>
<title>OnMobile Management Console - Console Login</title>
</head>
<font face="Verdana, Arial, Helvetica, sans-serif" size="3">
<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0" onLoad = 'return fnSetFocus(frmLogin,0)'>
<form name="frmLogin" id=frmLogin action="rbt_user_login.jsp" method=post>

<body topmargin="0" bottommargin="0" leftmargin="0" rightmargin="0">

<table border="0" cols=3 width="100%" cellspacing="0" cellpadding="0" height="100%">
 	<tr>
 		<td colspan=3 valign="middle" height="12%">
 			<table cols=3 border="0" width="100%" cellspacing="0" cellpadding="0" height="100%">
 				<tr>
					<td colspan=3 valign="top" height="15%">
					<jsp:include page="rbt_header_inc.jsp"/>
				</td>
				</tr>			
			</table>
 		</td>
 	</tr>
<!-- header End -->

<!-- body start -->

	<tr>
		<td colspan=3 valign="top" height="*%">
			<table width="100%" cols=2 border="0" cellspacing="0" cellpadding="8" background="images/bg_login.jpg">
				<tr>
					<td align="center" colspan=2>&nbsp;</td>
				</tr>
				<tr>
					<td align="center" colspan=2>&nbsp;</td>
				</tr>
				<tr>
					<td align="center" colspan=2>&nbsp;</td>
				</tr>
				<tr>
					<td align="center" colspan=2>
					<b>Console Login</b>
					</td>
				</tr>
				<tr>
					<td align="center" colspan=2>&nbsp;</td>
				</tr>
				<tr>
					<td align="right" width="50%">
						User Name
					</td>
					<td align="left">
					<input name="txtUser" maxlength="32" >
					</td>
				</tr>
				<tr>
					<td align="right">&nbsp;&nbsp;Password</td>
					<td><input type="password" name="txtPassword" maxlength="20"></td>
				</tr>
				<tr>
					<td align="right">
						<input type="image" border="0" name="btnLogin" src="images/login.gif" onClick = 'return fnSubmit(frmLogin)' >
					</td>
					<td align="left">
						<input type="image" border="0" name="btnLogin" src="images/reset.gif" onClick = 'return fnClearAll(frmLogin)' >
					</td>
				</tr>

				
				  <!--  User Logged out Condition Start-->

				              <%  
	    						strMessage =null;
						        strUser = request.getParameter("txtUser");
								strPwd = request.getParameter("txtPassword");
								String rbtLogin = null; 
								if (strUser != null) 
									rbtLogin = RBTSubUnsub.init().getLogin(strUser,strPwd);
								
								if (rbtLogin == null) 
									strMessage ="Login failed : Invalid user name/password.";
								else
								{
										if (strUser != null) 
										{
											session.setAttribute("UserId", strUser);
											session.setAttribute("Pwd", strPwd);
											session.setAttribute("Permission",rbtLogin);
								%>
									<jsp:forward page ="rbt_login.jsp" />
								<%
										}
							        
						  		}
						  %>							            
				<tr>
					<td align="center" colspan=2>
					
			<%		
					if (request.getParameter("txtUser") != null)
					{
						if (strMessage != null)
						{
							
			%>
						<tr>
								<td colspan="2" align="center"><%out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>"+strMessage+".</b></font>");%></td>
						</tr>

					
				<%
						}
					}
				%>					
					</td>
				</tr>							    
			         <!--  User Login Validations  Start -->
        	    
				<tr>
					<td align="center" colspan=2 rowSpan=5>&nbsp;</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
</font>
</form>
</body>
</html>

<!-- body end -->
