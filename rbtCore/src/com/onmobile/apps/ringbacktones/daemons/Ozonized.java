/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import org.w3c.dom.Node;

import com.onmobile.common.cjni.IJavaComponent;
import com.onmobile.common.cjni.O3InterfaceHelper;
import com.onmobile.common.message.O3Message;

/**
 * @author vinayasimha.patil
 * 
 */
public abstract class Ozonized implements IJavaComponent
{
	@Override
	public abstract String getComponentName();
	
	@Override
	public int initComponent(O3InterfaceHelper o3InterfaceHelper)
	{
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public int configureComponent(Node node)
	{
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public abstract int startComponent();

	@Override
	public abstract void stopComponent();

	@Override
	public int suspendComponent()
	{
		return JAVA_COMPONENT_NOTIMPLEMENTED;
	}

	@Override
	public int isSuspendCompleted()
	{
		return JAVA_COMPONENT_NOTIMPLEMENTED;
	}

	@Override
	public int resumeComponent()
	{
		return JAVA_COMPONENT_NOTIMPLEMENTED;
	}

	@Override
	public String getKey()
	{
		return "";
	}

	@Override
	public String getSubKey()
	{
		return "";
	}

	@Override
	public String getBuildComment()
	{
		return "";
	}

	@Override
	public String getBuildVersion()
	{
		return "";
	}

	@Override
	public String getBuildDate()
	{
		return "";
	}

	@Override
	public String getBuildTime()
	{
		return "";
	}

	@Override
	public void processMessage(O3Message o3Message)
	{

	}
}
