package org.jfw.apt.orm.core;

import javax.lang.model.type.ExecutableType;

import org.jfw.apt.exception.AptException;

public interface DaoHandler {
	
	void handler(ExecutableType exec) throws AptException;
}
