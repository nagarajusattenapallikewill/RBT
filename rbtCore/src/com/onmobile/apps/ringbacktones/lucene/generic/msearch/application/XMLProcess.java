package com.onmobile.apps.ringbacktones.lucene.generic.msearch.application;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;

public class XMLProcess {

	private Marshaller marshaller;
	private Unmarshaller unMarshaller;

	public XMLProcess() {
	}

	public Marshaller getMarshaller() {
		return marshaller;
	}

	public void setMarshaller(Marshaller marshaller) {
		this.marshaller = marshaller;
	}

	public Unmarshaller getUnMarshaller() {
		return unMarshaller;
	}

	public void setUnMarshaller(Unmarshaller unMarshaller) {
		this.unMarshaller = unMarshaller;
	}

}
