package com.onmobile.apps.ringbacktones.hunterFramework;

import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

public class HunterContainer
{
    private static HunterContainer hunterContainer = new HunterContainer();

    public static HunterContainer getHunterContainer()
    {
        return hunterContainer;
    }

    private HashMap<String, Hunter> hunters = new HashMap<String, Hunter>();
    public void registerHunter(Hunter hunter)
    {
        String hunterName = hunter.getHunterName();
        Hunter hunterOld = hunters.get(hunterName);
        if(hunterOld != null)
        {
            // Warning
        }
        hunters.put(hunterName, hunter);
    }
    
    public void unRegisterHunter(Hunter hunter)
    {
        String hunterName = hunter.getHunterName();
        hunters.remove(hunterName);
    }

    public void unRegisterAllHunter()
    {
        HashMap<String, Hunter> hunters = (HashMap<String, Hunter>) this.hunters.clone();
        Set<Entry<String, Hunter>> entries = hunters.entrySet();
        for (Entry<String, Hunter> entry : entries)
        {
            entry.getValue().unRegister();
        }
    }

    /**
	 * @return the hunters
	 */
	public HashMap<String, Hunter> getHunters()
	{
		return hunters;
	}
}
