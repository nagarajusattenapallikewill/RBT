package com.onmobile.apps.ringbacktones.rbt2.response;

import org.springframework.stereotype.Component;

@Component
public class StringGriffResponseHandler implements IResponseHandler{
	
	
	@Override
	public String processResponse(String response) {
		return response;
	}

}
