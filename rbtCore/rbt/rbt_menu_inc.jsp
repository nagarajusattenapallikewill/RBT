<%@ page import = "java.util.*"%>
<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub"%>
<%@ page import = "com.onmobile.apps.ringbacktones.content.UserRights"%>
<table border="0" align="center" cellpadding=5 >
<%
session.removeAttribute("Browse");
RBTSubUnsub rbtCC = RBTSubUnsub.init();
HashMap hMap = (HashMap) session.getAttribute("UserRights");
if(hMap == null)
{
hMap = new HashMap();
hMap.put("0", "File Browse Option");
hMap.put("1", "Activation,rbt_subscribe_manager.jsp");
hMap.put("2", "Deactivation,rbt_subscribe_manager_deact.jsp");
//hMap.put("3", "Reactivation,rbt_subscribe_manager_react.jsp");
hMap.put("4", "View Subscriber Details,rbt_subscriber_status.jsp");
hMap.put("5", "Prepaid <--> Postpaid,rbt_change_sub_type.jsp");
hMap.put("6", "Add Subscriber Selections,rbt_subs_selections.jsp");
hMap.put("7", "List Categories,rbt_categories.jsp");
hMap.put("8", "List Clips,rbt_all_clips.jsp");
hMap.put("9", "Add Corporate Selections,rbt_corp_tunes.jsp");
hMap.put("10", "Add SMS Promotion Selections,rbt_sms_promo_tunes.jsp");
hMap.put("11", "Add User Rights,rbt_add_user_permissions.jsp");
hMap.put("12", "View Pick Of The Day,rbt_view_pick_of_day.jsp");
hMap.put("13", "Add Pick Of The Day,rbt_add_pick_of_day.jsp");
hMap.put("14", "Process Bulk Selection,rbt_bulk_selection_manager.jsp");
hMap.put("15", "Add To Subscriber Promo,rbt_add_subscriber_promo_manager.jsp");
hMap.put("16", "Add Promotion,rbt_pre_promotion.jsp");
hMap.put("17", "TeleCalling,rbt_telecalling.jsp");
hMap.put("18", "Blacklist Numbers,rbt_blacklist.jsp");
hMap.put("19", "Add SMS Text,rbt_modify_sms_text.jsp");
hMap.put("20", "Add Bulk SMS Promotion Selections,rbt_sms_promo_tunes_bulk.jsp");
hMap.put("22", "Refresh RBT Content,rbt_content_refresh.jsp");
hMap.put("23", "Try and Buy (TNB),rbt_tnb.jsp");
hMap.put("24", "Intro Prompt,rbt_intro_prompt.jsp");
hMap.put("26","Add Bulk Selection,rbt_bulk_selection_task_manager.jsp");
hMap.put("28","Gift Service,rbt_gift_main.jsp"); 
hMap.put("29","Group,rbt_group_main.jsp"); 
hMap.put("27","View Bulk Selection Tasks,rbt_bulk_selection_tasks_manager.jsp");
hMap.put("30","Cricket,rbt_cricket.jsp");

session.setAttribute("UserRights", hMap);
}

String userType = (String)session.getAttribute("Permission");

if(userType == null){
	session.invalidate();%>
	<jsp:forward page="index.jsp" />
<%}%>
	<tr>
     <td> <IMG align=center border=0 height=15 src="images/bg_color.gif" width=174></td>
	</tr>

<%


String userRights = rbtCC.getUserRights(userType.trim());
if(userRights == null){
	session.invalidate();
	%>
	<jsp:forward page="index.jsp" />
<%}%>

<%
StringTokenizer st = new StringTokenizer(userRights.trim(), ",");
while(st.hasMoreElements())
{
	String key = st.nextToken().trim();
	if(key.equalsIgnoreCase("0"))
	{
		session.setAttribute("Browse", "TRUE");
	}
	else if(key.equalsIgnoreCase("21"))
	{
		session.setAttribute("SMSHistory", "TRUE");
	}
	else if(key.equalsIgnoreCase("25")) { 
		session.setAttribute("SelFromListClip", "TRUE"); 
   } 

	else if(hMap.containsKey(key)) 
	{
		String value = (String)hMap.get(key);
		StringTokenizer token = new StringTokenizer(value.trim(), ",");
		String name = token.nextToken().trim();
		String jsp = token.nextToken().trim();%>
    <tr>
	 <td> <IMG align=center border=0 height=15 src="images/bullet.gif" width=15>&nbsp;<A href="<%=jsp%>"><font face="Arial" size="2"><%=name%></font></a>
	 </td>
	</tr>
	<%}
	
}
%>
	<tr>
	 <td> <IMG align=center border=0 height=15 src="images/bullet.gif" width=15>&nbsp;<A href="rbt_logout.jsp"><font face="Arial" size="2">Logout</font></a>
	 </td>
    </tr>

</table>