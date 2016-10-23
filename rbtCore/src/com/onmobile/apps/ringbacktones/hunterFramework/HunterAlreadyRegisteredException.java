package com.onmobile.apps.ringbacktones.hunterFramework;

public class HunterAlreadyRegisteredException extends HunterException
{
    public HunterAlreadyRegisteredException(String hunterName)
    {
        super("The hunter with name '"+hunterName+"' is already registred.");
    }

}
