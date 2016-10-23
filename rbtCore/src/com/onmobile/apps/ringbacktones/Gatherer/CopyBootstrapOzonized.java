package com.onmobile.apps.ringbacktones.Gatherer;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterContainer;
import com.onmobile.apps.ringbacktones.hunterFramework.ThreadManager;
import com.onmobile.common.cjni.O3InterfaceHelper;

public class CopyBootstrapOzonized extends Ozonized
{
	public static final String COMPONENT_NAME = "RBTCopyProcessor";
	private static O3InterfaceHelper o3InterfaceHelper = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int initComponent(O3InterfaceHelper o3InterfaceHelper)
	{
		CopyBootstrapOzonized.o3InterfaceHelper = o3InterfaceHelper;
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public int startComponent()
	{
		CopyBootstrap copyBootstrap = new CopyBootstrap();
		copyBootstrap.start();
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		// TODO : make sure all threads go down
		HunterContainer.getHunterContainer().unRegisterAllHunter();
		ThreadManager.getThreadManager().releaseAllThreads();
	}

	public static O3InterfaceHelper getO3InterfaceHelper()
	{
		return o3InterfaceHelper;
	}
}
