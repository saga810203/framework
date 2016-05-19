package org.jfw.util.web.fileupload.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.jfw.util.web.fileupload.Item;
import org.jfw.util.web.fileupload.ItemHeaders;

class FileFieldItem implements Item {
	public static final String DEFAULT_CHARSET = "UTF-8";
	private final String fieldName;
	private final String contentType;
	private final ItemHeaders headers;
	private final InputStream in;
	private final String name;

	FileFieldItem(String name, String fieldName, String contentType, InputStream in, ItemHeaders headers) {
		this.name = name;
		this.fieldName = fieldName;
		this.contentType = contentType;
		this.in = in;
		this.headers = headers;
	}

	@Override
	public ItemHeaders getHeaders() {
		return this.headers;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.in;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public long getSize() {
		throw new UnsupportedOperationException();
	}

	@Override
	public byte[] get() throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buf = new byte[8092];
		int len;
		while ((len = in.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		return os.toByteArray();
	}

	@Override
	public String getString(String encoding) throws UnsupportedEncodingException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getString() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(OutputStream stream) throws Exception {
		byte[] buf = new byte[8092];
		int len;
		while ((len = in.read(buf)) != -1) {
			if(len>0)
			stream.write(buf, 0, len);
		}

	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}

	@Override
	public boolean isFormField() {
		return false;
	}

}
