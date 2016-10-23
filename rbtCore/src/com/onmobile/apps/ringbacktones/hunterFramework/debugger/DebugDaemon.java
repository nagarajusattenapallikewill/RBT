package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon;
import com.onmobile.apps.ringbacktones.hunterFramework.ThreadManager;

/**
 * DebugDaemon is a {@link ManagedDaemon}. It accepts the requests from the
 * Debug
 * CLI and handles the request to {@link DataHandler}
 * 
 * @author vinayasimha.patil
 */
public class DebugDaemon extends ManagedDaemon
{
	private static Logger logger = Logger.getLogger(DebugDaemon.class);
	/**
	 * Holds the reference of DebugDaemon. This is used make this class as
	 * singleton.
	 */
	private static DebugDaemon debugDaemon = null;

	/**
	 * The port number where server socket listening.
	 */
	private int portNo = 0;

	/**
	 * ServerSocket instance of DebugDaemon.
	 */
	ServerSocket serverSocket = null;

	/**
	 * Constructs DebugDaemon object
	 * 
	 * @param portNo
	 *            the port number for opening the Server Socket
	 * @throws IOException
	 *             If IO error occurs when opening the socket
	 */
	private DebugDaemon(int portNo) throws IOException
	{
		this.portNo = portNo;
		setUniqueName("DebugDaemon-" + portNo);
		init();
	}

	/**
	 * Initializes the Server Socket on port <tt>portNo</tt> and registers this
	 * daemon in {@link ThreadManager}.
	 * 
	 * @throws IOException
	 *             If IO error occurs when opening the server socket
	 */
	private void init() throws IOException
	{
		serverSocket = new ServerSocket(portNo);
		ThreadManager.getThreadManager().addManagedThread(this);
	}

	/**
	 * Starts the debug daemon and returns the reference for the same. If the
	 * daemon has already started, then returns the reference for the already
	 * running daemon. If the port is not configured, then the daemon will not
	 * be started and the <tt>null</tt> reference will be returned.
	 * 
	 * @return the DebugDaemon instance
	 * @throws IOException
	 *             If IO error occurs when opening the server socket
	 */
	public static DebugDaemon startDebugDemon() throws IOException
	{
		return startDebugDemon(iRBTConstant.GATHERER, "debugDeamon.port");
	}

	public static DebugDaemon startDebugDemon(String type, String paramName) throws IOException
	{
		if (debugDaemon == null)
		{
			synchronized (DebugDaemon.class)
			{
				if (debugDaemon == null)
				{
					ParametersCacheManager parametersCacheManager = CacheManagerUtil
							.getParametersCacheManager();
					Parameters portNoParam = parametersCacheManager
							.getParameter(type,
									paramName);
					if (portNoParam != null && portNoParam.getValue() != null)
					{
						int portNo = Integer.parseInt(portNoParam.getValue());
						debugDaemon = new DebugDaemon(portNo);
						logger.info("RBT:: DebugDaemon started on port: " + portNo);
					}
				}
			}
		}

		return debugDaemon;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon#execute()
	 */
	/**
	 * Accepts the client request and handles the request to {@link DataHandler}
	 */
	@SuppressWarnings("unused")
	@Override
	protected void execute()
	{
		try
		{
			Socket socket = serverSocket.accept();
			new DataHandler(socket);
		}
		catch (IOException e)
		{
			logger.error("", e);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.hunterFramework.ManagedDaemon#getLockObject
	 * ()
	 */
	@Override
	public Object getLockObject()
	{
		return null;
	}
}
