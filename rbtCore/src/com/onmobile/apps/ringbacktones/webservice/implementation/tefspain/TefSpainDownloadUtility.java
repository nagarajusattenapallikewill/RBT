package com.onmobile.apps.ringbacktones.webservice.implementation.tefspain;

import java.util.List;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class TefSpainDownloadUtility implements WebServiceConstants {
	public static boolean isSetForAll(List<String> allCallerWavFile,
			String rbtFile) {
		if (allCallerWavFile == null || allCallerWavFile.size() == 0)
			return false;
		if (allCallerWavFile.contains(rbtFile))
			return true;
		return false;
	}
}
