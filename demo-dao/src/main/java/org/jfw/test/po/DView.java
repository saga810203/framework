package org.jfw.test.po;

import org.jfw.apt.annotation.orm.CalcColumn;
import org.jfw.apt.annotation.orm.ExtendView;
import org.jfw.apt.orm.core.enums.DE;

@ExtendView(tableAlias="A",fromSentence="USER A INNER JOIN SCORE B ON A.ID=B.USER_ID")
public class DView extends User{
	@CalcColumn(calcExpression="B.SUBJECT",nullable=false,value=DE.string_de)
	private String subject;
	@CalcColumn(calcExpression="B.VALUE",nullable=false,value=DE.int_de)
	private int value;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
}
