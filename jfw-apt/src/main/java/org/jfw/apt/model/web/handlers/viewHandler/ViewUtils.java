package org.jfw.apt.model.web.handlers.viewHandler;

import org.jfw.apt.model.web.handlers.ViewHandler;

//FIXME:override this class
public abstract class ViewUtils {
	public static void printJSONException(ViewHandler handler, StringBuilder sb){
		//e:Exception
		//res:HttpServletResponse
		//out: jsp.out
		sb.append("out.print(\"{success:false}\");\r\n");
	}
	public static void printJSONWithValue(ViewHandler handler,StringBuilder sb){
		//res:HttpServletResponse
		//out: jsp.out
		//result:Object
		  sb.append("out.write(\"{\\\"success\\\":true,\\\"data\\\":\");\r\n")
          .append("//org.jfw.util.json.JsonService.toJson(result,out);\r\n")
          .append("out.write(\"}\");\r\n");      
	}
	public static void printJSONSuccess(ViewHandler handler,StringBuilder sb){
		//res:HttpServletResponse
		//out: jsp.out
		 sb.append("out.write(\"{\\\"success\\\":true}\");\r\n"); 	
	}
	
	public static void printJSPException(ViewHandler handler,StringBuilder sb){
		//e:Exception
		//res:HttpServletResponse
		// FIXME:Handler Error
		sb.append("\r\n//TODO:impl application logic handler in  org.jfw.apt.model.web.handlers.viewHandler.JspHandler  \r\n");
		sb.append("res.sendError(500);\r\n");
	}
}
