package org.jfw.test.po;

import org.jfw.apt.annotation.orm.CalcColumn;
import org.jfw.apt.orm.core.enums.DE;

@org.jfw.apt.annotation.orm.View(fromSentence="USER INNER JOIN SCORE ON USER.ID=SCORE.USER_ID")
public class View {
	@CalcColumn(value=DE.string_de,nullable=false,calcExpression="USER.ID")
	private String userId;
	@CalcColumn(value=DE.string_de,nullable=false,calcExpression="USER.NAME")
	private String userName;
	@CalcColumn(value=DE.string_de,nullable=false,calcExpression="SCORE.SUBJECT")
	private String subject;
	@CalcColumn(value=DE.int_de,nullable=false,calcExpression="SCORE.VALUE")
	private int value;
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
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
