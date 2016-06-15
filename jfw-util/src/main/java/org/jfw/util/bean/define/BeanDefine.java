package org.jfw.util.bean.define;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfw.util.bean.BeanBuilder;
import org.jfw.util.bean.BeanFactory;
import org.jfw.util.bean.InValidBeanConfigException;

public abstract class BeanDefine {
	private static final String LIST_GROUP = "list-group-";
	private BeanFactory factory;    	
	private String key;
	private String name;
	private String value;
	private List<String> groupNames = null;
	

	private List<String> depends = new ArrayList<String>();	
	
	protected BeanDefine(BeanFactory bf,String key,String value){
		this.factory = bf;
		this.key = key;
		this.value = value;
		List<String> list = BeanFactory.split(key,"::", false);		
		this.name= list.get(0);
		for(int i = 1; i < list.size(); ++i){
			String s = list.get(i);
			if(s.startsWith(LIST_GROUP)&& !s.equals(LIST_GROUP)){
				if(this.groupNames==null) this.groupNames = new LinkedList<String>();				
				this.groupNames.add(s.substring(LIST_GROUP.length()));
			}
		}
	}
	
	public boolean  isRef(List<BeanDefine> list,String beanid){
		if(this.depends.contains(beanid)) return true;
		for(String b:this.depends){
			BeanDefine bd = null;
			for(BeanDefine bdd:list){
				if(b.equals(bdd.getName())) {
					bd = bdd; 
					break;
				}
			}
			if(bd!=null && bd.isRef(list, beanid)) return true;			
		}
		return false;		
	}
	
	public void addAttributes(BeanFactory bf,Map<String,String> attrs) throws InValidBeanConfigException{
		for(Map.Entry<String,String> entry:attrs.entrySet()){
			try {
				this.addAttributeEntry(bf,entry.getKey(),entry.getValue());
			} catch (ConfigException e) {
				throw new InValidBeanConfigException(entry.getKey(), entry.getValue(), e.getMessage(), e.getCause());
			}
		}
	}
	public void addAttributeEntry(BeanFactory bf,String vkey,String vVal) throws ConfigException{
		if(vkey.startsWith(this.name+".")){
			this.addAttributeInternal(bf,vkey.substring(this.name.length()+1),vVal);
		}
	}
	public abstract void addAttributeInternal(BeanFactory bf,String attrName,String attrVal) throws ConfigException;	
	
	
	public abstract BeanBuilder buildBeanBuilder(BeanFactory bf);
	
	public boolean addDependBean(String beanid){
		if(!this.factory.contains(beanid)) return false;
		if(!this.depends.contains(beanid)){
			this.depends.add(beanid);
		}
		return true;
	}
	public List<String> getDependBeans(){
		return Collections.unmodifiableList(this.depends);
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	public List<String> getGroupNames() {
		return this.groupNames==null ?Collections.<String>emptyList():this.groupNames;
	}
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
