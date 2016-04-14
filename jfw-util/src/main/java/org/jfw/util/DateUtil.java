package org.jfw.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public abstract class DateUtil {
    public static  final int[] MONTHS_IN_NOT_LEAPYEAR={31,28,31,30,31,30,31,31,30,31,30,31};
    public static  final int[] MONTHS_IN_LEAPYEAR={31,29,31,30,31,30,31,31,30,31,30,31};
    
    public static final String TIMESTAMP_FORMAT="yyyyMMddHHmmssSSS";
    public static final String DATETIME_FORMAT="yyyyMMddHHmmss";
    public static final String DATE_FORMAT="yyyyMMdd";
    
    public static boolean isLeapYear(int year)
    {
         return ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0));
    }
    
    /*
     * 取得一个月的最后一天
     * 
     * @param  month  value range is [0-11];
     */
    public static int  getLastDayOfMonth(int month,int year)
    {
        int[] mm = isLeapYear(year)?MONTHS_IN_LEAPYEAR:MONTHS_IN_NOT_LEAPYEAR;      
        return mm[month];       
    }
    /*
     * 
     * @param month value range is [0-11]
     */
    public static Calendar getDateByYYYYMMDD(int year,int month,int day)
    {
        Calendar result = Calendar.getInstance();
        result.set(year, month, day, 0, 0,0);
        return result;
    }
    
    
    public static String formatTimeStamp(Date date)
    {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return sdf.format(date);
    }
    public static String formatDateTime(Date date)
    {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
        return sdf.format(date);
    }
    public static String formatDate(Date date)
    {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }
    public static String formatTimeStamp(long date)
    {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(TIMESTAMP_FORMAT);
        return sdf.format(new Date(date));
    }
    public static String formatDateTime(long date)
    {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FORMAT);
        return sdf.format(new Date(date));
    }
    public static String formatDate(long date)
    {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(new Date(date));
    }

}
