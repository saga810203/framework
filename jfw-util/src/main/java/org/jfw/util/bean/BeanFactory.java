package org.jfw.util.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jfw.util.bean.define.BeanDefine;
import org.jfw.util.bean.define.ClassBeanDefine;
import org.jfw.util.bean.define.CollectionBeanDefine;
import org.jfw.util.bean.define.ConfigException;
import org.jfw.util.bean.define.FactoryBeanBuilder;
import org.jfw.util.bean.define.MapBeanDefine;
import org.jfw.util.bean.define.StaticBuildBeanDefine;

/**
 * beanid=classname //singleto bean beanid::prototype=classname //prototype bean
 * 
 * beanid::build=classname //create bean by static method step 1
 * beanid.build-method=methodname //create bean by static method step 2
 * 
 * beanid::factory=beanid //create bean by bean method step 1
 * beanid.factory-method=methodname //create bean by bean method step 2
 * 
 * beanid::map=classname //create map bean;
 * beanid.map-key-{seq}[::classname]=value //map bean key id
 * beanid.map-val-{seq}[::classname]=value //map bean value id
 * 
 * beanid.map-key-{seq}-ref = beanid //map bean key id ref
 * beanid.map-val-{seq}-ref = beanid //map bean key id ref
 * 
 * beanid::collection=class //create collection bean
 * beanid.collection-ele[-{seq}][::classname]=value //collection element
 * beanid.collection-eleRef[-{seq}]=beanid //collection element
 * 
 * beanid.attributename[::classname]=value beanid.attributename-ref=beanid
 * 
 * beanid::list-group-{group name}
 * 
 * 
 * @author Saga_
 *
 */
