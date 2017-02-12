package com.cylee.androidlib.util;

import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;


import com.cylee.androidlib.base.BaseApplication;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by sn on 13-12-3.
 */
public class NetUtils {
    public static synchronized boolean isNetworkConnected() {
        try {
            ConnectivityManager connManager = (ConnectivityManager) BaseApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager != null) {
                NetworkInfo ni = connManager.getActiveNetworkInfo();
                if (ni != null) {
                    return ni.isConnected();
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static synchronized boolean isWifiConnected() {
        try {
            ConnectivityManager connManager = (ConnectivityManager) BaseApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connManager != null) {
                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
                if (networkInfo != null) {
                    int networkInfoType = networkInfo.getType();
                    if (networkInfoType == ConnectivityManager.TYPE_WIFI || networkInfoType == ConnectivityManager.TYPE_ETHERNET) {
                        return networkInfo.isConnected();
                    }
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static Proxy createProxy(String proxy, int port) throws UnknownHostException {
        Pattern p = Pattern.compile("(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})");
        Matcher m = p.matcher(proxy);
        InetAddress add;
        if (m != null && m.find()) {
            byte[] i4 = new byte[4];
            i4[0] = (byte) Integer.parseInt(m.group(1));
            i4[1] = (byte) Integer.parseInt(m.group(2));
            i4[2] = (byte) Integer.parseInt(m.group(3));
            i4[3] = (byte) Integer.parseInt(m.group(4));
            add = InetAddress.getByAddress(proxy, i4);
        } else {
            add = InetAddress.getByName(proxy);
        }
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(add, port));
    }

    /**
     * 获取系统当前的代理
     * @return null if failed
     */
    public static Proxy getProxy() {
        Cursor cursor = null;
        try{
            if (isWifiConnected() || !isNetworkConnected()){
                return null;
            }
            Uri uri = Uri.parse("content://telephony/carriers/preferapn"); // 获取当前正在使用的APN接入点
            cursor = BaseApplication.getApplication().getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                boolean b = cursor.moveToNext(); // 游标一直第一条记录，当前只有一条
                if (b) {
                    String proxyStr = cursor.getString(cursor
                            .getColumnIndex("proxy"));// 有可能报错
                    int port = cursor.getInt(cursor.getColumnIndex("port"));
                    if (proxyStr != null && proxyStr.trim().length() > 0) {
                        if (port == -1) {
                            port = 80;
                        }
                        return createProxy(proxyStr, port);
                    }
                }
            }
            return null;
        }catch (Exception e){
            return null;
        }finally {
            if (cursor !=null) {
                try{
                    cursor.close();
                }catch (Exception e){}
            }
        }
    }
    /**
     * 2 2G网络
     * 3 3G网络
     * 4 4G网络
     * 1 wifi,
     * 0 未知网络
     *
     * @return
     */
    public static int getMobileNetworkClass() {

        try {
            ConnectivityManager connectMgr = (ConnectivityManager) BaseApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectMgr == null) return 0;
            NetworkInfo info = connectMgr.getActiveNetworkInfo();
            if (info == null) return 0;
            if (info.getType() == ConnectivityManager.TYPE_WIFI || info.getType() == ConnectivityManager.TYPE_ETHERNET)
                return 1;
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                NetworkInfo networkInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if(networkInfo == null){
                    return 0;
                }
                int subType = networkInfo.getSubtype();

                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return 2; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return 2; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return 2; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return 3; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return 3; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return 2; // ~ 100 kbps
                    // case TelephonyManager.NETWORK_TYPE_HSDPA:
                    // return true; // ~ 2-14 Mbps
                    // case TelephonyManager.NETWORK_TYPE_HSPA:
                    // return true; // ~ 700-1700 kbps
                    // case TelephonyManager.NETWORK_TYPE_HSUPA:
                    // return true; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return 3; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return 3;
                    /** Current network is HSUPA */
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return 3;
                    /** Current network is HSPA */
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return 3;
                    /** Current network is iDen */
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return 3;
                    /** Current network is EVDO revision B*/
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        return 3;
                    /** Current network is LTE */
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return 4;
                    /** Current network is eHRPD */
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return 4;
                    /** Current network is HSPA+ */
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return 4;
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                        return 0;

                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return 0;
    }
    public static final String OTHER = "other";

    public static Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");

    /**
     * 判断Network具体类型（联通移动wap，电信wap，其他net）
     */
    public static int checkNetworkType(Context mContext) {
        try {
            final ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo mobNetInfoActivity = connectivityManager
                    .getActiveNetworkInfo();
            if (mobNetInfoActivity == null || !mobNetInfoActivity.isAvailable()) {
                // 注意一：
                // NetworkInfo 为空或者不可以用的时候正常情况应该是当前没有可用网络，
                // 但是有些电信机器，仍可以正常联网，
                // 所以当成net网络处理依然尝试连接网络。
                // （然后在socket中捕捉异常，进行二次判断与用户提示）。
                return TYPE_NET_WORK_DISABLED;
            } else {
                // NetworkInfo不为null开始判断是网络类型
                int netType = mobNetInfoActivity.getType();
                if (netType == ConnectivityManager.TYPE_WIFI) {
                    // wifi net处理
                    return TYPE_WIFI;
                } else if (netType == ConnectivityManager.TYPE_MOBILE) {

                    boolean is3G = isFastMobileNetwork(mContext);


                    // 注意三：
                    // 判断是移动联通wap:
                    // 其实还有一种方法通过getString(c.getColumnIndex("proxy")获取代理ip
                    // 来判断接入点，10.0.0.172就是移动联通wap，10.0.0.200就是电信wap，但在
                    // 实际开发中并不是所有机器都能获取到接入点代理信息，例如魅族M9 （2.2）等...
                    // 所以采用getExtraInfo获取接入点名字进行判断

                    String netMode = mobNetInfoActivity.getExtraInfo();

                    if (netMode != null) {
                        // 通过apn名称判断是否是联通和移动wap
                        netMode = netMode.toLowerCase();

                        if (netMode.equals(CMWAP)) {
                            return is3G ? TYPE_CM_WAP : TYPE_CM_WAP_2G;
                        } else if (netMode.equals(CMNET)) {
                            return is3G ? TYPE_CM_NET : TYPE_CM_NET_2G;
                        } else if (netMode.equals(NET_3G)
                                || netMode.equals(UNINET)) {
                            return is3G ? TYPE_CU_NET : TYPE_CU_NET_2G;
                        } else if (netMode.equals(WAP_3G)
                                || netMode.equals(UNIWAP)) {
                            return is3G ? TYPE_CU_WAP : TYPE_CU_WAP_2G;
                        }
                    }
                    Cursor c = null;
                    try {
                        // 注意二：
                        // 判断是否电信wap:
                        // 不要通过getExtraInfo获取接入点名称来判断类型，
                        // 因为通过目前电信多种机型测试发现接入点名称大都为#777或者null，
                        // 电信机器wap接入点中要比移动联通wap接入点多设置一个用户名和密码,
                        // 所以可以通过这个进行判断！

                        c = mContext.getContentResolver().query(
                                PREFERRED_APN_URI, null, null, null, null);
                        if (c != null) {
                            c.moveToFirst();
                            final String user = c.getString(c
                                    .getColumnIndex("user"));
                            if (!TextUtils.isEmpty(user) && user != null) {
                                if (user.startsWith(CTWAP)) {
                                    return is3G ? TYPE_CT_WAP : TYPE_CT_WAP_2G;
                                } else if (user.startsWith(CTNET)) {
                                    return is3G ? TYPE_CT_NET : TYPE_CT_NET_2G;
                                }
                            }
                        }
                    } catch (Exception e) {//部分手机执行会有权限问题，声明申请了也不成
                        e.printStackTrace();
                    }finally {
                        if(c != null){
                            c.close();
                        }
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return TYPE_OTHER;
        }

        return TYPE_OTHER;

    }
    public static boolean isFastMobileNetwork(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return true; // ~ 10+ Mbps
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    return false;
                default:
                    return false;

            }
        } catch (Exception e) {
            return false;
        }
    }

    public static String getFastMobileNetwork(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_1xRTT"; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_CDMA"; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_EDGE"; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_EVDO_0"; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_EVDO_A"; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_GPRS"; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_HSDPA"; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_HSPA"; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_HSUPA"; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_UMTS"; // ~ 400-7000 kbps
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_EHRPD"; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_EVDO_B"; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_HSPAP"; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_IDEN"; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_LTE"; // ~ 10+ Mbps

                default:
                    return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_UNKNOWN";

            }
        } catch (Exception e) {
            return isWifiConnected() ? "NETWORK_TYPE_WIFI" : "NETWORK_TYPE_UNKNOWN";
        }
    }

    /**
     * 是不是低速网络
     */
    public static boolean isLowMobileNetwork(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (!isNetworkConnected()) {
                return true;
            } else if (isWifiConnected()) {
                return false;
            } else {
                switch (telephonyManager.getNetworkType()) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return true;// "NETWORK_TYPE_1xRTT"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return true;// "NETWORK_TYPE_CDMA"; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return true;//"NETWORK_TYPE_EDGE"; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return true;// "NETWORK_TYPE_EVDO_0"; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return true;//"NETWORK_TYPE_EVDO_A"; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return true;//"NETWORK_TYPE_GPRS"; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return false;//"NETWORK_TYPE_HSDPA"; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return false;// "NETWORK_TYPE_HSPA"; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return false;//"NETWORK_TYPE_HSUPA"; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return false;// "NETWORK_TYPE_UMTS"; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD:
                        return false;// "NETWORK_TYPE_EHRPD"; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        return false;//"NETWORK_TYPE_EVDO_B"; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP:
                        return false;// "NETWORK_TYPE_HSPAP"; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN:
                        return true;// "NETWORK_TYPE_IDEN"; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE:
                        return false;//"NETWORK_TYPE_LTE"; // ~ 10+ Mbps


                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    /**
     * 没有网络
     */
    public static final int TYPE_NET_WORK_DISABLED = 0;

    /**
     * wifi网络
     */
    public static final int TYPE_WIFI = 4;
    public static final int TYPE_CT_WAP = 5;
    public static final int TYPE_CT_NET = 6;
    public static final int TYPE_CT_WAP_2G = 7;
    public static final int TYPE_CT_NET_2G = 8;

    public static final int TYPE_CM_WAP = 9;
    public static final int TYPE_CM_NET = 10;
    public static final int TYPE_CM_WAP_2G = 11;
    public static final int TYPE_CM_NET_2G = 12;

    public static final int TYPE_CU_WAP = 13;
    public static final int TYPE_CU_NET = 14;
    public static final int TYPE_CU_WAP_2G = 15;
    public static final int TYPE_CU_NET_2G = 16;

    public static final int TYPE_OTHER = 17;
    public static final String CTWAP = "ctwap";
    public static final String CTNET = "ctnet";
    public static final String CMWAP = "cmwap";
    public static final String CMNET = "cmnet";
    public static final String NET_3G = "3gnet";
    public static final String WAP_3G = "3gwap";
    public static final String UNIWAP = "uniwap";
    public static final String UNINET = "uninet";
    public static String getNetTypeInfo(Context context) {
        String rs = "unknow";
        try {
            long start = System.currentTimeMillis();
            int checkNetworkType = checkNetworkType(context);
            switch (checkNetworkType) {
                case TYPE_WIFI:
                    rs = "wifi";
                    break;
                case TYPE_NET_WORK_DISABLED:
                    rs = "network disabled";

                    break;
                case TYPE_CT_WAP:
                    rs = "ctwap";

                    break;
                case TYPE_CT_WAP_2G:
                    rs = "ctwap_2g";

                    break;
                case TYPE_CT_NET:
                    rs = "ctnet";

                    break;
                case TYPE_CT_NET_2G:
                    rs = "ctnet_2g";

                    break;
                case TYPE_CM_WAP:
                    rs = "cmwap";

                    break;
                case TYPE_CM_WAP_2G:
                    rs = "cmwap_2g";

                    break;
                case TYPE_CM_NET:
                    rs = "cmnet";

                    break;
                case TYPE_CM_NET_2G:
                    rs = "cmnet_2g";

                    break;
                case TYPE_CU_NET:
                    rs = "cunet";

                    break;
                case TYPE_CU_NET_2G:
                    rs = "cunet_2g";

                    break;
                case TYPE_CU_WAP:
                    rs = "cuwap";

                    break;
                case TYPE_CU_WAP_2G:
                    rs = "cuwap_2g";

                    break;
                case TYPE_OTHER:
                    rs = "other";

                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * Telecom service providers获取手机服务商信息
     * 需要加入权限<uses-permission android:name="android.permission.READ_PHONE_STATE"/>
     */
    public static String getProvidersName(Context context) {
        String ProvidersName = "未知";

        try {
            TelephonyManager telManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String operator = telManager.getSimOperator();

            if (operator != null) {
                if (operator.equals("46000") || operator.equals("46002") || operator.equals("46007")) {
                    ProvidersName = "中国移动";
                } else if (operator.equals("46001")) {
                    ProvidersName = "中国联通";
                } else if (operator.equals("46003")) {
                    ProvidersName = "中国电信";
                }
            }
            // 返回唯一的用户ID;就是这张卡的编号神马的

            String IMSI = telManager.getSubscriberId();

            // IMSI号前面3位460是国家，紧接着后面2位00 02是中国移动，01是中国联通，03是中国电信。


            if (IMSI.startsWith("46000") || IMSI.startsWith("46002") || IMSI.startsWith("46007")) {

                ProvidersName = "中国移动";

            } else if (IMSI.startsWith("46001")) {

                ProvidersName = "中国联通";

            } else if (IMSI.startsWith("46003")) {

                ProvidersName = "中国电信";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ProvidersName;
    }

    /**
     * 检查android联网状态
     */
    public static boolean checkNetworkState(final Context context) {
        boolean flag = false;
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager != null) {
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info != null) flag = info.isAvailable();
        }
        return flag;
    }

    public static boolean isWifiOn(final Context context) {
        boolean flag = false;
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            flag = true;
        }
        return flag;
    }

    public static NetworkInfo getActiveNetworkInfo(Context context) {
        ConnectivityManager connectivity =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        return networkInfo;
    }

    /**
     * getWifiAddress:获取wifimac地址
     *
     * @param @param context
     * @return String
     * @throws
     * @since 9:56:09 AM
     */
    public static String getWifiAddress(Context context) {
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = manager.getConnectionInfo();
        String address = info.getMacAddress();
        return address;
    }

    public static String getDefaultAPN(Context context) {
        final Uri PREFERRED_APN_URI = Uri.parse("content://telephony/carriers/preferapn");
        Cursor c = context.getContentResolver().query(PREFERRED_APN_URI, null, null, null, null);
        c.moveToFirst();
        int index = c.getColumnIndex("_id");
        index = c.getColumnIndex("appName");
        String name = c.getString(index);
        return name;
    }
}
