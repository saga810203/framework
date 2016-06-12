package org.jfw.apt.model.web.handlers.validparam;

import java.util.LinkedList;
import java.util.ListIterator;

import org.jfw.apt.annotation.web.validparam.ValidScript;
import org.jfw.apt.model.web.handlers.ValidateParamHandler;

public class ValidScriptHandler extends ValidateParamHandler.ValidParameter {


	@Override
	public void doValid() {
		String[] val = this.mpe.getRef().getAnnotation(ValidScript.class).value();
		if(val == null) return;
		LinkedList<String> list = new LinkedList<String>();
		for(String s:val){
			if(s!=null && s.trim().length()> 0) list.add(s.trim());
		}
		if(list.isEmpty()) return;
		sb.append(this.resultName).append(" = ");
		boolean first = true;
		for(ListIterator<String> it = list.listIterator(); it.hasNext();){
			if(first ){
				first = false;
				sb.append("(");
			}else{
				sb.append(") && (");
			}
			sb.append(it.next());
		}
		sb.append(");");
		
	}
	


	@Override
	public String annotationClassName() {
		return "@ValidScript";
	}

}
