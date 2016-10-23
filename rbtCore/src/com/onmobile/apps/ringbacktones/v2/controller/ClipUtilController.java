package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.IClipUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

@RestController
@RequestMapping(value = "/clipUtil")
public class ClipUtilController implements WebServiceConstants {
	@Autowired
	private IClipUtils clipUtils;
	private static Logger logger = Logger.getLogger(ClipUtilController.class);

	@RequestMapping(value = "/updateClipToTP", method = { RequestMethod.POST,
			RequestMethod.GET })
	public Object updateClipToTPIfNotExists(
			@RequestParam(value = "subscriberId", required = false) String subscriberId,
			@RequestParam(value = "circleId", required = false) String circleId,
			@RequestParam(value = "wavFile", required = true) String rbtWavFile,
			@RequestParam(value = "catId", required = false) String categoryId)
			throws UserException {
		logger.info("updateClipToTPIfNotExists method invoked");
		return clipUtils.updateClipToTPIfNotExists(subscriberId, rbtWavFile,
				categoryId, circleId);
	}
}
