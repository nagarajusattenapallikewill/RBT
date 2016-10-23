package com.onmobile.apps.ringbacktones.activemonitoring.core;

import com.onmobile.apps.ringbacktones.activemonitoring.common.ActiveMonitoringException;

public final class ObjectName 
{
	private static final String PATTERN_ATTRIBUTE = "[-A-Za-z0-9.=,()@\\-]+";
	private static final String PATTERN_COMPONENT = "[-A-Za-z0-9.()]+";
	private final String module;
	private final String component;
	private final String attribute;
	private String simonName;
    
	public ObjectName(final String module, final String component, final String attribute) throws ActiveMonitoringException
	{
//		if (module == null || component == null || attribute == null) throw new ActiveMonitoringException("Module, component or attribute is null");
//		
//		if (!module.matches(PATTERN_COMPONENT)) throw new IllegalArgumentException("Module should match the pattern " + PATTERN_COMPONENT + " Used: " + module);
//		if (!component.matches(PATTERN_COMPONENT)) throw new IllegalArgumentException("Component should match the pattern " + PATTERN_COMPONENT + " Used: " + component);
//		if (!attribute.matches(PATTERN_ATTRIBUTE)) throw new IllegalArgumentException("Attribute should match the pattern " + PATTERN_ATTRIBUTE + " Used: " + attribute);
//		if (!module.startsWith("com.onmobile.apps.ringbacktones.")) throw new IllegalArgumentException("Component should start with com.onmobile.apps.ringbacktones. Used: " + component);
		this.module = module;
		this.component = component;
		this.attribute = attribute;
		createSimonName();
	}

	private void createSimonName() 
	{
		simonName = module + "." + component + "." + attribute;
	}

	public String getModule() 
	{
		return module;
	}

	public String getComponent() 
	{
		return component;
	}

	public String getAttribute() 
	{
		return attribute;
	}

	public String getSimonName() 
	{
		return simonName;
	}

    public static void main(String[] args) throws ActiveMonitoringException
    {

    }

}
