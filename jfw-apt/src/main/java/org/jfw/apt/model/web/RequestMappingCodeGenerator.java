package org.jfw.apt.model.web;

import org.jfw.apt.WebHandlerSupported;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.AbstractMethodGenerater;

public class RequestMappingCodeGenerator extends AbstractMethodGenerater {
	private WebHandlerSupported wsh;
	private String uri;
	private String webMethodName;

	private boolean readedStringArray = false;
	private boolean readedString = false;
	private boolean readedHeaders = false;
	private boolean readedSession = false;
	private boolean readedOut = false;
	private boolean readedURI = false;

	public void readURI(StringBuilder sb, String pathAttribute) {
		if (!readedURI) {
			readedURI = true;
			sb.append("String[] _uriArray = (String[]) req.getAttribute(\"").append(pathAttribute.trim()).append("\");");
		}
	}

	public void readOut(StringBuilder sb) {
		if (!readedOut) {
			readedOut = true;
			sb.append("java.io.PrintWriter out = res.getWriter();");
		}
	}

	public void readSession(StringBuilder sb) {
		if (!readedSession) {
			readedSession = true;
			sb.append(" javax.servlet.http.HttpSession session = req.getSession();");
		}
	}

	public void readParameters(StringBuilder sb, String paramName) {
		if (!readedStringArray) {
			readedStringArray = true;
			sb.append("String[] ");
		}
		sb.append(" params = req.getParameterValues(\"").append(paramName).append("\");");
	}

	public void readHeaders(StringBuilder sb, String paramName) {
		if (!this.readedHeaders) {
			readedHeaders = true;
			sb.append("java.util.List<String> headers = new java.util.LinkedList<String>();");
			sb.append("java.util.Enumeration<String> ");
		}
		sb.append("enumHeaders = req.getHeaders(\"").append(paramName).append("\");\r\n");
		//sb.append("headers.clear();\r\n");
		sb.append("while(enumHeaders.hasMoreElements()){\r\n").append("  headers.add(enumHeaders.nextElement());\r\n")
				.append("}\r\n");
		if (!readedStringArray) {
			readedStringArray = true;
			sb.append("String[] ");
		}
		sb.append("params =headers.toArray(new String[headers.size()]);\r\n");
	}

	public void readParameter(StringBuilder sb, String paramName) {
		if (!readedString) {
			readedString = true;
			sb.append("String ");
		}
		sb.append(" param = req.getParameter(\"").append(paramName).append("\");");
	}

	public void readHeader(StringBuilder sb, String paramName) {
		if (!readedString) {
			readedString = true;
			sb.append("String ");
		}
		sb.append("param = req.getHeader(\"").append(paramName).append("\");");
	}

	public void setWebHandlerSupported(WebHandlerSupported wsh) {
		this.wsh = wsh;
	}

	public WebHandlerSupported getWebHandlerSupported() {
		return this.wsh;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public void writeMethod(StringBuilder sb) throws AptException {
		if (this.uri.length() == 0)
			throw new AptException(ref, "invalid ReqeustMappeing value: empty String");

		this.webMethodName = this.wsh.getServiceMethodName();
		RequestHandler[] rs = this.wsh.createHandler();
		for (int i = 0; i < rs.length; ++i) {
			rs[i].init(this);
		}

		sb.append("public void ").append(this.webMethodName)
				.append("(javax.servlet.http.HttpServletRequest req,javax.servlet.http.HttpServletResponse res,int viewType) ")
				.append(" throws javax.servlet.ServletException,java.io.IOException{\r\n");
		if (!this.returnType.equals("void")) {

			sb.append(this.returnType).append(" result");
			if (!org.jfw.apt.Utils.isPrimitive(this.returnType))
				sb.append(" = null");
			sb.append(";\r\n");
		}
		for (int i = 0; i < rs.length; ++i) {
			rs[i].appendBeforCode(sb);
		}
		for (int i = rs.length - 1; i >= 0; --i) {
			rs[i].appendAfterCode(sb);
		}
		sb.append("}");
	}

	public String getWebMethodName() {
		return this.webMethodName;
	}

}
