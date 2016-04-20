package org.jfw.test.po;

import org.jfw.apt.annotation.orm.Column;
import org.jfw.apt.annotation.orm.Table;
import org.jfw.apt.annotation.orm.Unique;
import org.jfw.apt.orm.core.enums.DataElement;
import org.jfw.test.po.abstracted.EnabledPO;
@Table(primaryKey=@Unique())
public class User extends EnabledPO {
	@Column(value=DataElement.string_de,nullable=false)
	private String id;
	@Column(value=DataElement.string_de,nullable=false)
	private String name;
	@Column(value=DataElement.string_de,nullable=false)
	private String loginName;
	@Column(value=DataElement.string_de,nullable=false)
	private String password;
	@Column(value=DataElement.String_de,nullable=true)
	private String descp;
	public int getAge() {
		return age;
	}
	public void setAge(int age) {
		this.age = age;
	}
	@Column(value=DataElement.int_de,nullable=false)
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
