package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class RBTDownloadFileRequest extends Request{

	
	private String bulkTaskFile = null;
	private String fileName = null;
	private String type = null;
	private boolean redirectionRequired = false;
	
	public RBTDownloadFileRequest(String subscriberID) {
		super(subscriberID);		
	}

	public String getBulkTaskFile() {
		return bulkTaskFile;
	}

	public void setBulkTaskFile(String bulkTaskFile) {
		this.bulkTaskFile = bulkTaskFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public boolean isRedirectionRequired() {
		return redirectionRequired;
	}

	public void setRedirectionRequired(boolean redirectionRequired) {
		this.redirectionRequired = redirectionRequired;
	}

	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (bulkTaskFile != null) requestParams.put(param_bulkTaskFile, bulkTaskFile);
		if (type != null) requestParams.put(param_type, type);
		if (fileName != null) requestParams.put(param_fileName, fileName);
		if (redirectionRequired) requestParams.put(param_redirectionRequired, "true");
		return requestParams;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.SelectionRequest#prepareRequestParams()
	 */
	@Override
	public void prepareRequestParams(WebServiceContext task)
	{
		super.prepareRequestParams(task);

		if(task.containsKey(param_bulkTaskFile)){
			bulkTaskFile = task.getString(param_bulkTaskFile);
		}
		if(task.containsKey(param_type)){
			type = task.getString(param_type).trim();			
		}
		if(task.containsKey(param_fileName)){
			fileName = task.getString(param_fileName);
		}
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		String superString = super.toString();
		superString = superString.substring(superString.indexOf('[') + 1);

		StringBuilder builder = new StringBuilder();
		builder.append("RBTDownloadFileRequest[bulkTaskFile = ");
		builder.append(bulkTaskFile);
		builder.append(", type = ");
		builder.append(type);
		builder.append(", fileName = ");
		builder.append(fileName);
		builder.append(", redirectionRequired = ");
		builder.append(redirectionRequired);
		builder.append(superString);
		return builder.toString();
	}
}
