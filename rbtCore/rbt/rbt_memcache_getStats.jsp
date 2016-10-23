<%@ page
	import="com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters,com.danga.MemCached.*,org.json.*,java.util.*,java.io.*"%>

<% 
	String servername = RBTContentJarParameters.getInstance().getParameter("memcached_serverlist"); 
		String strIP = request.getRemoteAddr();
		if(servername==null || servername.equals(""))
		{
			response.setStatus(403);
			out.write("Memcache Server Details are NOT configured");
		}
		else
		{
			session.setAttribute("id","onmobile");
			String[] server_list = servername.split(",");
			MemCachedClient mcc = new MemCachedClient();
			SockIOPool pool = SockIOPool.getInstance();
			pool.setServers(server_list);
			pool.initialize();
			Map stats = mcc.stats();
			if (stats.isEmpty())
			{
				out.println("Memcached server not initialized !!!");
			}
			 response.setContentType("application/json");
			Set servers = stats.keySet();
			JSONObject serverJson = new JSONObject();
			for(Iterator i=servers.iterator();i.hasNext(); )
			{
				String key = (String)i.next();
				JSONObject json = new JSONObject();
				Map my_stats = (Map)stats.get(key);
	
				Set keyset = my_stats.keySet();
				for(Iterator i1=keyset.iterator();i1.hasNext(); )
				{
					String key1 = (String)i1.next();
					json.put(key1,my_stats.get(key1));
				}
				serverJson.put(key,json);
			}
			out.println(serverJson);
			pool.shutDown();
		}
%>
