package org.jfw.util.comm;

public class ClassCreateFactory implements ObjectFactory{
	private Class<?> clazz;

	public Class<?> getClazz() {
		return clazz;
	}

	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Object get() {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			String classname = null == clazz?"":clazz.getName();	
			throw new RuntimeException("create class["+classname+"] instance error",e);
		}
	}

}
