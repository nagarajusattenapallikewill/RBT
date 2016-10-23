package com.onmobile.apps.ringbacktones.ussd.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class USSDSearchSessionManager {
	
	public static final Map<String, USSDSession> allSessions = new HashMap<String, USSDSession>();
	private static final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(); 
	
	static{
		SessionDeleteDaemon sessionDeleteDaemon=new SessionDeleteDaemon();
		sessionDeleteDaemon.startsessionDeletingDaemon();
	}
	/**
	 * Returns the session associated with the current subscriber
	 * @param subscriberId
	 * @return
	 */
	public static USSDSession getSearchSession(String subscriberId) {
		USSDSession session = null;
		readWriteLock.readLock().lock(); 
		session = allSessions.get(subscriberId);
		readWriteLock.readLock().unlock();
		//set the last access time of the current session
		session.setLastAccessedTime(System.currentTimeMillis());
		return session;
	}
	

	/**
	 * Returns the active session associated with the current subscriber
	 * @param subscriberId
	 * @return
	 */
	public static USSDSession getActiveSearchSession(String subscriberId) {
		USSDSession session = null;
		readWriteLock.readLock().lock(); 
		session = allSessions.get(subscriberId);
		if(null != session) {
			//check if the session is active - created in the last 5 minutes
//			long lastAccessTime = session.getLastAccessedTime();
			if( (System.currentTimeMillis() - session.getLastAccessedTime()) > 5*60*1000L) {
		        // upgrade lock manually
				readWriteLock.readLock().unlock();   // must unlock first to obtain write lock
				readWriteLock.writeLock().lock(); //acquire write lock
				allSessions.remove(subscriberId);
				readWriteLock.readLock().lock();  // re acquire read without giving up write lock
				readWriteLock.writeLock().unlock(); // unlock write, still hold read
				session = null;
			} else {
				//set the last access time of the current session
				session.setLastAccessedTime(System.currentTimeMillis());
			}
		}
		readWriteLock.readLock().unlock();
		return session;
	}

	/**
	 * Invalidates the current session associated with the current subscriber
	 * @param subscriberId
	 * @return
	 */
	public static boolean invalidateSearchSession(String subscriberId) {
		boolean result = false;
		readWriteLock.writeLock().lock(); //acquire write lock
		USSDSession session = allSessions.remove(subscriberId);
		readWriteLock.writeLock().unlock(); // unlock write
		if(null != session) {
			result = true;
		}
		return result;
	}
	
	/**
	 * Creates the session if the session is not existing already
	 * @param subscriberId
	 * @return true if the session does not exist and created now
	 * 		   false if the session exists already
	 */
	public static boolean createSearchSession(String subscriberId) {
		boolean result = false;
		readWriteLock.readLock().lock(); //acquire read lock
		USSDSession session = allSessions.get(subscriberId);
		if(null == session) {
	        // upgrade lock manually
			readWriteLock.readLock().unlock();   // must unlock first to obtain write lock
			readWriteLock.writeLock().lock(); //acquire write lock
			//no session. hence new user -> create session.
			USSDSession newSession = new USSDSession(subscriberId);
			allSessions.put(subscriberId, newSession);
			readWriteLock.readLock().lock();  // re acquire read without giving up write lock
			readWriteLock.writeLock().unlock(); // unlock write, still hold read
			result = true;
		}
		readWriteLock.readLock().unlock(); //release read lock 
		return result;
	}

}
