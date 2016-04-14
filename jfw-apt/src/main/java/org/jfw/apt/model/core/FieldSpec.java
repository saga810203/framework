/*
 * Copyright (C) 2015 Square, Inc.
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.jfw.apt.Utils;

/** A generated field declaration. */
public final class FieldSpec {
	private TypeName type;

	private String name;
	private Set<Modifier> modifiers;
	private Element refElement;
	private Field refField;
	private String keyword;

	public static List<FieldSpec> build(TypeElement ref) {
		List<FieldSpec> list = new ArrayList<FieldSpec>();
		for (Element ele : ref.getEnclosedElements()) {
			if (ele.getKind() != ElementKind.FIELD)
				continue;
			if (ele.getModifiers().contains(Modifier.STATIC))
				continue;
			FieldSpec fs = new FieldSpec();
			fs.name = ele.getSimpleName().toString();
			fs.type = TypeName.get(ele.asType());
			fs.modifiers = Collections.unmodifiableSet(ele.getModifiers());
			fs.refElement = ele;
			fs.refField = null;
			StringBuilder sb = new StringBuilder();
			for (Modifier m : fs.modifiers) {
				sb.append(m.toString());
				sb.append(" ");
			}
			fs.keyword = sb.toString();
			list.add(fs);
		}
		return list;

	}

	public static List<FieldSpec> build(Class<?> clazz) {
		List<FieldSpec> list = new ArrayList<FieldSpec>();
		for (Field ele : clazz.getDeclaredFields()) {
			if (java.lang.reflect.Modifier.isStatic(ele.getModifiers()))
				continue;
			FieldSpec fs = new FieldSpec();
			fs.name = ele.getName();
			fs.type = TypeName.get(ele.getGenericType());
			fs.modifiers = Collections.unmodifiableSet(Utils.convert(ele.getModifiers()));
			fs.refElement = null;
			fs.refField = ele;
			StringBuilder sb = new StringBuilder();
			for (Modifier m : fs.modifiers) {
				sb.append(m.toString());
				sb.append(" ");
			}
			fs.keyword = sb.toString();
			list.add(fs);
		}
		return list;
	}

	private FieldSpec() {
	}

	public boolean hasModifier(Modifier modifier) {
		return modifiers.contains(modifier);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		return toString().equals(o.toString());
	}

	public TypeName getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public Set<Modifier> getModifiers() {
		return modifiers;
	}

	public Element getRefElement() {
		return refElement;
	}

	public Field getRefField() {
		return refField;
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return this.keyword;
	}

}
