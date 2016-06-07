package org.jfw.util;

import java.util.Collections;
import java.util.List;

public class PageQueryResult<T> {
	private int total;
	private int pageNo;
	private int pageSize;
	
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public List<T> getData() {
		return data;
	}
	public void setData(List<T> data) {
		this.data = data;
	}
	private List<T> data;
	
	public static void main(String[] args){
		new PageQueryResult<String>().setData(Collections.<String>emptyList());
		
	}
}
