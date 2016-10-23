package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

public class Consents {

	Consent consent [] = null;

	
	public Consent[] getConsent() {
		return consent;
	}

	public void setConsent(Consent[] consent) {
		this.consent = consent;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(consent);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Consents other = (Consents) obj;
		if (!Arrays.equals(consent, other.consent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Consents [consent=" + Arrays.toString(consent) + "]";
	}
	
	
	
	
}
