package org.jfw.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jfw.util.web.ControllerMethod;
import org.jfw.util.web.WebHandlerContext;

public class BaseServlet extends HttpServlet{
	private static final long serialVersionUID = -1452578683832313005L;

	private static boolean startSuccess = true;
	private static String failReason = null;
	
	
	
	
	
	
	protected int prefixLen = 0;
	protected int viewType = 0;
	
	
	public static boolean isStartSuccess(){
		return startSuccess;
	}
	protected static void failStart( String reason ){
		startSuccess= false;
		if(failReason ==null && reason!=null && reason.trim().length()>0){
			failReason = reason;
		}
	}
	protected static void failStart(Throwable th){
		startSuccess= false;
		if(failReason ==null && th!=null ){
			StringWriter sw = new StringWriter();
			th.printStackTrace(new PrintWriter(sw));
			failReason = sw.toString();
		}
	}
	
	

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ControllerMethod cm = WebHandlerContext.findWithGetMethod(req, prefixLen);
		if(cm!=null)cm.invoke(req, resp, viewType);
		else resp.sendError(404);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ControllerMethod cm = WebHandlerContext.findWithPostMethod(req, prefixLen);
		if(cm!=null)cm.invoke(req, resp, viewType);
		else resp.sendError(404);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ControllerMethod cm = WebHandlerContext.findWithPutMethod(req, prefixLen);
		if(cm!=null)cm.invoke(req, resp, viewType);
		else resp.sendError(404);
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ControllerMethod cm = WebHandlerContext.findWithDeleteMethod(req, prefixLen);
		if(cm!=null)cm.invoke(req, resp, viewType);
		else resp.sendError(404);
	}

	@Override
	public void init() throws ServletException {
		try{
			String tmp = this.getServletConfig().getInitParameter("prefixLen");
			if(tmp!=null&&tmp.trim().length()>0)
		  this.prefixLen = Integer.parseInt(tmp.trim());
			
		}catch(NumberFormatException e){
			this.prefixLen = 0;
		}
		try{
			String tmp = this.getServletConfig().getInitParameter("viewType");
			if(tmp!=null&&tmp.trim().length()>0)
		  this.viewType = Integer.parseInt(tmp.trim());
			
		}catch(NumberFormatException e){
			this.viewType = 0;
		}
	}

}
