package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.CustomDMLSQL;
import org.jfw.apt.annotation.orm.SqlValue;
import org.jfw.apt.exception.AptException;

public class CustomDMLSQLOperateCG extends DBOperateCG {
	
	private String sql;
	private List<SqlValueEntry> values = new ArrayList<SqlValueEntry>();



	@Override
	protected void prepare() throws AptException {
		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@CustomDMLSQL) must return int");
		CustomDMLSQL csql = this.ref.getAnnotation(CustomDMLSQL.class);
		
		this.sql =Utils.emptyToNull(csql.value());
		if(this.sql == null) throw new AptException(ref,"@CustomDMLSQL'value is null or empytString");
		SqlValue[] sqls = csql.sqlValues();
		if(sqls!=null && sqls.length>0){
			for(int i = 0 ; i < sqls.length ; ++i){
				this.values.add(SqlValueEntry.build(sqls[i], false, false, ref, this.attributes));
			}
		}
		for(SqlValueEntry sve:this.values){
			sve.getOrm().prepare(sb);
		}
		sb.append("String sql=\"");
		Utils.addSqlToStringBuilder(this.sql, sb);
		sb.append("\";\r\n");
	}



	
	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.values.size(); ++i) {
		    this.values.get(i).getOrm().writeValue(sb,false);
		}
	}

	@Override
	protected void buildHandleResult() {
		sb.append("return ps.executeUpdate();");

	}

	@Override
	protected boolean needRelaceResource() {
		for (SqlValueEntry col : this.values) {
			if (col.getOrm().isReplaceResource())
				return true;
		}
		return false;
		
	}

	@Override
	protected void relaceResource() {
		for (SqlValueEntry col : this.values) {
			col.getOrm().replaceResource(sb);
		}

	}
}