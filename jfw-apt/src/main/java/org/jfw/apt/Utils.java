package org.jfw.apt;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.jfw.apt.annotation.Autowrie;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;
import org.jfw.apt.model.orm.Column;
import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.out.model.ClassBeanDefine;

public class Utils {
	private static Map<Class<?>, Class<?>> wrapClass = new HashMap<Class<?>, Class<?>>();
	private static Map<String, String> wrapClassName = new HashMap<String, String>();

	private Utils() {
	}

	public static void writeSetterBeforePart(StringBuilder sb, String objName, String attrName) {
		sb.append(objName.trim()).append(".set");
		String fed = attrName.trim();
		sb.append(fed.substring(0, 1).toUpperCase(Locale.US));
		if (fed.length() > 1)
			sb.append(fed.substring(1));
		sb.append("(");
	}

	public static void writeSetter(StringBuilder sb, String objName, String attrName, String valName) {
		writeSetterBeforePart(sb, objName, attrName);
		sb.append(valName.trim());
		sb.append(");\r\n");
	}

	public static boolean isPrimitive(String className) {
		return wrapClassName.containsKey(className);
	}

	public static OrmHandler getOrmHandler(Column col, Element ref) throws AptException {
		try {
			return col.getDataElement().getHandlerClass().newInstance();
		} catch (Exception ee) {
			String m = ee.getMessage();
			throw new AptException(ref, "can't create ormHandler instance:" + m == null ? "" : m);
		}
	}

	public static void checkArgument(boolean condition, String format, Object... args) {
		if (!condition)
			throw new IllegalArgumentException(String.format(format, args));
	}

	public static <T> T checkNotNull(T reference, String format, Object... args) {
		if (reference == null)
			throw new NullPointerException(String.format(format, args));
		return reference;
	}

	public static boolean isPrimitive(Class<?> clazz) {
		return wrapClass.containsKey(clazz);
	}

	public static String getWrapClass(String className) {
		return wrapClassName.get(className);
	}

