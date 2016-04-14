package org.jfw.apt.model.web.handlers;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.handlers.viewHandler.JsonHandler;

public class ViewHandler  extends RequestHandler {
	private ViewHandlerImpl himpl = null;
	
	
	private ViewHandlerImpl match(AnnotationMirror an){
		Object obj = Utils.getReturnValueOnAnnotation("viewHandlerClass", an);
		return Utils.getObjectWithClassNameOrClass(obj, ViewHandlerImpl.class);		
	}

	@Override
	public void init() throws AptException {
		List<? extends AnnotationMirror> ans = this.ref.getAnnotationMirrors();
		this.himpl = null;
		for(AnnotationMirror an:ans){
			this.himpl = this.match(an);
			if(null!= himpl){
				break;
			}
		}
		if(this.himpl==null){
			this.himpl = new JsonHandler();
		}
		himpl.setViewHandler(this);
	}
	
	
	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		 himpl.appendBeforCode(sb);
	}


	@Override
	public void appendAfterCode(StringBuilder sb) throws AptException {
		himpl.appendAfterCode(sb);
	}

	public static abstract class ViewHandlerImpl{
		protected ViewHandler viewHandler = null;
		public ViewHandler getViewHandler() {
			return viewHandler;
		}
		public void setViewHandler(ViewHandler viewHandler) {
			this.viewHandler = viewHandler;
		}
		public abstract void handlerFail(StringBuilder sb) throws AptException; 
		public abstract void handlerSuccess(StringBuilder sb) throws AptException; 

		public abstract void init(StringBuilder sb)throws AptException;
		public void appendBeforCode(StringBuilder sb) throws AptException{
			this.init(sb);
			sb.append("try{\r\n");
		}
		public void appendAfterCode(StringBuilder sb)throws AptException{
			sb.append("}catch(Exception e){\r\n");// begin catch
			this.handlerFail(sb);
			sb.append("return; \r\n}\r\n");// end catch
			this.handlerSuccess(sb);
		}
	}

}
