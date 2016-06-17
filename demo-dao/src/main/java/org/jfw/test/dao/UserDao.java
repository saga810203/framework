package org.jfw.test.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jfw.apt.annotation.orm.CustomDMLSQL;
import org.jfw.apt.annotation.orm.DataBaseHandler;
import org.jfw.apt.annotation.orm.Delete;
import org.jfw.apt.annotation.orm.Insert;
import org.jfw.apt.annotation.orm.InsertList;
import org.jfw.apt.annotation.orm.Query;
import org.jfw.apt.annotation.orm.SelectList;
import org.jfw.apt.annotation.orm.SelectOne;
import org.jfw.apt.annotation.orm.SqlValue;
import org.jfw.apt.annotation.orm.Update;
import org.jfw.apt.annotation.orm.Where;
import org.jfw.apt.orm.core.enums.DE;
import org.jfw.test.po.ExtendUser;
import org.jfw.test.po.User;

@DataBaseHandler
public interface UserDao {
	@Query
	List<User> query(Connection con) throws SQLException;
	
	@Query
	List<ExtendUser> queryExtendUser(Connection con) throws SQLException;

	@Query(singleRow = true, where = @Where(dynamic = true, values = {
			@SqlValue(de = DE.string_de,paramName = "id", sqlExpression = "ID=?") }))
	User query(Connection con, String id) throws SQLException;
	
	
	@Query(singleRow = true, where = @Where(sentence="EANBLE='1'", dynamic = true, values = {
			@SqlValue(de = DE.String_de,  paramName ="id", sqlExpression = "ID=?"),
			@SqlValue(de=DE.String_de,paramName="loginName",sqlExpression="LOGIN_NAME=?")}))
	User query(Connection con, String id,String loginName) throws SQLException;
	
	
	@Query(singleRow = true, where = @Where(sentence="ID=? AND LOGIN_NAME =? AND EANBLE=?", dynamic = false, values = {
			@SqlValue(de = DE.string_de, paramName = "id"),
			@SqlValue(de=DE.string_de,paramName="loginName"),
			@SqlValue(de=DE.boolean_de,paramName="enable")}))
	User query(Connection con, String id,String loginName,boolean enable) throws SQLException;
	
	
	

	@Query(singleRow = true, singleColumn = DE.String_de, singleColumnSql = "SELECT  MAX(ID) FROM USER", where = @Where(sentence = " Enabled='1'"))
	String queryMaxId(Connection con) throws SQLException;
	
	
	

	@Insert
	int insert(Connection con, User user) throws SQLException,IOException;

	@Update
	int update(Connection con, User user) throws SQLException,IOException;

	@Update(dynamicValue = true)
	int updateChoose(Connection con, User user) throws SQLException,IOException;

	@Delete
	int delete(Connection con, User user) throws SQLException;

	@Delete(target = User.class, value = "Unique_loginName")
	int deleteByLoginName(Connection con, String loginName) throws SQLException;

	@CustomDMLSQL(value = "DELETE FROM USER WHERE ENABLE='0'")
	int clean(Connection con) throws SQLException;

	@CustomDMLSQL(value = "DELETE FROM USER WHERE ENABLE=? AND LOGIN_NAME=?", sqlValues = {
			@SqlValue(de = DE.boolean_de, paramName = "enable"),
			@SqlValue(de = DE.string_de, paramName = "loginName") })
	int clean(Connection con, boolean enable, String loginName) throws SQLException;
	
	@SelectList
	List<User> queryList(Connection con,String id,String name) throws SQLException;
	@SelectOne
	User queryOne(Connection con,String id,String name) throws SQLException;
	
	@InsertList
	int[] insert(Connection con,List<User> users) throws SQLException,IOException;
	@InsertList
	int[] insert(Connection con,User[] users) throws SQLException,IOException;

}
