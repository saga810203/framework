package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.Delete;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.core.TypeName;

public class DeleteOperateCG extends DBOperateCG {

	private Delete delete;
	private PersistentObject po;
	private List<Column> values = new ArrayList<Column>();
	private boolean byBean = true;

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
						if (!TypeName.get(col.getHandler().supportsClass()).toString().equals(mpe.getTypeName())) {
							throw new AptException(ref,
									"param [" + pn + "] type not equals PersistentObject attribute type");
						}
						this.values.add(col);
						col.getHandler().init(pn, false, false, this.attributes);
						col.getHandler().prepare(sb);
						found = true;
						break;
					}
				}
				if (!found) {
					throw new AptException(ref,
							"this method[@Delete] param[" + pn + "] not found in PersistentObject attributes");
				}
			}
		}
	}

	private void resolerWhereByBean() throws AptException {

		String unName = Utils.emptyToNull(this.delete.value());
		if (null == unName)
			throw new AptException(ref, "@Delete'value can't empty or null");
		UniqueConstraint unique = null;
		if (unName.equals("PrimaryKey")) {
			unique = this.po.getPrimaryKey();
			if (unique == null)
				throw new AptException(ref, "Table[" + this.po.getJavaName() + "] hasn't PrimaryKey");
		} else {
			unique = this.po.getUniqueConstraint(this.delete.value());
			if (unique == null)
				throw new AptException(ref,
						"Table[" + this.po.getJavaName() + "] hasn't unique constraint " + delete.value());
		}
		List<Column> list = this.po.getAllColumn();
		String[] keys = unique.getJavaNames();
		for (String key : keys) {
			for (Column col : list) {
				if (col.getJavaName().equals(key)) {
					this.values.add(col);
					col.initHandler(ref);
					col.getHandler().init(this.params.get(1).getName() + "." + col.getGetter() + "()", false, false,
							this.attributes);
					col.getHandler().prepare(sb);
				}
			}
		}
	}

	@Override
	protected void prepare() throws AptException {
		this.delete = this.ref.getAnnotation(Delete.class);
		if (this.delete == null)
			throw new AptException(this.ref, "nofound @Delete on this method");
		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@Delete) must return int");

		String targetClassName = null;

		try {
			targetClassName = this.delete.target().getName();
		} catch (MirroredTypeException e) {
			targetClassName = TypeName.get(e.getTypeMirror()).toString();
		}

		if (targetClassName.equals("java.lang.Object")) {
			this.byBean = true;
			if (this.params.size() != 2)
				throw new AptException(ref, "this method[@Delete(targer=Object.class)] parameters count must be 2");
			targetClassName = this.params.get(1).getTypeName();
			this.po = ormDefine.getPersistentObject(targetClassName);
			if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
				throw new AptException(ref,
						"this method[@Delete(targer=Object.class)] secone parameter type must be WithAnnotation @Table");
			}
		} else {
			this.byBean = false;
			this.po = ormDefine.getPersistentObject(targetClassName);
			if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
				throw new AptException(ref, "this method[@Delete'target value must be Object.class or Class(@Table)");
			}
		}
		this.resolerWhere();

		sb.append("String sql=\"DELETE FROM ").append(this.po.getFromSentence());
		for (int i = 0; i < this.values.size(); ++i) {
			if (i != 0) {
				sb.append(" AND ");
			} else {
				sb.append(" WHERE ");
			}
			sb.append(this.values.get(i).getDbName()).append("=?");
		}
		sb.append("\";\r\n");
	}

	@Override
	protected void buildSqlParamter() {
		for (int i = 0; i < this.values.size(); ++i) {
			this.values.get(i).getHandler().writeValue(sb, false);
		}
	}

	@Override
	protected void buildHandleResult() {
		sb.append("return ps.executeUpdate();");

	}

	@Override
	protected boolean needRelaceResource() {
		for (Column col : this.values) {
			if (col.getHandler().isReplaceResource())
				return true;
		}
		return false;

	}

	@Override
	protected void relaceResource() {
		for (Column col : this.values) {
			col.getHandler().replaceResource(sb);
		}

	}

}
