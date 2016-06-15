package org.jfw.test.po;

import org.jfw.apt.annotation.orm.CalcColumn;
import org.jfw.apt.annotation.orm.ExtendTable;
import org.jfw.apt.orm.core.enums.DE;

@ExtendTable
public class ExtendUser extends User {
	private String alias;
	public String getFirstName() {
		return firstName;
	}
	@CalcColumn(alias="FIRST_NAME",nullable=false,value=DE.string_de, calcExpression = "SUBSTR(NAME,1,2)")
	
	private String firstName;

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}
}
