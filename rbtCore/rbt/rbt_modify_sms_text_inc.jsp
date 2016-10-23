<%@ page import = "com.onmobile.apps.ringbacktones.content.BulkPromoSMS"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub" %>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTMOHelper" %>
<%@ page import = "java.util.*"%>

<%@ include file = "validate.jsp" %>
<%
String strUser  = (String)(session.getAttribute("UserId"));
if (validateUser(request, session,  "rbt_modify_sms_text.jsp", response)) { 
	System.out.println("SMS Text Changed By" + strUser);
%>
<html>
<head>
<%@ include file = "javascripts/RBTValidate.js" %>
<script language="JavaScript">
function update()
{
	//if(document.forms[0].smsSelected == null)
	//{
	//	 alert('Please select atleast one option');
	//	 return false;
	//}

	 if ( (document.forms[0].newText0 != null && document.forms[0].newText0.value.length >160) || (document.forms[0].newText1 != null && document.forms[0].newText1.value.length >160) || (document.forms[0].newText2 != null && document.forms[0].newText2.value.length >160) || (document.forms[0].newText3 != null && document.forms[0].newText3.value.length >160) || (document.forms[0].newText4 != null && document.forms[0].newText4.value.length >160) || 
	 (document.forms[0].newText5 != null && document.forms[0].newText5.value.length >160) ||
	 (document.forms[0].newText6 != null && document.forms[0].newText6.value.length >160) ||
	 (document.forms[0].newText7 != null && document.forms[0].newText7.value.length >160) ||
	 (document.forms[0].newText8 != null && document.forms[0].newText8.value.length >160) ||
	 (document.forms[0].newText9 != null && document.forms[0].newText9.value.length >160) )
	{
	     if ( confirm(" One of the new SMS Texts entered is longer than 160 characters. To accept this text, press OK. To re-enter the text, press cancel !"))
			 return true;
		 else 
			 return false;
	}
	return true;
}
</script>
<title>RBT Modify SMS Text</title>
</head>
<body> 
<form action="rbt_modify_sms_text.jsp" onsubmit='return update()' method="post" name="frmRBT">
<p><b><center>Add SMS Text</center></b></p>

<table width="100%" align = "center">

<% String strStatus, strReason, promoId, smsNewText0,smsNewText1, smsSent, smsNewText2 ,smsNewText3, smsNewText4, smsNewText5, smsNewText6,smsNewText7,smsNewText8,smsNewText9;
boolean result=false;
smsSent=null;
smsNewText0=null;
smsNewText1=null;
smsNewText2=null;
smsNewText3=null;
smsNewText4=null;
smsNewText5=null;
smsNewText6=null;
smsNewText7=null;
smsNewText8=null;
smsNewText9=null;
BulkPromoSMS[] bulkPromoSmses = null;
BulkPromoSMS[] distinctPromoIds = null;
BulkPromoSMS[] conditionsForDistinctPromoId = null;

RBTSubUnsub	rbtLogin = RBTSubUnsub.init();

promoId = request.getParameter("smsSelected");
smsNewText0 = request.getParameter("newText0");
smsNewText1 = request.getParameter("newText1");
smsNewText2 = request.getParameter("newText2");
smsNewText3 = request.getParameter("newText3");
smsNewText4 = request.getParameter("newText4");
smsNewText5 = request.getParameter("newText5");
smsNewText6 = request.getParameter("newText6");
smsNewText7 = request.getParameter("newText7");
smsNewText8 = request.getParameter("newText8");
smsNewText9 = request.getParameter("newText9");

if( promoId != null )
{
	session.setAttribute("smsType",promoId);
}

%>
<tr> 
<td colspan="2">
<%if(  smsNewText0 !=null || smsNewText1 !=null || smsNewText2 !=null || smsNewText3 !=null || smsNewText4 !=null || smsNewText5 !=null || smsNewText6 !=null || smsNewText7 !=null || smsNewText8 !=null || smsNewText9 !=null )
{
	if(smsNewText0 !=null && smsNewText0.length() > 10)
	{
		if(smsNewText0.length() >320)
			smsNewText0 = smsNewText0.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("0") , smsNewText0, smsSent);
		session.removeAttribute("0");
	}
	
	if(smsNewText1 !=null && smsNewText1.length() > 10)
	{
		if(smsNewText1.length() >320)
			smsNewText1 = smsNewText1.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("1") , smsNewText1, smsSent);
		session.removeAttribute("1");
	}
	
	if(smsNewText2 !=null && smsNewText2.length() > 10)
	{
		if(smsNewText2.length() >320)
			smsNewText2 = smsNewText2.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("2") , smsNewText2, smsSent);
		session.removeAttribute("2");
	}

	if(smsNewText3 !=null && smsNewText3.length() > 10)
	{
		if(smsNewText3.length() >320)
			smsNewText3 = smsNewText3.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("3") , smsNewText3, smsSent);
		session.removeAttribute("3");
	}

	if(smsNewText4 !=null && smsNewText4.length() > 10)
	{
		if(smsNewText4.length() >320)
			smsNewText4 = smsNewText4.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("4") , smsNewText4, smsSent);
		session.removeAttribute("4");
	}

	if(smsNewText5 !=null && smsNewText5.length() > 10)
	{
		if(smsNewText5.length() >320)
			smsNewText5 = smsNewText5.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("5") , smsNewText5, smsSent);
		session.removeAttribute("5");
	}

	if(smsNewText6 !=null && smsNewText6.length() > 10)
	{
		if(smsNewText6.length() >320)
			smsNewText6 = smsNewText6.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("6") , smsNewText6, smsSent);
		session.removeAttribute("6");
	}

	if(smsNewText7 !=null && smsNewText7.length() > 10)
	{
		if(smsNewText7.length() >320)
			smsNewText7 = smsNewText7.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("7") , smsNewText7, smsSent);
		session.removeAttribute("7");
	}

	if(smsNewText8 !=null && smsNewText8.length() > 10)
	{
		if(smsNewText8.length() >320)
			smsNewText8 = smsNewText8.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("8") , smsNewText8, smsSent);
		session.removeAttribute("8");
	}

	if(smsNewText9 !=null && smsNewText9.length() > 10)
	{
		if(smsNewText9.length() >320)
			smsNewText9 = smsNewText9.substring(0,320);
		result = rbtLogin.changeSmsText((String)session.getAttribute("smsType") , (String)session.getAttribute("9") , smsNewText9, smsSent);
		session.removeAttribute("9");
	}

	session.removeAttribute("smsType");
	RBTMOHelper.init().loadSMSTexts();
	if(!result)
		out.print("<font face=\"Arial\" size=\"3\" color = \"red\" align = \"center\"><b>ERROR : Request Could Not be Completed</b></font>");
	else 
		out.print("<font face=\"Arial\" size=\"3\" color = \"green\" align = \"center\"><b>Request Completed Successfully.</b></font>");
}%>
</td></tr>
<tr></tr><tr></tr><tr></tr><tr></tr>
<tr>







