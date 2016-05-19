package org.jfw.util.web.fileupload.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jfw.util.web.fileupload.Item;
import org.jfw.util.web.fileupload.ItemHeaders;
import org.jfw.util.web.fileupload.UploadItemIterator;

public class DefaultIterator implements UploadItemIterator {
	private static final String POST_METHOD = "POST";
	public static final String CONTENT_TYPE = "Content-type";
	public static final String CONTENT_DISPOSITION = "Content-disposition";
	public static final String CONTENT_LENGTH = "Content-length";
	public static final String FORM_DATA = "form-data";
	public static final String ATTACHMENT = "attachment";
	public static final String MULTIPART = "multipart/";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String MULTIPART_MIXED = "multipart/mixed";
	private String charEncoding;
	private String contentType;

	private MultipartStream multi;

	private byte[] boundary;

	private Item currentItem;

	private String currentFieldName;

	private boolean skipPreamble;

	private boolean itemValid;

	private boolean eof;

	private DefaultIterator() {
	}

	public static final boolean isMultipartContent(HttpServletRequest request) {
		if (!POST_METHOD.equalsIgnoreCase(request.getMethod())) {
			return false;
		}
		String contentType = request.getContentType();
		if (contentType == null) {
			return false;
		}
		if (contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART)) {
			return true;
		}
		return false;
	}

	public static long parseContentLength(HttpServletRequest request) {
		long size;
		try {
			size = Long.parseLong(request.getHeader(CONTENT_LENGTH));
		} catch (NumberFormatException e) {
			size = request.getContentLength();
		}
		return size;
	}

	private static byte[] getBoundary(String contentType) {
		ParameterParser parser = new ParameterParser();
		parser.setLowerCaseNames(true);
		Map<String, String> params = parser.parse(contentType, new char[] { ';', ',' });
		String boundaryStr = params.get("boundary");
		if (boundaryStr == null) {
			return null;
		}
		byte[] boundary;
		try {
			boundary = boundaryStr.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			boundary = boundaryStr.getBytes();
		}
		return boundary;
	}

	private int parseEndOfLine(String headerPart, int end) {
		int index = end;
		for (;;) {
			int offset = headerPart.indexOf('\r', index);
			if (offset == -1 || offset + 1 >= headerPart.length()) {
				throw new IllegalStateException("Expected headers to be terminated by an empty line.");
			}
			if (headerPart.charAt(offset + 1) == '\n') {
				return offset;
			}
			index = offset + 1;
		}
	}

	private void parseHeaderLine(ItemHeadersImpl headers, String header) {
		final int colonOffset = header.indexOf(':');
		if (colonOffset == -1) {
			return;
		}
		String headerName = header.substring(0, colonOffset).trim();
		String headerValue = header.substring(header.indexOf(':') + 1).trim();
		headers.addHeader(headerName, headerValue);
	}

	protected ItemHeaders getParsedHeaders(String headerPart) {
		final int len = headerPart.length();
		ItemHeadersImpl headers = new ItemHeadersImpl();
		int start = 0;
		for (;;) {
			int end = parseEndOfLine(headerPart, start);
			if (start == end) {
				break;
			}
			StringBuilder header = new StringBuilder(headerPart.substring(start, end));
			start = end + 2;
			while (start < len) {
				int nonWs = start;
				while (nonWs < len) {
					char c = headerPart.charAt(nonWs);
					if (c != ' ' && c != '\t') {
						break;
					}
					++nonWs;
				}
				if (nonWs == start) {
					break;
				}
				// Continuation line found
				end = parseEndOfLine(headerPart, nonWs);
				header.append(" ").append(headerPart.substring(nonWs, end));
				start = end + 2;
			}
			parseHeaderLine(headers, header.toString());
		}
		return headers;
	}

	private String getFileName(String pContentDisposition) {
		String fileName = null;
		if (pContentDisposition != null) {
			String cdl = pContentDisposition.toLowerCase(Locale.ENGLISH);
			if (cdl.startsWith(FORM_DATA) || cdl.startsWith(ATTACHMENT)) {
				ParameterParser parser = new ParameterParser();
				parser.setLowerCaseNames(true);
				// Parameter parser can handle null input
				Map<String, String> params = parser.parse(pContentDisposition, ';');
				if (params.containsKey("filename")) {
					fileName = params.get("filename");
					if (fileName != null) {
						fileName = fileName.trim();
					} else {
						// Even if there is no value, the parameter is present,
						// so we return an empty file name rather than no file
						// name.
						fileName = "";
					}
				}
			}
		}
		return fileName;
	}

	protected String getFileName(ItemHeaders headers) {
		return getFileName(headers.getHeader(CONTENT_DISPOSITION));
	}

	protected String getFieldName(ItemHeaders headers) {
		return getFieldName(headers.getHeader(CONTENT_DISPOSITION));
	}

	private String getFieldName(String pContentDisposition) {
		String fieldName = null;
		if (pContentDisposition != null && pContentDisposition.toLowerCase(Locale.ENGLISH).startsWith(FORM_DATA)) {
			ParameterParser parser = new ParameterParser();
			parser.setLowerCaseNames(true);
			Map<String, String> params = parser.parse(pContentDisposition, ';');
			fieldName = params.get("name");
			if (fieldName != null) {
				fieldName = fieldName.trim();
			}
		}
		return fieldName;
	}

	private void buildCurrentItem(String pFileName, String pFieldName, ItemHeaders headers) throws IOException {
		String pContentType = headers.getHeader(CONTENT_TYPE);
		InputStream tIn = this.multi.newInputStream();
		if (pFileName == null) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			byte[] bs = new byte[4096];
			int len;
			try{
			while((len=tIn.read(bs))!=-1){
				if(len>0) os.write(bs,0,len);
			}
			}finally{
				tIn.close();
			}
			this.currentItem = new FormFieldItem(pFieldName, pContentType, os.toByteArray(), headers);
		}else{
			this.currentItem = new FileFieldItem(pFileName, pFieldName, pContentType, tIn, headers);
		}
	}

	public static DefaultIterator build(HttpServletRequest request) throws IOException {
		InputStream is = request.getInputStream();
		DefaultIterator it = new DefaultIterator();
		it.charEncoding = request.getCharacterEncoding();
		it.contentType = request.getContentType();
		it.boundary = getBoundary(it.contentType);
		if (it.boundary == null) {
			throw new IOException("the request was rejected because no multipart boundary was found");
		}

		it.multi = new MultipartStream(is, it.boundary);
		it.multi.setHeaderEncoding(it.charEncoding);
		it.skipPreamble = true;
		it.findNextItem();
		return it;
	}

	private boolean findNextItem() throws IOException {
		if (currentItem != null) {
			if (currentItem instanceof FileFieldItem) {
				((FileFieldItem) currentItem).getInputStream().close();
			}
			currentItem = null;
		}
		for (;;) {
			boolean nextPart;
			if (skipPreamble) {
				nextPart = multi.skipPreamble();
			} else {
				nextPart = multi.readBoundary();
			}
			if (!nextPart) {
				if (currentFieldName == null) {
					eof = true;
					return false;
				}
				multi.setBoundary(boundary);
				currentFieldName = null;
				continue;
			}
			ItemHeaders headers = getParsedHeaders(multi.readHeaders());
			if (currentFieldName == null) {
				String fieldName = getFieldName(headers);
				if (fieldName != null) {
					String subContentType = headers.getHeader(CONTENT_TYPE);
					if (subContentType != null
							&& subContentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART_MIXED)) {
						currentFieldName = fieldName;
						byte[] subBoundary = getBoundary(subContentType);
						multi.setBoundary(subBoundary);
						skipPreamble = true;
						continue;
					}
					String fileName = getFileName(headers);

					this.buildCurrentItem(fileName, fieldName, headers);

					itemValid = true;
					return true;
				}
			} else {
				String fileName = getFileName(headers);
				if (fileName != null) {

					this.buildCurrentItem(fileName, currentFieldName, headers);
					itemValid = true;
					return true;
				}
			}
			multi.discardBodyData();
		}
	}

	@Override
	public boolean hasNext() throws IOException {
		if (eof) {
			return false;
		}
		if (itemValid) {
			return true;
		}
		return findNextItem();
	}

	@Override
	public Item next() {
		return this.currentItem;
	}

	@Override
	public void clean() {
		byte[] skipBuf = new byte[4096];
		if (eof)
			return;
		try {
			while (this.hasNext()) {
				Item item = this.next();
				if (item instanceof FileFieldItem) {
					InputStream iStream = item.getInputStream();
					for (;;) {
						if (iStream.read(skipBuf) == -1)
							break;
					}
				}
			}
		} catch (Throwable th) {

		}

	}
}
