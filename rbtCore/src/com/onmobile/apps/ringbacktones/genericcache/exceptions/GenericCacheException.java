package com.onmobile.apps.ringbacktones.genericcache.exceptions;

public class GenericCacheException extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 167456389654378L;

	public GenericCacheException()
	{

	}

	public GenericCacheException(String msg)
	{
		super(msg);
	}

	public GenericCacheException(Throwable th)
	{
		super(th);
	}

	public GenericCacheException(String msg, Throwable th)
	{
		super(msg, th);
	}
}
