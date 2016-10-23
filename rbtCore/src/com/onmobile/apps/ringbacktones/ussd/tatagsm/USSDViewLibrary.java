package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class USSDViewLibrary {

	private static Logger basicLogger = Logger
			.getLogger(USSDViewLibrary.class);

	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;

	public USSDViewLibrary(Map<String, String> input,
			HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}

	public void process() throws IOException {
		response.setContentType(USSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());
	}

	public String getResponse() {
		RBTClient rbtClient = null;
		try {
			rbtClient = RBTClient.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		String subscriberId = input.get("subscriber");
		String confirmDelete = input.get("delete");
		String categoryId = input.get("catid");
		String callerId = input.get("callerid");

		if (basicLogger.isInfoEnabled()) {
			basicLogger.info("View the current selections. subscriberId: "
					+ subscriberId + " confirmRemove: " + confirmDelete);
		}

		if (StringUtils.isEmpty(confirmDelete)) {

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(
					subscriberId);

			Library library = rbtClient.getLibrary(rbtDetailsRequest);

			// confirm the selection/subscription
			StringBuilder chargingInfo = new StringBuilder();
			chargingInfo.append(USSDConfigParameters.getInstance()
					.getParameter("CONFIRM_DELETE_SONG"));

			List<USSDNode> output = new ArrayList<USSDNode>();

			for (Setting setting : library.getSettings().getSettings()) {
				String viewSelectionsURL = USSDConfigParameters.getInstance()
						.getUSSDHostURL()
						+ "&action=viewlibrary&delete=true&catid="
						+ setting.getCategoryID()
						+ "&caller="
						+ setting.getCallerID();

				USSDNode node = new USSDNode(setting.getToneID(), 0,
						setting.getToneName(), viewSelectionsURL);
				output.add(node);
			}

			return USSDResponseBuilder
					.convertToResponse(USSDConfigParameters.getInstance()
							.getParameter("MESSAGE_SONGS_IN_LIBRARY"), output,
							false, null, 0);
		} else if (StringUtils.isNotEmpty(confirmDelete)) {
			SelectionRequest selectionRequest = new SelectionRequest(
					subscriberId);
			selectionRequest.setCallerID(callerId);
			selectionRequest.setCategoryID(categoryId);
			rbtClient.deleteSubscriberSelection(selectionRequest);
			return USSDResponseBuilder.convertToResponse(
					USSDConfigParameters.getInstance().getParameter(
							"MESSAGE_SONG_DELETE_SUCCESS"),
					new ArrayList<USSDNode>(0), true, null, 0);
		}
		return "";
	}
}
