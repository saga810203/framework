package org.jfw.util.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtil {
	public static void close(InputStream in) {
		try {
			in.close();
		} catch (IOException e) {
		}
	}

	public static void close(OutputStream out) {
		try {
			out.close();
		} catch (IOException e) {
		}
	}

	public static void copy(InputStream in, OutputStream os, byte[] buf) throws IOException {
		int len = 0;
		while ((len = in.read(buf)) >= 0) {
			if (len > 0)
				os.write(buf, 0, len);
		}
	}

	public static void copy(InputStream in, OutputStream out, boolean closeIn, boolean closeOut) throws IOException {
		try {
			int len = 0;
			byte[] buf = new byte[8192];
			while ((len = in.read(buf)) >= 0) {
				if (len > 0)
					out.write(buf, 0, len);
			}
		} finally {
			try {
				if (closeIn)
					in.close();
			} catch (Throwable th) {
			}
			try {
				if (closeOut)
					out.close();
			} catch (Throwable th) {
			}
		}
	}

	public static byte[] readStream(InputStream in, boolean close) throws IOException {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(in, out, new byte[8192]);
			return out.toByteArray();
		} finally {
			if (close)
				in.close();
		}
	}
}
