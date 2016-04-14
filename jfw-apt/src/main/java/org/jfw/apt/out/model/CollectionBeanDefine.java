package org.jfw.apt.out.model;

public class CollectionBeanDefine extends BeanDefine {
	private boolean sorted = false;
	private int index = 0;

	public CollectionBeanDefine(String classname, boolean sorted) {
		this.value = classname;
		this.addFalg("collection");
		this.sorted = sorted;
	}

	public void addElement(String val, String classname) {
		BeanAttributeDefine ele = new BeanAttributeDefine();
		String name = "collection-ele";
		if (this.sorted) {
			++index;
			name = name + "-" + index;
		}
		ele.setKey(name);
		ele.setValue(val);
		if (classname != null)
			ele.setValueClassName(classname);
		this.addAttribute(ele);
	}

	public void addRefElement(String beanid) {
		BeanAttributeDefine ele = new BeanAttributeDefine();
		String name = "collection-eleRef";
		if (this.sorted) {
			++index;
			name = name + "-" + index;
		}
		ele.setKey(name);
		ele.setValue(beanid);
		this.addAttribute(ele);
	}

}
