package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Servlet implementation class TnbActivation
 */
public class TnbActivation extends HttpServlet 
{
	private static final long serialVersionUID = 1L;
    public TnbActivation() 
    {
        super();
    }

	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		
		String responseText = "FAILED";
		try
		{
			String subscriberID = request.getParameter("MSISDN");
			String requestType = request.getParameter("REQUEST");
			String subscriberType=request.getParameter("SUB_TYPE");
			String subscribtionClass=request.getParameter("SUBSCRIPTION_CLASS");
			String actBy=request.getParameter("ACTIVATED_BY");
			
		
		if (subscriberID == null)
			responseText = "INVALID PARAMETERS";
		else
			{	
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
				Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
				String status = subscriber.getStatus();
				
				if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE) || status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))
					responseText = "ALREADY_ACTIVE";
				else if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
							||status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))  
				{
					SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberID);
					subscriptionRequest.setSubscriptionClass(subscribtionClass);
					subscriptionRequest.setMode(actBy);
					subscriptionRequest.setType(subscriberType);
					RBTClient.getInstance().activateSubscriber(subscriptionRequest);
					
					responseText = subscriptionRequest.getResponse();
				}
				else
					responseText = "FAILURE";
				}
	}
	catch(Exception e)
	{
		Logger.getLogger(TnbActivation.class).error("RBT:: " + e.getMessage(), e);
		responseText = "FAILURE";
	}
		Logger.getLogger(TnbActivation.class).info("RBT:: Respone Text : "+responseText);
		if(responseText!=null)
			response.getWriter().write(responseText);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
	{
		doGet(request, response);
	}

	


}
