package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.DeleteList;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.core.TypeName;

public class DeleteListOperateCG extends DBOperateCG {

	private DeleteList deleteList;
	private PersistentObject po;
	private String poTypeName;
	private List<Column> values = new ArrayList<Column>();
	private boolean byBean = true;
	private boolean byList = true;
	private String[] itemNames;
	private String[] itemTypeName;
	private String beanItemName;
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

	private void resolerWhere() throws AptException {
		if (this.byBean) {
			this.resolerWhereByBean();
		} else {

			List<Column> list = this.po.getAllColumn();
			for (int i = 1; i < this.params.size(); ++i) {
				MethodParamEntry mpe = this.params.get(i);
				String pn = mpe.getName();
				boolean found = false;
				for (Column col : list) {
					if (pn.equals(col.getJavaName())) {
						col.initHandler(ref);
//						if (!TypeName.get(col.getHandler().supportsClass()).toString().equals(mpe.getTypeName())) {
//							throw new AptException(ref,
//									"param [" + pn + "] type not equals PersistentObject attribute type");
//						}
						this.values.add(col);
						col.getHandler().init(this.itemNames[i], false, false, this.attributes);
						found = true;
						break;
					}
				}
				if (!found) {
					throw new AptException(ref,
							"this method[@DeleteList] param[" + pn + "] not found in PersistentObject attributes");
				}
			}
		}
	}

	private void resolerWhereByBean() throws AptException {
		this.beanItemName = this.getTempalteVariableName();
		String unName = Utils.emptyToNull(this.deleteList.value());
		if (null == unName)
			throw new AptException(ref, "@Delete'value can't empty or null");
		UniqueConstraint unique = null;
		if (unName.equals("PrimaryKey")) {
			unique = this.po.getPrimaryKey();
			if (unique == null)
				throw new AptException(ref, "Table[" + this.po.getJavaName() + "] hasn't PrimaryKey");
		} else {
			unique = this.po.getUniqueConstraint(this.deleteList.value());
			if (unique == null)
				throw new AptException(ref,
						"Table[" + this.po.getJavaName() + "] hasn't unique constraint " + deleteList.value());
		}
		List<Column> list = this.po.getAllColumn();
		String[] keys = unique.getJavaNames();
		for (String key : keys) {
			for (Column col : list) {
				if (col.getJavaName().equals(key)) {
					this.values.add(col);
					col.initHandler(ref);
					col.getHandler().init(this.beanItemName + "." + col.getGetter() + "()", false, false,
							this.attributes);
				}
			}
		}
	}

	@Override
	protected void prepare() throws AptException {
		this.byList = true;
		this.deleteList = this.ref.getAnnotation(DeleteList.class);
		if (this.deleteList == null)
			throw new AptException(this.ref, "nofound @DeleteList on this method");
		if (!this.returnType.equals("int[]"))
			throw new AptException(ref, "this method(@DeleteList) must return int[]");

		try {
			poTypeName = this.deleteList.target().getName();
		} catch (MirroredTypeException e) {
			poTypeName = TypeName.get(e.getTypeMirror()).toString();
		}

		if (poTypeName.equals("java.lang.Object")) {
			this.byBean = true;
			if (this.params.size() != 2)
				throw new AptException(ref, "this method[@DeleteList(targer=Object.class)] parameters count must be 2");
			poTypeName = this.params.get(1).getTypeName();
			if (poTypeName.endsWith("[]")) {
				poTypeName = poTypeName.substring(0, poTypeName.length() - 2);
				this.byList = false;
			} else if (poTypeName.startsWith("java.util.List<") && poTypeName.endsWith(">")) {
				poTypeName = poTypeName.substring(15);
				poTypeName = poTypeName.substring(0, poTypeName.length() - 1).trim();
			} else {
				throw new AptException(this.ref,
						"this method second parameter must be java.util.List<Object> or Object[]");
			}

			this.po = ormDefine.getPersistentObject(this.poTypeName);
			if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
				throw new AptException(ref,
						"this method[@DeleteList(targer=Object.class)] secone parameter type must be WithAnnotation @Table");
			}
		} else {
			this.byBean = false;
			this.po = ormDefine.getPersistentObject(this.poTypeName);
			if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
				throw new AptException(ref,
						"this method[@DeleteList'target value must be Object.class or Class(@Table)");
			}
			this.validParamWithArrayOrList();
		}

		this.resolerWhere();
		sb.append("String sql=\"DELETE FROM ").append(this.po.getFromSentence());
		for (int i = 0; i < this.values.size(); ++i) {
			if (i != 0)
				sb.append(" AND ");
			else
				sb.append(" WHERE ");
			sb.append(this.values.get(i).getDbName()).append("=?");
		}
		sb.append("\";\r\n");
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
		if (this.byBean) {
			sb.append("for(").append(this.poTypeName).append(" ").append(this.beanItemName).append(":")
					.append(this.params.get(1).getName()).append("){");
		}else{
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
		}

		for (int i = 0; i < this.values.size(); ++i) {
			this.values.get(i).getHandler().prepare(sb);
		}
		for (int i = 0; i < this.values.size(); ++i) {
			this.values.get(i).getHandler().writeValue(sb, false);
		}
		sb.append("ps.addBatch();} return ps.executeBatch();");

		sb.append("}finally{\r\ntry{ps.close();}catch(Exception e){}\r\n}\r\n");
		sb.append("}");
		return sb.toString();
	}

	@Override
	protected void buildSqlParamter() {

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

	}
}
