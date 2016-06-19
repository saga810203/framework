package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.UpdateList;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;

public class UpdateListOperateCG extends DBOperateCG{
	private PersistentObject po;
	private String[] wheres;
	private List<Column> columns;
	private List<Column> values = new ArrayList<Column>();
	private List<Column> filters = new ArrayList<Column>();
	private String poTypeName;
	private String itemName ;
	
	
	private UpdateList updateList;
	
	private List<String> exincludeColumn = new ArrayList<String>();
	
	
	private void initExincludeColumn(){
		String[] ec = this.updateList.exincludeColumn();
		if(ec==null) return ;
		for(String str:ec){
			if(str!=null && str.trim().length()>0)
				this.exincludeColumn.add(str.trim());
		}
	}
	protected void prepare() throws AptException{
		this.itemName = this.getTempalteVariableName();
		this.updateList = this.ref.getAnnotation(UpdateList.class);
		this.initExincludeColumn();
		if (this.updateList == null)
			throw new AptException(this.ref, "nofound @UpdateList on this method");
		if (!this.returnType.equals("int[]"))
			throw new AptException(ref, "this method(@UpdateList) must return int[]");

		if (this.params.size() != 2)
			throw new AptException(ref, "this method(@UpdateList) parameters count must be 2");
		
		poTypeName=this.params.get(1).getTypeName();
		if(poTypeName.endsWith("[]")){
			poTypeName = poTypeName.substring(0, poTypeName.length()-2);
		}else if (poTypeName.startsWith("java.util.List<") && poTypeName.endsWith(">")) {
			poTypeName = poTypeName.substring(15);
			poTypeName= poTypeName.substring(0, poTypeName.length() - 1).trim();
		}else{
			throw new AptException(this.ref, "this method second parameter must be java.util.List<Object> or Object[]");
		}

		this.po = this.ormDefine.getPersistentObject(this.poTypeName);
		if (this.po == null)
			throw new AptException(ref, "this method(@UpdateList) second parameter must be a PersistentObject with list or array");
		if (this.po.getKind() != PersistentObjectKind.TABLE)
			throw new AptException(ref,
					"this method(@UpdateList) second parameter must be a PersistentObject(kind == TABLE) with list or array");
		this.columns = po.getAllColumn();
		String unName = Utils.emptyToNull(updateList.value());
		if (unName == null)
			throw new AptException(ref, "@UpdateList must be has name with unique key");
		if (unName.equals("PrimaryKey")) {
			if (po.getPrimaryKey() == null)
				throw new AptException(ref,
						"this method(@UpdateList) second parameter must be a PersistentObject(has primaryKey)");
			this.wheres = po.getPrimaryKey().getJavaNames();
		} else {
			UniqueConstraint uc = po.getUniqueConstraint(unName);
			if (uc == null)
				throw new AptException(ref,
						"this method(@UpdateList) second parameter must be a PersistentObject(has @unique'name=" + unName
								+ ")");
			this.wheres = uc.getJavaNames();
		}
		this.splitColumns();
		this.initOrmHandlers();
		this.buildStaticSQL();
	}
	

	private void buildStaticSQL() {
		this.sb.append("String sql=\"UPDATE ").append(this.po.getFromSentence()).append(" SET");
		for (int i = 0; i < this.values.size(); ++i) {
			sb.append(i == 0 ? " " : ",");
			Column col = this.values.get(i);
			String value = col.getFixUpdateSqlValue();

			sb.append(this.values.get(i).getDbName()).append("=").append(null == value ? "?" : value);
		}
		sb.append(" WHERE");
		for (int i = 0; i < this.filters.size(); ++i) {
			sb.append(i == 0 ? " " : " AND ").append(this.filters.get(i).getDbName()).append("=?");
		}
		sb.append("\";\r\n");
	}
	private void initOrmHandlers() throws AptException {
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if (null != col.getFixUpdateSqlValue())
				continue;
			col.initHandler(ref);
				col.getHandler().init(this.itemName + "." + col.getGetter() + "()", true,
						col.isNullable(), this.attributes);
//			col.getHandler().prepare(sb);

		}
		for (int i = 0; i < this.filters.size(); ++i) {
			Column col = this.filters.get(i);
			col.initHandler(ref);
			col.getHandler().init(this.itemName + "." + col.getGetter() + "()", true, false,
					this.attributes);
//			col.getHandler().prepare(sb);
		}

	}
	
	private void prepareSqlParams(){
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if (null != col.getFixUpdateSqlValue())
				continue;
			col.getHandler().prepare(sb);

		}
		for (int i = 0; i < this.filters.size(); ++i) {
			Column col = this.filters.get(i);
			col.getHandler().prepare(sb);
		}		
	}
	
	private void splitColumns() throws AptException {
		for (Column col : this.columns) {
			boolean inWhere = false;
			for (String s : this.wheres) {
				if (s.equals(col.getJavaName())) {
					this.filters.add(col);
					inWhere = true;
					break;
				}
			}
			if ((!inWhere) && col.isRenewable()&&(!this.exincludeColumn.contains(col.getJavaName())))
				this.values.add(col);
		}
		if (this.values.isEmpty())
			throw new AptException(this.ref, "not found modify value");
	}

	protected  void buildHandleResult(){
		sb.append("return ps.executeBatch();");
	}
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
		sb.append("try{")
		.append("for(").append(this.poTypeName).append(" ").append(this.itemName).append(":").append(this.params.get(1).getName())
		.append("){");
		this.prepareSqlParams();
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
		for (Column col : this.values) {
			if (null != col.getFixUpdateSqlValue())
				continue;
			if (col.getHandler().isReplaceResource())
				return true;
		}
		for (Column col : this.filters) {
			if (col.getHandler().isReplaceResource())
				return true;
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		for (Column col : this.values) {
			if (null != col.getFixUpdateSqlValue())
				continue;
			col.getHandler().replaceResource(sb);
		}
		for (Column col : this.filters) {
			col.getHandler().replaceResource(sb);
		}

	}
	
	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if (null == col.getFixUpdateSqlValue()) {
				col.getHandler().writeValue(sb, false);
			}
		}
		for (int i = 0; i < this.filters.size(); ++i) {
			this.filters.get(i).getHandler().writeValue(sb, false);
		}

	}

}
