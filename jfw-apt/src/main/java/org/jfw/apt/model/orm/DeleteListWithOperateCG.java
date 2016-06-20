package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.DeleteListWith;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.core.TypeName;

public class DeleteListWithOperateCG extends DBOperateCG{
	private String value;
	private List<Column> where = new ArrayList<Column>();
	private PersistentObject po;
	private boolean byList;
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

	@Override
	protected void prepare() throws AptException {
		DeleteListWith with = this.ref.getAnnotation(DeleteListWith.class);
		if (null == with)
			throw new AptException(this.ref, "nofound @DeleteWith on this method");

		if (!this.returnType.equals("int[]"))
			throw new AptException(ref, "this method(@DeleteWith) must return int[]");
		
		if(this.params.size()<2) throw new AptException(ref,"the method[@DeleteListWith] paramter count must be > 2");

		this.value = Utils.emptyToNull(with.value());

		String targetClassName = null;

		try {
			targetClassName = with.target().getName();
		} catch (MirroredTypeException e) {
			targetClassName = TypeName.get(e.getTypeMirror()).toString();
		}
		this.po = ormDefine.getPersistentObject(targetClassName);
		if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
			throw new AptException(ref, "this method[@DeleteWith'target value must be Object.class or Class(@Table)");
		}
		this.validParamWithArrayOrList();
		List<Column> list = this.po.getAllColumn();
		for (int i = 1; i < this.params.size(); ++i) {
			MethodParamEntry mpe = this.params.get(i);
			String pn = mpe.getName();
			boolean found = false;
			for (Column col : list) {
				if (col.getJavaName().equals(pn)) {
					this.where.add(col);
					col.initHandler(ref);
					col.getHandler().init(this.itemNames[i], false, false, this.attributes);
					found = true;
					break;
				}
			}
			if (!found)
				throw new AptException(ref, "param[" + pn + "] not found in persistentObject attributes");
		}
		
		

		sb.append("String sql=\"DELETE FROM ").append(this.po.getFromSentence());
		if (null != this.value || (!this.where.isEmpty())) {
			sb.append(" WHERE ");
			if (null != this.value)
				sb.append(this.value);
			for (int i = 0; i < this.where.size(); ++i) {
				if (i == 0) {
					if (null != this.value) {
						sb.append(" AND ");
					}
				} else {
					sb.append(" AND ");
				}
				sb.append(this.where.get(i).getDbName()).append("=?");

			}
		}
		sb.append("\";");
	}

	@Override
	protected void buildSqlParamter() {
		for(int i = 0 ;i < this.where.size(); ++i){
			this.where.get(i).getHandler().writeValue(sb, false);
		}
		sb.append("ps.addBatch();");
	}

	@Override
	protected boolean needRelaceResource() {
		return false;
	}

	@Override
	protected void relaceResource() {
	}

	@Override
	protected void buildHandleResult() {
		sb.append("return ps.executeBatch();");
	}
	
	public String getCode(ExecutableElement ref) throws AptException {
		this.ormDefine = (OrmDefine) this.getAttribute(OrmDefine.class.getName());
		this.fillMeta(ref);
		this.sb = new StringBuilder();
		this.checkJdbc();
		this.sb.append("@Override\r\n public ").append(this.returnType).append(" ").append(this.name).append("(");
		for (int i = 0; i < this.params.size(); ++i) {
			if (i != 0)
				sb.append(",");
			MethodParamEntry mpe = this.params.get(i);
			sb.append(mpe.getTypeName()).append(" ").append(mpe.getName());
		}
		sb.append(")");
		for (int i = 0; i < this.throwables.size(); ++i) {
			if (i == 0) {
				sb.append(" throws ");
			} else {
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
					.append(this.byList ? ".size()" : ".length").append(";for(int ").append(this.indexName).append("=0;")
					.append(this.indexName).append("<").append(this.itemLengthName).append(";++").append(this.indexName)
					.append("){");
			for (int i = 1; i < this.itemNames.length; ++i) {
				sb.append(this.itemTypeName[i]).append(" ").append(this.itemNames[i]).append(" = ")
						.append(this.params.get(i).getName());
				if (this.byList) {
					sb.append(".get(").append(this.indexName).append(");");
				} else {
					sb.append("[").append(this.indexName).append("];");
				}
			}
			
			this.buildSqlParamter();
		
			sb.append("}");
		
		this.buildHandleResult();
		sb.append("}finally{\r\ntry{ps.close();}catch(Exception e){}\r\n}\r\n");

		sb.append("}");
		return sb.toString();
	}

}
