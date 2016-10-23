package com.onmobile.apps.ringbacktones.content.database;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

public class IdeaDbMgrImpl extends VirginDbMgrImpl {
	public IdeaDbMgrImpl() throws ParserConfigurationException {
		super();
	}

	@Override
	public boolean isFirstProfileSong(List<String> overridableSelectionStatus,
			int status, int selcount) {
		boolean result = false;
		if (selcount == 0 && status == 99) {
			result = false;
		} else if (overridableSelectionStatus.contains("" + status)) {
			result = true;
		}
		return result;
	}

}
