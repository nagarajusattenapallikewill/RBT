package com.onmobile.apps.ringbacktones.v2.dao;

import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberDetails;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;

public interface IMigrateUser {

	public String migrateUser(RBTSubscriberDetails rbtSubscriberDetails) throws UserException;

}
