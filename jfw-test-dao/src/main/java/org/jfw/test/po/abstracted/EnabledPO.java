package org.jfw.test.po.abstracted;

import org.jfw.apt.annotation.orm.Column;
import org.jfw.apt.annotation.orm.VirtualTable;
import org.jfw.apt.orm.core.enums.DataElement;

@VirtualTable
public class EnabledPO extends BasePO {
	@Column(value=DataElement.boolean_de,nullable=false)
	private boolean enable;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
