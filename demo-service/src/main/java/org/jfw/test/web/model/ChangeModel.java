package org.jfw.test.web.model;

public class ChangeModel {
	private String id;
	private String oldpass;
	private String newpass;
	private String state;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOldpass() {
		return oldpass;
	}
	public void setOldpass(String oldpass) {
		this.oldpass = oldpass;
	}
	public String getNewpass() {
		return newpass;
	}
	public void setNewpass(String newpass) {
		this.newpass = newpass;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

}
