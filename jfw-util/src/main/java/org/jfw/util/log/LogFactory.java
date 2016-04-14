package org.jfw.util.log;

public class LogFactory
{
    private static String classNameForLogFactory;
	private static volatile LogFactory defaule  = new LogFactory();
	private static Logger defaultLog = new NoLogger();
	
	
	public Logger getLogger(Class<?> clazz)
	{
		return LogFactory.defaultLog;
	}
	
	public static Logger getLog(Class<?> clazz)
	{
		return LogFactory.defaule.getLogger(clazz);
	}
	public static void init(){
	    try {
            LogFactory lf = (LogFactory)Class.forName(classNameForLogFactory).newInstance();
            if(lf!=null) defaule = lf;
        } catch (Throwable th) {
        } 
	}
}
