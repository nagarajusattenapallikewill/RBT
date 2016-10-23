package com.onmobile.apps.ringbacktones.daemons.interfaces;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Groups;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

/**
 * Interface for Player-Updater (interface implementation Ex.Voda Romania).
 * @author Sreekar
 * @Date 2009-01-02
 * 
 * @edited Sreekar, added groups feature
 * @Date 2009-02-23
 */
public interface iPlayerModel {
	public static final String SUCCESS = "SUCCESS";
	public static final String FAILURE = "FAILURE";
	public static final String TECHNICAL_DIFFICULTIES = "TECHNICAL_DIFFICULTIES";
	public static final String ERROR = "ERROR";
	
	//Task Types
	public static final int TASK_UPDATE_SUBSCRIBER = 1;
	public static final int TASK_UPDATE_SETTING = 2;
	public static final int TASK_DELETE_SUBSCRIBER = 3;
	public static final int TASK_DELETE_SETTING = 4;
	public static final int TASK_DELETE_DOWNLOAD = 5;
	public static final int TASK_ADD_GROUP = 6;
	public static final int TASK_EDIT_GROUP = 7;
	public static final int TASK_DELETE_GROUP = 8;
	public static final int TASK_ADD_GROUP_MEMBER = 9;
	public static final int TASK_EDIT_GROUP_MEMBER = 10;
	public static final int TASK_DELETE_GROUP_MEMBER = 11;

	/**
	 * Updates subscriber configuration at player
	 * 
	 * @param subscriber to be updated @ player
	 * @return status of the update request success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String updateSubscriber(Subscriber subscriber) throws RBTException;
	/**
	 * Updates/adds setting at player
	 * 
	 * @param setting to be updated/added @ player
	 * @return status of the request if success/failure	
	 * @throws RBTException if any error occurs while processing
	 */
	public String updateSetting(SubscriberStatus setting) throws RBTException;
	/**
	 * Removes a subscriber from player
	 * 
	 * @param subscriber to be removed from player after deactivation/suspension
	 * @return status of the request if success/failure	
	 * @throws RBTException if any error occurs while processing
	 */
	public String deleteSubscriber(Subscriber subscriber) throws RBTException;
	/**
	 * Removes a setting from player
	 * 
	 * @param setting to be removed from player
	 * @return status of the request if success/failure	
	 * @throws RBTException if any error occurs while processing
	 */
	public String deleteSetting(SubscriberStatus setting) throws RBTException;
	/**
	 * Removes a download from player
	 * 
	 * @param download to be removed from player
	 * @return status of the request if success/failure	
	 * @throws RBTException if any error occurs while processing
	 */
	public String deleteDownload(SubscriberDownloads download) throws RBTException;
	/**
	 * Adds a group in player
	 * 
	 * @param group to be added in player
	 * @return status of the request if success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String addGroup(Groups group) throws RBTException;
	/**
	 * Removes a group from player
	 * 
	 * @param group to be removed from player
	 * @return status of the request if success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String deleteGroup(Groups group) throws RBTException;
	/**
	 * Edits a group already added in player from group name etc.
	 * 
	 * @param group to be edited in player
	 * @return status of the request if success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String editGroup(Groups group) throws RBTException;
	/**
	 * Adds a member to an already added group
	 * 
	 * @param group member to be added in player
	 * @return status of the request if success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String addGroupMember(GroupMembers groupMember) throws RBTException;
	/**
	 * Edits an existing member of a group for name etc
	 * 
	 * @param group member to be edited in player
	 * @return status of the request if success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String editGroupMember(GroupMembers groupMember) throws RBTException;
	/**
	 * Removes an existing member of a group
	 * 
	 * @param group member to be removed from player
	 * @return status of the request if success/failure
	 * @throws RBTException if any error occurs while processing
	 */
	public String deleteGroupMember(GroupMembers groupMember) throws RBTException;
}