package org.jfw.util.io;

import java.io.IOException;
import java.io.InputStream;

import org.omg.CORBA_2_3.portable.OutputStream;

public abstract class StreamUtil {
    public static void copy(InputStream in,OutputStream os,byte[] buf)throws IOException{
        int len = 0;
        while((len=in.read(buf))>=0){
            os.write(buf, 0, len);
        }        
    }

}
