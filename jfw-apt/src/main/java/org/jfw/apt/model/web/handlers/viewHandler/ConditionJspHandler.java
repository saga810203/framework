package org.jfw.apt.model.web.handlers.viewHandler;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.Condition;
import org.jfw.apt.annotation.web.ConditionJSP;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.handlers.ViewHandler;

public class ConditionJspHandler extends ViewHandler.ViewHandlerImpl {
	private String prefix = null;
	private String dataName = null;
	private String defaultValue = null;
	private Condition[] value = null;
	private boolean enableJson = false;
	private int jsonViewType = 1;

	@Override
	public void init(StringBuilder sb) throws AptException {
		ConditionJSP jsp = this.viewHandler.getRmcg().getRef().getAnnotation(ConditionJSP.class);
		if (null == jsp)
			throw new AptException(this.viewHandler.getRmcg().getRef(), "invalid @ConditionJSP");
		this.enableJson = jsp.enableJson();
		this.jsonViewType = jsp.jsonViewType();
		this.prefix = Utils.emptyToNull(jsp.prefix());
		if (this.prefix == null)
			this.prefix = "";

		this.dataName = jsp.dataName();
		this.defaultValue = Utils.emptyToNull(jsp.defaultValue());
		if (this.defaultValue == null)
			throw new AptException(this.viewHandler.getRmcg().getRef(), "invalid @ConditionJSP");
		this.value = jsp.value();
		if (this.value == null || this.value.length == 0)
			throw new AptException(this.viewHandler.getRmcg().getRef(), "invalid @ConditionJSP");
		for (Condition c : value) {
			if (null == Utils.emptyToNull(c.el()) || (Utils.emptyToNull(c.value()) == null))
				throw new AptException(this.viewHandler.getRmcg().getRef(), "invalid @ConditionJSP");
		}
		if(this.enableJson) this.viewHandler.getRmcg().readOut(sb);
	}

	@Override
	public void handlerFail(StringBuilder sb) throws AptException {
		if (this.enableJson) {
			sb.append("if(").append(this.jsonViewType).append("== viewType){\r\n");
			ViewUtils.printJSONException(this.viewHandler, sb);
			sb.append("} else {");
		}

		ViewUtils.printJSPException(this.viewHandler, sb);

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
				ViewUtils.printJSONWithValue(this.viewHandler, sb);
			} else {
				ViewUtils.printJSONSuccess(this.viewHandler, sb);
			}
			sb.append("return;\r\n");
			sb.append("}\r\n");
		}
		if (hasResult) {
			sb.append("req.setAttribute(\"").append(this.dataName).append("\",result);\r\n");
		}

		sb.append("String _jspView = \"").append(this.defaultValue).append("\";\r\n");
		for (int i = 0; i < value.length; ++i) {
			if (i != 0)
				sb.append("else ");
			sb.append("if(").append(value[i].el().trim()).append("){\r\n").append("\t_jspView = \"")
					.append(value[i].value().trim()).append("\";\r\n").append("}\r\n");
		}
		sb.append("_jspView =");
		if (this.prefix.length() > 0)
			sb.append("\"").append(this.prefix).append("\" + ");
		sb.append("_jspView + \".jsp\";\r\n");
		sb.append("req.getRequestDispatcher(_jspView).forward(req,res);");
	}
}
