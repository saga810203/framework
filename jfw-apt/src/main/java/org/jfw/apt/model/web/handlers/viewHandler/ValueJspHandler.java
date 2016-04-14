package org.jfw.apt.model.web.handlers.viewHandler;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.web.ValueJSP;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.handlers.ViewHandler;

public class ValueJspHandler extends ViewHandler.ViewHandlerImpl {
	private String prefix = null;
	private String value = null;
	private String dataName = null;
	private boolean enableJson = false;
	private int jsonViewType = 1;

	@Override
	public void init(StringBuilder sb) throws AptException {
		ValueJSP jsp = this.viewHandler.getRmcg().getRef().getAnnotation(ValueJSP.class);
		if (null == jsp)
			throw new AptException(this.viewHandler.getRmcg().getRef(), "invalid @JSP");
		this.enableJson = jsp.enableJson();
		this.jsonViewType = jsp.jsonViewType();
		this.prefix = Utils.emptyToNull(jsp.prefix());
		if (this.prefix == null)
			this.prefix = "";
		this.value = Utils.emptyToNull(jsp.value());
		if (this.value == null)
			throw new AptException(this.viewHandler.getRmcg().getRef(), "invalid @JSP");
		this.dataName = jsp.dataName();
		if(this.enableJson) this.viewHandler.getRmcg().readOut(sb);
	}
	@Override
	public void handlerFail(StringBuilder sb) throws AptException {
		if (this.enableJson) {
			sb.append("if(").append(this.jsonViewType).append("== viewType){\r\n");
			ViewUtils.printJSONException(this.viewHandler,sb);
			sb.append("} else {");
		}

		ViewUtils.printJSPException(this.viewHandler,sb);

		if (this.enableJson) {
			sb.append("}\r\n");
		}
	}
	@Override
	public void handlerSuccess(StringBuilder sb) throws AptException {
		boolean hasResult = !"void".equals(this.getViewHandler().getRmcg().getReturnType());
		if (this.enableJson) {
			sb.append("if(").append(this.jsonViewType).append("==").append("viewType){\r\n");
			if (hasResult) {
				ViewUtils.printJSONWithValue(this.viewHandler,sb);
			} else {
				ViewUtils.printJSONSuccess(this.viewHandler,sb);
			}
			sb.append("return;\r\n");
			sb.append("}\r\n");
		}
		if (hasResult) {
			sb.append("req.setAttribute(\"").append(this.dataName).append("\",result);\r\n");
		}

		sb.append("String _jspView = ");
		if(this.prefix!=null&& this.prefix.trim().length()>0){
			sb.append("\"").append(this.prefix.trim()).append("\" + ");
		}
		sb.append(this.value).append("+\".jsp\"").append(";\r\n");
		
		sb.append("req.getRequestDispatcher(_jspView).forward(req,res);");
	}
}
