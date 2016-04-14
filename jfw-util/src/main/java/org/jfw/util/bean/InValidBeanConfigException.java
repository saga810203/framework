package org.jfw.util.bean;

public class InValidBeanConfigException extends Exception {
	private static final long serialVersionUID = 8645549796586391140L;
	private String key;
	private String val;
	private String reason;
	
	public InValidBeanConfigException(String key,String val,String reason,Throwable throwable){
		super("invalid config["+key+"="+val+"]:"+reason==null?"":reason,throwable);
		this.key = key;
		this.val = val;
		this.reason = reason;
	}
	public InValidBeanConfigException(String key,String val,String reason){
		super("invalid config["+key+"="+val+"]:"+reason==null?"":reason);
		this.key = key;
		this.val = val;
		this.reason = reason;
	}
	public String getKey() {
		return key;
	}
	public String getVal() {
		return val;
	}
	public String getReason() {
		return reason;
	}

}
