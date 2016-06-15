package org.jfw.test.web;

import java.util.HashMap;
import java.util.Map;

import org.jfw.apt.annotation.web.PathVar;
import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.WebHandler;

@WebHandler("/path")
public class TestPath {
	
	@RequestMapping("/info")
	public String info(){
		return "info";
	}
	
	@RequestMapping("/{code}/{name}")
	public Map<String,String> info(@PathVar String code,@PathVar String name){
		Map<String,String> result= new HashMap<String,String>();
		result.put("code",code);
		result.put("name", name);
		return result;
		
	}

}
