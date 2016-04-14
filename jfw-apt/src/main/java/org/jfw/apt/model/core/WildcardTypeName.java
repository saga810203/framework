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

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.lang.model.type.TypeMirror;




public final class WildcardTypeName extends TypeName {
  public final List<TypeName> upperBounds;
  public final List<TypeName> lowerBounds;

  private WildcardTypeName(List<TypeName> upperBounds, List<TypeName> lowerBounds) {
    super();
    this.upperBounds =Collections.unmodifiableList(upperBounds);
    this.lowerBounds =Collections.unmodifiableList(lowerBounds);
    if(lowerBounds.size() == 1){
    	this.keyword =  "? super "+lowerBounds.get(0).toString();
    }else{
    	String on = upperBounds.get(0).toString();
    	if(on.equals("java.lang.Object")) {
    		this.keyword="?";
    	}else{
    		this.keyword = "? extends "+on;
    	}    	
    }    
  }


  public static WildcardTypeName subtypeOf(TypeName upperBound) {
    return new WildcardTypeName(Arrays.asList(upperBound), Collections.<TypeName>emptyList());
  }

  public static WildcardTypeName subtypeOf(Type upperBound) {
    return subtypeOf(TypeName.get(upperBound));
  }

  public static WildcardTypeName supertypeOf(TypeName lowerBound) {
    return new WildcardTypeName(Arrays.<TypeName>asList(OBJECT), Arrays.asList(lowerBound));
  }

  public static WildcardTypeName supertypeOf(Type lowerBound) {
    return supertypeOf(TypeName.get(lowerBound));
  }

  public static TypeName get(javax.lang.model.type.WildcardType mirror) {
    TypeMirror extendsBound = mirror.getExtendsBound();
    if (extendsBound == null) {
      TypeMirror superBound = mirror.getSuperBound();
      if (superBound == null) {
        return subtypeOf(Object.class);
      } else {
        return supertypeOf(TypeName.get(superBound));
      }
    } else {
      return subtypeOf(TypeName.get(extendsBound));
    }
  }

  public static TypeName get(WildcardType wildcardName) {
    return new WildcardTypeName(
        list(wildcardName.getUpperBounds()),
        list(wildcardName.getLowerBounds()));
  }
}
