package com.onmobile.apps.ringbacktones.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class MapUtils {

	private static Logger logger = Logger.getLogger(MapUtils.class);

	/**
	 * Converts the given string to map. For ex: If input string is
	 * "ABC=1,2;DEF=2,3;XYZ=9,10;" will be convert to 
	 * {3=DEF, 2=DEF, 10=XYZ, 1=ABC, 9=XYZ}
	 * 
	 * @param delimiter1
	 *            First delimiter split the given string(;)
	 * @param delimiter2
	 *            Second delimiter split the given string(=)
	 * @param delimiter3
	 *            Third delimiter split the given string(,)
	 * 
	 * @return to key value pair map. for ex: {3=DEF, 2=DEF, 10=XYZ, 1=ABC,
	 *         9=XYZ}
	 */
	public static Map<String, String> convertToMap(String str,
			String delimiter1, String delimiter2, String delimiter3) {
		logger.debug("Converting str: " + str + ", delimeter1: " + delimiter1
				+ ", delimeter2: " + delimiter2 + ", delimeter3: " + delimiter3);
		Map<String, String> map = new HashMap<String, String>();
		if (null == str || null == delimiter1) {
			logger.warn("Returning empty map for str: "+str);
			return map;
		}
		List<String> list = ListUtils.convertToList(str, delimiter1);
		for (String s : list) {
			int i = 0;
			if (null != delimiter2) {
				String[] keyValuePair = StringUtils
						.toStringArray(s, delimiter2);
				if (keyValuePair.length > 1) {
					String key = keyValuePair[0];
					String value = keyValuePair[1];

					if (null != delimiter3) {
						String[] values = StringUtils.toStringArray(value,
								delimiter3);
						if (null != values) {
							for (String val : values) {
								map.put(val, key);
							}
						}
					} else {
						map.put(key, value);
					}
				}
			} else {
				map.put(String.valueOf(i), s);
				i++;
			}
		}
		logger.info("Converted given string: " + str + ", to map: " + map);
		return map;
	}

	/*
	 * 1,2,3:AZAAN;4,12,13,14:COPTIC;101,24,67,78:DOAA
	 * (1=AZAAN , 2=AZAAN , 3=AZAAN)
	 * First delimiter = (;)
	 * Second delimiter = (:)
	 * Third delimiter = (,)
	 */
	public static Map<String, String> convertIntoMap(String str,
			String delimiter1, String delimiter2, String delimiter3) {
		logger.debug("Converting str to map: " + str + ", delimeter1: " + delimiter1
				+ ", delimeter2: " + delimiter2 + ", delimeter3: " + delimiter3);
		Map<String, String> map = new HashMap<String, String>();
		if (null == str || null == delimiter1) {
			logger.warn("Returning empty map for str: "+str);
			return map;
		}
		List<String> list = ListUtils.convertToList(str, delimiter1);
		System.out.println(list); 
		for (String s : list) {
			int i = 0;
			if (null != delimiter2) {
				String[] keyValuePair = StringUtils
						.toStringArray(s, delimiter2);
				System.out.println("key = "+keyValuePair); 
				if (keyValuePair.length > 1) {
					String key = keyValuePair[0];
					String value = keyValuePair[1];
                    System.out.println("key = "+key+",value="+value); 
					if (null != delimiter3) {
						String[] keys = StringUtils.toStringArray(key,
								delimiter3);
						if (null != keys) {
							for (String key1 : keys) {
								map.put(key1, value);
							}
						}
					} else {
						map.put(key, value);
					}
				}
			} else {
				map.put(String.valueOf(i), s);
				i++;
			}
		}
		logger.info("Converted the given string : " + str + ", to map: " + map);
		return map;
	}
	
	
	public static Map<String, List<String>> convertMapList(String str,
			String delimiter1, String delimiter2, String delimiter3) {
		logger.debug("Converting str: " + str + ", delimeter1: " + delimiter1
				+ ", delimeter2: " + delimiter2 + ", delimeter3: " + delimiter3);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		if (null == str || null == delimiter1) {
			logger.warn("Returning empty map for str: "+str);
			return map;
		}
		List<String> list = ListUtils.convertToList(str, delimiter1);
		for (String s : list) {
			if (null != delimiter2) {
				String[] keyValuePair = StringUtils
						.toStringArray(s, delimiter2);
				if (keyValuePair.length > 1) {
					String key = keyValuePair[0];
					String value = keyValuePair[1];
					if (null != delimiter3) {
						String[] values = StringUtils.toStringArray(value,
								delimiter3);
						if (null != values) {
								map.put(key, Arrays.asList(values));
						}
					} else {
						map.put(key, Arrays.asList(value));
					}
				}
			}
		}
		logger.info("Converted the given string : " + str + ", to map: " + map);
		return map;
	}
	

	/**
	 * Converts the given string to map. For ex: If input string is
	 * "ABC=1,2;DEF=2,3;XYZ=9,10;" will be convert to 
	 * {1=ABC, 2=ABC,DEF, 3=DEF,  10=XYZ,  9=XYZ}
	 * 
	 * @param delimiter1
	 *            First delimiter split the given string(;)
	 * @param delimiter2
	 *            Second delimiter split the given string(=)
	 * @param delimiter3
	 *            Third delimiter split the given string(,)
	 * 
	 * @return to key value pair map. for ex: {1=ABC, 2=ABC,DEF, 3=DEF,  10=XYZ,  9=XYZ}
	 */
	public static Map<String, List<String>> convertToMapList(String str,
			String delimiter1, String delimiter2, String delimiter3) {
		logger.debug("Converting str: " + str + ", delimeter1: " + delimiter1
				+ ", delimeter2: " + delimiter2 + ", delimeter3: " + delimiter3);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		if (null == str || null == delimiter1) {
			logger.warn("Returning empty map for str: "+str);
			return map;
		}
		List<String> list = ListUtils.convertToList(str, delimiter1);
		for (String s : list) {
			int i = 0;
			if (null != delimiter2) {
				String[] keyValuePair = StringUtils
						.toStringArray(s, delimiter2);
				if (keyValuePair.length > 1) {
					String key = keyValuePair[0];
					String value = keyValuePair[1];

					List<String> valList=null;
					if(map.containsKey(key)){
						 valList=map.get(key);
						
					
						if (null != delimiter3) {
							String[] values = StringUtils.toStringArray(value,
									delimiter3);
							if (null != values) {
								List<String> delimValues=Arrays.asList(values);
								valList.addAll(delimValues);
								map.put(key, valList);
							}
						} else {
							valList.add(value);
							map.put(key, valList);
						}
					}else{
						valList = new ArrayList<String>();
						valList.add(value);
						map.put(key, valList);
					}
				}
			} else {
				map.put(String.valueOf(i), ListUtils.convertToList(s,null));
				i++;
			}
		}
		logger.info("Converted given string: " + str + ", to map: " + map);
		return map;
	}
	
	public static void main(String[] args) {
		//String str = "ABC=1,2;DEF=4,3;XYZ=9,10;";
		String str = "ABC = 1 ; DEF = 4;XYZ = 9;";
		Map<String, String> map = convertIntoMap(str, ";", "=", null);
		System.out.println(" map: " + map);
		System.out.println(" map1: " + map.get("ABC"));
	}

}
