package org.jfw.util.io;

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
    public static void copy(InputStream in,OutputStream os,byte[] buf)throws IOException{
        int len = 0;
        while((len=in.read(buf))>=0){
            os.write(buf, 0, len);
        }        
    }
}
