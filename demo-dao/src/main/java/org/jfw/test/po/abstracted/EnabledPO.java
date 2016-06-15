package org.jfw.test.po.abstracted;

import org.jfw.apt.annotation.orm.Column;
import org.jfw.apt.annotation.orm.VirtualTable;
import org.jfw.apt.orm.core.enums.DE;

@VirtualTable
public class EnabledPO extends BasePO {
	@Column(value=DE.boolean_de)
	private boolean enable;

	public boolean isEnable() {
		return enable;
	}

	public void setEnable(boolean enable) {
		this.enable = enable;
	}
}
