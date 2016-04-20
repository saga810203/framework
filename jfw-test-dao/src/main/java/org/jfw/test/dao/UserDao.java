package org.jfw.test.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jfw.apt.annotation.orm.DataBaseHandler;
import org.jfw.apt.annotation.orm.Query;
import org.jfw.apt.annotation.orm.SqlValue;
import org.jfw.apt.annotation.orm.Where;
import org.jfw.apt.orm.core.enums.DataElement;
import org.jfw.test.po.User;

@DataBaseHandler
public interface UserDao {
	@Query 
	List<User> query(Connection con) throws SQLException;
	
	@Query(singleRow=true,where=@Where(dynamic=true,values={@SqlValue(de=DataElement.string_de,nullable=false,paramName="id",valueExpression="ID=?")}))
	User query(Connection con,String id) throws SQLException;
	@Query(singleRow=true,singleColumn= DataElement.String_de,singleColumnSql="SELECT MAX(ID) FROM USER",
			where=@Where(sentence=" Enabled='1'"))
	String queryMaxId(Connection con)throws SQLException;
}
