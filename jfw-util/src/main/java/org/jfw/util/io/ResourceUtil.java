package org.jfw.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;


public abstract class ResourceUtil {
    public static <T> T readClassResource(String name,MultiInputStreamHandler<T> handler,ClassLoader cl) throws Exception{
        if(cl ==null)cl = ResourceUtil.class.getClassLoader();
        Enumeration<URL> en = cl.getResources(name);
        
        while(en.hasMoreElements()){
            URL url = en.nextElement();
            URLConnection con = url.openConnection();
            InputStream in =con.getInputStream();
            try
            {
              handler.handle(in);
            }finally{
                try{
                in.close();}catch(IOException e){}
            }
        }
        return handler.get();        
    }
    

}
