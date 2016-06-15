package org.jfw.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletException;

import org.jfw.util.ConstData;
import org.jfw.util.bean.AfterBeanFactory;
import org.jfw.util.bean.BeanFactory;
import org.jfw.util.io.MultiInputStreamHandler;
import org.jfw.util.io.ResourceUtil;
import org.jfw.util.web.WebHandlerContext;
import org.jfw.util.web.model.WebRequestEntry;

public class FilenameConfigServlet extends BaseServlet {

	private static final long serialVersionUID = 4188897681050379687L;
	public static final String JFW_MVC_GROUPNAME = "jfwmvc";
	public static final String BEAN_FAC_FILE_NAME = "configFileName";

	public static final String AFTER_BEANFACTORY = "afterBeanFactory";

	protected String configFileName = "beanConfig.properties";

	protected BeanFactory bf = null;

	protected Map<String, String> config;

	private String[] split(String val) {
		ArrayList<String> list = new ArrayList<String>();

		String str = val.trim();
		for (;;) {
			boolean find = false;
			int index = 0;
			for (; index < str.length(); ++index) {
				char c = str.charAt(index);
				if (c == ';' || c == ',') {
					find = true;
					break;
				}
			}
			if (!find) {
				if (str.length() > 0)
					list.add(str);
				break;
			}
			if (index == 0) {
				str = str.substring(1).trim();
			} else if (index == (str.length() - 1)) {
				list.add(str.substring(0, index).trim());
				break;
			} else {
				list.add(str.substring(0, index).trim());
				str = str.substring(index + 1).trim();
			}
		}
		return list.toArray(new String[list.size()]);
	}

	protected void handleConfigResource(String name) throws Exception {
		Map<String, String> po = ResourceUtil.<Map<String, String>> readClassResource(name,
				new MultiInputStreamHandler<Map<String, String>>() {
					Map<String, String> pMap = new HashMap<String, String>();

					private void load(LineReader lr) throws IOException {
						char[] convtBuf = new char[1024];
						int limit;
						int keyLen;
						int valueStart;
						char c;
						boolean hasSep;
						boolean precedingBackslash;

						while ((limit = lr.readLine()) >= 0) {
							c = 0;
							keyLen = 0;
							valueStart = limit;
							hasSep = false;
							precedingBackslash = false;
							while (keyLen < limit) {
								c = lr.lineBuf[keyLen];
								if ((c == '=') && !precedingBackslash) {
									valueStart = keyLen + 1;
									hasSep = true;
									break;
								} else if ((c == ' ' || c == '\t' || c == '\f') && !precedingBackslash) {
									valueStart = keyLen + 1;
									break;
								}
								if (c == '\\') {
									precedingBackslash = !precedingBackslash;
								} else {
									precedingBackslash = false;
								}
								keyLen++;
							}
							while (valueStart < limit) {
								c = lr.lineBuf[valueStart];
								if (c != ' ' && c != '\t' && c != '\f') {
									if (!hasSep && (c == '=' || c == ':')) {
										hasSep = true;
									} else {
										break;
									}
								}
								valueStart++;
							}
							String key = loadConvert(lr.lineBuf, 0, keyLen, convtBuf);
							String value = loadConvert(lr.lineBuf, valueStart, limit - valueStart, convtBuf);
							this.pMap.put(key, value);
						}
					}

					private String loadConvert(char[] in, int off, int len, char[] convtBuf) {
						if (convtBuf.length < len) {
							int newLen = len * 2;
							if (newLen < 0) {
								newLen = Integer.MAX_VALUE;
							}
							convtBuf = new char[newLen];
						}
						char aChar;
						char[] out = convtBuf;
						int outLen = 0;
						int end = off + len;

						while (off < end) {
							aChar = in[off++];
							if (aChar == '\\') {
								aChar = in[off++];
								if (aChar == 'u') {
									// Read the xxxx
									int value = 0;
									for (int i = 0; i < 4; i++) {
										aChar = in[off++];
										switch (aChar) {
										case '0':
										case '1':
										case '2':
										case '3':
										case '4':
										case '5':
										case '6':
										case '7':
										case '8':
										case '9':
											value = (value << 4) + aChar - '0';
											break;
										case 'a':
										case 'b':
										case 'c':
										case 'd':
										case 'e':
										case 'f':
											value = (value << 4) + 10 + aChar - 'a';
											break;
										case 'A':
										case 'B':
										case 'C':
										case 'D':
										case 'E':
										case 'F':
											value = (value << 4) + 10 + aChar - 'A';
											break;
										default:
											throw new IllegalArgumentException("Malformed \\uxxxx encoding.");
										}
									}
									out[outLen++] = (char) value;
								} else {
									if (aChar == 't')
										aChar = '\t';
									else if (aChar == 'r')
										aChar = '\r';
									else if (aChar == 'n')
										aChar = '\n';
									else if (aChar == 'f')
										aChar = '\f';
									out[outLen++] = aChar;
								}
							} else {
								out[outLen++] = aChar;
							}
						}
						return new String(out, 0, outLen);
					}

					@Override
					public void handle(InputStream in) throws Exception {
						this.load(new LineReader(new InputStreamReader(in, ConstData.UTF8)));
					}

					@Override
					public Map<String, String> get() {
						return this.pMap;
					}
				}, Thread.currentThread().getContextClassLoader());
		config.putAll(po);

	}

