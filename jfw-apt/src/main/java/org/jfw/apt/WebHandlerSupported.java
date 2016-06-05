package org.jfw.apt;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import org.jfw.apt.annotation.ThreadSafe;
import org.jfw.apt.annotation.web.RequestMapping;
import org.jfw.apt.annotation.web.WebHandler;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;
import org.jfw.apt.model.web.RequestHandler;
import org.jfw.apt.model.web.RequestMappingCodeGenerator;
import org.jfw.apt.model.web.RequestMethod;
import org.jfw.apt.out.model.BeanConfig;
import org.jfw.apt.out.model.ClassBeanDefine;

public class WebHandlerSupported implements CodeGenerateHandler {
	protected Map<String, Object> env;
	protected TypeElement ref;
	protected Messager messager;
	protected BeanConfig beanConfig;
	protected Filer filer;
	protected String packageName = "";
	protected String className;
	protected String uri = "";
	protected WebHandler wh;
	protected int methodSeq = 0;
	protected String defaultHandlerClassName;

	protected StringBuilder sb;
	private boolean threadSafe;

	@SuppressWarnings("unchecked")
	private List<Class<? extends RequestHandler>> getHandlerClass() throws AptException {

		List<Class<? extends RequestHandler>> result = new ArrayList<Class<? extends RequestHandler>>();
		Class<? extends RequestHandler>[] clss = null;
		try {
			clss = wh.handler();
			for (int i = 0; i < clss.length; ++i) {
				result.add(clss[i]);
			}
		} catch (MirroredTypesException e) {
			List<? extends TypeMirror> list = e.getTypeMirrors();
			for (int i = 0; i < list.size(); ++i) {
				TypeName tn = TypeName.get(list.get(i));
				try {
					result.add((Class<? extends RequestHandler>) Class.forName(tn.toString()));
				} catch (Exception e1) {
					throw new AptException(ref, "unknow Exception:" + e1.getMessage());
				}
			}
		}
		return result;

	}

	public RequestHandler[] createHandler() throws AptException {
		List<Class<? extends RequestHandler>> list = this.getHandlerClass();
		RequestHandler[] handlers = new RequestHandler[list.size()];
		for (int i = 0; i < list.size(); ++i) {
			try {
				handlers[i] = list.get(i).newInstance();
			} catch (Exception ee) {
				String m = ee.getMessage();
				throw new AptException(ref, "can't create RequestHandler instance:" + m == null ? "" : m);
			}
		}
		return handlers;
	}

	@Override
	public void setEnv(Map<String, Object> env) {
		this.env = env;
		this.messager = (Messager) env.get(Messager.class.getName());
		this.filer = (Filer) env.get(Filer.class.getName());
		this.beanConfig = ((JfwProccess) env.get(JfwProccess.class.getName())).getBeanConfig();
	}

	public static String getAnnotationClassName(AnnotationMirror am) {
		TypeElement type = (TypeElement) am.getAnnotationType().asElement();
		return type.getQualifiedName().toString();
	}

	public String getTargetClassPackage() {
		if (this.packageName.length() == 0)
			return "";
		return this.packageName.substring(0, this.packageName.length() - 1);
	}

	public String getTargetClassName() {
		return this.className + "WebHandler";
	}

	public String getTargetQualifiedName() {
		return this.packageName + this.className + "WebHandler";
	}

	protected void writeContent() throws AptException {
		for (Element ele : this.ref.getEnclosedElements()) {
			if (ele.getKind() == ElementKind.METHOD) {
				RequestMapping rm = ele.getAnnotation(RequestMapping.class);
				if (rm == null)
					continue;

				String vuri = Utils.emptyToNull(rm.value());
				if (vuri == null)
					vuri = "";
				vuri = this.uri == null ? "" : this.uri + vuri;
				if (vuri.length() == 0 || !vuri.startsWith("/"))
					throw new AptException(ele, "invalid annotation @RequestMapping(value)");

				RequestMappingCodeGenerator rmcg = new RequestMappingCodeGenerator();
				rmcg.fillMeta((ExecutableElement) ele);
				rmcg.setWebHandlerSupported(this);
				rmcg.setUri(vuri);
				rmcg.writeMethod(sb);

				for (RequestMethod m : rm.method()) {

					ClassBeanDefine wre = this.beanConfig.addEntryBeanByClass("org.jfw.util.web.model.WebRequestEntry",
							null);
					wre.setRefAttribute("webHandler", this.getTargetQualifiedName().trim().replaceAll("\\.", "_"));
					wre.setString("uri", vuri);
					wre.setString("methodName", rmcg.getWebMethodName());
					wre.setString("methodType", m.toString());
					wre.joinGroup("jfwmvc");

				}
			}
		}
	}

