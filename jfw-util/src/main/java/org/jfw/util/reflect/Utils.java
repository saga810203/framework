package org.jfw.util.reflect;

import java.util.Locale;

public class Utils {
    public static String camelCase(String str){
        String fn = str.trim(); 
        StringBuilder sb = new StringBuilder();
        sb.append(fn.substring(0,1).toUpperCase(Locale.ENGLISH));
        if(fn.length()>1)
        {
            sb.append(fn.substring(1));
        }
        return sb.toString();
    }
}
