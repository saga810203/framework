package org.jfw.util.bean.define;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.jfw.util.bean.BeanBuilder;
import org.jfw.util.bean.BeanFactory;

public class CollectionBeanDefine extends BeanDefine {
	public static final String COLLECTION_ELE = "::collection-ele";

	private Class<?> clazz;
	private Method method;
	private List<ValueDefine> eles = null;

	private boolean refEle = false;
	private String valueClassName = null;

	private CollectionBeanDefine(BeanFactory bf, String key, String value) {
		super(bf, key, value);
	}

	public static BeanDefine build(String key, String value, BeanFactory bf) throws ConfigException {
		CollectionBeanDefine result = new CollectionBeanDefine(bf, key, value);
		try {
			result.clazz = Class.forName(value);
		} catch (ClassNotFoundException e) {
			throw new ConfigException("invalid className[" + value + "]", e);
		}
		try {
			result.method = result.clazz.getMethod("add", Object.class);
		} catch (Exception e) {
			throw new ConfigException("invalid classname[" + value + "] with not found method[add(Object obj)]");
		}
		return result;
	}

	private void addElement(ValueDefine vd) {
		if (this.eles == null) {
			this.eles = new ArrayList<ValueDefine>();
			this.eles.add(vd);
			return;
		}
		boolean added = false;
		for (int i = 0; i < this.eles.size(); ++i) {
			String vdName = this.eles.get(i).getName();
			int eqValue = vdName.compareTo(vd.getName());
			if (eqValue >= 0) {
				added = true;
				this.eles.add(i, vd);
				break;
			}
		}
		if (!added)
			this.eles.add(vd);
	}

	private String parseEleName(String an) {
		this.valueClassName = null;
		this.refEle = false;
		String eleName = an;

		int index = eleName.indexOf(COLLECTION_ELE);
		if (index < 0)
			return null;

		index = eleName.indexOf("::");
		if (index > 0) {
			this.valueClassName = eleName.substring(index + 2);
			eleName = eleName.substring(0, index);
		}
		if (index > 0)
			eleName = eleName.substring(index);
		if (eleName.equals(COLLECTION_ELE))
			return "";
		eleName = eleName.substring(COLLECTION_ELE.length());
		if (eleName.equals("Ref")) {
			this.refEle = true;
			return "";
		}
		if (eleName.startsWith("Ref-")) {
			this.refEle = true;
			return eleName.substring(4);
		}
		if (eleName.startsWith("-")) {
			return eleName.substring(1);
		}
		return null;

	}

	@Override
	public void addAttributeInternal(BeanFactory bf, String attrName, String attrVal) throws ConfigException {
		String aName = this.parseEleName(attrName);

		if (aName == null)
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
		this.addElement(vd);
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
		builder.values = this.eles;
		return builder;
	}

	private static class Builder implements BeanBuilder {
		private Class<?> clazz;
		private Method method;
		private List<ValueDefine> values;

		@Override
		public Object build(BeanFactory bf) {
			Object result;
			try {
				result = clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("invald classname[" + clazz.getName() + "] instance error", e);
			}
			if (this.values != null) {
				for (ListIterator<ValueDefine> it = values.listIterator(); it.hasNext();) {
					try {
						method.invoke(result, it.next().getValue(bf));
					} catch (Exception e) {
						throw new RuntimeException("invoke method[" + clazz.getName() + ".add(Object obj)]", e);
					}
				}
			}
			return result;
		}

	}

}
