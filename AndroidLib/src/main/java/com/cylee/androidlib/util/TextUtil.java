package com.cylee.androidlib.util;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;


public abstract class TextUtil {

    /**
     * 判断字符串是否只有标点符号
     *
     * @return
     */
    public static boolean isPunctuation(String str) {
        str = str.replaceAll("\\W", "").replaceAll("_", "");
        if (TextUtils.isEmpty(str)) {
            return true;
        }
        return false;
    }

    /**
     * 对字符串进行md5加密
     *
     * @param plainText 要加密的字符串
     * @return 加密后的密文
     */
    public static String md5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte b[] = md.digest(plainText.getBytes());
            StringBuilder buf = new StringBuilder();
            for (int offset = 0; offset < b.length; offset++) {
                int i = (b[offset] & 0xFF) | 0x100;
                buf.append(Integer.toHexString(i).substring(1));
            }
            return buf.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 按照UTF8进行urlencode
     *
     * @param content
     * @return
     */
    public static final String encode(String content) {
        try {
            return content == null ? null : URLEncoder.encode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 按照UTF8进行urldecode
     *
     * @param content
     * @return
     */
    public static final String decode(String content) {
        try {
            return content == null ? null : URLDecoder.decode(content, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**********************
     * 时间处理相关逻辑
     ************************/
    public final static long MILLSECORD_PER_SECOND = 1000L;
    public final static long MINITUE_PER_HOUR = 60L;

    public static String getNumber(long num) {
        String numStr = String.valueOf(num);
        int lenght = numStr.length();
        if (lenght < 4) {
            return numStr;
        } else if (lenght < 5) {
//            return numStr.substring(0,lenght-3)+"."+ numStr.substring(lenght-3,lenght-2)+"k";
            return "0." + numStr.substring(0, lenght - 3) + "万";
        } else if (lenght < 6) {
            return numStr.substring(0, lenght - 4) + "." + numStr.substring(lenght - 4, lenght - 3) + "万";
        } else {
            return numStr.substring(0, lenght - 4) + "万";
        }
    }

    /**
     * 返回时间格式为HH:MM
     *
     * @param time
     * @return
     */
    private static String formatTime(Date time) {
        return formatDate("HH:mm", time);
    }

    /**
     * 将内置的long mTime转化为String 单位 ms
     *
     * @return MM-dd
     */
    private static String formatDate(Date time) {
        return formatDate("MM-dd", time);
    }

    private static String formatDate_NIan(Date time) {
        return formatDate("yyyy年MM月dd日", time);
    }

    public static String formatDate(String format, Date time) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(time);
    }

    /*************************
     * 字符串相关处理
     *****************************/
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static boolean isNotEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||
                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||
                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
    }

    /**
     * 过滤emoji 或者 其他非文字类型的字符
     *
     * @param source
     * @return
     */
    public static String filterEmoji(String source) {
        StringBuilder buf = new StringBuilder();
        int len = source.length();
        for (int i = 0; i < len; i++) {
            char codePoint = source.charAt(i);
            if (isNotEmojiCharacter(codePoint)) {
                buf.append(codePoint);
            } else {
                buf.append('□');
            }
        }
        return buf.toString();
    }

    public static String twoBitNumberToChinese(int number) {
        final String[] chineseNum = new String[]{"十", "一", "二", "三", "四", "五", "六", "七", "八", "九"};
        if (number == 10) {
            return chineseNum[0];
        } else if (String.valueOf(number).substring(1).equals("0")) {
            return chineseNum[number / 10] + "十";
        } else {
            if (number < 10) {
                return chineseNum[number];
            } else {
                return chineseNum[0] + chineseNum[Integer.parseInt(String.valueOf(number).substring(1))];
            }
        }
    }

    /**
     * 数字处理，达到或超过四位数后，显示为“*万”，并精确到小数点后一位
     */
    public static String countIntToString(int num) {
        if (num < 10000)
            return Integer.toString(num);
        else {
            int temp = num / 1000;
            double temp2 = temp / 10.0;
            String numToShow = Double.toString(temp2) + "万";
            return numToShow;
        }
    }

    public static String getMessageCountString(int count) {
        if (count > 99) {
            return "99";
        } else {
            return "" + count;
        }
    }

    public static void refreshMessageCountView(TextView view, int count) {

        if (view == null) return;
        if (count > 0) {
            view.setVisibility(View.VISIBLE);
            view.setText(getMessageCountString(count));
        } else {
            view.setVisibility(View.GONE);
        }
    }

    public static long getCheckSum(String input) {
        CheckedInputStream checkedInputStream = null;
        try {
            byte[] bytes = input.getBytes("UTF-8");
            InputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            checkedInputStream = new CheckedInputStream(byteArrayInputStream, new CRC32());
            byte[] buf = new byte[bytes.length];
            while (checkedInputStream.read(buf) >= 0) {
            }
        } catch (IOException e) {
            return 0;
        }
        return checkedInputStream.getChecksum().getValue();
    }

    /**
     * @return True iff the url is an http: url.
     */
    public static boolean isHttpUrl(String url) {
        return (null != url) &&
                (url.length() > 6) &&
                url.substring(0, 7).equalsIgnoreCase("http://");
    }

    /**
     * @return True iff the url is an https: url.
     */
    public static boolean isHttpsUrl(String url) {
        return (null != url) &&
                (url.length() > 7) &&
                url.substring(0, 8).equalsIgnoreCase("https://");
    }

    /**
     * @return True iff the url is a network url.
     */
    public static boolean isNetworkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return isHttpUrl(url) || isHttpsUrl(url);
    }

    public static String getFileSizeStrMB(long sizeByte) {
        float fileMB = sizeByte / 1024f / 1024f;
        return String.format("%.2fMB", fileMB);
    }
}
