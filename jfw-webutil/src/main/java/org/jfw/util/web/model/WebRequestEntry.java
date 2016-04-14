package org.jfw.util.web.model;

public class WebRequestEntry {
	private Object webHandler;
	private String methodName;
	private String methodType;
	private String uri;
	public Object getWebHandler() {
		return webHandler;
	}
	public void setWebHandler(Object webHandler) {
		this.webHandler = webHandler;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	public String getMethodType() {
		return methodType;
	}
	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

}
