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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public final class ParameterizedTypeName extends TypeName {
  public final ClassName rawType;
  public final List<TypeName> typeArguments;


  public ParameterizedTypeName(ClassName rawType, List<TypeName> typeArguments) {
    super();
    this.rawType = rawType;
    this.typeArguments = Collections.unmodifiableList(typeArguments);
    this.keyword = this.rawType.toString()+"<";
    StringBuilder sb = new StringBuilder();
    sb.append(this.rawType.toString()).append("<");
    
    for(int i = 0 ; i < this.typeArguments.size() ; ++i){
    	if(i!=0)sb.append(",");
    	sb.append(this.typeArguments.get(i).toString());
    }
    sb.append(">");
    this.keyword = sb.toString();
  }

  public static ParameterizedTypeName get(ClassName rawType, TypeName... typeArguments) {
    return new ParameterizedTypeName(rawType, Arrays.asList(typeArguments));
  }
  public static ParameterizedTypeName get(Class<?> rawType, Type... typeArguments) {
    return new ParameterizedTypeName(ClassName.get(rawType), list(typeArguments));
  }

  public static ParameterizedTypeName get(ParameterizedType type) {
    return new ParameterizedTypeName(ClassName.get((Class<?>) type.getRawType()),
        TypeName.list(type.getActualTypeArguments()));
  }
}
