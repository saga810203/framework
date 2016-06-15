package org.jfw.test.web;

import java.sql.Connection;

import org.jfw.apt.annotation.Autowrie;
import org.jfw.apt.annotation.web.JdbcConn;
import org.jfw.apt.annotation.web.RequestBody;
import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.RequestParam;
import org.jfw.apt.annotation.web.WebHandler;
import org.jfw.test.dao.UserDao;
import org.jfw.test.po.User;
import org.jfw.test.web.model.ChangeModel;

@WebHandler("/login")
public class LoginService {
	@Autowrie
	private UserDao userDao;
	
	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	@RequestMapping
	public User login( 
			@RequestParam(required=false) String loginname,
			@RequestParam("pw") String loginPassword){
		return null;
	}
	
	@RequestMapping("change")
	public void change(@RequestParam() ChangeModel cm){
		
	}
	@RequestMapping("change1")
	public void change1(@RequestParam("a") ChangeModel cm){
		
	}
	@RequestMapping("change2")
	public void change2(@RequestParam(excludeFields={"oldpass"}) ChangeModel cm){
		
	}
	@RequestMapping("change3")
	public void change3(@RequestBody ChangeModel cm, @JdbcConn(true) Connection con){
		
	}
	
}
