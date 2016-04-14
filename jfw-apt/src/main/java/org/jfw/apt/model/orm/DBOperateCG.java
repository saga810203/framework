package org.jfw.apt.model.orm;

import javax.lang.model.element.ExecutableElement;

import org.jfw.apt.exception.AptException;
import org.jfw.apt.model.AbstractMethodGenerater;
import org.jfw.apt.model.MethodParamEntry;

public abstract class DBOperateCG extends AbstractMethodGenerater{
    protected OrmDefine ormDefine;
	
	protected boolean dynamic = false;
	protected StringBuilder sb;
	
	
	protected void checkJdbc() throws AptException{
		if(this.params.isEmpty()|| !this.params.get(0).getTypeName().equals("java.sql.Connection")|| !this.params.get(0).getName().equals("con"))
			throw new AptException(ref,"this method must be has param & the first param type is java.sql.Connection name is con");
		if(this.throwables.isEmpty() || !this.throwables.contains("java.sql.SQLException")){
			throw new AptException(ref,"this method must be throws java.sql.SQLException");
		}
		if(this.returnType.equals("void"))throw new AptException(ref,"ref must be have return type");
	}
	
	protected abstract void prepare() throws AptException;
	protected abstract void buildSqlParamter();
	
	protected abstract boolean needRelaceResource();
	protected abstract void relaceResource();
	protected abstract void buildHandleResult();
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
		boolean replaceSource = this.needRelaceResource();
		if(replaceSource) sb.append("try{\r\n");
		sb.append("java.sql.PreparedStatement ps = con.prepareStatement(sql");
		if (this.dynamic) {
			sb.append(".toString()");
		}
		sb.append(");\r\n");
		sb.append("try{");
		this.buildSqlParamter();
		this.buildHandleResult();
		sb.append("}finally{\r\ntry{ps.close();}catch(Exception e){}\r\n}\r\n");
		if(replaceSource){
			sb.append("}finally{");
			this.relaceResource();
			sb.append("}\r\n");
		}
		sb.append("}");			
		return sb.toString();
	}
	

	
	
//	protected void createPreparedStatement()
//	{
//		if(this.dynamic){
//			sb.append("")
//		}
//	}
	
	
	
}
