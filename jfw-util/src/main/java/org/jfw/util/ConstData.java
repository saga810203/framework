package org.jfw.util;

import java.nio.charset.Charset;

public abstract class ConstData {
	public static final Charset UTF8 = Charset.forName("UTF-8");
	public static final String EMPTY_STRING="";
	public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	public static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

}
