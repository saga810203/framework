package org.jfw.apt.model.web.handlers;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.web.LastScript;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;

public class LastScriptHandler extends RequestHandler{

	@Override
	public void init() throws AptException {
		
	}

	@Override
	public void appendBeforCode(StringBuilder sb) throws AptException {
		LastScript ls = this.getRmcg().getRef().getAnnotation(LastScript.class);
		if(ls!=null){
			String[] ss = ls.value();
			if(ss!=null&& ss.length>1){
				for(int i = 0 ; i < ss.length ; ++i){
					String s = Utils.emptyToNull(ss[i]);
					if(null!=s){
						sb.append(s);
					}
				}
			}
		}
	}

}
