package org.jfw.apt.model.web.handlers;

import org.jfw.apt.annotation.web.SetSession;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.web.RequestHandler;

public class SetSessionHandler extends RequestHandler{

	@Override
	public void init() throws AptException {
	}

	@Override
	public void appendAfterCode(StringBuilder sb) throws AptException {
		SetSession ss = this.getRmcg().getRef().getAnnotation(SetSession.class);
		if(ss!=null){
			this.getRmcg().readSession(sb);
			String[] vals = ss.value();
			for(String val:vals){
				if(val==null || val.trim().length()==0 )throw new AptException(this.getRmcg().getRef(),"invalid @SetSession");
				val = val.trim();
				int index = val.indexOf("=");
				if(index<1 || (index>=(val.length()-1)))throw new AptException(this.getRmcg().getRef(),"invalid @SetSession");
				String n = val.substring(0,index).trim();
				String v = val.substring(index+1).trim();
				if(v.length()==0)throw new AptException(this.getRmcg().getRef(),"invalid @SetSession");
				sb.append("session.setAttriubte(\"").append(n).append("\",").append(v).append(");\r\n");			
			}
		}
	}
	

}
