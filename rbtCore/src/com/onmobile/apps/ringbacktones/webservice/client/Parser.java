package com.onmobile.apps.ringbacktones.webservice.client;

import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;

public class Parser {
	
	private String response = null;
	private Document document = null;	
	private Request request = null;
	private IXMLParser parser = null;
	private String subscriberId = null;
	
	public Parser() {
		
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public IXMLParser getParser() {
		return parser;
	}

	public void setParser(IXMLParser parser) {
		this.parser = parser;
	}

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}
	
	

}
