package com.onmobile.apps.ringbacktones.v2.service;

import java.util.Map;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IClipUtils {
	public Map<String, String> updateClipToTPIfNotExists(String subscriberID,
			String rbtWavFile, String categoryId, String circleId)
			throws UserException;

}
