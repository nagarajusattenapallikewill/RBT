package com.onmobile.apps.ringbacktones.daemons.inline;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.daemons.RBTPlayerUpdateDaemon;

@Component
public class TPInlineHelper extends RBTPlayerUpdateDaemon {
	private static Logger logger = Logger.getLogger(TPInlineHelper.class);
	
	public TPInlineHelper() {
		super(null);
	}

	public static boolean addSelectionsToplayer(Subscriber subscriber) {
		try {
			return sendUserXMLToPlayer(subscriber, ACTION_TYPE_ADD_SEL,
					UPDATE_TYPE_BOTH);
		} catch (Exception e) {
			logger.error("", e);
		}
		return false;
	}

	public static boolean removeSelectionsFromplayer(Subscriber subscriber) {
		try {
			return sendUserXMLToPlayer(subscriber, ACTION_TYPE_REMOVE_SEL,
					UPDATE_TYPE_BOTH);
		} catch (Exception e) {
			logger.error("", e);
		}
		return false;
	}
}
