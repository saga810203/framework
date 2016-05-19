package org.jfw.util.web.fileupload.impl;

import static java.lang.String.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;


public class MultipartStream {
	public static final byte CR = 0x0D;
	public static final byte LF = 0x0A;
	public static final byte DASH = 0x2D;
	public static final int HEADER_PART_SIZE_MAX = 10240;
	protected static final int DEFAULT_BUFSIZE = 4096;
	protected static final byte[] HEADER_SEPARATOR = { CR, LF, CR, LF };
	protected static final byte[] FIELD_SEPARATOR = { CR, LF };
	protected static final byte[] STREAM_TERMINATOR = { DASH, DASH };
	protected static final byte[] BOUNDARY_PREFIX = { CR, LF, DASH, DASH };
	private final InputStream input;
	private int boundaryLength;
	private int keepRegion;
	private byte[] boundary;
	private final int bufSize;
	private final byte[] buffer;
	private int head;
	private int tail;
	private String headerEncoding;

	public MultipartStream(InputStream input, byte[] boundary, int bufSize) {

		this.input = input;
		this.bufSize = bufSize;
		this.buffer = new byte[bufSize];
		this.boundaryLength = boundary.length + BOUNDARY_PREFIX.length;
		if (bufSize < this.boundaryLength + 1) {
			throw new IllegalArgumentException("The buffer size specified for the MultipartStream is too small");
		}
		this.boundary = new byte[this.boundaryLength];
		this.keepRegion = this.boundary.length;
		System.arraycopy(BOUNDARY_PREFIX, 0, this.boundary, 0, BOUNDARY_PREFIX.length);
		System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
		head = 0;
		tail = 0;
	}

	MultipartStream(InputStream input, byte[] boundary) {
		this(input, boundary, DEFAULT_BUFSIZE);
	}

	public String getHeaderEncoding() {
		return headerEncoding;
	}

	public void setHeaderEncoding(String encoding) {
		headerEncoding = encoding;
	}

	public byte readByte() throws IOException {
		if (head == tail) {
			head = 0;
			// Refill.
			tail = input.read(buffer, head, bufSize);
			if (tail == -1) {
				throw new IOException("No more data is available");
			}
		}
		return buffer[head++];
	}

	public boolean readBoundary() throws IOException {
		byte[] marker = new byte[2];
		boolean nextChunk = false;

		head += boundaryLength;

		marker[0] = readByte();
		if (marker[0] == LF) {
			return true;
		}

		marker[1] = readByte();
		if (arrayequals(marker, STREAM_TERMINATOR, 2)) {
			nextChunk = false;
		} else if (arrayequals(marker, FIELD_SEPARATOR, 2)) {
			nextChunk = true;
		} else {
			throw new IOException("Unexpected characters follow a boundary");
		}

		return nextChunk;
	}

	public void setBoundary(byte[] boundary) throws IOException {
		if (boundary.length != boundaryLength - BOUNDARY_PREFIX.length) {
			throw new IOException("The length of a boundary token can not be changed");
		}
		System.arraycopy(boundary, 0, this.boundary, BOUNDARY_PREFIX.length, boundary.length);
	}

	public String readHeaders() throws IOException {
		int i = 0;
		byte b;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int size = 0;
		while (i < HEADER_SEPARATOR.length) {

			b = readByte();

			if (++size > HEADER_PART_SIZE_MAX) {
				throw new IOException(
						format("Header section has more than %s bytes (maybe it is not properly terminated)",
								Integer.valueOf(HEADER_PART_SIZE_MAX)));
			}
			if (b == HEADER_SEPARATOR[i]) {
				i++;
			} else {
				i = 0;
			}
			baos.write(b);
		}

		String headers = null;
		if (headerEncoding != null) {
			try {
				headers = baos.toString(headerEncoding);
			} catch (UnsupportedEncodingException e) {
				headers = baos.toString();
			}
		} else {
			headers = baos.toString();
		}

		return headers;
	}

	public long readBodyData(OutputStream out) throws IOException {
		InputStream in = newInputStream();
		try {
			long total = 0;
			for (;;) {
				int res = in.read(buffer);
				if (res == -1) {
					break;
				}
				if (res > 0) {
					total += res;
					if (out != null) {
						out.write(buffer, 0, res);
					}
				}
			}
			if (out != null){
				out.flush();
			}
			in.close();
			in = null;
			return total;
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (Throwable e) {
				}
		}
	}

