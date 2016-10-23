package com.onmobile.apps.ringbacktones.rbt2.response;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Component
public class JSONGriffResponseHandler implements IResponseHandler{

	@Override
	public String processResponse(String response) {
		JsonObject jobj = new Gson().fromJson(response, JsonObject.class);
		String returnStr = jobj.get("status").getAsString();
		if(returnStr == null) {
			returnStr = jobj.get("code").getAsString();
		}
		return returnStr;
		
	}

	

}
