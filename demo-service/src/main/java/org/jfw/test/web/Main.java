package org.jfw.test.web;

import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.WebHandler;

@WebHandler("/")
public class Main {
	@RequestMapping
	public String login() {
		return "OK";
	}
}