	ItemInputStream newInputStream() {
		return new ItemInputStream();
	}

	public long discardBodyData() throws IOException {
		return readBodyData(null);
	}
	public boolean skipPreamble() throws IOException {
		System.arraycopy(boundary, 2, boundary, 0, boundary.length - 2);
		boundaryLength = boundary.length - 2;
		try {
			discardBodyData();
			return readBoundary();
		} catch (IOException e) {
			return false;
		} finally {
			System.arraycopy(boundary, 0, boundary, 2, boundary.length - 2);
			boundaryLength = boundary.length;
			boundary[0] = CR;
			boundary[1] = LF;
		}
	}
	public static boolean arrayequals(byte[] a, byte[] b, int count) {
		for (int i = 0; i < count; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	protected int findByte(byte value, int pos) {
		for (int i = pos; i < tail; i++) {
			if (buffer[i] == value) {
				return i;
			}
		}
		return -1;
	}
	protected int findSeparator() {
		int first;
		int match = 0;
		int maxpos = tail - boundaryLength;
		for (first = head; first <= maxpos && match != boundaryLength; first++) {
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
	public class ItemInputStream extends InputStream{
		private long total;

		private int pad;

		private int pos;

		private boolean closed;
		ItemInputStream() {
			findSeparator();
		}

		private void findSeparator() {
			pos = MultipartStream.this.findSeparator();
			if (pos == -1) {
				if (tail - head > keepRegion) {
					pad = keepRegion;
				} else {
					pad = tail - head;
				}
			}
		}

		public long getBytesRead() {
			return total;
		}

		@Override
		public int available() throws IOException {
			if (pos == -1) {
				return tail - head - pad;
			}
			return pos - head;
		}

		private static final int BYTE_POSITIVE_OFFSET = 256;

		@Override
		public int read() throws IOException {
			if (closed) {
				throw new IOException();
			}
			if (available() == 0 && makeAvailable() == 0) {
				return -1;
			}
			++total;
			int b = buffer[head++];
			if (b >= 0) {
				return b;
			}
			return b + BYTE_POSITIVE_OFFSET;
		}

		/**
		 * Reads bytes into the given buffer.
		 *
		 * @param b
		 *            The destination buffer, where to write to.
		 * @param off
		 *            Offset of the first byte in the buffer.
		 * @param len
		 *            Maximum number of bytes to read.
		 * @return Number of bytes, which have been actually read, or -1 for
		 *         EOF.
		 * @throws IOException
		 *             An I/O error occurred.
		 */
		@Override
		public int read(byte[] b, int off, int len) throws IOException {
			if (closed) {
				throw new IOException("InputStream is already closed");
			}
			if (len == 0) {
				return 0;
			}
			int res = available();
			if (res == 0) {
				res = makeAvailable();
				if (res == 0) {
					return -1;
				}
			}
			res = Math.min(res, len);
			System.arraycopy(buffer, head, b, off, res);
			head += res;
			total += res;
			return res;
		}

		public void close() throws IOException {
			close(false);
		}
		public void close(boolean pCloseUnderlying) throws IOException {
			if (closed) {
				return;
			}
			if (pCloseUnderlying) {
				closed = true;
				input.close();
			} else {
				for (;;) {
					int av = available();
					if (av == 0) {
						av = makeAvailable();
						if (av == 0) {
							break;
						}
					}
					skip(av);
				}
			}
			closed = true;
		}
		public long skip(long bytes) throws IOException {
			if (closed) {
				throw new IOException("InputStream is already closed");
			}
			int av = available();
			if (av == 0) {
				av = makeAvailable();
				if (av == 0) {
					return 0;
				}
			}
			long res = Math.min(av, bytes);
			head += res;
			return res;
		}
		private int makeAvailable() throws IOException {
			if (pos != -1) {
				return 0;
			}
			total += tail - head - pad;
			System.arraycopy(buffer, tail - pad, buffer, 0, pad);
			head = 0;
			tail = pad;

			for (;;) {
				int bytesRead = input.read(buffer, tail, bufSize - tail);
				if (bytesRead == -1) {
					final String msg = "Stream ended unexpectedly";
					throw new IOException(msg);
				}
				tail += bytesRead;

				findSeparator();
				int av = available();

				if (av > 0 || pos != -1) {
					return av;
				}
			}
		}

		public boolean isClosed() {
			return closed;
		}

	}

}
