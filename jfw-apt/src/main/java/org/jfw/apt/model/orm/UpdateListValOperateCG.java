package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.annotation.orm.UpdateListVal;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.core.TypeName;

public class UpdateListValOperateCG extends DBOperateCG {
	private List<Column> where = new ArrayList<Column>();
	private List<Column> values = new ArrayList<Column>();
	private List<Column> columns;
	private List<String> colNames = new ArrayList<String>();
	private UpdateListVal updateListVal;
	private List<String> whereColNames = new ArrayList<String>();
	private PersistentObject po;

	private List<String> fixValColumns = new ArrayList<String>();
	private boolean byList = true;
	private String[] itemNames;
	private String[] itemTypeName;
	private String indexName;
	private String itemLengthName;

	private String getTypeNameWithByListOrArray(String pn) {
		if (this.byList) {
			if (pn.startsWith("java.util.List<") && pn.endsWith(">")) {
				pn = pn.substring(15);
				pn = pn.substring(0, pn.length() - 1).trim();
				return pn;
			}
		} else {
			if (pn.endsWith("[]"))
				return pn.substring(0, pn.length() - 2);
		}
		return null;
	}

	private void validParamWithArrayOrList() throws AptException {
		this.itemNames = new String[this.params.size()];
		this.itemTypeName = new String[this.params.size()];
		this.byList = true;
		for (int i = 1; i < this.params.size(); ++i) {
			MethodParamEntry mpe = this.params.get(i);
			String paramType = mpe.getTypeName();
			if (i == 1) {
				if (paramType.endsWith("[]")) {
					paramType = paramType.substring(0, paramType.length() - 2);
					this.byList = false;
				} else if (paramType.startsWith("java.util.List<") && paramType.endsWith(">")) {
					paramType = paramType.substring(15);
					paramType = paramType.substring(0, paramType.length() - 1).trim();
				} else {
					throw new AptException(this.ref,
							"this method second parameter must be java.util.List<Object> or Object[]");
				}
				this.itemTypeName[1] = paramType;
				this.itemNames[i] = this.getTempalteVariableName();
			} else {
				this.itemTypeName[i] = this.getTypeNameWithByListOrArray(paramType);
				this.itemNames[i] = this.getTempalteVariableName();
				if (null == this.itemTypeName[i]) {
					if (this.byList)
						throw new AptException(this.ref,
								"this method second parameter must be java.util.List<Object> ");
					else
						throw new AptException(this.ref, "this method second parameter must be Object[] ");
				}
			}

		}

	}

	private void resolerUpdateColumns() throws AptException {
		for (int i = 1; i < this.params.size(); ++i) {
			String pn = this.params.get(i).getName();
			boolean found = false;
			for (Column col : this.columns) {
				if (col.getJavaName().equals(pn)) {
					found = true;
					if (this.whereColNames.contains(pn))
						this.where.add(col);
					else
						this.values.add(col);
					col.initHandler(ref);
					col.getHandler().init(this.itemNames[i], false, false, this.attributes);
				}
			}
			if (!found) {
				throw new AptException(ref, "param[" + pn + "] not found in persistentObject attributes");
			}
		}
		if (this.values.isEmpty())
			throw new AptException(ref, "not found change value is define");
		if (!this.updateListVal.includeFixValues())
			return;
		for (Column col : this.columns) {
			if (this.where.contains(col))
				continue;
			if (this.values.contains(col))
				continue;
			if (col.getFixUpdateSqlValue() != null) {
				this.values.add(col);
				this.fixValColumns.add(col.getJavaName());
			}
		}
	}

	@Override
	protected void prepare() throws AptException {
		this.updateListVal = this.ref.getAnnotation(UpdateListVal.class);
		if (null == this.updateListVal)
			throw new AptException(this.ref, "nofound @UpdateListVal on this method");

		if (!this.returnType.equals("int[]"))
			throw new AptException(ref, "this method(@UpdateListVal) must return int[]");

		String targetClassName = null;

		try {
			targetClassName = this.updateListVal.target().getName();
		} catch (MirroredTypeException e) {
			targetClassName = TypeName.get(e.getTypeMirror()).toString();
		}
		this.po = ormDefine.getPersistentObject(targetClassName);
		if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
			throw new AptException(ref, "this method[@UpdateVal'target value must be Object.class or Class(@Table)");
		}
		String[] tmpArray = this.updateListVal.where();
		if (tmpArray != null && tmpArray.length > 0) {
			for (String str : tmpArray) {
				if (str != null && str.trim().length() > 0)
					this.whereColNames.add(str.trim());
			}
		}
		this.columns = this.po.getAllColumn();
		for (ListIterator<Column> it = this.columns.listIterator(); it.hasNext();) {
			this.colNames.add(it.next().getJavaName());
		}
		
		this.validParamWithArrayOrList();
		this.resolerUpdateColumns();

		this.sb.append("String sql=\"UPDATE ").append(this.po.getFromSentence()).append(" SET");
		for (int i = 0; i < this.values.size(); ++i) {
			sb.append(i == 0 ? " " : ",");
			Column col = this.values.get(i);
			String value = col.getFixUpdateSqlValue();
			if(value==null) {
				value ="?";
			}else{
				if(!this.fixValColumns.contains(col.getJavaName())){
					value ="?";
				}
			}

			sb.append(this.values.get(i).getDbName()).append("=").append(value);
		}
		for (int i = 0; i < this.where.size(); ++i) {
			sb.append(i == 0 ? " WHERE " : " AND ").append(this.where.get(i).getDbName()).append("=?");
		}
		sb.append("\";\r\n");

	}

	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.values.size(); ++i) {
			Column col = this.values.get(i);
			if((col.getFixUpdateSqlValue()!=null)&& this.fixValColumns.contains(col.getJavaName()) ) continue;
			col.getHandler().writeValue(sb, false);
		}
		for(int i = 0 ; i < this.where.size(); ++i){
			this.where.get(i).getHandler().writeValue(sb,false);
		}
		sb.append("ps.addBatch();");
	}

	@Override
	protected boolean needRelaceResource() {
		for(Column col:this.values){
			if((col.getFixUpdateSqlValue()!=null) && this.fixValColumns.contains(col.getJavaName())) continue;
			if(col.getHandler().isReplaceResource()) return true;
		}
		return false;
	}

	@Override
	protected void relaceResource() {
		for(Column col:this.values){
			if((col.getFixUpdateSqlValue()!=null) && this.fixValColumns.contains(col.getJavaName())) continue;
			if(col.getHandler().isReplaceResource()) col.getHandler().replaceResource(sb);;
		}
	}

	@Override
	protected void buildHandleResult() {
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
		sb.append("try{");
		this.itemLengthName = this.getTempalteVariableName();
		this.indexName = this.getTempalteVariableName();
		sb.append("int ").append(this.itemLengthName).append(" = ").append(this.params.get(1).getName())
		.append(this.byList?".size()":".length").append(";for(int ").append(this.indexName).append("=0;").append(this.indexName).append("<")
		.append(this.itemLengthName).append(";++").append(this.indexName).append("){");
		for(int i = 1 ;i < this.itemNames.length; ++i){
			sb.append(this.itemTypeName[i]).append(" ").append(this.itemNames[i]).append(" = ")
			.append(this.params.get(i).getName());
			if(this.byList){
				sb.append(".get(").append(this.indexName).append(");");
			}else{
				sb.append("[").append(this.indexName).append("];");
			}
		}
		boolean replaceSource = this.needRelaceResource();
		if(replaceSource) sb.append("try{\r\n");
		this.buildSqlParamter();
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

}
