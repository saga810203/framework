package org.jfw.apt.model.orm;

import javax.lang.model.element.Element;

import org.jfw.apt.Utils;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.orm.core.enums.DataElement;

public class CalcColumn extends Column {
	
	protected CalcColumn(){
		super();
	}

	public static CalcColumn build(org.jfw.apt.annotation.orm.CalcColumn col,Element ele) throws AptException
	{
		if(DataElement.invalid_de == col.value()) throw new AptException(ele,"value can't equals DataElement.invalid_de in @CalcColumn");
		CalcColumn result = new CalcColumn();
		result.ele = ele;
		result.javaName =ele.getSimpleName().toString();
		Utils.checkPersistentObjectName(result.javaName, ele);
		String str = col.calcExpression();
		if(str==null||str.trim().length()==0)throw new AptException(ele,"@CalcColumn'calcExpression must be no_empty string");
		result.dbName = col.calcExpression();
		
		String alias = col.alias();
		if(alias!=null&& alias.trim().length()>0) result.dbName= result.dbName+" "+alias.trim();
		result.de = col.value();
		String handlerClassName= Utils.getClassName(result.newHandler().supportsClass());
		
		if(!handlerClassName.equals(PersistentObject.getClassNameWithField(ele, null))){
			throw new AptException(ele,"Annotation not supported filed type");
		}
		if(col.nullable()&&(!result.de.isNullable())){
			throw new AptException(ele,"Field nullable must be false");
		}
		result.nullable = col.nullable();
		result.comment = col.comment();
		result.inQuery =true;		
		return result;
	}
	
}
