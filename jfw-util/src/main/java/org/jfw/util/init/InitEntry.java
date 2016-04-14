package org.jfw.util.init;

import java.util.Collection;

public interface InitEntry {
 void	execute() throws Throwable;
 Collection<InitEntry> getDependBeanIds();
}
