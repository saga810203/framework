package org.jfw.apt.model.orm;

import java.util.Locale;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.orm.core.enums.DataElement;

public class Column {


	protected String dbName;
	protected String javaName;
	protected boolean nullable;
	protected boolean inQuery;
	protected String comment;
	protected DataElement de;
	protected String javaType;
	private OrmHandler handler = null;



	protected Element ele;	
	protected Column(){}	
	public static Column build(org.jfw.apt.annotation.orm.Column col,Element ele) throws AptException{
		if(DataElement.invalid_de == col.value()) throw new AptException(ele,"value can't equals DataElement.invalid_de in @Column");
		Column result = new Column();
		result.ele = ele;
		result.javaName =ele.getSimpleName().toString();
		Utils.checkPersistentObjectName(result.javaName, ele);
		result.dbName = Utils.javaNameConverToDbName(result.javaName);
		result.de = col.value();
		result.javaType= Utils.getClassName(result.newHandler().supportsClass());
		
		if(!result.javaType.equals(PersistentObject.getClassNameWithField(ele, null))){
			throw new AptException(ele,"Annotation not supported filed type");
		}
		if(col.nullable()&&(!result.de.isNullable())){
			throw new AptException(ele,"Field nullable must be false");
		}
		result.nullable = col.nullable();
		result.comment = col.comment();
		result.inQuery = col.defaultQuery();
		return result;		
	}
	
	
	
	public boolean isPrimitive() {
		return Utils.isPrimitive(this.javaType);
	}
	public OrmHandler newHandler() throws AptException
	{
		
		Class<? extends OrmHandler> clazz = this.de.getHandlerClass();
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			String msg = e.getMessage();
			throw new AptException(this.ele,null==msg?"":msg);
		} 
	}
	
	
	
	public void initHandler(Element ref) throws AptException{
		this.handler = Utils.getOrmHandler(this, ref);
	}
	
	
	public String getColumnDefine(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.dbName).append(" ").append(this.de.getDbType());
		if(this.de.getDbTypeLength()>0){
			sb.append("(").append(this.de.getDbTypeLength());
			if(this.de.getDbTypePrecision()>=0){
				sb.append(",").append(this.de.getDbTypePrecision());
			}
			sb.append(")");
		}
		if(!this.nullable) sb.append(" not null");
		return sb.toString();
	}
	
	public void warnMessage(Messager messager){
		StringBuilder sb = new StringBuilder();
		sb.append("DBNAME:").append(this.getDbName()).append(" ").append(this.getColumnDefine()).append("\r\n");
		sb.append("JAVANAME:").append(this.getJavaName()).append("\r\n");
		messager.printMessage(Kind.WARNING,sb.toString(), this.ele);
	}
	
	public String getFixValueWithInsert(){
		return Utils.emptyToNull(this.de.getFixSqlValueWithInsert());
	}
	public String getFixValueWithUpdate(){
		return Utils.emptyToNull(this.de.getFixSqlValueWithUpdate());		
	}
	
	
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public String getJavaName() {
		return javaName;
	}
	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}
	public boolean isNullable() {
		return nullable;
	}
	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}
	public boolean isInQuery() {
		return inQuery;
	}
	public void setInQuery(boolean inQuery) {
		this.inQuery = inQuery;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public DataElement getDataElement() {
		return de;
	}
	public String getJavaType() {
		return javaType;
	}
	
	public String getSetter(){
		if(this.javaName.length()==1){
			return "set"+this.javaName.toUpperCase(Locale.US);
		}else{
			return "set"+this.javaName.substring(0, 1).toUpperCase(Locale.US)+this.javaName.substring(1);
		}
	}
	public String getGetter(){
		String frefix = this.javaType.equals("boolean")?"is":"get";
		
		if(this.javaName.length()==1){
			return frefix+this.javaName.toUpperCase(Locale.US);
		}else{
			return frefix+this.javaName.substring(0, 1).toUpperCase(Locale.US)+this.javaName.substring(1);
		}
	}
	
	public OrmHandler getHandler() {
		return handler;
	}
	

}
