package org.jfw.util.auth;

public interface AuthUser {
	String getId();
	String getOrgCode();
	boolean hasAuthority(int auth);
}
