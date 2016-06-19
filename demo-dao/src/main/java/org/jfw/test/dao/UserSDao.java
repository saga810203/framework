package org.jfw.test.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.jfw.apt.annotation.orm.DataBaseHandler;
import org.jfw.apt.annotation.orm.DeleteList;
import org.jfw.apt.annotation.orm.InsertList;
import org.jfw.apt.annotation.orm.UpdateList;
import org.jfw.apt.annotation.orm.UpdateListVal;
import org.jfw.apt.annotation.orm.UpdateVal;
import org.jfw.test.po.User;

@DataBaseHandler
public abstract class UserSDao {
	@InsertList
	public abstract int[] insert(Connection con,List<User> users) throws SQLException,IOException;
	@InsertList
	public abstract int[] insert(Connection con,User[] users) throws SQLException,IOException;
	
	@UpdateList
	public abstract int[] update(Connection con,List<User> users) throws SQLException,IOException;
	
	
	@UpdateList
	public abstract int[] update(Connection con,User[] users) throws SQLException,IOException;
	
	@DeleteList
	public abstract int[] delete(Connection con,User[] users) throws SQLException,IOException;
	
	@DeleteList
	public abstract int[] delete(Connection con,List<User> users) throws SQLException,IOException;
	
	@DeleteList(target=User.class)
	public abstract int[] deleteById(Connection con,String[] id) throws SQLException,IOException;
	
	@DeleteList(target=User.class)
	public abstract int[] deleteById(Connection con,List<String> id) throws SQLException,IOException;
	
	
	@UpdateListVal(target=User.class,where="id")
	public abstract int[] update(Connection con,List<String> id,List<String> name) throws SQLException,IOException;
	
	
	@UpdateListVal(target=User.class,where="id")
	public abstract int[] update(Connection con,String[] id,String[] name) throws SQLException,IOException;
	
	
	@UpdateVal(target=User.class,where="id")
	public abstract int update(Connection con,String id,String name) throws SQLException,IOException;
	
	@UpdateVal(target=User.class,where={"id","loginName"})
	public abstract int update(Connection con,String id,String name,String loginName,String commm) throws SQLException,IOException;
	
}
