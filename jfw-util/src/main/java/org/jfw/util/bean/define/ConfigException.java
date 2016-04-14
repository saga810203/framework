package org.jfw.util.bean.define;

public class ConfigException extends Exception {
	private static final long serialVersionUID = -3683286340446305667L;

	public ConfigException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigException(String message) {
		super(message);
	}
	
}
