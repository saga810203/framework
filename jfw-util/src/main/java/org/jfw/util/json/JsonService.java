package org.jfw.util.json;

import java.io.Reader;
import java.lang.reflect.Type;

import com.google.gson.Gson;

public class JsonService {

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
	static {
		// create gson by JsonConfig
		gson = new Gson();
	}
}
