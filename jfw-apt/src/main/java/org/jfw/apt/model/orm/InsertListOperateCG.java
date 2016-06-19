package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.annotation.orm.InsertList;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.orm.core.OrmHandler;

public class InsertListOperateCG extends DBOperateCG {

	private InsertList insertList;
	private PersistentObject po;
	private List<Column> columns = new ArrayList<Column>();
	
	private String itemName ;
	private String poTypeName;

	
	
	protected  void prepare() throws AptException{
		
		this.insertList = this.ref.getAnnotation(InsertList.class);
		if (this.insertList == null)
			throw new AptException(this.ref, "nofound @InsertList on this method");
		if (!this.returnType.equals("int[]"))
			throw new AptException(ref, "this method(@Insert) must return int[]");

		if (this.params.size() != 2)
			throw new AptException(ref, "this method(@InsertList) parameters count must be 2");
		
		poTypeName=this.params.get(1).getTypeName();
		if(poTypeName.endsWith("[]")){
			poTypeName = poTypeName.substring(0, poTypeName.length()-2);
		}else if (poTypeName.startsWith("java.util.List<") && poTypeName.endsWith(">")) {
			poTypeName = poTypeName.substring(15);
			poTypeName= poTypeName.substring(0, poTypeName.length() - 1).trim();
		}else{
			throw new AptException(this.ref, "this method second parameter must be java.util.List<Object> or Object[]");
		}

		this.po = this.ormDefine.getPersistentObject(poTypeName);
		if (this.po == null)
			throw new AptException(ref, "this method(@InsertList) second parameter must be a PersistentObject with list or array");
		if (this.po.getKind() != PersistentObjectKind.TABLE)
			throw new AptException(ref,
					"this method(@InsertList) second parameter must be a PersistentObject(kind == TABLE) with list or array");
		this.columns =po.getInsertColumn();
		this.itemName = this.getTempalteVariableName();
		this.initOrmHandlers();
		this.buildStaticSQL();		
	}
	private void buildStaticSQL() {
		this.sb.append("String sql=\"INSERT INTO ").append(this.po.getFromSentence()).append(" (");

		boolean firstColumn = true;
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (firstColumn) {
				firstColumn = false;
			} else {
				sb.append(",");
			}
			sb.append(col.getDbName());
		}
		sb.append(") values (");
		firstColumn = true;
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (firstColumn) {
				firstColumn = false;
			} else {
				sb.append(",");
			}
			String value =col.getFixInsertSqlValue();
			if (value == null) {
				sb.append("?");
			} else {
				sb.append(value);
			}
		}
		sb.append(")\";\r\n");
	}
	private void initOrmHandlers() throws AptException {
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (null != col.getFixInsertSqlValue())
				continue;
			col.initHandler(ref);
			col.getHandler().init(this.itemName + "." + col.getGetter() + "()", true, col.nullable,
					this.attributes);
		}

	}
	

	protected void buildHandleResult(){
		sb.append("return ps.executeBatch();");
	}
	
	
	@Override
	public String getCode(ExecutableElement ref) throws AptException
	{
		this.ormDefine = (OrmDefine)this.getAttribute(OrmDefine.class.getName());
		this.fillMeta(ref);
		this.sb = new StringBuilder();
		this.checkJdbc();
		this.sb.append("@Override\r\n public ")	.append(this.returnType).append(" ").append(this.name).append("(");
		for(int i = 0 ; i < this.params.size(); ++i){
			if(i!=0) sb.append(",");
			MethodParamEntry mpe = this.params.get(i);
			sb.append(mpe.getTypeName()).append(" ").append(mpe.getName());
		}
		sb.append(")");
		for(int i = 0 ; i < this.throwables.size() ; ++i){
			if(i==0){
				sb.append(" throws ");
			}else{
				sb.append(",");
			}
			sb.append(this.throwables.get(i));
		}
		sb.append(" {\r\n");		
		this.prepare();		
		sb.append("java.sql.PreparedStatement ps = con.prepareStatement(sql);\r\n");
		sb.append("try{");
		sb.append("for(").append(this.poTypeName).append(" ").append(this.itemName).append(":").append(this.params.get(1).getName())
		.append("){");
		for(Column cl:this.columns){
			OrmHandler oh = cl.getHandler();
			if(oh!=null) oh.prepare(sb);
		}		
		boolean replaceSource = this.needRelaceResource();
		if(replaceSource) sb.append("try{\r\n");
		this.buildSqlParamter();
		sb.append("ps.addBatch();");
		if(replaceSource){
			sb.append("}finally{");
			this.relaceResource();
			sb.append("}\r\n");
		}
		sb.append("}");
		this.buildHandleResult();
		sb.append("}finally{\r\ntry{ps.close();}catch(Exception e){}\r\n}\r\n");
		sb.append("}");			
		return sb.toString();
	}
	
	@Override
	protected boolean needRelaceResource() {
		for (Column col : this.columns) {
			if (null == col.getFixInsertSqlValue()) {
				if (col.getHandler().isReplaceResource())
					return true;
			}
			
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		for (Column col : this.columns) {
			if (null == col.getFixInsertSqlValue()) {
				col.getHandler().replaceResource(sb);
			}
			
		}
	}
	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.columns.size(); ++i) {
			Column col = this.columns.get(i);
			if (null == col.getFixInsertSqlValue()) {
				col.getHandler().writeValue(sb, false);
			}
		}
	}
	
}
