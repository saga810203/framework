package org.jfw.util.json;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;

import com.google.gson.Gson;

public class JsonService {
	private static boolean htmlSafe = false;

	private static Gson gson;

	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}

	public static void toJson(Object obj, Appendable writer) {
		gson.toJson(obj, writer);
	}

	public static <T> T fromJson(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	public static <T> T fromJson(String json, Type typeOfT) {
		return gson.fromJson(json, typeOfT);
	}

	public static <T> T fromJson(Reader json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}

	public static <T> T fromJson(Reader json, Type typeOfT) {
		return gson.fromJson(json, typeOfT);
	}

	public static void write(String val, Writer out) throws IOException {
		if (val != null) {
			out.write("\"");
			String[] replacements = htmlSafe ? HTML_SAFE_REPLACEMENT_CHARS : REPLACEMENT_CHARS;
			int last = 0;
			int length = val.length();
			for (int i = 0; i < length; i++) {
				char c = val.charAt(i);
				String replacement;
				if (c < 128) {
					replacement = replacements[c];
					if (replacement == null) {
						continue;
					}
				} else if (c == '\u2028') {
					replacement = "\\u2028";
				} else if (c == '\u2029') {
					replacement = "\\u2029";
				} else {
					continue;
				}
				if (last < i) {
					out.write(val, last, i - last);
				}
				out.write(replacement);
				last = i + 1;
			}
			if (last < length) {
				out.write(val, last, length - last);
			}
			out.write("\"");
		} else {
			out.write("null");
		}
	}
	
	
	public static void write(Exception e,Writer out) throws IOException{
		out.write("{\"success\":false,\"code\":");
		if(e instanceof org.jfw.util.exception.JfwBaseException)
		{out.write(((org.jfw.util.exception.JfwBaseException)e).getCode());}else{out.write(0);}
		out.write(",\"msg\":");
		write(e.getMessage(),out);
		out.write(",\"detailMsg\":");
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		write(sw.toString(),out);
		out.write("}");
	}

	private static final String[] REPLACEMENT_CHARS;
	private static final String[] HTML_SAFE_REPLACEMENT_CHARS;

	static {
		// create gson by JsonConfig
		gson = new Gson();

		REPLACEMENT_CHARS = new String[128];
		for (int i = 0; i <= 0x1f; i++) {
			REPLACEMENT_CHARS[i] = String.format("\\u%04x", (int) i);
		}
		REPLACEMENT_CHARS['"'] = "\\\"";
		REPLACEMENT_CHARS['\\'] = "\\\\";
		REPLACEMENT_CHARS['\t'] = "\\t";
		REPLACEMENT_CHARS['\b'] = "\\b";
		REPLACEMENT_CHARS['\n'] = "\\n";
		REPLACEMENT_CHARS['\r'] = "\\r";
		REPLACEMENT_CHARS['\f'] = "\\f";
		HTML_SAFE_REPLACEMENT_CHARS = REPLACEMENT_CHARS.clone();
		HTML_SAFE_REPLACEMENT_CHARS['<'] = "\\u003c";
		HTML_SAFE_REPLACEMENT_CHARS['>'] = "\\u003e";
		HTML_SAFE_REPLACEMENT_CHARS['&'] = "\\u0026";
		HTML_SAFE_REPLACEMENT_CHARS['='] = "\\u003d";
		HTML_SAFE_REPLACEMENT_CHARS['\''] = "\\u0027";
	}
}
