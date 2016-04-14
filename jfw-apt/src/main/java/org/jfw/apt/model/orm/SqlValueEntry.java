package org.jfw.apt.model.orm;

import java.util.Map;

import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.SqlValue;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.orm.core.enums.DataElement;

public class SqlValueEntry implements Comparable<SqlValueEntry> {
	public DataElement getDe() {
		return de;
	}

	public String getParamName() {
		return paramName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public String getValueExpression() {
		return valueExpression;
	}

	public boolean isNullable() {
		return nullable;
	}


	public OrmHandler getOrm() {
		return orm;
	}

	public String getSupportedClassName() {
		return supportedClassName;
	}




	public boolean isPrimitive() {
		return Utils.isPrimitive(this.supportedClassName);
	}

	private DataElement de;
	private String paramName;
	private String attributeName;
	private String paramExpression;
	private boolean nullable;
	private String valueExpression;
	private OrmHandler orm;
	private String supportedClassName;

	private SqlValueEntry() {
	}

	public static SqlValueEntry build(SqlValue val,boolean filtered,boolean dynamic,ExecutableElement ref,Map<String,Object> map) throws AptException {
		SqlValueEntry sve = new SqlValueEntry();
		sve.de = val.de();
		sve.paramName =Utils.emptyToNull(val.paramName());
		sve.attributeName =Utils.emptyToNull(val.attributeName());
		sve.paramExpression =Utils.emptyToNull(val.paramExpression());
		
		if(sve.paramExpression ==null && sve.paramName==null){
			throw new AptException(ref,"@SqlValue: paramName ==null && paramExpression==null");
		}
		if(sve.paramExpression !=null && sve.paramName!=null){
			throw new AptException(ref,"@SqlValue: paramName !=null && paramExpression!=null");
		}
		sve.valueExpression = val.valueExpression();
		if( dynamic && null == sve.valueExpression){
			throw new AptException(ref,"@Query @Where dynamic==true && @SqlValue valueExpression == null");
		}
		try {
			sve.orm = (OrmHandler)sve.de.getHandlerClass().newInstance();
		} catch (Exception e) {
			String m = e.getMessage();
			throw new AptException(ref,"create OrmHandler instance error:"+m==null?"":m);
		} 
		sve.supportedClassName = sve.orm.supportsClass().getName();

		if(filtered){
			if(!dynamic){
				sve.nullable = false;
			}else{
				if(sve.isPrimitive()){
					sve.nullable = false;					
				}else{
					sve.nullable = val.nullable();
				}
			}
		}else{
				if(sve.isPrimitive()){
					sve.nullable = false;					
				}else{
					sve.nullable= val.nullable();
				}  
		}
		
		
		sve.orm.init(sve.getParamExpression(), sve.paramName!=null && sve.attributeName==null, sve.nullable, map);
		return sve;				
	}

	public String getParamExpression(){
		if(this.paramName!=null){
			if(this.attributeName==null) return paramName;
			if(this.supportedClassName.equals("boolean"))
				return "is"+this.attributeName.substring(0, 1).toUpperCase()+
						(this.attributeName.length()>1?this.attributeName.substring(1):"")+"()";
			return "get"+this.attributeName.substring(0, 1).toUpperCase()+
					(this.attributeName.length()>1?this.attributeName.substring(1):"")+"()";
					
		}else return this.paramExpression;
	}

	@Override
	public int compareTo(SqlValueEntry o) {
		if(!this.nullable && o.nullable) return -1;
		if(this.nullable && !o.nullable) return 1;
		return 0;
	}

}
