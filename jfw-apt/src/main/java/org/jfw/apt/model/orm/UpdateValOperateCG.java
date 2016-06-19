package org.jfw.apt.model.orm;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import javax.lang.model.type.MirroredTypeException;

import org.jfw.apt.annotation.orm.UpdateVal;
import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.core.TypeName;

public class UpdateValOperateCG extends DBOperateCG{
	private List<Column> where = new ArrayList<Column>();
	private List<Column> values = new ArrayList<Column>();	
	private List<Column> columns;
	private List<String> colNames = new ArrayList<String>();
	private UpdateVal updateVal;
	private List<String> whereColNames = new ArrayList<String>();
	private PersistentObject po;
	private List<String> fixValColumns = new ArrayList<String>();
	
	

	private void resolerUpdateColumns() throws AptException{
		for(int i = 1; i < this.params.size(); ++i){
			String pn = this.params.get(i).getName();
			boolean found =false;
			for(Column col:this.columns){
				if(col.getJavaName().equals(pn)){
					found = true;
					if(this.whereColNames.contains(pn))	 this.where.add(col);
					else this.values.add(col);
					col.initHandler(ref);
					col.getHandler().init(pn, false, false, this.attributes);
				}
			}
			if(!found){
				throw new AptException(ref,"param["+pn+"] not found in persistentObject attributes");				
			}
		}
		if(this.values.isEmpty()) throw new AptException(ref,"not found change value is define");
		if(!this.updateVal.includeFixValues()) return;
		for(Column col:this.columns){
			if(this.where.contains(col)) continue;
			if(this.values.contains(col)) continue;
			if(col.getFixUpdateSqlValue()!=null){
				this.values.add(col);
				this.fixValColumns.add(col.getJavaName());
			}
		}
	}
	@Override
	protected void prepare() throws AptException {
		this.updateVal = this.ref.getAnnotation(UpdateVal.class);
		if(null== this.updateVal)
			throw new AptException(this.ref, "nofound @UpdateVal on this method");
		
		if (!this.returnType.equals("int"))
			throw new AptException(ref, "this method(@UpdateVal) must return int");
		
		String targetClassName = null;

		try {
			targetClassName = this.updateVal.target().getName();
		} catch (MirroredTypeException e) {
			targetClassName = TypeName.get(e.getTypeMirror()).toString();
		}
		this.po = ormDefine.getPersistentObject(targetClassName);
		if (this.po == null || this.po.getKind() != PersistentObjectKind.TABLE) {
			throw new AptException(ref, "this method[@UpdateVal'target value must be Object.class or Class(@Table)");
		}
		String[] tmpArray= this.updateVal.where();
		if(tmpArray!=null && tmpArray.length>0){
			for(String str:tmpArray){
				if(str!=null && str.trim().length()>0) this.whereColNames.add(str.trim());
			}
		}
		this.columns = this.po.getAllColumn();
		for(ListIterator<Column> it = this.columns.listIterator(); it.hasNext();){
			this.colNames.add(it.next().getJavaName());
		}
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
		sb.append("return ps.executeUpdate();");		
	}

}
