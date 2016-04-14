package org.jfw.apt.model.core;


import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleTypeVisitor7;


public class TypeName {
  public static final TypeName VOID = new TypeName("void");
  public static final TypeName BOOLEAN = new TypeName("boolean");
  public static final TypeName BYTE = new TypeName("byte");
  public static final TypeName SHORT = new TypeName("short");
  public static final TypeName INT = new TypeName("int");
  public static final TypeName LONG = new TypeName("long");
  public static final TypeName CHAR = new TypeName("char");
  public static final TypeName FLOAT = new TypeName("float");
  public static final TypeName DOUBLE = new TypeName("double");
  public static final ClassName OBJECT = ClassName.get(Object.class);

  private static final ClassName BOXED_VOID = ClassName.get(Void.class);
  private static final ClassName BOXED_BOOLEAN = ClassName.get(Boolean.class);
  private static final ClassName BOXED_BYTE = ClassName.get(Byte.class);
  private static final ClassName BOXED_SHORT = ClassName.get(Short.class);
  private static final ClassName BOXED_INT = ClassName.get(Integer.class);
  private static final ClassName BOXED_LONG = ClassName.get(Long.class);
  private static final ClassName BOXED_CHAR = ClassName.get(Character.class);
  private static final ClassName BOXED_FLOAT = ClassName.get(Float.class);
  private static final ClassName BOXED_DOUBLE = ClassName.get(Double.class);


  protected String keyword;
  
  protected TypeName(){
	  
  }

  private TypeName(String keyword) {
    this.keyword = keyword;
  }
  
  public boolean isPrimitive()
  {
	 return  this.getClass().equals(TypeName.class) && !this.keyword.equals("void");
  }


  /**
   * Returns a boxed type if this is a primitive type (like {@code Integer} for {@code int}) or
   * {@code void}. Returns this type if boxing doesn't apply.
   */
  public TypeName box() {
    if (this == VOID) return BOXED_VOID;
    if (this == BOOLEAN) return BOXED_BOOLEAN;
    if (this == BYTE) return BOXED_BYTE;
    if (this == SHORT) return BOXED_SHORT;
    if (this == INT) return BOXED_INT;
    if (this == LONG) return BOXED_LONG;
    if (this == CHAR) return BOXED_CHAR;
    if (this == FLOAT) return BOXED_FLOAT;
    if (this == DOUBLE) return BOXED_DOUBLE;
    throw new UnsupportedOperationException("cannot box:" + this);
  }

  /**
   * Returns an unboxed type if this is a boxed primitive type (like {@code int} for {@code
   * Integer}) or {@code Void}. Returns this type if it is already unboxed.
   *
   * @throws UnsupportedOperationException if this type isn't eligible for unboxing.
   */
  public TypeName unbox() {
    if (this.equals(BOXED_VOID)) return VOID;
    if (this.equals(BOXED_BOOLEAN)) return BOOLEAN;
    if (this.equals(BOXED_BYTE)) return BYTE;
    if (this.equals(BOXED_SHORT)) return SHORT;
    if (this.equals(BOXED_INT)) return INT;
    if (this.equals(BOXED_LONG)) return LONG;
    if (this.equals(BOXED_CHAR)) return CHAR;
    if (this.equals(BOXED_FLOAT)) return FLOAT;
    if (this.equals(BOXED_DOUBLE)) return DOUBLE;
    throw new UnsupportedOperationException("cannot unbox:" + this);
  }

  @Override 
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;
    if (getClass() != o.getClass()) return false;
    return toString().equals(o.toString());
  }

  @Override public int hashCode() {
    return toString().hashCode();
  }

  @Override public final String toString() {
    return this.keyword;
  }

 
  /** Returns a type name equivalent to {@code mirror}. */
  public static TypeName get(TypeMirror mirror) {
      return mirror.accept(new SimpleTypeVisitor7<TypeName, Void>() {
      @Override public TypeName visitPrimitive(PrimitiveType t, Void p) {
        switch (t.getKind()) {
          case BOOLEAN:
            return TypeName.BOOLEAN;
          case BYTE:
            return TypeName.BYTE;
          case SHORT:
            return TypeName.SHORT;
          case INT:
            return TypeName.INT;
          case LONG:
            return TypeName.LONG;
          case CHAR:
            return TypeName.CHAR;
          case FLOAT:
            return TypeName.FLOAT;
          case DOUBLE:
            return TypeName.DOUBLE;
          default:
            throw new AssertionError();
        }
      }

      @Override public TypeName visitDeclared(DeclaredType t, Void p) {
        ClassName rawType = ClassName.get((TypeElement) t.asElement());
        if (t.getTypeArguments().isEmpty()) return rawType;

        List<TypeName> typeArgumentNames = new ArrayList<>();
        for (TypeMirror mirror : t.getTypeArguments()) {
          typeArgumentNames.add(get(mirror));
        }
        return new ParameterizedTypeName(rawType, typeArgumentNames);
      }

      @Override public ArrayTypeName visitArray(ArrayType t, Void p) {
        return ArrayTypeName.get(t);
      }

      @Override public TypeName visitTypeVariable(javax.lang.model.type.TypeVariable t, Void p) {
        throw new UnsupportedOperationException("javax.lang.model.type.TypeVariable convert org.jfw.apt.model.core.TypeName");
      }

      @Override public TypeName visitWildcard(javax.lang.model.type.WildcardType t, Void p) {
        return WildcardTypeName.get(t);
      }

      @Override public TypeName visitNoType(NoType t, Void p) {
        if (t.getKind() == TypeKind.VOID) return TypeName.VOID;
        return super.visitUnknown(t, p);
      }

      @Override protected TypeName defaultAction(TypeMirror e, Void p) {
        throw new IllegalArgumentException("Unexpected type mirror: " + e);
      }
    }, null);
  }

  /** Returns a type name equivalent to {@code type}. */
  public static TypeName get(Type type) {
    if (type instanceof Class<?>) {
      Class<?> classType = (Class<?>) type;
      if (type == void.class) return VOID;
      if (type == boolean.class) return BOOLEAN;
      if (type == byte.class) return BYTE;
      if (type == short.class) return SHORT;
      if (type == int.class) return INT;
      if (type == long.class) return LONG;
      if (type == char.class) return CHAR;
      if (type == float.class) return FLOAT;
      if (type == double.class) return DOUBLE;
      if (classType.isArray()) return ArrayTypeName.of(get(classType.getComponentType()));
      return ClassName.get(classType);

    } else if (type instanceof ParameterizedType) {
      return ParameterizedTypeName.get((ParameterizedType) type);

    } else if (type instanceof WildcardType) {
      return WildcardTypeName.get((WildcardType) type);

    } else if (type instanceof TypeVariable<?>) {
      throw new UnsupportedOperationException("java.lang.reflect.TypeVariable convert org.jfw.apt.model.core.TypeName");
    } else if (type instanceof GenericArrayType) {
      return ArrayTypeName.get((GenericArrayType) type);

    } else {
      throw new IllegalArgumentException("unexpected type: " + type);
    }
  }

  /** Converts an array of types to a list of type names. */
  static List<TypeName> list(Type[] types) {
    List<TypeName> result = new ArrayList<>(types.length);
    for (Type type : types) {
      result.add(get(type));
    }
    return result;
  }
  static TypeName arrayComponent(TypeName type) {
    return type instanceof ArrayTypeName
        ? ((ArrayTypeName) type).componentType
        : null;
  }
}


