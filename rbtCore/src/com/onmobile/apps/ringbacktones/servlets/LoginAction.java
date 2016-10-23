package com.onmobile.apps.ringbacktones.servlets;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginAction extends BaseAction
{

    protected ActionForward doExecute(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception
    {
        /*if(action != null && action.equals("logout"))
        {
            session.setAttribute("AuthenticationInfo", null);
        }
        else 
        {
            
            response.sendRedirect("/ccc/subscriber/subscriber_info.jsp");
        }
        else if ((loginName != null || password != null) && action == null)
        {
            onload = "jsLogFail();";
        }*/
        // TODO Auto-generated method stub
        //actionForm.
        return mapping.findForward("success");
    }
}