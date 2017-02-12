/**
 * 创建人：SongZhiyong
 * 创建时间：2013-1-3
 */
package com.cylee.androidlib.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;

/**
 * 时间日期解析格式化
 *
 * @author SongZhiyong
 */
public class TimeUtils {
  /** UTC时间样式 */
  public static final String UTC_TIME_PATTERN = "E MMM dd HH:mm:ss ZZZZ yyyy";
  /** 普通时间样式 */
  public static final String NORMAL_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
  public static final String TIME_PATTERN = "HH:mm";

  public static final String ISO_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";
  public static final String ISO_TIME_PATTERN_DEPRACATED = "yyyy-MM-dd'T'HH:mm:ss.ssssss";

  /**
   * 根据字符串及样式解析出时间Date
   *
   * @param pattern 样式
   * @param time 时间
   * @return date 解析完成返回的日期
   * @throws java.text.ParseException 解析错误
   */
  public static Date parse(String pattern, String time) throws ParseException {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
    try {
      return sdf.parse(time);
    } catch (ParseException e) {
      Date date = null;
      try {
        date = parse(ISO_TIME_PATTERN_DEPRACATED, time);
      } catch (ParseException e1) {
      }
      return date;
    }
  }

  /**
   * 根据日期和样式格式化时间
   *
   * @param pattern 样式
   * @param date 输入日期
   * @return 格式化之后的时间字符串
   */
  public static String format(String pattern, Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
    return sdf.format(date);
  }

  public static String format(String pattern, long timeMills) {
    return format(pattern, new Date(timeMills));
  }

  public static String formatNormal(long timeMills) {
    return format(NORMAL_TIME_PATTERN, new Date(timeMills));
  }

  public static String formatToUTC(String pattern, Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
    sdf.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
    return sdf.format(date);
  }

  /**
   * @param mss 要转换的毫秒数
   * @return 该毫秒数转换为 * days * hours * minutes * seconds 后的格式
   * @author fy.zhang
   */
  public static String formatDuring(long mss) {
    long hours = mss / (1000 * 60 * 60);
    long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
    return hours + "hrs " + minutes + "mins";
  }
}
