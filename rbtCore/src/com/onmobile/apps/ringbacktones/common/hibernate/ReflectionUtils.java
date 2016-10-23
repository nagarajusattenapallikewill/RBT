/**
 * 
 */
package com.onmobile.apps.ringbacktones.common.hibernate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

/**
 * @author vinayasimha.patil
 * 
 */
public class ReflectionUtils
{
	public static Type getType(String typeName) throws ClassNotFoundException
	{
		typeName = typeName.trim();
		int index = typeName.indexOf('<');
		if (index > 0)
		{
			String rawTypeName = typeName.substring(0, index);
			Class<?> rawType = Class.forName(rawTypeName);

			List<Type> types = new ArrayList<Type>();
			String typeArgumentNames = typeName.substring(index + 1,
					typeName.length() - 1);
			String[] typeArguments = getTypeArguments(typeArgumentNames);
			for (String typeArgumentName : typeArguments)
			{
				Type typeArgument = getType(typeArgumentName);
				types.add(typeArgument);
			}

			return ParameterizedTypeImpl.make(rawType,
					types.toArray(new Type[0]), null);
		}
		else
			return Class.forName(typeName);
	}

	private static String[] getTypeArguments(String typeArguments)
	{
		List<String> typeArgumentNames = new ArrayList<String>();
		boolean foundAngularBracket = false;

		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < typeArguments.length(); i++)
		{
			char ch = typeArguments.charAt(i);
			switch (ch)
			{
				case '<':
					foundAngularBracket = true;
					builder.append(ch);
					break;

				case '>':
					foundAngularBracket = false;
					builder.append(ch);
					break;

				case ',':
					if (foundAngularBracket)
						builder.append(ch);
					else
					{
						typeArgumentNames.add(builder.toString());
						builder = new StringBuilder();
					}
					break;

				default:
					builder.append(ch);
					break;
			}
		}
		typeArgumentNames.add(builder.toString());

		return typeArgumentNames.toArray(new String[0]);
	}

	public static boolean isCollectionType(Type type)
	{
		Class<?> rawType = null;
		if (type instanceof ParameterizedType)
			rawType = (Class<?>) ((ParameterizedType) type).getRawType();
		else
			rawType = (Class<?>) type;

		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(rawType);
		Collections.addAll(list, rawType.getInterfaces());

		if (list.contains(Map.class)
				|| list.contains(NavigableMap.class)
				|| list.contains(Set.class)
				|| list.contains(NavigableSet.class)
				|| list.contains(List.class))
			return true;

		return false;
	}

	public static boolean isMapType(Type type)
	{
		Class<?> rawType = null;
		if (type instanceof ParameterizedType)
			rawType = (Class<?>) ((ParameterizedType) type).getRawType();
		else
			rawType = (Class<?>) type;

		List<Class<?>> list = new ArrayList<Class<?>>();
		list.add(rawType);
		Collections.addAll(list, rawType.getInterfaces());

		if (list.contains(Map.class) || list.contains(NavigableMap.class))
			return true;

		return false;
	}
}
