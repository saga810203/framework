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
	protected String uri = "";
	protected WebHandler wh;
	protected int methodSeq = 0;
	protected String sourceClassname;
	protected String targetClassname;

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
					wre.setRefAttribute("webHandler", this.getTargetClassname().trim().replaceAll("\\.", "_"));
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
					.append(this.ref.getQualifiedName().toString().trim().replaceAll("\\.", "_") + "@factroy")
					.append("\")\r\n");
			sb.append(" private org.jfw.util.comm.ObjectFactory handlerFactory = null;\r\n")
					.append("public void setHandlerFactory(org.jfw.util.comm.ObjectFactory paHandlerFactory){\r\n")
					.append("    this.handlerFactory = paHandlerFactory;\r\n}\r\n");
		} else {
			sb.append("@org.jfw.apt.annotation.Autowrie(\"")
					.append(this.ref.getQualifiedName().toString().replaceAll("\\.", "_")).append("\")\r\n");
			sb.append("private ").append(this.sourceClassname).append(" handler = null;\r\n");
			sb.append("public void setHandler(").append(this.sourceClassname)
					.append(" value)\r\n{\r\n handler = value;\r\n}\r\n");
		}

	}

	protected void writeFile() throws AptException {
		try {

			this.sb = new StringBuilder();
			String packageName = this.getTargetPackageName();
			if (packageName.length() > 0) {
				sb.append("package ").append(packageName).append(";\r\n");
			}
			sb.append("@org.jfw.apt.annotation.Bean(\"").append(this.getTargetClassname()).append("\")\r\n");
			sb.append("public class ").append(this.getTargetSimpleClassname()).append(" {\r\n");
			this.writeInstanceVariable();
			this.writeContent();
			sb.append("\r\n}");
			JavaFileObject jfo = this.filer.createSourceFile(this.getTargetClassname(), this.ref);
			Writer w = jfo.openWriter();
			try {
				w.write(sb.toString());
			} finally {
				w.close();
			}
		} catch (IOException e) {
			throw new AptException(this.ref,
					"write java sorce file(" + this.getTargetClassname() + ") error:" + e.getMessage());
		}
	}

	@Override
	public void handle(TypeElement ref, AnnotationMirror am, Object annotationObj) throws AptException {

		this.ref = ref;
		wh = ref.getAnnotation(WebHandler.class);
		this.sourceClassname = this.ref.getQualifiedName().toString();

		this.targetClassname = this.sourceClassname + "WebHandler";

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

		this.writeFile();
	}

	public String getServiceMethodName() {
		return "ws_" + (++this.methodSeq);
	}

	public boolean isThreadSafe() {
		return threadSafe;
	}

	@Override
	public boolean isManagedByBeanFactory() {
		return true;
	}

	@Override
	public String getSourceClassname() {
		return this.sourceClassname;
	}

	@Override
	public String getTargetClassname() {
		return this.targetClassname;
	}

	public String getSourcePackageName() {
		int index = this.sourceClassname.lastIndexOf(".");
		if (index == -1)
			return "";
		return this.sourceClassname.substring(0, index);
	}

	public String getSourceSimpleClassname() {
		int index = this.sourceClassname.lastIndexOf(".");
		if (index == -1)
			return this.sourceClassname;
		++index;
		return this.sourceClassname.substring(index);
	}

	public String getTargetPackageName() {
		int index = this.targetClassname.lastIndexOf(".");
		if (index == -1)
			return "";
		return this.targetClassname.substring(0, index);
	}

	public String getTargetSimpleClassname() {
		int index = this.targetClassname.lastIndexOf(".");
		if (index == -1)
			return this.targetClassname;
		++index;
		return this.targetClassname.substring(index);
	}

}
