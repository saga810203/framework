package org.jfw.test.web;

import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.WebHandler;

@WebHandler("/path")
public class TestPath {
	
	@RequestMapping("/info")
	public String info(){
		return "info";
	}

}
