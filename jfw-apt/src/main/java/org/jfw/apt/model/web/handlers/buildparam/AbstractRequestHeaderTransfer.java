package org.jfw.apt.model.web.handlers.buildparam;

import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.web.RequestHeaderModel;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;

public abstract class AbstractRequestHeaderTransfer implements RequestHeaderTransfer {
	protected StringBuilder sb;
	protected MethodParamEntry mpe;
	protected RequestMappingCodeGenerator rmcg;
	protected RequestHeaderModel annotation;
	protected RequestHeaderTransfer.FieldRequestParam frp;

	public abstract void bulidParam();

	public abstract void bulidBeanProterty();



	public void checkRequestFieldParamName() {
		if (this.frp.getValue() == null || this.frp.getValue().trim().length() == 0) {
			throw new RuntimeException("@RequestHeader.fields no set value");
		}
	}

	public void raiseNoFoundError(String paramName) {
		this.sb.append("throw new IllegalArgumentException(\"not found header:" + paramName + "\");");
	}

	@Override
	public void transfer(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg,
			RequestHeaderModel annotation) {
		this.sb = sb;
		this.mpe = mpe;
		this.annotation = annotation;
		this.frp = null;
		this.rmcg = rmcg;
		this.bulidParam();
	}

	@Override
	public void transferBeanProperty(StringBuilder sb, MethodParamEntry mpe, RequestMappingCodeGenerator rmcg,
			RequestHeaderTransfer.FieldRequestParam frp) {
		this.sb = sb;
		this.mpe = mpe;
		this.frp = frp;
		this.rmcg = rmcg;
		this.bulidBeanProterty();
	}

}
