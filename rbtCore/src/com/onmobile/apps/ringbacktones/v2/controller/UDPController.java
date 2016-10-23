package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.rbt.Shuffle;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.IUDPService;

/**
 * 
 * @author md.alam
 * @author koyel
 *
 */

@RestController
@RequestMapping("/shufflelist")
public class UDPController {

	private static Logger logger = Logger.getLogger(UDPController.class);
	
	@Autowired
	private ServiceResolver serviceResolver;

	public void setServiceResolver(ServiceResolver serviceResolver) {
		this.serviceResolver = serviceResolver;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/param/{implpath}")
	public Object createUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "extrainfo" ,required=false) String extrainfo,
			@RequestParam(value = "mode" , defaultValue="WAP" ,required=false) String mode , @PathVariable(value="implpath") String implpath) throws UserException {
		logger.info("Create UDP request reached: subscriberId: " + msisdn
				+ ",name: " + name + ", mode: " + mode + ", extrainfo: "
				+ extrainfo + "implpath: "+implpath);
		
		 IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		 if(udpService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
			}
		 return udpService.createUDP(msisdn, name, mode, extrainfo);

	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{implpath}")
	public Object createUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestBody Shuffle shuffle,
			@RequestParam(value = "mode" , defaultValue="WAP" ,required=false) String mode , @PathVariable(value="implpath") String implpath) throws UserException {
		logger.info("Create UDP request reached: subscriberId: " + msisdn
				+ ",name: " + shuffle.getName() + ", mode: " + mode + ", extrainfo: "
				+ shuffle.getExtraInfo() + "implpath: "+implpath);
		
		 IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		 if(udpService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
			}
		 return udpService.createUDP(msisdn, shuffle.getName(), mode, shuffle.getExtraInfo());

	}

    @RequestMapping(value = {"/{shufflelistId}/{implpath}"}, method = RequestMethod.DELETE)
	public Object deleteUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@PathVariable(value = "shufflelistId") String udpId, @PathVariable(value="implpath") String implpath)
			throws UserException {
		logger.info("Delete UDP request reached: subscriberId: " + msisdn
				+ " udpId: " + udpId +" implpath: "+implpath);

		IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		if(udpService == null){
			throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		}
		return udpService.deleteUDP(msisdn, udpId);

	}

	@RequestMapping(value = "/param/{shufflelistId}/{implpath}", method = RequestMethod.PUT)
	public Object updateUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "name", required = true) String name,
			@RequestParam(value = "extrainfo", required = true) String extrainfo,
			@RequestParam(value = "mode" , required = true, defaultValue="WAP") String mode,
			@PathVariable(value = "shufflelistId") String udpId, @PathVariable(value="implpath") String implpath)
			throws UserException {
		logger.info("Update UDP request reached: subscriberId: " + msisdn
				+ ",name: " + name + ", mode: " + mode + ", extrainfo: "
				+ extrainfo + " udpId: " + udpId +" implpath: "+implpath);

		IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		if(udpService == null){
			throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		}
		return udpService.updateUDP(msisdn, name, mode, extrainfo, udpId);

	}
	
	@RequestMapping(value = "/{shufflelistId}/{implpath}", method = RequestMethod.PUT)
	public Object updateUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestBody Shuffle shuffle,
			@PathVariable(value = "shufflelistId") String udpId, @PathVariable(value="implpath") String implpath)
			throws UserException {
		logger.info("Update UDP request reached: subscriberId: " + msisdn
				+ ",name: " + shuffle.getName() + ", extrainfo: "
				+ shuffle.getExtraInfo() + " udpId: " + udpId +" implpath: "+implpath);

		IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		if(udpService == null){
			throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		}
		return udpService.updateUDP(msisdn, shuffle.getName(), null, shuffle.getExtraInfo(), udpId);

	}

	@RequestMapping(value = "/{implpath}", method = RequestMethod.GET)
	public Object getAllUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn, @PathVariable(value="implpath") String implpath,
			@RequestParam(value = "offset", required = false, defaultValue = "-1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "-1") int pagesize)
			throws UserException {
		logger.info("Get UDP request reached: subscriberId: " + msisdn +" implpath: "+implpath);

		IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		if(udpService == null){
			throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		}
		return udpService.getAllUDP(msisdn, offset, pagesize);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/{shufflelistId}/song/{implpath}")
	public Object addContentToUDPVoltron(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestBody(required = true) Song song,
			@PathVariable(value = "shufflelistId") String udpId,
			@PathVariable(value="implpath") String implpath) throws UserException {
		logger.info("Add content to UDP request reached: subscriberId: " + msisdn
				+ " , udpId: "+ udpId
				+ ",requestBody: " + song.getId() +" and "+song.getType());

		IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		if(udpService == null){
			throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		}
		 return udpService.addContentToUDP(msisdn, udpId, song.getId()+"", song.getType().toString());

	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/{shufflelistId}/song/rbt/{implpath}")
	public Object addContentToUDPRBT(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "toneId") String toneId, @RequestParam(value="type") String type,
			@PathVariable(value = "shufflelistId") String udpId,
			@PathVariable(value="implpath") String implpath) throws UserException {
		logger.info("Add content to UDP request reached: subscriberId: " + msisdn
				+ " , udpId: "+ udpId
				+ ",toneId: " + toneId + "type: "+type);

		IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		if(udpService == null){
			throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		}
		 return udpService.addContentToUDP(msisdn, udpId, toneId, type);

	}
		
	@RequestMapping(method = RequestMethod.DELETE, value = "/{shufflelistId}/song/{toneId}/{implpath}")
	public Object deleteContentFromUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "ctype", required = true) String ctype,
			@PathVariable(value = "shufflelistId") String udpId,
			@PathVariable(value="toneId") String toneId,
			@PathVariable(value="implpath") String implpath) throws UserException {
		logger.info("Delete content from UDP request reached: subscriberId: " + msisdn
				+" udpId: "+ udpId + " toneId: "+toneId
				+ "implpath: "+implpath);

		 IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		 if(udpService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		 }
		 return udpService.deleteContentFromUDP(msisdn, udpId, toneId,ctype);

	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/{shufflelistId}/song/{implpath}")
	public Object getContentsFromUDP(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@PathVariable(value = "shufflelistId") String udpId,
			@PathVariable(value="implpath") String implpath,
			@RequestParam(value = "offset", required = false, defaultValue = "-1") int offset,
			@RequestParam(value = "pagesize", required = false, defaultValue = "-1") int pagesize) throws UserException {
		logger.info("Get contents from UDP request reached: subscriberId: " + msisdn
				 + "implpath: "+implpath);

		 IUDPService udpService = serviceResolver.getUDPServiceImpl(implpath);
		 if(udpService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
		 }
		 return udpService.getContentsFromUDP(msisdn , udpId, offset, pagesize);

	}
	
	
}
