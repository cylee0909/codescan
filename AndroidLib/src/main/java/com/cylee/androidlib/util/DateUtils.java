package com.cylee.androidlib.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sn on 13-12-19.
 */
public class DateUtils {
    private static final SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static boolean isSameDay(long oldTime, long newTime) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String newDay = format.format(newTime);
        String oldDay = format.format(oldTime);
        return newDay.equals(oldDay);
    }

    /**
     * 判断当前时间是否在 起始到终止时间段内，以小时为单位
     *
     * @param starthour 起始小时数(1-24)
     * @param endhour   终止小时数(1-24)
     */
    public static boolean isTimeInterval(int starthour, int endhour) {
        if (starthour < 1 || starthour > 24) starthour = 24;
        if (endhour < 1 || endhour > 24) endhour = 24;
        if (starthour == endhour) return true;
        Calendar now = Calendar.getInstance();
        now.setTimeInMillis(System.currentTimeMillis());
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour >= starthour && hour < endhour) {
            return true;
        }
        return false;
    }

    /**
     * 判断扭蛋机活动是否进行中 判断当前时间是否在19~22之间
     *
     * @return
     */
    public static boolean isAskNiuDanOn() {
//        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"));
//        now.setTimeInMillis(DateUtils.getApproximateServerTime().getTime());
//        int hour = now.get(Calendar.HOUR_OF_DAY);
//        return hour>=19 && hour<22;

        //扭蛋活动全天开放 2015.2.26下午新扭蛋需求;
        return true;
    }

    /**
     * 判断时间是否在两个指定日期中间，注意月份从0开始计算，开始日期设置为00：00，结束日期时间设置为23：59
     */
    public static boolean isTimeBetween(int year1, int mon1, int day1, int year2, int mon2, int day2) {
        long time = System.currentTimeMillis();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(year1, mon1, day1, 0, 0);
        long startTime = startCalendar.getTimeInMillis();
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(year2, mon2, day2, 23, 59);
        long endTime = endCalendar.getTimeInMillis();
        return time >= startTime && time <= endTime;

    }

    /**
     * 判断时间是否在两个指定日期中间，注意月份从0开始计算，开始日期，结束日期精确到时分秒
     * 当前时间按服务器矫正时间来判断
     */
    public static boolean isTimeBetween(int year1, int mon1, int day1, int hour1, int min1, int sec1,
                                        int year2, int mon2, int day2, int hour2, int min2, int sec2) {
        long time = System.currentTimeMillis();//debug时使用
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.set(year1, mon1, day1, hour1, min1, sec1);
        long startTime = startCalendar.getTimeInMillis();
        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(year2, mon2, day2, hour2, min2, sec2);
        long endTime = endCalendar.getTimeInMillis();
        return time >= startTime && time <= endTime;

    }

    /**
     * 秒转换成时间长度
     *
     * @param time 秒数
     * @return 时间字符串
     */
    public static String secondToTime(int time) {
        String timeStr = null;
        int hour = 0;
        int minute = 0;
        int second = 0;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;

                if (hour > 99) {
                    timeStr = Integer.valueOf(hour).toString() + ":" + unitFormat(minute) + ":" + unitFormat(second);
                } else {
                    timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
                }
            }
        }
        return timeStr;
    }

    /**
     * 为0-9添加十位的0
     *
     * @param i
     * @return
     */
    public static String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }

    /**
     * 将时间戳转化为yy-mm-dd
     *
     * @param timestamp
     * @return
     */
    public static String revertToDate(long timestamp) {
        String date = sSimpleDateFormat.format(new Date(timestamp));
        return date;
    }
}
