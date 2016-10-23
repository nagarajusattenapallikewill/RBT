/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.esia;

import java.util.ArrayList;
import java.util.List;

import com.onmobile.apps.ringbacktones.content.ChargePromoTypeMap;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClassMap;

/**
 * @author vinayasimha.patil
 *
 */
public class EsiaUtility
{
	private static ChargePromoTypeMap[] chargePromoTypeMaps = null;

	public static ChargeClassMap[] getChargeClassMapsForFinalClassType(
			String finalClassType, String accessedMode)
	{
		List<ChargeClassMap> chargeClassMapsList = CacheManagerUtil.getChargeClassMapCacheManager().getAllChargeClassMap();
		if (chargeClassMapsList == null || chargeClassMapsList.size() == 0)
			return null;

		List<ChargeClassMap> list = new ArrayList<ChargeClassMap>();

		for (ChargeClassMap chargeClassMap : chargeClassMapsList)
		{
			if (finalClassType.equals(chargeClassMap.getFinalClasstype()) && accessedMode.equals(chargeClassMap.getAccessMode()))
				list.add(chargeClassMap);
		}

		if (list.size() > 0)
			return (list.toArray(new ChargeClassMap[0])); 

		return null;
	}

	public static String[] getChargeOptModel(ChargeClassMap chargeClassMaps[])
	{
		String[] regexClasses = null;

		if (chargeClassMaps != null)
		{
			for (ChargeClassMap chargeClassMap : chargeClassMaps)
			{
				String value = chargeClassMap.getRegexSmsorVoice().trim();

				String[] chargeClasses = value.split("\\s");
				if (regexClasses == null || ((chargeClasses.length > regexClasses.length)))
					regexClasses = chargeClasses;    				
			}
		}

		return regexClasses;
	}

	public static ChargePromoTypeMap[] getChargePromoTypeMaps()
	{
		if (chargePromoTypeMaps == null)
		{
			chargePromoTypeMaps = RBTDBManager.getInstance().getChargePromoTypeMapsForLevelAndType("VUI", 2, "SEL");
			if (chargePromoTypeMaps == null)
				chargePromoTypeMaps = new ChargePromoTypeMap[0];

			ChargePromoTypeMap chargePromoTypeMap = null;
			for (int i = 0; i < chargePromoTypeMaps.length; i++)
			{
				for (int j = i; j < chargePromoTypeMaps.length; j++)
				{
					if (chargePromoTypeMaps[i].voiceOrder() > chargePromoTypeMaps[j].voiceOrder())
					{
						chargePromoTypeMap = chargePromoTypeMaps[i];
						chargePromoTypeMaps[i] = chargePromoTypeMaps[j];
						chargePromoTypeMaps[j] = chargePromoTypeMap;
					}
				}
			}
		}

		return chargePromoTypeMaps;
	}
}
