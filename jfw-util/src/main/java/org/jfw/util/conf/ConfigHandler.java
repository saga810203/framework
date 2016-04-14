package org.jfw.util.conf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.jfw.util.io.MultiInputStreamHandler;
import org.jfw.util.io.ResourceUtil;
import org.jfw.util.reflect.Utils;



public abstract class ConfigHandler {
    private static final String STATIC_CONFIG_FILE="jfw_config.properties";
    
    private static Properties load() throws Exception
    {
        return ResourceUtil.readClassResource(STATIC_CONFIG_FILE, new MultiInputStreamHandler<Properties>() {
            private Properties props = new Properties();
            public void handle(InputStream in) throws IOException {
                Properties tmp = new Properties();
                tmp.load(in);
                props.putAll(tmp);                
            }
            public Properties get() {
                return props;
            }
        }, null);
    }
    
   public static void config() throws Exception{
       Properties props = load();
       List<String> list = new LinkedList<String>();
       for(Object obj:props.keySet()){
           list.add((String)obj);
       }
       Collections.sort(list,new Comparator<String>(){

        public int compare(String o1, String o2) {
            if(o1.startsWith("$")){
                if(o2.startsWith("$")){
                    return o1.compareTo(o2);
                }else{
                    return -1;
                }
            }else{
                if(o2.startsWith("$")){
                    return 1;
                }else{
                    return o1.compareTo(o2);
                }
            }
        }});
   
       for(ListIterator<String> it = list.listIterator(); it.hasNext();){
           String key = it.next();
           doConfig(key,props.getProperty(key));
       }
   
   }
   private static void executeConfig(Method method,String val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
       Class<?> clazz = method.getParameterTypes()[0];
       if(clazz == String.class){
           method.invoke(null, new Object[]{val});
       } else if(clazz == int.class){
           method.invoke(null, new Object[]{Integer.parseInt(val)});
       }  else if(clazz == Integer.class){
           method.invoke(null, new Object[]{Integer.valueOf(val)});
       }  else if(clazz == byte.class){
           method.invoke(null, new Object[]{Byte.parseByte(val)});
       }  else if(clazz == Byte.class){
           method.invoke(null, new Object[]{Byte.valueOf(val)});
       }  else if(clazz == short.class){
           method.invoke(null, new Object[]{Short.parseShort(val)});
       }  else if(clazz == Short.class){
           method.invoke(null, new Object[]{Short.valueOf(val)});
       }  else if(clazz == long.class){
           method.invoke(null, new Object[]{Long.parseLong(val)});
       }  else if(clazz == Long.class){
           method.invoke(null, new Object[]{Long.valueOf(val)});
       }  else if(clazz == float.class){
           method.invoke(null, new Object[]{Float.parseFloat(val)});
       }  else if(clazz == Float.class){
           method.invoke(null, new Object[]{Float.valueOf(val)});
       }  else if(clazz == double.class){
           method.invoke(null, new Object[]{Double.parseDouble(val)});
       } else if(clazz == Double.class){
           method.invoke(null, new Object[]{Double.valueOf(val)});
       } else if(clazz == BigInteger.class){
           method.invoke(null, new Object[]{new BigInteger(val)});
       } else if(clazz == BigDecimal.class){
           method.invoke(null, new Object[]{new BigDecimal(val)});
       } else if(clazz == boolean.class){
           method.invoke(null, new Object[]{"1".equals(val)||"true".equalsIgnoreCase(val)||"yes".equalsIgnoreCase(val)});
       } else if(clazz == Boolean.class){
           method.invoke(null, new Object[]{"1".equals(val)||"true".equalsIgnoreCase(val)||"yes".equalsIgnoreCase(val)});
       } else{
           throw new IllegalArgumentException("无法处理的参类型");
       }
       
       
   }
   private static void doConfig(String key,String val) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
       String classname = key.startsWith("$")?key.substring(key.indexOf(".")+1):key;
       classname = classname.substring(0,classname.lastIndexOf("."));
       String methodName = classname.substring(classname.lastIndexOf(".")+1);
       Method method = null;
       Class<?> clazz = Class.forName(classname);
       for(Method m : clazz.getMethods()){
           if(m.getName().equals(methodName)&&(m.getParameterTypes().length==1) 
                   &&(Modifier.isStatic(m.getModifiers()))){
               method = m;
               break;
           }
       }
       if(null== method){
           methodName ="set"+Utils.camelCase(methodName);
           for(Method m : clazz.getMethods()){
               if(m.getName().equals(methodName)&&(m.getParameterTypes().length==1) &&(Modifier.isStatic(m.getModifiers())) ){
                   method = m;
                   break;
               }
           }
       }
       if(method==null) throw new IllegalArgumentException("无法处理参数："+key);
       executeConfig(method,val);      
   }
    

}
