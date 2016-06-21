package org.jfw.util;

import java.util.List;

public final class ListUtil {

	private ListUtil(){}
	
	public static <T> List<T> fill(List<T> list ,T ele,int size){
		for(int i = 0 ; i < size ; ++i){
			list.add(ele);
		}
		return list;
	}
}