	protected void buildBeanFactoryConfig() {
		String tmp = this.getServletConfig().getInitParameter(BEAN_FAC_FILE_NAME);
		if (tmp != null && tmp.trim().length() > 0)
			this.configFileName = tmp.trim();

		String[] filenames = this.split(this.configFileName);
		if (filenames.length == 0)
			failStart("no found config file");
		config = new HashMap<String,String>();
		for (int i = 0; i < filenames.length; ++i) {
			try {
				this.handleConfigResource(filenames[i]);
			} catch (Throwable th) {
				this.config = null;
				this.log(FilenameConfigServlet.class.getName() + ":load config file[" + filenames[i] + "] error", th);
				failStart(th);

			}
		}

	}

	protected void buildBeanFactory() {
		try {
			if (config != null && config.size() > 0)
				this.bf = BeanFactory.build(null, config);
		} catch (Throwable th) {
			this.bf = null;
			this.log(FilenameConfigServlet.class.getName() + ":build beanFactory error", th);
			failStart(th);
		}
	}

	protected void buildWebHandlers() {
		if (null == this.bf)
			return;
		List<String> list = this.bf.getBeanIdsWithGroup(JFW_MVC_GROUPNAME);
		if (list == null || list.isEmpty())
			return;
		for (String name : list) {
			Object obj = this.bf.getBean(name);
			if (obj instanceof WebRequestEntry) {
				if (!WebHandlerContext.addWebHandler((WebRequestEntry) obj)) {
					this.log(FilenameConfigServlet.class.getName() + ":bean[id=" + name + "] can't load as webHandler");
				}
			} else {
				this.log(FilenameConfigServlet.class.getName() + ":bean[id=" + name
						+ "] invalid org.jfw.web.model.WebRequestEntry");
			}
		}
	}

	public void doAfterBeanFactory() {
		if (null == this.bf)
			return;
		String tmp = this.getServletConfig().getInitParameter(AFTER_BEANFACTORY);
		if (tmp == null || tmp.trim().length() == 0)
			return;
		tmp = tmp.trim();

		String[] cns = tmp.split("[,;]");
		LinkedList<AfterBeanFactory> list = new LinkedList<AfterBeanFactory>();
		try {
			for (int i = 0; i < cns.length; ++i) {
				if (cns[i] != null && cns[i].trim().length() > 0) {
					Class<?> cls = Class.forName(cns[i].trim());
					list.add((AfterBeanFactory) cls.newInstance());
				}
			}
		} catch (Throwable th) {
			this.log("create " + AfterBeanFactory.class.getName() + " instance error", th);
			failStart(th);
			return;
		}
		try {
			for (ListIterator<AfterBeanFactory> it = list.listIterator(); it.hasNext();) {
				it.next().handle(this.bf);
			}
		} catch (Throwable th) {
			this.log("invoke " + AfterBeanFactory.class.getName() + ".handle instance error", th);
			failStart(th);
			return;
		}

	}

	@Override
	public void init() throws ServletException {
		super.init();
		this.buildBeanFactoryConfig();
		this.buildBeanFactory();
		this.doAfterBeanFactory();
		this.buildWebHandlers();
	}

	class LineReader {
		public LineReader(Reader reader) {
			this.reader = reader;
			inCharBuf = new char[8192];
		}

		byte[] inByteBuf;
		char[] inCharBuf;
		char[] lineBuf = new char[1024];
		int inLimit = 0;
		int inOff = 0;
		InputStream inStream;
		Reader reader;

		int readLine() throws IOException {
			int len = 0;
			char c = 0;

			boolean skipWhiteSpace = true;
			boolean isCommentLine = false;
			boolean isNewLine = true;
			boolean appendedLineBegin = false;
			boolean precedingBackslash = false;
			boolean skipLF = false;

			while (true) {
				if (inOff >= inLimit) {
					inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
					inOff = 0;
					if (inLimit <= 0) {
						if (len == 0 || isCommentLine) {
							return -1;
						}
						return len;
					}
				}
				if (inStream != null) {
					c = (char) (0xff & inByteBuf[inOff++]);
				} else {
					c = inCharBuf[inOff++];
				}
				if (skipLF) {
					skipLF = false;
					if (c == '\n') {
						continue;
					}
				}
				if (skipWhiteSpace) {
					if (c == ' ' || c == '\t' || c == '\f') {
						continue;
					}
					if (!appendedLineBegin && (c == '\r' || c == '\n')) {
						continue;
					}
					skipWhiteSpace = false;
					appendedLineBegin = false;
				}
				if (isNewLine) {
					isNewLine = false;
					if (c == '#' || c == '!') {
						isCommentLine = true;
						continue;
					}
				}

				if (c != '\n' && c != '\r') {
					lineBuf[len++] = c;
					if (len == lineBuf.length) {
						int newLength = lineBuf.length * 2;
						if (newLength < 0) {
							newLength = Integer.MAX_VALUE;
						}
						char[] buf = new char[newLength];
						System.arraycopy(lineBuf, 0, buf, 0, lineBuf.length);
						lineBuf = buf;
					}
					if (c == '\\') {
						precedingBackslash = !precedingBackslash;
					} else {
						precedingBackslash = false;
					}
				} else {
					// reached EOL
					if (isCommentLine || len == 0) {
						isCommentLine = false;
						isNewLine = true;
						skipWhiteSpace = true;
						len = 0;
						continue;
					}
					if (inOff >= inLimit) {
						inLimit = (inStream == null) ? reader.read(inCharBuf) : inStream.read(inByteBuf);
						inOff = 0;
						if (inLimit <= 0) {
							return len;
						}
					}
					if (precedingBackslash) {
						len -= 1;
						// skip the leading whitespace characters in following
						// line
						skipWhiteSpace = true;
						appendedLineBegin = true;
						precedingBackslash = false;
						if (c == '\r') {
							skipLF = true;
						}
					} else {
						return len;
					}
				}
			}
		}
	}
}
