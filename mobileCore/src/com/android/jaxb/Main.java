package com.android.jaxb;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class Main{
	
	public static void main(String[] args) {
		Main m = new Main();
		m.temp();
	}
	
	public void temp(){
		List<CCC> c = new ArrayList<CCC>();
		CCC c1 = new CCC();
		c1.id="1";
		c1.name="some";
		Map<String, String> m = new HashMap<String, String>();
		m.put("x","y");
		m.put("a","b");
		c1.clipMap = m;
		
		CCC c2 = new CCC();
		c2.id="1";
		c2.name="some";
		c2.clipMap = m;
		c.add(c1);
		c.add(c2);
		
		
		Gson gson = new Gson();
		String json = gson.toJson(c);
		System.out.println("JSON "+json);
		//CCC c3 = gson.fromJson(json, CCC.class);
		
		
		//System.out.println(c3.size());
		
	}
	
	
	
	public class CCC{
		
		String id;
		String name;
		Map<String, String> clipMap;
		
	}
}