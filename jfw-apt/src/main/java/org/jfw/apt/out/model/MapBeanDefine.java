package org.jfw.apt.out.model;

public class MapBeanDefine extends BeanDefine{
	private int keyIndex = 0;
	public MapBeanDefine(String classname){
		this.value = classname;
		this.addFalg("map");
	}
	public void addEntry(String key,String value){
		++keyIndex;
		BeanAttributeDefine keyAttr = new BeanAttributeDefine();
		keyAttr.setKey("map-key-"+keyIndex);
		keyAttr.setValue(value);
		this.attributes.add(keyAttr);
		BeanAttributeDefine valAttr = new BeanAttributeDefine();
		valAttr.setKey("map-val-"+keyIndex);
		valAttr.setValue(value);
		this.attributes.add(valAttr);	
	}
	public void setKeyRef(){
		if(keyIndex<1) return;
		BeanAttributeDefine key = this.attributes.get(this.attributes.size()-2);
		key.setRef(true);
	}
	public void setValueRef(){
		if(keyIndex<1) return;
		BeanAttributeDefine val = this.attributes.get(this.attributes.size()-1);
		val.setRef(true);
	}
	public void setKeyClassName(String classname){
		if(keyIndex<1) return;
		BeanAttributeDefine key = this.attributes.get(this.attributes.size()-2);
		key.setValueClassName(classname);
	}
	public void setValueClassName(String classname){
		if(keyIndex<1) return;
		BeanAttributeDefine val = this.attributes.get(this.attributes.size()-1);
		val.setValueClassName(classname);
	}
}
