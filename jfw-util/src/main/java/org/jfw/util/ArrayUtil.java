package org.jfw.util;

public final class ArrayUtil {
	private ArrayUtil(){}
	
	public static <T> T[] fill(T[] array,T ele){
		for(int i = 0 ; i < array.length ; ++i){
			array[i] = ele;
		}
		return array;
	}
}
