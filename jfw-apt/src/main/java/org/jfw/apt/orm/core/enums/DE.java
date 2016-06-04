package org.jfw.apt.orm.core.enums;

import org.jfw.apt.orm.core.OrmHandler;
import org.jfw.apt.orm.core.defaultImpl.UnOrmInt;
import org.jfw.apt.orm.core.defaultImpl.*;


public enum DE {
	invalid_de(null,null,-1,-1,false,null,null,false),
	
	boolean_de(UnOrmBoolean.class,"CHAR",1,-1,false,null,null,true),
	Boolean_de(OrmBoolean.class,"CHAR",1,-1,true,null,null,true),
	byte_de(UnOrmByte.class,"BYTE",1,-1,false,null,null,true),
	Byte_de(OrmByte.class,"BYTE",1,-1,true,null,null,true),	
	short_de(UnOrmShort.class,"SHORT",1,-1,false,null,null,true),
	Short_de(OrmShort.class,"SHORTR",1,-1,true,null,null,true),	
    int_de(UnOrmInt.class,"INTEGER",-1,-1,false,null,null,true),
    Integer_de(OrmInt.class,"INTEGER",-1,-1,true,null,null,true),
    long_de(UnOrmLong.class,"LONG",-1,-1,false,null,null,true),
    Long_de(OrmLong.class,"LONG",-1,-1,true,null,null,true),
    float_de(UnOrmFloat.class,"LONG",-1,-1,false,null,null,true),
    Float_de(OrmFloat.class,"LONG",-1,-1,true,null,null,true),
    double_de(UnOrmDouble.class,"LONG",-1,-1,false,null,null,true),
    Doutble_de(OrmDouble.class,"LONG",-1,-1,true,null,null,true),    
    string_de(OrmString.class,"VARCHAR",10,-1,false,null,null,true),
    String_de(OrmString.class,"VARCHAR",10,-1,true,null,null,true), 
    
    date_de(OrmString.class,"CHAR",8,-1,false,null,null,true),
    Date_de(OrmString.class,"CHAR",8,-1,true,null,null,true),
    
    time_de(OrmString.class,"CHAR",6,-1,false,null,null,true),
    Time_de(OrmString.class,"CHAR",6,-1,true,null,null,true), 
    
    dateTime_de(OrmString.class,"CHAR",14,-1,false,null,null,true),
    DateTime_de(OrmString.class,"CHAR",14,-1,true,null,null,true), 
    
    
    CreateTime_de(OrmString.class,"CHAR",14,-1,false,"TO_CHAR('YYYYMMDDHH24MISS',SYSDATE)",null,true),
    ModifyTime_de(OrmString.class,"CHAR",14,-1,false,"TO_CHAR('YYYYMMDDHH24MISS',SYSDATE)","TO_CHAR('YYYYMMDDHH24MISS',SYSDATE)",true),
    
	BIGDECMIMAL(OrmBigDecimal.class,"DECIMAL",10,10,true,null,null,true);
	
	
	
	private DE(Class<? extends OrmHandler> handlerClass, String dbType,int dbTypeLength,int dbTypePrecision,boolean nullable,
			String fixSqlValueWithInsert,String fixSqlValueWithUpdate,boolean searchable)
	{
		this.handlerClass = handlerClass;
		this.dbType = dbType;
		this.dbTypeLength = dbTypeLength;
		this.dbTypePrecision = dbTypePrecision;
		this.nullable = nullable;
		this.fixSqlValueWithInsert = fixSqlValueWithInsert;
		this.fixSqlValueWithUpdate = fixSqlValueWithUpdate;
		this.searchable = searchable;		
	}
	private Class<? extends OrmHandler> handlerClass;
	private String dbType;
	private int dbTypeLength=0;
	private int dbTypePrecision=0;
	private boolean nullable;
	private String fixSqlValueWithInsert;
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
	public String getFixSqlValueWithUpdate() {
		return fixSqlValueWithUpdate;
	}
	public boolean supportedSearch() {
		return searchable;
	}

}
