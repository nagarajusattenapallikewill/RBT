package com.onmobile.apps.ringbacktones.v2.controller;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.payment.Purchase;
import com.livewiremobile.store.storefront.dto.rbt.AssetList;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.dto.LibrayRequestDTO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.IDownloadRequest;

@RestController
@RequestMapping("/library")
public class Library {
	
	@Autowired
	@Qualifier(value = BeanConstant.DOWNLOAD_REQUEST_RESOLVER)
	private IDownloadRequest download;
	Logger logger =  Logger.getLogger(Library.class);
	@Autowired
	private ApplicationContext context;
	@Autowired
	private ResponseErrorCodeMapping errorCodeMapping;
	
	
	@RequestMapping(value="/SONG/{toneId}", method=RequestMethod.DELETE)
	public Map<String,String> deleteSongFromLibrary(@RequestParam(value = "subscriberId", required = true)String msisdn, 
			@RequestParam(value="mode", required = true) String mode, @RequestParam(value="ctype", required = true) String ctype,
			@PathVariable("toneId") String toneId, HttpServletResponse httpResponse) throws UserException{
		logger.info("Library Delete Request reached: subscriberId: " + msisdn + ", mode: " + mode + ", toneId: " + toneId);
		return download.deleteSongFromLibrary(msisdn, mode, toneId, ctype);
		
	}
	
	@RequestMapping(method=RequestMethod.GET)
	public AssetList getLibrary(@RequestParam(value = "subscriberId", required = true)String msisdn, 
			@RequestParam(value="mode", required = false, defaultValue="WAP")String mode, HttpServletResponse httpResponse) throws UserException{
		
		logger.info("Library Request reached: subscriberId: " + msisdn + ", mode: " + mode );
		
		return download.getLibrary(msisdn, mode);
	}
	
	@RequestMapping(value="/SONG", method=RequestMethod.POST)
	public Purchase likeContent(@RequestParam(value = "subscriberId", required = true)String msisdn, 
			@RequestParam(value="mode", required = true) String mode, @RequestBody(required = true) LibrayRequestDTO dtoResource) throws UserException{
		
		if(dtoResource == null || dtoResource.getToneId() == null || dtoResource.getToneId().isEmpty() || dtoResource.getType() == null || dtoResource.getType().isEmpty()) {
			ServiceUtil.throwCustomUserException(errorCodeMapping, Constants.INVALID_PARAMETER, MessageResource.INVALID_PARAMETER_MESSAGE);
		}
		logger.info("Library like Content Request reached: subscriberId: " + msisdn + ", mode: " + mode + ", toneId: " + dtoResource.getToneId());
		
		return download.likeContent(msisdn, mode, dtoResource);
		
	}

	@RequestMapping(value="/update", method=RequestMethod.PUT)
	public Object updateLibrary(@RequestParam(value = "subscriberId", required=true)String subscriberId,@RequestBody(required = true) LibrayRequestDTO librayRequestDTO) throws UserException{
		if(librayRequestDTO == null || librayRequestDTO.getChargeClass() == null || librayRequestDTO.getChargeClass().isEmpty()){
			ServiceUtil.throwCustomUserException(errorCodeMapping, Constants.INVALID_PARAMETER, MessageResource.LIBRARY_UPDATE_MESSAGE);
		}
		logger.info("Library Request reached to UPDATE CONTROLLER FOR : subscriberId: " + subscriberId + ", LibrayRequestDTO : " + librayRequestDTO);
		return download.updateLibrary(subscriberId,librayRequestDTO);
	}
	
	public void setContext(ApplicationContext context) {
		this.context = context;
	}

}
