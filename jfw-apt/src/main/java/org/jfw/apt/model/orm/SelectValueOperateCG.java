package org.jfw.apt.model.orm;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.SelectValue;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;
import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.orm.core.enums.DE;

public class SelectValueOperateCG  extends DBOperateCG {

	private OrmHandler valueHandler ;
	private SelectValue selectValue;

	private void checkReturnType() throws AptException {
		if(selectValue.de() == DE.invalid_de)
			throw new AptException(ref, "this mehtod return type  is not  a valid type");
		try{
		this.valueHandler = this.selectValue.de().getHandlerClass().newInstance();
		} catch (Exception ee) {
			String m = ee.getMessage();
			throw new AptException(ref, "can't create ormHandler instance:" + m == null ? "" : m);
		}
		if(!TypeName.get(this.valueHandler.supportsClass()).toString().equals(this.returnType)){
			throw new AptException(ref, "this mehtod return type  is not  a valid type");
		}
	}

	private void buildStaticSQL() throws AptException {


		sb.append("String sql = \"");
		Utils.addSqlToStringBuilder(this.selectValue.sql().trim(), sb);
		sb.append("\";\r\n");
	}

	
	@Override
	protected void prepare() throws AptException {
		this.selectValue = this.ref.getAnnotation(SelectValue.class);
		if (this.selectValue == null)
			throw new AptException(this.ref, "nofound @SelectValue on this method");
		this.checkReturnType();
		this.buildStaticSQL();

	}

	@Override
	protected void buildSqlParamter() {
		sb.append("java.sql.ResultSet rs = ps.executeQuery();\r\n");
	}

	@Override
	protected void buildHandleResult() {
		sb.append("try{\r\n");
		String rt = this.returnType;
		
			sb.append(rt).append(" result ");
			if(!Utils.isPrimitive(this.returnType))sb.append("= null");
			sb.append(";\r\n");
			sb.append("if(rs.next()){");
			    this.valueHandler.readValue(sb, "result = ", ";", 1, !Utils.isPrimitive(this.returnType), this.attributes);
			sb.append("}");

		
		sb.append("return result;\r\n");
		sb.append("}finally{try{rs.close();}catch(Exception e){}}");

	}

	@Override
	protected boolean needRelaceResource() {
	return	false;
	}

	@Override
	protected void relaceResource() {

	}
}
