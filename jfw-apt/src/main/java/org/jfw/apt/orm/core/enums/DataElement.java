package org.jfw.apt.orm.core.enums;

import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.orm.core.defaultImpl.UnOrmInt;
import org.jfw.apt.orm.core.defaultImpl.*;


public enum DataElement {
	invalid_de(null,null,-1,-1,false,null,false,null,false),
	
	boolean_de(UnOrmBoolean.class,"CHAR",1,-1,false,null,true,null,true),
	Boolean_de(OrmBoolean.class,"CHAR",1,-1,true,null,true,null,true),
	byte_de(UnOrmByte.class,"BYTE",1,-1,false,null,true,null,true),
	Byte_de(OrmByte.class,"BYTE",1,-1,true,null,true,null,true),	
	short_de(UnOrmShort.class,"SHORT",1,-1,false,null,true,null,true),
	Short_de(OrmShort.class,"SHORTR",1,-1,true,null,true,null,true),	
    int_de(UnOrmInt.class,"INTEGER",-1,-1,false,null,true,null,true),
    Integer_de(OrmInt.class,"INTEGER",-1,-1,true,null,true,null,true),
    long_de(UnOrmLong.class,"LONG",-1,-1,false,null,true,null,true),
    Long_de(OrmLong.class,"LONG",-1,-1,true,null,true,null,true),
    float_de(UnOrmFloat.class,"LONG",-1,-1,false,null,true,null,true),
    Float_de(OrmFloat.class,"LONG",-1,-1,true,null,true,null,true),
    double_de(UnOrmDouble.class,"LONG",-1,-1,false,null,true,null,true),
    Doutble_de(OrmDouble.class,"LONG",-1,-1,true,null,true,null,true),
    
    string_de(OrmString.class,"VARCHAR",10,-1,false,null,true,null,true),
    String_de(OrmString.class,"VARCHAR",10,-1,true,null,true,null,true),    
    
    
    CreateTime_de(OrmString.class,"VARCHAR",14,-1,false,"TO_DATA('YYYYMMDDHH24MISS',SYSDATE)",false,null,true),
    ModifyTime_de(OrmString.class,"VARCHAR",14,-1,false,"TO_DATA('YYYYMMDDHH24MISS',SYSDATE)",true,"TO_DATA('YYYYMMDDHH24MISS',SYSDATE)",true),
    
	BIGDECMIMAL(OrmBigDecimal.class,"DECIMAL",10,10,true,null,true,null,true);
	
	
	
	private DataElement(Class<? extends OrmHandler> handlerClass, String dbType,int dbTypeLength,int dbTypePrecision,boolean nullable,
			String fixSqlValueWithInsert,boolean inUpdate,String fixSqlValueWithUpdate,boolean searchable)
	{
		this.handlerClass = handlerClass;
		this.dbType = dbType;
		this.dbTypeLength = dbTypeLength;
		this.dbTypePrecision = dbTypePrecision;
		this.nullable = nullable;
		this.fixSqlValueWithInsert = fixSqlValueWithInsert;
		this.inUpdate = inUpdate;
		this.fixSqlValueWithUpdate = fixSqlValueWithUpdate;
		this.searchable = searchable;		
	}
	private Class<? extends OrmHandler> handlerClass;
	private String dbType;
	private int dbTypeLength=0;
	private int dbTypePrecision=0;
	private boolean nullable;
	private String fixSqlValueWithInsert;
	private boolean inUpdate;
	private String fixSqlValueWithUpdate;
	private boolean searchable;	 
		
		
	public Class<? extends OrmHandler> getHandlerClass() {
		return handlerClass;
	}
	public String getDbType() {
		return dbType;
	}
	public int getDbTypeLength() {
		return dbTypeLength;
	}
	public int getDbTypePrecision() {
		return dbTypePrecision;
	}

	public boolean isNullable() {
		return nullable;
	}
	public String getFixSqlValueWithInsert() {
		return fixSqlValueWithInsert;
	}
	public boolean supportedUpdate() {
		return inUpdate;
	}
	public String getFixSqlValueWithUpdate() {
		return fixSqlValueWithUpdate;
	}
	public boolean supportedSearch() {
		return searchable;
	}

}
