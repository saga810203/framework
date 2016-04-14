/*
 * Copyright (C) 2014 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jfw.apt.model.core;

import static javax.lang.model.element.NestingKind.MEMBER;
import static javax.lang.model.element.NestingKind.TOP_LEVEL;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.jfw.apt.Utils;

/** A fully-qualified class name for top-level and member classes. */
public final class ClassName extends TypeName {
	private static Map<String, ClassName> cache = new HashMap<String, ClassName>();

	public static final ClassName OBJECT = ClassName.get(Object.class);

	/**
	 * From top to bottom. This will be ["java.util", "Map", "Entry"] for
	 * {@link Map.Entry}.
	 */
	final List<String> names;
	final String packageName;
	protected Class<?> refClass = null;
	protected TypeElement refElement = null;

	private ClassName(String packageName, List<String> names) {
		super();
		this.packageName = packageName;
		this.names = Collections.unmodifiableList(names);
		if (this.packageName != null) {
			this.keyword = packageName + "." + Utils.join(".", names);
		} else {
			this.keyword = Utils.join(".", names);
		}
	}

	public String getPackageName() {
		return this.packageName;
	}

	private void addSetter(Map<String, List<TypeName>> map, String name, TypeName tn) {
		List<TypeName> list = map.get(name);
		if (list == null) {
			list = new ArrayList<TypeName>();
			list.add(tn);
			map.put(name, list);
			return;
		}
		if (list.contains(tn))
			return;
		list.add(tn);
	}

	public Map<String, List<TypeName>> getAllSetter() {
		Map<String, List<TypeName>> map = new HashMap<String, List<TypeName>>();
		if (this.refClass != null) {
			for (Class<?> cl = refClass; cl != null; cl = cl.getSuperclass()) {
				for (Method method : cl.getDeclaredMethods()) {
					String name = method.getName();
					if (java.lang.reflect.Modifier.isStatic(method.getModifiers()))
						continue;
					if (!java.lang.reflect.Modifier.isPublic(method.getModifiers()))
						continue;
					if (name.length() < 4)
						continue;
					if(!name.startsWith("set")) continue;
					name = name.substring(3);
					if (name.length() == 1) {
						name = name.toLowerCase(Locale.US);
					} else {
						name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
					}

					TypeName tn;
					try {
						tn = TypeName.get(method.getGenericReturnType());
					} catch (Exception e) {
						continue;
					}
					if (!tn.equals(TypeName.VOID))
						continue;
					if (method.getGenericParameterTypes().length != 1)
						continue;
					try {
						tn = TypeName.get(method.getGenericParameterTypes()[0]);
					} catch (Exception e) {
						continue;
					}
					this.addSetter(map, name, tn);
				}
			}

		} else if (this.refElement != null) {
			TypeElement tm = this.refElement;
			while (true) {
				for (Element ele : tm.getEnclosedElements()) {
					if (ele.getKind() != ElementKind.METHOD)
						continue;
					ExecutableElement ee = (ExecutableElement) ele;
					if (ee.getModifiers().contains(Modifier.STATIC))
						continue;
					if (!ele.getModifiers().contains(Modifier.PUBLIC))
						continue;
					String name = ee.getSimpleName().toString();
					if (name.length() < 4)
						continue;
					if(!name.startsWith("set")) continue;
					name = name.substring(3);
					if (name.length() == 1) {
						name = name.toLowerCase(Locale.US);
					} else {
						name = name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
					}

					TypeName tn;
					try {
						tn = TypeName.get(ee.getReturnType());
					} catch (Exception e) {
						continue;
					}
					if (!tn.equals(TypeName.VOID))
						continue;

					if (ee.getParameters() == null || ee.getParameters().size() != 1)
						continue;
					try {
						tn = TypeName.get(ee.getParameters().get(0).asType());
					} catch (Exception e) {
						continue;
					}
					this.addSetter(map, name, tn);
				}
				TypeMirror ptm = tm.getSuperclass();
				if (ptm.getKind() != TypeKind.DECLARED)
					break;
				Element e = ((DeclaredType) ptm).asElement();
				if (e.getKind() != ElementKind.CLASS)
					break;
				tm = (TypeElement) e;
			}

		}
		return map;
	}

	/**
	 * Returns the enclosing class, like {@link Map} for {@code Map.Entry}.
	 * Returns null if this class is not nested in another class.
	 */
	public ClassName enclosingClassName() {
		if (names.size() == 1)
			return null;
		if (this.refClass != null) {
			return ClassName.get(this.refClass.getEnclosingClass());
		} else {
			return ClassName.get((TypeElement) this.refElement.getEnclosingElement());
		}

	}

