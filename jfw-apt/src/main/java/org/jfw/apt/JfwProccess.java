package org.jfw.apt;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.jfw.apt.annotation.Bean;
import org.jfw.apt.annotation.BuildBean;
import org.jfw.apt.annotation.FactoryBean;
import org.jfw.apt.annotation.ThreadSafe;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;
import org.jfw.apt.model.orm.OrmDefine;
import org.jfw.apt.model.orm.PersistentObject;
import org.jfw.apt.out.model.BeanConfig;
import org.jfw.apt.out.model.ClassBeanDefine;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class JfwProccess extends javax.annotation.processing.AbstractProcessor {
	private BeanConfig beanConfig = new BeanConfig();
	private Messager messager;
	private Filer filer;
	private Map<String, Object> attributes = new HashMap<String, Object>();
	private Set<? extends TypeElement> annotations;
	private RoundEnvironment roundEnv;
	private Set<? extends Element> rootElements;

	private Element currentElement = null;

	public void changeElement(Element ele) {
		if (ele != null)
			this.currentElement = ele;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
		this.messager = processingEnv.getMessager();
		this.filer = processingEnv.getFiler();
	}

	private void handleOrm() throws AptException {
		OrmDefine od = new OrmDefine();
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			this.changeElement(ele);
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.VirtualTable.class)) {
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.VirtualTable.class));
				od.addPersistentObject(po);
			}
		}
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.Table.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.Table.class));
				od.addPersistentObject(po);
			}
		}
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendTable.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendTable.class));
				od.addPersistentObject(po);
			}
		}

		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.View.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.View.class));
				od.addPersistentObject(po);
			}
		}
		for (Element ele : this.rootElements) {
			if (ele.getKind() != ElementKind.CLASS)
				continue;
			if (null != ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendView.class)) {
				this.changeElement(ele);
				PersistentObject po = PersistentObject.build((TypeElement) ele,
						ele.getAnnotation(org.jfw.apt.annotation.orm.ExtendView.class));
				od.addPersistentObject(po);
			}
		}
		od.initPersistentObjects();
		this.setAttribute(OrmDefine.class.getName(), od);
		String sql = od.generateAllDDL();
		if (sql.length() > 0)
			this.saveResourceFile("jfw_table.sql", sql);
		// od.warnMessage(messager);

	}

	private void handle(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws AptException {
		this.rootElements = roundEnv.getRootElements();
		this.annotations = annotations;
		this.roundEnv = roundEnv;
		this.handleOrm();
		this.handleCodeGenerateHandler();
		this.handleBean();
	}

	private void handleBuildBean(TypeElement ele) throws AptException {
		for (Element el : ele.getEnclosedElements()) {
			BuildBean bd = el.getAnnotation(BuildBean.class);
			if(bd==null)continue;
			if (el.getKind() != ElementKind.METHOD) {
				throw new AptException(el, "@BuildBean must embellish public static method");
			}
			this.currentElement = el;
			ExecutableElement ee = (ExecutableElement) el;

			TypeName tn = TypeName.get(ee.getReturnType());
			if (tn.isPrimitive())
				throw new AptException(el, "method with @BuildBean must return Reference Class");
			Set<Modifier> ms = ee.getModifiers();
			if (!ms.contains(Modifier.STATIC) || !ms.contains(Modifier.PUBLIC))
				throw new AptException(el, "@BuildBean must embellish public static method");
			if (!ee.getParameters().isEmpty())
				throw new AptException(el, "@BuildBean must embellish empty paramter method");
			String nm = bd.value();
			if (nm == null || nm.trim().length() == 0) {
				nm=ee.getReturnType().toString()+"@builder";
			}
			nm = nm.trim().replaceAll("\\.", "_");
			this.beanConfig.addServiceBeanByBuilder(ele.getQualifiedName().toString(), ee.getSimpleName().toString(), nm);

		}
	}

	private void handleBean() throws AptException {
		for (Element ele : this.rootElements) {
			this.currentElement = ele;
			if (ele.getKind() == ElementKind.CLASS || ele.getKind() == ElementKind.INTERFACE) {
				TypeElement te = (TypeElement) ele;
				handleBuildBean(te);
				Bean bean = ele.getAnnotation(Bean.class);
				FactoryBean fb = ele.getAnnotation(FactoryBean.class);
				if (bean != null && fb != null)
					throw new AptException(ele, "@Bean @FactoryBean choose one");
				if (bean == null && fb == null)
					continue;
				if (bean != null) {
					String bn = bean.value();
					if (bn == null || bn.trim().length() == 0) {
						bn = te.getQualifiedName().toString();
					}
					bn = bn.trim().replaceAll("\\.", "_");
					ClassBeanDefine cbd = this.beanConfig.addServiceBeanByClass(te.getQualifiedName().toString(), bn);
					Utils.buildAtuowrieProperty(cbd, te);
				} else {
					String bn = fb.value();
					if (bn == null || bn.trim().length() == 0) {
						bn = te.getQualifiedName().toString().trim() + "@factory";
					}
					bn = bn.trim().replaceAll("\\.", "_");
					ClassBeanDefine cbd = this.beanConfig.addServiceBeanByClass("org.jfw.util.comm.ClassCreateFactory",
							bn);
					cbd.setAttribute("clazz", te.getQualifiedName().toString(), "java.lang.Class");
				}
			}
		}

	}

	// private void warnMethodInfo(TypeElement ref) throws AptException{
	// for(Element ele:ref.getEnclosedElements()){
	// if(ele.getKind()!= ElementKind.METHOD) continue;
	// ExecutableElement el =(ExecutableElement)ele;
	// this.warnMethodInfo((ExecutableElement)ele);
	//
	// }
	//
	// }

	// private void warnMethodInfo(ExecutableElement ref) throws AptException{
	// StringBuilder sb = new StringBuilder();
	// Set<Modifier> ms = ref.getModifiers();
	// for( Modifier m:ms) sb.append(m.toString()).append(" ");
	//
	// sb.append(Utils.getReturnTypeName(ref.getReturnType(), ref)).append(" ");
	// sb.append(ref.getSimpleName().toString()).append("(");
	//
	// List<? extends VariableElement> eles = ref.getParameters();
	// for(int i = 0 ; i < eles.size() ; ++i){
	// VariableElement ele = eles.get(i);
	// if(i !=0) sb.append(",");
	// sb.append(Utils.getReturnTypeName(ele.asType(), ele)).append("
	// ").append(ele.getSimpleName().toString());
	// }
	// sb.append(")");
	//
	// List<? extends TypeMirror> ths = ref.getThrownTypes();
	// for(int i = 0 ; i < ths.size() ; ++i){
	// if(i==0){
	// sb.append(" throws ");
	// }else{
	// sb.append(",");
	// }
	// sb.append(Utils.getReturnTypeName(ths.get(i), ref));
	// }
	//
	// this.messager.printMessage(Kind.WARNING, sb.toString(),ref);
	// }

	private void handleCodeGenerateHandler() throws AptException {
		for (Element ele : this.rootElements) {
			if ((ele.getKind() != ElementKind.CLASS) && (ele.getKind() != ElementKind.INTERFACE))
				continue;
			if (!(ele instanceof TypeElement))
				continue;
			TypeElement el = (TypeElement) ele;
			if (el.getNestingKind().isNested())
				continue;

			if (ele.getEnclosingElement() != null && el.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
				continue;
			}

			List<? extends TypeParameterElement> list = el.getTypeParameters();
			if (list != null && list.size() > 0)
				continue;
			List<? extends AnnotationMirror> ans = el.getAnnotationMirrors();
//			System.out.println(el.toString());
			for (AnnotationMirror anm : ans) {
				Object obj = Utils.getReturnValueOnAnnotation("handlerClass", anm);
				Class<CodeGenerateHandler> cghcls = Utils.getClass(obj, CodeGenerateHandler.class);
				if (cghcls != null) {
					CodeGenerateHandler cgh = null;
					try {
						cgh = cghcls.newInstance();
					} catch (Exception e) {
						throw new AptException(ele,
								"create Object instance with " + cghcls.getName() + "error:" + e.getMessage());
					}
					cgh.setEnv(this.attributes);
					this.changeElement(ele);
					cgh.handle(el, anm, this.getAnnotationObj(el, anm));
					if (cgh.isManagedByBeanFactory()) {
						ThreadSafe ts = el.getAnnotation(ThreadSafe.class);
						if (ts == null || ts.value()) {
							ClassBeanDefine cbd = this.beanConfig
									.addServiceBeanByClass(el.getQualifiedName().toString(), null);
							Utils.buildAtuowrieProperty(cbd, el);

						} else {
							ClassBeanDefine cbd = this.beanConfig.addServiceBeanByClass(
									"org.jfw.util.comm.ClassCreateFactory",
									(el.getQualifiedName().toString().trim() + "@factroy").replaceAll("\\.", "_"));
							cbd.setAttribute("clazz", el.getQualifiedName().toString().replaceAll("\\.","_"), "java.lang.Class");
						}
					}

					break;
				}

			}
		}

	}

	public void saveResourceFile(String fileName, String fileContent) throws AptException {
		try {
			FileObject fo = this.filer.createResource(javax.tools.StandardLocation.SOURCE_OUTPUT, "", fileName,
					(Element[]) null);
			OutputStream os = fo.openOutputStream();
			try {
				os.write(fileContent.getBytes("UTF-8"));
				os.flush();
			} finally {
				os.close();
			}
		} catch (Exception e) {
			throw new AptException(this.currentElement, "save resource file[" + fileName + "] error:" + e.getMessage());
		}
		try {
			FileObject fo = this.filer.createResource(javax.tools.StandardLocation.CLASS_OUTPUT, "", fileName,
					(Element[]) null);
			OutputStream os = fo.openOutputStream();
			try {
				os.write(fileContent.getBytes("UTF-8"));
				os.flush();
			} finally {
				os.close();
			}
		} catch (Exception e) {
			throw new AptException(this.currentElement, "save resource file[" + fileName + "] error:" + e.getMessage());
		}

	}

	@SuppressWarnings("unchecked")
	private Object getAnnotationObj(Element ele, AnnotationMirror am) {
		try {
			TypeElement type = (TypeElement) am.getAnnotationType().asElement();
			String cn = type.getQualifiedName().toString();
			Class<? extends Annotation> cls = (Class<? extends Annotation>) Class.forName(cn);
			return ele.getAnnotation(cls);
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		this.attributes.clear();
		this.setAttribute(JfwProccess.class.getName(), this);
		this.setAttribute(RoundEnvironment.class.getName(), roundEnv);
		this.setAttribute(Messager.class.getName(), this.messager);
		this.setAttribute(Filer.class.getName(), this.filer);
		try {
			this.handle(annotations, roundEnv);

			Set<? extends Element> allInputClass = roundEnv.getRootElements();
			if (allInputClass == null || allInputClass.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				this.beanConfig.appendTo(sb);
				this.saveResourceFile("beanConfig.properties", sb.toString());
			}
		} catch (AptException e) {
			this.messager.printMessage(Kind.ERROR, e.getMessage(), e.getEle());
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
			String m = e.getMessage();
			if (m == null)
				m = "nullException";
			this.messager.printMessage(Kind.ERROR, m, this.currentElement);
		}

		return true;
	}

	public Object getAttribute(String key) {
		return this.attributes.get(key);
	}

	public void setAttribute(String key, Object value) {
		this.attributes.put(key, value);
	}

	private static Iterable<JavaFileObject> getSourceFiles(String p_path) throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager files = compiler.getStandardFileManager(null, null, null);

		files.setLocation(StandardLocation.SOURCE_PATH, Arrays.asList(new File(p_path)));

		Set<javax.tools.JavaFileObject.Kind> fileKinds = Collections.singleton(javax.tools.JavaFileObject.Kind.SOURCE);
		return files.list(StandardLocation.SOURCE_PATH, "", fileKinds, true);
	}

	public static void main(String[] args) throws Exception {
		String source = "E:\\EclipseProject\\JFW\\aptTest2\\src";

		Iterable<JavaFileObject> files = getSourceFiles(source);

		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

		CompilationTask task = compiler.getTask(new PrintWriter(System.out), null, null, null, null, files);
		task.setProcessors(Arrays.asList(new JfwProccess()));

		task.call();
	}

	public Messager getMessager() {
		return messager;
	}

	public Filer getFiler() {
		return filer;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public Set<? extends TypeElement> getAnnotations() {
		return annotations;
	}

	public RoundEnvironment getRoundEnv() {
		return roundEnv;
	}

	public BeanConfig getBeanConfig() {
		return this.beanConfig;
	}

}
