package org.jfw.util;

import java.util.ArrayList;
import java.util.List;

public final class ListUtil {

	private ListUtil() {
	}

	public static <T> List<T> fill(List<T> list, T ele, int size) {
		for (int i = 0; i < size; ++i) {
			list.add(ele);
		}
		return list;
	}

	public static List<String> split(String val, char delim) {
		ArrayList<String> result = new ArrayList<String>();
		char[] cs = val.toCharArray();
		int mi = cs.length;
		if (mi > 0) {
			int begin = 0;
			for (int i = 0; i < mi; ++i) {
				char c = cs[i];
				if (c == delim) {
					if (i == begin)
						result.add("");
					else
						result.add(new String(cs, begin, i - begin));
					begin = i + 1;
				}
			}
			if (begin == mi)
				result.add("");
			else
				result.add(new String(cs, begin, mi - begin));
		} else {
			result.add("");
		}
		return result;
	}
	/**
	 * exclude string.leng()==0;
	 * @param val
	 * @param delim
	 * @return
	 */
	public static List<String> splitExcludeEmpty(String val, char delim) {
		ArrayList<String> result = new ArrayList<String>();
		char[] cs = val.toCharArray();
		int mi = cs.length;
		if (mi > 0) {
			int begin = 0;
			for (int i = 0; i < mi; ++i) {
				char c = cs[i];
				if (c == delim) {
					if (i != begin) result.add(new String(cs, begin, i - begin));
					begin = i + 1;
				}
			}
			if (begin!= mi) result.add(new String(cs, begin, mi - begin));
		} 
		return result;
	}
	/**
	 * string  = string.trim(), exclude string.length()==0
	 * @param val
	 * @param delim
	 * @return
	 */
	public static List<String> splitTrimExcludeEmpty(String val, char delim) {
		ArrayList<String> result = new ArrayList<String>();
		String e;
		char[] cs = val.toCharArray();
		int mi = cs.length;
		if (mi > 0) {
			int begin = 0;
			for (int i = 0; i < mi; ++i) {
				char c = cs[i];
				if (c == delim) {
					if (i != begin){
						e =new String(cs, begin, i - begin).trim();
						if(e.length()>0) result.add(e);
					}
					begin = i + 1;
				}
			}
			if (begin!= mi){
				e =new String(cs, begin, mi - begin).trim();
				if(e.length()>0) result.add(e);
			}
		} 
		return result;
	}
	
	public static void main(String[] args){
		String val ="           ,a,  , uoiho , ,      ";
		
		System.out.println("------"+val);
		
		List<String> aa = splitTrimExcludeEmpty(val,',');
		for(String a:aa) System.out.println("======"+a);	
		
		
	}
}