<%
distinctPromoIds = rbtLogin.getDistinctPromoIds();
if(distinctPromoIds == null || distinctPromoIds.length== 0  )
{ %>
	<tr>
	<td colspan="2"> No SMS Text Available in DB</td>
	</tr>
<% } 
else if( promoId == null )
{ %>
<tr><td colspan="2" align="center" >Select an SMS type </td></tr>
	<tr> 
		<td colspan="2" align="center"> <select name="smsSelected" > 				
			<% for( int i=0; i< distinctPromoIds.length; i++ )
			{%>
				<option value='<%=distinctPromoIds[i].bulkPromoId()%>'> <%=distinctPromoIds[i].bulkPromoId()%> </option>
			<% } %>
		</td>
	</tr>
<% } else 
{
	conditionsForDistinctPromoId = rbtLogin.getConditionsForDistinctPromoId((String)session.getAttribute("smsType") );
	
	 for( int i=0; i< conditionsForDistinctPromoId.length; i++ )
	{%>
		<tr> 
			<td colspan="2">
				<%=conditionsForDistinctPromoId[i].bulkPromoId()%> 
				<%=conditionsForDistinctPromoId[i].smsDate()!=null ? conditionsForDistinctPromoId[i].smsDate() : "" %> 
			</td>
		</tr>
		<tr>
			<td colspan="2"> Current Text: <%=conditionsForDistinctPromoId[i].smsText()%>
			</td>
		</tr>
		<tr>
			<td colspan="2"> New Text: <input type="radio" name="radio<%=i%>" OnClick="document.frmRBT.newText<%=i%>.disabled=false">&nbsp;&nbsp;&nbsp; Retain Text <input type="radio" name="radio<%=i%>" checked OnClick="document.frmRBT.newText<%=i%>.disabled=true"></td>
		</tr>
		<tr>
			<td colspan="2">
			<input type="Text" disabled name = "newText<%=i%>" size=100 maxSize=350 >
			</td>
		</tr>
		<tr></tr><tr></tr><tr></tr><tr></tr>
	 <%
		  	session.setAttribute(""+i,conditionsForDistinctPromoId[i].smsDate());
	} 
} %> 
<tr>
	</tr>
	<tr>
	</tr>
	</table>
	<center><input type="image" border="0" name="RBT_SUB" src="images/submit.GIF" ></center>
</form>
</body>
</html>
<%}
else{
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>