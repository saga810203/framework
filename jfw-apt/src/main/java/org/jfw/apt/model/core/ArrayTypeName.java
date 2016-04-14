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

import java.lang.reflect.GenericArrayType;

import javax.lang.model.type.ArrayType;


public final class ArrayTypeName extends TypeName {
  public final TypeName componentType;



  private ArrayTypeName(TypeName componentType) {
    super();
    this.componentType = componentType;
    this.keyword=this.componentType.toString()+"[]";
  }
  public static ArrayTypeName of(TypeName componentType) {
    return new ArrayTypeName(componentType);
  }

//  public static ArrayTypeName of(Type componentType) {
//    return of(TypeName.get(componentType));
//  }

  public static ArrayTypeName get(ArrayType mirror) {
    return new ArrayTypeName(TypeName.get(mirror.getComponentType()));
  }



  public static ArrayTypeName get(GenericArrayType type) {
    return get(type);
  }
}
