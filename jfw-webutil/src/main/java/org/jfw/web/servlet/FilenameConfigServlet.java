package org.jfw.web.servlet;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import javax.servlet.ServletException;

import org.jfw.util.bean.AfterBeanFactory;
import org.jfw.util.bean.BeanFactory;
import org.jfw.util.io.IoUtil;
import org.jfw.util.web.WebHandlerContext;
import org.jfw.util.web.model.WebRequestEntry;

public class FilenameConfigServlet extends BaseServlet {

	private static final long serialVersionUID = 4188897681050379687L;
	public static final String JFW_MVC_GROUPNAME = "jfwmvc";
	public static final String BEAN_FAC_FILE_NAME = "configFileName";

	public static final String AFTER_BEANFACTORY = "afterBeanFactory";

	protected String configFileName = "beanConfig.properties";

	protected BeanFactory bf = null;

	protected Properties config;

	private String[] split(String val) {
		ArrayList<String> list = new ArrayList<String>();

		String str = val.trim();
		for (;;) {
			boolean find = false;
			int index = 0;
			for (; index < str.length(); ++index) {
				char c = str.charAt(index);
				if (c == ';' || c == ',') {
					find = true;
					break;
				}
			}
			if (!find) {
				if (str.length() > 0)
					list.add(str);
				break;
			}
			if (index == 0) {
				str = str.substring(1).trim();
			} else if (index == (str.length() - 1)) {
				list.add(str.substring(0, index).trim());
				break;
			} else {
				list.add(str.substring(0, index).trim());
				str = str.substring(index + 1).trim();
			}
		}
		return list.toArray(new String[list.size()]);
	}

	protected void buildBeanFactoryConfig() {
		String tmp = this.getServletConfig().getInitParameter(BEAN_FAC_FILE_NAME);
		if (tmp != null && tmp.trim().length() > 0)
			this.configFileName = tmp.trim();

		String[] filenames = this.split(this.configFileName);
		if (filenames.length == 0)
			failStart("no found config file");
		config = new Properties();
		for (int i = 0; i < filenames.length; ++i) {
			try {
				InputStream in = this.getClass().getClassLoader().getResourceAsStream(filenames[i]);
				try {
					if (in == null)
						continue;
					Properties p = new Properties();
					p.load(in);
					config.putAll(p);
				} finally {
					IoUtil.close(in);
				}
			} catch (Throwable th) {
				this.config = null;
				this.log(FilenameConfigServlet.class.getName() + ":load config file[" + filenames[i] + "] error", th);
				failStart(th);

			}
		}

	}

	protected void buildBeanFactory() {
		try {
			if (config != null&& config.size()>0)
				this.bf = BeanFactory.build(null, config);
		} catch (Throwable th) {
			this.bf = null;
			this.log(FilenameConfigServlet.class.getName() + ":build beanFactory error", th);
			failStart(th);
		}
	}

	protected void buildWebHandlers() {
		if (null == this.bf)
			return;
		List<String> list = this.bf.getBeanIdsWithGroup(JFW_MVC_GROUPNAME);
		if (list == null || list.isEmpty())
			return;
		for (String name : list) {
			Object obj = this.bf.getBean(name);
			if (obj instanceof WebRequestEntry) {
				if (!WebHandlerContext.addWebHandler((WebRequestEntry) obj)) {
					this.log(FilenameConfigServlet.class.getName() + ":bean[id=" + name + "] can't load as webHandler");
				}
			} else {
				this.log(FilenameConfigServlet.class.getName() + ":bean[id=" + name
						+ "] invalid org.jfw.web.model.WebRequestEntry");
			}
		}
	}

	public void doAfterBeanFactory() {
		if (null == this.bf)
			return;
		String tmp = this.getServletConfig().getInitParameter(AFTER_BEANFACTORY);
		if (tmp == null || tmp.trim().length() == 0)
			return;
		tmp = tmp.trim();

		String[] cns = tmp.split("[,;]");
		LinkedList<AfterBeanFactory> list = new LinkedList<AfterBeanFactory>();
		try {
			for (int i = 0; i < cns.length; ++i) {
				if (cns[i] != null && cns[i].trim().length() > 0) {
					Class<?> cls = Class.forName(cns[i].trim());
					list.add((AfterBeanFactory) cls.newInstance());
				}
			}
		} catch (Throwable th) {
			this.log("create " + AfterBeanFactory.class.getName() + " instance error", th);
			failStart(th);
			return;
		}
		try {
			for (ListIterator<AfterBeanFactory> it = list.listIterator(); it.hasNext();) {
				it.next().handle(this.bf);
			}
		} catch (Throwable th) {
			this.log("invoke " + AfterBeanFactory.class.getName() + ".handle instance error", th);
			failStart(th);
			return;
		}

	}

	@Override
	public void init() throws ServletException {
		super.init();
		this.buildBeanFactoryConfig();
		this.buildBeanFactory();
		this.doAfterBeanFactory();
		this.buildWebHandlers();
	}

}
