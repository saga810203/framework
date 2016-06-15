package org.jfw.test.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jfw.apt.annotation.orm.DataBaseHandler;
import org.jfw.apt.annotation.orm.Query;
import org.jfw.apt.annotation.orm.SqlValue;
import org.jfw.apt.annotation.orm.Update;
import org.jfw.apt.annotation.orm.Where;
import org.jfw.apt.orm.core.enums.DE;
import org.jfw.test.po.DView;
import org.jfw.test.po.Score;
import org.jfw.test.po.View;

@DataBaseHandler
public interface UserScoreDao {
	@Query(where = @Where(dynamic = false, sentence = "USER.ID LIKE ?", values = {
			@SqlValue(paramExpression = "\"12%\"", de = DE.string_de) }))
	List<View> query(Connection con) throws SQLException;
	
	@Query(where = @Where(dynamic = false, sentence = "USER.ID LIKE ?", values = {
			@SqlValue(paramExpression = "idex", de = DE.string_de) }))
	List<View> query(Connection con,String idex) throws SQLException;
	
	@Query
	List<DView> query2(Connection con) throws SQLException;
	
	@Update 
	int update(Connection con,Score score) throws SQLException;

}
