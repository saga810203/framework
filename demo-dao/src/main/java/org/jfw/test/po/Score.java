package org.jfw.test.po;

import org.jfw.apt.annotation.orm.Column;
import org.jfw.apt.annotation.orm.Table;
import org.jfw.apt.annotation.orm.Unique;
import org.jfw.apt.orm.core.enums.DE;
import org.jfw.test.po.abstracted.BasePO;
@Table(primaryKey=@Unique({"userId","subject"}))
public class Score extends BasePO{
	@Column(value=DE.string_de)
	private String userId;
	@Column(value=DE.string_de)
	private String subject;
	@Column(value=DE.int_de)
	private int value;
	
	@Column(value=DE.String_de)
	private String comment;
	
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
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
