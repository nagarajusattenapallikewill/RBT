package com.onmobile.apps.ringbacktones.test.unitTest.callbacks;

import com.onmobile.apps.ringbacktones.interfaces.sm.callback.core.ServiceProvider;
import com.onmobile.apps.ringbacktones.service.configuration.ConfigurationService;
import com.onmobile.apps.ringbacktones.service.configuration.FileConfigProvider;
import com.onmobile.apps.ringbacktones.service.dblayer.DBService;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtPickOfTheDayDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtProvisioningRequestDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtSubscriberDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtSubscriberDownloadsDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtSubscriberSelectionDao;
import com.onmobile.apps.ringbacktones.service.dblayer.dao.RbtViralSmsDao;

import static org.mockito.Mockito.*;

public class EnvironmentInitializer
{
	public static void init()
	{
		DBService mockedDbService = mock(DBService.class);
		ServiceProvider.setDbService(mockedDbService);
		
		when(mockedDbService.getRBTPickOfTheDayDao()).thenReturn(mock(RbtPickOfTheDayDao.class));
		when(mockedDbService.getRbtProvisioningRequestDao()).thenReturn(mock(RbtProvisioningRequestDao.class));
		when(mockedDbService.getRbtSubscriberDao()).thenReturn(mock(RbtSubscriberDao.class));
		when(mockedDbService.getRbtSubscriberDownloadsDao()).thenReturn(mock(RbtSubscriberDownloadsDao.class));
		when(mockedDbService.getRbtSubscriberSelectionDao()).thenReturn(mock(RbtSubscriberSelectionDao.class));
		when(mockedDbService.getRbtViralSmsDao()).thenReturn(mock(RbtViralSmsDao.class));
		 
		ConfigurationService.setFileConfigProvider(new FileConfigProvider());
		
		//ConfigurationService.
		/*ServiceProvider.setDbService(dbService);
		ServiceProvider.setRbtCacheManager(rbtCacheManager);
		ServiceProvider.setMtSMSService(mtSmsService);
		ServiceProvider.setCacheManagerUtil(cacheManagerUtil);
		*/
	}
}
