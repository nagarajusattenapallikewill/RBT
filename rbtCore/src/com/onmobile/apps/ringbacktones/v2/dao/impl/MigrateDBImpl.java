package com.onmobile.apps.ringbacktones.v2.dao.impl;

import com.onmobile.apps.ringbacktones.v2.dao.IMigrateUser;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberDetails;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.resolver.request.impl.MigrationRequestResolver;

public class MigrateDBImpl implements IMigrateUser {

	@Override
	public String migrateUser(RBTSubscriberDetails rbtSubscriberDetails) throws UserException {

		MigrationRequestResolver migrationRequestResolver = new MigrationRequestResolver();
		return migrationRequestResolver.migrateUser(rbtSubscriberDetails);

	}

}
