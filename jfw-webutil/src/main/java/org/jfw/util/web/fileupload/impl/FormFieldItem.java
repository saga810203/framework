package org.jfw.util.web.fileupload.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.jfw.util.web.fileupload.Item;
import org.jfw.util.web.fileupload.ItemHeaders;


public class FormFieldItem implements Item{
	private static final UnsupportedOperationException UN_SUPPORTED_EX = new UnsupportedOperationException();
	
    public static final String DEFAULT_CHARSET = "UTF-8";
    private final String fieldName;
    private final String contentType;
    private final byte[] cachedContent;
    private final ItemHeaders headers;
    
    public FormFieldItem(String fieldName,String contentType,byte[] cachedContent,ItemHeaders headers){
    	this.fieldName = fieldName;
    	this.contentType = contentType;
    	this.cachedContent = cachedContent;
    	this.headers = headers;
    }
    
    
    
    public InputStream getInputStream()
        throws IOException {
       throw UN_SUPPORTED_EX;
    }
    public String getContentType() {
        return contentType;
    }
    public String getName() {
       throw UN_SUPPORTED_EX;
    }

    public long getSize() {
       return null== this.cachedContent ?-1:this.cachedContent.length;
    }

    public byte[] get() {
       return this.cachedContent;
    }

    public String getString(final String charset)
        throws UnsupportedEncodingException {
        return new String(get(), charset);
    }
    public String getCharSet() {
        ParameterParser parser = new ParameterParser();
        parser.setLowerCaseNames(true);
        // Parameter parser can handle null input
        Map<String, String> params = parser.parse(getContentType(), ';');
        return params.get("charset");
    }
    public String getString() {
        String charset = getCharSet();
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        try {
            return new String(this.cachedContent, charset);
        } catch (UnsupportedEncodingException e) {
            return new String(this.cachedContent);
        }
    }

    public void write(OutputStream file) throws Exception {
       file.write(this.cachedContent);
    }

  
    public String getFieldName() {
        return fieldName;
    }

    public boolean isFormField() {
        return true;
    }

    public ItemHeaders getHeaders() {
        return headers;
    }

}
