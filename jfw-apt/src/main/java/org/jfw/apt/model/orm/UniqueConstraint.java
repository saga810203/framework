package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jfw.apt.Utils;
import org.jfw.apt.annotation.orm.Unique;
import org.jfw.apt.exception.AptException;

public class UniqueConstraint {
	private int columnCount;
	private String[] javaNames;
	private String[] columnNames;
	private String name;

	protected UniqueConstraint() {
	}

	public static UniqueConstraint build(PersistentObject table, Unique unique,boolean isPrimaryKey) throws AptException {
		UniqueConstraint uc = new UniqueConstraint();
		uc.name = Utils.emptyToNull(unique.name());
        if(isPrimaryKey)uc.name ="PrimaryKey"; 
		if (uc.name == null) {
			throw new AptException(table.getRef(), "@Unique'name must be not null empty");
		}
		if(uc.name.equals("PrimaryKey")&& (!isPrimaryKey)){
			throw new AptException(table.getRef(), "@Unique'name must be not PrimaryKey");
		}
		
		uc.javaNames = unique.value();

		if (uc.javaNames == null || uc.javaNames.length == 0) {
			throw new AptException(table.getRef(), "not field in @Unique:"+uc.name);
		}
		Set<String> jset = new HashSet<String>();
		for (String s : uc.javaNames) {
			jset.add(s);
		}
		if (uc.javaNames.length != jset.size())
			throw new AptException(table.getRef(), "has same filed in @Unique:"+uc.name);


		List<String> cols = new ArrayList<String>();
		for (Column col : table.getAllColumn()) {
			cols.add(col.getJavaName());
		}
		uc.columnNames = new String[uc.javaNames.length];
		for (int i = 0; i < uc.javaNames.length; ++i) {
			String jn = uc.javaNames[i];
			uc.columnNames[i] = Utils.javaNameConverToDbName(jn);
			if (!cols.contains(jn))
				throw new AptException(table.getRef(), "nofound field(" + jn + ") in table");
		}
		return uc;
	}



	public String getName() {
		return this.name;
	}

	public int getColumnCount() {
		return columnCount;
	}

	public String[] getJavaNames() {
		return javaNames;
	}

	public String[] getColumnNames() {
		return columnNames;
	}

}
