<%@ page import = "com.onmobile.apps.ringbacktones.subscriptions.RBTSubUnsub,java.util.*,com.onmobile.apps.ringbacktones.content.UserRights"%>
<script language="Javascript">
{
window.history.forward(1);
}
</script>
<%!
	public boolean validateUser(HttpServletRequest request, HttpSession session, String page, HttpServletResponse response) 
	{
		String strUser  = (String)(session.getAttribute("UserId"));
		String strIP  = request.getRemoteAddr();
		String strUserType  = (String)(session.getAttribute("UserType"));
		String strPwd  = (String)(session.getAttribute("Pwd"));

		response.setHeader("pragma","no-cache");
		response.setHeader("Cache-Control","no-cache");
		response.setHeader("Cache-Control","no-store");
		response.addDateHeader("Expires", 0);
		response.setDateHeader("max-age", 0);
		response.setIntHeader ("Expires", -1);

		boolean isValid = false;
		RBTSubUnsub rbtLogin = null;
		try
		{
			rbtLogin = RBTSubUnsub.init();
		}
		catch(Exception e)
		{
		}
		if(strUserType !=null)
		{
			if (strUserType.equals("VALID"))
			{
				if (strUser != null)
				{
//					int iuserType = objUser.fnUserValid(strUser,strPwd);
//					if (iuserType != OnMobileGUIConstants.INT_ONM_INVALID_USER)
						isValid = true;
				}
			}
			else if(strIP.equalsIgnoreCase("127.0.0.1"))
				isValid = true;
		}
		if(isValid)
		{
			if(page.equalsIgnoreCase("rbt_login.jsp"))
				return true;
			HashMap hMap = (HashMap) session.getAttribute("UserRights");
			if(hMap == null)
				return false;
			String userType = (String)session.getAttribute("Permission");
			String userRights = rbtLogin.getUserRights(userType.trim());
			if(userRights == null)
				return false;
			StringTokenizer st = new StringTokenizer(userRights.trim(), ",");
			while(st.hasMoreElements())
			{
				String key = st.nextToken().trim();
				if(key.equalsIgnoreCase("0"))
					continue;
				if(hMap.containsKey(key))
				{
					String value = (String)hMap.get(key);
					StringTokenizer token = new StringTokenizer(value.trim(), ",");
					String name = token.nextToken().trim();
					String jsp = token.nextToken().trim();
					if(jsp.equalsIgnoreCase(page))
						return true;
				}

			}
		}

		return false;
	}
	
%>