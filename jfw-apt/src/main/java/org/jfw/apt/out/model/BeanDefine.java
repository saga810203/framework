package org.jfw.apt.out.model;

import java.util.LinkedList;
import java.util.List;

public class BeanDefine {
	private static final String PROTOTYPE = "prototype";
	protected String id;
	protected List<String> flags = new LinkedList<String>();
	protected String value;

	protected List<BeanAttributeDefine> attributes = new LinkedList<BeanAttributeDefine>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}


	protected boolean containFalg(String flag) {
		return this.flags.contains(flag);
	}

	protected void removeFalg(String flag) {
		this.flags.remove(flag);
	}

	protected void addFalg(String flag) {
		if (!this.flags.contains(flag))
			this.flags.add(flag);
	}

	public void setSingle(boolean val) {
		if (val)
			this.addFalg(PROTOTYPE);
		else
			this.removeFalg(PROTOTYPE);
	}

	public boolean isSingle() {
		return this.flags.contains(PROTOTYPE);
	}
	
	protected void addAttribute(BeanAttributeDefine bad){
		this.attributes.add(bad);
	}
	public void joinGroup(String groupname){
		String gn = "list-group-"+groupname;
		this.addFalg(gn);
	}

	
	public void appendToStringBuilder(StringBuilder sb) {
		sb.append("\r\n");
		sb.append(this.id);
		for(String s:this.flags){
			sb.append("::").append(s);
		}
		sb.append("=").append(this.value);
		for(BeanAttributeDefine bad:this.attributes){
			bad.appendToStringBuilder(sb, this);
		}		
	}
    
	

}