	public String getSimpleName() {
		return names.get(names.size() - 1);
	}

	public static ClassName get(Class<?> clazz) {
		Class<?> refclass = clazz;
		Utils.checkNotNull(clazz, "clazz == null");
		Utils.checkArgument(!clazz.isPrimitive(), "primitive types cannot be represented as a ClassName");
		Utils.checkArgument(!void.class.equals(clazz), "'void' type cannot be represented as a ClassName");
		Utils.checkArgument(!clazz.isArray(), "array types cannot be represented as a ClassName");
		LinkedList<String> names = new LinkedList<String>();
		while (true) {
			names.addFirst(clazz.getSimpleName());
			Class<?> enclosing = clazz.getEnclosingClass();
			if (enclosing == null)
				break;
			clazz = enclosing;
		}
		String pn = null;
		int lastDot = clazz.getName().lastIndexOf('.');
		if (lastDot != -1)
			pn = clazz.getName().substring(0, lastDot);
		ClassName result = new ClassName(pn, names);
		ClassName rt = cache.get(result.toString());
		if (rt != null)
			return rt;
		result.refClass = refclass;
		cache.put(result.toString(), result);
		return result;
	}

	public static ClassName get(TypeElement element) {
		Utils.checkNotNull(element, "element == null");
		LinkedList<String> names = new LinkedList<String>();
		for (Element e = element; isClassOrInterface(e); e = e.getEnclosingElement()) {
			Utils.checkArgument(element.getNestingKind() == TOP_LEVEL || element.getNestingKind() == MEMBER,
					"unexpected type testing");
			names.addFirst(e.getSimpleName().toString());
		}
		String pn = Utils.emptyToNull(getPackage(element).getQualifiedName().toString());
		ClassName result = new ClassName(pn, names);
		result.refElement = element;
		return result;
	}

	private static boolean isClassOrInterface(Element e) {
		return e.getKind().isClass() || e.getKind().isInterface();
	}

	public boolean canInstance(){
		if(this.refClass!=null){
			int m = this.refClass.getModifiers();
			if(java.lang.reflect.Modifier.isAbstract(m)|| java.lang.reflect.Modifier.isFinal(m)|| java.lang.reflect.Modifier.isInterface(m))	return false;
			if(!java.lang.reflect.Modifier.isPublic(m)) return false;	
			Constructor<?> cn;
			try {
				cn = this.refClass.getConstructor(new Class[]{});
			} catch (Exception e) {
				return false;
			} 
			if(cn==null) return false;
			if(!java.lang.reflect.Modifier.isPublic(cn.getModifiers()))return false;				
			return true;
		}else if(this.refElement!=null){
			Set<javax.lang.model.element.Modifier> ms = refElement.getModifiers();
			if(refElement.getKind()!=ElementKind.CLASS) return false;
			if(ms.contains(Modifier.ABSTRACT)|| ms.contains(Modifier.FINAL)) return false;
			if(!ms.contains(Modifier.PUBLIC)) return false;
			boolean existsCo = false;
			for(Element e :this.refElement.getEnclosedElements()){
				if(e.getKind()!=ElementKind.CONSTRUCTOR) continue;
				existsCo = true;
				ExecutableElement ee = (ExecutableElement)e;
				if(ee.getModifiers().contains(Modifier.PRIVATE)) continue;
				if(ee.getModifiers().contains(Modifier.PROTECTED)) continue;
				if(ee.getParameters()==null || ee.getParameters().size()==0) return true;				
			}
			return !existsCo;
		}
		return false;
	}

	private static PackageElement getPackage(Element type) {
		while (type.getKind() != ElementKind.PACKAGE) {
			type = type.getEnclosingElement();
		}
		return (PackageElement) type;
	}

	public boolean isBoxedClass() {
		for (String s : box_eds) {
			if (this.keyword.equals(s))
				return true;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof ClassName && keyword.equals(((ClassName) o).keyword);
	}

	@Override
	public int hashCode() {
		return keyword.hashCode();
	}

	private static String[] box_eds = { Void.class.getName(), Boolean.class.getName(), Byte.class.getName(),
			Short.class.getName(), Integer.class.getName(), Long.class.getName(), Character.class.getName(),
			Float.class.getName(), Double.class.getName() };
}
