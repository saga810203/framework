package org.jfw.test.po.abstracted;

import org.jfw.apt.annotation.orm.Column;
import org.jfw.apt.annotation.orm.VirtualTable;
import org.jfw.apt.orm.core.enums.DE;

@VirtualTable
public class BasePO {
	@Column(value=DE.createTime_de,renewable=false)
	private String createTime;

	@Column(value=DE.modifyTime_de)
	private String modifyTime;
	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public String getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(String modifyTime) {
		this.modifyTime = modifyTime;
	}

}
