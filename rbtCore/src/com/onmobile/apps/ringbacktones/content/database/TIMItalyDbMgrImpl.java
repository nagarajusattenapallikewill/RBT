package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;

public class TIMItalyDbMgrImpl extends VodafoneSpainDbMgrImpl{
	
	Logger log= Logger.getLogger(TIMItalyDbMgrImpl.class);
	
	@Override
	public SubscriberDownloads[] getNonDeactiveSubscriberDownloads(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			return SubscriberDownloadsImpl.getNonDeactiveAndTrackSubscriberDownloads(
					conn, subscriberId);
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	@Override
	public SubscriberDownloads[] getSubscriberDownloads(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getSubscriberDownloadsWithoutTrack(conn,
					subID(subscriberID));
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
	
	@Override
	public SubscriberDownloads[] getActiveSubscriberDownloads(
			String subscriberId) {
		Connection conn = getConnection();
		if (conn == null)
			return null;

		try {
			return SubscriberDownloadsImpl.getActiveSubscriberDownloadsWithoutTrack(conn,
					subID(subscriberId));
		} catch (Throwable e) {
			log.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}
}