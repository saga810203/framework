package org.jfw.apt.out.model;

import java.util.LinkedList;

public class BeanConfig {

	public ClassBeanDefine addServiceBeanByClass(String classname, String id) {
		if (id == null || id.trim().length() == 0)
			id = classname.trim().replaceAll("\\.", "_");
		for (Entry entry : this.entrys) {
			if (entry.getType().equals("class") && entry.id.equals(classname))
				return (ClassBeanDefine) entry.getBd();
		}
		ClassBeanDefine cbd = new ClassBeanDefine(classname);
		cbd.setId(id);
		Entry entry = new Entry();
		entry.setId(id);
		entry.setType("class");
		entry.setBd(cbd);
		this.entrys.add(entry);
		return cbd;
	}

	public ClassBeanDefine addEntryBeanByClass(String classname, String id) {
		if (id == null || id.trim().length() == 0)
			id = java.util.UUID.randomUUID().toString().replaceAll("-", "");

		ClassBeanDefine cbd = new ClassBeanDefine(classname);
		cbd.setId(id);
		Entry entry = new Entry();
		entry.setId(id);
		entry.setType("class");
		entry.setBd(cbd);
		this.entrys.add(entry);
		return cbd;
	}

	public BuilderBeanDefine addServiceBeanByBuilder(String classname, String method, String id) {
		String oid = classname.trim().replaceAll("//.", "_") + "$" + method.trim();
		if (id == null || id.trim().length() == 0)
			id = oid;
		for (Entry entry : this.entrys) {
			if (entry.getId().equals(id) && entry.getType().equals("build"))
				return (BuilderBeanDefine) entry.getBd();
		}
		BuilderBeanDefine bbd = new BuilderBeanDefine(classname.trim(), method.trim());
		bbd.setId(id);
		Entry entry = new Entry();
		entry.setBd(bbd);
		entry.setId(id);
		entry.setType("build");
		this.entrys.add(entry);
		return bbd;
	}

	public BuilderBeanDefine addEntryBeanByBuilder(String classname, String method, String id) {

		if (id == null || id.trim().length() == 0)
			id = java.util.UUID.randomUUID().toString().replaceAll("-", "");
		BuilderBeanDefine bbd = new BuilderBeanDefine(classname.trim(), method.trim());
		bbd.setId(id);
		Entry entry = new Entry();
		entry.setBd(bbd);
		entry.setId(id);
		entry.setType("build");
		this.entrys.add(entry);
		return bbd;
	}

	public FactoryBeanDefine addServiceBeanByFactory(String factoryBeanid, String method, String id) {
		String oid = factoryBeanid.trim() + "-" + method.trim();
		if (id == null || id.trim().length() == 0)
			id = oid;
		for (Entry entry : this.entrys) {
			if (entry.getId().equals(id) && entry.getType().equals("factory"))
				return (FactoryBeanDefine) entry.getBd();
		}
		FactoryBeanDefine bbd = new FactoryBeanDefine(factoryBeanid.trim(), method.trim());
		bbd.setId(id);
		Entry entry = new Entry();
		entry.setBd(bbd);
		entry.setId(id);
		entry.setType("factory");
		this.entrys.add(entry);
		return bbd;
	}

	public FactoryBeanDefine addEntryBeanByFactory(String factoryBeanid, String method, String id) {
		if (id == null || id.trim().length() == 0)
			id = java.util.UUID.randomUUID().toString().replaceAll("-", "");
		FactoryBeanDefine bbd = new FactoryBeanDefine(factoryBeanid.trim(), method.trim());
		bbd.setId(id);
		Entry entry = new Entry();
		entry.setBd(bbd);
		entry.setId(id);
		entry.setType("factory");
		this.entrys.add(entry);
		return bbd;
	}

	public void appendTo(StringBuilder sb) {
		for (Entry entry : this.entrys) {
			entry.getBd().appendToStringBuilder(sb);
		}
	}

	private final LinkedList<Entry> entrys = new LinkedList<Entry>();

	private static class Entry {
		private String id;
		private String type;
		private BeanDefine bd;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}

		public BeanDefine getBd() {
			return bd;
		}

		public void setBd(BeanDefine bd) {
			this.bd = bd;
		}
	}

}
