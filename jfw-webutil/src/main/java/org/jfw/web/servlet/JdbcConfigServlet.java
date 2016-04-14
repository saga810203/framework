package org.jfw.web.servlet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public class JdbcConfigServlet extends FilenameConfigServlet {
	private static final long serialVersionUID = -6623944599398162432L;

	public static final String OVER_CONFIG = "verrrideFileConfig";

	public static final String JDBC_DRIVER = "driverClassName";
	public static final String JDBC_URL = "url";
	public static final String JDBC_UN = "username";
	public static final String JDBC_PW = "password";
	public static final String JDBC_SQL = "sql";

	private String dirverClassname;
	private String jdbcUrl;
	private String jdbcUsername;
	private String jdbcPassword;
	private String jdbcSql;

	protected boolean initJdbcEvn() {
		String tmp = this.getServletConfig().getInitParameter(JDBC_DRIVER);
		if (tmp == null || tmp.trim().length() == 0) {
			this.log(JdbcConfigServlet.class.getName() + ":param[" + JDBC_DRIVER + "] is null or empty_string");
			return false;
		} else {
			this.dirverClassname = tmp.trim();
		}
		tmp = this.getServletConfig().getInitParameter(JDBC_URL);
		if (tmp == null || tmp.trim().length() == 0) {
			this.log(JdbcConfigServlet.class.getName() + ":param[" + JDBC_URL + "] is null or empty_string");
			return false;
		} else {
			this.jdbcUrl = tmp.trim();
		}
		tmp = this.getServletConfig().getInitParameter(JDBC_UN);
		if (tmp == null || tmp.trim().length() == 0) {
			this.log(JdbcConfigServlet.class.getName() + ":param[" + JDBC_UN + "] is null or empty_string");
			return false;
		} else {
			this.jdbcUsername = tmp.trim();
		}
		tmp = this.getServletConfig().getInitParameter(JDBC_PW);
		if (tmp == null || tmp.trim().length() == 0) {
			this.log(JdbcConfigServlet.class.getName() + ":param[" + JDBC_PW + "] is null or empty_string");
			return false;
		} else {
			this.jdbcPassword = tmp.trim();
		}
		tmp = this.getServletConfig().getInitParameter(JDBC_SQL);
		if (tmp == null || tmp.trim().length() == 0) {
			this.log(JdbcConfigServlet.class.getName() + ":param[" + JDBC_SQL + "] is null or empty_string");
			return false;
		} else {
			this.jdbcSql = tmp.trim();
		}
		return true;
	}

	protected void buildConfigWithJdbc(Properties props) {
		try {
			Class.forName(this.dirverClassname);
			Connection con = DriverManager.getConnection(this.jdbcUrl, this.jdbcUsername, this.jdbcPassword);
			try {
				Statement st = con.createStatement();
				try {
					ResultSet rs = st.executeQuery(this.jdbcSql);
					try {
						while (rs.next()) {
							props.put(rs.getString(1), rs.getString(2));
						}
					} finally {
						try {
							rs.close();
						} catch (Throwable th) {
						}
					}
				} finally {
					try {
						st.close();
					} catch (Throwable th) {
					}
				}

			} finally {
				try {
					con.close();
				} catch (Throwable th) {
				}
			}

		} catch (Throwable th) {
			props.clear();
			this.log(JdbcConfigServlet.class.getName() + ":load config with jdbc sql error", th);
			failStart(th);
		}
	}

	@Override
	protected void buildBeanFactoryConfig() {
		if (Boolean.valueOf(this.getServletConfig().getInitParameter(OVER_CONFIG)))
			super.buildBeanFactoryConfig();
		if (this.config == null)
			this.config = new Properties();
		if (this.initJdbcEvn()) {
			this.buildConfigWithJdbc(this.config);
		} else {
			failStart("无法加载数据库中的参数");
		}
	}

}
