package org.jfw.apt.exception;

import javax.lang.model.element.Element;

public class AptException extends Exception{
	private static final long serialVersionUID = 733532759786993110L;
	private Element ele;
	
	public Element getEle() {
		return ele;
	}

	public AptException(Element ele,String message){
		super(message);
		this.ele = ele;
	}
	
	

}