	public static void addSqlToStringBuilder(String s, StringBuilder sb) {
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == '\\' || c == '"')
				sb.append("\\");
			sb.append(c);
		}
	}

	public static String emptyToNull(String str) {
		if (str == null || str.trim().length() == 0)
			return null;
		return str.trim();
	}

	public static String getClassName(Class<?> clazz) {
		if (clazz.isArray()) {
			return getClassName(clazz.getComponentType()) + "[]";
		}
		return clazz.getName();
	}

	// public static String getTypeName(Type type) {
	// if (type instanceof Class) {
	// Class<?> cl = (Class<?>) type;
	// if (cl.isArray()) {
	// return getTypeName(cl.getComponentType()) + "[]";
	// } else {
	// return cl.getName();
	// }
	// } else if (type instanceof GenericArrayType) {
	// return getTypeName(((GenericArrayType) type).getGenericComponentType()) +
	// "[]";
	// } else if (type instanceof ParameterizedType) {
	// StringBuilder sb = new StringBuilder();
	// ParameterizedType pt = (ParameterizedType) type;
	// sb.append(getTypeName(pt.getRawType())).append("<");
	// Type[] ts = pt.getActualTypeArguments();
	// for (int i = 0; i < ts.length; ++i) {
	// if (i != 0)
	// sb.append(",");
	// sb.append(getTypeName(ts[i]));
	// }
	// sb.append(">");
	// return sb.toString();
	// } else if (type instanceof java.lang.reflect.TypeVariable) {
	// StringBuilder sb = new StringBuilder();
	// java.lang.reflect.TypeVariable<?> tt =
	// (java.lang.reflect.TypeVariable<?>) type;
	// sb.append(tt.getName()).append(" extends ");
	// Type[] ts = tt.getBounds();
	// for (int i = 0; i < ts.length; ++i) {
	// if (i != 0)
	// sb.append(" & ");
	// sb.append(getTypeName(ts[i]));
	// }
	// return sb.toString();
	// } else if (type instanceof WildcardType) {
	//
	// java.lang.reflect.WildcardType wt = (java.lang.reflect.WildcardType)
	// type;
	// if (wt.getLowerBounds().length > 0) {
	// return "? super " + getTypeName(wt.getLowerBounds()[0]);
	// } else if (Object.class != wt.getUpperBounds()[0]) {
	// return "? extends " + getTypeName(wt.getUpperBounds()[0]);
	// } else {
	// return "?";
	// }
	//
	// }
	// return null;
	// }

	public static String classNameToInstanceName(String className) {
		return className.substring(0, 1).toLowerCase(Locale.ENGLISH) + className.substring(1);
	}

	public static String getSupportedClassName(Class<? extends OrmHandler> clazz)
			throws InstantiationException, IllegalAccessException {
		return getClassName(clazz.newInstance().supportsClass());
	}

	public static void checkPersistentObjectName(String name, Element ele) throws AptException {
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')))
				throw new AptException(ele, "invalid name");
		}
	}

	public static String javaNameConverToDbName(String name) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < name.length(); ++i) {
			char c = name.charAt(i);
			if (c >= 'A' && c <= 'Z' && i != 0) {
				sb.append("_").append(c);
			} else {
				sb.append(Character.toUpperCase(c));
			}
		}
		return sb.toString();
	}

	public static PackageElement getPackage(Element type) {
		while (type.getKind() != ElementKind.PACKAGE) {
			type = type.getEnclosingElement();
		}
		return (PackageElement) type;
	}

	// public static String getReturnTypeName(TypeMirror t, Element ele) throws
	// AptException {
	// TypeKind td = t.getKind();
	// if (td == TypeKind.BOOLEAN) {
	// return "boolean";
	// } else if (td == TypeKind.BYTE) {
	// return "byte";
	// } else if (td == TypeKind.SHORT) {
	// return "short";
	// } else if (td == TypeKind.INT) {
	// return "int";
	// } else if (td == TypeKind.LONG) {
	// return "long";
	// } else if (td == TypeKind.CHAR) {
	// return "char";
	// } else if (td == TypeKind.FLOAT) {
	// return "float";
	// } else if (td == TypeKind.DOUBLE) {
	// return "double";
	// } else if (td == TypeKind.DOUBLE) {
	// return "double";
	// } else if (td == TypeKind.VOID) {
	// return "void";
	// } else if (td == TypeKind.ARRAY) {
	// return getReturnTypeName(((ArrayType) t).getComponentType(), ele) + "[]";
	// } else if (td == TypeKind.DECLARED) {
	// DeclaredType dt = (DeclaredType) t;
	// String name = ((TypeElement)
	// dt.asElement()).getQualifiedName().toString();
	//
	// List<? extends TypeMirror> list = dt.getTypeArguments();
	// if (list.isEmpty())
	// return name;
	// ArrayList<String> as = new ArrayList<String>();
	// for (int i = 0; i < list.size(); ++i) {
	// TypeMirror tm = list.get(i);
	// if (tm.getKind().isPrimitive() || tm.getKind() == TypeKind.VOID) {
	// throw new AptException(ele, "invalid ParameterizedType");
	// }
	// as.add(getReturnTypeName(tm, ele));
	// }
	// StringBuilder sb = new StringBuilder();
	// sb.append(name).append("<");
	// for (int i = 0; i < as.size(); ++i) {
	// if (i != 0)
	// sb.append(",");
	// sb.append(as.get(i));
	// }
	// sb.append(">");
	// return sb.toString();
	// } else if (td == TypeKind.TYPEVAR) {
	// TypeParameterElement element = (TypeParameterElement) ((TypeVariable)
	// t).asElement();
	//
	// List<String> strs = new ArrayList<String>();
	// for (TypeMirror typeMirror : element.getBounds()) {
	// String s = getReturnTypeName(typeMirror, ele);
	// if (s.equals("java.lang.Object"))
	// continue;
	// strs.add(s);
	// }
	// StringBuilder sb = new StringBuilder();
	//
	// for (int i = 0; i < strs.size(); ++i) {
	// if (i == 0) {
	// sb.append(element.getSimpleName().toString()).append(" extends ");
	// } else {
	// sb.append(" & ");
	// }
	// sb.append(strs.get(i));
	// }
	// return sb.toString();
	// } else if (td == TypeKind.WILDCARD) {
	// WildcardType mirror = (WildcardType) t;
	// TypeMirror extendsBound = mirror.getExtendsBound();
	// if (extendsBound == null) {
	// TypeMirror superBound = mirror.getSuperBound();
	// if (superBound == null) {
	// return "?";
	// } else {
	// return "? super " + getReturnTypeName(superBound, ele);
	// }
	// } else {
	// String cn = getReturnTypeName(extendsBound, ele);
	// if ("java.lang.Object".equals(cn))
	// return "?";
	// return "? extends " + cn;
	// }
	// } else {
	// throw new AptException(ele, "unknow exception for TypeKind");
	// }
	// }

	public static Set<Modifier> convert(int modifiers) {
		Set<Modifier> result = new LinkedHashSet<Modifier>();
		if (java.lang.reflect.Modifier.isAbstract(modifiers)) {
			result.add(Modifier.ABSTRACT);
		}
		if (java.lang.reflect.Modifier.isFinal(modifiers)) {
			result.add(Modifier.FINAL);
		}
		if (java.lang.reflect.Modifier.isNative(modifiers)) {
			result.add(Modifier.NATIVE);
		}
		if (java.lang.reflect.Modifier.isPrivate(modifiers)) {
			result.add(Modifier.PRIVATE);
		}
		if (java.lang.reflect.Modifier.isProtected(modifiers)) {
			result.add(Modifier.PROTECTED);
		}
		if (java.lang.reflect.Modifier.isPublic(modifiers)) {
			result.add(Modifier.PUBLIC);
		}
		if (java.lang.reflect.Modifier.isStatic(modifiers)) {
			result.add(Modifier.STATIC);
		}
		if (java.lang.reflect.Modifier.isStrict(modifiers)) {
			result.add(Modifier.STRICTFP);
		}
		if (java.lang.reflect.Modifier.isSynchronized(modifiers)) {
			result.add(Modifier.SYNCHRONIZED);
		}
		if (java.lang.reflect.Modifier.isTransient(modifiers)) {
			result.add(Modifier.TRANSIENT);
		}
		if (java.lang.reflect.Modifier.isVolatile(modifiers)) {
			result.add(Modifier.VOLATILE);
		}
		return result;
	}

	public static Object getReturnValueOnAnnotation(String methodName, AnnotationMirror annotation) {
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotation.getElementValues()
				.entrySet()) {
			ExecutableElement ee = entry.getKey();
			if (ee.getSimpleName().toString().equals(methodName)) {
				return entry.getValue().getValue();
			}
		}
		for (Element ele : annotation.getAnnotationType().asElement().getEnclosedElements()) {
			if (ele.getKind() == ElementKind.METHOD) {
				ExecutableElement ee = (ExecutableElement) ele;
				if (ee.getSimpleName().toString().equals(methodName) && (null != ee.getDefaultValue())) {
					return ee.getDefaultValue().getValue();
				}
			}
		}
		return null;
	}

	public static boolean isJavacClass(Object obj, Class<?> clazz) {
		if (obj == null)
			return false;

		if ((obj instanceof Class) && (clazz.isAssignableFrom((Class<?>) obj)))
			return true;
		try {
			return clazz.isAssignableFrom(Class.forName(obj.toString()));
		} catch (Exception e) {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> Class<T> getClass(Object obj, Class<T> clazz) {
		if (obj == null)
			return null;
		if ((obj instanceof Class) && (clazz.isAssignableFrom((Class<?>) obj)))
			return (Class<T>) obj;
		try {
			Class<?> cls = Class.forName(obj.toString());
			if (clazz.isAssignableFrom(cls)) {
				return (Class<T>) cls;
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T getObjectWithClassNameOrClass(Object obj, Class<T> clazz) {
		if (obj == null)
			return null;
		Class<?> cls = null;
		if ((obj instanceof Class) && (clazz.isAssignableFrom((Class<?>) obj))) {
			cls = (Class<?>) obj;
		} else {
			try {
				cls = Class.forName(obj.toString());
				if (!clazz.isAssignableFrom(cls)) {
					cls = null;
				}
			} catch (Exception e) {
				return null;
			}
		}

		try {
			return (T) cls.newInstance();
		} catch (Exception e) {
			return null;
		}
	}

	public static String join(String separator, List<String> parts) {
		if (parts.isEmpty())
			return "";
		StringBuilder result = new StringBuilder();
		result.append(parts.get(0));
		for (int i = 1; i < parts.size(); i++) {
			result.append(separator).append(parts.get(i));
		}
		return result.toString();
	}

	public static void fillAutowrieElement(Map<String, String> map, TypeElement typeEle) {
		for (Element ele : typeEle.getEnclosedElements()) {
			if(ele.getModifiers().contains(Modifier.STATIC)) continue;
			if(ele.getKind()==ElementKind.FIELD){
				Autowrie aw = ele.getAnnotation(Autowrie.class);
				if(null == aw) continue;
				String name = ele.getSimpleName().toString();
				if(map.containsKey(name)) continue;
				String refName = aw.value();
				if(refName==null || refName.trim().length()==0){
					TypeMirror tm = ele.asType();
					if(tm.getKind()!=TypeKind.DECLARED) continue;
					refName = TypeName.get(tm).toString().replaceAll("\\.","_");					
				}
				map.put(name, refName.trim());				
			}else if(ele.getKind()==ElementKind.METHOD){
				Autowrie aw = ele.getAnnotation(Autowrie.class);
				if(aw==null) continue;
				
				ExecutableElement ee = (ExecutableElement) ele;
				if(!TypeName.get(ee.getReturnType()).equals(TypeName.VOID)) continue;
				String mn = ee.getSimpleName().toString();
				if((mn.length()<4)||(!mn.startsWith("set"))) continue;
				List<? extends VariableElement> params = ee.getParameters();
				if(params.size()!=1) continue;
				mn = mn.substring(3);
				if(mn.length()==1) mn = mn.toLowerCase(Locale.US);
				else
					mn = mn.substring(0,1).toLowerCase(Locale.US)+mn.substring(1);
				
				if(map.containsKey(mn)) continue;
				String refName = aw.value();
				if(refName==null || refName.trim().length()==0){
					VariableElement param = params.get(0);
					refName =TypeName.get(param.asType()).toString().replaceAll("\\.","_");					
				}
				map.put(mn,refName);
			}
		}
		

		TypeMirror tm = typeEle.getSuperclass();
		if (tm instanceof NoType)
			return;
		if (tm.getKind() != TypeKind.DECLARED)
			return;

		try {
			DeclaredType dt = (DeclaredType) tm;
			fillAutowrieElement(map,(TypeElement) dt.asElement());
		} catch (Throwable th) {
			return;
		}
	}
	
	
	public static void buildAtuowrieProperty(ClassBeanDefine cbd ,TypeElement ele){
		Map<String,String> map = new HashMap<String,String>();
		fillAutowrieElement(map, ele);
		for(Map.Entry<String,String> entry:map.entrySet()){
			cbd.setRefAttribute(entry.getKey(),entry.getValue());
		}
		
	}

	static {
		wrapClass.put(int.class, Integer.class);
		wrapClass.put(byte.class, Byte.class);
		wrapClass.put(short.class, Short.class);
		wrapClass.put(float.class, Float.class);
		wrapClass.put(double.class, Double.class);
		wrapClass.put(boolean.class, Boolean.class);
		wrapClass.put(char.class, Character.class);
		wrapClass.put(long.class, Long.class);
		for (Map.Entry<Class<?>, Class<?>> en : wrapClass.entrySet()) {
			wrapClassName.put(en.getKey().getName(), en.getValue().getName());
		}
	}

}
