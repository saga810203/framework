package org.jfw.apt.model.web;

import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.exception.AptException;

public abstract class RequestHandler  {
	protected ExecutableElement ref;
	protected RequestMappingCodeGenerator rmcg;
	final public RequestMappingCodeGenerator getRmcg() {
		return rmcg;
	}

	final public void init(RequestMappingCodeGenerator rmcg) throws AptException
	{
		this.rmcg = rmcg;
		this.ref = rmcg.getRef();
		this.init();
	}

	public abstract void init()throws AptException;
	public  void appendBeforCode(StringBuilder sb) throws AptException{}
	public  void appendAfterCode(StringBuilder sb)throws AptException{}
}
