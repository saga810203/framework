package org.jfw.test.web;

import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.WebHandler;

@WebHandler("/")
public class Main {
	@RequestMapping
	public String login() {
		StringBuilder sb = new StringBuilder();
		for(int i = 0 ; i < 10000;++i){
			sb.append("123");
		}
		return sb.toString();
	}
}
