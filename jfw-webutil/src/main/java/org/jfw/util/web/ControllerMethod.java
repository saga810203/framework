package org.jfw.util.web;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ControllerMethod {
	private Object obj;
	private Method method;
	private final String[] dynamicUrl;
	private final int pathLen;

	public ControllerMethod(Object obj, Method method) {
		this.obj = obj;
		this.method = method;
		this.dynamicUrl = null;
		this.pathLen = 0;
	}

	public ControllerMethod(Object obj, Method method, String[] dynamicUrl) {
		this.obj = obj;
		this.method = method;
		this.dynamicUrl = dynamicUrl;
		this.pathLen = this.dynamicUrl.length;
	}

	public boolean match(String[] urls) {
		for (int i = 0; i < pathLen; ++i) {
			if (this.dynamicUrl[i] != null && (!this.dynamicUrl.equals(urls[i])))
				return false;
		}
		return true;
	}

	public void invoke(HttpServletRequest req, HttpServletResponse res, int viewType)
			throws ServletException, IOException {
		try {
			this.method.invoke(obj, req, res, viewType);
		} catch (InvocationTargetException e) {
			Throwable th = e.getTargetException();
			if (th instanceof IOException) {
				throw (IOException) th;
			} else if (th instanceof ServletException) {
				throw (ServletException) th;
			} else {
				throw new ServletException(th);
			}
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (IllegalArgumentException e) {
			throw new ServletException(e);
		}
	}

}
