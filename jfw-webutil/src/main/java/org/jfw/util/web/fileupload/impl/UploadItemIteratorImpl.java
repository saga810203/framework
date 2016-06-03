package org.jfw.util.web.fileupload.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.jfw.util.web.fileupload.Item;
import org.jfw.util.web.fileupload.UploadItemIterator;

public class UploadItemIteratorImpl implements UploadItemIterator {
	private static final String POST_METHOD = "POST";
	public static final String CONTENT_TYPE = "Content-type";
	public static final String CONTENT_DISPOSITION = "Content-disposition";
	public static final String CONTENT_LENGTH = "Content-length";
	public static final String FORM_DATA = "form-data";
	public static final String ATTACHMENT = "attachment";
	public static final String MULTIPART = "multipart/";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String MULTIPART_MIXED = "multipart/mixed";

	public static final byte CR = 0x0D;
	public static final byte LF = 0x0A;
	public static final byte DASH = 0x2D;
	public static final int HEADER_PART_SIZE_MAX = 10240;
	protected static final int DEFAULT_BUFSIZE = 8192;
	protected static final int HALF_BUFSIZE = DEFAULT_BUFSIZE / 2;
	protected static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };
	protected static final byte[] FIELD_SEPARATOR = { CR, LF };
	protected static final byte[] STREAM_TERMINATOR = { DASH, DASH };
	protected static final byte[] BOUNDARY_PREFIX = { CR, LF, DASH, DASH };
	
	private byte[] orginBoundary;
	private byte[] marker;
	private byte[] shareCache ;
	private InputStream input;
	private int boundaryLength;
	// private int minFillCount4FindBoundary;
	private byte[] boundary;
	private byte[] buffer;
	private int head;
	private int tail;
	private String charEncoding;
	private String contentType;
	private ItemInputStream currentFileFieldItemStream;

	private boolean eof;

	private String currentFieldName;

	private int pos4NextBoundary;

	private UploadItemIteratorImpl() {
		this.marker = new byte[2];
		this.shareCache = new byte[4096];
		this.buffer = new byte[DEFAULT_BUFSIZE];
		this.buffer[0] = CR;
		this.buffer[1] = LF;
		this.head = 0;
		this.tail = 2;
		this.eof = false;
		this.pos4NextBoundary = 0;
		this.currentFieldName = null;
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

	private int canReadNumber() {
		return this.tail - this.head;
	}

	private int readBuf(byte[] bs, int off, int len) throws IOException {
		int limit;
		if (this.pos4NextBoundary == -1) {
			this.pos4NextBoundary = this.findBoundaryInCache();
		}
		if (this.pos4NextBoundary == -1) {
			limit = this.tail - this.head - this.boundaryLength + 1;
		} else {
			limit = this.pos4NextBoundary - this.head;
		}
		if (limit == 0)
			return -1;

		if (limit < len) {
			System.arraycopy(this.buffer, this.head, bs, off, limit);
			this.head += limit;
			return limit;
		} else {
			System.arraycopy(this.buffer, this.head, bs, off, len);
			this.head += len;
			return len;
		}
	}

	private int fillBuffer(int minNum) throws IOException {
		int result = 0;
		if (!this.eof) {
			int num4CanRead = this.canReadNumber();
			if (num4CanRead < minNum) {
				if (num4CanRead == 0) {
					this.head = 0;
					this.tail = 0;
				} else if ((this.head > HALF_BUFSIZE) || ((DEFAULT_BUFSIZE - this.head) < minNum)) {
					System.arraycopy(this.buffer, this.head, this.buffer, 0, num4CanRead);
					this.tail = num4CanRead;
					this.head = 0;
					if (this.pos4NextBoundary != -1) {
						this.pos4NextBoundary = this.pos4NextBoundary - num4CanRead;
					}
				}
				int len;
				while ((len = (DEFAULT_BUFSIZE - this.tail)) > 0) {
					try {
						len = this.input.read(this.buffer, this.tail, len);
					} catch (IOException e) {
						try {
							this.input.close();
						} catch (Throwable th) {
						}
						throw e;
					}
					if (len == -1) {
						this.eof = true;
						this.input.close();
						break;
					}
					if (len > 0) {
						this.tail += len;
						num4CanRead += len;
						result += len;
						if ((0 != minNum) && (num4CanRead >= minNum)) {
							break;
						}
					}

				}
				if (num4CanRead < minNum)
					throw new IOException(
							"the request was rejected because invalid multipart stream with no more data is available");
			}
		}
		return result;

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

	public static boolean arrayequals(byte[] a, byte[] b, int count) {
		for (int i = 0; i < count; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	public static UploadItemIteratorImpl build(String charEncoding,String contentType,InputStream input) throws IOException{
		UploadItemIteratorImpl it = new UploadItemIteratorImpl();
		it.charEncoding = charEncoding;
		it.contentType =contentType;
		it.input = input;
		byte[] boundary = getBoundary(it.contentType);
		if (boundary == null) {
			throw new IOException("the request was rejected because no multipart boundary was found");
		}
		if (boundary.length > 200)
			throw new IOException("Unsupported multipart boundary length in HTTP POST header Content-Type");
		it.orginBoundary = boundary;
		it.boundaryLength = boundary.length + 4;
		// it.minFillCount4FindBoundary = it.boundaryLength + 1;
		it.boundary = new byte[it.boundaryLength];
		it.boundary[0] = CR;
		it.boundary[1] = LF;
		it.boundary[2] = DASH;
		it.boundary[3] = DASH;
		System.arraycopy(boundary, 0, it.boundary, 4, boundary.length);

		return it;
	}

	public static UploadItemIteratorImpl build(HttpServletRequest request) throws IOException {
		UploadItemIteratorImpl it = new UploadItemIteratorImpl();
		it.charEncoding = request.getCharacterEncoding();
		it.contentType = request.getContentType();
		it.input = request.getInputStream();
		byte[] boundary = getBoundary(it.contentType);
		if (boundary == null) {
			throw new IOException("the request was rejected because no multipart boundary was found");
		}
		if (boundary.length > 200)
			throw new IOException("Unsupported multipart boundary length in HTTP POST header Content-Type");
		it.orginBoundary = boundary;
		it.boundaryLength = boundary.length + 4;
		// it.minFillCount4FindBoundary = it.boundaryLength + 1;
		it.boundary = new byte[it.boundaryLength];
		it.boundary[0] = CR;
		it.boundary[1] = LF;
		it.boundary[2] = DASH;
		it.boundary[3] = DASH;
		System.arraycopy(boundary, 0, it.boundary, 4, boundary.length);

		return it;
	}

	private void setBoundary(byte[] boundary) throws IOException {
		if (boundary.length != boundaryLength - BOUNDARY_PREFIX.length) {
			throw new IOException("The length of a boundary token can not be changed");
		}
		System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
	}

	protected int findByte(byte value, int pos) {
		for (int i = pos; i < tail; i++) {
			if (buffer[i] == value) {
				return i;
			}
		}
		return -1;
	}

	private int findBoundaryInCache() throws IOException {
		this.fillBuffer(this.boundaryLength);
		int first;
		int match = 0;
		int maxpos = tail - boundaryLength;
		for (first = head; first <= maxpos && match != boundaryLength; ++first) {
			first = findByte(boundary[0], first);
			if (first == -1 || first > maxpos) {
				return -1;
			}
			for (match = 1; match < boundaryLength; match++) {
				if (buffer[first + match] != boundary[match]) {
					break;
				}
			}
		}
		if (match == boundaryLength) {
			return first - 1;
		}
		return -1;
	}

	private int findHeaderSeparatorInCache() throws IOException {
		int first;
		int maxpos = tail - 4;
		fillBuffer(4);
		for (first = head; first <= maxpos; ++first) {
			first = findByte(CR, first);
			if (first == -1 || first > maxpos) {
				return -1;
			}
			if (this.buffer[first + 1] == LF && this.buffer[first + 2] == CR && this.buffer[first + 3] == LF)
				return first;
		}
		return -1;
	}

	private boolean nextChunk() throws IOException {
		boolean result = false;
		
		head += boundaryLength;
		// int num4CanRead = this.canReadNumber();
		//
		// if (num4CanRead < 2) {
		// this.fillBuffer(2 - num4CanRead);
		// }
		this.fillBuffer(2);
		marker[0] = this.buffer[this.head++];
		if (marker[0] == LF) {
			// Work around IE5 Mac bug with input type=image.
			// Because the boundary delimiter, not including the trailing
			// CRLF, must not appear within any file (RFC 2046, section
			// 5.1.1), we know the missing CR is due to a buggy browser
			// rather than a file containing something similar to a
			// boundary.
			return true;
		}
		marker[1] = this.buffer[this.head++];
		if (arrayequals(marker, STREAM_TERMINATOR, 2)) {
			if (this.currentFieldName == null) {
				// Outer multipart terminated -> No more data
				this.clean();
			}
			result = false;
		} else if (arrayequals(marker, FIELD_SEPARATOR, 2)) {
			result = true;
		} else {
			throw new IOException("the request was rejected because invalid multipart stream with boundary after");
		}
		return result;
	}

	private boolean hasNextInternal() throws IOException {
		while (this.pos4NextBoundary == -1) {
			fillBuffer(this.boundaryLength);
			this.pos4NextBoundary = this.findBoundaryInCache();
			if (this.pos4NextBoundary != -1) {
				this.head = this.pos4NextBoundary;
			} else {
				this.head = this.tail - this.boundaryLength + 1;
			}
		}
		boolean result = this.nextChunk();
		if ((!result) && (this.currentFieldName != null)) {
			// Inner multipart terminated -> Return to parsing the outer
			this.currentFieldName = null;
			this.pos4NextBoundary = -1;
			this.setBoundary(this.orginBoundary);
			result = this.hasNextInternal();
		}
		return result;
	}

	@Override
	synchronized public boolean hasNext() throws IOException {
		try {
			return this.hasNextInternal();
		} catch (IOException e) {
			this.clean();
			throw e;
		}
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

	private ItemHeadersImpl parseHeaders(String headerPart) {
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
				end = parseEndOfLine(headerPart, nonWs);
				header.append(" ").append(headerPart.substring(nonWs, end));
				start = end + 2;
			}
			parseHeaderLine(headers, header.toString());
		}
		return headers;
	}

	private ItemHeadersImpl readHeaders() throws IOException {
		this.pos4NextBoundary = -1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		int pos;
		int len;
		int maxpos;
		for (;;) {
			pos = this.findHeaderSeparatorInCache();
			if (pos == -1) {
				maxpos = this.tail - 3;
				len = maxpos - this.head;
				if (len > 0) {
					os.write(this.buffer, this.head, len);
					this.head = maxpos;
				}
			} else {
				len = pos - this.head;
				if (len > 0) {
					os.write(this.buffer, this.head, len);
				}
				this.head = pos + 4;
				break;
			}
		}
		os.write(HEADER_SEPARATOR);
		String headers = null;
		if (charEncoding != null) {
			try {
				headers = os.toString(charEncoding);
			} catch (UnsupportedEncodingException e) {
				headers = os.toString();
			}
		} else {
			headers = os.toString();
		}
		return this.parseHeaders(headers);
	}

	private String getFieldName(ItemHeadersImpl headers) {
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

	protected String getFileName(ItemHeadersImpl headers) {
		return getFileName(headers.getHeader(CONTENT_DISPOSITION));
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

	private Item buildFormItem(String name, ItemHeadersImpl headers) throws IOException {

		ByteArrayOutputStream os = new ByteArrayOutputStream();
	    int len ;
	    while((len= this.readBuf(this.shareCache, 0,this.shareCache.length))!=-1){
	    	if(len>0)os.write(this.shareCache,0,len);
	    }
		return new FormFieldItem(name, headers.getHeader(CONTENT_TYPE), os.toByteArray(), headers);
	}

	private Item buildFileItem(String name, String fileName, ItemHeadersImpl headers) {
		
		this.currentFileFieldItemStream = new ItemInputStream();
		return		new FileFieldItem(fileName, name, headers.getHeader(CONTENT_TYPE), this.currentFileFieldItemStream, headers);
	}

	private Item nextInternal() throws IOException {
		ItemHeadersImpl headers = this.readHeaders();
		if (this.currentFieldName == null) {
			// We're parsing the outer multipart
			String fieldName = getFieldName(headers);
			if (fieldName != null) {
				String subContentType = headers.getHeader(CONTENT_TYPE);
				if (subContentType != null && subContentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART_MIXED)) {
					this.currentFieldName = fieldName;
					// Multiple files associated with this field name
					byte[] subBoundary = getBoundary(subContentType);
					this.setBoundary(subBoundary);
					this.head -= 2;
					this.pos4NextBoundary = -1;
					if (!this.hasNextInternal())
						throw new IOException(
								"the request was rejected because invalid multipart stream with not found subBoundary");
					headers = this.readHeaders();
				}
				String fileName = getFileName(headers);
				if (fileName == null) {
					return buildFormItem(fieldName, headers);
				} else {
					return buildFileItem(fieldName, fileName, headers);
				}
			} else {
				throw new IOException(
						"the request was rejected because invalid multipart stream with not found name in headers");
			}
		} else {
			String fileName = getFileName(headers);
			if (fileName != null) {
				return buildFileItem(this.currentFieldName, fileName, headers);
			}
			throw new IOException(
					"the request was rejected because invalid multipart stream with not found filename in headers");
		}
	}

	@Override
	synchronized public Item next() throws IOException {
		try {
			return this.nextInternal();
		} catch (IOException e) {
			this.clean();
			throw e;
		}
	}

	@Override
	synchronized public void clean() {
		try {
			if (this.eof)
				return;
			this.eof = true;
			// clean data in stream ?
			this.input.close();
		} catch (Throwable e) {
		}
	}

	class ItemInputStream extends InputStream {
		private final byte[] singleByteArray = new byte[1];
		private boolean closed = false;
		private IOException err;

		private void check() throws IOException {
			if(this!= currentFileFieldItemStream) throw new IllegalStateException("stream was invalid");
			if (err != null)
				throw err;
			if (this.closed)
				throw new IOException("stream was already closed");
		}

		@Override
		public int read() throws IOException {
			check();
			try {
				if (readBuf(this.singleByteArray, 0, 1) == -1)
					return -1;
				return this.singleByteArray[0] & 0xff;
			} catch (IOException e) {
				this.err = e;
				throw e;
			}
		}

		@Override
		public int read(byte[] b) throws IOException {
			check();
			try {
				if (b == null) {
					throw new NullPointerException();
				} else if (b.length == 0) {
					return 0;
				}
				return readBuf(b, 0, b.length);
			} catch (IOException e) {
				this.err = e;
				throw e;
			}
		}

		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			check();
			try {
				if (b == null) {
					throw new NullPointerException();
				} else if (off < 0 || len < 0 || len > b.length - off) {
					throw new IndexOutOfBoundsException();
				} else if (len == 0) {
					return 0;
				}
				return readBuf(b, off, len);
			} catch (IOException e) {
				this.err = e;
				throw e;
			}
		}

		@Override
		public int available() throws IOException {
			if(pos4NextBoundary==-1){
				int l = tail - head - boundaryLength +1;
				return l<0?0:l;
			}else{
				return pos4NextBoundary - head;
			}
		}

		@Override
		public void close() throws IOException {
			this.check();
			try {
				byte[] bb = new byte[4096];
				while(readBuf(bb, 0, bb.length)!=-1){;}
				this.closed = true;
			} catch (IOException e) {			
				this.err= new  IOException("closing error", e);
				throw this.err;
			}
		}
	}
}
