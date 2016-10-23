package com.onmobile.apps.ringbacktones.v2.dao.impl;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.v2.dao.IMigrateUser;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberDetails;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public class MigrateHttpImpl implements IMigrateUser {
	private static Logger logger = Logger.getLogger(MigrateHttpImpl.class);
	
	@Override
	public String migrateUser(RBTSubscriberDetails rbtSubscriberDetails) throws UserException {

		try {
			logger.info("Inside MigrateHttpImpl--> ");
			if (rbtSubscriberDetails == null || rbtSubscriberDetails.getRbtSubscriber() == null) {
				throw new UserException("Invalid user details");
			}
			String url = ConsentPropertyConfigurator.getRBTD2CMigrationURLFormConfig();
			HttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(url);
			Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
			String jsonStr = gson.toJson(rbtSubscriberDetails);
			StringEntity se = new StringEntity(jsonStr);
			se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(se);
			HttpResponse response = client.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK
					&& response.getEntity().getContent() != null) {
				logger.info("setting user info success");
				return "SUCCESS";
			}
		} catch (Throwable ce) {
			logger.error("setOperatorUserInfo failed:  " + ce.getMessage());
		}
		return "FAILURE";
	}

}
