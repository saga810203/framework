package org.jfw.util.bean.define;

import org.jfw.util.bean.BeanFactory;

public class FixedValueDefine extends ValueDefine{
	private Object value;
	

	@Override
	public void init(BeanFactory bf, String name, Class<?> clazz, boolean isRef, String val) throws ConfigException {
		this.name = name;
		this.clazz = clazz==null?String.class:clazz;
		
		if(this.clazz.equals(Integer.class)||this.clazz.equals(int.class) ){
			this.value = Integer.valueOf(val.trim());
		} else if(this.clazz.equals(Byte.class) ||this.clazz.equals(byte.class)){
			this.value = Byte.valueOf(val.trim());
		} else if(this.clazz.equals(Short.class)||this.clazz.equals(short.class) ){
			this.value = Short.valueOf(val.trim());
		} else if(this.clazz.equals(Long.class)||this.clazz.equals(long.class)){
			this.value = Long.valueOf(val.trim());
		} else if(this.clazz.equals(Float.class)||this.clazz.equals(float.class)){
			this.value = Float.valueOf(val.trim());
		} else if(this.clazz.equals(Double.class)||this.clazz.equals(double.class)){
			this.value = Double.class;
		} else if(this.clazz.equals(Boolean.class)||this.clazz.equals(boolean.class)){
			this.value = Boolean.valueOf(val.trim());
		} else if(this.clazz.equals(Character.class)||this.clazz.equals(char.class)){
			this.value = Character.valueOf(val.charAt(0));
		} else if(this.clazz.equals(String.class)){
			this.value = val;
		} else if(this.clazz.equals(java.math.BigInteger.class)){
			this.value = new  java.math.BigInteger(val.trim());
		} else if(this.clazz.equals(java.math.BigDecimal.class)){
			this.value = new java.math.BigDecimal(val.trim());
		} else if(this.clazz.equals(Class.class)){
			try {
				this.value = Class.forName(val.trim());
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
