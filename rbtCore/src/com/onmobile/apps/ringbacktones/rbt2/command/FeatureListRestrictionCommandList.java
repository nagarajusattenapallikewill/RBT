package com.onmobile.apps.ringbacktones.rbt2.command;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.onmobile.apps.ringbacktones.v2.exception.RestrictionException;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

@Repository(value="featureListRestrictionCommandList")
public class FeatureListRestrictionCommandList {
	
	private Map<String, List<FeatureListRestrictionCommand>>  commandList = null;

	protected Map<String, List<FeatureListRestrictionCommand>> getCommandList() {
		return commandList;
	}

	public void setCommandList(
			Map<String, List<FeatureListRestrictionCommand>> commandList) {
		this.commandList = commandList;
	}
	
	public String executeCallbackCommands(String msisdn, String srvKey) {
		List<FeatureListRestrictionCommand> commands = getCommandList().get(srvKey);
		if(commands != null){
			for(FeatureListRestrictionCommand command : commands) {
				command.executeCalback(msisdn);
			}
		}
		return null;
	}
	
	
	public String executeInlineCallCommands(SelectionRequest selectionRequest, String srvKey, String clipID) throws RestrictionException {
		List<FeatureListRestrictionCommand> commands = getCommandList().get(srvKey);
		if(commands != null){
			for(FeatureListRestrictionCommand command : commands) {
				command.executeInlineCall(selectionRequest,clipID);
			}	
		}
		return null;
	}
}
