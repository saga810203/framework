package org.jfw.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class StringUtil {
	private StringUtil() {
	}

	public static String fromWithUTF8(InputStream in) throws IOException {
		char[] buf = new char[1024];
		Reader r = new BufferedReader(new InputStreamReader(in, ConstData.UTF8));
		int len = 0;
		StringBuilder sb = new StringBuilder();
		while ((len = r.read(buf)) != -1) {
			if (len > 0)
				sb.append(buf, 0, len);
		}
		return sb.toString();
	}

	public static String fromByUTF8AndClose(InputStream in) throws IOException {
		try {
			char[] buf = new char[1024];
			Reader r = new BufferedReader(new InputStreamReader(in, ConstData.UTF8));
			int len = 0;
			StringBuilder sb = new StringBuilder();
			while ((len = r.read(buf)) != -1) {
				if (len > 0)
					sb.append(buf, 0, len);
			}
			return sb.toString();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
	}

}
