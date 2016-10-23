package com.onmobile.apps.ringbacktones.servlets;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.util.MessageResources;

public abstract class BaseAction extends DispatchAction
{
    /**
     * Log.
     */
    protected final Log log = LogFactory.getLog(this.getClass());

    /**
     * Override the execute method in Action. Subclasses should implement
     * doExecute method.
     * 
     * @param mapping The ActionMapping used to select this instance
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @param form The optional ActionForm bean for this request (if any)
     * @return Describes where and how control should be forwarded.
     * @throws Exception if an error occurs
     */
    public ActionForward execute(final ActionMapping mapping,
            final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception
    {
        ActionForward actionForward = null;
        actionForward = this.checkLogin(mapping, form, request, response);

        if (actionForward != null)
        {
            return actionForward; // user not loged in or session timed out.
        }
        actionForward = this.doExecute(mapping, form, request, response);
        return actionForward;
    }

    /**
     * Subclasses must implement this method.
     * 
     * @param mapping .
     * @param actionForm .
     * @param request .
     * @param response .
     * @return .
     * @throws Exception .
     */
    protected abstract ActionForward doExecute(ActionMapping mapping,
            ActionForm actionForm, HttpServletRequest request,
            HttpServletResponse response) throws Exception;

    /**
     * This method checks whether the user has logged in to access the
     * application.
     * 
     * @param mapping .
     * @param form .
     * @param request .
     * @param response .
     * @return .
     */
    private ActionForward checkLogin(final ActionMapping mapping,
            final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response)
    {
        ActionMessage error = null;
        HttpSession session = request.getSession();

        if (session == null)
        {
            return mapping.findForward("logout");
        }

        //        EmployeeForm userProfile =
        //            (EmployeeForm) session.getAttribute(TRMConstants.USER_PROFILE);
        //
        //        if (userProfile == null) {
        //            error = new ActionMessage("errors.session.timeout");
        //            this.storeException(request, error.getKey(), error);
        //            return mapping.findForward("logout");
        //        }
        return null;
    }

    /**
     * Logs print stack trace of an exception.
     * 
     * @param ex .
     */
    protected void logException(final Exception ex)
    {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        this.log.error(sw.toString());
    }

    /**
     * Logs print stack trace of an exception.
     * 
     * @param ex .
     * @return .
     */
    protected String getStackTrace(final Exception ex)
    {
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * This method overrides the the ExceptionHandler's storeException method in
     * order to create more than one error message.
     * 
     * @param request - The request we are handling
     * @param property - The property name to use for this error
     * @param error - The error generated from the exception mapping
     */
    protected void storeException(HttpServletRequest request, String property,
            ActionMessage error)
    {
        /*
         * ActionMessages errors = (ActionMessages)
         * request.getAttribute(Globals.ERROR_KEY);
         */

        /*
         * if (errors == null) { errors = new ActionMessages(); }
         * errors.add(property, error); saveMessages(req, actionMessages);
         * request.setAttribute(Globals.ERROR_KEY, errors);
         */

        MessageResources msgResources = getResources(request);
        ActionMessages actionMessages = new ActionMessages();
        actionMessages.add(ActionMessages.GLOBAL_MESSAGE, error);
        saveMessages(request, actionMessages);

    }

    /**
     * 
     * @param request - The request we are handling
     * @param property - The property name to use for this error
     * @param error - The error generated from the exception mapping
     * @param argument - Argument to be passed to the message
     */

    protected void storeException(final HttpServletRequest request,
            final String property, final ActionMessage error,
            final String argument)
    {
        /*
         * ActionMessages errors = (ActionMessages)
         * request.getAttribute(Globals.ERROR_KEY);
         */

        /*
         * if (errors == null) { errors = new ActionMessages(); }
         * errors.add(property, error); saveMessages(req, actionMessages);
         * request.setAttribute(Globals.ERROR_KEY, errors);
         */

        ActionMessages actionMessages = new ActionMessages();
        actionMessages.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(
                error.getKey(), argument));

        saveMessages(request, actionMessages);

    }

}