public class BeanFactory {
	public Object getBean(String id) {
		Object result = this.singleton.get(id);
		if (result == null) {
			BeanBuilder bd = this.beanBuilders.get(id);
			if (bd != null)
				result = bd.build(this);
			else if (this.parent != null)
				result = this.parent.getBean(id);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public <T> T getBean(String id, Class<T> clazz) {
		Object result = this.getBean(id);
		if (null == result && (null != this.parent))
			result = parent.getBean(id);
		if (null != result && clazz.isAssignableFrom(result.getClass())) {
			return (T) result;
		}
		return null;
	}

	public List<Object> getBeansWithGroup(String groupname){
		List<Object> result = new ArrayList<Object>();
		if(this.groups!=null){
			List<String> list = this.groups.get(groupname);
			if(list!=null){
				for(String s:list){
					result.add(this.getBean(s));
				}
			}
		}
		if(this.parent!=null)
		result.addAll(this.parent.getBeansWithGroup(groupname));
		return result;
	}
	public List<String> getBeanIdsWithGroup(String groupname){
		return this.groups.get(groupname);
	}

	

	public boolean contains(String beanid) {
		return this.names.contains(beanid) || (this.parent != null && this.parent.contains(beanid));
	}

	public boolean isSingletonBean(String beanid) {
		return this.names4Singleton.contains(beanid) || (null != this.parent && this.parent.isSingletonBean(beanid));
	}

	public List<String> listBeanName() {
		if (null == this.parent)

			return Collections.unmodifiableList(this.names);
		else {
			List<String> result = new ArrayList<String>();
			result.addAll(this.names);
			result.addAll(this.parent.listBeanName());
			return Collections.unmodifiableList(result);
		}
	}

	protected void addBeanId(String key) {
		String beanid = key;
		int index = beanid.indexOf("::");
		if (index > 0) {
			beanid = beanid.substring(0, index);
		}
		if (this.contains(beanid))
			throw new RuntimeException("Duplicate BeanId[" + beanid + "]");
		this.names.add(beanid);
		if (key.indexOf("::prototype") < 0)
			this.names4Singleton.add(beanid);
	}

	protected void fillBeanNames(Map<String, String> beans) {
		for (String key : beans.keySet()) {
			this.addBeanId(key);
		}
	}

	protected static BeanDefine buildBeanDefine(BeanFactory bf, String key, String val, Map<String, String> attrs)
			throws InValidBeanConfigException {
		List<String> list = split(key, "::", false);
		BeanDefine bd = null;
		try {
			if (list.contains("collection")) {
				bd = CollectionBeanDefine.build(key, val, bf);
			} else if (list.contains("map")) {
				bd = MapBeanDefine.build(key, val, bf);
			} else if (list.contains("build")) {
				bd = StaticBuildBeanDefine.build(key, val, bf);
			} else if (list.contains("factory")) {
				bd = FactoryBeanBuilder.build(key, val, bf);
			} else {
				bd = ClassBeanDefine.build(key, val, bf);
			}
		} catch (ConfigException e) {
			throw new InValidBeanConfigException(key, val, e.getMessage(), e);
		}
		bd.addAttributes(bf, attrs);
		return bd;
	}

	protected static List<BeanDefine> buildBeanDefines(BeanFactory bf, Map<String, String> beans,
			Map<String, String> attrs) throws InValidBeanConfigException {
		List<BeanDefine> result = new ArrayList<BeanDefine>();
		for (Map.Entry<String, String> entry : beans.entrySet()) {
			result.add(buildBeanDefine(bf, entry.getKey(), entry.getValue(), attrs));
		}
		return result;

	}

	private void addGroupElement(String groupName, String beanid) {

		if (this.groups == null)
			this.groups = new HashMap<String, List<String>>();
		List<String> list = this.groups.get(groupName);
		if (null != list) {
			list.add(beanid);
		} else {
			list = new ArrayList<String>();
			list.add(beanid);
			this.groups.put(groupName, list);
		}
	}

	protected static void ckeckNestedRef(List<BeanDefine> list) throws InValidBeanConfigException {
		for (BeanDefine bd : list) {
			if (bd.isRef(list, bd.getName()))
				throw new InValidBeanConfigException(bd.getKey(), bd.getValue(), "exists nested ref");
		}
	}

	public static BeanFactory build(BeanFactory parent, Properties config)
			throws InValidBeanConfigException, ConfigException {
		Map<String, String> beans = new HashMap<String, String>();
		Map<String, String> attrs = new HashMap<String, String>();
		fillBeanAndAttribute(config, beans, attrs);
		BeanFactory result = new BeanFactory();
		if (parent != null)
			result.parent = parent;
		result.fillBeanNames(beans);
		List<BeanDefine> bds = buildBeanDefines(result, beans, attrs);
		ckeckNestedRef(bds);
		for (BeanDefine bd : bds) {
			BeanBuilder builder = bd.buildBeanBuilder(result);
			if (bd.isSingleton()) {
				result.singleton.put(bd.getName(), builder.build(result));
			} else {
				result.beanBuilders.put(bd.getName(), builder);
			}
			if (bd.getGroupName() != null) {
				result.addGroupElement(bd.getGroupName(), bd.getName());
			}
		}
		return result;
	}

	protected static void fillBeanAndAttribute(Properties config, Map<String, String> beans, Map<String, String> attrs)
			throws InValidBeanConfigException {
		for (Map.Entry<Object, Object> entry : config.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			int index = key.indexOf("::");
			String name = key;
			if (index >= 0) {
				if (index == 0)
					raiseConfigException(key, value);
			} else {
				name = key.substring(0, index);
				index = name.indexOf(".");
				if (index < 0) {
					beans.put(key, value);
				} else if (index == 0) {
					raiseConfigException(key, value);
				} else {
					attrs.put(key, value);
				}
			}
		}
	}

	private Map<String, Object> singleton = new HashMap<String, Object>();
	private Map<String, BeanBuilder> beanBuilders = new HashMap<String, BeanBuilder>();

	private BeanFactory parent = null;

	private List<String> names = new ArrayList<String>();
	private List<String> names4Singleton = new ArrayList<String>();
	private Map<String, List<String>> groups;

	protected BeanFactory() {
	}

	protected static void raiseConfigException(String key, String val) throws InValidBeanConfigException {
		if (val == null)
			throw new InValidBeanConfigException(key, val, "config val is null or empty");
		throw new InValidBeanConfigException(key, val, "");
	}

	public static List<String> split(String str, String splitStr, boolean includeEmptyString) {
		List<String> result = new LinkedList<String>();
		String s = str;
		while (true) {
			int index = s.indexOf(splitStr);
			if (index == 0) {
				if (includeEmptyString)
					result.add("");
				s = s.substring(splitStr.length());
			} else if (index > 0) {
				result.add(s.substring(0, index));
				s = s.substring(index + splitStr.length());
			} else {
				result.add(s);
				break;
			}

		}
		return result;

	}
}
