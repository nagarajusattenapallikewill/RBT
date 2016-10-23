package com.onmobile.apps.ringbacktones.lucene.generic.msearch.application;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.XmlMappingException;

import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchResponse;


public class SpringApp {
	public int i = 0;

	public SpringApp() {
	}

	public void marshall(RBTMSearchResponse rBTMSearchResponse, String filePath,
			XMLProcess xmlProcess) {

		try {
			xmlProcess.getMarshaller().marshal(rBTMSearchResponse,
					new StreamResult(new FileOutputStream(filePath)));
		} catch (XmlMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public RBTMSearchResponse unMarshall(String filePath, XMLProcess xmlProcess) {
		RBTMSearchResponse rBTMSearchResponse = null;
		try {
			rBTMSearchResponse = (RBTMSearchResponse) xmlProcess.getUnMarshaller().unmarshal(
					new StreamSource(new FileInputStream(filePath)));
		} catch (XmlMappingException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
		if (rBTMSearchResponse != null)
			return rBTMSearchResponse;
		return null;
	}

}
