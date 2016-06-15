package org.jfw.util.exception;

public class JfwBaseException extends Exception {

	private static final long serialVersionUID = 6512669010239097709L;
	
	private int code;
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public JfwBaseException() {
		super();
		this.code = 0;
	}
	public JfwBaseException(int code) {
		super();
		this.code = code;
	}

	public JfwBaseException(String message, Throwable cause) {
		super(message, cause);
		this.code = 0;
	}
	public JfwBaseException(int code,String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
	public JfwBaseException(String message) {
		super(message);
		this.code = 0;
	}
	public JfwBaseException(int code,String message) {
		super(message);
		this.code = code;
	}
	public JfwBaseException(int code ,Throwable cause) {
		super(cause);
		this.code = code;
	}
	public JfwBaseException(Throwable cause) {
		super(cause);
		this.code = 0;
	}
	
	

}
