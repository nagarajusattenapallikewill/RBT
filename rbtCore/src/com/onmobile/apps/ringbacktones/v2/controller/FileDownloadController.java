package com.onmobile.apps.ringbacktones.v2.controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.service.IFileDownloadService;

@RestController
@RequestMapping(value = "/utils/fileDownload")
public class FileDownloadController {
	
	Logger logger = Logger.getLogger(FileDownloadController.class);

	/*
	 * Once get the multipart request from client, then controller will invoke this api to handle multipart request.
	 * To support multipart request, multipartResolver bean to be configured in spring xml.
	 */
	@RequestMapping(value = "/{type}", method = RequestMethod.POST)
	public Object fileDownload(@RequestParam(value= "subscriberId", required = true) long msisdn, @PathVariable(value = "type") String type,
			@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "fileName") String fileName, 
			@RequestParam(value = "mode", required = false, defaultValue = "RBT") String mode) throws UserException{
		
		logger.info("FileDownload received request:: subscriberId: " + msisdn + ", type: " + type + ", mode: " + mode + ", fileName: " + fileName);
		
		IFileDownloadService donwloadService = null;
		try {
			donwloadService = (IFileDownloadService) ConfigUtil.getBean(BeanConstant.FILE_DOWNLOAD_SERVICE + type.toLowerCase());
		}
		catch(Exception e){}
		
		 if(donwloadService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
			}
		 Object object = donwloadService.downloadFile(msisdn, mode, file, fileName);
		 logger.debug("FileDownload respond:: " + object);
		 return object;
	}
	
	/*
	 * API is for test api.
	 */
	@Deprecated
	@RequestMapping(value = "/{type}/test", method = RequestMethod.POST)
	public Object fileDownloadTest(@RequestParam(value= "operator", required = true) String msisdn,
			@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "filename") String fileName, 
			@RequestParam(value = "mode", required = false, defaultValue = "RBT") String mode) throws UserException{
		
		logger.info("FileDownload received request:: subscriberId: " + msisdn + ", mode: " + mode + ", fileName: " + fileName);
		
		logger.info("Received call fileName: " + fileName);
		
		fileName = fileName + "test";
		
		if (!file.isEmpty()) {
			BufferedOutputStream stream = null;
			try {
				File newFile = new File("/opt/Tomcat/upload" + File.separator + fileName);
				byte[] bytes = file.getBytes();
				stream = new BufferedOutputStream(
						new FileOutputStream(newFile));
				stream.write(bytes);
				stream.close();
				logger.info("Successfully file stored filePaht: " + newFile.getAbsolutePath());
				if("vodafone".equalsIgnoreCase(msisdn)) {
					throw new Exception("Invalid msisdn");
				}
				Map<String, String> map = new HashMap<String,String>();
				map.put("status","success");
				return map;
			} catch (Exception e) {
				//ToDo throw UserException
			}
			finally{
				try{
					if(stream != null) {
						stream.close();
					}
				}
				catch(Exception e){}
			}
		} else {
			//Doto throw UserException
		}
		Map<String, String> map = new HashMap<String,String>();
		map.put("code","INVALID_PARAMETER");
		map.put("sub_code", "NA");
		map.put("summary", "Summary");
		map.put("description", "Discription");
		return map;
	}
	
	
}
