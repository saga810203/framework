package org.jfw.util.web.fileupload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public interface Item {
	ItemHeaders getHeaders();
	InputStream getInputStream() throws IOException;

	String getContentType();

	String getName();

	long getSize();

	byte[] get()throws IOException;

	String getString(String encoding) throws UnsupportedEncodingException;

	String getString();

	void write(OutputStream stream) throws Exception;

	String getFieldName();

	boolean isFormField();
}
