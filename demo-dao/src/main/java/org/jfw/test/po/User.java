package org.jfw.test.po;

import org.jfw.apt.annotation.orm.Column;
import org.jfw.apt.annotation.orm.Table;
import org.jfw.apt.annotation.orm.Unique;
import org.jfw.apt.orm.core.enums.DE;
import org.jfw.test.po.abstracted.EnabledPO;
@Table(primaryKey=@Unique("id"),uniques={@Unique(value={"loginName"},name="Unique_loginName")})
public class User extends EnabledPO {
	@Column(value=DE.string_de)
	private String id;
	@Column(value=DE.string_de)
	private String name;
	@Column(value=DE.string_de)
	private String loginName;
	@Column(value=DE.string_de)
	private String password;
	@Column(value=DE.String_de)
	private String descp;
	@Column(value=DE.String_de)
	private String commm;
	
	public String getCommm() {
		return commm;
	}
	public void setCommm(String commm) {
		this.commm = commm;
	}
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	@Column(value=DE.int_de)
	private int age;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getDescp() {
		return descp;
	}
	public void setDescp(String descp) {
		this.descp = descp;
	}
}
