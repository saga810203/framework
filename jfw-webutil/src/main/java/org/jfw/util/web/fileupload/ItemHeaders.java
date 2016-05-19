package org.jfw.util.web.fileupload;

import java.util.Iterator;

public interface ItemHeaders {
    String getHeader(String name);
    Iterator<String> getHeaders(String name);
    Iterator<String> getHeaderNames();
}
