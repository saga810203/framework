package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.DeleteWith;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.MethodParamEntry;
import org.jfw.apt.model.core.TypeName;

public class DeleteWithOperateCG extends DBOperateCG {
	private String value;
	private List<Column> where = new ArrayList<Column>();
	private PersistentObject po;

	@Override
	protected void prepare() throws AptException {
		DeleteWith with = this.ref.getAnnotation(DeleteWith.class);
		if (null == with)
			throw new AptException(this.ref, "nofound @DeleteWith on this method");

		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@DeleteWith) must return int");

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

		List<Column> list = this.po.getAllColumn();
		for (int i = 1; i < this.params.size(); ++i) {
			MethodParamEntry mpe = this.params.get(i);
			String pn = mpe.getName();
			boolean found = false;
			for (Column col : list) {
				if (col.getJavaName().equals(pn)) {
					this.where.add(col);
					col.initHandler(ref);
					col.getHandler().init(pn, false, false, this.attributes);
					col.getHandler().prepare(sb);
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
		sb.append("return ps.executeUpdate();");

	}

}
