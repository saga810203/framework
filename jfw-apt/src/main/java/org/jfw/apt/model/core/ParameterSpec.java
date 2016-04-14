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

import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;

public final class ParameterSpec {
  private String name;
  private Set<Modifier> modifiers;
  private TypeName type;
  private String keyword;
  private VariableElement ref;
  
  
  
  public String getName() {
	return name;
}

public Set<Modifier> getModifiers() {
	return modifiers;
}

public TypeName getType() {
	return type;
}


public VariableElement getRef() {
	return ref;
}

public static ParameterSpec build(VariableElement ref){
	  ParameterSpec ps = new ParameterSpec();
	  ps.name = ref.getSimpleName().toString();
	  ps.type = TypeName.get(ref.asType());
	  ps.modifiers =Collections.unmodifiableSet(ref.getModifiers());
	  ps.ref = ref;
	  StringBuilder sb = new StringBuilder();
	  for(Modifier m:ps.modifiers){
		  sb.append(m.toString());
		  sb.append(" "); 
	  }
	  sb.append(ps.type.toString()).append(" ").append(ps.name);
	  ps.keyword = sb.toString();
	  return ps;
  }

  private ParameterSpec() {

  }

  public boolean hasModifier(Modifier modifier) {
    return modifiers.contains(modifier);
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    return toString().equals(o.toString());
  }

  @Override public int hashCode() {
    return toString().hashCode();
  }

  @Override public String toString() {
   return this.keyword;
  }
}
