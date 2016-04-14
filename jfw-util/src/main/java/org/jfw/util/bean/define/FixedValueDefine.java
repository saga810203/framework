package org.jfw.util.bean.define;

import org.jfw.util.bean.BeanFactory;

public class FixedValueDefine extends ValueDefine{
	private Object value;
	

	@Override
	public void init(BeanFactory bf, String name, Class<?> clazz, boolean isRef, String val) throws ConfigException {
		this.name = name;
		this.clazz = clazz==null?String.class:clazz;
		
		if(this.clazz.equals(Integer.class) ){
			this.value = Integer.valueOf(val);
		} else if(this.clazz.equals(Byte.class) ){
			this.value = Byte.valueOf(val);
		} else if(this.clazz.equals(Short.class) ){
			this.value = Short.valueOf(val);
		} else if(this.clazz.equals(Long.class)){
			this.value = Long.valueOf(val);
		} else if(this.clazz.equals(Float.class)){
			this.value = Float.valueOf(val);
		} else if(this.clazz.equals(Double.class)){
			this.value = Double.class;
		} else if(this.clazz.equals(Boolean.class)){
			this.value = Boolean.valueOf(val);
		} else if(this.clazz.equals(Character.class)){
			this.value = Character.valueOf(val.charAt(0));
		} else if(this.clazz.equals(String.class)){
			this.value = val;
		} else if(this.clazz.equals(java.math.BigInteger.class)){
			this.value = new  java.math.BigInteger(val);
		} else if(this.clazz.equals(java.math.BigDecimal.class)){
			this.value = new java.math.BigDecimal(val);
		} else if(this.clazz.equals(Class.class)){
			try {
				this.value = Class.forName(val);
			} catch (ClassNotFoundException e) {
				throw new ConfigException("invalid class name["+val+"]",e);
			}
		} 
	}
	public void setValue(Object value){
		this.value = value;
	}

	@Override
	public Object getValue(BeanFactory bf) {
		return this.value;
	}
}
