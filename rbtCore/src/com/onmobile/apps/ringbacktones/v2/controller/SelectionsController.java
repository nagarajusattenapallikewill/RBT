package com.onmobile.apps.ringbacktones.v2.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.rbt.PlayRule;
import com.livewiremobile.store.storefront.dto.rbt.PlayRuleList;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.ISelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


@RestController
@RequestMapping(value = "/selection")
public class SelectionsController implements WebServiceConstants {
	
	@Autowired
	@Qualifier(value = BeanConstant.SELECTION_REQUEST_RESOLVER)
	private ISelectionRequest selections;
	private static Logger logger = Logger.getLogger(SelectionsController.class);
	
	@RequestMapping(value = "", method = RequestMethod.POST)
	public PlayRule activateSongForAllCaller(@RequestBody PlayRule playRule,
			@RequestParam(value="subscriberId")String subscriberID,
			@RequestParam(value="mode") String mode, HttpServletRequest request) throws UserException {

			logger.info("activateSongForAllCaller method invoked");
			return selections.activateSong(playRule, subscriberID, mode);
				
	}
	
	
	@RequestMapping(value = "/SONG/{toneId}",method = RequestMethod.DELETE)
	public Map<String, String> deactivateSong(@PathVariable(value="toneId")String toneId,
			@RequestParam(value="subscriberId") String subscriberID, @RequestParam(value="mode") String mode)  throws UserException{
		
		logger.info("deactivateSong method invoked");
		return selections.deactivateSong(toneId, subscriberID,mode);
	}
	
	
	@RequestMapping(value = "" ,method = RequestMethod.GET)
	public PlayRuleList getPlayRules(@RequestParam(value = "type", required = false) String type, 
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "id", required = false) String id,
			@RequestParam(value = "status", required = false) String status) throws UserException {
		
		return selections.getPlayRules(type, msisdn, id, status);
		
	}

	//Added for ephemeral
	@RequestMapping(method = RequestMethod.DELETE, value ="/ephemeral")
	public Map<String, String> deactivateEphemeralRBT(@RequestParam(value="called") String subscriberID,
			@RequestParam(value="caller", required =true) String callerID,
			@RequestParam(value="categoryId", required =true) String categoryId,
			@RequestParam(value="wavfile", required =true) String wavfile,
			@RequestParam(value="status", required =true) int status,
			@RequestParam(value="mode" , required =false) String mode){
		
		    logger.info("deactivateEphemeralRBT method invoked");
		    Map<String,String> responseMap = new HashMap<String, String>();
		    String response = selections.deleteEphemeralRBTSelection(subscriberID, callerID, wavfile, categoryId, status, mode);
			responseMap.put("status", response);
		    return responseMap;
	}
	

	public void setSelections(ISelectionRequest selections) {
		this.selections = selections;
	}
	
	
}
