package org.jfw.util.bean.define;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jfw.util.bean.BeanBuilder;
import org.jfw.util.bean.BeanFactory;

public class MapBeanDefine extends BeanDefine {
	private static final String MAP_KEY = "map-key-";
	private static final String MAP_VAL = "map-val-";

	private Class<?> clazz;
	private Method method;
	private String valueClassName;
	private boolean refEle;
	private boolean keyElement;

	private Map<String, ValueDefine> valueEles = new HashMap<String, ValueDefine>();
	private Map<String, ValueDefine> keyEles = new HashMap<String, ValueDefine>();

	protected MapBeanDefine(BeanFactory bf, String key, String value) {
		super(bf, key, value);

	}

	public static BeanDefine build(String key, String value, BeanFactory bf) throws ConfigException {

		MapBeanDefine result = new MapBeanDefine(bf, key, value);
		try {
			result.clazz = Class.forName(value);
		} catch (ClassNotFoundException e) {
			throw new ConfigException("invalid classname[" + value + "]", e);
		}
		try {
			result.method = result.clazz.getMethod("put", Object.class, Object.class);
		} catch (Exception e) {
			throw new ConfigException("unfound method[pub(Object key,Object val)] class[" + value + "]");
		}
		return result;
	}

	private void addElement(String eleName, ValueDefine vd) throws ConfigException {
		if (this.keyElement)
			this.addKeyElement(eleName, vd);
		this.addValueElement(eleName, vd);

	}

	private void addKeyElement(String eleName, ValueDefine vd) throws ConfigException {
		if (this.keyEles.containsKey(eleName))
			throw new ConfigException("duplicat key[" + eleName + "] config");
		this.keyEles.put(eleName, vd);
	}

	private void addValueElement(String eleName, ValueDefine vd) throws ConfigException {
		if (this.valueEles.containsKey(eleName))
			throw new ConfigException("duplicat val[" + eleName + "] config");
		this.valueEles.put(eleName, vd);
	}

	private String parseEleName(String an) throws ConfigException {
		this.valueClassName = null;
		this.refEle = false;
		this.keyElement = true;

		String eleName = an;
		if (eleName.startsWith(MAP_KEY)) {
			if (eleName.equals(MAP_KEY))
				throw new ConfigException("not found map key seq");
			eleName = eleName.substring(MAP_KEY.length());
			int index = eleName.indexOf("::");
			if (index > 0) {
				this.valueClassName = eleName.substring(index + 2);
				eleName = eleName.substring(0, index);
			}
			if (eleName.endsWith("-ref")) {
				this.refEle = true;
				eleName = eleName.substring(0, eleName.length() - 4);
			}
		} else if (eleName.startsWith(MAP_VAL)) {
			if (eleName.equals(MAP_VAL))
				throw new ConfigException("not found map val seq");
			this.keyElement = false;
			eleName = eleName.substring(MAP_VAL.length());
			int index = eleName.indexOf("::");
			if (index > 0) {
				this.valueClassName = eleName.substring(index + 2);
				eleName = eleName.substring(0, index);
			}
			if (eleName.endsWith("-ref")) {
				this.refEle = true;
				eleName = eleName.substring(0, eleName.length() - 4);
			}
		} else {
			return null;
		}

		return eleName;
	}

	@Override
	public void addAttributeInternal(BeanFactory bf, String attrName, String attrVal) throws ConfigException {
		String aName = this.parseEleName(attrName);

		if (null == aName)
			return;

		if (this.refEle && (!bf.contains(attrVal))) {
			throw new ConfigException("not found ref bean[" + attrVal + "]");
		}
		
		Class<?> vClazz = null;

		if (this.valueClassName != null && this.valueClassName.trim().length() > 0) {
			try {
				vClazz = Class.forName(this.valueClassName.trim());
			} catch (Exception e) {
				throw new ConfigException("invalid className[" + this.valueClassName + "]", e);
			}
		}
		ValueDefine vd = ValueDefine.build(bf, aName, vClazz, this.refEle, attrVal);
		this.addElement(aName, vd);
		if (this.refEle) {
			if (!this.addDependBean(attrVal))
				throw new ConfigException("not found ref bean[" + attrVal + "]");
		}

	}

	@Override
	public BeanBuilder buildBeanBuilder(BeanFactory bf) {
		Builder builder = new Builder();
		builder.clazz = this.clazz;
		builder.method = this.method;
		for (Map.Entry<String, ValueDefine> entry : this.keyEles.entrySet()) {
			ValueDefine vd = this.valueEles.get(entry.getKey());
			if (vd != null)
			builder.eles.put(entry.getValue(), vd);
		}
		return builder;
	}

	private static class Builder implements BeanBuilder {
		private Class<?> clazz;
		private Method method;
		private Map<ValueDefine, ValueDefine> eles = new HashMap<ValueDefine, ValueDefine>();

		@Override
		public Object build(BeanFactory bf) {
			Object result;
			try {
				result = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("crate class[" + clazz.getName() + "] instance error", e);
			}
			
			return result;
		}
		@Override
		public void config(Object obj, BeanFactory bf) {
			for (Map.Entry<ValueDefine, ValueDefine> entry : this.eles.entrySet()) {
				try {
					method.invoke(obj, entry.getKey().getValue(bf), entry.getValue().getValue(bf));
				} catch (Exception e) {
					throw new RuntimeException("invoke method[" + clazz.getName() + ".put(Object obj,Object)]", e);
				}
			}
			
		}

	}

}