	protected void writeInstanceVariable() {
		sb.append("@org.jfw.apt.annotation.Autowrie(\"dataSource\")\r\n");
		sb.append(" private javax.sql.DataSource dataSource = null;\r\n");
		sb.append("public void setDataSource(javax.sql.DataSource dataSource){this.dataSource = dataSource;}");
		if (!this.threadSafe) {
			sb.append("@org.jfw.apt.annotation.Autowrie(\"")
					.append(this.ref.getQualifiedName().toString().trim() + "@factroy").append("\")\r\n");
			sb.append(" private org.jfw.util.comm.ObjectFactory handlerFactory = null;\r\n")
					.append("public void setHandlerFactory(org.jfw.util.comm.ObjectFactory paHandlerFactory){\r\n")
					.append("    this.handlerFactory = paHandlerFactory;\r\n}\r\n");
		} else {
			sb.append("@org.jfw.apt.annotation.Autowrie(\"").append(this.ref.getQualifiedName().toString())
					.append("\")\r\n");
			sb.append("private ").append(this.packageName).append(this.className).append(" handler = null;\r\n");
			sb.append("public void setHandler(").append(this.packageName).append(this.className)
					.append(" value)\r\n{\r\n handler = value;\r\n}\r\n");
		}

	}

	protected void writeFile() throws AptException {
		String tpn = this.getTargetClassPackage();
		String tcn = this.getTargetClassName();

		try {

			this.sb = new StringBuilder();
			if (this.packageName.length() > 0) {
				sb.append("package ").append(this.getTargetClassPackage()).append(";\r\n");
			}
			sb.append("@org.jfw.apt.annotation.Bean(\"").append(this.getTargetQualifiedName()).append("\")\r\n");
			sb.append("public class ").append(this.getTargetClassName()).append(" {\r\n");
			this.writeInstanceVariable();
			this.writeContent();
			sb.append("\r\n}");
			JavaFileObject jfo = this.filer.createSourceFile(tpn.length() == 0 ? "" : (tpn + ".") + tcn, this.ref);
			Writer w = jfo.openWriter();
			try {
				w.write(sb.toString());
			} finally {
				w.close();
			}
		} catch (IOException e) {
			throw new AptException(this.ref, "write java sorce file(" + tpn + "." + tcn + ") error:" + e.getMessage());
		}
	}

	@Override
	public void handle(TypeElement ref, AnnotationMirror am, Object annotationObj) throws AptException {

		this.ref = ref;
		wh = ref.getAnnotation(WebHandler.class);
		try {
			try {
				this.defaultHandlerClassName = wh.defaultHandlerClass().getName();
			} catch (MirroredTypeException e) {
				this.defaultHandlerClassName = TypeName.get(e.getTypeMirror()).toString();
			}
		} catch (Exception e) {
			this.defaultHandlerClassName = null;
		}

		String vuri = Utils.emptyToNull(wh.value());
		DeclaredType dt = (DeclaredType) ref.asType();
		if (!dt.getTypeArguments().isEmpty())
			throw new AptException(ref, "@WebHandler annotation target not Parameterized class");
		if (vuri == null) {
			vuri = "";
		} else {
			if (!vuri.startsWith("/")) {
				throw new AptException(ref, "@WebHandler'value must be startsWith '/'");
			}
		}
		this.uri = vuri;

		ThreadSafe ts = this.ref.getAnnotation(ThreadSafe.class);
		this.threadSafe = null == ts || ts.value();
		List<Class<? extends RequestHandler>> list = this.getHandlerClass();
		if (list.isEmpty())
			throw new AptException(ref, "@WebHandler'handler not null or empty array");
		String cn = ref.getQualifiedName().toString();
		if (this.defaultHandlerClassName == null || (this.defaultHandlerClassName.equals("java.lang.Object")))
			this.defaultHandlerClassName = cn;
		int index = cn.lastIndexOf(".");
		if (index > 0) {
			this.packageName = cn.substring(0, index + 1);
			this.className = cn.substring(index + 1);
		} else {
			this.className = cn;
		}
		// MvcBean mvc = MvcBean.build(cn, this.defaultHandlerClassName,
		// threadSafe);
		// mvcBeans.add(mvc);

		// this.addToBeanConfig();

		this.writeFile();
	}

	// private void addToBeanConfig() {
	// this.cbd = beanConfig.addServiceBeanByClass(this.packageName +
	// this.getTargetClassName(), null);
	//
	// if (this.threadSafe) {
	// ClassBeanDefine h =
	// this.beanConfig.addServiceBeanByClass(this.defaultHandlerClassName,
	// null);
	// this.cbd.setRefAttribute("handler", h.getId());
	// } else {
	// ClassBeanDefine of =
	// this.beanConfig.addServiceBeanByClass("org.jfw.util.comm.ClassCreateFactory",
	// (this.defaultHandlerClassName+ "@factroy").replaceAll("\\.", "_"));
	// of.setClass("clazz", this.packageName + this.className);
	// this.cbd.setRefAttribute("handlerFactory", of.getId());
	// }
	//
	// }

	public String getServiceMethodName() {
		return "ws_" + (++this.methodSeq);
	}

	public boolean isThreadSafe() {
		return threadSafe;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public boolean isManagedByBeanFactory() {
		return true;
	}

}
